/*
 * org.openmicroscopy.omero.shoolaadapter.AdapterUtils
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    
    static public DataObject adaptLoadedPDIHierarchy(Class rootNodeType, Object result) {
        if (rootNodeType.equals(Project.class)){
            return AdapterUtils.go((Project) result);
        } else if (rootNodeType.equals(Dataset.class)){
            return AdapterUtils.go((Dataset) result);
        } else {
            throw new IllegalArgumentException("Method only takes Project and Dataset as argument.");
        }
    }

    static public DataObject adaptLoadedCGCIHierarchy(Class rootNodeType, Object result) {
        if (rootNodeType.equals(CategoryGroup.class)){
            return AdapterUtils.go((CategoryGroup) result);
        } else if (rootNodeType.equals(Category.class)){
            return AdapterUtils.go((Category) result);
        } else {
            throw new IllegalArgumentException("Method only takes CategoryGroup and Category as argument.");
        }
    }

    static public Set adaptFoundPDIHierarchies(Set result) {
        Set dataObjects = new HashSet();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof Project) {
                Project prj = (Project) obj;
                dataObjects.add(AdapterUtils.go(prj));
            } else if (obj instanceof Dataset) {
                Dataset ds = (Dataset) obj;
                dataObjects.add(AdapterUtils.go(ds)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(AdapterUtils.go(img));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    static public Set adaptFoundCGCIHierarchies(Set result) {
        Set dataObjects = new HashSet();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof CategoryGroup) {
                CategoryGroup cg = (CategoryGroup) obj;
                dataObjects.add(AdapterUtils.go(cg));
            } else if (obj instanceof Category) {
                Category ca = (Category) obj;
                dataObjects.add(AdapterUtils.go(ca)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(AdapterUtils.go(img));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    static public Map adaptFoundImageAnnotations(Map result) {
        Map dataObjects = new HashMap();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                ImageAnnotation ann = (ImageAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(AdapterUtils.go(ann));
            }
        }
        return dataObjects; 
    }

    static public Map adaptFoundDatasetAnnotations(Map result) {
        Map dataObjects = new HashMap();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                DatasetAnnotation ann = (DatasetAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(AdapterUtils.go(ann));
            }
        }
        return dataObjects; 
    }
    
    static public ProjectData go(Project p) {
        return go(p, newCache());
    }

    static public ProjectData go(Project p, Map cache) {

        if (check(cache,p)) {
            return (ProjectData) from(cache, p);
        }

        ProjectData pd = new ProjectData();
        to(cache, p, pd);
        
        if (null == p.getProjectId()) {
            log.debug("Id for "+p+" is null.");
        } else {
            pd.setID(p.getProjectId().intValue());
        }
        pd.setName(p.getName());
        pd.setDescription(p.getDescription());
        if (null==p.getExperimenter()){
            log.debug("Experimenter for "+p+" is null.");
        } else {
            pd.setOwnerFirstName(p.getExperimenter().getFirstname());
        }
        //TODO

        if (null==p.getDatasets()){
            log.debug("Datasets for "+p+" is null.");
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

    static public DatasetData go(Dataset d) {
        return go(d, newCache());
    }

    static public DatasetData go(Dataset d, Map cache) {

        if (check(cache, d)) {
            return (DatasetData) from(cache, d);
        }

        DatasetData dd = new DatasetData();
        to(cache, d, dd);

        if (null==d.getDatasetId()){
            log.debug("Id for "+d+" is null.");
        } else {
            dd.setID(d.getDatasetId().intValue());
        }

        dd.setName(d.getName());
        dd.setDescription(d.getDescription());
        if (null==d.getExperimenter()){
            log.debug("Experimenter for "+d+" is null.");
        } else {
            dd.setOwnerFirstName(d.getExperimenter().getFirstname());
        }
        //TODO

        if (null==d.getImages()){
            log.debug("Images for "+d+" is null.");
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
        
        if (null==img.getImageId()){
            log.debug("Id for "+img+" is null.");
        } else {
            id.setID(img.getImageId().intValue());
        }
        id.setName(img.getName());
        id.setDescription(img.getDescription());
        id.setInserted(convertDate(img.getInserted()));
        id.setCreated(convertDate(img.getCreated()));
        
        if (null==img.getExperimenter()){
            log.debug("Experimenter for "+img+" is null.");
        } else {
            //TODO and id here?
            id.setOwnerID(img.getExperimenter().getAttributeId().intValue());
        }
        //TODO

        if (null==img.getImagePixels()){
            log.debug("Pixels for "+img+" is null.");
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
        if (null==date){
            log.debug("Null date.");
            return null;
        } 
        return new Timestamp(date.getTime());
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

}