-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

begin;
  insert into dbpatch (currentVersion,     currentPatch,   previousVersion,     previousPatch)
               values ('TEST',             2,              'TEST',              1);
commit;
