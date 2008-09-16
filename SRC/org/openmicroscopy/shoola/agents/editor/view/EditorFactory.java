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
import org.openmicroscopy.shoola.env.ui.TaskBar;

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

	/** The sole instance. */
	private static final EditorFactory  singleton = new EditorFactory();

	/**
	 * Returns the {@link Editor}.
	 * 
	 * @param fileName  The name of the file to edit.
	 * @param fileID    The id of the file to edit.
	 * @param fileSize  The size of the file to edit.
	 * @return See above.
	 */
	public static Editor getEditor(String fileName, long fileID, long fileSize)
	{
		EditorModel model = new EditorModel(fileName, fileID, fileSize);
		return singleton.getEditor(model);
	}
	
	/**
	 * Returns the {@link Editor}.
	 * 
	 * @param file 		The file to open in Editor. 
	 * @return See above.
	 */
	public static Editor getEditor(File file)
	{
		EditorModel model = new EditorModel(file);
		return singleton.getEditor(model);
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
	 * 
	 * @param model The component's Model.
	 * @return A {@link Editor} for the specified <code>model</code>.  
	 */
	private Editor getEditor(EditorModel model)
	{
		Iterator v = editors.iterator();
		EditorComponent comp;
		while (v.hasNext()) {
			comp = (EditorComponent) v.next();
			if ((comp.getModel().getFileID() == model.getFileID()) && 
					(comp.getModel().getFileName().equals(model.getFileName())))
				return comp;
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
		}
	}
	
}
