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
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/**
 * Declares the operations to be used to access configuration entries and 
 * the services of the container.
 * <p>The objects corresponding to configuration entries are accessed through
 * the {@link #lookup(String) lookup)} method, passing in the content of the 
 * <i>name</i> attribute of the entry tag.  The services of the container are
 * accessed through the <code>getXXX</code> methods (with the exception of 
 * the rendering service, which is accessed by means of the event bus).</p>
 * <p>A registry can also be used as a map for in-memory objects. The 
 * {@link #bind(String, Object) bind} method maps a name onto an arbitrary
 * object, which can then be retrieved by passing that name to the
 * {@link #lookup(String) lookup} method.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface Registry
{  
	
	/** 
	 * Maps <code>name</code> onto <code>value<code>.
	 * The object can then be retrieved by passing <code>name</code> to the
	 * {@link #lookup(String) lookup} method.  
	 * 
	 * @param name	This name of the entry. If <code>null</code>, this method
	 * 				does nothing.		
	 * @param value	The object to map onto <code>name</code>.
	 */
	public void bind(String name, Object value);
	
	/**
	 * Retrieves the object keyed by <code>name</code> from the registry.
	 * The <code>name</code> parameter is either the value of the <i>name</i>
	 * attribute of an entry tag in the configuration file (in this case the
	 * object that represents the configuration entry will be returned) or the
	 * name to which the object was bound &#151; by means of the
	 * {@link #bind(String, Object) bind} method (in this case the object that
	 * was originally passed to {@link #bind(String, Object) bind()} will be 
	 * returned). 
	 * 
	 * @param name	The name which an object within this registry is
	 * 				mapped onto.
	 * @return The object mapped to <code>name</code> or <code>null</code> if
	 * 			no such a mapping exists.
	 */  
	public Object lookup(String name);
	
   	/**
   	 * Returns a reference to the {@link EventBus}.
   	 * 
   	 * @return See above.
   	 */
	public EventBus getEventBus();
	
	/**
	 * Returns a reference to the {@link DataManagementService}.
	 * 
	 * @return See above.
	 */
   	public DataManagementService getDataManagementService();
   	
   	/**
	 * Returns a reference to the {@link SemanticTypesService}.
	 * 
	 * @return See above.
	 */
    public SemanticTypesService getSemanticTypesService();
    
	/**
	 * Returns a reference to the {@link Logger}.
	 * 
	 * @return See above.
	 */
	public Logger getLogger();
	
	/**
	 * Returns a reference to the {@link TaskBar}.
	 * 
	 * @return See above.
	 */
	public TaskBar getTaskBar();
	
   	/**
	 * Returns a reference to the {@link UserNotifier}.
	 * 
	 * @return See above.
	 */
   	public UserNotifier getUserNotifier();
   	
	/**
	 * Returns a reference to the {@link PixelsService}.
	 * 
	 * @return See above.
	 */
	public PixelsService getPixelsService();
   
}
