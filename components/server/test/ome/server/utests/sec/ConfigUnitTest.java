/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sec;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ome.api.IConfig;
import ome.api.local.LocalConfig;
import ome.conditions.SecurityViolation;
import ome.logic.ConfigImpl;
import ome.security.basic.CurrentDetails;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.PreferenceContext;
import ome.util.SqlAction;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class ConfigUnitTest extends MockObjectTestCase {

    OmeroContext ctx;
    LocalConfig config;
    Current cd;
    PreferenceContext prefs;
    SqlAction sql;
    Mock sqlMock, ecMock;

    class Current extends CurrentDetails {
        EventContext ec;

        @Override
        public EventContext getCurrentEventContext() {
            return ec;
        }
    }

    @BeforeMethod
    public void create() {

        ctx = new OmeroContext(new String[] { "classpath:/ome/config.xml" });
        prefs = (PreferenceContext) ctx.getBean("preferenceContext");

        cd = new Current();
        ecMock = mock(EventContext.class);
        cd.ec = (EventContext) ecMock.proxy();

        sqlMock = mock(SqlAction.class);
        sql = (SqlAction) sqlMock.proxy();

        ConfigImpl bean = new ConfigImpl();

        bean.setPreferenceContext(prefs);
        bean.setSqlAction(sql);
        bean.setCurrentDetails(cd);
        config = bean;
    }

    @Test
    public void testThatVersionsAreAccessible() {
        mockAdmin();
        String v = config.getVersion();
        String cv = config.getConfigValue("omero.version");
        assertTrue(cv.contains(v));
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testThatDatabasePasswordIsNotAcessible() {
        mockAdmin();
        config.getConfigValue("omero.db.pass");
    }

    @Test
    public void testGettingAHiddenVariableInternally() {
        String pass = config.getInternalValue("omero.db.pass");
        assertEquals("omero", pass);
    }

    @Test
    public void testThatAliasesAreWorking() {
        String host1 = config.getInternalValue("omero.db.host");
        String host2 = config.getInternalValue("database.host");
        assertEquals(host1, host2);
    }

    @Test
    public void testVersionRegex() {
        Pattern pattern = Pattern.compile(IConfig.VERSION_REGEX);
        match(pattern, "4.0.0", "4.0.0");
        match(pattern, "4.0.0", "A-4.0.0-B");
        match(pattern, "4.0.10", "A.5-4.0.10-B.6");
        match(pattern, "4.1.0", "My-Work-4.1.0-r1234-b4321");
        match(pattern, "123.456.789", "foo.4kj4ma.4k-123.456.789-rc1^$^##@^%&");
    }

    @Test
    public void testThatValuesAreSettableAsAdmin() {
        mockAdmin();
        
        String test = UUID.randomUUID().toString()+"-config-test";

        notInDatabase();
        String oldValue = config.getConfigValue(test);
        assertEquals(null, oldValue);
        
        notInDatabase();
        updateDb(1);
        config.setConfigValue(test, "new");
        
        inDatabase("new");
        String newValue = config.getConfigValue(test);
        assertEquals("new", newValue);
    }

    @Test
    public void testThatOmeroDataDirIsAccessible() {
        mockAdmin();
        notInDatabase();
        assertTrue(config.getConfigValue("omero.data.dir").contains("OMERO"));
    }
    
    @Test
    public void testThatSetConfigIfEqualsWorksForDb() {
        mockAdmin();
        inDatabase("old");
        inDatabase("old");
        updateDb(1);
        assertTrue(config.setConfigValueIfEquals("redirect","new","old"));
    }
    
    @Test(groups = {"broken", "ticket:2491"})
    public void testThatSetConfigIfEqualsWorksForPrefs() {
        mockAdmin();
        notInDatabase();
        notInDatabase();
        prefs.setProperty("redirect","old");
        assertTrue(config.setConfigValueIfEquals("redirect","new","old"));
    }

    @Test(groups = "ticket:2491")
    public void testThatSetConfigIfEqualsWorksForSystem() {
        mockAdmin();
        notInDatabase();
        notInDatabase();
        System.setProperty("redirect", "old");
        assertTrue(config.setConfigValueIfEquals("redirect","new","old"));
    }
    
    @Test
    public void testThatSystemValuesWork() {
        System.setProperty("omero.name","OMERO.test");
        mockAdmin();
        notInDatabase();
        assertEquals("OMERO.test",config.getConfigValue("omero.name"));
    }
    
    // Helpers
    // =========================================================================

    private void mockAdmin() {
        ecMock.expects(atLeastOnce()).method("isCurrentUserAdmin").will(
                returnValue(true));
    }

    private void notInDatabase() {
        sqlMock.expects(once()).method("configValue").will(returnValue(null));
    }
    
    private void inDatabase(String value) {
        sqlMock.expects(once()).method("configValue").will(returnValue(value));
    }
    
    private void updateDb(int count) {
        sqlMock.expects(once()).method("updateConfiguration").will(returnValue(count));
    }
    
    private void match(Pattern pattern, String goal, String text) {
        Matcher matcher = pattern.matcher(text);
        assertTrue(matcher.matches());
        assertEquals(goal, matcher.group(1));
    }
}
