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

from omero_model_PermissionsI import PermissionsI

class BaseController(object):
    
    conn = None
    
    def __init__(self, conn, **kw):
        self.conn = conn
    
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
    