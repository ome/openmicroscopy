#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# Copyright (C) 2011-2013 University of Dundee & Open Microscopy Environment.
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

import omero
from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from random import random
import os, time

import sys


class WebClientTestBase (SeleniumTestBase):

    pass


class WebClientTests (WebClientTestBase):


    def createObject(self, dtype="project"):

        driver = self.driver

        # Launch dialog and Simply fill the name field
        self.startStep()
        driver.find_element_by_id("add"+dtype+"Button").click()
        nameInput = driver.find_element_by_css_selector("#new-container-form input[name='name']")
        nameInput.send_keys("Selenium-testCreate-"+dtype)

        # submit
        self.startStep()
        nameInput.submit()

        # wait for right panel to load, and get ProjectId
        self.startStep()
        time.sleep(1)       # For some reason this is makes the subsequent jsTree queries work better!?!?!
        WebDriverWait(driver, 10).until(lambda driver: driver.find_element_by_css_selector('.data_heading_id'))
        newId = driver.find_element_by_css_selector(".data_heading_id strong").text

        # get the jsTree node by ID
        newNode = driver.find_element_by_id(dtype+"-"+newId)
        # confirms Project node is selected
        newLeaf = driver.find_element_by_css_selector("#"+dtype+"-"+newId+" a.jstree-clicked")
        return newId


    def testCreateProjectAndDataset(self):
        #self.stepPause = 3;
        driver = self.driver

        eid = self.createUserAndLogin()

        # Test Project creation
        self.getRelativeUrl("/webclient/")
        self.createObject("project")

        # Refresh page - test Dataset creation independently
        self.getRelativeUrl("/webclient/")
        self.createObject("dataset")

        # Refresh again - Now test Project AND Dataset creation
        self.getRelativeUrl("/webclient/")
        pid = self.createObject("project")
        # At this point, Project is selected, Dataset should be created under it.
        did = self.createObject("dataset")
        # Check that parent of the Dataset is Project
        parentId = driver.execute_script("return $('#dataset-%s').parent().parent().attr('id')" % did)
        self.assertEqual(parentId, "project-%s" % pid, "Dataset parentId %s should be project-%s" % (parentId, pid))


    def testCreateContainersEnabled(self):
        #self.stepPause = 3;
        driver = self.driver

        eid = self.createUserAndLogin()
        self.getRelativeUrl("/webclient/")

        def assertButtonEnabled(buttonId, enabled):
            state = driver.find_element_by_id(buttonId).is_enabled()
            self.assertEqual(state, enabled, "Button %s should be enabled: %s" % (buttonId, enabled))

        # Tried to do a right-click via Selenium without success. (failed with jquery too)
        # def rightClick(elementId):
        #     # from selenium.webdriver.common.action_chains import ActionChains
        #     # ac = ActionChains(driver)
        #     # element = driver.find_element_by_css_selector(selector)
        #     # ac.context_click(on_element=element)

        #     from selenium.webdriver.remote.command import Command
        #     from selenium.webdriver.common.keys import Keys
        #     driver.execute(Command.MOVE_TO, {'element': elementId})
        #     driver.execute(Command.CLICK, {'button': 2})

        def selectNode(cssSelector):
            #driver.execute_script("$('#dataTree').jstree('select_node', '%s')" % cssSelector)
            driver.find_element_by_css_selector("%s>a" % cssSelector).click()

        def assertContextMenuEnabled(rel, enabled):
            # works on the currently selected node
            driver.execute_script("$('#dataTree').jstree('show_contextmenu')")
            disabled = driver.execute_script("return $('#vakata-contextmenu a[rel=\"%s\"]').parent().hasClass('jstree-contextmenu-disabled')" % rel)
            self.assertNotEqual(disabled, enabled, "Context menu %s should be enabled: %s" % (rel, enabled))

        # Start with Experimenter selected in jsTree: P/D buttons disabled.
        self.startStep()
        assertButtonEnabled("addprojectButton", True)
        assertButtonEnabled("adddatasetButton", True)
        assertButtonEnabled("addscreenButton", True)
        assertContextMenuEnabled("project", True)
        assertContextMenuEnabled("dataset", True)
        assertContextMenuEnabled("screen", True)
        assertContextMenuEnabled("delete", False)

        # Create a Project (selected)...
        self.startStep()
        self.createObject("project")
        assertButtonEnabled("addprojectButton", True)
        assertButtonEnabled("adddatasetButton", True)
        assertButtonEnabled("addscreenButton", True)
        assertContextMenuEnabled("project", False)
        assertContextMenuEnabled("dataset", True)
        assertContextMenuEnabled("screen", False)
        assertContextMenuEnabled("delete", True)

        # Create a Dataset (selected)...
        self.startStep()
        self.createObject("dataset")
        assertButtonEnabled("addprojectButton", False)
        assertButtonEnabled("adddatasetButton", False)
        assertButtonEnabled("addscreenButton", False)
        assertContextMenuEnabled("project", False)
        assertContextMenuEnabled("dataset", False)
        assertContextMenuEnabled("screen", False)
        assertContextMenuEnabled("delete", True)

        # Create a Screen (selected)...
        self.startStep()
        selectNode("#experimenter-0")
        self.createObject("screen")
        assertButtonEnabled("addprojectButton", True)
        assertButtonEnabled("adddatasetButton", False)
        assertButtonEnabled("addscreenButton", True)
        assertContextMenuEnabled("project", False)
        assertContextMenuEnabled("dataset", False)
        assertContextMenuEnabled("screen", False)
        assertContextMenuEnabled("delete", True)

if __name__ == "__main__":
   Utils.runAsScript('webadmin')
