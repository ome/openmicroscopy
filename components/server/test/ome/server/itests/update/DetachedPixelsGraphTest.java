package ome.server.itests.update;

import java.util.HashSet;
import java.util.Set;

import ome.model.acquisition.AcquisitionContext;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.core.PlaneInfo;

public class DetachedPixelsGraphTest extends AbstractUpdateTest
{

    /** the "original" pixels from which p will be the detached version */
    Pixels example;
    
    /** the test object; a detached pixels object which exactly matches
     * example but has never been associated with a session (this simulates
     * serialization!)
     */
    Pixels p;
    int channelsSizeBefore;
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        example = createPixelGraph(null);
        assertNotNull("need to start off with acq. ctx", example
                .getAcquisitionContext());
        example = (Pixels) _up.saveAndReturnObject(example);
        flush();
        clear();

        prepareCurrentDetails(); // Need new event now

        p = createPixelGraph(example.getId());
        assertTrue("Starting off empty", p.getChannels() != null);
        channelsSizeBefore = p.getChannels().size();
        assertTrue("Starting off empty", channelsSizeBefore > 0);
        
    }

    public void testNewRecursiveEntityFieldOnDetachedPixels() throws Exception
    {
        // PREPARE ----------------------------------------------
        p.setRelatedTo(createPixelGraph(null));
        p = (Pixels) _up.saveAndReturnObject(p);
        flush();
        clear();
        
        // TEST -------------------------------------------------
        assertTrue("Related-to is null",p.getRelatedTo()!=null);
        assertTrue("or it has no id",p.getRelatedTo().getId().longValue()>0);
        
        long id = _jt.queryForLong("select relatedto from pixels where id = ?",
                new Object[]{p.getId()});
        assertTrue("Id *really* has to be there.",
                p.getRelatedTo().getId().longValue()==id);
        
    }
    public void testDetachedRecursiveEntityFieldOnDetachedPixels() throws Exception
    {
        // PREPARE ----------------------------------------------
        // Make field entry; we have to re-do what is done in setup above.
        Pixels example2 = createPixelGraph(null);
        example2 = (Pixels) _up.saveAndReturnObject(example2);
        flush();
        clear();
        prepareCurrentDetails();
        Pixels p2 = createPixelGraph(example2.getId());
        
        p.setRelatedTo(p2);
        p = (Pixels) _up.saveAndReturnObject(p);
        flush();
        clear();
        
        // TEST -------------------------------------------------
        assertTrue("Related-to is null",p.getRelatedTo()!=null);
        assertTrue("and it has no id",p.getRelatedTo().getId().equals(p2.getId()));
        
    }
    
    public void testNewEntityFieldOnDetachedPixels() throws Exception
    {
        // PREPARE ----------------------------------------------
        PixelsDimensions pd = new PixelsDimensions();
        pd.setSizeX(new Float(1));
        pd.setSizeY(new Float(2));
        pd.setSizeZ(new Float(3));
        
        p.setPixelsDimensions(pd);
        p = (Pixels) _up.saveAndReturnObject(p);
        flush();
        clear();
        
        // TEST -------------------------------------------------
        assertTrue("Dimensions is valid.",
                p.getPixelsDimensions()!=null 
                && p.getPixelsDimensions().getId().longValue()>0);
        
    }
    
    public void testUnloadedEntityFieldOnDetachedPixels() throws Exception
    {
        // PREPARE -------------------------------------------------
        //      TODO or bool flag?
        AcquisitionContext ac = new AcquisitionContext();
        ac.setId(-1l);
        p.setAcquisitionContext(ac);
        _up.saveAndReturnObject(p);
        flush();
        clear();
        
        // TEST -------------------------------------------------
        assertNotNull("should be back.", p.getAcquisitionContext());
        assertTrue("and it should have a valid id.", p.getAcquisitionContext()
                .getId().longValue() > 0);
    }

    public void testNulledCollectionFieldOnDetachedPixels() throws Exception
    {
        // PREPARE -------------------------------------------------
        p.setChannels(null);
        p = (Pixels) _up.saveAndReturnObject(p);
        flush();
        clear();
        
        // TEST -------------------------------------------------
        assertTrue("Didn't get re-filled", p.getChannels() != null);
        assertTrue("channel ids aren't the same", 
                equalSets(example.getChannels(),p.getChannels()));
    }
    
    public void testFilteredCollectionFieldOnDetachedPixels() throws Exception
    {
        // PREPARE -------------------------------------------------
        Channel first = (Channel) p.getChannels().iterator().next();
        p.getChannels().remove(first);
        p.getDetails().addFiltered(Pixels.CHANNELS);
        
        // Save and it should be back
        p = (Pixels) _up.saveAndReturnObject(p);
        flush();
        clear();
        
        // TEST -------------------------------------------------        
        int channelsSizeAfter = p.getChannels().size();
        assertTrue("Filtered channels not refilled",channelsSizeAfter == channelsSizeBefore);
        for (Channel c : (Set<Channel>) p.getChannels())
        {
            assertTrue("Channel missing a valid id.", c.getId().longValue() > 0);
        }
        
    }
    
    public void testNewCollectionFieldOnDetachedPixels() throws Exception
    {
        // PREPARE -------------------------------------------------
        Set infos = new HashSet();
        PlaneInfo pi1 = new PlaneInfo(),pi2 = new PlaneInfo();
        infos.add(pi1); infos.add(pi2);
        
        pi1.setExposureTime(new Float(10));
        pi1.setTimestamp(new Float(-11));
        pi2.setExposureTime(new Float(100));
        pi2.setTimestamp(new Float(-193));
        // TODO case of only inverse!
        
        p.setPlaneInfo(infos);
        p = (Pixels) _up.saveAndReturnObject(p);
        flush();
        clear();
        
        // TEST ----------------------------------------------------
        assertTrue("Need two pixInfos, please.",p.getPlaneInfo().size() == 2);
        for (PlaneInfo pi : (Set<PlaneInfo>)p.getPlaneInfo())
        {
            assertTrue("Need an id, please.", pi.getId().longValue()>0);
        }
    }
    
    // TODO need to check that for detached that the version is not 
    // incremented unless we really change something!
    
    // TODO assumptions about Experimenter.version increasing, security, etc.
}
