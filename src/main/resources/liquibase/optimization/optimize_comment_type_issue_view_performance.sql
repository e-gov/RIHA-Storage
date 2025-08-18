-- Optimization for comment_type_issue_view performance
-- These indexes should improve the performance of the /api/v1/issues endpoint
-- when filtering by status and sub_type and sorting by creation_date

-- Index for filtering by status (used in WHERE status = 'OPEN')
CREATE INDEX IF NOT EXISTS idx_comment_status 
ON riha.comment (status) 
WHERE type = 'ISSUE';

-- Index for filtering by sub_type (used in WHERE sub_type IS NOT NULL)  
CREATE INDEX IF NOT EXISTS idx_comment_sub_type
ON riha.comment (sub_type)
WHERE type = 'ISSUE' AND sub_type IS NOT NULL;

-- Composite index for the exact query pattern: filtering by status and sub_type, sorting by creation_date
CREATE INDEX IF NOT EXISTS idx_comment_status_subtype_creation_date
ON riha.comment (status, sub_type, creation_date DESC)
WHERE type = 'ISSUE';

-- Index for comment_parent_id to speed up the event aggregation LEFT JOIN
CREATE INDEX IF NOT EXISTS idx_comment_parent_id_creation_date
ON riha.comment (comment_parent_id, creation_date DESC)
WHERE comment_parent_id IS NOT NULL;

-- Analyze tables after creating indexes
ANALYZE riha.comment;
