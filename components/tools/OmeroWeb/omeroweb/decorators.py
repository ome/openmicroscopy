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

from django.http import Http404


logger = logging.getLogger(__name__)

class login_required(object):
    """
    OMERO.web specific extension of the Django login_required() decorator,
    https://docs.djangoproject.com/en/dev/topics/auth/, which is responsible
    for ensuring a valid L{omero.gateway.BlitzGateway} connection. Is
    configurable by various options.
    """

    def __init__(self, useragent='OMERO.web', isAdmin=False,
                 isGroupOwner=False):
        """
        Initialises the decorator.
        """
        self.useragent = useragent
        self.isAdmin = isAdmin
        self.isGroupOwner = isGroupOwner

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

    def on_not_logged_in(self, request, url, error=None):
        """Called whenever the user is not logged in."""
        raise Http404

    def on_logged_in(self, request):
        """Called whenever the users is successfully logged in."""
        pass

    def on_share_connection_prepared(self, request):
        """Called whenever a share connection is successfully prepared."""
        pass

    def verify_is_admin(self, conn):
        """
        If we have been requested to by the isAdmin flag, verify the user
        is an admin and raise an exception if they are not.
        """
        if self.isAdmin and not conn.isAdmin():
            raise Http404

    def verify_is_group_owner(self, conn, gid):
        """
        If we have been requested to by the isGroupOwner flag, verify the user
        is the owner of the provided group. If no group is provided the user's
        active session group ownership will be verified.
        """
        if not self.isGroupOwner:
            return
        if gid is not None:
            if not conn.isOwner(gid):
                raise Http404
        else:
            if not conn.isOwner():
                raise Http404

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
            error = None
            try:
                # Lazy import due to the potential usage of the decorator in
                # the omeroweb.webgateway.views package.
                from omeroweb.webgateway.views import getBlitzConnection
                conn = getBlitzConnection(request, useragent=ctx.useragent)
            except Exception, x:
                logger.error('Error retrieving connection.', exc_info=True)
                error = str(x)
            
            if conn is None:
                return ctx.on_not_logged_in(request, url, error)
            else:
                ctx.on_logged_in(request)
            ctx.verify_is_admin(conn)
            ctx.verify_is_group_owner(conn, kwargs.get('gid'))

            share_id = kwargs.get('share_id')
            conn_share = ctx.prepare_share_connection(request, share_id)
            ctx.on_share_connection_prepared(request)
            kwargs['error'] = request.REQUEST.get('error')
            kwargs['conn'] = conn
            kwargs['conn_share'] = conn_share
            kwargs['url'] = url
            return f(request, *args, **kwargs)
        return wrapped

