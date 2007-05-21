begin;
  insert into dbpatch (currentVersion,     currentPatch,   previousVersion,     previousPatch)
               values ('TEST',             2,              'TEST',              1);
commit;
