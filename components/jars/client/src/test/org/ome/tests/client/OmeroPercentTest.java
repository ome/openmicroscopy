/*
 * Created on Feb 27, 2005
 */
package org.ome.tests.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.sql.DataSource;

import org.ome.omero.client.ServiceFactory;
import org.ome.omero.interfaces.HierarchyBrowsing;
import org.springframework.jdbc.core.JdbcTemplate;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

import junit.framework.TestCase;

/** this class is not fully a JUnit test case, but rather
 * is intended for use with Grinder.
 * @author josh
 */
public class OmeroPercentTest extends TestCase {

    ServiceFactory services = new ServiceFactory();
    HierarchyBrowsing hb = services.getHierarchyBrowsingService();
    DataSource ds = (DataSource)SpringTestHarness.ctx.getBean("dataSource");
    
    // Main field
    double percent = 0.05;
    long seed = (new Random()).nextLong();
    Random rnd = new Random(seed);

    // Test data : calculated before to not change times.
    Set allUsers = getAllIds("experimenters","attribute_id");
    Set allImgs = getAllIds("images","image_id");
    Set allDss = getAllIds("datasets","dataset_id");
    Set allPrjs = getAllIds("projects","project_id");
    Set allCgs = getAllIds("category_groups","attribute_id");
    Set allCs = getAllIds("categories","attribute_id");

    int userId = getOneFromCollection(allUsers); // Perhaps generalize on type HOW OFTEN IS THIS USED! Each ONCE ?
    int prjId = getOneFromCollection(allPrjs);
    int dsId = getOneFromCollection(allDss);
    int cgId = getOneFromCollection(allCgs);
    int cId = getOneFromCollection(allCs);
    Set imgsPDI = getPercentOfCollection(allImgs, percent);
    Set imgsCGCI = getPercentOfCollection(allImgs, percent);
    Set imgsAnn1 = getPercentOfCollection(allImgs, percent);
    Set imgsAnn2 = getPercentOfCollection(allImgs, percent);
    Set dsAnn1 = getPercentOfCollection(allDss, percent);
    Set dsAnn2 = getPercentOfCollection(allDss, percent);

    // Messages
    String emptyColl = "collections may not be empty";
    
    public OmeroPercentTest(String name){
        super(name);
    }
    
    public OmeroPercentTest(double percent) {
        this.percent = percent;
    }
    
    public OmeroPercentTest(double percent, long seed){
        this.percent=percent;
        this.seed = seed;
        this.rnd = new Random(seed);
    }

    public void testAll(){
        Object result = testLoadPDIHierarchyProject();
        System.out.println(Utils.fieldCount(result));
        System.out.println(Utils.structureSize(result));
        testLoadPDIHierarchyDataset();
        testLoadCGCIHierarchyCategoryGroup() ;
        testLoadCGCIHierarchyCategory() ;
        testFindCGCIHierarchies() ;
        testFindPDIHierarchies() ;
        testFindImageAnnotationsSet() ;
        testFindImageAnnotationsSetForExperimenter() ;
        testFindDatasetAnnotationsSet() ;
        testFindDatasetAnnotationsSetForExperimenter();
    }
    
    public Object testLoadPDIHierarchyProject() {
        return hb.loadPDIHierarchy(ProjectData.class, prjId);
    }

    public Object testLoadPDIHierarchyDataset() {
        return hb.loadPDIHierarchy(DatasetData.class, dsId);
    }

    public Object testLoadCGCIHierarchyCategoryGroup() {
        return hb.loadCGCIHierarchy(CategoryGroupData.class,cgId);
    }
    
    public Object testLoadCGCIHierarchyCategory() {
        return hb.loadCGCIHierarchy(CategoryData.class,cId);
    }

    public Object testFindCGCIHierarchies() {
        return hb.findCGCIHierarchies(imgsCGCI);
    }
    
    public Object testFindPDIHierarchies() {
        return hb.findPDIHierarchies(imgsPDI);
    }

    public Object testFindImageAnnotationsSet() {
        return hb.findImageAnnotations(imgsAnn1);
    }

    public Object testFindImageAnnotationsSetForExperimenter() {
        return hb.findImageAnnotationsForExperimenter(imgsAnn2, userId);
    }

    public Object testFindDatasetAnnotationsSet() {
        return hb.findDatasetAnnotations(dsAnn1);
    }

    public Object testFindDatasetAnnotationsSetForExperimenter() {
        return hb.findDatasetAnnotationsForExperimenter(dsAnn2, userId);
    }

    private Set getAllIds(String table, String field) {
        JdbcTemplate jt = new JdbcTemplate(ds);
        List rows = jt.queryForList("select "+field+" from "+table);
        Set result = new HashSet();
        for (Iterator i = rows.iterator(); i.hasNext();) {
            Map element = (Map) i.next();
            result.add(element.get(field));
        }
        return result;
    }

    private int getOneFromCollection(final Collection ids) {
        
        if (ids.size()==0){
            throw new IllegalArgumentException(emptyColl);
        }
        
        List ordered = new ArrayList(ids);
        int choice = randomChoice(ids.size());
        return ((Integer)ordered.get(choice)).intValue();
    }

    private Set getPercentOfCollection(final Set ids, double percent) {
        
        if (ids.size()==0){
            throw new IllegalArgumentException(emptyColl);
        }
        
        List ordered = new ArrayList(ids);
        Set result = new HashSet();

        while (result.size() < ids.size() * percent){ 
            int choice = randomChoice(ordered.size());
            result.add(ordered.remove(choice));
        }
        
        return result;
    }
    
    private int randomChoice(int size){
        double value = (size-1) * rnd.nextDouble();
        return (new Double(value)).intValue();
    }

    
}
