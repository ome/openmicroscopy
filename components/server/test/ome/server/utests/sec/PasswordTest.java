/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sec;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ome.api.ILdap;
import ome.conditions.ApiUsageException;
import ome.logic.LdapImpl;
import ome.model.meta.Experimenter;
import ome.security.auth.ConfigurablePasswordProvider;
import ome.security.auth.FilePasswordProvider;
import ome.security.auth.JdbcPasswordProvider;
import ome.security.auth.LdapConfig;
import ome.security.auth.LdapPasswordProvider;
import ome.security.auth.PasswordChangeException;
import ome.security.auth.PasswordProvider;
import ome.security.auth.PasswordProviders;
import ome.security.auth.PasswordUtil;
import ome.security.auth.PasswordUtility;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.util.SqlAction;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class PasswordTest extends MockObjectTestCase {

    private final static Long GUEST_ID = new Roles().getGuestId();

    private final static Long NON_GUEST_ID = new Roles().getGuestId() + 1;

    static File file = null;
    static {
        try {
            file = ResourceUtils.getFile("classpath:ome/server/utests/sec/"
                    + "PasswordTest_FilePasswordProvider.properties");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Concrete implementation of {@link ConfigurablePasswordProvider}'s default
     * actions.
     */
    static class PP extends ConfigurablePasswordProvider {
        PP() {
            super(null);
        }
    }

    Charset latin1 = Charset.forName("ISO-8859-1"), utf8 = Charset.forName("UTF-8");

    PasswordProvider provider;

    PasswordUtil utf8Util, latin1Util;

    Mock mockSql, mockLdap;

    SqlAction sql;

    LdapImpl ldap;

    AtomicBoolean validPassword = new AtomicBoolean();

    AtomicReference<String> currentDn = new AtomicReference<String>();

    AtomicReference<Experimenter> createdUser = new AtomicReference<Experimenter>();

    @BeforeMethod
    protected void initJdbc() {
        initJdbc(utf8);
    }

    protected void initJdbc(Charset ch) {
        initJdbc(ch, true);
    }

    protected void initJdbc(Charset ch, boolean requirePassword) {
        initJdbc(ch, requirePassword, true);
    }

    protected void initJdbc(Charset ch, boolean requirePassword, boolean salt) {
        mockSql = mock(SqlAction.class);
        sql = (SqlAction) mockSql.proxy();
        utf8Util = new PasswordUtil(sql, requirePassword, utf8);
        latin1Util = new PasswordUtil(sql, requirePassword, latin1);
        if (utf8 == ch) {
            initProvider(utf8Util, salt);
        } else {
            initProvider(latin1Util, salt);
        }
    }

    protected void initProvider(PasswordUtil util) {
        initProvider(util, true);
    }

    protected void initProvider(PasswordUtil util, boolean salt) {
        provider = new JdbcPasswordProvider(util, false, salt);
        setApplicationContext();
    }

    protected void setApplicationContext() {
        ((ConfigurablePasswordProvider) provider).setApplicationContext(
                new OmeroContext(new String[]{}));
    }

    protected void initLdap(boolean setting) {
        initJdbc();
        mockLdap = mock(ILdap.class);
        // ldap = (ILdap) mockLdap.proxy();
        ldap = new LdapImpl(null, null, null,
                new LdapConfig(setting, "", "", "", "", "", false, ""), null, sql) {
            @Override
            public String findDN(String username) {
                return currentDn.get();
            }
            @Override
            public boolean validatePassword(String dn, String password) {
                return validPassword.get();
            }
            /* broken @Override*/
            public Experimenter createUser(String username,
                    String password, boolean checkPassword) {
                return createdUser.get();
            }
        };
        mockLdap.expects(atLeastOnce()).method("getSetting").will(
                returnValue(setting));
    }

    // CONFIGURABLE

    /**
     * By default, the base class should return False ("Password rejected")
     */
    public void testConfigurableDefaultsReturnsFalse() {
        provider = new PP();
        assertFalse(provider.checkPassword("", "", false));
        assertFalse(provider.hasPassword(""));
    }

    /**
     * By default, the base class should return False ("Password rejected") and
     * throw a {@link PasswordChangeException} ("Can't change")
     */
    @Test(expectedExceptions = PasswordChangeException.class)
    public void testConfigurableDefaultsThrows() throws Exception {
        provider = new PP();
        provider.changePassword("", "");
    }

    // FILE

    public void testFileDefaults() throws Exception {
        provider = new FilePasswordProvider(new PasswordUtil(sql), file);
        setApplicationContext();
        assertTrue(provider.hasPassword("test"));
        assertTrue(provider.checkPassword("test", "test", false));
        assertFalse(provider.checkPassword("unknown", "anything", false));
    }

    public void testFilesDontIgnoreUnknownReturnsNull() throws Exception {
        provider = new FilePasswordProvider(null, file, true);
        setApplicationContext();
        assertFalse(provider.hasPassword("unknown"));
        assertNull(provider.checkPassword("unknown", "anything", false));
    }

    @Test(expectedExceptions = PasswordChangeException.class)
    public void testFilesThrowsOnChange() throws Exception {
        provider = new FilePasswordProvider(null, file, true);
        setApplicationContext();
        provider.changePassword("test", "something new");
    }

    // JDBC

    public void testJdbcDefaults() throws Exception {
        initJdbc();

        userIdReturns1();
        provider.hasPassword("test");

        userIdReturnsNull();
        provider.hasPassword("unknown");

        String encoded = ((PasswordUtility) provider).encodePassword("test");
        getPasswordHash(encoded);
        userIdReturns1();
        assertTrue(provider.checkPassword("test", "test", false));

        getPasswordHash(encoded);
        userIdReturns1();
        assertFalse(provider.checkPassword("test", "GARBAGE", false));
    }

    public void tesJdbcIgnoreUnknownReturnsFalse() throws Exception {
        initJdbc();
        userIdReturnsNull();
        assertFalse(provider.checkPassword("unknown", "anything", false));
    }

    public void tesJdbcDontIgnoreUnknownReturnsNull() throws Exception {
        initJdbc();
        userIdReturnsNull();
        provider = new JdbcPasswordProvider(new PasswordUtil(sql), true);
        setApplicationContext();
        assertNull(provider.checkPassword("unknown", "anything", false));
    }

    public void testJdbcChangesPassword() throws Exception {
        initJdbc();
        userIdReturns1();
        setHashCalledWith(eq(1l), ANYTHING);
        provider.changePassword("a", "b");
    }

    @Test(expectedExceptions = PasswordChangeException.class)
    public void testJdbcThrowsOnBadUsername() throws Exception {
        initJdbc();
        userIdReturnsNull();
        provider.changePassword("a", "b");
    }

    // LDAP

    public void tesLdapDefaults() throws Exception {
        initLdap(true);
        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap);
        setApplicationContext();

        String encoded = ((PasswordUtility) provider).encodePassword("test");
        getPasswordHash(encoded);
        getDn("dn");
        userIdReturns1();
        validateLdapPassword(true);
        assertTrue(provider.checkPassword("test", "test", false));

        getPasswordHash(encoded);
        userIdReturns1();
        validateLdapPassword(false);
        getDn("dn");
        assertFalse(provider.checkPassword("test", "GARBAGE", false));

        userIdReturnsNull();
        assertFalse(provider.hasPassword("unknown"));

        userIdReturns1();
        getPasswordHash(null);
        validateLdapPassword(false);
        getDn(null);
        assertFalse(provider.hasPassword("no-dn"));
        getPasswordHash("dn");
        getDn("dn");
        userIdReturns1();
        assertTrue(provider.hasPassword("dn"));
    }

    public void tesLdapIgnoreUnknownCreatesFailsReturnsFalse() throws Exception {
        initLdap(true);
        userIdReturnsNull();
        ldapCreatesUser(false);
        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap, true);
        setApplicationContext();
        assertNull(provider.checkPassword("unknown", "anything", false));
    }

    public void tesLdapIgnoreUnknownCreatesSucceedsReturnsTrue()
            throws Exception {
        initLdap(true);
        userIdReturnsNull();
        ldapCreatesUser(true);
        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap, true);
        setApplicationContext();
        assertTrue(provider.checkPassword("unknown", "anything", false));
    }

    public void tesLdapIgnoreUnknownCreatesThrows() throws Exception {
        initLdap(true);
        userIdReturnsNull();
        ldapCreatesUserAndThrows();
        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap, true);
        setApplicationContext();
        assertNull(provider.checkPassword("unknown", "anything", false));
    }

    public void tesLdapDontIgnoreUnknownCreatesFailsReturnsFalse()
            throws Exception {
        initLdap(true);
        userIdReturnsNull();
        ldapCreatesUser(false);
        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap, false);
        setApplicationContext();
        assertFalse(provider.checkPassword("unknown", "anything", false));
    }

    public void tesLdapDontIgnoreUnknownCreatesSucceedsReturnsTrue()
            throws Exception {
        initLdap(true);
        userIdReturnsNull();
        ldapCreatesUser(true);
        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap, false);
        setApplicationContext();
        assertTrue(provider.checkPassword("unknown", "anything", false));
    }

    public void tesLdapDontIgnoreUnknownCreatesThrows() throws Exception {
        initLdap(true);
        userIdReturnsNull();
        ldapCreatesUserAndThrows();
        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap, false);
        setApplicationContext();
        assertFalse(provider.checkPassword("unknown", "anything", false));
    }

    @Test(expectedExceptions = PasswordChangeException.class)
    public void testLdapChangesPasswordThrows() throws Exception {
        initLdap(true);
        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap);
        setApplicationContext();
        provider.changePassword("a", "b");
    }

    /**
     * Straight-forward stub to allow easy composite testing.
     */
    static class Stub implements PasswordProvider {
        Boolean check = null;
        boolean exception = true;
        boolean hasPasswordCalled = false;
        boolean changePasswordCalled = false;
        boolean checkPasswordCalled = false;

        public Stub() {

        }

        public Stub(Boolean check, boolean exc) {
            this.check = check;
            this.exception = exc;
        }

        public void changePassword(String user, String password)
                throws PasswordChangeException {
            changePasswordCalled = true;
            if (exception) {
                throw new PasswordChangeException("");
            }
        }

        public boolean hasPassword(String user) {
            hasPasswordCalled = true;
            return check == null ? false : check.booleanValue();
        }

        public Boolean checkPassword(String user, String password, boolean readOnly) {
            checkPasswordCalled = true;
            return check;
        }

        void assertChangePasswordCalled() {
            if (!changePasswordCalled) {
                fail();
            }
        }


        void assertChangePasswordNotCalled() {
            if (changePasswordCalled) {
                fail();
            }
        }

        void assertCheckPasswordCalled() {
            if (!checkPasswordCalled) {
                fail();
            }
        }

        void assertCheckPasswordNotCalled() {
            if (checkPasswordCalled) {
                fail();
            }
        }

        void assertHasPasswordCalled() {
            if (!hasPasswordCalled) {
                fail();
            }
        }

        void assertHasPasswordNotCalled() {
            if (hasPasswordCalled) {
                fail();
            }
        }

    }

    // COMPOSITE LDAP FIRST THEN JDBC (standard)

    public void testChainedUnknownPropogatesToSecondStub() throws Exception {
        Stub s1 = new Stub();
        Stub s2 = new Stub();
        provider = new PasswordProviders(s1, s2);
        assertNull(provider.checkPassword("known", "password", false));
        s1.assertCheckPasswordCalled();
        s2.assertCheckPasswordCalled();
    }

    public void testChainedUnknownPropogatesToSecondStubWhichFails() throws Exception {
        Stub s1 = new Stub();
        Stub s2 = new Stub(false, false);
        provider = new PasswordProviders(s1, s2);
        assertFalse(provider.checkPassword("known", "password", false));
        s1.assertCheckPasswordCalled();
        s2.assertCheckPasswordCalled();
    }

    public void testChainedKnownPropogatesToSecondStubWhichSucceeds() throws Exception {
        Stub s1 = new Stub();
        Stub s2 = new Stub(true, false);
        provider = new PasswordProviders(s1, s2);
        assertTrue(provider.checkPassword("known", "password", false));
        s1.assertCheckPasswordCalled();
        s2.assertCheckPasswordCalled();
    }

    public void testChainedKnownDoesntPropagate() throws Exception {
        Stub s1 = new Stub(true, false);
        Stub s2 = new Stub(true, false);
        provider = new PasswordProviders(s1, s2);
        assertTrue(provider.checkPassword("known", "password", false));
        s1.assertCheckPasswordCalled();
        s2.assertCheckPasswordNotCalled();
    }

    public void testChainedUnknownDoesntPropagate() throws Exception {
        Stub s1 = new Stub(false, false);
        Stub s2 = new Stub(false, false);
        provider = new PasswordProviders(s1, s2);
        assertFalse(provider.checkPassword("unknown", "password", false));
        s1.assertCheckPasswordCalled();
        s2.assertCheckPasswordNotCalled();
    }

    public void testChainedFirstChangePassword() throws Exception {
        Stub s1 = new Stub(true, false);
        Stub s2 = new Stub(true, false);
        provider = new PasswordProviders(s1, s2);
        provider.changePassword("","");
        s1.assertHasPasswordCalled();
        s1.assertChangePasswordCalled();
        s2.assertHasPasswordNotCalled();
        s2.assertChangePasswordNotCalled();
    }

    public void testChainedSecondChangePassword() throws Exception {
        Stub s1 = new Stub(false, false);
        Stub s2 = new Stub(true, false);
        provider = new PasswordProviders(s1, s2);
        provider.changePassword("","");
        s1.assertHasPasswordCalled();
        s1.assertChangePasswordNotCalled();
        s2.assertHasPasswordCalled();
        s2.assertChangePasswordCalled();
    }

    public void testChainedNoneChangePassword() throws Exception {
        Stub s1 = new Stub(false, false);
        Stub s2 = new Stub(false, false);
        provider = new PasswordProviders(s1, s2);
        assertChangeThrows();
        s1.assertHasPasswordCalled();
        s1.assertChangePasswordNotCalled();
        s2.assertHasPasswordCalled();
        s2.assertChangePasswordNotCalled();
    }

    public void testChainedFirstWontChangePassword() throws Exception {
        Stub s1 = new Stub(true, true);
        Stub s2 = new Stub(false, false);
        provider = new PasswordProviders(s1, s2);
        assertChangeThrows();
        s1.assertHasPasswordCalled();
        s2.assertHasPasswordNotCalled();
    }

    public void testChainedSecondWontChangePassword() throws Exception {
        Stub s1 = new Stub(false, true);
        Stub s2 = new Stub(true, true);
        provider = new PasswordProviders(s1, s2);
        assertChangeThrows();
        s1.assertHasPasswordCalled();
        s2.assertHasPasswordCalled();
    }

    // ~ password encoding
    // =========================================================================

    final static String good = "ążćę";
    final static String bad = "????";
    final static String badHash = "6U8L+rjJh6dDe6ThaXwcwA==";
    final static String badHashSalt1 = "AfSXrwNujnMYx1CagyujVA==";
    final static String goodHash = "iIoEyIOGsGsDhWZMYNBTKQ==";
    final static String goodHashSalt1 = "RBH/4oA/c43qLXeotWM/XA==";

    public void testLatin1Encoding() {
        final PasswordUtil latin1Util = new PasswordUtil(sql, latin1);
        byte[] badBytes = bad.getBytes(latin1);
        byte[] goodBytes = good.getBytes(latin1);
        assertTrue(Arrays.equals(badBytes, goodBytes));
        assertEquals(bad, new String(good.getBytes(latin1)));
        assertEquals(badHash, latin1Util.passwordDigest(bad));
        assertEquals(badHash, latin1Util.passwordDigest(good));
    }

    public void testUtf8Encoding() throws UnsupportedEncodingException{
        final PasswordUtil utf8Util = new PasswordUtil(sql, utf8);
        assertEquals(good, new String(good.getBytes(utf8), "UTF-8"));
        assertEquals(badHash, utf8Util.passwordDigest(bad));
        assertEquals(goodHash, utf8Util.passwordDigest(good));
        assertFalse(goodHash.equals(badHash));
    }

    public void testJdbcLatin1PasswordOldUtil() throws Exception {
        initJdbc(latin1);

        // Setting the password with latin1 uses the bad hash
        userIdReturns1();
        setHashCalledWith(eq(1L), eq(badHashSalt1));
        provider.changePassword("test", good);

        // Checking the password whether good or bad passes
        // 1) Good: Yes
        userIdReturns1();
        getPasswordHash(badHash);
        assertTrue(provider.checkPassword("test", good, true));
        // 1) Bad: Yes?! This was the bug.
        userIdReturns1();
        getPasswordHash(badHash);
        assertTrue(provider.checkPassword("test", bad, true));
    }

    public void testJdbcLatin1PasswordNewUtil() throws Exception {
        initJdbc(utf8);
        // For this to work, the old util must be set.
        ((JdbcPasswordProvider) provider).setLegacyUtil(latin1Util);

        // Here we don't worry about testing the setting of the password
        // since a latin1 util was used for the setting.

        // Checking the password whether good or bad passes
        // 1) Good: Yes
        userIdReturns1();
        getPasswordHash(badHash);
        assertTrue(provider.checkPassword("test", good, true));
        // 1) Bad: Yes, but ERROR printed to the logs.
        userIdReturns1();
        getPasswordHash(badHash);
        assertTrue(provider.checkPassword("test", bad, true));
    }

    public void testJdbcUtf8PasswordNewUtil() throws Exception {
        initJdbc(utf8);

        // Setting the password with utf8 uses the good hash
        userIdReturns1();
        setHashCalledWith(eq(1L), eq(goodHashSalt1));
        provider.changePassword("test", good);

        // Only checking the password with good passes.
        // 1) Good: yes
        userIdReturns1();
        getPasswordHash(goodHashSalt1);
        assertTrue(provider.checkPassword("test", good, true));
        // 2) Bad: NO!
        userIdReturns1();
        getPasswordHash(goodHashSalt1);
        assertFalse(provider.checkPassword("test", bad, true));
    }

    // ~ empty passwords
    // =========================================================================

    public void testIsPasswordRequiredWithoutStrictSetting() {
        PasswordUtil util = new PasswordUtil(sql, false);
        assertFalse(util.isPasswordRequired(null));
        assertFalse(util.isPasswordRequired(456l));
        assertFalse(util.isPasswordRequired(GUEST_ID));
    }

    public void testIsPasswordRequiredWithStrictSetting() {
        PasswordUtil util = new PasswordUtil(sql, true);
        assertTrue(util.isPasswordRequired(null));
        assertTrue(util.isPasswordRequired(456l));
        assertFalse(util.isPasswordRequired(GUEST_ID));
    }

    public void testNonstrictProviderAcceptsEmptyGuest() throws Exception {
        initJdbc(utf8, false);
        userIdReturns(GUEST_ID);
        setHashCalledWith(eq(GUEST_ID), eq(""));
        provider.changePassword("test", "");

        userIdReturns(GUEST_ID);
        getPasswordHash("");
        assertTrue(provider.checkPassword("test", "", true));
    }

    public void testNonstrictProviderAcceptsEmptyUser() throws Exception {
        initJdbc(utf8, false);
        userIdReturns(NON_GUEST_ID);
        setHashCalledWith(eq(NON_GUEST_ID), eq(""));
        getPasswordHash("");
        provider.changePassword("test", "");

        userIdReturns(NON_GUEST_ID);
        assertTrue(provider.checkPassword("test", "", true));
    }

    public void testStrictProviderAcceptsEmptyGuestNoLock() throws Exception {
        initJdbc(utf8, true);
        userIdReturns(GUEST_ID);
        setHashCalledWith(eq(GUEST_ID), eq(""));
        provider.changePassword("test", "");

        userIdReturns(GUEST_ID);
        getPasswordHash("");
        assertTrue(provider.checkPassword("test", "", true));
    }

    public void testStrictProviderAcceptsEmptyGuestNoSaltStillNoLock() throws Exception {
        // Now without salting turned on, see if we can still have
        // the "require_password" setting adhered to.
        initJdbc(utf8, true, false);
        userIdReturns(GUEST_ID);
        setHashCalledWith(eq(GUEST_ID), eq(""));
        provider.changePassword("test", "");

        userIdReturns(GUEST_ID);
        getPasswordHash("");
        assertTrue(provider.checkPassword("test", "", true));
    }
    public void testStrictProviderAcceptsEmptyUserLocks() throws Exception {
        initJdbc(utf8, true);
        userIdReturns(NON_GUEST_ID);
        setHashCalledWith(eq(NON_GUEST_ID), eq(null));
        provider.changePassword("test", "");

        userIdReturns(NON_GUEST_ID);
        getPasswordHash(null);
        assertFalse(provider.checkPassword("test", "", true));
    }

    // ~ Helpers
    // =========================================================================

    private void setHashCalledWith(Constraint... constraints) {
        mockSql.expects(once()).method("clearPermissionsBit")
            .will(returnValue(true));
        mockSql.expects(once()).method("setUserPassword")
            .with(constraints).will(returnValue(true));
    }

    private void getPasswordHash(String value) {
        mockSql.expects(once()).method("getPasswordHash").will(returnValue(value));
    }

    private void getDn(String value) {
        mockSql.expects(once()).method("dnForUser").will(returnValue(value));
        currentDn.set(value);
    }

    private void userIdReturnsNull() {
        mockSql.expects(once()).method("getUserId").will(returnValue(null));
    }

    private void userIdReturns1() {
        userIdReturns(1L);
    }

    private void userIdReturns(Long id) {
        mockSql.expects(once()).method("getUserId").will(returnValue(id));
    }

    private void ldapCreatesUser(boolean andReturns) {
        Experimenter e = andReturns ? new Experimenter() : null;
        createdUser.set(e);
        mockLdap.expects(once()).method("createUser").will(
                returnValue(andReturns));
    }

    private void ldapCreatesUserAndThrows() {
        createdUser.set(null);
        mockLdap.expects(once()).method("createUser").will(
                throwException(new ApiUsageException("")));
    }

    private void validateLdapPassword(boolean v) {
        validPassword.set(v);
    }

    private void assertChangeThrows() {
        try {
            provider.changePassword("", "");
            fail("must throw");
        } catch (PasswordChangeException pce) {
            // good.
        }
    }
}
