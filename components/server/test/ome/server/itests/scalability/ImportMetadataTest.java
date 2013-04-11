/*
 *   Copyright (C) 2009-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.scalability;

import java.util.Arrays;
import java.util.List;

import ome.model.IObject;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.testing.ObjectFactory;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
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
        StopWatch sw = new Slf4JStopWatch("test.import.save");
        List<Long> ids = iUpdate.saveAndReturnIds(data());
        Long[] ids2 = new Long[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            ids2[i] = ids.get(i);
        }
        iQuery.findAllByQuery("select p from Pixels p where p.image.id in (:list)",
                new Parameters().addList("list", Arrays.<Long>asList(ids2)));
        sw.stop();
    }

    public void testMerge() {
        StopWatch sw = new Slf4JStopWatch("test.import.merge");
        iUpdate.saveAndReturnArray(data());
        sw.stop();
    }

}
