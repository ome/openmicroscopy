package org.openmicroscopy.shoola.env.config;

//Java imports
import java.util.HashMap;
/**
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
    //private LogService              ls;
    //private TopFrame                tf;
    //private UserNotifier            un;
    
    public RegistryImpl() {
        entriesMap = new HashMap();
    }
    void addEntry(Entry e) {
        entriesMap.put(e.getName(), e);
    }
    public Object lookup(String name) {
        return entriesMap.get(name);
    }
    //public EventBus getEventBus();
   //public DataManagementService getDataManagementService();
   //public SemanticTypesService getSemanticTypesServices();
   //public LogService getLogService();
   //public TopFrame getTopFrame();
   //public UserNotifier getUserNotifier();
}
