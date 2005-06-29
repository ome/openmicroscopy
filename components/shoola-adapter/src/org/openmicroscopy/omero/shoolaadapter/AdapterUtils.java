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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
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
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.data.model.ProjectData;

/** 
 * provides nearly all functionality in an adapter. The implementation
 * of <code>AdapterUtils</code> is very much bound to both the target
 * and the source models.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class AdapterUtils {

    private static Log log = LogFactory.getLog(AdapterUtils.class);
    private static final String init_time = "cache.initialization_time";
    private static final String cacheError = "Cache not properly initialized.";
    
    static public DataObject adaptLoadedPDIHierarchy(Class rootNodeType, Object result) {
        if (rootNodeType.equals(Project.class)){
            return AdapterUtils.go((Project) result, newCache());
        } else if (rootNodeType.equals(Dataset.class)){
            return AdapterUtils.go((Dataset) result, newCache());
        } else {
            throw new IllegalArgumentException("Method only takes Project and Dataset as argument.");
        }
    }

    static public DataObject adaptLoadedCGCIHierarchy(Class rootNodeType, Object result) {
        if (rootNodeType.equals(CategoryGroup.class)){
            return AdapterUtils.go((CategoryGroup) result, newCache());
        } else if (rootNodeType.equals(Category.class)){
            return AdapterUtils.go((Category) result, newCache());
        } else {
            throw new IllegalArgumentException("Method only takes CategoryGroup and Category as argument.");
        }
    }

    static public Set adaptFoundPDIHierarchies(Set result) {
        Set dataObjects = new HashSet();
        Map cache = newCache();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof Project) {
                Project prj = (Project) obj;
                dataObjects.add(AdapterUtils.go(prj,cache));
            } else if (obj instanceof Dataset) {
                Dataset ds = (Dataset) obj;
                dataObjects.add(AdapterUtils.go(ds,cache)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(AdapterUtils.go(img,cache));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    static public Set adaptFoundCGCIHierarchies(Set result) {
        Set dataObjects = new HashSet();
        Map cache = newCache();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof CategoryGroup) {
                CategoryGroup cg = (CategoryGroup) obj;
                dataObjects.add(AdapterUtils.go(cg,cache));
            } else if (obj instanceof Category) {
                Category ca = (Category) obj;
                dataObjects.add(AdapterUtils.go(ca,cache)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(AdapterUtils.go(img,cache));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    static public Map adaptFoundImageAnnotations(Map result) {
        Map dataObjects = new HashMap();
        Map cache = newCache();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                ImageAnnotation ann = (ImageAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(AdapterUtils.go(ann,cache));
            }
        }
        return dataObjects; 
    }

    static public Map adaptFoundDatasetAnnotations(Map result) {
        Map dataObjects = new HashMap();
        Map cache = newCache();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                DatasetAnnotation ann = (DatasetAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(AdapterUtils.go(ann,cache));
            }
        }
        return dataObjects; 
    }
    
//    static public ProjectData go(Project p) {
//        return go(p, newCache());
//    }

    
    static public ProjectData go(Project p, Map cache) {

        if (!ok(cache)) throw new IllegalArgumentException(cacheError);
        
        if (check(cache,p)) {
            return (ProjectData) from(cache, p);
        }

        ProjectData pd = new ProjectData();
        to(cache, p, pd);
        
        if (null == p.getProjectId()) { 
            if (log.isDebugEnabled())log.debug(nullId+p);
        } else {
            pd.setID(p.getProjectId().intValue());
        }
        pd.setName(p.getName());
        pd.setDescription(p.getDescription());
        if (null==p.getExperimenter()){
            if (log.isDebugEnabled())log.debug(nullExp + p);
        } else {
            pd.setOwnerFirstName(p.getExperimenter().getFirstname());
        }
        //TODO

        if (null==p.getDatasets()){
            if (log.isDebugEnabled())log.debug(nullDs + p);
        } else {
            Set set = new HashSet();
            for (Iterator i = p.getDatasets().iterator(); i.hasNext();) {
                Dataset d = (Dataset) i.next();
                set.add(go(d,cache));
            }
            pd.setDatasets(new ArrayList(set));
        }

        return pd;
    }

//    static public DatasetData go(Dataset d) {
//        return go(d, newCache());
//    }

    static public DatasetData go(Dataset d, Map cache) {
        
        if (!ok(cache)) throw new IllegalArgumentException(cacheError);

        if (check(cache, d)) {
            return (DatasetData) from(cache, d);
        }

        DatasetData dd = new DatasetData();
        to(cache, d, dd);

        if (null==d.getDatasetId()){
            if (log.isDebugEnabled())log.debug(nullId + d);
        } else {
            dd.setID(d.getDatasetId().intValue());
        }

        dd.setName(d.getName());
        dd.setDescription(d.getDescription());
        if (null==d.getExperimenter()){
            if (log.isDebugEnabled())log.debug(nullExp + d);
        } else {
            dd.setOwnerFirstName(d.getExperimenter().getFirstname());
        }
        //TODO

        if (null==d.getImages()){
            if (log.isDebugEnabled())log.debug(nullImgs + d);
        } else {
	        Set set = new HashSet();
	        for (Iterator i = d.getImages().iterator(); i.hasNext();) {
	              Image img = (Image) i.next();
	              set.add(go(img,cache));
	        }
	
	        dd.setImages(new ArrayList(set));
        }
        return dd;
        
    }

//    static public CategoryGroupData go(CategoryGroup cg) {
//        return go(cg, newCache());
//    }
    
    static public CategoryGroupData go(CategoryGroup cg, Map cache) {

        if (!ok(cache)) throw new IllegalArgumentException(cacheError);
        
        if (check(cache, cg)) {
            return (CategoryGroupData) from(cache, cg);
        }

        CategoryGroupData cgd = new CategoryGroupData();
        to(cache, cg, cgd);
        
        if (null==cg.getAttributeId()){
            if (log.isDebugEnabled()) log.debug(nullId + cg);
        } else {
            cgd.setID(cg.getAttributeId().intValue());
    	}	
        cgd.setDescription(cg.getDescription());
        cgd.setName(cg.getName());
        // TODO
        
        return cgd;
    }

//    static public CategoryData go(Category c) {
//        return go(c, newCache());
//    }
    
    static public CategoryData go(Category c, Map cache){

        if (!ok(cache)) throw new IllegalArgumentException(cacheError);
        
        if (check(cache, c)) {
            return (CategoryData) from(cache, c);
        }

        CategoryData cd = new CategoryData();
        to(cache, c, cd);
        
        if (null==c.getAttributeId()){
            if (log.isDebugEnabled())
                log.debug(nullId+c);
        } else {
            cd.setID(c.getAttributeId().intValue());
        }
        cd.setName(c.getName());
        cd.setDescription(c.getDescription());
        //TODO
        
        return cd;
    }

//    static public ImageData go(Image img) {
//        return go(img, newCache());
//    }

    static public ImageData go(Image img, Map cache) {

        if (!ok(cache)) throw new IllegalArgumentException(cacheError);
        
        if (check(cache, img)) {
            return (ImageData) from(cache, img);
        }

        ImageData id = new ImageData();
        to(cache, img, id);
        
        if (null==img.getImageId()){
            if (log.isDebugEnabled())log.debug("Id for "+img+" is null.");
        } else {
            id.setID(img.getImageId().intValue());
        }
        id.setName(img.getName());
        id.setDescription(img.getDescription());
        id.setInserted(convertDate(img.getInserted()));
        id.setCreated(convertDate(img.getCreated()));
        
        if (null==img.getExperimenter()){
            if (log.isDebugEnabled())log.debug("Experimenter for "+img+" is null.");
        } else {
            //TODO and id here?
            id.setOwnerID(img.getExperimenter().getAttributeId().intValue());
        }
        //TODO

        if (null==img.getImagePixels()){
            if (log.isDebugEnabled())log.debug("Pixels for "+img+" is null.");
        } else {
            Set set = new HashSet();
            for (Iterator i = img.getImagePixels().iterator(); i.hasNext();) {
                	ImagePixel p = (ImagePixel) i.next();
                	set.add(go(p,cache));
            }
            id.setPixels(new ArrayList(set));
        }
        
        return id;
    }

    static public PixelsDescription go(ImagePixel ip, Map cache) {

        if (!ok(cache)) throw new IllegalArgumentException(cacheError);
        
        if (check(cache, ip)) {
            return (PixelsDescription) from(cache, ip);
        }

        PixelsDescription pd = new PixelsDescription();
        to(cache, ip, pd);

        ImageDimension dim;
        Set dimensions = ip.getImage().getImageDimensions();
        if (dimensions.size()>0) {
            dim=(ImageDimension) dimensions.iterator().next();
            pd.setPixelSizeX(dim.getPixelSizeX().doubleValue());
            pd.setPixelSizeY(dim.getPixelSizeY().doubleValue());
            pd.setPixelSizeZ(dim.getPixelSizeZ().doubleValue());
        }

        pd.setID(ip.getAttributeId().intValue());//TODO
        pd.setImageServerID(ip.getImageServerId().longValue());
        pd.setImageServerUrl(ip.getRepository().getImageServerUrl());
        pd.setPixelType(ip.getPixelType());
        pd.setSizeC(ip.getSizeC().intValue());
        pd.setSizeT(ip.getSizeT().intValue());
        pd.setSizeX(ip.getSizeX().intValue());
        pd.setSizeY(ip.getSizeY().intValue());
        pd.setSizeZ(ip.getSizeZ().intValue());

        return pd;
    }

    static public UserDetails go(Experimenter e, Map cache) {

        if (!ok(cache)) throw new IllegalArgumentException(cacheError);
        
        if (check(cache, e)) {
            return (UserDetails) from(cache, e);
        }

        List groups = new ArrayList();
        groups.add(e.getGroup().getAttributeId());
        UserDetails ed = new UserDetails(e.getAttributeId().intValue(),e.getFirstname(),e.getLastname(),groups);
        to(cache, e, ed);

        //TODO
        
        return ed;
    }

//    static public AnnotationData go(ImageAnnotation ann) {
//        return go(ann, newCache());
//    }
    
    static public AnnotationData go(ImageAnnotation ann, Map cache) {

        if (!ok(cache)) throw new IllegalArgumentException(cacheError);
        
        if (check(cache, ann)) {
            return (AnnotationData) from(cache, ann);
        }

        AnnotationData ad;
        ModuleExecution mex = ann.getModuleExecution();
        if (null==mex){
            if (log.isDebugEnabled())log.debug("Mex for "+ann+" is null.");
            ad = null;
        } else {
            ad = new AnnotationData(ann.getAttributeId().intValue(),
                mex.getExperimenter().getAttributeId().intValue(),
                convertDate(mex.getTimestamp()));//TODO
        }
        to(cache, ann, ad);
        
        if (null==ad){//TODO
            if (log.isErrorEnabled())log.error("Error: Annotation data not created.");
        } else {
            ad.setOwnerFirstName(ann.getModuleExecution().getExperimenter().getFirstname());
            ad.setOwnerLastName(ann.getModuleExecution().getExperimenter().getLastname());
            ad.setAnnotation(ann.getContent());
        }
        //TODO

        return ad;
    }
    
//    static public AnnotationData go(DatasetAnnotation ann) {
//        return go(ann, newCache());
//    }
    
    static public AnnotationData go(DatasetAnnotation ann, Map cache) {

        if (!ok(cache)) throw new IllegalArgumentException(cacheError);
        
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