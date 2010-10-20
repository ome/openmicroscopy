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

import omero
import omero.clients
from django.http import HttpResponse, HttpResponseServerError, HttpResponseRedirect, Http404
from django.utils import simplejson
from django.core import template_loader
from django.core.urlresolvers import reverse
from django.template import RequestContext as Context

from omero import client_wrapper, ApiUsageException
from omero.gateway import timeit, TimeIt


#from models import StoredConnection

from webgateway_cache import webgateway_cache, CacheBase

cache = CacheBase()

connectors = {}
CONNECTOR_POOL_SIZE = 70
CONNECTOR_POOL_KEEP = 0.75 # keep only SIZE-SIZE*KEEP of the connectors if POOL_SIZE is reached

import logging, os, traceback, time

#omero.gateway.BlitzGateway.ICE_CONFIG = os.environ.get('ICE_CONFIG', 'etc/ice.config')

logger = logging.getLogger('webgateway')

logger.debug("INIT")

def _session_logout (request, server_id, force_key=None):
    """ Remove reference to old sUuid key and old blitz connection. """
    if force_key:
        session_key = force_key
    else:
        browsersession_connection_key = 'cuuid#%s'%server_id
        session_key = 'S:' + request.session.get(browsersession_connection_key,'') + '#' + str(server_id)
        if request.session.has_key(browsersession_connection_key):
            logger.debug('logout: removing "%s"' % (request.session[browsersession_connection_key]))
            del request.session[browsersession_connection_key]
    for k in ('username', 'password', 'server', 'host', 'port', 'ssl'):
        if request.session.has_key(k):
            del request.session[k]
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

    def canBeAdmin (self):
        return self._blitzcon.canBeAdmin()

    def getId (self):
        return self._blitzcon._user.id

    def getName (self):
        return self._blitzcon._user.omeName

    def getFirstName (self):
        return self._blitzcon._user.firstName or self.getName()

#    def getPreferences (self):
#        return self._blitzcon._user.getPreferences()
#
#    def getUserObj (self):
#        return self._blitzcon._user

#class SessionCB (object):
#    def _log (self, what, c):
#        logger.debug('CONN:%s %s:%d:%s' % (what, c._user, os.getpid(), c._sessionUuid))
#
#    def create (self, c):
#        self._log('create',c)
#
#    def join (self, c):
#        self._log('join',c)
#
#    def close (self, c):
#        self._log('close',c)
#_session_cb = SessionCB()

def _createConnection (server_id, sUuid=None, username=None, passwd=None, host=None, port=None, retry=True, group=None, try_super=False, secure=False, useragent=None):
    try:
        blitzcon = client_wrapper(username, passwd, host=host, port=port, group=None, try_super=try_super, secure=secure, useragent=useragent)
        blitzcon.connect(sUuid=sUuid)
        blitzcon.server_id = server_id
        blitzcon.user = UserProxy(blitzcon)
        if blitzcon._anonymous and hasattr(blitzcon.c, 'onEventLogs'):
            logger.debug('Connecting weblitz_cache to eventslog')
            def eventlistener (e):
                return webgateway_cache.eventListener(server_id, e)
            blitzcon.c.onEventLogs(eventlistener)
        return blitzcon
    except:
        logger.debug(traceback.format_exc())
        if not retry:
            return None
        logger.error("Critical error during connect, retrying after _purge")
        logger.debug(traceback.format_exc())
        _purge(force=True)
        return _createConnection(server_id, sUuid, username, passwd, retry=False, host=host, port=port, group=None, try_super=try_super, useragent=useragent)

def _purge (force=False):
    if force or len(connectors) > CONNECTOR_POOL_SIZE:
        keys = connectors.keys()
        for i in range(int(len(connectors)*CONNECTOR_POOL_KEEP)):
            try:
                c = connectors.pop(keys[i])
                c.seppuku(softclose=True)
            except:
                logger.debug(traceback.format_exc())
        logger.info('reached connector_pool_size (%d), size after purge: (%d)' %
                    (CONNECTOR_POOL_SIZE, len(connectors)))

