#!/usr/bin/env python
# 
# Experimenter controller
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
from omero_model_ExperimenterI import ExperimenterI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from webadmin.controller import BaseController

class BaseExperimenters(BaseController):
    
    experimenters = None
    experimentersCount = 0
    auth = None
    
    def __init__(self, conn):
        BaseController.__init__(self, conn)
        self.experimentersList = list(self.conn.lookupExperimenters())
        self.auth = self.conn.lookupLdapAuthExperimenters()
        self.experimenters = list()
        for exp in self.experimentersList:
            self.experimenters.append({'experimenter': exp, 'active': self.isActive(exp.id), 'ldap':self.isLdap(exp.id),
                                       'admin': self.isAdmin(exp.id), 'guest': self.isGuest(exp.id)})
        self.experimentersCount = len(self.experimenters)
    
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
    
    def isActive(self, eid):
        for exp in self.experimentersList:
            if exp.id == eid:
                for ob in exp.copyGroupExperimenterMap():
                    if ob.parent.name.val == "user":
                        return True
        return False
    
    def isGuest(self, eid):
        for exp in self.experimentersList:
            if exp.id == eid:
                for ob in exp.copyGroupExperimenterMap():
                    if ob.parent.name.val == "guest":
                        return True
        return False
    
    def isAdmin(self, eid):
        for exp in self.experimentersList:
            if exp.id == eid:
                for ob in exp.copyGroupExperimenterMap():
                    if ob.parent.name.val == "system":
                        return True
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
            self.ldapAuth = self.conn.lookupLdapAuthExperimenter(eid)
            if self.experimenter.sizeOfGroupExperimenterMap() > 0:
                self.defaultGroup = self.experimenter.copyGroupExperimenterMap()[0].parent.id.val
            else:
                self.defaultGroup = None
            self.otherGroups = list()
            for gem in self.experimenter.copyGroupExperimenterMap():
                if gem.parent.name.val == "user":
                    pass
                elif gem.parent.name.val == "system":
                    pass
                else:
                    self.otherGroups.append(gem.parent.id.val)
        self.groups = list(self.conn.lookupGroups())
    
    def defaultGroupsInitialList(self):
        formGroups = list()
        for gr in self.groups:
            flag = False
            if gr.name == "user":
                flag = True
            if not flag:
                formGroups.append(gr)
        return formGroups
    
    def otherGroupsInitialList(self):
        formGroups = list()
        for gr in self.groups:
            flag = False
            if gr.name == "user":
                flag = True
            elif gr.name == "system":
                flag = True
            if not flag:
                formGroups.append(gr)
        return formGroups
    
    def getMyDetails(self):
        self.experimenter = self.conn.getExperimenter(self.conn.getUser().id.val)
        self.ldapAuth = self.conn.lookupLdapAuthExperimenter(self.conn.getUser().id.val)
        self.defaultGroup = self.experimenter.copyGroupExperimenterMap()[0].parent.id.val
        self.otherGroups = list()
        for gem in self.experimenter.copyGroupExperimenterMap():
            if gem.parent.name.val == "user":
                pass
            else:
                self.otherGroups.append(gem.parent)
    
    def updateMyAccount(self, firstName, lastName, email, dGroup, middleName=None, institution=None, password=None):
        up_exp = self.experimenter._obj
        up_exp.firstName = rstring(firstName)
        up_exp.middleName = rstring(middleName)
        up_exp.lastName = rstring(lastName)
        up_exp.email = rstring(email)
        up_exp.institution = rstring(institution)
        
        defaultGroup = self.conn.getGroup(long(dGroup))._obj
        self.conn.updateMyAccount(up_exp, defaultGroup, password)
    
    def isActive(self):
        for ob in self.experimenter.copyGroupExperimenterMap():
            if ob.parent.name.val == "user":
                return True
        return False
    
    def isAdmin(self):
        for ob in self.experimenter.copyGroupExperimenterMap():
            if ob.parent.name.val == "system":
                return True
        return False
    
    def createExperimenter(self, omeName, firstName, lastName, email, admin, active, dGroup, otherGroups, password, middleName=None, institution=None):
        new_exp = ExperimenterI()
        new_exp.omeName = rstring(omeName)
        new_exp.firstName = rstring(firstName)
        new_exp.middleName = rstring(middleName)
        new_exp.lastName = rstring(lastName)
        new_exp.email = rstring(email)
        new_exp.institution = rstring(institution)
        
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
                # remove defaultGroup from otherGroups if containes
                if long(og) == long(defaultGroup.id.val):
                    pass
                elif long(og) == g.id:
                    listOfGroups.add(g._obj)
        self.conn.createExperimenter(new_exp, defaultGroup, list(listOfGroups), password)
    
    def updateExperimenter(self, omeName, firstName, lastName, email, admin, active, dGroup, otherGroups, middleName=None, institution=None, password=None):
        up_exp = self.experimenter._obj
        up_exp.omeName = rstring(omeName)
        up_exp.firstName = rstring(firstName)
        up_exp.middleName = rstring(middleName)
        up_exp.lastName = rstring(lastName)
        up_exp.email = rstring(email)
        up_exp.institution = rstring(institution)
        
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
                
        #print
        '''print "add"
        for g in add_grs:
            print g.id.val, g.name.val
        print "remove" 
        for g in rm_grs:
            print g.id.val, g.name.val'''
        
        self.conn.updateExperimenter(up_exp, defaultGroup, add_grs, rm_grs, password)
    
    def deleteExperimenter(self):
        self.conn.deleteExperimenter(self.experimenter._obj)
