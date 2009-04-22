#
# webgateway/views.py - django application view handling functions
# 
# Copyright (c) 2007, 2008, 2009 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>

from django.http import HttpResponse, HttpResponseServerError, HttpResponseRedirect, Http404
from django.utils import simplejson

from omero import client_wrapper, ApiUsageException
from omero.gateway import timeit

from models import StoredConnection

from webgateway_cache import webgateway_cache

connectors = {}
CONNECTOR_POOL_SIZE = 70
CONNECTOR_POOL_KEEP = 0.75 # keep only SIZE-SIZE*KEEP of the connectors if POOL_SIZE is reached

import logging, os, traceback, time

logger = logging.getLogger('webgateway')

logger.debug("INIT")

def _session_logout (request, client_base, force_key=None):
    """ Remove reference to old sUuid key and old blitz connection. """
    if force_key:
        session_key = force_key
    else:
        browsersession_connection_key = 'cuuid#%s'%client_base
        session_key = 'S:' + request.session.get(browsersession_connection_key,'') + '#' + str(client_base)
        if request.session.has_key(browsersession_connection_key):
            logger.debug('logout: removing "%s"' % (request.session[browsersession_connection_key]))
            del request.session[browsersession_connection_key]
    if connectors.has_key(session_key):
        logger.debug('logout: killing connection "%s"' % (session_key))
        connectors[session_key] and connectors[session_key].seppuku()
        del connectors[session_key]

class UserProxy (object):
    def __init__ (self, blitzcon):
        self._blitzcon = blitzcon
        self.loggedIn = False

    def logIn (self):
        self.loggedIn = True

    def isAdmin (self):
        return self._blitzcon.isAdmin()

    def getName (self):
        return self._blitzcon._user.omeName

class SessionCB (object):
    def _log (self, what, c):
        logger.debug('CONN:%s %s:%d:%s' % (what, c._user, os.getpid(), c._sessionUuid))

    def create (self, c):
        self._log('create',c)

    def join (self, c):
        self._log('join',c)

    def close (self, c):
        self._log('close',c)
_session_cb = SessionCB()

def _createConnection (client_base, sUuid=None, trysuper=False, username=None, passwd=None, host=None, port=None, retry=True, skip_stored=False):
    try:
        if skip_stored:
            blitzcon = client_wrapper(username, passwd, host=host, port=port)
            blitzcon.connect()
            blitzcon.client_base = client_base
            blitzcon.user = UserProxy(blitzcon)
            return blitzcon
        else:
            conns = StoredConnection.objects.filter(base_path__iexact=client_base, enabled__exact=True).order_by('failcount')
            for n, c in enumerate(conns):
                blitzcon = c.getBlitzGateway(trysuper)
                logger.debug('#' + str(blitzcon))
                blitzcon._session_cb = _session_cb
                if username:
                    blitzcon.setIdentity(username, passwd)
                    sUuid = None
                    logger.debug("Trying BlitzGateway(%s,%s,%s)" % (username,passwd,c))
                r = not blitzcon.isConnected() and not blitzcon.connect(sUuid=sUuid)
                if r:
                    logger.warning("failed connect to StoreConnection #%i" % n)
                    if not sUuid:
                        c.failcount += 1
                        c.save()
                        continue
                blitzcon.client_base = client_base
                blitzcon.user = UserProxy(blitzcon)
                return blitzcon
        return None
    except:
        if not retry:
            return None
        logger.error("Critical error during connect, retrying after _purge")
        logger.debug(traceback.format_exc())
        _purge(force=True)
        return _createConnection(client_base, sUuid, trysuper, username, passwd, retry=False, host=host, port=port, skip_stored=skip_stored)

@timeit
def _purge (force=False):
    logger.debug("#$# %d" %len(connectors))
    if force or len(connectors) > CONNECTOR_POOL_SIZE:
        logger.debug('reached connector_pool_size (%d)' % CONNECTOR_POOL_SIZE)
        keys = connectors.keys()
        for i in range(int(len(connectors)*CONNECTOR_POOL_KEEP)):
            try:
                c = connectors.pop(keys[i])
                c.seppuku(softclose=True)
            except:
                logger.debug(traceback.format_exc())
        logger.debug('new connector_pool_size = %d' % len(connectors))

