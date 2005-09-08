/*
 * org.openmicroscopy.shoola.env.data.t.TestExample
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

package org.openmicroscopy.shoola.env.data.t;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import ome.testing.HierarchyBrowsingTests;
import ome.testing.OMEData;
import ome.testing.OMEPerformanceData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DataServicesTestCase;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/** 
 * used externally by Grinder to test call times in a manner similar to
 * Omero. See {@link ome.testing.OmeroGrinderTest}.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/05/20 13:32:12 $)
 * </small>
 * @since OME2.2
 */
public class ShoolaGrinderTest
    extends DataServicesTestCase
    implements HierarchyBrowsingTests
{
    private static Log log = LogFactory.getLog(ShoolaGrinderTest.class);
    
    OMEData data;
    GrinderObserver observer = new GrinderObserver();
    HierarchyBrowsingView hbw;
    DataManagementService dms;
    SemanticTypesService sts;
    //OMEDSGateway gateway;
    ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
            new String[] {
                    "ome/client/itests/test.xml",
                    "ome/client/itests/data.xml",
                    "ome/client/spring.xml"});

    public ShoolaGrinderTest(){
        this(new OMEPerformanceData());
    }
    
    public ShoolaGrinderTest(OMEData data){
        this.data = data;
        ((OMEPerformanceData)data).setDataSource((DataSource) appContext.getBean("dataSource"));
        data.init();
    }
    
    protected void setUp(){
        super.setUp();
        hbw = (HierarchyBrowsingView)
        registry.getDataServicesView(HierarchyBrowsingView.class);
        dms = registry.getDataManagementService();
        sts = registry.getSemanticTypesService();
    }
    /***********************************/
    public void testLoadPDIHierarchyProjectNoReturn() {
        Object obj = testLoadPDIHierarchyProject();
    }
    public Object testLoadPDIHierarchyProject()
    {
    	try {
    		setUp();
    		return dms.retrieveProjectTree(data.prjId,false);
    	} catch (Throwable t){
    		throw new RuntimeException(t);
    	}

    }
    /***********************************/
    public void testLoadPDIHierarchyDatasetNoReturn() {
        Object obj = testLoadPDIHierarchyDataset();
    }
    public Object testLoadPDIHierarchyDataset(){
    	try {
    		setUp();
    		return dms.retrieveDatasetTree(data.dsId,false);
    	} catch (Throwable t){
    		throw new RuntimeException(t);
    	}
    }
    /***********************************/
    public void testLoadPDIAnnotatedHierarchyProjectNoReturn() {
        Object obj = testLoadPDIAnnotatedHierarchyProject();
    }
    public Object testLoadPDIAnnotatedHierarchyProject()
    {	
    	try {
    		setUp();
    		return dms.retrieveProjectTree(data.prjId,true);
    	} catch (Throwable t){
    		throw new RuntimeException(t);
    	}
    }
    /***********************************/
    public void testLoadPDIAnnotatedHierarchyDatasetNoReturn() {
        Object obj = testLoadPDIAnnotatedHierarchyDataset();
    }
    public Object testLoadPDIAnnotatedHierarchyDataset(){
    	try {
    		setUp();
    		return dms.retrieveDatasetTree(data.dsId,true);
    	} catch (Throwable t){
    		throw new RuntimeException(t);
    	}

    }
    /***********************************/
    public void testLoadCGCIHierarchyCategoryGroupNoReturn() {
        Object obj = testLoadCGCIHierarchyCategoryGroup();
    }
    public Object testLoadCGCIHierarchyCategoryGroup(){
        setUp();
        try {
            Object rootNode = sts.retrieveCategoryGroupTree(data.cgId, -1, false);
            if (rootNode==null) throw new RuntimeException("null in loadcgci-cg");
            return rootNode;
        } catch (Exception e){
            throw new RuntimeException(e	);
        }
        
    }
    /***********************************/
    public void testLoadCGCIHierarchyCategoryNoReturn() {
        Object obj = testLoadCGCIHierarchyCategory();
    }
    public Object testLoadCGCIHierarchyCategory(){
        setUp();
        try {
            Object rootNode = sts.retrieveCategoryTree(data.cId, -1, false);
            if (rootNode==null) throw new RuntimeException("null in loadcgci-c");
            return rootNode;
        } catch (Exception e){
            throw new RuntimeException(e	);
        }    
     }
    /***********************************/
    public void testLoadCGCIAnnotatedHierarchyCategoryGroupNoReturn() {
        Object obj = testLoadCGCIAnnotatedHierarchyCategoryGroup();
    }
    public Object testLoadCGCIAnnotatedHierarchyCategoryGroup(){
        setUp();
        try {
            Object rootNode = sts.retrieveCategoryGroupTree(data.cgId, -1, true);
            if (rootNode==null) throw new RuntimeException("null in loadcgci-cg-annotated");
            return rootNode;
        } catch (Exception e){
            throw new RuntimeException(e	);
        }        
    }
    /***********************************/
    public void testLoadCGCIAnnotatedHierarchyCategoryNoReturn() {
        Object obj = testLoadCGCIAnnotatedHierarchyCategory();
    }
    public Object testLoadCGCIAnnotatedHierarchyCategory(){
        setUp();
        try {
            Object rootNode = sts.retrieveCategoryTree(data.cId, -1, true);
            if (rootNode==null) throw new RuntimeException("null in loadcgci-c-annotated");
            return rootNode;
        } catch (Exception e){
            throw new RuntimeException(e	);
        }         }
    /***********************************/
    public void testFindCGCIHierarchiesNoReturn() {
        Object obj = testFindCGCIHierarchies(); 
    }
    public Object testFindCGCIHierarchies(){
        setUp();
        Set images = getImageSummariesFromIds(data.imgsCGCI);
        hbw.findCGCIHierarchies(images, observer);
        return observer.result;
    }
    /***********************************/
    public void testFindPDIHierarchiesNoReturn() {
        Object obj = testFindPDIHierarchies();
    }
    public Object testFindPDIHierarchies(){
        setUp();
        Set images = getImageSummariesFromIds(data.imgsPDI);
        hbw.findPDIHierarchies(images, observer);
        return observer.result;
    }
    /***********************************/
	public void testFindCGCPathsContainedNoReturn() throws Exception {
		// TODO Auto-generated method stub
		//
		throw new RuntimeException("Not implemented yet.");
	}

	public Object testFindCGCPathsContained() {
		// TODO Auto-generated method stub
		//return null;
		throw new RuntimeException("Not implemented yet.");
	}
    /***********************************/
	public void testFindCGCPathsNotContainedNoReturn() throws Exception {
		// TODO Auto-generated method stub
		//
		throw new RuntimeException("Not implemented yet.");
	}

	public Object testFindCGCPathsNotContained() {
		// TODO Auto-generated method stub
		//return null;
		throw new RuntimeException("Not implemented yet.");
	}
    /***********************************/
    public void testFindImageAnnotationsSetNoReturn() {
        Object obj = testFindImageAnnotationsSet();
    }
    public Object testFindImageAnnotationsSet() {
        try {
    	setUp();
        Map result = new HashMap();
        for (Iterator iter = data.imgsAnn1.iterator(); iter.hasNext();) {
            Integer i = (Integer) iter.next();
            result.put(i,sts.getImageAnnotations(i.intValue()));
        }
        return result;
        } catch (Throwable t){
        	throw new RuntimeException(t);
        }
    }
    /***********************************/
    public void testFindImageAnnotationsSetForExperimenterNoReturn() {
        Object obj = testFindImageAnnotationsSetForExperimenter();
    }
    public Object testFindImageAnnotationsSetForExperimenter(){
        setUp();
        try {
            Object result = sts.getImageAnnotations(new ArrayList(data.imgsAnn2),data.userId);
            if (null==result) throw new RuntimeException ("no return imgannforexp");
            return result;
        } catch (Exception e){
            throw new RuntimeException("Error getting Image annotations", e);
        }
    }
    /***********************************/
    public void testFindDatasetAnnotationsSetNoReturn() {
        Object obj = testFindDatasetAnnotationsSet();
    }
    public Object testFindDatasetAnnotationsSet() {
        try {
    	setUp();
        Map result = new HashMap();
        for (Iterator iter = data.imgsAnn1.iterator(); iter.hasNext();) {
            Integer i = (Integer) iter.next();
            result.put(i,sts.getDatasetAnnotations(i.intValue()));
        }
        return result;
        } catch (Throwable t){
        	throw new RuntimeException(t);
        }
        }
    /***********************************/
    public void testFindDatasetAnnotationsSetForExperimenterNoReturn() {
        Object obj = testFindDatasetAnnotationsSetForExperimenter();
    }
    public Object testFindDatasetAnnotationsSetForExperimenter(){
        setUp();
        try {
            Object result = sts.getDatasetAnnotations(new ArrayList(data.dsAnn2),data.userId);
            if (null==result) throw new RuntimeException ("no return dsannforexp");
            return result;
        } catch (Exception e){
            throw new RuntimeException("Error getting dataset annotations", e);
        }
    }
    /***********************************/

    /*
     * 
     *  UTILS
     * 
     */
    Set getImageSummariesFromIds(Set ids) {
        Set images = new HashSet();
        for (Iterator iter = ids.iterator(); iter.hasNext();) {
            Integer i = (Integer) iter.next();
            images.add(new ImageSummary(i.intValue(),"", (int[])null ));
        }
        return images;
    }

}

class GrinderObserver extends DSCallAdapter{
    public Object result;
    public void handleException(Throwable exc)
    {
        throw new RuntimeException("Unexpected exception.", exc);
    }
    public void handleNullResult()
    {
        throw new RuntimeException("Shouldn't have returned null.");
    }

    public void handleResult(Object result)
    {
        this.result = result;
    }
}
