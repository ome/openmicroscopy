import java.util.ArrayList;
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


public class TestChangeGroups {

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
        
        System.out.println("add new user to this group... ");
        
        List<String> formExps = new ArrayList<String>();
        formExps.add("3");
        formExps.add("11");
        
        for(int i=0; i<formExps.size(); i++) {
        	
        	ExperimenterGroup[] exGr = as.containedGroups(Long.parseLong(formExps.get(i)));
        	List<String> exGrL = new ArrayList<String>();
        	for(int j=0; j<exGr.length; j++) {
        		System.out.println("existing gr '"+exGr[j].getName()+"' id:"+exGr[j].getId());
        		exGrL.add(exGr[j].getId().toString());
        	}
        	
        	System.out.println("if contains... " + formExps.get(i));
        	if(!exGrL.contains(group.getId().toString())) {
        		Experimenter tExp = as.getExperimenter(Long.parseLong(formExps.get(i)));
        		System.out.println("exp can be add... '"+ tExp.getOmeName()+"' id:"+tExp.getId());
        		
        		//as.addGroups(tExp, group);
        	} else {
        		System.out.println("user exist on this group...");
        	}
        	
        }
	}
}
