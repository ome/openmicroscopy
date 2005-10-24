/*
 * ome.adapters.pojos.AdapterUtils
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
package ome.adapters.pojos;

// Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencie
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Dataset;
import ome.model.DatasetAnnotation;
import ome.model.Experimenter;
import ome.model.Group;
import ome.model.Image;
import ome.model.ImageAnnotation;
import ome.model.ImageDimension;
import ome.model.ImagePixel;
import ome.model.ModuleExecution;
import ome.model.Project;
import ome.model.Repository;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;

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

	static public DataObject adaptLoadedPDIHierarchy(Class rootNodeType,
			Object result) {

		if (null == result) {
			return null;
		}

		if (null == rootNodeType) {
			throw new IllegalArgumentException(
					"Class parameter may not be null.");
		}
//TODO wrap all of this code in try/catch !!!
		if (rootNodeType.equals(Project.class)) {
			return PojoAdapterUtils.go((Project) result, newCache());
		} else if (rootNodeType.equals(Dataset.class)) {
			return PojoAdapterUtils.go((Dataset) result, newCache());
		} else {
			throw new IllegalArgumentException(
					"Method only takes Project and Dataset as argument.");
		}
	}

	static public DataObject adaptLoadedCGCIHierarchy(Class rootNodeType,
			Object result) {

		if (null == result) {
			return null;
		}

		if (null == rootNodeType) {
			throw new IllegalArgumentException(
					"Class parameter may not be null.");
		}

		if (rootNodeType.equals(CategoryGroup.class)) {
			return PojoAdapterUtils.go((CategoryGroup) result, newCache());
		} else if (rootNodeType.equals(Category.class)) {
			return PojoAdapterUtils.go((Category) result, newCache());
		} else {
			throw new IllegalArgumentException(
					"Method only takes CategoryGroup and Category as argument.");
		}
	}

	static public Set adaptFoundPDIHierarchies(Set result) {

		if (null == result) {
			return null;
		}

		Map cache = newCache();
		Set dataObjects = new HashSet();
		for (Iterator i = result.iterator(); i.hasNext();) {
			Object obj = i.next();
			if (obj instanceof Project) {
				Project prj = (Project) obj;
				dataObjects.add(PojoAdapterUtils.go(prj, cache));
			} else if (obj instanceof Dataset) {
				Dataset ds = (Dataset) obj;
				dataObjects.add(PojoAdapterUtils.go(ds, cache));
			} else if (obj instanceof Image) {
				Image img = (Image) obj;
				dataObjects.add(PojoAdapterUtils.go(img, cache));
			} else {
				throw new RuntimeException(
						"Method returned unexpected value type:"
								+ obj.getClass());
			}
		}
		return dataObjects;
	}

	static public Set adaptFoundCGCIHierarchies(Set result) {

    	if (null == result) {
    		return null;
    	}
    			
		Map cache = newCache();
		Set dataObjects = new HashSet();
		for (Iterator i = result.iterator(); i.hasNext();) {
			Object obj = i.next();
			if (obj instanceof CategoryGroup) {
				CategoryGroup cg = (CategoryGroup) obj;
				dataObjects.add(PojoAdapterUtils.go(cg, cache));
			} else if (obj instanceof Category) {
				Category ca = (Category) obj;
				dataObjects.add(PojoAdapterUtils.go(ca, cache));
			} else if (obj instanceof Image) {
				Image img = (Image) obj;
				dataObjects.add(PojoAdapterUtils.go(img, cache));
			} else {
				throw new RuntimeException(
						"Method returned unexpected value type:"
								+ obj.getClass());
			}
		}
		return dataObjects;
	}

	static public Map adaptFoundImageAnnotations(Map result) {

    	if (null == result) {
    		return null;
    	}
    	
		Map cache = newCache();
		Map dataObjects = new HashMap();
		for (Iterator i = result.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Set value = (Set) result.get(key);
			dataObjects.put(key, new HashSet());
			for (Iterator j = value.iterator(); j.hasNext();) {
				ImageAnnotation ann = (ImageAnnotation) j.next();
				((Set) dataObjects.get(key)).add(PojoAdapterUtils
						.go(ann, cache));
			}
		}
		return dataObjects;
	}

	static public Map adaptFoundDatasetAnnotations(Map result) {
    	
		if (null == result) {
    		return null;
    	}
    			
		Map cache = newCache();
		Map dataObjects = new HashMap();
		for (Iterator i = result.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Set value = (Set) result.get(key);
			dataObjects.put(key, new HashSet());
			for (Iterator j = value.iterator(); j.hasNext();) {
				DatasetAnnotation ann = (DatasetAnnotation) j.next();
				((Set) dataObjects.get(key)).add(PojoAdapterUtils
						.go(ann, cache));
			}
		}
		return dataObjects;
	}

	/* ===================================================
	 * GO Methods
	 * =================================================== */
	
	static ProjectData go(Project p, Map cache) {

		if (check(cache, p)) {
			return (ProjectData) from(cache, p);
		}

		ProjectData pd = new ProjectData();
		to(cache, p, pd);

		pd.setId(p.getProjectId().intValue());
		pd.setName(p.getName());
		pd.setDescription(p.getDescription());
		pd.setOwner(go(p.getExperimenter(), cache));

		Set set = new HashSet();
		for (Iterator i = p.getDatasets().iterator(); i.hasNext();) {
			Dataset d = (Dataset) i.next();
			set.add(go(d, cache));
		}
		pd.setDatasets(set);
		
		return pd;
	}

	static DatasetData go(Dataset d, Map cache) {
		if (check(cache, d)) {
			return (DatasetData) from(cache, d);
		}

		DatasetData dd = new DatasetData();
		to(cache, d, dd);

		dd.setId(d.getDatasetId().intValue());
		dd.setName(d.getName());
		dd.setDescription(d.getDescription());
		dd.setOwner(go(d.getExperimenter(), cache));
		
		Set set = new HashSet();
		if (null==d.getImages()){
			if (log.isWarnEnabled()){
				log.warn(nullImgs+d);
			}
		} else {
			for (Iterator i = d.getImages().iterator(); i.hasNext();) {
				Image img = (Image) i.next();
				set.add(go(img, cache));
			}
		}
		dd.setImages(set);
		
		Set set2 = new HashSet();
		if (null==d.getDatasetAnnotations()){//TODO no size()==0 on this warnings!
			if (log.isWarnEnabled()){
				log.warn(nullAnns+d);
			}
		} else {//TODO convert all of this to visitor pattern PLEASE!
			for (Iterator i = d.getDatasetAnnotations().iterator(); i.hasNext();) {
				DatasetAnnotation dann = (DatasetAnnotation) i.next();
				set2.add(go(dann,cache));
			}
		}
		dd.setAnnotations(set2);
		
		Set set3 = new HashSet();
		if (null==d.getProjects()){
			if (log.isWarnEnabled()){
				log.warn(nullPrjs+d);
			}
		} else {
			for (Iterator i = d.getProjects().iterator(); i.hasNext();) {
				Project p = (Project) i.next();
				set3.add(go(p,cache));
			}
		}
		dd.setProjects(set3);
		
		return dd;
	}

	static CategoryGroupData go(CategoryGroup cg, Map cache) {
		if (check(cache, cg)) {
			return (CategoryGroupData) from(cache, cg);
		}

		CategoryGroupData cgd = new CategoryGroupData();
		to(cache, cg, cgd);

		cgd.setId(cg.getAttributeId().intValue());
		cgd.setDescription(cg.getDescription());
		cgd.setName(cg.getName());
		cgd.setOwner(getOwnerFromMex(cg,cg.getModuleExecution(),cache));
		Set set = new HashSet();
		if (null==cg.getCategories()){
			if (log.isWarnEnabled()){
				log.warn(nullCats+cg);
			}
		} else {
			for (Iterator i = cg.getCategories().iterator(); i.hasNext();) {
				Category c = (Category) i.next();
				set.add(go(c,cache));
			}
		}
		cgd.setCategories(set); 
		
		return cgd;
	}

	static CategoryData go(Category c, Map cache) {

		if (check(cache, c)) {
			return (CategoryData) from(cache, c);
		}

		CategoryData cd = new CategoryData();
		to(cache, c, cd);

		cd.setId(c.getAttributeId().intValue());
		cd.setName(c.getName());
		cd.setDescription(c.getDescription());
		cd.setOwner(getOwnerFromMex(c,c.getModuleExecution(), cache));
		cd.setGroup(go(c.getCategoryGroup(),cache));
		
		Set set = new HashSet();
		Set clas = c.getClassifications();
		if (null==clas||clas.size()<1){
			if (log.isWarnEnabled()){
				log.warn(nullClas+c);
			}
		} else {
			for (Iterator i = clas.iterator(); i.hasNext();) {
				Classification cla = (Classification) i.next();
				Image img = cla.getImage();
				if (null==img) {
					if (log.isWarnEnabled()){
						log.warn(nullImg+cla);
					}
				} else {
					set.add(go(img,cache));
				}
			}
		}
		cd.setImages(set);

		return cd;
	}

	static ImageData go(Image img, Map cache) {

		if (check(cache, img)) {
			return (ImageData) from(cache, img);
		}

		ImageData id = new ImageData();
		to(cache, img, id);

		id.setId(img.getImageId().intValue());
		id.setName(img.getName());
		id.setDescription(img.getDescription());
		id.setInserted(new Timestamp(img.getInserted().getTime()));
		id.setCreated(new Timestamp(img.getCreated().getTime()));
		id.setOwner(go(img.getExperimenter(), cache));
		id.setDefaultPixels(go(img.getImagePixel(), cache));

		Set set = new HashSet();
		if (null==img.getImagePixels()){
			if (log.isWarnEnabled()){
				log.warn(nullPixs+img);
			}
		} else {
			for (Iterator i = img.getImagePixels().iterator(); i.hasNext();) {
				ImagePixel p = (ImagePixel) i.next();
				set.add(go(p, cache));
			}
		}
		id.setAllPixels(set);

		Set set2 = new HashSet();
		if (null==img.getImageAnnotations()){
			if (log.isWarnEnabled()){
				log.warn(nullAnns+img);
			}
		} else {
			for (Iterator i = img.getImageAnnotations().iterator(); i.hasNext();) {
				ImageAnnotation iann = (ImageAnnotation) i.next();
				set2.add(go(iann,cache));
			}
		}
		id.setAnnotations(set2);
		
		Set set3 = new HashSet();
		if (null==img.getDatasets()){
			if (log.isWarnEnabled()){//TODO not all of these need to be warn
				log.warn(nullDs+img);
			}
		} else {
			for (Iterator i = img.getDatasets().iterator(); i.hasNext();) {
				Dataset ds = (Dataset) i.next();
				set3.add(go(ds,cache));
			}
		}
		id.setDatasets(set3);
		
		return id;
	}

	static PixelsData go(ImagePixel ip, Map cache) {

		if (check(cache, ip)) {
			return (PixelsData) from(cache, ip);
		}

		PixelsData pd = new PixelsData();
		to(cache, ip, pd);

		pd.setId(ip.getAttributeId().intValue());
		pd.setImage(go(ip.getImage(), cache));
		pd.setImageServerID(ip.getImageServerId().longValue());
		Repository rep = ip.getRepository();
		if (null==rep){
			if (log.isWarnEnabled()){
				log.warn(nullRepository+ip);
			}
		} else {
			pd.setImageServerURL(ip.getRepository().getImageServerUrl());
		}

		Set dims = ip.getImage().getImageDimensions();
		if (dims == null || dims.size() < 1) {
			if (log.isWarnEnabled()){
				log.warn("No dimenions found for ImagePixel "+ip.getAttributeId());
			}
		} else {
			if (dims.size()>1 && log.isWarnEnabled()){
				log.warn("Image contains multiple dimensions. Using first.");
			}
			List list = new ArrayList(dims);
			ImageDimension dim = (ImageDimension) list.get(0);
			pd.setPixelSizeX(dim.getPixelSizeX().doubleValue());
			pd.setPixelSizeY(dim.getPixelSizeY().doubleValue());
			pd.setPixelSizeZ(dim.getPixelSizeZ().doubleValue());
		} 
		pd.setPixelType(PojoAdapterUtils.getPixelTypeID(ip.getPixelType()));
		pd.setSizeC(ip.getSizeC().intValue());
		pd.setSizeT(ip.getSizeT().intValue());
		pd.setSizeX(ip.getSizeX().intValue());
		pd.setSizeY(ip.getSizeY().intValue());
		pd.setSizeZ(ip.getSizeZ().intValue());
		
		return pd;
	}

	static ExperimenterData go(Experimenter e, Map cache) {

		if (check(cache, e)) {
			return (ExperimenterData) from(cache, e);
		}

		ExperimenterData ed = new ExperimenterData();
		to(cache, e, ed);

		ed.setId(e.getAttributeId().intValue());
		ed.setFirstName(e.getFirstname());
		ed.setLastName(e.getLastname());
		ed.setEmail(e.getEmail());
		ed.setInstitution(e.getInstitution());
		Group g = e.getGroup();
		if (null==g){
			if (log.isWarnEnabled()){
				log.warn(nullGroup+e);
			}
		} else {
			ed.setGroupID(e.getGroup().getAttributeId().intValue());
			ed.setGroupName(e.getGroup().getName());
		}

		return ed;
	}

	static AnnotationData go(ImageAnnotation ann, Map cache) {

		if (check(cache, ann)) {
			return (AnnotationData) from(cache, ann);
		}

		AnnotationData ad = new AnnotationData();
		to(cache, ann, ad);

		ad.setId(ann.getAttributeId().intValue());
		ad.setOwner(getOwnerFromMex(ann,ann.getModuleExecution(), cache));
		ad.setText(ann.getContent());
		ad.setLastModified(convertDate(ann.getModuleExecution().getTimestamp()));
		ad.setAnnotatedObject(go(ann.getImage(), cache));

		return ad;
	}

	static AnnotationData go(DatasetAnnotation ann, Map cache) {

		if (check(cache, ann)) {
			return (AnnotationData) from(cache, ann);
		}

		AnnotationData ad = new AnnotationData();
		to(cache, ann, ad);

		ad.setId(ann.getAttributeId().intValue());
		ad.setOwner(getOwnerFromMex(ann,ann.getModuleExecution(), cache));
		ad.setText(ann.getContent());
		ad.setLastModified(convertDate(ann.getModuleExecution().getTimestamp()));
		ad.setAnnotatedObject(go(ann.getDataset(), cache));

		return ad;
	}

	static Timestamp convertDate(Date date) {
		if (null == date) {
			return null;
		}
		return new Timestamp(date.getTime());
	}

	static Map newCache() {
		Map m = new HashMap();
		m.put(null, null); // This keeps things from getting hairy
		m.put(init_time, new Date());
		return m;
	}

	static boolean ok(Map cache) {
		if (cache.containsKey(init_time))
			return true;
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
	
	final static String nullMex = "Null MEX for: ";
	
	final static String nullRepository = "Null Repository for: "; 

	final static String nullGroup = "Null Group for: ";
	
	final static String nullPixs = "Null ImagePixels for: ";
	
	final static String nullCats = "Null Categories for: ";
	
	final static String nullImg = "Null image for: ";
	
	final static String nullClas = "Null classifications for: ";
	
	final static String nullAnns = "Null Annotations for: ";
	
	final static String nullPrjs = "Null Projects for: ";
	
	/* =====================================================
	 * Other Helper methods
	 * ===================================================== */

	static private ExperimenterData getOwnerFromMex(Object target, ModuleExecution mex, Map cache){
		if (null==mex){
			if (log.isWarnEnabled()){
				log.warn(nullMex+target);
			}
			return null;
		} 
		return go(mex.getExperimenter(), cache);
	}

	private static Map pixelTypesMap = new HashMap();
	
	static {
        pixelTypesMap.put("INT8", new Integer(PixelsData.INT8_TYPE));
        pixelTypesMap.put("INT16", new Integer(PixelsData.INT16_TYPE));
        pixelTypesMap.put("INT32", new Integer(PixelsData.INT32_TYPE));
        pixelTypesMap.put("UINT8", new Integer(PixelsData.UINT8_TYPE));
        pixelTypesMap.put("UINT16", new Integer(PixelsData.UINT16_TYPE));
        pixelTypesMap.put("UINT32", new Integer(PixelsData.UINT32_TYPE));
        pixelTypesMap.put("FLOAT", new Integer(PixelsData.FLOAT_TYPE));
        pixelTypesMap.put("DOUBLE", new Integer(PixelsData.DOUBLE_TYPE));
	}
	
	static private int getPixelTypeID(String pixelType) {
		return 0;//pixelType == null ? 0 : ((Integer)pixelTypesMap.get(pixelType)).intValue();//TODO exceptions 
		// FIXME quick hack
	}

	
}