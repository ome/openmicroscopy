/*
 * org.openmicroscopy.omero.shoolaadapter.AdapterUtils
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package org.openmicroscopy.omero.shoolaadapter;

//Java imports
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencie
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.DatasetAnnotation;
import org.openmicroscopy.omero.model.Experimenter;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.ImageAnnotation;
import org.openmicroscopy.omero.model.ImageDimension;
import org.openmicroscopy.omero.model.ImagePixel;
import org.openmicroscopy.omero.model.Project;

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
public class PojoAdapterUtils {

    private static Log log = LogFactory.getLog(PojoAdapterUtils.class);
    private static final String init_time = "cache.initialization_time";
    private static final String cacheError = "Cache not properly initialized.";
     
    static public DataObject adaptLoadedPDIHierarchy(Class rootNodeType, Object result) {
        
        if (rootNodeType.equals(Project.class)){
            return PojoAdapterUtils.go((Project) result, newCache());
        } else if (rootNodeType.equals(Dataset.class)){
            return PojoAdapterUtils.go((Dataset) result, newCache());
        } else {
            throw new IllegalArgumentException("Method only takes Project and Dataset as argument.");
        }
    }

    static public DataObject adaptLoadedCGCIHierarchy(Class rootNodeType, Object result) {

        if (rootNodeType.equals(CategoryGroup.class)){
            return PojoAdapterUtils.go((CategoryGroup) result, newCache());
        } else if (rootNodeType.equals(Category.class)){
            return PojoAdapterUtils.go((Category) result, newCache());
        } else {
            throw new IllegalArgumentException("Method only takes CategoryGroup and Category as argument.");
        }
    }

    static public Set adaptFoundPDIHierarchies(Set result) {

    		Map cache = newCache();
    		Set dataObjects = new HashSet();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof Project) {
                Project prj = (Project) obj;
                dataObjects.add(PojoAdapterUtils.go(prj,cache));
            } else if (obj instanceof Dataset) {
                Dataset ds = (Dataset) obj;
                dataObjects.add(PojoAdapterUtils.go(ds,cache)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(PojoAdapterUtils.go(img,cache));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    static public Set adaptFoundCGCIHierarchies(Set result) {
        
    		Map cache = newCache();
        Set dataObjects = new HashSet();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof CategoryGroup) {
                CategoryGroup cg = (CategoryGroup) obj;
                dataObjects.add(PojoAdapterUtils.go(cg,cache));
            } else if (obj instanceof Category) {
                Category ca = (Category) obj;
                dataObjects.add(PojoAdapterUtils.go(ca,cache)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(PojoAdapterUtils.go(img,cache));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    static public Map adaptFoundImageAnnotations(Map result) {
        
    		Map cache = newCache();
        Map dataObjects = new HashMap();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                ImageAnnotation ann = (ImageAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(PojoAdapterUtils.go(ann,cache));
            }
        }
        return dataObjects; 
    }

    static public Map adaptFoundDatasetAnnotations(Map result) {
       
    		Map cache = newCache();
        Map dataObjects = new HashMap();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                DatasetAnnotation ann = (DatasetAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(PojoAdapterUtils.go(ann,cache));
            }
        }
        return dataObjects; 
    }

    static ProjectData go(Project p, Map cache) {
        
        if (check(cache,p)) {
            return (ProjectData) from(cache,p);
        }

        ProjectData pd = new ProjectData();
        to(cache,p, pd);
        
        pd.id = p.getProjectId().intValue();
        pd.name = p.getName();
        pd.description = p.getDescription();
        pd.owner = go(p.getExperimenter(),cache);

        Set set = new HashSet();
        for (Iterator i = p.getDatasets().iterator(); i.hasNext();) {
        		Dataset d = (Dataset) i.next();
             set.add(go(d,cache));
        }
        pd.datasets = set;

        return pd;
    }

    static DatasetData go(Dataset d, Map cache) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Ds) *****");
        if (check(cache,d)) {
            return (DatasetData) from(cache,d);
        }

        DatasetData dd = new DatasetData();
        to(cache,d, dd);

        dd.id = d.getDatasetId().intValue();
        dd.name = d.getName();
        dd.description = d.getDescription();
        dd.owner = go(d.getExperimenter(),cache);

        Set set = new HashSet();
        for (Iterator i = d.getImages().iterator(); i.hasNext();) {
        		Image img = (Image) i.next();
        		set.add(go(img,cache));
        }

        dd.images = set;
        return dd;
    }

    static CategoryGroupData go(CategoryGroup cg, Map cache) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(CG) *****");
        if (check(cache,cg)) {
            return (CategoryGroupData) from(cache,cg);
        }

        CategoryGroupData cgd = new CategoryGroupData();
        to(cache,cg, cgd);
        
        cgd.id = cg.getAttributeId().intValue();
        cgd.description = cg.getDescription();
        cgd.name = cg.getName();
        cgd.owner = go(cg.getModuleExecution().getExperimenter(),cache);
        // FIXMEcgd.categories = cg.

        
        return cgd;
    }
   
    static CategoryData go(Category c, Map cache){
        System.out.println(" ***** HierarchyBrowsingImpl.go(Cat) *****");

        if (check(cache,c)) {
            return (CategoryData) from(cache,c);
        }

        CategoryData cd = new CategoryData();
        to(cache,c, cd);
        
        cd.id = c.getAttributeId().intValue();
        cd.name = c.getName();
        cd.description = c.getDescription();
        cd.owner = go(c.getModuleExecution().getExperimenter(),cache);
        
        return cd;
    }

    static ImageData go(Image img, Map cache) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Img) *****");
        if (check(cache,img)) {
            return (ImageData) from(cache,img);
        }

        ImageData id = new ImageData();
        to(cache,img, id);
        
        id.id = img.getImageId().intValue();
        id.name = img.getName();
        id.description = img.getDescription();
        id.inserted = new Timestamp(img.getInserted().getTime());
        id.created = new Timestamp(img.getCreated().getTime());
        id.owner = go(img.getExperimenter(),cache);
        id.defaultPixels = go(img.getImagePixel(),cache);

        Set set = new HashSet();
        for (Iterator i = img.getImagePixels().iterator(); i.hasNext();) {
        		ImagePixel p = (ImagePixel) i.next();
        		set.add(go(p,cache));
        }
        id.allPixels = set;
        
        return id;
    }

    static PixelsData go(ImagePixel ip, Map cache) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Pix) *****");
        if (check(cache,ip)) {
            return (PixelsData) from(cache,ip);
        }

        ImageDimension dim = new ImageDimension(); 
        ip.getImage().getImageDimensions();
       
        PixelsData pd = new PixelsData();
        to(cache,ip, pd);
        
        pd.id = ip.getAttributeId().intValue();
        pd.image = go(ip.getImage(),cache);
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

    static ExperimenterData go(Experimenter e, Map cache) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Exp) *****");
        if (check(cache,e)) {
            return (ExperimenterData) from(cache,e);
        }

        ExperimenterData ed = new ExperimenterData();
        to(cache,e, ed);
        
        ed.id = e.getAttributeId().intValue();
        ed.firstName = e.getFirstname();
        ed.lastName = e.getLastname();
        ed.email = e.getEmail();
        ed.institution = e.getInstitution();
        ed.groupID = e.getGroup().getAttributeId().intValue();
        ed.groupName = e.getGroup().getName();
        
        return ed;
    }

    static AnnotationData go(ImageAnnotation ann, Map cache) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(IAnn) *****");
        if (check(cache,ann)) {
            return (AnnotationData) from(cache,ann);
        }

        AnnotationData ad = new AnnotationData();
        to(cache,ann, ad);
        
        ad.id = ann.getAttributeId().intValue();
        ad.owner = go(ann.getModuleExecution().getExperimenter(),cache);
        /*
         * STs have FX constraints on the 'explicit containers' (images,
         * datasets, features), but nothing else. We were going to add those to
         * the new release (goes hand in hand with not null constraints on the
         * attribute's mex FK)
         */
        ad.text = ann.getContent();
        ad.lastModified = convertDate(ann.getModuleExecution().getTimestamp());
        ad.annotatedObject = go(ann.getImage(),cache);

        return ad;
    }
  

    static AnnotationData go(DatasetAnnotation ann, Map cache) {

        if (check(cache,ann)) {
            return (AnnotationData) from(cache,ann);
        }

        AnnotationData ad = new AnnotationData();
        to(cache,ann, ad);
        
        ad.id = ann.getAttributeId().intValue();
        ad.owner = go(ann.getModuleExecution().getExperimenter(),cache);
        ad.text = ann.getContent();
        ad.lastModified = convertDate(ann.getModuleExecution().getTimestamp());
        ad.annotatedObject = go(ann.getDataset(),cache);

        return ad;
    }

    static Timestamp convertDate(Date date){
        if (null==date){
            return null;
        } 
        return new Timestamp(date.getTime());
    }
    
    static Map newCache() {
        Map m = new HashMap();
        m.put(null,null); // This keeps things from getting hairy
        m.put(init_time,new Date());
        return m;
    }
    
    static boolean ok(Map cache){
        if (cache.containsKey(init_time)) return true;
        return false;
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
        cache.put(key, value);
    }

    final static String nullId = "Null ID for: ";
    final static String nullExp = "Null Experimenter for: ";
    final static String nullDs = "Null Dataset for: ";
    final static String nullImgs = "Null Images for: ";
    

}