--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- ticket:1047 Adding UNIQUE (parent, child) constraints
-- on all link types
--

BEGIN;

    INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                 values ('OMERO3A',      10,              'OMERO3A',           9);

    alter table annotationannotationlink add constraint annotationannotationlink_parent_key UNIQUE (parent, child);
    alter table categorygroupcategorylink add constraint categorygroupcategorylink_parent_key UNIQUE (parent, child);
    alter table categoryimagelink add constraint categoryimagelink_parent_key UNIQUE (parent, child);
    alter table channelannotationlink add constraint channelannotationlink_parent_key UNIQUE (parent, child);
    alter table datasetannotationlink add constraint datasetannotationlink_parent_key UNIQUE (parent, child);
    alter table datasetimagelink add constraint datasetimagelink_parent_key UNIQUE (parent, child);
    alter table experimenterannotationlink add constraint experimenterannotationlink_parent_key UNIQUE (parent, child);
    alter table experimentergroupannotationlink add constraint experimentergroupannotationlink_parent_key UNIQUE (parent, child);
    alter table groupexperimentermap add constraint groupexperimentermap_parent_key UNIQUE (parent, child);
    alter table imageannotationlink add constraint imageannotationlink_parent_key UNIQUE (parent, child);
    alter table joboriginalfilelink add constraint joboriginalfilelink_parent_key UNIQUE (parent, child);
    alter table originalfileannotationlink add constraint originalfileannotationlink_parent_key UNIQUE (parent, child);
    alter table pixelsannotationlink add constraint pixelsannotationlink_parent_key UNIQUE (parent, child);
    alter table pixelsoriginalfilemap add constraint pixelsoriginalfilemap_parent_key UNIQUE (parent, child);
    alter table planeinfoannotationlink add constraint planeinfoannotationlink_parent_key UNIQUE (parent, child);
    alter table plateannotationlink add constraint plateannotationlink_parent_key UNIQUE (parent, child);
    alter table projectannotationlink add constraint projectannotationlink_parent_key UNIQUE (parent, child);
    alter table projectdatasetlink add constraint projectdatasetlink_parent_key UNIQUE (parent, child);
    alter table reagentannotationlink add constraint reagentannotationlink_parent_key UNIQUE (parent, child);
    alter table roilink add constraint roilink_parent_key UNIQUE (parent, child);
    alter table roilinkannotationlink add constraint roilinkannotationlink_parent_key UNIQUE (parent, child);
    alter table screenacquisitionannotationlink add constraint screenacquisitionannotationlink_parent_key UNIQUE (parent, child);
    alter table screenacquisitionwellsamplelink add constraint screenacquisitionwellsamplelink_parent_key UNIQUE (parent, child);
    alter table screenannotationlink add constraint screenannotationlink_parent_key UNIQUE (parent, child);
    alter table screenplatelink add constraint screenplatelink_parent_key UNIQUE (parent, child);
    alter table sessionannotationlink add constraint sessionannotationlink_parent_key UNIQUE (parent, child);
    alter table wellannotationlink add constraint wellannotationlink_parent_key UNIQUE (parent, child);
    alter table wellreagentlink add constraint wellreagentlink_parent_key UNIQUE (parent, child);
    alter table wellsampleannotationlink add constraint wellsampleannotationlink_parent_key UNIQUE (parent, child);

   UPDATE dbpatch set message = 'Database updated.', finished = now()
    where currentVersion  = 'OMERO3A' and
          currentPatch    = 10         and
          previousVersion = 'OMERO3A' and
          previousPatch   = 9;


COMMIT;
