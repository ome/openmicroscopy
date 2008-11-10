#/bin/env python -i

#
# blitz_connector - python bindings and wrappers to access an OMERO blitz server
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

# Set up the python include paths
import sys
import logging
sys.path.append('blitzcon/icepy')
sys.path.append('blitzcon/lib')

from types import ListType, TupleType, UnicodeType

import omero
import Ice
import Glacier2

import traceback
import threading
import time

logger = logging.getLogger('blitz_connector')

TIMEOUT = 30

class BlitzConnector (threading.Thread):
    ICE_CONFIG = 'blitzcon/etc/ice.config'
    def __init__ (self, username, passwd, server, port, client_obj=None):
        super(BlitzConnector, self).__init__()
        self.setDaemon(True)
        self.client = client_obj
        self.server = server
        #props = Ice.createProperties(sys.argv)
        #props.setProperty("Ice.Default.Router","OMERO.Glacier2/router:tcp -p %s -h %s" % (port, server))
        #iid.properties = props
        self.c = omero.client(['--Ice.Config='+self.ICE_CONFIG])#, iid)

        # Calculate some defaults
        if server or port:
            iid = Ice.InitializationData()
            iid.properties = self.c.ic.getProperties()
            def_router = str(self.c.ic.getDefaultRouter()).split(' ')
            def_router_host = '-h' in def_router and def_router[def_router.index('-h')+1] or None
            def_router_port = '-p' in def_router and def_router[def_router.index('-p')+1] or None
            for t in ('-h', '-p'):
                if t in def_router:
                    p = def_router.index(t)
                    def_router.pop(p)
                    def_router.pop(p)
            new_router = ' '.join(def_router)
            if server or def_router_host:
                new_router += ' -h %s' % (server or def_router_host)
            if port or def_router_port:
                new_router += ' -p %s' % (port or def_router_port)
            logger.debug(new_router)
            iid.properties.setProperty('Ice.Default.Router', new_router)
            self.c = omero.client(id=iid)

        # The properties we are setting through the interface
        setprop = self.c.ic.getProperties().setProperty
        setprop(omero.constants.USERNAME, username)
        setprop(omero.constants.PASSWORD, passwd)

        self._connected = False
        self._user = None
        self._userid = None
        self.allow_thread_timeout = True
        self.updateTimeout()
        self.start()

    def updateTimeout (self):
        self._timeout = time.time() + TIMEOUT

    def isTimedout (self):
        if self._timeout < time.time():
            return True
        self.updateTimeout()
        return False

    def run (self):
        """ this thread lives forever, pinging whatever connection exists to keep it's services alive """
        logger.debug("Starting thread...")
        while not (self.allow_thread_timeout and self.isTimedout()):
            try:
                time.sleep(TIMEOUT)
                if self._connected:
                    #self.c._sf.keepAllAlive([x.obj for x in self._proxies.items()])
                    for k,v in self._proxies.items():
                        #print "Sending keepalive to %s" % k
                        if not v._ping():
                            logger.debug("... some error sending keepalive to %s" % k)
                            # connection should have been recreated and proxies are different now, so start all over
                            break
            except:
                print "!! something bad on the client proxy keepalive thread !!"
                logger.debug(traceback.format_exc())
        if self._connected:
            self.seppuku()
        logger.debug("##THREAD DEATH")

    def seppuku (self):
        self._connected = False
        self._timeout = 0
        if self.c:
            try:
                self.c.closeSession()
            except omero.Glacier2.SessionNotExistException:
                pass
            self.c = None
        self._proxies = None
        logger.debug("##DELETED CONNECTION")

    def __del__ (self):
        logger.debug("##GARBAGE COLLECTOR KICK IN")

    def connect (self):
        try:
            self._connected = False
            if not self.c:
                return False
            try:
                self.c.closeSession()
            except omero.Glacier2.SessionNotExistException:
                pass
            self.c.createSession()
            self._last_error = None
            self._proxies = {}
            self._proxies['query'] = ProxyObjectWrapper(self, 'getQueryService')
            #self._proxies['repository'] = self.c.sf.getRepositoryInfoService()
            self._proxies['pojos'] = ProxyObjectWrapper(self, 'getPojosService')
            self._proxies['rendering'] = ProxyObjectWrapper(self, 'createRenderingEngine')
            self._proxies['thumbs'] = ProxyObjectWrapper(self, 'createThumbnailStore')
            self._user = self.c.getProperty(omero.constants.USERNAME)
            self._userid = self._proxies['query']._getObj().findByQuery("from Experimenter as e where e.omeName='%s'" % self._user,
                                                                     None).id.val
            self._connected = True
        except Exception, x:
            logger.debug("BlitzConnector.connect(): " + traceback.format_exc())
            self._last_error = x
            return False
        return True

    def getLastError (self):
        return self._last_error

    def isConnected (self):
        return self._connected

    ######################
    ## Connection Stuff ##
    
    def getUser (self):
        return self._user

    ##############
    ## Services ##

