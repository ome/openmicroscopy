/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorControl 
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
package org.openmicroscopy.shoola.agents.util.annotator.view;




//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.annotator.actions.AnnotatorEditorAction;
import org.openmicroscopy.shoola.agents.util.annotator.actions.DeleteAction;
import org.openmicroscopy.shoola.agents.util.annotator.actions.SaveAction;

import pojos.AnnotationData;

/** 
 * The {@link AnnotatorEditor}'s controller. 
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
class AnnotatorEditorControl 
	implements PropertyChangeListener
{

	/** Identifies the <code>Delete action</code> in the Edit menu. */
	static final Integer	DELETE = new Integer(0);
  
	/** Identifies the <code>Save action</code> in the Edit menu. */
	static final Integer	SAVE = new Integer(1);
  
	
	/** Maps actions ids onto actual <code>Action</code> object. */
	private Map<Integer, AnnotatorEditorAction>	actionsMap;
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap.put(DELETE, new DeleteAction(model));
		actionsMap.put(SAVE, new SaveAction(model));
	}
  
	/** 
	 * Attaches a window listener to the view to discard the model when 
	 * the user closes the window. 
	 */
	private void attachListeners()
	{
		//model.addChangeListener(this);
	}
	
	/** 
	 * Reference to the {@link Annotator} component, which, in this context,
	 * is regarded as the Model.
  	 */
	private AnnotatorEditor		model;
  
	/** Reference to the View. */
	private AnnotatorEditorView	view;
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize(AnnotatorView) initialize} method 
	 * should be called straight 
	 * after to link this Controller to the other MVC components.
	 * 
	 * @param model  Reference to the {@link AnnotatorEditor} component, which
	 * 				 in this context, is regarded as the Model.
	 *               Mustn't be <code>null</code>.
	 */
	AnnotatorEditorControl(AnnotatorEditor model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		actionsMap = new HashMap<Integer, AnnotatorEditorAction>();
	}
	
	/**
	 * Links this Controller to its View.
	 * 
	 * @param view   Reference to the View. Mustn't be <code>null</code>.
	 */
	void initialize(AnnotatorEditorView view)
	{
		if (view == null) throw new NullPointerException("No view.");
		this.view = view;
		createActions();
		attachListeners();
	}

	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	AnnotatorEditorAction getAction(Integer id) { return actionsMap.get(id); }
	
	/**
	 * Deletes the annotation.
	 * 
	 * @param data The annotation to delete.
	 */
	void deleteAnnotation(AnnotationData data)
	{
		List<AnnotationData> l = new ArrayList<AnnotationData>(1);
		l.add(data);
		model.delete(l);
	}
	
	/**
	 * Updates the annotation.
	 * 
	 * @param data The annotation to delete.
	 */
	void updateAnnotation(String data)
	{
		System.err.println("data: "+data);
		model.save(data);
	}
	
	/**
	 * Reacts to property changes fired by {@link AnnotatorSavingDialog}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (AnnotatorSavingDialog.ANNOTATE_ONE_PROPERTY.equals(name)) {
			model.save(Annotator.SELECT_ONE);
		} else if (AnnotatorSavingDialog.ANNOTATE_ALL_PROPERTY.equals(name)) {
			model.save(Annotator.SELECT_ALL);
		} 	
	}

}
