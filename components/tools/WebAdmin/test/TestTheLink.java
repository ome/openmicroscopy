

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IRepositoryInfo;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.model.meta.GroupExperimenterMap;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;

public class TestTheLink {

    private static Log log = LogFactory.getLog(TestTheLink.class);

    public static void main(String args[]) {
        
       
        Login l = new Login("root", "ome", "system", "User");
		Server s = new Server("warlock.openmicroscopy.org.uk", 1099);
		ServiceFactory sf = new ServiceFactory(s, l);
        IAdmin as = sf.getAdminService();
        IRepositoryInfo rs = sf.getRepositoryInfoService();
        IQuery qs = sf.getQueryService();
        IUpdate us = sf.getUpdateService();
        
        
        Experimenter exp = as.lookupExperimenter("af");
        
        List<GroupExperimenterMap> maps =  qs.findAllByQuery(
            "from GroupExperimenterMap as map " +
            "where map.child.id = :id", new Parameters().addId(exp.getId()));
        
        for(GroupExperimenterMap map : maps) {
        	if (log.isDebugEnabled()) {
        	    log.debug("m "+map.getParent().getId());
            }
        	//us.deleteObject(map);
        }
    }
}
