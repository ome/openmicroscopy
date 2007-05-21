begin;
  insert into dbpatch (currentVersion,     currentPatch,   previousVersion,     previousPatch)
               values ('TEST',             1,              'OMERO3',            0);
commit;
