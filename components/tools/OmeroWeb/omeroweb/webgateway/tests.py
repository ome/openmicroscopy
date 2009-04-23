#
# webgateway/tests - unit tests for webgateway
# 
# Copyright (c) 2008, 2009 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>

from testdb_create import ROOT, GUEST, AUTHOR, AUTHOR_NAME, TESTIMG_NS
from testdb_create import getTestImage, getTinyTestImage, getBadTestImage, getTestDataset, getTestProject, getTestImage2

import unittest, time, os, datetime
from models import StoredConnection
from webgateway_cache import FileCache, WebGatewayCache
import omero
import Ice

from webgateway import views

import Image
from cStringIO import StringIO
import tempfile
from django.utils import simplejson

from django.test.client import Client
from django.core.handlers.wsgi import WSGIRequest
from django.conf import settings

CLIENT_BASE='test'
#TESTDS_ID=50

#def getTestImage (client):
#    try:
#        i = client.listImages(ns=TESTIMG_NS).next()
#    except StopIteration:
#        return None
#    return i

def getTestImageId (client):
    i = getTestImage(client)
    return i is not None and i.getId() or None

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
            'wsgi.version':      (1,0),
            'wsgi.url_scheme':   'http',
            'wsgi.errors':       self.errors,
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
        return r
    Client.bogus_request = bogus_request
    c = Client()
    return c.bogus_request()

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


class StoredConnectionModelTest(unittest.TestCase):
    def setUp (self):
        self._conf = tempfile.mkstemp()
        f = os.fdopen(self._conf[0], 'w')
        conffile = os.path.join(getattr(settings, 'ETCPATH', '.'), 'weblitz.conf')
        if os.path.exists(conffile):
            for l in open(conffile, 'rb').readlines():
                if l.startswith('omero.'):
                    f.write(l.strip()+'\n')
        f.write('weblitz.anon_user=%s\nweblitz.anon_pass=%s\nweblitz.admin_group=system\n' % (GUEST[0], GUEST[1]))
        f.close()
        conn = StoredConnection.objects.filter(base_path__iexact=CLIENT_BASE, enabled__exact=True)
        if len(conn) > 0:
            # already have the connection, update the config file
            s = conn[0]
            s.config_file = self._conf[1]
        else:
            # First call, create the connection
            s = StoredConnection(base_path=CLIENT_BASE, config_file=self._conf[1])
        s.save()
        conn = StoredConnection.objects.filter(base_path__iexact=CLIENT_BASE, enabled__exact=True)
        self.assert_(len(conn) > 0, 'Can not find connection %s' % CLIENT_BASE)
        self.conn = conn[0]
        self.gateway = self.conn.getBlitzGateway(trysuper=True)
        self.assert_(self.gateway, 'Can not get gateway from connection')
        self._has_connected = False
    
    def doConnect (self):
        if not self._has_connected:
            self.gateway.connect()
            self._has_connected = True
        self.assert_(self.gateway.isConnected(), 'Can not connect')
        self.failUnless(self.gateway.keepAlive(), 'Could not send keepAlive to connection')
    
    def doLogin (self, user, passwd):
        if self._has_connected:
            self.doDisconnect()
        self.gateway.setIdentity(user, passwd)
        self.assertEqual(self.gateway._ic_props[omero.constants.USERNAME], user)
        self.assertEqual(self.gateway._ic_props[omero.constants.PASSWORD], passwd)
        self.doConnect()
    
    def doDisconnect(self):
        if self._has_connected:
            self.doConnect()
            self.gateway.seppuku()
            self.assert_(not self.gateway.isConnected(), 'Can not disconnect')
        self.gateway = self.conn.getBlitzGateway()
        self.assert_(self.gateway, 'Can not get gateway from connection')
        self._has_connected = False

    def getTestProject (self):
        return getTestProject(self.gateway)

    def getTestDataset (self):
        return getTestDataset(self.gateway)

    def getTestDatasetId (self):
        d = self.getTestDataset()
        return d is not None and d.getId() or None

    def getTestImage (self):
        return getTestImage(self.gateway)

    def getTestImage2 (self):
        return getTestImage2(self.gateway)

    def getTestImageId (self):
        i = self.getTestImage()
        return i is not None and i.getId() or None

    def getTinyTestImage (self):
        return getTinyTestImage(self.gateway)

    def getTinyTestImageId (self):
        i = self.getTinyTestImage()
        return i is not None and i.getId() or None

    def getBadTestImage (self):
        return getBadTestImage(self.gateway)

    def tearDown (self):
        self.doDisconnect()
        os.remove(self._conf[1])


