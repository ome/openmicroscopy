/*
 * org.openmicroscopy.shoola.env.config.Registry
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

package org.openmicroscopy.shoola.env.config;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.TopFrame;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** Declares the operations to be used to access configuration entries and 
 * container's services.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface Registry
{  
	/** 
	 * Binds a name to an object. 
	 * Adds an object in the {@link Entry}'s map.
	 * e.g. UserCredentials.
	 * 
	 * @param name		
	 * @param object
	 */
	public void bind(String name, Object obj);
	/**
	 * Retrieves an {@link Entry} from the map maintained by the Registry.
	 * 
	 * @param name	{@link Entry}'s name.
	 * @return See above.
	 */  
	public Object lookup(String name);
   	/**
    * Returns the {@link EventBus} registered.
    * 
    * @return See above.
    */
	public EventBus getEventBus();
   	/**
    * Returns the {@link DataManagementService} registered.
    * 
    * @return See above.
    */
   	public DataManagementService getDataManagementService();
   	/**
   	* Returns the {@link SemanticTypesService} registered.
   	* 
   	* @return See above.
   	*/
   	public SemanticTypesService getSemanticTypesService();	
   	/**
   	* Returns the {@link Logger} registered.
   	* 
   	* @return See above.
   	*/
	public Logger getLogService();
   	/**
   	* Returns the {@link TopFrame} registered.
   	* 
   	* @return See above.
   	*/
	public TopFrame getTopFrame();
   	/**
   	* Returns the {@link UserNotifier} registered.
   	* 
   	* @return See above.
   	*/
   	public UserNotifier getUserNotifier();
	/**
	* Returns the {@link PixelsService} registered.
	* 
	* @return See above.
	*/
	//	public PixelsService getPixelsServce();
   
}
