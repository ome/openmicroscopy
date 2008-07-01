/*
 *   $Id: PojosGetImagesQueryDefinition.java 1931 2007-11-12 10:39:50Z ola $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.query;

import static ome.parameters.Parameters.OPTIONS;

import java.sql.SQLException;
import java.util.Map;

import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.tools.hibernate.QueryBuilder;
import ome.util.builders.PojoOptions;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class PojosGetImagesByOptionsQueryDefinition extends Query {

    static Definitions defs = new Definitions(new OptionsQueryParameterDef());

    public PojosGetImagesByOptionsQueryDefinition(Parameters parameters) {
        super(defs, parameters);
    }

    @Override
    protected void buildQuery(Session session) throws HibernateException,
            SQLException {

        // TODO copied from PojosGetImagesQueryDefinition. Should be merged.
        QueryBuilder qb = new QueryBuilder(256);
        qb.select("img");
        qb.from("Image", "img");
        qb.join("img.details.creationEvent", "ce", true, true);
        qb.join("img.details.updateEvent", "ue", true, true);
        qb.join("img.pixels", "pix", true, true);
        qb.join("pix.pixelsType", "pt", true, true);
        qb.join("pix.pixelsDimensions", "pd", true, true);
        qb.join("img.annotationLinksCountPerOwner", "i_c_ann", true, true);
        // qb.join("img.datasetLinksCountPerOwner", "i_c_ds", true, true);

        qb.where();

        // if PojoOptions sets START_TIME and/or END_TIME
        if (check(OPTIONS)) {
            PojoOptions po = new PojoOptions((Map) value(OPTIONS));
            if (po.getStartTime() != null) {
                qb.and("img.details.creationEvent.time > :starttime");
                qb.param("starttime", po.getStartTime());
            }
            if (po.getEndTime() != null) {
                qb.and("img.details.creationEvent.time < :endtime");
                qb.param("endtime", po.getEndTime());
            }
        }

        setQuery(qb.query(session));
    }

    @Override
    protected void enableFilters(Session session) {
        ownerOrGroupFilters(session, new String[] { Image.OWNER_FILTER },
                new String[] { Image.GROUP_FILTER });
    }

}
// select i from Image i
// #bottomUpHierarchy()
// where
// #imagelist()
// #filters()
// #typeExperimenter()
