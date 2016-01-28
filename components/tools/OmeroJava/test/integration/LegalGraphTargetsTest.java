/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration;

import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import omero.cmd.Chgrp2;
import omero.cmd.Chmod2;
import omero.cmd.Chown2;
import omero.cmd.Delete2;
import omero.cmd.LegalGraphTargets;
import omero.cmd.LegalGraphTargetsResponse;
import omero.cmd.SkipHead;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test which model object classes are reported as being legal as targets for a request.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.4
 */
public class LegalGraphTargetsTest extends AbstractServerTest {
    /**
     * Test a model object class that is expected to be legal for {@link Chgrp2}.
     * @throws Exception unexpected
     */
    @Test
    public void testInclusionChgrp() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new Chgrp2();
        final LegalGraphTargetsResponse response = (LegalGraphTargetsResponse) doChange(query);
        Assert.assertTrue(response.targets.contains(Image.class.getName()));
    }

    /**
     * Test a model object class that is not expected to be legal for {@link Chgrp2}.
     * @throws Exception unexpected
     */
    @Test
    public void testExclusionChgrp() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new Chgrp2();
        final LegalGraphTargetsResponse response = (LegalGraphTargetsResponse) doChange(query);
        Assert.assertFalse(response.targets.contains(Experimenter.class.getName()));
    }

    /**
     * Test a model object class that is expected to be legal for {@link Chmod2}.
     * @throws Exception unexpected
     */
    @Test
    public void testInclusionChmod() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new Chmod2();
        final LegalGraphTargetsResponse response = (LegalGraphTargetsResponse) doChange(query);
        Assert.assertTrue(response.targets.contains(ExperimenterGroup.class.getName()));
    }

    /**
     * Test a model object class that is not expected to be legal for {@link Chmod2}.
     * @throws Exception unexpected
     */
    @Test
    public void testExclusionChmod() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new Chmod2();
        final LegalGraphTargetsResponse response = (LegalGraphTargetsResponse) doChange(query);
        Assert.assertFalse(response.targets.contains(Image.class.getName()));
    }

    /**
     * Test a model object class that is expected to be legal for {@link Chown2}.
     * @throws Exception unexpected
     */
    @Test
    public void testInclusionChown() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new Chown2();
        final LegalGraphTargetsResponse response = (LegalGraphTargetsResponse) doChange(query);
        Assert.assertTrue(response.targets.contains(Image.class.getName()));
    }

    /**
     * Test a model object class that is not expected to be legal for {@link Chown2}.
     * @throws Exception unexpected
     */
    @Test
    public void testExclusionChown() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new Chown2();
        final LegalGraphTargetsResponse response = (LegalGraphTargetsResponse) doChange(query);
        Assert.assertFalse(response.targets.contains(Experimenter.class.getName()));
    }

    /**
     * Test a model object class that is expected to be legal for {@link Delete2}.
     * @throws Exception unexpected
     */
    @Test
    public void testInclusionDelete() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new Delete2();
        final LegalGraphTargetsResponse response = (LegalGraphTargetsResponse) doChange(query);
        Assert.assertTrue(response.targets.contains(Image.class.getName()));
    }

    /**
     * Test a model object class that is not expected to be legal for {@link Delete2}.
     * @throws Exception unexpected
     */
    @Test
    public void testExclusionDelete() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new Delete2();
        final LegalGraphTargetsResponse response = (LegalGraphTargetsResponse) doChange(query);
        Assert.assertFalse(response.targets.contains(Experimenter.class.getName()));
    }

    /**
     * Test that {@link SkipHead} cannot be queried for legal targets.
     * @throws Exception unexpected
     */
    @Test
    public void testSkipHeadFails() throws Exception {
        final LegalGraphTargets query = new LegalGraphTargets();
        query.request = new SkipHead();
        doChange(client, factory, query, false);
    }
}