class UserTest (StoredConnectionModelTest):
    def testGuest (self):
        self.assertEqual(self.gateway._ic_props[omero.constants.USERNAME], GUEST[0])
        self.assertEqual(self.gateway._ic_props[omero.constants.PASSWORD], GUEST[1])
        self.doConnect()
        # Try reconnecting without disconnect
        self._has_connected = False
        self.doConnect()

    def testAuthor (self):
        self.doLogin(*AUTHOR)

    def testRoot (self):
        self.doLogin(*ROOT)

    def testSaveAs (self):
        for u in (ROOT, AUTHOR):
            self.doLogin(*u)
            # Test image should be owned by author
            image = self.getTestImage()
            self.assertEqual(image.getOwnerOmeName(), AUTHOR[0])
            # Create some object
            param = omero.sys.Parameters()
            param.map = {'ns': omero.rtypes.rstring('weblitz.UserTest.testSaveAs')}
            ann = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
            self.assertEqual(len(ann), 0)
            ann = omero.gateway.CommentAnnotationWrapper(conn=self.gateway)
            ann.setNs(param.map['ns'].val)
            ann.setValue('foo')
            ann.saveAs(image.getDetails())
            try:
                ann2 = self.gateway.getQueryService().findAllByQuery('from CommentAnnotation as a where a.ns=:ns', param)
                self.assertEqual(len(ann2), 1)
                self.assertEqual(omero.gateway.CommentAnnotationWrapper(self.gateway, ann2[0]).getOwnerOmeName(), AUTHOR[0])
            finally:
                self.gateway.getUpdateService().deleteObject(ann._obj)
        

class RDefsTest (StoredConnectionModelTest):
    TESTIMG_ID = None

    def setUp (self):
        super(RDefsTest, self).setUp()
        self.doLogin(*AUTHOR)
        self.image = self.getTestImage()
        self.assertNotEqual(self.image, None, 'No test image found on database')
        self.TESTIMG_ID = self.image.getId()
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.c0color = self.channels[0].getColor().getHtml()
        self.c1color = self.channels[1].getColor().getHtml()

    def tearDown (self):
        super(RDefsTest, self).tearDown()

    def testDefault (self):
        # Change the color for the rendering defs
        self.channels = self.image.getChannels()
        self.assertNotEqual(self.c0color, 'F0F000')
        self.assertNotEqual(self.c1color, '000F0F')
        self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'F0F000', u'000F0F'])
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')
        # Save it as default
        self.assert_(self.image.saveDefaults(), 'Failed saveDefaults')
        # Verify that it comes back as default
        self.image._re = None
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')
        #82 Changing default colors doesn't work correctly
        # the customizations weren't global, each user had its own
        self.doLogin(*ROOT)
        self.image = getTestImage(self.gateway)
        self.assertNotEqual(self.image, None, 'No test image found on database')
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')
        # so root sees the changes, but do root's changes get seen by author?
        self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'000F0F', u'F0F000'])
        self.assert_(self.image.saveDefaults(), 'Failed saveDefaults')
        self.doLogin(*AUTHOR)
        self.image = getTestImage(self.gateway)
        self.assertNotEqual(self.image, None, 'No test image found on database')
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), '000F0F')
        self.assertEqual(self.channels[1].getColor().getHtml(), 'F0F000')
        #82 ends, back to AUTHOR
        # Clean the customized default
        self.image.clearDefaults()
        self.image = getTestImage(self.gateway)
        self.channels = self.image.getChannels()
        # Verify we got back to the original state
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), self.c0color)
        self.assertEqual(self.channels[1].getColor().getHtml(), self.c1color)
        ## Check that only author (or admin) can change defaults
        #self.doLogin(*GUEST)
        #self.image = getTestImage(self.gateway, public=True)
        #self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'F0F000', u'000F0F'])
        #self.assert_(not self.image.saveDefaults(), 'saveDefaults should have failed!')
        ## Verify we are still in the original state
        #self.channels = self.image.getChannels()
        #self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        #self.assertEqual(self.channels[0].getColor().getHtml(), self.c0color)
        #self.assertEqual(self.channels[1].getColor().getHtml(), self.c1color)

    def testCustomized (self):
        self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'FF0000', u'0000FF'])
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'FF0000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '0000FF')

        self.image.setActiveChannels([1, 2],[[292.0, 1631.0], [409.0, 5015.0]],[u'F0F000', u'000F0F'])
        self.channels = self.image.getChannels()
        self.assert_(len(self.channels) == 2, 'bad channel count on image #%d' % self.TESTIMG_ID)
        self.assertEqual(self.channels[0].getColor().getHtml(), 'F0F000')
        self.assertEqual(self.channels[1].getColor().getHtml(), '000F0F')

    def testChannelWindows (self):
        """ Verify getters and setter related to channel window settings """
        for channel in self.channels:
            max = channel.getWindowMax()
            min = channel.getWindowMin()
            start = channel.getWindowStart()
            end = channel.getWindowEnd()
            self.assert_(min < start < end < max)
            channel.setWindowStart(min)
            self.assertEqual(channel.getWindowStart(), min)
            channel.setWindowEnd(max)
            self.assertEqual(channel.getWindowEnd(), max)
            channel.setWindow(start, end)
            self.assertEqual(channel.getWindowStart(), start)
            self.assertEqual(channel.getWindowEnd(), end)

    def testEmissionWave (self):
        """ """
        self.assertEqual(self.channels[0].getEmissionWave(), 457)
        self.assertEqual(self.channels[1].getEmissionWave(), 528)
        # Tiny image does not have emission wave set on the channel, ~should get channel index~
        # not channel index anymore, now get default wavelengths (first is 500)
        tiny = self.getTinyTestImage().getChannels()
        self.assertEqual(tiny[0].getEmissionWave(), 500)

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
        self.assertEqual(self.wcache.getImage(self.request, 'test', 1, 2, 3), None)
        self.wcache.setImage(self.request, 'test', 1, 2, 3, 'imagedata')
        self.assertEqual(self.wcache.getImage(self.request, 'test', 1, 2, 3), 'imagedata', 'Image not properly cached')
        self.wcache.clearImage(self.request, 'test', 1)
        self.assertEqual(self.wcache.getImage(self.request, 'test', 1, 2, 3), None)
        self.assertEqual(self.wcache.getThumb(self.request, 'test', 1), None)

