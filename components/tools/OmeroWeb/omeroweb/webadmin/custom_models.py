#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
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
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0

from django.utils.encoding import smart_unicode, force_unicode

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
        if cls._registry.has_key(pk):
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
            if cls._registry.has_key(pk):
                r = cls._registry[pk]
        return r

    @classmethod
    def find(cls, server_host):
        for s in cls._registry.values():
            if unicode(s.host) == unicode(server_host):
                return s
        return None
