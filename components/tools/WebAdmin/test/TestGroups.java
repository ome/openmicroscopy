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

public class TestGroups {

    private static Log log = LogFactory.getLog(TestGroups.class);
    
    public static void main(String args[]) {
        
       
        Login l = new Login("root", "ome", "system", "User");
		Server s = new Server("warlock.openmicroscopy.org.uk", 1099);
		ServiceFactory sf = new ServiceFactory(s, l);
        IAdmin as = sf.getAdminService();
        IRepositoryInfo rs = sf.getRepositoryInfoService();
        IQuery qs = sf.getQueryService();
        IUpdate us = sf.getUpdateService();
        
        
        Experimenter exp_t = new Experimenter();
        exp_t.setOmeName("test-ola");
        exp_t.setFirstName("ola");
        exp_t.setMiddleName("a");
        exp_t.setLastName("AA");
        exp_t.setEmail("aa@mail.com");
        exp_t.setInstitution("some");
        
        ExperimenterGroup df = as.getGroup(2L);
        if (log.isDebugEnabled()) {
            log.debug("df id 2:"+df.getName());
        }
        
        ExperimenterGroup [] exg_t = new ExperimenterGroup[2];
        exg_t[0] = as.getGroup(4L);
        exg_t[1] = as.getGroup(5L);
        
        Long id = as.createExperimenter(exp_t, df, exg_t);
        if (log.isDebugEnabled()) {
            log.debug("id "+id);
        }
        
        Experimenter exp = as.getExperimenter(id);
        
        //Experimenter exp = as.lookupExperimenter("AA");
        ExperimenterGroup [] exg = as.containedGroups(exp.getId());
        
        for (int i=0; i<exg.length; i++) {
            if (log.isDebugEnabled()) {
                log.debug(exg[i].getName());
            }
        }
        
        //as.deleteExperimenter(exp);
        
    }
}
