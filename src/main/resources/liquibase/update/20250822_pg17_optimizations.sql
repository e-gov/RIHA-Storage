-- PostgreSQL 17 Performance Optimizations for RIHA
-- Migration: 20250822_pg17_optimizations.sql
-- Date: 2025-08-22
-- Description: Optimize queries for PostgreSQL 17 features and improved performance

-- =======================================================================================
-- PHASE 1: BRIN INDEXES FOR TIME-SERIES DATA
-- =======================================================================================

-- BRIN indexes are much smaller and faster for append-only timestamp columns in PG17
-- These tables are append-heavy with timestamp-based queries

-- Comment table BRIN indexes
CREATE INDEX IF NOT EXISTS idx_comment_creation_date_brin 
ON riha.comment USING brin (creation_date)
WITH (pages_per_range = 128);

CREATE INDEX IF NOT EXISTS idx_comment_modified_date_brin 
ON riha.comment USING brin (modified_date)
WITH (pages_per_range = 128);

-- Main resource table BRIN indexes  
CREATE INDEX IF NOT EXISTS idx_main_resource_creation_date_brin 
ON riha.main_resource USING brin (creation_date)
WITH (pages_per_range = 128);

CREATE INDEX IF NOT EXISTS idx_main_resource_modified_date_brin 
ON riha.main_resource USING brin (modified_date)
WITH (pages_per_range = 128);

-- =======================================================================================
-- PHASE 2: COMPOSITE INDEXES FOR IMPROVED SORTING (PG17 INCREMENTAL SORT)
-- =======================================================================================

-- First, create IMMUTABLE helper functions for JSON extraction
-- This is required for functional indexes in PostgreSQL

CREATE OR REPLACE FUNCTION riha.extract_uuid_immutable(json_data jsonb)
RETURNS text
LANGUAGE SQL
IMMUTABLE
STRICT
PARALLEL SAFE
AS $$
  SELECT json_data #>> '{uuid}';
$$;

