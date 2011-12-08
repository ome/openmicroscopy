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

class Server(object):
    
    def __init__ (self, pk, host, port, server=None):
        self.id = pk
        self.host = host
        self.port = port
        self.server = None
        if server is not None and server != '':
            self.server = server

    def __repr__(self):
        """
        Json for printin settings.py: [["localhost", 4064, "omero"]]'
        """
        return """["%s", %s, "%s"]""" % (self.host, self.port, self.server)

    def __str__(self):
        if hasattr(self, '__unicode__'):
            return force_unicode(self).encode('utf-8')
        return '%s object' % (self.__class__.__name__)
    
    def __unicode__(self):
        return str(self.id)   
    

class ServerObjects(object):
    
    def __init__ (self, glist):
        self.blitz_list = list()
        i = 1
        for s in glist:
            self.blitz_list.append(Server(pk=i, host=s[0], port=s[1], server=s[2]))
            i+=1
    
    def get(self, pk):
        try:
            pk = int(pk)
        except:
            pass
        else:
            for b in self.blitz_list:
                if b.id == pk:
                    return b
        return None
    
    def find(self, **kwargs):
        rv = []
        for b in self.blitz_list:
            for k,v in kwargs.items():
                if getattr(b, k) != v:
                    continue
                rv.append(b)
        return rv
    
    def all(self):
        return self.blitz_list

    def __repr__(self):
        return repr(self.blitz_list)
