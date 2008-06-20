#
# blitzcon/views.py - django application view handling functions
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

from thread import start_new_thread

from django.template import Context, loader
from django.http import HttpResponse, HttpResponseRedirect, Http404
from django.shortcuts import render_to_response
from django.utils import simplejson
from django.core.cache import cache
from blitz_connector import BlitzConnector

from models import StoredConnection

#SIDE_CACHE = 5
BASE_CACHE_TIME = 60*60 # 1 hour
connectors = {}

import logging, os

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(levelname)s %(message)s',
                    filename='debug.log',
                    filemode='a')

logger = logging.getLogger()

logger.debug("INIT %s" % os.getpid())


class UserAgent (object):
    def __init__ (self, request):
        self.ua = request.META['HTTP_USER_AGENT']

    def isIE (self):
        return 'MSIE' in self.ua

    def isFF (self):
        return 'Firefox' in self.ua

    def isSafari (self):
        return 'Safari' in self.ua

from time import time
def timeit (func):
    def wrapped (*args, **kwargs):
        logger.debug("timing %s" % (func.func_name))
        now = time()
        rv = func(*args, **kwargs)
        logger.debug("timed %s: %f" % (func.func_name, time()-now))
        return rv
    return wrapped


@timeit
def getBlitzConnection (request, client_base, with_session=False, skip_connect=False):
    for k,v in connectors.items():
        if v is None:
            del connectors[k]
        elif k.startswith('S') and v.isTimedout():
            # We can't rely on the GC to kick on so..
            v.seppuku()
            del connectors[k]

    if len(connectors) > 75:
        for k,v in connectors.items()[50:]:
            v.seppuku()
            del connectors[k]

    r = request.REQUEST
    if with_session and request.session.session_key is not None:
        ckey = 'S:' + str(request.session.session_key) + '#' + str(client_base)
    else:
        #logger.debug("Connectors: %s" % (str(connectors)))
        ckey = 'C:' + str(client_base)
    logger.debug("(%i) Finding connection: %s" % (len(connectors), ckey))

    request.session.modified = True
    blitzcon = connectors.get(ckey, None)
    #print connectors
    if blitzcon is None:
        logger.debug("No connection: %s, %s" % (ckey, connectors.keys()))
        if r.has_key('login'):
            blitzcon = BlitzConnector(r['user'], r['pass'], r['server'], r['port'])
            blitzcon.template_base = 'blitzcon'
            request.session['weblitz_server'] = r['server']
            request.session['weblitz_port'] = r['port']
            request.session['weblitz_user'] = r['user']
            #print "Trying from form data"
        else:
            conns = StoredConnection.objects.filter(base_path__iexact=client_base, enabled__exact=True).order_by('failcount')
            for n, c in enumerate(conns):
                #print "Trying from StoreConnection #%i" % n
                blitzcon = c.getBlitzConnector()
                if ckey.startswith('C'):
                    blitzcon.allow_thread_timeout = False
                blitzcon.template_base = 'blitzcon'
                if not blitzcon.isConnected() and not blitzcon.connect():
                    logger.warning("failed connect to StoreConnection #%i" % n)
                    c.failcount += 1
                    c.save()
                    continue
                blitzcon.client_base = client_base
                if c.has_template:
                    blitzcon.template_base = 'blitzcon_%s' % c.base_path
                break
    if blitzcon is not None:
        if not blitzcon.isConnected() and not blitzcon.connect():
            connectors[ckey] = None
        else:
            connectors[ckey] = blitzcon
            #print "Have connection, will travel:%s" % str(ckey)
    print connectors
    return blitzcon