CREATE OR REPLACE FUNCTION riha.extract_update_timestamp_immutable(json_data jsonb)
RETURNS timestamp with time zone
LANGUAGE SQL
IMMUTABLE
STRICT
PARALLEL SAFE
AS $$
  SELECT (json_data #>> '{meta,update_timestamp}')::timestamp with time zone;
$$;

-- Now create the composite index using the IMMUTABLE functions
-- PG17 can use incremental sort with this composite index
CREATE INDEX IF NOT EXISTS idx_main_resource_uuid_timestamp_id 
ON riha.main_resource (
  riha.extract_uuid_immutable(json_content), 
  riha.extract_update_timestamp_immutable(json_content) DESC NULLS LAST,
  main_resource_id DESC
);

-- Optimize comment filtering and sorting for issues
-- This supports the exact query pattern in comment_type_issue_view
CREATE INDEX IF NOT EXISTS idx_comment_issue_comprehensive_v17
ON riha.comment (type, status, sub_type, creation_date DESC, infosystem_uuid)
WHERE type = 'ISSUE';

-- =======================================================================================
-- PHASE 3: JSON OPTIMIZATION WITH GIN INDEXES
-- =======================================================================================

-- GIN indexes for commonly accessed JSON paths
-- PG17 has significant improvements for GIN index performance
-- Using path expressions that are compatible with PostgreSQL's GIN operator classes

CREATE INDEX IF NOT EXISTS idx_main_resource_json_content_gin
ON riha.main_resource USING gin (json_content);

-- Specific path-based indexes using jsonb_path_ops for better performance
CREATE INDEX IF NOT EXISTS idx_main_resource_json_uuid_gin
ON riha.main_resource USING gin ((json_content -> 'uuid') jsonb_path_ops);

CREATE INDEX IF NOT EXISTS idx_main_resource_json_meta_gin
ON riha.main_resource USING gin ((json_content -> 'meta') jsonb_path_ops);

-- Index for topics array queries (used in main_resource_view)
CREATE INDEX IF NOT EXISTS idx_main_resource_json_topics_gin
ON riha.main_resource USING gin ((json_content -> 'topics') jsonb_path_ops);

-- =======================================================================================
-- PHASE 4: OPTIMIZED VIEW WITH CTE MATERIALIZATION
-- =======================================================================================

-- Update main_resource_view to use PG17's improved CTE handling
-- Note: Using conditional aggregation instead of FILTER clause for broader PostgreSQL compatibility
-- Note: Using CREATE OR REPLACE to maintain dependent views
CREATE OR REPLACE VIEW riha.main_resource_view AS
WITH comment_aggregates AS MATERIALIZED (
  -- Pre-aggregate comment data to reduce complex JOINs
  -- PG17's improved CTE materialization makes this efficient
  SELECT 
    c.infosystem_uuid,
    -- Use conditional aggregation instead of multiple subqueries
    MAX(CASE WHEN c.sub_type = 'ESTABLISHMENT_REQUEST' AND c.status = 'CLOSED' AND c.resolution_type = 'POSITIVE' 
             THEN c.modified_date END) as last_positive_establishment_request_date,
    MAX(CASE WHEN c.sub_type = 'TAKE_INTO_USE_REQUEST' AND c.status = 'CLOSED' AND c.resolution_type = 'POSITIVE' 
             THEN c.modified_date END) as last_positive_take_into_use_request_date,
    MAX(CASE WHEN c.sub_type = 'FINALIZATION_REQUEST' AND c.status = 'CLOSED' AND c.resolution_type = 'POSITIVE' 
             THEN c.modified_date END) as last_positive_finalization_request_date,
    -- Get the most recent approval request type using a simpler subquery approach
    -- This avoids aggregation function complexity and NULL handling issues
    (SELECT c2.sub_type 
     FROM riha.comment c2 
     WHERE c2.infosystem_uuid = c.infosystem_uuid
       AND c2.type = 'ISSUE'
       AND c2.status = 'CLOSED' 
       AND c2.resolution_type = 'POSITIVE'
       AND c2.sub_type IN ('ESTABLISHMENT_REQUEST', 'TAKE_INTO_USE_REQUEST', 'FINALIZATION_REQUEST')
     ORDER BY c2.modified_date DESC 
     LIMIT 1)::varchar(150) as last_positive_approval_request_type,
    MAX(CASE WHEN c.sub_type IN ('ESTABLISHMENT_REQUEST', 'TAKE_INTO_USE_REQUEST', 'FINALIZATION_REQUEST') 
             AND c.status = 'CLOSED' AND c.resolution_type = 'POSITIVE' 
             THEN c.modified_date END) as last_positive_approval_request_date
  FROM riha.comment c
  WHERE c.type = 'ISSUE'
    AND c.status = 'CLOSED' 
    AND c.resolution_type = 'POSITIVE'
    AND c.sub_type IN ('ESTABLISHMENT_REQUEST', 'TAKE_INTO_USE_REQUEST', 'FINALIZATION_REQUEST')
  GROUP BY c.infosystem_uuid
),
used_system_relations AS MATERIALIZED (
  -- Pre-aggregate used system relations
  SELECT 
    mrr.infosystem_uuid,
    (count(*) > 0) AS has_used_system_type_relations
  FROM riha.main_resource_relation mrr
  WHERE mrr.type = 'USED_SYSTEM'
  GROUP BY mrr.infosystem_uuid
)
SELECT DISTINCT ON (riha.extract_uuid_immutable(main_resource.json_content))
  main_resource.main_resource_id,
  main_resource.uri,
  main_resource.name,
  main_resource.owner,
  main_resource.short_name,
  main_resource.version,
  main_resource.json_content,
  main_resource.parent_uri,
  main_resource.main_resource_parent_id,
  main_resource.kind,
  main_resource.state,
  main_resource.start_date,
  main_resource.end_date,
  main_resource.creator,
  main_resource.modifier,
  main_resource.creation_date,
  main_resource.modified_date,
  main_resource.old_id,
  main_resource.field_name,
  main_resource.kind_id,
  main_resource.main_resource_template_id,
  main_resource.search_content,
  ((main_resource.json_content #>> '{meta,creation_timestamp}'::text[]))::timestamp with time zone AS j_creation_timestamp,
  riha.extract_update_timestamp_immutable(main_resource.json_content) AS j_update_timestamp,
  
  -- Improved logic for approval request type determination
  CASE WHEN
    COALESCE(jsonb_exists_any(main_resource.json_content -> 'topics', 
             array['x-tee alamsüsteem','X-tee alamsüsteem', 'X-TEE ALAMSÜSTEEM', 
                   'asutusesiseseks kasutamiseks', 'standardlahendus-dhs']), false)
    OR COALESCE(usr.has_used_system_type_relations, false)
    THEN 'AUTOMATICALLY_REGISTERED'::varchar(150)
    ELSE ca.last_positive_approval_request_type::varchar(150)
  END as last_positive_approval_request_type,

  ca.last_positive_approval_request_date,
  ca.last_positive_establishment_request_date,
  ca.last_positive_take_into_use_request_date,
  ca.last_positive_finalization_request_date,
  COALESCE(usr.has_used_system_type_relations, false) AS has_used_system_type_relations

FROM riha.main_resource main_resource
LEFT JOIN comment_aggregates ca ON riha.extract_uuid_immutable(main_resource.json_content)::uuid = ca.infosystem_uuid
LEFT JOIN used_system_relations usr ON riha.extract_uuid_immutable(main_resource.json_content)::uuid = usr.infosystem_uuid

ORDER BY 
  riha.extract_uuid_immutable(main_resource.json_content), 
  riha.extract_update_timestamp_immutable(main_resource.json_content) DESC NULLS LAST, 
  main_resource.main_resource_id DESC;

-- =======================================================================================
-- PHASE 5: ANALYZE TABLES FOR OPTIMIZER STATISTICS
-- =======================================================================================

-- Update table statistics for the query planner
ANALYZE riha.comment;
ANALYZE riha.main_resource;
ANALYZE riha.main_resource_relation;

-- =======================================================================================
-- PHASE 6: ADDITIONAL OPTIMIZATIONS FOR SPECIFIC QUERY PATTERNS
-- =======================================================================================

-- Optimize the NamesDAO queries that use IN clauses
-- These will benefit significantly from PG17's improved IN clause handling
CREATE INDEX IF NOT EXISTS idx_main_resource_uri_name
ON riha.main_resource (uri, name)
WHERE uri IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_data_object_uri_name  
ON riha.data_object (uri, name)
WHERE uri IS NOT NULL;

-- Index for comment parent-child relationships
CREATE INDEX IF NOT EXISTS idx_comment_parent_child_v17
ON riha.comment (comment_parent_id, creation_date DESC, type)
WHERE comment_parent_id IS NOT NULL;

-- =======================================================================================
-- PERFORMANCE MONITORING SETUP
-- =======================================================================================

-- Enable pg_stat_statements if not already enabled (requires restart)
-- CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Create a view to monitor RIHA-specific query performance
CREATE OR REPLACE VIEW riha.pg17_query_performance AS
SELECT 
  substr(query, 1, 100) as query_snippet,
  calls,
  total_exec_time,
  mean_exec_time,
  min_exec_time,
  max_exec_time,
  stddev_exec_time,
  rows
FROM pg_stat_statements 
WHERE query ILIKE '%riha%'
  AND calls > 5  -- Only show queries called more than 5 times
ORDER BY mean_exec_time DESC;

-- Create a view to monitor index usage
CREATE OR REPLACE VIEW riha.pg17_index_usage AS
SELECT 
  schemaname,
  tablename, 
  indexname,
  idx_scan,
  idx_tup_read,
  idx_tup_fetch,
  pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes 
WHERE schemaname = 'riha'
ORDER BY idx_scan DESC;

-- Log completion
DO $$
BEGIN
  RAISE NOTICE 'PostgreSQL 17 optimizations completed successfully';
  RAISE NOTICE 'BRIN indexes created for timestamp columns';
  RAISE NOTICE 'Composite indexes created for sorting optimization';
  RAISE NOTICE 'GIN indexes created for JSON queries';
  RAISE NOTICE 'Main resource view optimized with materialized CTEs';
  RAISE NOTICE 'Performance monitoring views created';
END $$;
