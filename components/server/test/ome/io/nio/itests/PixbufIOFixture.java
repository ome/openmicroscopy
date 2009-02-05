/*
 * ome.io.nio.itests.PixbufIOFixture
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.itests;

import java.io.IOException;

import ome.api.IContainer;
import ome.api.local.LocalUpdate;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.testing.ObjectFactory;

/**
 * @author callan
 * 
 */
public class PixbufIOFixture {
    private Pixels pixels;

    private final IContainer pojos;
    private final LocalUpdate updater;

    public PixbufIOFixture(IContainer pojos, LocalUpdate updater) {
        this.updater = updater;
        this.pojos = pojos;
    }

    private void createPixels() {
        Pixels p = ObjectFactory.createPixelGraph(null);
        p.setSizeX(new Integer(64));
        p.setSizeY(new Integer(64));
        p.setSizeZ(new Integer(16));
        p.setSizeC(new Integer(2));
        p.setSizeT(new Integer(10));
        // FIXME: Bit of a hack until the model is updated, the following
        // is a SHA1 of "pixels"
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356");

        Image i = p.getImage();
        i.addPixels(p);
        i = updater.saveAndReturnObject(p.getImage());
        // List<Pixels> plist = (List<Pixels>) pojos.retrieveCollection(image,
        // Image.PIXELS, null);
        pixels = i.getPrimaryPixels();

    }

    public Pixels setUp() throws IOException {
        createPixels();
        return pixels;
    }

    protected void tearDown() {
        // ((LocalUpdate) updater).rollback();
    }
}
