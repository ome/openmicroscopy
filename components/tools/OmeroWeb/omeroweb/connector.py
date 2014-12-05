#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 University of Dundee & Open Microscopy Environment.
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

import re
import logging

from django.utils.encoding import force_unicode

from omero import client_wrapper
from omero_version import omero_version

logger = logging.getLogger(__name__)


class IterRegistry(type):
    def __new__(cls, name, bases, attr):
        attr['_registry'] = {}
        attr['_frozen'] = False
        return type.__new__(cls, name, bases, attr)

    def __iter__(cls):
        return iter(cls._registry.values())


class ServerBase(object):
    __metaclass__ = IterRegistry
    _next_id = 1

    def __init__(self, host, port, server=None):
        if hasattr(self, 'host') or hasattr(self, 'port'):
            return
        self.id = type(self)._next_id
        self.host = host
        self.port = port
        self.server = (server is not None and server != '') and server or None
        type(self)._registry[self.id] = self
        type(self)._next_id += 1

    def __new__(cls, host, port, server=None):
        for key in cls._registry:
            val = cls._registry[key]
            if val.host == host and val.port == port:
                return cls._registry[key]

        if cls._frozen:
            raise TypeError('No more instances allowed')
        else:
            return object.__new__(cls)

    @classmethod
    def instance(cls, pk):
        if pk in cls._registry:
            return cls._registry[pk]
        return None

    @classmethod
    def freeze(cls):
        cls._frozen = True

    @classmethod
    def reset(cls):
        cls._registry = {}
        cls._frozen = False
        cls._next_id = 1


class Server(ServerBase):

    def __repr__(self):
        """
        Json for printin settings.py: [["localhost", 4064, "omero"]]'
        """
        return """["%s", %s, "%s"]""" % (self.host, self.port, self.server)

    def __str__(self):
        return force_unicode(self).encode('utf-8')

    def __unicode__(self):
        return str(self.id)

    @classmethod
    def get(cls, pk):
        r = None
        try:
            pk = int(pk)
        except:
            pass
        else:
            if pk in cls._registry:
                r = cls._registry[pk]
        return r

    @classmethod
    def find(cls, host=None, port=None, server=None):
        rv = []
        for s in cls._registry.values():
            if (host is not None and host != s.host) or \
               (port is not None and port != s.port) or \
               (server is not None and server != s.server):
                continue
            rv.append(s)
        return rv


class Connector(object):
    """
    Object which encompasses all of the logic related to a Blitz connection
    and its status with respect to OMERO.web.
    """

    SERVER_VERSION_RE = re.compile("^.*?[-]?(\\d+[.]\\d+([.]\\d+)?)[-]?.*?$")

    def __init__(self, server_id, is_secure):
        self.server_id = server_id
        self.is_secure = is_secure
        self.is_public = False
        self.omero_session_key = None
        self.user_id = None

    def lookup_host_and_port(self):
        server = Server.get(self.server_id)
        if server is None:
            server = Server.find(server=self.server_id)[0]
        return (server.host, server.port)

    def create_gateway(self, useragent, username=None, password=None,
                       userip=None):
        host, port = self.lookup_host_and_port()
        return client_wrapper(
            username, password, host=host, port=port, secure=self.is_secure,
            useragent=useragent, anonymous=self.is_public, userip=userip)

    def prepare_gateway(self, connection):
        connection.server_id = self.server_id
        # Lazy import due to the potential usage of the decorator in
        # the omeroweb.webgateway.views package.
        # TODO: UserProxy needs to be moved to this package or similar
        from omeroweb.webgateway.views import UserProxy
        connection.user = UserProxy(connection)
        connection.user.logIn()
        self.omero_session_key = connection._sessionUuid
        self.user_id = connection.getUserId()
        logger.debug('Successfully prepared gateway: %s'
                     % self.omero_session_key)
        # TODO: Properly handle activating the weblitz_cache

    def create_connection(self, useragent, username, password,
                          is_public=False, userip=None):
        self.is_public = is_public
        try:
            connection = self.create_gateway(
                useragent, username, password, userip)
            if connection.connect():
                logger.debug('Successfully created connection for: %s'
                             % username)
                self.prepare_gateway(connection)
                return connection
        except:
            logger.debug('Cannot create a new connection.', exc_info=True)
        return None

    def create_guest_connection(self, useragent, is_public=False):
        connection = None
        guest = 'guest'
        try:
            connection = self.create_gateway(useragent, guest, guest, None)
            if connection.connect():
                logger.debug('Successfully created a guest connection.')
            else:
                logger.warn('Cannot create a guest connection.')
        except:
            logger.error('Cannot create a guest connection.', exc_info=True)
        return connection

    def join_connection(self, useragent, userip=None):
        try:
            connection = self.create_gateway(useragent, userip=userip)
            if connection.connect(sUuid=self.omero_session_key):
                logger.debug('Successfully joined connection: %s'
                             % self.omero_session_key)
                connection.setUserId(self.user_id)
                self.prepare_gateway(connection)
                return connection
        except:
            logger.debug('Cannot create a new connection.', exc_info=True)
        return None

    def is_server_up(self, useragent):
        connection = self.create_guest_connection(useragent)
        if connection is None:
            return False
        try:
            connection.getServerVersion()
            return True
        except:
            logger.error('Cannot request server version.', exc_info=True)
        return False

    def check_version(self, useragent):
        connection = self.create_guest_connection(useragent)
        if connection is None:
            return False
        try:
            server_version = connection.getServerVersion()
            server_version = self.SERVER_VERSION_RE.match(server_version)
            server_version = server_version.group(1).split('.')

            client_version = self.SERVER_VERSION_RE.match(omero_version)
            client_version = client_version.group(1).split('.')
            logger.info("Client version: '%s'; Server version: '%s'"
                        % (client_version, server_version))
            return server_version == client_version
        except:
            logger.error('Cannot compare server to client version.',
                         exc_info=True)
        return False
