from django.http import HttpResponseRedirect
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views
import settings
import logging
import traceback
import omero

logger = logging.getLogger('webmobilewebmobile')


def viewer(request, imageId):
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webmobile_login'))
        
    image = conn.getImage(imageId)
    w = image.getWidth()
    h = image.getHeight()
    
    return render_to_response('webmobile/viewer.html', {'image': image})


def dataset(request, id):
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webmobile_login'))
        
    ds = conn.getDataset(id)
    return render_to_response('webmobile/dataset.html', {'ds': ds})


def login (request):
    if request.method == 'POST' and request.REQUEST['server']:
        blitz = settings.SERVER_LIST.get(pk=request.REQUEST['server'])
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
    
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    logger.debug(conn)
    if conn is not None:
        return HttpResponseRedirect(reverse('webmobile_index'))
    return render_to_response('webmobile/login.html', {'gw':settings.SERVER_LIST})
    

def logout (request):
    _session_logout(request, request.session['server'])
    try:
        del request.session['username']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['password']
    except KeyError:
        logger.error(traceback.format_exc())

    #request.session.set_expiry(1)
    return HttpResponseRedirect(reverse('webmobile_login'))


def index (request):
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webmobile_login'))

    return render_to_response('webmobile/index.html', {'client': conn})


def image_viewer (request, iid, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webmobile_login'))
    
    kwargs['viewport_server'] = '/webclient'
    
    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)