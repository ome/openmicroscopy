from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils

class TestBase (SeleniumTestBase):
    
    def testLoginAndPages (self):
        """
        This logs in to webadmin as 'root' 'omero' and checks that the links exist for the main pages. 
        Then visits each page in turn. Starts at '
        """
        sel = self.selenium
        sel.open("/webadmin/login/")
        sel.type("id_username", "root")
        sel.type("id_password", "omero")
        sel.click("//input[@value='Connect']")
        sel.wait_for_page_to_load("30000")
        try: self.assertEqual("WebAdmin - Scientists", sel.get_title())
        except AssertionError, e: self.verificationErrors.append(str(e))
        try: self.failUnless(sel.is_element_present("link=Scientists"))
        except AssertionError, e: self.verificationErrors.append(str(e))
        try: self.failUnless(sel.is_element_present("link=Groups"))
        except AssertionError, e: self.verificationErrors.append(str(e))
        try: self.failUnless(sel.is_element_present("link=Drive Space"))
        except AssertionError, e: self.verificationErrors.append(str(e))
        try: self.failUnless(sel.is_element_present("link=My Account"))
        except AssertionError, e: self.verificationErrors.append(str(e))
        sel.click("link=Groups")
        sel.wait_for_page_to_load("30000")
        sel.click("link=My Account")
        sel.wait_for_page_to_load("30000")
        sel.click("link=Drive Space")
        sel.wait_for_page_to_load("30000")
        sel.click("link=Logout")
        sel.wait_for_page_to_load("30000")


if __name__ == "__main__":
   Utils.runAsScript('webtest')
