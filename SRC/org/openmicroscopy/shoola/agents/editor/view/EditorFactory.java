/*
 * org.openmicroscopy.shoola.agents.editor.view.EditorFactory 
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
package org.openmicroscopy.shoola.agents.editor.view;

//Java imports
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.actions.ActivationAction;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TaskBar;

import pojos.DataObject;
import pojos.FileAnnotationData;

/** 
 * Factory to create {@link Editor} component.
 * This class keeps track of the {@link Editor} instance that has been 
 * created and is not yet in the {@link Editor#DISCARDED} state.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class EditorFactory
	implements ChangeListener
{

	/** 
	 * String indicating that the editor is opened without reference to
	 * any specific file.
	 */
	public static final String BLANK_MODEL = "No File Open";
	
	/** The sole instance. */
	private static final EditorFactory  singleton = new EditorFactory();

	/**
	 * Returns the {@link Editor}.
	 * 
	 * @param fileAnnotation  The annotation hosting the information about
	 * 						  the file to edit.
	 * @return See above.
	 */
	public static Editor getEditor(FileAnnotationData fileAnnotation)
	{
		EditorModel model = new EditorModel(fileAnnotation);
		return singleton.getEditor(model);
	}
	
	/**
	 * Returns the {@link Editor}.
	 * 
	 * @param fileID    The id of the file to edit.
	 * @return See above.
	 */
	public static Editor getEditor(long fileID)
	{
		EditorModel model = new EditorModel(fileID);
		return singleton.getEditor(model);
	}
	
	/**
	 * Returns the {@link Editor} for the passed data object.
	 * 
	 * @param parent The data object that the file should be linked to.
	 * @param name	 The name of the editor file.
	 * @param type	 Either {@link Editor#PROTOCOL} or 
	 * 				 {@link Editor#EXPERIMENT}.
	 * @return See above.
	 */
	public static Editor getEditor(DataObject parent, String name, int type)
	{
		EditorModel model = new EditorModel(parent, name, type);
		Editor editor = singleton.getEditor(model);
		if (editor != null) {
			((EditorComponent) editor).setNewExperiment();
			editor.setStatus("", true);
		}
		return editor;
	}
	
	/**
	 * Returns the {@link Editor} created to display a particular file. 
	 * 
	 * 
	 * @param file 		The file to open in Editor. 
	 * @return See above.
	 */
	public static Editor getEditor(File file)
	{
		if (file == null) return getEditor();	// just in case. Never used! 
		
		EditorModel model = new EditorModel(file);
		
		// if a "blank" editor is open, with a "blank" model, this is returned
		// or, if the model matches the model in an existing editor, return this,
		// or, create a new editor with this new model.
		return singleton.getEditor(model);
	}

	/**
	 * If no editors exist, this returns a new {@link Editor}, 
	 * with an {@link EditorModel} that has no file.
	 * Otherwise, it simply returns the first editor in the {@link #editors} list. 
	 * 
	 * This provides the functionality for handling a "show editor", where 
	 * it doesn't matter which editor/file you show.
	 * 
	 * @return See above.
	 */
	public static Editor getEditor()
	{
		EditorModel model;
		if (singleton.editors.isEmpty())
			return getNewBlankEditor();
		Editor e = singleton.editors.iterator().next();
		if (e == null) return e;
		model = ((EditorComponent) e).getModel();
		return singleton.getEditor(model);
	}
	
	/**
	 * Sets a reference to data copied 'to - clipboard' in Editor. 
	 * Allows fields, text, parameters etc to be copied and pasted between
	 * Editor instances. 
	 * 
	 * @param copiedData		The data object. 
	 */
	public static void setCopiedData(Object copiedData)
	{
		singleton.copiedData = copiedData;
	}
	
	/**
	 * Gets a reference to data copied 'to - clipboard' in Editor. 
	 * Allows fields, text, parameters etc to be copied and pasted between
	 * Editor instances. 
	 * 
	 * @return Object		The data object. 
	 */
	public static Object getCopiedData() { return singleton.copiedData; }

	/**
	 * This has a similar functionality to <code>getEditor</code> method, 
	 * except this method always returns a new editor 
	 * (never an existing editor).
	 * 	
	 * @return	A new Editor, with a new EditorModel 
	 */
	public static Editor getNewBlankEditor()
	{
		EditorModel model = new EditorModel();
		 // this will return any existing editors with a 'blank' model, or
		// create an editor with the blank model above, if none exist. 
		Editor editor = singleton.getEditor(model);
		if (editor != null) editor.setStatus("", true);
		
		return editor;
	}
	
	/**
	 * Returns the number of Editors. 
	 * Used for incrementing the position on screen of new Editor windows. 
	 * 
	 * @return		see above. 
	 */
	public static int getEditorCount()
	{
		return singleton.editors.size();
	}
	
	/** 
	 * Adds all the {@link Editor} components that this factory is
	 * currently tracking to the passed menu.
	 * 
	 * @param menu The menu to add the components to. 
	 */
	static void register(JMenu menu)
	{ 
		//return singleton.viewers; 
		if (menu == null) return;
		Iterator i = singleton.editors.iterator();
		menu.removeAll();
		while (i.hasNext()) 
			menu.add(new JMenuItem(new ActivationAction((Editor) i.next())));
	}
	
	/** 
	 * Returns the <code>window</code> menu. 
	 * 
	 * @return See above.
	 */
	static JMenu getWindowMenu() { return singleton.windowMenu; }

	/** Attaches the {@link #windowMenu} to the <code>TaskBar</code>. */
	static void attachWindowMenuToTaskBar()
	{
		if (singleton.isAttached) return;
		TaskBar tb = EditorAgent.getRegistry().getTaskBar();
		tb.addToMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
		singleton.isAttached = true;
	}
	
	/** All the tracked components. */
	private Set<Editor>     	editors;
	
	/** The windows menu. */
	private JMenu   			windowMenu;

	/**  
	 * A reference to clip-board data, for copying between Editors
	 * e.g. A list of fields (nodes) or text with parameters. 
	 */
	private Object 				copiedData;
	
	/** 
	 * Indicates if the {@link #windowMenu} is attached to the 
	 * <code>TaskBar</code>.
	 */
	private boolean 			isAttached;
	
	/** Creates a new instance. */
	private EditorFactory()
	{
		editors = new HashSet<Editor>();
		windowMenu = new JMenu("Editors");
		isAttached = false;
	}
	
	/**
	 * Creates or recycles a viewer component for the specified 
	 * <code>model</code>.
	 * Returns the first existing editor where the model matches the 
	 * 
	 * @param model The component's Model.
	 * @return A {@link Editor} for the specified <code>model</code>.  
	 */
	private Editor getEditor(EditorModel model)
	{
		Iterator<Editor> v = editors.iterator();
		EditorComponent comp;
		EditorModel m;
		while (v.hasNext()) {
			comp = (EditorComponent) v.next();
			m = comp.getModel();
			// if the annotationID is not 0 and the IDs are the same, files are same
			// (need to compare annotationIDs because sometimes you are trying
			// to open a file using only the annotationID, so that's all you have) 
			if (m.getAnnotationId() == model.getAnnotationId()) {
				if (m.getFileID() > 0)	
					return comp;
				else if (m.getFileName().equals(model.getFileName()))
					return comp;
			}
			if (m.getFileName().equals(model.getFileName())) {
				return comp;
			}
			// if the model is "blank" (no file open) then open the current file
			if (BLANK_MODEL.equals(m.getFileName())) {
				m.setFileToEdit(model.getFileToEdit());
				return comp;
			}
		}
		comp = new EditorComponent(model);	// creates View and Controller
		comp.initialize();		// initialises MVC
		comp.addChangeListener(this);
		editors.add(comp);
		return comp;
	}

	/**
	 * Removes a viewer from the {@link #editors} set when it is
	 * {@link Editor#DISCARDED discarded}. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent ce)
	{
		EditorComponent comp = (EditorComponent) ce.getSource(); 
		if (comp.getState() == Editor.DISCARDED) editors.remove(comp);
		if (editors.size() == 0) {
			TaskBar tb = EditorAgent.getRegistry().getTaskBar();
			tb.removeFromMenu(TaskBar.WINDOW_MENU, windowMenu);
			isAttached = false;
			if (!EditorAgent.isServerAvailable()) {
				EventBus bus = EditorAgent.getRegistry().getEventBus();
		        bus.post(new ExitApplication(false));
			}
		}
	}
	
}
