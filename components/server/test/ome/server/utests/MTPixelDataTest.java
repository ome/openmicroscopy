/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import ome.io.nio.BackOff;
import ome.io.nio.ConfiguredTileSizes;
import ome.io.nio.FilePathResolver;
import ome.io.nio.PixelsService;
import ome.io.nio.TileSizes;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.enums.DimensionOrder;
import ome.model.enums.PixelsType;
import ome.model.meta.EventLog;
import ome.services.pixeldata.PersistentEventLogLoader;
import ome.services.pixeldata.PixelDataHandler;
import ome.services.pixeldata.PixelDataThread;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import org.apache.commons.io.FileUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the effects of running multiple pixel data threads
 * at the same time.
 */
@Test(groups = { "pixeldata", "broken" }, timeOut=10000)
public class MTPixelDataTest extends MockObjectTestCase {

    String uuid;
    String path;

    @BeforeMethod
    public void setup() {
        uuid = UUID.randomUUID().toString();
        path = dir(uuid);
    }

    @AfterMethod
    public void teardown() throws Exception {
        FileUtils.deleteDirectory(new File(path));
    }

    public void testBasic() throws Exception {

        final AtomicInteger pixelsId = new AtomicInteger();
        final int numThreads = 4;
        final String path = dir(uuid);

        // MT items
        ExecutorService threads = Executors.newFixedThreadPool(numThreads);

        // nio mocks
        Mock boMock = mock(BackOff.class);
        BackOff backOff = (BackOff) boMock.proxy();
        Mock fprMock = mock(FilePathResolver.class);
        FilePathResolver resolver = (FilePathResolver) fprMock.proxy();
        fprMock.expects(atLeastOnce()).method("getOriginalFilePath")
            .will(returnValue(tiny(path)));
        fprMock.expects(atLeastOnce()).method("getPixelsParams")
            .will(returnValue(new HashMap<String, String>()));

        // nio settings
        TileSizes tileSizes = new ConfiguredTileSizes(5, 5, 10, 10);
        PixelsService service = new PixelsService(path, resolver, backOff, tileSizes, null);

        // session mocks
        Mock mgrMock = mock(SessionManager.class);
        Mock sqlMock = mock(SqlAction.class);
        SessionManager mgr = (SessionManager) mgrMock.proxy();
        Executor ex = new DummyExecutor(null, null, threads);
        SqlAction sql = (SqlAction) sqlMock.proxy();
        sqlMock.expects(atLeastOnce()).method("setStatsInfo").will(
            returnValue(1L));


        // pixeldata
        PersistentEventLogLoader loader = new PersistentEventLogLoader("REPO", numThreads) {
            @Override
            protected  EventLog query()
            {
                long id = (long) pixelsId.incrementAndGet();
                EventLog log = new EventLog();
                log.setEntityId(id);
                return log;
            }
        };

        PixelDataHandler handler = new PixelDataHandler(loader, service) {
            @Override
            protected Pixels getPixels(Long id, ServiceFactory sf)
            {
                Pixels pix = new Pixels(id, true);
                pix.setSizeX(20);
                pix.setSizeY(20);
                pix.setSizeZ(2);
                pix.setSizeC(1);
                pix.setSizeT(2);
                pix.setDimensionOrder(new DimensionOrder("XYZCT"));
                pix.setPixelsType(new PixelsType("int8"));
                pix.addChannel(new Channel());
                return pix;
            }
        };
        handler.setSqlAction(sql);

        PixelDataThread thread = new PixelDataThread(true, mgr, ex, handler,
            new Principal("test"), uuid, numThreads) {
            @Override
            protected void onExecutionException(ExecutionException ee)
            {
                Throwable t = ee.getCause();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new RuntimeException(t);
                }
            }
        };

        // test
        thread.doRun();
    }

    String tiny(String dir) throws Exception {
        File fake = new File(new File(dir), "test&sizeX=20&sizeY=20&sizeZ=2&sizeC=1&sizeT=2&.fake");
        return fake.getAbsolutePath();
    }

    String dir(String uuid) {
        File here = new File(".");
        File target = new File(here, "target");
        File test = new File(target, "test");
        File Pixels = new File(test, uuid);
        if (!Pixels.exists()) {
            Pixels.mkdirs();
        }
        return Pixels.getAbsolutePath();
    }
}
