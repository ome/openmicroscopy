--
-- Copyright 2010 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- This SQL script produces a report of permission issues in
-- OMERO4.1__0 databases. See ticket #2204 for more information.
--

begin;
create or replace function omero_41_check(target varchar, tbl varchar, col varchar) returns text as '
declare
    sql varchar;
begin

    sql := ''select target.id as target_id, target.group_id as target_group, target.owner_id as target_owner, ome_perms(target.permissions) as target_perms, '' ||
           ''          tbl.id as tbl_id,       tbl.group_id as tbl_group,       tbl.owner_id as tbl_owner,    ome_perms(tbl.permissions) as tbl_perms '' ||
           ''from '' || target || '' target, '' || tbl || '' tbl '' ||
           ''where target.id = tbl.''||col||'' and target.group_id <> tbl.group_id;'';
    return omero_41_check(sql, target, tbl, col);
end;' language plpgsql;

create or replace function omero_41_check(sql varchar, target varchar, tbl varchar, col varchar) returns text as '
declare
    rec record;
    txt text;
begin
    for rec in execute sql loop
        if txt is null then
            txt := chr(10);
        end if;

        txt := txt || ''Differing groups'';

        if rec.target_group <> rec.tbl_group then
            txt := txt || '' and owners!'';
        end if;

        txt := txt || '': '' || target || ''('';
        txt := txt || ''id=''    || rec.target_id || '', '';
        txt := txt || ''group='' || rec.target_group || '', '';
        txt := txt || ''owner='' || rec.target_owner || '', '';
        txt := txt || ''perms='' || rec.target_perms || '')'';
        txt := txt || '' <--> '' || tbl || ''.'';
        txt := txt || col || ''('';
        txt := txt || ''id=''    || rec.tbl_id || '', '';
        txt := txt || ''group='' || rec.tbl_group || '', '';
        txt := txt || ''owner='' || rec.tbl_owner || '', '';
        txt := txt || ''perms='' || rec.tbl_perms || '')'';
        txt := txt || chr(10);
    end loop;

    return txt;

end;' language plpgsql;

