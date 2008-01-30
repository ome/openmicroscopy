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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchComponent;
import org.openmicroscopy.shoola.util.ui.search.SearchContext;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;

import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * The class actually managing the search.
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
	private static final String 	TITLE = "Search";
	
	/** Reference to the component handling data. */ 
	private List<FinderLoader>		finderHandlers;
	
	/** One of the constants defined by this class. */
	private int						state;
	
	/** Collection of selected users. */
	private List<ExperimenterData>	users;
	
	/**
	 * Determines the scope of the search.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	private Class convertScope(int value)
	{
		switch (value) {
			case SearchContext.TEXT_ANNOTATION:
				return AnnotationData.class;
			case SearchContext.TAGS:
				return CategoryData.class;
			case SearchContext.DATASETS:
				return DatasetData.class;
			case SearchContext.PROJECTS:
				return ProjectData.class;
			case SearchContext.IMAGES:
				return ImageData.class;
			default:
				return null;
		}
	}
	
	/**
	 * Converts the UI context into a searchable context.
	 * 
	 * @param ctx The value to convert.
	 */
	private void handleSearchContext(SearchContext ctx)
	{
		String[] some = ctx.getSome();
		String[] must = ctx.getMust();
		String[] none = ctx.getNone();
		UserNotifier un = FinderFactory.getRegistry().getUserNotifier();
		if (some == null && must == null && none == null) {
			un.notifyInfo(TITLE, "Please enter a term to search for.");
			return;
		}
		List<Integer> context = ctx.getContext();
		if (context == null || context.size() == 0) {
			un.notifyInfo(TITLE, "Please enter a context.");
			return;
		}
		Timestamp start = ctx.getStartTime();
		Timestamp end = ctx.getEndTime();
		if (start != null && end != null && start.after(end)) {
			un.notifyInfo(TITLE, "The time interval selected is not valid.");
			return;
		}
		List<Class> scope = new ArrayList<Class>(context.size());
		Iterator i = context.iterator();
		Class k;
		while (i.hasNext()) {
			k = convertScope((Integer) i.next());
			if (k != null) scope.add(k);
		}
			
		
		List<Class> types = new ArrayList<Class>();
		i = ctx.getType().iterator();
		
		while (i.hasNext()) {
			k = convertScope((Integer) i.next());
			if (k != null) types.add(k);
		}
		
		switch (ctx.getUserSearchContext()) {
			case SearchContext.JUST_CURRENT_USER:
			case SearchContext.CURRENT_USER_AND_OTHERS:
				users.add(getUserDetails());
		}
		SearchDataContext searchContext = new SearchDataContext(scope, types, 
										some, must, none);
		searchContext.setTimeInterval(start, end);
		searchContext.setUsers(users);
		searchContext.setCaseSensitive(ctx.isCaseSensitive());
		searchContext.setNumberOfResults(ctx.getNumberOfResults());
		AdvancedFinderLoader loader = new AdvancedFinderLoader(this, 
													searchContext);
		loader.load();
		finderHandlers.add(loader);
		state = SEARCH;
		setSearchEnabled(true);
	}
	
	/**
	 * Returns the current user's details.
	 * 
	 * @return See above.
	 */
	private ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) FinderFactory.getRegistry().lookup(
				LookupNames.CURRENT_USER_DETAILS);
	}
	
	/**
	 * Returns the available groups.
	 * 
	 * @return See above.
	 */
	private Map getAvailableGroups()
	{
		return (Map) FinderFactory.getRegistry().lookup(
				LookupNames.USER_GROUP_DETAILS);
	}
	
	/** Displays the widget allowing the select users. */
	private void showUserSelection()
	{
		IconManager icons = IconManager.getInstance();
		UserManagerDialog dialog = new UserManagerDialog(
				FinderFactory.getRefFrame(), getUserDetails(), 
				getAvailableGroups(), icons.getIcon(IconManager.OWNER), 
				icons.getIcon(IconManager.OWNER_48));
		dialog.addPropertyChangeListener(this);
		dialog.setDefaultSize();
		UIUtilities.centerAndShow(dialog);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param registry 	Helper reference to the registry. Mustn't be 
	 * 					<code>null</code>.
	 */
	public AdvancedFinder()
	{
		super(FinderFactory.getRefFrame());
		finderHandlers = new ArrayList<FinderLoader>();
		addPropertyChangeListener(SEARCH_PROPERTY, this);
		addPropertyChangeListener(CANCEL_SEARCH_PROPERTY, this);
		addPropertyChangeListener(OWNER_PROPERTY, this);
		//setModal(true);
		users = new ArrayList<ExperimenterData>();
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
		setSearchEnabled(false);
		setVisible(false);
		cancel();
	}

	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#setStatus(String, boolean)
	 */
	public void setStatus(String text, boolean status)
	{
		if (text == null) text = "";
		setSearchEnabled(text, status);
	}
	
	/**
	 * Reacts to the property fired by the <code>SearchComponent</code>
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SEARCH_PROPERTY.equals(name)) {
			SearchContext ctx = (SearchContext) evt.getNewValue();
			if (ctx == null) return;
			handleSearchContext(ctx);
		} else if (CANCEL_SEARCH_PROPERTY.equals(name)) {
			cancel();
		} else if (OWNER_PROPERTY.equals(name)) {
			showUserSelection();
		} else if (UserManagerDialog.USER_SWITCH_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null) return;
			Iterator i = m.keySet().iterator();
			ExperimenterData exp;
			while (i.hasNext()) {
				exp = (ExperimenterData) m.get(i.next());
				if (!users.contains(exp)) {
					users.add(exp);
					setUserString(exp.getFirstName()+SearchUtil.SPACE_SEPARATOR+
								exp.getLastName());
				}
			}
		}
	}

}
