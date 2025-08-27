-- Optimization for data object search performance (RIHA USER VERSION)
-- Author: Kristjan Kruus
-- Date: 2025-08-27
-- This version is designed to be run as the riha user

-- Check current user
SELECT current_user as running_as;

-- Ensure required extensions are available (only if not exists)
-- Note: This might require superuser privileges, so it's conditional
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_trgm') THEN
        -- Try to create extension, but don't fail if we don't have permission
        BEGIN
            CREATE EXTENSION pg_trgm;
        EXCEPTION WHEN insufficient_privilege THEN
            RAISE NOTICE 'pg_trgm extension not available - will use basic indexes instead';
        END;
    END IF;
END $$;

-- Create indexes on the underlying tables to improve join performance
-- Use IF NOT EXISTS to avoid conflicts with existing indexes

-- Index on file_resource.large_object_id for faster joins
CREATE INDEX IF NOT EXISTS idx_file_resource_large_object_id 
ON riha.file_resource(large_object_id);

-- Index on registered_file.file_resource_uuid for faster joins
CREATE INDEX IF NOT EXISTS idx_registered_file_file_resource_uuid 
ON riha.registered_file(file_resource_uuid);

-- Index on registered_file.main_resource_uuid for faster joins  
CREATE INDEX IF NOT EXISTS idx_registered_file_main_resource_uuid
ON riha.registered_file(main_resource_uuid);

