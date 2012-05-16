/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Mock;
import org.testng.annotations.BeforeClass;

import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;

import omero.RType;
import omero.api.IDeletePrx;
import omero.cmd.ERR;
import omero.cmd.HandleI;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.RequestObjectFactoryRegistry;
import omero.cmd.Response;
import omero.cmd.State;
import omero.cmd.Status;
import omero.cmd._HandleTie;
import omero.sys.ParametersI;


/**
 * Tests call to {@link IDeletePrx}, especially important for testing the
 * {@link IDeletePrx#queueDelete(omero.api.delete.DeleteCommand[]) since it is
 * not available from {@link ome.api.IDelete}
 */
public class AbstractGraphTest extends AbstractServantTest {

    Mock adapterMock;

    Ice.Communicator ic;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        adapterMock = (Mock) user.ctx.getBean("adapterMock");
        adapterMock.setDefaultStub(new FakeAdapter());
        ic = ctx.getBean("Ice.Communicator", Ice.Communicator.class);

        // Register ChgrpI, etc. This happens automatically on the server.
        RequestObjectFactoryRegistry rofr = new RequestObjectFactoryRegistry(
                user.ctx.getBean(ExtendedMetadata.class),
                user.ctx.getBean(Roles.class)
                );
        rofr.setApplicationContext(ctx);
        rofr.setIceCommunicator(ic);
    }

    //
    // Helpers
    //

    protected _HandleTie submit(IRequest req) throws Exception {
        return submit(req, null);
    }

    protected _HandleTie submit(IRequest req, long groupID) throws Exception {
        Map<String, String> callContext = new HashMap<String, String>();
        callContext.put("omero.group", ""+groupID);
        return submit(req, callContext);
    }

    protected _HandleTie submit(IRequest req, Map<String, String> callContext) throws Exception {
        Ice.Identity id = new Ice.Identity("handle", req.toString());
        HandleI handle = new HandleI(1000, callContext);
        handle.setSession(user.sf);
        handle.initialize(id, req);
        handle.run();
        // Client side this would need a try/finally { handle.close() }
        return new _HandleTie(handle);
    }

    protected void block(_HandleTie handle, int loops, long pause)
            throws InterruptedException {
        for (int i = 0; i < loops && null == handle.getResponse(); i++) {
            Thread.sleep(pause);
        }
    }

    protected Response assertSuccess(_HandleTie handle) {
        Response rsp = handle.getResponse();
        Status status = handle.getStatus();
        assertSuccess(rsp);
        assertFalse(status.flags.contains(State.FAILURE));
        return rsp;
    }

    protected Response assertSuccess(Response rsp) {
        assertNotNull(rsp);
        if (rsp instanceof ERR) {
            ERR err = (ERR) rsp;
            fail(printErr(err));
        }
        return rsp;
    }

    protected void assertFailure(_HandleTie handle, String...allowedMessages) {
        final List<String> msgs = Arrays.asList(allowedMessages);
        final Response rsp = handle.getResponse();
        assertNotNull(rsp);
        if (rsp instanceof OK) {
            OK ok = (OK) rsp;
            fail(ok.toString());
        } else {
            ERR err = (ERR) rsp;
            if (msgs.size() > 0) {
                assertTrue(String.format("%s not in %s: %s", err.name,
                        msgs, printErr(err)), msgs.contains(err.name));
            }
        }

        assertTrue(handle.getStatus().flags.contains(State.FAILURE));
    }

    protected void assertDoesExist(String table, long id) throws Exception {
        List<List<RType>> ids = assertProjection(
                "select x.id from " +table+" x where x.id = :id",
                new ParametersI().addId(id));
        assertEquals(1, ids.size());
    }

    protected void assertDoesNotExist(String table, long id) throws Exception {
        List<List<RType>> ids = assertProjection(
                "select x.id from " +table+" x where x.id = :id",
                new ParametersI().addId(id));
        assertEquals(0, ids.size());
    }

    private String printErr(ERR err) {
        StringBuilder sb = new StringBuilder();
        sb.append(err.toString());
        sb.append("\n");
        sb.append("==========================================\n");
        sb.append("category=");
        sb.append(err.category);
        sb.append("\n");
        sb.append("name=");
        sb.append(err.name);
        sb.append("\n");
        sb.append("params=");
        sb.append(err.parameters);
        sb.append("\n");
        sb.append("==========================================\n");
        return sb.toString();
    }

}
