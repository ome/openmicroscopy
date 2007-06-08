

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IRepositoryInfo;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;

public class TestDelete {

    private static Log log = LogFactory.getLog(TestDelete.class);

    public static void main(String args[]) {
        
       
        Login l = new Login("root", "ome", "system", "User");
		Server s = new Server("warlock.openmicroscopy.org.uk", 1099);
		ServiceFactory sf = new ServiceFactory(s, l);
        IAdmin as = sf.getAdminService();
        IRepositoryInfo rs = sf.getRepositoryInfoService();
        IQuery qs = sf.getQueryService();
        IUpdate us = sf.getUpdateService();
        

        Experimenter exp = as.lookupExperimenter("test-ola");
        
        //Experimenter exp = as.lookupExperimenter("AA");
        ExperimenterGroup [] exg = as.containedGroups(exp.getId());
        
        for (int i=0; i<exg.length; i++) {
        	if (log.isDebugEnabled()) {
        	    log.debug(exg[i].getName());
            }
        }
        
        as.deleteExperimenter(exp);
        
    }
}
