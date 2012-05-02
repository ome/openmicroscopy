/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.update;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import ome.model.IObject;
import ome.model.core.Channel;
import ome.model.core.Dataset;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.parameters.Parameters;
import ome.testing.ObjectFactory;

import org.testng.annotations.Test;

public class PostAnnotationsUpdateTest extends AbstractUpdateTest {

    @Test
    public void testUpdateReloadsAfterMerge() {
        Pixels pix = ObjectFactory.createPixelGraph(null);
        java.sql.Timestamp testTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
        Image i = new Image();
        i.setAcquisitionDate(testTimestamp);
        i.setName("size");
        Dataset d = new Dataset("size");
        i.setPixels(pix);
        i = iUpdate.saveAndReturnObject(i);
        assertNotNull(i.getPixels());
        Pixels p = i.getPixels();
        if (p.sizeOfChannels() < 0) {
            throw new RuntimeException("channel");
        }

        d.linkImage(i);
        d = iUpdate.saveAndReturnObject(d);

        Set<IObject> s = iContainer.loadContainerHierarchy(Dataset.class,
                Collections.singleton(d.getId()), new Parameters().leaves());
        d = (Dataset) s.iterator().next();
        i = d.linkedImageList().get(0);
        p = i.getPixels();
        assertTrue(p.isLoaded());
        // assertTrue(p.unmodifiableChannels().iterator().next().isLoaded());

        s = iContainer.loadContainerHierarchy(Dataset.class, Collections
                .singleton(d.getId()), new Parameters().noLeaves());
        d = (Dataset) s.iterator().next();
        assertTrue(d.sizeOfImageLinks() < 0);

    }

    @Test
    public void testUpdateReloadsAfterMergeOfArray() {
        java.sql.Timestamp testTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
        Image[] imgs = new Image[2];
        Image i = new Image();
        i.setAcquisitionDate(testTimestamp);
        i.setName("arr");
        imgs[0] = i;
        imgs[0].setPixels(ObjectFactory.createPixelGraph(null));
        i = new Image();
        i.setAcquisitionDate(testTimestamp);
        i.setName("arr");
        imgs[1] = i;
        imgs[1].setPixels(ObjectFactory.createPixelGraph(null));

        IObject[] objs = iUpdate.saveAndReturnArray(imgs);
        imgs[0] = (Image) objs[0];
        imgs[1] = (Image) objs[1];
        assertTrue(imgs[0].isLoaded());
        assertTrue(imgs[0].getPixels().isLoaded());
        Iterator<Channel> j = imgs[0].getPixels().iterateChannels();
        while (j.hasNext()) {
			assertTrue(j.next().isLoaded());
		}
        

    }

}
