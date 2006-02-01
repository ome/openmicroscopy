package ome.server.itests.update;

import java.util.HashSet;
import java.util.Set;

import org.springframework.orm.hibernate3.HibernateTemplate;

import ome.model.acquisition.AcquisitionContext;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.enums.PIType;
import ome.security.CurrentDetails;

public class UpdateTest extends AbstractUpdateTest
{
    /*
     * TODO Process: 0) AbstractTest 1) need to setup current context 2) create
     * object 3) try to save 4) check to see if saved (completely?) later) try
     * to violate security in "SecurityTest"
     */

    public void testSaveSimpleObject() throws Exception
    {
        // TODO put this into ome.testing.ObjectFactory
        PIType pi = new PIType();
        pi.setValue("hi");

        AcquisitionContext ac = new AcquisitionContext();
        ac.setPhotometricInterpretation(pi);

        Channel c = new Channel();
        c.setIndex(1);

        Pixels p = new Pixels();
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // "pixels"
        p.setAcquisitionContext(ac);
        p.setChannels(new HashSet());
        p.getChannels().add(c);

        p = (Pixels) _up.saveAndReturnObject(p); // TODO we need the id here! saveAndReturn();
        flush();

        Set logs = CurrentDetails.getCreationEvent().getLogs();
        assertTrue(logs.size() > 0);

        // _jt.q TODO
    }

    public void testDetachedEntity() throws Exception
    {
        Pixels p = getSomePixels();
        Long id = p.getId();
        assertNotNull("need to start off with acq. ctx",p.getAcquisitionContext());
        
        p.setAcquisitionContext(new AcquisitionContext(-1l));
        _up.saveObject(p);
        
        p = (Pixels) _qu.getById(Pixels.class,id);
        assertNotNull("it should magically be back",p.getAcquisitionContext());
        
    }
    
    public void testDetachedCollection() throws Exception
    {
        // TODO IQuery doesn't allow us to do limit efficiently (at all?)
        Pixels p = getSomePixels();
        assertTrue("Starting off empty",p.getChannels() != null);
        int sizeBefore = p.getChannels().size();
        p.setChannels(null);
        p = (Pixels) _up.saveAndReturnObject(p);
        assertTrue("Didn't get re-filled",p.getChannels() != null);
        int sizeAfter = p.getChannels().size();
        assertEquals("Refilled to different size!",sizeBefore,sizeAfter);
    }
    
    public void testUpdatingPixelSha() throws Exception
    {
        String test = "changed value";
        Pixels p = getSomePixels();
        p.setSha1(test);
        p = (Pixels) _up.saveAndReturnObject(p);
        assertTrue("Not the same sha?!",test.equals(p.getSha1()));
    }

}
