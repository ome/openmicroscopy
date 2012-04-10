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
from django.conf import settings
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

    def on_logged_in(self, request, conn):
        """Called whenever the users is successfully logged in."""
        super(login_required, self).on_logged_in(request, conn)
        self.prepare_session(request)

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
            connector = request.session['connector']
            blitz = '%s:%s' % connector.lookup_host_and_port()
            request.session['nav'] = {'blitz': blitz, 'menu': 'mydata',
                    'view': 'tree', 'basket': 0,'experimenter': None}
            changes = True
        if changes:
            request.session.modified = True


class render_response(omeroweb.decorators.render_response):
    """ Subclass for adding additional data to the 'context' dict passed to templates """

    def prepare_context(self, request, context, *args, **kwargs):
        """
        This allows templates to access the current eventContext and user from the L{omero.gateway.BlitzGateway}.
        If these are not required by the template, then they will not need to be loaded by the Blitz Gateway.
        The results are cached by Blitz Gateway, so repeated calls have no additional cost.
        We also process some values from settings and add these to the context.
        """

        # we expect @login_required to pass us 'conn', but just in case...
        if 'conn' not in kwargs:
            return
        conn = kwargs['conn']

        context['ome'] = {}
        context['ome']['eventContext'] = conn.getEventContext
        context['ome']['user'] = conn.getUser
        context['ome']['request_error'] = request.REQUEST.get('error')

        self.load_settings(request, context, conn)

        if 'nav' not in context:
            context['nav'] = {}
        context['nav'] = request.session['nav']     # only needed for nav.basket
        # used for pagination
        context['nav']['view'] = request.REQUEST.get('view')


    def load_settings(self, request, context, conn):
        # This is the new navHelper function...

        top_links = settings.TOP_LINKS
        links = []
        for tl in top_links:
            try:
                label = tl[0]
                link_id = tl[1]
                link = reverse(link_id)
                links.append( {"label":label, "link":link} )
            except:
                logger.error("Failed to reverse() tab_link: %s" % tl)
        context['ome']['top_links'] = links
