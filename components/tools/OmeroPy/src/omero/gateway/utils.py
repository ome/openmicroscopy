#!/usr/bin/env python
# 
# webclient_gateway
# 
# Copyright (c) 2008-2011 University of Dundee.
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
#         Carlos Neves <carlos(at)glencoesoftware(dot)com>, 2008
# 
# Version: 1.0
#

class ServiceOptsDict(dict):

    def __init__(self, data=None):
        if data is None:
            data = dict()
        if isinstance(data, dict):
            _data = dict()
            for key in data:
                _data[key] = str(data[key])
            super(ServiceOptsDict, self).__init__(_data)
        else:
            raise AttributeError("%s argument must be a dictionary" % self.__class__.__name__)

    def __repr__(self):
        return "<%s: %s>" % (self.__class__.__name__,
                             super(ServiceOptsDict, self).__repr__())
    
    def __setitem__(self, key, value):
        super(ServiceOptsDict, self).__setitem__(key, str(value))
    
    def __getitem__(self, key):
        try:
            val_ = super(ServiceOptsDict, self).__getitem__(key)
        except KeyError:
            raise KeyError("Key %r not found in %r" % (key, self))
        return val_
    
    def get(self, key, default=None):
        try:
            return super(ServiceOptsDict, self).__getitem__(key)
        except KeyError:
            return default
    
    def set(self, key, value):
        return super(ServiceOptsDict, self).__setitem__(key,str(value))
    
    def getOmeroGroup(self):
        return self.get('omero.group')
    
    def setOmeroGroup(self, value):
        if value is not None:
            self.set('omero.group',value)
    
    def getOmeroUser(self):
        return self.get('omero.user')
    
    def setOmeroUser(self, value):
        if value is not None:
            self.set('omero.user',value)
    
    def getOmeroShare(self):
        return self.get('omero.share')
    
    def setOmeroShare(self, value):
        if value is not None:
            self.set('omero.share',value)
    
