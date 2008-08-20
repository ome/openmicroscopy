--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- ticket:1056 fixing unique(ordered, ordered_index)
--

BEGIN;

   INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                values ('OMERO3A',      11,              'OMERO3A',          10);

   CREATE OR REPLACE FUNCTION channel_pixels_index_move() RETURNS "trigger" AS '
     DECLARE
       duplicate INT8;
     BEGIN

       -- Avoids a query if the new and old values of x are the same.
       IF new.pixels = old.pixels AND new.pixels_index = old.pixels_index THEN
           RETURN new;
       END IF;

       -- At most, there should be one duplicate
       SELECT id INTO duplicate
         FROM channel
        WHERE pixels = new.pixels AND pixels_index = new.pixels_index
       OFFSET 0
        LIMIT 1;

       IF duplicate IS NOT NULL THEN
           RAISE NOTICE ''Remapping channel % via ( -1 - oldvalue) '', duplicate;
           UPDATE channel SET pixels_index = -1 * pixels_index WHERE id = duplicate;
       END IF;

       RETURN new;
     END;' LANGUAGE plpgsql;

   CREATE TRIGGER channel_pixels_index_trigger
         BEFORE UPDATE ON channel
         FOR EACH ROW EXECUTE PROCEDURE channel_pixels_index_move ();

   CREATE OR REPLACE FUNCTION channelbinding_renderingDef_index_move() RETURNS "trigger" AS '
     DECLARE
       duplicate INT8;
     BEGIN

       -- Avoids a query if the new and old values of x are the same.
       IF new.renderingDef = old.renderingDef AND new.renderingDef_index = old.renderingDef_index THEN
           RETURN new;
       END IF;

       -- At most, there should be one duplicate
       SELECT id INTO duplicate
         FROM channelbinding
        WHERE renderingDef = new.renderingDef AND renderingDef_index = new.renderingDef_index
       OFFSET 0
        LIMIT 1;

       IF duplicate IS NOT NULL THEN
           RAISE NOTICE ''Remapping channelbinding % via ( -1 - oldvalue) '', duplicate;
           UPDATE channelbinding SET renderingDef_index = -1 * renderingDef_index WHERE id = duplicate;
       END IF;

       RETURN new;
     END;' LANGUAGE plpgsql;

   CREATE TRIGGER channelbinding_renderingDef_index_trigger
         BEFORE UPDATE ON channelbinding
         FOR EACH ROW EXECUTE PROCEDURE channelbinding_renderingDef_index_move ();

   CREATE OR REPLACE FUNCTION codomainmapcontext_renderingDef_index_move() RETURNS "trigger" AS '
     DECLARE
       duplicate INT8;
     BEGIN

       -- Avoids a query if the new and old values of x are the same.
       IF new.renderingDef = old.renderingDef AND new.renderingDef_index = old.renderingDef_index THEN
           RETURN new;
       END IF;

       -- At most, there should be one duplicate
       SELECT id INTO duplicate
         FROM codomainmapcontext
        WHERE renderingDef = new.renderingDef AND renderingDef_index = new.renderingDef_index
       OFFSET 0
        LIMIT 1;

       IF duplicate IS NOT NULL THEN
           RAISE NOTICE ''Remapping codomainmapcontext % via ( -1 - oldvalue) '', duplicate;
           UPDATE codomainmapcontext SET renderingDef_index = -1 * renderingDef_index WHERE id = duplicate;
       END IF;

       RETURN new;
     END;' LANGUAGE plpgsql;

   CREATE TRIGGER codomainmapcontext_renderingDef_index_trigger
         BEFORE UPDATE ON codomainmapcontext
         FOR EACH ROW EXECUTE PROCEDURE codomainmapcontext_renderingDef_index_move ();

   CREATE OR REPLACE FUNCTION groupexperimentermap_child_index_move() RETURNS "trigger" AS '
     DECLARE
       duplicate INT8;
     BEGIN

       -- Avoids a query if the new and old values of x are the same.
       IF new.child = old.child AND new.child_index = old.child_index THEN
           RETURN new;
       END IF;

       -- At most, there should be one duplicate
       SELECT id INTO duplicate
         FROM groupexperimentermap
        WHERE child = new.child AND child_index = new.child_index
       OFFSET 0
        LIMIT 1;

       IF duplicate IS NOT NULL THEN
           RAISE NOTICE ''Remapping groupexperimentermap % via ( -1 - oldvalue) '', duplicate;
           UPDATE groupexperimentermap SET child_index = -1 -  child_index WHERE id = duplicate;
       END IF;

       RETURN new;
     END;' LANGUAGE plpgsql;

   CREATE TRIGGER groupexperimentermap_child_index_trigger
         BEFORE UPDATE ON groupexperimentermap
         FOR EACH ROW EXECUTE PROCEDURE groupexperimentermap_child_index_move ();

   CREATE OR REPLACE FUNCTION pixels_image_index_move() RETURNS "trigger" AS '
     DECLARE
       duplicate INT8;
     BEGIN

       -- Avoids a query if the new and old values of x are the same.
       IF new.image = old.image AND new.image_index = old.image_index THEN
           RETURN new;
       END IF;

       -- At most, there should be one duplicate
       SELECT id INTO duplicate
         FROM pixels
        WHERE image = new.image AND image_index = new.image_index
       OFFSET 0
        LIMIT 1;

       IF duplicate IS NOT NULL THEN
           RAISE NOTICE ''Remapping pixels % via ( -1 - oldvalue) '', duplicate;
           UPDATE pixels SET image_index = -1 -  image_index WHERE id = duplicate;
       END IF;

       RETURN new;
     END;' LANGUAGE plpgsql;

   CREATE TRIGGER pixels_image_index_trigger
         BEFORE UPDATE ON pixels
         FOR EACH ROW EXECUTE PROCEDURE pixels_image_index_move ();

   UPDATE dbpatch set message = 'Database updated.', finished = now()
    where currentVersion  = 'OMERO3A' and
          currentPatch    = 11         and
          previousVersion = 'OMERO3A' and
          previousPatch   = 10;

COMMIT;
