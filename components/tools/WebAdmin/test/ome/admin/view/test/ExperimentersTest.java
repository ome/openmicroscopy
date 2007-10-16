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
		assertKeyPresent("experimentersLastName");
		assertKeyPresent("experimentersOmeName");
		assertKeyPresent("experimentersInstitution");
		assertImagePresent("/WebAdmin/images/admin.png", "Administrator");
		assertImagePresent("/WebAdmin/images/active.png", "Active");
		assertImagePresent("/WebAdmin/images/active.png", "Active");
		assertImagePresent("/WebAdmin/images/del.png", "Delete scientist");
		assertImagePresent("/WebAdmin/images/edit.png", "Edit");
	}

	public void testExperimenterAdd() {
		beginAt("/experimenterForm.jsf");
		login();

		assertKeyPresent("experimentersAddNew");
		assertKeyPresent("experimentersName");
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

	}

}
