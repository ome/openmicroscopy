/*
 * org.openmicroscopy.shoola.env.config.RegistryImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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

/*------------------------------------------------------------------------------
 *
* Written by:     Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *                      <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 *                      Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *                      <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.config;

//Java imports
import java.util.HashMap;

/** Implements the <code>Registry</code> interface. It maintains a map of <code>Entry</code> objects
 * which are keyed by their <code>name</code> attribute and represent entries in configuration file.
 * It also maintains references to the container's services into member fields, this ensures <code>
 * o(1)</code> access time.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
public class RegistryImpl
    implements Registry {
    
    private HashMap                 entriesMap;
    //private EventBus                eb;
    //private DataManagementService   dms;
    //private SemanticTypesService    sts;
    //private PixelsService           ps;
    //private LogService              ls;
    //private TopFrame                tf;
    //private UserNotifier            un;
    
    public RegistryImpl() {
        entriesMap = new HashMap();
    }
    void addEntry(Entry e) {
        entriesMap.put(e.getName(), e);
    }
    
/** Implemented as specified by {@linkRegistry}.
 */
    public Object lookup(String name) {
        return entriesMap.get(name);
    }
/** Implemented as specified by {@linkRegistry}.
 */
    //public EventBus getEventBus();
/** Implemented as specified by {@linkRegistry}.
 */
   //public DataManagementService getDataManagementService();
/** Implemented as specified by {@linkRegistry}.
 */
   //public SemanticTypesService getSemanticTypesServices();
/** Implemented as specified by {@linkRegistry}.
 */
   //public PixelsService getPixelsServices();
/** Implemented as specified by {@linkRegistry}.
 */
   //public LogService getLogService();
/** Implemented as specified by {@linkRegistry}.
 */
   //public TopFrame getTopFrame();
/** Implemented as specified by {@linkRegistry}.
 */
   //public UserNotifier getUserNotifier();
    
    
}
