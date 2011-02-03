from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils
from omero.gateway.scripts import dbhelpers
from random import random


def createGroup(sel, groupName):
    """
    Helper method for creating a new group with the give name. 
    Must be logged in as root. Creates a private group. 
    Returns true if creation sucessful (the groups page displays new group name)
    """
    sel.open("/webadmin/groups")
    sel.click("link=Add new group")
    sel.wait_for_page_to_load("30000")
    sel.type("id_name", groupName)
    sel.click("//input[@value='Save']")
    sel.wait_for_page_to_load("30000")
    
    return sel.is_element_present("jquery=#groupTable tbody tr td:containsExactly(%s)" % groupName)


def createExperimenter(sel, omeName, groupNames, password="ome", firstName="Selenium", lastName="Test"):
    """
    Helper method for creating an experimenter in the specified group. 
    The group 'groupName' must already exist. 
    Returns True if experimenter created successfully (omeName is found in table of experimenters)
    """
    sel.open("/webadmin/experimenters")
    sel.click("link=Add new scientist")
    sel.wait_for_page_to_load("30000")
    sel.type("id_omename", omeName)
    sel.type("id_first_name", firstName)
    sel.type("id_last_name", lastName)
    sel.type("id_password", password)
    sel.type("id_confirmation", password)
    
    # choose existing group, add to new user, choose one as default group 
    for gName in groupNames:
        sel.add_selection("id_available_groups", "label=%s" % gName)
        sel.click("add")
    sel.click("default_group")
    sel.click("//input[@value='Save']")
    sel.wait_for_page_to_load("30000")
    
    if sel.is_element_present("jquery=#experimenterTable tbody tr td:containsExactly(%s)" % omeName):
        # try to get experimenter ID
        #expId = self.selenium.get_value("//div[@id='curr-link']/descendant::input") parent() children():first
        print dir(sel)
        expId = sel.get_text("jquery=#experimenterTable tbody tr td:containsExactly(%s)" % omeName)
        print expId     # OmeName0.742919097585    OK
        expId = sel.get_text("jquery=#experimenterTable tbody tr td:containsExactly(%s):parent" % omeName)
        print expId     # OmeName0.742919097585     Strange?
        expId = sel.get_text("jquery=#experimenterTable tbody tr td:containsExactly(%s):parent:parent td:containsExactly(%s)" % (omeName, omeName) )
        print expId     # ERROR: element not found 
        
        expId = sel.get_text("jquery=#experimenterTable tbody tr td:containsExactly(%s):parent td:contains(id)" % omeName)
        print expId
        
    return True

class WebAdminTestBase (SeleniumTestBase):

        
    def login (self, u, p):
        sel = self.selenium
        if self.selenium.is_element_present('link=Log out'):
            print "logging out..."
            self.logout()
        print "logging in..."
        sel.open("/webadmin/login")
        sel.type("id_username", u)
        sel.type("id_password", p)
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
        Creates a new experimenter (creates group first). Tests that ommiting to fill 
        in 'ome-name' gives a correct message to user.
        Checks that the new user is displayed in the table of experimenters.
        """
        print "testCreateExperimenter"
        
        groupName = "Selenium-testCreateExp%s" % random()
        
        # uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        uuid = random()
        omeName = 'OmeName%s' % uuid
        firstName = 'Selenium'
        lastName = 'Test'
        password = 'secretPassword'
        
        # first create a group for the new experimenter
        sel = self.selenium
        self.assertTrue(createGroup(sel, groupName))
        
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
        groupName = "Selenium-testCreateGroup%s" % random()
        
        sel = self.selenium
        # check new Group is on the page of Groups. 
        self.assertTrue(createGroup(sel, groupName))
    
    
    def testRemoveExpFromGroup(self):
        
        print "testRemoveExpFromGroup"
        
        groupName1 = "Selenium-testCreateExp1%s" % random()
        groupName2 = "Selenium-testCreateExp2%s" % random()
        
        omeName = 'OmeName%s' % random()
        firstName = 'Selenium'
        lastName = 'Test'
        password = 'secretPassword'
        sel = self.selenium
        
        # first create groups and a new experimenter in both groups
        self.assertTrue(createGroup(sel, groupName1))
        self.assertTrue(createGroup(sel, groupName2))
        self.assertTrue(createExperimenter(sel, omeName, [groupName1, groupName2]) )
    

    def tearDown(self):
        self.logout()
        super(AdminTests, self).tearDown()


if __name__ == "__main__":
   Utils.runAsScript('webadmin')
