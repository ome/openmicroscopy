--
-- Copyright 2010 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- This SQL script is an EXAMPLE of the necessary cleanups that
-- may be necessary for an OMERO4.1__0 databases.
--

BEGIN;

-- Re-add users to groups due to "User removed from group" messages
INSERT INTO groupexperimentermap (id, owner_id, group_id, creation_id, update_id, permissions, parent, child, child_index)
     SELECT ome_nextval('seq_groupexperimentermap'), 0, 0, 0, 0, -35, 2, 5, max(child_index)+1
       FROM groupexperimentermap
      WHERE child = 5
   GROUP BY child;

INSERT INTO groupexperimentermap (id, owner_id, group_id, creation_id, update_id, permissions, parent, child, child_index)
     SELECT ome_nextval('seq_groupexperimentermap'), 0, 0, 0, 0, -35, 2, 20, max(child_index)+1
       FROM groupexperimentermap
      WHERE child = 20
   GROUP BY child;

-- Remove certain types of data marked as "Different group"

UPDATE image
   SET stagelabel = null
  FROM stagelabel
 WHERE image.group_id = 4
   AND stagelabel.group_id = 2;

UPDATE pixels
   SET relatedto = null
 WHERE id in (3779909, 3779910, 3779911);


-- Logical channels can be reused. However, they cannot be reused
-- between groups in 4.2. Those channels will need to be duplicated.

--ns3=# select img.id, img.group_id, pix.id, pix.group_id, ch.id, ch.group_id, lc.id, lc.group_id  from image img, pixels pix, channel ch, logicalchannel lc where img.id = pix.image and pix.id = ch.pixels and ch.logicalchannel = lc.id and lc.group_id <> ch.group_id;
--   id    | group_id |   id    | group_id |   id    | group_id | id | group_id
-----------+----------+---------+----------+---------+----------+----+----------
-- 3781710 |        4 | 3779910 |        4 | 8120992 |        4 |  8 |        2
-- 3781710 |        4 | 3779910 |        4 | 8120993 |        4 |  9 |        2
-- 3781710 |        4 | 3779910 |        4 | 8120991 |        4 |  7 |        2
-- 3781709 |        4 | 3779909 |        4 | 8120989 |        4 |  8 |        2
-- 3781709 |        4 | 3779909 |        4 | 8120990 |        4 |  9 |        2
-- 3781709 |        4 | 3779909 |        4 | 8120988 |        4 |  7 |        2
-- 3781711 |        4 | 3779911 |        4 | 8120995 |        4 |  8 |        2
-- 3781711 |        4 | 3779911 |        4 | 8120996 |        4 |  9 |        2
-- 3781711 |        4 | 3779911 |        4 | 8120994 |        4 |  7 |        2
--(9 rows)


CREATE OR REPLACE FUNCTION fix_duplicate_logical_channels() RETURNS VOID AS $$
DECLARE
    rec RECORD;
    lid INT8;
BEGIN
    -- FOR rec IN SELECT * FROM logicalchannel WHERE id IN (7,8,9) LOOP
    FOR rec IN SELECT lc.*, ch.group_id as ch_group_id, ch.id as ch_id FROM logicalchannel lc, channel ch WHERE lc.id = ch.logicalchannel and lc.group_id <> ch.group_id LOOP
        SELECT INTO lid ome_nextval('seq_logicalchannel');
        INSERT INTO logicalchannel (id, owner_id, group_id, creation_id, update_id, permissions, version, emissionwave, excitationwave, ndfilter)
             VALUES (lid, rec.owner_id, rec.ch_group_id, rec.creation_id, rec.update_id, rec.permissions, rec.version, rec.emissionwave, rec.excitationwave, rec.ndfilter);
        UPDATE channel SET logicalchannel = lid WHERE id = rec.ch_id;
    END LOOP;
END;$$ LANGUAGE plpgsql;
SELECT fix_duplicate_logical_channels();
DROP FUNCTION fix_duplicate_logical_channels();


