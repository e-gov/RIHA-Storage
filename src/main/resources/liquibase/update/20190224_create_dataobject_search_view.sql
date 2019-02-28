
drop view riha.data_object_search_view;
drop view riha.main_resource_current_version;

-- added commonly used json fields as a separate fields for convenience
create or replace view riha.main_resource_current_version as
  select mr.*,
   json_content #>> '{short_name}' as json_short_name,
       COALESCE (json_content #>> '{name}', '') || '  ' || COALESCE (json_content #>> '{short_name}' , '') as search_name,
        mr.json_content #>> '{uuid}' as json_uuid

      from riha.main_resource mr,
                   (select max(mr.main_resource_id) max_id
                    from riha.main_resource mr
                    group by json_content #>> '{short_name}') tmp
  where mr.main_resource_id = tmp.max_id;


-- recreating view with json fields
create or replace view riha.data_object_search_view as
  select raw_data."Infosüsteem"       as infosystem,
         raw_data."Andmeobjekti nimi" as andmeobjekti_nimi,
         raw_data."Kommentaar"        as kommentaar,
         raw_data."Vanemobjekt 1"     as vanemobjekt,
         case when "EIA" ilike 'jah' then 'jah, eriliigiline'
              when "DIA" ilike 'jah' then 'jah, delikaatne'
              when "IA" ilike 'jah' then 'jah'
              when "IA" ilike 'ei' then 'ei'
              when ( "IA" is not null and  "IA"  not ilike 'ei' and "IA" not ilike 'jah') then 'muu'
              else null
             end as personal_data,
         "EIA" ilike 'jah'            as eia,
         "DIA" ilike 'jah'            as dia,
         "AV" ilike 'jah'             as av,
         "IA" ilike 'jah'             as ia,
         "PA" ilike 'jah'             as pa,
        mr.json_short_name as short_name,
        mr.search_name as search_name,
         f.uuid as file_uuid,
         md5(concat("AV", "Vanemobjekt 1", "Andmeobjekti nimi", "DIA", "EIA", "IA", "Kommentaar", "PA", "Vanemobjekt 1", mr.json_short_name )) as id,
          COALESCE (raw_data.search_text, '') || COALESCE (mr.search_name, '') as search_text

  from (
       select records.*, lo.id as lo_id,
      COALESCE (records."Infosüsteem", '') || COALESCE ( records."Andmeobjekti nimi", '') || COALESCE ( records."Kommentaar", '') || COALESCE (  records."Vanemobjekt 1", '') as search_text
       from riha.large_object lo,
            jsonb_to_recordset(lo.search_content -> 'records')
                as records ("AV" text, "Infosüsteem" text, "Andmeobjekti nimi" text, "DIA" text, "EIA" text,
                  "IA" text, "Kommentaar" text, "PA" text, "Vanemobjekt 1" text)) raw_data

         inner join riha.file_resource f on f.large_object_id = lo_id
         inner join riha.registered_file rf on rf.file_resource_uuid = f.uuid
         inner join riha.main_resource_current_version mr on rf.main_resource_uuid :: TEXT = mr.json_uuid
;
