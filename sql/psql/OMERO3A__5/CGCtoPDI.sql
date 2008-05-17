--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- Script to convert:
--
--   categories --> datasets
--   categorygroups --> projects
--   categoryimagelink --> datasetimagelink
--   categorygroupcategorylink --> projectdatasetlink
--
-- but does not increment the dbpatch. (This implies
-- also that it can only be run once. See ticket:970)
--
-- Note: the 4 converted tables in the CGCI hierarchy
-- have all of their rows deleted, though the table
-- themselves are not deleted.
--
-- Requires plpsql; can be installed via `createlang plpgsql DBNAME`
--
BEGIN;

CREATE OR REPLACE FUNCTION OMERO3A__1__CGtoPDI() RETURNS varchar(255) AS $$
DECLARE
    mviews RECORD;
    cnt INT8;
    nid INT8;
    link INT8;
    new_project INT8;
    new_dataset INT8;
BEGIN

  --
  -- Convert all categories and category groups
  --
  CREATE TABLE OMERO3A_1__cg_to_prj (cg INT8 primary key, prj_id INT8);
  CREATE TABLE OMERO3A_1__c_to_ds (c INT8 primary key, ds_id INT8);

  FOR mviews IN SELECT
  id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, name, description FROM categorygroup cg LOOP

    SELECT INTO nid nextval('seq_project');
    INSERT INTO project
      (id, owner_id, group_id, creation_id, update_id, permissions, external_id, description, name)
      VALUES
      (nid, mviews.owner_id, mviews.group_id, mviews.creation_id, mviews.update_id,
      mviews.permissions, mviews.external_id, mviews.description, mviews.name);
    INSERT INTO OMERO3A_1__cg_to_prj VALUES (mviews.id, nid);

  END LOOP;

  FOR mviews IN SELECT
  id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, name, description FROM category g LOOP

    SELECT INTO nid nextval('seq_dataset');
    INSERT INTO dataset
      (id, owner_id, group_id, creation_id, update_id, permissions, external_id, description, name)
      VALUES
      (nid, mviews.owner_id, mviews.group_id, mviews.creation_id, mviews.update_id,
      mviews.permissions, mviews.external_id, mviews.description, mviews.name);
    INSERT INTO OMERO3A_1__c_to_ds VALUES (mviews.id, nid);

  END LOOP;

  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child FROM categorygroupcategorylink LOOP

    SELECT INTO link nextval('seq_projectdatasetlink');
    SELECT INTO new_dataset ds_id FROM OMERO3A_1__c_to_ds where c = mviews.child;
    SELECT INTO new_project prj_id FROM OMERO3A_1__cg_to_prj where cg = mviews.parent;
    INSERT INTO projectdatasetlink
      (id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child)
      VALUES
      (link, mviews.owner_id, mviews.group_id, mviews.creation_id, mviews.update_id, mviews.permissions, mviews.external_id, mviews.version, new_project, new_dataset);

  END LOOP;

  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child FROM categoryimagelink LOOP

    SELECT INTO link nextval('seq_datasetimagelink');
    SELECT INTO new_dataset ds_id FROM OMERO3A_1__c_to_ds where c = mviews.parent;
    INSERT INTO datasetimagelink
      (id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child)
      VALUES
      (link, mviews.owner_id, mviews.group_id, mviews.creation_id, mviews.update_id, mviews.permissions, mviews.external_id, mviews.version, new_dataset, mviews.child);

  END LOOP;

  DELETE FROM OMERO3A_1__cg_to_prj;
  DROP TABLE OMERO3A_1__cg_to_prj;
  DELETE FROM OMERO3A_1__c_to_ds;
  DROP TABLE OMERO3A_1__c_to_ds;

  DELETE FROM categorygroupcategorylink;
  DELETE FROM categoryimagelink;
  DELETE FROM categorygroup;
  DELETE FROM category;

  RETURN 'success';
END;
$$ LANGUAGE plpgsql;

SELECT OMERO3A__1__CGtoPDI();
DROP FUNCTION OMERO3A__1__CGtoPDI();
INSERT INTO dbpatch (message, finished, currentVersion, currentPatch, previousVersion, previousPatch)
      VALUES ('Converted CGC to PDI', now(),
              'OMERO3A',5, 'OMERO3A', 5);
COMMIT;
