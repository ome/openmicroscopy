from django.http import HttpResponseRedirect
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from webadmin.models import Gateway

import logging
import traceback

logger = logging.getLogger('webtest')

def login (request):
    if request.method == 'POST' and request.REQUEST['server']:
        blitz = Gateway.objects.get(pk=request.REQUEST['server'])
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
    
    conn = getBlitzConnection (request)
    logger.debug(conn)
    if conn is not None:
        return HttpResponseRedirect(reverse('webtest_index'))
    return render_to_response('webtest/login.html', {'gw':Gateway})

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
    return HttpResponseRedirect(reverse('webtest_login'))

def index (request):
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webtest_login'))

    return render_to_response('webtest/index.html', {'client': conn})
