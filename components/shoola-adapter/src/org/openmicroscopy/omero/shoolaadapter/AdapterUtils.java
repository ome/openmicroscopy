/*
 * Created on May 31, 2005
 */
package org.openmicroscopy.omero.shoolaadapter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.DatasetAnnotation;
import org.openmicroscopy.omero.model.Experimenter;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.ImageDimension;
import org.openmicroscopy.omero.model.ImagePixel;
import org.openmicroscopy.omero.model.ModuleExecution;
import org.openmicroscopy.omero.model.Project;

import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;

/**
 * @author josh
 */
public class AdapterUtils {

    static public ProjectData go(Project p) {
        return go(p, newCache());
    }

    static public ProjectData go(Project p, Map cache) {

        if (check(cache,p)) {
            return (ProjectData) from(cache, p);
        }

        ProjectData pd = new ProjectData();
        to(cache, p, pd);
        
        pd.setID(p.getProjectId().intValue());
        pd.setName(p.getName());
        pd.setDescription(p.getDescription());
        pd.setOwnerFirstName(p.getExperimenter().getFirstname());
        //TODO

        Set set = new HashSet();
        for (Iterator i = p.getDatasets().iterator(); i.hasNext();) {
            Dataset d = (Dataset) i.next();
            set.add(go(d,cache));
        }
        pd.setDatasets(new ArrayList(set));

        return pd;
    }

    static public DatasetData go(Dataset d) {
        return go(d, newCache());
    }

    static public DatasetData go(Dataset d, Map cache) {

        if (check(cache, d)) {
            return (DatasetData) from(cache, d);
        }

        DatasetData dd = new DatasetData();
        to(cache, d, dd);

        dd.setID(d.getDatasetId().intValue());
        dd.setName(d.getName());
        dd.setDescription(d.getDescription());
        dd.setOwnerFirstName(d.getExperimenter().getFirstname());
        //TODO

        Set set = new HashSet();
        for (Iterator i = d.getImages().iterator(); i.hasNext();) {
              Image img = (Image) i.next();
              set.add(go(img,cache));
        }

        dd.setImages(new ArrayList(set));
        return dd;
    }

    static public CategoryGroupData go(CategoryGroup cg) {
        return go(cg, newCache());
    }
    
    static public CategoryGroupData go(CategoryGroup cg, Map cache) {

        if (check(cache, cg)) {
            return (CategoryGroupData) from(cache, cg);
        }

        CategoryGroupData cgd = new CategoryGroupData();
        to(cache, cg, cgd);
        
        cgd.setID(cg.getAttributeId().intValue());
        cgd.setDescription(cg.getDescription());
        cgd.setName(cg.getName());
        // TODO
        
        return cgd;
    }

    static public CategoryData go(Category c) {
        return go(c, newCache());
    }
    
    static public CategoryData go(Category c, Map cache){


        if (check(cache, c)) {
            return (CategoryData) from(cache, c);
        }

        CategoryData cd = new CategoryData();
        to(cache, c, cd);
        
        cd.setID(c.getAttributeId().intValue());
        cd.setName(c.getName());
        cd.setDescription(c.getDescription());
        //TODO
        
        return cd;
    }

    static public ImageData go(Image img) {
        return go(img, newCache());
    }

    static public ImageData go(Image img, Map cache) {

        if (check(cache, img)) {
            return (ImageData) from(cache, img);
        }

        ImageData id = new ImageData();
        to(cache, img, id);
        
        id.setID(img.getImageId().intValue());
        id.setName(img.getName());
        id.setDescription(img.getDescription());
        id.setInserted(convertDate(img.getInserted()));
        id.setCreated(convertDate(img.getCreated()));
        id.setOwnerID(img.getExperimenter().getAttributeId().intValue());
        //TODO

        Set set = new HashSet();
        for (Iterator i = img.getImagePixels().iterator(); i.hasNext();) {
                ImagePixel p = (ImagePixel) i.next();
                set.add(go(p,cache));
        }
        id.setPixels(new ArrayList(set));
        
        return id;
    }

