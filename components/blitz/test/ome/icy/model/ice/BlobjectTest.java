/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.ice;

import java.util.HashMap;
import java.util.Map;

import ome.icy.model.itests.IceTest;
import ome.services.blitz.util.CommonsLoggingAdapter;
import omero.ResourceError;
import omero.api.IQueryPrx;
import omero.api.IQueryPrxHelper;
import omero.model.IObject;
import omero.model.ImageI;
import omero.util.ObjectFactoryRegistrar;

import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Ice.AMD_Object_ice_invoke;
import Ice.Blobject;
import Ice.BlobjectAsync;
import Ice.ByteSeqHolder;
import Ice.Current;
import Ice.Util;

public class BlobjectTest extends IceTest {

    static class S {
        static volatile int PORT = 19998;

        Ice.Communicator ic;

        Ice.ObjectAdapter oa;

        int port;

        void shutdown() {
            ic.shutdown();
        }
    }

    class B extends Blobject {

        @Override
        public boolean ice_invoke(byte[] inParams, ByteSeqHolder outParams,
                Current current) {
            System.err.println("Blobject called.");
            System.err.println(current.operation);
            System.err.println(current.requestId);
            System.err.println(current.id.category);
            System.err.println(current.id.name);
            System.err.println(current.facet);
            System.err.println(current.con._toString());
            System.err.println(current.mode);
            System.err.println(current.ctx);

            if (current.operation.equals("find")) {
                Ice.Communicator communicator = current.adapter
                        .getCommunicator();
                Ice.InputStream in = Ice.Util.createInputStream(communicator,
                        inParams);
                String x = in.readString();
                long y = in.readLong();
                Ice.OutputStream out = Ice.Util
                        .createOutputStream(communicator);
                try {
                    if (false /* exceptional case */) {
                        ResourceError re = new ResourceError();
                        out.writeException(re);
                        outParams.value = out.finished();
                        return false;
                    } else {
                        IObject obj = new ImageI();
                        out.writeObject(obj);
                        out.writePendingObjects();
                        outParams.value = out.finished();
                        return true;
                    }
                } finally {
                    out.destroy();
                }
            } else {
                Ice.OperationNotExistException ex = new Ice.OperationNotExistException();
                ex.id = current.id;
                ex.facet = current.facet;
                ex.operation = current.operation;
                throw ex;
            }
        }
        // also ice_id, ice_ids (with ::Ice::Object), ice_isA(checkedCast),
        // ice_ping
    }

    class BA extends BlobjectAsync {

        @Override
        public void ice_invoke_async(AMD_Object_ice_invoke cb, byte[] inParams,
                Current current) {
            Ice.Communicator _ic = current.adapter.getCommunicator();
            Ice.OutputStream out = Ice.Util.createOutputStream(_ic);
            out.writeObject(new ImageI());
            out.writePendingObjects();
            cb.ice_response(true, out.finished());
            // out.writeException(new ResourceError());
            // cb.ice_response(false, out.finished());
            out.destroy();
        }

    }

    protected synchronized S s(Ice.Object obj) throws Exception {

        S s = new S();
        s.port = S.PORT++;

        Map<String, String> context = new HashMap<String, String>();

        int status = 0;
        Ice.InitializationData id = new Ice.InitializationData();
        id.logger = new CommonsLoggingAdapter(LogFactory
                .getLog("port" + s.port));
        id.properties = Ice.Util.createProperties();
        String endpoint = "tcp -p " + s.port + " -h 127.0.0.1";
        id.properties.setProperty("OA.EndPoints", endpoint);
        id.properties.setProperty("Ice.ImplicitContext", "Shared");
        s.ic = Ice.Util.initialize(id);
        s.ic.getImplicitContext().setContext(context);
        ObjectFactoryRegistrar.registerObjectFactory(s.ic,
                ObjectFactoryRegistrar.INSTANCE);
        s.oa = s.ic.createObjectAdapterWithEndpoints("OA", endpoint);
        s.oa.add(obj, Util.stringToIdentity("B"));
        s.oa.activate();

        return s;
    }

    S s1, s2;
    IQueryPrx query, aQuery;

    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        s1 = s(new BA());
        s2 = s(new B());

        String proxyStr = "B:tcp -p " + s2.port + " -h 127.0.0.1";
        String asyncProxyStr = "B:tcp -p " + s1.port + " -h 127.0.0.1";

        Ice.ObjectPrx prx = s1.ic.stringToProxy(proxyStr);
        query = IQueryPrxHelper.uncheckedCast(prx);

        prx = s2.ic.stringToProxy(asyncProxyStr);
        aQuery = IQueryPrxHelper.uncheckedCast(prx);

    }

    @Test
    public void testSync() throws Exception {
        query.find("Image", 1L);
    }

    @Test
    public void testAsync() throws Exception {
        aQuery.find("Image", 1L);
    }

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        s1.shutdown();
        s1 = null;

        s2.shutdown();
        s2 = null;
    }

}
