/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import ome.conditions.ApiUsageException;
import ome.services.procs.Process;
import ome.services.procs.ProcessCallback;
import ome.services.procs.ProcessSkeleton;
import ome.services.procs.Processor;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(groups = {"jobs","ignore"})
public class ProcessSkeletonTest extends MockObjectTestCase {

    private Process process;
    private Processor processor;
    private ProcessCallback callback;

    protected Mock mockCallback, mockProcessor;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        mockProcessor = mock(Processor.class);
        mockCallback = mock(ProcessCallback.class);
        processor = (Processor) mockProcessor.proxy();
        callback = (ProcessCallback) mockCallback.proxy();
        process = new ProcessSkeleton(processor);
    }

    @Test
    public void testRegister() throws Exception {
        assertTrue(process.isActive());
        process.registerCallback(callback);
        mockCallback.expects(once()).method("processCancelled");
        process.cancel();
        assertFalse(process.isActive());
    }

    @Test
    public void testUnRegister() throws Exception {
        process.registerCallback(callback);
        process.unregisterCallback(callback);
        // note: no expectations here.
        process.cancel();
        assertFalse(process.isActive());
    }

    @Test
    public void testFinished() throws Exception {
        process.registerCallback(callback);
        mockCallback.expects(once()).method("processFinished");
        process.finish();
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testState() throws Exception {
        process.registerCallback(callback);
        mockCallback.expects(once()).method("processFinished");
        process.finish();
        assertFalse(process.isActive());
        process.registerCallback(callback);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testStateNoCallbacks() throws Exception {
        assertTrue(process.isActive());
        process.finish();
        assertFalse(process.isActive());
        process.finish();
    }

    @Test
    public void testCallbackThrowsException() throws Exception {
        mockCallback.expects(once()).method("processCancelled").will(
                throwException(new NullPointerException()));
        process.registerCallback(callback);
        process.cancel();
        process.cancel();
    }

}
