/*
 * Copyright 2012 Glencoe Software, Inc. All rights reserved.
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

package ome.services.blitz.test;

import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ome.api.IUpdate;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.services.blitz.impl.commands.SaveI;
import ome.system.EventContext;
import omero.api.SaveRsp;
import omero.cmd.Chgrp;
import omero.cmd.DoAllRsp;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.State;
import omero.cmd._HandleTie;
import omero.cmd.basic.DoAllI;
import omero.cmd.graphs.ChgrpFacadeI;
import omero.model.DatasetI;
import omero.model.ImageI;

import org.testng.annotations.Test;

/**
 * Tests around the changing of group-based permissions.
 *
 * @see ticket:2874
 * @see 4.4.0
 * @see https
 *      ://www.openmicroscopy.org/site/community/minutes/minigroup/2012.03.12
 *      -groupperms
 */
@Test(groups = { "integration", "doall" })
public class DoAllITest extends AbstractGraphTest {

    static class Data {
        Dataset d;
        Image i;

        public Data(ManagedContextFixture user) {
            d = new Dataset("OldDoAllData");
            i = new Image();
            i.setName("DoAllData");
            d.linkImage(i);
            IUpdate update = user.managedSf.getUpdateService();
            d = update.saveAndReturnObject(d);
            i = d.linkedImageList().get(0);
        }
    }

    Request chgrp(long imageID, long groupID) {
        ChgrpFacadeI chgrp = (ChgrpFacadeI) ic.findObjectFactory(Chgrp.ice_staticId())
                .create("");
        chgrp.type = "/Image";
        chgrp.id = imageID;
        chgrp.options = null;
        chgrp.grp = groupID;
        return chgrp;
    }

    /**
     *
     * @param newGroupID
     * @param data
     * @return
     */
    SaveI addImageToNewDataset(long newGroupID, Image i) throws Exception {
        DatasetI d = new DatasetI();
        d.setName(rstring("NewDoAllData"));
        d.linkImage(new ImageI(i.getId(), false));
        SaveI save = new SaveI();
        save.obj = d;
        return save;
    }

    @Test
    public void testNoSteps() throws Exception {
        Request cs1 = new CheckSteps("1");
        DoAllI all = new DoAllI(ctx);
        all.requests = Arrays.asList(cs1);
        _HandleTie handle = submit(all);
        block(handle, 5, 1000);
        assertFlag(handle, State.CANCELLED);
    }

    @Test
    public void testSteps() throws Exception {
        Request cs1 = new CheckSteps("1", 0, 1, 2, 3, 4);
        Request cs2 = new CheckSteps("2", 0);
        Request cs3 = new CheckSteps("3", 0); // Can't be nothing.
        Request cs4 = new CheckSteps("4", 0, 1, 2, 3, 4, 5, 6, 7, 8);
        Request cs5 = new CheckSteps("5", 0);
        DoAllI all = new DoAllI(ctx);
        all.requests = Arrays.asList(cs1, cs2, cs3, cs4, cs5);

        _HandleTie handle = submit(all);
        block(handle, 5, 1000);
        assertSuccess(handle);

    }

    @Test
    public void testSimple() throws Exception {
        EventContext before = user.getCurrentEventContext();
        long userID = before.getCurrentUserId();
        long newGroupID = root.newGroup();
        root.addUserToGroup(userID, newGroupID);
        EventContext after = user.getCurrentEventContext(); // Refresh

        assertEquals(before.getCurrentGroupId(), after.getCurrentGroupId());
        Data data = new Data(user); // Data in oldGroupID
        DoAllI all = new DoAllI(ctx);
        Request chgrp = chgrp(data.i.getId(), newGroupID); // Image in newGroupID
        Request save = addImageToNewDataset(newGroupID, data.i);
        all.requests = Arrays.asList(chgrp, save);

        _HandleTie handle = submit(all, newGroupID); // Login to newGroupID
        block(handle, 5, 1000);
        DoAllRsp rsp = (DoAllRsp) assertSuccess(handle);
        assertSuccess(rsp.responses.get(0));

        // Specifically check the save
        SaveRsp saveRsp = (SaveRsp) rsp.responses.get(1);
        assertSuccess(saveRsp);
        assertEquals(newGroupID,
                saveRsp.obj.getDetails().getGroup().getId().getValue());
    }

    class CheckSteps extends Request implements IRequest {

        private static final long serialVersionUID = 1L;

        private Helper helper;

        private List<Integer> expected;

        private List<Object> responses = new ArrayList<Object>();

        private String name;

        CheckSteps(String name, int... expected) {
            this.name = name;
            this.expected = new ArrayList<Integer>();
            for (int i = 0; i < expected.length; i++) {
                this.expected.add(expected[i]);
            }
        }

        public Map<String, String> getCallContext() {
            return null;
        }

        public void init(Helper helper) throws Cancel {
            this.helper = helper;
            this.helper.setSteps(this.expected.size());
        }

        public Object step(int i) throws Cancel {
            int j = expected.remove(0);
            if (j != i) {
                throw new RuntimeException(String.format(
                        "[check:%s] Received: %s. Expected: %s", name, i, j));
            }
            return null;
        }

        public void finish() {
            // no-op
        }

        public void buildResponse(int i, Object object) {
            if (responses.size() != i) {
                throw new RuntimeException(String.format(
                        "[check:%s] Response count: %s. Expected: %s", name,
                        responses.size(), i));
            }
            responses.add(object);
            if (expected.size() != 0) {
                throw new RuntimeException(String.format(
                        "[check:%s] leftovers: %s", name, expected));
            }
        }

        public Response getResponse() {
            return null;
        }

    }

}
