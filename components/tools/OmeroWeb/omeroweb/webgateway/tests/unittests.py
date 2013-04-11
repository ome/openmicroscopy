#!/usr/bin/env python
# -*- coding: utf-8 -*-
# coding=utf-8

import unittest, time, os, datetime
import tempfile
import zlib
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

def fakeRequest (body=None, session_key=None, **kwargs):
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
        r.session = engine.SessionStore(session_key)
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
        # set up reponame to be retrieved dynamically later on, unless
        # it gets overridden in the meantime
        self._reponame = "*"
        self.loginmethod = self.loginAsAdmin

    def _setRepoName(self, val):
        self._reponame = val

    def _getRepoName(self):
        if self._reponame == "*":
            if not self.gateway:
                self.loginAsAdmin()
            self._reponame = os.path.split(self.gateway.getConfigService().getConfigValue("omero.data.dir"))[1]
        return self._reponame

    reponame = property(_getRepoName, _setRepoName)

    def _getrepo(self):
        return views.get_repository(self.gateway, self.repoclass)

    def _getDirectory(self, dirpath):
        if self.repoclass == "ManagedRepository":
            user = self.gateway.getUser()
            return os.path.join('%s_%s' % (user.getName(), user.getId()), dirpath)
        else:
            return dirpath


    def _delete(self, repository, filepath, timeout=30):
        handle = repository.deletePaths([filepath], True, True)
        try:
            self.gateway._waitOnCmd(handle, loops=timeout * 2)  # loops are 500ms
        finally:
            handle.close()

    def tearDown(self):
        if self.toDelete:
            if hasattr(self, 'gateway'):
                lastloginmethod = None
                for loginmethod, repoclass, reponame, name, expect in self.toDelete:
                    if loginmethod != lastloginmethod:
                        loginmethod()
                        lastloginmethod = loginmethod
                    repository, description = views.get_repository(self.gateway, repoclass, reponame)
                    if repository.fileExists(name):
                        if not expect:
                            print "Unexpectedly found %s in repository %s" % (name, repository)
                        try:
                            self._delete(repository, name)
                            if repository.fileExists(name):
                                print "Could not delete %s in repository %s" % (name, repository)
                        except Exception, ex:
                            print "\nCould not clean up %s in repository %s (Reason: %s)" % (name, repository, ex)
                self.toDelete = []
            else:
                print "\nNeed to cleanup files, but don't have gateway"
        super(RepositoryApiBaseTest, self).tearDown()

    def deleteLater(self, filename, repoclass=None, reponame=None, expect=True):
        self.toDelete.append((self.loginmethod, repoclass or self.repoclass, reponame or self.reponame, filename, expect))


