#!/usr/bin/env python
# -*- coding: utf-8 -*-
# coding=utf-8

import time
import os
import pytest

from webgateway.webgateway_cache import FileCache, WebGatewayCache
from webgateway.webgateway_cache import WebGatewayTempFile
import omero.gateway


class TestHelperObjects(object):
    def testColorHolder(self):
        ColorHolder = omero.gateway.ColorHolder
        c1 = ColorHolder()
        assert c1._color == {'red': 0, 'green': 0, 'blue': 0, 'alpha': 255}
        c1 = ColorHolder('blue')
        assert c1.getHtml() == '0000FF'
        assert c1.getCss() == 'rgba(0,0,255,1.000)'
        assert c1.getRGB() == (0, 0, 255)
        c1.setRed(0xF0)
        assert c1.getCss() == 'rgba(240,0,255,1.000)'
        c1.setGreen(0x0F)
        assert c1.getCss() == 'rgba(240,15,255,1.000)'
        c1.setBlue(0)
        assert c1.getCss() == 'rgba(240,15,0,1.000)'
        c1.setAlpha(0x7F)
        assert c1.getCss() == 'rgba(240,15,0,0.498)'
        c1 = ColorHolder.fromRGBA(50, 100, 200, 300)
        assert c1.getCss() == 'rgba(50,100,200,1.000)'

    def testOmeroType(self):
        omero_type = omero.gateway.omero_type
        assert isinstance(omero_type('rstring'), omero.RString)
        assert isinstance(omero_type(u'rstring'), omero.RString)
        assert isinstance(omero_type(1), omero.RInt)
        assert isinstance(omero_type(1L), omero.RLong)
        assert isinstance(omero_type(False), omero.RBool)
        assert isinstance(omero_type(True), omero.RBool)
        assert not isinstance(omero_type((1, 2, 'a')), omero.RType)

    def testSplitHTMLColor(self):
        splitHTMLColor = omero.gateway.splitHTMLColor
        assert splitHTMLColor('abc') == [0xAA, 0xBB, 0xCC, 0xFF]
        assert splitHTMLColor('abcd') == [0xAA, 0xBB, 0xCC, 0xDD]
        assert splitHTMLColor('abbccd') == [0xAB, 0xBC, 0xCD, 0xFF]
        assert splitHTMLColor('abbccdde') == [0xAB, 0xBC, 0xCD, 0xDE]
        assert splitHTMLColor('#$%&%') is None


def _testCacheFSBlockSize(cache):
    cache.wipe()
    c1 = cache._du()
    cache.set('test/1', 'a')
    c2 = cache._du()
    cache.wipe()
    return c1, c2-c1


