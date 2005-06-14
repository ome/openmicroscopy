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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;

//Application-internal dependencies
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Classification;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.DatasetAnnotation;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.Project;
import org.openmicroscopy.omero.model2.Category2;
import org.openmicroscopy.omero.model2.CategoryGroup2;


/**
 * implementation of the HierarchyBrowsing service. A single service
 * object is configured through IoC (most likely by Spring) and is
 * available for all calls.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 1.0
 */
public class HierarchyBrowsingImpl implements HierarchyBrowsing {

    private static Log log = LogFactory.getLog(HierarchyBrowsingImpl.class);

    AnnotationDao annotationDao;

    ContainerDao containerDao;
    
    public void setAnnotationDao(AnnotationDao dao) {
        this.annotationDao = dao;
    }

    public void setContainerDao(ContainerDao dao) {
        this.containerDao = dao;
    }

    /**
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class, int)
     */
    public Object loadPDIHierarchy(final Class arg0, final int arg1) {

        // CONTRACT
        if (!Project.class.equals(arg0) && !Dataset.class.equals(arg0)) {
            throw new IllegalArgumentException(
                    "Class parameter for loadPDIHierarchy() must be Project or Dataset, not "+arg0);
        }

        return clean(containerDao.loadHierarchy(arg0, arg1));

    }

    /**
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class,int)
     */
    public Object loadCGCIHierarchy(final Class arg0, final int arg1) {

        // CONTRACT
        if (!CategoryGroup.class.equals(arg0) && !Category.class.equals(arg0)) {
            throw new IllegalArgumentException(
                    "Class parameter for loadCGCIHierarchy() must be CategoryGroup or Category, not "+arg0);
        }

        return clean(containerDao.loadHierarchy(arg0, arg1));

    }

    /**
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
     */
    public Set findPDIHierarchies(final Set arg0) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashSet();
        }

        List result = containerDao.findPDIHierarchies(arg0);
        Set imagesAll = new HashSet(result);

        if (null == imagesAll || imagesAll.size() == 0) {
            if (log.isDebugEnabled()){
                log.debug("findPDIHierarchies() -- no results found:\n"+
                        arg0.toString());
            }
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

        return (Set) clean(hierarchies);

    }

    /** 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
     */
    public Set findCGCIHierarchies(final Set arg0) {
        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashSet();
        }

        List result = containerDao.findCGCIHierarchies(arg0);
        Set imagesAll = new HashSet(result);

        if (null == imagesAll || imagesAll.size() == 0) {
            if (log.isDebugEnabled()){
                log.debug("findCGCIHierarchies() -- no results found:\n"+
                        arg0.toString());
            }
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
                categories.add(cla.getCategory());
            }

            if (categories == null || categories.size() < 1) {
                hierarchies.add(img);
            } else {
                Iterator c = categories.iterator();
                while (c.hasNext()) { 
                    Category tmp = (Category) c.next();
                    Category2 ca = new Category2(tmp);

                    if (!(ca.getImages() instanceof HashSet)) //TODO Hack 
                        ca.setImages(new HashSet());
                    ca.getImages().add(img);

                    CategoryGroup cg = ca.getCategoryGroup(); 
                    if (cg == null) {
                        hierarchies.add(ca);
                    } else {
                        if (!(cg.getCategories() instanceof HashSet))
                            cg.setCategories(new HashSet());
                        cg.getCategories().add(ca);
                        hierarchies.add(cg);
                    }
                }
            }
        }

        return (Set) clean(hierarchies);
    }

    /** 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findImageAnnotations(java.util.Set)
     */
    public Map findImageAnnotations(final Set arg0) {

        // CONTRACT
        if (null == arg0 || arg0.size() == 0) {
            return new HashMap();
        }

        List result = annotationDao.findImageAnnotations(arg0);
        
        return (Map) clean(sortImageAnnotations(result));

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

        List result = annotationDao.findImageAnnotationsForExperimenter(arg0,
                arg1);
        return (Map) clean(sortImageAnnotations(result));

    }

    Map sortImageAnnotations(final List l) {

        Set result = new HashSet(l); 

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

        List result = annotationDao.findDataListAnnotations(arg0);
        return (Map) clean(sortDatasetAnnotations(result));

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

        List result = annotationDao.findDataListAnnotationForExperimenter(arg0,
                arg1);
        return sortDatasetAnnotations(result);

    }

    Map sortDatasetAnnotations(final List l) {

        Set result = new HashSet(l); 

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

        return map;
    }

    /** top-level call. If this is not initialized abort */
    Object clean(Object obj){
        if (! Hibernate.isInitialized(obj)){
            throw new IllegalStateException("If the return object is not initialized then we can't send it.");
        }
        Set done = new HashSet();
        hessianClean(obj,done);
        return obj;
    }
       
    /** removes all Hibernate-related code. 
     * @DEV.TODO Currently tests all Objects, should eventually test only Hibernate parent class
     * @param An object to clean of Hibernate code
     * @param Set to catch circular references
     */
    void hessianClean(Object obj, Set done) {
        
        if (null==obj) return;
        if (done.contains(obj)) return;
        done.add(obj);
        
        if (obj instanceof Map) {
            Map map = (Map) obj;
            for (Iterator i = map.values().iterator(); i.hasNext();) {
                Object value = i.next();
                hessianClean(value,done);
            }
        } else if (obj instanceof Set) {
            Set set = (Set) obj;
            for (Iterator i = set.iterator(); i.hasNext();) {
                Object item = i.next();
                hessianClean(item,done);
            }
        } else { 
            Method[] methods = obj.getClass().getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().startsWith("get")){
                    if (0==method.getParameterTypes().length){
                        Object result=invokeGetter(obj,method);
                        if (! Hibernate.isInitialized(result)){
                            Method setter = getSetterForGetter(methods,method);
                            if (null==setter){
                                throw new IllegalStateException("No setter for getter; this will explode");
                            }
                            setToNull(obj,setter);
                        } else {
                            hessianClean(result,done);
                        }
                    }
                }
            }
            
        }
    }
    
    Method getSetterForGetter(Method[] methods, Method getter){
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().startsWith("set")){
                if (method.getName().substring(1).equals(getter.getName().substring(1))){
                    return method;
                }
            }
        }
        return null;
    }
    
    /** call getter and return object. 
     * @DEV.TODO there maybe be cases where an exception is ok
     * @param object on which to call getter
     * @param getter method for some field
     * @return value stored in field
     */
    Object invokeGetter(Object target, Method getter){
        RuntimeException re = new RuntimeException("Error trying to get value.");
        Object result=null;
        try {
            result = getter.invoke(target,new Object[]{});
        } catch (IllegalArgumentException e) {
            re.initCause(e);
            throw re;
        } catch (IllegalAccessException e) {
            re.initCause(e);
            throw re; 
        } catch (InvocationTargetException e) {
            re.initCause(e);
            throw re;
        }
        return result;
    }
    
    void setToNull(Object obj, Method setter){
        RuntimeException re = new RuntimeException("Error trying to set to null."); 

        try {
            setter.invoke(obj,new Object[]{null});
        } catch (IllegalArgumentException e) {
            re.initCause(e);
            throw re;
        } catch (IllegalAccessException e) {
            re.initCause(e);
            throw re;
        } catch (InvocationTargetException e) {
            re.initCause(e);
            throw re;
        }
    }


}