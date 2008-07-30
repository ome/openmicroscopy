/*
 * ome.security.basic.FlushEntityListener
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports

// Third-party imports
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.event.FlushEntityEvent;
import org.hibernate.event.def.DefaultFlushEntityEventListener;
import org.springframework.util.Assert;

/**
 * responsible for responding to {@link FlushEntityEvent}. Necessary to perform
 * clean up of entities.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since 3.0
 * @see BasicSecuritySystem#lockMarked()
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class FlushEntityEventListener extends DefaultFlushEntityEventListener {

    private static final long serialVersionUID = 240558701677298961L;

    private static Log log = LogFactory.getLog(FlushEntityEventListener.class);

    private final OmeroInterceptor interceptor;

    /** main constructor. Requires a non-null security system */
    public FlushEntityEventListener(OmeroInterceptor interceptor) {
        Assert.notNull(interceptor);
        this.interceptor = interceptor;
    }

    @Override
    public void onFlushEntity(FlushEntityEvent event) throws HibernateException {

        interceptor.lockMarked();
        super.onFlushEntity(event);
    }
}