def getBlitzConnection (request, server_id=None, with_session=False, retry=True, force_key=None, group=None, try_super=False, useragent=None):
    """
    Grab a connection to the Ice server, trying hard to reuse connections as possible.
    A per-process dictionary of StoredConnection based connections (key = "C:$base") is kept.
    A per-process dictionary of session created connections (key = "S:$session_id") is kept.
    Another set of connections (key = "C:$server_id") is also kept, for creating 'guest' connections
    using with_session=False.
    Server id, if not passed in as function argument, is retrieved from session['server'].
    To allow multiple worker processes to access the session created connections (which will need to be
    recreated on each process) the blitz session key will be kept in the django session data, and joining that
    session is attempted, thus avoiding having to keep user/pass around.
    """
    r = request.REQUEST
    if server_id is None:
        # If no server id is passed, the db entry will not be used and instead we'll depend on the
        # request.session and request.REQUEST values
        try:
            server_id = request.session['server']
        except KeyError:
            return None
        with_session = True
#        skip_stored = True
    browsersession_connection_key = 'cuuid#%s'%server_id
    browsersession_key = request.session.session_key
    blitz_session = None

    username = request.session.get('username', r.get('username', None))
    passwd = request.session.get('password', r.get('password', None))
    host = request.session.get('host', r.get('host', None))
    port = request.session.get('port', r.get('port', None))
    secure = request.session.get('ssl', r.get('ssl', False))
    #logger.debug(':: %s %s :: %s' % (str(username), str(passwd), str(browsersession_connection_key)))

#    if r.has_key('logout'):
#        logger.debug('logout required by HTTP GET or POST')
#    elif r.has_key('bsession') and not force_anon:
#        blitz_session = r['bsession']
#        request.session[browsersession_connection_key] = blitz_session

    if force_key:
        ckey = force_key
    else:
        ####
        # If we don't want to use sessions, just go with the client connection
        if not with_session and not request.session.has_key(browsersession_connection_key) and \
           not username and not blitz_session:
            ckey = 'C:' + str(server_id)
            #logger.debug("a)connection key: %s" % ckey)
        elif browsersession_key is None:
            ckey = 'C:' + str(server_id)
        else:
            ####
            # If there is a session key, find if there's a connection for it
            if browsersession_key is not None and request.session.get(browsersession_connection_key, False):
                ckey = 'S:' + request.session[browsersession_connection_key] + '#' + str(server_id)
                #logger.debug("b)connection key: %s" % ckey)
            else:
                ckey = 'S:' # postpone key creation to when we have a session uuid

    if r.get('username', None):
        logger.info('getBlitzConnection(host=%s, port=%s, ssl=%s, username=%s)' %
                    (str(host), str(port), str(secure), str(username)))
        logger.debug('p=%s, k=%s' % (str(passwd), str(browsersession_connection_key)))
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
        logger.debug('creating new connection with "%s" (%s)' % (ckey, try_super))
        if force_key or r.get('username', None):
            sUuid = None
        else:
            sUuid = request.session.get(browsersession_connection_key, None)
        blitzcon = _createConnection(server_id, sUuid=sUuid,
                                     username=username, passwd=passwd,
                                     host=host, port=port, group=group, try_super=try_super, secure=secure, useragent=useragent)
        if blitzcon is None:
            if not retry or username:
                logger.debug('connection failed with provided login information, bail out')
                return None
            return getBlitzConnection(request, server_id, with_session, retry=False, group=group, try_super=try_super, useragent=useragent)
        else:
            logger.debug('created new connection %s' % str(blitzcon))
            if not blitzcon.isConnected():
                ####
                # Have a blitzcon, but it doesn't connect.
                if username:
                    logger.debug('connection failed with provided login information, bail out')
                    return None
                logger.debug('Failed connection, logging out')
                _session_logout(request, server_id)
                return blitzcon
                #return getBlitzConnection(request, server_id, with_session, force_anon=True, skip_stored=skip_stored, useragent=useragent)
            else:
                ####
                # Success, new connection created
                if ckey == 'S:':
                    ckey = 'S:' + blitzcon._sessionUuid + '#' + str(server_id)
                _purge()
                connectors[ckey] = blitzcon
                logger.debug(str(connectors.items()))
                if username or blitz_session:
                    ####
                    # Because it was a login, store some data
                    if not force_key:
                        request.session[browsersession_connection_key] = blitzcon._sessionUuid
                    logger.debug('blitz session key: ' + blitzcon._sessionUuid)
                    logger.debug('stored as session.' + ckey)
                    blitzcon.user.logIn()
                elif request.session.get(browsersession_connection_key, None):
                    blitzcon.user.logIn()

    if blitzcon and not blitzcon.keepAlive() and not ckey.startswith('C:'):
        # session could expire or be closed by another client. webclient needs to recreate connection with new uuid
        # otherwise it will forward user to login screen.
        logger.info("Failed keepalive for connection %s" % ckey)
        del request.session[browsersession_connection_key]
        del connectors[ckey]
        #_session_logout(request, server_id)
        #return blitzcon
        return getBlitzConnection(request, server_id, with_session, retry=False, group=group, try_super=try_super, useragent=useragent)
    if blitzcon and ckey.startswith('C:') and not blitzcon.isConnected():
        logger.info("Something killed the base connection, recreating")
        del connectors[ckey]
        return None
        #return getBlitzConnection(request, server_id, with_session, force_anon=True, skip_stored=skip_stored, useragent=useragent)
    if r.has_key('logout') and not ckey.startswith('C:'):
        logger.debug('logout required by HTTP GET or POST : killing current connection')
        _session_logout(request, server_id)
        return None
        #return getBlitzConnection(request, server_id, with_session, force_anon=True, skip_stored=skip_stored, useragent=useragent)
