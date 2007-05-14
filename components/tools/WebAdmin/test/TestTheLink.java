

import java.util.List;

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
        	System.out.println("m "+map.getParent().getId());
        	//us.deleteObject(map);
        }
    }
}
