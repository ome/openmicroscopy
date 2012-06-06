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
             values ('OMERO4.4RC1',  0,              'OMERO4.3',          0);

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
-- FINISHED
--

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4.4RC1'    and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.3' and
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO4.4RC1__0'||CHR(10)||CHR(10)||CHR(10) as Status;

COMMIT;
