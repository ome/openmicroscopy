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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.testng.annotations.Test;

import ome.api.IUpdate;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.services.blitz.impl.commands.SaveI;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import omero.api.SaveRsp;
import omero.cmd.Chgrp;
import omero.cmd.ChgrpRsp;
import omero.cmd.DoAllRsp;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.Status;
import omero.cmd._HandleTie;
import omero.cmd.basic.DoAllI;
import omero.cmd.graphs.ChgrpI;
import omero.model.DatasetI;
import omero.util.IceMapper;

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
            i.setAcquisitionDate(new Timestamp(0L));
            d.linkImage(i);
            IUpdate update = user.managedSf.getUpdateService();
            d = update.saveAndReturnObject(d);
            i = d.linkedImageList().get(0);
        }
    }

    ChgrpI chgrp(long imageID, long groupID) {
        ChgrpI chgrp = (ChgrpI) ic.findObjectFactory(Chgrp.ice_staticId())
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
        Dataset d = new Dataset("NewDoAllData");
        d.linkImage(i.proxy());
        IceMapper mapper = new IceMapper();
        DatasetI dI = (DatasetI) mapper.map(d);
        SaveI save = new SaveI();
        save.obj = dI;
        return save;
    }

    @Test
    public void testSteps() throws Exception {
        Request cs1 = new CheckSteps("1", 0, 1, 2, 3, 4);
        Request cs2 = new CheckSteps("2", 0);
        Request cs3 = new CheckSteps("3");
        Request cs4 = new CheckSteps("4", 0, 1, 2, 3, 4, 5, 6, 7, 8);
        Request cs5 = new CheckSteps("5", 0);
        DoAllI all = new DoAllI();
        all.list = Arrays.asList(cs1, cs2, cs3, cs4, cs5);

        _HandleTie handle = submit(all);
        block(handle, 5, 1000);
        assertSuccess(handle);

    }

    @Test
    public void testSimple() throws Exception {
        EventContext ec = user.getCurrentEventContext();
        long userID = ec.getCurrentUserId();
        long newGroupID = root.newGroup();
        root.addUserToGroup(userID, newGroupID);
        user.getCurrentEventContext(); // Refresh

        Data data = new Data(user);
        DoAllI all = new DoAllI();
        Request chgrp = chgrp(data.i.getId(), newGroupID);
        Request save = addImageToNewDataset(newGroupID, data.i);
        all.list = Arrays.asList(chgrp, save);

        _HandleTie handle = submit(all);
        block(handle, 5, 1000);
        DoAllRsp rsp = (DoAllRsp) assertSuccess(handle);
        assertSuccess(rsp.list.get(0));
        assertSuccess(rsp.list.get(1));

    }

    class CheckSteps extends Request implements IRequest {

        private Helper helper;

        private List<Integer> expected;

        private String name;

        CheckSteps(String name, int... expected) {
            this.name = name;
            this.expected = new ArrayList<Integer>();
            for (int i = 0; i < expected.length; i++) {
                this.expected.add(expected[i]);
            }
        }

        public void init(Status status, SqlAction sql, Session session,
                ServiceFactory sf) throws Cancel {
            status.steps = this.expected.size();
            this.helper = new Helper(this, status, sql, session, sf);
        }

        public void step(int i) throws Cancel {
            int j = expected.remove(0);
            if (j != i) {
                throw new RuntimeException(String.format(
                        "[check:%s] Received: %s. Expected: %s", name, i, j));
            }
        }

        public void finish() throws Cancel {
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
