/*
 * org.openmicroscopy.shoola.agents.util.tagging.view.TaggerControl 
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
package org.openmicroscopy.shoola.agents.util.tagging.view;


//Java imports
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.util.tagging.actions.CloseAction;
import org.openmicroscopy.shoola.agents.util.tagging.actions.FinishAction;
import org.openmicroscopy.shoola.agents.util.tagging.actions.TagPopupAction;
import org.openmicroscopy.shoola.agents.util.tagging.actions.TaggerAction;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagItem;
import org.openmicroscopy.shoola.agents.util.tagging.util.TaggedMenuPane;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;

/** 
 * The {@link Tagger}'s controller. 
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
class TaggerControl
	implements PropertyChangeListener
{
	
	/** Identifies the <code>Close</code> action. */
	static final Integer     CLOSE = new Integer(0);

	/** Identifies the <code>Finish</code> action. */
	static final Integer     FINISH = new Integer(1);     

	/** Identifies the <code>POP_UP</code> action. */
	static final Integer     POP_UP = new Integer(2);
	
	/** Maps actions ids onto actual <code>Action</code> object. */
	private Map<Integer, TaggerAction>	actionsMap;
	
	/** 
	 * Reference to the {@link Tagger} component, which, in this context,
	 * is regarded as the Model.
	 */
	private Tagger						model;
	
	/** Reference to the View. */
	private TaggerView					view;
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap.put(CLOSE, new CloseAction(model));
		actionsMap.put(FINISH, new FinishAction(model));
		actionsMap.put(POP_UP, new TagPopupAction(model));
	}
	
	/** 
	 * Attaches a window listener to the view to discard the model when 
	 * the user closes the window.
	 */
	private void attachListeners()
	{
		view.addWindowListener(new WindowAdapter()
        {
        	public void windowOpened(WindowEvent e) {
        		view.requestFocusOnField();
        	} 
        });
	}
	
	private void removeTag(long tagID)
	{
		
	}
	
	/**
	 * Indicates to the browse the specified tag.
	 * 
	 * @param tag The tag to browse.
	 */
	void browseTag(CategoryData tag)
	{
		if (tag == null) return;
    	EventBus bus = TaggerFactory.getRegistry().getEventBus();
    	bus.post(new Browse(tag.getId(), Browse.CATEGORY, 
    			view.getExperimenter(), null));  
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize(TaggerView) initialize} method 
	 * should be called straight 
	 * after to link this Controller to the other MVC components.
	 * 
	 * @param model  Reference to the {@link Tagger} component, which, in 
	 *               this context, is regarded as the Model.
	 *               Mustn't be <code>null</code>.
	 */
	TaggerControl(Tagger model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
	}
	
	/**
	 * Links this Controller to its View.
	 * 
	 * @param view   Reference to the View. Mustn't be <code>null</code>.
	 */
	void initialize(TaggerView view)
	{
		if (view == null) throw new NullPointerException("No view.");
		this.view = view;
		actionsMap = new HashMap<Integer, TaggerAction>();
		createActions();
		attachListeners();
	}
	
	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	TaggerAction getAction(Integer id) { return actionsMap.get(id); }
	
	/** 
	 * Handles the selection of a new item in the list.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (TaggedMenuPane.REMOVE_PROPERTY.equals(name)) {
			CategoryData item = (CategoryData) evt.getNewValue();
			removeTag(item.getId());
		} else if (TaggedMenuPane.BROWSE_PROPERTY.equals(name)) {
			CategoryData item = (CategoryData) evt.getNewValue();
			browseTag(item);
		} else if (HistoryDialog.SELECTION_PROPERTY.equals(name)) {
			Object item = evt.getNewValue();
			if (!(item instanceof TagItem)) return;
			DataObject ho = ((TagItem) item).getDataObject();
			if (ho instanceof CategoryData) 
				view.handleTagSelection((CategoryData) ho);
			else if (ho instanceof CategoryGroupData)
				view.handleTagSetSelection((CategoryGroupData) ho);
		}
	}
	
}
