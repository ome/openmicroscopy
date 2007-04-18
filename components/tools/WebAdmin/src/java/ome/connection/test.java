/*
 * test.java
 *
 * Created on March 23, 2007, 3:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ome.connection;

import java.util.List;
import java.util.ArrayList;
import ome.api.IAdmin;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.api.IQuery;
import ome.system.EventContext;
import ome.model.meta.GroupExperimenterMap;
import javax.ejb.EJBAccessException;
import java.util.Iterator;
/**
 *
 * @author Ola
 */
public class test {
    

    public static void main(String args[]){
        String username = "ola";
        String password = "aaa";
        String pr = "system";
        String server = "warlock.openmicroscopy.org.uk";
        int port = 1099;       
            
        
        try {
            Login l = new Login(username, password, pr, "User");
            Server s = new Server(server, port);
            ServiceFactory sf = new ServiceFactory(s, l);
            IAdmin adminService = sf.getAdminService();   
            System.out.println("isAdmin "+adminService.getEventContext().isCurrentUserAdmin());
 /*           
            System.out.println("type " + adminService.getEventContext().getCurrentEventType());
            System.out.println("group "+adminService.getEventContext().getCurrentGroupName());
            List<Long> list = adminService.getEventContext().getLeaderOfGroupsList();
        
            for (int i=0; i<list.size(); i++ ) {

                System.out.println("list l " + list.get(i));

            }

            List<Long> listm = adminService.getEventContext().getMemberOfGroupsList();
            for (int i=0; i<listm.size(); i++ ) {

                System.out.println("list m " + listm.get(i));

            }
*/
            Experimenter exp1 = adminService.lookupExperimenter("aleksa");
            System.out.println("user "+exp1.getOmeName() + exp1.getId());
            System.out.println("size " +exp1.sizeOfGroupExperimenterMap());

            ExperimenterGroup gg = (ExperimenterGroup) exp1.getDefaultGroupLink().getParent();
            System.out.println("default group " +gg.getId() + " " + gg.getName() );

            for(Iterator iter  = exp1.iterateGroupExperimenterMap(); iter.hasNext(); ) {
                GroupExperimenterMap value = (GroupExperimenterMap) iter.next();
                ExperimenterGroup gr = (ExperimenterGroup) value.getParent();
                System.out.println("iter "+ gr.getName() + " "+gr.getId());
            }
            
            System.out.println("default "+adminService.getDefaultGroup(exp1.getId()).getId());
            
            //adminService.addGroups(exp1, adminService.getGroup(4L));
            //adminService.setDefaultGroup(exp1, adminService.getGroup(0L));
            /*ExperimenterGroup [] rmg = new ExperimenterGroup [2];
            rmg[0]=adminService.getGroup(5L);
            rmg[1]=adminService.getGroup(4L);
            adminService.removeGroups(exp1, rmg);
            ExperimenterGroup [] exgs = adminService.containedGroups(exp1.getId());
            //List<ExperimenterGroup> newList = new ArrayList();

            for (int i=0; i<exgs.length; i++ ) {

                System.out.println("group "+exgs[i].getId()+" "+exgs[i].getName());

            }

            System.out.println("default "+adminService.getDefaultGroup(exp1.getId()).getName());
            //ExperimenterGroup d = adminService.getDefaultGroup(exp1.getId());
            //System.out.println("defaut "+d.getId()+" "+d.getName());
            //ExperimenterGroup [] nexgs = newList.toArray(new ExperimenterGroup[newList.size()]);

            //adminService.deleteExperimenter(exp1);
            //adminService.addGroups(exp1, adminService.getGroup(0L));
            //adminService.setDefaultGroup(exp1, adminService.getGroup(0L));
            //adminService.removeGroups(exp1, adminService.getGroup(0L));

            //exp1.setMiddleName("asfads");
            //adminService.updateExperimenter(exp1);
            /*
            Experimenter exp = adminService.lookupExperimenter("josh");
            System.out.println("id "+exp.getId());

            ExperimenterGroup [] exgs = adminService.containedGroups(exp.getId());
            for (int i=0; i<exgs.length; i++ ) {
                System.out.println("group "+exgs[i].getId()+" "+exgs[i].getName());
            }
            //exp1.
    */
       } catch (Exception e) {
            e.printStackTrace();
        }
        
    }    
    
}
