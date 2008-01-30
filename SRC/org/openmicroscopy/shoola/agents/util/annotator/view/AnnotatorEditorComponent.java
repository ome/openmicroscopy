/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorComponent 
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.TextAnnotation;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;

/** 
 * Implements the {@link AnnotatorEditor} interface to provide the functionality
 * required of the annotator editor component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorModel
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorView
 * @see org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditorControl
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
class AnnotatorEditorComponent 
	extends AbstractComponent
	implements AnnotatorEditor
{

	/** The Model sub-component. */
    private AnnotatorEditorModel     model;
    
    /** The Controller sub-component. */
    private AnnotatorEditorControl   controller;
    
    /** The View sub-component. */
    private AnnotatorEditorView       view;
    
    /**
     * Returns the owner of the {@link AnnotatorEditorView}.
     * 
     * @return See above.
     */
	private Container getOwner()
	{
		Component source = view;
		Container parent = null;
		while (source != null) {
            parent = source.getParent();
            if (parent instanceof JFrame) return parent;
            else if (parent instanceof JDialog) return parent;
            else source = parent;
        }
		return null;
	}
	
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     * @param layout One of the constant defined by {@link AnnotatorEditor}.
     */
    AnnotatorEditorComponent(AnnotatorEditorModel model, int layout)
	{
		if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new AnnotatorEditorControl(this);
        view = new AnnotatorEditorView(layout);
	}
    
    /** Links up the MVC triad. */
    void initialize()
    {
    	controller.initialize(view);
    	view.initialize(model, controller);
    }
    
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#activate()
     */
	public void activate()
	{
		switch (model.getState()) {
	        case NEW:
	        case READY:
	        	view.onSelectedDisplay();
	        	model.fireAnnotationsRetrieval();
	        	fireStateChange();
	            break;
	        case DISCARDED:
	            throw new IllegalStateException(
	                    "This method can't be invoked in the DISCARDED state.");
		} 
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#discard()
     */
	public void discard()
	{
		if (model.getState() != DISCARDED) {
			model.discard();
			fireStateChange();
		}
	}

	/**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditorComponent#close()
     */
	public void close()
	{
	}
	
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#getState()
     */
	public int getState() { return model.getState(); }

	 /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#cancel()
     */
	public void cancel()
	{
		discard();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#setAnnotations(Map)
     */
	public void setAnnotations(Map annotations)
	{
		if (model.getState() != LOADING) {
			firePropertyChange(ANNOTATION_LOADED_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
			return;
		}
		model.setAnnotations(annotations);
		view.showAnnotations();
		firePropertyChange(ANNOTATION_LOADED_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#setAnnotationSaved(DataObject)
     */
	public void setAnnotationSaved(List result)
	{
		if (model.getState() != SAVING)
			throw new IllegalStateException("This method can only be invoked "+
					"in the SAVING state: "+model.getState());
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		//model.fireAnnotationsRetrieval();
		model.setState(READY);
    	fireStateChange();
    	Iterator i = result.iterator();
    	DataObject object;
    	DataObject refObject = model.getDataObject();
    	long id = refObject.getId();
    	while (i.hasNext()) {
			object = (DataObject) i.next();
			if (object.getId() == id 
				&& object.getClass().equals(refObject.getClass())) {
				firePropertyChange(ANNOTATED_PROPERTY, null, object);
				break;
			}
		}
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#delete(List)
     */ 
	public void delete(List l)
	{
		if (model.getState() != READY)
			throw new IllegalStateException("This method can only be invoked "+
			"in the READY state.");
		if (l == null) {
			l = view.getSelectedAnnotations();
			if (l == null || l.size() == 0) {
				UserNotifier un = 
					AnnotatorFactory.getRegistry().getUserNotifier();
				un.notifyInfo("Annotation", "No annotation to delete");
				return;
			}
		}
		model.fireAnnotationDelete(l);
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#getUI()
     */
	public JComponent getUI() { return view; }

    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#hasAnnotation()
     */
	public boolean hasAnnotation() 
	{
		if (model.getState() != READY) return false;
		return view.hasAnnotation();
	}
	
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#hasDataToSave()
     */
	public boolean hasDataToSave()
	{
		if (model.getState() != READY) return false;
		return view.hasDataToSave();
	}

	/**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#hasTextEntered()
     */
	public boolean hasTextEntered()
	{
		if (model.getState() != READY) return false;
		return view.hasTextEntered();
	}
	
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#retrieveAnnotations(DataObject)
     */
	public void retrieveAnnotations(DataObject object) 
	{
		switch (model.getState()) {
			case READY:
			case NEW:
				break;
			default:
				model.cancel();
		}
		model.setDataObject(object);
		view.onSelectedDisplay();
		//if (object == null)
		//	view.onSelectedDisplay(false);
		//else
		if (object != null)	model.fireAnnotationsRetrieval(object);
	}
	
    /**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#save()
     */
	public void save() 
	{
		if (model.getState() != READY) return;
			//throw new IllegalStateException("This method can only be invoked "+
			//"in the READY state.");
		Set siblings = model.getSiblings();
		if (siblings == null || siblings.size() == 1) {
			save(AnnotatorEditor.SELECT_ONE);
			return;
		}
		AnnotatorSavingDialog dialog = null;
		Container c = getOwner();
		if (c instanceof JFrame)
			dialog = new AnnotatorSavingDialog((JFrame) c, 
										AnnotatorSavingDialog.ANNOTATOR_EDITOR, 
										model.getDataObjectName());
		else if (c instanceof JDialog)
			dialog = new AnnotatorSavingDialog((JDialog) c, 
					AnnotatorSavingDialog.ANNOTATOR_EDITOR, 
					model.getDataObjectName());
		if (dialog != null) {
			dialog.addPropertyChangeListener(controller);
			UIUtilities.centerAndShow(dialog);
		}
	}
	
	/**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#save(int)
     */
	public void save(int index)
	{
		if (model.getState() != READY)
			throw new IllegalStateException("This method can only be invoked "+
			"in the READY state.");
		TextAnnotation data = new TextAnnotation();
		data.setText(view.getAnnotationText());
		model.fireAnnotationUpdate(data);
		fireStateChange();
	}

	/**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#save(String)
     */
	public void save(String text)
	{
		TextAnnotation newData = new TextAnnotation();
		newData.setText(text);
		model.fireAnnotationUpdate(newData);
		fireStateChange();
	}
	
	/**
     * Implemented as specified by the {@link Annotator} interface.
     * @see AnnotatorEditor#addSelectedNodes(List)
     */
	public void addSelectedNodes(List nodes)
	{
		if (nodes == null || nodes.size() == 0) return;
		Set<DataObject> objects = new HashSet<DataObject>();
		DataObject object = model.getDataObject();
		Iterator i = nodes.iterator();
		Class klass = object.getClass();
		DataObject n;
		while (i.hasNext()) {
			n = (DataObject) i.next();
			if (klass.equals(n.getClass()))
				objects.add(n);
		}
		model.setSiblings(objects);
	}
	
}