    static public PixelsDescription go(ImagePixel ip, Map cache) {

        if (check(cache, ip)) {
            return (PixelsDescription) from(cache, ip);
        }

        ImageDimension dim = new ImageDimension(); // FIXME =
        //ip.getImage(). getImageDimensions();
        dim.setPixelSizeX((new Float(0))); //XXX
        dim.setPixelSizeY((new Float(0))); //XXX
        dim.setPixelSizeZ((new Float(0))); //XXX

        PixelsDescription pd = new PixelsDescription();
        to(cache, ip, pd);
        
        pd.setID(ip.getAttributeId().intValue());
        pd.setImageServerID(ip.getImageServerId().longValue());
        //pd.setImageServerUrl(ip.getRepository().getImageServerUrl()); TODO
        pd.setPixelSizeX(dim.getPixelSizeX().doubleValue());
        pd.setPixelSizeY(dim.getPixelSizeY().doubleValue());
        pd.setPixelSizeZ(dim.getPixelSizeZ().doubleValue());
        pd.setPixelType(ip.getPixelType());
        pd.setSizeC(ip.getSizeC().intValue());
        pd.setSizeT(ip.getSizeT().intValue());
        pd.setSizeX(ip.getSizeX().intValue());
        pd.setSizeY(ip.getSizeY().intValue());
        pd.setSizeZ(ip.getSizeZ().intValue());

        return pd;
    }

    static public UserDetails go(Experimenter e, Map cache) {

        if (check(cache, e)) {
            return (UserDetails) from(cache, e);
        }

        List groups = new ArrayList();
        //groups.add(e.getGroup().getAttributeId());//FIXME
        UserDetails ed = new UserDetails(e.getAttributeId().intValue(),e.getFirstname(),e.getLastname(),groups);
        to(cache, e, ed);

        //TODO
        
        return ed;
    }

    static public AnnotationData go(ImageAnnotation ann) {
        return go(ann, newCache());
    }
    
    static public AnnotationData go(ImageAnnotation ann, Map cache) {

        if (check(cache, ann)) {
            return (AnnotationData) from(cache, ann);
        }

        ModuleExecution mex = ann.getModuleExecution();
        AnnotationData ad = new AnnotationData(ann.getAttributeId().intValue(),
                mex.getExperimenter().getAttributeId().intValue(),
                convertDate(mex.getTimestamp()));
        to(cache, ann, ad);
        
        ad.setOwnerFirstName(ann.getModuleExecution().getExperimenter().getFirstname());
        ad.setOwnerLastName(ann.getModuleExecution().getExperimenter().getLastname());
        ad.setAnnotation(ann.getContent());
        //TODO

        return ad;
    }
    
    static public AnnotationData go(DatasetAnnotation ann) {
        return go(ann, newCache());
    }
    
    static public AnnotationData go(DatasetAnnotation ann, Map cache) {

        if (check(cache, ann)) {
            return (AnnotationData) from(cache, ann);
        }

        ModuleExecution mex = ann.getModuleExecution();
        AnnotationData ad = new AnnotationData(ann.getAttributeId().intValue(),
                mex.getExperimenter().getAttributeId().intValue(),
                convertDate(mex.getTimestamp()));
        to(cache, ann, ad);
        
        ad.setOwnerFirstName(ann.getModuleExecution().getExperimenter().getFirstname());
        ad.setOwnerLastName(ann.getModuleExecution().getExperimenter().getLastname());
        ad.setAnnotation(ann.getContent());
        //TODO
        
        return ad;
    }

    static public Timestamp convertDate(Date date){
        return null == date ? null : new Timestamp(date.getTime());
    }
    
    static Map newCache() {
        Map m = new HashMap();
        m.put(null,null); // This keeps things from getting hairy
        return m;
    }

    static boolean check(Map cache, Object key) {
        if (cache.containsKey(key)) {
            return true;
        }
        return false;
    }

    static Object from(Map cache, Object key) {
        return cache.get(key);
    }

