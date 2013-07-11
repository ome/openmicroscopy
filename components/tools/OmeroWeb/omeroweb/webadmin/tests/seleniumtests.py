#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
#
# Copyright (C) 2008-2013 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

import os
import omero
from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from random import random
from django.conf import settings


def createExperimenter(sel, omeName, groupNames, password="ome", firstName="Selenium", lastName="Test"):
    """
    Helper method for creating an experimenter in the specified group. 
    The group 'groupName' must already exist. 
    Returns the expId if experimenter created successfully (omeName is found in table of experimenters)
    Otherwise returns 0
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
    
    eId = 0
    if sel.is_element_present("jquery=#experimenterTable tbody tr td:containsExactly(%s)" % omeName):
        # try to get experimenter ID, look in the table
        i = 0   # jquery selector uses 0-based index
        while sel.get_text('jquery=#experimenterTable tbody tr td.action+td+td:eq(%d)' % i) != omeName:
           i+=1
           # raises exception if out of bounds for the html table
        idTxt = sel.get_text("//table[@id='experimenterTable']/tbody/tr[%d]/td[1]" % (i+1) ) # 1-based index
        eId = long(idTxt.strip("id:"))  # 'id:123'
        
    return eId

class WebAdminTestBase (SeleniumTestBase):

    def login (self, u, p, sid=None): #sid
        driver = self.driver
        # self.getRelativeUrl("/webclient/logout/")    # not needed?
        self.getRelativeUrl("/webclient/login/")
        if sid is not None:
            select = driver.find_element_by_tag_name("select")
            option = select.find_element_by_css_selector("option[value='%s']" % sid)
            option.click()

        driver.find_element_by_name("username").send_keys(u)
        pwInput = driver.find_element_by_name("password")
        pwInput.send_keys(p)
        # submit the form
        pwInput.submit()
        # Wait to be redirected to the webadmin 'home page'
        WebDriverWait(driver, 10).until(EC.title_contains("Webclient"))

    def logout (self):
        driver = self.driver
        self.getRelativeUrl("/webclient/logout/")
        WebDriverWait(driver, 10).until(EC.title_contains("Login"))

    def createGroup (self, groupName):
        """
        Helper method for creating a new group with the given name. 
        Must be logged in as root. Creates a private group. 
        Returns groupId if creation sucessful (the groups page displays new group name)
        Otherwise returns None
        """
        driver = self.driver
        self.getRelativeUrl("/webadmin/groups")
        driver.find_element_by_link_text("Add new Group").click()
        WebDriverWait(driver, 10).until(EC.title_is("Add group"))
        nameInput = driver.find_element_by_name("name")
        nameInput.send_keys(groupName)
        nameInput.submit()
        WebDriverWait(driver, 10).until(EC.title_is("OMERO Groups"))
        tdText = driver.execute_script("return $('td:contains(\"%s\")').prev().text()" % groupName)
        if len(tdText) > 0:
            return long(tdText)


class AdminTests (WebAdminTestBase):

    def setUp(self):
        super(AdminTests, self).setUp()

        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            root_password = c.ic.getProperties().getProperty('omero.rootpass')
            omero_host = c.ic.getProperties().getProperty('omero.host')
        finally:
            c.__del__()

        from omeroweb.connector import Server
        server_id = Server.find(host=omero_host)[0].id
        self.login('root', root_password, server_id)


    def testPages (self):
        """
        This checks that the links exist for the main Users & Groups pages. 
        Visits each page in turn. Starts at experimenters and clicks links to each other main page '
        """
        # login done already in setUp()
        driver = self.driver
        self.getRelativeUrl("/webadmin/experimenters")     # start at the 'Users' page

        driver.find_element_by_link_text("Groups").click()
        WebDriverWait(driver, 10).until(EC.title_contains("Groups"))
        driver.find_element_by_link_text("Users").click()
        WebDriverWait(driver, 10).until(EC.title_contains("Users"))


    def testCreateExperimenter (self):
        """
        Creates a new group and experimenter. Tests that ommiting to fill 
        in 'ome-name' gives a correct message to user.
        Checks that the new user is displayed in the table of experimenters.
        """
        #print "testCreateExperimenter"  #print

        groupName = "Selenium-testCreateExp%s" % random()

        # uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        uuid = random()
        omeName = 'OmeName%s' % uuid
        firstName = 'Selenium'
        lastName = 'Test'
        password = 'secretPassword'

        # first create a group for the new experimenter
        driver = self.driver
        gId = self.createGroup(groupName)
        self.assertTrue(gId is not None and gId > 0)

        self.getRelativeUrl("/webadmin/experimenters")
        driver.find_element_by_link_text("Add new User").click()
        WebDriverWait(driver, 10).until(EC.title_is("New User"))
        # Don't fill out omeName here.
        nameInput = driver.find_element_by_id("id_first_name")
        nameInput.send_keys(firstName)
        driver.find_element_by_id("id_last_name").send_keys(lastName)
        driver.find_element_by_id("id_password").send_keys(password)
        driver.find_element_by_id("id_confirmation").send_keys(password)

        # In order to use the Chosen plugin, we first need to know the text of the group option we want
        # We can get this from the underlying select element
        optionText = driver.execute_script("return $('#id_other_groups option:[value=\"%s\"]').text()" % gId)

        # Chosen: show the list of groups to choose from...
        cznInput = driver.find_element_by_css_selector("#id_other_groups_chzn input").click()
        # ...and click the one with the text we want
        driver.find_element_by_xpath('//li[contains(text(), "%s")]' % optionText).click()
        # NB: Can't use :contains in css_selector!?
        # driver.find_element_by_css_selector("#li:contains('private (rw----)')")

        # Submit form...
        nameInput.submit()
        # wait for 'errorlist' to appear
        WebDriverWait(driver, 10).until(lambda driver: driver.find_element_by_css_selector("ul.errorlist"))
        self.assertEqual(driver.find_element_by_css_selector("ul.errorlist").text, "This field is required.")

        # Now we fill in the missing OME username.
        omenameInput = driver.find_element_by_id("id_omename")
        omenameInput.send_keys(omeName)
        # Need to re-enter passwords
        driver.find_element_by_id("id_password").send_keys(password)
        driver.find_element_by_id("id_confirmation").send_keys(password)
        # submit
        omenameInput.submit()

        # Check the 'Users' page for omeName:
        WebDriverWait(driver, 10).until(EC.title_contains("Users"))
        # self.failUnless(sel.is_text_present(omeName))
        self.assertTrue(len(driver.find_elements_by_xpath('//td[contains(text(), "%s")]' % omeName)) > 0, "New username not in Users table")


    def testCreateGroup(self):
        """
        This needs to run before testCreateExperimenter()
        """
        groupName = "Selenium-testCreateGroup%s" % random()

        driver = self.driver
        # Create a group and checks for new Group on the page of Groups.
        gId = self.createGroup(groupName)
        self.assertTrue(gId is not None and gId > 0)


    def XtestRemoveExpFromGroup(self):
        
        #print "testRemoveExpFromGroup"
        
        groupName1 = "Sel-test1%s" % random()
        groupName2 = "Sel-test2%s" % random()
        groupName3 = "Sel-test3%s" % random()
        
        omeName = 'OmeName%s' % random()
        firstName = 'Selenium'
        lastName = 'Test'
        password = 'secretPassword'
        sel = self.selenium
        
        # first create groups and a new experimenter in both groups
        group1Id = createGroup(sel, groupName1)
        self.assertTrue(group1Id > 0)
        group2Id = createGroup(sel, groupName2)
        self.assertTrue(group2Id > 0)
        group3Id = createGroup(sel, groupName3)
        self.assertTrue(group2Id > 0)
        
        # create the experimenter in 2 groups
        eId = createExperimenter(sel, omeName, [groupName1, groupName2])
        self.assertTrue(eId > 0)
        sel.open("/webadmin/experimenter/edit/%d" % eId)
        sel.wait_for_page_to_load("30000")
        self.assertEqual("WebAdmin - Edit scientist", sel.get_title())
        
        # try promoting the user to admin and adding to new group, making that group the default
        sel.click("id_administrator")
        sel.add_selection("id_available_groups", "label=%s" % groupName3)
        sel.click("add")
        self.waitForElementVisibility('id_default_group_%d' % group3Id, True)  # radio button for 'default group'
        sel.click('id_default_group_%d' % group3Id)
        
        # try remove one of the original groups
        sel.click("default_group_%d" % group1Id)
        #self.waitForElementVisibility('id_default_group_%d' % group1Id, False)
        self.waitForElementVisibility('default_group_%d' % group1Id, False)     # BUG: this is not working at the moment. 
        
        # save
        sel.click("//input[@value='Save']")
        sel.wait_for_page_to_load("30000")
        
        # find experimenter in table - look for 'admin' icon
        i = 1
        while sel.get_text("//table[@id='experimenterTable']/tbody/tr[%s]/td[3]" % i) != omeName:
           i+=1
           # raises exception if out of bounds for the html table
        self.assert_(sel.is_element_present("//table[@id='experimenterTable']/tbody/tr[%s]/td[5]/img[@alt='admin']" % i))
        

    def tearDown(self):
        self.logout()
        super(AdminTests, self).tearDown()


if __name__ == "__main__":
   Utils.runAsScript('webadmin')
