import os
import unittest
import exceptions

import omero

from django.conf import settings
from request_factory import Client

from omeroweb.webgateway import views as webgateway_views

class WebTest(unittest.TestCase):
        
    def setUp (self):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            self.root_password = c.ic.getProperties().getProperty('omero.rootpass')
            omero_host = c.ic.getProperties().getProperty('omero.host')
        finally:
            c.__del__()

        blitz = settings.SERVER_LIST.find(server_host=omero_host)
        if blitz is not None:
            self.server_id = blitz.id
            self.rootconn = webgateway_views._createConnection('', host=blitz.host, port=blitz.port, username='root', passwd=self.root_password, secure=True, useragent="TEST.webadmin")
            if self.rootconn is None or not self.rootconn.isConnected() or not self.rootconn.keepAlive():
                raise exceptions.Exception("Cannot connect")
        else:
            raise exceptions.Exception("'%s' is not on omero.web.server_list")
    
    def tearDown(self):
        try:
            self.rootconn.seppuku()
        except Exception,e:
            self.fail(e)
    
    def loginAsUser(self, username, password):
        blitz = settings.SERVER_LIST.get(pk=self.server_id) 
        if blitz is not None:       
            conn = webgateway_views._createConnection('', host=blitz.host, port=blitz.port, username=username, passwd=password, secure=True, useragent="TEST.webadmin")
            if conn is None or not conn.isConnected() or not conn.keepAlive():
                raise exceptions.Exception("Cannot connect")
            return conn
        else:
            raise exceptions.Exception("'%s' is not on omero.web.server_list")

class WebAdminClientTest(WebTest):
        
    def setUp (self):
        super(WebAdminClientTest, self).setUp()
        self.client = Client()

    def tearDown(self):
        try:
            self.client.logout()
        except Exception,e:
            self.fail(e)
