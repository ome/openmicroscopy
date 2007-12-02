package ome.admin.view.test;

import java.util.Locale;

import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;

public class GroupsTest extends WebTestCase {

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

    public void testGroupsPage() {
        beginAt("/groups.jsf");
        login();
        assertKeyPresent("groupsAddNewGroup");
        assertKeyPresent("groupsName");
        assertKeyPresent("groupsListGroup");
        assertKeyPresent("groupsActions");

        assertImagePresent("/WebAdmin/images/editing.png",
                "Scientists in group");
        assertImagePresent("/WebAdmin/images/edit.png", "Edit group");
    }

    public void testGroupAdd() {
        beginAt("/groups.jsf");
        login();
        clickLink("groups:_idJsp0");

        // checks bundles
        assertKeyPresent("groupsAddNewGroup");
        assertKeyPresent("groupsGroupName");
        assertKeyPresent("groupsDescription");
        assertKeyPresent("groupsSave");

        // checks the form
        assertFormPresent("groupForm");
        assertFormElementPresent("groupForm:name");
        assertFormElementPresent("groupForm:description");
        assertSubmitButtonPresent("groupForm:submitAdd");

        // add the new user
        assertFormPresent("groupForm");
        setTextField("groupForm:name", "group1");
        setTextField("groupForm:description", "description for group1");
        submit("groupForm:submitAdd");
    }

    public void testGroupEdit() {
        beginAt("/groups.jsf");
        login();
        clickLink("groups:data:1:_idJsp17");

        // checks bundle
        assertKeyPresent("groupsEditGroup");
        assertKeyPresent("groupsGroupName");
        assertKeyPresent("groupsDescription");
        assertKeyPresent("groupsSave");

        // checks form
        assertFormPresent("groupForm");
        assertFormElementPresent("groupForm:name");
        assertFormElementEquals("groupForm:name", "group1");
        assertFormElementPresent("groupForm:description");
        assertFormElementEquals("groupForm:description",
                "description for group1");
        assertSubmitButtonPresent("groupForm:submitUpdate");

        assertFormPresent("groupForm");
        setTextField("groupForm:name", "group1");
        setTextField("groupForm:description", "description for group1");
        submit("groupForm:submitUpdate");
    }

}
