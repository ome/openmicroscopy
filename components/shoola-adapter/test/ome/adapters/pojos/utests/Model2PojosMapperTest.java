/*
 * ome.adapters.pojos.utests.Model2PojosMapper
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
package ome.adapters.pojos.utests;

//Java imports
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import ome.adapters.pojos.Model2PojosMapper;
import ome.model.containers.Category;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.util.ModelMapper;
import pojos.ProjectData;

/**
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class Model2PojosMapperTest extends TestCase {
	
	ModelMapper mapper;
	Project p;
	Dataset d1,d2,d3;
	Image i1,i2,i3;
	
	protected void setUp() throws Exception {
		mapper=new Model2PojosMapper(); 
		p = new Project(new Long(1));
		d1 = new Dataset(new Long(2));
		d2 = new Dataset(new Long(3));
		d3 = new Dataset(new Long(4));
		i1 = new Image(new Long(5));
		i2 = new Image(new Long(6));
		i3 = new Image(new Long(7));
//		p.setDatasets(new HashSet(Arrays.asList(new Dataset[]{d1,d2,d2})));
//		d1.setImages(new HashSet(Arrays.asList(new Image[]{i1})));
//		d2.setImages(new HashSet(Arrays.asList(new Image[]{i2})));
//		d3.setImages(new HashSet(Arrays.asList(new Image[]{i3})));
//		i1.setCreated(new Date());
//		i2.setInserted(new Date());
//		i1.setDatasets(new HashSet(Arrays.asList(new Dataset[]{d1})));
//		i2.setDatasets(new HashSet(Arrays.asList(new Dataset[]{d2})));
//		i3.setDatasets(new HashSet(Arrays.asList(new Dataset[]{d3})));
	}
	
	public void test(){
		ProjectData pd = (ProjectData) mapper.map(p);
		assertNotNull(pd.getDatasets());
		assertFalse(pd.getDatasets().size()==0);
		assertFalse(pd.getDatasets().iterator().next().getClass()==Dataset.class);
		System.out.println(pd);
	}
	
    public void testEmptyClassificationsBug(){
        mapper = new Model2PojosMapper();
        Category c = new Category();
        mapper.map(c);
    }
}

