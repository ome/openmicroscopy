/*
 * org.openmicroscopy.shoola.agents.util.tagging.view.TaggerComponent 
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
import java.util.List;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/** 
 * Implements the {@link Tagger} interface to provide the functionality
 * required of the tagger component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
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
class TaggerComponent 
	extends AbstractComponent
	implements Tagger
{

	/** Reference to the view. */
	private TaggerView 		view;
	
	/** Reference to the control. */
	private TaggerControl 	controller;
	
	/** Reference to the model. */
	private TaggerModel		model;
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component.
	 */
	TaggerComponent(TaggerModel model)
	{
		if (model == null) throw new NullPointerException("No model."); 
		this.model = model;
		controller = new TaggerControl(this);
		view = new TaggerView(TaggerFactory.getRefFrame());
	}
	
	/** Links up the MVC triad.  */
	void initialize()
	{
		controller.initialize(view);
		view.initialize(model, controller);
	}
	
	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#activate()
     */
	public void activate()
	{
		switch (model.getState()) {
			case NEW:
				model.fireTagsRetrieval();
				fireStateChange();
				break;
	
			default:
				break;
		}
	}

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#getUI()
     */
	public JDialog getUI() { return view; }

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#discard()
     */
	public void discard()
	{

	}

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#cancel()
     */
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#getState()
     */
	public int getState() { return model.getState(); }

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#close()
     */
	public void close()
	{
		discard();
		if (view != null) {
			view.setVisible(false);
			view.dispose();
		}
	}

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#setTags(List, List, List)
     */
	public void setTags(List tags, List availableTags, List tagSets)
	{
		switch (model.getState()) {
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED state.");
		}
		model.setTags(tags, availableTags, tagSets);
		view.setProgress(true);
		fireStateChange();
		firePropertyChange(TAG_LOADED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#setImageTagged()
     */
	public void setImageTagged()
	{
		firePropertyChange(TAGGED_PROPERTY, Boolean.FALSE, Boolean.TRUE);
		close();
	}
	
	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#finish()
     */
	public void finish()
	{
		switch (model.getState()) {
			case LOADING:
			case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED or " +
					"LOADING state.");
		}
		model.fireTagsSaving(view.saveTags());
		fireStateChange();
		//close();
	}

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#showTags()
     */
	public void showTags()
	{
		switch (model.getState()) {
			case LOADING:
			case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED or " +
					"LOADING state.");
		}
		view.showTags();
	}

	/**
     * Implemented as specified by the {@link Tagger} interface.
     * @see Tagger#getTags()
     */
	public List getTags()
	{
		// TODO Auto-generated method stub
		return model.getTags();
	}
	
}