class AsyncDeleteTest(RepositoryApiBaseTest):

    def setUp(self):
        super(AsyncDeleteTest, self).setUp()
        self.repoclass = "ManagedRepository"
        self.reponame = None
        self.loginmethod = self.loginAsAuthor

    def _createOriginalFiles(self, directory, count, subdirs=0):
        repository, description = self._getrepo()
        userdir = self._getDirectory(directory)
        repository.makeDir(userdir, True)
        for i in range(count):
            name = os.path.join(userdir, 'file%s.txt' % i)
            targetfile = repository.file(name, 'rw')
            targetfile.truncate(0)
            targetfile.write('ABC123', 0, 6)
            targetfile.close()
            self.deleteLater(name, expect=False)
        for i in range(subdirs):
            self._createOriginalFiles(os.path.join(directory, 'subdir%d' % i), count)
        self.deleteLater(userdir, expect=False)
        l = repository.listFiles(userdir)
        return [unwrap(x.id) for x in l]

    def _testAbc(self):
        self.loginmethod()
        name = 'delete_test_%s' % time.time()
        ids = self._createOriginalFiles(name, 3, 2)

        repository, description = self._getrepo()
        ctx = self.gateway.SERVICE_OPTS.copy()

        explore = [self._getDirectory(name)]
        files = []
        directories = []

        while explore or files or directories:
            result = views._gather_files_for_deletion(repository, ctx, 5, explore, files, directories)
            print '*' * 50
            print result
            print explore
            print files
            print directories

    def testDeleteCallback(self, count=10):
        self.loginmethod()
        name = 'delete_test_%s' % time.time()
        ids = self._createOriginalFiles(name, count)

        r = fakeRequest(REQUEST_METHOD='POST', body='', QUERY_STRING='async=false')
        starttime = datetime.datetime.now()
        response = views.repository_delete(r, klass=self.repoclass,
            name=self.reponame, filepath=self._getDirectory(name), conn=self.gateway,
            server_id=1, _internal=True)
        endtime = datetime.datetime.now()
        response = simplejson.loads(response)
        self.assertEqual(False, response['async'])
        #print "\nDeleted %s OriginalFile objects in %s" % (
        #        count, endtime - starttime
        #     )

    def testAsyncDeleteCallback(self, count=10, checkinterval=1, batchsize=100, subdirs=0):
        self.loginmethod()
        name = 'delete_test_%s' % time.time()
        ids = self._createOriginalFiles(name, count, subdirs)

        r = fakeRequest(REQUEST_METHOD='POST', body='', QUERY_STRING='async=true&batchsize=%s&progress=true' % batchsize)
        response = views.repository_delete(r, klass=self.repoclass,
            name=self.reponame, filepath=self._getDirectory(name), conn=self.gateway,
            server_id=1, _internal=True)
        session_key = r.session.session_key
        response = simplejson.loads(response)
        #self.assertEqual(count + 1, response['total'], msg=response) # files plus containing dir
        self.assertEqual(True, response['async'])
        self.assertTrue(response.has_key('handle'))
        strhandle = response['handle']

        starttime = datetime.datetime.now()
        while True:
            r = fakeRequest(session_key=session_key, QUERY_STRING='handle=' + strhandle)
            response = views.repository_delete_status(r, klass=self.repoclass,
                name=self.reponame, conn=self.gateway, server_id=1, _internal=True)
            response = simplejson.loads(response)
            #print response
            self.assertFalse(response.has_key('error'), msg=response)
            self.assertTrue(response.has_key('complete'))
            if response['complete']:
                break
            time.sleep(checkinterval)
        endtime = datetime.datetime.now()
        #print "\nDeleted %s OriginalFiles in %s (batch size %s, check interval %ss)" % (
        #        count + count * subdirs + subdirs, endtime - starttime, batchsize, checkinterval
        #     )
        self.assertTrue(response['complete'])

    def testLargeDeleteCallback(self):
        #self.testAsyncDeleteCallback(count=100)
        #self.testAsyncDeleteCallback(count=200)
        self.testAsyncDeleteCallback(count=30, batchsize=20, subdirs=2)
        #self.testAsyncDeleteCallback(count=500)
        #self.testAsyncDeleteCallback(count=1000)
        pass

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
        v = views.repository(r, klass=self.repoclass, name=self.reponame, server_id=1, conn=self.gateway, _internal=True)
        check = '"name": "%s"' % self.reponame if self.reponame else self.repoclass
        self.assertTrue(check in v, "Did not find %s in %s" % (check, v))
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
        self.assertTrue('"bad": "false"' in v, msg=v)

        r = fakeRequest()
        v = views.repository_download(r, **viewargs)
        self.assertEqual(200, v.status_code)
        self.assertEqual('ABC123', v.content)

        r = fakeRequest()
        viewargs['filepath'] = None
        v = views.repository_listfiles(r, **viewargs)
        self.assertTrue('"name": "%s"' % NAME in v, msg=v)
        self.assertTrue('"size": 6' in v)
        self.assertTrue('"mtime": ' in v)

        mtime = None
        for metadata in simplejson.loads(v)['result']:
            if metadata['name'] == NAME:
                mtime = metadata['mtime']
                break
        self.assertTrue(mtime, msg="mtime not set in '%s'" % metadata)
        self.assertAlmostEqual(time.time() * 1000, mtime, delta=5000)

    def testRepositoryCompressedUpload(self):
        NAME = 'compressedtest%d' % int(time.time() * 1000)
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

        r = fakeRequest(QUERY_STRING='uploadId=%s&partNumber=1&compressed=gzip'
                        % uploadid)
        r._stream = StringIO(zlib.compress('ABC'))
        v = view.put(r, **viewargs)
        self.assertTrue('"bad": "false"' in v, msg=v)

        r = fakeRequest(QUERY_STRING='uploadId=%s&partNumber=2&compressed=gzip'
                        % uploadid)
        r._stream = StringIO(zlib.compress('123'))
        v = view.put(r, **viewargs)
        self.assertTrue('"bad": "false"' in v)

        r = fakeRequest(QUERY_STRING='uploadId=%s' % uploadid)
        v = view.get(r, **viewargs)
        self.assertTrue('"bad": "false"' in v)
        parts = simplejson.loads(v)['parts']
        self.assertEqual(2, len(parts))

        r = fakeRequest(QUERY_STRING='uploadId=%s' % uploadid)
        v = view.post(r, **viewargs)
        self.assertTrue('"bad": "false"' in v, msg=v)

        r = fakeRequest()
        v = views.repository_download(r, **viewargs)
        self.assertEqual(200, v.status_code)
        self.assertEqual('ABC123', v.content)

        r = fakeRequest()
        viewargs['filepath'] = None
        v = views.repository_listfiles(r, **viewargs)
        self.assertTrue('"name": "%s"' % NAME in v, msg=v)
        self.assertTrue('"size": 6' in v)
        self.assertTrue('"mtime": ' in v)

        mtime = None
        for metadata in simplejson.loads(v)['result']:
            if metadata['name'] == NAME:
                mtime = metadata['mtime']
                break
        self.assertTrue(mtime, msg="mtime not set in '%s'" % metadata)
        self.assertAlmostEqual(time.time() * 1000, mtime, delta=5000)

    def testRepositoryMkdir(self):
        NAME = 'testdir%d' % int(time.time() * 1000)
        self.deleteLater(NAME)

        self.loginmethod()
        dirpath = self._getDirectory(NAME)

        r = fakeRequest(REQUEST_METHOD='POST', body='')
        v = views.repository_makedir(r, dirpath=dirpath, klass=self.repoclass,
                                     server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"bad": "false"' in v, msg='Returned: %s' % v)

        if self.repoclass == 'Repository':
            # TODO: the following file listing tests fail for Repository
            return

        r = fakeRequest()
        v = views.repository_list(r, klass=self.repoclass, name=self.reponame,
                                  filepath=self._getDirectory(''),
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        self.assertTrue(dirpath in result, msg="/%s not in '%s'" % (NAME, result))

        r = fakeRequest()
        v = views.repository_listfiles(r, klass=self.repoclass, name=self.reponame,
                                  filepath=self._getDirectory(''),
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        self.assertTrue(any(entry['name'] == NAME for entry in result), msg=result)

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
        # pre-fetch reponame, otherwise a security exception
        # occurs when reponame is fetched with author login
        reponame = self.reponame
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

    def setUp(self, repoclass=None, reponame=''):
        super(RepositoryApiPermissionsTest, self).setUp()
        if repoclass is not None:
            self.repoclass = repoclass
        if reponame != '':
            self.reponame = reponame
        self.loginAsAdmin()
        self.FILENAME = self._getDirectory('RepositoryApiPermissionsTest')
        repository, repodesc = self._getrepo()
        if self._getDirectory('') != '':  # don't try to recreate root
            repository.makeDir(self._getDirectory(''), True)
        targetfile = repository.file(self.FILENAME, 'rw')
        targetfile.truncate(0)
        targetfile.write('ABC123', 0, 6)
        targetfile.close()

    def tearDown(self):
        self.loginAsAdmin()
        repository, repodesc = self._getrepo()
        repository.deletePaths([self.FILENAME], True, True)
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
        self.loginAsAdmin()
        self.admindir = self._getDirectory('')
        self.loginAsUser()
        self.FILENAME_USER = self._getDirectory('ManagedRepositoryApiCrossGroupTest_User')
        self.FILENAME_USER_DELTEST = self._getDirectory('ManagedRepositoryApiCrossGroupTest_User2')
        self.dir = self._getDirectory('')
        repository, repodesc = self._getrepo()
        repository.makeDir(self.dir, True)
        targetfile = repository.file(self.FILENAME_USER, 'rw')
        targetfile.truncate(0)
        targetfile.write('DEF456', 0, 6)
        targetfile.close()
        targetfile = repository.file(self.FILENAME_USER_DELTEST, 'rw')
        targetfile.truncate(0)
        targetfile.write('GHI789', 0, 6)
        targetfile.close()

    def tearDown(self):
        timeout = 1
        self.loginAsUser()
        repository, repodesc = self._getrepo()
        repository.deletePaths([self.FILENAME_USER], True, True)
        if repository.fileExists(self.FILENAME_USER_DELTEST):
            self._delete(repository, self.FILENAME_USER_DELTEST)
        super(ManagedRepositoryApiCrossGroupTest, self).tearDown()

    def testListAsAdmin(self):
        self.loginAsAdmin()
        r = fakeRequest()
        v = views.repository_list(r, filepath=self.dir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertTrue(self.FILENAME_USER in files, msg="%s not in '%s'" %
                        (self.FILENAME_USER, files))

        r = fakeRequest()
        v = views.repository_list(r, filepath=self.admindir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertTrue(self.FILENAME in files, msg="%s not in '%s'" %
                        (self.FILENAME, files))

    def testListAsUser(self):
        self.loginAsUser()
        r = fakeRequest()
        v = views.repository_list(r, filepath=self.dir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertTrue(self.FILENAME_USER in files, msg="%s not in '%s'" %
                        (self.FILENAME_USER, files))

        r = fakeRequest()
        v = views.repository_list(r, filepath=self.admindir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertFalse(self.FILENAME in files, msg="%s in '%s'" %
                        (self.FILENAME, files))

    def testListFilesAsAdmin(self):
        self.loginAsAdmin()
        r = fakeRequest()
        v = views.repository_listfiles(r, filepath=self.dir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.get('name') for f in result]
        self.assertTrue(os.path.split(self.FILENAME_USER)[-1] in files, msg="%s not in '%s'" %
                        (os.path.split(self.FILENAME_USER)[-1], files))

        r = fakeRequest()
        v = views.repository_listfiles(r, filepath=self.admindir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.get('name') for f in result]
        self.assertTrue(os.path.split(self.FILENAME)[-1] in files, msg="%s not in '%s'" %
                        (os.path.split(self.FILENAME)[-1], files))

    def testListFilesAsUser(self):
        self.loginAsUser()
        r = fakeRequest()
        v = views.repository_listfiles(r, filepath=self.dir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.get('name') for f in result]
        self.assertTrue(os.path.split(self.FILENAME_USER)[-1] in files, msg="%s not in '%s'" %
                        (os.path.split(self.FILENAME_USER)[-1], files))

        r = fakeRequest()
        v = views.repository_listfiles(r, filepath=self.admindir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.get('name') for f in result]
        self.assertFalse(os.path.split(self.FILENAME)[-1] in files, msg="%s in '%s'" %
                        (os.path.split(self.FILENAME)[-1], files))



    def testSha(self):
        self.loginAsAdmin()
        r = fakeRequest()
        v = views.repository_sha(r, self.repoclass, filepath=self.FILENAME_USER,
                                 server_id=1, conn=self.gateway, _internal=True)
        self.assertEqual('{"sha": "25577cfc23d0e779241727f063b0648ad451360c"}', v)

    def testDelete(self):
        self.loginAsUser()

        r = fakeRequest()
        v = views.repository_list(r, filepath=self.dir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertTrue(self.FILENAME_USER_DELTEST in files, msg="%s not in '%s'" %
                        (self.FILENAME_USER_DELTEST, files))

        r = fakeRequest(REQUEST_METHOD='POST', body='')
        v = views.repository_delete(r, filepath=self.FILENAME_USER_DELTEST,
                                    klass=self.repoclass,
                                    server_id=1, conn=self.gateway, _internal=True)
        #self.assertTrue('"total": 1' in v, msg='Returned: %s' % v)


        r = fakeRequest()
        v = views.repository_list(r, filepath=self.dir, klass=self.repoclass,
                                  server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('"result": [' in v, msg='Returned: %s' % v)
        result = simplejson.loads(v)['result']
        files = [f.strip('/') for f in result]
        self.assertFalse(self.FILENAME_USER_DELTEST in files, msg="%s in '%s'" %
                        (self.FILENAME_USER_DELTEST, files))

    def testDownload(self):
        self.loginAsAdmin()
        r = fakeRequest()
        v = views.repository_download(r, filepath=self.FILENAME_USER,
                                    klass=self.repoclass,
                                    server_id=1, conn=self.gateway, _internal=True)
        self.assertEqual(200, v.status_code)
        self.assertEqual('DEF456', v.content)


class AnnotationTest(RepositoryApiBaseTest):

    TEST_NS = 'omero.webgateway.annotate_test'

    def setUp(self):
        super(AnnotationTest, self).setUp()
        self.loginAsAdmin()
        repository, repodesc = self._getrepo()

        # clean up from previous failed runs
        self.obj = self.gateway.getObject('OriginalFile', attributes=dict(name='annotationTest', path='/'))
        if self.obj:
            self.obj.removeAnnotations(self.TEST_NS)
        else:
            targetfile = repository.file('annotationTest', 'rw')
            targetfile.truncate(0)
            targetfile.write('DEF456', 0, 6)
            targetfile.close()
            self.obj = self.gateway.getObject('OriginalFile', attributes=dict(name='annotationTest', path='/'))

    def tearDown(self):
        self.obj.removeAnnotations(self.TEST_NS)
        repository, repodesc = self._getrepo()
        repository.deletePaths(['annotationTest'], True, True)
        super(AnnotationTest, self).tearDown()

    def testAnnotation(self):
        query = dict(QUERY_STRING='ns=%s' % self.TEST_NS)

        r = fakeRequest(**query)
        v = views.annotate(r, 'OriginalFile', self.obj.id,
                           server_id=1, conn=self.gateway, _internal=True)
        self.assertEqual('[]', v)

        r = fakeRequest(REQUEST_METHOD='POST', body='{"name": "test"}', **query)
        v = views.annotate(r, 'OriginalFile', self.obj.id,
                           server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('ok' in v)

        r = fakeRequest(**query)
        v = views.annotate(r, 'OriginalFile', self.obj.id,
                           server_id=1, conn=self.gateway, _internal=True)
        result = simplejson.loads(v)
        self.assertEqual(1, len(result))
        self.assertTrue(result[0].has_key('type'))
        self.assertTrue(result[0].has_key('id'))
        self.assertTrue(result[0].has_key('value'))
        self.assertEqual('{"name": "test"}', result[0]['value'])

        r = fakeRequest(REQUEST_METHOD='POST', body='{"testkey": "testvalue"}', **query)
        v = views.annotate(r, 'OriginalFile', self.obj.id,
                           server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('ok' in v)

        r = fakeRequest(**query)
        v = views.annotate(r, 'OriginalFile', self.obj.id,
                           server_id=1, conn=self.gateway, _internal=True)
        result = simplejson.loads(v)
        self.assertEqual(1, len(result), msg=result)
        self.assertTrue(result[0].has_key('type'))
        self.assertTrue(result[0].has_key('id'))
        self.assertTrue(result[0].has_key('value'))
        self.assertEqual('{"testkey": "testvalue"}', result[0]['value'])

        r = fakeRequest(REQUEST_METHOD='DELETE', **query)
        v = views.annotate(r, 'OriginalFile', self.obj.id,
                           server_id=1, conn=self.gateway, _internal=True)
        self.assertTrue('ok' in v)

        r = fakeRequest(**query)
        v = views.annotate(r, 'OriginalFile', self.obj.id,
                           server_id=1, conn=self.gateway, _internal=True)
        self.assertEqual('[]', v)
