/*
 * Created on Feb 19, 2005
 */
package org.openmicroscopy.omero.logic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.IntegerType;

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

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.ProjectData;

/**
 * @author josh
 */
public class HierarchyBrowsingImpl implements HierarchyBrowsing {

    SessionFactory sessions;

    static final ThreadLocal cache = new ThreadLocal();

    /*
     * (non-Javadoc)
     * 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class,
     *      int)
     */
    public DataObject loadPDIHierarchy(final Class arg0, final int arg1) {

        // CONTRACT
        if (!ProjectData.class.equals(arg0) && !DatasetData.class.equals(arg0)) {
            throw new IllegalArgumentException(
                    "Class parameter for loadPDIHierarchy() must be ProjectData or DatasetData.");
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return (DataObject) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                newCache();

                Query q = session.getNamedQuery("PDI_by_" + arg0.getName());
                q.setLong("id", arg1);

                DataObject dobj;
                if (arg0.equals(DatasetData.class)) {
                    Dataset result = (Dataset) q.uniqueResult();
                    if (null == result)
                        return null;
                    dobj = go(result);
                } else {
                    Project result = (Project) q.uniqueResult();
                    if (null == result)
                        return null;
                    dobj = go(result);
                }

                emptyCache();
                return dobj;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class,
     *      int)
     */
    public DataObject loadCGCIHierarchy(final Class arg0, final int arg1) {

        // CONTRACT
        if (!CategoryGroupData.class.equals(arg0)
                && !CategoryData.class.equals(arg0)) {
            throw new IllegalArgumentException(
                    "Class parameter for loadCGCIHierarchy() must be CategoryGroupData or CategoryData.");
        }

        HibernateTemplate ht = new HibernateTemplate(sessions);
        return (DataObject) ht.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {

                newCache();

                Query q = session.getNamedQuery("CGCI_by_" + arg0.getName());
                q.setLong("id", arg1);

                DataObject dobj;
                if (arg0.equals(CategoryData.class)) {
                    Category result = (Category) q.uniqueResult();
                    if (null == result)
                        return null;
                    dobj = go(result);
                } else {
                    CategoryGroup result = (CategoryGroup) q.uniqueResult();
                    if (null == result)
                        return null;
                    dobj = go(result);
                }

                emptyCache();
                return dobj;
            }
        });

    }

    /*
     * (non-Javadoc)
     * 
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

                newCache();

                //QUERY
                Query q = session.getNamedQuery("findPDI");
                q.setParameterList("img_list", arg0, new IntegerType());

                List result = q.list();
                Set imagesAll = new HashSet(result);

                if (null == imagesAll || imagesAll.size() == 0) {
                    return new HashSet();
                }

                // LOGIC
                // This is all possible because of the use of the cache!
                Set hierarchies = new HashSet();
                Iterator i = imagesAll.iterator();
                while (i.hasNext()) {
                    Image img = (Image) i.next();
                    ImageData id = go(img, false); // Start using *Data to
                    // prevent lazy loading
                    Set datasets = img.getDatasets();

                    if (datasets == null || datasets.size() < 1) {
                        hierarchies.add(id);
                    } else {
                        Iterator d = datasets.iterator();
                        while (d.hasNext()) {
                            Dataset ds = (Dataset) d.next();
                            DatasetData dd = go(ds, false);

                            // Add images (since not resolved because
                            // go(*,false);
                            if (null == dd.images)
                                dd.images = new HashSet();
                            dd.images.add(id);

                            Set projects = ds.getProjects();
                            if (projects == null || projects.size() < 1) {
                                hierarchies.add(dd);
                            } else {
                                Iterator p = projects.iterator();
                                while (p.hasNext()) {
                                    Project prj = (Project) p.next();
                                    ProjectData pd = go(prj, false);

                                    // Add datsets (since not resolved because
                                    // go(*,false)
                                    if (null == pd.datasets)
                                        pd.datasets = new HashSet();
                                    pd.datasets.add(dd);

                                    hierarchies.add(pd);
                                }
                            }

                        }
                    }
                }

                emptyCache();
                return hierarchies;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
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

                newCache();

                //QUERY
                Query q = session.getNamedQuery("findCGCI");
                q.setParameterList("img_list", arg0, new IntegerType());

                List result = q.list();
                Set imagesAll = new HashSet(result);

                if (null == imagesAll || imagesAll.size() == 0) {
                    return new HashSet();
                }

                // LOGIC
                // This is all possible because of the use of the cache!
                Set hierarchies = new HashSet();
                Iterator i = imagesAll.iterator();
                while (i.hasNext()) {
                    Image img = (Image) i.next();
                    ImageData id = go(img, false); // Start using *Data to
                    // prevent lazy loading
                    Set classifications = img.getClassifications();
                    Set categories = new HashSet();

                    for (Iterator c = classifications.iterator(); c.hasNext();) {
                        Classification cla = (Classification) c.next();
                        if (cla.getValid().booleanValue()) { // TODO do this in query
                            categories.add(cla.getCategory());
                        }
                    }

                    if (categories == null || categories.size() < 1) {
                        hierarchies.add(id);
                    } else {
                        Iterator c = categories.iterator();
                        while (c.hasNext()) { // OPTIMAL TODO looping over get!!
                                              // ???
//                          Category ca = (Category) c.next(); // HERE ??
                            Integer cId = (Integer) c.next();
                            Category ca = (Category) session.get(Category.class, cId);
                            CategoryData cd = go(ca, false);

                            // Add images (since not resolved because
                            // go(*,false);
                            if (null == cd.images)
                                cd.images = new HashSet();
                            cd.images.add(id);

                            Integer cgId = ca.getCategoryGroup(); // and HERE ??
                                                                  // FIXME
                            CategoryGroup cg = (CategoryGroup) session.get(CategoryGroup.class, cgId);
                            // not a
                            // collection??
                            if (cg == null) {
                                hierarchies.add(cd);
                            } else {
                                CategoryGroupData cgd = go(cg);

                                // Add categories (since not resolved because
                                // go(*,false)
                                    if (null == cgd.categories)
                                        cgd.categories = new HashSet();
                                    cgd.categories.add(cd);
                                    hierarchies.add(cgd);
                            }
                        }
                    }
                }

                emptyCache();
                return hierarchies;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
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
                emptyCache();
                return map;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findImageAnnotations(java.util.Set,
     *      int)
     */
    public Map findImageAnnotationsForExperimenter(final Set arg0, final int arg1) {

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
                emptyCache();
                return map;
            }
        });
    }

    Map findImageAnnotations(final Query q) {
        newCache();

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

        //MAP TO DATAOBJECT
        Map map2 = new HashMap();
        Iterator i2 = map.keySet().iterator();
        while (i2.hasNext()) {
            Object key = i2.next();
            Set annotations = (Set) map.get(key);
            Set annotationDataObjects = new HashSet();
            Iterator i3 = annotations.iterator();
            while (i3.hasNext()) {
                annotationDataObjects.add(go((ImageAnnotation) i3.next()));
            }
            map2.put(key, annotationDataObjects);
        }

        return map2;
    }

    /*
     * (non-Javadoc)
     * 
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
                emptyCache();
                return map;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findDatasetAnnotations(java.util.Set,
     *      int)
     */
    public Map findDatasetAnnotationsForExperimenter(final Set arg0, final int arg1) {

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
                emptyCache();
                return map;
            }
        });
    }

    Map findDatasetAnnotations(final Query q) {

        newCache();

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

        //MAP TO DATAOBJECT
        Map map2 = new HashMap();
        Iterator i2 = map.keySet().iterator();
        while (i2.hasNext()) {
            Object key = i2.next();
            Set annotations = (Set) map.get(key);
            Set annotationDataObjects = new HashSet();
            Iterator i3 = annotations.iterator();
            while (i3.hasNext()) {
                annotationDataObjects.add(go((DatasetAnnotation) i3.next()));
            }
            map2.put(key, annotationDataObjects);
        }

        return map2;
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

    // NON PUBLIC METHOD //
    // ------------------------------------------------------

    ProjectData go(Project p) {
        return go(p, true);
    }

    ProjectData go(Project p, boolean resolve) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Prj) *****");
        if (checkCache(p)) {
            return (ProjectData) fromCache(p);
        }

        ProjectData pd = new ProjectData();
        toCache(p, pd);
        
        pd.id = p.getProjectId().intValue();
        pd.name = p.getName();
        pd.description = p.getDescription();
        pd.owner = go(p.getExperimenter());

        Set set = new HashSet();
        if (resolve) {
            for (Iterator i = p.getDatasets().iterator(); i.hasNext();) {
                Dataset d = (Dataset) i.next();
                set.add(go(d));
            }
        }
        pd.datasets = set;

        return pd;
    }

    DatasetData go(Dataset d) {
        return go(d, true);
    }

    DatasetData go(Dataset d, boolean resolve) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Ds) *****");
        if (checkCache(d)) {
            return (DatasetData) fromCache(d);
        }

        DatasetData dd = new DatasetData();
        toCache(d, dd);

        dd.id = d.getDatasetId().intValue();
        dd.name = d.getName();
        dd.description = d.getDescription();
        dd.owner = go(d.getExperimenter());

        Set set = new HashSet();
        if (resolve) {
            for (Iterator i = d.getImages().iterator(); i.hasNext();) {
                Image img = (Image) i.next();
                set.add(go(img));
            }
        }

        dd.images = set;
        return dd;
    }

    CategoryGroupData go(CategoryGroup cg) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(CG) *****");
        if (checkCache(cg)) {
            return (CategoryGroupData) fromCache(cg);
        }

        CategoryGroupData cgd = new CategoryGroupData();
        toCache(cg, cgd);
        
        cgd.id = cg.getAttributeId().intValue();
        cgd.description = cg.getDescription();
        cgd.name = cg.getName();
        cgd.owner = go(cg.getModuleExecution().getExperimenter());
        // FIXMEcgd.categories = cg.

        
        return cgd;
    }

    CategoryData go(Category c) {
        return go(c,true);
    }
    
    CategoryData go(Category c, boolean resolve){
        System.out.println(" ***** HierarchyBrowsingImpl.go(Cat) *****");

        if (checkCache(c)) {
            return (CategoryData) fromCache(c);
        }

        CategoryData cd = new CategoryData();
        toCache(c, cd);
        
        cd.id = c.getAttributeId().intValue();
        cd.name = c.getName();
        cd.description = c.getDescription();
        cd.owner = go(c.getModuleExecution().getExperimenter());
        
        return cd;
    }

    ImageData go(Image img) {
        return go(img, true);
    }

    ImageData go(Image img, boolean resolve) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Img) *****");
        if (checkCache(img)) {
            return (ImageData) fromCache(img);
        }

        ImageData id = new ImageData();
        toCache(img, id);
        
        id.id = img.getImageId().intValue();
        id.name = img.getName();
        id.description = img.getDescription();
        id.inserted = new Timestamp(img.getInserted().getTime());
        id.created = new Timestamp(img.getCreated().getTime());
        id.owner = go(img.getExperimenter());
        id.defaultPixels = go(img.getPixels());

        Set set = new HashSet();
        if (resolve) {
            for (Iterator i = img.getImagePixels().iterator(); i.hasNext();) {
                ImagePixel p = (ImagePixel) i.next();
                set.add(go(p));
            }
        }
        id.allPixels = set;
        
        return id;
    }

    PixelsData go(ImagePixel ip) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Pix) *****");
        if (checkCache(ip)) {
            return (PixelsData) fromCache(ip);
        }

        ImageDimension dim = new ImageDimension(); // FIXME =
        //ip.getImage(). getImageDimensions();
        dim.setPixelSizeX((new Float(0))); //XXX
        dim.setPixelSizeY((new Float(0))); //XXX
        dim.setPixelSizeZ((new Float(0))); //XXX

        PixelsData pd = new PixelsData();
        toCache(ip, pd);
        
        pd.id = ip.getAttributeId().intValue();
        pd.image = go(ip.getImage());
        pd.imageServerID = ip.getImageServerId().longValue();
        pd.imageServerURL = ip.getRepository().getImageServerUrl();
        pd.pixelSizeX = dim.getPixelSizeX().doubleValue();
        pd.pixelSizeY = dim.getPixelSizeY().doubleValue();
        pd.pixelSizeZ = dim.getPixelSizeZ().doubleValue();
        //pd.pixelType = ip.getPixelType();//FIXME List SEE STATIC FIELDS
        pd.sizeC = ip.getSizeC().intValue();
        pd.sizeT = ip.getSizeT().intValue();
        pd.sizeX = ip.getSizeX().intValue();
        pd.sizeY = ip.getSizeY().intValue();
        pd.sizeZ = ip.getSizeZ().intValue();

        return pd;
    }

    ExperimenterData go(Experimenter e) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Exp) *****");
        if (checkCache(e)) {
            return (ExperimenterData) fromCache(e);
        }

        ExperimenterData ed = new ExperimenterData();
        toCache(e, ed);
        
        ed.id = e.getAttributeId().intValue();
        ed.firstName = e.getFirstname();
        ed.lastName = e.getLastname();
        ed.email = e.getEmail();
        ed.institution = e.getInstitution();
        ed.groupID = e.getGroup().getAttributeId().intValue();
        ed.groupName = e.getGroup().getName();
        
        return ed;
    }

    AnnotationData go(ImageAnnotation ann) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(IAnn) *****");
        if (checkCache(ann)) {
            return (AnnotationData) fromCache(ann);
        }

        AnnotationData ad = new AnnotationData();
        toCache(ann, ad);
        
        ad.id = ann.getAttributeId().intValue();
        ad.owner = go(ann.getModuleExecution().getExperimenter());
        /*
         * STs have FX constraints on the 'explicit containers' (images,
         * datasets, features), but nothing else. We were going to add those to
         * the new release (goes hand in hand with not null constraints on the
         * attribute's mex FK)
         */
        ad.text = ann.getContent();
        ad.lastModified = go(ann.getModuleExecution().getTimestamp());
        ad.annotatedObject = go(ann.getImage());

        return ad;
    }
    
    Timestamp go(Date date){
        return null == date ? null : new Timestamp(date.getTime());
    }

    AnnotationData go(DatasetAnnotation ann) {
System.out.println(" ***** HierarchyBrowsingImpl.go(Ann) *****");
        if (checkCache(ann)) {
            return (AnnotationData) fromCache(ann);
        }

        AnnotationData ad = new AnnotationData();
        toCache(ann, ad);
        
        ad.id = ann.getAttributeId().intValue();
        ad.owner = go(ann.getModuleExecution().getExperimenter());
        ad.text = ann.getContent();
        ad.lastModified = go(ann.getModuleExecution().getTimestamp());
        ad.annotatedObject = go(ann.getDataset());

        return ad;
    }

    void newCache() {
        Map m = new HashMap();
        m.put(null,null); // This keeps things from getting hairy
        cache.set(m);
    }

    void emptyCache() {
        //TODO Need to make sure cache is emptied on exception
        cache.set(null);
    }

    boolean checkCache(Object key) {
        if (((Map) cache.get()).containsKey(key)) {
            return true;
        }
        return false;
    }

    Object fromCache(Object key) {
        return ((Map) cache.get()).get(key);
    }

    void toCache(Object key, Object value) {
        // FIXME Why are there NULLs in the Cache!!!!
        ((Map) cache.get()).put(key, value);
        // TODO return either original value or a new one -- tough. Not when all
        // inherit from a single DataObject!!??
    }

}