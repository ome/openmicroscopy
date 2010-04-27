import unittest, time, os, datetime
import tempfile

#from models import StoredConnection
from webgateway.webgateway_cache import FileCache, WebGatewayCache
from webgateway import views
import omero
from omero.gateway.scripts.testdb_create import *

from django.test.client import Client
from django.core.handlers.wsgi import WSGIRequest
from django.conf import settings
from django.http import QueryDict

CLIENT_BASE='test'

def fakeRequest ():
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
    return c.bogus_request()

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

class HelperObjectsTest (unittest.TestCase):
    def testColorHolder (self):
        ColorHolder = omero.gateway.ColorHolder
        c1 = ColorHolder()
        self.assertEqual(c1._color, {'red': 0, 'green': 0,'blue': 0, 'alpha': 255})
        c1 = ColorHolder('blue')
        self.assertEqual(c1.getHtml(), '0000FF')
        self.assertEqual(c1.getCss(), 'rgba(0,0,255,1.000)')
        self.assertEqual(c1.getRGB(), (0,0,255))
        c1.setRed(0xF0)
        self.assertEqual(c1.getCss(), 'rgba(240,0,255,1.000)')
        c1.setGreen(0x0F)
        self.assertEqual(c1.getCss(), 'rgba(240,15,255,1.000)')
        c1.setBlue(0)
        self.assertEqual(c1.getCss(), 'rgba(240,15,0,1.000)')
        c1.setAlpha(0x7F)
        self.assertEqual(c1.getCss(), 'rgba(240,15,0,0.498)')
        c1 = ColorHolder.fromRGBA(50,100,200,300)
        self.assertEqual(c1.getCss(), 'rgba(50,100,200,1.000)')

    def testOmeroType (self):
        omero_type = omero.gateway.omero_type
        self.assert_(isinstance(omero_type('rstring'), omero.RString))
        self.assert_(isinstance(omero_type(u'rstring'), omero.RString))
        self.assert_(isinstance(omero_type(1), omero.RInt))
        self.assert_(isinstance(omero_type(1L), omero.RLong))
        self.assert_(not isinstance(omero_type((1,2,'a')), omero.RType))

    def testSplitHTMLColor (self):
        splitHTMLColor = omero.gateway.splitHTMLColor
        self.assertEqual(splitHTMLColor('abc'), [0xAA, 0xBB, 0xCC, 0xFF])
        self.assertEqual(splitHTMLColor('abcd'), [0xAA, 0xBB, 0xCC, 0xDD])
        self.assertEqual(splitHTMLColor('abbccd'), [0xAB, 0xBC, 0xCD, 0xFF])
        self.assertEqual(splitHTMLColor('abbccdde'), [0xAB, 0xBC, 0xCD, 0xDE])
        self.assertEqual(splitHTMLColor('#$%&%'), None)


#class StoredConnectionModelTest(WGTest):
#    def setUp (self):
#        super(StoredConnectionModelTest, self).setUp()
#        self._conf = tempfile.mkstemp()
#        f = os.fdopen(self._conf[0], 'w')
#        conffile = os.path.join(getattr(settings, 'ETCPATH', '.'), 'ice.config')
#        if os.path.exists(conffile):
#            for l in open(conffile, 'rb').readlines():
#                if l.startswith('omero.'):
#                    f.write(l.strip()+'\n')
#        f.write('weblitz.anon_user=%s\nweblitz.anon_pass=%s\nweblitz.admin_group=system\n' % (self.USER.name, self.USER.passwd))
#        f.close()
#        conn = StoredConnection.objects.filter(base_path__iexact=CLIENT_BASE, enabled__exact=True)
#        if len(conn) > 0:
#            # already have the connection, update the config file
#            s = conn[0]
#            s.config_file = self._conf[1]
#        else:
#            # First call, create the connection
#            s = StoredConnection(base_path=CLIENT_BASE, config_file=self._conf[1])
#        s.save()
#        conn = StoredConnection.objects.filter(base_path__iexact=CLIENT_BASE, enabled__exact=True)
#        self.assert_(len(conn) > 0, 'Can not find connection %s' % CLIENT_BASE)
#        self.conn = conn[0]
#        self.gateway = self.conn.getBlitzGateway(trysuper=True)
#        self.assert_(self.gateway, 'Can not get gateway from connection')
#        self._has_connected = False
#
#    def doLogin (self, user):
#        super(StoredConnectionModelTest, self).doLogin(user)
#        self.gateway.conn = self.conn
#    
#    def tearDown (self):
#        self.doDisconnect()
#        os.remove(self._conf[1])

