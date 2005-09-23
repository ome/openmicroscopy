/*
 * ome.server.dbtests.HierarchyBrowsingDbUnitTest
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
package ome.server.dbtests;

//Java imports
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Third-party libraries
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;

//Application-internal dependencies
import ome.model.CategoryGroup;
import ome.server.itests.*;

/**
 * @author josh
 * @DEV.TODO test "valid=false" sections of queries
 */
public class CGCPathsDbUnitTest extends AbstractDbUnitTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(CGCPathsDbUnitTest.class);
    }

    @Override
    public IDataSet getData() throws Exception {
        URL file = this.getClass().getClassLoader().getResource("cgc-paths.xml");
        return new XmlDataSet(new FileInputStream(file.getFile()));
    }

    public void testFindCGCPathsContained(){
    	
    	Set set,contained,notContained;
    	
    	set = TestUtils.getSetFromInt(new int[]{2});
        contained= new HashSet(cdao.findCGCPaths(set,true));
        notContained = new HashSet(cdao.findCGCPaths(set,false));
        assertTrue("X not-contained paths expected in but found none",notContained.size()>0);
        assertTrue("X contained paths expected but found none",contained.size()>0);
        
    }
}
