package ome.services.utests;

import org.testng.annotations.*;
import java.util.Set;

import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.services.util.CountCollector;

import junit.framework.TestCase;


public class CountCollectorTest extends TestCase
{

    protected CountCollector c;
   
    protected long current = 0;
    
    protected Long next() { return current++; }
    
  @Test
    public void testSingleField() throws Exception
    {
        c = new CountCollector(new String[]{Dataset.ANNOTATIONS});
        
        Project p = new Project(next());
        Dataset d = new Dataset(next());
        p.linkDataset(d);
        
        c.collect(p);
        Set s = (Set) c.getIds(Dataset.ANNOTATIONS);
        
        assertTrue(s.contains(d.getId()));
    }
    
  @Test
    public void testMultipleFields() throws Exception
    {
        c = new CountCollector(new String[]{Dataset.IMAGELINKS,Image.ANNOTATIONS});
        
        Project p = new Project(next());
        Dataset d = new Dataset(next());
        p.linkDataset(d);
        
        Image i = new Image(next());
        d.linkImage(i);
        
        ImageAnnotation iann = new ImageAnnotation(next());
        i.addToAnnotations( iann );
        
        c.collect(p);
        Set s_1 = (Set) c.getIds(Dataset.IMAGELINKS);
        Set s_2 = (Set) c.getIds(Image.ANNOTATIONS);
        
        assertTrue(s_1.contains(d.getId()));
        assertTrue(s_2.contains(i.getId()));
        
    }
    
  @Test
    public void testMultipleIdsInOneField() throws Exception
    {
        
        c = new CountCollector(new String[]{Image.CATEGORYLINKS});
        
        Dataset d = new Dataset(next());
        Image i1 = new Image(next());
        Image i2 = new Image(next());
        d.linkImage(i1);
        d.linkImage(i2);
        
        c.collect(d);
        Set s = (Set) c.getIds(Image.CATEGORYLINKS);
        assertTrue(s.size() == 2);
        assertTrue(s.contains(i1.getId()));
        assertTrue(s.contains(i2.getId()));
    }
    
  @Test
    public void testLookupTablesCreated() throws Exception
    {
         c = new CountCollector(new String[]{Project.DATASETLINKS});
         
         Project p = new Project(next());
         c.collect(p);
         c.addCounts(Project.DATASETLINKS,p.getId(),10L);
    }
    
  @Test
    public void testNoCountGiven() throws Exception
    {
        c = new CountCollector(new String[]{Project.DATASETLINKS});
        
        Project p = new Project(next());
        c.collect(p);
        c.addCounts(Project.DATASETLINKS,p.getId(),null);
    }    

  @Test
    public void testNegativeCountGiven() throws Exception
    {
        c = new CountCollector(new String[]{Project.DATASETLINKS});
        
        Project p = new Project(next());
        c.collect(p);
        c.addCounts(Project.DATASETLINKS,p.getId(),-10L);
    }    
    
  @Test
    public void testWhatHappensOnNullIdThough() throws Exception
    {
        c = new CountCollector(new String[]{Project.DATASETLINKS});
        
        Project p = new Project();
        
        c.collect(p);
    }
    
}
