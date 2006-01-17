/*
 * ome.adapter.pojos.Model2PojoMapper
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

//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.DatasetAnnotation;
import ome.model.Experimenter;
import ome.model.Group;
import ome.model.Image;
import ome.model.ImageAnnotation;
import ome.model.ImagePixel;
import ome.model.Project;

import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.ProjectData;

/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class Model2PojosMapper extends ome.util.ModelMapper {
	
	protected static Log log = LogFactory.getLog(Model2PojosMapper.class); // TODO protected in cases where inherited
	
	private final static Map _c2c = new HashMap(); 
	static {
		_c2c.put(HashSet.class,HashSet.class);
		_c2c.put(Image.class,ImageData.class);
		_c2c.put(Dataset.class,DatasetData.class);
		_c2c.put(Project.class,ProjectData.class);
		_c2c.put(Experimenter.class,ExperimenterData.class);
		_c2c.put(ImageAnnotation.class,AnnotationData.class);
		_c2c.put(DatasetAnnotation.class,AnnotationData.class);
		_c2c.put(Category.class,CategoryData.class);
		_c2c.put(CategoryGroup.class, CategoryGroupData.class);
		_c2c.put(ImagePixel.class,PixelsData.class);
		_c2c.put(Group.class, GroupData.class);
    }
	
	protected Map c2c() {
		return _c2c;
	}
	
	public static Map pixelTypesMap = new HashMap();
	
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
	
	static public int getPixelTypeID(String pixelType) {
		if (null==pixelType){
			throw new IllegalArgumentException("Null values not excepted");
		}
		
		if (!pixelTypesMap.containsKey(pixelType.toUpperCase()))
			throw new IllegalArgumentException("Unknown pixel type:"+pixelType);
		
		return ((Integer)pixelTypesMap.get(pixelType.toUpperCase())).intValue();
	}
	
}