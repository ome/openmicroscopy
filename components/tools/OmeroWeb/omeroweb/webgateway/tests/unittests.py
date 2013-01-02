#!/usr/bin/env python
# -*- coding: utf-8 -*-
# coding=utf-8

import unittest, time, os, datetime
import tempfile
from StringIO import StringIO

#from models import StoredConnection
from webgateway.webgateway_cache import FileCache, WebGatewayCache, WebGatewayTempFile
from webgateway import views
import omero
from omero.gateway.scripts.testdb_create import *
from omero.gateway import OriginalFileWrapper
from omero.rtypes import unwrap

from decorators import login_required

from django.test.client import Client
from django.core.handlers.wsgi import WSGIRequest
from django.conf import settings
from django.http import QueryDict
from django.utils import simplejson

#omero.gateway.BlitzGateway = omero.gateway._BlitzGateway
#omero.gateway.ProjectWrapper = omero.gateway._ProjectWrapper
#omero.gateway.DatasetWrapper = omero.gateway._DatasetWrapper
#omero.gateway.ImageWrapper = omero.gateway._ImageWrapper


CLIENT_BASE='test'

def fakeRequest (body=None, **kwargs):
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
            'wsgi.input':        None,
        }
        environ.update(self.defaults)
        environ.update(request)
        r = WSGIRequest(environ)
        if body is not None:
            r._stream = StringIO(body)
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
    return c.bogus_request(body=None, **kwargs)

class WGTest (GTest):
    def doLogin (self, user=None):
        self.gateway = None
        if user:
            r = fakeRequest()
            q = QueryDict('', mutable=True)
            q.update({'username': user.name, 'password': user.passwd})
            r.REQUEST.dicts += (q,)
            t = login_required(isAdmin=user.admin)
            self.gateway = t.get_connection(1, r) #, group=user.groupname)
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

