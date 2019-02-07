
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
         mr.json_content #>> '{short_name}' as short_name,
         f.uuid as file_uuid

  from (
       select records.*, lo.id as lo_id
       from riha.large_object lo,
            jsonb_to_recordset(lo.search_content -> 'records')
                as records ("AV" text, "Infosüsteem" text, "Andmeobjekti nimi" text, "DIA" text, "EIA" text,
                  "IA" text, "Kommentaar" text, "PA" text, "Vanemobjekt 1" text)) raw_data

         inner join riha.file_resource f on f.large_object_id = lo_id
         inner join riha.registered_file rf on rf.file_resource_uuid = f.uuid
         inner join riha.main_resource mr on rf.main_resource_uuid :: TEXT = mr.json_content #>> '{uuid}'



select * from riha.data_object_search_view