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

from django.http import Http404, HttpResponse, HttpResponseRedirect, HttpResponseForbidden

from django.conf import settings
from django.utils.http import urlencode
from django.utils.functional import wraps
from django.utils import simplejson
from django.core.urlresolvers import reverse
from django.core import template_loader
from django.template import RequestContext
from django.core.cache import cache

from omeroweb.connector import Connector

logger = logging.getLogger(__name__)

class ConnCleaningHttpResponse(HttpResponse):
    """Extension of L{HttpResponse} which closes the OMERO connection."""

    def close(self):
        super(ConnCleaningHttpResponse, self).close()
        try:
            logger.debug('Closing OMERO connection in %r' % self)
            if self.conn is not None and self.conn.c is not None:
                for v in self.conn._proxies.values():
                    v.close()
                self.conn.c.closeSession()
        except:
            logger.error('Failed to clean up connection.', exc_info=True)

class login_required(object):
    """
    OMERO.web specific extension of the Django login_required() decorator,
    https://docs.djangoproject.com/en/dev/topics/auth/, which is responsible
    for ensuring a valid L{omero.gateway.BlitzGateway} connection. Is
    configurable by various options.
    """

    def __init__(self, useragent='OMERO.web', isAdmin=False,
                 isGroupOwner=False, doConnectionCleanup=True, omero_group='-1',
                 allowPublic=None):
        """
        Initialises the decorator.
        """
        self.useragent = useragent
        self.isAdmin = isAdmin
        self.isGroupOwner = isGroupOwner
        self.doConnectionCleanup = doConnectionCleanup
        self.omero_group = omero_group
        self.allowPublic = allowPublic

    def get_login_url(self):
        """The URL that should be redirected to if not logged in."""
        return reverse(settings.LOGIN_VIEW)
    login_url = property(get_login_url)

    def get_share_connection (self, request, conn, share_id):
        try:
            conn.SERVICE_OPTS.setOmeroShare(share_id)
            share = conn.getShare(share_id)
            return conn
        except:
            logger.error('Error activating share.', exc_info=True)
            return None
    
    def prepare_share_connection(self, request, conn, share_id):
        """Prepares the share connection if we have a valid share ID."""
        # we always need to clear any dirty 'omero.share' values from previous calls
        conn.SERVICE_OPTS.setOmeroShare()
        if share_id is None:
            return None
        share = conn.getShare(share_id)
        try:
            if share.getOwner().id != conn.getEventContext().userId:
                return self.get_share_connection(request, conn, share_id)
        except:
            logger.error('Error retrieving share connection.', exc_info=True)
            return None

    def on_not_logged_in(self, request, url, error=None):
        """Called whenever the user is not logged in."""
        if request.is_ajax():
            logger.debug('Request is Ajax, returning HTTP 403.')
            return HttpResponseForbidden()
        args = {'url': url}
        logger.debug('Request is not Ajax, redirecting to %s' % self.login_url)
        return HttpResponseRedirect('%s?%s' % (self.login_url, urlencode(args)))

    def on_logged_in(self, request, conn):
        """
        Called whenever the users is successfully logged in.
        Sets the 'omero.group' option if specified in the constructor
        """
        if self.omero_group is not None:
            conn.SERVICE_OPTS.setOmeroGroup(self.omero_group)
    
    def on_share_connection_prepared(self, request, conn_share):
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
            if not conn.isLeader(gid):
                raise Http404
        else:
            if not conn.isLeader():
                raise Http404

    def is_valid_public_url(self, server_id, request):
        """
        Verifies that the URL for the resource being requested falls within
        the scope of the OMERO.webpublic URL filter.
        """
        if settings.PUBLIC_ENABLED:
            if not hasattr(settings, 'PUBLIC_USER'):
                logger.warn('OMERO.webpublic enabled but public user ' \
                            '(omero.web.public.user) not set, disabling ' \
                            'OMERO.webpublic.')
                settings.PUBLIC_ENABLED = False
                return False
            if not hasattr(settings, 'PUBLIC_PASSWORD'):
                logger.warn('OMERO.webpublic enabled but public user ' \
                            'password (omero.web.public.password) not set, ' \
                            'disabling OMERO.webpublic.')
                settings.PUBLIC_ENABLED = False
                return False
            if self.allowPublic is None:
                return settings.PUBLIC_URL_FILTER.search(request.path) is not None
            return self.allowPublic
        return False

    def get_public_user_connector(self):
        """
        Returns the current cached OMERO.webpublic connector or None if
        nothing has been cached.
        """
        if not settings.PUBLIC_CACHE_ENABLED:
            return
        return cache.get(settings.PUBLIC_CACHE_KEY)

    def set_public_user_connector(self, connector):
        """Sets the current cached OMERO.webpublic connector."""
        if not settings.PUBLIC_CACHE_ENABLED \
                or connector.omero_session_key is None:
            return
        logger.debug('Setting OMERO.webpublic connector: %r' % connector)
        cache.set(settings.PUBLIC_CACHE_KEY, connector,
                  settings.PUBLIC_CACHE_TIMEOUT)

    def get_connection(self, server_id, request):
        """
        Prepares a Blitz connection wrapper (from L{omero.gateway}) for
        use with a view function.
        """
        connection = self.get_authenticated_connection(server_id, request)
        is_valid_public_url = self.is_valid_public_url(server_id, request)
        logger.debug('Is valid public URL? %s' % is_valid_public_url)
        if connection is None and is_valid_public_url:
            # If OMERO.webpublic is enabled, pick up a username and
            # password from configuration and use those credentials to
            # create a connection.
            logger.debug('OMERO.webpublic enabled, attempting to login ' \
                         'with configuration supplied credentials.')
            if server_id is None:
                server_id = settings.PUBLIC_SERVER_ID
            username = settings.PUBLIC_USER
            password = settings.PUBLIC_PASSWORD
            is_secure = request.REQUEST.get('ssl', False)
            logger.debug('Is SSL? %s' % is_secure)
            # Try and use a cached OMERO.webpublic user session key.
            public_user_connector = self.get_public_user_connector()
            if public_user_connector is not None:
                logger.debug('Attempting to use cached OMERO.webpublic ' \
                             'connector: %r' % public_user_connector)
                connection = public_user_connector.join_connection(
                        self.useragent)
                if connection is not None:
                    request.session['connector'] = public_user_connector
                    logger.debug('Attempt to use cached OMERO.web public ' \
                                 'session key successful!')
                    return connection
                logger.debug('Attempt to use cached OMERO.web public ' \
                             'session key failed.')
            # We don't have a cached OMERO.webpublic user session key,
            # create a new connection based on the credentials we've been
            # given.
            connector = Connector(server_id, is_secure)
            connection = connector.create_connection(
                    self.useragent, username, password, is_public=True)
            request.session['connector'] = connector
            self.set_public_user_connector(connector)
        elif connection is not None:
            is_anonymous = connection.isAnonymous()
            logger.debug('Is anonymous? %s' % is_anonymous)
            if is_anonymous and not is_valid_public_url:
                return None
        return connection

    def get_authenticated_connection(self, server_id, request):
        """
        Prepares an authenticated Blitz connection wrapper (from
        L{omero.gateway}) for use with a view function.
        """
        # TODO: Handle previous try_super logic; is it still needed?

        session = request.session
        request = request.REQUEST
        is_secure = request.get('ssl', False)
        logger.debug('Is SSL? %s' % is_secure)
        connector = session.get('connector', None)
        logger.debug('Connector: %s' % connector)

        if server_id is None:
            # If no server id is passed, the db entry will not be used and
            # instead we'll depend on the request.session and request.REQUEST
            # values
            if connector is not None:
                server_id = connector.server_id
            else:
                try:
                    server_id = request['server']
                except:
                    logger.debug('No Server ID available.')
                    return None

        # If we have an OMERO session key in our request variables attempt
        # to make a connection based on those credentials.
        try:
            omero_session_key = request['bsession']
            connector = Connector(server_id, is_secure)
        except KeyError:
            # We do not have an OMERO session key in the current request.
            pass
        else:
            # We have an OMERO session key in the current request use it
            # to try join an existing connection / OMERO session.
            logger.debug('Have OMERO session key %s, attempting to join...' % \
                    omero_session_key)
            connector.omero_session_key = omero_session_key
            connection = connector.join_connection(self.useragent)
            session['connector'] = connector
            return connection

        # An OMERO session is not available, we're either trying to service
        # a request to a login page or an anonymous request.
        username = None
        password = None
        try:
            username = request['username']
            password = request['password']
        except KeyError:
            if connector is None:
                logger.debug('No username or password in request, exiting.')
                # We do not have an OMERO session or a username and password
                # in the current request and we do not have a valid connector.
                # Raise an error (return None).
                return None

        if username is not None and password is not None:
            # We have a username and password in the current request, or
            # OMERO.webpublic is enabled and has provided us with a username
            # and password via configureation. Use them to try and create a
            # new connection / OMERO session.
            logger.debug('Creating connection with username and password...')
            connector = Connector(server_id, is_secure)
            connection = connector.create_connection(
                    self.useragent, username, password)
            session['connector'] = connector
            return connection

        logger.debug('Django session connector: %r' % connector)
        if connector is not None:
            # We have a connector, attempt to use it to join an existing
            # connection / OMERO session.
            connection = connector.join_connection(self.useragent)
            if connection is not None:
                logger.debug('Connector valid, session successfully joined.')
                return connection
            # Fall through, we the session we've been asked to join may
            # be invalid and we may have other credentials as request
            # variables.
            logger.debug('Connector is no longer valid, destroying...')
            del session['connector']
            return None

        session['connector'] = connector
        return connection

    def __call__(ctx, f):
        """
        Tries to prepare a logged in connection, then calls function and
        returns the result.
        """
        def wrapped(request, *args, **kwargs):
            url = request.REQUEST.get('url')
            if url is None or len(url) == 0:
                url = request.get_full_path()

            doConnectionCleanup = False

            conn = kwargs.get('conn', None)
            error = None
            server_id = kwargs.get('server_id', None)
            # Short circuit connection retrieval when a connection was
            # provided to us via 'conn'. This is useful when in testing
            # mode or when stacking view functions/methods.
            if conn is None:
                doConnectionCleanup = ctx.doConnectionCleanup
                logger.debug('Connection not provided, attempting to get one.')
                try:
                    conn = ctx.get_connection(server_id, request)
                except Exception, x:
                    logger.error('Error retrieving connection.', exc_info=True)
                    error = str(x)
                else:
                    # various configuration & checks only performed on new 'conn'
                    if conn is None:
                        return ctx.on_not_logged_in(request, url, error)
                    else:
                        ctx.on_logged_in(request, conn)
                    ctx.verify_is_admin(conn)
                    ctx.verify_is_group_owner(conn, kwargs.get('gid'))

                    share_id = kwargs.get('share_id')
                    conn_share = ctx.prepare_share_connection(request, conn, share_id)
                    if conn_share is not None:
                        ctx.on_share_connection_prepared(request, conn_share)
                        kwargs['conn'] = conn_share
                    else:
                        kwargs['conn'] = conn

                    #kwargs['error'] = request.REQUEST.get('error')
                    kwargs['url'] = url

            retval = f(request, *args, **kwargs)
            try:
                logger.debug('Doing connection cleanup? %s' % \
                        doConnectionCleanup)
                if doConnectionCleanup:
                    if conn is not None and conn.c is not None:
                        for v in conn._proxies.values():
                            v.close()
                        conn.c.closeSession()
            except:
                logger.warn('Failed to clean up connection.', exc_info=True)
            return retval
        return wraps(f)(wrapped)
        
