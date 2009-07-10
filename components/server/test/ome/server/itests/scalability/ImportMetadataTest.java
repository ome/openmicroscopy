package ome.server.itests.scalability;

import java.util.Arrays;

import ome.model.IObject;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.testing.ObjectFactory;

import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.testng.annotations.Test;

@Test(groups = { "integration" })
public class ImportMetadataTest extends AbstractManagedContextTest {

    IObject[] data() {
        IObject[] data = new IObject[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = ObjectFactory.createPixelGraph(null).getImage();
        }
        return data;
    }

    @Test
    public void testSave() {
        StopWatch sw = new CommonsLogStopWatch("test.import.save");
        long[] ids = iUpdate.saveAndReturnIds(data());
        Long[] ids2 = new Long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ids2[i] = Long.valueOf(ids[i]);
        }
        iQuery.findAllByQuery("select p from Pixels p where p.image.id in (:list)",
                new Parameters().addList("list", Arrays.<Long>asList(ids2)));
        sw.stop();
    }

    public void testMerge() {
        StopWatch sw = new CommonsLogStopWatch("test.import.merge");
        iUpdate.saveAndReturnArray(data());
        sw.stop();
    }

}
