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

/** 
 * Implements the <code>Registry</code> interface. 
 * It maintains a map of
 * <code>Entry</code> objects which are keyed by their <code>name</code> 
 * attribute and represent entries in configuration file.
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
    
    private HashMap             entriesMap;
    //private EventBus                eb;
    //private DataManagementService   dms;
    //private SemanticTypesService    sts;
    //private PixelsService           ps;
    //private LogService              ls;
    //private TopFrame                tf;
    //private UserNotifier            un;
    
    RegistryImpl()
    {
        entriesMap = new HashMap();
    }
	/** 
	 * Add a new entry in the map.
	 * 
	 * @param e new Entry.
	 */
    void addEntry(Entry e)
    {
        entriesMap.put(e.getName(), e);
    }
    
	/** Implemented as specified by {@link Registry}. */
    public Object lookup(String name)
    {
        Entry entry = (Entry) entriesMap.get(name);
        return entry.getValue();
    }
	/** Implemented as specified by {@link Registry}. */
    //public EventBus getEventBus();
	/** Implemented as specified by {@link Registry}. */
   //public DataManagementService getDataManagementService();
	/** Implemented as specified by {@link Registry}. */
   //public SemanticTypesService getSemanticTypesServices();
	/** Implemented as specified by {@link Registry}. */
   //public PixelsService getPixelsServices();
	/** Implemented as specified by {@link Registry}. */
   //public LogService getLogService();
	/** Implemented as specified by {@link Registry}. */
   //public TopFrame getTopFrame();
	/** Implemented as specified by {@link Registry}. */
   //public UserNotifier getUserNotifier();
    
    
}
