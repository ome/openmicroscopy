from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils
from omero.gateway.scripts import dbhelpers

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
        
    def testLoginAndPages (self):
        """
        This logs in to webadmin as 'root' 'omero' and checks that the links exist for the main pages. 
        Then visits each page in turn. Starts at '
        """
        # login done already in setUp()
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

    def tearDown(self):
        self.logout()
        super(AdminTests, self).tearDown()

    def tearDown(self):
        self.logout()
        super(AdminTests, self).tearDown()


if __name__ == "__main__":
   Utils.runAsScript('webadmin')
