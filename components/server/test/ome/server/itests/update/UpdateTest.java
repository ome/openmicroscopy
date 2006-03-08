package ome.server.itests.update;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ome.model.acquisition.AcquisitionContext;
import ome.model.containers.Project;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.CodomainMapContext;
import ome.model.display.Color;
import ome.model.display.PlaneSlicingContext;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.security.CurrentDetails;
import ome.testing.ObjectFactory;

public class UpdateTest extends AbstractUpdateTest
{

    public void testSaveSimpleObject() throws Exception
    {
        Pixels p = ObjectFactory.createPixelGraph(null);
        p = (Pixels) iUpdate.saveAndReturnObject(p); 
        flush();

        List logs = CurrentDetails.getCreationEvent().collectFromLogs(null);
        assertTrue(logs.size() > 0);

        Pixels check = (Pixels) iQuery.queryUnique(
                "select p from Pixels p " +
                " left outer join fetch p.acquisitionContext " +
                " left outer join fetch p.channels " +
                "  where p.id = ?",new Object[]{p.getId()});
                
        assertTrue("channel ids differ",equalCollections(p.getChannels(),check.getChannels()));
        assertTrue("acq ctx differ",
                p.getAcquisitionContext().getId().equals(
                        check.getAcquisitionContext().getId()));
    }
    
    public void test_uh_oh_duplicate_rows_0() throws Exception
    {
        String name = "SIMPLE:"+System.currentTimeMillis();
        Project p = new Project();
        p.setName( name );
        p = (Project) iUpdate.saveAndReturnObject( p );
        
        Project compare = (Project) 
            iQuery.getUniqueByFieldILike( Project.class, "name", name);
        
        assertTrue( p.getId().equals( compare.getId() ));
        
        Project send = new Project();
        send.setName( p.getName() );
        send.setId( p.getId() );
        send.setVersion( p.getVersion() ); // This is important.
        send.setDescription( "...test..." );
        Project test = (Project) iUpdate.saveAndReturnObject( send );
        
        assertTrue( p.getId().equals( test.getId() ));
        
        iQuery.getUniqueByFieldILike( Project.class, "name", name);
        
    }
    
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
        
        flush();
        clear();
        
        Experimenter test = (Experimenter) iQuery.queryUnique(
                " select e from Experimenter e join fetch e.defaultGroupLink " +
                " where e.id = ? ", new Object[] { defaultLink.child().getId() });
        assertNotNull(test.getDefaultGroupLink());
        assertTrue(test.getDefaultGroupLink().parent().getName().startsWith("DEFAULT"));
        
    }
    
}
