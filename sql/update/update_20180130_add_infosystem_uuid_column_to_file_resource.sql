ALTER TABLE riha.file_resource
    ADD COLUMN infosystem_uuid UUID NULL;

UPDATE riha.file_resource
SET infosystem_uuid = 
(SELECT system_uuid::uuid FROM
  (SELECT uuid AS file_uuid, infosystem.json_content ->> 'uuid' AS system_uuid 
   FROM riha.file_resource file 
   LEFT JOIN riha.main_resource infosystem ON
      (infosystem.json_content #>> '{documents}')
   like '%"file://' || file.uuid || '"%'   
GROUP BY file_uuid, system_uuid
  ORDER BY file.creation_date) AS joined_uuids
WHERE file_uuid = uuid
);

UPDATE riha.file_resource
SET infosystem_uuid = 
(SELECT system_uuid::uuid FROM
  (SELECT uuid AS file_uuid, infosystem.json_content ->> 'uuid' AS system_uuid 
   FROM riha.file_resource file 
   LEFT JOIN riha.main_resource infosystem ON
      (infosystem.json_content #>> '{data_files}')
   like '%"file://' || file.uuid || '"%'   
GROUP BY file_uuid, system_uuid
  ORDER BY file.creation_date) AS joined_uuids
WHERE file_uuid = uuid
)
WHERE file_resource.infosystem_uuid IS NULL;
