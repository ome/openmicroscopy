#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 
# 
# Copyright (c) 2008 University of Dundee. 
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
import unittest

import omero

from django.conf import settings

from request_factory import Client
from connector import Server, Connector
from omeroweb.webgateway import views as webgateway_views

class WebTest(unittest.TestCase):
        
    def setUp (self):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            self.root_password = c.ic.getProperties().getProperty('omero.rootpass')
            omero_host = c.ic.getProperties().getProperty('omero.host')
        finally:
            c.__del__()

        blitz = Server.find(host=omero_host)
        if blitz is None:
            Server.reset()
            for s in settings.SERVER_LIST:
                server = (len(s) > 2) and unicode(s[2]) or None
                Server(host=unicode(s[0]), port=int(s[1]), server=server)
            Server.freeze()
            blitz = Server.find(server=omero_host)
        
        if len(blitz):
            self.server_id = blitz[0].id
            connector = Connector(self.server_id, True)
            self.rootconn = connector.create_connection('TEST.webadmin', 'root', self.root_password)

            if self.rootconn is None or not self.rootconn.isConnected() or not self.rootconn.keepAlive():
                raise Exception("Cannot connect")
        else:
            raise Exception("'%s' is not on omero.web.server_list" % omero_host)
    
    def tearDown(self):
        try:
            self.rootconn.seppuku()
        except Exception,e:
            self.fail(e)
    
    def loginAsUser(self, username, password):
        blitz = Server.get(pk=self.server_id) 
        if blitz is not None:
            connector = Connector(self.server_id, True)
            conn = connector.create_connection('TEST.webadmin', username, password)
            if conn is None or not conn.isConnected() or not conn.keepAlive():
                raise Exception("Cannot connect")
            return conn
        else:
            raise Exception("'%s' is not on omero.web.server_list"  % omero_host)

class WebAdminClientTest(WebTest):
        
    def setUp (self):
        super(WebAdminClientTest, self).setUp()
        self.client = Client()

    def tearDown(self):
        try:
            self.client.logout()
        except Exception,e:
            self.fail(e)