#    def getRepositoryInfoService (self):
#        return self._proxies['repository']#.getObj()

    def getQueryService (self):
        return self._proxies['query']#.getObj()

    def getPojosService (self):
        return self._proxies['pojos']#.getObj()

    def createRenderingEngine (self):
        return self._proxies['rendering']#.getObj()

    def createThumbnailStore (self):
        return self._proxies['thumbs']#.getObj()

    def createSearchService (self):
        """
        Creates a new search service.
        This service is special in that it does not get cached inside BlitzConnector so every call to this function
        returns a new object, avoiding unexpected inherited states.
        """
        return ProxyObjectWrapper(self, 'createSearchService')

    def getUpdateService (self):
        """
        """
        return ProxyObjectWrapper(self, 'getUpdateService')

    #############################
    # Top level object fetchers #

    def listProjects (self, only_owned=True):
        q = self.getQueryService()
        cache = {}
        if only_owned:
            for e in q.findAllByQuery("from Project as p where p.details.owner.id=%i" % self._userid, None):
                yield ProjectWrapper(self, e, cache)
        else:
            for e in q.findAll('Project', None):
                yield ProjectWrapper(self, e, cache)

    def listCategoryGroups (self):
        q = self.getQueryService()
        cache = {}
        for e in q.findAll("CategoryGroup", None):
            yield CategoryGroupWrapper(self, e, cache)

    def listExperimenters (self, start=''):
        """ Return a generator for all Experimenters whose omeName starts with 'start'.
        The generated values follow the alphabetic order on omeName."""
        if isinstance(start, UnicodeType):
            start = start.encode('utf8')
        q = self.getQueryService()
        rv = q.findAllByQuery("from Experimenter e where lower(e.omeName) like '%s%%'" % start.lower(), None)
        rv.sort(lambda x,y: cmp(x.omeName.val,y.omeName.val))
        for e in rv:
            yield ExperimenterWrapper(self, e)

    ###########################
    # Specific Object Getters #

    def getProject (self, oid):
        q = self.getQueryService()
        pr = q.find("Project", long(oid))
        if pr is not None:
            pr = ProjectWrapper(self, pr)
        return pr

    def getDataset (self, oid):
        q = self.getQueryService()
        ds = q.find("Dataset", long(oid))
        if ds is not None:
            ds = DatasetWrapper(self, ds)
        return ds

    def getImage (self, oid):
        q = self.getQueryService()
        img = q.find("Image", long(oid))
        if img is not None:
            img = ImageWrapper(self, img)
        return img

    ###################
    # Searching stuff #

    def simpleSearch (self, text):
        """
        Fulltext search on Projects, Datasets and Images.
        TODO: search other object types?
        TODO: batch support.
        """
        if not text:
            return []
        if isinstance(text, UnicodeType):
            text = text.encode('utf8')

        tokens = text.split(' ')
        text = []
        author = None
        for token in tokens:
            if token.find(':') > 0:
                pass
            else:
                text.append(token)
        text = text.join(' ')

        search = self.createSearchService()
        rv = []
        for t in (ProjectWrapper, DatasetWrapper, ImageWrapper):
            search.onlyType(t.OMERO_CLASS)
            search.byFullText(text)
            #search.bySomeMustNone(some, must, none)
            
            if search.hasNext():
                rv.extend(map(lambda x: t(self, x), search.results()))
        return rv

