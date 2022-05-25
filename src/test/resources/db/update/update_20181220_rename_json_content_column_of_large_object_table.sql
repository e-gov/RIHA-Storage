-- Rename csv_search_content column of large_object table to search_content, as additional file types are available for indexing

ALTER TABLE riha.large_object
  RENAME COLUMN csv_search_content TO search_content;