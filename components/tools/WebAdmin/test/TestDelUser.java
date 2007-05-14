import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IRepositoryInfo;
import ome.api.IUpdate;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;


public class TestDelUser {

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
        
        Experimenter exp = as.lookupExperimenter("aleksa");
        as.deleteExperimenter(exp);
	}

}
