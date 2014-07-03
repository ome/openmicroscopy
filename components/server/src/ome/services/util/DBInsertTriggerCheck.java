/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.util;

import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring bean run on start-up to make sure that there the necessary insert
 * triggers are in place for search. Each change to an annotation or an
 * annotation link should generate a REINDEX to the appropriate type.
 *
 * A SQL upgrade script handles this issue for 5.1 and beyond.
 *
 * @since 5.0.3
 */
public class DBInsertTriggerCheck extends BaseDBCheck {

    private static final Logger log = LoggerFactory.getLogger(DBInsertTriggerCheck.class);

    private static final String[][] triggers = new String[][]{
        // Skipping insert trigger on annotation itself.
        new String[]{"annotation_annotation_link_event_trigger_insert", "annotationannotationlink", "annotation_link_event_trigger('ome.model.annotations.Annotation')"},
        new String[]{"channel_annotation_link_event_trigger_insert", "channelannotationlink", "annotation_link_event_trigger('ome.model.core.Channel')"},
        new String[]{"dataset_annotation_link_event_trigger_insert", "datasetannotationlink", "annotation_link_event_trigger('ome.model.containers.Dataset')"},
        new String[]{"experimenter_annotation_link_event_trigger_insert", "experimenterannotationlink", "annotation_link_event_trigger('ome.model.meta.Experimenter')"},
        new String[]{"experimentergroup_annotation_link_event_trigger_insert", "experimentergroupannotationlink", "annotation_link_event_trigger('ome.model.meta.ExperimenterGroup')"},
        new String[]{"fileset_annotation_link_event_trigger_insert", "filesetannotationlink", "annotation_link_event_trigger('ome.model.fs.Fileset')"},
        new String[]{"image_annotation_link_event_trigger_insert", "imageannotationlink", "annotation_link_event_trigger('ome.model.core.Image')"},
        new String[]{"namespace_annotation_link_event_trigger_insert", "namespaceannotationlink", "annotation_link_event_trigger('ome.model.meta.Namespace')"},
        new String[]{"node_annotation_link_event_trigger_insert", "nodeannotationlink", "annotation_link_event_trigger('ome.model.meta.Node')"},
        new String[]{"originalfile_annotation_link_event_trigger_insert", "originalfileannotationlink", "annotation_link_event_trigger('ome.model.core.OriginalFile')"},
        new String[]{"pixels_annotation_link_event_trigger_insert", "pixelsannotationlink", "annotation_link_event_trigger('ome.model.core.Pixels')"},
        new String[]{"planeinfo_annotation_link_event_trigger_insert", "planeinfoannotationlink", "annotation_link_event_trigger('ome.model.core.PlaneInfo')"},
        new String[]{"plate_annotation_link_event_trigger_insert", "plateannotationlink", "annotation_link_event_trigger('ome.model.screen.Plate')"},
        new String[]{"plateacquisition_annotation_link_event_trigger_insert", "plateacquisitionannotationlink", "annotation_link_event_trigger('ome.model.screen.PlateAcquisition')"},
        new String[]{"project_annotation_link_event_trigger_insert", "projectannotationlink", "annotation_link_event_trigger('ome.model.containers.Project')"},
        new String[]{"reagent_annotation_link_event_trigger_insert", "reagentannotationlink", "annotation_link_event_trigger('ome.model.screen.Reagent')"},
        new String[]{"roi_annotation_link_event_trigger_insert", "roiannotationlink", "annotation_link_event_trigger('ome.model.roi.Roi')"},
        new String[]{"screen_annotation_link_event_trigger_insert", "screenannotationlink", "annotation_link_event_trigger('ome.model.screen.Screen')"},
        new String[]{"session_annotation_link_event_trigger_insert", "sessionannotationlink", "annotation_link_event_trigger('ome.model.meta.Session')"},
        new String[]{"well_annotation_link_event_trigger_insert", "wellannotationlink", "annotation_link_event_trigger('ome.model.screen.Well')"},
        new String[]{"wellsample_annotation_link_event_trigger_insert", "wellsampleannotationlink", "annotation_link_event_trigger('ome.model.screen.WellSample')"},
    };

    private final SqlAction sql;

    public DBInsertTriggerCheck(Executor executor, SqlAction sql) {
        super(executor);
        this.sql = sql;
    }

    @Override
    protected void doCheck() {
        for (String[] trigger : triggers) {
            try {
                String name = trigger[0];
                String table = trigger[1];
                String procedure = trigger[2];
                sql.createInsertTrigger(name, table, procedure);
            } catch (Exception e) {
                log.error("Failed to create trigger: {}", trigger, e);
            }
        }
    }
}
