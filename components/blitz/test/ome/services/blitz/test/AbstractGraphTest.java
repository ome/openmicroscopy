/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.services.delete.DeleteStepFactory;
import ome.services.graphs.BaseGraphSpec;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphState;
import ome.services.util.Executor;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.hibernate.ExtendedMetadata;
import omero.RLong;
import omero.RType;
import omero.ServerError;
import omero.api.AMD_IDelete_queueDelete;
import omero.api.IDeletePrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.cmd.Chgrp;
import omero.cmd.ERR;
import omero.cmd.HandleI;
import omero.cmd.OK;
import omero.cmd.RequestObjectFactoryRegistry;
import omero.cmd.Response;
import omero.cmd.State;
import omero.cmd.graphs.ChgrpI;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.ExperimenterGroupI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.Plate;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.Well;
import omero.model.WellI;
import omero.model.WellSample;
import omero.model.WellSampleI;
import omero.sys.ParametersI;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.InvocationMatcher;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


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

    protected void block(HandleI handle, int loops, long pause)
            throws InterruptedException {
        for (int i = 0; i < loops && null == handle.getResponse(); i++) {
            Thread.sleep(pause);
        }
    }

    protected void assertSuccess(HandleI handle) {
        Response rsp = handle.getResponse();
        assertNotNull(rsp);
        if (rsp instanceof ERR) {
            ERR err = (ERR) rsp;
            fail(err.category + ":" + err.name + ":" + err.parameters);
        }
        assertFalse(handle.getStatus().flags.contains(State.FAILURE));
    }

    protected void assertFailure(HandleI handle) {
        Response rsp = handle.getResponse();
        assertNotNull(rsp);
        if (rsp instanceof OK) {
            OK ok = (OK) rsp;
            fail(ok.toString());
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

}
