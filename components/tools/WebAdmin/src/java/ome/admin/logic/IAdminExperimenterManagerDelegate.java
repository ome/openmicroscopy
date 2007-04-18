/*
 * IAdminGroupManagerDelegate.java
 *
 * Created on March 14, 2007, 10:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ome.admin.logic;

import ome.connection.ConnectionDB;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Ola
 */
public class IAdminExperimenterManagerDelegate implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Experimenter> experimenters = new ArrayList<Experimenter>();
    private String sortByProperty = "firstName";
        
    private transient final Comparator propertyAscendingComparator = new Comparator() {
            public int compare(Object object1, Object object2) {
                try {
                    String property1 = BeanUtils.getProperty(object1,
                            IAdminExperimenterManagerDelegate.this.sortByProperty
                        );
                    String property2 = BeanUtils.getProperty(object2,
                            IAdminExperimenterManagerDelegate.this.sortByProperty
                        );

                    return property1.toLowerCase().compareTo(property2.toLowerCase());
                } catch (Exception e) {
                    return 0;
                }
            }
        };

    private transient final Comparator propertyDescendingComparator = new Comparator() {
            public int compare(Object object1, Object object2) {
                try {
                    String property1 = BeanUtils.getProperty(object1,
                            IAdminExperimenterManagerDelegate.this.sortByProperty
                        );
                    String property2 = BeanUtils.getProperty(object2,
                            IAdminExperimenterManagerDelegate.this.sortByProperty
                        );

                    return property2.toLowerCase().compareTo(property1.toLowerCase());
                } catch (Exception e) {
                    return 0;
                }
            }
        };
 
        ConnectionDB db = new ConnectionDB();
    {
        getExperimenters();
    }
    
    public ExperimenterGroup[] containedGroups(Long experimenterId) {
        ExperimenterGroup[] exg = db.containedGroups(experimenterId);
        return exg;
    } 
    
    public ExperimenterGroup[] containedMyGroups(Long experimenterId) {
        ExperimenterGroup[] exg = db.containedMyGroups(experimenterId);
        return exg;
    } 
    
    public ExperimenterGroup getDefaultGroup(Long experimenterId) {
        ExperimenterGroup exg = db.getDefaultGroup(experimenterId);
        return exg;
    }
    
    public void changeMyPassword (String password) {
        db.changeMyPassword(password);
    }
    
    public void changePassword (String username, String password) {
        db.changePassword(username, password);
    }
    
    public boolean isAdmin(Long experimenterId) {
        return db.isAdmin(experimenterId);
    }

    public boolean isUser(Long experimenterId) {
        return db.isUser(experimenterId);
    }
        
    public List<ExperimenterGroup> getGroups() {
        List<ExperimenterGroup> exg = db.lookupGroups();
        return exg;
    }
    
    public List<ExperimenterGroup> getGroupsAdd() {
        List<ExperimenterGroup> exg = db.lookupGroupsAdd();
        return exg;
    }
    
    public Experimenter getExperimenterById(Long id) {
        return (Experimenter) db.getExperimenter(id);
    }
        
    public List<Experimenter> getExperimenters() {
        this.experimenters = db.lookupExperimenters();
        return this.experimenters;
    }
    
    public void createExperimenter(Experimenter experimenter, Long group, List otherGroups, boolean userRole, boolean adminRole) {
        ExperimenterGroup defaultGroup = new ExperimenterGroup(group); 
        
        int c = 0;        
        if(userRole) c++;        
        if(adminRole) c++;
        
        ExperimenterGroup [] others = new ExperimenterGroup[(otherGroups.size())+c];
        for(int i=0; i<(otherGroups.size()); i++){
            others[i] = db.getGroup(Long.parseLong(otherGroups.get(i).toString()));           
        }
        
        if(userRole) {
            others[otherGroups.size()] = db.getGroup("user");
            if (adminRole) {
                others[otherGroups.size()+1] = db.getGroup("system");
            }
        } else if (adminRole) others[otherGroups.size()] = db.getGroup("system");
        

        db.createExperimenter(experimenter, defaultGroup, others);
        //if(adminRole) db.setDefaultGroup(experimenter,db.getGroup("system"));
    }

    public boolean checkExperimenter(String omeName){
        return db.checkExperimenter(omeName);
    }

    public boolean checkEmail(String email){
        return db.checkEmail(email);
    }

    public List<Experimenter> sortItems(String sortItem, String sort) {
        this.experimenters = getExperimenters();
        sortByProperty = sortItem;
        if(sort.equals("asc")) sort(propertyAscendingComparator);
        else if(sort.equals("dsc")) sort(propertyDescendingComparator);
        return experimenters;
    }

    public Experimenter readExperimenter(int id) {
        Experimenter experimenter = (Experimenter) this.experimenters.get(id);
        return experimenter;
    }

    public void updateExperimenter(Experimenter experimenter, Long dgroup, List otherGroups, boolean userRole, boolean adminRole) {
        db.updateExperimenter(experimenter);
        ExperimenterGroup [] others = new ExperimenterGroup[otherGroups.size()];
        for(int i=0; i<otherGroups.size(); i++){
            others[i] = db.getGroup(Long.parseLong(otherGroups.get(i).toString()));            
        }
        
        ExperimenterGroup defaultGroup = db.getGroup(dgroup);          
        db.setOtherGroups(experimenter, others, defaultGroup, userRole, adminRole); 
   
    }

    public void updateMyAccount(Experimenter experimenter, Long group) {
        db.updateExperimenter(experimenter);
        ExperimenterGroup defaultGroup = db.getGroup(group);    
        db.setDefaultGroup(experimenter, defaultGroup);          
              
    }

    public void deleteExperimenter(Long id) {
        db.deleteExperimenter(id);
    }

    private void sort(Comparator comparator) {
        Collections.sort(experimenters, comparator);
    }
    
}
