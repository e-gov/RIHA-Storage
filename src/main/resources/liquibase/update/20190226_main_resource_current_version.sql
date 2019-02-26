
-- added commonly used json fields as a separate fields for convenience
create or replace view riha.main_resource_current_version as
  select mr.*,
   json_content #>> '{short_name}' as json_short_name,
       COALESCE (json_content #>> '{name}', '') || '  ' || COALESCE (json_content #>> '{short_name}' , '') as search_name,
        mr.json_content #>> '{uuid}' as json_uuid

      from riha.main_resource mr,
                   (select max(mr.main_resource_id) max_id
                    from riha.main_resource mr
                    group by json_content #>> '{uuid}') tmp
  where mr.main_resource_id = tmp.max_id;

