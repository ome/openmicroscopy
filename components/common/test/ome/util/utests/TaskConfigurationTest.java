/*
 * ome.util.utests.TaskConfigurationTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.utests;

import org.testng.annotations.*;
import java.util.Properties;

import ome.util.tasks.Configuration;
import ome.util.tasks.Task;

import junit.framework.TestCase;

/**
 * fairly limited unit tests of configuration. The real work is in
 * {@link Configuration#createServiceFactory()} and
 * {@link Configuration#createTask()} but these can't be called from common.
 */
public class TaskConfigurationTest extends TestCase {

    @Test
    public void testRequiredElements() throws Exception {
        fails(null);
        fails(new Properties());
        fails(pairs("task", "unknown class"));
//      No longer required:
//        fails(pairs("task", "admin.AddUserTask", "user", "forgotlots"));
//        fails(pairs("task", "admin.AddUserTask", "pass", "forgotlots"));
//        fails(pairs("task", "admin.AddUserTask", "type", "forgotlots"));
//        fails(pairs("task", "admin.AddUserTask", "group", "forgotlots"));
//        fails(pairs("task", "admin.AddUserTask", "host", "forgotport"));
//        fails(pairs("task", "admin.AddUserTask", "port", "forgothost"));

    }

    @Test
    public void testGetProperties() throws Exception {
        Properties p = pairs("task", "admin.AddUserTask", "foo", "bar");
        Configuration c = new Configuration(p);
        c.getProperties().containsKey("foo");
    }

    @Test
    public void testGetTaskClass() throws Exception {
        Properties p = pairs("task", "admin.AddUserTask");
        Configuration c = new Configuration(p);
        Class<Task> k = c.getTaskClass();
        assertTrue(k.getName().endsWith("admin.AddUserTask"));
    }

    // ~ Helpers
    // =========================================================================

    protected void fails(Properties p) {
        try {
            new Configuration(p);
            fail("Should have failed.");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    protected Properties pairs(String... values) {
        Properties p = new Properties();
        for (int i = 0; i < values.length; i += 2) {
            p.setProperty(values[i], values[i + 1]);
        }
        return p;
    }
}
