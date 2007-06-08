
import java.util.Arrays;
import java.util.List;

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


public class TestArray2List {
    
    private static Log log = LogFactory.getLog(TestArray2List.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        
        Login l = new Login("root", "ome", "system", "User");
		Server s = new Server("warlock.openmicroscopy.org.uk", 1099);
		ServiceFactory sf = new ServiceFactory(s, l);
        IAdmin as = sf.getAdminService();
        IRepositoryInfo rs = sf.getRepositoryInfoService();
        IQuery qs = sf.getQueryService();
        IUpdate us = sf.getUpdateService();
        
        ExperimenterGroup group = as.getGroup(4L);
        Experimenter [] exps = as.containedExperimenters(group.getId());
        
        if (log.isDebugEnabled()) {
            log.debug("group "+group.getName());
        }
        for (int i=0; i<exps.length; i++) {
        	Experimenter tExp = exps[i];
            if (log.isDebugEnabled()) {
                log.debug("ex "+tExp.getOmeName()+" id: "+tExp.getId());
            }
        }
        
        List<Experimenter> newExp = Arrays.asList(exps);
        if (log.isDebugEnabled()) {
            log.debug(newExp);
        }
	}
}
