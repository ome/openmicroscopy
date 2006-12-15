/*
 * ome.logic.AbstractLevel2Service
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

package ome.logic;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;

/**
 * service level 2
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel2Service extends AbstractBean {

    protected transient LocalUpdate iUpdate;

    protected transient LocalQuery iQuery;

    // ~ Selfconfiguration (injection) for Non-JavaEE
    // =========================================================================

    public final void setUpdateService(LocalUpdate update) {
        throwIfAlreadySet(this.iUpdate, update);
        this.iUpdate = update;
    }

    public final void setQueryService(LocalQuery query) {
        throwIfAlreadySet(this.iQuery, query);
        this.iQuery = query;
    }

}
