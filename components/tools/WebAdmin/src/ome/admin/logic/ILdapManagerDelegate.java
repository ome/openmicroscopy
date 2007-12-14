/*
 * ome.admin.controller
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.logic;

// Java imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ome.admin.data.ConnectionDB;
import ome.admin.model.User;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import org.apache.commons.beanutils.BeanUtils;

public class ILdapManagerDelegate implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@link java.lang.String}
     */
    private String sortByProperty = "firstName";

    /**
     * {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
     */
    private List<User> experimenters = Collections.EMPTY_LIST;

    private ConnectionDB db;

    /**
     * {@link java.util.Comparator}
     */
    private transient final Comparator propertyAscendingComparator = new Comparator() {
        public int compare(Object object1, Object object2) {
            try {
                String property1 = BeanUtils.getProperty(object1,
                        ILdapManagerDelegate.this.sortByProperty);
                String property2 = BeanUtils.getProperty(object2,
                        ILdapManagerDelegate.this.sortByProperty);

                return property1.toLowerCase().compareTo(
                        property2.toLowerCase());
            } catch (Exception e) {
                return 0;
            }
        }
    };

    /**
     * {@link java.util.Comparator}
     */
    private transient final Comparator propertyDescendingComparator = new Comparator() {
        public int compare(Object object1, Object object2) {
            try {
                String property1 = BeanUtils.getProperty(object1,
                        ILdapManagerDelegate.this.sortByProperty);
                String property2 = BeanUtils.getProperty(object2,
                        ILdapManagerDelegate.this.sortByProperty);

                return property2.toLowerCase().compareTo(
                        property1.toLowerCase());
            } catch (Exception e) {
                return 0;
            }
        }
    };

    /**
     * Creates a new instance of IAdminExperimenterManagerDelegate.
     */
    public ILdapManagerDelegate() {
        db = new ConnectionDB();
    }

    /**
     * Gets {@link java.util.List} of {@link ome.admin.model.User} from the
     * specified file. There is possible to import users from XLS, CSV and XML.
     * Not supported format throws IOException
     * 
     * @return {@link java.util.List}<{@link ome.admin.model.User}>.
     * @throws FileNotFoundException,
     *             IOException
     */
    public List<User> lookupImportingExperimenters(String base, String attr, String value) {
        List<User> mexps = new ArrayList<User>();
        for(Experimenter exp: db.findExperimenters(base, attr, value)) {
            User mexp = new User();
            mexp.setExperimenter(exp);            
            mexp.setDn(exp.retrieve("LDAP_DN").toString());  
            if (db.checkExperimenter(exp.getOmeName())) {                
                mexp.setSelectBooleanCheckboxValue(false);
            } else if (db.checkEmail(exp.getEmail())) {
                mexp.setSelectBooleanCheckboxValue(false);
            } else
                mexp.setSelectBooleanCheckboxValue(true);
            
            mexps.add(mexp);
        }
        this.experimenters = mexps;
        return this.experimenters;
    }

    /**
     * Sort {@link java.util.List} items
     * 
     * @param sortItem
     *            {@link java.lang.String}
     * @param sort
     *            {@link java.lang.String}
     * @return {@link java.util.List}<{@link ome.admin.model.User}>
     * @throws IOException
     * @throws FileNotFoundException
     * @throws Exception
     */
    public List<User> getAndSortItems(String sortItem, String sort) {
        //this.experimenters = lookupImportingExperimenters();
        sortByProperty = sortItem;
        if (sort.equals("asc"))
            sort(propertyAscendingComparator);
        else if (sort.equals("dsc"))
            sort(propertyDescendingComparator);

        return experimenters;
    }

    /**
     * Sort {@link ome.admin.model.User} by {@link java.util.Comparator}
     * 
     * @param comparator
     *            {@link java.util.Comparator}
     */
    private void sort(Comparator comparator) {
        Collections.sort(experimenters, comparator);
    }

    /**
     * Creates {@link ome.model.meta.Experimenter} in database
     * 
     * @param list
     *            {@link java.util.List}<{@link ome.admin.model.User}>
     */
    public void createExperimenters(List<User> list) {

        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            if (!user.isSelectBooleanCheckboxValue()) {
                list.remove(i);
                i--;
            }
        }

        for (User user : this.experimenters) {
            Experimenter experimenter = user.getExperimenter();
            ExperimenterGroup defaultGroup = db.getGroup("default");
            ExperimenterGroup groups = db.getGroup("user");
            Long id = db.createExperimenter(experimenter, defaultGroup, groups);
            db.setDn(id, user.getDn());

        }

    }

}
