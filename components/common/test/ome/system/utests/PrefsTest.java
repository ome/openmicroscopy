/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.system.utests;

import java.util.Properties;
import java.util.prefs.Preferences;

import junit.framework.TestCase;
import ome.system.PreferenceContext;

import org.springframework.beans.factory.config.PreferencesPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "ticket:800")
public class PrefsTest extends TestCase {

    public final static String testDefault = "test_default";

    PreferenceContext ctx;
    Preferences sys, user, test, test_sys;
    String oldDefault;

    @BeforeClass
    public void setup() {
        sys = Preferences.systemRoot().node(PreferenceContext.ROOT);
        user = Preferences.userRoot().node(PreferenceContext.ROOT);
        oldDefault = user.get(PreferenceContext.DEFAULT,
                PreferenceContext.DEFAULT);
        user.put(PreferenceContext.DEFAULT, testDefault);
        test = user.node(testDefault);
        test.put("test", "ok");
        test_sys = user.node(testDefault);
    }

    @Override
    @AfterClass
    public void tearDown() {
        user.put(PreferenceContext.DEFAULT, oldDefault);
    }

    @Test
    public void testSimple() {
        ctx = new PreferenceContext();
        ctx.afterPropertiesSet();
        assertEquals("ok", ctx.getProperty("test"));
    }

    // Locals

    @Test
    public void testLocalsOverridePrefs() {

        String key = "localsOverridePrefs";

        System.setProperty(key, "false");

        Properties p = new Properties();
        p.setProperty(key, "true");

        ctx = new PreferenceContext();
        ctx.setProperties(p);
        ctx.afterPropertiesSet();

        assertEquals("true", ctx.getProperty(key));
    }

    @Test
    public void testLocalsOverrideSystem() {

        String key = "localsOverrideSystem";

        System.setProperty(key, "false");

        Properties p = new Properties();
        p.setProperty(key, "true");

        ctx = new PreferenceContext();
        ctx.setProperties(p);
        ctx.afterPropertiesSet();

        assertEquals("true", ctx.getProperty(key));
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
        ctx.afterPropertiesSet();

        assertEquals("true", ctx.getProperty(key));

    }

    // Preferences

    @Test
    public void testPrefsOverrideSystem() {

        String key = "prefsOverrideSystem";

        test.put(key, "true");

        System.setProperty(key, "false");

        ctx = new PreferenceContext();
        ctx.afterPropertiesSet();

        assertEquals("true", ctx.getProperty(key));

    }

    @Test
    public void testPrefsOverrideFiles() {

        String key = "prefsOverrideFiles";

        test.put(key, "true");

        ctx = new PreferenceContext();
        ctx.setIgnoreResourceNotFound(false);
        ctx.setLocations(new Resource[] { new ClassPathResource(
                "ome/system/utests/Prefs.properties") });
        ctx.afterPropertiesSet();

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
        ctx.afterPropertiesSet();

        assertEquals("true", ctx.getProperty(key));

    }

    @Test
    public void testMissingFilesOk() {
        ctx = new PreferenceContext();
        ctx.setLocations(new Resource[] { new ClassPathResource(
                "DOES_NOT_EXIST") });
        ctx.afterPropertiesSet();
        ctx.getProperty("test");
    }

}
