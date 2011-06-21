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
from omero.model import ExperimenterGroupI, PermissionsI

from webadmin.controller import BaseController

class BaseGroups(BaseController):

    groups = None
    groupsCount = 0
    
    def __init__(self, conn):
        BaseController.__init__(self, conn)
        groupsList = list(self.conn.getObjects("ExperimenterGroup"))
        groupsList.sort(key=lambda x: x.getName().lower())
        self.groups = list()
        for gr in groupsList:
            self.groups.append({'group': gr, 'locked': self.isLocked(gr.name), 'permissions': self.getPermissions(gr)})
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
            self.group = self.conn.getObject("ExperimenterGroup", gid)
        
            self.owners = list()
            for gem in self.group.copyGroupExperimenterMap():
                if gem.owner.val == True:
                    self.owners.append(gem.child.id.val)
        self.experimenters = list(self.conn.listExperimenters())
        self.experimenters.sort(key=lambda x: x.getOmeName().lower())
    
    def getOwnersNames(self):
        owners = list()
        for e in self.conn.getObjects("Experimenter", self.owners):
            owners.append(e.getFullName())
        return ", ".join(owners)
    
    def containedExperimenters(self):
        self.members = list(self.conn.containedExperimenters(self.group.id))
        self.members.sort(key=lambda x: x.getOmeName().lower())
        for i, m in enumerate(self.members):
            if m.copyGroupExperimenterMap()[0].parent.id.val == self.group.id:
                self.members[i].setFirstName("*%s" % (m.firstName))
        
        self.available = list()
        memberIds = [m.id for m in self.members]
        for e in self.experimenters:
            if e.id not in memberIds:
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
            for m in members:
                if oa.id == long(str(m)):
                    add_exps.append(oa._obj)
                
        for r in rm_exps:
            if self.conn.getDefaultGroup(r.id.val).id == self.group.id:
                rm_exps.remove(r)
        
        self.conn.setMembersOfGroup(self.group._obj, add_exps, rm_exps)
    
    def createGroup(self, name, owners, perm, r=None, description=None):
        new_gr = ExperimenterGroupI()
        new_gr.name = rstring(str(name))
        new_gr.description = (description!="" and description is not None) and rstring(str(description)) or None
        new_gr.details.permissions = self.setActualPermissions(perm, r)
        
        listOfOwners = set()
        for e in self.experimenters:
            for o in owners:
                if long(o) == e.id:
                    listOfOwners.add(e._obj)
        
        return self.conn.createGroup(new_gr, list(listOfOwners))
    
    def updateGroup(self, name, owners, perm, r=None, description=None):
        up_gr = self.group._obj
        up_gr.name = rstring(str(name))
        up_gr.description = (description!="" and description is not None) and rstring(str(description)) or None
        permissions = None
        perm = int(perm)
        if self.getActualPermissions() != perm or self.isReadOnly()!=r:
            permissions = self.setActualPermissions(perm, r)
        # old list of groups
        old_owners = list()
        for oex in up_gr.copyGroupExperimenterMap():
            if oex.owner.val:
                old_owners.append(oex.child)        

        # create list of new groups
        new_owners = list()        
        for e in self.experimenters:
            for o in owners:
                if long(o) == e.id:
                    new_owners.append(e._obj)
        
        add_exps = list()
        rm_exps = list()
        
        # remove
        for oex in old_owners:
            flag = False
            for nex in new_owners:
                if nex.id.val == oex.id.val:
                    flag = True
            if not flag:
                rm_exps.append(oex)
        
        # add
        for nex in new_owners:
            flag = False
            for oex in old_owners:
                if oex.id.val == nex.id.val:
                    flag = True
            if not flag:
                add_exps.append(nex)
        
        self.conn.updateGroup(up_gr, add_exps, rm_exps, permissions)

    def updatePermissions(self, perm, r=None):
        permissions = None
        perm = int(perm)
        if self.getActualPermissions() != perm or self.isReadOnly()!=r:
            permissions = self.setActualPermissions(perm, r)
            self.conn.updatePermissions(self.group._obj, permissions)
    
    def getActualPermissions(self):
        p = None
        if self.group.details.getPermissions() is None:
            raise AttributeError('Object has no permissions')
        else:
            p = self.group.details.getPermissions()
        
        flag = None
        if p.isUserRead():
            flag = 0
        if p.isGroupRead():
            flag = 1
        if p.isWorldRead():
            flag = 2
        
        return flag
    
    def isReadOnly(self):
        p = None
        if self.group.details.getPermissions() is None:
            raise AttributeError('Object has no permissions')
        else:
            p = self.group.details.getPermissions()
        
        flag = False
        if p.isUserRead() and not p.isUserWrite():
            flag = True
        if p.isGroupRead() and not p.isGroupWrite():
            flag = True
        if p.isWorldRead() and not p.isWorldWrite():
            flag = True
        return flag
        
    def setActualPermissions(self, p, r=None):
        permissions = PermissionsI()
        p = int(p)        
        if p == 0:
            #private
            permissions.setUserRead(True)
            permissions.setUserWrite(True)
            permissions.setGroupRead(False)
            permissions.setGroupWrite(False)
            permissions.setWorldRead(False)
            permissions.setWorldWrite(False)
        elif p == 1:
            #collaborative
            permissions.setUserRead(True)
            permissions.setUserWrite(True)
            permissions.setGroupRead(True)
            permissions.setGroupWrite(not r)
            permissions.setWorldRead(False)
            permissions.setWorldWrite(False)
        elif p == 2:
            #public
            permissions.setUserRead(True)
            permissions.setUserWrite(True)
            permissions.setGroupRead(True)
            permissions.setGroupWrite(not r)
            permissions.setWorldRead(True)
            permissions.setWorldWrite(not r)
        return permissions