def safeCallWrap (self, attr, f):
    def wrapped (*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except Ice.Exception, x:
            # Failed
            print "Ice.Exception (1) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs))
            logger.debug(traceback.format_exc())
            # Recreate the proxy object
            try:
                self._obj = self._create_func()
                func = getattr(self._obj, attr)
                return func(*args, **kwargs)
            except Ice.Exception, x:
                # Still Failed
                print "Ice.Exception (2) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs))
                logger.debug(traceback.format_exc())
                try:
                    # Recreate connection
                    self._connect()
                    # Last try, don't catch exception
                    func = getattr(self._obj, attr)
                    return func(*args, **kwargs)
                except:
                    raise
                #    import pdb
                #    pdb.set_trace()

    return wrapped

def splitHTMLColor (color):
    out = []
    if len(color) in (3,4):
        c = color
        color = ''
        for e in c:
            color += e + e
    if len(color) == 6:
        color += 'FF'
    if len(color) == 8:
        for i in range(0, 8, 2):
            out.append(int(color[i:i+2], 16))
        return out
    return None

class ProxyObjectWrapper (object):
    def __init__ (self, conn, func_str):
        self._obj = None
        self._conn = conn
        self._func_str = func_str
        self._sf = conn.c.sf
        self._create_func = getattr(self._sf, self._func_str)
        self._obj = self._create_func()

    def _connect (self):
        logger.debug("proxy_connect: a");
        if not self._conn.connect():
            return False
        logger.debug("proxy_connect: b");
        self._sf = self._conn.c.sf
        logger.debug("proxy_connect: c");
        self._create_func = getattr(self._sf, self._func_str)
        logger.debug("proxy_connect: d");
        self._obj = self._create_func()
        logger.debug("proxy_connect: e");
        return True

    def _getObj (self):
        self._ping()
        return self._obj

    def _ping (self):
        """ For some reason, it seems that keepAlive doesn't, so every so often I need to recreate the objects """
        try:
            if not self._sf.keepAlive(self._obj):
                logger.debug("... died, recreating ...")
                self._obj = self._create_func()
        except Ice.ObjectNotExistException:
            # The connection is there, but it has been reset, because the proxy no longer exists...
            logger.debug("... reset, reconnecting")
            self._connect()
            return False
        except Ice.ConnectionLostException:
            # The connection was lost. This shouldn't happen, as we keep pinging it, but does so...
            logger.debug(traceback.format_stack())
            logger.debug("... lost, reconnecting")
            self._connect()
            return False
        except Ice.ConnectionRefusedException:
            # The connection was refused. We lost contact with glacier2router...
            logger.debug(traceback.format_stack())
            logger.debug("... refused, reconnecting")
            self._connect()
            return False
        return True

    def __getattr__ (self, attr):
        # safe call wrapper
        rv = getattr(self._obj, attr)
        if callable(rv):
            rv = safeCallWrap(self, attr, rv)
        self._conn.updateTimeout()
        return rv

class BlitzObjectWrapper (object):
    OMERO_CLASS = None
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None
    PARENT_WRAPPER_CLASS = None
    
    def __init__ (self, conn, obj, cache={}, **kwargs):
        self._conn = conn
        self._obj = obj
        self._cache = cache
        if hasattr(obj, 'id') and obj.id is not None:
            self._oid = obj.id.val
            if not self._obj.loaded:
                self._obj = self._conn.getQueryService().get(self._obj.__class__.__name__, self._oid)
        self.__prepare__ (**kwargs)

    def __prepare__ (self, **kwargs):
        pass

    def listChildren (self):
        if self.CHILD_WRAPPER_CLASS is None:
            raise NotImplementedError
        param = omero.sys.Parameters() # TODO: What can I use this for?
        childnodes = [ x.child for x in self._conn.getQueryService().findAllByQuery("from %s as c where c.parent.id=%i" % (self.LINK_CLASS, self._oid), param)]
        for child in childnodes:
            yield self.CHILD_WRAPPER_CLASS(self._conn, child, self._cache)

    def listChildren_cached (self):
        """ This version caches all child nodes for all parents, so next parent does not need to search again.
        Good for full depth traversal, but a waste of time otherwise """
        if self.CHILD_WRAPPER_CLASS is None:
            raise NotImplementedError
        if not self._cache.has_key(self.LINK_CLASS):
            pdl = {}
            for link in self._conn.getQueryService().findAll(self.LINK_CLASS, None):
                pid = link.parent.id.val
                if pdl.has_key(pid):
                    pdl[pid].append(link.child)
                else:
                    pdl[pid] = [link.child]
            self._cache[self.LINK_CLASS] = pdl
        for child in self._cache[self.LINK_CLASS].get(self._oid, ()):
            yield self.CHILD_WRAPPER_CLASS(self._conn, child, self._cache)


    def listParents (self):
        if self.PARENT_WRAPPER_CLASS is None:
            raise StopIteration
        if type(self.PARENT_WRAPPER_CLASS) is type(''):
            # resolve class
            g = globals()
            if not g.has_key(self.PARENT_WRAPPER_CLASS):
                raise NotImplementedError
            self.__class__.PARENT_WRAPPER_CLASS = g[self.PARENT_WRAPPER_CLASS]
        pwc = self.PARENT_WRAPPER_CLASS
        param = omero.sys.Parameters() # TODO: What can I use this for?
        parentnodes = [ x.parent for x in self._conn.getQueryService().findAllByQuery("from %s as c where c.child.id=%i" % (pwc.LINK_CLASS, self._oid), param)]
        for parent in parentnodes:
            yield pwc(self._conn, parent, self._cache)

    def listAnnotations (self):
        if not hasattr(self._obj, 'isAnnotationsLoaded'):
            raise NotImplemented
        if not self._obj.isAnnotationsLoaded():
            pojos = self._conn.getPojosService()
            self.annotations = pojos.findAnnotations(self._obj.__class__.__name__, [self._oid], None, None).get(self._oid, [])
            self.annotationsLoaded = True
        for ann in self.annotations:
            #print "annotation", ann.__class__.__name__
            yield AnnotationWrapper(self._conn, ann)

    def inspect (self):
        import pdb
        pdb.set_trace()

    def __str__ (self):
        if hasattr(self._obj, 'value'):
            return str(self.value)
        return str(self._obj)

    def __getattr__ (self, attr):
        if hasattr(self._obj, attr):
            rv = getattr(self._obj, attr)
            if hasattr(rv, 'val'):
                return rv.val
            return rv
        raise AttributeError("'%s' object has no attribute '%s'" % (self._obj.__class__.__name__, attr))

class AnnotationWrapper (BlitzObjectWrapper):
    pass

class ExperimenterWrapper (BlitzObjectWrapper):
    def getDetails (self):
        if not self._obj.details.owner:
            details = omero.model.Details()
            details.owner = self._obj
            self._obj.details = details
        return self._obj.details

class ColorWrapper (BlitzObjectWrapper):
    RED = 'Red'
    GREEN = 'Green'
    BLUE = 'Blue'
    DEFAULT_ALPHA = omero.RInt(255)
    @classmethod
    def new (klass, conn, colorname=None):
        color = klass(conn, omero.model.ColorI())
        if colorname is not None:
            color.setAlpha(klass.DEFAULT_ALPHA)
            for cname in (klass.RED, klass.GREEN, klass.BLUE):
                getattr(color, 'set'+cname)(cname == colorname and omero.RInt(255) or omero.RInt(0))
        return color

    def getHtml (self):
        """ Return the html usable color. Dumps the alpha information. """
        return "%0.2X%0.2X%0.2X" % (self.red,self.green,self.blue)

    def getCss (self):
        """ Return rgba(r,g,b,a) for this color """
        return "rgba(%i,%i,%i,%0.3f)" % (self.red,self.green,self.blue, self.alpha/255.0)

class ChannelWrapper (BlitzObjectWrapper):
    BLUE_MIN = 400
    BLUE_MAX = 500
    GREEN_MIN = 501
    GREEN_MAX = 600
    RED_MIN = 601
    RED_MAX = 700
    COLOR_MAP = ((BLUE_MIN, BLUE_MAX, ColorWrapper.BLUE),
                 (GREEN_MIN, GREEN_MAX, ColorWrapper.GREEN),
                 (RED_MIN, RED_MAX, ColorWrapper.RED),
                 )
    def __prepare__ (self, idx, re):
        self._re = re
        self._idx = idx

    def isActive (self):
        return self._re.isActive(self._idx)

    def getEmissionWave (self):
        lc = self._obj.getLogicalChannel()
        emWave = lc.getEmissionWave()
        if emWave is None:
            return self._idx
        else:
            return emWave.val

    def getColor (self):
        r,g,b,a = self._re.getRGBA(self._idx)
        #print r,g,b,a
        color = omero.model.ColorI()
        color.setRed(r)
        color.setGreen(g)
        color.setBlue(b)
        color.setAlpha(a)
        return ColorWrapper(self._conn, color)

    def getWindowStart (self):
        return int(self._re.getChannelWindowStart(self._idx))

    def setWindowStart (self, val):
        self.setWindow(val, self.getWindowEnd())

    def getWindowEnd (self):
        return int(self._re.getChannelWindowEnd(self._idx))

    def setWindowEnd (self, val):
        self.setWindow(self.getWindowStart(), val)

    def setWindow (self, minval, maxval):
        self._re.setChannelWindow(self._idx, float(minval), float(maxval))

    def getWindowMin (self):
        return self._obj.getStatsInfo().getGlobalMin().val

    def getWindowMax (self):
        return self._obj.getStatsInfo().getGlobalMax().val

def assert_re (func):
    def wrapped (self, *args, **kwargs):
        if not self._prepareRenderingEngine():
            return None
        return func(self, *args, **kwargs)
    return wrapped

def assert_pixels (func):
    def wrapped (self, *args, **kwargs):
        self._loadPixels()
        if not len(self._obj.pixels):
            print "No pixels!"
            return None
        return func(self, *args, **kwargs)
    return wrapped

class ImageWrapper (BlitzObjectWrapper):
    OMERO_CLASS = 'Image'
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None

    _re = None
    _pd = None
    _rm = {}
    
    PLANEDEF = omero.romio.XY

    def _loadPixels (self):
        if not self._obj.pixelsLoaded:
            param = omero.sys.Parameters() # TODO: What can I use this for?
            pixels = self._conn.getQueryService().findAllByQuery("from Pixels as p where p.image.id=%i" % self._oid, param)
            self._obj.pixelsLoaded = True
            self._obj.addPixelsSet(pixels)

    def _prepareRenderingEngine (self):
        self._loadPixels()
        if self._re is None:
            if not len(self._obj.pixels):
                print "No pixels!"
                return False
            pixels_id = self._obj.pixels[0].id.val
            if self._pd is None:
                self._pd = omero.romio.PlaneDef(self.PLANEDEF)
            if self._re is None:
                self._re = self._conn.createRenderingEngine()
                self._re.lookupPixels(pixels_id)
                if self._re.lookupRenderingDef(pixels_id) == False:
                    self._re.resetDefaults()
                    self._re.lookupRenderingDef(pixels_id)
                self._re.load()
        return True

    def shortname(self):
        max = 20
        try:
            name = self._obj.name.val
            if not name:
                return ""
            l = len(name)
            if l < max:
                return name
            return "..." + name[l - 20:l]
        except:
            logger.debug(traceback.format_exc())
            return self.name

    def getAuthor(self):
        q = self._conn.getQueryService()
        e = q.findByQuery("select e from Experimenter e where e.id = %i" % self._obj.details.owner.id.val,None)
        self._author = e.firstName.val + " " + e.lastName.val
        return self._author

    def getPublication(self):
        try:
            q = """
            select p from Image i join i.datasetLinks dl join dl.parent ds join ds.projectLinks pl join pl.parent p
            where i.id = %i
            """ % self._obj.id.val
            query = self._conn.getQueryService()
            prj = query.findByQuery(q,None)
            self._pub = prj.name.val
            self._pubId = prj.id.val
            return  self._pub
        except:
            logger.debug(traceback.format_exc())
            self._pub = "Muliple"
            self._pubId = "Multiple"
            return "Multiple"
        
    def getPublicationId(self):
        self.getPublication()
        return self._pubId

    def getDate(self):
        try:
            import time
            query = self._conn.getQueryService()
            event = query.findByQuery("select e from Event e where id = %i" % self._obj.details.creationEvent.id.val, None)
            return time.ctime(event.time.val / 1000)
        except:
            logger.debug(traceback.format_exc())
            self._date = "Today"
            return "Today"

    def getThumbnail (self, size=(64,64)):
        self._loadPixels()
        if not len(self._obj.pixels):
            print "No pixels!"
            return None
        pixels_id = self._obj.pixels[0].id.val
        tb = self._conn.createThumbnailStore()
        if not tb.setPixelsId(pixels_id):
            tb.resetDefaults()
            tb.setPixelsId(pixels_id)
        return tb.getThumbnailDirect(omero.RInt(64),omero.RInt(64))

    @assert_re
    def getChannels (self):
        return [ChannelWrapper(self._conn, c, idx=n, re=self._re) for n,c in enumerate(self._re.getPixels().channels)]

    def setActiveChannels(self, channels, windows, colors):
        for c in range(len(self.getChannels())):
            self._re.setActive(c, (c+1) in channels)
            if (c+1) in channels:
                if windows[c][0] and windows[c][1]:
                    self._re.setChannelWindow(c, *windows[c])
                if colors[c]:
                    rgba = splitHTMLColor(colors[c])
                    logger.debug('rgba[%i]=%s' %(c, str(rgba)))
                    if rgba:
                        self._re.setRGBA(c, *rgba)
            #print "Channel %i active: %s" % (c, str(self._re.isActive(c)))
        return True

    @assert_re
    def getRenderingModels (self):
        if not len(self._rm):
            for m in [BlitzObjectWrapper(self._conn, m) for m in self._re.getAvailableModels()]:
                self._rm[m.value.lower()] = m
        return self._rm.values()

    @assert_re
    def getRenderingModel (self):
        return BlitzObjectWrapper(self._conn, self._re.getModel())

    @assert_re
    def setRenderingModel (self, model_idx):
        models = self.getRenderingModels()
        if model_idx < len(models):
            self._re.setModel(models[model_idx]._obj)
        return True

    def setGreyscaleRenderingModel (self):
        """ Sets the Greyscale rendering model on this image's current renderer """
        rm = self.getRenderingModels()
        self._re.setModel(self._rm.get('greyscale', rm[0])._obj)

    def setColorRenderingModel (self):
        """ Sets the HSB rendering model on this image's current renderer """
        rm = self.getRenderingModels()
        self._re.setModel(self._rm.get('hsb', rm[0])._obj)

    def isGreyscaleRenderingModel (self):
        return self.getRenderingModel().value.lower() == 'greyscale'
        
    @assert_re
    def renderJpeg (self, z, t, active_channels=(), compression=0.9):
        self._pd.z = long(z)
        self._pd.t = long(t)
        try:
            if compression is not None:
                try:
                    self._re.setCompressionLevel(float(compression))
                    #print "CompressionLevel = " + str(compression)
                except omero.SecurityViolation:
                    self._obj.clearPixels()
                    self._obj.pixelsLoaded = False
                    self._re = None
                    return self.renderJpeg(z,t,active_channels, None)
            rv = self._re.renderCompressed(self._pd)
            return rv
        except omero.InternalException:
            logger.debug(traceback.format_exc())
            return None

    @assert_re
    def getZ (self):
        return self._pd.z

    @assert_re
    def getT (self):
        return self._pd.t

    @assert_pixels
    def getPixelSizeX (self):
        return self._obj.pixels[0].physicalSizeX.val

    @assert_pixels
    def getPixelSizeY (self):
        return self._obj.pixels[0].physicalSizeY.val

    @assert_pixels
    def getPixelSizeZ (self):
        return self._obj.pixels[0].physicalSizeZ.val

    @assert_pixels
    def getWidth (self):
        return self._obj.pixels[0].getSizeX().val

    @assert_pixels
    def getHeight (self):
        return self._obj.pixels[0].getSizeY().val

    @assert_pixels
    def z_count (self):
        return self._obj.pixels[0].getSizeZ().val

    @assert_pixels
    def t_count (self):
        return self._obj.pixels[0].getSizeT().val

    @assert_pixels
    def c_count (self):
        return self._obj.pixels[0].getSizeC().val
            

class DatasetWrapper (BlitzObjectWrapper):
    OMERO_CLASS = 'Dataset'
    LINK_CLASS = "DatasetImageLink"
    CHILD_WRAPPER_CLASS = ImageWrapper
    PARENT_WRAPPER_CLASS = 'ProjectWrapper'

class ProjectWrapper (BlitzObjectWrapper):
    OMERO_CLASS = 'Project'
    LINK_CLASS = "ProjectDatasetLink"
    CHILD_WRAPPER_CLASS = DatasetWrapper
    PARENT_WRAPPER_CLASS = None

class CategoryWrapper (BlitzObjectWrapper):
    LINK_CLASS = "CategoryImageLink"
    CHILD_WRAPPER_CLASS = ImageWrapper
    PARENT_WRAPPER_CLASS= 'CategoryGroupWrapper'

class CategoryGroupWrapper (BlitzObjectWrapper):
    LINK_CLASS = "CategoryGroupCategoryLink"
    CHILD_WRAPPER_CLASS = CategoryWrapper
    PARENT_WRAPPER_CLASS = None