class BlitzWrapperTest (StoredConnectionModelTest):
    def testProjectWrapper (self):
        self.doLogin(*AUTHOR)
        p = self.getTestProject()
        m = p.simpleMarshal()
        self.assertEqual(m['name'], p.getName())
        self.assertEqual(m['description'], p.getDescription())
        self.assertEqual(m['id'], p.getId())
        self.assertEqual(m['type'], p.OMERO_CLASS)
        self.assert_('parents' not in m)
        m = p.simpleMarshal(parents=True)
        self.assertEqual(m['name'], p.getName())
        self.assertEqual(m['description'], p.getDescription())
        self.assertEqual(m['id'], p.getId())
        self.assertEqual(m['type'], p.OMERO_CLASS)
        self.assertEqual(m['parents'], [])
        m = p.simpleMarshal(xtra={'childCount':None})
        self.assertEqual(m['name'], p.getName())
        self.assertEqual(m['description'], p.getDescription())
        self.assertEqual(m['id'], p.getId())
        self.assertEqual(m['type'], p.OMERO_CLASS)
        self.assert_('parents' not in m)
        self.assertEqual(m['child_count'], p.countChildren_cached())
        # Verify canOwnerWrite
        self.doLogin(*ROOT)
        p = self.getTestProject()
        self.assertEqual(p.canOwnerWrite(), True)
        p.getDetails().permissions.setUserWrite(False)
        self.assertEqual(p.canOwnerWrite(), False)
        # we did not save, but revert anyway
        p.getDetails().permissions.setUserWrite(True)
        self.assertEqual(p.canOwnerWrite(), True)

    def testDatasetWrapper (self):
        self.doLogin(*AUTHOR)
        d = self.getTestDataset()
        # first call to count_cached should calculate and store
        self.assertEqual(d.countChildren_cached(), 4)
        pm = d.listParents(single=True).simpleMarshal()
        m = d.simpleMarshal()
        self.assertEqual(m['name'], d.getName())
        self.assertEqual(m['description'], d.getDescription())
        self.assertEqual(m['id'], d.getId())
        self.assertEqual(m['type'], d.OMERO_CLASS)
        self.assert_('parents' not in m)
        m = d.simpleMarshal(parents=True)
        self.assertEqual(m['name'], d.getName())
        self.assertEqual(m['description'], d.getDescription())
        self.assertEqual(m['id'], d.getId())
        self.assertEqual(m['type'], d.OMERO_CLASS)
        self.assertEqual(m['parents'], [pm])
        m = d.simpleMarshal(xtra={'childCount':None})
        self.assertEqual(m['name'], d.getName())
        self.assertEqual(m['description'], d.getDescription())
        self.assertEqual(m['id'], d.getId())
        self.assertEqual(m['type'], d.OMERO_CLASS)
        self.assert_('parents' not in m)
        self.assertEqual(m['child_count'], d.countChildren_cached())
        # Do an extra check on listParents
        pm_multi = d.listParents(single=False)
        self.assertEqual([d.listParents(single=True)], pm_multi)

    def testExperimenterWrapper (self):
        self.doLogin(*ROOT)
        e = self.gateway.lookupExperimenter(GUEST[0])
        self.assertEqual(e.getDetails().getOwner().omeName, GUEST[0])

    def testDetailsWrapper (self):
        self.doLogin(*AUTHOR)
        img = self.getTestImage()
        d = img.getDetails()
        self.assertEqual(d.getOwner().omeName, AUTHOR[0])
        self.assertEqual(d.getGroup().name, AUTHOR[0] + '_group')

    def testSetters (self):
        """ verify the setters that coerce values into blitz friendly rtypes."""
        self.doLogin(*AUTHOR)
        p = self.getTestProject()
        n = p.getName()
        p.setName('some name')
        self.assertEqual(p.getName(), 'some name')
        # we have not saved, but just in case revert it
        p.setName(n)
        self.assertEqual(p.getName(), n)
        # Trying for something that does not exist must raise
        self.assertRaises(AttributeError, getattr, self, 'something_wild_that_does_not_exist')
        
    def testOther (self):
        p = omero.gateway.ProjectWrapper()
        self.assertNotEqual(repr(p), None)