@timeit
def getBlitzConnection (request, client_base, with_session=False, force_anon=False, retry=True, skip_stored=False,
                        force_key=None):
    """
    Grab a connection to the Ice server, trying hard to reuse connections as possible.
    A per-process dictionary of StoredConnection based connections (key = "C:$base") is kept.
    A per-process dictionary of session created connections (key = "S:$session_id") is kept too.
    To allow multiple worker processes to access the session created connections (which will need to be
    recreated on each process) the blitz session key will be kept in the django session data, and joining that
    session is attempted, thus avoiding having to keep user/pass around.
    """
    r = request.REQUEST
    browsersession_connection_key = 'cuuid#%s'%client_base
    browsersession_key = request.session.session_key
    blitz_session = None

    username = request.session.get('username', r.get('username', None))
    passwd = request.session.get('password', r.get('password', None))
    host = request.session.get('host', r.get('host', None))
    port = request.session.get('port', r.get('port', None))
    logger.debug(':: %s %s ::' % (str(username), str(passwd)))

    if r.has_key('logout'):
        logger.debug('logout required by HTTP GET or POST')
    elif r.has_key('bsession') and not force_anon:
        blitz_session = r['bsession']
        request.session[browsersession_connection_key] = blitz_session

    if force_key:
        ckey = force_key
    else:
        ####
        # If we don't want to use sessions, just go with the client connection
        if not with_session and not request.session.has_key(browsersession_connection_key) and \
           not username and not blitz_session:
            ckey = 'C:' + str(client_base)
            logger.debug("a)connection key: %s" % ckey)
        elif browsersession_key is None:
            ckey = 'C:' + str(client_base)
        else:
            ####
            # If there is a session key, find if there's a connection for it
            if browsersession_key is not None and request.session.get(browsersession_connection_key, False):
                ckey = 'S:' + request.session[browsersession_connection_key] + '#' + str(client_base)
                logger.debug("b)connection key: %s" % ckey)
            else:
                ckey = 'S:' # postpone key creation to when we have a session uuid

    if username and not skip_stored:
        logger.debug('forcing new connection with login')
        blitzcon = None
    else:
        logger.debug('trying stored connection')
        logger.debug(connectors.items())
        blitzcon = connectors.get(ckey, None)
        if not force_key and blitzcon and request.session.get(browsersession_connection_key, blitzcon._sessionUuid) != blitzcon._sessionUuid:
            logger.debug('stale connection found: %s != %s' % (str(request.session.get(browsersession_connection_key, None)), str(blitzcon._sessionUuid)))
            blitzcon.seppuku()
            blitzcon = None

    if blitzcon is None:
        ####
        # No stored connection matching the request found, so create a new one
        if ckey.startswith('S:') and not force_key:
            ckey = 'S:'
        logger.debug('creating new connection with "%s"' % (ckey))
        if force_key:
            sUuid = None
        else:
            sUuid = request.session.get(browsersession_connection_key, None)
        blitzcon = _createConnection(client_base, sUuid=sUuid, trysuper=not ckey.startswith('C:'),
                                     username=username, passwd=passwd,
                                     host=host, port=port, skip_stored=skip_stored)
        if blitzcon is None:
            if not retry or username:
                logger.debug('connection failed with provided login information, bail out')
                return None
            if force_anon:
                logger.debug('connection failed without provided login information, already retried, bail out')
                return None
            return getBlitzConnection(request, client_base, with_session, force_anon=True, retry=False, skip_stored=skip_stored)
        else:
            logger.debug('created new connection %s' % str(blitzcon))
            if not blitzcon.isConnected():
                ####
                # Have a blitzcon, but it doesn't connect.
                if username:
                    logger.debug('connection failed with provided login information, bail out')
                    return None
                logger.debug('Failed connection, logging out')
                _session_logout(request, client_base)
                return getBlitzConnection(request, client_base, with_session, force_anon=True, skip_stored=skip_stored)
            else:
                ####
                # Success, new connection created
                if ckey == 'S:':
                    ckey = 'S:' + blitzcon._sessionUuid + '#' + str(client_base)
                _purge()
                connectors[ckey] = blitzcon
                logger.debug(str(connectors.items()))
                if username or blitz_session:
                    ####
                    # Because it was a login, store some data
                    if not force_key:
                        request.session[browsersession_connection_key] = blitzcon._sessionUuid #blitzcon._sessionUuid
                    logger.debug('blitz session key: ' + blitzcon._sessionUuid)
                    logger.debug('stored as session.' + ckey)
                    blitzcon.user.logIn()
                elif request.session.get(browsersession_connection_key, None):
                    blitzcon.user.logIn()

    if blitzcon and not blitzcon.keepAlive() and not ckey.startswith('C:'):
        logger.info("Failed keepalive")
        _session_logout(request, client_base)
        return getBlitzConnection(request, client_base, with_session, force_anon=True, skip_stored=skip_stored)
    if blitzcon and ckey.startswith('C:') and not blitzcon.isConnected():
        logger.info("Something killed the base connection, recreating")
        del connectors[ckey]
        return getBlitzConnection(request, client_base, with_session, force_anon=True, skip_stored=skip_stored)
    if r.has_key('logout') and not ckey.startswith('C:'):
        logger.debug('logout required by HTTP GET or POST : killing current connection')
        _session_logout(request, client_base)
        return getBlitzConnection(request, client_base, with_session, force_anon=True, skip_stored=skip_stored)
    # After keepalive the user session may have been replaced with an 'anonymous' one...
    if not force_key and blitzcon and request.session.get(browsersession_connection_key, None) != blitzcon._sessionUuid:
        logger.debug('Cleaning the user proxy %s!=%s' % (str(request.session.get(browsersession_connection_key, None)), str(blitzcon._sessionUuid)))
        blitzcon.user = UserProxy(blitzcon)

    return blitzcon

