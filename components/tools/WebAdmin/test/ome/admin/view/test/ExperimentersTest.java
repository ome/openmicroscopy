package ome.admin.view.test;

import java.util.Locale;

import net.sourceforge.jwebunit.junit.WebTestCase;
import net.sourceforge.jwebunit.util.TestingEngineRegistry;

public class ExperimentersTest extends WebTestCase {

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

	public void testExperimentersPage() {
		beginAt("/experimenters.jsf");
		login();
		assertKeyPresent("experimentersRole");
		assertKeyPresent("experimentersAddNew");
		assertKeyPresent("experimentersName");
		assertKeyPresent("experimentersList");
		assertKeyPresent("experimentersActions");
		assertKeyPresent("experimentersRole");
		assertKeyPresent("experimentersName");
		assertKeyPresent("experimentersOmeName");
		assertKeyPresent("experimentersInstitution");
		assertImagePresent("/WebAdmin/images/admin.png", "Administrator");
		assertImagePresent("/WebAdmin/images/active.png", "Active");
		assertImagePresent("/WebAdmin/images/active.png", "Active");
		assertImagePresent("/WebAdmin/images/del.png", "Delete Scientist");
		assertImagePresent("/WebAdmin/images/edit.png", "Edit Scientist");
		
		clickLinkWithImage("/WebAdmin/images/edit.png");
	}

	public void testExperimenterAdd() {
		beginAt("/experimenterForm.jsf");
		login();

		//checks bundles
		assertKeyPresent("experimentersAddNew");
		assertKeyPresent("experimentersOmeName");
		assertKeyPresent("experimentersFirstName");
		assertKeyPresent("experimentersMiddleName");
		assertKeyPresent("experimentersLastName");
		assertKeyPresent("experimentersOmeName");
		assertKeyPresent("experimentersEmail");
		assertKeyPresent("experimentersInstitution");
		assertKeyPresent("experimentersDefaultGroup");
		assertKeyPresent("experimentersOtherGroups");
		assertKeyPresent("experimentersAdminRole");
		assertKeyPresent("experimentersUserRole");
		assertKeyPresent("experimentersSave");

		//checks the form
		assertFormPresent("experimenterForm");
		assertFormElementPresent("experimenterForm:omeName");
		assertFormElementPresent("experimenterForm:firstName");
		assertFormElementPresent("experimenterForm:middleName");
		assertFormElementPresent("experimenterForm:lastName");
		assertFormElementPresent("experimenterForm:email");
		assertFormElementPresent("experimenterForm:institution");
		assertCheckboxPresent("experimenterForm:_idJsp10");
		assertCheckboxPresent("experimenterForm:_idJsp13");
		String[] values = { "system", "default" };
		assertSelectOptionsEqual("experimenterForm:defaultGroup", values);
		assertSelectOptionsEqual("experimenterForm:otherGroup", values);
		assertSubmitButtonPresent("experimenterForm:submitAdd");

		//add the new user
		assertFormPresent("experimenterForm");
        assertSubmitButtonPresent("experimenterForm:submitAdd");
        setTextField("experimenterForm:omeName", "user1");
        setTextField("experimenterForm:firstName", "User");
        setTextField("experimenterForm:middleName", "T");
        setTextField("experimenterForm:lastName", "Test");
        setTextField("experimenterForm:email", "user@email.com");
        setTextField("experimenterForm:institution", "Lab");
        checkCheckbox("experimenterForm:_idJsp13");
        submit("experimenterForm:submitAdd");
        
        
	}

}
