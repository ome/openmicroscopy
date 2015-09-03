/*
 * ome.services.query.AbstractClassIdsOptionsQuery
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.services.query;

import ome.parameters.Parameters;

/**
 * simple abstract class that provides a common
 * {@link ome.services.query.Definitions} containing:
 * {@link ome.services.query.IdsQueryParameterDef},
 * {@link ome.services.query.OptionsQueryParameterDef}, and
 * {@link ome.services.query.ClassQueryParameterDef}.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 */
public abstract class AbstractClassIdsOptionsQuery extends Query {

    static Definitions defs = new Definitions(new IdsQueryParameterDef(),
            new ClassQueryParameterDef());

    public AbstractClassIdsOptionsQuery(Parameters parameters) {
        super(defs, parameters);
    }

}