def _split_channel_info (rchannels):
    """
    Splits the request query channel information for images into a sequence of channels, window ranges
    and channel colors.
    """
    channels = []
    windows = []
    colors = []
    for chan in rchannels.split(','):
        chan = chan.split('|')
        t = chan[0].strip()
        color = None
        if t.find('$')>=0:
            t,color = t.split('$')
        try:
            channels.append(int(t))
            ch_window = (None, None)
            if len(chan) > 1:
                t = chan[1].strip()
                if t.find('$')>=0:
                    t, color = t.split('$')
                t = t.split(':')
                if len(t) == 2:
                    try:
                        ch_window = [float(x) for x in t]
                    except ValueError:
                        pass
            windows.append(ch_window)
            colors.append(color)
        except ValueError:
            pass
    logger.debug(str(channels)+","+str(windows)+","+str(colors))
    return channels, windows, colors

def getImgDetailsFromReq (request, as_string=False):
    """ Break the GET information from the request object into details on how to render the image.
    The following keys are recognized:
    z - Z axis position
    t - T axis position
    q - Quality set (0,0..1,0)
    m - Model (g for greyscale, c for color)
    p - Projection (see blitz_gateway.ImageWrapper.PROJECTIONS for keys)
    x - X position (for now based on top/left offset on the browser window)
    y - Y position (same as above)
    c - a comma separated list of channels to be rendered (start index 1)
      - format for each entry [-]ID[|wndst:wndend][#HEXCOLOR][,...]
    zm - the zoom setting (as a percentual value)
    """
    r = request.REQUEST
    rv = {}
    for k in ('z', 't', 'q', 'm', 'zm', 'x', 'y', 'p'):
        if r.has_key(k):
           rv[k] = r[k]
    if r.has_key('c'):
        rv['c'] = []
        ci = _split_channel_info(r['c'])
        logger.debug(ci)
        for i in range(len(ci[0])):
            # a = abs channel, i = channel, s = window start, e = window end, c = color
          rv['c'].append({'a':abs(ci[0][i]), 'i':ci[0][i], 's':ci[1][i][0], 'e':ci[1][i][1], 'c':ci[2][i]})
    if as_string:
        return "&".join(["%s=%s" % (x[0], x[1]) for x in rv.items()])
    return rv

