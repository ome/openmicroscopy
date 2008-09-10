/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserControl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;



//Java imports
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ManageObjectAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ManageRndSettingsAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.RefreshAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ViewAction;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog;
import org.openmicroscopy.shoola.agents.dataBrowser.util.ObjectEditor;
import org.openmicroscopy.shoola.agents.dataBrowser.util.QuickFiltering;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.util.ui.search.QuickSearch;
import org.openmicroscopy.shoola.util.ui.search.SearchComponent;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import pojos.DataObject;

/** 
 * The DataBrowser's Controller.
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
class DataBrowserControl
	implements PropertyChangeListener
{

	/** Identifies the <code>View action</code>. */
    static final Integer     VIEW = new Integer(0);
    
    /** Identifies the <code>Copy object action</code>. */
	static final Integer	COPY_OBJECT = new Integer(1);

	/** Identifies the <code>Paste object action</code>. */
	static final Integer	PASTE_OBJECT = new Integer(2);

	/** Identifies the <code>Remove object action</code>. */
	static final Integer	REMOVE_OBJECT = new Integer(3);
	
	/** Identifies the <code>Cut object action</code>. */
	static final Integer	CUT_OBJECT = new Integer(4);
	
	/** Identifies the <code>Paste rendering settings action</code>. */
	static final Integer    PASTE_RND_SETTINGS = new Integer(5);

	/** Identifies the <code>Copy rendering settings action</code>. */
	static final Integer    COPY_RND_SETTINGS = new Integer(6);
	
	/** Identifies the <code>Reset rendering settings action</code>. */
	static final Integer    RESET_RND_SETTINGS = new Integer(7);
	
	/** 
	 * Identifies the <code>Set the original rendering settings action</code>. 
	 */
	static final Integer    SET_ORIGINAL_RND_SETTINGS = new Integer(8);
	
	/** Identifies the <code>Refresh action</code>. */
	static final Integer    REFRESH = new Integer(9);

	/** 
	 * Reference to the {@link DataBrowser} component, which, in this context,
	 * is regarded as the Model.
	 */
	private DataBrowser 			model;
	
	/** Reference to the view. */
	private DataBrowserUI			view;
	
	 /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, Action>	actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
    	actionsMap.put(VIEW, new ViewAction(model));
    	actionsMap.put(COPY_OBJECT, new ManageObjectAction(model,
    								ManageObjectAction.COPY));
    	actionsMap.put(PASTE_OBJECT, new ManageObjectAction(model,
									ManageObjectAction.PASTE));
    	actionsMap.put(REMOVE_OBJECT,new ManageObjectAction(model,
									ManageObjectAction.REMOVE));
    	actionsMap.put(CUT_OBJECT,new ManageObjectAction(model,
								ManageObjectAction.CUT));
    	actionsMap.put(PASTE_RND_SETTINGS, new ManageRndSettingsAction(model, 
    						ManageRndSettingsAction.PASTE));
    	actionsMap.put(COPY_RND_SETTINGS, new ManageRndSettingsAction(model, 
									ManageRndSettingsAction.COPY));
    	actionsMap.put(RESET_RND_SETTINGS, new ManageRndSettingsAction(model, 
								ManageRndSettingsAction.RESET));
    	actionsMap.put(REFRESH, new RefreshAction(model));
    	actionsMap.put(SET_ORIGINAL_RND_SETTINGS, 
    			new ManageRndSettingsAction(model, 
    					ManageRndSettingsAction.SET_ORIGINAL));
    }
    
	/** 
	 * Filters the nodes. 
	 * 
	 * @param filter The selected filter.
	 */
	private void filterNodes(SearchObject filter)
	{
		if (filter == null) return;
		List<String> values = filter.getResult();
		switch (filter.getIndex()) {
			case QuickSearch.FULL_TEXT:
				view.setFilterLabel(SearchComponent.NAME_TEXT);
				if (values != null && values.size() > 0)
					model.filterByFullText(values);
				else {
					view.setFilterLabel("");
					model.showAll();
				}
				break;
			case QuickSearch.TAGS:
				view.setFilterLabel(SearchComponent.NAME_TAGS);
				if (values != null && values.size() > 0)
					model.filterByTags(values);
				else {
					view.setFilterLabel("");
					model.showAll();
				}
				break;
			case QuickSearch.COMMENTS:
				view.setFilterLabel(SearchComponent.NAME_COMMENTS);
				if (values != null && values.size() > 0)
					model.filterByComments(values);
				else {
					view.setFilterLabel("");
					model.showAll();
				}
				break;
			case QuickSearch.RATED_ONE_OR_BETTER:
				view.setFilterLabel(SearchComponent.NAME_RATE);
				model.filterByRate(DataBrowser.RATE_ONE);
				break;
			case QuickSearch.RATED_TWO_OR_BETTER:
				view.setFilterLabel(SearchComponent.NAME_RATE);
				model.filterByRate(DataBrowser.RATE_TWO);
				break;
			case QuickSearch.RATED_THREE_OR_BETTER:
				view.setFilterLabel(SearchComponent.NAME_RATE);
				model.filterByRate(DataBrowser.RATE_THREE);
				break;
			case QuickSearch.RATED_FOUR_OR_BETTER:
				view.setFilterLabel(SearchComponent.NAME_RATE);
				model.filterByRate(DataBrowser.RATE_FOUR);
				break;
			case QuickSearch.RATED_FIVE:
				view.setFilterLabel(SearchComponent.NAME_RATE);
				model.filterByRate(DataBrowser.RATE_FIVE);
				break;
			case QuickSearch.UNRATED:
				view.setFilterLabel(SearchComponent.UNRATED);
				model.filterByRate(DataBrowser.UNRATED);
				break;	
			case QuickSearch.SHOW_ALL:
				view.setFilterLabel("");
				model.showAll();
				break;
			case QuickSearch.TAGGED:
				view.setFilterLabel(SearchComponent.TAGGED_TEXT);
				model.filterByTagged(true);
				break;
			case QuickSearch.UNTAGGED:
				view.setFilterLabel(SearchComponent.UNTAGGED_TEXT);
				model.filterByTagged(false);
				break;
			case QuickSearch.COMMENTED:
				view.setFilterLabel(SearchComponent.COMMENTED_TEXT);
				model.filterByCommented(true);
				break;
			case QuickSearch.UNCOMMENTED:
				view.setFilterLabel(SearchComponent.UNCOMMENTED_TEXT);
				model.filterByCommented(false);
		}
	}
	
	/** Creates a new instance. */
	DataBrowserControl() {}
	
	/**
	 * Links the components composing the MVC triad.
	 * 
	 * @param model	Reference to the model. Mustn't be <code>null</code>.
	 * @param view	Reference to the view. Mustn't be <code>null</code>.
	 */
	void initialize(DataBrowser model, DataBrowserUI view)
	{
		if (view == null)
			throw new IllegalArgumentException("No control.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		
		this.model = model;
		this.view = view;
		actionsMap = new HashMap<Integer, Action>();
		createActions();
	}
	
	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	Action getAction(Integer id) { return actionsMap.get(id); }
	
	void viewField(int selectedIndex)
	{
		model.viewField(selectedIndex);
		
	}
	
	/**
	 * Loads data, filters nodes or sets the selected node.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Browser.SELECTED_DISPLAY_PROPERTY.equals(name)) {
			ImageDisplay node = (ImageDisplay) evt.getNewValue();
            if (node == null) return;
			model.setSelectedDisplay(node);
		} else if (Browser.UNSELECTED_DISPLAY_PROPERTY.equals(name)) {
			ImageDisplay node = (ImageDisplay) evt.getNewValue();
            if (node == null) return;
			model.setUnselectedDisplay(node);
		} else if (QuickFiltering.FILTER_DATA_PROPERTY.equals(name)) {
			filterNodes((SearchObject) evt.getNewValue());
		} else if (FilteringDialog.FILTER_PROPERTY.equals(name)) {
			model.filterByContext((FilterContext) evt.getNewValue());
		} else if (FilteringDialog.LOAD_TAG_PROPERTY.equals(name) ||
				QuickFiltering.TAG_LOADING_PROPERTY.equals(name)) {
			model.loadExistingTags();
		} else if (Browser.ROLL_OVER_PROPERTY.equals(name)) {
            if (view.isRollOver()) {
                ImageDisplay n = (ImageDisplay) evt.getNewValue();
                if (n instanceof ImageNode)
                    ThumbnailWindowManager.rollOverDisplay((ImageNode) n);
                else ThumbnailWindowManager.rollOverDisplay(null);
           }
        } else if (SlideShowView.CLOSE_SLIDE_VIEW_PROPERTY.equals(name)) {
        	view.slideShowView(false, false);
        } else if (ObjectEditor.CREATE_DATAOBJECT_PROPERTY.equals(name)) {
        	List l = (List) evt.getNewValue();
        	if (l != null && l.size() == 2) {
        		boolean visible = (Boolean) l.get(0);
            	DataObject object = (DataObject) l.get(1);
            	model.createDataObject(object, visible);
        	}
        } else if (ImageTableView.TABLE_NODES_SELECTION_PROPERTY.equals(name)) {
        	List<ImageDisplay> selected = (List) evt.getNewValue();
        	model.setTableNodesSelected(selected);
        } else if (ImageTableView.TABLE_SELECTION_MENU_PROPERTY.equals(name)) {
        	Point location = (Point) evt.getNewValue();
        	if (location != null) view.showPopup(location);
        } else if (Browser.POPUP_POINT_PROPERTY.equals(name)) {
			Point p = (Point) evt.getNewValue();
            if (p != null) view.showPopup(p);
		} 
	}


	
}
