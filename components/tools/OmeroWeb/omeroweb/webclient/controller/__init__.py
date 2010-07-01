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

def sortByAttr(seq, attr, reverse=False):
    # Use the "Schwartzian transform".
    # Wrapped object only.
    #intermed = map(None, map(getattr, seq, (attr,)*len(seq)), xrange(len(seq)), seq)
    #intermed.sort()
    #if reverse:
    #    intermed.reverse()
    #return map(operator.getitem, intermed, (-1,) * len(intermed))
    
    intermed = list()
    for i in xrange(len(seq)):
        val = getAttribute(seq[i],attr)
        intermed.append((val, i, seq[i]))
    
    intermed.sort()
    if reverse:
        intermed.reverse()
    return [ tup[-1] for tup in intermed ]

def getAttribute(o,a):
    attr = a.split(".")
    if len(attr) > 1:
        for i in xrange(len(attr)):
            if hasattr(o,attr[i]):
                rv = getattr(o,attr[i])
                if hasattr(rv,'val'):
                    return getattr(rv,'val')
                else:
                    attr.remove(attr[i])
                    return getAttribute(rv, ".".join(attr))
    else:
        if hasattr(o,attr[0]):
            rv = getattr(o,attr[0])
            if hasattr(rv,'val'):
                return getattr(rv,'val')
            else:
                return rv

from django.conf import settings
PAGE = settings.PAGE

class BaseController(object):
    
    conn = None
    eContext = dict()
    
    def __init__(self, conn, **kw):
        self.conn = conn
        self.eContext['context'] = self.conn.getEventContext()
        gr = self.conn.getGroup(self.conn.getEventContext().groupId)
        self.eContext['isReadOnly'] = gr.isReadOnly()
        if not gr.isPrivate() and not gr.isReadOnly():
            self.eContext['isEditable'] = True
        else:
            self.eContext['isEditable'] = False
        self.eContext['user'] = self.conn.getUser()
        
        grs = list(self.conn.getGroupsMemberOf())
        self.eContext['memberOfGroups'] = self.sortByAttr(grs, "name")        
        self.eContext['allGroups'] = self.sortByAttr(grs, "name")
        self.eContext['advice'] = None
    
    def sortByAttr(self, seq, attr, reverse=False):
        return sortByAttr(seq, attr, reverse)
    
    def getShareId(self):
        return self.conn._shareId
    
    #####################################################################
    # Permissions
    
    def setObjectPermissions(self, obj, permissions):
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
    
    def getObjectPermissions(self, obj):
        perm = None
        if obj.details.getPermissions() is None:
            raise AttributeError('Object has no permissions')
        else:
            perm = obj.details.getPermissions()

        permissions = {'owner':None, 'group':None, 'world':None}
        if perm.isUserRead() and perm.isUserWrite():
            permissions['owner'] = 'rw'
        elif not perm.isUserRead() and perm.isUserWrite():
            permissions['owner'] = 'w'
        elif perm.isUserRead() and not perm.isUserWrite():
            permissions['owner'] = 'r'
        else:
            permissions['owner'] = None
        
        if perm.isGroupRead() and perm.isGroupWrite():
            permissions['group'] = 'rw'
        elif not perm.isGroupRead() and perm.isGroupWrite():
            permissions['group'] = 'w'
        elif perm.isGroupRead() and not perm.isGroupWrite():
            permissions['group'] = 'r'
        else:
            permissions['group'] = None

        if perm.isWorldRead() and perm.isWorldWrite():
            permissions['world'] = 'rw'
        elif not perm.isWorldRead() and perm.isWorldWrite():
            permissions['world'] = 'w'
        elif perm.isWorldRead() and not perm.isWorldWrite():
            permissions['world'] = 'r'
        else:
            permissions['world'] = None
        
        return permissions
    ###########################################################
    # Paging
        
    def doPaging(self, page, page_size, total_size, limit=PAGE):
        total = list()
        t = total_size/limit
        if total_size > (limit*10):
            if page > 10 :
                total.append(-1)
            for i in range((1, page-9)[ page-9 >= 1 ], (t+1, page+10)[ page+9 < t ]):
                total.append(i)
            if page < t-9:
                total.append(-1)

        elif total_size > limit and total_size <= (limit*10):
            for i in range(1, t+2):
                total.append(i)
        else:
            total.append(1)
        next = None
        if page_size == limit and (page*limit) < total_size:
            next = page + 1
        prev = None
        if page > 1:
            prev = page - 1
        
        return {'page': page, 'total':total, 'next':next, "prev":prev}
