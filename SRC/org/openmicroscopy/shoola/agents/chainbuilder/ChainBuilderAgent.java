/*
 * org.openmicroscopy.shoola.agents.zoombrowser.ChainBuilderAgent
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.chainbuilder;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ui.UIManager;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;


/** 
 * The chain builder agent
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * after code by 
 *  @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ChainBuilderAgent  
	implements Agent
{
	
	/** 
	 * Holds the agent's configuration and lets the agent access all services.
	 */
	private Registry	registry;
	
	/** Creates and manages the agent's UI. */
	private UIManager	uiManager;
	
	/** The data manager */
	private ChainDataManager dataManager;
	
	/**
	 * Does nothing.
	 * However, all agents must have a no-params public constructor.
	 */
	public ChainBuilderAgent() {}
	
	public void setContext(Registry ctx)
	{			
		registry = ctx;  //The container built our registry, store a reference.
	}
	
	public void activate() 
	{
		dataManager = new ChainDataManager(registry);
		uiManager = new UIManager(dataManager);  //Create the UI.
	}
	
	public boolean canTerminate() 
	{
		return true;  //This agent can be shut down at any time.
	}
	
	public void terminate() 
	{
		uiManager.disposeUI();  //Release native resources to exit gracefully.
	}
}
