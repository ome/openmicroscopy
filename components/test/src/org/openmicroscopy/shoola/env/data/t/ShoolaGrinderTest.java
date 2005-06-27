/*
 * org.openmicroscopy.shoola.env.data.t.TestExample
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

package org.openmicroscopy.shoola.env.data.t;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.tests.OMEPerformanceData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.OMEDSInfo;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DataServicesTestCase;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.map.CategoryMapper;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/** 
 * used externally by Grinder to test call times in a manner similar to
 * Omero. See {@link org.openmicroscopy.omero.test.OmeroGrinderTest}.
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
                    "org/openmicroscopy/omero/client/itests/test.xml",
                    "org/openmicroscopy/omero/client/itests/data.xml",
                    "org/openmicroscopy/omero/client/spring.xml"});

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
//		//Retrieve the connection URL and create the internal proxy to OMEDS.
//		OMEDSInfo info = (OMEDSInfo) registry.lookup(LookupNames.OMEDS);
//		if (info == null)  //TODO: get rid of this when we have an XML schema.
//			throw new NullPointerException("No data server host provided!");
//        gateway = new OMEDSGateway(info.getServerAddress(), this);
    }
    /***********************************/
    public void testLoadPDIHierarchyProjectNoReturn() {
        Object obj = testLoadPDIHierarchyProject();
    }
    public Object testLoadPDIHierarchyProject()
    {
        setUp();
        hbw.loadHierarchy(ProjectSummary.class, data.prjId , observer);
        return observer.result;
    }
    /***********************************/
    public void testLoadPDIHierarchyDatasetNoReturn() {
        Object obj = testLoadPDIHierarchyDataset();
    }
    public Object testLoadPDIHierarchyDataset(){
        setUp();
        hbw.loadHierarchy(DatasetSummaryLinked.class, data.dsId, observer);
        return observer.result;
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

    /**
     * @throws DSAccessException
     * @throws DSOutOfServiceException*********************************/
    public void testFindImageAnnotationsSetNoReturn() throws DSOutOfServiceException, DSAccessException {
        Object obj = testFindImageAnnotationsSet();
    }
    public Object testFindImageAnnotationsSet() throws DSOutOfServiceException, DSAccessException{
        setUp();
        Map result = new HashMap();
        for (Iterator iter = data.imgsAnn1.iterator(); iter.hasNext();) {
            Integer i = (Integer) iter.next();
            result.put(i,sts.getImageAnnotations(i.intValue()));
        }
        return result;
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
    /**
     * @throws DSAccessException
     * @throws DSOutOfServiceException*********************************/
    public void testFindDatasetAnnotationsSetNoReturn() throws DSOutOfServiceException, DSAccessException {
        Object obj = testFindDatasetAnnotationsSet();
    }
    public Object testFindDatasetAnnotationsSet() throws DSOutOfServiceException, DSAccessException{
        setUp();
        Map result = new HashMap();
        for (Iterator iter = data.imgsAnn1.iterator(); iter.hasNext();) {
            Integer i = (Integer) iter.next();
            result.put(i,sts.getDatasetAnnotations(i.intValue()));
        }
        return result;
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
