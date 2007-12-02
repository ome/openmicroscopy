package ome.admin.view.test;

import java.util.Locale;

import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;

public class NavigationTest extends WebTestCase {

    public void setUp() throws Exception {
        setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);
        getTestContext().setBaseUrl("http://localhost:8080/WebAdmin");
    }

    /**
     * test navigation links
     */
    public void testLinks() {
        beginAt("/login.jsf");
        beginAt("/experimenters.jsf");
        beginAt("/experimenterForm.jsf");
        beginAt("/groups.jsf");
        beginAt("/groupForm.jsf");
        beginAt("/editInGroup.jsf");
        beginAt("/myAccount.jsf");
        beginAt("/myPassword.jsf");
        beginAt("/imports.jsf");
        beginAt("/importForm.jsf");
        beginAt("/uploadFile.jsf");
        beginAt("/space.jsf");
    }

}
