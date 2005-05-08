/*
 * Created on Feb 27, 2005
*/
package org.ome.tests.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ome.omero.client.ServiceFactory;
import org.ome.omero.interfaces.HierarchyBrowsing;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class SpringFacadeTest extends TestCase {

    ServiceFactory services = new ServiceFactory();
    HierarchyBrowsing srv;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SpringFacadeTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        srv = services.getHierarchyBrowsingService();  
            
    }

    public void test1() {
           	DataObject dobj = srv.loadPDIHierarchy(DatasetData.class, 10);
    }
    
    public void test2(){
            Set ids = new HashSet();
            ids.add(new Integer(19));
            ids.add(new Integer(13));
            ids.add(new Integer(11));
            
            Set result = srv.findPDIHierarchies(ids);
            
            // Not to much
            Set test = new HashSet();
            Iterator i = result.iterator();
            while (i.hasNext()){
                DataObject o = (DataObject) i.next();
                if (o instanceof ImageData) {
                    test.add(o);
                } else if (o instanceof DatasetData) {
                    DatasetData dd = (DatasetData) o;
                    test.addAll(dd.images);
                } else if (o instanceof ProjectData) {
                    ProjectData pd = (ProjectData) o;
                    Iterator p = pd.datasets.iterator();
                    while (p.hasNext()){
                        DatasetData dd = (DatasetData) p.next();
                        test.addAll(dd.images);
                    }
                }
            }
            
            return;
            
    }
    
}
