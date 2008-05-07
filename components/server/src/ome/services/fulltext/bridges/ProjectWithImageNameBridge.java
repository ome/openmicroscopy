/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext.bridges;

import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.services.fulltext.BridgeHelper;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;

/**
 * Example custom {@link FieldBridge} implementation which parses all
 * {@link Image} names from a {@link Project} and inserts them into the index
 * for that {@link Project}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ProjectWithImageNameBridge extends BridgeHelper {

    /**
     * If the "value" argument is a {@link Project}, this
     * {@link FieldBridge bridge} gathers all images and adds them to the index
     * with a slightly reduced boost value. The field name of the image name is
     * "image_name" but the values are also added to the
     * {@link BridgeHelper#COMBINED} field via the
     * {@link #add(Document, String, String, org.apache.lucene.document.Field.Store, org.apache.lucene.document.Field.Index, Float)}
     * method.
     */
    @Override
    public void set(final String name, final Object value,
            final Document document, final Field.Store store,
            final Field.Index index, final Float boost) {

        if (value instanceof Project) {

            logger().info("Indexing all image names for " + value);

            final float reduced_boost = boost.floatValue() / 2;

            final Project p = (Project) value;
            for (ProjectDatasetLink pdl : p.unmodifiableDatasetLinks()) {
                final Dataset d = pdl.child();
                for (DatasetImageLink dil : d.unmodifiableImageLinks()) {
                    final Image i = dil.child();

                    // Name is never null, but as an example it is important
                    // to always check the value for null, and either simply
                    // not call add() or to use a null token like "null".
                    if (i.getName() != null) {
                        add(document, "image_name", i.getName(), store, index,
                                reduced_boost);
                    } else {
                        add(document, "image_name", "null", store, index,
                                reduced_boost);
                    }
                }
            }
        }
    }
}