class WebGatewayCacheTempFileTest(unittest.TestCase):
    def setUp (self):
        self.tmpfile = WebGatewayTempFile(tdir='test_cache')

    def tearDown (self):
        os.system('rm -fr test_cache')

    def testFilenameSize (self):
        """
        Make sure slashes, dashes, underscores and other chars don't mess things up.
        Also check for filename size limits.
        """
        fname='1/2_3!"\'#$%&()=@€£‰¶÷[]≠§±+*~^\,.;:'

        try:
            fpath, rpath, fobj = self.tmpfile.new(fname, key='specialchars')
        except:
            self.fail('WebGatewayTempFile.new not handling special characters properly')
        # ext2/3/4 limit is 255 bytes, most others are equal to or larger
        fname = "a"*384
        try:
            fpath, rpath, fobj = self.tmpfile.new(fname, key='longname')
            fobj.close()
            # is it keeping extensions properly?
            fpath, rpath, fobj = self.tmpfile.new("1" + fname + '.tif', key='longname')
            fobj.close()
            self.assertEqual(fpath[-5:], 'a.tif')
            fpath, rpath, fobj = self.tmpfile.new("2" + fname + '.ome.tiff', key='longname')
            fobj.close()
            self.assertEqual(fpath[-10:], 'a.ome.tiff')
            fpath, rpath, fobj = self.tmpfile.new("3" + fname + 'ome.tiff', key='longname')
            fobj.close()
            self.assertEqual(fpath[-6:], 'a.tiff')
            fpath, rpath, fobj = self.tmpfile.new("4" + fname + 'somethingverylong.zip', key='longname')
            fobj.close()
            self.assertEqual(fpath[-5:], 'a.zip')
            fpath, rpath, fobj = self.tmpfile.new("5" + fname + '.tif.somethingverylong', key='longname')
            fobj.close()
            self.assertEqual(fpath[-5:], 'aaaaa')
        except:
            self.fail('WebGatewayTempFile.new not handling long file names properly')


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
        #empty_size, cache_block = _testCacheFSBlockSize(self.wcache._thumb_cache)
        self.wcache._updateCacheSettings(self.wcache._thumb_cache, timeout=2, max_entries=5, max_size=0 )
        cachestr = 'abcdefgh'*127
        self.wcache._thumb_cache.wipe()
        for i in range(6):
            self.wcache.setThumb(self.request, 'test', uid, i, cachestr)
        max_size = self.wcache._thumb_cache._du()
        self.wcache._updateCacheSettings(self.wcache._thumb_cache, timeout=2, max_entries=5, max_size=max_size )
        self.wcache._thumb_cache.wipe()
        for i in range(6):
            self.wcache.setThumb(self.request, 'test', uid, i, cachestr)
        for i in range(4):
            self.assertEqual(self.wcache.getThumb(self.request, 'test', uid, i), cachestr,
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
        v = views.imageData_json(r, iid=iid, server_id=1, conn=self.gateway, _internal=True)
        self.assert_(type(v) == type(''))
        self.assert_('"width": 512' in v)
        self.assert_('"split_channel":' in v)
        self.assert_('"pixel_range": [-32768, 32767]' in v)

    def testListChildren (self):
        self.loginAsAuthor()
        img = self.getTestImage()
        did = img.getParent().getId()
        r = fakeRequest()
        v = views.listImages_json(r, did=did, server_id=1, conn=self.gateway, _internal=True)
        self.assert_(type(v) == type(''))
        self.assert_('"id": %d,' % img.getId() in v)
        self.assert_('"tiled: "' not in v)
        r.setQuery(tiled='1')
        v = views.listImages_json(r, did=did, server_id=1, conn=self.gateway, _internal=True)
        self.assert_(type(v) == type(''))
        self.assert_('"id": %d,' % img.getId() in v)
        self.assert_('"tiled": false' in v)

class UserProxyTest (WGTest):
    def test (self):
        self.loginAsAuthor()
        user = self.gateway.getUser()
        self.assertEqual(user.isAdmin(), False)
        int(user.getId())
        self.assertEqual(user.getName(), self.AUTHOR.name)
        self.assertEqual(user.getFirstName(), self.AUTHOR.firstname)

class ZZ_TDTest(WGTest):
    def setUp (self):
        super(ZZ_TDTest, self).setUp(skipTestDB=True)
        dbhelpers.cleanup()

    def runTest (self):
        pass



class WGTestUsersOnly(WGTest):
    def setUp(self):
        super(WGTestUsersOnly, self).setUp(skipTestDB=True)


class RepositoryApiBaseTest(WGTestUsersOnly):

    def setUp(self):
        super(RepositoryApiBaseTest, self).setUp()
        self.toDelete = []
        self.repoclass = "Repository"
        self.reponame = "omero.data"
        self.loginmethod = self.loginAsAdmin

    def tearDown(self):
        if self.toDelete:
            self.loginAsAdmin()
            if hasattr(self, 'gateway'):
                for repoclass, reponame, name in self.toDelete:
                    repository, description = views.get_repository(self.gateway, repoclass, reponame)
                    try:
                        repository.delete(name)
                    except Exception, ex:
                        print "\nCould not clean up %s in repository %s (Reason: %s)" % (name, repo, ex)
                self.toDelete = []
            else:
                print "\nNeed to cleanup files, but don't have gateway"
        super(RepositoryApiBaseTest, self).tearDown()

    def deleteLater(self, filename, repoclass=None, reponame=None):
        self.toDelete.append((repoclass or self.repoclass, reponame or self.reponame, filename))


class RepositoryApiTest(RepositoryApiBaseTest):
    """
    Admin can upload and read file in regular repository, and can create
    subdirectory, all using webgateway API
    """

    def testRepositories(self):
        self.loginmethod()
        r = fakeRequest()
        v = views.repositories(r, server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"name": "ManagedRepository"' in v)

    def testRepository(self):
        self.loginmethod()
        r = fakeRequest()
        v = views.repository(r, klass="Repository", name='omero.data', server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"name": "omero.data"' in v)
        self.assertTrue('"type": "OriginalFile"' in v)
        v = views.repository(r, klass="ManagedRepository", server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"name": "ManagedRepository"' in v)
        self.assertTrue('"type": "OriginalFile"' in v)

    def testRepositoryUploadDownload(self):
        NAME = 'test%d' % int(time.time() * 1000)
        self.deleteLater(NAME)

        self.loginmethod()
        view = views.repository_upload()
        viewargs = dict(klass=self.repoclass, name=self.reponame,
                        filepath=NAME, server_id=1,
                        conn=self.gateway, _internal=True)

        r = fakeRequest(QUERY_STRING='uploads')
        v = view.post(r, **viewargs)
        self.assertTrue('"bad": "false"' in v)
        self.assertTrue('"uploadId": "' in v)
        uploadid = simplejson.loads(v)['uploadId']

        r = fakeRequest(QUERY_STRING='uploadId=%s&partNumber=1' % uploadid)
        r._stream = StringIO('ABC')
        v = view.put(r, **viewargs)
        self.assertTrue('"bad": "false"' in v)

        r = fakeRequest(QUERY_STRING='uploadId=%s&partNumber=2' % uploadid)
        r._stream = StringIO('123')
        v = view.put(r, **viewargs)
        self.assertTrue('"bad": "false"' in v)

        r = fakeRequest(QUERY_STRING='uploadId=%s' % uploadid)
        v = view.get(r, **viewargs)
        self.assertTrue('"bad": "false"' in v)
        parts = simplejson.loads(v)['parts']
        self.assertEqual(2, len(parts))

        r = fakeRequest(QUERY_STRING='uploadId=%s' % uploadid)
        v = view.post(r, **viewargs)
        self.assertTrue('"bad": "false"' in v)

        r = fakeRequest()
        v = views.repository_download(r, **viewargs)
        self.assertEqual(200, v.status_code)
        self.assertEqual('ABC123', v.content)

        r = fakeRequest()
        viewargs['filepath'] = None
        v = views.repository_listfiles(r, **viewargs)
        self.assertTrue('"name": "%s"' % NAME in v)
        self.assertTrue('"size": 6' in v)
        self.assertTrue('"mtime": ' in v)
        
        mtime = None
        for metadata in simplejson.loads(v)['result']:
            if metadata['name'] == NAME:
                mtime = metadata['mtime']
                break
        self.assertTrue(mtime)
        self.assertAlmostEqual(time.time() * 1000, mtime, delta=5000)

    def testRepositoryMkdir(self):
        NAME = 'testdir%d' % int(time.time() * 1000)
        self.deleteLater(NAME)

        self.loginmethod()
        r = fakeRequest(REQUEST_METHOD='POST', body='')
        v = views.repository_makedir(r, dirpath=NAME, klass=self.repoclass,
                                     server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"bad": "false"' in v, msg='Returned: %s' % v)

        if self.repoclass == 'Repository':
            # TODO: the following file listing tests fail for Repository
            return

        r = fakeRequest()
        v = views.repository_list(r, klass=self.repoclass, name=self.reponame,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        self.assertIn('/' + NAME, result)

        r = fakeRequest()
        v = views.repository_listfiles(r, klass=self.repoclass, name=self.reponame,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        self.assertTrue(any(entry['name'] == NAME for entry in result))

class ManagedRepositoryApiTest(RepositoryApiTest):
    """
    Admin can upload and read file in ManagedRepository, and can create
    subdirectory, all using webgateway API
    """

    def setUp(self):
        super(ManagedRepositoryApiTest, self).setUp()
        self.repoclass = "ManagedRepository"
        self.reponame = None


class RepositoryApiAsAuthorTest(RepositoryApiTest):
    """
    Author can upload and read file in regular repository, and can create
    subdirectory, all using webgateway API
    """

    def setUp(self):
        super(RepositoryApiAsAuthorTest, self).setUp()
        self.loginmethod = self.loginAsAuthor


class ManagedRepositoryApiAsAuthorTest(RepositoryApiTest):
    """
    Author can upload and read file in ManagedRepository, and can create
    subdirectory, all using webgateway API
    """

    def setUp(self):
        super(ManagedRepositoryApiAsAuthorTest, self).setUp()
        self.repoclass = "ManagedRepository"
        self.reponame = None
        self.loginmethod = self.loginAsAuthor


class RepositoryApiPermissionsTest(RepositoryApiBaseTest):
    """
    If admin creates a file in regular repository, user can read it
    """

    def _getrepo(self):
        return views.get_repository(self.gateway, self.repoclass)

    def setUp(self, repoclass=None, reponame=''):
        super(RepositoryApiPermissionsTest, self).setUp()
        if repoclass is not None:
            self.repoclass = repoclass
        if reponame != '':
            self.reponame = reponame
        self.FILENAME = 'RepositoryApiPermissionsTest'
        self.loginAsAdmin()
        repository, repodesc = self._getrepo()
        targetfile = repository.file(self.FILENAME, 'rw')
        targetfile.truncate(0)
        targetfile.write('ABC123', 0, 6)

    def tearDown(self):
        self.loginAsAdmin()
        repository, repodesc = self._getrepo()
        repository.delete(self.FILENAME)
        super(RepositoryApiPermissionsTest, self).tearDown()

    def testFileAccessAsAdmin(self):
        self.loginAsAdmin()
        repository, repodesc = self._getrepo()
        targetfile = repository.file(self.FILENAME, 'r')
        filesize = targetfile.size()
        self.assertEqual(6, filesize)

    def testFileAccessAsUser(self):
        self.loginAsUser()
        repository, repodesc = self._getrepo()
        self.assertRaises(Exception, repository.file, self.FILENAME, 'r')
        #targetfile = repository.file(self.FILENAME, 'r')
        #filesize = targetfile.size()
        #self.assertEqual(6, filesize)

    def _test(self):
        print "\nUser"
        self.loginAsUser()
        group = self.gateway.getGroupFromContext()
        print "\nCurrent group: ", group.getName()
        print "Member of:"
        for g in self.gateway.getGroupsMemberOf():
            print "   ID:", g.getName(), " Name:", g.getId()

        print "\nAuthor"
        self.loginAsAuthor()
        group = self.gateway.getGroupFromContext()
        print "\nCurrent group: ", group.getName()
        print "Member of:"
        for g in self.gateway.getGroupsMemberOf():
            print "   ID:", g.getName(), " Name:", g.getId()

        print "\nAdmin"
        self.loginAsAdmin()
        group = self.gateway.getGroupFromContext()
        print "\nCurrent group: ", group.getName()
        print "Member of:"
        for g in self.gateway.getGroupsMemberOf():
            print "   ID:", g.getName(), " Name:", g.getId()


class ManagedRepositoryApiPermissionsTest(RepositoryApiPermissionsTest):
    """
    If admin creates a file in ManagedRepository, admin should be able to
    read the file, but user should get an exception
    """

    def setUp(self):
        super(ManagedRepositoryApiPermissionsTest, self).setUp(repoclass="ManagedRepository", reponame=None)

    def testFileAccess(self):
        self.loginAsUser()
        repository, repodesc = self._getrepo()
        self.assertRaises(Exception, repository.file, self.FILENAME, 'r')

    def testAdminFileAccess(self):
        self.loginAsAdmin()
        repository, repodesc = self._getrepo()
        targetfile = repository.file(self.FILENAME, 'r')
        filesize = targetfile.size()
        self.assertEqual(6, filesize)


class ManagedRepositoryApiCrossGroupTest(RepositoryApiPermissionsTest):
    """
    If admin and user create a file each in ManagedRepository, admin should
    see both but user should only see own file. Also, repository.list()
    returns list of file names relative to repository root
    """
    
    def setUp(self):
        super(ManagedRepositoryApiCrossGroupTest, self).setUp(repoclass="ManagedRepository", reponame=None)
        self.FILENAME_USER = 'RepositoryApiPermissionsTest_User'
        self.FILENAME_USER_DELTEST = 'RepositoryApiPermissionsTest_User2'
        self.loginAsUser()
        repository, repodesc = self._getrepo()
        targetfile = repository.file(self.FILENAME_USER, 'rw')
        targetfile.truncate(0)
        targetfile.write('DEF456', 0, 6)
        targetfile = repository.file(self.FILENAME_USER_DELTEST, 'rw')
        targetfile.truncate(0)
        targetfile.write('GHI789', 0, 6)

    def tearDown(self):
        self.loginAsUser()
        repository, repodesc = self._getrepo()
        repository.delete(self.FILENAME_USER)
        super(ManagedRepositoryApiCrossGroupTest, self).tearDown()

    def testListAsAdmin(self):
        self.loginAsAdmin()
        r = fakeRequest()
        v = views.repository_list(r, dirpath='', klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertIn(self.FILENAME, files)
        self.assertIn(self.FILENAME_USER, files)

    def testListAsUser(self):
        self.loginAsUser()
        r = fakeRequest()
        v = views.repository_list(r, dirpath='', klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertNotIn(self.FILENAME, files)
        self.assertIn(self.FILENAME_USER, files)

    def testListFilesAsAdmin(self):
        self.loginAsAdmin()
        r = fakeRequest()
        v = views.repository_listfiles(r, dirpath='', klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.get('name') for f in result]
        self.assertIn(self.FILENAME, files)
        self.assertIn(self.FILENAME_USER, files)

    def testListFilesAsUser(self):
        self.loginAsUser()
        r = fakeRequest()
        v = views.repository_listfiles(r, dirpath='', klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.get('name') for f in result]
        self.assertNotIn(self.FILENAME, files)
        self.assertIn(self.FILENAME_USER, files)

    def testSha(self):
        self.loginAsAdmin()
        r = fakeRequest()
        v = views.repository_sha(r, self.repoclass, filepath=self.FILENAME_USER,
                                 server_id=1, conn=self.gateway, _internal=True)
        self.assertEqual('{"sha": "25577cfc23d0e779241727f063b0648ad451360c"}', v)

    def testDelete(self):
        self.loginAsAdmin()

        r = fakeRequest()
        v = views.repository_list(r, dirpath='', klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertIn(self.FILENAME_USER_DELTEST, files)

        r = fakeRequest(REQUEST_METHOD='POST', body='')
        v = views.repository_delete(r, filepath=self.FILENAME_USER_DELTEST,
                                    klass=self.repoclass,
                                    server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"matched_ids": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['matched_ids']
        self.assertEqual(1, len(result))

        r = fakeRequest()
        v = views.repository_list(r, dirpath='', klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertNotIn(self.FILENAME_USER_DELTEST, files)