@timeit
def index (request, client_base, dsid=None, prid=None):
    """ This view is responsible for showing pixel data as images """
    ckey = ":dsviewer:%s#%s:%s" % (client_base, str(dsid), str(prid))
    rsp = cache.get(ckey)
    if rsp is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')

        if dsid:
            ds = blitzcon.getDataset(dsid)
        else:
            ds = None
        if prid:
            pr = blitzcon.getProject(prid)
        else:
            pr = None
        d = {'blitzcon': blitzcon,
             'dataset': ds,
             'project': pr,
             }
        t = loader.get_template('%s/index.html' % blitzcon.template_base)
        c = Context(d)
        rsp = t.render(c)
        cache.set(ckey, rsp, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(rsp)

@timeit
def test (request, client_base):
    """ This view is used for testing new features """
    blitzcon = getBlitzConnection(request, client_base)
    if blitzcon is None or not blitzcon.isConnected():
        return HttpResponseRedirect('/')

    d = {'blitzcon': blitzcon,
         }
    t = loader.select_template(('%s/test.html' % blitzcon.template_base, 'blitzcon/test.html'))
    c = Context(d)
    rsp = t.render(c)
    return HttpResponse(rsp)

@timeit
def page (request, client_base, page):
    """ View for mostly static pages, that need template parsing (for inheritance basicaly) nonetheless.
        Put your templates in templates/pages/{{page}}.html """
    ckey = ":page:%s#%s" % (client_base, page)
    rsp = cache.get(ckey)
    blitzcon = {'template_base': 'blitzcon', 'client_base': client_base }
    conns = StoredConnection.objects.filter(base_path__iexact=client_base, enabled__exact=True).order_by('failcount')
    for n, c in enumerate(conns):
        if c.has_template:
            blitzcon['template_base'] = 'blitzcon_%s' % c.base_path
    if rsp is None:
        t = loader.get_template('%s/pages/%s.html' % (blitzcon['template_base'], page))
        d = {'blitzcon': blitzcon,}
        c = Context(d)
        rsp = t.render(c)
        cache.set(ckey, rsp, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(rsp)

@timeit
def disconnect (request):
    session_key = request.session.session_key
    blitzcon = connectors.get(session_key, None)
    if blitzcon is None:
        return HttpResponseRedirect('/')
    if blitzcon.isConnected():
        connectors[session_key] = None
    return HttpResponseRedirect('/')

@timeit
def dataset_viewer (request, client_base, dsid, prid=None):
    """ This view is responsible for showing pixel data as images """
    ckey = ":dsviewer:%s#%s:%s" % (client_base, str(dsid), str(prid))
    rsp = cache.get(ckey)
    if rsp is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')

        ds = blitzcon.getDataset(dsid)
        if prid:
            pr = blitzcon.getProject(prid)
        else:
            pr = None
        d = {'blitzcon': blitzcon,
             'dataset': ds,
             'project': pr,
             'object': 'dataset:%i' % int(dsid),
             }
        t = loader.get_template('%s/omero_dataset.html' % blitzcon.template_base)
        c = Context(d)
        rsp = t.render(c)
        cache.set(ckey, rsp, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(rsp)
    #return render_to_response('blitzcon/omero_dataset.html', )

@timeit
def project_viewer (request, client_base, prid, dsid=None):
    """ Project level view """
    ckey = ":prviewer:%s#%s:%s" % (client_base, str(prid), str(dsid))
    rsp = cache.get(ckey)
    if rsp is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')

        pr = blitzcon.getProject(prid)
        if dsid:
            ds = blitzcon.getDataset(dsid)
        else:
            ds = None
        d = {'blitzcon': blitzcon,
             'dataset': ds,
             'project': pr,
             'object': 'project:%i' % int(prid),
             }
        t = loader.get_template('%s/omero_project.html' % blitzcon.template_base)
        c = Context(d)
        rsp = t.render(c)
        cache.set(ckey, rsp, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(rsp)

def _split_channel_info (rchannels):
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

def _get_img_details_from_req (request, as_string=False):
    """ Break the GET information from the request object into details on how to render the image.
    The following keys are recognized:
    z - Z axis position
    t - T axis position
    q - Quality set (0,0..1,0)
    m - Model (g for greyscale, c for color)
    x - X position (for now based on top/left offset on the browser window)
    y - Y position (same as above)
    c - a comma separated list of channels to be rendered (start index 1)
      - format for each entry [-]ID[|wndst:wndend][#HEXCOLOR][,...]
    zm - the zoom setting (as a percentual value)
    """
    r = request.REQUEST
    rv = {}
    for k in ('z', 't', 'q', 'm', 'zm', 'x', 'y'):
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
def image_viewer (request, client_base, iid, dsid=None):
    """ This view is responsible for showing the omero_image template """
    rid = _get_img_details_from_req(request)
    rk = "&".join(["%s=%s" % (x[0], x[1]) for x in rid.items()])
    ckey = ":imgviewer:%s+%s#%s:%s?%s" % (str(request.session.session_key), client_base, str(iid), str(dsid), rk)
    rsp = cache.get(ckey)
    user_agent = UserAgent(request)
    if rsp is None:
        blitzcon = getBlitzConnection(request, client_base, with_session=True)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')
        image = blitzcon.getImage(iid)
        if image is None:
            logger.debug("(a)Image %s not found..." % (str(iid)))
            raise Http404
        if dsid is not None:
            ds = blitzcon.getDataset(dsid)
        else:
            ds = None
        d = {'blitzcon': blitzcon,
             'image': image,
             'dataset': ds,
             'opts': rid,
             'user_agent': user_agent,
             'object': 'image:%i' % int(iid)}
        t = loader.get_template('%s/omero_image.html' % blitzcon.template_base)
        c = Context(d)
        rsp = timeit(t.render)(c)
        cache.set(ckey, rsp, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(rsp)
    #return render_to_response('blitzcon/omero_image.html', d)

@timeit
def render_thumbnail (request, client_base, iid):
    ckey = ":thumb:%s#%s" % (client_base, str(iid))
    jpeg_data = cache.get(ckey)
    if jpeg_data is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            raise Http404
        img = blitzcon.getImage(iid)
        if img is None:
            logger.debug("(b)Image %s not found..." % (str(iid)))
            raise Http404
        jpeg_data = img.getThumbnail()
        if jpeg_data is None:
            logger.debug("(c)Image %s not found..." % (str(iid)))
            raise Http404
        cache.set(ckey, jpeg_data, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

def _get_prepared_image (request, client_base, iid):
    r = request.REQUEST
    blitzcon = getBlitzConnection(request, client_base, with_session=True)
    if blitzcon is None or not blitzcon.isConnected():
        return None
    img = blitzcon.getImage(iid)
    if r.has_key('c'):
        logger.debug("c="+r['c'])
        channels, windows, colors =  _split_channel_info(r['c'])
        if not img.setActiveChannels(channels, windows, colors):
            logger.debug("Something bad happened while setting the active channels...")
    if r.get('m', None) == 'g':
        img.setGreyscaleRenderingModel()
    elif r.get('m', None) == 'c':
        img.setColorRenderingModel()
    compress_quality = r.get('q', None)
    return (img, compress_quality)

@timeit
def render_image (request, client_base, iid, z, t):
    """ Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong """
    r = request.REQUEST
    base_ckey = ":img:%s+%s#%s@%%sx%%s?c=%s&g=%s&q=%s" % (str(request.session.session_key), client_base, str(iid),
                                                          r.get('c', ''), r.get('m', 'c'), r.get('q', ''))
    ckey = base_ckey % (str(z), str(t))
    jpeg_data = cache.get(ckey)
    if jpeg_data is None:
        pi = _get_prepared_image(request, client_base, iid)
        if pi is None:
            raise Http404
        img, compress_quality = pi
        jpeg_data = img.renderJpeg(z,t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        cache.set(ckey, jpeg_data, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

def imageData_json (request, client_base, iid):
    """ Get a dict with image information """
    r = request.REQUEST
    ckey = ":imgdata:%s#%s" % (client_base, str(iid))

    json_data = cache.get(ckey)
    if json_data is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')
        image = blitzcon.getImage(iid)
        if image is None:
            return HttpResponseRedirect('/')
        rv = {
            'id': iid,
            'size': {'width': image.getWidth(),
                     'height': image.getHeight(),
                     'z': image.z_count(),
                     't': image.t_count(),
                     'c': image.c_count(),},
            'pixel_size': {'x': image.getPixelSizeX(),
                           'y': image.getPixelSizeY(),
                           'z': image.getPixelSizeZ(),},
            'rdefs': {'model': image.isGreyscaleRenderingModel() and 'greyscale' or 'color',
                      },
            'channels': map(lambda x: {'emissionWave': x.getEmissionWave(),
                                       'color': x.getColor().getHtml(),
                                       'window': {'min': x.getWindowMin(),
                                                  'max': x.getWindowMax(),
                                                  'start': x.getWindowStart(),
                                                  'end': x.getWindowEnd(),},
                                       'active': x.isActive()}, image.getChannels()),
            'meta': {'name': image.name or '',
                     'description': image.description or '',
                     'author': image.getAuthor(),
                     'publication': image.getPublication(),
                     'publication_id': image.getPublicationId(),
                     'timestamp': image.getDate(),},
            }
        json_data = simplejson.dumps(rv)
        cache.set(ckey, json_data, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(json_data, mimetype='application/javascript')

@timeit
def listImages_json (request, client_base, did):
    """ lists all Images in a Dataset, as json """
    r = request.REQUEST
    ckey = ":listis:%s#%s" % (client_base, str(did))
    json_data = cache.get(ckey)
    if json_data is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')
        dataset = blitzcon.getDataset(did)
        if dataset is None:
            return HttpResponseRedirect('/')
        rv = []
        for im in dataset.listChildren():
            rv.append( {'id': im.id,
                        'shortname': im.shortname(),
                        'author': im.getAuthor(),
                        'date': im.getDate(),
                        'description': im.description or '',
                        'thumb_url': 'http://'+request.get_host()+'/'+client_base+'/render_thumbnail/'+str(im.id),}, )
        json_data = simplejson.dumps(rv)
        cache.set(ckey, json_data, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(json_data, mimetype='application/javascript')

@timeit
def listDatasets_json (request, client_base, pid):
    """ lists all Datasets in a Project, as json """
    r = request.REQUEST
    ckey = ":listds:%s#%s" % (client_base, str(pid))

    json_data = cache.get(ckey)
    if json_data is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')
        project = blitzcon.getProject(pid)
        rv = []
        if project is None:
            return HttpResponse('[]', mimetype='application/javascript')
        for ds in project.listChildren():
            rv.append( {'id': ds.id, 'name': ds.name, 'description': ds.description or ''} )
        json_data = simplejson.dumps(rv)
        cache.set(ckey, json_data, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(json_data, mimetype='application/javascript')

@timeit
def datasetDetail_json (request, client_base, did):
    """ grab details from one specific project """
    r = request.REQUEST
    ckey = ":dsdetail:%s" % (client_base)

    json_data = cache.get(ckey)
    if json_data is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')
        ds = blitzcon.getDataset(did)
        rv = {'id': ds.id, 'name': ds.name, 'description': ds.description or ''}
        json_data = simplejson.dumps(rv)
        cache.set(ckey, json_data, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(json_data, mimetype='application/javascript')

@timeit
def listProjects_json (request, client_base):
    """ lists all Projects, as json """
    r = request.REQUEST
    ckey = ":listpr:%s" % (client_base)

    json_data = cache.get(ckey)
    if json_data is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')
        rv = []
        for pr in blitzcon.listProjects():
            rv.append( {'id': pr.id, 'name': pr.name, 'description': pr.description or ''} )
        json_data = simplejson.dumps(rv)
        cache.set(ckey, json_data, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(json_data, mimetype='application/javascript')

@timeit
def projectDetail_json (request, client_base, pid):
    """ grab details from one specific project """
    r = request.REQUEST
    ckey = ":prdetail:%s" % (client_base)

    json_data = cache.get(ckey)
    if json_data is None:
        blitzcon = getBlitzConnection(request, client_base)
        if blitzcon is None or not blitzcon.isConnected():
            return HttpResponseRedirect('/')
        pr = blitzcon.getProject(pid)
        rv = {'id': pr.id, 'name': pr.name, 'description': pr.description or ''}
        json_data = simplejson.dumps(rv)
        cache.set(ckey, json_data, BASE_CACHE_TIME)
        logger.debug("cache miss %s" % ckey)
    else:
        logger.debug("cache hit %s" % ckey)
    return HttpResponse(json_data, mimetype='application/javascript')

@timeit
def search_json (request, client_base):
    """ """
    logger.debug("search request: %s" % (str(request.REQUEST.items())))
    blitzcon = getBlitzConnection(request, client_base)
    if blitzcon is None or not blitzcon.isConnected():
        return HttpResponseRedirect('/')
    r = request.REQUEST
    search = r.get('text', '')
    author = r.get('author', '')
    if author:
        search += ' author:'+author
    rv = []
    logger.debug("simpleSearch(%s)" % (search))
    # search returns blitz_connector wrapper objects
    for wrapper in blitzcon.simpleSearch(search):
        rv.append({'type': wrapper.OMERO_CLASS,
                   'id':wrapper.id,
                   'description': wrapper.description,
                   'parents': map(lambda x: x.id, wrapper.listParents()),})
    json_data = simplejson.dumps(rv)
    return HttpResponse(json_data, mimetype='application/javascript')

@timeit
def listAuthors_lflist (request, client_base):
    """ Returns a linefeed separated list of Authors (Experimenters) for usage in selectors.
    if the request property 'q' is passed, it will be used as a start string filter for author name."""
    blitzcon = getBlitzConnection(request, client_base)
    if blitzcon is None or not blitzcon.isConnected():
        return HttpResponseRedirect('/')
    r = request.REQUEST
    logger.debug("listAuthors(%s)" % (r.get('q', '')))
    rv = '\n'.join(map(lambda x: x.omeName, blitzcon.listExperimenters(r.get('q', ''))))
    return HttpResponse(rv, mimetype='text/plain')
