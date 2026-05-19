-- Additional performance optimization indexes for data objects search and InfoSystem operations
-- These address the slow performance in production with PostgreSQL 14 and large datasets

-- =============================================================================
-- LARGE_OBJECT table indexes (for data_object_search_view performance)
-- =============================================================================

-- Index for large_object.id used in data_object_search_view JOINs
CREATE INDEX IF NOT EXISTS idx_large_object_id
ON riha.large_object USING btree (id);

-- GIN index for JSON search_content operations (used in data_object_search_view)
-- This handles the jsonb_to_recordset operations efficiently
CREATE INDEX IF NOT EXISTS idx_large_object_search_content_gin
ON riha.large_object USING gin (search_content);

-- =============================================================================
-- FILE_RESOURCE table indexes (for data_object_search_view JOINs)
-- =============================================================================

-- Index for file_resource.large_object_id JOIN
CREATE INDEX IF NOT EXISTS idx_file_resource_large_object_id
ON riha.file_resource USING btree (large_object_id);

-- Index for file_resource.uuid (used in registered_file JOIN)
CREATE INDEX IF NOT EXISTS idx_file_resource_uuid
ON riha.file_resource USING btree (uuid);

-- =============================================================================
-- REGISTERED_FILE table indexes (for data_object_search_view JOINs)
-- =============================================================================

-- Index for registered_file.file_resource_uuid JOIN
CREATE INDEX IF NOT EXISTS idx_registered_file_file_resource_uuid
ON riha.registered_file USING btree (file_resource_uuid);

-- Index for registered_file.main_resource_uuid JOIN
CREATE INDEX IF NOT EXISTS idx_registered_file_main_resource_uuid
ON riha.registered_file USING btree (main_resource_uuid);

-- =============================================================================
-- MAIN_RESOURCE_CURRENT_VERSION table/view indexes
-- =============================================================================

-- Index for main_resource_current_version.json_uuid (used in data_object_search_view)
CREATE INDEX IF NOT EXISTS idx_main_resource_current_version_json_uuid
ON riha.main_resource USING btree ((json_content ->> 'uuid'))
WHERE (json_content ->> 'uuid') IS NOT NULL;

-- Index for main_resource_current_version search operations
-- This handles search_name and json_short_name queries
CREATE INDEX IF NOT EXISTS idx_main_resource_search_name
ON riha.main_resource USING btree (
    (json_content ->> 'short_name'),
    (json_content ->> 'name')
);

-- =============================================================================
-- MAIN_RESOURCE table additional indexes for InfoSystem performance
-- =============================================================================

-- Enhanced search_content index for PostgreSQL 14 compatibility
-- Using trigram index with explicit operator class
CREATE INDEX IF NOT EXISTS idx_main_resource_search_content_pg14
ON riha.main_resource USING gin (search_content gin_trgm_ops)
WHERE search_content IS NOT NULL AND length(search_content) > 0;

-- Index for owner.code extraction (commonly filtered in production)
CREATE INDEX IF NOT EXISTS idx_main_resource_owner_code_filtered
ON riha.main_resource USING btree ((json_content #>> '{owner,code}'))
WHERE (json_content #>> '{owner,code}') IS NOT NULL;

-- Index for owner.name extraction (commonly filtered in production)
CREATE INDEX IF NOT EXISTS idx_main_resource_owner_name_filtered
ON riha.main_resource USING btree ((json_content #>> '{owner,name}'))
WHERE (json_content #>> '{owner,name}') IS NOT NULL;

-- Composite index for common InfoSystem filtering patterns
CREATE INDEX IF NOT EXISTS idx_main_resource_common_filters
ON riha.main_resource USING btree (
    (json_content ->> 'short_name'),
    (json_content ->> 'name'),
    (json_content #>> '{owner,code}')
)
WHERE (json_content ->> 'short_name') IS NOT NULL;

-- =============================================================================
-- Additional indexes for PostgreSQL 14 optimization
-- =============================================================================

-- Index for creation_date operations (used in sorting)
CREATE INDEX IF NOT EXISTS idx_main_resource_creation_date
ON riha.main_resource USING btree (creation_date DESC)
WHERE creation_date IS NOT NULL;

-- Partial index for active/current resources only
CREATE INDEX IF NOT EXISTS idx_main_resource_active_uuid_timestamp
ON riha.main_resource USING btree (
    (json_content ->> 'uuid'),
    creation_date DESC
)
WHERE (json_content ->> 'uuid') IS NOT NULL 
  AND creation_date IS NOT NULL;

-- =============================================================================
-- Analyze tables after creating indexes
-- =============================================================================
ANALYZE riha.large_object;
ANALYZE riha.file_resource;
ANALYZE riha.registered_file;
ANALYZE riha.main_resource;
