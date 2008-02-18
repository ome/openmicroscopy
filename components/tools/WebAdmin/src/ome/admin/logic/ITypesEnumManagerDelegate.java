/*
 * ome.admin.logic
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.logic;

// Java imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Third-party libraries
import javax.faces.context.FacesContext;
import org.apache.commons.beanutils.BeanUtils;

// Application-internal dependencies
import ome.admin.data.ConnectionDB;
import ome.admin.model.Enumeration;
import ome.model.IEnum;

/**
 * Delegate of enumeration mangement.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class ITypesEnumManagerDelegate implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@link java.util.List} of {@link ome.admin.model.Enumeration}
     */
    private List<Enumeration> enums = new ArrayList<Enumeration>();

    /**
     * {@link java.util.List} of {@link ome.admin.model.Enumeration}
     */
    private List<? extends IEnum> entrys = new ArrayList();

    /**
     * {@link java.lang.String} set by "className";
     */
    private String sortByProperty = "className";

    private final static int scrollerSize = Integer.parseInt(FacesContext
            .getCurrentInstance().getExternalContext().getInitParameter(
                    "scrollerSize"));

    /**
     * {@link java.util.Comparator}
     */
    private transient final Comparator propertyAscendingComparator = new Comparator() {
        public int compare(Object object1, Object object2) {
            try {
                String property1 = BeanUtils.getProperty(object1,
                        ITypesEnumManagerDelegate.this.sortByProperty);
                String property2 = BeanUtils.getProperty(object2,
                        ITypesEnumManagerDelegate.this.sortByProperty);

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
                        ITypesEnumManagerDelegate.this.sortByProperty);
                String property2 = BeanUtils.getProperty(object2,
                        ITypesEnumManagerDelegate.this.sortByProperty);

                return property2.toLowerCase().compareTo(
                        property1.toLowerCase());
            } catch (Exception e) {
                return 0;
            }
        }
    };

    /**
     * {@link ome.admin.data.ConnectionDB}
     */
    private ConnectionDB db;

    /**
     * Creates a new instance of ITypesEnumManagerDelegate.
     */
    public ITypesEnumManagerDelegate() {
        db = new ConnectionDB();
        getEnumerationsWithEntries();
    }

    /**
     * Allowes scroller to appear.
     * 
     * @return boolean
     */
    public boolean setScroller() {
        if (this.enums.size() > scrollerSize)
            return true;
        else
            return false;
    }

    /**
     * Gets {@link java.util.List} of {@link ome.admin.model.Enumeration}.
     * 
     * @return {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
     */
    public List<Enumeration> getEnumerationsWithEntries() {
        List<Enumeration> list = new ArrayList<Enumeration>();
        Map map = db.getEnumerationsWithEntries();
        List<IEnum> oryginList = db.getOriginalEnumerations();
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            Class klass = (Class) iter.next();
            List<? extends IEnum> entries = (List<? extends IEnum>) map
                    .get(klass);
            
            Enumeration en = new Enumeration();
            en.setClassName(klass.getName());
            en.setEntryList(entries);
            
            for(IEnum entry:entries) {
                boolean flag = false;
                int c = 0;
                for(IEnum oryginal:oryginList) {
                    if(entry.getClass().equals(oryginal.getClass())) {
                        c++;
                        if(entry.getValue().equals(oryginal.getValue())) {
                            flag = true;
                        }
                    }
                }
                if(c!=entries.size()) {
                    en.setOriginalVales(false);
                } else {
                    en.setOriginalVales(flag);
                }                    
            }  
            list.add(en);
        }
        this.enums = list;
        return this.enums;
    }

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}
     * which was add for select default group list.
     * 
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
    public List<Class<IEnum>> getEnumerations() {
        return db.getEnumerations();
    }

    public List<? extends IEnum> getEntries(Class klass) {
        return (List<? extends IEnum>) db.getEntries(klass);
    }

    /**
     * Adds new object extends IEnum.
     * 
     * @param en
     *            {@link ome.admin.model.Enumeration}
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    public void addEnumeration(Enumeration en) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            IllegalArgumentException, SecurityException,
            InvocationTargetException, NoSuchMethodException {
        Class klass = Class.forName(en.getClassName());
        db.createEnumeration((IEnum) klass.getConstructor(String.class)
                .newInstance(en.getEvent()));
    }

    /**
     * Deletes object extends IEnum
     * 
     * @param en
     *            Object extends IEnum
     */
    public void delEnumeration(IEnum en) {
        db.deleteEnumeration(en);
    }

    public void resetEnumeration(Class klass) throws ClassNotFoundException,
            IllegalArgumentException, SecurityException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException,
            FileNotFoundException, IOException {
        db.resetEnumeration(klass);
    }

    /**
     * Updates list of objects extend IEnum
     * 
     * @param list
     *            {@link java.util.List} of objects extend IEnum
     */
    public void updateEnumerations(List<? extends IEnum> list) {
        db.updateEnumerations(list);
    }

    /**
     * {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
     * 
     * @param sortItem
     *            {@link java.lang.String}.
     * @param sort
     *            {@link java.lang.String}.
     * @return {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
     */
    public List<Enumeration> sortItems(String sortItem, String sort) {
        sortByProperty = sortItem;
        if (sort.equals("asc"))
            sort(propertyAscendingComparator);
        else if (sort.equals("dsc"))
            sort(propertyDescendingComparator);
        return enums;
    }

    /**
     * {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
     * 
     * @param sortItem
     *            {@link java.lang.String}.
     * @param sort
     *            {@link java.lang.String}.
     * @return {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
     */
    public List<Enumeration> getAndSortItems(String sortItem, String sort) {
        this.enums = getEnumerationsWithEntries();
        sortByProperty = sortItem;
        if (sort.equals("asc"))
            sort(propertyAscendingComparator);
        else if (sort.equals("dsc"))
            sort(propertyDescendingComparator);
        return enums;
    }

    /**
     * {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
     * 
     * @param sortItem
     *            {@link java.lang.String}.
     * @param sort
     *            {@link java.lang.String}.
     * @return {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
     */
    public List<? extends IEnum> getAndSortEntrys(String sortItem, String sort,
            List<? extends IEnum> list, Class klass) {
        if (list == null)
            this.entrys = getEntries(klass);
        else if (klass == null)
            this.entrys = list;

        sortByProperty = sortItem;
        if (sort.equals("asc"))
            sort(propertyAscendingComparator);
        else if (sort.equals("dsc"))
            sort(propertyDescendingComparator);
        return entrys;
    }

    /**
     * Sort {@link ome.admin.model.Enumeration} by {@link java.util.Comparator}
     * 
     * @param comparator
     *            {@link java.util.Comparator}.
     */
    private void sort(Comparator comparator) {
        Collections.sort(enums, comparator);
    }

    public boolean checkEnumeration(Class klass, String value) throws Exception {
        return db.checkEnumeration(klass, value);
    }

}
