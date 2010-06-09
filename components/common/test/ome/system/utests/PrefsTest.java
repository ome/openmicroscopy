/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.system.utests;

import java.util.Properties;

import junit.framework.TestCase;
import ome.system.PreferenceContext;

import org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

@Test(groups = "ticket:800")
public class PrefsTest extends TestCase {

    public final static String testDefault = "test_default";

    PreferenceContext ctx;
    String oldDefault;

    @Test
    public void testSimple() {
        ctx = new PreferenceContext();
        System.setProperty("test", "ok"); // ticket:2214
        assertEquals("ok", ctx.getProperty("test"));
    }

    // Locals

    @Test
    public void testSystemOverridesLocals() {

        String key = "localsOverrideSystem";

        System.setProperty(key, "false");

        Properties p = new Properties();
        p.setProperty(key, "true");

        ctx = new PreferenceContext();
        ctx.setProperties(p);

        assertEquals("false", ctx.getProperty(key));
    }

    @Test
    public void testLocalsOverrideFiles() {

        String key = "localsOverridesFiles";

        Properties p = new Properties();
        p.setProperty(key, "true");

        ctx = new PreferenceContext();
        ctx.setProperties(p);
        ctx.setIgnoreResourceNotFound(false);
        ctx.setLocations(new Resource[] { new ClassPathResource(
                "ome/system/utests/Prefs.properties") });

        assertEquals("true", ctx.getProperty(key));

    }

    // System

    /**
     * Currently the {@link PreferencesPlaceholderConfigurer} does not use the
     * {@link System#getProperties()} to as
     * {@link PropertyPlaceholderConfigurer} does. We may need to modify
     * {@link PreferenceContext} to do so.
     */
    @Test(groups = "broken")
    public void testSystemOverridesFiles() {

        String key = "systemOverridesFiles";

        System.setProperty(key, "true");

        ctx = new PreferenceContext();
        ctx.setIgnoreResourceNotFound(false);
        ctx.setLocations(new Resource[] { new ClassPathResource(
                "ome/system/utests/Prefs.properties") });

        assertEquals("true", ctx.getProperty(key));

    }

    @Test
    public void testMissingFilesOk() {
        ctx = new PreferenceContext();
        ctx.setLocations(new Resource[] { new ClassPathResource(
                "DOES_NOT_EXIST") });
        ctx.getProperty("test");
    }

    @Test
    public void testOmeroVersion() {
        ctx = new PreferenceContext();
        ctx.setLocation(new ClassPathResource("omero.properties"));
        String v = ctx.getProperty("omero.version");
        assertNotNull(v);
        assertTrue(v.length() > 0);
    }

}
