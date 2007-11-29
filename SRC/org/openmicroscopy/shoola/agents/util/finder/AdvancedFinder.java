/*
 * org.openmicroscopy.shoola.agents.util.finder.AdvancedFinder 
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
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.search.SearchComponent;
import org.openmicroscopy.shoola.util.ui.search.SearchContext;

import pojos.ExperimenterData;

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
public class AdvancedFinder
	extends SearchComponent
	implements Finder, PropertyChangeListener
{

	/** The default title of the notification message. */
	private static final String TITLE = "Search";
	
	/** Reference to the registry. */
	private Registry 			registry;
	
	/** Reference to the component handling data. */ 
	private List<FinderLoader>	finderHandlers;
	
	/** One of the constants defined by this class. */
	private int					state;
	
	private int convertScope(int value)
	{
		switch (value) {
			case SearchContext.ANNOTATIONS:
				return FinderLoader.ANNOTATIONS;
			case SearchContext.TAGS:
				return FinderLoader.TAGS;
			case SearchContext.DATASETS:
				return FinderLoader.DATASETS;
			case SearchContext.PROJECTS:
				return FinderLoader.PROJECTS;
			default:
			case SearchContext.IMAGES:
				return FinderLoader.IMAGES;
		}
	}
	
	private void handleSearchContext(SearchContext ctx)
	{
		List<String> terms = ctx.getTerms();
		UserNotifier un = registry.getUserNotifier();
		if (terms == null || terms.size() == 0) {
			un.notifyInfo(TITLE, "Please enter a term to search for.");
			return;
		}
		List<Integer> context = ctx.getContext();
		if (context == null || context.size() == 0) {
			un.notifyInfo(TITLE, "Please enter a context.");
			return;
		}
		List<Integer> scope = new ArrayList<Integer>(context.size());
		Iterator i = context.iterator();
		int index; 
		while (i.hasNext()) {
			index = (Integer) i.next();
			scope.add(convertScope(index));
		}
		List<String> users = ctx.getUsers();
		List<ExperimenterData> exps = null;
		if (users != null) {
			exps = new ArrayList<ExperimenterData>();
			i = users.iterator();
			String user;
			ExperimenterData exp;
			while (i.hasNext()) {
				user = (String) i.next();
				if (user != null) {
					exp = new ExperimenterData();
					exp.setLastName(user);
					exps.add(exp);
				}
			}
		}
		AdvancedFinderLoader loader = new AdvancedFinderLoader(this, registry,
				terms, exps, scope, ctx.getStartTime(),
				ctx.getEndTime());
		loader.load();
		finderHandlers.add(loader);
		state = SEARCH;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner 	The owner of the frame.
	 * @param registry 	Helper reference to the registry. Mustn't be 
	 * 					<code>null</code>.
	 */
	public AdvancedFinder(JFrame owner, Registry registry)
	{
		super(owner);
		if (registry == null)
			throw new IllegalArgumentException("No registry.");
		this.registry = registry;
		finderHandlers = new ArrayList<FinderLoader>();
		addPropertyChangeListener(SEARCH_PROPERTY, this);
		//setModal(true);
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
	 * Reacts to the property fired by the <code>SearchComponent</code>
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		SearchContext ctx = (SearchContext) evt.getNewValue();
		if (ctx == null) return;
		handleSearchContext(ctx);
	}

}
