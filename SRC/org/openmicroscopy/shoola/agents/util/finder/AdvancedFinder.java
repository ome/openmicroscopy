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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ui.UserManagerDialog;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchComponent;
import org.openmicroscopy.shoola.util.ui.search.SearchContext;
import org.openmicroscopy.shoola.util.ui.search.SearchHelp;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.TagAnnotationData;

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
	private List<FinderLoader>				finderHandlers;
	
	/** One of the constants defined by this class. */
	private int								state;
	
	/** Collection of selected users. */
	private Map<Long, ExperimenterData>		users;
    
	/** The collection of tags. */
	private Collection 						tags;
	
	/**
	 * Determines the scope of the search.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	private Integer convertScope(int value)
	{
		switch (value) {
			case SearchContext.TEXT_ANNOTATION:
				return SearchDataContext.TEXT_ANNOTATION;
			case SearchContext.TAGS:
				return SearchDataContext.TAGS;
			case SearchContext.URL_ANNOTATION:
				return SearchDataContext.URL_ANNOTATION;
			case SearchContext.FILE_ANNOTATION:
				return SearchDataContext.FILE_ANNOTATION;
			case SearchContext.NAME:
				return SearchDataContext.NAME;
			case SearchContext.DESCRIPTION:
				return SearchDataContext.DESCRIPTION;
			case SearchContext.CUSTOMIZED:
				return SearchDataContext.CUSTOMIZED;
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
	private Class convertType(int value)
	{
		switch (value) {
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
	 * Returns the description associated to the passed value.
	 * 
	 * @param value The value to handle.
	 * @return See above
	 */
	private String getScope(int value)
	{
		switch (value) {
			case SearchDataContext.NAME:
				return NAME_TEXT;
			case SearchDataContext.DESCRIPTION:
				return NAME_DESCRIPTION;
			case SearchDataContext.TEXT_ANNOTATION:
				return NAME_COMMENTS;
			case SearchDataContext.TAGS:
				return NAME_TAGS;
			case SearchDataContext.URL_ANNOTATION:
				return NAME_URL;
			case SearchDataContext.FILE_ANNOTATION:
				return NAME_ATTACHMENT;
			case SearchDataContext.TIME:
				return NAME_TIME;
			case SearchDataContext.CUSTOMIZED:
				return NAME_CUSTOMIZED;
			default:
				return null;
		}
	}
	
	/**
	 * Creates and returns the list of users corresponding to the collection
	 * of names.
	 * 
	 * @param names Collection of names to handle.
	 * @return See above.
	 */
	private List<ExperimenterData> fillUsersList(List<Long> names)
	{
		List<ExperimenterData> l = new ArrayList<ExperimenterData>();
		if (names == null) return l;
		Iterator i = names.iterator();
		Long id;
		ExperimenterData user;
		while (i.hasNext()) {
			id = (Long) i.next();
			user = users.get(id);
			if (user != null && !l.contains(user))
				l.add(user);
		}
		return l;
	}
	
	/**
	 * Fills the passed lists depending on the specified context.
	 * 
	 * @param usersContext	The context.
	 * @param toKeep		The users to consider.
	 * @param toExclude		The users to exclude.
	 */
	private void fillUsersList(List<Integer> usersContext, 
			List<ExperimenterData> toKeep, List<ExperimenterData> toExclude)
	{
		if (usersContext == null) {
			toKeep.clear();
			toExclude.clear();
			return;
		}
		switch (usersContext.size()) {
			case 2:
				if (toKeep.size() >= 0)
					toKeep.add(getUserDetails());
				else {
					toKeep.clear();
					toExclude.clear();
				}
				break;
			case 1:
				if (usersContext.contains(SearchContext.CURRENT_USER)) {
					toKeep.clear();
					toExclude.clear();
					toKeep.add(getUserDetails());
				} else {
					if (toKeep.size() == 0)
					toExclude.add(getUserDetails());
				}
		}
	}
	
	/**
	 * Converts the UI context into a context to search for.
	 * 
	 * @param ctx The value to convert.
	 */
	private void handleSearchContext(SearchContext ctx)
	{
		String[] some = ctx.getSome();
		String[] must = ctx.getMust();
		String[] none = ctx.getNone();
		UserNotifier un = FinderFactory.getRegistry().getUserNotifier();
		Timestamp start = ctx.getStartTime();
		Timestamp end = ctx.getEndTime();
		if (start != null && end != null && start.after(end)) {
			un.notifyInfo(TITLE, "The selected time interval is not valid.");
			return;
		}
		
		if (some == null && must == null && none == null) {
			if (start == null && end == null) {
				un.notifyInfo(TITLE, "Please enter a term to search for " +
						"or a valid time interval.");
				return;
			}
		}
		List<Integer> context = ctx.getContext();
		if (context == null || context.size() == 0) {
			//un.notifyInfo(TITLE, "Please enter a context.");
			//return;
			context = new ArrayList<Integer>();
			context.add(SearchContext.CUSTOMIZED);
		}
		List<Integer> scope = new ArrayList<Integer>(context.size());
		Iterator i = context.iterator();
		Integer v;
		while (i.hasNext()) {
			v = convertScope((Integer) i.next());
			if (v != null) scope.add(v);
		}
		List<Class> types = new ArrayList<Class>();
		i = ctx.getType().iterator();
		Class k;
		while (i.hasNext()) {
			k = convertType((Integer) i.next());
			if (k != null) types.add(k);
		}
		
		List<ExperimenterData> owners = fillUsersList(ctx.getSelectedOwners());
		List<ExperimenterData> annotators = fillUsersList(null);
		//fillUsersList(ctx.getSelectedAnnotators());
		List<ExperimenterData> excludedOwners = fillUsersList(null);
		//fillUsersList(ctx.getExcludedOwners());
		List<ExperimenterData> excludedAnnotators = fillUsersList(null);
		
		fillUsersList(ctx.getOwnerSearchContext(), owners, excludedOwners);
		fillUsersList(ctx.getAnnotatorSearchContext(), annotators, 
						excludedAnnotators);
		
		SearchDataContext searchContext = new SearchDataContext(scope, types, 
										some, must, none);
		searchContext.setTimeInterval(start, end);
		searchContext.setOwners(owners);
		searchContext.setAnnotators(annotators);
		searchContext.setExcludedOwners(excludedOwners);
		searchContext.setExcludedAnnotators(excludedAnnotators);
		searchContext.setCaseSensitive(ctx.isCaseSensitive());
		searchContext.setNumberOfResults(ctx.getNumberOfResults());
		AdvancedFinderLoader loader = new AdvancedFinderLoader(this, 
													searchContext);
		loader.load();
		finderHandlers.add(loader);
		state = Finder.SEARCH;
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
	private Set getAvailableGroups()
	{
		return (Set) FinderFactory.getRegistry().lookup(
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
	 * Creates a list of controls to add to the searching component.
	 * 
	 * @return See above.
	 */
	private List<JButton> createControls()
	{
		List<JButton> list = new ArrayList<JButton>();
		IconManager icons = IconManager.getInstance();
		JButton button = new JButton(icons.getIcon(IconManager.TAG));
		UIUtilities.unifiedButtonLookAndFeel(button);
		button.setToolTipText("Load existing Tags to search by.");
		button.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				loadTags();
			}
		});
		list.add(button);
		return list;
	}
	
	/** Loads the tags. */
	private void loadTags()
	{
		if (tags == null) {
			TagsLoader loader = new TagsLoader(this);
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
			setSomeValues(toAdd);
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
		setSomeValues(toAdd);
	}
	
	/** Creates a new instance. */
	AdvancedFinder()
	{
		initialize(createControls());
		finderHandlers = new ArrayList<FinderLoader>();
		addPropertyChangeListener(SEARCH_PROPERTY, this);
		addPropertyChangeListener(CANCEL_SEARCH_PROPERTY, this);
		addPropertyChangeListener(OWNER_PROPERTY, this);
		users = new HashMap<Long, ExperimenterData>();
	}

	/**
	 * Brings up the <code>Help</code> dialog.
	 * @see #help()
	 */
	protected void help()
	{
		SearchHelp help = new SearchHelp(FinderFactory.getRefFrame());
		UIUtilities.centerAndShow(help);
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
	 * Implemented as specified by {@link Finder} I/F
	 * @see Finder#setResult(Object)
	 */
	public void setResult(Object result)
	{
		setSearchEnabled(false);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		Map map = (Map) result;

		//Format UI component
		Set nodes = new HashSet();
		if (map != null) {
			Set set = map.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Set<Long> ids = new HashSet<Long>();
			Collection r;
			Integer key;
			String term;
			Iterator j;
			DataObject data;
			JLabel l;
			Object value;
			int v;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				key = (Integer) entry.getKey();
				term = getScope(key);
				if (term != null) {
					value = entry.getValue();
					if (value instanceof Integer) {
						v = (Integer) value;
						if (v < 0)
							l = UIUtilities.setTextFont(term+": Not supported."
									+" Refine criteria");
						else 
							l = UIUtilities.setTextFont(term+": " +
									"Too many results.");
					} else {
						r = (Collection) value;
						j = r.iterator();
						while (j.hasNext()) {
							data = (DataObject) j.next();
							if (!ids.contains(data.getId())) {
								nodes.add(data);
								ids.add(data.getId());
							}
						}
						l = UIUtilities.setTextFont(term+": "+r.size());
					}
					
					p.add(l);
				}
			}
			
			displayResult(UIUtilities.buildComponentPanel(p));
		}
		
		firePropertyChange(RESULTS_FOUND_PROPERTY, null, nodes);
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
		List<String> l = getSome();
		Collection available = new ArrayList<TagAnnotationData>();
		
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (l.contains(tag.getTagValue()))
				selected.add(tag);
			else available.add(tag);
		}
		long id = DataBrowserAgent.getUserDetails().getId();
		SelectionWizard wizard = new SelectionWizard(
				DataBrowserAgent.getRegistry().getTaskBar().getFrame(), 
				available, selected, TagAnnotationData.class, false, id);
		wizard.setTitle(title, text, icons.getIcon(IconManager.TAG_48));
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
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
				users.put(exp.getId(), exp);
				setUserString(exp.getId(), EditorUtil.formatExperimenter(exp));
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
