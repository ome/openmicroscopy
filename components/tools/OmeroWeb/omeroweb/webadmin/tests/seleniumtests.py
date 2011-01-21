from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils
from omero.gateway.scripts import dbhelpers

class WebAdminTestBase (SeleniumTestBase):
    
    def setUp(self):
        pass
        
    def login (self, u, p):
        if self.selenium.is_element_present('link=Log out'):
            print "logging out..."
            self.logout()
        print "logging in..."
        self.selenium.click("link=Log in")
        self.selenium.wait_for_page_to_load("30000")
        self.selenium.type("username", u)
        self.selenium.type("password", p)
        self.selenium.click("//input[@value='LOG IN']")
        self.selenium.wait_for_page_to_load("30000")
        self.waitForElementPresence('link=Log out')
        
    def logout (self):
        self.selenium.click("link=Log out")
        self.selenium.wait_for_page_to_load("30000")
        self.waitForElementPresence('link=Log in')
        
    def tearDown (self):
        pass
        
        
class AdminTests (SeleniumTestBase):
    
    from omero.gateway.scripts import dbhelpers
    
    """
    def setUp(self):
        super(SeleniumTestBase, self).setUp()
        
    
    def tearDown (self):
        super(SeleniumTestBase, self).tearDown()
    """
        
        
    def login (self, u, p):
        if self.selenium.is_element_present('link=Log out'):
            print "logging out..."
            self.logout()
        print "logging in..."
        self.selenium.click("link=Log in")
        self.selenium.wait_for_page_to_load("30000")
        self.selenium.type("username", u)
        self.selenium.type("password", p)
        self.selenium.click("//input[@value='LOG IN']")
        self.selenium.wait_for_page_to_load("30000")
        self.waitForElementPresence('link=Log out')
        
    def testLoginAndPages (self):
        """
        This logs in to webadmin as 'root' 'omero' and checks that the links exist for the main pages. 
        Then visits each page in turn. Starts at '
        """
        
        sel = self.selenium
        print sel
        
        u = dbhelpers.ROOT.name
        p = dbhelpers.ROOT.passwd
        print "testLoginAndPages", u, p  # root, ome  # etc/ice-config says root, omero! 
        self.login(u, 'omero')
        
        #sel = self.selenium
        sel.open("/webadmin/login")
        sel.type("id_username", "root")
        sel.type("id_password", "omero")
        sel.click("//input[@value='Connect']")
        sel.wait_for_page_to_load("30000")
        self.assertEqual("WebAdmin - Scientists", sel.get_title())
        self.failUnless(sel.is_element_present("link=Scientists"))
        sel.click("link=Groups")
        sel.wait_for_page_to_load("30000")
        sel.click("link=My Account")
        sel.wait_for_page_to_load("30000")
        sel.click("link=Drive Space")
        sel.wait_for_page_to_load("30000")
        sel.click("link=Logout")
        sel.wait_for_page_to_load("30000")


if __name__ == "__main__":
   Utils.runAsScript('webadmin')
