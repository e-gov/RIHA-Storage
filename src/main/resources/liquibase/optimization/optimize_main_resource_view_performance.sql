-- Optimization indexes for riha.main_resource_view and related tables
-- These indexes should improve performance for InfoSystem queries and full-text search

-- =============================================================================
-- MAIN_RESOURCE table indexes
-- =============================================================================

-- Index for JSON UUID extraction (used heavily in main_resource_view)
-- This is critical as the view uses DISTINCT ON (json_content ->> 'uuid')
CREATE INDEX IF NOT EXISTS idx_main_resource_json_uuid
ON riha.main_resource USING btree ((json_content ->> 'uuid'));

-- Index for JSON creation timestamp extraction and sorting
-- Used in main_resource_view ORDER BY with j_update_timestamp DESC
-- Using text-based sorting to avoid IMMUTABLE function issues
CREATE INDEX IF NOT EXISTS idx_main_resource_json_timestamps
ON riha.main_resource USING btree (
    (json_content #>> '{meta,update_timestamp}') DESC NULLS LAST,
    main_resource_id DESC
);

-- Index for full-text search on search_content column
-- This is used when searching InfoSystems by text
-- Using PostgreSQL GIN index with pg_trgm extension for better full-text search
-- First ensure the pg_trgm extension is available
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create GIN index using trigrams - handles large text efficiently
CREATE INDEX IF NOT EXISTS idx_main_resource_search_content_gin
ON riha.main_resource USING gin (search_content gin_trgm_ops);

-- Index for search_content prefix matching (limited to first 255 characters to avoid size limit)
-- Keep this as backup for exact prefix matching
CREATE INDEX IF NOT EXISTS idx_main_resource_search_content_text
ON riha.main_resource USING btree (LEFT(search_content, 255) text_pattern_ops);

-- =============================================================================
-- COMMENT table indexes for main_resource_view subqueries
-- =============================================================================

-- Composite index for approval request queries
-- Used in main_resource_view LEFT JOINs for last_positive_*_request
CREATE INDEX IF NOT EXISTS idx_comment_approval_requests
ON riha.comment USING btree (
    infosystem_uuid,
    type,
    sub_type,
    status,
    resolution_type,
    modified_date DESC
) WHERE type = 'ISSUE' AND status = 'CLOSED' AND resolution_type = 'POSITIVE';

-- Specific index for establishment requests
CREATE INDEX IF NOT EXISTS idx_comment_establishment_requests
ON riha.comment USING btree (
    infosystem_uuid,
    modified_date DESC
) WHERE type = 'ISSUE' AND sub_type = 'ESTABLISHMENT_REQUEST' AND status = 'CLOSED' AND resolution_type = 'POSITIVE';

-- Specific index for take into use requests  
CREATE INDEX IF NOT EXISTS idx_comment_take_into_use_requests
ON riha.comment USING btree (
    infosystem_uuid,
    modified_date DESC
) WHERE type = 'ISSUE' AND sub_type = 'TAKE_INTO_USE_REQUEST' AND status = 'CLOSED' AND resolution_type = 'POSITIVE';

-- Specific index for finalization requests
CREATE INDEX IF NOT EXISTS idx_comment_finalization_requests
ON riha.comment USING btree (
    infosystem_uuid,
    modified_date DESC
) WHERE type = 'ISSUE' AND sub_type = 'FINALIZATION_REQUEST' AND status = 'CLOSED' AND resolution_type = 'POSITIVE';

-- =============================================================================
-- MAIN_RESOURCE_RELATION table indexes
-- =============================================================================

-- Index for used system type relations (used in main_resource_view)
CREATE INDEX IF NOT EXISTS idx_main_resource_relation_used_system
ON riha.main_resource_relation USING btree (infosystem_uuid, type)
WHERE type = 'USED_SYSTEM';

-- =============================================================================
-- Additional indexes for common filtering patterns
-- =============================================================================

-- Index for JSON short_name extraction (commonly used for lookups)
CREATE INDEX IF NOT EXISTS idx_main_resource_json_short_name
ON riha.main_resource USING btree ((json_content ->> 'short_name'));

-- Index for JSON owner code extraction (used for organization filtering)
CREATE INDEX IF NOT EXISTS idx_main_resource_json_owner_code
ON riha.main_resource USING btree ((json_content #>> '{owner,code}'));

-- Composite index for UUID and timestamps (for the main_resource_view DISTINCT ON + ORDER BY)
-- Using text-based timestamp sorting to avoid IMMUTABLE function issues
CREATE INDEX IF NOT EXISTS idx_main_resource_uuid_timestamps_composite
ON riha.main_resource USING btree (
    (json_content ->> 'uuid'),
    (json_content #>> '{meta,update_timestamp}') DESC NULLS LAST,
    main_resource_id DESC
);

-- =============================================================================
-- Analyze tables after creating indexes
-- =============================================================================
ANALYZE riha.main_resource;
ANALYZE riha.comment;
ANALYZE riha.main_resource_relation;
