/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.system.utests;

import java.util.prefs.Preferences;

import junit.framework.TestCase;
import ome.system.PreferenceContext;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "ticket:800")
public class PrefsTest extends TestCase {

    public final static String testDefault = "test_default";

    PreferenceContext ctx;
    Preferences user, test;
    String oldDefault;

    @BeforeClass
    public void setup() {
        user = Preferences.userRoot().node(PreferenceContext.ROOT);
        oldDefault = user.get(PreferenceContext.DEFAULT,
                PreferenceContext.DEFAULT);
        user.put(PreferenceContext.DEFAULT, testDefault);
        test = user.node(testDefault);
        test.put("test", "ok");
    }

    @Override
    @AfterClass
    public void tearDown() {
        user.put(PreferenceContext.DEFAULT, oldDefault);
    }

    @Test
    public void testSimple() {
        ctx = new PreferenceContext();
        assertEquals("ok", ctx.getProperty("${test}"));
    }

    @Test
    public void testUserOverridesSystem() {
        ctx = new PreferenceContext();
    }

    @Test
    public void testEnvironmentOverridesUser() {
        ctx = new PreferenceContext();
    }

    @Test
    public void testShouldEnvironmentOverridesSystem() {
        ctx = new PreferenceContext();
    }

    @Test
    public void testMissingPrefsFromEnvironmentDoesntFail() {
        ctx = new PreferenceContext();
    }

    @Test
    public void testMissingFilesOk() {
        ctx = new PreferenceContext();
    }

}