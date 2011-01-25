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
from omero.model import ExperimenterI, GroupExperimenterMapI

from webadmin.controller import BaseController

class BaseExperimenters(BaseController):
    
    experimenters = None
    experimentersCount = 0
    auth = None
    
    def __init__(self, conn):
        BaseController.__init__(self, conn)
        self.experimentersList = list(self.conn.listExperimenters())
        self.auth = self.conn.listLdapAuthExperimenters()
        self.experimenters = list()
        self.experimentersCount = {'experimenters': 0, 'active': 0, 'ldap': 0, 'admin': 0, 'guest': 0}
        for exp in self.experimentersList:
            isLdap = self.isLdap(exp.id)
            isActive = exp.isActive()
            isAdmin = exp.isAdmin()
            isGuest = exp.isGuest()
            self.experimenters.append({'experimenter': exp, 'active': isActive, 'ldap':isLdap,
                                       'admin': isAdmin, 'guest':isGuest })
            if isActive:
                self.experimentersCount['active'] += 1
            if isLdap:
                self.experimentersCount['ldap'] += 1
            if isAdmin:
                self.experimentersCount['admin'] += 1
            if isGuest:
                self.experimentersCount['guest'] += 1
        
        self.experimentersCount['experimenters'] = len(self.experimenters)
    
    def isLdap(self, eid):
        try:
            if len(self.auth.val) > 0:
                for a in self.auth.val:
                    for k,v in a.val.iteritems():
                        if long(eid) == long(v.val):
                            return True
        except:
            return False
        return False

    
