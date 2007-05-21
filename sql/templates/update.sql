--
-- Template update script
-- ===========================================
-- This script is intended to take an OMERO db
-- from the PREVIOUSVERSION/PATCH to the
-- CURRENTVERSION/PATCH, where the file name is:
--
--   PREVIOUSVERSION__PREVIOUSPATCH__CURRENTVERSION__CURRENTPATCH.sql
--
-- and PREVIOUSVERSION/PATCH is determined by 
-- the old current version:
--
--  select currentversion || '__' || currentpatch from dbpatch order by id desc limit 1;
--

begin;

  insert into dbpatch (currentVersion,     currentPatch,   previousVersion,     previousPatch)
               values ('@CURRENTVERSION@', @CURRENTPATCH@, '@PREVIOUSVERSION@', @PREVIOUSPATCH@);

  //
  // Insert your update here
  //

  update dbpatch set message = 'Database updated.', finished = now() where 
    currentVersion  = '@CURRENTVERSION@'  and
    currentPatch    =  @CURRENTPATCH@     and
    previousVersion = '@PREVIOUSVERSION@' and
    previousPatch   =  @PREVIOUSPATCH@;

commit;
