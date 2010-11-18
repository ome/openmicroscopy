import unittest, time, os, datetime
import tempfile

from webgateway import views
from webgateway import views

import omero
from omero.gateway.scripts.testdb_create import *

from django.test.client import Client
from django.core.handlers.wsgi import WSGIRequest
from django.conf import settings
from django.http import QueryDict

CLIENT_BASE='test'

def fakeRequest (**kwargs):
    def bogus_request(self, **request):
        """
        The master request method. Composes the environment dictionary
        and passes to the handler, returning the result of the handler.
        Assumes defaults for the query environment, which can be overridden
        using the arguments to the request.
        """
        environ = {
            'HTTP_COOKIE':      self.cookies,
            'PATH_INFO':         '/',
            'QUERY_STRING':      '',
            'REQUEST_METHOD':    'GET',
            'SCRIPT_NAME':       '',
            'SERVER_NAME':       'testserver',
            'SERVER_PORT':       '80',
            'SERVER_PROTOCOL':   'HTTP/1.1',
            'HTTP_HOST':         'localhost',
            'wsgi.version':      (1,0),
            'wsgi.url_scheme':   'http',
            'wsgi.errors':       None,#self.errors,
            'wsgi.multiprocess': True,
            'wsgi.multithread':  False,
            'wsgi.run_once':     False,
        }
        environ.update(self.defaults)
        environ.update(request)
        r = WSGIRequest(environ)
        if 'django.contrib.sessions' in settings.INSTALLED_APPS:
            engine = __import__(settings.SESSION_ENGINE, {}, {}, [''])
        r.session = engine.SessionStore()
        qlen = len(r.REQUEST.dicts)
        def setQuery (**query):
            r.REQUEST.dicts = r.REQUEST.dicts[:qlen]
            q = QueryDict('', mutable=True)
            q.update(query)
            r.REQUEST.dicts += (q,)
        r.setQuery = setQuery
        return r
    Client.bogus_request = bogus_request
    c = Client()
    return c.bogus_request(**kwargs)

class WGTest (GTest):
    def doLogin (self, user):
        r = fakeRequest()
        q = QueryDict('', mutable=True)
        q.update({'username': user.name, 'password': user.passwd})
        r.REQUEST.dicts += (q,)
        self.gateway = views.getBlitzConnection(r, 1, group=user.groupname, try_super=user.admin)
        if self.gateway is None:
            # If the login framework was customized (using this app outside omeroweb) the above fails
            super(WGTest, self).doLogin(user)
            self.gateway.user = views.UserProxy(self.gateway)

class UserProxyTest (WGTest):
    def test (self):
        self.loginAsAuthor()
        user = self.gateway.user
        self.assertEqual(user.isAdmin(), False)
        int(user.getId())
        self.assertEqual(user.getName(), self.AUTHOR.name)
        self.assertEqual(user.getFirstName(), self.AUTHOR.firstname)
        views._purge(True)
