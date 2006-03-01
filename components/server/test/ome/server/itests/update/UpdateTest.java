package ome.server.itests.update;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ome.model.acquisition.AcquisitionContext;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.CodomainMapContext;
import ome.model.display.Color;
import ome.model.display.PlaneSlicingContext;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.security.CurrentDetails;
import ome.testing.ObjectFactory;

public class UpdateTest extends AbstractUpdateTest
{

    public void testSaveSimpleObject() throws Exception
    {
        Pixels p = ObjectFactory.createPixelGraph(null);
        p = (Pixels) iUpdate.saveAndReturnObject(p); 
        flush();

        Set logs = CurrentDetails.getCreationEvent().getLogs();
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
    
    public void test_index_save() throws Exception
    {

        PlaneSlicingContext enhancement = new PlaneSlicingContext();
        enhancement.setConstant( Boolean.FALSE );
        enhancement.setLowerLimit( new Integer(1) );
        enhancement.setPlanePrevious( new Integer(1) );
        enhancement.setPlaneSelected( new Integer(1) );
        enhancement.setUpperLimit( new Integer(1) );
        
        // Prereqs for binding
        Color color = new Color();
        color.setAlpha( new Integer(1) );
        color.setBlue( new Integer(1) );
        color.setGreen( new Integer(1) );
        color.setRed( new Integer(1) );
        
        Family family = new Family();
        family.setValue( "test" );
        
        ChannelBinding binding = new ChannelBinding();
        binding.setActive( Boolean.FALSE );
        binding.setCoefficient( new Double(1) );
        binding.setColor( color );
        binding.setFamily( family );
        binding.setInputEnd( new Float(1.0) );
        binding.setInputStart( new Float(1.0) );
        binding.setNoiseReduction( Boolean.FALSE );
        
        // Prereqs for RenderingDef
        RenderingModel model = new RenderingModel();
        model.setValue( "test" );
        
        QuantumDef qdef = new QuantumDef();
        qdef.setBitResolution( new Integer(1) );
        qdef.setCdEnd( new Integer(1) );
        qdef.setCdStart( new Integer(1) );
        
        RenderingDef def = new RenderingDef();
        def.setDefaultT( new Integer(1) );
        def.setDefaultZ( new Integer(1) );
        def.setModel( model );
        def.setPixels( ObjectFactory.createPixelGraph( null ) );
        def.setQuantization( qdef );
        
        // What we're interested in
        List enhancements = Collections.singletonList( enhancement );
        List bindings = Collections.singletonList( binding );

        def.setWaveRendering( bindings );
        def.setSpatialDomainEnhancement( enhancements );

        def = (RenderingDef) iUpdate.saveAndReturnObject( def );
        
    }

    
}
