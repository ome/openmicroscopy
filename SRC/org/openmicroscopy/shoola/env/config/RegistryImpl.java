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
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.TopFrame;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * Implements the <code>Registry</code> interface. 
 * It maintains a map of {@link Entry} objects which are keyed 
 * by their <code>name</code> attribute and represent entries in 
 * configuration file.
 * It also maintains references to the container's services into member fields,
 * this ensures <code>o(1)</code> access time.
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
class RegistryImpl
    implements Registry
{
    
    private HashMap             	entriesMap;
    private EventBus                eb;
    private DataManagementService   dms;
    private SemanticTypesService	sts;
	private Logger             		logger;
	private TopFrame                tf;
   	private UserNotifier            un;
    //private PixelsService           ps;
    //private ImageService			is;
   
    
    RegistryImpl()
    {
        entriesMap = new HashMap();
    }
    
	/**  Implemented as specified by {@link Registry}. */
	public void bind(String name, Object obj)
	{
		entriesMap.put(name, obj);
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
    public EventBus getEventBus()
    {
    	return eb;
    }
    
	/** Implemented as specified by {@link Registry}. */
	public DataManagementService getDataManagementService()
	{
		return dms;
	}
	
	/** Implemented as specified by {@link Registry}. */
	public SemanticTypesService getSemanticTypesService()
	{
		return sts;
	}
	
	/** Implemented as specified by {@link Registry}. */
	public Logger getLogger()
	{
		return logger;
	}
	
	/** Implemented as specified by {@link Registry}. */
	public TopFrame getTopFrame()
	{
		return tf;
	}
	
	/** Implemented as specified by {@link Registry}. */
	public UserNotifier getUserNotifier() 
	{
   		return un;
   	}
   	
	/** Implemented as specified by {@link Registry}. */
//	public PixelsService getPixelsServices();
//TODO: add it when ready.

	/** 
	* Add a new entry in the map of {@link Entry}.
	* The {@link Entry} object is created after parsing the configuration file.
	* 
	* @param e new Entry.
	*/
	void addEntry(Entry e)
	{
		entriesMap.put(e.getName(), e);
   	}
   	
	/**
	* Sets the {@link EventBus}.
	* 
	* @param eb	{@link EventBus}.
	*/
	void setEventBus(EventBus eb)
	{
		this.eb = eb;
	}
	
   	/**
	* Sets the {@link DataManagementService}.
	* 
	* @param dms	{@link DataManagementService}.
	*/
	void setDMS(DataManagementService dms)
	{
		this.dms = dms;
	}
	
	/**
	* Sets the {@link SemanticTypeService}.
	* 
	* @param sts {@link SemanticTypeService}.
	*/
	void setSTS(SemanticTypesService sts)
   	{
		this.sts = sts;
   	}
   	
   	/**
	* Sets the {@link TopFrame}.
	* 
	* @param tf {@link TopFrame}.
	*/
   	void setTopFrame(TopFrame tf)
   	{
		this.tf = tf;
   	}
   	
	/**
	* Sets the {@link Logger}.
	* 
	* @param logger {@link Logger}.
	*/
	void setLogger(Logger logger)
	{
		this.logger = logger;
	}
	
	/**
	* Sets the {@link UserNotifier}.
	* 
	* @param un {@link UserNotifier}.
	*/
	void setUserNotifier(UserNotifier un)
	{
		this.un = un;
	}
	
}
