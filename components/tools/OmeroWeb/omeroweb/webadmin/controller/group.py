#!/usr/bin/env python
# 
# Group controller
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# This file is distributed under the same license as the OMERO package.
# Use is subject to license terms supplied in LICENSE.txt
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

import omero

from omero.rtypes import *
from omero_model_ExperimenterGroupI import ExperimenterGroupI
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
            #if m.copyGroupExperimenterMap()[0].parent.id.val == self.group.id: #1109
            if self.conn.getDefaultGroup(m.id).id == self.group.id: # TODO: when ticket done remove it
                self.members[i].lastName = "*%s" % (m.lastName)
        
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
                if om.id == long(a.encode('utf8')):
                    rm_exps.append(om._obj)
        for oa in old_available:
            flag = False
            for a in available:
                if oa.id == long(a.encode('utf8')):
                    flag = True
            if not flag:
                add_exps.append(oa._obj)
                
        for r in rm_exps:
            if self.conn.getDefaultGroup(r.id.val).id == self.group.id:
                rm_exps.remove(r)
        
        self.conn.setMembersOfGroup(self.group._obj, add_exps, rm_exps)
    
    def createGroup(self, name, eid, description=None):
        new_gr = ExperimenterGroupI()
        new_gr.name = rstring(name)
        new_gr.description = rstring(description)
        gr_owner = self.conn.getExperimenter(eid)._obj
        self.conn.createGroup(new_gr, gr_owner)
    
    def updateGroup(self, name, eid, description=None):
        up_gr = self.group._obj
        up_gr.name = rstring(name)
        up_gr.description = rstring(description)
        gr_owner = self.conn.getExperimenter(eid)._obj
        up_gr.details.owner = gr_owner
        self.conn.updateGroup(up_gr, gr_owner)