#    # After keepalive the user session may have been replaced with an 'anonymous' one...
#    if not force_key and blitzcon and request.session.get(browsersession_connection_key, None) != blitzcon._sessionUuid:
#        logger.debug('Cleaning the user proxy %s!=%s' % (str(request.session.get(browsersession_connection_key, None)), str(blitzcon._sessionUuid)))
#        blitzcon.user = UserProxy(blitzcon)

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

def render_thumbnail (request, iid, server_id=None, w=None, h=None, **kwargs):
    """ Returns an HttpResponse wrapped jpeg with the rendered thumbnail for image 'iid' """
    if w is None:
        size = (64,)
    else:
        if h is None:
            size = (int(w),)
        else:
            size = (int(w), int(h))
    jpeg_data = webgateway_cache.getThumb(request, server_id, iid, size)
    if jpeg_data is None:
        blitzcon = getBlitzConnection(request, server_id, useragent="OMERO.webgateway")
        if blitzcon is None or not blitzcon.isConnected():
            logger.debug("failed connect, HTTP404")
            raise Http404
        img = blitzcon.getImage(iid)
        if img is None:
            logger.debug("(b)Image %s not found..." % (str(iid)))
            raise Http404
        jpeg_data = img.getThumbnail(size=size)
        if jpeg_data is None:
            logger.debug("(c)Image %s not found..." % (str(iid)))
            raise Http404
        webgateway_cache.setThumb(request, server_id, iid, jpeg_data, size)
    else:
        pass
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

def _get_prepared_image (request, iid, server_id=None, _conn=None, with_session=True, saveDefs=False, retry=True):
    """
    Fetches the Image object for image 'iid' and prepares it according to the request query, setting the channels,
    rendering model and projection arguments. The compression level is parsed and returned too.
    """
    r = request.REQUEST
    if _conn is None:
        _conn = getBlitzConnection(request, server_id=server_id, with_session=with_session, useragent="OMERO.webgateway")
    if _conn is None or not _conn.isConnected():
        return HttpResponseServerError('""', mimetype='application/javascript')
    #blitzcon = getBlitzConnection(request, server_id=server_id,with_session=with_session, useragent="OMERO.webgateway")
    if _conn is None or not _conn.isConnected():
        return None
    img = _conn.getImage(iid)
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
        r.has_key('z') and img._re.setDefaultZ(long(r['z'])-1)
        r.has_key('t') and img._re.setDefaultT(long(r['t'])-1)
        try:
            img.saveDefaults()
        except:
            # retry once, to get around "Session is dirty" exceptions
            if retry:
                return _get_prepared_image(request, iid=iid, server_id=server_id, _conn=_conn, with_session=with_session, saveDefs=saveDefs, retry=False)
            raise
    return (img, compress_quality)

