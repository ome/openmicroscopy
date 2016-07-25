/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package integration;


import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import omero.ServerError;
import omero.cmd.CmdCallbackI;
import omero.cmd.DoAll;
import omero.cmd.ERR;
import omero.cmd.HandlePrx;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.Status;
import omero.cmd.Timing;
import omero.sys.EventContext;

import org.testng.Assert;
import org.testng.annotations.Test;

import Ice.Current;

/**
 * Various uses of the {@link omero.cmd.CmdCallbackI} object.
 */
@SuppressWarnings("serial")
public class CmdCallbackTest extends AbstractServerTest {

    class TestCB extends CmdCallbackI {

        final CountDownLatch finished = new CountDownLatch(1);

        final AtomicInteger steps = new AtomicInteger();

        public TestCB(omero.client client, HandlePrx handle) throws ServerError {
            super(client, handle);
        }

        @Override
        public void step(int complete, int total, Current __current) {
            steps.incrementAndGet();
        }

        @Override
        public void onFinished(Response rsp, Status s, Current __current) {
            finished.countDown();
        }

        public void assertSteps(int expected) {
            Assert.assertEquals(expected, steps.get());
        }

        public void assertFinished() {
            Assert.assertEquals(0, finished.getCount());
            Assert.assertFalse(isCancelled());
            Assert.assertFalse(isFailure());
            Response rsp = getResponse();
            if (rsp == null) {
                Assert.fail("null response");
            } else if (rsp instanceof ERR) {
                ERR err = (ERR) rsp;
                String msg = String.format("%s\ncat:%s\nname:%s\nparams:%s\n",
                        err, err.category, err.name, err.parameters);
                Assert.fail(msg);
            }
        }

        public void assertFinished(int expectedSteps) {
            assertFinished();
            assertSteps(expectedSteps);
        }

        public void assertCancelled() {
            Assert.assertEquals(0, finished.getCount());
            Assert.assertTrue(isCancelled());
        }
    }

    TestCB run(Request req) throws Exception {
        EventContext ec = newUserAndGroup("rw----");
        loginUser(ec);
        HandlePrx handle = client.getSession().submit(req);
        return new TestCB(client, handle);
    }

    // Timing
    // =========================================================================

    TestCB timing(int millis, int steps) throws Exception {
        Timing t = new Timing();
        t.millisPerStep = millis;
        t.steps = steps;
        return run(t);
    }

    @Test
    public void testTimingFinishesOnLatch() throws Exception {
        TestCB cb = timing(25, 4 * 10); // Runs 1 second
        cb.finished.await(1500, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, cb.finished.getCount());
        cb.assertFinished(10); // Modulus-10
    }

    @Test
    public void testTimingFinishesOnBlock() throws Exception {
        TestCB cb = timing(25, 4 * 10); // Runs 1 second
        cb.block(1500);
        cb.assertFinished(10); // Modulus-10
    }

    @Test
    public void testTimingFinishesOnLoop() throws Exception {
        TestCB cb = timing(25, 4 * 10); // Runs 1 second
        cb.loop(3, scalingFactor);
        cb.assertFinished(10); // Modulus-10
    }

    // DoAll
    // =========================================================================

    TestCB doAllOfNothing() throws Exception {
        return run(new DoAll());
    }

    TestCB doAllTiming(int count) throws Exception {
        Timing[] timings = new Timing[count];
        for (int i = 0; i < count; i++) {
            timings[i] = new Timing(3, 2); // 6 ms
        }
        return run(new DoAll(Arrays.<Request> asList(timings), null));
    }

    @Test
    public void testDoNothingFinishesOnLatch() throws Exception {
        TestCB cb = doAllOfNothing();
        cb.finished.await(5, TimeUnit.SECONDS);
        cb.assertCancelled();
    }

    @Test
    public void testDoNothingFinishesOnLoop() throws Exception {
        TestCB cb = doAllOfNothing();
        cb.loop(5, scalingFactor);
        cb.assertCancelled();
    }

    @Test
    public void testDoAllTimingFinishesOnLoop() throws Exception {
        TestCB cb = doAllTiming(5);
        cb.loop(5, scalingFactor);
        cb.assertFinished();
        // For some reason the number of steps is varying between 10 and 15
    }

}