select omero_41_check('Dataset', 'DatasetImageLink','parent');
select omero_41_check('Dataset', 'ProjectDatasetLink','child');
select omero_41_check('Dataset', 'DatasetAnnotationLink','parent');
select omero_41_check('Plate', 'Well','plate');
select omero_41_check('Plate', 'PlateAnnotationLink','parent');
select omero_41_check('Plate', 'ScreenPlateLink','child');
select omero_41_check('Channel', 'ChannelAnnotationLink','parent');
select omero_41_check('Microscope', 'Instrument','microscope');
select omero_41_check('WellSample', 'WellSampleAnnotationLink','parent');
select omero_41_check('WellSample', 'ScreenAcquisitionWellSampleLink','child');
select omero_41_check('PlaneInfo', 'PlaneInfoAnnotationLink','parent');
select omero_41_check('TransmittanceRange', 'Filter','transmittanceRange');
select omero_41_check('QuantumDef', 'RenderingDef','quantization');
select omero_41_check('Image', 'ImageAnnotationLink','parent');
select omero_41_check('Image', 'WellSample','image');
select omero_41_check('Image', 'DatasetImageLink','child');
select omero_41_check('Image', 'Pixels','image');
select omero_41_check('Image', 'Roi','image');
select omero_41_check('MicrobeamManipulation', 'LightSettings','microbeamManipulation');
select omero_41_check('ExperimenterGroup', 'GroupExperimenterMap','parent');
select omero_41_check('ExperimenterGroup', 'ExperimenterGroupAnnotationLink','parent');
select omero_41_check('RenderingDef', 'CodomainMapContext','renderingDef');
select omero_41_check('RenderingDef', 'ChannelBinding','renderingDef');
select omero_41_check('Project', 'ProjectAnnotationLink','parent');
select omero_41_check('Project', 'ProjectDatasetLink','parent');
select omero_41_check('StageLabel', 'Image','stageLabel');
select omero_41_check('Pixels', 'Thumbnail','pixels');
select omero_41_check('Pixels', 'Channel','pixels');
select omero_41_check('Pixels', 'PlaneInfo','pixels');
select omero_41_check('Pixels', 'RenderingDef','pixels');
select omero_41_check('Pixels', 'Pixels','relatedTo');
select omero_41_check('Pixels', 'Shape','pixels');
select omero_41_check('Pixels', 'PixelsAnnotationLink','parent');
select omero_41_check('Pixels', 'PixelsOriginalFileMap','child');
select omero_41_check('Roi', 'RoiAnnotationLink','parent');
select omero_41_check('Roi', 'Shape','roi');
select omero_41_check('ObjectiveSettings', 'Image','objectiveSettings');
select omero_41_check('Instrument', 'Image','instrument');
select omero_41_check('Instrument', 'Detector','instrument');
select omero_41_check('Instrument', 'OTF','instrument');
select omero_41_check('Instrument', 'FilterSet','instrument');
select omero_41_check('Instrument', 'LightSource','instrument');
select omero_41_check('Instrument', 'Dichroic','instrument');
select omero_41_check('Instrument', 'Objective','instrument');
select omero_41_check('Instrument', 'Filter','instrument');
select omero_41_check('ScreenAcquisition', 'ScreenAcquisitionAnnotationLink','parent');
select omero_41_check('ScreenAcquisition', 'ScreenAcquisitionWellSampleLink','parent');
select omero_41_check('Well', 'WellAnnotationLink','parent');
select omero_41_check('Well', 'WellSample','well');
select omero_41_check('Well', 'WellReagentLink','parent');
select omero_41_check('ImagingEnvironment', 'Image','imagingEnvironment');
select omero_41_check('Reagent', 'WellReagentLink','child');
select omero_41_check('Reagent', 'ReagentAnnotationLink','parent');
select omero_41_check('Detector', 'DetectorSettings','detector');
select omero_41_check('OTF', 'LogicalChannel','otf');
select omero_41_check('LightSettings', 'LogicalChannel','lightSourceSettings');
select omero_41_check('LightSource', 'LightSettings','lightSource');
select omero_41_check('OriginalFile', 'OriginalFileAnnotationLink','parent');
select omero_41_check('OriginalFile', 'JobOriginalFileLink','child');
select omero_41_check('OriginalFile', 'Roi','source');
select omero_41_check('OriginalFile', 'PixelsOriginalFileMap','parent');
select omero_41_check('Job', 'JobOriginalFileLink','parent');
select omero_41_check('Annotation', 'WellSampleAnnotationLink','child');
select omero_41_check('Annotation', 'WellAnnotationLink','child');
select omero_41_check('Annotation', 'ImageAnnotationLink','child');
select omero_41_check('Annotation', 'OriginalFileAnnotationLink','child');
select omero_41_check('Annotation', 'PlaneInfoAnnotationLink','child');
select omero_41_check('Annotation', 'ChannelAnnotationLink','child');
select omero_41_check('Annotation', 'ExperimenterGroupAnnotationLink','child');
select omero_41_check('Annotation', 'RoiAnnotationLink','child');
select omero_41_check('Annotation', 'AnnotationAnnotationLink','child');
select omero_41_check('Annotation', 'AnnotationAnnotationLink','parent');
select omero_41_check('Annotation', 'NodeAnnotationLink','child');
select omero_41_check('Annotation', 'ProjectAnnotationLink','child');
select omero_41_check('Annotation', 'ReagentAnnotationLink','child');
select omero_41_check('Annotation', 'PlateAnnotationLink','child');
select omero_41_check('Annotation', 'ExperimenterAnnotationLink','child');
select omero_41_check('Annotation', 'ScreenAcquisitionAnnotationLink','child');
select omero_41_check('Annotation', 'ScreenAnnotationLink','child');
select omero_41_check('Annotation', 'PixelsAnnotationLink','child');
select omero_41_check('Annotation', 'DatasetAnnotationLink','child');
select omero_41_check('Annotation', 'SessionAnnotationLink','child');
select omero_41_check('FilterSet', 'OTF','filterSet');
select omero_41_check('FilterSet', 'LogicalChannel','filterSet');
select omero_41_check('StatsInfo', 'Channel','statsInfo');
select omero_41_check('Screen', 'ScreenAcquisition','screen');
select omero_41_check('Screen', 'Reagent','screen');
select omero_41_check('Screen', 'ScreenAnnotationLink','parent');
select omero_41_check('Screen', 'ScreenPlateLink','parent');
select omero_41_check('Dichroic', 'FilterSet','dichroic');
select omero_41_check('Objective', 'ObjectiveSettings','objective');
select omero_41_check('Objective', 'OTF','objective');
select omero_41_check('Experiment', 'Image','experiment');
select omero_41_check('Experiment', 'MicrobeamManipulation','experiment');
select omero_41_check('DetectorSettings', 'LogicalChannel','detectorSettings');
select omero_41_check('Filter', 'FilterSet','emFilter');
select omero_41_check('Filter', 'FilterSet','exFilter');
select omero_41_check('Filter', 'LogicalChannel','secondaryEmissionFilter');
select omero_41_check('Filter', 'LogicalChannel','secondaryExcitationFilter');
select omero_41_check('LogicalChannel', 'Channel','logicalChannel');
select omero_41_check('Shape', 'LogicalChannel','shapes');


-- The following are irregular and so must be custom written.


select omero_41_check(
           'select target.id as target_id, target.group_id as target_group, target.owner_id as target_owner, ome_perms(target.permissions) as target_perms, ' ||
           '          tbl.id as tbl_id,       tbl.group_id as tbl_group,       tbl.owner_id as tbl_owner,    ome_perms(tbl.permissions) as tbl_perms ' ||
           '  from lightsource target, lightsource tbl, laser tbl2 ' ||
           ' where target.id = tbl2.pump and tbl2.lightsource_id = tbl.id ' ||
           '   and target.group_id <> tbl.group_id;',
           'LightSource', 'Laser', 'pump');

select omero_41_check(
           'select target.id as target_id, target.group_id as target_group, target.owner_id as target_owner, ome_perms(target.permissions) as target_perms, ' ||
           '          tbl.id as tbl_id,       tbl.group_id as tbl_group,       tbl.owner_id as tbl_owner,    ome_perms(tbl.permissions) as tbl_perms ' ||
           '  from originalfile target, annotation tbl ' ||
           ' where target.id = tbl.file ' ||
           '   and target.group_id <> tbl.group_id;',
           'OriginalFile', 'FileAnnotation','file');

select omero_41_check(
           'select target.id as target_id, target.group_id as target_group, target.owner_id as target_owner, ome_perms(target.permissions) as target_perms, ' ||
           '          tbl.id as tbl_id,       tbl.group_id as tbl_group,       tbl.owner_id as tbl_owner,    ome_perms(tbl.permissions) as tbl_perms ' ||
           '  from thumbnail target, annotation tbl ' ||
           ' where target.id = tbl.thumbnail ' ||
           '   and target.group_id <> tbl.group_id;',
           'Thumbnail', 'ThumbnailAnnotation','thumbnail');


drop function omero_41_check(varchar, varchar, varchar);
drop function omero_41_check(varchar, varchar, varchar, varchar);
commit;