class ImageTest (StoredConnectionModelTest):
    def setUp (self):
        super(ImageTest, self).setUp()
        self.doLogin(*AUTHOR)
        self.doConnect()
        self.TESTIMG_ID = self.getTestImageId()
        self.assertNotEqual(self.TESTIMG_ID, None, 'No test image found on database')
        self.image = self.gateway.getImage(self.TESTIMG_ID)
        self.assertEqual(repr(self.image), '<%s id=%i>' % (self.image.__class__.__name__, self.TESTIMG_ID))

    def testThumbnail (self):
        thumb = self.image.getThumbnail()
        tfile = StringIO(thumb)
        thumb = Image.open(tfile) # Raises if invalid
        thumb.verify() # Raises if invalid
        self.assertEqual(thumb.format, 'JPEG')
        self.assertEqual(thumb.size, (64,64))
        thumb = self.image.getThumbnail(96)
        tfile = StringIO(thumb)
        thumb = Image.open(tfile) # Raises if invalid
        thumb.verify() # Raises if invalid
        self.assertEqual(thumb.size, (96,96))
        thumb = self.image.getThumbnail((128, 96))
        tfile = StringIO(thumb)
        thumb = Image.open(tfile) # Raises if invalid
        thumb.verify() # Raises if invalid
        self.assertEqual(thumb.size, (128,96))
        badimage = self.getBadTestImage() # no pixels
        self.assertEqual(badimage.getThumbnail(), None)

    def testRenderingModels (self):
        # default is color model
        cimg = self.image.renderJpeg(0,0)
        ifile = StringIO(cimg)
        img = Image.open(ifile)
        extrema = img.getextrema()
        self.assert_(extrema[0] != extrema [1] or extrema[0] != extrema[2], 'Looks like a greyscale image')
        # Explicitely set the color model
        self.image.setColorRenderingModel()
        self.assertEqual(cimg, self.image.renderJpeg(0,0))
        # Now for greyscale
        self.image.setGreyscaleRenderingModel()
        ifile = StringIO(self.image.renderJpeg(0,0))
        img = Image.open(ifile)
        extrema = img.getextrema()
        self.assert_(extrema[0] == extrema [1] and extrema[0] == extrema[2], 'Looks like a color image')

    def testSplitChannel (self):
        cdims = self.image.splitChannelDims(border=4)
        # Verify border attribute works
        self.assert_(self.image.splitChannelDims(border=2)['c']['width']<cdims['c']['width'])
        # Default is color model, we have 2 channels
        self.assertEqual(cdims['c']['gridx'], 2)
        self.assertEqual(cdims['c']['gridy'], 2)
        # Render the view
        ifile = StringIO(self.image.renderSplitChannel(0,0,border=4))
        img = Image.open(ifile)
        self.assertEqual(img.size[0], cdims['c']['width'])
        self.assertEqual(img.size[1], cdims['c']['height'])
        # Same dance in greyscale
        self.assertEqual(cdims['g']['gridx'], 2)
        self.assertEqual(cdims['g']['gridy'], 1)
        # Render the view
        self.image.setGreyscaleRenderingModel()
        ifile = StringIO(self.image.renderSplitChannel(0,0,border=4))
        img = Image.open(ifile)
        self.assertEqual(img.size[0], cdims['g']['width'])
        self.assertEqual(img.size[1], cdims['g']['height'])
        # Make really sure the grid calculation works as expected
        g = ((1,1),(2,1),(2,2),(2,2),(3,2),(3,2),(3,3),(3,3),(3,3),(4,3),(4,3),(4,3),(4,4),(4,4),(4,4),(4,4))
        def c_count2 ():
            return i
        self.image.c_count = c_count2
        for i in range(1,len(g)): # 1..15
            dims = self.image.splitChannelDims()
            self.assertEqual((dims['g']['gridx'], dims['g']['gridy']), g[i-1]) 
            self.assertEqual((dims['c']['gridx'], dims['c']['gridy']), g[i]) 

    def testLinePlots (self):
        """ Verify requesting lineplots give out images matching size with the original. """
        # Vertical plot
        gif = StringIO(self.image.renderColLinePlotGif (z=0, t=0, x=1))
        img = Image.open(gif)
        img.verify() # Raises if invalid
        self.assertEqual(img.format, 'GIF')
        self.assertEqual(img.size, (self.image.getWidth(), self.image.getHeight()))
        # Horizontal plot
        gif = StringIO(self.image.renderRowLinePlotGif (z=0, t=0, y=1))
        img = Image.open(gif)
        img.verify() # Raises if invalid
        self.assertEqual(img.format, 'GIF')
        self.assertEqual(img.size, (self.image.getWidth(), self.image.getHeight()))
        badimage = self.getBadTestImage() # no pixels
        self.assertEqual(badimage.getCol(z=0, t=0, x=1), None)
        self.assertEqual(badimage.getRow(z=0, t=0, y=1), None)
        self.assertEqual(badimage.renderColLinePlotGif(z=0, t=0, x=1), None)
        self.assertEqual(badimage.renderRowLinePlotGif(z=0, t=0, y=1), None)

    def testProjections (self):
        """ Test image projections """
        for p in self.image.getProjections():
            self.image.setProjection(p)
            ifile = StringIO(self.image.renderJpeg(0,0))
            img = Image.open(ifile) # Raises if invalid
            img.verify() # Raises if invalid
            self.assertEqual(img.format, 'JPEG')
            self.assertEqual(img.size, (self.image.getWidth(), self.image.getHeight()))

    def testProperties (self):
        """ Tests the property getters that are not exercised implicitly on other tests. """
        self.assertEqual(self.image.getZ(), 0)
        self.assertEqual(self.image.getT(), 0)
        # Make sure methods fail with none if no pixels are found
        self.assertNotEqual(self.image.getPixelSizeX(), None)
        badimage = self.getBadTestImage() # no pixels
        self.assertEqual(badimage.getPixelSizeX(), None)
        self.assertEqual(badimage.getChannels(), None)
            
    def testShortname (self):
        """ Test the shortname method """
        name = self.image.name
        l = len(self.image.name)
        self.assertEqual(self.image.shortname(length=l+4, hist=5), self.image.name)
        self.assertEqual(self.image.shortname(length=l-4, hist=5), self.image.name)
        self.assertEqual(self.image.shortname(length=l-5, hist=5), '...'+self.image.name[-l+5:])
        self.image.name = ''
        self.assertEqual(self.image.shortname(length=20, hist=5), '')
        self.image.name = name

    def testSimpleMarshal (self):
        """ Test the call to simpleMarhal """
        m = self.image.simpleMarshal()
        self.assertEqual(m['name'], self.image.getName())
        self.assertEqual(m['description'], self.image.getDescription())
        self.assertEqual(m['id'], self.image.getId())
        self.assertEqual(m['type'], self.image.OMERO_CLASS)
        self.assertEqual(m['author'], ' '.join(AUTHOR_NAME))
        self.assert_('parents' not in m)
        self.assert_('thumb_url' not in m)
        self.assert_('date' in m)
        parents = map(lambda x: x.simpleMarshal(), self.image.getAncestry())
        m = self.image.simpleMarshal(parents=True, xtra={'thumbUrlPrefix': '/render_url_thumb/'})
        self.assertEqual(m['name'], self.image.getName())
        self.assertEqual(m['description'], self.image.getDescription())
        self.assertEqual(m['id'], self.image.getId())
        self.assertEqual(m['type'], self.image.OMERO_CLASS)
        self.assertEqual(m['author'], ' '.join(AUTHOR_NAME))
        self.assertEqual(m['thumb_url'], '/render_url_thumb/%i/' % self.image.getId())
        self.assert_('date' in m)
        self.assertEqual(m['parents'], parents)

