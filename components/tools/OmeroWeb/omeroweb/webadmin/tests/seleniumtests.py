from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils
from omero.gateway.scripts import dbhelpers
from random import random

class WebAdminTestBase (SeleniumTestBase):

        
    def login (self, u, p):
        sel = self.selenium
        if self.selenium.is_element_present('link=Log out'):
            print "logging out..."
            self.logout()
        print "logging in..."
        sel.open("/webadmin/login")
        sel.type("id_username", "root")
        sel.type("id_password", "omero")
        sel.click("//input[@value='Connect']")
        self.waitForElementPresence('link=Scientists')
        
    def logout (self):
        self.selenium.click("link=Logout")
        self.selenium.wait_for_page_to_load("30000")
        self.waitForElementPresence("//input[@value='Connect']")
        
        
class AdminTests (WebAdminTestBase):
    
    from omero.gateway.scripts import dbhelpers
    
    def setUp(self):
        super(AdminTests, self).setUp()
        #dbhelpers.refreshConfig()
        #user = dbhelpers.ROOT.name
        #password = dbhelpers.ROOT.passwd
        #print user, password    # seems to always be 'root', 'ome' 
        self.login('root', 'omero')
        self.newGroupName = "SeleniumTestGroup%s" % random()
        
        
    def testPages (self):
        """
        This checks that the links exist for the main pages. 
        Visits each page in turn. Starts at experimenters and clicks links to each other main page '
        """
        # login done already in setUp()
        print "testPages"
        
        sel = self.selenium
        sel.open("/webadmin/experimenters")
        sel.wait_for_page_to_load("30000")
        self.assertEqual("WebAdmin - Scientists", sel.get_title())
        sel.click("link=Groups")
        sel.wait_for_page_to_load("30000")
        sel.click("link=My Account")
        sel.wait_for_page_to_load("30000")
        sel.click("link=Drive Space")
        sel.wait_for_page_to_load("30000")


    def testCreateExperimenter (self):
        """
        Creates a new experimenter. Tests that ommiting to fill in 'ome-name' gives a correct message to user.
        Checks that the new user is displayed in the table of experimenters 
        """
        print "testCreateExperimenter"
        #groupName = self.newGroupName
        groupName = 'private'
        
        # uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        uuid = random()
        omeName = 'OmeName%s' % uuid
        firstName = 'Selenium'
        lastName = 'Test'
        password = 'secretPassword'
        
        sel = self.selenium
        sel.open("/webadmin/experimenters")
        sel.click("link=Add new scientist")
        sel.wait_for_page_to_load("30000")
        # Don't fill out omeName here.
        sel.type("id_first_name", firstName)
        sel.type("id_last_name", lastName)
        sel.type("id_password", password)
        sel.type("id_confirmation", password)
        
        # choose existing group, add to new user, choose one as default group 
        sel.add_selection("id_available_groups", "label=%s" % groupName)
        sel.click("add")
        sel.click("default_group")
        sel.click("//input[@value='Save']")
        sel.wait_for_page_to_load("30000")
        
        # check that we failed to create experimenter - ome-name wasn't filled out
        self.failUnless(sel.is_text_present("This field is required."))
        sel.type("id_omename", omeName)
        sel.click("//input[@value='Save']")
        sel.wait_for_page_to_load("30000")
        
        # check omeName and 'Full Name' are on the page of Scientists. 
        self.assertEqual("WebAdmin - Scientists", sel.get_title())
        self.failUnless(sel.is_text_present(omeName))
        self.failUnless(sel.is_text_present("%s %s" % (firstName, lastName)))
        # better to check text in right place
        self.assert_(sel.is_element_present("jquery=#experimenterTable tbody tr td:containsExactly(%s)" % omeName))
    
    
    def testCreateGroup(self):
        """
        This needs to run before testCreateExperimenter()
        """
        print "testCreateGroup"
        groupName = self.newGroupName
        
        sel = self.selenium
        sel.open("/webadmin/groups")
        sel.click("link=Add new group")
        sel.wait_for_page_to_load("30000")
        sel.type("id_name", groupName)
        sel.click("//input[@value='Save']")
        sel.wait_for_page_to_load("30000")
        
        # check new Group is on the page of Groups. 
        self.assertEqual("WebAdmin - Groups", sel.get_title())
        self.failUnless(sel.is_text_present(groupName))
        self.assert_(sel.is_element_present("jquery=#groupTable tbody tr td:containsExactly(%s)" % groupName))
        

    def tearDown(self):
        self.logout()
        super(AdminTests, self).tearDown()


if __name__ == "__main__":
   Utils.runAsScript('webadmin')
