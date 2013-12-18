--
-- Copyright 2012 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

---
--- OMERO-Beta4.4 release upgrade from OMERO4.1__0 to OMERO4.4__0
--- This combined upgraded is composed of the files:
---
---    OMERO4.1__0.sql, OMERO4.2__0.sql and OMERO4.3__0.sql
---
--- However, the restrictions on row-level permissions have been
--- removed since in 4.4__0 only group-level permissions apply.
---
--- The following setting is used in the omero-4.1-permissions-report.sql
--- file to decide how issues should be handled.
---
--- Valid values are: '''ABORT''', '''FIX''', '''DELETE'''
---
--- ABORT causes this script to exit and prints out a list of all the
--- issues which must be resolved before upgrade can complete.
---

\set ACTION '''ABORT'''


BEGIN;

\timing

-- Requirements:
--  * Applies only to OMERO4.1__0
--  * No annotations of deleted types may exist: query, thumbnail, url
--  * No Format with the value of a mimetype may be left over after original files are updated.
--  * No channels point to shapes.
--  * wellsample.timepoint should be null, and will be ignored.
--
-- If any of the requirements are not met, you
-- will need to contact the OME developers for
-- help on upgrading your data.
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

    SELECT INTO rec count(id) as count
           FROM annotation
          WHERE discriminator in (''/basic/text/uri/'', ''/basic/text/query/'', ''/type/Thumbnail/'');

    IF rec.count > 0 THEN
        RAISE EXCEPTION ''ASSERTION ERROR: Found annotations of type: (query, thumbnail, or uri). Count=%'', rec.count;
    END IF;

    SELECT INTO rec count(id) as count
           FROM logicalchannel
          WHERE shapes IS NOT NULL;

    IF rec.count > 0 THEN
        RAISE EXCEPTION ''ASSERTION ERROR: Found channels pointing to shapes: Count=%'', rec.count;
    END IF;

END;' LANGUAGE plpgsql;

SELECT omero_assert_db_version('OMERO4.1',0);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4.2',     0,              'OMERO4.1',          0);


