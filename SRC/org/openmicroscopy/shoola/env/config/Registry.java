package org.openmicroscopy.shoola.env.config;

/** Declares the operations to be used to access configuration entries and container's services
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
public interface Registry{    
   public Object lookup(String name);
   //public EventBus getEventBus();
   //public DataManagementService getDataManagementService();
   //public SemanticTypesService getSemanticTypesServices();
   //public LogService getLogService();
   //public TopFrame getTopFrame();
   //public UserNotifier getUserNotifier();
   
   
}
