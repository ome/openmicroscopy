/*
 * Created on Jun 10, 2005
*/
package ome.itests;

import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmicroscopy.shoola.env.data.t.ShoolaGrinderTest;

import ome.testing.OMEData;
import ome.testing.OMEPerformanceData;

/**
 * @author josh
 */
public class TemporyGrinderMain {

    private static Log log = LogFactory.getLog(TemporyGrinderMain.class);

    OMEData data;
    OmeroGrinderTest omero;
    ShoolaGrinderTest shoola;
    DataSource ds;
    
    
    /** move this to static Object method() and use in Grinder */
    public static void main(String[] args) {
        TemporyGrinderMain main = new TemporyGrinderMain();
        main.init();
        main.run();
    }
    
    public void init(){
        try {
        data = new OMEPerformanceData(0.05);
        omero = new OmeroGrinderTest("null");
        ShoolaGrinderTest shoola = new ShoolaGrinderTest(data);
        omero.init();
        ds = (DataSource)omero.getAppContext().getBean("dataSource");
        data.setDataSource(ds);
        data.init();
        omero.setData(data);
        log.info("DATA\n"+data.toString());
        } catch (Exception e){
            log.fatal("Grinder died during initialization.");
        }
    }
    
    public void run(){
    	Set imgs=new HashSet();
    	imgs.add(2);
    	imgs.add(3);
    	data.userId=1;
    	data.imgsPDI=imgs;
       	Object o = omero.testFindPDIHierarchies();
       	log.info(o);
    }
    
    public void run4(){
    	Object contained = omero.testFindCGCPathsContained();
    	Object notContained = omero.testFindCGCPathsNotContained();
    	log.info(contained);
    	log.info(notContained);
    }
    
    public void run3(){
        for (int i = 0; i < 10; i++) {
            init();
            shoola.testFindCGCIHierarchies();
            shoola.testFindPDIHierarchies();
            shoola.testLoadPDIHierarchyProject();
        }
    }
    
    public void run2(){
        for (int i = 0; i < 50; i++) {
            try {
                init();
                data.userId=286033;
                omero.testFindDatasetAnnotationsSetForExperimenter();
                log.error("We reached this point; therefore bug is gone");
            } catch (Exception e){
                //
            }
        }
    }
    
    public void run1(){
        try {
        log.info("Making service call");
        for (int i = 0; i < 5; i++) {
            omero.testFindCGCIHierarchies();
            omero.testFindDatasetAnnotationsSet();
            omero.testFindDatasetAnnotationsSetForExperimenter();
            omero.testFindImageAnnotationsSet();
            omero.testFindImageAnnotationsSetForExperimenter();
            omero.testFindPDIHierarchies();
            omero.testLoadCGCIHierarchyCategory();
            omero.testLoadCGCIHierarchyCategoryGroup();
            omero.testLoadPDIHierarchyDataset();
            omero.testLoadPDIHierarchyProject();
            data=new OMEPerformanceData(0.05);
            data.setDataSource(ds);
            omero.setData(data.init());
            System.out.println(i);
        }
        //log.info("Omero\n"+ComparisonUtils.summary(a));
//        Object b = shoola.testFindDatasetAnnotationsSet();
//        log.info("Shoola\n"+ComparisonUtils.summary(b));
//        ComparisonUtils.compare(a,b);
        } catch (Exception e){
            log.error("Grinder died during run.",e);
        }
    }
    
    Set int2set(int[] ints){
        Set set = new HashSet();
        for (int i = 0; i < ints.length; i++) {
            int j = ints[i];
            set.add(new Integer(j));
        }
        return set;
    }
}
