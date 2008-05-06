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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.util.FilteringDialog;
import org.openmicroscopy.shoola.agents.dataBrowser.util.ObjectEditor;
import org.openmicroscopy.shoola.agents.dataBrowser.util.QuickFiltering;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.util.ui.search.QuickSearch;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;

import pojos.DataObject;

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
class DataBrowserControl
	implements PropertyChangeListener
{

	/** 
	 * Reference to the {@link DataBrowser} component, which, in this context,
	 * is regarded as the Model.
	 */
	private DataBrowser 	model;
	
	/** Reference to the view. */
	private DataBrowserUI	view;
	
	/** Filters the nodes. */
	private void filterNodes()
	{
		SearchObject filter = view.getSelectedFilter();
		if (filter == null) return;
		List<String> values = filter.getResult();
		switch (filter.getIndex()) {
			case QuickSearch.FULL_TEXT:
				model.filterByFullText(values);
				break;
			case QuickSearch.UNTAGGED:
				model.filterByTags(null);
				break;
			case QuickSearch.TAGS:
				if (values != null && values.size() > 0)
					model.filterByTags(values);
				break;
			case QuickSearch.UNCOMMENTED:
				model.filterByComments(null);
				break;
			case QuickSearch.COMMENTS:
				if (values != null && values.size() > 0)
					model.filterByComments(values);
				break;
			case QuickSearch.RATED_ONE_OR_BETTER:
				model.filterByRate(DataBrowser.RATE_ONE);
				break;
			case QuickSearch.RATED_TWO_OR_BETTER:
				model.filterByRate(DataBrowser.RATE_TWO);
				break;
			case QuickSearch.RATED_THREE_OR_BETTER:
				model.filterByRate(DataBrowser.RATE_THREE);
				break;
			case QuickSearch.RATED_FOUR_OR_BETTER:
				model.filterByRate(DataBrowser.RATE_FOUR);
				break;
			case QuickSearch.RATED_FIVE:
				model.filterByRate(DataBrowser.RATE_FIVE);
				break;
			case QuickSearch.UNRATED:
				model.filterByRate(DataBrowser.UNRATED);
				break;	
			case QuickSearch.SHOW_ALL:
				model.showAll();
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
		} else if (QuickSearch.QUICK_SEARCH_PROPERTY.equals(name)) {
			filterNodes();
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
        }
	}

}
