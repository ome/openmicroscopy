-- Note: no annotation insert trigger

DROP TRIGGER IF EXISTS annotation_annotation_link_event_trigger_insert ON annotationannotationlink;

CREATE TRIGGER annotation_annotation_link_event_trigger_insert
        AFTER INSERT ON annotationannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.annotations.Annotation');

DROP TRIGGER IF EXISTS channel_annotation_link_event_trigger_insert ON channelannotationlink;

CREATE TRIGGER channel_annotation_link_event_trigger_insert
        AFTER INSERT ON channelannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Channel');

DROP TRIGGER IF EXISTS dataset_annotation_link_event_trigger_insert ON datasetannotationlink;

CREATE TRIGGER dataset_annotation_link_event_trigger_insert
        AFTER INSERT ON datasetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Dataset');

DROP TRIGGER IF EXISTS experimenter_annotation_link_event_trigger_insert ON experimenterannotationlink;

CREATE TRIGGER experimenter_annotation_link_event_trigger_insert
        AFTER INSERT ON experimenterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Experimenter');

DROP TRIGGER IF EXISTS experimentergroup_annotation_link_event_trigger_insert ON experimentergroupannotationlink;

CREATE TRIGGER experimentergroup_annotation_link_event_trigger_insert
        AFTER INSERT ON experimentergroupannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.ExperimenterGroup');

DROP TRIGGER IF EXISTS fileset_annotation_link_event_trigger_insert ON filesetannotationlink;

CREATE TRIGGER fileset_annotation_link_event_trigger_insert
        AFTER INSERT ON filesetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.fs.Fileset');

DROP TRIGGER IF EXISTS image_annotation_link_event_trigger_insert ON imageannotationlink;

CREATE TRIGGER image_annotation_link_event_trigger_insert
        AFTER INSERT ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Image');

DROP TRIGGER IF EXISTS namespace_annotation_link_event_trigger_insert ON namespaceannotationlink;

CREATE TRIGGER namespace_annotation_link_event_trigger_insert
        AFTER INSERT ON namespaceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Namespace');

DROP TRIGGER IF EXISTS node_annotation_link_event_trigger_insert ON nodeannotationlink;

CREATE TRIGGER node_annotation_link_event_trigger_insert
        AFTER INSERT ON nodeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Node');

DROP TRIGGER IF EXISTS originalfile_annotation_link_event_trigger_insert ON originalfileannotationlink;

CREATE TRIGGER originalfile_annotation_link_event_trigger_insert
        AFTER INSERT ON originalfileannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.OriginalFile');

DROP TRIGGER IF EXISTS pixels_annotation_link_event_trigger_insert ON pixelsannotationlink;

CREATE TRIGGER pixels_annotation_link_event_trigger_insert
        AFTER INSERT ON pixelsannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Pixels');

DROP TRIGGER IF EXISTS planeinfo_annotation_link_event_trigger_insert ON planeinfoannotationlink;

CREATE TRIGGER planeinfo_annotation_link_event_trigger_insert
        AFTER INSERT ON planeinfoannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.PlaneInfo');

DROP TRIGGER IF EXISTS plate_annotation_link_event_trigger_insert ON plateannotationlink;

CREATE TRIGGER plate_annotation_link_event_trigger_insert
        AFTER INSERT ON plateannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Plate');

DROP TRIGGER IF EXISTS plateacquisition_annotation_link_event_trigger_insert ON plateacquisitionannotationlink;

CREATE TRIGGER plateacquisition_annotation_link_event_trigger_insert
        AFTER INSERT ON plateacquisitionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.PlateAcquisition');

DROP TRIGGER IF EXISTS project_annotation_link_event_trigger_insert ON projectannotationlink;

CREATE TRIGGER project_annotation_link_event_trigger_insert
        AFTER INSERT ON projectannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Project');

DROP TRIGGER IF EXISTS reagent_annotation_link_event_trigger_insert ON reagentannotationlink;

CREATE TRIGGER reagent_annotation_link_event_trigger_insert
        AFTER INSERT ON reagentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Reagent');

DROP TRIGGER IF EXISTS roi_annotation_link_event_trigger_insert ON roiannotationlink;

CREATE TRIGGER roi_annotation_link_event_trigger_insert
        AFTER INSERT ON roiannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.roi.Roi');

DROP TRIGGER IF EXISTS screen_annotation_link_event_trigger_insert ON screenannotationlink;

CREATE TRIGGER screen_annotation_link_event_trigger_insert
        AFTER INSERT ON screenannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Screen');

DROP TRIGGER IF EXISTS session_annotation_link_event_trigger_insert ON sessionannotationlink;

CREATE TRIGGER session_annotation_link_event_trigger_insert
        AFTER INSERT ON sessionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Session');

DROP TRIGGER IF EXISTS well_annotation_link_event_trigger_insert ON wellannotationlink;

CREATE TRIGGER well_annotation_link_event_trigger_insert
        AFTER INSERT ON wellannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Well');

DROP TRIGGER IF EXISTS wellsample_annotation_link_event_trigger_insert ON wellsampleannotationlink;

CREATE TRIGGER wellsample_annotation_link_event_trigger_insert
        AFTER INSERT ON wellsampleannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.WellSample');