@timeit
def render_thumbnail (request, client_base, iid, **kwargs):
    """ Returns an HttpResponse wrapped jpeg with the rendered thumbnail for image 'iid' """
    jpeg_data = webgateway_cache.getThumb(request, client_base, iid)
    if jpeg_data is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            logger.debug("failed connect, HTTP404")
            raise Http404
        def getImage ():
            return blitzcon.getImage(iid)
        img = timeit(getImage)()
        if img is None:
            logger.debug("(b)Image %s not found..." % (str(iid)))
            raise Http404
        def getThumb ():
            return img.getThumbnail()
        jpeg_data = timeit(getThumb)()
        if jpeg_data is None:
            logger.debug("(c)Image %s not found..." % (str(iid)))
            raise Http404
        webgateway_cache.setThumb(request, client_base, iid, jpeg_data)
    else:
        pass
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

def _get_prepared_image (request, client_base, iid, with_session=True, saveDefs=False):
    """
    Fetches the Image object for image 'iid' and prepares it according to the request query, setting the channels,
    rendering model and projection arguments. The compression level is parsed and returned too.
    """
    r = request.REQUEST
    blitzcon = getBlitzConnection(request, client_base, with_session=with_session)
    if blitzcon is None or not blitzcon.isConnected():
        return None
    img = blitzcon.getImage(iid)
    if saveDefs:
        img._prepareRenderingEngine()
        img._re.resetDefaults()
    if r.has_key('c'):
        logger.debug("c="+r['c'])
        channels, windows, colors =  _split_channel_info(r['c'])
        if not img.setActiveChannels(channels, windows, colors):
            logger.debug("Something bad happened while setting the active channels...")
    if r.get('m', None) == 'g':
        img.setGreyscaleRenderingModel()
    elif r.get('m', None) == 'c':
        img.setColorRenderingModel()
    img.setProjection(r.get('p', None))
    compress_quality = r.get('q', None)
    if saveDefs:
        img._re.saveCurrentSettings()
    return (img, compress_quality)

