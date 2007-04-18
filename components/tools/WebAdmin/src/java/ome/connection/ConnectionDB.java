/*
 * ConnectionDB.java
 *
 * Created on March 6, 2007, 11:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ome.connection;

import java.util.ArrayList;
import java.util.List;

import ome.api.IAdmin;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.api.IQuery;
import ome.system.EventContext;

import javax.faces.context.FacesContext;
import ome.admin.controller.LoginBean;

import org.apache.log4j.Logger;
/**
 *
 * @author Ola
 */
public class ConnectionDB {

    static Logger logger = Logger.getLogger(ConnectionDB.class.getName());
	
    private IAdmin adminService;
    private IQuery queryService;
    private String userid;
    
    /** Creates a new instance of ConnectionDB */
    public ConnectionDB(String username, String password, String server, int port) {
        logger.info("Login - Service Factory connection to " + server + ":" + port +" by " + username + " ...");       
        try {
            
            Login l = new Login(username, password, "system", "User");
            Server s = new Server(server, port);
            ServiceFactory sf = new ServiceFactory(s, l);
            adminService = sf.getAdminService();
            queryService = sf.getQueryService();
            logger.info("Admin role for user "+adminService.getEventContext().getCurrentUserId());       
        
        } catch (Exception e) {
           
            Login l = new Login(username, password, "user", "User");
            Server s = new Server(server, port);
            ServiceFactory sf = new ServiceFactory(s, l);
            adminService = sf.getAdminService();
            queryService = sf.getQueryService();
            logger.info("User role for user "+adminService.getEventContext().getCurrentUserId());
 
        }
    }
    
    public ConnectionDB() { 
        FacesContext facesContext = FacesContext.getCurrentInstance();
        LoginBean lb = (LoginBean) facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "LoginBean");
        String username = lb.getUsername();
        String password = lb.getPassword();
        String server = lb.getServer(); 
        int port = lb.getPort();
        
        this.userid = lb.getId();
        
        logger.info("Service Factory connection to " + server + ":" + port +" by " + username + " ...");       
        String role = "user";
        if(lb.getRole()) role="system";
        
