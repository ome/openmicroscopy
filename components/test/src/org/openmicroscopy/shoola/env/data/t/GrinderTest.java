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
import org.openmicroscopy.omero.tests.client.OMEData;
import org.openmicroscopy.omero.tests.client.OMEPerformanceData;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DataServicesTestCase;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

/** 
 * 
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
public class GrinderTest
    extends DataServicesTestCase
{
    
    OMEData data;
    GrinderObserver observer = new GrinderObserver();
    HierarchyBrowsingView hbw;
    DataManagementService dms;
    SemanticTypesService sts;
    
    public GrinderTest(){
        this.data = new OMEPerformanceData();
    }
    
    public GrinderTest(OMEData data){
        this.data = data;
    }
    
    protected void setup(){
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
        hbw.loadHierarchy(ProjectData.class, data.prjId , observer);
        return observer.result;
    }
    /***********************************/
    public void testLoadPDIHierarchyDatasetNoReturn() {
        Object obj = testLoadPDIHierarchyDataset();
    }
    public Object testLoadPDIHierarchyDataset(){
        hbw.loadHierarchy(DatasetData.class, data.dsId, observer);
        return observer.result;
    }
    /***********************************/
    public void testLoadCGCIHierarchyCategoryGroupNoReturn() {
        Object obj = testLoadCGCIHierarchyCategoryGroup();
    }
    public Object testLoadCGCIHierarchyCategoryGroup(){
        hbw.loadHierarchy(CategoryGroupData.class, data.cgId, observer);
        return observer.result;
    }
    /***********************************/
    public void testLoadCGCIHierarchyCategoryNoReturn() {
        Object obj = testLoadCGCIHierarchyCategory();
        
    }
    public Object testLoadCGCIHierarchyCategory(){
        hbw.loadHierarchy(CategoryData.class, data.cId, observer);
        return observer.result;
    }
    /***********************************/
    public void testFindCGCIHierarchiesNoReturn() {
        Object obj = testFindCGCIHierarchies(); 
    }
    public Object testFindCGCIHierarchies(){
        hbw.findCGCIHierarchies(data.imgsCGCI, observer);
        return observer.result;
    }
    /***********************************/
    public void testFindPDIHierarchiesNoReturn() {
        Object obj = testFindPDIHierarchies();
    }
    public Object testFindPDIHierarchies(){
        hbw.findPDIHierarchies(data.imgsPDI, observer);
        return observer.result;
    }
    /***********************************/
    public void testFindImageAnnotationsSetNoReturn() {
        Object obj = testFindImageAnnotationsSet();
    }
    public Object testFindImageAnnotationsSet(){
        //
        return observer.result;
    }
    /***********************************/
    public void testFindImageAnnotationsSetForExperimenterNoReturn() {
        Object obj = testFindImageAnnotationsSetForExperimenter();
    }
    public Object testFindImageAnnotationsSetForExperimenter(){
        //
        return observer.result;
    }
    /***********************************/
    public void testFindDatasetAnnotationsSetNoReturn() {
        Object obj = testFindDatasetAnnotationsSet();
    }
    public Object testFindDatasetAnnotationsSet(){
        //
        return observer.result;
    }
    /***********************************/
    public void testFindDatasetAnnotationsSetForExperimenterNoReturn() {
        Object obj = testFindDatasetAnnotationsSetForExperimenter();
    }
    public Object testFindDatasetAnnotationsSetForExperimenter(){
        return observer.result;
    }
    /***********************************/
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
