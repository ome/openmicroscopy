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
Decorators for use with the webclient application.
"""

import logging

import omeroweb.decorators

from django.http import HttpResponseServerError, Http404
from django.utils.http import urlencode
from django.core.urlresolvers import reverse

from omeroweb.webgateway import views as webgateway_views
from omeroweb.webadmin.custom_models import Server
from omeroweb.webclient.webclient_http import HttpLoginRedirect
from omeroweb.webclient.webclient_utils import string_to_dict

logger = logging.getLogger('omeroweb.webclient.decorators')

class login_required(omeroweb.decorators.login_required):
    """
    webclient specific extension of the OMERO.web login_required() decorator.
    """

    def __init__(self, useragent='OMERO.web', isAdmin=False,
                 isGroupOwner=False):
        """
        Initialises the decorator.
        """
        super(login_required, self).__init__(useragent, isAdmin, isGroupOwner)

    def get_login_url(self):
        """The URL that should be redirected to if not logged in."""
        return reverse('weblogin')
    login_url = property(get_login_url)

    def on_share_connection_prepared(self, request):
        """Called whenever a share connection is successfully prepared."""
        super(login_required, self).on_share_connection_prepared(request)
        self.prepare_session(request)

    def on_not_logged_in(self, request, url, error=None):
        """Called whenever the user is not logged in."""
        path = string_to_dict(request.REQUEST.get('path'))
        server_id = request.REQUEST.get('server')
        server = path.get('server', server_id)
        if request.is_ajax():
            return HttpResponseServerError(self.login_url)
        self.cleanup_session(request, server_id)
        args = {'url': url}
        if server is not None:
            args['server'] = server
        if error is not None:
            args['error'] = error
        return HttpLoginRedirect('%s?%s' % (self.login_url, urlencode(args)))

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

