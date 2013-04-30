#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
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

from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils
from omero.gateway.scripts import dbhelpers
from random import random

import sys


class WebClientTestBase (SeleniumTestBase):

        
    def login (self, u, p):
        sel = self.selenium
        if self.selenium.is_element_present('link=Log out'):
            self.logout()
        sel.open("/webclient/login")
        sel.type("id_username", u)
        sel.type("id_password", p)
        sel.click("//input[@value='Connect']")
        
    def logout (self):
        self.selenium.open("/webclient/logout")
        self.selenium.wait_for_page_to_load("30000")
        self.waitForElementPresence("//input[@value='Connect']")
        
    
    def import_image(self, filename = None):
        """
        This code from OmeroPy/tests/integration/library.py
        TODO: Trying to find a way to do import from here, but no luck yet. 
        """
        #server = self.client.getProperty("omero.host")
        #port = self.client.getProperty("omero.port")
        #key = self.client.getSessionId()
        server = 'localhost'
        port = '4064'
        key = ''
        
        if filename is None:
            filename = self.OmeroPy / ".." / ".." / ".." / "components" / "common" / "test" / "tinyTest.d3d.dv"

        # Search up until we find "OmeroPy"
        dist_dir = self.OmeroPy / ".." / ".." / ".." / "dist"
        args = [sys.executable]
        args.append(str(path(".") / "bin" / "omero"))
        args.extend(["-s", server, "-k", key, "-p", port, "import", filename])
        popen = subprocess.Popen(args, cwd=str(dist_dir), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = popen.communicate()
        rc = popen.wait()
        if rc != 0:
            raise Exception("import failed: [%r] %s\n%s" % (args, rc, err))
        pix_ids = []
        for x in out.split("\n"):
            if x and x.find("Created") < 0 and x.find("#") < 0:
                try:    # if the line has an image ID...
                    imageId = str(long(x.strip()))
                    pix_ids.append(imageId)
                except: pass
        return pix_ids
        
        
class WebClientTests (WebClientTestBase):
    
    from omero.gateway.scripts import dbhelpers
    
    def setUp(self):
        super(WebClientTests, self).setUp()
        #dbhelpers.refreshConfig()
        #user = dbhelpers.ROOT.name
        #password = dbhelpers.ROOT.passwd
        #print user, password    # seems to always be 'root', 'ome' 
        self.login('will', 'ome')
        
        
    def testMetadata (self):
        """
        Displays the metadata page for an image.
        """
        
        #print "testMetadata"
        
        sel = self.selenium
        sel.open("/webclient/metadata_details/image/4183")
        #sel.click("link=Metadata")     # Making metadata 'visible' to user is unecessary for tests below
        self.assertEqual("480 x 480 x 46 x 1", sel.get_table("//div[@id='metadata_tab']/table[2].0.1"))
        
        # Check channel names...
        self.failUnless(sel.is_text_present("DAPI"))    # anywhere on page
        # more specific (too fragile?)
        self.assertEqual("DAPI", sel.get_text("//div[@id='metadata_tab']/h1[5]/span"))
        self.assertEqual("FITC", sel.get_text("//div[@id='metadata_tab']/h1[6]/span"))
        self.assertEqual("RD-TR-PE", sel.get_text("//div[@id='metadata_tab']/h1[7]/span"))
        self.assertEqual("CY-5", sel.get_text("//div[@id='metadata_tab']/h1[8]/span"))
        
        # check value of Channel inputs.
        self.assertEqual("DAPI", sel.get_value("//div[@id='metadata_tab']/div[4]/table/tbody/tr[1]/td[2]/input"))   # Name
        self.assertEqual("360", sel.get_value("//div[@id='metadata_tab']/div[4]/table/tbody/tr[2]/td[2]/input"))   # Excitation
        self.assertEqual("457", sel.get_value("//div[@id='metadata_tab']/div[4]/table/tbody/tr[3]/td[2]/input"))   # Excitation
        
        # using id='id_name' gets us the FIRST element with that id (currently 1 per channel)
        self.assertEqual("DAPI", sel.get_value("//input[@id='id_name']"))
        

    def tearDown(self):
        self.logout()
        super(WebClientTests, self).tearDown()


if __name__ == "__main__":
   Utils.runAsScript('webadmin')
