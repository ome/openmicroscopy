/*
 * ome.tools.hibernate.ReloadingRefreshEventListener
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.hibernate;

// Java imports
import java.util.Map;

// Third-party imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.event.EventSource;
import org.hibernate.event.RefreshEvent;
import org.hibernate.event.RefreshEventListener;
import org.hibernate.util.IdentityMap;

// Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.model.IObject;

/**
 * responsible for responding to {@link RefreshEvent}. in particular in
 * reloading the {@link IObject#unload() unloaded} entities.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since 3.0-M3
 */
public class ReloadingRefreshEventListener implements RefreshEventListener {

    private static final long serialVersionUID = 4292680015211981832L;

    private static Logger log = LoggerFactory
            .getLogger(ReloadingRefreshEventListener.class);

    /**
     * @see RefreshEventListener#onRefresh(RefreshEvent)
     */
    @SuppressWarnings("unchecked")
    public void onRefresh(RefreshEvent event) throws HibernateException {
        onRefresh(event, IdentityMap.instantiate(10));
    }

    /**
     * @see RefreshEventListener#onRefresh(RefreshEvent, Map)
     */
    @SuppressWarnings("unchecked")
    public void onRefresh(RefreshEvent event, Map refreshedAlready)
            throws HibernateException {
        IObject orig = (IObject) event.getObject();

        if (orig.getId() == null) {
            throw new ApiUsageException(
                    "Transient entities cannot be refreshed.");
        }

        if (HibernateUtils.isUnloaded(orig)) {
            final EventSource source = event.getSession();
            log("Reloading unloaded entity:", orig.getClass(), ":", orig
                    .getId());
            Object obj = source.load(orig.getClass(), orig.getId());
            refreshedAlready.put(orig, obj);
            return; // EARLY EXIT!
        }
    }

    // ~ Helpers
    // =========================================================================

    private void log(Object... objects) {
        if (log.isDebugEnabled() && objects != null && objects.length > 0) {
            StringBuilder sb = new StringBuilder(objects.length * 16);
            for (Object obj : objects) {
                sb.append(obj.toString());
            }
            log.debug(sb.toString());
        }
    }
}