class JsonTest (StoredConnectionModelTest):
    def setUp (self):
        super(JsonTest, self).setUp()
        self.doLogin(*AUTHOR)
        self.TESTDS_ID = self.getTestDatasetId()
        self.TESTIMG_ID = self.getTestImageId()
        self.assertNotEqual(self.TESTIMG_ID, None, 'No test image found on database')

    def testImageData (self):
        r = fakeRequest()
        v = views.imageData_json(r, CLIENT_BASE, self.TESTIMG_ID, _conn=self.gateway)
        self.assert_(type(v) == type(''))
        self.assert_('"width": 512' in v)
        self.assert_('"split_channel":' in v)
        v = views.listImages_json(r, CLIENT_BASE, self.TESTDS_ID, _conn=self.gateway)
        self.assert_('"id": %i,' % self.TESTIMG_ID in v)
        #75 simpleMarshal was failing with images lacking pixel sizes
        img = self.getTestImage2()
        self.assertNotEqual(img, None, 'test image 2 not found.')
        v = simplejson.loads(views.imageData_json(r, CLIENT_BASE, img.id, _conn=self.gateway))
        self.assertEqual(v['pixel_size']['x'], None)
        self.assertEqual(v['pixel_size']['y'], None)
        self.assertEqual(v['pixel_size']['z'], None)

