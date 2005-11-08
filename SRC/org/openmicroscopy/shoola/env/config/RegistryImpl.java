/*
 * org.openmicroscopy.shoola.env.config.RegistryImpl
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
import java.util.HashMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.OmeroPojoService;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.views.DataServicesView;
import org.openmicroscopy.shoola.env.data.views.DataViewsFactory;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
    private HashMap             	entriesMap;
    
    /** Reference to container's service. */
    private EventBus                eb;
    
	/** Reference to container's service. */
    private DataManagementService   dms;
    
	/** Reference to container's service. */
    private SemanticTypesService	sts;
    
	/** Reference to container's service. */
	private Logger             		logger;
	
	/** Reference to container's service. */
	private TaskBar					tb;
	
	/** Reference to container's service. */
   	private UserNotifier            un;
   	
    /** Reference to the container's pixel service. */
    private PixelsService           ps;
    
    /** Reference to the Omero service. */
    private OmeroPojoService            os;
    
    //private ImageService			is;

    /** Just creates an empty map. */
    RegistryImpl()
    {
        entriesMap = new HashMap();
    }
    
	/** Implemented as specified by {@link Registry}. */
	public void bind(String name, Object value)
	{
		if (name != null) {
			ObjectEntry entry = new ObjectEntry(name);
			entry.setContent(value);
			entriesMap.put(name, entry);
		}
	}
	
	/** Implemented as specified by {@link Registry}. */
    public Object lookup(String name)
    {
        Entry entry = (Entry) entriesMap.get(name);
        Object ret = null;
        if (entry != null)	ret = entry.getValue();
        return ret;
    }
    
	/** Implemented as specified by {@link Registry}. */
    public EventBus getEventBus() { return eb; }
    
	/** Implemented as specified by {@link Registry}. */
	public DataManagementService getDataManagementService() { return dms; }
	
	/** Implemented as specified by {@link Registry}. */
	public SemanticTypesService getSemanticTypesService() { return sts; }
	
	/** Implemented as specified by {@link Registry}. */
	public Logger getLogger() { return logger; }
	
	/** Implemented as specified by {@link Registry}. */
	public TaskBar getTaskBar() { return tb; }
	
	/** Implemented as specified by {@link Registry}. */
	public UserNotifier getUserNotifier() { return un; }
   	
	/** Implemented as specified by {@link Registry}. */
    public PixelsService getPixelsService() { return ps; }
    
    /** Implemented as specified by {@link Registry}. */
    public OmeroPojoService getOmeroService() { return os; }
    
    /** Implemented as specified by {@link Registry}. */
    public DataServicesView getDataServicesView(Class view)
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
	 * Stores a reference to the {@link DataManagementService}.
	 * 
	 * @param dms	The {@link DataManagementService}.
	 */
	void setDMS(DataManagementService dms) { this.dms = dms; }
	
	/**
	 * Stores a reference to the {@link SemanticTypesService}.
	 * 
	 * @param sts	The {@link SemanticTypesService}.
	 */
	void setSTS(SemanticTypesService sts) { this.sts = sts; }
    
    /**
     * Stores a reference to the {@link PixelsService}.
     * 
     * @param ps The {@link PixelsService}.
     */
    void setPS(PixelsService ps) { this.ps = ps; }
   	
	/**
	 * Stores a reference to the {@link TaskBar}.
	 * 
	 * @param tb	The {@link TaskBar}.
	 */
	void setTaskBar(TaskBar tb) { this.tb = tb; }
   	
	/**
	 * Stores a reference to the {@link Logger}.
	 * 
	 * @param logger	The {@link Logger}.
	 */
	void setLogger(Logger logger) { this.logger = logger; }
	
	/**
	 * Stores a reference to the {@link UserNotifier}.
	 * 
	 * @param un	The {@link UserNotifier}.
	 */
	void setUserNotifier(UserNotifier un) { this.un = un; }
    
    /**
     * Stores a reference to the {@link OmeroPojoService}.
     * 
     * @param os    The {@link OmeroPojoService}.
     */
    void setOS(OmeroPojoService os) { this.os = os; }
	
}