def render_image (request, iid, z, t, server_id=None, _conn=None, **kwargs):
    """ Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong """
    USE_SESSION = False
    pi = _get_prepared_image(request, iid, server_id=server_id, _conn=_conn, with_session=USE_SESSION)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    jpeg_data = webgateway_cache.getImage(request, server_id, img, z, t)
    if jpeg_data is None:
        jpeg_data = img.renderJpeg(z,t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setImage(request, server_id, img, z, t, jpeg_data)
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

def render_split_channel (request, iid, z, t, server_id=None, _conn=None, **kwargs):
    """ Renders a split channel view of the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict. """
    USE_SESSION = False
    pi = _get_prepared_image(request, iid, server_id=server_id, _conn=_conn, with_session=USE_SESSION)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    compress_quality = compress_quality and float(compress_quality) or 0.9
    jpeg_data = webgateway_cache.getSplitChannelImage(request, server_id, img, z, t)
    if jpeg_data is None:
        jpeg_data = img.renderSplitChannel(z,t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setSplitChannelImage(request, server_id, img, z, t, jpeg_data)
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

def debug (f):
    def wrap (request, *args, **kwargs):
        debug = request.REQUEST.getlist('debug')
        if 'slow' in debug:
            time.sleep(5)
        if 'fail' in debug:
            raise Http404
        if 'error' in debug:
            raise AttributeError('Debug requested error')
        return f(request, *args, **kwargs)
    wrap.func_name = f.func_name
    return wrap

def jsonp (f):
    def wrap (request, *args, **kwargs):
        logger.debug('jsonp')
        try:
            server_id = kwargs.get('server_id', None)
            kwargs['server_id'] = server_id
            _conn = kwargs.get('_conn', None)
            if _conn is None:
                blitzcon = getBlitzConnection(request, server_id, useragent="OMERO.webgateway")
                kwargs['_conn'] = blitzcon
            if kwargs['_conn'] is None or not kwargs['_conn'].isConnected():
                return HttpResponseServerError('"failed connection"', mimetype='application/javascript')
            rv = f(request, *args, **kwargs)
            if _conn is not None and kwargs.get('_internal', False):
                return rv
            rv = simplejson.dumps(rv)
            c = request.REQUEST.get('callback', None)
            if c is not None and not kwargs.get('_internal', False):
                rv = '%s(%s)' % (c, rv)
            if _conn is not None or kwargs.get('_internal', False):
                return rv
            return HttpResponse(rv, mimetype='application/javascript')
        except omero.ServerError:
            if kwargs.get('_internal', False):
                raise
            return HttpResponseServerError('("error in call","%s")' % traceback.format_exc(), mimetype='application/javascript')
        except:
            logger.debug(traceback.format_exc())
    wrap.func_name = f.func_name
    return wrap

#def json_error_catch (f):
#    def wrap (*args, **kwargs):
#        try:
#            return f(*args, **kwargs)
#        except omero.ServerError:
#            return HttpResponseServerError('"error in call"', mimetype='application/javascript')
#    return wrap

@debug
def render_row_plot (request, iid, z, t, y, server_id=None, _conn=None, w=1, **kwargs):
    """ Renders the line plot for the image with id {{iid}} at {{z}} and {{t}} as gif with transparent background.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong 
    TODO: cache """
    if not w:
        w = 1
    pi = _get_prepared_image(request, iid, server_id=server_id, _conn=_conn)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    gif_data = img.renderRowLinePlotGif(int(z),int(t),int(y), int(w))
    if gif_data is None:
        raise Http404
    rsp = HttpResponse(gif_data, mimetype='image/gif')
    return rsp

@debug
def render_col_plot (request, iid, z, t, x, w=1, server_id=None, _conn=None, **kwargs):
    """ Renders the line plot for the image with id {{iid}} at {{z}} and {{t}} as gif with transparent background.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong 
    TODO: cache """
    if not w:
        w = 1
    pi = _get_prepared_image(request, iid, server_id=server_id, _conn=_conn)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    gif_data = img.renderColLinePlotGif(int(z),int(t),int(x), int(w))
    if gif_data is None:
        raise Http404
    rsp = HttpResponse(gif_data, mimetype='image/gif')
    return rsp
 
def channelMarshal (channel):
    """ return a dict with all there is to know about a channel """
    return {'emissionWave': channel.getEmissionWave(),
            'label': channel.getEmissionWave(),
            'color': channel.getColor().getHtml(),
            'window': {'min': channel.getWindowMin(),
                       'max': channel.getWindowMax(),
                       'start': channel.getWindowStart(),
                       'end': channel.getWindowEnd(),},
            'active': channel.isActive()}

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
            'meta': {'imageName': image.name or '',
                     'imageDescription': image.description or '',
                     'imageAuthor': image.getAuthor(),
                     'projectName': pr and pr.name or 'Multiple',
                     'projectId': pr and pr.id or None,
                     'projectDescription':pr and pr.description or '',
                     'datasetName': ds and ds.name or 'Multiple',
                     'datasetId': ds and ds.id or '',
                     'datasetDescription': ds and ds.description or '',
                     'imageTimestamp': time.mktime(image.getDate().timetuple()),
                     'imageId': image.id,},
            }
        try:
            rv['pixel_range'] = image.getPixelRange()
            rv['channels'] = map(lambda x: channelMarshal(x), image.getChannels())
            rv['split_channel'] = image.splitChannelDims()
            rv['rdefs'] = {'model': image.isGreyscaleRenderingModel() and 'greyscale' or 'color',
                           'projection': 'normal',
                           'defaultZ': image._re.getDefaultZ(),
                           'defaultT': image._re.getDefaultT()}
        except TypeError:
            # Will happen if an image has bad or missing pixel data
            rv['pixel_range'] = (0, 0)
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
def imageData_json (request, server_id=None, _conn=None, _internal=False, **kwargs):
    """ Get a dict with image information
    TODO: cache """
    iid = kwargs['iid']
    key = kwargs.get('key', None)
    blitzcon = _conn
    image = blitzcon.getImage(iid)
    if image is None:
        return HttpResponseServerError('""', mimetype='application/javascript')
    rv = imageMarshal(image, key)
    return rv

@jsonp
def listImages_json (request, did, server_id=None, _conn=None, **kwargs):
    """ lists all Images in a Dataset, as json
    TODO: cache """
    blitzcon = _conn
    dataset = blitzcon.getDataset(did)
    if dataset is None:
        return HttpResponseServerError('""', mimetype='application/javascript')
    def urlprefix(iid):
        return reverse('webgateway.views.render_thumbnail', args=(iid,))
    xtra = {'thumbUrlPrefix': urlprefix}
    return map(lambda x: x.simpleMarshal(xtra=xtra), dataset.listChildren())

@jsonp
def listDatasets_json (request, pid, server_id=None, _conn=None, **kwargs):
    """ lists all Datasets in a Project, as json
    TODO: cache """
    blitzcon = _conn
    project = blitzcon.getProject(pid)
    rv = []
    if project is None:
        return HttpResponse('[]', mimetype='application/javascript')
    return [x.simpleMarshal(xtra={'childCount':0}) for x in project.listChildren()]

@jsonp
def datasetDetail_json (request, did, server_id=None, _conn=None, **kwargs):
    """ return json encoded details for a dataset
    TODO: cache """
    blitzcon = _conn
    ds = blitzcon.getDataset(did)
    return ds.simpleMarshal()

@jsonp
def listProjects_json (request, server_id=None, _conn=None, **kwargs):
    """ lists all Projects, as json
    TODO: cache """
    blitzcon = _conn
    rv = []
    for pr in blitzcon.listProjects():
        rv.append( {'id': pr.id, 'name': pr.name, 'description': pr.description or ''} )
    return rv

@jsonp
def projectDetail_json (request, pid, server_id=None, _conn=None, **kwargs):
    """ grab details from one specific project
    TODO: cache """
    blitzcon = _conn
    pr = blitzcon.getProject(pid)
    rv = pr.simpleMarshal()
    return rv

def searchOptFromRequest (request):
    """
    @param ctx: (http request) 'imgs' to search only images
    @param text: (http request) the actual text phrase
    @param start: starting index (0 based) for result
    @param limit: nr of results to retuen (0 == unlimited)
    @param author: 
    @param grabData:
    @param parents:
    """
    try:
        r = request.REQUEST
        opts = {
            'search': unicode(r.get('text', '')).encode('utf8'),
            'ctx': r.get('ctx', ''),
            'grabData': not not r.get('grabData', False),
            'parents': not not bool(r.get('parents', False)),
            'start': int(r.get('start', 0)),
            'limit': int(r.get('limit', 0)),
            'key': r.get('key', None)
            }
        author = r.get('author', '')
        if author:
            opts['search'] += ' author:'+author
        return opts
    except:
        logger.error(traceback.format_exc())
        return {}

@TimeIt(logging.INFO)
@jsonp
def search_json (request, server_id=None, _conn=None, **kwargs):
    """
    Search for objects in blitz.

    @return: json encoded list of marshalled objects found by the search query
    TODO: cache
    """
    opts = searchOptFromRequest(request)
    rv = []
    logger.debug("simpleSearch(%s)" % (opts['search']))
    # search returns blitz_connector wrapper objects
    def urlprefix(iid):
        return reverse('webgateway.views.render_thumbnail', args=(iid,))
    xtra = {'thumbUrlPrefix': urlprefix}
    pks = None
    try:
        if opts['ctx'] == 'imgs':
            sr = _conn.searchImages(opts['search'])
        else:
            sr = _conn.simpleSearch(opts['search'])
    except ApiUsageException:
        return HttpResponseServerError('"parse exception"', mimetype='application/javascript')
    logger.debug(opts)
    def marshal ():
        rv = []
        if (opts['grabData'] and opts['ctx'] == 'imgs'):
            bottom = min(opts['start'], len(sr)-1)
            if opts['limit'] == 0:
                top = len(sr)
            else:
                top = min(len(sr), bottom + opts['limit'])
            for i in range(bottom, top):
                e = sr[i]
            #for e in sr:
                try:
                    rv.append(imageData_json(request, server_id, iid=e.id, key=opts['key'], _conn=_conn, _internal=True))
                except AttributeError, x:
                    logger.debug('(iid %i) ignoring Attribute Error: %s' % (e.id, str(x)))
                    pass
                except omero.ServerError, x:
                    logger.debug('(iid %i) ignoring Server Error: %s' % (e.id, str(x)))
            return rv
        else:
            return map(lambda x: x.simpleMarshal(xtra=xtra, parents=opts['parents']), sr)
    rv = timeit(marshal)()
    logger.debug(rv)
    return rv

def save_image_rdef_json (request, iid, server_id=None, **kwargs):
    """ Requests that the rendering defs passed in the request be set as the default for this image.
    TODO: jsonp """
    r = request.REQUEST

    pi = _get_prepared_image(request, iid, server_id=server_id, with_session=True, saveDefs=True)
    if pi is None:
        json_data = 'false'
    else:
        webgateway_cache.invalidateObject(server_id, pi[0])
        pi[0].getThumbnail()
        json_data = 'true'
    if r.get('callback', None):
        json_data = '%s(%s)' % (r['callback'], json_data)
    return HttpResponse(json_data, mimetype='application/javascript')
 
def list_compatible_imgs_json (request, server_id, iid, _conn=None, **kwargs):
    """ Lists the images on the same project that would be viable targets for copying rendering settings. """
    json_data = 'false'
    r = request.REQUEST
    if _conn is None:
        blitzcon = getBlitzConnection(request, server_id, with_session=True, useragent="OMERO.webgateway")
    else:
        blitzcon = _conn
    if blitzcon is None or not blitzcon.isConnected():
        img = None
    else:
        img = blitzcon.getImage(iid)

    if img is not None:
        # List all images in project
        imgs = []
        for ds in img.getProject().listChildren():
            imgs.extend(ds.listChildren())
        # Filter the ones that would pass the applySettingsToImages call
        img_ptype = img.getPrimaryPixels().getPixelsType().getValue()
        img_ccount = img.c_count()
        img_ew = [x.getEmissionWave() for x in img.getChannels()]
        img_ew.sort()
        def compat (i):
            if long(i.getId()) == long(iid):
                return False
            pp = i.getPrimaryPixels()
            if pp is None or \
               i.getPrimaryPixels().getPixelsType().getValue() != img_ptype or \
               i.c_count() != img_ccount:
                return False
            ew = [x.getEmissionWave() for x in i.getChannels()]
            ew.sort()
            if ew != img_ew:
                return False
            return True
        imgs = filter(compat, imgs)
        json_data = simplejson.dumps([x.getId() for x in imgs])

    if r.get('callback', None):
        json_data = '%s(%s)' % (r['callback'], json_data)
    return HttpResponse(json_data, mimetype='application/javascript')

def copy_image_rdef_json (request, server_id, _conn=None, **kwargs):
    """ Copy the rendering settings from one image to a list of images. """
    json_data = 'false'
    r = request.REQUEST
    try:
        fromid = long(r.get('fromid', None))
        toids = map(lambda x: long(x), r.getlist('toids'))
    except TypeError:
        fromid = None
    except ValueError:
        fromid = None
    if fromid is not None and len(toids) > 0:
        if _conn is None:
            blitzcon = getBlitzConnection(request, server_id, with_session=True, useragent="OMERO.webgateway")
        else:
            blitzcon = _conn

            
        fromimg = blitzcon.getImage(fromid)
        details = fromimg.getDetails()
        frompid = fromimg.getPixelsId()
        newConn = None
        if blitzcon.isAdmin():
            p = omero.sys.Principal()
            p.name = details.getOwner().omeName
            p.group = details.getGroup().name
            p.eventType = "User"
            # This connection will have a 20 minute timeout
            newConnId = blitzcon.getSessionService().createSessionWithTimeout(p, 1200000)
            newConn = blitzcon.clone()
            newConn.connect(sUuid=newConnId.getUuid().val)
        elif fromimg.isEditable():
            newConn = blitzcon
            newConn.setGroupForSession(details.getGroup().getId())

        if newConn is not None and newConn.isConnected():
            frompid = newConn.getImage(fromid).getPixelsId()
            rsettings = newConn.getRenderingSettingsService()
            json_data = rsettings.applySettingsToImages(frompid, list(toids))
            for iid in json_data[True]:
                img = newConn.getImage(iid)
                img is not None and webgateway_cache.invalidateObject(server_id, img)
            json_data = simplejson.dumps(json_data)

    if r.get('callback', None):
        json_data = '%s(%s)' % (r['callback'], json_data)
    return HttpResponse(json_data, mimetype='application/javascript')

def reset_image_rdef_json (request, server_id, iid, _conn=None, **kwargs):
    """ Try to remove all rendering defs the logged in user has for this image. """
    if _conn is None:
        blitzcon = getBlitzConnection(request, server_id, with_session=True, useragent="OMERO.webgateway")
    else:
        blitzcon = _conn
    r = request.REQUEST

    if blitzcon is None or not blitzcon.isConnected():
        img = None
    else:
        img = blitzcon.getImage(iid)

    if img is not None and img.resetRDefs():
        webgateway_cache.invalidateObject(server_id, img)
        json_data = 'true'
    else:
        json_data = 'false'
    if _conn is not None:
        return json_data == 'true'
    if r.get('callback', None):
        json_data = '%s(%s)' % (r['callback'], json_data)
    return HttpResponse(json_data, mimetype='application/javascript')

def dbg_connectors (request):
    rv = connectors.items()
    return HttpResponse(rv, mimetype='text/plain')

def full_viewer (request, iid, server_id=None, _conn=None, **kwargs):
    """ This view is responsible for showing the omero_image template """
    rid = getImgDetailsFromReq(request)
    try:
        if _conn is None:
            _conn = getBlitzConnection(request, server_id=server_id, useragent="OMERO.webgateway")
        if _conn is None or not _conn.isConnected():
            raise Http404
        image = _conn.getImage(iid)
        if image is None:
            logger.debug("(a)Image %s not found..." % (str(iid)))
            raise Http404
        d = {'blitzcon': _conn,
             'image': image,
             'opts': rid,
             'viewport_server': kwargs.get('viewport_server', '/webgateway'),
             'object': 'image:%i' % int(iid)}
        template = kwargs.get('template', "webgateway/omero_image.html")
        t = template_loader.get_template(template)
        c = Context(request,d)
        rsp = t.render(c)
    except omero.SecurityViolation:
        raise Http404
    return HttpResponse(rsp)

def test (request):
    context = {}

    t = template_loader.get_template('webgateway/omero_image.html')
    c = Context(request,context)
    return HttpResponse(t.render(c))

@jsonp
def su (request, user, server_id=None, _conn=None, **kwargs):
    if not _conn.canBeAdmin():
        return False
    _conn.setGroupNameForSession('system')
    if server_id is None:
        # If no server id is passed, the db entry will not be used and instead we'll depend on the
        # request.session and request.REQUEST values
        try:
            server_id = request.session['server']
        except KeyError:
            return None
    browsersession_connection_key = 'cuuid#%s'%server_id
    c = _conn.suConn(user,
                     ttl=_conn.getSessionService().getSession(_conn._sessionUuid).getTimeToIdle().val)
    _conn.revertGroupForSession()
    _conn.seppuku()
    logger.debug(browsersession_connection_key)
    request.session[browsersession_connection_key] = c._sessionUuid
    return True
