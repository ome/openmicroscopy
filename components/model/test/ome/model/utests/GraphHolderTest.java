/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.utests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import ome.model.acquisition.Laser;
import ome.model.internal.GraphHolder;

public class GraphHolderTest{

    GraphHolder gh;

    @Test
    public void testShouldNeverBeNull() throws Exception {
        Laser dl = new Laser();
        Assert.assertNotNull(dl.getGraphHolder());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dl);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Laser test = (Laser) ois.readObject();
        baos.close();
        oos.close();
        bais.close();
        ois.close();

        Assert.assertNotNull(test.getGraphHolder());
    }
}