    static void to(Map cache, Object key, Object value) {
        // FIXME Why are there NULLs in the Cache!!!!
        cache.put(key, value);
        // TODO return either original value or a new one -- tough. Not when all
        // inherit from a single DataObject!!??
    }

   
//    SessionFactory sessions;
//
//    static final ThreadLocal cache = new ThreadLocal();
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#loadPDIHierarchy(java.lang.Class,
//     *      int)
//     */
//    public DataObject loadPDIHierarchy(final Class arg0, final int arg1) {
//
//        // CONTRACT
//        if (!ProjectData.class.equals(arg0) && !DatasetData.class.equals(arg0)) {
//            throw new IllegalArgumentException(
//                    "Class parameter for loadPDIHierarchy() must be ProjectData or DatasetData.");
//        }
//
//        HibernateTemplate ht = new HibernateTemplate(sessions);
//        return (DataObject) ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//
//                newCache();
//
//                Query q = session.getNamedQuery("PDI_by_" + arg0.getName());
//                q.setLong("id", arg1);
//
//                DataObject dobj;
//                if (arg0.equals(DatasetData.class)) {
//                    Dataset result = (Dataset) q.uniqueResult();
//                    if (null == result)
//                        return null;
//                    dobj = go(result);
//                } else {
//                    Project result = (Project) q.uniqueResult();
//                    if (null == result)
//                        return null;
//                    dobj = go(result);
//                }
//
//                emptyCache();
//                return dobj;
//            }
//        });
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#loadCGCIHierarchy(java.lang.Class,
//     *      int)
//     */
//    public DataObject loadCGCIHierarchy(final Class arg0, final int arg1) {
//
//        // CONTRACT
//        if (!CategoryGroupData.class.equals(arg0)
//                && !CategoryData.class.equals(arg0)) {
//            throw new IllegalArgumentException(
//                    "Class parameter for loadCGCIHierarchy() must be CategoryGroupData or CategoryData.");
//        }
//
//        HibernateTemplate ht = new HibernateTemplate(sessions);
//        return (DataObject) ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//
//                newCache();
//
//                Query q = session.getNamedQuery("CGCI_by_" + arg0.getName());
//                q.setLong("id", arg1);
//
//                DataObject dobj;
//                if (arg0.equals(CategoryData.class)) {
//                    Category result = (Category) q.uniqueResult();
//                    if (null == result)
//                        return null;
//                    dobj = go(result);
//                } else {
//                    CategoryGroup result = (CategoryGroup) q.uniqueResult();
//                    if (null == result)
//                        return null;
//                    dobj = go(result);
//                }
//
//                emptyCache();
//                return dobj;
//            }
//        });
//
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findPDIHierarchies(java.util.Set)
//     */
//    public Set findPDIHierarchies(final Set arg0) {
//
//        // CONTRACT
//        if (null == arg0 || arg0.size() == 0) {
//            return new HashSet();
//        }
//
//        HibernateTemplate ht = new HibernateTemplate(sessions);
//        return (Set) ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//
//                newCache();
//
//                //QUERY
//                Query q = session.getNamedQuery("findPDI");
//                q.setParameterList("img_list", arg0, new IntegerType());
//
//                List result = q.list();
//                Set imagesAll = new HashSet(result);
//
//                if (null == imagesAll || imagesAll.size() == 0) {
//                    return new HashSet();
//                }
//
//                // LOGIC
//                // This is all possible because of the use of the cache!
//                Set hierarchies = new HashSet();
//                Iterator i = imagesAll.iterator();
//                while (i.hasNext()) {
//                    Image img = (Image) i.next();
//                    ImageData id = go(img, false); // Start using *Data to
//                    // prevent lazy loading
//                    Set datasets = img.getDatasets();
//
//                    if (datasets == null || datasets.size() < 1) {
//                        hierarchies.add(id);
//                    } else {
//                        Iterator d = datasets.iterator();
//                        while (d.hasNext()) {
//                            Dataset ds = (Dataset) d.next();
//                            DatasetData dd = go(ds, false);
//
//                            // Add images (since not resolved because
//                            // go(*,false);
//                            if (null == dd.images)
//                                dd.images = new HashSet();
//                            dd.images.add(id);
//
//                            Set projects = ds.getProjects();
//                            if (projects == null || projects.size() < 1) {
//                                hierarchies.add(dd);
//                            } else {
//                                Iterator p = projects.iterator();
//                                while (p.hasNext()) {
//                                    Project prj = (Project) p.next();
//                                    ProjectData pd = go(prj, false);
//
//                                    // Add datsets (since not resolved because
//                                    // go(*,false)
//                                    if (null == pd.datasets)
//                                        pd.datasets = new HashSet();
//                                    pd.datasets.add(dd);
//
//                                    hierarchies.add(pd);
//                                }
//                            }
//
//                        }
//                    }
//                }
//
//                emptyCache();
//                return hierarchies;
//            }
//        });
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findCGCIHierarchies(java.util.Set)
//     */
//    public Set findCGCIHierarchies(final Set arg0) {
//        // CONTRACT
//        if (null == arg0 || arg0.size() == 0) {
//            return new HashSet();
//        }
//
//        HibernateTemplate ht = new HibernateTemplate(sessions);
//        return (Set) ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//
//                newCache();
//
//                //QUERY
//                Query q = session.getNamedQuery("findCGCI");
//                q.setParameterList("img_list", arg0, new IntegerType());
//
//                List result = q.list();
//                Set imagesAll = new HashSet(result);
//
//                if (null == imagesAll || imagesAll.size() == 0) {
//                    return new HashSet();
//                }
//
//                // LOGIC
//                // This is all possible because of the use of the cache!
//                Set hierarchies = new HashSet();
//                Iterator i = imagesAll.iterator();
//                while (i.hasNext()) {
//                    Image img = (Image) i.next();
//                    ImageData id = go(img, false); // Start using *Data to
//                    // prevent lazy loading
//                    Set classifications = img.getClassifications();
//                    Set categories = new HashSet();
//
//                    for (Iterator c = classifications.iterator(); c.hasNext();) {
//                        Classification cla = (Classification) c.next();
//                        if (cla.getValid().booleanValue()) { // TODO do this in query
//                            categories.add(cla.getCategory());
//                        }
//                    }
//
//                    if (categories == null || categories.size() < 1) {
//                        hierarchies.add(id);
//                    } else {
//                        Iterator c = categories.iterator();
//                        while (c.hasNext()) { // OPTIMAL TODO looping over get!!
//                                              // ???
////                          Category ca = (Category) c.next(); // HERE ??
//                            Integer cId = (Integer) c.next();
//                            Category ca = (Category) session.get(Category.class, cId);
//                            CategoryData cd = go(ca, false);
//
//                            // Add images (since not resolved because
//                            // go(*,false);
//                            if (null == cd.images)
//                                cd.images = new HashSet();
//                            cd.images.add(id);
//
//                            Integer cgId = ca.getCategoryGroup(); // and HERE ??
//                                                                  // FIXME
//                            CategoryGroup cg = (CategoryGroup) session.get(CategoryGroup.class, cgId);
//                            // not a
//                            // collection??
//                            if (cg == null) {
//                                hierarchies.add(cd);
//                            } else {
//                                CategoryGroupData cgd = go(cg);
//
//                                // Add categories (since not resolved because
//                                // go(*,false)
//                                    if (null == cgd.categories)
//                                        cgd.categories = new HashSet();
//                                    cgd.categories.add(cd);
//                                    hierarchies.add(cgd);
//                            }
//                        }
//                    }
//                }
//
//                emptyCache();
//                return hierarchies;
//            }
//        });
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findImageAnnotations(java.util.Set)
//     */
//    public Map findImageAnnotations(final Set arg0) {
//
//        // CONTRACT
//        if (null == arg0 || arg0.size() == 0) {
//            return new HashMap();
//        }
//
//        HibernateTemplate ht = new HibernateTemplate(sessions);
//        return (Map) ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//
//                Query q = session.getNamedQuery("findImageAnn");
//                q.setParameterList("img_list", arg0, new IntegerType());
//
//                Map map = findImageAnnotations(q);
//                emptyCache();
//                return map;
//            }
//        });
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findImageAnnotations(java.util.Set,
//     *      int)
//     */
//    public Map findImageAnnotationsForExperimenter(final Set arg0, final int arg1) {
//
//        // CONTRACT
//        if (null == arg0 || arg0.size() == 0) {
//            return new HashMap();
//        }
//
//        HibernateTemplate ht = new HibernateTemplate(sessions);
//        return (Map) ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//
//                Query q = session.getNamedQuery("findImageAnnWithID");
//                q.setParameterList("img_list", arg0, new IntegerType());
//                q.setInteger("expId", arg1);
//
//                Map map = findImageAnnotations(q);
//                emptyCache();
//                return map;
//            }
//        });
//    }
//
//    Map findImageAnnotations(final Query q) {
//        newCache();
//
//        Set result = new HashSet(q.list()); // TODO this everywhere
//
//        if (null == result || result.size() == 0) {
//            return new HashMap();
//        }
//
//        Map map = new HashMap();
//
//        // SORT
//        Iterator i = result.iterator();
//        while (i.hasNext()) {
//            ImageAnnotation ann = (ImageAnnotation) i.next();
//            Integer img_id = ann.getImage().getImageId();
//            if (!map.containsKey(img_id)) {
//                map.put(img_id, new HashSet());
//            }
//            ((Set) map.get(img_id)).add(ann);
//        }
//
//        //FIXME REMOVE INVALID ENTRIES
//
//        //MAP TO DATAOBJECT
//        Map map2 = new HashMap();
//        Iterator i2 = map.keySet().iterator();
//        while (i2.hasNext()) {
//            Object key = i2.next();
//            Set annotations = (Set) map.get(key);
//            Set annotationDataObjects = new HashSet();
//            Iterator i3 = annotations.iterator();
//            while (i3.hasNext()) {
//                annotationDataObjects.add(go((ImageAnnotation) i3.next()));
//            }
//            map2.put(key, annotationDataObjects);
//        }
//
//        return map2;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findDatasetAnnotations(java.util.Set)
//     */
//    public Map findDatasetAnnotations(final Set arg0) {
//
//        // CONTRACT
//        if (null == arg0 || arg0.size() == 0) {
//            return new HashMap();
//        }
//
//        HibernateTemplate ht = new HibernateTemplate(sessions);
//        return (Map) ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//
//                Query q = session.getNamedQuery("findDatasetAnn");
//                q.setParameterList("ds_list", arg0, new IntegerType());
//
//                Map map = findDatasetAnnotations(q);
//                emptyCache();
//                return map;
//            }
//        });
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.openmicroscopy.omero.interfaces.HierarchyBrowsing#findDatasetAnnotations(java.util.Set,
//     *      int)
//     */
//    public Map findDatasetAnnotationsForExperimenter(final Set arg0, final int arg1) {
//
//        // CONTRACT
//        if (null == arg0 || arg0.size() == 0) {
//            return new HashMap();
//        }
//
//        HibernateTemplate ht = new HibernateTemplate(sessions);
//        return (Map) ht.execute(new HibernateCallback() {
//            public Object doInHibernate(Session session)
//                    throws HibernateException {
//
//                Query q = session.getNamedQuery("findDatasetAnnWithID");
//                q.setParameterList("ds_list", arg0, new IntegerType());
//                q.setInteger("expId", arg1);
//
//                Map map = findDatasetAnnotations(q);
//                emptyCache();
//                return map;
//            }
//        });
//    }
//
//    Map findDatasetAnnotations(final Query q) {
//
//        newCache();
//
//        Set result = new HashSet(q.list()); // TODO this everywhere
//
//        if (null == result || result.size() == 0) {
//            return new HashMap();
//        }
//
//        Map map = new HashMap();
//
//        // SORT
//        Iterator i = result.iterator();
//        while (i.hasNext()) {
//            DatasetAnnotation ann = (DatasetAnnotation) i.next();
//            Integer ds_id = ann.getDataset().getDatasetId();
//            if (!map.containsKey(ds_id)) {
//                map.put(ds_id, new HashSet());
//            }
//            ((Set) map.get(ds_id)).add(ann);
//        }
//
//        //FIXME REMOVE INVALID ENTRIES
//
//        //MAP TO DATAOBJECT
//        Map map2 = new HashMap();
//        Iterator i2 = map.keySet().iterator();
//        while (i2.hasNext()) {
//            Object key = i2.next();
//            Set annotations = (Set) map.get(key);
//            Set annotationDataObjects = new HashSet();
//            Iterator i3 = annotations.iterator();
//            while (i3.hasNext()) {
//                annotationDataObjects.add(go((DatasetAnnotation) i3.next()));
//            }
//            map2.put(key, annotationDataObjects);
//        }
//
//        return map2;
//    }
//
//    /**
//     * @return Returns the sessions.
//     */
//    public SessionFactory getSessions() {
//        return sessions;
//    }
//
//    /**
//     * @param sessions
//     *           The sessions to set.
//     */
//    public void setSessions(SessionFactory sessions) {
//        this.sessions = sessions;
//    }

}