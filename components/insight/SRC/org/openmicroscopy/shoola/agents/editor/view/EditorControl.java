/*
 * org.openmicroscopy.shoola.agents.editor.view.EditorControl 
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JMenu;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.actions.CloseEditorAction;
import org.openmicroscopy.shoola.agents.editor.actions.EditorAction;
import org.openmicroscopy.shoola.agents.editor.actions.NewBlankFileAction;
import org.openmicroscopy.shoola.agents.editor.actions.OpenLocalFileAction;
import org.openmicroscopy.shoola.agents.editor.actions.OpenWwwFileAction;
import org.openmicroscopy.shoola.agents.editor.actions.SaveAsProtocolAction;
import org.openmicroscopy.shoola.agents.editor.actions.SaveFileLocallyAction;
import org.openmicroscopy.shoola.agents.editor.actions.SaveFileAction;
import org.openmicroscopy.shoola.agents.editor.actions.SaveFileServerAction;

/** 
 * The {@link Editor}'s controller. 
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
class EditorControl
	implements ChangeListener
{

	/** Identifies the <code>Close Editor</code> Action. */
	static final Integer	CLOSE_EDITOR = Integer.valueOf(1);
	
	/** Identifies the <code>Open Local File</code> Action. */
	static final Integer	OPEN_LOCAL_FILE = Integer.valueOf(2);
	
	/** Identifies the <code>Save File As</code> Action. */
	static final Integer	SAVE_FILE_LOCALLY = Integer.valueOf(3);
	
	/** Identifies the <code>New Blank File</code> Action. */
	static final Integer	NEW_BLANK_FILE = Integer.valueOf(4);
	
	/** Identifies the <code>Save UPE File</code> Action. */
	static final Integer	SAVE_FILE = Integer.valueOf(5);
	
	/** Identifies the <code>Open file from web</code> Action. */
	static final Integer	OPEN_WWW_FILE = Integer.valueOf(6);
	
	/** Identifies the <code>SaveFileServer</code> Action. */
	static final Integer	SAVE_FILE_SERVER = Integer.valueOf(7);
	
	/** Identifies the <code>SaveFileAsProtocol</code> Action. */
	static final Integer	SAVE_AS_PROTOCOL = Integer.valueOf(8);
	
	/** 
	 * Reference to the {@link Editor} component, which, in this context,
	 * is regarded as the Model.
	 */
	private Editor			model;

	/** Reference to the View. */
	private EditorUI		view;
	
	/** Maps actions ids onto actual <code>Action</code> object. */
	private Map<Integer, EditorAction>	actionsMap;
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap.put(CLOSE_EDITOR, new CloseEditorAction(model));
		actionsMap.put(OPEN_LOCAL_FILE, new OpenLocalFileAction(model));
		actionsMap.put(SAVE_FILE_LOCALLY, new SaveFileLocallyAction(model));
		actionsMap.put(NEW_BLANK_FILE, new NewBlankFileAction(model));
		actionsMap.put(SAVE_FILE, new SaveFileAction(model));
		actionsMap.put(OPEN_WWW_FILE, new OpenWwwFileAction(model));
		actionsMap.put(SAVE_FILE_SERVER, new SaveFileServerAction(model));
		actionsMap.put(SAVE_AS_PROTOCOL, new SaveAsProtocolAction(model));
	}
	
	/** 
	 * Creates the windowsMenuItems. 
	 * 
	 * @param menu The menu to handle.
	 */
	private void createWindowsMenuItems(JMenu menu)
	{
		EditorFactory.register(menu);
	}
	
	/** Adds the listeners. */
	private void attachListeners()
	{
		model.addChangeListener(this);
		JMenu menu = EditorFactory.getWindowMenu();
		menu.addMenuListener(new MenuListener() {

			public void menuSelected(MenuEvent e)
			{ 
				Object source = e.getSource();
				if (source instanceof JMenu)
					createWindowsMenuItems((JMenu) source);
			}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-op implementation.
			 * @see MenuListener#menuCanceled(MenuEvent)
			 */ 
			public void menuCanceled(MenuEvent e) {}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-op implementation.
			 * @see MenuListener#menuDeselected(MenuEvent)
			 */ 
			public void menuDeselected(MenuEvent e) {}

		});
		
//		Listen to keyboard selection
		menu.addMenuKeyListener(new MenuKeyListener() {

			public void menuKeyReleased(MenuKeyEvent e)
			{
				Object source = e.getSource();
				if (source instanceof JMenu)
					createWindowsMenuItems((JMenu) source);
			}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no op implementation.
			 * @see MenuKeyListener#menuKeyPressed(MenuKeyEvent)
			 */
			public void menuKeyPressed(MenuKeyEvent e) {}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no op implementation.
			 * @see MenuKeyListener#menuKeyTyped(MenuKeyEvent)
			 */
			public void menuKeyTyped(MenuKeyEvent e) {}

		});
		
		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { model.discard(); }
		});
	}
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize(EditorUI) initialize} method should be called 
	 * straight after to link this Controller to the other MVC components.
	 * 
	 * @param model  Reference to the {@link Editor} component, which, in 
	 *               this context, is regarded as the Model.
	 *               Mustn't be <code>null</code>.
	 */
	EditorControl(Editor model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		actionsMap = new HashMap<Integer, EditorAction>();
	}

	/**
	 * Links this Controller to its View.
	 * 
	 * @param view   Reference to the View. Mustn't be <code>null</code>.
	 */
	void initialize(EditorUI view)
	{
		if (view == null) throw new NullPointerException("No view.");
		this.view = view;
		attachListeners();
		createActions();
		EditorFactory.attachWindowMenuToTaskBar();
	}
	
	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	EditorAction getAction(Integer id) { return actionsMap.get(id); }
	
	/**
	 * Reacts to state changes in the {@link Editor}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		int state = model.getState();
		switch (state) {
			case Editor.LOADING:
				model.setStatus("Loading...", false);
				break;
			case Editor.SAVING:
				model.setStatus("Saving...", false);
				break;
			case Editor.READY:
				model.setStatus("", true);
				break;
			case Editor.NEW:
				model.setStatus("", true);
				break;
			case Editor.DISCARDED:
				view.close();
		}
	}

}
