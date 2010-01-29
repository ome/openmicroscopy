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

from omero.rtypes import *
from omero.model import ExperimenterGroupI

from webadmin.controller import BaseController

class BaseGroups(BaseController):

    groups = None
    groupsCount = 0
    
    def __init__(self, conn):
        BaseController.__init__(self, conn)
        groupsList = list(self.conn.lookupGroups())
        self.groups = list()
        for gr in groupsList:
            self.groups.append({'group': gr, 'locked': self.isLocked(gr.name)})
        self.groupsCount = len(groupsList)
    
    def isLocked(self, gname):
        if gname == "user":
            return True
        elif gname == "system":
            return True
        elif gname == "guest":
            return True
        else:
            False
    
class BaseGroup(BaseController):

    group = None
    experimenters = None

    def __init__(self, conn, gid=None):
        BaseController.__init__(self, conn)
        if gid is not None:
            self.group = self.conn.getGroup(gid)
        self.experimenters = list(self.conn.lookupExperimenters())

    def containedExperimenters(self):
        self.members = list(self.conn.containedExperimenters(self.group.id))
        self.defaultMembers = list()
        for i, m in enumerate(self.members):
            if m.copyGroupExperimenterMap()[0].parent.id.val == self.group.id: #1109
            #if self.conn.getDefaultGroup(m.id).id == self.group.id: # TODO: when ticket done remove it
                self.members[i].setFirstName("*%s" % (m.firstName))
        
        self.available = list()
        for e in self.experimenters:
            flag = False
            for m in self.members:
                if e.id == m.id:
                    flag = True
            if not flag:
                self.available.append(e)
    
    def setMembersOfGroup(self, available, members):
        old_members = list(self.conn.containedExperimenters(self.group.id))
        old_available = list()
        for e in self.experimenters:
            flag = False
            for m in old_members:
                if e.id == m.id:
                    flag = True
            if not flag:
                old_available.append(e)
                
        add_exps = list()
        rm_exps = list()
        for om in old_members:
            for a in available:
                if om.id == long(str(a)):
                    rm_exps.append(om._obj)
        for oa in old_available:
            flag = False
            for a in available:
                if oa.id == long(str(a)):
                    flag = True
            if not flag:
                add_exps.append(oa._obj)
                
        for r in rm_exps:
            if self.conn.getDefaultGroup(r.id.val).id == self.group.id:
                rm_exps.remove(r)
        
        self.conn.setMembersOfGroup(self.group._obj, add_exps, rm_exps)
    
    def createGroup(self, name, eid, permissions, description=None):
        new_gr = ExperimenterGroupI()
        new_gr.name = rstring(str(name))
        new_gr.description = description is not None and rstring(str(description)) or None
        #self.setObjectPermissions(new_gr, self.setActualPermissions(permissions))
        gr_owner = self.conn.getExperimenter(long(eid))._obj
        self.conn.createGroup(new_gr, gr_owner)
    
    def updateGroup(self, name, eid, permissions, description=None):
        up_gr = self.group._obj
        up_gr.name = rstring(str(name))
        up_gr.description = description is not None and rstring(str(description)) or None
        #self.setObjectPermissions(up_gr, self.setActualPermissions(permissions))
        gr_owner = self.conn.getExperimenter(long(eid))._obj
        # This does nothing!
        up_gr.details.owner = gr_owner
        self.conn.updateGroup(up_gr, gr_owner)

    def getActualPermissions(self):
        perm = self.getObjectPermissions(self.group)
        if perm['owner'] == 'rw' and perm['group'] == 'r' and perm['world'] == None:
            return 1
        elif perm['owner'] == 'rw' and perm['group'] == None and perm['world'] == None:
            return 0    
        
    def setActualPermissions(self, perm):
        perm = int(perm)
        if perm == 1:
            return {'owner':'rw', 'group':'r', 'world':None}
        else:
            return {'owner':'rw', 'group':None, 'world':None}