class TestFileCache(object):
    @pytest.fixture(autouse=True)
    def setUp(self, request):

        def fin():
            os.system('rm -fr test_cache')
        request.addfinalizer(fin)
        self.cache = FileCache('test_cache')

    def testTimeouts(self):
        assert (self.cache.get('date/test/1') is None,
                'Key already exists in cache')
        self.cache.set('date/test/1', '1', timeout=3)
        assert self.cache.get('date/test/1') == '1', 'Key not properly cached'
        time.sleep(4)
        assert self.cache.get('date/test/1') is None, 'Timeout failed'
        # if _default_timeout is 0, timeouts are simply not checked
        self.cache.wipe()
        self.cache._default_timeout = 0
        assert (self.cache.get('date/test/1') is None,
                'Key already exists in cache')
        self.cache.set('date/test/1', '1', timeout=3)
        assert (self.cache.get('date/test/1') == '1',
                'Key not properly cached')
        time.sleep(4)
        assert self.cache.has_key('date/test/1')  # noqa
        assert (self.cache.get('date/test/1') == '1',
                'Key got timedout and should not')

    def testMaxSize(self):
        empty_size, cache_block = _testCacheFSBlockSize(self.cache)
        self.cache._max_size = empty_size + 4*cache_block + 1
        # There is an overhead (8 bytes in my system) for timestamp per file,
        # and the limit is only enforced after we cross over it
        for i in range(6):
            self.cache.set('date/test/%d' % i, 'abcdefgh'*127*cache_block)
        for i in range(4):
            assert (self.cache.get('date/test/%d' % i) ==
                    'abcdefgh' * 127 * cache_block,
                    'Key %d not properly cached' % i)
        assert self.cache.get('date/test/5') is None, 'Size limit failed'
        self.cache._max_size = 0
        self.cache.wipe()
        for i in range(6):
            self.cache.set('date/test/%d' % i, 'abcdefgh'*127*cache_block)
        for i in range(6):
            assert (self.cache.get('date/test/%d' % i) ==
                    'abcdefgh' * 127 * cache_block,
                    'Key %d not properly cached' % i)

    def testMaxEntries(self):
        self.cache._max_entries = 2
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        assert self.cache.get('date/test/1') == '1', 'Key not properly cached'
        assert self.cache.get('date/test/2') == '2', 'Key not properly cached'
        assert (self.cache.get('date/test/3') is None,
                'File number limit failed')
        self.cache.wipe()
        self.cache._max_entries = 0
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        assert self.cache.get('date/test/1') == '1', 'Key not properly cached'
        assert self.cache.get('date/test/2') == '2', 'Key not properly cached'
        assert self.cache.get('date/test/3') == '3', 'Key not properly cached'

    def testPurge(self):
        self.cache._max_entries = 2
        self.cache._default_timeout = 3
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        assert self.cache.get('date/test/1') == '1', 'Key not properly cached'
        assert self.cache.get('date/test/2') == '2', 'Key not properly cached'
        assert (self.cache.get('date/test/3') is None,
                'File number limit failed')
        time.sleep(4)
        self.cache.set('date/test/3', '3')
        assert self.cache.get('date/test/3') == '3', 'Purge not working'

    def testOther(self):
        # set should only accept strings as values
        pytest.raises(ValueError, self.cache.set, 'date/test/1', 123)
        # keys can't have .. or start with /
        pytest.raises(ValueError, self.cache.set, '/date/test/1', '1')
        pytest.raises(ValueError, self.cache.set, 'date/test/../1', '1')
        # get some test data in
        self.cache.set('date/test/1', '1')
        self.cache.set('date/test/2', '2')
        self.cache.set('date/test/3', '3')
        assert self.cache.get('date/test/1') == '1', 'Key not properly cached'
        assert self.cache.get('date/test/2') == '2', 'Key not properly cached'
        assert self.cache.get('date/test/3') == '3', 'Key not properly cached'
        # check has_key
        assert self.cache.has_key('date/test/1')  # noqa
        assert not self.cache.has_key('date/test/bogus')  # noqa
        # assert wipe() nukes the whole thing
        assert self.cache._num_entries == 3
        self.cache.wipe()
        assert self.cache._num_entries == 0


class TestWebGatewayCacheTempFile(object):
    @pytest.fixture(autouse=True)
    def setUp(self, request):

        def fin():
            os.system('rm -fr test_cache')
        request.addfinalizer(fin)
        self.tmpfile = WebGatewayTempFile(tdir='test_cache')

    def testFilenameSize(self):
        """
        Make sure slashes, dashes, underscores and other chars don't mess
        things up.
        Also check for filename size limits.
        """
        fname = '1/2_3!"\'#$%&()=@€£‰¶÷[]≠§±+*~^\,.;:'

        try:
            fpath, rpath, fobj = self.tmpfile.new(fname, key='specialchars')
        except:
            raise
            pytest.fail('WebGatewayTempFile.new not handling special'
                        ' characters properly')
        # ext2/3/4 limit is 255 bytes, most others are equal to or larger
        fname = "a"*384
        try:
            fpath, rpath, fobj = self.tmpfile.new(fname, key='longname')
            fobj.close()
            # is it keeping extensions properly?
            fpath, rpath, fobj = self.tmpfile.new(
                "1" + fname + '.tif', key='longname')
            fobj.close()
            assert fpath[-5:] == 'a.tif'
            fpath, rpath, fobj = self.tmpfile.new(
                "2" + fname + '.ome.tiff', key='longname')
            fobj.close()
            assert fpath[-10:] == 'a.ome.tiff'
            fpath, rpath, fobj = self.tmpfile.new(
                "3" + fname + 'ome.tiff', key='longname')
            fobj.close()
            assert fpath[-6:] == 'a.tiff'
            fpath, rpath, fobj = self.tmpfile.new(
                "4" + fname + 'somethingverylong.zip', key='longname')
            fobj.close()
            assert fpath[-5:] == 'a.zip'
            fpath, rpath, fobj = self.tmpfile.new(
                "5" + fname + '.tif.somethingverylong', key='longname')
            fobj.close()
            assert fpath[-5:] == 'aaaaa'
        except:
            pytest.fail('WebGatewayTempFile.new not handling long file names'
                        ' properly')


