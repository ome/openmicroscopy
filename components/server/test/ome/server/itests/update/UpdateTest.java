package ome.server.itests.update;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.CodomainMapContext;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.parameters.Parameters;
import ome.testing.ObjectFactory;

public class UpdateTest extends AbstractUpdateTest
{

    @Test
    public void testSaveSimpleObject() throws Exception
    {
        Pixels p = ObjectFactory.createPixelGraph(null);
        p = (Pixels) iUpdate.saveAndReturnObject(p); 

//        FIXME This can no longer be done this way.
//        List logs = securitySystem.getCurrentEvent().collectLogs(null);
//        assertTrue(logs.size() > 0);

        Pixels check = (Pixels) iQuery.findByQuery(
                "select p from Pixels p " +
                " left outer join fetch p.acquisitionContext " +
                " left outer join fetch p.channels " +
                "  where p.id = :id",new Parameters().addId(p.getId()));
                
        assertTrue("channel ids differ",equalCollections(p.getChannels(),check.getChannels()));
        assertTrue("acq ctx differ",
                p.getAcquisitionContext().getId().equals(
                        check.getAcquisitionContext().getId()));
    }
    
    @Test
    public void test_uh_oh_duplicate_rows_0() throws Exception
    {
        String name = "SIMPLE:"+System.currentTimeMillis();
        Project p = new Project();
        p.setName( name );
        p = (Project) iUpdate.saveAndReturnObject( p );
        
        Project compare = (Project) 
            iQuery.findByString( Project.class, "name", name);
        
        assertTrue( p.getId().equals( compare.getId() ));
        
        Project send = new Project();
        send.setName( p.getName() );
        send.setId( p.getId() );
        send.setVersion( p.getVersion() ); // This is important.
        send.setDescription( "...test..." );
        Project test = (Project) iUpdate.saveAndReturnObject( send );
        
        assertTrue( p.getId().equals( test.getId() ));
        
        iQuery.findByString( Project.class, "name", name);
        
    }
    
    @Test
    public void test_images_pixels() throws Exception
    {
        Image image = new Image();
        image.setName( "test" );
        
        Pixels active = ObjectFactory.createPixelGraph( null );
        active.setImage( image );
        active.setDefaultPixels( true );
        
        active = (Pixels) iUpdate.saveAndReturnObject( active );
        Pixels other = ObjectFactory.createPixelGraph( null );
        other.setImage( active.getImage() );
        
        iUpdate.saveAndReturnObject( other );
        
    }
    
    @Test
    public void test_index_save() throws Exception
    {

        RenderingDef def = ObjectFactory.createRenderingDef();
        CodomainMapContext enhancement = ObjectFactory.createPlaneSlicingContext();
        ChannelBinding binding = ObjectFactory.createChannelBinding();
        
        // What we're interested in
        List enhancements = Collections.singletonList( enhancement );
        List bindings = Collections.singletonList( binding );

        def.setWaveRendering( bindings );
        def.setSpatialDomainEnhancement( enhancements );

        def = (RenderingDef) iUpdate.saveAndReturnObject( def );
        
    }

    @Test
    public void test_index_save_order() throws Exception
    {

        RenderingDef def = ObjectFactory.createRenderingDef();
        
        ChannelBinding binding1 = ObjectFactory.createChannelBinding();
        binding1.setInputStart( new Float(1.0) );
        
        ChannelBinding binding2 = ObjectFactory.createChannelBinding();
        binding2.setInputStart( new Float(2.0) );
        
        ChannelBinding binding3 = ObjectFactory.createChannelBinding();
        binding3.setInputStart( new Float(3.0) );
        
        List bindings = Arrays.asList( binding1, binding2, binding3 );

        def.setWaveRendering( bindings );

        def = (RenderingDef) iUpdate.saveAndReturnObject( def );

    }
    
    @Test
    public void test_experimenters_groups() throws Exception
    {
        Experimenter e = new Experimenter();
        ExperimenterGroup g_1 = new ExperimenterGroup();
        ExperimenterGroup g_2 = new ExperimenterGroup();
        
        e.setOmeName("j.b."+System.currentTimeMillis());
        e.setFirstName(" Joe ");
        e.setLastName(" Brown ");
        
        g_1.setName( "DEFAULT: "+System.currentTimeMillis());
        g_2.setName( "NOTDEFAULT: "+System.currentTimeMillis());
        
        GroupExperimenterMap m_1 = new GroupExperimenterMap();
        m_1.setDefaultGroupLink( true );
        m_1.link( g_1, e );

        GroupExperimenterMap defaultLink = 
            (GroupExperimenterMap) iUpdate.saveAndReturnObject( m_1 );
        
        GroupExperimenterMap m_2 = new GroupExperimenterMap();
        m_2.setDefaultGroupLink( false );
        m_2.link( g_2, defaultLink.child() ); // Need the new exp.id here.

        GroupExperimenterMap notDefaultLink = 
            (GroupExperimenterMap) iUpdate.saveAndReturnObject( m_2 );
        
        Experimenter test = (Experimenter) iQuery.findByQuery(
                " select e from Experimenter e join fetch e.defaultGroupLink " +
                " where e.id = :id ", 
                new Parameters().addId(defaultLink.child().getId()));
        assertNotNull(test.getDefaultGroupLink());
        assertTrue(test.getDefaultGroupLink().parent().getName().startsWith("DEFAULT"));
        
    }
    
    @Test
    /** attempt to reproduce an error seen on the client side */
    public void testSaveArray() throws Exception {
    	
    	loginRoot();
 
    	Long e = -1L;
    	List<Experimenter> es = iQuery.findAll(Experimenter.class, null);
    	for (Experimenter experimenter : es) {
			Long l = experimenter.getId();
			if ( ! l.equals( new Long(0L)  ))
					e = l;
    	}
    	
		Project[] ps = new Project[]{
			new Project(), new Project(), new Project()	
		};
		for (Project project : ps) {
			project.setName("save-array");
		}
		ps[0].getDetails().setOwner( new Experimenter( e, false ));
		ps[1].getDetails().setOwner( new Experimenter( e, false ));
		
		Dataset[] ds = new Dataset[]{
				new Dataset(), new Dataset()
		};
		for (Dataset dataset : ds) {
			dataset.setName("save-array");
		}
		
		for (Dataset dataset : ds) {
			for (Project project : ps) {
				project.linkDataset(dataset);
			}
		}
		
		iUpdate.saveAndReturnArray(ps);
	}
   
}
