package ome.server.itests.query.pojos;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.testing.CreatePojosFixture;
import ome.testing.ObjectFactory;
import ome.util.IdBlock;
import ome.util.builders.PojoOptions;

@Test
public class LoadContainersQueryTest extends AbstractManagedContextTest
{
    PojosLoadHierarchyQueryDefinition q;
    List list;

    List level2cg;
    List level2p;
	List level1c;
    List level1ds;
    List level0img;
	PojoOptions po10000;
	Parameters filterForUser;
	Parameters noFilter;
	CreatePojosFixture DATA;
        
    @Configuration( beforeTestClass = true )
    public void makePojos() throws Exception
    {
    	try {
    		setUp();
    		loginNewUser();
    		DATA = new CreatePojosFixture( this.factory );
    		DATA.createAllPojos();
    		level2cg = DATA.asIdList( DATA.cgu9991, DATA.cgu9992 );
    		level2p = DATA.asIdList( DATA.pu9991, DATA.pu9992 );
    		level1c = DATA.asIdList( DATA.cu7771, DATA.cu7772 );
    	    level1ds = DATA.asIdList( DATA.du7771, DATA.du7772 );
    	    level0img = DATA.asIdList( DATA.iu5551, DATA.iu5552 );
    
    	    // if something goes wrong and the fixture has root as the
    	    // user, things will be weird.
    	    assertFalse( DATA.e.getId().equals( 
    	    		securitySystem.getSecurityRoles().getRootId()));
    	    
    	    po10000 = new PojoOptions().exp( DATA.e.getId() );
			filterForUser = new Parameters().addOptions( po10000.map() );
			noFilter = new Parameters().addOptions( null );

    	} finally {
    		tearDown();
    	}
    }
        
    protected void creation_fails(Parameters parameters){
        try {
            q= new PojosLoadHierarchyQueryDefinition( // TODO if use lookup, more generic
                    parameters);
            fail("Should have failed!");
        } catch (IllegalArgumentException e) {
        } catch (ApiUsageException e) {
        }
    }
    
    @Test
    public void test_illegal_arguments() throws Exception
    {

        creation_fails( null );
        
        creation_fails(
                new Parameters()
                    .addIds(null) //Null
                    .addOptions(null)
                    .addClass(null)
                );
        
        creation_fails(
                new Parameters()
                .addIds(Arrays.asList( 1 )) // Not long
                .addOptions(null)
                .addClass(Project.class)
            );

        /* TODO currently handled by IPojos
        creation_fails(
                PojosQP.ids( new ArrayList() ), // Empty
                PojosQP.options( null ),
                PojosQP.Class(QP.CLASS, Project.class )
                );
        
        PojoOptions po = new PojoOptions().allExps();
        creation_fails(
                PojosQP.ids( null ), 
                PojosQP.options( po.map() ), // Has to have experimenter
                PojosQP.Class(QP.CLASS, Project.class )
                );
        */
        
    }

    @org.testng.annotations.Test
    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q= new PojosLoadHierarchyQueryDefinition(
                new Parameters()
                .addIds(Arrays.asList( doesntExist )) 
                .addOptions(null)
                .addClass(Project.class)
            );
           
        list = (List) iQuery.execute(q);

        PojoOptions po = new PojoOptions().exp( doesntExist );
        q= new PojosLoadHierarchyQueryDefinition(
                new Parameters()
                .addIds(Arrays.asList( doesntExist )) 
                .addOptions(po.map())
                .addClass(Project.class)
            );
           
