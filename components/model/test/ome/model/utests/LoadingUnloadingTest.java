/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.utests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.model.meta.Event;
import ome.util.ModelMapper;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoadingUnloadingTest{

    Project p;

    Dataset d;

    Image i;

    Pixels pix;

    @BeforeMethod
    protected void setUp() throws Exception {
        p = new Project();
        d = new Dataset();
        i = new Image();
        pix = new Pixels();
    }

    @Test
    public void test_unloaded_allow_only_ID_accessors() throws Exception {

        p.unload();
        p.getId();
        p.setId(null);
        try_and_fail(p.fields());

    }

    @Test(expectedExceptions = Error.class)
    public void test_model_mapping_events() throws Exception {
        ModelMapper m = new ModelMapper() {
            @Override
            protected Map c2c() {
                return new HashMap();
            }
        };

        Event e = new Event(new Long(1), false);

        m.event2timestamp(e);

    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAddingUnloadedToCollection() throws Exception {

        // this needs to be unallowed, because hibernate only saves the
        // non-inverse
        // (tb->pixels) side of this relation. sending an unloaded thumbnail
        // attached to a Pixels is a waste of bandwidth.
        Pixels p = new Pixels();
        Thumbnail tb = new Thumbnail(1L, false);
        p.addThumbnail(tb);

    }

    // ~ Helpers
    // =========================================================================

    private void try_and_fail(Set strings) {
        for (Iterator it = strings.iterator(); it.hasNext();) {
            String field = (String) it.next();
            // The counts are always available, skip them
            if ("ome.model.containers.Project_annotationLinksCountPerOwner".equals(field)) {
                continue;
            }
            if ("ome.model.containers.Project_datasetLinksCountPerOwner".equals(field)) {
                continue;
            }
            try {
                p.retrieve(field);
                if (!Project.ID.equals(field)) {
                    Assert.fail("Should have thrown an exception on:" + field);
                }
            } catch (IllegalStateException stateExc) {
                if (Project.ID.equals(field)) {
                    Assert.fail("Should NOT throw an exception on id");
                }
            }

        }
    }

}