-- Index on main_resource.json_content for JSON operations
-- Using B-tree for exact matches on UUID
CREATE INDEX IF NOT EXISTS idx_main_resource_json_uuid 
ON riha.main_resource ((json_content #>> '{uuid}'));

-- Index on main_resource.json_content for short_name lookups
-- Using B-tree for exact matches and pattern searches
CREATE INDEX IF NOT EXISTS idx_main_resource_json_short_name 
ON riha.main_resource ((json_content #>> '{short_name}'));

-- Index on large_object.search_content for JSON operations
CREATE INDEX IF NOT EXISTS idx_large_object_search_content 
ON riha.large_object USING GIN (search_content);

-- Create a materialized view for better performance on data object searches
-- This will pre-compute the expensive JSON operations and joins
-- Drop views in correct order to handle dependencies
DROP VIEW IF EXISTS riha.data_object_search_view_optimized;
DROP MATERIALIZED VIEW IF EXISTS riha.data_object_search_materialized CASCADE;

CREATE MATERIALIZED VIEW riha.data_object_search_materialized AS
SELECT 
    raw_data."Infosüsteem" as infosystem,
    raw_data."Andmeobjekti nimi" as andmeobjekti_nimi,
    raw_data."Kommentaar" as kommentaar,
    raw_data."Vanemobjekt 1" as vanemobjekt,
    CASE 
        WHEN raw_data."EIA" ILIKE 'jah' THEN 'jah, eriliigiline'
        WHEN raw_data."DIA" ILIKE 'jah' THEN 'jah, delikaatne'
        WHEN raw_data."IA" ILIKE 'jah' THEN 'jah'
        WHEN raw_data."IA" ILIKE 'ei' THEN 'ei'
        WHEN (raw_data."IA" IS NOT NULL AND raw_data."IA" NOT ILIKE 'ei' AND raw_data."IA" NOT ILIKE 'jah') THEN 'muu'
        ELSE NULL
    END as personal_data,
    raw_data."EIA" ILIKE 'jah' as eia,
    raw_data."DIA" ILIKE 'jah' as dia,
    raw_data."AV" ILIKE 'jah' as av,
    raw_data."IA" ILIKE 'jah' as ia,
    raw_data."PA" ILIKE 'jah' as pa,
    mr.json_short_name as short_name,
    mr.search_name,
    f.uuid as file_uuid,
    md5(concat(raw_data."AV", raw_data."Vanemobjekt 1", raw_data."Andmeobjekti nimi", raw_data."DIA", raw_data."EIA", raw_data."IA", raw_data."Kommentaar", raw_data."PA", raw_data."Vanemobjekt 1", mr.json_short_name)) as id,
    COALESCE(raw_data.search_text, '') || COALESCE(mr.search_name, '') as search_text,
    -- Add computed columns for faster filtering
    LOWER(COALESCE(raw_data."Andmeobjekti nimi", '')) as andmeobjekti_nimi_lower,
    LOWER(COALESCE(raw_data."Kommentaar", '')) as kommentaar_lower,
    LOWER(COALESCE(raw_data."Vanemobjekt 1", '')) as vanemobjekt_lower,
    LOWER(COALESCE(mr.search_name, '')) as search_name_lower,
    LOWER(COALESCE(raw_data.search_text, '') || COALESCE(mr.search_name, '')) as search_text_lower,
    -- Add creation timestamp for refresh tracking
    NOW() as materialized_at
FROM (
    SELECT 
        records.*,
        lo.id as lo_id,
        COALESCE(records."Infosüsteem", '') || COALESCE(records."Andmeobjekti nimi", '') || COALESCE(records."Kommentaar", '') || COALESCE(records."Vanemobjekt 1", '') as search_text
    FROM riha.large_object lo,
         LATERAL jsonb_to_recordset(lo.search_content -> 'records') 
         AS records("AV" text, "Infosüsteem" text, "Andmeobjekti nimi" text, "DIA" text, "EIA" text,
                   "IA" text, "Kommentaar" text, "PA" text, "Vanemobjekt 1" text)
) raw_data
INNER JOIN riha.file_resource f ON f.large_object_id = raw_data.lo_id
INNER JOIN riha.registered_file rf ON rf.file_resource_uuid = f.uuid
INNER JOIN riha.main_resource_current_version mr ON rf.main_resource_uuid::TEXT = mr.json_uuid;

-- Create indexes on the materialized view for fast searching
-- Check if pg_trgm is available and use appropriate indexing strategy

DO $$
BEGIN
    -- Try to create trigram indexes if extension is available
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_trgm') THEN
        -- Primary text search index using trigram
        CREATE INDEX IF NOT EXISTS idx_data_object_search_text_lower 
        ON riha.data_object_search_materialized USING GIN (search_text_lower gin_trgm_ops);

        -- Individual field indexes for specific searches using trigram
        CREATE INDEX IF NOT EXISTS idx_data_object_andmeobjekti_nimi_lower 
        ON riha.data_object_search_materialized USING GIN (andmeobjekti_nimi_lower gin_trgm_ops);

        CREATE INDEX IF NOT EXISTS idx_data_object_kommentaar_lower 
        ON riha.data_object_search_materialized USING GIN (kommentaar_lower gin_trgm_ops);

        CREATE INDEX IF NOT EXISTS idx_data_object_vanemobjekt_lower 
        ON riha.data_object_search_materialized USING GIN (vanemobjekt_lower gin_trgm_ops);

        CREATE INDEX IF NOT EXISTS idx_data_object_search_name_lower 
        ON riha.data_object_search_materialized USING GIN (search_name_lower gin_trgm_ops);
        
        RAISE NOTICE 'Created trigram indexes for optimized text search';
    ELSE
        -- Fallback to B-tree indexes for basic pattern matching
        CREATE INDEX IF NOT EXISTS idx_data_object_search_text_lower 
        ON riha.data_object_search_materialized (search_text_lower);

        CREATE INDEX IF NOT EXISTS idx_data_object_andmeobjekti_nimi_lower 
        ON riha.data_object_search_materialized (andmeobjekti_nimi_lower);

        CREATE INDEX IF NOT EXISTS idx_data_object_kommentaar_lower 
        ON riha.data_object_search_materialized (kommentaar_lower);

        CREATE INDEX IF NOT EXISTS idx_data_object_vanemobjekt_lower 
        ON riha.data_object_search_materialized (vanemobjekt_lower);

        CREATE INDEX IF NOT EXISTS idx_data_object_search_name_lower 
        ON riha.data_object_search_materialized (search_name_lower);
        
        RAISE NOTICE 'Created basic B-tree indexes (pg_trgm not available)';
    END IF;
END $$;

-- Btree indexes for exact matches and sorting
CREATE INDEX IF NOT EXISTS idx_data_object_personal_data 
ON riha.data_object_search_materialized(personal_data);

CREATE INDEX IF NOT EXISTS idx_data_object_short_name 
ON riha.data_object_search_materialized(short_name);

CREATE INDEX IF NOT EXISTS idx_data_object_file_uuid 
ON riha.data_object_search_materialized(file_uuid);

-- Composite index for common filter combinations
CREATE INDEX IF NOT EXISTS idx_data_object_composite 
ON riha.data_object_search_materialized(personal_data, short_name);

-- Partial indexes for boolean flags
CREATE INDEX IF NOT EXISTS idx_data_object_eia_true 
ON riha.data_object_search_materialized(eia) WHERE eia = true;

CREATE INDEX IF NOT EXISTS idx_data_object_dia_true 
ON riha.data_object_search_materialized(dia) WHERE dia = true;

CREATE INDEX IF NOT EXISTS idx_data_object_ia_true 
ON riha.data_object_search_materialized(ia) WHERE ia = true;

-- Create a function to refresh the materialized view
CREATE OR REPLACE FUNCTION riha.refresh_data_object_search_materialized()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW riha.data_object_search_materialized;
END;
$$ LANGUAGE plpgsql;

-- Initial refresh of the materialized view
SELECT riha.refresh_data_object_search_materialized();

-- Create a trigger function to automatically refresh the materialized view when underlying data changes
CREATE OR REPLACE FUNCTION riha.auto_refresh_data_object_search()
RETURNS trigger AS $$
DECLARE
    last_refresh timestamp;
    refresh_interval interval := '30 seconds'; -- Minimum time between refreshes
BEGIN
    -- Check when view was last refreshed
    SELECT MAX(materialized_at) INTO last_refresh 
    FROM riha.data_object_search_materialized LIMIT 1;
    
    -- Only refresh if enough time has passed since last refresh
    -- This prevents excessive refreshes during bulk operations
    IF last_refresh IS NULL OR (NOW() - last_refresh) > refresh_interval THEN
        REFRESH MATERIALIZED VIEW riha.data_object_search_materialized;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create triggers on tables that affect the view
DROP TRIGGER IF EXISTS trigger_refresh_data_object_search_large_object ON riha.large_object;
CREATE TRIGGER trigger_refresh_data_object_search_large_object
    AFTER INSERT OR UPDATE OR DELETE ON riha.large_object
    FOR EACH STATEMENT EXECUTE FUNCTION riha.auto_refresh_data_object_search();

DROP TRIGGER IF EXISTS trigger_refresh_data_object_search_file_resource ON riha.file_resource;
CREATE TRIGGER trigger_refresh_data_object_search_file_resource
    AFTER INSERT OR UPDATE OR DELETE ON riha.file_resource
    FOR EACH STATEMENT EXECUTE FUNCTION riha.auto_refresh_data_object_search();

DROP TRIGGER IF EXISTS trigger_refresh_data_object_search_registered_file ON riha.registered_file;
CREATE TRIGGER trigger_refresh_data_object_search_registered_file
    AFTER INSERT OR UPDATE OR DELETE ON riha.registered_file
    FOR EACH STATEMENT EXECUTE FUNCTION riha.auto_refresh_data_object_search();

DROP TRIGGER IF EXISTS trigger_refresh_data_object_search_main_resource ON riha.main_resource;
CREATE TRIGGER trigger_refresh_data_object_search_main_resource
    AFTER INSERT OR UPDATE OR DELETE ON riha.main_resource
    FOR EACH STATEMENT EXECUTE FUNCTION riha.auto_refresh_data_object_search();

-- Create an improved version of the original view that uses the materialized view
-- This maintains backward compatibility while providing better performance
CREATE OR REPLACE VIEW riha.data_object_search_view_optimized AS
SELECT 
    infosystem,
    andmeobjekti_nimi,
    kommentaar,
    vanemobjekt,
    personal_data,
    eia,
    dia,
    av,
    ia,
    pa,
    short_name,
    search_name,
    file_uuid,
    id,
    search_text
FROM riha.data_object_search_materialized;

-- Add comments explaining the optimization
COMMENT ON MATERIALIZED VIEW riha.data_object_search_materialized IS 
'Optimized materialized view for data object search. Refreshed automatically when underlying data changes. 
Provides significant performance improvement for search operations by pre-computing expensive JSON operations and joins.
Created by riha user.';

COMMENT ON VIEW riha.data_object_search_view_optimized IS 
'Optimized version of data_object_search_view that uses the materialized view for better performance.';

COMMENT ON FUNCTION riha.refresh_data_object_search_materialized() IS 
'Function to manually refresh the data object search materialized view. Owned by riha user.';

COMMENT ON FUNCTION riha.auto_refresh_data_object_search() IS 
'Trigger function to notify when data object search materialized view needs refreshing. Owned by riha user.';

-- Final status
SELECT 
    current_user as created_by,
    'Data object search optimization completed successfully' as status,
    (SELECT COUNT(*) FROM riha.data_object_search_materialized) as records_count;
