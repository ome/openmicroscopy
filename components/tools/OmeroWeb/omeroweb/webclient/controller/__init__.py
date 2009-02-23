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
#

import operator
from omeroweb.webclient.models import Advice

class BaseController(object):
    
    conn = None
    eContext = dict()
    
    def __init__(self, conn, **kw):
        self.conn = conn
        self.eContext['context'] = self.conn.getEventContext()
        self.eContext['user'] = self.conn.getUserWrapped()
        
        grs = list(self.conn.getGroupsMemberOf())
        self.eContext['memberOfGroups'] = self.sortByAttr(grs, "name")
        
        grs.extend(list(self.conn.getGroupsLeaderOf()))
        self.eContext['allGroups'] = self.sortByAttr(grs, "name")
        self.eContext['advice'] = Advice.objects.get(pk=1)
    
    def sortByAttr(self, seq, attr, reverse=False):
        # Use the "Schwartzian transform".
        # Wrapped object only.
        #intermed = map(None, map(getattr, seq, (attr,)*len(seq)), xrange(len(seq)), seq)
        #intermed.sort()
        #if reverse:
        #    intermed.reverse()
        #return map(operator.getitem, intermed, (-1,) * len(intermed))
        
        intermed = list()
        for i in xrange(len(seq)):
            val = self.getAttribute(seq[i],attr)
            intermed.append((val, i, seq[i]))
        
        intermed.sort()
        if reverse:
            intermed.reverse()
        return [ tup[-1] for tup in intermed ]
    
    def getAttribute(self, o,a):
        attr = a.split(".")
        if len(attr) > 1:
            for i in xrange(len(attr)):
                if hasattr(o,attr[i]):
                    rv = getattr(o,attr[i])
                    if hasattr(rv,'val'):
                        return getattr(rv,'val')
                    else:
                        attr.remove(attr[i])
                        return self.getAttribute(rv, ".".join(attr))
        else:
            if hasattr(o,attr[0]):
                rv = getattr(o,attr[0])
                if hasattr(rv,'val'):
                    return getattr(rv,'val')
                else:
                    return rv
    
    #####################################################################
    # Permissions
    
    def objectPermissions(self, obj, permissions):
        if permissions['owner'] == 'rw':
            obj.details.permissions.setUserRead(True)
            obj.details.permissions.setUserWrite(True)
        elif permissions['owner'] == 'w':
            obj.details.permissions.setUserRead(False)
            obj.details.permissions.setUserWrite(True)
        elif permissions['owner'] == 'r':
            obj.details.permissions.setUserRead(True)
            obj.details.permissions.setUserWrite(False)
        else:
            obj.details.permissions.setUserRead(False)
            obj.details.permissions.setUserWrite(False)
        
        if permissions['group'] == 'rw':
            obj.details.permissions.setGroupRead(True)
            obj.details.permissions.setGroupWrite(True)
        elif permissions['group'] == 'w':
            obj.details.permissions.setGroupRead(False)
            obj.details.permissions.setGroupWrite(True)
        elif permissions['group'] == 'r':
            obj.details.permissions.setGroupRead(True)
            obj.details.permissions.setGroupWrite(False)
        else:
            obj.details.permissions.setGroupRead(False)
            obj.details.permissions.setGroupWrite(False)
        
        if permissions['world'] == 'rw':
            obj.details.permissions.setWorldRead(True)
            obj.details.permissions.setWorldWrite(True)
        elif permissions['world'] == 'w':
            obj.details.permissions.setWorldRead(False)
            obj.details.permissions.setWorldWrite(True)
        elif permissions['world'] == 'r':
            obj.details.permissions.setWorldRead(True)
            obj.details.permissions.setWorldWrite(False)
        else:
            obj.details.permissions.setWorldRead(False)
            obj.details.permissions.setWorldWrite(False)
    