----
--
-- Before anything else make changes for group permissions (#1434)
-- These are the maximal changes that can be made without user intervention.
-- Afterwards, a report is run and any discrepancies will have to be handled
-- manually.
--

CREATE OR REPLACE FUNCTION ome_perms(p bigint) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    ur CHAR DEFAULT '-';
    uw CHAR DEFAULT '-';
    gr CHAR DEFAULT '-';
    gw CHAR DEFAULT '-';
    wr CHAR DEFAULT '-';
    ww CHAR DEFAULT '-';
BEGIN
    -- shift 8
    SELECT INTO ur CASE WHEN (cast(p as bit(64)) & cast(1024 as bit(64))) = cast(1024 as bit(64)) THEN 'r' ELSE '-' END;
    SELECT INTO uw CASE WHEN (cast(p as bit(64)) & cast( 512 as bit(64))) = cast( 512 as bit(64)) THEN 'w' ELSE '-' END;
    -- shift 4
    SELECT INTO gr CASE WHEN (cast(p as bit(64)) & cast(  64 as bit(64))) = cast(  64 as bit(64)) THEN 'r' ELSE '-' END;
    SELECT INTO gw CASE WHEN (cast(p as bit(64)) & cast(  32 as bit(64))) = cast(  32 as bit(64)) THEN 'w' ELSE '-' END;
    -- shift 0
    SELECT INTO wr CASE WHEN (cast(p as bit(64)) & cast(   4 as bit(64))) = cast(   4 as bit(64)) THEN 'r' ELSE '-' END;
    SELECT INTO ww CASE WHEN (cast(p as bit(64)) & cast(   2 as bit(64))) = cast(   2 as bit(64)) THEN 'w' ELSE '-' END;

    RETURN ur || uw || gr || gw || wr || ww;
END;$$;

-- Change all groups to private; groups were set to rwr-r-
-- by default in previous versions making everything readable.

UPDATE experimentergroup SET permissions = -103;

-- Moving user photos to "user"

UPDATE experimenterannotationlink SET group_id = 1
  FROM annotation
 WHERE annotation.ns = 'openmicroscopy.org/omero/experimenter/photo'
   AND annotation.id = experimenterannotationlink.child;

UPDATE annotation SET group_id = 1
 WHERE annotation.ns = 'openmicroscopy.org/omero/experimenter/photo';

UPDATE originalfile SET group_id = 1
  FROM annotation
 WHERE annotation.ns = 'openmicroscopy.org/omero/experimenter/photo'
   AND annotation.file = originalfile.id;

-- #2204: Various perm changes that can be made globally
--
-- Moving scripts from 4.1 into "user" group to keep old jobs readable.
UPDATE originalfile SET group_id = user_group_id, permissions = user_group_permissions
  FROM (select id as user_group_id, permissions as user_group_permissions
          from experimentergroup where name = 'user') AS ug
 WHERE group_id = 0
   AND (cast(permissions as bit(64)) & cast(   4 as bit(64))) = cast(   4 as bit(64))
   AND name in ('populateroi.py', 'makemovie.py');

DELETE
   FROM thumbnail
  USING pixels
  WHERE thumbnail.pixels = pixels.id
    AND pixels.group_id <> thumbnail.group_id;

DELETE
   FROM channelbinding
  USING pixels, renderingdef
  WHERE channelbinding.renderingdef = renderingdef.id
    AND renderingdef.pixels = pixels.id
    AND pixels.group_id <> renderingdef.group_id;

DELETE
   FROM renderingdef
  USING pixels
  WHERE renderingdef.pixels = pixels.id
    AND pixels.group_id <> renderingdef.group_id;


----
-- This section is a copy of omero-4.1-permissions-report.sql EXCEPT for the actual execute,
-- "select omero_41_check()". That function is used to calculate any non-updateable structures,
-- and if present, stop the upgrade.

-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

--
-- Helper methods
--

create or replace function omero_unnest(anyarray)
  returns setof anyelement AS '
    select $1[I] from
        generate_series(array_lower($1,1),
                        array_upper($1,1)) i;
' language 'sql' immutable;

--
-- Check methods
--

create or replace function omero_41_check4(target text, tbl text, col text, ACTION text) returns setof text as '
declare
    sql text;
    rec record;
begin

    sql := ''select target.id as target_id, target.group_id as target_group, target.owner_id as target_owner, ome_perms(target.permissions) as target_perms, '' ||
        ''          tbl.id as tbl_id,       tbl.group_id as tbl_group,       tbl.owner_id as tbl_owner,    ome_perms(tbl.permissions) as tbl_perms '' ||
        ''from '' || target || '' target, '' || tbl || '' tbl '' ||
        ''where target.id = tbl.''|| col || -- Base query linking the two tables
                '' and ( ( target.group_id <> 1 and target.group_id <> tbl.group_id )'' || -- groups do not match
                ''   or  ( target.owner_id <> 0 and target.owner_id not in (select child from groupexperimentermap where parent = tbl.group_id) ) '' || -- target owner not in tbl group (non-root)
                ''   or  ( tbl.owner_id <> 0    and tbl.owner_id not in (select child from groupexperimentermap where parent = target.group_id) ) ) ''; -- tbl owner not in target group (non-root)

    for rec in select * from omero_41_check5(sql, target, tbl, col, ACTION) as txt loop
        return next rec.txt;
    end loop;

    return;

end;' language plpgsql;

create or replace function omero_41_check5(sql text, target text, tbl text, col text, ACTION text) returns setof text as $$
declare
    rec record;
    rec2 record;
    txt text;
    mod text;
begin

    for rec in execute sql loop

        if rec.target_group <> rec.tbl_group then
            txt := 'Different groups';
        else
            txt := 'User removed from group';
        end if;

        txt := txt || ': ' || target || '(';
        txt := txt || 'id='    || rec.target_id || ', ';
        txt := txt || 'group=' || rec.target_group || ', ';
        txt := txt || 'owner=' || rec.target_owner || ', ';
        txt := txt || 'perms=' || rec.target_perms || ')';
        txt := txt || ' <--> ' || tbl || '.';
        txt := txt || col || '(';
        txt := txt || 'id='    || rec.tbl_id || ', ';
        txt := txt || 'group=' || rec.tbl_group || ', ';
        txt := txt || 'owner=' || rec.tbl_owner || ', ';
        txt := txt || 'perms=' || rec.tbl_perms || ')';

        if ACTION = 'DELETE' then

            mod := 'delete from ' || tbl || ' where id = '|| rec.tbl_id || ' returning ''Check:deleted '|| tbl ||'(id='||rec.tbl_id||')'' as txt';
            for rec2 in execute mod loop
                return next rec2.txt;
            end loop;

        elsif ACTION = 'FIX' then

            mod := 'update ' || tbl || ' set group_id = ' || rec.target_group ||' where id = '|| rec.tbl_id || ' returning ''Check:changed group '|| tbl ||'(id='||rec.tbl_id||')'' as txt';
            for rec2 in execute mod loop
                return next rec2.txt;
            end loop;

        elsif ACTION = 'ABORT' then

            raise exception '%', txt;

        else

            return next txt;

        end if;
    end loop;

    return;

end;$$ language plpgsql;


create or replace function omero_41_perms(tbl text, ACTION text) returns setof text as $$
declare
    rec record;
    rec2 record;
    sql text;
    mod text;
    txt text;
    fmt int8;
begin

    -- Skipped because system types in 4.2 (includes enums)
    if tbl in ('experimenter', 'experimentergroup', 'groupexperimentermap', 'sharemember', 'eventtype',
        'immersion', 'arctype',  'renderingmodel',  'acquisitionmode',
        'binning',  'family',  'medium',  'pixelstype',  'format',  'pulse',
        'lasertype',  'jobstatus',  'detectortype',  'microbeammanipulationtype',
        'illumination',  'photometricinterpretation',  'correction',  'eventtype',
        'lasermedium',  'microscopetype',  'dimensionorder',  'experimenttype',
        'contrastmethod',  'filamenttype',  'filtertype') then

        return;
    end if;

    sql := 'select id, group_id, owner_id, ome_perms(permissions) from ' || tbl ||
        ' where group_id <> 1 and ' ||
        ' (cast(permissions as bit(64)) & cast(   4 as bit(64))) = cast(   4 as bit(64))';

    begin

        if tbl = 'originalfile' then
            select into fmt id from format where value = 'Directory';
        end if;

        for rec in execute sql loop

            if tbl = 'originalfile' then
                select into rec2 name, format from originalfile where id = rec.id;
                if rec.group_id = 0 and
                    (
                        rec2.name in ('makemovie.py', 'populateroi.py')
                        or
                        rec2.format = fmt
                    ) then
                    continue;
                end if;
            end if;

            if ACTION = 'DELETE' then

                mod := 'delete from ' || tbl || ' where id = '|| rec.id || ' returning ''Permissions:deleted '' '|| tbl ||' ''(id='' ||  id::text || '')'' as txt';
                for rec2 in execute mod loop
                    return next rec2.txt;
                end loop;

            elsif ACTION = 'FIX' then

                mod := 'update ' || tbl || ' set permissions = g.permissions from experimenter group g where g.id = group_id and id = '|| rec.id || ' returning ''Permissions:modified '' '|| tbl ||' ''(id='' ||  id::text || '')'' as txt';
                for rec2 in execute mod loop
                    return next rec2.txt;
                end loop;
            elsif ACTION = 'ABORT' then

                raise exception '%', 'Non-private permissions:' || tbl || '(id=' || rec.id || ')';

            else

                return next 'Non-private permissions:' || tbl || '(id=' || rec.id || ')';

            end if;
        end loop;
    exception when others then
        -- do nothing
    end;

    return;

end;$$ language plpgsql;


-- General information for parsing the rest.
select * from dbpatch;
select * from experimentergroup;
select * from groupexperimentermap;
-- Not displaying experimenter to protect emails, etc.

create or replace function omero_41_lockchecks() returns setof record stable strict as '
declare
    rec record;
begin
    for rec in select ''Dataset''::text, ''DatasetImageLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Dataset''::text, ''ProjectDatasetLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Dataset''::text, ''DatasetAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Plate''::text, ''Well''::text, ''plate''::text loop return next rec; end loop;
    for rec in select ''Plate''::text, ''PlateAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Plate''::text, ''ScreenPlateLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Channel''::text, ''ChannelAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Microscope''::text, ''Instrument''::text, ''microscope''::text loop return next rec; end loop;
    for rec in select ''WellSample''::text, ''WellSampleAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''WellSample''::text, ''ScreenAcquisitionWellSampleLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''PlaneInfo''::text, ''PlaneInfoAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''TransmittanceRange''::text, ''Filter''::text, ''transmittanceRange''::text loop return next rec; end loop;
    for rec in select ''QuantumDef''::text, ''RenderingDef''::text, ''quantization''::text loop return next rec; end loop;
    for rec in select ''Image''::text, ''ImageAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Image''::text, ''WellSample''::text, ''image''::text loop return next rec; end loop;
    for rec in select ''Image''::text, ''DatasetImageLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Image''::text, ''Pixels''::text, ''image''::text loop return next rec; end loop;
    for rec in select ''Image''::text, ''Roi''::text, ''image''::text loop return next rec; end loop;
    for rec in select ''MicrobeamManipulation''::text, ''LightSettings''::text, ''microbeamManipulation''::text loop return next rec; end loop;
    for rec in select ''RenderingDef''::text, ''CodomainMapContext''::text, ''renderingDef''::text loop return next rec; end loop;
    for rec in select ''RenderingDef''::text, ''ChannelBinding''::text, ''renderingDef''::text loop return next rec; end loop;
    for rec in select ''Project''::text, ''ProjectAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Project''::text, ''ProjectDatasetLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''StageLabel''::text, ''Image''::text, ''stageLabel''::text loop return next rec; end loop;
    for rec in select ''Pixels''::text, ''Channel''::text, ''pixels''::text loop return next rec; end loop;
    for rec in select ''Pixels''::text, ''PlaneInfo''::text, ''pixels''::text loop return next rec; end loop;
    for rec in select ''Pixels''::text, ''Pixels''::text, ''relatedTo''::text loop return next rec; end loop;
    for rec in select ''Pixels''::text, ''Shape''::text, ''pixels''::text loop return next rec; end loop;
    for rec in select ''Pixels''::text, ''PixelsAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Pixels''::text, ''PixelsOriginalFileMap''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Roi''::text, ''RoiAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Roi''::text, ''Shape''::text, ''roi''::text loop return next rec; end loop;
    for rec in select ''ObjectiveSettings''::text, ''Image''::text, ''objectiveSettings''::text loop return next rec; end loop;
    for rec in select ''Instrument''::text, ''Image''::text, ''instrument''::text loop return next rec; end loop;
    for rec in select ''Instrument''::text, ''Detector''::text, ''instrument''::text loop return next rec; end loop;
    for rec in select ''Instrument''::text, ''OTF''::text, ''instrument''::text loop return next rec; end loop;
    for rec in select ''Instrument''::text, ''FilterSet''::text, ''instrument''::text loop return next rec; end loop;
    for rec in select ''Instrument''::text, ''LightSource''::text, ''instrument''::text loop return next rec; end loop;
    for rec in select ''Instrument''::text, ''Dichroic''::text, ''instrument''::text loop return next rec; end loop;
    for rec in select ''Instrument''::text, ''Objective''::text, ''instrument''::text loop return next rec; end loop;
    for rec in select ''Instrument''::text, ''Filter''::text, ''instrument''::text loop return next rec; end loop;
    for rec in select ''ScreenAcquisition''::text, ''ScreenAcquisitionAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''ScreenAcquisition''::text, ''ScreenAcquisitionWellSampleLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Well''::text, ''WellAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Well''::text, ''WellSample''::text, ''well''::text loop return next rec; end loop;
    for rec in select ''Well''::text, ''WellReagentLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''ImagingEnvironment''::text, ''Image''::text, ''imagingEnvironment''::text loop return next rec; end loop;
    for rec in select ''Reagent''::text, ''WellReagentLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Reagent''::text, ''ReagentAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Detector''::text, ''DetectorSettings''::text, ''detector''::text loop return next rec; end loop;
    for rec in select ''OTF''::text, ''LogicalChannel''::text, ''otf''::text loop return next rec; end loop;
    for rec in select ''LightSettings''::text, ''LogicalChannel''::text, ''lightSourceSettings''::text loop return next rec; end loop;
    for rec in select ''LightSource''::text, ''LightSettings''::text, ''lightSource''::text loop return next rec; end loop;
    for rec in select ''OriginalFile''::text, ''OriginalFileAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''OriginalFile''::text, ''JobOriginalFileLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''OriginalFile''::text, ''Roi''::text, ''source''::text loop return next rec; end loop;
    for rec in select ''OriginalFile''::text, ''PixelsOriginalFileMap''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Job''::text, ''JobOriginalFileLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''WellSampleAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''WellAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''ImageAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''OriginalFileAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''PlaneInfoAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''ChannelAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''ExperimenterGroupAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''RoiAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''AnnotationAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''AnnotationAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''NodeAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''ProjectAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''ReagentAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''PlateAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''ExperimenterAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''ScreenAcquisitionAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''ScreenAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''PixelsAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''DatasetAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''Annotation''::text, ''SessionAnnotationLink''::text, ''child''::text loop return next rec; end loop;
    for rec in select ''FilterSet''::text, ''OTF''::text, ''filterSet''::text loop return next rec; end loop;
    for rec in select ''FilterSet''::text, ''LogicalChannel''::text, ''filterSet''::text loop return next rec; end loop;
    for rec in select ''StatsInfo''::text, ''Channel''::text, ''statsInfo''::text loop return next rec; end loop;
    for rec in select ''Screen''::text, ''ScreenAcquisition''::text, ''screen''::text loop return next rec; end loop;
    for rec in select ''Screen''::text, ''Reagent''::text, ''screen''::text loop return next rec; end loop;
    for rec in select ''Screen''::text, ''ScreenAnnotationLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Screen''::text, ''ScreenPlateLink''::text, ''parent''::text loop return next rec; end loop;
    for rec in select ''Dichroic''::text, ''FilterSet''::text, ''dichroic''::text loop return next rec; end loop;
    for rec in select ''Objective''::text, ''ObjectiveSettings''::text, ''objective''::text loop return next rec; end loop;
    for rec in select ''Objective''::text, ''OTF''::text, ''objective''::text loop return next rec; end loop;
    for rec in select ''Experiment''::text, ''Image''::text, ''experiment''::text loop return next rec; end loop;
    for rec in select ''Experiment''::text, ''MicrobeamManipulation''::text, ''experiment''::text loop return next rec; end loop;
    for rec in select ''DetectorSettings''::text, ''LogicalChannel''::text,''detectorSettings''::text loop return next rec; end loop;
    for rec in select ''Filter''::text, ''FilterSet''::text, ''emFilter''::text loop return next rec; end loop;
    for rec in select ''Filter''::text, ''FilterSet''::text, ''exFilter''::text loop return next rec; end loop;
    for rec in select ''Filter''::text, ''LogicalChannel''::text, ''secondaryEmissionFilter''::text loop return next rec; end loop;
    for rec in select ''Filter''::text, ''LogicalChannel''::text, ''secondaryExcitationFilter''::text loop return next rec; end loop;
    for rec in select ''LogicalChannel''::text, ''Channel''::text, ''logicalChannel''::text loop return next rec; end loop;
    for rec in select ''Shape''::text, ''LogicalChannel''::text, ''shapes''::text loop return next rec; end loop;
    -- Disabled since deleted by upgrade script
    --for rec in select ''Pixels''::text, ''Thumbnail''::text, ''pixels''::text loop return next rec; end loop;
    --for rec in select ''Pixels''::text, ''RenderingDef''::text, ''pixels''::text loop return next rec; end loop;
    return;
end;' language plpgsql;


create or replace function omero_41_check1(ACTION text) returns setof text as $$
declare
    sum text = '';
    msg text;
    rec record;
    rec2 record;
begin

    for rec in select * from omero_41_lockchecks() as (target text, tbl text, col text) loop
        for rec2 in select * from omero_41_check4(rec.target, rec.tbl, rec.col, ACTION) as txt loop
            sum := sum || rec2.txt || chr(10);
            return next rec2.txt;
        end loop;
    end loop;

    -- The following are irregular and so must be custom written.

    for rec in select * from omero_41_check5(
        'select target.id as target_id, target.group_id as target_group, target.owner_id as target_owner, ome_perms(target.permissions) as target_perms, ' ||
        '          tbl.id as tbl_id,       tbl.group_id as tbl_group,       tbl.owner_id as tbl_owner,    ome_perms(tbl.permissions) as tbl_perms ' ||
        '  from lightsource target, lightsource tbl, laser tbl2 ' ||
        ' where target.id = tbl2.pump and tbl2.lightsource_id = tbl.id ' ||
        '   and target.group_id <> tbl.group_id;',
        'LightSource', 'Laser', 'pump', ACTION) as txt loop
        sum := sum || rec.txt || chr(10);
        return next rec.txt;
    end loop;

    for rec in select * from omero_41_check5(
        'select target.id as target_id, target.group_id as target_group, target.owner_id as target_owner, ome_perms(target.permissions) as target_perms, ' ||
        '          tbl.id as tbl_id,       tbl.group_id as tbl_group,       tbl.owner_id as tbl_owner,    ome_perms(tbl.permissions) as tbl_perms ' ||
        '  from originalfile target, annotation tbl ' ||
        ' where target.id = tbl.file ' ||
        '   and target.group_id <> tbl.group_id;',
        'OriginalFile', 'FileAnnotation','file', ACTION) as txt loop
        sum := sum || rec.txt || chr(10);
        return next rec.txt;
    end loop;

    for rec in select * from omero_41_check5(
        'select target.id as target_id, target.group_id as target_group, target.owner_id as target_owner, ome_perms(target.permissions) as target_perms, ' ||
        '          tbl.id as tbl_id,       tbl.group_id as tbl_group,       tbl.owner_id as tbl_owner,    ome_perms(tbl.permissions) as tbl_perms ' ||
        '  from thumbnail target, annotation tbl ' ||
        ' where target.id = tbl.thumbnail ' ||
        '   and target.group_id <> tbl.group_id;',
        'Thumbnail', 'ThumbnailAnnotation','thumbnail', ACTION) as txt loop
        sum := sum || rec.txt || chr(10);
        return next rec.txt;
    end loop;

    -- The following are disabled since they are system types in 4.2
    -- select * from omero_41_check('ExperimenterGroup', 'GroupExperimenterMap','parent');
    -- select * from omero_41_check('ExperimenterGroup', 'ExperimenterGroupAnnotationLink','parent');

    for rec2 in select * from omero_unnest(string_to_array(
                'acquisitionmode annotation annotationannotationlink arc arctype binning channel ' ||
                'channelannotationlink channelbinding codomainmapcontext contrastmethod ' ||
                'contraststretchingcontext correction dataset datasetannotationlink ' ||
                'datasetimagelink dbpatch detector detectorsettings detectortype dichroic ' ||
                'dimensionorder event eventlog eventtype experiment experimenter ' ||
                'experimenterannotationlink experimentergroup experimentergroupannotationlink ' ||
                'experimenttype externalinfo family filament filamenttype filter filterset ' ||
                'filtertype format groupexperimentermap illumination image imageannotationlink ' ||
                'imagingenvironment immersion importjob instrument job joboriginalfilelink ' ||
                'jobstatus laser lasermedium lasertype lightemittingdiode lightsettings ' ||
                'lightsource link logicalchannel medium microbeammanipulation ' ||
                'microbeammanipulationtype microscope microscopetype node nodeannotationlink ' ||
                'objective objectivesettings originalfile originalfileannotationlink otf ' ||
                'photometricinterpretation pixels pixelsannotationlink pixelsoriginalfilemap ' ||
                'pixelstype planeinfo planeinfoannotationlink planeslicingcontext plate ' ||
                'plateannotationlink project projectannotationlink projectdatasetlink pulse ' ||
                'quantumdef reagent reagentannotationlink renderingdef renderingmodel ' ||
                'reverseintensitycontext roi roiannotationlink screen screenacquisition ' ||
                'screenacquisitionannotationlink screenacquisitionwellsamplelink ' ||
                'screenannotationlink screenplatelink scriptjob session sessionannotationlink ' ||
                'shape share sharemember stagelabel statsinfo thumbnail transmittancerange well ' ||
                'wellannotationlink wellreagentlink wellsample wellsampleannotationlink', ' ')) as txt loop

        ----- MODIFICATION:
        ----- Disabling call to omero_41_perms since we don't need to check all permissions
        ----- for OMERO4.4
        -----
        ----- for rec in select * from omero_41_perms(rec2.txt, ACTION) as txt loop
        -----     sum := sum || rec.txt || chr(10);
        -----     return next rec.txt;
        ----- end loop;
        -----
    end loop;

    if ACTION = 'ABORT' and char_length(sum) > 0 then
        msg := chr(10) || sum || chr(10);
        msg := msg || 'ERROR ON omero_41_check:' || chr(10);
        msg := msg || 'Your database has data which is incompatible with 4.2 and will need to be manually updated' || chr(10);
        msg := msg || 'Contact ome-users@lists.openmicroscopy.org.uk for help adjusting your data.' || chr(10) || chr(10);
        raise exception '%', msg;
    end if;

    return;

end;$$ language plpgsql;


-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

select * from omero_41_check1(cast(:ACTION as text));

-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


drop function omero_41_check1(text);
drop function omero_41_perms(text, text);
drop function omero_41_check4(text, text, text, text);
drop function omero_41_check5(text, text, text, text, text);
drop function omero_unnest(anyarray);
drop function omero_41_lockchecks();
-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


----
--
-- #1176 : create our own nextval() functionality for more consistent
-- sequence operation in hibernate. This functionality was updated for
-- OMERO 4.2 (#2508) in order to prevent logging during triggers, which
-- requires re-creating a sequence for every OMERO model type.
--

CREATE OR REPLACE FUNCTION upgrade_sequence(seqname VARCHAR) RETURNS void
    AS '
    BEGIN

        PERFORM c.relname AS sequencename FROM pg_class c WHERE (c.relkind = ''S'') AND c.relname = seqname;
        IF NOT FOUND THEN
            EXECUTE ''CREATE SEQUENCE '' || seqname;
        END IF;

        PERFORM next_val FROM seq_table WHERE sequence_name = seqname;
        IF FOUND THEN
            PERFORM SETVAL(seqname, next_val) FROM seq_table WHERE sequence_name = seqname;
        ELSE
            INSERT INTO seq_table (sequence_name, next_val) VALUES (seqname, 1);
        END IF;

    END;'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ome_unnest(anyarray)
  RETURNS SETOF anyelement AS '
    SELECT $1[i] FROM
        generate_series(array_lower($1,1),
                        array_upper($1,1)) i;
' LANGUAGE 'sql' IMMUTABLE;

SELECT count(upgrade_sequence(x)) FROM ome_unnest(string_to_array(
    'seq_wellsampleannotationlink,seq_wellannotationlink,seq_filtertype,seq_dataset,seq_plate,seq_thumbnail,'||
    'seq_immersion,seq_channel,seq_imageannotationlink,seq_link,seq_lightpathemissionfilterlink,seq_arctype,'||
    'seq_experimenttype,seq_filtersetemissionfilterlink,seq_filtersetexcitationfilterlink,seq_microscope,'||
    'seq_originalfileannotationlink,seq_wellsample,seq_planeinfo,seq_lightpathexcitationfilterlink,'||
    'seq_groupexperimentermap,seq_planeinfoannotationlink,seq_transmittancerange,seq_wellreagentlink,'||
    'seq_eventlog,seq_quantumdef,seq_namespace,seq_image,seq_renderingmodel,seq_microbeammanipulation,'||
    'seq_joboriginalfilelink,seq_experimentergroup,seq_renderingdef,seq_datasetimagelink,seq_codomainmapcontext,'||
    'seq_eventtype,seq_project,seq_microscopetype,seq_channelannotationlink,seq_filamenttype,seq_stagelabel,'||
    'seq_photometricinterpretation,seq_experimentergroupannotationlink,seq_pixels,seq_lightpath,seq_roi,'||
    'seq_roiannotationlink,seq_externalinfo,seq_annotationannotationlink,seq_objectivesettings,seq_lasertype,'||
    'seq_nodeannotationlink,seq_dimensionorder,seq_binning,seq_instrument,seq_namespaceannotationlink,seq_well,'||
    'seq_family,seq_imagingenvironment,seq_illumination,seq_projectannotationlink,seq_detectortype,seq_reagent,'||
    'seq_pulse,seq_detector,seq_otf,seq_reagentannotationlink,seq_lightsettings,seq_originalfile,seq_lightsource,'||
    'seq_annotation,seq_job,seq_sharemember,seq_dbpatch,seq_filterset,seq_projectdatasetlink,seq_plateannotationlink,'||
    'seq_experimenterannotationlink,seq_channelbinding,seq_microbeammanipulationtype,seq_medium,seq_statsinfo,'||
    'seq_lasermedium,seq_pixelstype,seq_screen,seq_dichroic,seq_session,seq_plateacquisition,seq_screenannotationlink,'||
    'seq_format,seq_node,seq_pixelsannotationlink,seq_objective,seq_datasetannotationlink,seq_experiment,seq_detectorsettings,'||
    'seq_correction,seq_filter,seq_plateacquisitionannotationlink,seq_pixelsoriginalfilemap,seq_logicalchannel,'||
    'seq_sessionannotationlink,seq_screenplatelink,seq_shape,seq_experimenter,seq_acquisitionmode,seq_event,seq_jobstatus,seq_contrastmethod', ',')) as x;

DROP FUNCTION upgrade_sequence(VARCHAR);
DROP FUNCTION ome_unnest(anyarray);

-- These renamings allow us to reuse the Hibernate-generated tables
-- for sequence generation. Eventually, a method might be found to
-- make Hibernate generate them for us.

CREATE SEQUENCE _lock_seq;
CREATE TABLE _lock_ids (name VARCHAR(255) NOT NULL);
ALTER TABLE _lock_ids ADD COLUMN id int PRIMARY KEY DEFAULT nextval('_lock_seq');
CREATE UNIQUE INDEX _lock_ids_name ON _lock_ids (name);
INSERT INTO _lock_ids (name) SELECT sequence_name FROM seq_table;
DROP TABLE seq_table;

-- The primary nextval function used by OMERO. Acquires an advisory lock
-- then generates a sequence of ids as quickly as possible using generate_series.
CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR, increment int4) RETURNS INT8 AS '
DECLARE
      Lid  int4;
      nv   int8;
BEGIN
      SELECT id INTO Lid FROM _lock_ids WHERE name = seq;
      IF Lid IS NULL THEN
          SELECT INTO Lid nextval(''_lock_seq'');
          INSERT INTO _lock_ids (id, name) VALUES (Lid, seq);
      END IF;

      BEGIN
        PERFORM pg_advisory_lock(1, Lid);
      EXCEPTION
        WHEN undefined_function THEN
          RAISE DEBUG ''No function pg_advisory_lock'';
      END;
      PERFORM nextval(seq) FROM generate_series(1, increment);
      SELECT currval(seq) INTO nv;
      BEGIN
        PERFORM pg_advisory_unlock(1, Lid);
      EXCEPTION
        WHEN undefined_function THEN
          RAISE DEBUG ''No function pg_advisory_unlock'';
      END;

      RETURN nv;

END;' LANGUAGE plpgsql;

--
-- Aliases
--

CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR) RETURNS INT8 AS '
BEGIN
      RETURN ome_nextval(seq, 1);
