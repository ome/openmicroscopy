--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

-- Converts `CategoryGroup` and `Category` to `TagAnnotation` with an
-- approriate `name` field. The `description` fields for CG
-- and C are mapped to a `TextAnnotation` on the `TagAnnotation`
-- with a `name` of "description".

BEGIN;

CREATE OR REPLACE FUNCTION OMERO3A__1__CGtoTags() RETURNS varchar(255) AS $$
DECLARE
    mviews RECORD;
    cnt INT8;
    ann INT8;
    dsc INT8;
    link INT8;
    new_annotation INT8;
    cat INT8;
    img INT8;
BEGIN

  --
  -- Convert all categories and category groups
  --
  CREATE TABLE OMERO3A_1__cg_to_ann (cg INT8 primary key, ann_id INT8);
  CREATE TABLE OMERO3A_1__c_to_ann (c INT8 primary key, ann_id INT8);

  FOR mviews IN SELECT
  id, owner_id, group_id, creation_id, permissions, external_id, update_id, version, name, description FROM categorygroup cg LOOP

    SELECT INTO ann nextval('seq_annotation');
    INSERT INTO annotation (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name) SELECT
      '/basic/text/tag/' as discriminator,
      ann as id,
      mviews.owner_id as owner_id,
      mviews.group_id as group_id,
      mviews.creation_id as creation_id,
      mviews.permissions as permissions,
      mviews.external_id as external_id,
      mviews.name as textValue,
      'CategoryGroup' as name;
    INSERT INTO OMERO3A_1__cg_to_ann VALUES (mviews.id, ann);

    IF mviews.description IS NOT NULL
    THEN
      -- Now insert description as an annotation on that annotation
      SELECT INTO dsc nextval('seq_annotation');
      INSERT INTO annotation (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name) SELECT
        '/basic/text/' as discriminator,
        dsc as id,
        mviews.owner_id as owner_id,
        mviews.group_id as group_id,
        mviews.creation_id as creation_id,
        mviews.permissions as permissions,
        mviews.external_id as external_id,
        mviews.description as textValue,
        'description' as name;

      SELECT INTO link nextval('seq_annotationannotationlink');
      INSERT INTO annotationannotationlink (id, owner_id, group_id, creation_id, permissions, external_id, update_id, version, parent, child) SELECT
        link as id,
        mviews.owner_id as owner_id,
        mviews.group_id as group_id,
        mviews.creation_id as creation_id,
        mviews.permissions as permissions,
        mviews.external_id as external_id,
        mviews.update_id as update_id,
        mviews.version as version,
        ann as parent,
        dsc as child;
    END IF;

  END LOOP;


  -- Now the same for categories

  FOR mviews IN SELECT
  id, owner_id, group_id, creation_id, permissions, external_id, update_id, version, name, description FROM category c LOOP

    SELECT INTO ann nextval('seq_annotation');
    INSERT INTO annotation (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name) SELECT
      '/basic/text/tag/' as discriminator,
      ann as id,
      mviews.owner_id as owner_id,
      mviews.group_id as group_id,
      mviews.creation_id as creation_id,
      mviews.permissions as permissions,
      mviews.external_id as external_id,
      mviews.name as textValue,
      'Category' as name;
    INSERT INTO OMERO3A_1__c_to_ann VALUES (mviews.id, ann);

    IF mviews.description IS NOT NULL
    THEN
      -- Now insert description as an annotation on that annotation
      SELECT INTO dsc nextval('seq_annotation');
      INSERT INTO annotation (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name) SELECT
        '/basic/text/' as discriminator,
        dsc as id,
        mviews.owner_id as owner_id,
        mviews.group_id as group_id,
        mviews.creation_id as creation_id,
        mviews.permissions as permissions,
        mviews.external_id as external_id,
        mviews.description as textValue,
        'description' as name;

      SELECT INTO link nextval('seq_annotationannotationlink');
      INSERT INTO annotationannotationlink (id, owner_id, group_id, creation_id, permissions, external_id, update_id, version, parent, child) SELECT
        link as id,
        mviews.owner_id as owner_id,
        mviews.group_id as group_id,
        mviews.creation_id as creation_id,
        mviews.permissions as permissions,
        mviews.external_id as external_id,
        mviews.update_id as update_id,
        mviews.version as version,
        ann as parent,
        dsc as child;
    END IF;

  END LOOP;

  --
  -- For each categoryimagelink, link the new annotation to the image
  --
  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child FROM categoryimagelink LOOP

    SELECT INTO link nextval('seq_imageannotationlink');
    SELECT INTO new_annotation ann_id FROM OMERO3A_1__c_to_ann where c = mviews.parent;
    INSERT INTO imageannotationlink (id, owner_id, group_id, creation_id, permissions, external_id, update_id, version, parent, child) SELECT
      link as id,
      mviews.owner_id as owner_id,
      mviews.group_id as group_id,
      mviews.creation_id as creation_id,
      mviews.permissions as permissions,
      mviews.external_id as external_id,
      mviews.update_id as update_id,
      mviews.version as version,
      mviews.child as parent,
      new_annotation as child;

  END LOOP;

  --
  -- For each categorygroupcategorylink, link the new annotation ALSO to the image
  --
  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, parent, child FROM categorygroupcategorylink LOOP
    SELECT INTO new_annotation ann_id FROM OMERO3A_1__cg_to_ann where cg = mviews.parent;
    SELECT INTO cat ann_id FROM OMERO3A_1__c_to_ann where c = mviews.child;
    FOR img IN SELECT child FROM categoryimagelink WHERE parent = cat LOOP
      SELECT INTO link nextval('seq_annotationannotationlink');
      INSERT INTO imageannotationlink (id, owner_id, group_id, creation_id, permissions, external_id, update_id, version, parent, child) SELECT
        link as id,
        mviews.owner_id as owner_id,
        mviews.group_id as group_id,
        mviews.creation_id as creation_id,
        mviews.permissions as permissions,
        mviews.external_id as external_id,
        mviews.update_id as update_id,
        mviews.version as version,
        img as parent,
        new_annotation as child;
    END LOOP;
  END LOOP;

  DELETE FROM OMERO3A_1__cg_to_ann;
  DROP TABLE OMERO3A_1__cg_to_ann;
  DELETE FROM OMERO3A_1__c_to_ann;
  DROP TABLE OMERO3A_1__c_to_ann;

  RETURN 'success';
END;
$$ LANGUAGE plpgsql;

SELECT OMERO3A__1__CGtoTags();
DROP FUNCTION OMERO3A__1__CGtoTags();
INSERT INTO dbpatch (message, finished, currentVersion, currentPatch, previousVersion, previousPatch)
      VALUES ('Converted CGC to tags', now(),
              'OMERO3A',1, 'OMERO3A__pre__CGCtoTags', 1);
COMMIT;
