--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- ticket:1047 Fixing a typo in UNIQUE index creation
-- which *substantially* hurt performance on indexed
-- collections
--

BEGIN;

  INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
               values ('OMERO3A',      9,              'OMERO3A',           8);

  ALTER TABLE pixels DROP CONSTRAINT pixels_id_key;
  ALTER TABLE pixels  ADD CONSTRAINT pixels_image_key UNIQUE (image, image_index);

  ALTER TABLE channel DROP CONSTRAINT channel_id_key;
  ALTER TABLE channel  ADD CONSTRAINT channel_pixels_key UNIQUE (pixels, pixels_index);

  ALTER TABLE channelbinding DROP CONSTRAINT channelbinding_id_key;
  ALTER TABLE channelbinding  ADD CONSTRAINT channelbinding_renderingDef_key UNIQUE (renderingdef, renderingdef_index);

  ALTER TABLE codomainmapcontext DROP CONSTRAINT codomainmapcontext_id_key;
  ALTER TABLE codomainmapcontext  ADD CONSTRAINT codomainmapcontext_renderingDef_key UNIQUE (renderingdef, renderingdef_index);

  ALTER TABLE groupexperimentermap DROP CONSTRAINT groupexperimentermap_id_key;
  ALTER TABLE groupexperimentermap  ADD CONSTRAINT groupexperimentermap_child_key UNIQUE (child, child_index);

  UPDATE dbpatch set message = 'Database updated.', finished = now()
    where currentVersion  = 'OMERO3A' and
          currentPatch    = 9         and
          previousVersion = 'OMERO3A' and
          previousPatch   = 8;

COMMIT;
