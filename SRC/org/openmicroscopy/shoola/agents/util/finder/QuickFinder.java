/*
 * org.openmicroscopy.shoola.agents.util.finder.QuickFinder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.search.QuickSearch;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class QuickFinder
	extends QuickSearch
	implements Finder
{
	
	/** Reference to the registry. */
	private Registry 				registry;
	
	/** Reference to the component handling data. */ 
	private List<FinderLoader>	finderHandlers;
	
	/** One of the constants defined by this class. */
	private int						state;
	
	/** 
	 * Searches for the passed values.
	 * 
	 * @param values The value to search for.
	 */
	private void fireTagsRetrieval(List values)
	{
		state = SEARCH;
		QuickFinderLoader handler = new QuickFinderLoader(this, registry, 
										values, QuickFinderLoader.TAGS);
		handler.load();
		finderHandlers.add(handler);
	}
	
	/** 
	 * Searches for the passed values.
	 * 
	 * @param values The value to search for.
	 */
	private void fireImagesRetrieval(List values)
	{
		state = SEARCH;
		QuickFinderLoader handler = new QuickFinderLoader(this, registry, 
										values, QuickFinderLoader.IMAGES);
		handler.load();
		finderHandlers.add(handler);
	}
	
	/** 
	 * Searches for the passed values.
	 * 
	 * @param values The value to search for.
	 */
	private void fireAnnotationsRetrieval(List values)
	{
		state = SEARCH;
		QuickFinderLoader handler = new QuickFinderLoader(this, registry, 
										values, QuickFinderLoader.ANNOTATIONS);
		handler.load();
		finderHandlers.add(handler);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param registry Helper reference to the registry. Mustn't be 
	 * 					<code>null</code>.
	 */
	public QuickFinder(Registry registry)
	{
		if (registry == null)
			throw new IllegalArgumentException("No registry.");
		this.registry = registry;
		finderHandlers = new ArrayList<FinderLoader>();
		setDefaultSearchContext();
		addPropertyChangeListener(QUICK_SEARCH_PROPERTY, this);
	}
	
	/** Overridden to search for the terms. */
	public void search()
	{
		//SearchObject node = (SearchObject) evt.getNewValue();
		if (selectedNode == null) return;
		switch (selectedNode.getIndex()) {
			case TAGS:
				fireTagsRetrieval(selectedNode.getResult());
				break;
			case IMAGES:
				fireImagesRetrieval(selectedNode.getResult());
				break;
			case ANNOTATIONS:
				fireAnnotationsRetrieval(selectedNode.getResult());
		}
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
	
}
