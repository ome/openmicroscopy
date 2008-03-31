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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.util.ui.search.QuickSearch;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

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
	
	/**
	 * Notifies the user if no terms to filter by.
	 * 
	 * @param values The values to handle.
	 */
	private void notifyInfo(List<String> values)
	{
		if (values == null || values.size() == 0) {
			//UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			//un.notifyInfo("Filter", "Please enter a term.");
			return;
		}
	}
	
	/** Filters the nodes. */
	private void filterNodes()
	{
		SearchObject filter = view.getSelectedFilter();
		if (filter == null) return;
		List<String> values = filter.getResult();
		switch (filter.getIndex()) {
			case QuickSearch.FULL_TEXT:
				notifyInfo(values);
				model.filterByFullText(values);
				break;
			case QuickSearch.TAGS:
				System.err.println(values);
				notifyInfo(values);
				model.filterByTags(values);
				break;
			case QuickSearch.COMMENTS:
				notifyInfo(values);
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
				break;	
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
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Browser.SELECTED_DISPLAY_PROPERTY.equals(name)) {
			ImageDisplay node = (ImageDisplay) evt.getNewValue();
            if (node == null) return;
			model.setSelectedDisplay(node);
		} else if (QuickSearch.QUICK_SEARCH_PROPERTY.equals(name)) {
			filterNodes();
		}
	}

}
