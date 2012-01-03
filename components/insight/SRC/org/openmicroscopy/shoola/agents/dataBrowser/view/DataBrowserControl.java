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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.actions.CreateExperimentAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.FieldsViewAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ManageObjectAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ManageRndSettingsAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.RefreshAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.SaveAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.SendFeedbackAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.TaggingAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ViewAction;
import org.openmicroscopy.shoola.agents.dataBrowser.actions.ViewOtherAction;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.CellDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.RollOverNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog;
import org.openmicroscopy.shoola.agents.dataBrowser.util.QuickFiltering;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.RollOverThumbnailManager;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.util.ui.PlateGrid;
import org.openmicroscopy.shoola.util.ui.PlateGridObject;
import org.openmicroscopy.shoola.util.ui.search.QuickSearch;
import org.openmicroscopy.shoola.util.ui.search.SearchComponent;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import pojos.DataObject;
import pojos.DatasetData;

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
    static final Integer     VIEW = Integer.valueOf(0);
    
    /** Identifies the <code>Copy object action</code>. */
	static final Integer	COPY_OBJECT = Integer.valueOf(1);

	/** Identifies the <code>Paste object action</code>. */
	static final Integer	PASTE_OBJECT = Integer.valueOf(2);

	/** Identifies the <code>Remove object action</code>. */
	static final Integer	REMOVE_OBJECT = Integer.valueOf(3);
	
	/** Identifies the <code>Cut object action</code>. */
	static final Integer	CUT_OBJECT = Integer.valueOf(4);
	
	/** Identifies the <code>Paste rendering settings action</code>. */
	static final Integer    PASTE_RND_SETTINGS = Integer.valueOf(5);

	/** Identifies the <code>Copy rendering settings action</code>. */
	static final Integer    COPY_RND_SETTINGS = Integer.valueOf(6);
	
	/** Identifies the <code>Reset rendering settings action</code>. */
	static final Integer    RESET_RND_SETTINGS = Integer.valueOf(7);
	
	/** 
	 * Identifies the <code>Set the min/max for each channel</code>. 
	 */
	static final Integer    SET_MIN_MAX_SETTINGS = Integer.valueOf(8);
	
	/** Identifies the <code>Refresh</code> action. */
	static final Integer    REFRESH = Integer.valueOf(9);
	
	/** Identifies the <code>Save As</code> action. */
	static final Integer    SAVE_AS = Integer.valueOf(10);
	
	/** Identifies the <code>Tag</code> action. */
	static final Integer    TAG = Integer.valueOf(11);

	/** Identifies the <code>New Experiment</code> action. */
	static final Integer    NEW_EXPERIMENT = Integer.valueOf(12);
	
	/** Identifies the <code>Fields View</code> action. */
	static final Integer    FIELDS_VIEW = Integer.valueOf(13);
	
	/** Identifies the <code>Fields View</code> action. */
	static final Integer    OPEN_WITH = Integer.valueOf(14);
	
	/** 
	 * Identifies the <code>Set the original rendering settings action</code>. 
	 */
	static final Integer    SET_OWNER_RND_SETTINGS = Integer.valueOf(15);
	
	/** Identifies the <code>Send Feedback action</code>.  */
	static final Integer    SEND_FEEDBACK = Integer.valueOf(16);
	
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
    	actionsMap.put(REMOVE_OBJECT, new ManageObjectAction(model,
									ManageObjectAction.REMOVE));
    	actionsMap.put(CUT_OBJECT, new ManageObjectAction(model,
								ManageObjectAction.CUT));
    	actionsMap.put(PASTE_RND_SETTINGS, new ManageRndSettingsAction(model, 
    						ManageRndSettingsAction.PASTE));
    	actionsMap.put(COPY_RND_SETTINGS, new ManageRndSettingsAction(model, 
									ManageRndSettingsAction.COPY));
    	actionsMap.put(RESET_RND_SETTINGS, new ManageRndSettingsAction(model, 
								ManageRndSettingsAction.RESET));
    	actionsMap.put(REFRESH, new RefreshAction(model));
    	actionsMap.put(SET_MIN_MAX_SETTINGS, 
    			new ManageRndSettingsAction(model, 
    					ManageRndSettingsAction.SET_MIN_MAX));
    	actionsMap.put(SET_OWNER_RND_SETTINGS, 
    			new ManageRndSettingsAction(model, 
    					ManageRndSettingsAction.SET_OWNER));
    	actionsMap.put(SAVE_AS, new SaveAction(model));
    	actionsMap.put(TAG, new TaggingAction(model));
    	actionsMap.put(NEW_EXPERIMENT, new CreateExperimentAction(model));
    	actionsMap.put(FIELDS_VIEW, new FieldsViewAction(model));
    	actionsMap.put(OPEN_WITH, new ViewOtherAction(model, null));
    	actionsMap.put(SEND_FEEDBACK, new SendFeedbackAction(model));
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
					showAll();
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
	
	/** Shows all the nodes. */
	private void showAll()
	{ 
		view.setFilterLabel("");
		model.showAll(); 
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
	
	/**
	 * Views the selected well sample field while browsing a plate.
	 * 
	 * @param field The index of the field.
	 */
	void viewField(int field) { model.viewField(field); }
	
	/**
	 * Forwards to the model to create a report.
	 * 
	 * @param name The name of the file.
	 */
	void createReport(String name) { model.createReport(name); }
	
	/** Loads the existing datasets. */
	void loadExistingDatasets() { model.loadExistingDatasets(); }
	
	/**
	 * Returns the external application previously used to open 
	 * the selected document.
	 * 
	 * @return See above.
	 */
	List<ViewOtherAction> getApplicationActions()
	{
		List<ApplicationData> applications = view.getApplications();
		if (applications == null || applications.size() == 0) return null;
		Iterator<ApplicationData> i = applications.iterator();
		List<ViewOtherAction> actions = new ArrayList<ViewOtherAction>();
		while (i.hasNext()) {
			actions.add(new ViewOtherAction(model, i.next()));
		}
		return actions;
	}
	
	/**
	 * Views the passed node if supported.
	 * 
	 * @param node The node to handle.
	 */
	void viewDisplay(ImageDisplay node) { model.viewDisplay(node); }
	
	/**
	 * Loads data, filters nodes or sets the selected node.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Browser.SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY.equals(name)) {
			ImageDisplay node = (ImageDisplay) evt.getNewValue();
            if (node == null) return;
			model.setSelectedDisplay(node);
		} else if (Browser.SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY.equals(
				name)) {
			List<ImageDisplay> nodes = (List<ImageDisplay>) evt.getNewValue();
			model.setSelectedDisplays(nodes);
		} else if (Browser.UNSELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY.equals(
				name)) {
			ImageDisplay node = (ImageDisplay) evt.getNewValue();
            if (node == null) return;
			model.setUnselectedDisplay(node);
		} else if (QuickFiltering.FILTER_DATA_PROPERTY.equals(name)) {
			filterNodes((SearchObject) evt.getNewValue());
		} else if (QuickFiltering.DISPLAY_ALL_NODES_PROPERTY.equals(name)) {
			showAll();
		} else if (FilteringDialog.FILTER_PROPERTY.equals(name) ||
				QuickFiltering.FILTER_TAGS_PROPERTY.equals(name)) {
			model.filterByContext((FilterContext) evt.getNewValue());
		} else if (FilteringDialog.LOAD_TAG_PROPERTY.equals(name) ||
				QuickFiltering.TAG_LOADING_PROPERTY.equals(name)) {
			model.loadExistingTags();
		} else if (Browser.ROLL_OVER_PROPERTY.equals(name)) {
            if (view.isRollOver()) {
            	RollOverNode n = (RollOverNode) evt.getNewValue();
                if (n != null && n.getNode() != null) {
                	ImageNode node = n.getNode();
                	Thumbnail prv = node.getThumbnail();
                    BufferedImage full = prv.getFullScaleThumb();
                    if (prv.getScalingFactor() == Thumbnail.MAX_SCALING_FACTOR)
                    	full = prv.getZoomedFullScaleThumb();
                    RollOverThumbnailManager.rollOverDisplay(full, 
                    		node.getBounds(), n.getLocationOnScreen(), 
                    		node.toString());
                 } else RollOverThumbnailManager.stopOverDisplay();
           }
        } else if (SlideShowView.CLOSE_SLIDE_VIEW_PROPERTY.equals(name)) {
        	view.slideShowView(false);
        } else if (EditorDialog.CREATE_NO_PARENT_PROPERTY.equals(name) ||
        		EditorDialog.CREATE_PROPERTY.equals(name)) {
        	DataObject object = (DataObject) evt.getNewValue();
        	model.createDataObject(object);
        } else if (ImageTableView.TABLE_NODES_SELECTION_PROPERTY.equals(name)) {
        	List<ImageDisplay> selected = (List) evt.getNewValue();
        	model.setTableNodesSelected(selected);
        } else if (ImageTableView.TABLE_SELECTION_MENU_PROPERTY.equals(name)) {
        	Point location = (Point) evt.getNewValue();
        	if (location != null) view.showPopup(location);
        } else if (Browser.POPUP_POINT_PROPERTY.equals(name)) {
			Point p = (Point) evt.getNewValue();
            if (p != null) view.showPopup(p);
		} else if (ImageTableView.TABLE_SELECTION_VIEW_PROPERTY.equals(name)) {
			Boolean b = (Boolean) evt.getNewValue();
			if (b) view.viewSelectedNode();
		} else if (ImageTableView.TABLE_SELECTION_ROLL_OVER_PROPERTY.equals(
				name)) {
			RollOverNode node = (RollOverNode) evt.getNewValue();
			model.getBrowser().setRollOverNode(node);
		} else if (Browser.CELL_SELECTION_PROPERTY.equals(name)) {
			CellDisplay cell = (CellDisplay) evt.getNewValue();
			model.setSelectedCell(cell);
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
				if (DatasetData.class.equals(type)) {
					model.addToDatasets((Collection) entry.getValue());
				}
			}
		} else if (PlateGrid.WELL_FIELDS_PROPERTY.equals(name)) {
			PlateGridObject p = (PlateGridObject) evt.getNewValue();
			if (p == null) return;
			model.viewFieldsFor(p.getRow(), p.getColumn(), 
					p.isMultipleSelection());
		} else if (Browser.VIEW_DISPLAY_PROPERTY.equals(name)) {
			viewDisplay((ImageDisplay) evt.getNewValue());
		}
	}
	
}
