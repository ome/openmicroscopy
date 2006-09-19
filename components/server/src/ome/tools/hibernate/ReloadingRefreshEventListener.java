/* ome.tools.hibernate.ReloadingRefreshEventListener
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.tools.hibernate;

// Java imports
import java.util.Map;

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.event.EventSource;
import org.hibernate.event.RefreshEvent;
import org.hibernate.event.RefreshEventListener;
import org.hibernate.util.IdentityMap;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
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
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class ReloadingRefreshEventListener implements RefreshEventListener {

	private static final long serialVersionUID = 4292680015211981832L;

	private static Log log = LogFactory
			.getLog(ReloadingRefreshEventListener.class);

	/**
	 * @see RefreshEventListener#onRefresh(RefreshEvent)
	 */
	@SuppressWarnings("unchecked")
	public void onRefresh(RefreshEvent event) throws HibernateException 
	{
		onRefresh( event, IdentityMap.instantiate(10) );
	}
	
	/** 
	 * @see RefreshEventListener#onRefresh(RefreshEvent, Map)
	 */
	@SuppressWarnings("unchecked")
	public void onRefresh(RefreshEvent event, Map refreshedAlready)
			throws HibernateException {
		IObject orig = (IObject) event.getObject();
		
		if (orig.getId() == null)
			throw new ApiUsageException("Transient entities cannot be refreshed.");
		
		if (Reloader.isUnloaded(orig)) {
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