class AnnotationsTest (StoredConnectionModelTest):
    TESTANN_NS = 'weblitz.test_annotation'
    def setUp (self):
        super(AnnotationsTest, self).setUp()
        self.doLogin(*AUTHOR)
        self.TESTIMG = getTestImage(self.gateway)
        self.assertNotEqual(self.TESTIMG, None, 'No test image found on database')

    def _testAnnotation (self, obj, annclass, ns, value):
        # Make sure it doesn't yet exist
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)
        # Create new, link and check
        ann = annclass(self.gateway)
        ann.setNs(ns)
        ann.setValue(value)
        obj.linkAnnotation(ann)
        ann = obj.getAnnotation(ns)
        tval = hasattr(value, 'val') and value.val or value
        self.assert_(ann.getValue() == value, '%s != %s' % (str(ann.getValue()), str(tval)))
        self.assert_(ann.getNs() == ns,  '%s != %s' % (str(ann.getNs()), str(ns)))
        # Remove and check
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)
        # Same dance, createAndLink shortcut
        annclass.createAndLink(target=obj, ns=ns, val=value)
        ann = obj.getAnnotation(ns)
        tval = hasattr(value, 'val') and value.val or value
        self.assert_(ann.getValue() == value, '%s != %s' % (str(ann.getValue()), str(tval)))
        self.assert_(ann.getNs() == ns,  '%s != %s' % (str(ann.getNs()), str(ns)))
        # Remove and check
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)


    def testCommentAnnotation (self):
        self._testAnnotation(self.TESTIMG, omero.gateway.CommentAnnotationWrapper, self.TESTANN_NS, 'some value')

    def testTimestampAnnotation (self):
        now = time.time()
        t = datetime.datetime.fromtimestamp(int(now))
        self._testAnnotation(self.TESTIMG, omero.gateway.TimestampAnnotationWrapper, self.TESTANN_NS, t)
        # Now use RTime, but this one doesn't fit in the general test case
        t = omero.rtypes.rtime(int(now))
        omero.gateway.TimestampAnnotationWrapper.createAndLink(target=self.TESTIMG, ns=self.TESTANN_NS, val=t)
        t = datetime.datetime.fromtimestamp(t.val / 1000.0)
        ann = self.TESTIMG.getAnnotation(self.TESTANN_NS)
        self.assert_(ann.getValue() == t, '%s != %s' % (str(ann.getValue()), str(t)))
        self.assert_(ann.getNs() == self.TESTANN_NS,  '%s != %s' % (str(ann.getNs()), str(self.TESTANN_NS)))
        # Remove and check
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)
        # A simple int stating secs since the epoch, also not fitting in the general test case
        t = int(now)
        omero.gateway.TimestampAnnotationWrapper.createAndLink(target=self.TESTIMG, ns=self.TESTANN_NS, val=t)
        t = datetime.datetime.fromtimestamp(t)
        ann = self.TESTIMG.getAnnotation(self.TESTANN_NS)
        self.assert_(ann.getValue() == t, '%s != %s' % (str(ann.getValue()), str(t)))
        self.assert_(ann.getNs() == self.TESTANN_NS,  '%s != %s' % (str(ann.getNs()), str(self.TESTANN_NS)))
        # Remove and check
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)

    def testBooleanAnnotation (self):
        self._testAnnotation(self.TESTIMG, omero.gateway.BooleanAnnotationWrapper, self.TESTANN_NS, True)

    def testLongAnnotation (self):
        self._testAnnotation(self.TESTIMG, omero.gateway.LongAnnotationWrapper, self.TESTANN_NS, 1000L)

    def testDualLinkedAnnotation (self):
        """ Tests linking the same annotation to 2 separate objects """
        dataset = self.TESTIMG.listParents(single=True)
        self.assertNotEqual(dataset, None)
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)
        dataset.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS), None)
        ann = omero.gateway.CommentAnnotationWrapper(self.gateway)
        ann.setNs(self.TESTANN_NS)
        value = 'I suffer from multi link disorder'
        ann.setValue(value)
        self.TESTIMG.linkAnnotation(ann)
        dataset.linkAnnotation(ann)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS).getValue(), value)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS).getValue(), value)
        self.TESTIMG.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(self.TESTIMG.getAnnotation(self.TESTANN_NS), None)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS).getValue(), value)
        dataset.removeAnnotations(self.TESTANN_NS)
        self.assertEqual(dataset.getAnnotation(self.TESTANN_NS), None)

    def testListAnnotations (self):
        """ Other small things that need to be tested """
        ns1 = self.TESTANN_NS
        ns2 = ns1 + '_2'
        obj = self.TESTIMG
        annclass = omero.gateway.CommentAnnotationWrapper
        value = 'foo'
        # Make sure it doesn't yet exist
        obj.removeAnnotations(ns1)
        obj.removeAnnotations(ns2)
        self.assertEqual(obj.getAnnotation(ns1), None)
        self.assertEqual(obj.getAnnotation(ns2), None)
        # createAndLink
        annclass.createAndLink(target=obj, ns=ns1, val=value)
        annclass.createAndLink(target=obj, ns=ns2, val=value)
        ann1 = obj.getAnnotation(ns1)
        ann2 = obj.getAnnotation(ns2)
        l = list(obj.listAnnotations())
        self.assert_(ann1 in l)
        self.assert_(ann2 in l)
        l = list(obj.listAnnotations(ns=ns1))
        self.assert_(ann1 in l)
        self.assert_(ann2 not in l)
        l = list(obj.listAnnotations(ns=ns2))
        self.assert_(ann1 not in l)
        self.assert_(ann2 in l)
        l = list(obj.listAnnotations(ns='bogusns...bogusns...'))
        self.assert_(ann1 not in l)
        self.assert_(ann2 not in l)
        # Remove and check
        obj.removeAnnotations(ns1)
        obj.removeAnnotations(ns2)
        self.assertEqual(obj.getAnnotation(ns1), None)
        self.assertEqual(obj.getAnnotation(ns2), None)
        

