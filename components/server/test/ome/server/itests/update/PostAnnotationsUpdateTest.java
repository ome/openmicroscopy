/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.update;

import java.util.Collections;
import java.util.Set;

import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.parameters.Parameters;
import ome.testing.ObjectFactory;

import org.testng.annotations.Test;

public class PostAnnotationsUpdateTest extends AbstractUpdateTest {

    @Test
    public void testUpdateReloadsAfterMerge() {
        Pixels pix = ObjectFactory.createPixelGraph(null);
        Image i = new Image("size");
        Dataset d = new Dataset("size");
        i.addPixels(pix);
        i = iUpdate.saveAndReturnObject(i);
        if (i.sizeOfPixels() < 0) {
            throw new RuntimeException("pixlels");
        }
        Pixels p = i.getPrimaryPixels();
        if (p.sizeOfChannels() < 0) {
            throw new RuntimeException("channel");
        }

        d.linkImage(i);
        d = iUpdate.saveAndReturnObject(d);

        Set<IObject> s = iContainer.loadContainerHierarchy(Dataset.class,
                Collections.singleton(d.getId()), new Parameters().leaves());
        d = (Dataset) s.iterator().next();
        i = d.linkedImageList().get(0);
        p = i.getPrimaryPixels();
        assertTrue(p.isLoaded());
        // assertTrue(p.unmodifiableChannels().iterator().next().isLoaded());

        s = iContainer.loadContainerHierarchy(Dataset.class, Collections
                .singleton(d.getId()), new Parameters().noLeaves());
        d = (Dataset) s.iterator().next();
        assertTrue(d.sizeOfImageLinks() < 0);

    }

    @Test
    public void testUpdateReloadsAfterMergeOfArray() {
        Image[] imgs = new Image[2];
        imgs[0] = new Image("arr");
        imgs[0].addPixels(ObjectFactory.createPixelGraph(null));
        imgs[1] = new Image("arr");
        imgs[1].addPixels(ObjectFactory.createPixelGraph(null));

        IObject[] objs = iUpdate.saveAndReturnArray(imgs);
        imgs[0] = (Image) objs[0];
        imgs[1] = (Image) objs[1];
        assertTrue(imgs[0].isLoaded());
        assertTrue(imgs[0].getPrimaryPixels().isLoaded());
        assertTrue(imgs[0].getPrimaryPixels().getPrimaryChannel().isLoaded());

    }

}