class render_response(object):
    """
    This decorator handles the rendering of view methods to HttpResponse. It expects
    that wrapped view methods return a dict. This allows:
    - The template to be specified in the method arguments OR within the view method itself
    - The dict to be returned as json if required
    - The request is passed to the template context, as required by some tags etc
    - A hook is provided for adding additional data to the context, from the L{omero.gateway.BlitzGateway}
        or from the request.
    """

    def prepare_context(self, request, context, *args, **kwargs):
        """ Hook for adding additional data to the context dict """
        pass


    def __call__(ctx, f):
        """ Here we wrap the view method f and return the wrapped method """

        def wrapper(request, *args, **kwargs):
            """ Wrapper calls the view function, processes the result and returns HttpResponse """

            # call the view function itself...
            context = f(request, *args, **kwargs)

            # if we happen to have a Response, return it
            if isinstance(context, HttpResponse):
                return context

            # get template from view dict. Can be overridden from the **kwargs
            template = 'template' in context and context['template'] or None
            template = kwargs.get('template', template)
            logger.debug("Rendering template: %s" % template)

            # allows us to return the dict as json  (NB: BlitzGateway objects don't serialize)
            if template is None or template == 'json':
                json_data = simplejson.dumps(context)
                return HttpResponse(json_data, mimetype='application/javascript')
            else:
                # allow additional processing of context dict
                ctx.prepare_context(request, context, *args, **kwargs)
                t = template_loader.get_template(template)
                c = RequestContext(request, context)
                return HttpResponse(t.render(c))
        return wraps(f)(wrapper)
