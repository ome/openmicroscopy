/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses.test;

import java.util.Properties;

import junit.framework.TestCase;
import ome.services.blitz.client.IceServiceFactory;
import ome.services.blitz.tasks.BlitzTask;
import ome.services.licenses.tasks.Run;
import ome.system.ServiceFactory;

import org.testng.annotations.Test;

@Test
public class ResetLicensesTest extends TestCase {

    @Test(groups = { "client", "blitz", "integration" })
    public void testUseBlitzDecision() {
        Run.main(new String[] { "blitz=true",
                "task=ome.services.licenses.test.ResetLicensesTest$TaskTest" });
    }

    public static class TaskTest extends BlitzTask {

        public TaskTest(ServiceFactory serviceFactory, Properties properties) {
            super(serviceFactory, properties);
        }

        public TaskTest(IceServiceFactory serviceFactory, Properties properties) {
            super(serviceFactory, properties);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void doTask() {
            super.doTask();
            assert useBlitz;
        }

    }
}