@timeit
def render_image (request, client_base, iid, z, t, **kwargs):
    """ Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong """
    USE_SESSION = False
    jpeg_data = webgateway_cache.getImage(request, client_base, iid, z, t)
    if jpeg_data is None:
        pi = _get_prepared_image(request, client_base, iid, with_session=USE_SESSION)
        if pi is None:
            raise Http404
        img, compress_quality = pi
        jpeg_data = img.renderJpeg(z,t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setImage(request, client_base, iid, z, t, jpeg_data)
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

@timeit
def render_split_channel (request, client_base, iid, z, t, **kwargs):
    """ Renders a split channel view of the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict. """
    USE_SESSION = False
    jpeg_data = webgateway_cache.getSplitChannelImage(request, client_base, iid, z, t)
    if jpeg_data is None:
        pi = _get_prepared_image(request, client_base, iid, with_session=USE_SESSION)
        if pi is None:
            raise Http404
        img, compress_quality = pi
        compress_quality = compress_quality and float(compress_quality) or 0.9
        jpeg_data = img.renderSplitChannel(z,t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setSplitChannelImage(request, client_base, iid, z, t, jpeg_data)
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

def debug (f):
    def wrap (request, *args, **kwargs):
        debug = request.REQUEST.getlist('debug')
        if 'slow' in debug:
            time.sleep(15)
        if 'fail' in debug:
            raise Http404
        if 'error' in debug:
            raise AttributeError('Debug requested error')
        return f(request, *args, **kwargs)
    return wrap

def jsonp (f):
    def wrap (request, client_base, *args, **kwargs):
        _conn = kwargs.get('_conn', None)
        if _conn is None:
            blitzcon = getBlitzConnection(request, client_base)
            kwargs['_conn'] = blitzcon
        if kwargs['_conn'] is None or not kwargs['_conn'].isConnected():
            return HttpResponseServerError('""', mimetype='application/javascript')
        rv = f(request, client_base, *args, **kwargs)
        rv = simplejson.dumps(rv)
        c = request.REQUEST.get('callback', None)
        if c is not None:
            rv = '%s(%s)' % (c, rv)
        if _conn is not None:
            return rv
        return HttpResponse(rv, mimetype='application/javascript')
    return wrap

@timeit
@debug
def render_row_plot (request, client_base, iid, z, t, y, w=1, **kwargs):
    """ Renders the line plot for the image with id {{iid}} at {{z}} and {{t}} as gif with transparent background.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong 
    TODO: cache """
    if not w:
        w = 1
    pi = _get_prepared_image(request, client_base, iid)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    gif_data = img.renderRowLinePlotGif(int(z),int(t),int(y), int(w))
    if gif_data is None:
        raise Http404
    rsp = HttpResponse(gif_data, mimetype='image/gif')
    return rsp

@timeit
@debug
def render_col_plot (request, client_base, iid, z, t, x, w=1, **kwargs):
    """ Renders the line plot for the image with id {{iid}} at {{z}} and {{t}} as gif with transparent background.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong 
    TODO: cache """
    if not w:
        w = 1
    pi = _get_prepared_image(request, client_base, iid)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    gif_data = img.renderColLinePlotGif(int(z),int(t),int(x), int(w))
    if gif_data is None:
        raise Http404
    rsp = HttpResponse(gif_data, mimetype='image/gif')
    return rsp

def imageMarshal (image, key=None):
    """ return a dict with pretty much everything we know and care about an image,
    all wrapped in a pretty structure."""
    pr = image.getProject()
    ds = image.getDataset()
    try:
        rv = {
            'id': image.id,
            'size': {'width': image.getWidth(),
                     'height': image.getHeight(),
                     'z': image.z_count(),
                     't': image.t_count(),
                     'c': image.c_count(),},
            'pixel_size': {'x': image.getPixelSizeX(),
                           'y': image.getPixelSizeY(),
                           'z': image.getPixelSizeZ(),},
            'meta': {'name': image.name or '',
                     'description': image.description or '',
                     'author': image.getAuthor(),
                     'project': pr and pr.name or 'Multiple',
                     'project_id': pr and pr.id or None,
                     'project_description':pr and pr.description or '',
                     'dataset': ds and ds.name or 'Multiple',
                     'dataset_id': ds and ds.id or '',
                     'dataset_description': ds and ds.description or '',
                     'timestamp': time.mktime(image.getDate().timetuple()),
                     'image_id': image.id,},
            }
        try:
            rv['channels'] = map(lambda x: {'emissionWave': x.getEmissionWave(),
                                       'color': x.getColor().getHtml(),
                                       'window': {'min': x.getWindowMin(),
                                                  'max': x.getWindowMax(),
                                                  'start': x.getWindowStart(),
                                                  'end': x.getWindowEnd(),},
                                       'active': x.isActive()}, image.getChannels())
            rv['split_channel'] = image.splitChannelDims()
            rv['rdefs'] = {'model': image.isGreyscaleRenderingModel() and 'greyscale' or 'color',
                           'projection': 'normal', }
        except TypeError:
            # Will happen if an image has bad or missing pixel data
            rv['channels'] = ()
            rv['split_channel'] = ()
            rv['rdefs'] = {'model': 'color', 'projection': 'normal',}
    except AttributeError:
        rv = None
        raise
    if key is not None and rv is not None:
        for k in key.split('.'):
            rv = rv.get(k, {})
        if rv == {}:
            rv = None
    return rv

@jsonp
def imageData_json (request, client_base, iid, key=None, _conn=None, **kwargs):
    """ Get a dict with image information
    TODO: cache """
    blitzcon = _conn
    image = blitzcon.getImage(iid)
    if image is None:
        return HttpResponseServerError('""', mimetype='application/javascript')
    rv = imageMarshal(image, key)
    return rv

@timeit
@jsonp
def listImages_json (request, client_base, did, _conn=None, **kwargs):
    """ lists all Images in a Dataset, as json
    TODO: cache """
    blitzcon = _conn
    dataset = blitzcon.getDataset(did)
    if dataset is None:
        return HttpResponseServerError('""', mimetype='application/javascript')
    path = request.REQUEST.get('baseurl', 'http://'+request.get_host()+'/'+client_base)
    xtra = {'thumbUrlPrefix': path+'/render_thumbnail/'}
    return map(lambda x: x.simpleMarshal(xtra=xtra), dataset.listChildren())

@timeit
@jsonp
def listDatasets_json (request, client_base, pid, _conn=None, **kwargs):
    """ lists all Datasets in a Project, as json
    TODO: cache """
    blitzcon = _conn
    project = blitzcon.getProject(pid)
    rv = []
    if project is None:
        return HttpResponse('[]', mimetype='application/javascript')
    return [x.simpleMarshal(xtra={'childCount':0}) for x in project.listChildren()]

@timeit
@jsonp
def datasetDetail_json (request, client_base, did, _conn=None, **kwargs):
    """ return json encoded details for a dataset
    TODO: cache """
    blitzcon = _conn
    ds = blitzcon.getDataset(did)
    return ds.simpleMarshal()

@timeit
@jsonp
def listProjects_json (request, client_base, _conn=None, **kwargs):
    """ lists all Projects, as json
    TODO: cache """
    blitzcon = _conn
    rv = []
    for pr in blitzcon.listProjects():
        rv.append( {'id': pr.id, 'name': pr.name, 'description': pr.description or ''} )
    return rv

@timeit
@jsonp
def projectDetail_json (request, client_base, pid, _conn=None, **kwargs):
    """ grab details from one specific project
    TODO: cache """
    blitzcon = _conn
    pr = blitzcon.getProject(pid)
    rv = pr.simpleMarshal()
    return rv

@timeit
def search_json (request, client_base, *args, **kwargs):
    """ TODO: jsonp, cache """
    logger.debug("search request: %s" % (str(request.REQUEST.items())))
    blitzcon = getBlitzConnection(request, client_base)
    if blitzcon is None or not blitzcon.isConnected():
        return HttpResponseServerError('""', mimetype='application/javascript')
    r = request.REQUEST
    search = unicode(r.get('text', '')).encode('utf8')
    author = r.get('author', '')
    ctx = r.get('ctx', '')
    grabData = r.get('grabData', False);
    parents = bool(r.get('parents', False))
    if author:
        search += ' author:'+author
    rv = []
    logger.debug("simpleSearch(%s)" % (search))
    # search returns blitz_connector wrapper objects
    xtra = {'thumbUrlPrefix': 'http://'+request.get_host()+'/'+client_base+'/render_thumbnail/'}
    pks = None
    try:
        if ctx == 'imgs':
            sr = blitzcon.searchImages(search)
        else:
            sr = blitzcon.simpleSearch(search)
    except ApiUsageException:
        return HttpResponseServerError('"parse exception"', mimetype='application/javascript')
    def marshal ():
        rv = []
        if (grabData and ctx == 'imgs'):
            key = r.get('key', None)
            for e in sr:
                try:
                    rv.append(imageData_json(request, client_base, e.id, key, _conn=blitzcon))
                except AttributeError:
                    pass
            return rv
        else:
            return map(lambda x: x.simpleMarshal(xtra=xtra, parents=parents), sr)
    rv = timeit(marshal)()
    def rv2json ():
        if (grabData and ctx == 'imgs'):
            return '['+','.join(rv)+']'
        return simplejson.dumps(rv)
    json_data = timeit(rv2json)()
    return HttpResponse(json_data, mimetype='application/javascript')

@timeit
def save_image_rdef_json (request, client_base, iid):
    """ Requests that the rendering defs passed in the request be set as the default for this image.
    Only channel colors are used for now.
    TODO: jsonp """
    r = request.REQUEST

    pi = _get_prepared_image(request, client_base, iid, with_session=True, saveDefs=True)
    if pi is None:
        return HttpResponse('false', mimetype='application/javascript')
    webgateway_cache.clearImage(request, client_base, iid)
    return HttpResponse('true', mimetype='application/javascript')

def dbg_connectors (request):
    rv = connectors.items()
    return HttpResponse(rv, mimetype='text/plain')
