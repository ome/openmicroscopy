/*
 * org.openmicroscopy.omero.logic.HierarchyBrowsingImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.omero.logic;

//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.IntegerType;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

//Application-internal dependencies
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Classification;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.DatasetAnnotation;
import org.openmicroscopy.omero.model.Experimenter;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.ImageDimension;
import org.openmicroscopy.omero.model.ImagePixel;
import org.openmicroscopy.omero.model.Project;
import org.openmicroscopy.omero.model2.Category2;
import org.openmicroscopy.omero.model2.CategoryGroup2;

/**
 * implementation of the HierarchyBrowsing service. A single service
 * object is configured through IoC (most likely by Spring) and is
 * available for all calls.
 * @DEV.TODO Hibernate dependencies should be pushed down into DAOs 
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 1.0
 */
public class HierarchyBrowsingImpl implements HierarchyBrowsing {

    /** central Hibernate object 
     * @DEV.TODO this needs to be moved to DAOs use constructor not get/setters 
     */
    SessionFactory sessions;

    /**
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class, int)
     */
    public Object loadPDIHierarchy(final Class arg0, final int arg1) {

        // CONTRACT
        if (!Project.class.equals(arg0) && !Dataset.class.equals(arg0)) {
            throw new IllegalArgumentException(
                    "Class parameter for loadPDIHierarchy() must be Project or Dataset.");
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("PDI_by_"
                        + arg0.getName().substring(
                                arg0.getPackage().getName().length() + 1));
                q.setLong("id", arg1);
                return q.uniqueResult();
            }
        });
    }

    /**
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class,int)
     */
    public Object loadCGCIHierarchy(final Class arg0, final int arg1) {

        // CONTRACT
        if (!CategoryGroup.class.equals(arg0) && !Category.class.equals(arg0)) {
            throw new IllegalArgumentException(
                    "Class parameter for loadCGCIHierarchy() must be CategoryGroup or Category.");
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("CGCI_by_"
                        + arg0.getName().substring(
                                arg0.getPackage().getName().length() + 1));
                q.setLong("id", arg1);
                return q.uniqueResult();
            }
        });

    }

    /**
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(final Set arg0) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashSet();
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return (Set) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                //QUERY
                Query q = session.getNamedQuery("findPDI");
                q.setParameterList("img_list", arg0, new IntegerType());

                List result = q.list();
                Set imagesAll = new HashSet(result);

                if (null == imagesAll || imagesAll.size() == 0) {
                    return new HashSet();
                }

                // LOGIC
                Set hierarchies = new HashSet();
                Iterator i = imagesAll.iterator();
                while (i.hasNext()) {
                    Image img = (Image) i.next();
                    Set datasets = img.getDatasets();

                    if (datasets == null || datasets.size() < 1) {
                        hierarchies.add(img);
                    } else {
                        Iterator d = datasets.iterator();
                        while (d.hasNext()) {
                            Dataset ds = (Dataset) d.next();

                            if (!(ds.getImages() instanceof HashSet))
                                ds.setImages(new HashSet());
                            ds.getImages().add(img);

                            Set projects = ds.getProjects();
                            if (projects == null || projects.size() < 1) {
                                hierarchies.add(ds);
                            } else {
                                Iterator p = projects.iterator();
                                while (p.hasNext()) {
                                    Project prj = (Project) p.next();

                                    if (!(prj.getDatasets() instanceof HashSet))
                                        prj.setDatasets(new HashSet());
                                    prj.getDatasets().add(ds);

                                    hierarchies.add(prj);
                                }
                            }

                        }
                    }
                }

                return hierarchies;
            }

        });
    }

    /** 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(final Set arg0) {
        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashSet();
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return (Set) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                //QUERY
                Query q = session.getNamedQuery("findCGCI");
                q.setParameterList("img_list", arg0, new IntegerType());

                List result = q.list();
                Set imagesAll = new HashSet(result);

                if (null == imagesAll || imagesAll.size() == 0) {
                    return new HashSet();
                }

                // LOGIC
                Set hierarchies = new HashSet();
                Iterator i = imagesAll.iterator();
                while (i.hasNext()) {
                    Image img = (Image) i.next();
                    Set classifications = img.getClassifications();
                    Set categories = new HashSet();

                    for (Iterator c = classifications.iterator(); c.hasNext();) {
                        Classification cla = (Classification) c.next();
                        if (cla.getValid().booleanValue()) { // TODO do this in query
                            categories.add(cla.getCategory());
                        }
                    }

                    if (categories == null || categories.size() < 1) {
                        hierarchies.add(img);
                    } else {
                        Iterator c = categories.iterator();
                        while (c.hasNext()) { // OPTIMAL TODO looping over get!!
                            // ???
                            Integer cId = (Integer) c.next();
                            Category2 ca = (Category2) session.get(
                                    Category.class, cId); // TODO hql

                            if (null == ca.images)
                                ca.images = new HashSet();
                            ca.images.add(img);

                            Integer cgId = ca.getCategoryGroup(); // and HERE ??
                            // FIXME
                            CategoryGroup2 cg = (CategoryGroup2) session.get(
                                    CategoryGroup.class, cgId);
                            // not a
                            // collection??
                            if (cg == null) {
                                hierarchies.add(ca);
                            } else {
                                if (null == cg.categories)
                                    cg.categories = new HashSet();
                                cg.categories.add(ca);
                                hierarchies.add(cg);
                            }
                        }
                    }
                }

                return hierarchies;
            }
        });
    }

    /** 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(final Set arg0) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return (Map) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findImageAnn");
                q.setParameterList("img_list", arg0, new IntegerType());

                Map map = findImageAnnotations(q);
                return map;
            }
        });
    }

    /** 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findImageAnnotations(java.util.Set,int)
     */
    public Map findImageAnnotationsForExperimenter(final Set arg0,
            final int arg1) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return (Map) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findImageAnnWithID");
                q.setParameterList("img_list", arg0, new IntegerType());
                q.setInteger("expId", arg1);

                Map map = findImageAnnotations(q);
                return map;
            }
        });
    }

    Map findImageAnnotations(final Query q) {

        Set result = new HashSet(q.list()); // TODO this everywhere

        if (null == result || result.size() == 0) {
            return new HashMap();
        }

        Map map = new HashMap();

        // SORT
        Iterator i = result.iterator();
        while (i.hasNext()) {
            ImageAnnotation ann = (ImageAnnotation) i.next();
            Integer img_id = ann.getImage().getImageId();
            if (!map.containsKey(img_id)) {
                map.put(img_id, new HashSet());
            }
            ((Set) map.get(img_id)).add(ann);
        }

        //FIXME REMOVE INVALID ENTRIES

        return map;
    }

    /** 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findDatasetAnnotations(java.util.Set)
     */
    public Map findDatasetAnnotations(final Set arg0) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return (Map) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findDatasetAnn");
                q.setParameterList("ds_list", arg0, new IntegerType());

                Map map = findDatasetAnnotations(q);
                return map;
            }
        });
    }

    /** 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findDatasetAnnotations(java.util.Set, int)
     */
    public Map findDatasetAnnotationsForExperimenter(final Set arg0,
            final int arg1) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return (Map) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                Query q = session.getNamedQuery("findDatasetAnnWithID");
                q.setParameterList("ds_list", arg0, new IntegerType());
                q.setInteger("expId", arg1);

                Map map = findDatasetAnnotations(q);
                return map;
            }
        });
    }

    Map findDatasetAnnotations(final Query q) {

        Set result = new HashSet(q.list()); // TODO this everywhere

        if (null == result || result.size() == 0) {
            return new HashMap();
        }

        Map map = new HashMap();

        // SORT
        Iterator i = result.iterator();
        while (i.hasNext()) {
            DatasetAnnotation ann = (DatasetAnnotation) i.next();
            Integer ds_id = ann.getDataset().getDatasetId();
            if (!map.containsKey(ds_id)) {
                map.put(ds_id, new HashSet());
            }
            ((Set) map.get(ds_id)).add(ann);
        }

        //FIXME REMOVE INVALID ENTRIES

        return map;
    }

    /**
     * @return Returns the sessions.
     */
    public SessionFactory getSessions() {
        return sessions;
    }

    /**
     * @param sessions
     *           The sessions to set.
     */
    public void setSessions(SessionFactory sessions) {
        this.sessions = sessions;
    }

}