/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import ome.icy.fixtures.BlitzServerFixture;
import ome.services.blitz.Main;
import ome.services.blitz.Status;

import org.testng.annotations.Test;

public class StatusTest extends MockedBlitzTest {

    BlitzServerFixture fixture;

    @Test
    public void testStatus() throws Exception {

        fixture = new BlitzServerFixture();

        String[] args = new String[] {};
        Status status = new Status(args);
        status.run();
    }

    @Test
    public void testManualShutdownLikeSignal() throws Exception {

        class Fixture extends BlitzServerFixture {
            void kill() {
                super.m.shutdown(); // Simulates a SIGINT or SIGTERM
            }
        }
        fixture = new Fixture();
        ((Fixture) fixture).kill();

    }

    @Test
    public void testWaitingForInput() throws Exception {

        // blocks Main.main(new String[] {});

    }
}
