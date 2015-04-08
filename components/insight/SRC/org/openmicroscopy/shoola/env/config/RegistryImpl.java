/*
 * org.openmicroscopy.shoola.env.config.RegistryImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.util.Map;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.env.cache.CacheService;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.views.DataServicesView;
import org.openmicroscopy.shoola.env.data.views.DataViewsFactory;
import org.openmicroscopy.shoola.env.event.EventBus;
import omero.log.Logger;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import com.google.common.collect.MapMaker;

/** 
 * Implements the <code>Registry</code> interface. 
 * It maintains a map of {@link Entry} objects which are keyed 
 * by their <code>name</code> attribute and represent entries in a 
 * configuration file.  The map also contains all name-value pairs that are
 * added to the registry by means of the {@link #bind(String, Object) bind}
 * method.  References to the container's services are stored into member
 * fields &#151; as services are accessed frequently, this ensures <i>o(1)</i>
 * access time.
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
class RegistryImpl
    implements Registry
{
    
    /** The name-value map. */
    private final Map<String, Object> entriesMap = new MapMaker().makeMap();
    
    /** Reference to container's service. */
    private EventBus                eb;
    
	/** Reference to container's service. */
	private Logger             		logger;
	
	/** Reference to container's service. */
	private TaskBar					tb;
	
	/** Reference to container's service. */
   	private UserNotifier            un;
   	
    /** Reference to the image service. */
    private OmeroImageService		is;
    
    /** Reference to the metadata service. */
    private OmeroMetadataService	ms;
    
    /** Reference to the OMERO service. */
    private OmeroDataService		os;
    
    /** Reference to the Administration service. */
    private AdminService			admin;

    /** Reference to the Cache service. */
    private CacheService			cache;
    
    /* may be constructed only by classes in this package */
    RegistryImpl() { }
    
	/** 
     * Implemented as specified by {@link Registry}. 
     * @see Registry#bind(String, Object)
     */
	public void bind(String name, Object value)
	{
		if (name != null) {
			ObjectEntry entry = new ObjectEntry(name);
			entry.setContent(value);
			entriesMap.put(name, entry);
		}
	}
	
	/** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#lookup(String)
     */
    public Object lookup(String name)
    {
        Entry entry = (Entry) entriesMap.get(name);
        Object ret = null;
        if (entry != null)	ret = entry.getValue();
        return ret;
    }
    
	/** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getEventBus()
     */
    public EventBus getEventBus() { return eb; }
	
	/**
     * Implemented as specified by {@link Registry}.
     * @see Registry#getLogger()
     */
	public Logger getLogger() { return logger; }
	
	/** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getTaskBar()
     */
	public TaskBar getTaskBar() { return tb; }
	
	/** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getUserNotifier()
     */
	public UserNotifier getUserNotifier() { return un; }
   	
	/** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getImageService()
     */
    public OmeroImageService getImageService() { return is; }
    
    /** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getDataService()
     */
    public OmeroDataService getDataService() { return os; }
    
    /** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getMetadataService()
     */
    public OmeroMetadataService getMetadataService() { return ms; }
    
    /** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getAdminService()
     */
    public AdminService getAdminService() { return admin; }
    
    /** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getCacheService()
     */
    public CacheService getCacheService() { return cache; }
    
    /** 
     * Implemented as specified by {@link Registry}.
     * @see Registry#getDataServicesView(Class)
     */
    public DataServicesView getDataServicesView(Class<?> view)
    {
        return DataViewsFactory.makeView(view);
    }

	/** 
	* Adds the specified {@link Entry} to the map.
	*
	* @param e 	A new {@link Entry} created from an entry tag in the
	* 			configuration file.
	*/
	void addEntry(Entry e) { entriesMap.put(e.getName(), e); }
   	
	/**
	 * Stores a reference to the {@link EventBus}.
	 * 
	 * @param eb	The {@link EventBus}.
	 */
	void setEventBus(EventBus eb) { this.eb = eb; }
    
    /**
     * Stores a reference to the {@link OmeroImageService}.
     * 
     * @param is The {@link OmeroImageService}.
     */
    void setImageService(OmeroImageService is) { this.is = is; }
   	
    /**
     * Stores a reference to the {@link OmeroMetadataService}.
     * 
     * @param ms The {@link OmeroMetadataService}.
     */
    void setMetadataService(OmeroMetadataService ms) { this.ms = ms; }
    
    /**
     * Stores a reference to the {@link AdminService}.
     * 
     * @param ms The {@link AdminService}.
     */
    void setAdminService(AdminService admin) { this.admin = admin; }
    
	/**
	 * Stores a reference to the {@link TaskBar}.
	 * 
	 * @param tb The {@link TaskBar}.
	 */
	void setTaskBar(TaskBar tb) { this.tb = tb; }
   	
	/**
	 * Stores a reference to the {@link Logger}.
	 * 
	 * @param logger The {@link Logger}.
	 */
	void setLogger(Logger logger) { this.logger = logger; }
	
	/**
	 * Stores a reference to the {@link UserNotifier}.
	 * 
	 * @param un The {@link UserNotifier}.
	 */
	void setUserNotifier(UserNotifier un) { this.un = un; }
    
    /**
     * Stores a reference to the {@link OmeroDataService}.
     * 
     * @param os The {@link OmeroDataService}.
     */
    void setOS(OmeroDataService os) { this.os = os; }
    
    /**
     * Stores a reference to the {@link CacheService}.
     * 
     * @param cache The {@link CacheService}.
     */
    void setCacheService(CacheService cache) { this.cache = cache; }
    
}
