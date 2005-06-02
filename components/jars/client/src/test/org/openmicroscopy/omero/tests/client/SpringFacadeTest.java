/*
 * Created on Feb 27, 2005
*/
package org.openmicroscopy.omero.tests.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.omero.client.ServiceFactory;
import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.Project;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class SpringFacadeTest extends TestCase {

    ServiceFactory services = new ServiceFactory();
    HierarchyBrowsing srv;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(OmeroPercentTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        srv = services.getHierarchyBrowsingService();  
            
    }

    public void test1() {
           	Object obj = srv.loadPDIHierarchy(Dataset.class, 10);
    }
    
    public void testAgainForId(){
        Map result = srv.findImageAnnotations(new HashSet());
        Iterator i = result.values().iterator();
        while (i.hasNext()){
            System.out.println(i.next());
        }
    }
    
    public void testOnceForSameIDs(){
            Set ids = new HashSet();
            ids.add(new Integer(19));
            ids.add(new Integer(13));
            ids.add(new Integer(11));
            
            Set result = srv.findPDIHierarchies(ids);
            
            // Not to much
            Set test = new HashSet();
            Iterator i = result.iterator();
            while (i.hasNext()){
                Object o = i.next();
                if (o instanceof Image) {
                    test.add(o);
                } else if (o instanceof Dataset) {
                    Dataset dd = (Dataset) o;
                    test.addAll(dd.getImages());
                } else if (o instanceof Project) {
                    Project pd = (Project) o;
                    Iterator p = pd.getDatasets().iterator();
                    while (p.hasNext()){
                        Dataset dd = (Dataset) p.next();
                        test.addAll(dd.getImages());
                    }
                }
            }
            
            return;
            
    }
    
    }
