#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

"""
Decorators for use with OMERO.web applications.
"""

import logging

from django.http import HttpResponseServerError
from django.core.urlresolvers import reverse

from omeroweb.webclient.webclient_utils import string_to_dict
from omeroweb.webclient.webclient_http import HttpJavascriptRedirect, \
                                              HttpJavascriptResponse, \
                                              HttpLoginRedirect
from omeroweb.webgateway.views import getBlitzConnection
from omeroweb.webgateway import views as webgateway_views
from omeroweb.webadmin.custom_models import Server

logger = logging.getLogger('omeroweb.decorators')

class login_required(object):
    """
    OMERO.web specific extension of the Django login_required() decorator,
    https://docs.djangoproject.com/en/dev/topics/auth/, which is responsible
    for ensuring a valid L{omero.gateway.BlitzGateway} connection. Is
    configurable by various options.
    """

    def __init__(self):
        """
        Initialises the decorator.
        """
        pass

    def prepare_session(self, request):
        """Prepares various session variables."""
        changes = False
        if request.session.get('callback') is None:
            request.session['callback'] = dict()
            changes = True
        if request.session.get('shares') is None:
            request.session['shares'] = dict()
            changes = True
        if request.session.get('imageInBasket') is None:
            request.session['imageInBasket'] = set()
            changes = True
        if request.session.get('nav') is None:
            if request.session.get('server') is not None:
                blitz = Server.get(pk=request.session.get('server'))
            elif request.session.get('host') is not None:
                blitz = Server.get(host=request.session.get('host'))
            blitz = '%s:%s' % (blitz.host, blitz.port)
            request.session['nav'] = {'blitz': blitz, 'menu': 'mydata',
                    'view': 'tree', 'basket': 0,'experimenter': None}
            changes = True
        if changes:
            request.session.modified = True

    def cleanup_session(self, request, server_id):
        """
        Cleans up session variables and performs L{omero.gateway.BlitzGateway}
        logout semantics.
        """
        webgateway_views._session_logout(request, server_id)
        try:
            for key in request.session.get('shares', list()):
                session_key = 'S:%s#%s#%s' % \
                        (request.session.session_key,server_id, key)
                webgateway_views._session_logout(
                        request,server_id, force_key=session_key)
            for k in request.session:
                try:
                    del request.session[k]      
                except KeyError:
                    pass
        except:
            logger.error('Error performing session logout.', exc_info=True)

    def prepare_share_connection(self, request, share_id):
        """Prepares the share connection if we have a valid share ID."""
        if share_id is None:
            return None
        share = conn.getShare(share_id)
        if share is None:
            return None
        try:
            if share.getOwner().id != conn.getEventContext().userId:
                return getShareConnection(request, share_id)
        except:
            logger.error('Error retrieving share connection.', exc_info=True)
            return None

    def get_login_url(self):
        """The URL that should be redirected to if not logged in."""
        return reverse('weblogin')
    login_url = property(get_login_url)

    def on_not_logged_in(self, request, url):
        """Called whenever the user is not logged in."""
        path = string_to_dict(request.REQUEST.get('path'))
        server = path.get('server', request.REQUEST.get('server'))
        if request.is_ajax():
            return HttpResponseServerError(self.login_url)
        self.cleanup_session(request, request.REQUEST.get('server'))
        if server is not None:
            return HttpLoginRedirect('%s?url=%s&server=%s' % \
                    (self.login_url, url, server))
        return HttpLoginRedirect('%s?url=%s' % (self.login_url, url))

    def __call__(ctx, f):
        """
        Tries to prepare a logged in connection , then calls function and
        returns the result.
        """
        def wrapped(request, *args, **kwargs):
            url = request.REQUEST.get('url')
            if url is None or len(url) == 0:
                url = request.get_full_path()

            conn = None
            try:
                conn = getBlitzConnection(request, useragent='OMERO.web')
            except:
                logger.error('Error retrieving connection.', exc_info=True)
            
            if conn is None:
                return ctx.on_not_logged_in(request, url)

            share_id = kwargs.get('share_id')
            conn_share = ctx.prepare_share_connection(request, share_id)
            ctx.prepare_session(request)
            kwargs['error'] = request.REQUEST.get('error')
            kwargs['conn'] = conn
            kwargs['conn_share'] = conn_share
            kwargs['url'] = url
            return f(request, *args, **kwargs)
        return wrapped

