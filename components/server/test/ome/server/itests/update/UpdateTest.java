package ome.server.itests.update;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ome.model.acquisition.AcquisitionContext;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.security.CurrentDetails;
import ome.testing.ObjectFactory;

public class UpdateTest extends AbstractUpdateTest
{

    public void testSaveSimpleObject() throws Exception
    {
        Pixels p = ObjectFactory.createPixelGraph(null);
        p = (Pixels) _up.saveAndReturnObject(p); 
        flush();

        Set logs = CurrentDetails.getCreationEvent().getLogs();
        assertTrue(logs.size() > 0);

        Pixels check = (Pixels) _qu.queryUnique(
                "select p from Pixels p " +
                " left outer join fetch p.acquisitionContext " +
                " left outer join fetch p.channels " +
                "  where p.id = ?",new Object[]{p.getId()});
                
        assertTrue("channel ids differ",equalCollections(p.getChannels(),check.getChannels()));
        assertTrue("acq ctx differ",
                p.getAcquisitionContext().getId().equals(
                        check.getAcquisitionContext().getId()));
    }

}
