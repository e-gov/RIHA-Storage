SELECT setval('large_object_seq', (select max(id) from large_object));
