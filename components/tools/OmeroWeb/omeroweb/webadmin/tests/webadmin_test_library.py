import os
import unittest
import exceptions

import omero

from django.conf import settings
from request_factory import RequestFactory, Client

from omeroweb.webgateway import views as webgateway_views


def fakeRequest (method, path="/", params={}, **kwargs):
    def bogus_request(self, **request):
        """
        Usage:
        rf = RequestFactory()
        get_request = rf.get('/hello/')
        post_request = rf.post('/submit/', {'foo': 'bar'})
        """
        if not method.lower() in ('post', 'get'):
            raise AttributeError("Method must be 'get' or 'post'")                
        if not isinstance(params, dict):
            raise AttributeError("Params must be a dictionary")
                
        rf = RequestFactory()
        r = getattr(rf, method.lower())(path, params)
        return r
    Client.bogus_request = bogus_request
    c = Client()
    return c.bogus_request(**kwargs)


class WebTest(unittest.TestCase):
        
    def setUp (self):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            self.root_password = c.ic.getProperties().getProperty('omero.rootpass')
        finally:
            c.__del__()

        blitz = settings.SERVER_LIST.get(pk=1)
        self.rootconn = webgateway_views._createConnection('', host=blitz.host, port=blitz.port, username='root', passwd=self.root_password, secure=True, useragent="TEST.webadmin")
        if self.rootconn is None or not self.rootconn.isConnected() or not self.rootconn.keepAlive():
            raise exceptions.Exception("Cannot connect")
    
    def tearDown(self):
        try:
            self.rootconn.seppuku()
        except Exception,e:
            self.fail(e)
    
    def loginAsUser(self, username, password, server_id=1):
        blitz = settings.SERVER_LIST.get(pk=server_id)        
        conn = webgateway_views._createConnection('', host=blitz.host, port=blitz.port, username=username, passwd=password, secure=True, useragent="TEST.webadmin")
        if conn is None or not conn.isConnected() or not conn.keepAlive():
            raise exceptions.Exception("Cannot connect")
        return conn

class WebClientTest(unittest.TestCase):
        
    def setUp (self):
        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            self.root_password = c.ic.getProperties().getProperty('omero.rootpass')
        finally:
            c.__del__()
        
        self.client = Client()

    def tearDown(self):
        try:
            self.client.get("/webadmin/logout/")
        except Exception,e:
            self.fail(e)
