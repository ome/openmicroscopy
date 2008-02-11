--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

CREATE OR REPLACE FUNCTION OMERO3A__1__CGtoTTG() RETURNS varchar(255) AS $$
DECLARE
    mviews RECORD;
    indexed RECORD;
    cnt INT8;
    ann INT8;
    link INT8;
    new_annotated INT8;
    new_annotation INT8;
BEGIN

  --
  -- Convert all categories and category groups
  --
  CREATE TABLE OMERO3A_1__cg_to_ann (cg INT8 primary key, ann_id INT8);
  CREATE TABLE OMERO3A_1__c_to_ann (c INT8 primary key, ann_id INT8);

  FOR mviews IN SELECT
  id, owner_id, group_id, creation_id, permissions, external_id, version, name, description FROM categorygroup cg LOOP

    SELECT INTO ann nextval('seq_annotation');
    INSERT INTO annotation
      (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name)
      VALUES
      ('/basic/text/tag/', ann, mviews.owner_id, mviews.group_id, mviews.creation_id,
      mviews.permissions, mviews.external_id, mviews.description, mviews.name);
    INSERT INTO OMERO3A_1__cg_to_ann VALUES (mviews.id, ann);

  END LOOP;

  FOR mviews IN SELECT
  id, owner_id, group_id, creation_id, permissions, external_id, version, name, description FROM category g LOOP

    SELECT INTO ann nextval('seq_annotation');
    INSERT INTO annotation
      (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name)
      VALUES
      ('/basic/text/tag/', ann, mviews.owner_id, mviews.group_id, mviews.creation_id,
      mviews.permissions, mviews.external_id, mviews.description, mviews.name);
    INSERT INTO OMERO3A_1__c_to_ann VALUES (mviews.id, ann);

  END LOOP;

  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child FROM categorygroupcategorylink LOOP

    SELECT INTO link nextval('seq_annotationannotationlink');
    SELECT INTO new_annotated ann_id FROM OMERO3A_1__c_to_ann where c = mviews.child;
    SELECT INTO new_annotation ann_id FROM OMERO3A_1__cg_to_ann where cg = mviews.parent;
    INSERT INTO annotationannotationlink
      (id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child)
      VALUES
      (link, mviews.owner_id, mviews.group_id, mviews.creation_id, mviews.update_id, mviews.permissions, mviews.external_id, mviews.version, new_annotation, new_annotated);

  END LOOP;

  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child FROM categoryimagelink LOOP

    SELECT INTO link nextval('seq_imageannotationlink');
    SELECT INTO new_annotation ann_id FROM OMERO3A_1__c_to_ann where c = mviews.parent;
    INSERT INTO imageannotationlink
      (id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child)
      VALUES
      (link, mviews.owner_id, mviews.group_id, mviews.creation_id, mviews.update_id, mviews.permissions, mviews.external_id, mviews.version, mviews.child, new_annotation);

  END LOOP;

  DELETE FROM OMERO3A_1__cg_to_ann;
  DROP TABLE OMERO3A_1__cg_to_ann;
  DELETE FROM OMERO3A_1__c_to_ann;
  DROP TABLE OMERO3A_1__c_to_ann;

  RETURN 'success';
END;
$$ LANGUAGE plpgsql;

SELECT OMERO3A__1__CGtoTTG();
DROP FUNCTION OMERO3A__1__CGtoTTG();
INSERT INTO dbpatch (message, finished, currentVersion, currentPatch, previousVersion, previousPatch)
      VALUES ('Converted CGC to tag/taggroup', now(),
              'OMERO3A',1, 'OMERO3A__pre__CGCtoTTG', 1);
COMMIT;
