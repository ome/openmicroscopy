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
from django.conf import settings

from omeroweb.connector import Connector

logger = logging.getLogger(__name__)

class Http403(Exception):
    pass

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

    def get_connection(self, server_id, request, useragent):
        """
        Prepares a Blitz connection wrapper (from L{omero.gateway}) for
        use with a view function.
        """
        # TODO: Handle previous try_super logic; is it still needed?

        session = request.session
        request = request.REQUEST
        if server_id is None:
            # If no server id is passed, the db entry will not be used and
            # instead we'll depend on the request.session and request.REQUEST
            # values
            server_id = session.get('server', None)
            if server_id is None:
                return None
    
        connector = session.get('connector', None)
        logger.debug('Django session connector: %r' % connector)
        if connector is not None:
            # We have a connector, attempt to use it to join an existing
            # connection / OMERO session.
            return connector.join_connection()

        # We have no current connector, create one and attempt to
        # create a connection based on the credentials we have available
        # in the current request.
        is_secure = request.get('ssl', False)
        logger.debug('Is SSL? %s' % is_secure)
        connector = Connector(server_id, is_secure, useragent)
        try:
            omero_session_key = request.get('bsession')
        except KeyError:
            # We do not have an OMERO session key in the current request.
            pass
        else:
            # We have an OMERO session key in the current request use it
            # to try join an existing connection / OMERO session.
            logger.debug('Have OMERO session key %s, attempting to join...' % \
                    omero_session_key)
            connector.omero_session_key = omero_session_key
            connection = connector.join_connection()
            session['connector'] = connector
            connection.user.logIn()
            return connection

        # An OMERO session is not available, we're either trying to service
        # a request to a login page or an anonymous request.
        username = None
        password = None
        try:
            username = request.get('username')
            password = request.get('password')
        except KeyError:
            if not settings.PUBLIC_ENABLED:
                logger.debug('No username or password in request, raising ' \
                             'HTTP 403')
                # We do not have an OMERO session or a username and password
                # in the current request, raise an error.
                raise Http403
            # If OMERO.webpublic is enabled, pick up a username and
            # password from configuration.
            logger.debug('OMERO.webpublic enabled, attempting to login with ' \
                         'configuration supplied credentials.')
            connector = Connector(server_id, is_secure, useragent)
            username = settings.PUBLIC_USER
            password = settings.PUBLIC_PASSWORD
        # We have a username and password in the current request, or
        # OMERO.webpublic is enabled and has provided us with a username
        # and password via configureation. Use them to try and create a
        # new connection / OMERO session.
        connection = connector.create_connection(username, password)
        session['connector'] = connector
        connection.user.logIn()
        return connector.create_connection(username, password)

    def __call__(ctx, f):
        """
        Tries to prepare a logged in connection, then calls function and
        returns the result.
        """
        def wrapped(request, *args, **kwargs):
            url = request.REQUEST.get('url')
            if url is None or len(url) == 0:
                url = request.get_full_path()

            conn = kwargs.get('_conn', None)
            error = None
            server_id = kwargs.get('server_id', None)
            # Short circuit connection retrieval when a connection was
            # provided to us via '_conn'. This is useful when in testing
            # mode or when stacking view functions/methods.
            if conn is None:
                logger.debug('Connection not provided, attempting to get one.')
                try:
                    conn = ctx.get_connection(
                            server_id, request, useragent=ctx.useragent)
                except Http403:
                    # An authentication error should go all the way up the
                    # stack.
                    raise
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