class BaseExperimenter(BaseController):
    
    experimenter = None
    defaultGroup = None
    otherGroups = None
    ldapAuth = None
    
    groups = None
    
    def __init__(self, conn, eid=None):
        BaseController.__init__(self, conn)
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
            self.ldapAuth = self.conn.getLdapAuthExperimenter(eid)
            if self.experimenter.sizeOfGroupExperimenterMap() > 0:
                self.defaultGroup = self.experimenter.copyGroupExperimenterMap()[0].parent.id.val
            else:
                self.defaultGroup = None
            self.otherGroups = list()
            self.others = list()
            self.default = list()
            for gem in self.experimenter.copyGroupExperimenterMap():
                if gem.parent.name.val == "user":
                    pass
                #elif gem.parent.name.val == "system":
                #    pass
                elif gem.parent.name.val == "guest":
                    pass
                else:
                    self.otherGroups.append(gem.parent.id.val)
                    self.others.append(gem.parent)
                    self.default.append((gem.parent.id.val, gem.parent.name.val))
        self.groups = list(self.conn.listGroups())
    
    def otherGroupsInitialList(self, exclude=list()):
        formGroups = list()
        for gr in self.groups:
            flag = False
            if gr.name == "user":
                flag = True
            elif gr.name == "system":
                flag = True
            elif gr.name == "guest":
                flag = True
            if gr.id in exclude:
                flag = True
            if not flag:
                formGroups.append(gr)
        return formGroups
    
    def getSelectedGroups(self, ids):
        if len(ids)>0:
            return list(self.conn.getExperimenterGroups(ids))
        return list()
    
    def getMyDetails(self):
        self.experimenter = self.conn.getUser()
        self.ldapAuth = self.conn.getLdapAuthExperimenter(self.conn._userid)
        self.defaultGroup = self.experimenter.copyGroupExperimenterMap()[0].parent.id.val
        self.otherGroups = list()
        for gem in self.experimenter.copyGroupExperimenterMap():
            if gem.parent.name.val == "user":
                pass
            else:
                self.otherGroups.append(gem.parent)
    
    def getOwnedGroups(self):
        groupsList = list(self.conn.listOwnedGroups())
        self.groups = list()
        for gr in groupsList:
            if gr.name == "user" or gr.name == "system" or gr.name == "guest":
                pass
            else:
                self.groups.append({'group': gr, 'permissions': self.getPermissions(gr)})
        self.groupsCount = len(self.groups)
    
    def updateMyAccount(self, firstName, lastName, email, dGroup, middleName=None, institution=None):
        up_exp = self.experimenter._obj
        up_exp.firstName = rstring(str(firstName))
        up_exp.middleName = middleName is not None and rstring(str(middleName)) or None
        up_exp.lastName = rstring(str(lastName))
        up_exp.email = rstring(str(email))
        up_exp.institution = (institution!="" and institution is not None) and rstring(str(institution)) or None
        
        defaultGroup = self.conn.getGroup(long(dGroup))._obj
        self.conn.updateMyAccount(up_exp, defaultGroup)
    
    def createExperimenter(self, omeName, firstName, lastName, email, admin, active, dGroup, otherGroups, password, middleName=None, institution=None):
        new_exp = ExperimenterI()
        new_exp.omeName = rstring(str(omeName))
        new_exp.firstName = rstring(str(firstName))
        new_exp.middleName = middleName is not None and rstring(str(middleName)) or None
        new_exp.lastName = rstring(str(lastName))
        new_exp.email = rstring(str(email))
        new_exp.institution = (institution!="" and institution is not None) and rstring(str(institution)) or None
        
        listOfGroups = set()
        # default group
        for g in self.groups:
            if long(dGroup) == g.id:
                defaultGroup = g._obj
                break
        # system group
        if admin:
            for g in self.groups:
                if g.name == "system":
                    sysGroup = g._obj
                    break
            listOfGroups.add(sysGroup)
        # user group
        if active:
            for g in self.groups:
                if g.name == "user":
                    userGroup = g._obj
                    break
            listOfGroups.add(userGroup)
        # rest of groups
        for g in self.groups:
            for og in otherGroups:
                # remove defaultGroup from otherGroups if contains
                if long(og) == long(defaultGroup.id.val):
                    pass
                elif long(og) == g.id:
                    listOfGroups.add(g._obj)
        return self.conn.createExperimenter(new_exp, defaultGroup, list(listOfGroups), password)
    
    def updateExperimenter(self, omeName, firstName, lastName, email, admin, active, dGroup, otherGroups, middleName=None, institution=None):
        up_exp = self.experimenter._obj
        up_exp.omeName = rstring(str(omeName))
        up_exp.firstName = rstring(str(firstName))
        up_exp.middleName = middleName is not None and rstring(str(middleName)) or None
        up_exp.lastName = rstring(str(lastName))
        up_exp.email = rstring(str(email))
        up_exp.institution = (institution!="" and institution is not None) and rstring(str(institution)) or None
        
        # old list of groups
        old_groups = list()
        for ogr in up_exp.copyGroupExperimenterMap():
            old_groups.append(ogr.parent)
        
        # create list of new groups
        new_groups = list()
        
        # default group
        for g in self.groups:
            if long(dGroup) == g.id:
                defaultGroup = g._obj
        new_groups.append(defaultGroup)
        
        # user group
        if active:
            for g in self.groups:
                if g.name == "user":
                    sysGroup = g._obj
                    break
            new_groups.append(sysGroup)
        
        # system group
        if admin:
            if defaultGroup.name.val != "system":
                for g in self.groups:
                    if g.name == "system":
                        sysGroup = g._obj
                        break
                new_groups.append(sysGroup)
        
        # rest of groups
        for g in self.groups:
            for og in otherGroups:
                if long(og) == g.id and g.name != defaultGroup.name.val:
                    new_groups.append(g._obj)
        
        add_grs = list()
        rm_grs = list()
        
        # remove
        for ogr in old_groups:
            flag = False
            for ngr in new_groups:
                if ngr.id.val == ogr.id.val:
                    flag = True
            if not flag:
                rm_grs.append(ogr)
        
        # add
        for ngr in new_groups:
            flag = False
            for ogr in old_groups:
                if ogr.id.val == ngr.id.val:
                    flag = True
            if not flag:
                add_grs.append(ngr)
        
        self.conn.updateExperimenter(up_exp, defaultGroup, add_grs, rm_grs)
    
    def deleteExperimenter(self):
        self.conn.deleteExperimenter(self.experimenter._obj)
