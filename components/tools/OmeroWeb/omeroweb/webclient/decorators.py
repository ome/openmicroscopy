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

from django.http import HttpResponse
from django.conf import settings
from django.core.urlresolvers import reverse

from omeroweb.webclient.forms import GlobalSearchForm

logger = logging.getLogger('omeroweb.webclient.decorators')


class login_required(omeroweb.decorators.login_required):
    """
    webclient specific extension of the OMERO.web login_required() decorator.
    """

    def __init__(self, ignore_login_fail=False, setGroupContext=False,
                 login_redirect=None, **kwargs):
        """
        Initialises the decorator.
        """
        super(login_required, self).__init__(**kwargs)
        self.ignore_login_fail = ignore_login_fail
        self.setGroupContext = setGroupContext
        self.login_redirect = login_redirect

    def on_logged_in(self, request, conn):
        """Called whenever the users is successfully logged in."""
        super(login_required, self).on_logged_in(request, conn)
        self.prepare_session(request)
        if self.setGroupContext:
            if request.session.get('active_group'):
                conn.SERVICE_OPTS.setOmeroGroup(
                    request.session.get('active_group'))
            else:
                conn.SERVICE_OPTS.setOmeroGroup(
                    conn.getEventContext().groupId)

    def on_not_logged_in(self, request, url, error=None):
        """
        This can be used to fail silently (not return 403, 500 etc. E.g.
        keepalive ping)
        """
        if self.ignore_login_fail:
            return HttpResponse("Connection Failed")
        if self.login_redirect is not None:
            try:
                url = reverse(self.login_redirect)
            except:
                pass
        return super(
            login_required, self).on_not_logged_in(request, url, error)

    def prepare_session(self, request):
        """Prepares various session variables."""
        changes = False
        if request.session.get('callback') is None:
            request.session['callback'] = dict()
            changes = True
        if request.session.get('shares') is None:
            request.session['shares'] = dict()
            changes = True
        if changes:
            request.session.modified = True


class render_response(omeroweb.decorators.render_response):
    """
    Subclass for adding additional data to the 'context' dict passed to
    templates
    """

    def prepare_context(self, request, context, *args, **kwargs):
        """
        This allows templates to access the current eventContext and user from
        the L{omero.gateway.BlitzGateway}.
        E.g. <h1>{{ ome.user.getFullName }}</h1>
        If these are not required by the template, then they will not need to
        be loaded by the Blitz Gateway.
        The results are cached by Blitz Gateway, so repeated calls have no
        additional cost.
        We also process some values from settings and add these to the
        context.
        """

        # we expect @login_required to pass us 'conn', but just in case...
        if 'conn' not in kwargs:
            return
        conn = kwargs['conn']

        context.setdefault('ome', {})   # don't overwrite existing ome
        context['ome']['eventContext'] = conn.getEventContext
        context['ome']['user'] = conn.getUser
        context['ome']['user_id'] = request.session.get('user_id', None)
        context['ome']['group_id'] = request.session.get('group_id', None)
        context['ome']['active_group'] = request.session.get(
            'active_group', conn.getEventContext().groupId)
        context['global_search_form'] = GlobalSearchForm()
        # UI server preferences
        if request.session.get('server_settings'):
            context['ome']['email'] = request.session.get(
                'server_settings').get('email', False)
            if request.session.get('server_settings').get('ui'):
                context.setdefault('ui', {})  # don't overwrite existing ui
                context['ui']['orphans_name'] = request.session.get(
                    'server_settings').get('ui').get('orphans_name')
                context['ui']['orphans_desc'] = request.session.get(
                    'server_settings').get('ui').get('orphans_desc')
                context['ui']['dropdown_menu'] = request.session.get(
                    'server_settings').get('ui').get('dropdown_menu')

        self.load_settings(request, context, conn)

    def load_settings(self, request, context, conn):

        # Process various settings and add to the template context dict
        ping_interval = settings.PING_INTERVAL
        if ping_interval > 0:
            context['ping_interval'] = ping_interval

        top_links = settings.TOP_LINKS
        links = []
        for tl in top_links:
            if len(tl) < 2:
                continue
            l = {}
            l["label"] = tl[0]
            link_id = tl[1]
            try:
                l["link"] = reverse(link_id)
            except:
                # assume we've been passed a url
                l["link"] = link_id
            # simply add optional attrs dict
            if len(tl) > 2:
                l['attrs'] = tl[2]
            links.append(l)
        context['ome']['top_links'] = links

        right_plugins = settings.RIGHT_PLUGINS
        r_plugins = []
        for rt in right_plugins:
            label = rt[0]
            include = rt[1]
            plugin_id = rt[2]
            r_plugins.append({
                "label": label, "include": include, "plugin_id": plugin_id})
        context['ome']['right_plugins'] = r_plugins

        center_plugins = settings.CENTER_PLUGINS
        c_plugins = []
        for cp in center_plugins:
            label = cp[0]
            include = cp[1]
            plugin_id = cp[2]
            c_plugins.append({
                "label": label, "include": include, "plugin_id": plugin_id})
        context['ome']['center_plugins'] = c_plugins
