import java.util.ArrayList;
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


public class TestChangeGroups {

    private static Log log = LogFactory.getLog(TestChangeGroups.class);

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
        if (log.isDebugEnabled()) {
            log.debug("add new user to this group... ");
        }
        
        List<String> formExps = new ArrayList<String>();
        formExps.add("3");
        formExps.add("11");
        
        for(int i=0; i<formExps.size(); i++) {
        	
        	ExperimenterGroup[] exGr = as.containedGroups(Long.parseLong(formExps.get(i)));
        	List<String> exGrL = new ArrayList<String>();
        	for(int j=0; j<exGr.length; j++) {
                if (log.isDebugEnabled()) {
                    log.debug("existing gr '"+exGr[j].getName()+"' id:"+exGr[j].getId());
                }
        		exGrL.add(exGr[j].getId().toString());
        	}
            if (log.isDebugEnabled()) {
                log.debug("if contains... " + formExps.get(i));
            }
        	if(!exGrL.contains(group.getId().toString())) {
        		Experimenter tExp = as.getExperimenter(Long.parseLong(formExps.get(i)));
                if (log.isDebugEnabled()) {
                    log.debug("exp can be add... '"+ tExp.getOmeName()+"' id:"+tExp.getId());
                }
        		
        		//as.addGroups(tExp, group);
        	} else {
                if (log.isDebugEnabled()) {
                    log.debug("user exist on this group...");
                }
        	}
        	
        }
	}
}