        try {
            Login l = new Login(username, password, role,"User");   
            Server s = new Server(server, port);
            ServiceFactory sf = new ServiceFactory(s, l);
            adminService = sf.getAdminService();
            queryService = sf.getQueryService();
        } catch (Exception e) {         
            e.printStackTrace();

        }
    }
    
    public void changeMyPassword(String password) {
        logger.info("changeMyPassword by user ID: " + userid);       
        adminService.changePassword(password);
    }
    
    public EventContext getCurrentEventContext() {
        return adminService.getEventContext();
    }

    public void changePassword(String username, String password) {
        logger.info("changePassword by user ID: " + userid);         
        adminService.changeUserPassword(username, password);
    }
        
    public List<ExperimenterGroup> lookupGroupsAdd() {
        return filterAdd(adminService.lookupGroups());
    }
    
    public List<ExperimenterGroup> lookupGroups() {
        return filter(adminService.lookupGroups());
    }
    
    public List<Experimenter> lookupExperimenters() {
        return adminService.lookupExperimenters();
    }
    
    public Experimenter lookupExperimenter(String omename){
        return adminService.lookupExperimenter(omename);
    }
        
    public ExperimenterGroup getGroup(Long id) {
        ExperimenterGroup exg = new ExperimenterGroup();
        exg = adminService.getGroup(id);
        return exg;
    }
    
    public ExperimenterGroup getGroup(String name) {
        ExperimenterGroup exg = new ExperimenterGroup();
        exg = queryService.findByString(ExperimenterGroup.class, "name", name);
        return exg;
    }
    
    public void updateGroup(ExperimenterGroup exg) {
        logger.info("updateGroup by user ID: " + userid);       
        try {
            adminService.updateGroup(exg);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    
    public void deleteGroup(Long id) {
        logger.info("deleteGroup by user ID: " + userid);       
        System.out.println("no method in adminService");
    }
    
    public void deleteExperimenter(Long id) {
        logger.info("deleteExperimenter by user ID: " + userid);       
        try {
            adminService.deleteExperimenter(adminService.getExperimenter(id));
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
    
    public void updateExperimenter(Experimenter exp) {
        logger.info("updateExperimenter by user ID: " + userid);       
        try {
            adminService.updateExperimenter(exp);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

    }

    public Experimenter getExperimenter(Long id) {      
        return adminService.getExperimenter(id);
    }
    
    public long createGroup(ExperimenterGroup group) {
        logger.info("createGroup by user ID: " + userid); 
        long id = 0L;
        try {
            id = adminService.createGroup(group);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return id;        
    }
    
    public long createExperimenter(Experimenter experimenter, ExperimenterGroup defaultGroup, ExperimenterGroup... groups) {       
        logger.info("createExperimenter by user ID: " + userid);  
        long id = 0L;
        try {
            id = adminService.createExperimenter(experimenter, defaultGroup, groups);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return id;
    }
    
    public boolean checkExperimenter(String omeName) {
        if(queryService.findByString(Experimenter.class, "omeName", omeName) != null) return true;
        return false;
    }
    
    public boolean checkEmail(String email) {
        if(queryService.findByString(Experimenter.class, "email", email) != null) return true;
        return false;
    }
    
    public boolean isAdmin(Long experimenterId) {
        boolean role = false; 
        ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);        
        for(int i=0; i<exg.length; i++) {
            if(exg[i].getName().equals("system"))  role= true;
        }
        return role;
    }
    
    public boolean isUser(Long experimenterId) {
        boolean role = false; 
        ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);        
        for(int i=0; i<exg.length; i++) {
            if(exg[i].getName().equals("user"))  role= true;
        }
        return role;
    }
    
    public ExperimenterGroup[] containedGroups(Long experimenterId) {       
        ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
        return filter(exg);
    }
    
    public ExperimenterGroup[] containedMyGroups(Long experimenterId) {       
        ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
        return filterMy(exg);
    }
    
    public ExperimenterGroup getDefaultGroup(Long experimenterId) {
        ExperimenterGroup exg = new ExperimenterGroup();
        try {
            exg = adminService.getDefaultGroup(experimenterId);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return exg;
    }
      
    public void setDefaultGroup(Experimenter user, ExperimenterGroup group) {
        logger.info("setDefaultGroup by user ID: " + userid);  
        try {
            adminService.setDefaultGroup(user, group);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        logger.info("default group " + group.getName() + "[id:" + group.getId() + "] for user: " + user.getOmeName() + "[id:" + user.getId() + "] was set");               
    }        
    
    public void setOtherGroups(Experimenter user, ExperimenterGroup [] group, ExperimenterGroup defaultGroup, boolean userRole, boolean adminRole) {
        logger.info("setOtherGroups by user ID: " + userid);       
        try {
            ExperimenterGroup [] old = adminService.containedGroups(user.getId());
            adminService.addGroups(user, filterAd(old, group, defaultGroup));

            adminService.setDefaultGroup(user, defaultGroup);
            adminService.removeGroups(user, filterRm(old, group, defaultGroup));

            if(!isAdmin(user.getId())==adminRole) {
                   ExperimenterGroup adminGroup = getGroup("system");
                   if(adminRole) {                   
                       adminService.addGroups(user, adminGroup);
                   } else {
                       adminService.removeGroups(user, adminGroup);
                   }
            }

            if(!isUser(user.getId())==userRole) {
                   ExperimenterGroup userGroup = getGroup("user");
                   if(userRole) {                   
                       adminService.addGroups(user, userGroup);
                   } else {
                       adminService.removeGroups(user, userGroup);
                   }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

    } 
    
    private ExperimenterGroup [] filterAd(ExperimenterGroup [] old, ExperimenterGroup [] newGroups, ExperimenterGroup defaultGroup){
        List<ExperimenterGroup> fNewGroups = new ArrayList<ExperimenterGroup>();
        for (int i=0; i < newGroups.length; i++) {
            if (!newGroups[i].getName().equals("user") 
                && !newGroups[i].getName().equals("system") ) {
                fNewGroups.add(newGroups[i]);   
               
            }
        }
        
        List<ExperimenterGroup> fOld = new ArrayList<ExperimenterGroup>();
        for (int i=0; i < old.length; i++) {
            if (!old[i].getName().equals("user") 
                && !old[i].getName().equals("system") ) {
                fOld.add(old[i]);
                
            }
        }
        
        for (int i=0; i < fNewGroups.size(); i++){
            int flag = 0;
            for(int j=0; j<fOld.size(); j++) {                  
                if(fNewGroups.get(i).getId().equals(fOld.get(j).getId())) {
                    flag++;
                }                
            }
            if(flag>0) {
                fNewGroups.remove(i);
                i--;
            } else {
                logger.info("group to add " + fNewGroups.get(i).getName() + "[id:" + fNewGroups.get(i).getId() + "]"); 
                
            }
        }
        return fNewGroups.toArray(new ExperimenterGroup[fNewGroups.size()]);
    }

    private ExperimenterGroup [] filterRm(ExperimenterGroup [] old, ExperimenterGroup [] newGroups, ExperimenterGroup defaultGroup){
        List<ExperimenterGroup> fOldGroups = new ArrayList<ExperimenterGroup>();
        for (int i=0; i < old.length; i++) {
            if (!old[i].getName().equals("default") 
                && !old[i].getName().equals("user") 
                && !old[i].getName().equals("system") ) {
                fOldGroups.add(old[i]);
                
            }
        }
        
        List<ExperimenterGroup> fNew = new ArrayList<ExperimenterGroup>();
        for (int i=0; i < newGroups.length; i++) {
            if (!newGroups[i].getName().equals("default") 
                && !newGroups[i].getName().equals("user") 
                && !newGroups[i].getName().equals("system") ) {
                fNew.add(newGroups[i]);
                
            }
        }
        
        for (int i=0; i < fOldGroups.size(); i++){   
            int flag = 0;
            for (int j=0; j < fNew.size(); j++){           
                if(fOldGroups.get(i).getId().equals(fNew.get(j).getId())) {
                    flag++;
                }               
            }   
            if(flag>0) {
                fOldGroups.remove(i);
                i--;
            } else {
                logger.info("group to remove " + fOldGroups.get(i).getName() + "[id:" + fOldGroups.get(i).getId() + "]");   
                
            }
                    
        }
        
        return fOldGroups.toArray(new ExperimenterGroup[fOldGroups.size()]);
    }
    
    private ExperimenterGroup [] filter(ExperimenterGroup [] groups){
        List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
        for (int i=0; i < groups.length; i++) {
            if (!groups[i].getName().equals("default") 
                && !groups[i].getName().equals("user") 
                && !groups[i].getName().equals("system")) {
                filteredGroups.add(groups[i]);
            }
        }
        return filteredGroups.toArray(new ExperimenterGroup[filteredGroups.size()]);
    }
    
    private ExperimenterGroup [] filterMy(ExperimenterGroup [] groups){
        List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
        for (int i=0; i < groups.length; i++) {
            if (!groups[i].getName().equals("user") 
                && !groups[i].getName().equals("system")
            ) {
                filteredGroups.add(groups[i]);
            }
        }
        return filteredGroups.toArray(new ExperimenterGroup[filteredGroups.size()]);
    }

    private List<ExperimenterGroup> filter(List<ExperimenterGroup> groups){
        List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
        for (int i=0; i < groups.size(); i++) {
            if (!groups.get(i).getName().equals("default") 
                && !groups.get(i).getName().equals("user") 
                && !groups.get(i).getName().equals("system")) {
                filteredGroups.add(groups.get(i));
            }
        }
        return filteredGroups;
    }
    
    private List<ExperimenterGroup> filterAdd(List<ExperimenterGroup> groups){
        List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
        for (int i=0; i < groups.size(); i++) {
            if (!groups.get(i).getName().equals("user") 
                && !groups.get(i).getName().equals("system")
            ) {
                filteredGroups.add(groups.get(i));
            }
        }
        return filteredGroups;
    }

}
