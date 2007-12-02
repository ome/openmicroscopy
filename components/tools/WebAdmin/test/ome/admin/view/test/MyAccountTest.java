package ome.admin.view.test;

import java.util.Locale;

import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;

public class MyAccountTest extends WebTestCase {

    public void setUp() throws Exception {
        String baseUrl = "http://localhost:8080/WebAdmin";
        setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);
        getTestContext().setBaseUrl(baseUrl);
        getTestContext().setResourceBundleName("ome.admin.bundle.messages");
        Locale l = Locale.UK;
        // Locale l = new Locale(Locale.getISOLanguages()[132],
        // Locale.getISOCountries()[172]);
        getTestContext().setLocale(l);
    }

    private void login() {
        assertFormPresent("loginForm");
        assertFormElementPresent("loginForm:server");
        assertFormElementEquals("loginForm:server", "localhost");
        assertFormElementPresent("loginForm:port");
        assertFormElementEquals("loginForm:port", "1099");
        setTextField("loginForm:username", "root");
        setTextField("loginForm:password", "ome");
        submit("loginForm:submit");
    }

    public void testMyAccountPage() {
        beginAt("/myAccount.jsf");
        login();

        // checks bundle
        assertKeyPresent("myaccountEdit");
        assertKeyPresent("myaccountOmeName");
        assertKeyPresent("myaccountFirstName");
        assertKeyPresent("myaccountMiddleName");
        assertKeyPresent("myaccountLastName");
        assertKeyPresent("myaccountEmail");
        assertKeyPresent("myaccountInstitution");
        assertKeyPresent("myaccountDefaultGroup");
        assertKeyPresent("myaccountSave");
        assertKeyPresent("myaccountChangePassword");

        // checks the form
        assertFormPresent("experimenterForm");
        assertFormElementPresent("experimenterForm:firstName");
        assertFormElementPresent("experimenterForm:middleName");
        assertFormElementPresent("experimenterForm:lastName");
        assertFormElementPresent("experimenterForm:email");
        assertFormElementPresent("experimenterForm:institution");
        assertFormElementPresent("experimenterForm:defaultGroup");
        assertSelectedOptionEquals("experimenterForm:defaultGroup", "system");
        assertSubmitButtonPresent("experimenterForm:submitUpdate");

    }

}
