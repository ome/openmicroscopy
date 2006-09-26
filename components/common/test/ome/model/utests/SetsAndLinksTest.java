package ome.model.utests;

import org.testng.annotations.*;

import java.util.List;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;

import junit.framework.TestCase;


public class SetsAndLinksTest extends TestCase
{

    Project p;
    Dataset d;
    Image i;
    Pixels pix;
    
  @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        p = new Project();
        d = new Dataset();
        i = new Image();
        pix = new Pixels();
    }
    
  @Test
    public void test_linking() throws Exception
    {
        p.linkDataset( d );
        
        assertTrue( p.linkedDatasetList().size() == 1);
        assertTrue( p.linkedDatasetIterator().next().equals( d ));
        
    }
    
  @Test
    public void test_unlinking() throws Exception
    {
        p.linkDataset( d );
        p.unlinkDataset( d );
        assertTrue( p.linkedDatasetList().size() == 0 );

        p.linkDataset( d );
        p.clearDatasetLinks();
        assertTrue( p.linkedDatasetList().size() == 0 );
        
    }
    
  @Test
    public void test_retrieving() throws Exception
    {
        p.linkDataset( d );
        List l = p.eachLinkedDataset( null );
        assertTrue( l.size() == 1 );
        assertTrue( l.get(0).equals( d ));
    }
    
  @Test
    public void test_adding_a_placeholder() throws Exception
    {
        Project p = new Project();
        Dataset d = new Dataset( new Long(1L), false );
        
        p.linkDataset( d );
    }
  
  @Test( groups = "ticket:60" )
  public void test_cantLinkNullSet() throws Exception
  {
      p.putAt( Project.DATASETLINKS, null); // This is a workaround.
      try { 
          p.linkDataset( d );
          fail("Should not be allowed.");
      } catch (ApiUsageException api) {
          // ok.
      }
  
  }
  
  @Test( groups = "ticket:60" )
  public void test_butWeStillWantToUseUnloadeds() throws Exception
  {
      d.unload();
      p.linkDataset( d );
  }
  
  @Test( groups = "ticket:60" )
  public void test_andTheReverseToo() throws Exception
  {
      d.putAt( Dataset.PROJECTLINKS, null); // This is a workaround.
      try { 
          p.linkDataset( d );
          fail("Should not be allowed.");
      } catch (ApiUsageException api) {
          // ok.
      }
  }
  
  
  // Default Experimenter Group
  @Test
  public void test_one_way_to_default_link() throws Exception
  {
      Experimenter experimenter = new Experimenter();
      ExperimenterGroup defaultGroup = new ExperimenterGroup();
      
      experimenter.linkExperimenterGroup(defaultGroup);
      Set s = experimenter.findGroupExperimenterMap(defaultGroup);
      for (Object o : s)
      {
          GroupExperimenterMap map = (GroupExperimenterMap) o;
          map.setDefaultGroupLink( true );
      }
      testIsDefault(experimenter);
  }

  @Test
  public void test_easier_default_linking() throws Exception
  {
      Experimenter experimenter = new Experimenter();
      ExperimenterGroup defaultGroup = new ExperimenterGroup();
      GroupExperimenterMap map = new GroupExperimenterMap();
      map.link( defaultGroup, experimenter );
      map.setDefaultGroupLink( true );
      experimenter.addGroupExperimenterMap( map, true );
      testIsDefault(experimenter);
  }
  
  @Test( groups = {"broken","ticket:346"} )
  public void testAddingFillsContainer() throws Exception {
	  Pixels p = new Pixels();
	  Thumbnail tb = new Thumbnail();
	  tb.setPixels(p);
	  assertTrue(p.iterateThumbnails().hasNext());
  }
  
  @Test( groups = {"broken","ticket:346"} )
  public void testLinkingFillsContainer() throws Exception {
	  Project p = new Project();
	  Dataset d = new Dataset();
	  ProjectDatasetLink link = new ProjectDatasetLink();
	  link.link(p,d);
	  assertNotNull(link.parent());
	  assertNotNull(link.child());
	  assertTrue(link.parent().sizeOfDatasetLinks()==1);
	  assertTrue(link.child().sizeOfProjectLinks()==1);
  }
  
  // ~ Private helpers
  // ===========================================================================
  private void testIsDefault(Experimenter experimenter)
  {
      assert( Boolean.TRUE.equals(
                ((GroupExperimenterMap)
                experimenter.iterateGroupExperimenterMap().next()).getDefaultGroupLink()));
  }

  
  
}
