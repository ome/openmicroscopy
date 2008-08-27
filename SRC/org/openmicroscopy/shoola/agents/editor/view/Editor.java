/*
 * org.openmicroscopy.shoola.agents.editor.view.Editor 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

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
 * @since 3.0-Beta3
 */
public interface Editor
	extends ObservableComponent
{

	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Loading</i> state. */
	public static final int     LOADING = 2;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 3;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 4;

	
	/**
	 * Starts the data loading process when the current state is {@link #NEW} 
	 * and puts the window on screen.
	 * If the state is not {@link #NEW}, then this method simply moves the
	 * window to front.
	 * 
	 * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
	 */
	public void activate();

	/**
	 * Transitions the viewer to the {@link #DISCARDED} state.
	 * Any ongoing data loading is cancelled.
	 */
	public void discard();

	/**
	 * Queries the current state.
	 * 
	 * @return One of the state flags defined by this interface.
	 */
	public int getState();

	/**
	 * Callback used by data loaders to provide the viewer with feedback about
	 * the data retrieval.
	 * 
	 * @param description   Textual description of the ongoing operation.
	 * @param hide          Pass <code>true</code> to indicate tha the work 
	 * 						is done, <code>false</otherwise> otherwise.
	 */
	public void setStatus(String description, boolean hide);

	/** Cancels any ongoing data loading. */
	public void cancel();

	/**
	 * Sets the loaded file.
	 * 
	 * @param file The loaded file.
	 */
	public void setFileToEdit(File file);

	/**
	 * Returns the title of this editor.
	 * 
	 * @return See above.
	 */
	public String getEditorTitle();
	
	/**
	 * Returns the browser component
	 * 
	 * @return	See above.
	 */
	public Browser getBrowser();
	
}
