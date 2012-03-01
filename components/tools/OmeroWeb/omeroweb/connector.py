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

import logging

from omero import client_wrapper
from omeroweb.webadmin.custom_models import Server

logger = logging.getLogger(__name__)

class Connector(object):
    """
    Object which encompasses all of the logic related to a Blitz connection
    and its status with respect to OMERO.web.
    """

    def __init__(self, server_id, is_secure, useragent):
        self.server_id = server_id
        self.is_secure = is_secure
        self.omero_session_key = None
        self.useragent = useragent

    def set_server_id(self, server_id):
        self._server_id = server_id
        self.server = None
        if server_id is not None:
            self.server = Server.get(server_id)

    def get_server_id(self):
        if self.server is not None:
            return self.server.id
    server_id = property(get_server_id, set_server_id)

    def lookup_host_and_port(self):
        server = Server.get(self.server_id)
        return (server.host, server.port)

    def create_gateway(self, username=None, password=None):
        host, port = self.lookup_host_and_port()
        return client_wrapper(
                username, password, host=host, port=port, secure=self.is_secure,
                useragent=self.useragent)

    def prepare_gateway(self, connection):
        connection.server_id = self.server_id
        # Lazy import due to the potential usage of the decorator in
        # the omeroweb.webgateway.views package.
        # TODO: UserProxy needs to be moved to this package or similar
        from omeroweb.webgateway.views import UserProxy
        connection.user = UserProxy(connection)
        connection.user.logIn()
        # TODO: Properly handle activating the weblitz_cache

    def create_connection(self, username, password):
        try:
            connection = self.create_gateway(username, password)
            connection.connect()
            self.prepare_gateway(connection)
            return connection
        except:
            logger.debug('Cannot create a new connection.', exc_info=True)
            return None

    def join_connection(self):
        try:
            connection = self.create_gateway()
            connection.connect(sUuid=self.omero_session_key)
            self.prepare_gateway(connection)
        except:
            logger.debug('Cannot create a new connection.', exc_info=True)
            return None

