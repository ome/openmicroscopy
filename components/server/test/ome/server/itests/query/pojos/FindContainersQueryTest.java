/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query.pojos;

import java.sql.Timestamp;
import java.util.Collections;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.testing.ObjectFactory;

import org.testng.annotations.Test;

@Test(groups = { "ticket:735" })
public class FindContainersQueryTest extends AbstractManagedContextTest {
    PojosLoadHierarchyQueryDefinition q;

    @Test(groups = "ticket:735")
    public void testCheckWithoutPixels() throws Exception {
        long id = createImage();
        this.iContainer.findContainerHierarchies(Project.class, Collections
                .singleton(id), null);
    }

    @Test(groups = "ticket:735")
    public void testCheckWithPixels() throws Exception {
        long id = createImage();
        createPixelsFromImage(id, false);
        this.iContainer.findContainerHierarchies(Project.class, Collections
                .singleton(id), null);
    }

    @Test(groups = "ticket:735")
    public void testCheckWithDefaultPixels() throws Exception {
        long id = createImage();
        createPixelsFromImage(id, true);
        this.iContainer.findContainerHierarchies(Project.class, Collections
                .singleton(id), null);
    }

    private long createImage() {
        String name = "find containers query test";
        Image i = new Image();
        i.setName(name);
        Dataset d = new Dataset();
        d.setName(name);
        i.linkDataset(d);
        return this.iContainer.createDataObject(i, null).getId().longValue();
    }

    private long createPixelsFromImage(long imageId, boolean def) {
        Image i = iContainer.getImages(Image.class, Collections.singleton(imageId),
                null).iterator().next();
        Pixels pix = ObjectFactory.createPixelGraph(null);
        i.addPixels(pix);
        i = this.iContainer.updateDataObject(i, null);
        pix = i.getPixels(i.sizeOfPixels() - 1);
        return pix.getId().longValue();
    }

}
