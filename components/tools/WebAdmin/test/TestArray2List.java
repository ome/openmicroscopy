
import java.util.Arrays;
import java.util.List;

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	      
        Login l = new Login("root", "ome", "system", "User");
		Server s = new Server("warlock.openmicroscopy.org.uk", 1099);
		ServiceFactory sf = new ServiceFactory(s, l);
        IAdmin as = sf.getAdminService();
        IRepositoryInfo rs = sf.getRepositoryInfoService();
        IQuery qs = sf.getQueryService();
        IUpdate us = sf.getUpdateService();
        
        ExperimenterGroup group = as.getGroup(4L);
        Experimenter [] exps = as.containedExperimenters(group.getId());
       
        System.out.println("group "+group.getName());
        for (int i=0; i<exps.length; i++) {
        	Experimenter tExp = exps[i];
        	System.out.println("ex "+tExp.getOmeName()+" id: "+tExp.getId());
        }
        
        List<Experimenter> newExp = Arrays.asList(exps);

        System.out.println(newExp);
	}
}