class FileCacheTest(unittest.TestCase):
    def setUp (self):
        self.cache = FileCache('test_cache')
    
    def tearDown (self):
        os.system('rm -fr test_cache')

    def testTimeouts (self):
        self.assertEqual(self.cache.get('date/test/1'), None, 'Key already exists in cache')
        self.cache.set('date/test/1', '1', 3)
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key not properly cached')
        time.sleep(4)
        self.assertEqual(self.cache.get('date/test/1'), None, 'Timeout failed')

    def testMaxSize (self):
        self.cache._max_size = 16 # KBytes
        # There is an overhead (8 bytes in my system) for timestamp per file,
        # and the limit is only enforced after we cross over it
        for i in range(5):
            self.cache.set('date/test/%d' % i, 'abcdefgh'*510) # just under 4KB
        #for i in range(4):
        #    self.assertEqual(self.cache.get('date/test/%d' % i), 'abcdefgh'*510, 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/0'), 'abcdefgh'*510, 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/4'), None, 'Size limit failed')

    def testMaxEntries (self):
        self.cache._max_entries = 2
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/2'), '2', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/3'), None, 'File number limit failed')

    def testPurge (self):
        self.cache._max_entries = 2
        self.cache._default_timeout = 3
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/2'), '2', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/3'), None, 'File number limit failed')
        time.sleep(4)
        self.cache.set('date/test/3', '3')
        self.assertEqual(self.cache.get('date/test/3'), '3', 'Purge not working')

        
class WebGatewayCacheTest(unittest.TestCase):
    def setUp (self):
        self.wcache = WebGatewayCache(backend=FileCache, basedir='test_cache')
        class r:
            REQUEST = {'c':'1|292:1631$FF0000,2|409:5015$0000FF','m':'c', 'q':'0.9'}
        self.request = r()

    def tearDown (self):
        os.system('rm -fr test_cache')

    def testThumbCache (self):
        self.assertEqual(self.wcache.getThumb(self.request, 'test', 1), None)
        self.wcache.setThumb(self.request, 'test', 1, 'thumbdata')
        self.assertEqual(self.wcache.getThumb(self.request, 'test', 1), 'thumbdata', 'Thumb not properly cached')
        self.wcache.clearThumb(self.request, 'test', 1)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', 1), None)

    def testImageCache (self):
        # Also add a thumb, as it should get deleted with image
        self.assertEqual(self.wcache.getThumb(self.request, 'test', 1), None)
        self.wcache.setThumb(self.request, 'test', 1, 'thumbdata')
        self.assertEqual(self.wcache.getThumb(self.request, 'test', 1), 'thumbdata', 'Thumb not properly cached')
        img = omero.gateway.ImageWrapper(None, omero.model.ImageI(1,False))
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), None)
        self.wcache.setImage(self.request, 'test', img, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), 'imagedata', 'Image not properly cached')
        self.wcache.clearImage(self.request, 'test', img)
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), None)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', 1), None)

class JsonTest (WGTest):
    def testImageData (self):
        self.loginAsAuthor()
        iid = self.getTestImage().getId()
        r = fakeRequest()
        v = views.imageData_json(r, iid=iid, _conn=self.gateway)
        self.assert_(type(v) == type(''))
        self.assert_('"width": 512' in v)
        self.assert_('"split_channel":' in v)
        self.assert_('"pixel_range": [-32768, 32767]' in v)

class UserProxyTest (WGTest):
    def test (self):
        self.loginAsAuthor()
        user = self.gateway.user
        self.assertEqual(user.isAdmin(), False)
        int(user.getId())
        self.assertEqual(user.getName(), self.AUTHOR.name)
        self.assertEqual(user.getFirstName(), self.AUTHOR.firstname)
        views._purge(True)


class ZZ_TDTest(WGTest):
    def setUp (self):
        super(ZZ_TDTest, self).setUp(skipTestDB=True)
        dbhelpers.cleanup()

    def runTest (self):
        pass
