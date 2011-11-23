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

#omero.gateway.BlitzGateway = omero.gateway._BlitzGateway
#omero.gateway.ProjectWrapper = omero.gateway._ProjectWrapper
#omero.gateway.DatasetWrapper = omero.gateway._DatasetWrapper
#omero.gateway.ImageWrapper = omero.gateway._ImageWrapper


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


def _testCacheFSBlockSize (cache):
    cache.wipe()
    c1 = cache._du()
    cache.set('test/1', 'a')
    c2 = cache._du()
    cache.wipe()
    return c1, c2-c1

class FileCacheTest(unittest.TestCase):
    def setUp (self):
        self.cache = FileCache('test_cache')
    
    def tearDown (self):
        os.system('rm -fr test_cache')

    def testTimeouts (self):
        self.assertEqual(self.cache.get('date/test/1'), None, 'Key already exists in cache')
        self.cache.set('date/test/1', '1', timeout=3)
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key not properly cached')
        time.sleep(4)
        self.assertEqual(self.cache.get('date/test/1'), None, 'Timeout failed')
        # if _default_timeout is 0, timeouts are simply not checked
        self.cache.wipe()
        self.cache._default_timeout = 0
        self.assertEqual(self.cache.get('date/test/1'), None, 'Key already exists in cache')
        self.cache.set('date/test/1', '1', timeout=3)
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key not properly cached')
        time.sleep(4)
        self.assert_(self.cache.has_key('date/test/1'))
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key got timedout and should not')

    def testMaxSize (self):
        empty_size, cache_block = _testCacheFSBlockSize(self.cache)
        self.cache._max_size = empty_size + 4*cache_block + 1
        # There is an overhead (8 bytes in my system) for timestamp per file,
        # and the limit is only enforced after we cross over it
        for i in range(6):
            self.cache.set('date/test/%d' % i, 'abcdefgh'*127*cache_block)
        for i in range(4):
            self.assertEqual(self.cache.get('date/test/%d' % i), 'abcdefgh'*127*cache_block,
                             'Key %d not properly cached' % i)
        self.assertEqual(self.cache.get('date/test/5'), None, 'Size limit failed')
        self.cache._max_size = 0
        self.cache.wipe()
        for i in range(6):
            self.cache.set('date/test/%d' % i, 'abcdefgh'*127*cache_block)
        for i in range(6):
            self.assertEqual(self.cache.get('date/test/%d' % i), 'abcdefgh'*127*cache_block,
                             'Key %d not properly cached' % i)

    def testMaxEntries (self):
        self.cache._max_entries = 2
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/2'), '2', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/3'), None, 'File number limit failed')
        self.cache.wipe()
        self.cache._max_entries = 0
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/2'), '2', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/3'), '3', 'Key not properly cached')

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

    def testOther (self):
        # set should only accept strings as values
        self.assertRaises(ValueError, self.cache.set, 'date/test/1', 123)
        # keys can't have .. or start with /
        self.assertRaises(ValueError, self.cache.set, '/date/test/1', '1')
        self.assertRaises(ValueError, self.cache.set, 'date/test/../1', '1')
        # get some test data in
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        self.assertEqual(self.cache.get('date/test/1'), '1', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/2'), '2', 'Key not properly cached')
        self.assertEqual(self.cache.get('date/test/3'), '3', 'Key not properly cached')
        # check has_key
        self.assert_(self.cache.has_key('date/test/1'))
        self.assert_(not self.cache.has_key('date/test/bogus'))
        # assert wipe() nukes the whole thing
        self.assertEqual(self.cache._num_entries, 3)
        self.cache.wipe()
        self.assertEqual(self.cache._num_entries, 0)
        
class WebGatewayCacheTest(unittest.TestCase):
    def setUp (self):
        self.wcache = WebGatewayCache(backend=FileCache, basedir='test_cache')
        class r:
            def __init__ (self):
                self.REQUEST = {'c':'1|292:1631$FF0000,2|409:5015$0000FF','m':'c', 'q':'0.9'}
            def new (self, q):
                rv = self.__class__()
                rv.REQUEST.update(q)
                return rv
        self.request = r()

    def tearDown (self):
        os.system('rm -fr test_cache')

    def testCacheSettings (self):
        uid = 123
        empty_size, cache_block = _testCacheFSBlockSize(self.wcache._thumb_cache)
        max_size = empty_size + 4 * cache_block + 1
        self.wcache._updateCacheSettings(self.wcache._thumb_cache, timeout=2, max_entries=5, max_size=max_size )
        for i in range(6):
            self.wcache.setThumb(self.request, 'test', uid, i, 'abcdefgh'*127*cache_block)
        for i in range(4):
            self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, i), 'abcdefgh'*127*cache_block,
                             'Key %d not properly cached' % i)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 5), None, 'Size limit failed')
        for i in range(10):
            self.wcache.setThumb(self.request, 'test', uid, i, 'abcdefgh')
        for i in range(5):
            self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, i), 'abcdefgh', 'Key %d not properly cached' % i)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 5), None, 'Entries limit failed')
        time.sleep(2)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 0), None, 'Time limit failed')

    def testThumbCache (self):
        uid = 123
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), None)
        self.wcache.setThumb(self.request, 'test', uid, 1, 'thumbdata')
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), 'thumbdata',
                         'Thumb not properly cached (%s)' % self.wcache.getThumb(self.request, 'test', uid, 1))
        self.wcache.clearThumb(self.request, 'test', uid, 1)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), None)
        # Make sure clear() nukes this
        self.wcache.setThumb(self.request, 'test', uid, 1, 'thumbdata')
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), 'thumbdata', 'Thumb not properly cached')
        self.assertNotEqual(self.wcache._thumb_cache._num_entries, 0)
        self.wcache.clear()
        self.assertEqual(self.wcache._thumb_cache._num_entries, 0)

    def testImageCache (self):
        uid = 123
        # Also add a thumb, a split channel and a projection, as it should get deleted with image
        preq = self.request.new({'p':'intmax'})
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), None)
        self.wcache.setThumb(self.request, 'test', uid, 1, 'thumbdata')
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), 'thumbdata')
        img = omero.gateway.ImageWrapper(None, omero.model.ImageI(1,False))
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), None)
        self.wcache.setImage(self.request, 'test', img, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), 'imagedata')
        self.assertEqual(self.wcache.getImage(preq, 'test', img, 2, 3), None)
        self.wcache.setImage(preq, 'test', img, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getImage(preq, 'test', img, 2, 3), 'imagedata')
        self.assertEqual(self.wcache.getSplitChannelImage(self.request, 'test', img, 2, 3), None)
        self.wcache.setSplitChannelImage(self.request, 'test', img, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getSplitChannelImage(self.request, 'test', img, 2, 3), 'imagedata')
        self.wcache.clearImage(self.request, 'test', uid, img)
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), None)
        self.assertEqual(self.wcache.getSplitChannelImage(self.request, 'test', img, 2, 3), None)
        self.assertEqual(self.wcache.getImage(preq, 'test', img, 2, 3), None)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), None)
        # The exact same behaviour, using invalidateObject
        self.wcache.setThumb(self.request, 'test', uid, 1, 'thumbdata')
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), 'thumbdata')
        self.wcache.setImage(self.request, 'test', img, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), 'imagedata')
        self.assertEqual(self.wcache.getImage(preq, 'test', img, 2, 3), None)
        self.wcache.setImage(preq, 'test', img, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getImage(preq, 'test', img, 2, 3), 'imagedata')
        self.assertEqual(self.wcache.getSplitChannelImage(self.request, 'test', img, 2, 3), None)
        self.wcache.setSplitChannelImage(self.request, 'test', img, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getSplitChannelImage(self.request, 'test', img, 2, 3), 'imagedata')
        self.wcache.invalidateObject('test', uid, img)
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), None)
        self.assertEqual(self.wcache.getSplitChannelImage(self.request, 'test', img, 2, 3), None)
        self.assertEqual(self.wcache.getImage(preq, 'test', img, 2, 3), None)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, 1), None)
        # Make sure clear() nukes this
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), None)
        self.wcache.setImage(self.request, 'test', img, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getImage(self.request, 'test', img, 2, 3), 'imagedata')
        self.assertNotEqual(self.wcache._img_cache._num_entries, 0)
        self.wcache.clear()
        self.assertEqual(self.wcache._img_cache._num_entries, 0)

    def testLocks (self):
        wcache2 = WebGatewayCache(backend=FileCache, basedir=self.wcache._basedir)
        #wcache2 will hold the lock
        self.assert_(wcache2.tryLock())
        self.assert_(not self.wcache.tryLock())
        self.assert_(wcache2.tryLock())
        del wcache2
        # The lock should have been removed
        self.assert_(self.wcache.tryLock())

    def testJsonCache (self):
        uid = 123
        ds = omero.gateway.DatasetWrapper(None, omero.model.DatasetI(1,False))
        self.assertEqual(self.wcache.getDatasetContents(self.request, 'test', ds), None)
        self.wcache.setDatasetContents(self.request, 'test', ds, 'datasetdata')
        self.assertEqual(self.wcache.getDatasetContents(self.request, 'test', ds), 'datasetdata')
        self.wcache.clearDatasetContents(self.request, 'test', ds)
        self.assertEqual(self.wcache.getDatasetContents(self.request, 'test', ds), None)
        # The exact same behaviour, using invalidateObject
        self.assertEqual(self.wcache.getDatasetContents(self.request, 'test', ds), None)
        self.wcache.setDatasetContents(self.request, 'test', ds, 'datasetdata')
        self.assertEqual(self.wcache.getDatasetContents(self.request, 'test', ds), 'datasetdata')
        self.wcache.invalidateObject('test', uid, ds)
        self.assertEqual(self.wcache.getDatasetContents(self.request, 'test', ds), None)
        # Make sure clear() nukes this
        self.assertEqual(self.wcache.getDatasetContents(self.request, 'test', ds), None)
        self.wcache.setDatasetContents(self.request, 'test', ds, 'datasetdata')
        self.assertEqual(self.wcache.getDatasetContents(self.request, 'test', ds), 'datasetdata')
        self.assertNotEqual(self.wcache._json_cache._num_entries, 0)
        self.wcache.clear()
        self.assertEqual(self.wcache._json_cache._num_entries, 0)
        
        

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
