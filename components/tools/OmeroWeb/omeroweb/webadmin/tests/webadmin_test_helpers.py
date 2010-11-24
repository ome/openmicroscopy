import unittest, os
import omero

from django.test.client import Client
from django.conf import settings

from webgateway import views
from request_factory import RequestFactory

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
        
        self.rootconn = self.doLogin('root', self.root_password, 1)
    
    def doLogin (self, username, passwd, server_id, secure=None):
        request = fakeRequest('get')
        blitz = settings.SERVER_LIST.get(pk=server_id)
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['username'] = username
        request.session['password'] = passwd
        request.session['ssl'] = (True, False)[secure is None]
        
        from omeroweb.webgateway.views import getBlitzConnection
        conn = views.getBlitzConnection(request, useragent="TEST.webadmin")
        if conn is not None and conn.isConnected() and conn.keepAlive():
            return conn        
        raise AttributeError('Cannot connect')
        
    def createNewUser(self):        
        if not self.rootconn:
            raise exceptions.Exception("No root connection. Cannot create user")

        admin = self.rootconn.getAdminService()
        name = self.uuid()

        # Create group if necessary
        if not group:
            g = self.new_group(perms = perms)
            group = g.name.val
        else:
            g, group = self.group_and_name(group)

        # Create user
        e = omero.model.ExperimenterI()
        e.omeName = rstring(name)
        e.firstName = rstring(name)
        e.lastName = rstring(name)
        uid = admin.createUser(e, group)
        e = admin.lookupExperimenter(name)
        if admin:
            admin.setGroupOwner(g, e)
        return admin.getExperimenter(uid)