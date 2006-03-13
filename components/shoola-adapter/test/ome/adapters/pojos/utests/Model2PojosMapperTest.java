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
import java.util.Set;

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import ome.adapters.pojos.Model2PojosMapper;
import ome.model.containers.Category;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

import pojos.DataObject;
import pojos.DatasetData;
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
    ReverseModelMapper reverse;
	Project p;
	Dataset d1,d2,d3;
	Image i1,i2,i3;
	
	protected void setUp() throws Exception {
		mapper=new Model2PojosMapper();
        reverse = new ReverseModelMapper();
		p = new Project(new Long(1));
		d1 = new Dataset(new Long(2));
		d2 = new Dataset(new Long(3));
		d3 = new Dataset(new Long(4));
		i1 = new Image(new Long(5));
		i2 = new Image(new Long(6));
		i3 = new Image(new Long(7));
		p.linkDataset( d1 );
        p.linkDataset( d2 );
        p.linkDataset( d3 );
		d1.linkImage( i1 );
        d1.linkImage( i2 );
        d1.linkImage( i3 );

	}
	
	public void test(){
		ProjectData pd = (ProjectData) mapper.map(p);
		assertNotNull(pd.getDatasets());
		assertFalse(pd.getDatasets().size()==0);
		assertFalse(pd.getDatasets().iterator().next().getClass()==Dataset.class);
		System.out.println(pd);
	}
	
    public void testEmptyClassificationsBug(){
        Category c = new Category();
        mapper.map(c);
    }
    
    public void testReverseMapping() throws Exception
    {
        ProjectData p = new ProjectData();
        DatasetData d = new DatasetData();
        
        p.setDatasets( new HashSet() );
        p.getDatasets().add( d );
        
        d.setProjects( new HashSet() );
        d.getProjects().add( p );
        
        reverse.map( p );
        
    }
    
    public void testNoDuplicateLinks() throws Exception
    {
        DataObject dO = (DataObject) mapper.map( p );
        Project p = (Project) reverse.map( dO );
        Dataset d = (Dataset) p.linkedDatasetList().get(0);
        
        Set p_links = new HashSet( p.collectDatasetLinks( null ));
        Set d_links = new HashSet( d.collectProjectLinks( null ));
        
        System.out.println( p_links );
        System.out.println( d_links );
        
        assertTrue( p_links.containsAll( d_links ));
        
        DataObject d00 = (DataObject) mapper.map( d );
        Dataset d2 = (Dataset) reverse.map( d00 );
        Image i2 = (Image) d2.linkedImageList().get(0); // TODO something weird here!
        
        Set d2_links = new HashSet( d2.collectImageLinks( null ));
        Set i2_links = new HashSet( i2.collectDatasetLinks( null ));
        
        System.out.println( d2_links );
        System.out.println( i2_links );
        
        assertTrue( d2_links.containsAll( i2_links ) );
        
    }
    
}

