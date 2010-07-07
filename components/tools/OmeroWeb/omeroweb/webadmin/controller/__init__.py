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

from omero_model_PermissionsI import PermissionsI

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
                
class BaseController(object):
    
    conn = None
    
    def __init__(self, conn, **kw):
        self.conn = conn
    
    def sortByAttr(self, seq, attr, reverse=False):
        return sortByAttr(seq, attr, reverse)
    
    def getPermissions(self, ob):
        p = None
        if ob.details.getPermissions() is None:
            return 'unknown'
        else:
            p = ob.details.getPermissions()

        if p.isUserRead() and p.isUserWrite():
            flag = 'Private'
        elif p.isUserRead() and not p.isUserWrite():
            flag = 'Private (read-only)'
        if p.isGroupRead() and p.isGroupWrite():
            flag = 'Collaborative'
        elif p.isGroupRead() and not p.isGroupWrite():
            flag = 'Collaborative (read-only)'
        if p.isWorldRead() and p.isWorldWrite():
            flag = 'Public'
        elif p.isWorldRead() and not p.isWorldWrite():
            flag = 'Public (read-only)'
        return flag
    