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
