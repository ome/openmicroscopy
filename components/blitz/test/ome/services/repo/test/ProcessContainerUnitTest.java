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

package ome.services.repo.test;

import java.util.Arrays;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ome.services.blitz.repo.ProcessContainer;

@Test(groups = { "repo" })
public class ProcessContainerUnitTest extends MockObjectTestCase {

    ProcessContainer pc;

    Mock m;

    ProcessContainer.Process p;

    List<ProcessContainer.Process> procs;

    @BeforeMethod
    public void setUp() {
        procs = null;
        pc = new ProcessContainer();
        m = mock(ProcessContainer.Process.class);
        m.expects(atLeastOnce()).method("getGroup").will(returnValue(1L));
        p = (ProcessContainer.Process) m.proxy();
    }

    public void testListByGroup() {
        pc.addProcess(p);
        procs = pc.listProcesses(Arrays.asList(p.getGroup()));
        assertEquals(1, procs.size());
    }

    public void testListAll() {
        pc.addProcess(p);
        procs = pc.listProcesses(null);
        assertEquals(1, procs.size());
    }

    public void testRemove() {
        pc.addProcess(p);
        procs = pc.listProcesses(null);
        assertEquals(1, procs.size());
        pc.removeProcess(p);
        procs = pc.listProcesses(null);
        assertEquals(0, procs.size());
    }

    public void testPingOk() {
        m.expects(once()).method("ping");
        pc.addProcess(p);
        assertEquals(0, pc.pingAll());
    }

    public void testPingThrows() {
        m.expects(once()).method("ping")
            .will(throwException(new RuntimeException()));
        pc.addProcess(p);
        assertEquals(1, pc.pingAll());
    }

    public void testShutdownOk() {
        m.expects(once()).method("shutdown");
        pc.addProcess(p);
        assertEquals(0, pc.shutdownAll());
    }

    public void testShutdownThrows() {
        m.expects(once()).method("shutdown")
            .will(throwException(new RuntimeException()));
        pc.addProcess(p);
        assertEquals(1, pc.shutdownAll());
    }

}