        list = (List) iQuery.execute(q);
        
    }

    // =========================================================================
    // =========================================================================
    // ~ UNFILTERED
    // =========================================================================
    // =========================================================================

    @org.testng.annotations.Test
    public void test_retrieve_levels() throws Exception
    {
       
        runLevel( Project.class, level2p, new PojoOptions().noLeaves() );
        check_pd_ids( level1ds);

        runLevel( Project.class, level2p, new PojoOptions().leaves() );
        check_pdi_ids( level1ds, level0img );
        
        runLevel( CategoryGroup.class, level2cg, new PojoOptions().noLeaves() );
        check_cgc_ids( level1c );

        runLevel( CategoryGroup.class, level2cg, new PojoOptions().leaves() );
        check_cgci_ids( level1c, level0img );

        runLevel( Dataset.class, level1ds, new PojoOptions().noLeaves() );
        // TODO why no check?
        
        runLevel( Dataset.class, level1ds, new PojoOptions().leaves() );
        check_di_ids( level0img );
        
        runLevel( Category.class, level1c, new PojoOptions().noLeaves() );
        // TODO why no check?
        
        runLevel( Category.class, level1c, new PojoOptions().leaves() );
        check_ci_ids( level0img );

        
    }

    // =========================================================================
    // =========================================================================
    // ~ FILTERING
    // =========================================================================
    // =========================================================================

    @Test
    public void test_owner_filter_user_obj() throws Exception 
    {
        Parameters ids;
        
        // Belongs to user.
        ids = new Parameters().addIds( DATA.asIdList( DATA.pu9990 ));
        q= new PojosLoadHierarchyQueryDefinition(
                new Parameters( ids ).addAll(noFilter).addClass(Project.class));
           
        list = (List) iQuery.execute(q);
        assertTrue( list.size() > 0 );
        
        q= new PojosLoadHierarchyQueryDefinition(
                new Parameters( ids ).addAll(filterForUser).addClass(Project.class));
           
        list = (List) iQuery.execute(q);
        assertTrue( list.size() > 0 );

    }

    @Test( groups = {"broken","ticket:356"})
    public void test_owner_filter_root_obj() throws Exception 
    {
        Parameters ids;
        
        // Doesn't belong to user.
        ids = new Parameters().addIds( DATA.asIdList(DATA.pr9090));
        q= new PojosLoadHierarchyQueryDefinition(
                new Parameters( ids ).addAll(noFilter).addClass(Project.class));
   
        list = (List) iQuery.execute(q);
        assertTrue( list.size() > 0 );
        
        q= new PojosLoadHierarchyQueryDefinition(
                new Parameters( ids ).addAll(filterForUser).addClass(Project.class));
        
        list = (List) iQuery.execute(q);
        assertTrue( list.size() == 0 );

    }
 
    @Test
    public void test_null_owner_filter() throws Exception 
    {
    
        // Null ids.
        run_null_filter_check_size(Project.class, 3);
        run_null_filter_check_size(CategoryGroup.class, 8);
        run_null_filter_check_size(Dataset.class, 3);
        run_null_filter_check_size(Category.class, 10);
    }

    @Test( groups = "ticket:376" )
    public void test_leaves_handled_properly() throws Exception 
    {
    	PojoOptions 
    	withLeaves = new PojoOptions().leaves(),
    	noLeaves = new PojoOptions().noLeaves();
    	
    	runLevel( Project.class, 
    			Arrays.asList(DATA.pu9992.getId(),DATA.pu9991.getId()), 
    			withLeaves );
    	
    	for (Project project : (List<Project>) list) {
    		for (Dataset dataset : (List<Dataset>)project.linkedDatasetList()) {
				assertTrue(dataset.sizeOfImageLinks()>0);
			}
		}

    	runLevel( Project.class, 
    			Arrays.asList(DATA.pu9992.getId(),DATA.pu9991.getId()), 
    			noLeaves );
    	
    	for (Project project : (List<Project>) list) {
    		for (Dataset dataset : (List<Dataset>)project.linkedDatasetList()) {
				assertTrue(dataset.sizeOfImageLinks()<0);
			}
		}
    	
    	runLevel( Dataset.class, 
    			Arrays.asList(DATA.du7772.getId(),DATA.du7771.getId()), 
    			withLeaves );
    	
		for (Dataset dataset : (List<Dataset>) list) {
			assertTrue(dataset.sizeOfImageLinks()>0);
		}
    
    	runLevel( Dataset.class, 
    			Arrays.asList(DATA.du7772.getId(),DATA.du7771.getId()), 
    			noLeaves );
    	
		for (Dataset dataset : (List<Dataset>) list) {
			assertTrue(dataset.sizeOfImageLinks()<0);
		}
    	
	}
    
    @Test( groups = "ticket:401" )
    public void testReturnsDefaultPixels() throws Exception {
    	PojoOptions 
    	withLeaves = new PojoOptions().leaves();
    	
    	runLevel( Dataset.class, 
    			Arrays.asList(DATA.du7771.getId()), 
    			withLeaves );
    	
    	Long id = null;
    	
    	assertTrue(list.size()>0);
		for (Dataset dataset : (List<Dataset>)list) {
			assertTrue(dataset.sizeOfImageLinks()>0);
			Image image = (Image) dataset.linkedImageList().get(0);
			id = image.getId(); // store for later.
			Pixels p = ObjectFactory.createPixelGraph(null);
			p.setDefaultPixels(Boolean.TRUE);
			p.setImage( new Image(id,false) );
			iUpdate.saveObject(p);
		}

		runLevel( Dataset.class, 
    			Arrays.asList(DATA.du7771.getId()), 
    			withLeaves );
    	
		boolean found = false;
		for (Dataset dataset : (List<Dataset>)list) {
			for (Image image : (List<Image>) dataset.linkedImageList()){
				if (!image.getId().equals(id)) continue;
				found = true;
				assertNotNull(image.getDefaultPixels());
			}
		}
		assertTrue(found);

		
	}
    
    // ~ Helpers
    // =========================================================================

    private void runLevel( Class klass, List ids, PojoOptions po )
    {
        q= new PojosLoadHierarchyQueryDefinition(
                new Parameters()
                    .addIds(ids)
                    .addOptions(po.map())
                    .addClass(klass));
           
        list = (List) iQuery.execute(q);
        
        assertTrue( "Didn't find any results expected results!", 
                list.size() == ids.size() );
    }

    private void check_pd_ids(List ids)
    {
        for (Project prj : (List<Project>) list)
        {
            List datasetIds = prj.eachLinkedDataset( new IdBlock() );
            assertTrue( "And our datasets weren't there", 
                    datasetIds.containsAll( ids ));
        }
    }

    private void check_pdi_ids(List ids1, List ids2)
    {
        check_pd_ids( ids1 );
        for (Project prj : (List<Project>) list)
        {
            for (Dataset ds : (List<Dataset>) prj.eachLinkedDataset(null))
            {
                List imagesIds = ds.eachLinkedImage( new IdBlock() );
                assertTrue( "Missing images", 
                        imagesIds.containsAll( ids2 ));
            }
            
        }
    }

    private void check_di_ids(List ids)
    {
        for (Dataset ds: (List<Dataset>) list)
        {
            List imgIds = ds.eachLinkedImage( new IdBlock() );
            assertTrue( "And our images weren't there", 
                    imgIds.containsAll( ids ));
        }
    }

    private void check_cgc_ids(List ids)
    {
        for (CategoryGroup cg: (List<CategoryGroup>) list)
        {
            List catIds = cg.eachLinkedCategory( new IdBlock() );
            assertTrue( "And our categories weren't there", 
                    catIds.containsAll( ids ));
        }
    }

    private void check_cgci_ids(List ids1, List ids2)
    {
        check_cgc_ids( ids1 );
        for (CategoryGroup cg: (List<CategoryGroup>) list)
        {
            for (Category cat: (List<Category>) cg.eachLinkedCategory(null))
            {
                List imagesIds = cat.eachLinkedImage( new IdBlock() );
                assertTrue( "Missing images", 
                        imagesIds.containsAll( ids2 ));
            }
            
        }
    }
    
    private void check_ci_ids(List ids)
    {
        for (Category cat: (List<Category>) list)
        {
            List imgIds = cat.eachLinkedImage( new IdBlock() );
            assertTrue( "And our images weren't there", 
                    imgIds.containsAll( ids ));
        }
    }
    
    private void run_null_filter_check_size(Class klass, int size)
    {
        q= new PojosLoadHierarchyQueryDefinition(
                new Parameters( filterForUser)
                    .addClass(klass)
                    .addIds(null));
           
        list = (List) iQuery.execute(q);
        assertTrue( String.format(
                "Didn't find all our objects of type %s, %d < %d ", 
                klass.getName(), list.size(),size),
                list.size() >= size );
    }
    
}
