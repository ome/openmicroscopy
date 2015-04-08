/*
 * org.openmicroscopy.shoola.agents.util.finder.QuickFinder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.finder;



//Java imports
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.QuickSearch;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.ui.UserNotifier;


/** 
 * Class used to perform quick search.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * 
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class QuickFinder
	extends QuickSearch
	implements Finder
{

	/** Reference to the component handling data. */ 
	private List<FinderLoader> finderHandlers;
	
	/** One of the constants defined by this class. */
	private int state;
	
	/** Creates a new instance.*/
	public QuickFinder()
	{
		finderHandlers = new ArrayList<FinderLoader>();
		setDefaultSearchContext(null);
		addPropertyChangeListener(QUICK_SEARCH_PROPERTY, this);
	}
	
	/** Overridden to search for the terms. */
	public void search()
	{
		//SearchObject node = (SearchObject) evt.getNewValue();
		if (getSelectedNode() == null) return;
		List values = null;//getSelectedNode().getResult();
		if (values == null) {
			UserNotifier un = FinderFactory.getRegistry().getUserNotifier();
			un.notifyInfo("Quick Search", "Please enter a term to search for.");
			return;
		}
		String sep = "or";
		List terms = null;
		if (values.size() == 1) {
			String v = (String) values.get(0);
			v = v.toLowerCase();
			String[] array = v.split("and");
			if (array.length > 1) {
				sep = "and";
				terms = new ArrayList();
				for (int i = 0; i < array.length; i++) {
					terms.add(array[i].trim());
				}
			}
			if (terms == null) {
				array = v.split("or");
				if (array.length > 1) {
					sep = "or";
					terms = new ArrayList();
					for (int i = 0; i < array.length; i++) {
						terms.add(array[i].trim());
					}
				}
			}
		}
		if (terms == null) terms = values;	
		/*
		switch (selectedNode.getIndex()) {
			case TAGS:
				fireTagsRetrieval(terms, sep);
				break;
			case IMAGES:
				fireImagesRetrieval(terms, sep);
				break;
			case ANNOTATIONS:
				fireAnnotationsRetrieval(terms, sep);
				break;
		}
		*/
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#cancel()
	 */
	public void cancel()
	{
		Iterator i = finderHandlers.iterator();
		while (i.hasNext())
			((FinderLoader) i.next()).cancel();
		state = DISCARDED;
	}
	
	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#getState()
	 */
	public int getState() { return state; }

	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#dispose()
	 */
	public void dispose()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#setStatus(String, boolean)
	 */
	public void setStatus(String text, boolean status)
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#setResult(SecurityContext, Object)
	 */
	public void setResult(AdvancedSearchResultCollection result) {}

	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#setExistingTags(Collection)
	 */
	public void setExistingTags(Collection tags) {}
	
}
