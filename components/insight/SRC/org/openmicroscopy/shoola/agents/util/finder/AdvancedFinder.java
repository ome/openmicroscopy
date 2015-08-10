/*
 * org.openmicroscopy.shoola.agents.util.finder.AdvancedFinder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.agents.dataBrowser.view.SearchComponent;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.LookupNames;

import omero.gateway.SecurityContext;
import omero.gateway.model.SearchResultCollection;
import omero.gateway.model.SearchParameters;
import omero.gateway.model.SearchScope;

import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.GroupContext;
import org.openmicroscopy.shoola.util.ui.search.SearchContext;
import org.openmicroscopy.shoola.util.ui.search.SearchHelp;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellData;

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
	private static final String TITLE = "Search";
	
	/** Reference to the component handling data. */ 
	private FinderLoader loader;
	
	/** One of the constants defined by this class. */
	private int state;
	
	/** Collection of selected users. */
	private Map<Long, ExperimenterData> users;
    
	/** The collection of tags. */
	private Collection tags;
	
	/** Host the result per group.*/
	private SearchResultCollection results = new SearchResultCollection();
	
	/** The identifier of the group.*/
	private long groupId;
	
	/** The components used to sort the nodes.*/
	private ViewerSorter sorter;
	
	/** The display mode e.g. Experimenter/Group.*/
	private int displayMode;

	/**
	 * Determines the scope of the search.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	private SearchScope convertScope(int value)
	{
		switch (value) {
			case SearchContext.TEXT_ANNOTATION:
				return SearchScope.ANNOTATION;
			case SearchContext.TAGS:
				return SearchScope.ANNOTATION;
			case SearchContext.URL_ANNOTATION:
				return SearchScope.ANNOTATION;
			case SearchContext.FILE_ANNOTATION:
				return SearchScope.ANNOTATION;
			case SearchContext.NAME:
				return SearchScope.NAME;
			case SearchContext.DESCRIPTION:
				return SearchScope.DESCRIPTION;
			case SearchContext.ANNOTATION:
			    return SearchScope.ANNOTATION;
			default:
				return null;
		}
	}
	
	/**
	 * Determines the type of the search.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	private Class<? extends DataObject> convertType(int value)
	{
		switch (value) {
			case SearchContext.DATASETS: return DatasetData.class;
			case SearchContext.PROJECTS: return ProjectData.class;
			case SearchContext.IMAGES: return ImageData.class;
			case SearchContext.SCREENS: return ScreenData.class;
			case SearchContext.PLATES: return PlateData.class;
			case SearchContext.WELLS: return WellData.class;
			default:
				return null;
		}
	}

	/**
	 * Converts the UI context into a context to search for.
	 * 
	 * @param ctx The value to convert.
	 */
	private void handleSearchContext(SearchContext ctx)
	{
		String query = ctx.getQuery();
		UserNotifier un = FinderFactory.getRegistry().getUserNotifier();
		Timestamp start = ctx.getStartTime();
		Timestamp end = ctx.getEndTime();
		if (start != null && end != null && start.after(end)) {
			un.notifyInfo(TITLE, "The selected time interval is not valid.");
			return;
		}
		
		if (CommonsLangUtils.isEmpty(query) && start == null && end == null) {
			un.notifyInfo(TITLE, "Please enter a term to search for " +
				"or a valid time interval.");
			return;
		}
		
		List<Integer> context = ctx.getContext();
		if (context == null || context.size() == 0) {
			context = new ArrayList<Integer>();
			context.add(SearchContext.CUSTOMIZED);
		}
		Set<SearchScope> scope = new HashSet<SearchScope>();
		Iterator i = context.iterator();
		SearchScope v;
		while (i.hasNext()) {
			v = convertScope((Integer) i.next());
			if (v != null) scope.add(v);
		}
		List<Class<? extends DataObject>> types = new ArrayList<Class<? extends DataObject>>();
		i = ctx.getType().iterator();
		Class<? extends DataObject> k;
		while (i.hasNext()) {
			k = convertType((Integer) i.next());
			if (k != null) types.add(k);
		}
		
		SearchParameters searchContext = new SearchParameters(scope, types, query);
		searchContext.setTimeInterval(start, end, ctx.getTimeType());
		searchContext.setUserId(ctx.getSelectedOwner());
		
		SecurityContext secCtx;
		
		if (ctx.getSelectedGroup() == GroupContext.ALL_GROUPS_ID) {
		    secCtx = new SecurityContext(
		            FinderFactory.getUserDetails().getGroupId());
		    searchContext.setGroupId(SearchParameters.ALL_GROUPS_ID);
		}
		else {
		    secCtx = new SecurityContext(ctx.getSelectedGroup());
		    searchContext.setGroupId(ctx.getSelectedGroup());
		}

		loader = new AdvancedFinderLoader(this, secCtx, searchContext);
		loader.load();
		state = Finder.SEARCH;
		setSearchEnabled(-1);
	}

	/** Loads the tags. */
	private void loadTags()
	{
		if (tags == null) {
			List<SecurityContext> l = new ArrayList<SecurityContext>();
			Iterator<GroupData> i = groups.iterator();
			while (i.hasNext()) {
				l.add(new SecurityContext(i.next().getId()));
			}
			TagsLoader loader = new TagsLoader(this, l);
			loader.load();
		} else setExistingTags(tags);
	}
	
	/**
	 * Handles the selection.
	 * 
	 * @param selected The selected tags.
	 */
	private void handleTagsSelection(Collection selected)
	{
		List<String> toAdd = new ArrayList<String>();
		if (selected == null || selected.size() == 0) {
			setTerms(toAdd);
			return;
		}
		Iterator i = selected.iterator();
		TagAnnotationData tag;
		String value;
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			value = tag.getTagValue();
			if (value.contains(SearchUtil.SPACE_SEPARATOR)) {
				toAdd.add(SearchUtil.QUOTE_SEPARATOR+value+
						SearchUtil.QUOTE_SEPARATOR);
			} else toAdd.add(value);
		}	
		setTerms(toAdd);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param groups The available groups.
	 */
	AdvancedFinder(Collection<GroupData> groups)
	{
		//sort
		displayMode = LookupNames.EXPERIMENTER_DISPLAY;
		sorter = new ViewerSorter();
		List<GroupData> l = sorter.sort(groups);
		initialize(l);
		addPropertyChangeListener(SEARCH_PROPERTY, this);
		addPropertyChangeListener(CANCEL_SEARCH_PROPERTY, this);
		users = new HashMap<Long, ExperimenterData>();
	}

	/**
	 * Brings up the <code>Help</code> dialog.
	 * @see #help(String)
	 */
	protected void help(String url)
	{
	    if (CommonsLangUtils.isBlank(url)) return;
	    SearchHelp help = new SearchHelp(FinderFactory.getRefFrame(), url);
	    UIUtilities.centerAndShow(help);

	    if (help.hasError()) {
	        FinderFactory.getRegistry().getUserNotifier()
	        .notifyError(
	                "Could not open web browser",
	                "Please open your web browser and go to page: "
	                        + url);
	    }
	}

	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#cancel()
	 */
	public void cancel()
	{
		if (loader != null) loader.cancel();
		results.clear();
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
		setSearchEnabled(-1);
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
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#setResult(SecurityContext, Object)
	 */
	public void setResult(SearchResultCollection result)
	{
            if (result.isError()) {
                String msg = "";
                switch (result.getError()) {
                    case SearchResultCollection.GENERAL_ERROR:
                        msg = "Invalid search expression";
                        break;
                    case SearchResultCollection.TOO_MANY_RESULTS_ERROR:
                        msg = "Too many results, please refine your search criteria.";
                        break;
                    case SearchResultCollection.TOO_MANY_CLAUSES:
                        msg = "Please try to narrow down your query. The wildcard matched too many terms.";
                        break;
                }
                UserNotifier un = FinderFactory.getRegistry().getUserNotifier();
                un.notifyError("Search error", msg);
                setSearchEnabled(-1);
                return;
            }
    
            results = result;
            setSearchEnabled(result.size());
            firePropertyChange(RESULTS_FOUND_PROPERTY, null, results);
	}
	
	/** 
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#setExistingTags(Collection)
	 */
	public void setExistingTags(Collection tags)
	{
		this.tags = tags;
		if (tags == null || tags.size() == 0) {
			UserNotifier un = FinderFactory.getRegistry().getUserNotifier();
			un.notifyInfo("Existing Tags", "No existing tags to search by.");
			return;
		}
		IconManager icons = IconManager.getInstance();
		String title = "Filter By Tags";
		String text = "Select the Tags to filter by.";
		Collection selected = new ArrayList<TagAnnotationData>();
		Iterator i = tags.iterator();
		TagAnnotationData tag;
		List<String> l = getTerms();
		Collection available = new ArrayList<TagAnnotationData>();
		
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (l.contains(tag.getTagValue()))
				selected.add(tag);
			else available.add(tag);
		}
		SelectionWizard wizard = new SelectionWizard(
				FinderFactory.getRefFrame(), 
				available, selected, TagAnnotationData.class, false, 
				FinderFactory.getUserDetails());
		wizard.setGroups(groups);
		wizard.setTitle(title, text, icons.getIcon(IconManager.TAG_48));
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}
	
	/** 
	 * Sets the current tags.
	 * 
	 * @param groupId The identifier of the group.
	 */
	public void setCurrentGroup(long groupId)
	{
		if (this.groupId == groupId) return;
		this.groupId = groupId;
		tags = null;
	}
	
	/** 
	 * Resets the component after switching users.
	 * 
	 * @param groups The collection of groups to handle.
	 */
	public void reset(Collection<GroupData> groups)
	{
		sorter = new ViewerSorter();
		this.groups = sorter.sort(groups);
		users.clear();
		results.clear();
		if (tags != null) tags.clear();
		tags = null;
		groupsContext.clear();
		addResult(null, true);
		uiDelegate.reset();
	}
	
	/**
	 * Sets the display mode e.g. Group Display
	 * 
	 * @param displayMode The value to set.
	 */
	public void setDisplayMode(int displayMode)
	{
		this.displayMode = displayMode;
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
		} else if (UserManagerDialog.USER_SWITCH_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null) return;
			Iterator i = m.keySet().iterator();
			List<ExperimenterData> l;
			ExperimenterData exp;
			Iterator<ExperimenterData> j;
			while (i.hasNext()) {
				l = (List<ExperimenterData>) m.get(i.next());
				j = l.iterator();
				while (j.hasNext()) {
					exp = j.next();
					users.put(exp.getId(), exp);
				}
				//uiValue += value;
			}
			//setUserString(uiValue);
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null || m.size() != 1) return;
			Set set = m.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Class type;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				type = (Class) entry.getKey();
				handleTagsSelection(
						(Collection) entry.getValue());
			}
		}
	}
	
}