class TestWebGatewayCache(object):
    @pytest.fixture(autouse=True)
    def setUp(self, request):

        def fin():
            os.system('rm -fr test_cache')
        request.addfinalizer(fin)
        self.wcache = WebGatewayCache(backend=FileCache, basedir='test_cache')

        class r:

            def __init__(self):
                self.REQUEST = {'c': '1|292:1631$FF0000,2|409:5015$0000FF',
                                'm': 'c', 'q': '0.9'}

            def new(self, q):
                rv = self.__class__()
                rv.REQUEST.update(q)
                return rv
        self.request = r()

    def testCacheSettings(self):
        uid = 123
        # empty_size, cache_block =
        # _testCacheFSBlockSize(self.wcache._thumb_cache)
        self.wcache._updateCacheSettings(self.wcache._thumb_cache, timeout=2,
                                         max_entries=5, max_size=0)
        cachestr = 'abcdefgh' * 127
        self.wcache._thumb_cache.wipe()
        for i in range(6):
            self.wcache.setThumb(self.request, 'test', uid, i, cachestr)
        max_size = self.wcache._thumb_cache._du()
        self.wcache._updateCacheSettings(self.wcache._thumb_cache, timeout=2,
                                         max_entries=5, max_size=max_size)
        self.wcache._thumb_cache.wipe()
        for i in range(6):
            self.wcache.setThumb(self.request, 'test', uid, i, cachestr)
        for i in range(4):
            assert (self.wcache.getThumb(self.request, 'test', uid, i) ==
                    cachestr, 'Key %d not properly cached' % i)
        assert (self.wcache.getThumb(self.request, 'test', uid, 5) is None,
                'Size limit failed')
        for i in range(10):
            self.wcache.setThumb(self.request, 'test', uid, i, 'abcdefgh')
        for i in range(5):
            assert (self.wcache.getThumb(self.request, 'test', uid, i) ==
                    'abcdefgh', 'Key %d not properly cached' % i)
        assert (self.wcache.getThumb(self.request, 'test', uid, 5) is None,
                'Entries limit failed')
        time.sleep(2)
        assert (self.wcache.getThumb(self.request, 'test', uid, 0) is None,
                'Time limit failed')

    def testThumbCache(self):
        uid = 123
        assert self.wcache.getThumb(self.request, 'test', uid, 1) is None
        self.wcache.setThumb(self.request, 'test', uid, 1, 'thumbdata')
        assert (self.wcache.getThumb(self.request, 'test', uid, 1) ==
                'thumbdata', 'Thumb not properly cached (%s)' %
                self.wcache.getThumb(self.request, 'test', uid, 1))
        self.wcache.clearThumb(self.request, 'test', uid, 1)
        assert self.wcache.getThumb(self.request, 'test', uid, 1) is None
        # Make sure clear() nukes this
        self.wcache.setThumb(self.request, 'test', uid, 1, 'thumbdata')
        assert (self.wcache.getThumb(self.request, 'test', uid, 1) ==
                'thumbdata', 'Thumb not properly cached')
        assert self.wcache._thumb_cache._num_entries != 0
        self.wcache.clear()
        assert self.wcache._thumb_cache._num_entries == 0

    def testImageCache(self):
        uid = 123
        # Also add a thumb, a split channel and a projection, as it should get
        # deleted with image
        preq = self.request.new({'p': 'intmax'})
        assert self.wcache.getThumb(self.request, 'test', uid, 1) is None
        self.wcache.setThumb(self.request, 'test', uid, 1, 'thumbdata')
        assert (self.wcache.getThumb(self.request, 'test', uid, 1) ==
                'thumbdata')
        img = omero.gateway.ImageWrapper(None, omero.model.ImageI(1, False))
        assert self.wcache.getImage(self.request, 'test', img, 2, 3) is None
        self.wcache.setImage(self.request, 'test', img, 2, 3, 'imagedata')
        assert (self.wcache.getImage(self.request, 'test', img, 2, 3) ==
                'imagedata')
        assert self.wcache.getImage(preq, 'test', img, 2, 3) is None
        self.wcache.setImage(preq, 'test', img, 2, 3, 'imagedata')
        assert self.wcache.getImage(preq, 'test', img, 2, 3) == 'imagedata'
        assert (self.wcache.getSplitChannelImage(self.request, 'test', img, 2,
                                                 3) is None)
        self.wcache.setSplitChannelImage(self.request, 'test', img, 2, 3,
                                         'imagedata')
        assert (self.wcache.getSplitChannelImage(self.request, 'test', img, 2,
                                                 3) == 'imagedata')
        self.wcache.clearImage(self.request, 'test', uid, img)
        assert self.wcache.getImage(self.request, 'test', img, 2, 3) is None
        assert (self.wcache.getSplitChannelImage(self.request, 'test', img, 2,
                                                 3) is None)
        assert self.wcache.getImage(preq, 'test', img, 2, 3) is None
        assert self.wcache.getThumb(self.request, 'test', uid, 1) is None
        # The exact same behaviour, using invalidateObject
        self.wcache.setThumb(self.request, 'test', uid, 1, 'thumbdata')
        assert (self.wcache.getThumb(self.request, 'test', uid, 1) ==
                'thumbdata')
        self.wcache.setImage(self.request, 'test', img, 2, 3, 'imagedata')
        assert (self.wcache.getImage(self.request, 'test', img, 2, 3) ==
                'imagedata')
        assert self.wcache.getImage(preq, 'test', img, 2, 3) is None
        self.wcache.setImage(preq, 'test', img, 2, 3, 'imagedata')
        assert self.wcache.getImage(preq, 'test', img, 2, 3) == 'imagedata'
        assert (self.wcache.getSplitChannelImage(self.request, 'test', img, 2,
                                                 3) is None)
        self.wcache.setSplitChannelImage(self.request, 'test', img, 2, 3,
                                         'imagedata')
        assert (self.wcache.getSplitChannelImage(self.request, 'test', img, 2,
                                                 3) == 'imagedata')
        self.wcache.invalidateObject('test', uid, img)
        assert self.wcache.getImage(self.request, 'test', img, 2, 3) is None
        assert (self.wcache.getSplitChannelImage(self.request, 'test', img, 2,
                                                 3) is None)
        assert self.wcache.getImage(preq, 'test', img, 2, 3) is None
        assert self.wcache.getThumb(self.request, 'test', uid, 1) is None
        # Make sure clear() nukes this
        assert self.wcache.getImage(self.request, 'test', img, 2, 3) is None
        self.wcache.setImage(self.request, 'test', img, 2, 3, 'imagedata')
        assert (self.wcache.getImage(self.request, 'test', img, 2, 3) ==
                'imagedata')
        assert self.wcache._img_cache._num_entries != 0
        self.wcache.clear()
        assert self.wcache._img_cache._num_entries == 0

    def testLocks(self):
        wcache2 = WebGatewayCache(backend=FileCache,
                                  basedir=self.wcache._basedir)
        # wcache2 will hold the lock
        assert wcache2.tryLock()
        assert not self.wcache.tryLock()
        assert wcache2.tryLock()
        del wcache2
        # The lock should have been removed
        assert self.wcache.tryLock()

    def testJsonCache(self):
        uid = 123
        ds = omero.gateway.DatasetWrapper(None, omero.model.DatasetI(1,
                                                                     False))
        assert (self.wcache.getDatasetContents(self.request, 'test', ds) is
                None)
        self.wcache.setDatasetContents(self.request, 'test', ds,
                                       'datasetdata')
        assert (self.wcache.getDatasetContents(self.request, 'test', ds) ==
                'datasetdata')
        self.wcache.clearDatasetContents(self.request, 'test', ds)
        assert (self.wcache.getDatasetContents(self.request, 'test', ds) is
                None)
        # The exact same behaviour, using invalidateObject
        assert (self.wcache.getDatasetContents(self.request, 'test', ds) is
                None)
        self.wcache.setDatasetContents(self.request, 'test', ds,
                                       'datasetdata')
        assert (self.wcache.getDatasetContents(self.request, 'test', ds) ==
                'datasetdata')
        self.wcache.invalidateObject('test', uid, ds)
        assert (self.wcache.getDatasetContents(self.request, 'test', ds) is
                None)
        # Make sure clear() nukes this
        assert (self.wcache.getDatasetContents(self.request, 'test', ds) is
                None)
        self.wcache.setDatasetContents(self.request, 'test', ds,
                                       'datasetdata')
        assert (self.wcache.getDatasetContents(self.request, 'test', ds) ==
                'datasetdata')
        assert self.wcache._json_cache._num_entries != 0
        self.wcache.clear()
        assert self.wcache._json_cache._num_entries == 0
