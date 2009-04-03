/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.FSImporter 
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface provided by the fs importer component. 
 * The Viewer provides a top-level window hosting UI components to interact 
 * with the fs instance.
 *
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public interface Importer
	extends ObservableComponent
{

	/** Flag to denote the <i>New</i> state. */
	public static final int     NEW = 1;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int     READY = 2;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int     DISCARDED = 3;
	
	/** Flag to denote the <i>Importing</i> state. */
	public static final int     IMPORTING = 4;
	
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
	
	/** Cancels any ongoing data loading. */
	public void cancel();

	/**
	 * Imports the specified files.
	 * 
	 * @param data The data to import.
	 */
	public void importData(File[] data);

	/**
	 * Monitors the passed directory.
	 * 
	 * @param dir The directory to monitor.
	 */
	public void monitorDirectory(File dir);
	
}