END;' LANGUAGE plpgsql;

--
-- Replace the one table which had a default of nextval
--
ALTER TABLE dbpatch ALTER COLUMN id SET DEFAULT ome_nextval('seq_dbpatch');

----
--
-- SPW
--

-- Add new SPW data structures
CREATE TABLE plateacquisitionannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL
);

CREATE TABLE plateacquisition (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	maximumfieldcount integer,
	name character varying(255) NOT NULL,
        starttime timestamp without time zone,
        endtime timestamp without time zone,
        plate bigint NOT NULL,
	version integer,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL
);

ALTER TABLE plateacquisition
	ADD CONSTRAINT plateacquisition_pkey PRIMARY KEY (id);

ALTER TABLE plate
	ADD COLUMN cols integer,
	ADD COLUMN rows integer;

ALTER TABLE wellsample
        ADD COLUMN plateacquisition bigint;

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_plateacquisition_plateacquisition
        FOREIGN KEY (plateacquisition) REFERENCES plateacquisition(id);

ALTER TABLE wellsample
        RENAME COLUMN timepoint to old_timepoint;

ALTER TABLE wellsample
        ADD COLUMN timepoint timestamp without time zone;

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT plateacquisitionannotationlink_external_id_key UNIQUE (external_id),
        ADD CONSTRAINT plateacquisitionannotationlink_parent_key UNIQUE (parent, child, owner_id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_parent_plateacquisition FOREIGN KEY (parent) REFERENCES plateacquisition(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
	ADD CONSTRAINT plateacquisitionannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE plateacquisition
	ADD CONSTRAINT plateacquisition_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fkplateacquisition_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fkplateacquisition_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fkplateacquisition_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fkplateacquisition_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fkplateacquisition_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
        ADD CONSTRAINT fkplateacquisition_plate_plate FOREIGN KEY (plate) REFERENCES plate(id);

-- Leaving timepoint null

CREATE OR REPLACE FUNCTION omero_convert_spw_42() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    UPDATE plate SET cols = max.c, rows = max.r
      FROM
        (SELECT p2.id as plate_id, max("column") as c, max(row) as r
           FROM plate p2, well w
          WHERE p2.id = w.plate
       GROUP BY p2.id) as max
     WHERE id = max.plate_id;

    INSERT INTO plateacquisition (id, permissions, starttime, endtime, version, creation_id, external_id, group_id, owner_id, update_id, plate, maximumfieldcount, name)
    SELECT sa.sa_id, permissions, starttime, endtime, version, creation_id, external_id, group_id, owner_id, update_id, plate.plate_id, count.maxfieldcount, ''''
      FROM
        (SELECT sa.id as sa_id, sa.permissions, sa.starttime, sa.endtime, sa.version, sa.creation_id, sa.external_id, sa.group_id, sa.owner_id, sa.update_id
           FROM screenacquisition sa) as sa,
        (SELECT distinct link.parent as sa_id, p.id as plate_id
           FROM plate p, well w, wellsample ws, screenacquisitionwellsamplelink link
          WHERE link.child = ws.id AND ws.well = w.id AND w.plate = p.id) as plate,
        (SELECT plate_id, max(fieldcount) as maxfieldcount
           FROM
            (SELECT p.id as plate_id, w.id as well_id, count(ws.id) as fieldcount
               FROM wellsample ws, well w, plate p
              WHERE p.id = w.plate and w.id = ws.well
           GROUP BY p.id, w.id) as inner_count
       GROUP BY plate_id) as count
     WHERE sa.sa_id = plate.sa_id AND plate.plate_id = count.plate_id;

    UPDATE wellsample SET plateacquisition = sa.id
      FROM screenacquisition sa, screenacquisitionwellsamplelink link
     WHERE sa.id = link.parent AND link.child = wellsample.id;

    INSERT INTO plateacquisitionannotationlink (id, permissions, version, child, creation_id, external_id, group_id, owner_id, update_id, parent)
    SELECT id, permissions, version, child, creation_id, external_id, group_id, owner_id, update_id, parent FROM screenacquisitionannotationlink
     WHERE parent IN (SELECT id FROM plateacquisition);

    PERFORM setval(''seq_plateacquisition'', nextval(''seq_plateacquisition''));
    PERFORM setval(''seq_plateacquisitionannotationlink'', nextval(''seq_plateacquisitionannotationlink''));

END;' LANGUAGE plpgsql;
SELECT omero_convert_spw_42();
DROP FUNCTION omero_convert_spw_42();

-- Remove old SPW data structures
DROP VIEW count_screenacquisition_annotationlinks_by_owner;
DROP VIEW count_wellsample_screenacquisitionlinks_by_owner;
DROP VIEW count_screenacquisition_wellsamplelinks_by_owner;
DROP TABLE screenacquisitionannotationlink;
DROP TABLE screenacquisitionwellsamplelink;
DROP TABLE screenacquisition;

-- #2428 convention namings

UPDATE plate SET rowNamingConvention = 'letter'
 WHERE rowNamingConvention ~ '[a-zA-Z]';

UPDATE plate SET rowNamingConvention = 'number'
 WHERE rowNamingConvention ~ '[0-9]';

UPDATE plate SET columnNamingConvention = 'letter'
 WHERE columnNamingConvention ~ '[a-zA-Z]';

UPDATE plate SET columnNamingConvention = 'number'
 WHERE columnNamingConvention ~ '[0-9]';

-- #1640

create unique index well_col_row on well(plate, "column", "row");

----
--
-- Instruments/Filters
--

-- Add new instrument data structures
ALTER TABLE logicalchannel
	ADD COLUMN lightpath bigint;

CREATE TABLE lightpathemissionfilterlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL
);

CREATE TABLE lightpathexcitationfilterlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL,
	parent_index integer NOT NULL
);

CREATE TABLE filtersetemissionfilterlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id)
);

CREATE TABLE filtersetexcitationfilterlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id)
);

CREATE TABLE lightpath (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	dichroic bigint
);

ALTER TABLE lightpath
	ADD CONSTRAINT lightpath_pkey PRIMARY KEY (id),
	ADD CONSTRAINT lightpath_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fklightpath_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fklightpath_dichroic_dichroic FOREIGN KEY (dichroic) REFERENCES dichroic(id),
	ADD CONSTRAINT fklightpath_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fklightpath_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fklightpath_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fklightpath_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE logicalchannel
	ADD CONSTRAINT fklogicalchannel_lightpath_lightpath FOREIGN KEY (lightpath) REFERENCES lightpath(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT filtersetemissionfilterlink_parent_key UNIQUE (parent, child, owner_id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_parent_filterset FOREIGN KEY (parent) REFERENCES filterset(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT filtersetexcitationfilterlink_parent_key UNIQUE (parent, child, owner_id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_parent_filterset FOREIGN KEY (parent) REFERENCES filterset(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE lightpathemissionfilterlink
        ADD CONSTRAINT lightpathemissionfilterlink_parent_key UNIQUE (parent, child, owner_id),
	ADD CONSTRAINT lightpathemissionfilterlink_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fklightpathemissionfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_parent_lightpath FOREIGN KEY (parent) REFERENCES lightpath(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
	ADD CONSTRAINT lightpathemissionfilterlink_pkey PRIMARY KEY (id);

ALTER TABLE lightpathexcitationfilterlink
        ADD CONSTRAINT lightpathexcitationfilterlink_parent_key1 UNIQUE (parent, child, owner_id),
	ADD CONSTRAINT lightpathexcitationfilterlink_parent_key UNIQUE (parent, parent_index),
	ADD CONSTRAINT lightpathexcitationfilterlink_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_parent_lightpath FOREIGN KEY (parent) REFERENCES lightpath(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
	ADD CONSTRAINT lightpathexcitationfilterlink_pkey PRIMARY KEY (id);

  CREATE OR REPLACE FUNCTION lightpathexcitationfilterlink_parent_index_move() RETURNS "trigger" AS '
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.parent = old.parent AND new.parent_index = old.parent_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM lightpathexcitationfilterlink
       WHERE parent = new.parent AND parent_index = new.parent_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE ''Remapping lightpathexcitationfilterlink % via (-1 - oldvalue )'', duplicate;
          UPDATE lightpathexcitationfilterlink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;' LANGUAGE plpgsql;

  CREATE TRIGGER lightpathexcitationfilterlink_parent_index_trigger
        BEFORE UPDATE ON lightpathexcitationfilterlink
        FOR EACH ROW EXECUTE PROCEDURE lightpathexcitationfilterlink_parent_index_move ();

----
--
-- Convert old instrument data
--

CREATE OR REPLACE FUNCTION omero_convert_instruments_42() RETURNS void AS '
DECLARE
    rec RECORD;         -- General purpose record
    fs_rec RECORD;      -- Filterset for any given logicalchannel
    lightpath_id  INT8; -- Newly created lightpath per logicalchannel
    ex_count INT4 := 0; -- Count of excitation for setting parent_index
BEGIN

    FOR rec IN SELECT * FROM logicalchannel
                WHERE filterset IS NOT NULL OR secondaryemissionfilter IS NOT NULL OR secondaryexcitationfilter IS NOT NULL LOOP

        -- If any of the 3 fields is not null, then we will need to generate a light path object to hold them.
        SELECT INTO lightpath_id nextval(''seq_lightpath'');
        INSERT INTO lightpath (id, permissions, creation_id, group_id, owner_id, update_id)
             SELECT lightpath_id, rec.permissions, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id;

        -- First, we parse the filterset if present
        IF rec.filterset IS NOT NULL THEN
            SELECT INTO fs_rec * FROM filterset WHERE id = rec.filterset;
            UPDATE lightpath SET dichroic = fs_rec.dichroic WHERE id = lightpath_id;

            IF fs_rec.emfilter IS NOT NULL THEN
                INSERT INTO lightpathemissionfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent)
                     SELECT nextval(''seq_lightpathemissionfilterlink''), rec.permissions, fs_rec.emfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, lightpath_id;
            END IF;

            IF fs_rec.exfilter IS NOT NULL THEN
                INSERT INTO lightpathexcitationfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent, parent_index)
                     SELECT nextval(''seq_lightpathexcitationfilterlink''), rec.permissions, fs_rec.exfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, lightpath_id, ex_count;
                ex_count := ex_count + 1;
            END IF;

        END IF;

        -- Now we parse out the secondary filters, which may become primary filters if no filterset was present.
        IF rec.secondaryemissionfilter IS NOT NULL THEN
            INSERT INTO lightpathemissionfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent)
                 SELECT nextval(''seq_lightpathemissionfilterlink''), rec.permissions, rec.secondaryemissionfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, lightpath_id;
        END IF;

        IF rec.secondaryexcitationfilter IS NOT NULL THEN
            INSERT INTO lightpathexcitationfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent, parent_index)
                 SELECT nextval(''seq_lightpathexcitationfilterlink''), rec.permissions, rec.secondaryexcitationfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, lightpath_id, ex_count;
            ex_count := ex_count + 1;
        END IF;

        UPDATE logicalchannel SET lightpath = lightpath_id WHERE id = rec.id;

    END LOOP;

    -- Now that we haveve culled the logicalchannels, update the filtersets themselves
    -- by creating links for the two columns which will be deleted.
    FOR rec IN SELECT * FROM filterset
                WHERE emfilter IS NOT NULL or exfilter IS NOT NULL LOOP

        IF rec.emfilter IS NOT NULL THEN
            INSERT INTO filtersetemissionfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent)
                 SELECT nextval(''seq_filtersetemissionfilterlink''), rec.permissions, rec.emfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, rec.id;
        END IF;

        IF rec.exfilter IS NOT NULL THEN
            INSERT INTO filtersetexcitationfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent)
                 SELECT nextval(''seq_filtersetexcitationfilterlink''), rec.permissions, rec.exfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, rec.id;
        END IF;
    END LOOP;


END;' LANGUAGE plpgsql;
SELECT omero_convert_instruments_42();
DROP FUNCTION omero_convert_instruments_42();


-- Remove old instrument data structures
ALTER TABLE logicalchannel
	DROP COLUMN secondaryemissionfilter,
	DROP COLUMN secondaryexcitationfilter,
        DROP CONSTRAINT fklogicalchannel_shapes_shape,
        DROP COLUMN shapes;

ALTER TABLE filterset
	DROP COLUMN emfilter,
	DROP COLUMN exfilter;

----
--
-- New system types
--

CREATE table namespace (
        id int8 not null,
        description text,
        permissions int8 not null,
        display bool,
        keywords text[],
        multivalued bool,
        name varchar(255) not null,
        version int4,
        creation_id int8 not null,
        external_id int8,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null
    );;

CREATE TABLE namespaceannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL
);

ALTER TABLE namespace
	ADD CONSTRAINT namespace_pkey PRIMARY KEY (id),
	ADD CONSTRAINT namespace_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fknamespace_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
        ADD CONSTRAINT fknamespace_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
        ADD CONSTRAINT fknamespace_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
        ADD CONSTRAINT fknamespace_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
        ADD CONSTRAINT fknamespace_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT namespaceannotationlink_external_id_key UNIQUE (external_id),
        ADD CONSTRAINT namespaceannotationlink_parent_key UNIQUE (parent, child, owner_id),
	ADD CONSTRAINT fknamespaceannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id),
	ADD CONSTRAINT fknamespaceannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fknamespaceannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fknamespaceannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fknamespaceannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fknamespaceannotationlink_parent_namespace FOREIGN KEY (parent) REFERENCES namespace(id),
	ADD CONSTRAINT fknamespaceannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
        ADD CONSTRAINT namespaceannotationlink_pkey PRIMARY KEY (id);


CREATE UNIQUE INDEX namespace_name ON namespace USING btree (name);

CREATE TABLE parsejob (
	params bytea,
	job_id bigint NOT NULL
);

ALTER TABLE parsejob
	ADD CONSTRAINT fkparsejob_job_id_job FOREIGN KEY (job_id) REFERENCES job(id),
	ADD CONSTRAINT parsejob_pkey PRIMARY KEY (job_id);

ALTER TABLE session
	DROP COLUMN defaultpermissions;


CREATE OR REPLACE FUNCTION upgrade_original_metadata_txt() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    FOR rec IN SELECT o.id as file, o.path, o.name, i.id as image FROM originalfile o, image i, imageannotationlink l, annotation a
                WHERE o.id = a.file AND a.id = l.child AND l.parent = i.id
                  AND o.path LIKE ''%tmp%omero_%metadata%.txt'' AND o.name LIKE ''original_metadata.txt''
                  AND a.ns = ''openmicroscopy.org/omero/import/companionFile'' LOOP

        -- An original_metadata.txt should not be attached to multiple images
        IF substring(rec.path from 1 for 16) = ''/imported_image/'' THEN
            RAISE EXCEPTION ''Already modified! Image:%'', rec.image;
        END IF;

        UPDATE originalfile SET path = ''/openmicroscopy.org/omero/image_files/''||rec.image||''/'' WHERE id = rec.file;
    END LOOP;

END;' LANGUAGE plpgsql;

SELECT upgrade_original_metadata_txt();
DROP FUNCTION upgrade_original_metadata_txt();

UPDATE annotation SET ns = 'openmicroscopy.org/omero/movie' WHERE ns IN
    ( 'openmicroscopy.org/omero/movie/mpeg',
      'openmicroscopy.org/omero/movie/qt',
      'openmicroscopy.org/omero/movie/wmv');

alter  table originalfile drop column url;
alter  table originalfile alter column path TYPE text;
alter  table originalfile add column mimetype varchar(255) default 'application/octet-stream';
update originalfile set mimetype = fmt.value from Format fmt where format = fmt.id;
alter  table originalfile drop column format;

alter  table originalfile add column repo varchar(36);
alter  table originalfile add column params text[2][];
create index originalfile_mime_index on originalfile (mimetype);
create index originalfile_repo_index on originalfile (repo);
create unique index originalfile_repo_path_index on originalfile (repo, path, name) where repo is not null;

ALTER TABLE image
	ADD COLUMN partial boolean,
	ADD COLUMN format bigint;

ALTER TABLE image
	ADD CONSTRAINT fkimage_format_format FOREIGN KEY (format) REFERENCES format(id);

-- Not attempting to fill Image.format

ALTER TABLE pixels
	DROP COLUMN url,
	ADD COLUMN path text,
	ADD COLUMN name text,
	ADD COLUMN repo character varying(36),
	ADD COLUMN params text[];

CREATE INDEX pixels_repo_index ON pixels USING btree (repo);

ALTER TABLE thumbnail DROP COLUMN url;

----
--
-- Modify system types
--
CREATE OR REPLACE FUNCTION upgrade_group_owners() RETURNS void AS '
DECLARE
    mid INT8;
    rec RECORD;
BEGIN

    ALTER TABLE groupexperimentermap ADD COLUMN owner boolean;

    -- For every group, if the owner is not in the group, add them
    -- If they are in the group, set the boolean flag.
    FOR rec IN SELECT * FROM experimentergroup LOOP
        SELECT INTO mid id FROM groupexperimentermap WHERE child = rec.owner_id;
        IF NOT FOUND THEN
            SELECT INTO mid nextval(''seq_groupexperimentermap'');
            INSERT INTO groupexperimentermap (id, permissions, version, parent, child, child_index)
                 SELECT mid, -35, 0, rec.id, rec.owner_id, max(child_index) + 1
                   FROM groupexperimentermap WHERE child = rec.owner_id;
        ELSE
            UPDATE groupexperimentermap SET owner = true WHERE id = mid;
        END IF;
    END LOOP;

    UPDATE groupexperimentermap SET owner = FALSE WHERE owner IS NULL;

    ALTER TABLE groupexperimentermap ALTER COLUMN owner SET NOT NULL;

END;' LANGUAGE plpgsql;

SELECT upgrade_group_owners();
DROP FUNCTION upgrade_group_owners();

----
--
-- Remove old system types
--
DROP VIEW count_experimentergroup_groupexperimentermap_by_owner;
DROP VIEW count_experimenter_groupexperimentermap_by_owner;

ALTER TABLE groupexperimentermap
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id,
	DROP COLUMN update_id;

----
--
-- ROI modifications
--

CREATE OR REPLACE FUNCTION hex_to_dec(t text) RETURNS integer AS $$
DECLARE
    r RECORD;
    sql VARCHAR;
BEGIN
    sql := 'SELECT x';
    sql := sql || E'\'';
    sql := sql || t;
    sql := sql || E'\'';
    sql := sql || '::integer AS hex';
    FOR r IN EXECUTE sql LOOP
        RETURN r.hex;
    END LOOP;
END;$$ LANGUAGE plpgsql IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION hex_to_argb(color VARCHAR, opacity FLOAT8) RETURNS INT8 AS '
DECLARE

    OFFST INT8 := 4294967296;
    MAXINT INT8 := 2147483647;
    MININT INT8 := -2147483648;

    rval INT8;
    gval INT8;
    bval INT8;
    aval INT8;
    argb INT8;
BEGIN

    IF opacity < 0.0 or opacity > 1.0 THEN
        RAISE EXCEPTION ''Opacity out of bounds: %'', opacity;
    ELSIF substring(color from 1 for 1) = ''#'' THEN
        aval := cast(round((255.0 * opacity)::numeric) as int8);
        rval := hex_to_dec(substring(color from 2 for 2));
        gval := hex_to_dec(substring(color from 4 for 2));
        bval := hex_to_dec(substring(color from 6 for 2));
    ELSE
        RAISE EXCEPTION ''Unknown color format: %'', color;
    END IF;

    argb := aval << 24;
    argb := argb + (rval << 16);
    argb := argb + (gval << 8);
    argb := argb + bval;

    IF argb < 0 or argb > OFFST THEN
        RAISE EXCEPTION ''Overflow: % (color=%, opacity=%, argb=(%,%,%,%))'',
            argb, color, opacity, aval, rval, gval, bval;
    ELSIF argb > MAXINT THEN
        argb := argb - OFFST;
    END IF;

    IF argb < MININT or argb > MAXINT THEN
        RAISE EXCEPTION ''Late overflow: %'', argv;
    END IF;

    RETURN argb;

END;' LANGUAGE plpgsql IMMUTABLE STRICT;

ALTER TABLE roi
	ADD COLUMN keywords text[],
	ADD COLUMN namespaces text[];

ALTER TABLE shape
	ADD COLUMN thec integer,
	ALTER COLUMN points TYPE text,
	ALTER COLUMN d TYPE text;

-- r7154
ALTER TABLE shape ADD COLUMN new_fillcolor integer;
ALTER TABLE shape ADD COLUMN new_strokecolor integer;
UPDATE shape SET new_fillcolor = hex_to_argb(fillcolor, fillopacity);
UPDATE shape SET new_strokecolor = hex_to_argb(strokecolor, strokeopacity);
ALTER TABLE shape DROP COLUMN fillopacity;
ALTER TABLE shape DROP COLUMN strokeopacity;
ALTER TABLE shape DROP COLUMN fillcolor;
ALTER TABLE shape DROP COLUMN strokecolor;
ALTER TABLE shape RENAME COLUMN new_fillcolor to fillcolor;
ALTER TABLE shape RENAME COLUMN new_strokecolor to strokecolor;
DROP FUNCTION hex_to_dec(text);
DROP FUNCTION hex_to_argb(VARCHAR, FLOAT8);

----
--
-- Make all enumerations system types
--

ALTER TABLE acquisitionmode
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE arctype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE binning
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE contrastmethod
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE correction
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE detectortype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE dimensionorder
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE eventtype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE experimentergroup
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id,
	DROP COLUMN update_id;

ALTER TABLE experimenttype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE family
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE filamenttype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE filtertype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE format
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE illumination
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE immersion
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE jobstatus
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE lasermedium
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE lasertype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE medium
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE microbeammanipulationtype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE microscopetype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE photometricinterpretation
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE pixelstype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE pulse
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE renderingmodel
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

--
-- Enumeration values
--
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'LCM';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'FSM';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'PALM';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'STED';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'STORM';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'TIRF';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'LaserScanningConfocalMicroscopy';

update logicalchannel set mode = am_new.id
  from acquisitionmode am_old, acquisitionmode am_new
 where mode = am_old.id
   and am_old.value in ('LaserScanningConfocal', 'LaserScanningMicroscopy')
   and am_new.value = 'LaserScanningConfocalMicroscopy';

delete from acquisitionmode where value in ('LaserScanningConfocal', 'LaserScanningMicroscopy');

insert into detectortype (id,permissions,value)
    select nextval('seq_detectortype'),-35,'EBCCD';

insert into filtertype (id,permissions,value)
    select nextval('seq_filtertype'),-35,'Dichroic';
insert into filtertype (id,permissions,value)
    select nextval('seq_filtertype'),-35,'NeutralDensity';

insert into microbeammanipulationtype (id,permissions,value)
    select nextval('seq_microbeammanipulationtype'),-35,'FLIP';
insert into microbeammanipulationtype (id,permissions,value)
    select nextval('seq_microbeammanipulationtype'),-35,'InverseFRAP';

-- Deleting from Format. If any of these have been assigned to an Image
-- this will fail.
delete from format where value in
    ('application/msword', 'application/octet-stream', 'application/pdf', 'application/vnd.ms-excel',
     'application/vnd.ms-powerpoint', 'audio/basic', 'audio/mpeg', 'audio/wav',
     'image/bmp', 'image/gif', 'image/jpeg', 'image/png', 'image/tiff',
     'text/csv', 'text/html', 'text/ini', 'text/plain', 'text/richtext',
     'text/rtf', 'text/x-python', 'text/xml',
     'video/jpeg2000', 'video/mp4', 'video/mpeg', 'video/quicktime');

----
--
-- Unify Annotation types (#2354, r7000)
--

ALTER TABLE annotation
	DROP COLUMN thumbnail,
	ADD COLUMN termvalue text;

----
--
-- Fix shares with respect to group permissions (#1434, #2327, r6882)
--

ALTER TABLE share
        ADD COLUMN "group" bigint;

ALTER TABLE share
        ADD CONSTRAINT fkshare_group_experimentergroup FOREIGN KEY ("group") REFERENCES experimentergroup(id);

UPDATE share
        SET "group" = m.parent
            FROM session sess, experimenter e, groupexperimentermap m
           WHERE sess.id = session_id AND sess.owner = e.id AND e.id = m.child AND m.child_index = 0;

ALTER TABLE share
        ALTER COLUMN "group" SET NOT NULL;

ALTER TABLE sharemember
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id,
	DROP COLUMN update_id;

----
--
-- #1390 Triggers for keeping the search index up-to-date.
--

CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        rec RECORD;
        eid int8;
    BEGIN

        FOR rec IN SELECT id, parent FROM imageannotationlink WHERE child = new.id LOOP
            SELECT into eid ome_nextval(''seq_event'');
            INSERT INTO event (id, permissions, status, time, experimenter, experimentergroup, session, type)
                 SELECT eid, -35, ''TRIGGERED'', now(), 0, 0, 0, 0;
            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                 SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, rec.parent, ''ome.model.core.Image'', eid;
        END LOOP;

        RETURN new;

    END;'
LANGUAGE plpgsql;

CREATE TRIGGER annotation_trigger
        AFTER UPDATE ON annotation
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_update_event_trigger();


CREATE OR REPLACE FUNCTION annotation_link_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        eid int8;
    BEGIN

        SELECT into eid ome_nextval(''seq_event'');
        INSERT INTO event (id, permissions, status, time, experimenter, experimentergroup, session, type)
                SELECT eid, -35, ''TRIGGERED'', now(), 0, 0, 0, 0;
        INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, new.parent, ''ome.model.core.Image'', eid;

        RETURN new;

    END;'
LANGUAGE plpgsql;

CREATE TRIGGER image_annotation_link_event_trigger
        AFTER UPDATE ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger();


----
--
-- Create views needed by all other types
--

CREATE VIEW count_filterset_emissionfilterlink_by_owner AS
	SELECT filtersetemissionfilterlink.parent AS filterset_id, filtersetemissionfilterlink.owner_id, count(*) AS count FROM filtersetemissionfilterlink GROUP BY filtersetemissionfilterlink.parent, filtersetemissionfilterlink.owner_id ORDER BY filtersetemissionfilterlink.parent;

CREATE VIEW count_filterset_excitationfilterlink_by_owner AS
	SELECT filtersetexcitationfilterlink.parent AS filterset_id, filtersetexcitationfilterlink.owner_id, count(*) AS count FROM filtersetexcitationfilterlink GROUP BY filtersetexcitationfilterlink.parent, filtersetexcitationfilterlink.owner_id ORDER BY filtersetexcitationfilterlink.parent;

CREATE VIEW count_lightpath_emissionfilterlink_by_owner AS
	SELECT lightpathemissionfilterlink.parent AS lightpath_id, lightpathemissionfilterlink.owner_id, count(*) AS count FROM lightpathemissionfilterlink GROUP BY lightpathemissionfilterlink.parent, lightpathemissionfilterlink.owner_id ORDER BY lightpathemissionfilterlink.parent;

CREATE VIEW count_lightpath_excitationfilterlink_by_owner AS
	SELECT lightpathexcitationfilterlink.parent AS lightpath_id, lightpathexcitationfilterlink.owner_id, count(*) AS count FROM lightpathexcitationfilterlink GROUP BY lightpathexcitationfilterlink.parent, lightpathexcitationfilterlink.owner_id ORDER BY lightpathexcitationfilterlink.parent;

CREATE VIEW count_namespace_annotationlinks_by_owner AS
	SELECT namespaceannotationlink.parent AS namespace_id, namespaceannotationlink.owner_id, count(*) AS count FROM namespaceannotationlink GROUP BY namespaceannotationlink.parent, namespaceannotationlink.owner_id ORDER BY namespaceannotationlink.parent;

CREATE VIEW count_plateacquisition_annotationlinks_by_owner AS
	SELECT plateacquisitionannotationlink.parent AS plateacquisition_id, plateacquisitionannotationlink.owner_id, count(*) AS count FROM plateacquisitionannotationlink GROUP BY plateacquisitionannotationlink.parent, plateacquisitionannotationlink.owner_id ORDER BY plateacquisitionannotationlink.parent;

CREATE VIEW count_filter_emissionfilterlink_by_owner AS
        SELECT filtersetemissionfilterlink.child AS filter_id, filtersetemissionfilterlink.owner_id, count(*) AS count FROM filtersetemissionfilterlink GROUP BY filtersetemissionfilterlink.child, filtersetemissionfilterlink.owner_id ORDER BY filtersetemissionfilterlink.child;

CREATE VIEW count_filter_excitationfilterlink_by_owner AS
        SELECT filtersetexcitationfilterlink.child AS filter_id, filtersetexcitationfilterlink.owner_id, count(*) AS count FROM filtersetexcitationfilterlink GROUP BY filtersetexcitationfilterlink.child, filtersetexcitationfilterlink.owner_id ORDER BY filtersetexcitationfilterlink.child;

DROP VIEW count_experimenter_annotationlinks_by_owner;

DROP VIEW count_experimentergroup_annotationlinks_by_owner;

DROP VIEW count_node_annotationlinks_by_owner;

DROP VIEW count_session_annotationlinks_by_owner;


----
--
-- Replacing (parent, child) indexes with (parent, child, owner_id) indexes.
-- Note: other places in this file may have similar replacements which are
-- made.
--

ALTER TABLE annotationannotationlink
        DROP CONSTRAINT annotationannotationlink_parent_child_key;

ALTER TABLE channelannotationlink
        DROP CONSTRAINT channelannotationlink_parent_child_key;

ALTER TABLE datasetannotationlink
        DROP CONSTRAINT datasetannotationlink_parent_child_key;

ALTER TABLE datasetimagelink
        DROP CONSTRAINT datasetimagelink_parent_child_key;

ALTER TABLE projectdatasetlink
        DROP CONSTRAINT projectdatasetlink_parent_child_key;

ALTER TABLE imageannotationlink
        DROP CONSTRAINT imageannotationlink_parent_child_key;

ALTER TABLE joboriginalfilelink
        DROP CONSTRAINT joboriginalfilelink_parent_child_key;

ALTER TABLE originalfileannotationlink
        DROP CONSTRAINT originalfileannotationlink_parent_child_key;

ALTER TABLE pixelsoriginalfilemap
        DROP CONSTRAINT pixelsoriginalfilemap_parent_child_key;

ALTER TABLE pixelsannotationlink
        DROP CONSTRAINT pixelsannotationlink_parent_child_key;

ALTER TABLE planeinfoannotationlink
        DROP CONSTRAINT planeinfoannotationlink_parent_child_key;

ALTER TABLE plateannotationlink
        DROP CONSTRAINT plateannotationlink_parent_child_key;

ALTER TABLE screenplatelink
        DROP CONSTRAINT screenplatelink_parent_child_key;

ALTER TABLE projectannotationlink
        DROP CONSTRAINT projectannotationlink_parent_child_key;

ALTER TABLE reagentannotationlink
        DROP CONSTRAINT reagentannotationlink_parent_child_key;

ALTER TABLE wellreagentlink
        DROP CONSTRAINT wellreagentlink_parent_child_key;

ALTER TABLE roiannotationlink
        DROP CONSTRAINT roiannotationlink_parent_child_key;

ALTER TABLE screenannotationlink
        DROP CONSTRAINT screenannotationlink_parent_child_key;

ALTER TABLE wellannotationlink
        DROP CONSTRAINT wellannotationlink_parent_child_key;

ALTER TABLE wellsampleannotationlink
        DROP CONSTRAINT wellsampleannotationlink_parent_child_key;

ALTER TABLE experimenterannotationlink
        DROP CONSTRAINT experimenterannotationlink_parent_child_key;

ALTER TABLE experimentergroupannotationlink
        DROP CONSTRAINT experimentergroupannotationlink_parent_child_key;

ALTER TABLE nodeannotationlink
        DROP CONSTRAINT nodeannotationlink_parent_child_key;

ALTER TABLE sessionannotationlink
        DROP CONSTRAINT sessionannotationlink_parent_child_key;

ALTER TABLE annotationannotationlink
        ADD CONSTRAINT annotationannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE channelannotationlink
        ADD CONSTRAINT channelannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE datasetannotationlink
        ADD CONSTRAINT datasetannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE datasetimagelink
        ADD CONSTRAINT datasetimagelink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE projectdatasetlink
        ADD CONSTRAINT projectdatasetlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE imageannotationlink
        ADD CONSTRAINT imageannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE joboriginalfilelink
        ADD CONSTRAINT joboriginalfilelink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE originalfileannotationlink
        ADD CONSTRAINT originalfileannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE pixelsoriginalfilemap
        ADD CONSTRAINT pixelsoriginalfilemap_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE pixelsannotationlink
        ADD CONSTRAINT pixelsannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE planeinfoannotationlink
        ADD CONSTRAINT planeinfoannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE plateannotationlink
        ADD CONSTRAINT plateannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE screenplatelink
        ADD CONSTRAINT screenplatelink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE projectannotationlink
        ADD CONSTRAINT projectannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE reagentannotationlink
        ADD CONSTRAINT reagentannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE wellreagentlink
        ADD CONSTRAINT wellreagentlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE roiannotationlink
        ADD CONSTRAINT roiannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE screenannotationlink
        ADD CONSTRAINT screenannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE wellannotationlink
        ADD CONSTRAINT wellannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE wellsampleannotationlink
        ADD CONSTRAINT wellsampleannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE experimenterannotationlink
        ADD CONSTRAINT experimenterannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE experimentergroupannotationlink
        ADD CONSTRAINT experimentergroupannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE nodeannotationlink
        ADD CONSTRAINT nodeannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE sessionannotationlink
        ADD CONSTRAINT sessionannotationlink_parent_key UNIQUE (parent, child, owner_id);


----
--
-- Newly annotatable types
--
CREATE TABLE count_experimenter_annotationlinks_by_owner (
        experimenter_id bigint NOT NULL,
        count bigint NOT NULL,
        owner_id bigint NOT NULL
);

CREATE TABLE count_experimentergroup_annotationlinks_by_owner (
        experimentergroup_id bigint NOT NULL,
        count bigint NOT NULL,
        owner_id bigint NOT NULL
);

CREATE TABLE count_node_annotationlinks_by_owner (
        node_id bigint NOT NULL,
        count bigint NOT NULL,
        owner_id bigint NOT NULL
);

CREATE TABLE count_session_annotationlinks_by_owner (
        session_id bigint NOT NULL,
        count bigint NOT NULL,
        owner_id bigint NOT NULL
);

ALTER TABLE count_experimenter_annotationlinks_by_owner
        ADD CONSTRAINT count_experimenter_annotationlinks_by_owner_pkey PRIMARY KEY (experimenter_id, owner_id);

ALTER TABLE count_experimentergroup_annotationlinks_by_owner
        ADD CONSTRAINT count_experimentergroup_annotationlinks_by_owner_pkey PRIMARY KEY (experimentergroup_id, owner_id);

ALTER TABLE count_node_annotationlinks_by_owner
        ADD CONSTRAINT count_node_annotationlinks_by_owner_pkey PRIMARY KEY (node_id, owner_id);

ALTER TABLE count_session_annotationlinks_by_owner
        ADD CONSTRAINT count_session_annotationlinks_by_owner_pkey PRIMARY KEY (session_id, owner_id);

ALTER TABLE count_experimenter_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_experimenter_annotationlinks FOREIGN KEY (experimenter_id) REFERENCES experimenter(id);

ALTER TABLE count_experimentergroup_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_experimentergroup_annotationlinks FOREIGN KEY (experimentergroup_id) REFERENCES experimentergroup(id);

ALTER TABLE count_node_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_node_annotationlinks FOREIGN KEY (node_id) REFERENCES node(id);

ALTER TABLE count_session_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_session_annotationlinks FOREIGN KEY (session_id) REFERENCES session(id);


----
--
-- #2574 Fixing triggers for ordered collections
--

DROP TRIGGER channel_pixels_index_trigger ON channel;

DROP TRIGGER channelbinding_renderingdef_index_trigger ON channelbinding;

DROP TRIGGER codomainmapcontext_renderingdef_index_trigger ON codomainmapcontext;

DROP TRIGGER lightpathexcitationfilterlink_parent_index_trigger ON lightpathexcitationfilterlink;

DROP TRIGGER groupexperimentermap_child_index_trigger ON groupexperimentermap;

DROP TRIGGER pixels_image_index_trigger ON pixels;

DROP TRIGGER shape_roi_index_trigger ON shape;

DROP TRIGGER wellsample_well_index_trigger ON wellsample;


DROP FUNCTION channel_pixels_index_move();

DROP FUNCTION channelbinding_renderingdef_index_move();

DROP FUNCTION codomainmapcontext_renderingdef_index_move();

DROP FUNCTION groupexperimentermap_child_index_move();

DROP FUNCTION lightpathexcitationfilterlink_parent_index_move();

DROP FUNCTION pixels_image_index_move();

DROP FUNCTION shape_roi_index_move();

DROP FUNCTION wellsample_well_index_move();

CREATE OR REPLACE FUNCTION channel_pixels_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM channel
       WHERE pixels = new.pixels AND pixels_index = new.pixels_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping channel % via (-1 - oldvalue )', duplicate;
          UPDATE channel SET pixels_index = -1 - pixels_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION channel_pixels_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
          RAISE NOTICE 'Remapping channel % ', duplicate;
          UPDATE channel SET pixels_index = -1 - pixels_index WHERE id = duplicate;
      END IF;

      RETURN new;
  END;$$;


CREATE OR REPLACE FUNCTION channelbinding_renderingdef_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM channelbinding
       WHERE renderingDef = new.renderingDef AND renderingDef_index = new.renderingDef_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping channelbinding % via (-1 - oldvalue )', duplicate;
          UPDATE channelbinding SET renderingDef_index = -1 - renderingDef_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION channelbinding_renderingdef_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
          RAISE NOTICE 'Remapping channelbinding % via (-1 - oldvalue )', duplicate;
          UPDATE channelbinding SET renderingDef_index = -1 - renderingDef_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION codomainmapcontext_renderingdef_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM codomainmapcontext
       WHERE renderingDef = new.renderingDef AND renderingDef_index = new.renderingDef_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping codomainmapcontext % via (-1 - oldvalue )', duplicate;
          UPDATE codomainmapcontext SET renderingDef_index = -1 - renderingDef_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION codomainmapcontext_renderingdef_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
          RAISE NOTICE 'Remapping codomainmapcontext % via (-1 - oldvalue )', duplicate;
          UPDATE codomainmapcontext SET renderingDef_index = -1 - renderingDef_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION groupexperimentermap_child_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM groupexperimentermap
       WHERE child = new.child AND child_index = new.child_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping groupexperimentermap % via (-1 - oldvalue )', duplicate;
          UPDATE groupexperimentermap SET child_index = -1 - child_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION groupexperimentermap_child_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
          RAISE NOTICE 'Remapping groupexperimentermap % via (-1 - oldvalue )', duplicate;
          UPDATE groupexperimentermap SET child_index = -1 - child_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION lightpathexcitationfilterlink_parent_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM lightpathexcitationfilterlink
       WHERE parent = new.parent AND parent_index = new.parent_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping lightpathexcitationfilterlink % via (-1 - oldvalue )', duplicate;
          UPDATE lightpathexcitationfilterlink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION lightpathexcitationfilterlink_parent_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.parent = old.parent AND new.parent_index = old.parent_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM lightpathexcitationfilterlink
       WHERE parent = new.parent AND parent_index = new.parent_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping lightpathexcitationfilterlink % via (-1 - oldvalue )', duplicate;
          UPDATE lightpathexcitationfilterlink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION pixels_image_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM pixels
       WHERE image = new.image AND image_index = new.image_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping pixels % via (-1 - oldvalue )', duplicate;
          UPDATE pixels SET image_index = -1 - image_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION pixels_image_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
          RAISE NOTICE 'Remapping pixels % via (-1 - oldvalue )', duplicate;
          UPDATE pixels SET image_index = -1 - image_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION shape_roi_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM shape
       WHERE roi = new.roi AND roi_index = new.roi_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping shape % via (-1 - oldvalue )', duplicate;
          UPDATE shape SET roi_index = -1 - roi_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION shape_roi_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.roi = old.roi AND new.roi_index = old.roi_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM shape
       WHERE roi = new.roi AND roi_index = new.roi_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping shape % via (-1 - oldvalue )', duplicate;
          UPDATE shape SET roi_index = -1 - roi_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION wellsample_well_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM wellsample
       WHERE well = new.well AND well_index = new.well_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping wellsample % via (-1 - oldvalue )', duplicate;
          UPDATE wellsample SET well_index = -1 - well_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION wellsample_well_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.well = old.well AND new.well_index = old.well_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM wellsample
       WHERE well = new.well AND well_index = new.well_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping wellsample % via (-1 - oldvalue )', duplicate;
          UPDATE wellsample SET well_index = -1 - well_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE TRIGGER channel_pixels_index_trigger_insert
        BEFORE INSERT ON channel
        FOR EACH ROW
        EXECUTE PROCEDURE channel_pixels_index_insert();

CREATE TRIGGER channel_pixels_index_trigger_update
        BEFORE UPDATE ON channel
        FOR EACH ROW
        EXECUTE PROCEDURE channel_pixels_index_update();

CREATE TRIGGER channelbinding_renderingdef_index_trigger_insert
        BEFORE INSERT ON channelbinding
        FOR EACH ROW
        EXECUTE PROCEDURE channelbinding_renderingdef_index_insert();

CREATE TRIGGER channelbinding_renderingdef_index_trigger_update
        BEFORE UPDATE ON channelbinding
        FOR EACH ROW
        EXECUTE PROCEDURE channelbinding_renderingdef_index_update();

CREATE TRIGGER codomainmapcontext_renderingdef_index_trigger_insert
        BEFORE INSERT ON codomainmapcontext
        FOR EACH ROW
        EXECUTE PROCEDURE codomainmapcontext_renderingdef_index_insert();

CREATE TRIGGER codomainmapcontext_renderingdef_index_trigger_update
        BEFORE UPDATE ON codomainmapcontext
        FOR EACH ROW
        EXECUTE PROCEDURE codomainmapcontext_renderingdef_index_update();

CREATE TRIGGER lightpathexcitationfilterlink_parent_index_trigger_insert
        BEFORE INSERT ON lightpathexcitationfilterlink
        FOR EACH ROW
        EXECUTE PROCEDURE lightpathexcitationfilterlink_parent_index_insert();

CREATE TRIGGER lightpathexcitationfilterlink_parent_index_trigger_update
        BEFORE UPDATE ON lightpathexcitationfilterlink
        FOR EACH ROW
        EXECUTE PROCEDURE lightpathexcitationfilterlink_parent_index_update();

CREATE TRIGGER groupexperimentermap_child_index_trigger_insert
        BEFORE INSERT ON groupexperimentermap
        FOR EACH ROW
        EXECUTE PROCEDURE groupexperimentermap_child_index_insert();

CREATE TRIGGER groupexperimentermap_child_index_trigger_update
        BEFORE UPDATE ON groupexperimentermap
        FOR EACH ROW
        EXECUTE PROCEDURE groupexperimentermap_child_index_update();

CREATE TRIGGER pixels_image_index_trigger_insert
        BEFORE INSERT ON pixels
        FOR EACH ROW
        EXECUTE PROCEDURE pixels_image_index_insert();

CREATE TRIGGER pixels_image_index_trigger_update
        BEFORE UPDATE ON pixels
        FOR EACH ROW
        EXECUTE PROCEDURE pixels_image_index_update();

CREATE TRIGGER shape_roi_index_trigger_insert
        BEFORE INSERT ON shape
        FOR EACH ROW
        EXECUTE PROCEDURE shape_roi_index_insert();

CREATE TRIGGER shape_roi_index_trigger_update
        BEFORE UPDATE ON shape
        FOR EACH ROW
        EXECUTE PROCEDURE shape_roi_index_update();

CREATE TRIGGER wellsample_well_index_trigger_insert
        BEFORE INSERT ON wellsample
        FOR EACH ROW
        EXECUTE PROCEDURE wellsample_well_index_insert();

CREATE TRIGGER wellsample_well_index_trigger_update
        BEFORE UPDATE ON wellsample
        FOR EACH ROW
        EXECUTE PROCEDURE wellsample_well_index_update();

-- #2573

CREATE INDEX planeinfo_pixels ON planeinfo (pixels);

-- #2565

CREATE OR REPLACE FUNCTION omero_42_check_pg_advisory_lock() RETURNS text AS '
DECLARE
    txt text;
BEGIN
      BEGIN
        PERFORM pg_advisory_lock(1, 1);
        PERFORM pg_advisory_unlock(1, 1);
        RETURN ''ok'';
      EXCEPTION
        WHEN undefined_function THEN
         txt := chr(10) ||
            ''====================================================================================='' || chr(10) ||
            ''pg_advisory_lock does not exist!'' || chr(10) || chr(10) ||
            ''You should consider upgrading to PostgreSQL 8.2 or above'' || chr(10) ||
            ''Until then, you may experience infrequent key constraint issues on insert.'' || chr(10) ||
            ''====================================================================================='' || chr(10) || chr(10);
          RAISE WARNING ''%'', txt;
          RETURN txt;
      END;

END;' LANGUAGE plpgsql;
SELECT omero_42_check_pg_advisory_lock();
DROP FUNCTION omero_42_check_pg_advisory_lock();

--
-- FINISHED
--

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4.2'    and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.1' and
          previousPatch   = 0;

----- END OF OMERO4.1__0.sql
-----
----- MODIFICATION:
----- No commit is being called.
-----

---
--- OMERO-Beta4.3 release upgrade from OMERO4.2__0 to OMERO4.3__0
--- If the upgrade fails to apply due to data checks, see the
--- script omero-4.2-data-fix.sql in the same directory.
---


CREATE OR REPLACE FUNCTION omero_43_check_pg_advisory_lock() RETURNS void AS '
DECLARE
    txt text;
BEGIN
      BEGIN
        PERFORM pg_advisory_lock(1, 1);
        PERFORM pg_advisory_unlock(1, 1);
      EXCEPTION
        WHEN undefined_function THEN
         txt := chr(10) ||
            ''====================================================================================='' || chr(10) ||
            ''pg_advisory_lock does not exist!'' || chr(10) || chr(10) ||
            ''You must upgrade to PostgreSQL 8.2 or above'' || chr(10) ||
            ''====================================================================================='' || chr(10) || chr(10);
         -- 8.1 is unsupported starting with OMERO4.3 (See #4902)
         RAISE EXCEPTION ''%%'', txt;
      END;
END;' LANGUAGE plpgsql;
SELECT omero_43_check_pg_advisory_lock();
DROP FUNCTION omero_43_check_pg_advisory_lock();

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

SELECT omero_assert_db_version('OMERO4.2',0);
DROP FUNCTION omero_assert_db_version(varchar, int);


----- MODIFICATION:
----- Rather than inserting a new dbpatch, we will
----- update the latest.

UPDATE dbpatch set currentVersion = 'OMERO4.3' where
                   currentVersion = 'OMERO4.2' and currentPatch = 0;


-- #2694 Remove pg_geom
CREATE OR REPLACE FUNCTION omero_43_drop_pg_geom() RETURNS void AS '
BEGIN

    IF EXISTS(SELECT column_name FROM information_schema.columns where table_name ilike ''shape'' and column_name ilike ''pg_geom'') THEN
        ALTER TABLE shape DROP COLUMN pg_geom;
    END IF;
END;'
LANGUAGE plpgsql;
SELECT omero_43_drop_pg_geom();
DROP FUNCTION omero_43_drop_pg_geom();

-- #2710 Roi.image optional
ALTER TABLE roi ALTER COLUMN image DROP NOT NULL;

-- #2574 some SPW optional names
ALTER TABLE plateacquisition ALTER COLUMN name DROP NOT NULL;
ALTER TABLE reagent ALTER COLUMN name DROP NOT NULL;

-- #2569 rename Plate.cols to columns
ALTER TABLE plate RENAME COLUMN cols TO columns;

-- #4841 add Well.status
ALTER TABLE well ADD COLUMN status varchar(255);

-- #4822 checks on numeric types
ALTER TABLE imagingenvironment ADD check (humidity >= 0 and humidity <= 1 and co2percent >= 0 and co2percent <= 1);
ALTER TABLE laser ADD check (frequencyMultiplication > 0 and wavelength > 0);
ALTER TABLE lightsettings ADD check (attenuation >= 0 and attenuation <= 1 and wavelength > 0);
ALTER TABLE logicalchannel ADD check (excitationWave > 0 and emissionWave > 0 and samplesPerPixel > 0);
ALTER TABLE objective ADD check (nominalMagnification > 0);
ALTER TABLE otf ADD check (sizeX > 0 and sizeY > 0);
ALTER TABLE pixels ADD check (sizeX > 0 and sizeY > 0 and sizeZ > 0 and sizeC > 0 and sizeT > 0);
ALTER TABLE planeinfo ADD check (theZ >= 0 and theC >= 0 and theT >= 0);
ALTER TABLE transmittancerange ADD check (cutIn > 0 and cutOut > 0 and cutInTolerance >= 0 and cutOutTolerance >= 0 and transmittance >= 0 and transmittance <= 1);

-- #2724 PixelsType.bitSize
ALTER TABLE pixelstype ADD COLUMN bitsize int4;
UPDATE pixelstype SET bitsize = 1 WHERE value = 'bit';
UPDATE pixelstype SET bitsize = 8 WHERE value = 'int8';
UPDATE pixelstype SET bitsize = 8 WHERE value = 'uint8';
UPDATE pixelstype SET bitsize = 16 WHERE value = 'int16';
UPDATE pixelstype SET bitsize = 16 WHERE value = 'uint16';
UPDATE pixelstype SET bitsize = 32 WHERE value = 'int32';
UPDATE pixelstype SET bitsize = 32 WHERE value = 'uint16';
UPDATE pixelstype SET bitsize = 32 WHERE value = 'uint32';
UPDATE pixelstype SET bitsize = 32 WHERE value = 'float';
UPDATE pixelstype SET bitsize = 64 WHERE value = 'double';
UPDATE pixelstype SET bitsize = 64 WHERE value = 'complex';
UPDATE pixelstype SET bitsize = 128 WHERE value = 'double-complex';
ALTER TABLE pixelstype ALTER COLUMN bitsize SET NOT NULL;

-- #4693 lotNumber and serialNumber
ALTER TABLE detector ADD COLUMN lotnumber character varying(255);
ALTER TABLE dichroic ADD COLUMN serialnumber character varying(255);
ALTER TABLE filter ADD COLUMN serialnumber character varying(255);
ALTER TABLE filterset ADD COLUMN serialnumber character varying(255);
ALTER TABLE lightsource ADD COLUMN lotnumber character varying(255);
ALTER TABLE microscope ADD COLUMN lotnumber character varying(255);
ALTER TABLE objective ADD COLUMN lotnumber character varying(255);

-- #4900 Adding almost all FK indexes
DROP INDEX planeinfo_pixels;
CREATE INDEX eventlog_action ON eventlog USING btree (action);
CREATE INDEX eventlog_entityid ON eventlog USING btree (entityid);
CREATE INDEX eventlog_entitytype ON eventlog USING btree (entitytype);

CREATE INDEX i_wellsampleannotationlink_owner ON wellsampleannotationlink(owner_id);
CREATE INDEX i_wellsampleannotationlink_group ON wellsampleannotationlink(group_id);
CREATE INDEX i_WellSampleAnnotationLink_parent ON wellsampleannotationlink(parent);
CREATE INDEX i_WellSampleAnnotationLink_child ON wellsampleannotationlink(child);
CREATE INDEX i_wellannotationlink_owner ON wellannotationlink(owner_id);
CREATE INDEX i_wellannotationlink_group ON wellannotationlink(group_id);
CREATE INDEX i_WellAnnotationLink_parent ON wellannotationlink(parent);
CREATE INDEX i_WellAnnotationLink_child ON wellannotationlink(child);
CREATE INDEX i_dataset_owner ON dataset(owner_id);
CREATE INDEX i_dataset_group ON dataset(group_id);
CREATE INDEX i_plate_owner ON plate(owner_id);
CREATE INDEX i_plate_group ON plate(group_id);
CREATE INDEX i_thumbnail_owner ON thumbnail(owner_id);
CREATE INDEX i_thumbnail_group ON thumbnail(group_id);
CREATE INDEX i_Thumbnail_pixels ON thumbnail(pixels);
CREATE INDEX i_channel_owner ON channel(owner_id);
CREATE INDEX i_channel_group ON channel(group_id);
CREATE INDEX i_Channel_statsInfo ON channel(statsInfo);
CREATE INDEX i_Channel_logicalChannel ON channel(logicalChannel);
CREATE INDEX i_Channel_pixels ON channel(pixels);
CREATE INDEX i_imageannotationlink_owner ON imageannotationlink(owner_id);
CREATE INDEX i_imageannotationlink_group ON imageannotationlink(group_id);
CREATE INDEX i_ImageAnnotationLink_parent ON imageannotationlink(parent);
CREATE INDEX i_ImageAnnotationLink_child ON imageannotationlink(child);
CREATE INDEX i_link_owner ON link(owner_id);
CREATE INDEX i_link_group ON link(group_id);
CREATE INDEX i_lightpathemissionfilterlink_owner ON lightpathemissionfilterlink(owner_id);
CREATE INDEX i_lightpathemissionfilterlink_group ON lightpathemissionfilterlink(group_id);
CREATE INDEX i_LightPathEmissionFilterLink_parent ON lightpathemissionfilterlink(parent);
CREATE INDEX i_LightPathEmissionFilterLink_child ON lightpathemissionfilterlink(child);
CREATE INDEX i_filtersetemissionfilterlink_owner ON filtersetemissionfilterlink(owner_id);
CREATE INDEX i_filtersetemissionfilterlink_group ON filtersetemissionfilterlink(group_id);
CREATE INDEX i_FilterSetEmissionFilterLink_parent ON filtersetemissionfilterlink(parent);
CREATE INDEX i_FilterSetEmissionFilterLink_child ON filtersetemissionfilterlink(child);
CREATE INDEX i_filtersetexcitationfilterlink_owner ON filtersetexcitationfilterlink(owner_id);
CREATE INDEX i_filtersetexcitationfilterlink_group ON filtersetexcitationfilterlink(group_id);
CREATE INDEX i_FilterSetExcitationFilterLink_parent ON filtersetexcitationfilterlink(parent);
CREATE INDEX i_FilterSetExcitationFilterLink_child ON filtersetexcitationfilterlink(child);
CREATE INDEX i_microscope_owner ON microscope(owner_id);
CREATE INDEX i_microscope_group ON microscope(group_id);
CREATE INDEX i_Microscope_type ON microscope(type);
CREATE INDEX i_originalfileannotationlink_owner ON originalfileannotationlink(owner_id);
CREATE INDEX i_originalfileannotationlink_group ON originalfileannotationlink(group_id);
CREATE INDEX i_OriginalFileAnnotationLink_parent ON originalfileannotationlink(parent);
CREATE INDEX i_OriginalFileAnnotationLink_child ON originalfileannotationlink(child);
CREATE INDEX i_wellsample_owner ON wellsample(owner_id);
CREATE INDEX i_wellsample_group ON wellsample(group_id);
CREATE INDEX i_WellSample_plateAcquisition ON wellsample(plateAcquisition);
CREATE INDEX i_WellSample_well ON wellsample(well);
CREATE INDEX i_WellSample_image ON wellsample(image);
CREATE INDEX i_planeinfo_owner ON planeinfo(owner_id);
CREATE INDEX i_planeinfo_group ON planeinfo(group_id);
CREATE INDEX i_PlaneInfo_pixels ON planeinfo(pixels);
CREATE INDEX i_lightpathexcitationfilterlink_owner ON lightpathexcitationfilterlink(owner_id);
CREATE INDEX i_lightpathexcitationfilterlink_group ON lightpathexcitationfilterlink(group_id);
CREATE INDEX i_LightPathExcitationFilterLink_parent ON lightpathexcitationfilterlink(parent);
CREATE INDEX i_LightPathExcitationFilterLink_child ON lightpathexcitationfilterlink(child);
CREATE INDEX i_GroupExperimenterMap_parent ON groupexperimentermap(parent);
CREATE INDEX i_GroupExperimenterMap_child ON groupexperimentermap(child);
CREATE INDEX i_planeinfoannotationlink_owner ON planeinfoannotationlink(owner_id);
CREATE INDEX i_planeinfoannotationlink_group ON planeinfoannotationlink(group_id);
CREATE INDEX i_PlaneInfoAnnotationLink_parent ON planeinfoannotationlink(parent);
CREATE INDEX i_PlaneInfoAnnotationLink_child ON planeinfoannotationlink(child);
CREATE INDEX i_transmittancerange_owner ON transmittancerange(owner_id);
CREATE INDEX i_transmittancerange_group ON transmittancerange(group_id);
CREATE INDEX i_wellreagentlink_owner ON wellreagentlink(owner_id);
CREATE INDEX i_wellreagentlink_group ON wellreagentlink(group_id);
CREATE INDEX i_WellReagentLink_parent ON wellreagentlink(parent);
CREATE INDEX i_WellReagentLink_child ON wellreagentlink(child);
CREATE INDEX i_Arc_type ON arc(type);
CREATE INDEX i_EventLog_event ON eventlog(event);
CREATE INDEX i_quantumdef_owner ON quantumdef(owner_id);
CREATE INDEX i_quantumdef_group ON quantumdef(group_id);
CREATE INDEX i_namespace_owner ON namespace(owner_id);
CREATE INDEX i_namespace_group ON namespace(group_id);
CREATE INDEX i_image_owner ON image(owner_id);
CREATE INDEX i_image_group ON image(group_id);
CREATE INDEX i_Image_format ON image(format);
CREATE INDEX i_Image_imagingEnvironment ON image(imagingEnvironment);
CREATE INDEX i_Image_objectiveSettings ON image(objectiveSettings);
CREATE INDEX i_Image_instrument ON image(instrument);
CREATE INDEX i_Image_stageLabel ON image(stageLabel);
CREATE INDEX i_Image_experiment ON image(experiment);
CREATE INDEX i_microbeammanipulation_owner ON microbeammanipulation(owner_id);
CREATE INDEX i_microbeammanipulation_group ON microbeammanipulation(group_id);
CREATE INDEX i_MicrobeamManipulation_type ON microbeammanipulation(type);
CREATE INDEX i_MicrobeamManipulation_experiment ON microbeammanipulation(experiment);
CREATE INDEX i_joboriginalfilelink_owner ON joboriginalfilelink(owner_id);
CREATE INDEX i_joboriginalfilelink_group ON joboriginalfilelink(group_id);
CREATE INDEX i_JobOriginalFileLink_parent ON joboriginalfilelink(parent);
CREATE INDEX i_JobOriginalFileLink_child ON joboriginalfilelink(child);
CREATE INDEX i_renderingdef_owner ON renderingdef(owner_id);
CREATE INDEX i_renderingdef_group ON renderingdef(group_id);
CREATE INDEX i_RenderingDef_pixels ON renderingdef(pixels);
CREATE INDEX i_RenderingDef_model ON renderingdef(model);
CREATE INDEX i_RenderingDef_quantization ON renderingdef(quantization);
CREATE INDEX i_datasetimagelink_owner ON datasetimagelink(owner_id);
CREATE INDEX i_datasetimagelink_group ON datasetimagelink(group_id);
CREATE INDEX i_DatasetImageLink_parent ON datasetimagelink(parent);
CREATE INDEX i_DatasetImageLink_child ON datasetimagelink(child);
CREATE INDEX i_codomainmapcontext_owner ON codomainmapcontext(owner_id);
CREATE INDEX i_codomainmapcontext_group ON codomainmapcontext(group_id);
CREATE INDEX i_CodomainMapContext_renderingDef ON codomainmapcontext(renderingDef);
CREATE INDEX i_project_owner ON project(owner_id);
CREATE INDEX i_project_group ON project(group_id);
CREATE INDEX i_channelannotationlink_owner ON channelannotationlink(owner_id);
CREATE INDEX i_channelannotationlink_group ON channelannotationlink(group_id);
CREATE INDEX i_ChannelAnnotationLink_parent ON channelannotationlink(parent);
CREATE INDEX i_ChannelAnnotationLink_child ON channelannotationlink(child);
CREATE INDEX i_stagelabel_owner ON stagelabel(owner_id);
CREATE INDEX i_stagelabel_group ON stagelabel(group_id);
CREATE INDEX i_experimentergroupannotationlink_owner ON experimentergroupannotationlink(owner_id);
CREATE INDEX i_experimentergroupannotationlink_group ON experimentergroupannotationlink(group_id);
CREATE INDEX i_ExperimenterGroupAnnotationLink_parent ON experimentergroupannotationlink(parent);
CREATE INDEX i_ExperimenterGroupAnnotationLink_child ON experimentergroupannotationlink(child);
CREATE INDEX i_pixels_owner ON pixels(owner_id);
CREATE INDEX i_pixels_group ON pixels(group_id);
CREATE INDEX i_Pixels_image ON pixels(image);
CREATE INDEX i_Pixels_relatedTo ON pixels(relatedTo);
CREATE INDEX i_Pixels_pixelsType ON pixels(pixelsType);
CREATE INDEX i_Pixels_dimensionOrder ON pixels(dimensionOrder);
CREATE INDEX i_lightpath_owner ON lightpath(owner_id);
CREATE INDEX i_lightpath_group ON lightpath(group_id);
CREATE INDEX i_LightPath_dichroic ON lightpath(dichroic);
CREATE INDEX i_roi_owner ON roi(owner_id);
CREATE INDEX i_roi_group ON roi(group_id);
CREATE INDEX i_Roi_image ON roi(image);
CREATE INDEX i_Roi_source ON roi(source);
CREATE INDEX i_roiannotationlink_owner ON roiannotationlink(owner_id);
CREATE INDEX i_roiannotationlink_group ON roiannotationlink(group_id);
CREATE INDEX i_RoiAnnotationLink_parent ON roiannotationlink(parent);
CREATE INDEX i_RoiAnnotationLink_child ON roiannotationlink(child);
CREATE INDEX i_externalinfo_owner ON externalinfo(owner_id);
CREATE INDEX i_externalinfo_group ON externalinfo(group_id);
CREATE INDEX i_FileAnnotation_file ON annotation("file");
CREATE INDEX i_annotationannotationlink_owner ON annotationannotationlink(owner_id);
CREATE INDEX i_annotationannotationlink_group ON annotationannotationlink(group_id);
CREATE INDEX i_AnnotationAnnotationLink_parent ON annotationannotationlink(parent);
CREATE INDEX i_AnnotationAnnotationLink_child ON annotationannotationlink(child);
CREATE INDEX i_objectivesettings_owner ON objectivesettings(owner_id);
CREATE INDEX i_objectivesettings_group ON objectivesettings(group_id);
CREATE INDEX i_ObjectiveSettings_medium ON objectivesettings(medium);
CREATE INDEX i_ObjectiveSettings_objective ON objectivesettings(objective);
CREATE INDEX i_nodeannotationlink_owner ON nodeannotationlink(owner_id);
CREATE INDEX i_nodeannotationlink_group ON nodeannotationlink(group_id);
CREATE INDEX i_NodeAnnotationLink_parent ON nodeannotationlink(parent);
CREATE INDEX i_NodeAnnotationLink_child ON nodeannotationlink(child);
CREATE INDEX i_Share_group ON share("group");
CREATE INDEX i_instrument_owner ON instrument(owner_id);
CREATE INDEX i_instrument_group ON instrument(group_id);
CREATE INDEX i_Instrument_microscope ON instrument(microscope);
CREATE INDEX i_namespaceannotationlink_owner ON namespaceannotationlink(owner_id);
CREATE INDEX i_namespaceannotationlink_group ON namespaceannotationlink(group_id);
CREATE INDEX i_NamespaceAnnotationLink_parent ON namespaceannotationlink(parent);
CREATE INDEX i_NamespaceAnnotationLink_child ON namespaceannotationlink(child);
CREATE INDEX i_well_owner ON well(owner_id);
CREATE INDEX i_well_group ON well(group_id);
CREATE INDEX i_Well_plate ON well(plate);
CREATE INDEX i_imagingenvironment_owner ON imagingenvironment(owner_id);
CREATE INDEX i_imagingenvironment_group ON imagingenvironment(group_id);
CREATE INDEX i_projectannotationlink_owner ON projectannotationlink(owner_id);
CREATE INDEX i_projectannotationlink_group ON projectannotationlink(group_id);
CREATE INDEX i_ProjectAnnotationLink_parent ON projectannotationlink(parent);
CREATE INDEX i_ProjectAnnotationLink_child ON projectannotationlink(child);
CREATE INDEX i_reagent_owner ON reagent(owner_id);
CREATE INDEX i_reagent_group ON reagent(group_id);
CREATE INDEX i_Reagent_screen ON reagent(screen);
CREATE INDEX i_detector_owner ON detector(owner_id);
CREATE INDEX i_detector_group ON detector(group_id);
CREATE INDEX i_Detector_type ON detector(type);
CREATE INDEX i_Detector_instrument ON detector(instrument);
CREATE INDEX i_otf_owner ON otf(owner_id);
CREATE INDEX i_otf_group ON otf(group_id);
CREATE INDEX i_OTF_pixelsType ON otf(pixelsType);
CREATE INDEX i_OTF_filterSet ON otf(filterSet);
CREATE INDEX i_OTF_objective ON otf(objective);
CREATE INDEX i_OTF_instrument ON otf(instrument);
CREATE INDEX i_reagentannotationlink_owner ON reagentannotationlink(owner_id);
CREATE INDEX i_reagentannotationlink_group ON reagentannotationlink(group_id);
CREATE INDEX i_ReagentAnnotationLink_parent ON reagentannotationlink(parent);
CREATE INDEX i_ReagentAnnotationLink_child ON reagentannotationlink(child);
CREATE INDEX i_lightsettings_owner ON lightsettings(owner_id);
CREATE INDEX i_lightsettings_group ON lightsettings(group_id);
CREATE INDEX i_LightSettings_lightSource ON lightsettings(lightSource);
CREATE INDEX i_LightSettings_microbeamManipulation ON lightsettings(microbeamManipulation);
CREATE INDEX i_originalfile_owner ON originalfile(owner_id);
CREATE INDEX i_originalfile_group ON originalfile(group_id);
CREATE INDEX i_lightsource_owner ON lightsource(owner_id);
CREATE INDEX i_lightsource_group ON lightsource(group_id);
CREATE INDEX i_LightSource_instrument ON lightsource(instrument);
CREATE INDEX i_annotation_owner ON annotation(owner_id);
CREATE INDEX i_annotation_group ON annotation(group_id);
CREATE INDEX i_job_owner ON job(owner_id);
CREATE INDEX i_job_group ON job(group_id);
CREATE INDEX i_Job_status ON job(status);
CREATE INDEX i_Mask_pixels ON shape(pixels);
CREATE INDEX i_ShareMember_parent ON sharemember(parent);
CREATE INDEX i_ShareMember_child ON sharemember(child);
CREATE INDEX i_filterset_owner ON filterset(owner_id);
CREATE INDEX i_filterset_group ON filterset(group_id);
CREATE INDEX i_FilterSet_instrument ON filterset(instrument);
CREATE INDEX i_FilterSet_dichroic ON filterset(dichroic);
CREATE INDEX i_projectdatasetlink_owner ON projectdatasetlink(owner_id);
CREATE INDEX i_projectdatasetlink_group ON projectdatasetlink(group_id);
CREATE INDEX i_ProjectDatasetLink_parent ON projectdatasetlink(parent);
CREATE INDEX i_ProjectDatasetLink_child ON projectdatasetlink(child);
CREATE INDEX i_experimenterannotationlink_owner ON experimenterannotationlink(owner_id);
CREATE INDEX i_experimenterannotationlink_group ON experimenterannotationlink(group_id);
CREATE INDEX i_ExperimenterAnnotationLink_parent ON experimenterannotationlink(parent);
CREATE INDEX i_ExperimenterAnnotationLink_child ON experimenterannotationlink(child);
CREATE INDEX i_plateannotationlink_owner ON plateannotationlink(owner_id);
CREATE INDEX i_plateannotationlink_group ON plateannotationlink(group_id);
CREATE INDEX i_PlateAnnotationLink_parent ON plateannotationlink(parent);
CREATE INDEX i_PlateAnnotationLink_child ON plateannotationlink(child);
CREATE INDEX i_Laser_type ON laser(type);
CREATE INDEX i_Laser_laserMedium ON laser(laserMedium);
CREATE INDEX i_Laser_pulse ON laser(pulse);
CREATE INDEX i_Laser_pump ON laser(pump);
CREATE INDEX i_channelbinding_owner ON channelbinding(owner_id);
CREATE INDEX i_channelbinding_group ON channelbinding(group_id);
CREATE INDEX i_ChannelBinding_renderingDef ON channelbinding(renderingDef);
CREATE INDEX i_ChannelBinding_family ON channelbinding(family);
CREATE INDEX i_statsinfo_owner ON statsinfo(owner_id);
CREATE INDEX i_statsinfo_group ON statsinfo(group_id);
CREATE INDEX i_screen_owner ON screen(owner_id);
CREATE INDEX i_screen_group ON screen(group_id);
CREATE INDEX i_dichroic_owner ON dichroic(owner_id);
CREATE INDEX i_dichroic_group ON dichroic(group_id);
CREATE INDEX i_Dichroic_instrument ON dichroic(instrument);
CREATE INDEX i_Session_node ON session(node);
CREATE INDEX i_Session_owner ON session(owner);
CREATE INDEX i_plateacquisition_owner ON plateacquisition(owner_id);
CREATE INDEX i_plateacquisition_group ON plateacquisition(group_id);
CREATE INDEX i_PlateAcquisition_plate ON plateacquisition(plate);
CREATE INDEX i_Filament_type ON filament(type);
CREATE INDEX i_screenannotationlink_owner ON screenannotationlink(owner_id);
CREATE INDEX i_screenannotationlink_group ON screenannotationlink(group_id);
CREATE INDEX i_ScreenAnnotationLink_parent ON screenannotationlink(parent);
CREATE INDEX i_ScreenAnnotationLink_child ON screenannotationlink(child);
CREATE INDEX i_pixelsannotationlink_owner ON pixelsannotationlink(owner_id);
CREATE INDEX i_pixelsannotationlink_group ON pixelsannotationlink(group_id);
CREATE INDEX i_PixelsAnnotationLink_parent ON pixelsannotationlink(parent);
CREATE INDEX i_PixelsAnnotationLink_child ON pixelsannotationlink(child);
CREATE INDEX i_objective_owner ON objective(owner_id);
CREATE INDEX i_objective_group ON objective(group_id);
CREATE INDEX i_Objective_immersion ON objective(immersion);
CREATE INDEX i_Objective_correction ON objective(correction);
CREATE INDEX i_Objective_instrument ON objective(instrument);
CREATE INDEX i_datasetannotationlink_owner ON datasetannotationlink(owner_id);
CREATE INDEX i_datasetannotationlink_group ON datasetannotationlink(group_id);
CREATE INDEX i_DatasetAnnotationLink_parent ON datasetannotationlink(parent);
CREATE INDEX i_DatasetAnnotationLink_child ON datasetannotationlink(child);
CREATE INDEX i_experiment_owner ON experiment(owner_id);
CREATE INDEX i_experiment_group ON experiment(group_id);
CREATE INDEX i_Experiment_type ON experiment(type);
CREATE INDEX i_detectorsettings_owner ON detectorsettings(owner_id);
CREATE INDEX i_detectorsettings_group ON detectorsettings(group_id);
CREATE INDEX i_DetectorSettings_binning ON detectorsettings(binning);
CREATE INDEX i_DetectorSettings_detector ON detectorsettings(detector);
CREATE INDEX i_filter_owner ON filter(owner_id);
CREATE INDEX i_filter_group ON filter(group_id);
CREATE INDEX i_Filter_type ON filter(type);
CREATE INDEX i_Filter_transmittanceRange ON filter(transmittanceRange);
CREATE INDEX i_Filter_instrument ON filter(instrument);
CREATE INDEX i_plateacquisitionannotationlink_owner ON plateacquisitionannotationlink(owner_id);
CREATE INDEX i_plateacquisitionannotationlink_group ON plateacquisitionannotationlink(group_id);
CREATE INDEX i_PlateAcquisitionAnnotationLink_parent ON plateacquisitionannotationlink(parent);
CREATE INDEX i_PlateAcquisitionAnnotationLink_child ON plateacquisitionannotationlink(child);
CREATE INDEX i_pixelsoriginalfilemap_owner ON pixelsoriginalfilemap(owner_id);
CREATE INDEX i_pixelsoriginalfilemap_group ON pixelsoriginalfilemap(group_id);
CREATE INDEX i_PixelsOriginalFileMap_parent ON pixelsoriginalfilemap(parent);
CREATE INDEX i_PixelsOriginalFileMap_child ON pixelsoriginalfilemap(child);
CREATE INDEX i_logicalchannel_owner ON logicalchannel(owner_id);
CREATE INDEX i_logicalchannel_group ON logicalchannel(group_id);
CREATE INDEX i_LogicalChannel_illumination ON logicalchannel(illumination);
CREATE INDEX i_LogicalChannel_contrastMethod ON logicalchannel(contrastMethod);
CREATE INDEX i_LogicalChannel_otf ON logicalchannel(otf);
CREATE INDEX i_LogicalChannel_detectorSettings ON logicalchannel(detectorSettings);
CREATE INDEX i_LogicalChannel_lightSourceSettings ON logicalchannel(lightSourceSettings);
CREATE INDEX i_LogicalChannel_filterSet ON logicalchannel(filterSet);
CREATE INDEX i_LogicalChannel_photometricInterpretation ON logicalchannel(photometricInterpretation);
CREATE INDEX i_LogicalChannel_mode ON logicalchannel("mode");
CREATE INDEX i_LogicalChannel_lightPath ON logicalchannel(lightPath);
CREATE INDEX i_sessionannotationlink_owner ON sessionannotationlink(owner_id);
CREATE INDEX i_sessionannotationlink_group ON sessionannotationlink(group_id);
CREATE INDEX i_SessionAnnotationLink_parent ON sessionannotationlink(parent);
CREATE INDEX i_SessionAnnotationLink_child ON sessionannotationlink(child);
CREATE INDEX i_screenplatelink_owner ON screenplatelink(owner_id);
CREATE INDEX i_screenplatelink_group ON screenplatelink(group_id);
CREATE INDEX i_ScreenPlateLink_parent ON screenplatelink(parent);
CREATE INDEX i_ScreenPlateLink_child ON screenplatelink(child);
CREATE INDEX i_shape_owner ON shape(owner_id);
CREATE INDEX i_shape_group ON shape(group_id);
CREATE INDEX i_Shape_roi ON shape(roi);
CREATE INDEX i_Event_experimenter ON event(experimenter);
CREATE INDEX i_Event_experimenterGroup ON event(experimenterGroup);
CREATE INDEX i_Event_type ON event(type);
CREATE INDEX i_Event_containingEvent ON event(containingEvent);
CREATE INDEX i_Event_session ON event("session");

--
-- #1390 : Triggering the addition of an "REINDEX" event on annotation events.
--

CREATE OR REPLACE FUNCTION _prepare_session(_event_id int8, _user_id int8, _group_id int8) RETURNS void
    AS '
    BEGIN

        IF NOT EXISTS(SELECT table_name FROM information_schema.tables where table_name = ''_current_session'') THEN
            CREATE TEMP TABLE _current_session ("event_id" int8, "user_id" int8, "group_id" int8) ON COMMIT DELETE ROWS;
            INSERT INTO _current_session VALUES (_event_id, _user_id, _group_id);
        ELSE
            DELETE FROM _current_session;
            INSERT INTO _current_session VALUES (_event_id, _user_id, _group_id);
        END IF;
    END;'
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION _current_event() RETURNS int8
    AS '
    DECLARE
        eid int8;
    BEGIN
        IF NOT EXISTS(SELECT table_name FROM information_schema.tables where table_name = ''_current_session'') THEN
            RETURN 0;
        END IF;
        SELECT INTO eid event_id FROM _current_session;
        RETURN eid;

    END;'
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION _current_or_new_event() RETURNS int8
    AS '
    DECLARE
        eid int8;
    BEGIN
        SELECT INTO eid _current_event();
        IF eid = 0 OR eid IS NULL THEN
            SELECT INTO eid ome_nextval(''seq_event'');
            INSERT INTO event (id, permissions, status, time, experimenter, experimentergroup, session, type)
                SELECT eid, -35, ''TRIGGERED'', now(), 0, 0, 0, 0;
        END IF;
        RETURN eid;
    END;'
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        rec RECORD;
        eid int8;
        cnt int8;
    BEGIN

        IF NOT EXISTS(SELECT table_name FROM information_schema.tables where table_name = ''_updated_annotations'') THEN
            CREATE TEMP TABLE _updated_annotations (entitytype varchar, entityid int8) ON COMMIT DELETE ROWS;
        END IF;


        FOR rec IN SELECT id, parent FROM datasetannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.containers.Dataset'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM plateannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Plate'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM channelannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Channel'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM wellsampleannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.WellSample'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM planeinfoannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.PlaneInfo'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM namespaceannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Namespace'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM imageannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Image'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM experimentergroupannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.ExperimenterGroup'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM projectannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.containers.Project'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM pixelsannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Pixels'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM roiannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.roi.Roi'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM wellannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Well'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM reagentannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Reagent'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM originalfileannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.OriginalFile'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM annotationannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.annotations.Annotation'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM screenannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Screen'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM sessionannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Session'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM plateacquisitionannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.PlateAcquisition'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM nodeannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Node'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM experimenterannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Experimenter'');
        END LOOP;

        SELECT INTO cnt count(*) FROM _updated_annotations;
        IF cnt <> 0 THEN
            SELECT INTO eid _current_or_new_event();
            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                 SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, entityid, entitytype, eid
                   FROM _updated_annotations;
        END IF;

        RETURN new;

    END;'
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS annotation_trigger ON annotation;

CREATE TRIGGER annotation_trigger
        AFTER UPDATE ON annotation
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_update_event_trigger();


CREATE OR REPLACE FUNCTION annotation_link_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        eid int8;
    BEGIN

        SELECT INTO eid _current_or_new_event();
        INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, new.parent, TG_ARGV[0], eid;

        RETURN new;

    END;'
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS dataset_annotation_link_event_trigger ON datasetannotationlink;

CREATE TRIGGER dataset_annotation_link_event_trigger
        AFTER UPDATE ON datasetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Dataset');

DROP TRIGGER IF EXISTS plate_annotation_link_event_trigger ON plateannotationlink;

CREATE TRIGGER plate_annotation_link_event_trigger
        AFTER UPDATE ON plateannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Plate');

DROP TRIGGER IF EXISTS channel_annotation_link_event_trigger ON channelannotationlink;

CREATE TRIGGER channel_annotation_link_event_trigger
        AFTER UPDATE ON channelannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Channel');

DROP TRIGGER IF EXISTS wellsample_annotation_link_event_trigger ON wellsampleannotationlink;

CREATE TRIGGER wellsample_annotation_link_event_trigger
        AFTER UPDATE ON wellsampleannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.WellSample');

DROP TRIGGER IF EXISTS planeinfo_annotation_link_event_trigger ON planeinfoannotationlink;

CREATE TRIGGER planeinfo_annotation_link_event_trigger
        AFTER UPDATE ON planeinfoannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.PlaneInfo');

DROP TRIGGER IF EXISTS namespace_annotation_link_event_trigger ON namespaceannotationlink;

CREATE TRIGGER namespace_annotation_link_event_trigger
        AFTER UPDATE ON namespaceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Namespace');

DROP TRIGGER IF EXISTS image_annotation_link_event_trigger ON imageannotationlink;

CREATE TRIGGER image_annotation_link_event_trigger
        AFTER UPDATE ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Image');

DROP TRIGGER IF EXISTS experimentergroup_annotation_link_event_trigger ON experimentergroupannotationlink;

CREATE TRIGGER experimentergroup_annotation_link_event_trigger
        AFTER UPDATE ON experimentergroupannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.ExperimenterGroup');

DROP TRIGGER IF EXISTS project_annotation_link_event_trigger ON projectannotationlink;

CREATE TRIGGER project_annotation_link_event_trigger
        AFTER UPDATE ON projectannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Project');

DROP TRIGGER IF EXISTS pixels_annotation_link_event_trigger ON pixelsannotationlink;

CREATE TRIGGER pixels_annotation_link_event_trigger
        AFTER UPDATE ON pixelsannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Pixels');

DROP TRIGGER IF EXISTS roi_annotation_link_event_trigger ON roiannotationlink;

CREATE TRIGGER roi_annotation_link_event_trigger
        AFTER UPDATE ON roiannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.roi.Roi');

DROP TRIGGER IF EXISTS well_annotation_link_event_trigger ON wellannotationlink;

CREATE TRIGGER well_annotation_link_event_trigger
        AFTER UPDATE ON wellannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Well');

DROP TRIGGER IF EXISTS reagent_annotation_link_event_trigger ON reagentannotationlink;

CREATE TRIGGER reagent_annotation_link_event_trigger
        AFTER UPDATE ON reagentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Reagent');

DROP TRIGGER IF EXISTS originalfile_annotation_link_event_trigger ON originalfileannotationlink;

CREATE TRIGGER originalfile_annotation_link_event_trigger
        AFTER UPDATE ON originalfileannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.OriginalFile');

DROP TRIGGER IF EXISTS annotation_annotation_link_event_trigger ON annotationannotationlink;

CREATE TRIGGER annotation_annotation_link_event_trigger
        AFTER UPDATE ON annotationannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.annotations.Annotation');

DROP TRIGGER IF EXISTS screen_annotation_link_event_trigger ON screenannotationlink;

CREATE TRIGGER screen_annotation_link_event_trigger
        AFTER UPDATE ON screenannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Screen');

DROP TRIGGER IF EXISTS session_annotation_link_event_trigger ON sessionannotationlink;

CREATE TRIGGER session_annotation_link_event_trigger
        AFTER UPDATE ON sessionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Session');

DROP TRIGGER IF EXISTS plateacquisition_annotation_link_event_trigger ON plateacquisitionannotationlink;

CREATE TRIGGER plateacquisition_annotation_link_event_trigger
        AFTER UPDATE ON plateacquisitionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.PlateAcquisition');

DROP TRIGGER IF EXISTS node_annotation_link_event_trigger ON nodeannotationlink;

CREATE TRIGGER node_annotation_link_event_trigger
        AFTER UPDATE ON nodeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Node');

DROP TRIGGER IF EXISTS experimenter_annotation_link_event_trigger ON experimenterannotationlink;

CREATE TRIGGER experimenter_annotation_link_event_trigger
        AFTER UPDATE ON experimenterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Experimenter');

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
-- END #1390
--
--
-- FINISHED
--

----- MODIFICATION:
----- Against, modifying the 4.1 dbpatch rather than a newer one

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4.3'    and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.1' and
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO4.3__0'||CHR(10)||CHR(10)||CHR(10) as Status;

----- END OF OMERO4.2__0.sql
-----
----- MODIFICATION:
----- No commit is being called.
-----

---
--- OMERO-Beta4.4 release upgrade from OMERO4.3__0 to OMERO4.4__0
--- Primarily for upgrading group permissions to include the split
--- between ra and rw
---

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


----- MODIFICATION:
----- Rather than inserting a new dbpatch, we will
----- update the latest.

UPDATE dbpatch set currentVersion = 'OMERO4.4' where
                   currentVersion = 'OMERO4.3' and currentPatch = 0;

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
