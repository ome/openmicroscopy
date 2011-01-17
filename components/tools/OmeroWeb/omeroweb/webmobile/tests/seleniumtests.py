from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils

class TestBase (SeleniumTestBase):
   def testTheTests (self):
       #import pdb
       #pdb.set_trace()
       
       sel = self.selenium
       sel.open("/m/login")
       sel.type("username", "will")
       sel.type("password", "ome")
       sel.click("//input[@value='login']")
       sel.wait_for_page_to_load("30000")
       sel.click("//div[4]/a[1]/div")
       sel.wait_for_page_to_load("30000")
       sel.click("//div[@id='top-header']/a/div/img")
       sel.wait_for_page_to_load("30000")


if __name__ == "__main__":
   Utils.runAsScript('m')