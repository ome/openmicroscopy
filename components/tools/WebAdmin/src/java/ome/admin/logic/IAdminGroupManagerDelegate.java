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
public class IAdminGroupManagerDelegate implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
    private String sortByProperty = "name";

    private transient final Comparator propertyAscendingComparator = new Comparator() {
            public int compare(Object object1, Object object2) {
                try {
                    String property1 = BeanUtils.getProperty(object1,
                            IAdminGroupManagerDelegate.this.sortByProperty
                        );
                    String property2 = BeanUtils.getProperty(object2,
                            IAdminGroupManagerDelegate.this.sortByProperty
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
                            IAdminGroupManagerDelegate.this.sortByProperty
                        );
                    String property2 = BeanUtils.getProperty(object2,
                            IAdminGroupManagerDelegate.this.sortByProperty
                        );

                    return property2.toLowerCase().compareTo(property1.toLowerCase());
                } catch (Exception e) {
                    return 0;
                }
            }
        };
 
        ConnectionDB db = new ConnectionDB( );
    {
        getGroups();
    }
    
    public ExperimenterGroup getGroupById(Long id) {
        return (ExperimenterGroup) db.getGroup(id);
    }
        
    public List<ExperimenterGroup> getGroups() {
        this.groups = db.lookupGroups();
        return this.groups;
    }
    
    public void addGroup(ExperimenterGroup group) {
        db.createGroup(group);
    }
    public List<ExperimenterGroup> sortItems(String sortItem, String sort) {
        this.groups = getGroups();
        sortByProperty = sortItem;
        if(sort.equals("asc")) sort(propertyAscendingComparator);
        else if(sort.equals("dsc")) sort(propertyDescendingComparator);
        return groups;
    }

    public ExperimenterGroup readGroup(int id) {
        ExperimenterGroup group = (ExperimenterGroup) this.groups.get(id);
        return group;
    }

    public void updateGroup(ExperimenterGroup group) {
        db.updateGroup(group);
    }
    
    public void deleteGroup(Long id) {
        db.deleteGroup(id);
    }

    private void sort(Comparator comparator) {
        Collections.sort(groups, comparator);
    }
    
    
}
