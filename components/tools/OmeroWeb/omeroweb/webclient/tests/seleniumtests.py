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
import os

import sys


class WebClientTestBase (SeleniumTestBase):

    pass


class WebClientTests (WebClientTestBase):


    def createObject(self, dtype="project"):

        driver = self.driver
        driver.find_element_by_id("add"+dtype+"Button").click()

        # Simply fill the name field and submit
        nameInput = driver.find_element_by_css_selector("#new-container-form input[name='name']")
        nameInput.send_keys("Selenium-testCreate-"+dtype)
        nameInput.submit()

        # wait for right panel to load, and get ProjectId
        WebDriverWait(driver, 10).until(lambda driver: driver.find_element_by_css_selector('.data_heading_id'))
        newId = driver.find_element_by_css_selector(".data_heading_id strong").text

        # get the jsTree node by ID
        newNode = driver.find_element_by_id(dtype+"-"+newId)
        # confirms Project node is selected
        newLeaf = driver.find_element_by_css_selector("#"+dtype+"-"+newId+" a.jstree-clicked")


    def testCreateDataset(self):

        driver = self.driver

        eid = self.createUserAndLogin()
        self.getRelativeUrl("/webclient/")

        self.createObject("dataset")


    def testCreateProject(self):

        driver = self.driver

        eid = self.createUserAndLogin()
        self.getRelativeUrl("/webclient/")

        self.createObject("project")


if __name__ == "__main__":
   Utils.runAsScript('webadmin')