--
-- PDI+Annotations
--
-- $ grep Different omero-4.1-permissions-report.log | perl -pe 's/.*?(owner=\d+),.*?(owner=\d+).*/$1\n$2/' | sort | uniq
--owner=1
--owner=2
--owner=20
--owner=3
--owner=30
--owner=31
--owner=32
--owner=33
--owner=38
--owner=5
--owner=8

-- $ psql omero4.1 -c "select id, omename, firstname, lastname, email from experimenter where id in (1,2,20,3,30,31,32,33,38,5,8)"
-- id | omename  | firstname  | lastname |                email
------+----------+------------+----------+-------------------------------------
--  1 | mike     | ...
--  2 | brian    | ...
--  3 | jburel   | ...
--  5 | david    | ...
--  8 | cat      | ...
-- 20 | xinyi    | ...
-- 30 | paula    | ...
-- 31 | scott    | ...
-- 32 | testuser | ...
-- 33 | aferrand | ...
-- 38 | iwitzel  | ...
--(11 rows)

-- Different groups: Image(id=5331, group=4, owner=3, perms=rw----) <--> ImageAnnotationLink.parent(id=234, group=3, owner=33, perms=rw----)
-- Different groups: Image(id=4670, group=2, owner=20, perms=rw----) <--> ImageAnnotationLink.parent(id=1075, group=8, owner=38, perms=rw----)

--omero4.1=# select img.id, img.group_id, link.id, link.group_id, ann.id, ann.group_id, ann.ns, ann.discriminator, ann.textvalue from image img, imageannotationlink link, annotation ann where img.id = link.parent and link.child = ann.id and img.group_id<>link.group_id and link.owner_id in (33,38);;
--  id  | group_id |  id  | group_id |  id  | group_id | ns |    discriminator     |    textvalue
--------+----------+------+----------+------+----------+----+----------------------+-----------------
-- 4670 |        2 | 1075 |        8 | 1075 |        8 |    | /basic/text/comment/ | hello :)
-- 5331 |        4 |  234 |        3 |  234 |        3 |    | /basic/text/comment/ | test annotation
--(2 rows)

-- $ psql omero4.1 -c "select id, ns, discriminator, textvalue from annotation where id in (select child from datasetannotationlink where id in (20,22,21,14,12,11,8,9,7))"
--  id  | ns |    discriminator     |                        textvalue
--------+----+----------------------+---------------------------------------------------------
-- 1125 |    | /basic/text/comment/ | i see
-- 1124 |    | /basic/text/comment/ | annotation for the whole dataset?
-- 1112 |    | /basic/text/comment/ | ADDING ANOTHER ANNOATION
-- 1110 |    | /basic/text/comment/ | exactly how were these fixed?
-- 1117 |    | /basic/text/comment/ | t
-- 1123 |    | /basic/text/comment/ | This dataset looks rubbish. See me about this tomorrow!
-- 1111 |    | /basic/text/comment/ | test
-- 1114 |    | /basic/text/comment/ | test
-- 1115 |    | /basic/text/comment/ | test2
--(9 rows)

--
-- Breaking all links since the about data does not seem of value.
--

DELETE FROM imageannotationlink
 USING image
 WHERE image.id = imageannotationlink.parent
   AND image.group_id <> imageannotationlink.group_id;

DELETE FROM datasetannotationlink
 USING dataset
 WHERE dataset.id = datasetannotationlink.parent
   AND dataset.group_id <> datasetannotationlink.group_id;

DELETE FROM datasetimagelink
 USING image
 WHERE image.id = datasetimagelink.child
   AND image.group_id <> datasetimagelink.group_id;

DELETE FROM datasetimagelink
 USING dataset
 WHERE dataset.id = datasetimagelink.parent
   AND dataset.group_id <> datasetimagelink.group_id;

DELETE FROM projectdatasetlink
 USING dataset
 WHERE dataset.id = projectdatasetlink.child
   AND dataset.group_id <> projectdatasetlink.group_id;

DELETE FROM projectdatasetlink
 USING project
 WHERE project.id = projectdatasetlink.parent
   AND project.group_id <> projectdatasetlink.group_id;

COMMIT;
