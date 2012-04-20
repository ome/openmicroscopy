/*
 * org.openmicroscopy.shoola.env.config.Registry
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.config;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.cache.CacheService;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.views.DataServicesView;
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
	 * Maps <code>name</code> onto <code>value</code>.
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
	 * Returns a reference to the {@link OmeroImageService}.
	 * 
	 * @return See above.
	 */
	public OmeroImageService getImageService();
    
    /**
     * Returns a reference to the {@link OmeroDataService}.
     * 
     * @return See above.
     */
    public OmeroDataService getDataService();
    
    /**
     * Returns a reference to the {@link OmeroMetadataService}.
     * 
     * @return See above.
     */
    public OmeroMetadataService getMetadataService();
    
    /**
     * Returns a reference to the {@link AdminService}.
     * 
     * @return See above.
     */
    public AdminService getAdminService();
 
    /**
     * Returns a reference to the {@link CacheService}.
     * 
     * @return See above.
     */
    public CacheService getCacheService();

    /**
     * Returns an implementation of the specified <code>view</code>.
     * The <code>view</code> argument specifies the interface class that
     * defines the desired view.  It has to be one of the sub-interfaces 
     * of {@link DataServicesView} defined in its enclosing package.  The
     * returned object implements the <code>view</code> interface and can
     * be safely cast to it.
     * 
     * @param view The view's interface.
     * @return An implementation of the specified <code>view</code>.
     * @throws NullPointerException If <code>view</code> is <code>null</code>.
     * @throws IllegalArgumentException If <code>view</code> is not one of the
     *          supported {@link DataServicesView} interfaces.
     */
    public DataServicesView getDataServicesView(Class<?> view);

}
