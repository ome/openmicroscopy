-- Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
--
-- This program is free software; you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation; either version 2 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along
-- with this program; if not, write to the Free Software Foundation, Inc.,
-- 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--
-- Copyright 2012 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

---
--- OMERO-Beta4.4 release upgrade from OMERO4.3__0 to OMERO4.4__0
--- Primarily for upgrading group permissions to include the split
--- between ra and rw
---

BEGIN;

CREATE OR REPLACE FUNCTION omero_assert_db_version(version varchar, patch int) RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = version
            AND currentpatch = patch;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''ASSERTION ERROR: Wrong database version'';
    END IF;

END;' LANGUAGE plpgsql;

SELECT omero_assert_db_version('OMERO4.3',0);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4.4',  0,              'OMERO4.3',          0);

--
-- PERMISSION UPGRADE
--


CREATE OR REPLACE FUNCTION ome_perms(p INT8) RETURNS VARCHAR AS '
DECLARE
    ur CHAR DEFAULT ''-'';
    uw CHAR DEFAULT ''-'';
    gr CHAR DEFAULT ''-'';
    gw CHAR DEFAULT ''-'';
    wr CHAR DEFAULT ''-'';
    ww CHAR DEFAULT ''-'';
BEGIN
    -- annotate flags may be overwritten by write flags

    -- shift 8 (-RWA--------)
    SELECT INTO ur CASE WHEN (p & 1024) = 1024 THEN ''r'' ELSE ''-'' END;
    SELECT INTO uw CASE WHEN (p &  512) =  512 THEN ''w''
                        WHEN (p &  256) =  256 THEN ''a'' ELSE ''-'' END;

    -- shift 4 (-----RWA----)
    SELECT INTO gr CASE WHEN (p &   64) =   64 THEN ''r'' ELSE ''-'' END;
    SELECT INTO gw CASE WHEN (p &   32) =   32 THEN ''w''
                        WHEN (p &   16) =   16 THEN ''a'' ELSE ''-'' END;

    -- shift 0 (---------RWA)
    SELECT INTO wr CASE WHEN (p &    4) =    4 THEN ''r'' ELSE ''-'' END;
    SELECT INTO ww CASE WHEN (p &    2) =    2 THEN ''w''
                        WHEN (p &    1) =    1 THEN ''a'' ELSE ''-'' END;

    RETURN ur || uw || gr || gw || wr || ww;
END;' LANGUAGE plpgsql;


-- Unset all world annotate and write flags.
update experimentergroup
   set permissions = (permissions & (-1 # 3));

-- Where the group read flag is not set,
-- do not allow the annotate bit to be set.
update experimentergroup
   set permissions = (permissions & (-1 # 16))
 where (permissions & 32) <> 32;

-- Where the group write flag is set,
-- set the group annotate flag.
update experimentergroup
   set permissions = (permissions | 16)
 where (permissions & 32) = 32;

-- Where the group write flag is set,
-- unset it.
update experimentergroup
   set permissions = (permissions & (-1 # 32))
 where (permissions & 32) = 32;

--
-- Changes of now() function usage (#5862)
--

CREATE OR REPLACE FUNCTION _current_or_new_event() RETURNS int8
    AS '
    DECLARE
        eid int8;
    BEGIN
        SELECT INTO eid _current_event();
        IF eid = 0 OR eid IS NULL THEN
            SELECT INTO eid ome_nextval(''seq_event'');
            INSERT INTO event (id, permissions, status, time, experimenter, experimentergroup, session, type)
                SELECT eid, -52, ''TRIGGERED'', clock_timestamp(), 0, 0, 0, 0;
        END IF;
        RETURN eid;
    END;'
LANGUAGE plpgsql;

create or replace function uuid() returns character(36)
as '
    select substring(x.my_rand from 1 for 8)||''-''||
           substring(x.my_rand from 9 for 4)||''-4''||
           substring(x.my_rand from 13 for 3)||''-''||x.clock_1||
           substring(x.my_rand from 16 for 3)||''-''||
           substring(x.my_rand from 19 for 12)
from
(select md5(clock_timestamp()::text||random()) as my_rand, to_hex(8+(3*random())::int) as clock_1) as x;'
language sql;

-- Delete triggers to go with update triggers (See #9337)
CREATE OR REPLACE FUNCTION annotation_link_delete_trigger() RETURNS "trigger"
    AS '
    DECLARE
        eid int8;
    BEGIN

        SELECT INTO eid _current_or_new_event();
        INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -52, old.parent, TG_ARGV[0], eid;

        RETURN old;

    END;'
LANGUAGE plpgsql;

CREATE TRIGGER annotation_annotation_link_delete_trigger
        BEFORE DELETE ON annotationannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.annotations.Annotation');
CREATE TRIGGER channel_annotation_link_delete_trigger
        BEFORE DELETE ON channelannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.core.Channel');
CREATE TRIGGER dataset_annotation_link_delete_trigger
        BEFORE DELETE ON datasetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.containers.Dataset');
CREATE TRIGGER experimenter_annotation_link_delete_trigger
        BEFORE DELETE ON experimenterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.meta.Experimenter');
CREATE TRIGGER experimentergroup_annotation_link_delete_trigger
        BEFORE DELETE ON experimentergroupannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.meta.ExperimenterGroup');
CREATE TRIGGER image_annotation_link_delete_trigger
        BEFORE DELETE ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.core.Image');
CREATE TRIGGER namespace_annotation_link_delete_trigger
        BEFORE DELETE ON namespaceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.meta.Namespace');
CREATE TRIGGER node_annotation_link_delete_trigger
        BEFORE DELETE ON nodeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.meta.Node');
CREATE TRIGGER originalfile_annotation_link_delete_trigger
        BEFORE DELETE ON originalfileannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.core.OriginalFile');
CREATE TRIGGER pixels_annotation_link_delete_trigger
        BEFORE DELETE ON pixelsannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.core.Pixels');
CREATE TRIGGER planeinfo_annotation_link_delete_trigger
        BEFORE DELETE ON planeinfoannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.core.PlaneInfo');
CREATE TRIGGER plate_annotation_link_delete_trigger
        BEFORE DELETE ON plateannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.screen.Plate');
CREATE TRIGGER plateacquisition_annotation_link_delete_trigger
        BEFORE DELETE ON plateacquisitionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.screen.PlateAcquisition');
CREATE TRIGGER project_annotation_link_delete_trigger
        BEFORE DELETE ON projectannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.containers.Project');
CREATE TRIGGER reagent_annotation_link_delete_trigger
        BEFORE DELETE ON reagentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.screen.Reagent');
CREATE TRIGGER roi_annotation_link_delete_trigger
        BEFORE DELETE ON roiannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.roi.Roi');
CREATE TRIGGER screen_annotation_link_delete_trigger
        BEFORE DELETE ON screenannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.screen.Screen');
CREATE TRIGGER session_annotation_link_delete_trigger
        BEFORE DELETE ON sessionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.meta.Session');
CREATE TRIGGER well_annotation_link_delete_trigger
        BEFORE DELETE ON wellannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.screen.Well');
CREATE TRIGGER wellsample_annotation_link_delete_trigger
        BEFORE DELETE ON wellsampleannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.screen.WellSample');

--
-- FINISHED
--

UPDATE dbpatch set message = 'Database updated.', finished = clock_timestamp()
 WHERE currentVersion  = 'OMERO4.4'    and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.3' and
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO4.4__0'||CHR(10)||CHR(10)||CHR(10) as Status;

COMMIT;
