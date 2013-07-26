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


class WebAdminTestBase (SeleniumTestBase):


    def chosenPicker(self, selectSelector, toAdd=[], toRemove=[]):
        """
        For a Chosen plugin based on the <select> with selectSelector E.g. #id_other_groups
        click on options with the specified values
        """
        driver = self.driver
        for val in toAdd:
            # get the text value from the underlying select option
            optionText = driver.execute_script("return $('%s option:[value=\"%s\"]').text()" % (selectSelector, val))
            driver.find_element_by_css_selector("%s_chzn input" % selectSelector).click()     # show the list each time
            # ...and click the one with the text we want # NB: Can't use :contains in css_selector!?
            # driver.find_element_by_css_selector("#li:contains('private (rw----)')")
            driver.find_element_by_xpath('//li[contains(text(), "%s")]' % optionText).click()

        for val in toRemove:
            optionText = driver.execute_script("return $('%s option:[value=\"%s\"]').text()" % (selectSelector, val))
            driver.find_element_by_xpath('//span[contains(text(), "%s")]/following-sibling::a[1]' % optionText).click()


class AdminTests (WebAdminTestBase):


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

        # Pick the group from the Chosen plugin
        self.chosenPicker("#id_other_groups", [gId])

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


    def testRemoveExpFromGroup(self):
        
        #print "testRemoveExpFromGroup"
        
        groupName1 = "Sel-test1%s" % random()
        groupName2 = "Sel-test2%s" % random()
        groupName3 = "Sel-test3%s" % random()
        
        omeName = 'OmeName%s' % random()
        firstName = 'Selenium'
        lastName = 'Test'
        password = 'secretPassword'
        driver = self.driver
        
        # first create groups and a new experimenter in both groups
        group1Id = self.createGroup(groupName1)
        self.assertTrue(group1Id > 0)
        group2Id = self.createGroup(groupName2)
        self.assertTrue(group2Id > 0)
        group3Id = self.createGroup(groupName3)
        self.assertTrue(group2Id > 0)
        
        # create the experimenter in 2 groups
        eId = self.createExperimenter(omeName, group1Id)
        self.assertTrue(eId > 0)
        self.getRelativeUrl("/webadmin/experimenter/edit/%d" % eId)

        # try promoting the user to admin and adding to new group, making that group the default
        adminChbx = driver.find_element_by_id("id_administrator")
        adminChbx.click()
        self.chosenPicker("#id_other_groups", [group3Id])
        # try remove one of the original groups
        self.chosenPicker("#id_other_groups", toRemove=[group1Id])
        
        # submit and wait
        adminChbx.submit()
        WebDriverWait(driver, 10).until(EC.title_contains("Users"))

        # failed to do this with xpath: driver.find_elements_by_xpath('//tr[td/text() = "%s")]/td[5]/img[@alt="admin"]' % omeName)
        # use jQuery instead:
        adminImg = driver.execute_script("return $('#experimenterTable td:contains(\"%s\")').next().next().children('img[title=\"admin\"]').length" % omeName)
        self.assertTrue(adminImg > 0, "No admin icon for User: %s" % eId)


    def tearDown(self):
        self.logout()
        super(AdminTests, self).tearDown()


if __name__ == "__main__":
   Utils.runAsScript('webadmin')