class ConnectionMethodsTest (StoredConnectionModelTest):
    def testSeppuku (self):
        self.doLogin(*AUTHOR)
        self.assertNotEqual(getTestImage(self.gateway), None)
        self.gateway.seppuku()
        self.assertRaises(Ice.ConnectionLostException, getTestImage, self.gateway)
        self._has_connected = False
        self.doDisconnect()
        self.doLogin(*AUTHOR)
        self.assertNotEqual(getTestImage(self.gateway), None)
        self.gateway.seppuku(softclose=False)
        self.assertRaises(Ice.ConnectionLostException, getTestImage, self.gateway)
        self._has_connected = False
        self.doDisconnect()

    def testTopLevelObjects (self):
        ##
        # Test listProjects as root (sees, does not own)
        self.doLogin(*ROOT)
        parents = getTestImage(self.gateway).getAncestry()
        project_id = parents[-1].getId()
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=False))
        self.assert_(project_id in ids)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=True))
        self.assert_(project_id not in ids)
        ##
        # Test listProjects as author (sees, owns)
        self.doLogin(*AUTHOR)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=False))
        self.assert_(project_id in ids)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=True))
        self.assert_(project_id in ids)
        ##
        # Test listProjects as guest (does not see, does not own)
        self.doLogin(*GUEST)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=False))
        self.assert_(project_id not in ids)
        ids = map(lambda x: x.getId(), self.gateway.listProjects(only_owned=True))
        self.assert_(project_id not in ids)
        ##
        # Test getProject
        self.doLogin(*AUTHOR)
        self.assertEqual(self.gateway.getProject(project_id).getId(), project_id)
        ##
        # Test getDataset
        dataset_id = parents[0].getId()
        self.assertEqual(self.gateway.getDataset(dataset_id).getId(), dataset_id)
        ##
        # Test listExperimenters
        exps = map(lambda x: x.omeName, self.gateway.listExperimenters())
        for omeName in (GUEST[0], AUTHOR[0], ROOT[0].decode('utf-8')):
            self.assert_(omeName in exps)
            self.assert_(len(list(self.gateway.listExperimenters(omeName))) > 0)
        self.assert_(len(list(self.gateway.listExperimenters(GUEST[0]+AUTHOR[0]+ROOT[0]))) ==  0)
        ##
        # Test lookupExperimenter
        self.assertEqual(self.gateway.lookupExperimenter(GUEST[0]).omeName, GUEST[0])
        self.assertEqual(self.gateway.lookupExperimenter(GUEST[0]+AUTHOR[0]+ROOT[0]), None)
        ##
        # still logged in as Author, test listImages(ns)
        ns = 'weblitz.test_annotation'
        obj = self.getTestImage()
        # Make sure it doesn't yet exist
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)
        # Check without the ann
        self.assertEqual(len(list(self.gateway.listImages(ns=ns))), 0)
        annclass = omero.gateway.CommentAnnotationWrapper
        # createAndLink
        annclass.createAndLink(target=obj, ns=ns, val='foo')
        imgs = list((self.gateway.listImages(ns=ns)))
        self.assertEqual(len(imgs), 1)
        self.assertEqual(imgs[0], obj)
        # and clean up
        obj.removeAnnotations(ns)
        self.assertEqual(obj.getAnnotation(ns), None)

    def testCloseSession (self):
        #74 the failed connection for a user not in the system group does not get closed
        self.gateway.setIdentity(*GUEST)
        setprop = self.gateway.c.ic.getProperties().setProperty
        map(lambda x: setprop(x[0],str(x[1])), self.gateway._ic_props.items())
        self.gateway.c.ic.getImplicitContext().put(omero.constants.GROUP, self.gateway.group)
        self.assertEqual(self.gateway._sessionUuid, None)
        self.assertRaises(omero.SecurityViolation, self.gateway._createSession)
        self.assertNotEqual(self.gateway._sessionUuid, None)
        #74 bug found while fixing this, the uuid passed to closeSession was not wrapped in rtypes, so logout didn't
        self.gateway._closeSession() # was raising ValueError

        
    def testMiscellaneous (self):
        self.doLogin(*GUEST)
        self.assertEqual(self.gateway.getUser().omeName, GUEST[0])

class AdminServiceTest (StoredConnectionModelTest):
    def testBasic (self):
        self.doLogin(*GUEST)
        self.assertNotEqual(self.gateway.getAdminService(), None)
