#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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
Decorators for use with the webstart application.
"""

import logging

from functools import update_wrapper

import omeroweb.decorators

from django.conf import settings
from django.core.urlresolvers import reverse


logger = logging.getLogger(__name__)


class login_required(omeroweb.webclient.decorators.login_required):
    """
    webstart specific extension of the OMERO.web login_required() decorator.
    """

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
                    # various configuration and checks only performed
                    # on new 'conn'
                    if conn is not None:
                        ctx.on_logged_in(request, conn)
                        ctx.verify_is_admin(conn)
                        ctx.verify_is_group_owner(conn, kwargs.get('gid'))

                        share_id = kwargs.get('share_id')
                        conn_share = ctx.prepare_share_connection(
                            request, conn, share_id)
                        if conn_share is not None:
                            ctx.on_share_connection_prepared(
                                request, conn_share)
                            kwargs['conn'] = conn_share
                        else:
                            kwargs['conn'] = conn
                    else:
                        if settings.WEBSTART_ADMINS_ONLY:
                            return ctx.on_not_logged_in(request, url, error)
                        else:
                            pass
                    #kwargs['error'] = request.REQUEST.get('error')
                    kwargs['url'] = url

            retval = f(request, *args, **kwargs)
            try:
                logger.debug(
                    'Doing connection cleanup? %s' % doConnectionCleanup)
                if doConnectionCleanup:
                    if conn is not None and conn.c is not None:
                        for v in conn._proxies.values():
                            v.close()
                        conn.c.closeSession()
            except:
                logger.warn('Failed to clean up connection.', exc_info=True)
            return retval
        return update_wrapper(wrapped, f)


class render_response(omeroweb.webclient.decorators.render_response):
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
        We also process some values from settings and add these to the context.
        """

        # we expect @login_required to pass us 'conn', but just in case...
        if 'conn' not in kwargs:
            if settings.WEBSTART and not settings.WEBSTART_ADMINS_ONLY:
                context['insight_url'] = request.build_absolute_uri(
                    reverse("webstart_insight"))
        else:
            super(render_response, self).prepare_context(
                request, context, *args, **kwargs)
