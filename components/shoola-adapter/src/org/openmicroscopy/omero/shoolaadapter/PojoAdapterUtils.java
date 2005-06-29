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

    static final ThreadLocal cache = new ThreadLocal();
 
    static public DataObject adaptLoadedPDIHierarchy(Class rootNodeType, Object result) {
        newCache();
        
        if (rootNodeType.equals(Project.class)){
            return PojoAdapterUtils.go((Project) result);
        } else if (rootNodeType.equals(Dataset.class)){
            return PojoAdapterUtils.go((Dataset) result);
        } else {
            throw new IllegalArgumentException("Method only takes Project and Dataset as argument.");
        }
    }

    static public DataObject adaptLoadedCGCIHierarchy(Class rootNodeType, Object result) {
        newCache();
        
        if (rootNodeType.equals(CategoryGroup.class)){
            return PojoAdapterUtils.go((CategoryGroup) result);
        } else if (rootNodeType.equals(Category.class)){
            return PojoAdapterUtils.go((Category) result);
        } else {
            throw new IllegalArgumentException("Method only takes CategoryGroup and Category as argument.");
        }
    }

    static public Set adaptFoundPDIHierarchies(Set result) {
        newCache();
        
        Set dataObjects = new HashSet();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof Project) {
                Project prj = (Project) obj;
                dataObjects.add(PojoAdapterUtils.go(prj));
            } else if (obj instanceof Dataset) {
                Dataset ds = (Dataset) obj;
                dataObjects.add(PojoAdapterUtils.go(ds)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(PojoAdapterUtils.go(img));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    static public Set adaptFoundCGCIHierarchies(Set result) {
        newCache();
        
        Set dataObjects = new HashSet();
        for (Iterator i = result.iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj instanceof CategoryGroup) {
                CategoryGroup cg = (CategoryGroup) obj;
                dataObjects.add(PojoAdapterUtils.go(cg));
            } else if (obj instanceof Category) {
                Category ca = (Category) obj;
                dataObjects.add(PojoAdapterUtils.go(ca)); 
            } else if (obj instanceof Image) {
                Image img = (Image) obj;
                dataObjects.add(PojoAdapterUtils.go(img));
            } else {
                throw new RuntimeException("Method returned unexpected value type:" + obj.getClass()	);
            }
        }
        return dataObjects;
    }

    static public Map adaptFoundImageAnnotations(Map result) {
        newCache();
        
        Map dataObjects = new HashMap();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                ImageAnnotation ann = (ImageAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(PojoAdapterUtils.go(ann));
            }
        }
        return dataObjects; 
    }

    static public Map adaptFoundDatasetAnnotations(Map result) {
        newCache();
        
        Map dataObjects = new HashMap();
        for (Iterator i = result.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Set value = (Set) result.get(key);
            dataObjects.put(key,new HashSet());
            for (Iterator j = value.iterator(); j.hasNext();) {
                DatasetAnnotation ann = (DatasetAnnotation) j.next();
                ((Set) dataObjects.get(key)).add(PojoAdapterUtils.go(ann));
            }
        }
        return dataObjects; 
    }
    
    
    static ProjectData go(Project p) {
        return go(p, true);
    }

    static ProjectData go(Project p, boolean resolve) {
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

    static DatasetData go(Dataset d) {
        return go(d, true);
    }

    static DatasetData go(Dataset d, boolean resolve) {
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

    static CategoryGroupData go(CategoryGroup cg) {
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

    static CategoryData go(Category c) {
        return go(c,true);
    }
    
    static CategoryData go(Category c, boolean resolve){
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

    static ImageData go(Image img) {
        return go(img, true);
    }

    static ImageData go(Image img, boolean resolve) {
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
        id.defaultPixels = go(img.getImagePixel());

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

    static PixelsData go(ImagePixel ip) {
        System.out.println(" ***** HierarchyBrowsingImpl.go(Pix) *****");
        if (checkCache(ip)) {
            return (PixelsData) fromCache(ip);
        }

        ImageDimension dim = new ImageDimension(); 
        ip.getImage().getImageDimensions();
       
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

    static ExperimenterData go(Experimenter e) {
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

    static AnnotationData go(ImageAnnotation ann) {
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
    
    static Timestamp go(Date date){
        return null == date ? null : new Timestamp(date.getTime());
    }

    static AnnotationData go(DatasetAnnotation ann) {
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

    static void newCache() {
        Map m = new HashMap();
        m.put(null,null); // This keeps things from getting hairy
        cache.set(m);
    }

    static void emptyCache() {
        //TODO Need to make sure cache is emptied on exception
        cache.set(null);
    }

    static boolean checkCache(Object key) {
        if (((Map) cache.get()).containsKey(key)) {
            return true;
        }
        return false;
    }

    static Object fromCache(Object key) {
        return ((Map) cache.get()).get(key);
    }

    static void toCache(Object key, Object value) {
        ((Map) cache.get()).put(key, value);
    }

}