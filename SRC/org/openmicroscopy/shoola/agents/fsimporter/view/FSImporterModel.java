/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.FSImporterModel 
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

import pojos.DataObject;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The Model component in the <code>FSImporter</code> MVC triad.
 * This class tracks the <code>FSImporter</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link FSImporterComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
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
class FSImporterModel
{

	/** Holds one of the state flags defined by {@link FSImporter}. */
	private int				state;

	/** Reference to the component that embeds this model. */
	protected FSImporter	component;

	/** The object where to import the images. */
	private DataObject		container;
	
	/** Creates a new instance. */
	FSImporterModel()
	{
		state = FSImporter.NEW;
	}
	
	/**
	 * Called by the <code>FSImporter</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(FSImporter component)
	{
		this.component = component;
	}
	
	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link FSImporter} interface.  
	 */
	int getState() { return state; }    

	/**
	 * Sets the staof the component.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }
	
	/**
	 * Sets the object in the {@link FSImporter#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		cancel();
		state = FSImporter.DISCARDED;
	}

	/**
	 * Sets the object in the {@link FSImporter#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
		state = FSImporter.READY;
	}
	
	/**
	 * Sets the container. 
	 * 
	 * @param container
	 */
	void setContainer(DataObject container) { this.container = container; }
	
	/**
	 * Returns the container.
	 * 
	 * @return See above.
	 */
	DataObject getContainer() { return container; }
	
}
