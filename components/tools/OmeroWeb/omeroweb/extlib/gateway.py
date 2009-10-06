#!/usr/bin/env python
# 
# Gateway
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
#         Carlos Neves <carlos(at)glencoesoftware(dot)com>, 2008
# 
# Version: 1.0
#

import cStringIO
import traceback
import logging

logger = logging.getLogger('gateway')

try:
    import Image,ImageDraw
except:
    logger.error("You need to install the Python Imaging Library. Get it at http://www.pythonware.com/products/pil/")
    logger.error(traceback.format_exc())
from StringIO import StringIO

#import threading
import time
from datetime import datetime
from types import IntType, ListType, TupleType, UnicodeType, StringType

from django.utils.translation import ugettext as _
from django.conf import settings

import Ice
import Glacier2
import omero
from omero.gateway import timeit
import omero_api_IScript_ice
from omero.rtypes import *

from omero_model_FileAnnotationI import FileAnnotationI
from omero_model_TagAnnotationI import TagAnnotationI
from omero_model_DatasetI import DatasetI
from omero_model_ProjectI import ProjectI
from omero_model_ImageI import ImageI
from omero_model_DetectorI import DetectorI
from omero_model_FilterI import FilterI
from omero_model_ObjectiveI import ObjectiveI
from omero_model_InstrumentI import InstrumentI

from omero_sys_ParametersI import ParametersI

TIMEOUT = 580 #sec
SLEEPTIME = 60

class OmeroWebGateway (omero.gateway.BlitzGateway):
    def __init__ (self, *args, **kwargs):
        super(OmeroWebGateway, self).__init__(*args, **kwargs)
        self._shareId = None

    def connect (self, *args, **kwargs):
        rv = super(OmeroWebGateway, self).connect(*args,**kwargs)
        try:
            self.removeGroupFromContext()
        except omero.SecurityViolation:
            pass
        return rv

    def attachToShare (self, share_id):
        sh = self._proxies['share'].getShare(long(share_id))
        if self._shareId is None:
            self._proxies['share'].activate(sh.id.val)
        self._shareId = sh.id.val

    def isForgottenPasswordSet(self):
        """ Retrieves a configuration value "omero.resetpassword.config" for
            Forgotten password form from the backend store. """
        
        conf = self.getConfigService()
        try:
            return bool(conf.getConfigValue("omero.resetpassword.config").title())
        except:
            logger.error(traceback.format_exc())
            return False

    def getUserWrapped(self):
        return ExperimenterWrapper(self, self._user)
    
    def removeGroupFromContext (self):
        """ Removes group "User" from the current context."""
        
        a = self.getAdminService()
        gr_u = a.lookupGroup('user')
        try:
            self._ctx.memberOfGroups.remove(gr_u.id.val)
            self._ctx.leaderOfGroups.remove(gr_u.id.val)
        except:
            pass
    
    ##############################################
    #    Session methods                         #
    
    def changeActiveGroup(self, gid): # TODO: should be moved to ISession
        """ Every time session is created default group becomes active group 
            and is loaded with the security for the current user and thread.
            Public data has to be created in the context of the group where user,
            who would like to look at these data, is a member of.
            Public data can be only visible by the member of group."""
        
        s = self.getSessionService()
        a = self.getAdminService()
        gr = a.getGroup(long(gid))
        session = s.getSession(self._sessionUuid)
        session.details.group = gr
        s.updateSession(session)
        self._ctx = self._proxies['admin'].getEventContext()
    
    ##############################################
    ##   Forgotten password                     ##
    
    def reportForgottenPassword(self, username, email):
        """ Allows to reset the password (temporary password is sent). The
            given email must match the email for the user listed under the name
            argument."""
        
        admin_serv = self.getAdminService()
        return admin_serv.reportForgottenPassword(username, email)
    
    ##############################################
    ##   Gets methods                           ##
    
    def lookupExperimenters(self):
        """ Look up all experimenters all related groups.
            The experimenters are also loaded."""
        
        admin_serv = self.getAdminService()
        for exp in admin_serv.lookupExperimenters():
            yield ExperimenterWrapper(self, exp)
    
    def getExperimenters(self, ids=None):
        """ Get experimenters for for the given user ids. If ID is not set, return current user. """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        if ids is not None:
            p.map = {}
            p.map["ids"] = rlist([rlong(long(a)) for a in ids])
            sql = "select e from Experimenter as e where e.id in (:ids)"
        else:
            p.map = {}
            p.map["id"] = rlong(self.getEventContext().userId)
            sql = "select e from Experimenter as e where e.id != :id "
        for e in q.findAllByQuery(sql, p):
            yield ExperimenterWrapper(self, e)
    
    def lookupLdapAuthExperimenters(self):
        """ Looks up all IDs of experimenters who are authenticated by LDAP
            (has set dn on password table). """
        
        admin_serv = self.getAdminService()
        return admin_serv.lookupLdapAuthExperimenters()
    
    def lookupGroups(self):
        """ Looks up all groups and all related experimenters. 
            The experimenters' groups are also loaded."""
            
        admin_serv = self.getAdminService()
        for gr in admin_serv.lookupGroups():
            yield ExperimenterGroupWrapper(self, gr)
    
    def getExperimenterGroups(self, ids):
        """ Get group for for the given group ids. """
            
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in ids])
        sql = "select e from ExperimenterGroup as e where e.id in (:ids)"
        for e in q.findAllByQuery(sql, p):
            if e.name.val != 'user':
                yield ExperimenterGroupWrapper(self, e)
    
    def lookupScripts(self):
        script_serv = self.getScriptService()
        return script_serv.getScripts()
    
    
    # Repository info
    def getUsedSpaceInKilobytes(self):
        """ Returns the total space in bytes for this file system
            including nested subdirectories. """
        
        rep_serv = self.getRepositoryInfoService()
        return rep_serv.getUsedSpaceInKilobytes()
    
    def getFreeSpaceInKilobytes(self):
        """ Returns the free or available space on this file system
            including nested subdirectories. """
        
        rep_serv = self.getRepositoryInfoService()
        return rep_serv.getFreeSpaceInKilobytes()
    
    def getUsage(self):
        """ Returns list of users and how much space each of them use."""
        
        query_serv = self.getQueryService()
        pixels = query_serv.findAllByQuery("select p from Pixels as p left outer join fetch p.pixelsType",None)
        usage = dict()
        for p in pixels:
            expid = long(p.details.owner.id.val)
            if usage.has_key(expid):
                bytesUsed = usage[expid]
            else:
                bytesUsed = 0
            bytesUsed += p.sizeX.val * p.sizeY.val * p.sizeZ.val * p.sizeC.val * p.sizeT.val * self.bytesPerPixel(p.pixelsType.value)
            usage[expid] = long(bytesUsed)
        
        return usage
    
    # SPW
    def lookupScreens(self, eid=None, page=None):
        """ Retrieves every Screens. If user id not set, owned by 
            the current user."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select sc from Screen sc " \
                "join fetch sc.details.creationEvent "\
                "join fetch sc.details.owner join fetch sc.details.group " \
                "where sc.details.owner.id=:eid order by sc.id asc"
        for e in q.findAllByQuery(sql, p):
            yield ScreenWrapper(self, e)
    
    def lookupPlatesInScreens (self, oid, eid=None, page=None):
        """ Retrieves every Datasets in a for the given Project id. 
            If user id not set, owned by the current user."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select pl from Plate pl "\
                "join fetch pl.details.creationEvent "\
                "join fetch pl.details.owner join fetch pl.details.group " \
                "left outer join fetch pl.screenLinks spl "\
                "left outer join fetch spl.parent sc " \
                "where sc.id=:oid and pl.details.owner.id=:eid "\
                "order by pl.id asc"
        for e in q.findAllByQuery(sql,p):
            yield PlateWrapper(self, e)
    
    
    # DATA RETRIVAL
    def lookupProjects (self, eid=None, page=None):
        """ Retrieves every Projects. If user id not set, owned by 
            the current user."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select pr from Project pr " \
                "join fetch pr.details.creationEvent "\
                "join fetch pr.details.owner join fetch pr.details.group " \
                "where pr.details.owner.id=:eid order by pr.id asc"
        for e in q.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)

    def lookupOrphanedDatasets (self, eid=None, page=None):
        """ Retrieves every orphaned Datasets. If user id not set, owned by
            the current user."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select ds from Dataset as ds " \
                "join fetch ds.details.creationEvent "\
                "join fetch ds.details.owner join fetch ds.details.group " \
                "where ds.details.owner.id=:eid and " \
                "not exists ( "\
                    "select pld from ProjectDatasetLink as pld where pld.child=ds.id "\
                ") order by ds.id asc"
        for e in q.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)

    def lookupOrphanedImages (self, eid=None, page=None):
        """ Retrieves every orphaned Images. If user id not set, owned by 
            the current user."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select im from Image as im "\
                "join fetch im.details.owner join fetch im.details.group " \
                "where im.details.owner.id=:eid and "\
                "not exists ( "\
                    "select dsl from DatasetImageLink as dsl "\
                    "where dsl.child=im.id and dsl.details.owner.id=:eid "\
                ") order by im.id asc"
        for e in q.findAllByQuery(sql,p):
            yield ImageWrapper(self, e)

    def lookupDatasetsInProject (self, oid, eid=None, page=None):
        """ Retrieves every Datasets in a for the given Project id. 
            If user id not set, owned by the current user."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select ds from Dataset ds "\
                "join fetch ds.details.creationEvent "\
                "join fetch ds.details.owner join fetch ds.details.group " \
                "left outer join fetch ds.projectLinks pdl "\
                "left outer join fetch pdl.parent p " \
                "where p.id=:oid and ds.details.owner.id=:eid "\
                "order by ds.id asc"
        for e in q.findAllByQuery(sql,p):
            yield DatasetWrapper(self, e)

    def lookupImagesInDataset (self, oid, eid=None, page=None):
        """ Retrieves every Images in a for the given Dataset. 
            If user id not set, owned by the current user."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select im from Image im "\
                "join fetch im.details.creationEvent "\
                "join fetch im.details.owner join fetch im.details.group " \
                "left outer join fetch im.datasetLinks dil "\
                "left outer join fetch dil.parent d " \
                "where d.id = :oid and im.details.owner.id=:eid "\
                "order by im.id asc"
        for e in q.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)
    
    # COLLABORATION DATA RETRIVAL
#    def listProjectsInGroup (self, gid):
#        """ Retrieves Projects accessed by the for the given group id."""
#        
#        q = self.getQueryService()
#        p = omero.sys.Parameters()
#        p.map = {}
#        p.map["gid"] = rlong(long(gid))
#        sql = "select pr from Project pr join fetch pr.details.creationEvent join fetch pr.details.owner join fetch pr.details.group " \
#              "where pr.details.permissions > '-103' and pr.details.group.id=:gid order by pr.name"
#        for e in q.findAllByQuery(sql, p):
#            yield ProjectWrapper(self, e)
#
#    def listDatasetsOutoffProjectInGroup(self, gid):
#        """ Retrieves orphaned Datasets accessed by the for the given group id."""
#        
#        q = self.getQueryService()
#        p = omero.sys.Parameters()
#        p.map = {}
#        p.map["gid"] = rlong(long(gid))
#        sql = "select ds from Dataset as ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
#                "where ds.details.permissions > '-103' and ds.details.group.id=:gid and " \
#                "not exists ( select pld from ProjectDatasetLink as pld where pld.child=ds.id)) order by ds.name"
#        for e in q.findAllByQuery(sql, p):
#            yield DatasetWrapper(self, e)
#
#    def listImagesOutoffDatasetInGroup(self, gid, page=None):
#        """ Retrieves orphaned Images accessed by the for the given group id."""
#        
#        q = self.getQueryService()
#        p = omero.sys.Parameters()
#        p.map = {}
#        p.map["gid"] = rlong(long(gid))
#        sql = "select im from Image as im join fetch im.details.owner join fetch im.details.group " \
#                "where im.details.permissions > '-103' and im.details.group.id=:gid and " \
#                "not exists ( select dsl from DatasetImageLink as dsl where dsl.child=im.id and dsl.details.owner.id=:gid) " \
#                "order by im.id asc"
#        for e in q.findAllByQuery(sql, p):
#            yield ImageWrapper(self, e)
#
#    def listDatasetsInProjectInGroup (self, oid, gid, page=None):
#        """ Retrieves Datasets in a for the given Project id accessed by the for the given group id."""
#        
#        q = self.getQueryService()
#        p = omero.sys.Parameters()
#        p.map = {}
#        p.map["gid"] = rlong(long(gid))
#        p.map["oid"] = rlong(long(oid))
#        if page is not None:
#            f = omero.sys.Filter()
#            f.limit = rint(24)
#            f.offset = rint((int(page)-1)*24)
#            p.theFilter = f
#        sql = "select ds from Dataset ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
#              "left outer join fetch ds.projectLinks pdl left outer join fetch pdl.parent p " \
#              "where p.id=:oid and ds.details.group.id=:gid order by ds.id asc"
#        for e in q.findAllByQuery(sql,p):
#            yield DatasetWrapper(self, e)
#
#    def listImagesInDatasetInGroup (self, oid, gid, page=None):
#        """ Retrieves Images in a for the given Dataset id accessed by the for the given group id."""
#        
#        q = self.getQueryService()
#        p = omero.sys.Parameters()
#        p.map = {}
#        p.map["gid"] = rlong(long(gid))
#        p.map["oid"] = rlong(long(oid))
#        if page is not None:
#            f = omero.sys.Filter()
#            f.limit = rint(24)
#            f.offset = rint((int(page)-1)*24)
#            p.theFilter = f
#        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group " \
#              "left outer join fetch im.datasetLinks dil left outer join fetch dil.parent d " \
#              "where d.id=:oid and im.details.group.id=:gid order by im.id asc"
#        for e in q.findAllByQuery(sql, p):
#            yield ImageWrapper(self, e)
    
    # LISTS selections
    def listSelectedImages(self, ids):
        """ Retrieves for the given Image ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in ids])
        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group where im.id in (:ids) order by im.name"
        for e in q.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)

    def listSelectedDatasets(self, ids):
        """ Retrieves for the given Dataset ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in ids])
        sql = "select ds from Dataset ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group where ds.id in (:ids) order by ds.name"
        for e in q.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)

    def listSelectedProjects(self, ids):
        """ Retrieves for the given Project ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in ids])
        sql = "select pr from Project pr join fetch pr.details.creationEvent join fetch pr.details.owner join fetch pr.details.group where pr.id in (:ids) order by pr.name"
        for e in q.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)
    
    # HIERARCHY RETRIVAL
    def loadContainerHierarchy(self, eid=None):
        """ Retrieves hierarchy trees rooted by a given node - Project, 
            for the given user id linked to the objects in the tree,
            filter them by parameters."""
            
        q = self.getContainerService()
        if eid == None: 
            p = ParametersI().orphan().exp(self.getEventContext().userId)
        else: 
            p = ParametersI().orphan().exp(long(eid))
        for e in q.loadContainerHierarchy('Project', None,  p):
            if isinstance(e, ProjectI):
                yield ProjectWrapper(self, e)
            if isinstance(e, DatasetI):
                yield DatasetWrapper(self, e)

#    def loadGroupContainerHierarchy(self, gid=None):
#        """ Retrieves hierarchy trees rooted by a given node - Project, 
#            for the given Group id linked to the objects in the tree, filter them by parameters."""
#            
#        q = self.getContainerService()
#        if gid == None: 
#            p = ParametersI().orphan().grp(self.getEventContext().groupId)
#        else:
#            p = ParametersI().orphan().grp(long(gid))
#        for e in q.loadContainerHierarchy('Project', None,  p):
#            if isinstance(e, ProjectI):
#                yield ProjectWrapper(self, e)
#            if isinstance(e, DatasetI):
#                yield DatasetWrapper(self, e)
    
    def findContainerHierarchies(self, nid):
        """ Finds hierarchy trees rooted by a given node - Project, 
            for the given Image ids. TODO: #1015"""
            
        q = self.getContainerService()
        for e in q.findContainerHierarchies("Project", [long(nid)], None):
            if isinstance(e, ProjectI):
                yield ProjectWrapper(self, e)
            if isinstance(e, DatasetI):
                yield DatasetWrapper(self, e)
    
    # DATA RETRIVAL BY TAGs
    def listProjectsByTag(self, tids):
        """ Retrieves Projects linked to the for the given tag ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["tids"] = rlist([rlong(a) for a in set(tids)])
        sql = "select pr from Project pr join fetch pr.details.creationEvent join fetch pr.details.owner join fetch pr.details.group " \
              "left outer join fetch pr.annotationLinks pal " \
              "left outer join fetch pal.child tag " \
              "where tag.id in (:tids) and pr.details.owner.id=:eid"
        for e in q.findAllByQuery(sql,p):
            yield ProjectWrapper(self, e)
    
    def listDatasetsByTag(self, tids):
        """ Retrieves Datasets linked to the for the given tag ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["tids"] = rlist([rlong(a) for a in set(tids)])
        sql = "select ds from Dataset ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
              "left outer join fetch ds.annotationLinks dal " \
              "left outer join fetch dal.child tag " \
              "where tag.id in (:tids) and ds.details.owner.id=:eid "
        for e in q.findAllByQuery(sql,p):
            yield DatasetWrapper(self, e)
    
    def listImagesByTag(self, tids):
        """ Retrieves Images linked to the for the given tag ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["tids"] = rlist([rlong(a) for a in set(tids)])
        sql = "select im from Image im join fetch im.details.creationEvent join fetch im.details.owner join fetch im.details.group " \
              "left outer join fetch im.annotationLinks ial " \
              "left outer join fetch ial.child tag " \
              "where tag.id in (:tids) and im.details.owner.id=:eid "
        for e in q.findAllByQuery(sql,p):
            yield ImageWrapper(self, e)
    
    def listTags(self, o_type, oid):
        """ Retrieves list of Tags not linked to the for the given Project/Dataset/Image id."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["eid"] = rlong(self.getEventContext().userId)
        if o_type == "image":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select ial from ImageAnnotationLink as ial where ial.child=a.id and ial.parent.id=:oid ) " \
                "and a.details.owner.id=:eid "
        elif o_type == "dataset":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select dal from DatasetAnnotationLink as dal where dal.child=a.id and dal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid "
        elif o_type == "project":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select pal from ProjectAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid )" \
                "and a.details.owner.id=:eid "
        elif o_type == "screen":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select sal from ScreenAnnotationLink as sal where sal.child=a.id and sal.parent.id=:oid )" \
                "and a.details.owner.id=:eid "
        elif o_type == "plate":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select pal from PlateAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid )" \
                "and a.details.owner.id=:eid "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listComments(self, o_type, oid):
        """ Retrieves list of Comments not linked to the for the given Project/Dataset/Image id."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["eid"] = rlong(self.getEventContext().userId)
        if o_type == "image":
            sql = "select a from CommentAnnotation as a " \
                "where not exists ( select ial from ImageAnnotationLink as ial where ial.child=a.id and ial.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        elif o_type == "dataset":
            sql = "select a from CommentAnnotation as a " \
                "where not exists ( select dal from DatasetAnnotationLink as dal where dal.child=a.id and dal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        elif o_type == "project":
            sql = "select a from CommentAnnotation as a " \
                "where not exists ( select pal from ProjectAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        elif o_type == "screen":
            sql = "select a from CommentAnnotation as a " \
                "where not exists ( select sal from ScreenAnnotationLink as sal where sal.child=a.id and sal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        elif o_type == "plate":
            sql = "select a from CommentAnnotation as a " \
                "where not exists ( select pal from PlateAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listUrls(self, o_type, oid):
        """ Retrieves list of Urls not linked to the for the given Project/Dataset/Image id."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["eid"] = rlong(self.getEventContext().userId)
        if o_type == "image":
            sql = "select a from UriAnnotation as a " \
                "where not exists ( select ial from ImageAnnotationLink as ial where ial.child=a.id and ial.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null "
        elif o_type == "dataset":
            sql = "select a from UriAnnotation as a " \
                "where not exists ( select dal from DatasetAnnotationLink as dal where dal.child=a.id and dal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null "
        elif o_type == "project":
            sql = "select a from UriAnnotation as a " \
                "where not exists ( select pal from ProjectAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null "
        elif o_type == "screen":
            sql = "select a from UriAnnotation as a " \
                "where not exists ( select sal from ScreenAnnotationLink as sal where sal.child=a.id and sal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null "
        elif o_type == "plate":
            sql = "select a from UriAnnotation as a " \
                "where not exists ( select pal from PlateAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listFiles(self, o_type, oid):
        """ Retrieves list of Files not linked to the for the given Project/Dataset/Image id."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["eid"] = rlong(self.getEventContext().userId)
        if o_type == "image":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select ial from ImageAnnotationLink as ial where ial.child=a.id and ial.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null "
        elif o_type == "dataset":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select dal from DatasetAnnotationLink as dal where dal.child=a.id and dal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        elif o_type == "project":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select pal from ProjectAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        elif o_type == "screen":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select sal from ScreenAnnotationLink as sal where sal.child=a.id and sal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        elif o_type == "plate":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select pal from PlateAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
                "and a.details.owner.id=:eid and a.ns is null"
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listSpecifiedTags(self, ids):
        """ Retrieves list of for the given Tag ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from TagAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listSpecifiedComments(self, ids):
        """ Retrieves list of for the given Comment ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from CommentAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listSpecifiedFiles(self, ids):
        """ Retrieves list of for the given Fiel ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from FileAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listSpecifiedUrls(self, ids):
        """ Retrieves list of for the given Url ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from UriAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def lookupTags(self):
        """ Retrieves list of Tags owned by current user and return them as a dictionary list with selected field.
            This method is used by autocomplite."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select tg from TagAnnotation tg where tg.details.owner.id = :eid and tg.ns is null"
        tags = list()
        for e in q.findAllByQuery(sql,p):
            t = AnnotationWrapper(self, e)
            tags.append({'tag': t.textValue,'id':t.id, 'desc':(t.tinyDescription())} )
        return tags
    
    ##############################################
    ##   Share methods
    
    # SHARE
    def getOwnShares(self):
        """ Gets all owned shares for the current user. """
        
        sh = self.getShareService()
        for e in sh.getOwnShares(False):
            yield ShareWrapper(self, e)
    
    def getMemberShares(self):
        """ Gets all shares where current user is a member. """
        
        sh = self.getShareService()
        for e in sh.getMemberShares(False):
            yield ShareWrapper(self, e)
    
    def getMemberCount(self, share_ids):
        """ Returns a map from share id to the count of total members (including the
            owner). This is represented by ome.model.meta.ShareMember links."""
        
        sh = self.getShareService()
        return sh.getMemberCount(share_ids)
    
    def getCommentCount(self, share_ids):
        """ Returns a map from share id to comment count. """
        
        sh = self.getShareService()
        return sh.getCommentCount(share_ids)
    
    def getContents(self, share_id):
        """ Looks up all items belong to the share."""
        
        sh = self.getShareService()
        for e in sh.getContents(long(share_id)):
            yield ShareContentWrapper(self, e)
    
    def getComments(self, share_id):
        """ Looks up all comments which belong to the share."""
        
        sh = self.getShareService()
        for e in sh.getComments(long(share_id)):
            yield ShareCommentWrapper(self, e)
    
    def getAllMembers(self, share_id):
        """ Get all {@link Experimenter users} who are a member of the share."""
        
        sh = self.getShareService()
        for e in sh.getAllMembers(long(share_id)):
            yield ExperimenterWrapper(self, e)

    def getAllGuests(self, share_id):
        """ Get the email addresses for all share guests."""
        
        sh = self.getShareService()
        return sh.getAllGuests(long(share_id))

    def getAllUsers(self, share_id):
        """ Get a single set containing the login names of the users as well email addresses for guests."""
        
        sh = self.getShareService()
        return sh.getAllUsers(long(share_id))
    
    ##############################################
    ##  Specific Object Getters                 ##
    
    def getGroup(self, gid):
        """ Fetch an Group and all contained users."""
        
        admin_service = self.getAdminService()
        group = admin_service.getGroup(long(gid))
        return ExperimenterGroupWrapper(self, group)
    
    def lookupGroup(self, name):
        """ Look up an Group and all contained users by name."""
        
        admin_service = self.getAdminService()
        group = admin_service.lookupGroup(str(name))
        return ExperimenterGroupWrapper(self, group)
    
    def lookupLdapAuthExperimenter(self, eid):
        """ Looks up all id of experimenters who uses LDAP authentication (has set dn on password table)."""
        
        admin_serv = self.getAdminService()
        return admin_serv.lookupLdapAuthExperimenter(long(eid))
    
    def getDefaultGroup(self, eid):
        """ Retrieve the default group for the given user id."""
        
        admin_serv = self.getAdminService()
        dgr = admin_serv.getDefaultGroup(long(eid))
        return ExperimenterGroupWrapper(self, dgr)
    
    def getOtherGroups(self, eid):
        """ Fetch all groups of which the given user is a member. 
            The returned groups will have all fields filled in and all collections unloaded."""
        
        admin_serv = self.getAdminService()
        for gr in admin_serv.containedGroups(long(eid)):
            yield ExperimenterGroupWrapper(self, gr)
    
    def containedExperimenters(self, gid):
        """ Fetch all users contained in this group. 
            The returned users will have all fields filled in and all collections unloaded."""
        
        admin_serv = self.getAdminService()
        for exp in admin_serv.containedExperimenters(long(gid)):
            yield ExperimenterWrapper(self, exp)
    
    def getGroupsLeaderOf(self):
        """ Look up Groups where current user is a leader of."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in self.getEventContext().leaderOfGroups])
        sql = "select e from ExperimenterGroup as e where e.id in (:ids)"
        for e in q.findAllByQuery(sql, p):
            yield ExperimenterGroupWrapper(self, e)

    def getGroupsMemberOf(self):
        """ Look up Groups where current user is a member of (except "user")."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in self.getEventContext().memberOfGroups])
        sql = "select e from ExperimenterGroup as e where e.id in (:ids)"
        for e in q.findAllByQuery(sql, p):
            if e.name.val == "user":
                pass
            else:
                yield ExperimenterGroupWrapper(self, e)

    def getCurrentSupervisor(self):
        """ Gets the owner of a group for current user."""
        #default = self.getAdminService().getGroup(self.getEventContext().groupId)
        p = omero.sys.Parameters()
        p.map = {}
        p.map["id"] = rlong(self.getEventContext().groupId)
        default = self.getQueryService().findByQuery("select e from ExperimenterGroup as e join fetch e.details.owner where e.id = :id", p)
        return ExperimenterWrapper(self, default.details.owner)
    
    def getColleagues(self):
        """ Look up users who are a member of the current user active group."""
        
        a = self.getAdminService()
        default = self.getAdminService().getGroup(self.getEventContext().groupId)
        for d in default.copyGroupExperimenterMap():
            if d.child.id.val != self.getEventContext().userId:
                yield ExperimenterWrapper(self, d.child)

    def getStaffs(self):
        """ Look up users who are a member of the group owned by the current user."""
        
        q = self.getQueryService()
        gr_list = list()
        gr_list.extend(self.getEventContext().leaderOfGroups)
        p = omero.sys.Parameters()
        p.map = {}
        p.map["gids"] = rlist([rlong(a) for a in set(gr_list)])
        sql = "select e from Experimenter as e where " \
                "exists ( select gem from GroupExperimenterMap as gem where gem.child = e.id and gem.parent.id in (:gids)) order by e.omeName"
        for e in q.findAllByQuery(sql, p):
            if e.id.val != self.getEventContext().userId:
                yield ExperimenterWrapper(self, e)

    def getColleaguesAndStaffs(self):
        """ Look up users who are a member of the current user active group 
            and users who are a member of the group owned by the current user."""
        
        q = self.getQueryService()
        gr_list = list()
        gr_list.extend(self.getEventContext().memberOfGroups)
        gr_list.extend(self.getEventContext().leaderOfGroups)
        p = omero.sys.Parameters()
        p.map = {}
        p.map["gids"] = rlist([rlong(a) for a in set(gr_list)])
        sql = "select e from Experimenter as e where " \
                "exists ( select gem from GroupExperimenterMap as gem where gem.child = e.id and gem.parent.id in (:gids)) order by e.omeName"
        for e in q.findAllByQuery(sql, p):
            if e.id.val != self.getEventContext().userId:
                yield ExperimenterWrapper(self, e)
    
    def getScriptwithDetails(self, sid):
        script_serv = self.getScriptService()
        return script_serv.getScriptWithDetails(long(sid))
    
    # GETTERs
    def getShare (self, oid):
        """ Gets share for the given share id. """
        
        sh_serv = self.getShareService()
        sh = sh_serv.getShare(long(oid))
        if sh is not None:
            return ShareWrapper(self, sh)
        else:
            return None
    
    def getProject (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select pr from Project pr join fetch pr.details.owner join fetch pr.details.group where pr.id=:oid "
        pr = query_serv.findByQuery(sql,p)
        if pr is not None:
            return ProjectWrapper(self, pr)
        else:
            return None
    
    def getScreen (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select sc from Screen sc join fetch sc.details.owner join fetch sc.details.group where sc.id=:oid "
        sc = query_serv.findByQuery(sql,p)
        if sc is not None:
            return ScreenWrapper(self, sc)
        else:
            return None

    def getDataset (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select ds from Dataset ds join fetch ds.details.owner join fetch ds.details.group " \
              "left outer join fetch ds.projectLinks pdl " \
              "left outer join fetch pdl.parent p where ds.id=:oid "
        ds = query_serv.findByQuery(sql,p)
        if ds is not None:
            return DatasetWrapper(self, ds)
        else:
            return None
    
    def getPlate (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select pl from Plate pl join fetch pl.details.owner join fetch pl.details.group " \
              "left outer join fetch pl.screenLinks spl " \
              "left outer join fetch spl.parent sc where pl.id=:oid "
        pl = query_serv.findByQuery(sql,p)
        if pl is not None:
            return PlateWrapper(self, pl)
        else:
            return None

    def getImage (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select im from Image im " \
              "left outer join fetch im.pixels as p " \
              "join fetch im.details.owner join fetch im.details.group " \
              "where im.id=:oid "
        img = query_serv.findByQuery(sql,p)
        
        if img is not None:
            return ImageWrapper(self, img)
        else:
            return None
    
    def getImageWithMetadata (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select im from Image im " \
              "join fetch im.details.owner join fetch im.details.group " \
              "left outer join fetch im.pixels as p " \
              "left outer join fetch im.stageLabel as stageLabel  " \
              "left outer join fetch im.imagingEnvironment as imagingEnvironment " \
              "left outer join fetch im.objectiveSettings as os " \
              "left outer join fetch os.medium as medium " \
              "left outer join fetch os.objective as objective " \
              "left outer join fetch objective.immersion as immersion " \
              "left outer join fetch objective.correction as co " \
              "where im.id=:oid "
        img = query_serv.findByQuery(sql,p)
        if img is not None:
            return ImageWrapper(self, img)
        else:
            return None
    
    def getDatasetImageLink (self, parent, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select dsl from DatasetImageLink as dsl left outer join fetch dsl.child as im \
                left outer join fetch dsl.parent as ds where ds.id=:parent and im.id=:oid"
        dsl = query_serv.findByQuery(sql, p)
        return DatasetImageLinkWrapper(self, dsl)

    def getDatasetImageLinks (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select dsl from DatasetImageLink as dsl left outer join fetch dsl.child as im \
                left outer join fetch dsl.parent as ds where im.id=:oid"
        for dsl in query_serv.findAllByQuery(sql, p):
            yield DatasetImageLinkWrapper(self, dsl)
    
    def getProjectDatasetLink (self, parent, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select pdl from ProjectDatasetLink as pdl left outer join fetch pdl.child as ds \
                left outer join fetch pdl.parent as pr where pr.id=:parent and ds.id=:oid"
        pdl = query_serv.findByQuery(sql, p)
        return ProjectDatasetLinkWrapper(self, pdl)
    
    def getScreenPlateLink (self, parent, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select spl from ScreenPlateLink as spl left outer join fetch spl.child as pl \
                left outer join fetch spl.parent as sc where sc.id=:parent and pl.id=:oid"
        pdl = query_serv.findByQuery(sql, p)
        return ProjectDatasetLinkWrapper(self, pdl)
    
    def getProjectDatasetLinks (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select pdl from ProjectDatasetLink as pdl left outer join fetch pdl.child as ds \
                left outer join fetch pdl.parent as pr where ds.id=:oid"
        for pdl in query_serv.findAllByQuery(sql, p):
            yield ProjectDatasetLinkWrapper(self, pdl)
    
    def getImageAnnotationLink (self, parent, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select ial from ImageAnnotationLink as ial left outer join fetch ial.child as an \
                left outer join fetch ial.parent as im where im.id=:parent and an.id=:oid"
        dsl = query_serv.findByQuery(sql, p)
        return AnnotationLinkWrapper(self, dsl)
    
    def getDatasetAnnotationLink (self, parent, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select dal from DatasetAnnotationLink as dal left outer join fetch dal.child as an \
                left outer join fetch dal.parent as ds where ds.id=:parent and an.id=:oid"
        dsl = query_serv.findByQuery(sql, p)
        return AnnotationLinkWrapper(self, dsl)
    
    def getPlateAnnotationLink (self, parent, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select pal from PlateAnnotationLink as pal left outer join fetch pal.child as an \
                left outer join fetch pal.parent as pl where pl.id=:parent and an.id=:oid"
        dsl = query_serv.findByQuery(sql, p)
        return AnnotationLinkWrapper(self, dsl)
    
    def getProjectAnnotationLink (self, parent, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select pal from ProjectAnnotationLink as pal left outer join fetch pal.child as an \
                left outer join fetch pal.parent as pr where pr.id=:parent and an.id=:oid"
        dsl = query_serv.findByQuery(sql, p)
        return AnnotationLinkWrapper(self, dsl)
    
    def getScreenAnnotationLink (self, parent, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select sal from ScreenAnnotationLink as sal left outer join fetch sal.child as an \
                left outer join fetch sal.parent as sc where pr.id=:parent and sc.id=:oid"
        dsl = query_serv.findByQuery(sql, p)
        return AnnotationLinkWrapper(self, dsl)
    
    def getSpecifiedImages(self, oids):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["ids"] = rlist([rlong(a) for a in oids])
        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group where im.id in (:ids) order by im.name"
        for e in query_serv.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)

    def getSpecifiedDatasets(self, oids):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["ids"] = rlist([rlong(a) for a in oids])
        sql = "select ds from Dataset ds join fetch ds.details.owner join fetch ds.details.group where ds.id in (:ids) order by ds.name"
        for e in query_serv.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)
    
    def getSpecifiedDatasetsWithImages(self, oids):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["ids"] = rlist([rlong(a) for a in oids])
        sql = "select ds from Dataset ds join fetch ds.details.owner join fetch ds.details.group " \
                "left outer join fetch ds.imageLinks dil left outer join fetch dil.child im " \
                "where ds.id in (:ids) order by ds.name"
        for e in query_serv.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)
    
    def getSpecifiedProjects(self, oids):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["ids"] = rlist([rlong(a) for a in oids])
        sql = "select pr from Project pr join fetch pr.details.owner join fetch pr.details.group where pr.id in (:ids) order by pr.name"
        for e in query_serv.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)
    
    def getSpecifiedProjectsWithDatasets(self, oids):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["ids"] = rlist([rlong(a) for a in oids])
        sql = "select pr from Project pr join fetch pr.details.owner join fetch pr.details.group " \
            "left outer join fetch pr.datasetLinks pdl left outer join fetch pdl.child ds " \
            "where pr.id in (:ids) order by pr.name"
        for e in query_serv.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)
    
    def getSpecifiedProjectsWithLeaves(self, oids):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["ids"] = rlist([rlong(a) for a in oids])
        sql = "select pr from Project pr join fetch pr.details.owner join fetch pr.details.group " \
            "left outer join fetch pr.datasetLinks pdl left outer join fetch pdl.child ds " \
            "left outer join fetch ds.imageLinks dil left outer join fetch dil.child im " \
            "where pr.id in (:ids) order by pr.name"
        for e in query_serv.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)
    
    def getCommentAnnotation (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["oid"] = rlong(long(oid))
        sql = "select ca from CommentAnnotation ca where ca.id = :oid"
        ta = query_serv.findByQuery(sql, p)
        return AnnotationWrapper(self, ta)
    
    def getUriAnnotation (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["oid"] = rlong(long(oid))
        sql = "select ua from UriAnnotation ua where ua.id = :oid"
        ta = query_serv.findByQuery(sql, p)
        return AnnotationWrapper(self, ta)
    
    def getTagAnnotation (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["oid"] = rlong(long(oid))
        sql = "select tg from TagAnnotation tg where tg.id = :oid"
        tg = query_serv.findByQuery(sql, p)
        return AnnotationWrapper(self, tg)
    
    def lookupTagsAnnotation (self, names):
        query_serv = self.getQueryService()
        res = list()
        for n in names:
            p = omero.sys.Parameters()
            p.map = {} 
            p.map["text"] = rstring(str(n))
            p.map["eid"] = rlong(self.getEventContext().userId)
            f = omero.sys.Filter()
            f.limit = rint(1)
            p.theFilter = f
            sql = "select tg from TagAnnotation tg " \
                  "where tg.textValue=:text and tg.details.owner.id=:eid and tg.ns is null order by tg.textValue"
            res.append(query_serv.findByQuery(sql, p))
        for e in res:
            if e is None:
                yield None
            else:
                yield AnnotationWrapper(self, e)
    
    def getFileAnnotation (self, oid):
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["oid"] = rlong(long(oid))
        sql = "select f from FileAnnotation f join fetch f.file where f.id = :oid"
        of = query_serv.findByQuery(sql, p)
        if not of.file.format.loaded:
            of.file.format = query_serv.find("Format", of.file.format.id.val)
        return AnnotationWrapper(self, of)
    
    def getFile(self, f_id, size):
        store = self.createRawFileStore()
        store.setFileId(long(f_id))
        buf = 1048576
        if size <= buf:
            return store.read(0,long(size))
        else:
            temp = "%s/%i-%s.download" % (settings.FILE_UPLOAD_TEMP_DIR, size, self._sessionUuid)
            outfile = open (temp, "wb")
            for pos in range(0,long(size),buf):
                data = None
                if size-pos < buf:
                    data = store.read(pos+1, size-pos)
                else:
                    if pos == 0:
                        data = store.read(pos, buf)
                    else:
                        data = store.read(pos+1, buf)
                outfile.write(data)
            outfile.close()
            return temp
        return None
    
    def hasExperimenterPhoto(self, oid=None):
        photo = None
        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations("Experimenter", [self.getEventContext().userId], None, None, None).get(self.getEventContext().userId, [])[0]
            else:
                ann = meta.loadAnnotations("Experimenter", [long(oid)], None, None, None).get(long(oid), [])[0]
            if ann is not None:
                return AnnotationWrapper(self, ann)
            else:
                return None
        except:
            return None
    
    def getExperimenterPhoto(self, oid=None):
        photo = None
        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations("Experimenter", [self.getEventContext().userId], None, None, None).get(self.getEventContext().userId, [])[0]
            else:
                ann = meta.loadAnnotations("Experimenter", [long(oid)], None, None, None).get(long(oid), [])[0]
            store = self.createRawFileStore()
            store.setFileId(ann.file.id.val)
            photo = store.read(0,long(ann.file.size.val))
        except:
            photo = self.getExperimenterDefaultPhoto()
        if photo == None:
            photo = self.getExperimenterDefaultPhoto()
        return photo
    
    def getExperimenterPhotoSize(self, oid=None):
        photo = None
        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations("Experimenter", [self.getEventContext().userId], None, None, None).get(self.getEventContext().userId, [])[0]
            else:
                ann = meta.loadAnnotations("Experimenter", [long(oid)], None, None, None).get(long(oid), [])[0]
            store = self.createRawFileStore()
            store.setFileId(ann.file.id.val)
            photo = store.read(0,long(ann.file.size.val))
            try:
                im = Image.open(StringIO(photo))
            except IOError:
                return None
            else:
                return (im.size, ann.file.size.val)
        except:
            return None
    
    def cropExperimenterPhoto(self, box, oid=None):
        # TODO: crop method could be moved to the server side
        photo = None
        meta = self.getMetadataService()
        ann = None
        try:
            if oid is None:
                ann = meta.loadAnnotations("Experimenter", [self.getEventContext().userId], None, None, None).get(self.getEventContext().userId, [])[0]
            else:
                ann = meta.loadAnnotations("Experimenter", [long(oid)], None, None, None).get(long(oid), [])[0]
            store = self.createRawFileStore()
            store.setFileId(ann.file.id.val)
            photo = store.read(0,long(ann.file.size.val))
        except:
            raise IOError("Photo does not exist.")
        region = None
        try:
            im = Image.open(StringIO(photo))
            region = im.crop(box)
        except IOError:
            raise IOError("Cannot open that photo.")
        else:
            store = self.createRawFileStore()
            store.setFileId(long(ann.file.id.val))
            buf = 1048576
            imdata=StringIO()
            region.save(imdata, format=im.format)
            size = len(imdata.getvalue())
            store.write(imdata.getvalue(), 0, size)
            
            oFile = ann.file
            oFile.setSize(rlong(size));
            self.saveObject(oFile)
            
    def getExperimenterDefaultPhoto(self):
        img = Image.open(settings.DEFAULT_USER)
        img.thumbnail((32,32), Image.ANTIALIAS)
        draw = ImageDraw.Draw(img)
        f = cStringIO.StringIO()
        img.save(f, "PNG")
        f.seek(0)
        return f.read()
    
    def getFileFormt(self, format):
        query_serv = self.getQueryService()
        return query_serv.findByString("Format", "value", format);
    
    def saveFile(self, binary, oFile_id):
        store = self.createRawFileStore()
        store.setFileId(oFile_id);
        pos = 0
        rlen = 0
        
        for chunk in binary.chunks():
            rlen = len(chunk)
            store.write(chunk, pos, rlen)
            pos = pos + rlen
    
    ################################################
    ##   Counters
    
    def getCollectionCount(self, parent, child, ids):
        container = self.getContainerService()
        return container.getCollectionCount(parent, child, ids, None)
    
    ################################################
    ##   Enumeration
    
    def getEnumerationEntries(self, klass):
        types = self.getTypesService()
        for e in types.allEnumerations(str(klass)):
            yield EnumerationWrapper(self, e)
    
    def getEnumeration(self, klass, string):
        types = self.getTypesService()
        obj = types.getEnumeration(str(klass), str(string))
        if obj is not None:
            return EnumerationWrapper(self, obj)
        else:
            return None
    
    def getEnumerationById(self, klass, eid):
        query_serv = self.getQueryService()
        obj =  query_serv.find(klass, long(eid))
        if obj is not None:
            return EnumerationWrapper(self, obj)
        else:
            return None
            
    def getOriginalEnumerations(self):
        types = self.getTypesService()
        rv = dict()
        for e in types.getOriginalEnumerations():
            if rv.get(e.__class__.__name__) is None:
                rv[e.__class__.__name__] = list()
            rv[e.__class__.__name__].append(EnumerationWrapper(self, e))
        return rv
        
    def getEnumerations(self):
        types = self.getTypesService()
        return types.getEnumerationTypes() 
    
    def getEnumerationsWithEntries(self):
        types = self.getTypesService()
        rv = dict()
        for key, value in types.getEnumerationsWithEntries().items():
            r = list()
            for e in value:
                r.append(EnumerationWrapper(self, e))
            rv[key+"I"] = r
        return rv
    
    def deleteEnumeration(self, obj):
        types = self.getTypesService()
        types.deleteEnumeration(obj)
        
    def createEnumeration(self, obj):
        types = self.getTypesService()
        types.createEnumeration(obj)
    
    def resetEnumerations(self, klass):
        types = self.getTypesService()
        types.resetEnumerations(klass)
    
    def updateEnumerations(self, new_entries):
        types = self.getTypesService()
        types.updateEnumerations(new_entries)
        
    ################################################
    ##   Validators     
    
    def checkOmeName(self, ome_name, old_omeName=None):
        if ome_name == old_omeName:
            return False
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["omeName"] = rstring(str(ome_name))
        sql = "select e from Experimenter as e where e.omeName = (:omeName)"
        exps = query_serv.findAllByQuery(sql, p)
        if len(exps) > 0:
            return True
        else:
            return False
    
    def checkGroupName(self, name, old_name=None):
        if name == old_name:
            return False
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["name"] = rstring(str(name))
        sql = "select g from ExperimenterGroup as g where g.name = (:name)"
        grs = query_serv.findAllByQuery(sql, p)
        if len(grs) > 0:
            return True
        else:
            return False
    
    def checkEmail(self, email, old_email=None):
        if email == old_email:
            return False
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["email"] = rstring(str(email))
        sql = "select e from Experimenter as e where e.email = (:email)"
        exps = query_serv.findAllByQuery(sql, p)
        if len(exps) > 0:
            return True
        else:
            return False
    
    ##############################################
    ##   Sets methods                           ##
    
    def createExperimenter(self, experimenter, defaultGroup, otherGroups, password):
        admin_serv = self.getAdminService()
        admin_serv.createExperimenterWithPassword(experimenter, rstring(str(password)), defaultGroup, otherGroups)
    
    def updateExperimenter(self, experimenter, defaultGroup, addGroups, rmGroups, password=None):
        admin_serv = self.getAdminService()
        if password is not None and password!="":
            admin_serv.updateExperimenterWithPassword(experimenter, rstring(str(password)))
        else:
            admin_serv.updateExperimenter(experimenter)
        if len(addGroups) > 0:
            admin_serv.addGroups(experimenter, addGroups)
        admin_serv.setDefaultGroup(experimenter, defaultGroup)
        if len(rmGroups) > 0:
            admin_serv.removeGroups(experimenter, rmGroups)
    
    def setMembersOfGroup(self, group, add_exps, rm_exps):
        admin_serv = self.getAdminService()
        for e in add_exps:
            admin_serv.addGroups(e, [group])
        for e in rm_exps:
            admin_serv.removeGroups(e, [group])
    
    def deleteExperimenter(self, experimenter):
        admin_serv = self.getAdminService()
        admin_serv.deleteExperimenter(experimenter)
    
    def createGroup(self, group, group_owner):
        admin_serv = self.getAdminService()
        gr_id = admin_serv.createGroup(group)
        new_gr = admin_serv.getGroup(gr_id)
        admin_serv.setGroupOwner(new_gr, group_owner)
    
    def updateGroup(self, group, group_owner):
        admin_serv = self.getAdminService()
        admin_serv.updateGroup(group)
        admin_serv.setGroupOwner(group, group_owner)
    
    def updateMyAccount(self, experimenter, defultGroup, password=None):
        admin_serv = self.getAdminService()
        admin_serv.updateSelf(experimenter)
        admin_serv.setDefaultGroup(experimenter, defultGroup)
        if password is not None and password!="":
            admin_serv.changePassword(rstring(str(password)))
    
    def saveObject (self, obj):
        u = self.getUpdateService()
        u.saveObject(obj)
    
    def saveArray (self, objs):
        u = self.getUpdateService()
        u.saveArray(objs)
    
    def saveAndReturnObject (self, obj):
        u = self.getUpdateService()
        res = u.saveAndReturnObject(obj)
        obj = omero.gateway.BlitzObjectWrapper(self, res)
        return obj
    
    def deleteObject(self, obj):
        u = self.getUpdateService()
        u.deleteObject(obj)
    
    def addComment(self, host, blitz_id, share_id, comment):
        sh = self.getShareService()
        new_cm = sh.addComment(long(share_id), str(comment))
        
        members = list(self.getAllMembers(long(share_id)))
        sh = self.getShare(long(share_id))
        if self.getEventContext().userId != sh.owner.id.val:
            members.append(sh.getOwnerAsExperimetner())
        
        if sh.active:
            if settings.EMAIL_NOTIFICATION:
                try:
                    for m in members:
                        try:
                            if m.id == self.getEventContext().userId:
                                members.remove(m)
                        except:
                            logger.error(traceback.format_exc())
                    from omeroweb.feedback.notification.sendemail import prepareRecipientsAsString
                    recipients = prepareRecipientsAsString(members)
                except Exception, x:
                    logger.error(x)
                    logger.error(traceback.format_exc())
                else:
                    from omeroweb.webadmin.models import Gateway
                    blitz = Gateway.objects.get(id=blitz_id)
                    from omeroweb.feedback.models import EmailToSend, EmailTemplate
                    t = EmailTemplate.objects.get(template="add_comment_to_share")
                    message = t.content_txt % (settings.APPLICATION_HOST, share_id, blitz_id)
                    message_html = t.content_html % (settings.APPLICATION_HOST, share_id, blitz_id, settings.APPLICATION_HOST, share_id, blitz_id)
                    e = EmailToSend(recipients=recipients, template=t, sender=self.getUser().getFullName(), sender_email=self.getUser().email, message=message, message_html=message_html)
                    e.save()
    
    def createShare(self, host, blitz_id, imageInBasket, message, members, enable, expiration=None):
        sh = self.getShareService()
        q = self.getQueryService()
        
        items = list()
        ms = list()
        p = omero.sys.Parameters()
        p.map = {} 
        #images
        if len(imageInBasket) > 0:
            p.map["ids"] = rlist([rlong(long(a)) for a in imageInBasket])
            sql = "select i from Image as i " \
                  "left outer join fetch i.pixels as p " \
                  "left outer join fetch p.pixelsType as pt " \
                  "left outer join fetch p.channels as c " \
                  "left outer join fetch c.logicalChannel as lc " \
                  "left outer join fetch c.statsInfo " \
                  "left outer join fetch lc.photometricInterpretation " \
                  "left outer join fetch p.thumbnails as thumb join fetch thumb.details.updateEvent " \
                  "left outer join fetch p.settings as rdef join fetch rdef.details.updateEvent join fetch rdef.details.owner " \
                  "left outer join fetch rdef.quantization " \
                  "left outer join fetch rdef.model " \
                  "left outer join fetch rdef.waveRendering as cb " \
                  "left outer join fetch cb.family " \
                  "left outer join fetch rdef.spatialDomainEnhancement " \
                  "where i.id in (:ids)"
            items.extend(q.findAllByQuery(sql, p))
        
        #members
        if members is not None:
            p.map["ids"] = rlist([rlong(long(a)) for a in members])
            sql = "select e from Experimenter e " \
                  "where e.id in (:ids) order by e.omeName"
            ms = q.findAllByQuery(sql, p)
        sid = sh.createShare(message, expiration, items, ms, [], enable)
        for ob in items:
            sh.addObject(sid, ob)
        
        #send email if avtive
        if enable:
            if settings.EMAIL_NOTIFICATION:
                try:
                    from omeroweb.feedback.notification.sendemail import prepareRecipientsAsString
                    recipients = prepareRecipientsAsString(ms)
                except Exception, x:
                    logger.error(x)
                    logger.error(traceback.format_exc())
                else:
                    from omeroweb.feedback.models import EmailToSend, EmailTemplate
                    t = EmailTemplate.objects.get(template="create_share")
                    message = t.content_txt % (settings.APPLICATION_HOST, sid, blitz_id, self.getUser().getFullName())
                    message_html = t.content_html % (settings.APPLICATION_HOST, sid, blitz_id, settings.APPLICATION_HOST, sid, blitz_id, self.getUser().getFullName())
                    e = EmailToSend(recipients=recipients, template=t, sender=self.getUser().getFullName(), sender_email=self.getUser().email, message=message, message_html=message_html)
                    e.save()
    
    def updateShareOrDiscussion (self, host, blitz_id, share_id, message, add_members, rm_members, enable, expiration=None):
        sh = self.getShareService()
        sh.setDescription(long(share_id), message)
        sh.setExpiration(long(share_id), expiration)
        sh.setActive(long(share_id), enable)
        if len(add_members) > 0:
            sh.addUsers(long(share_id), add_members)
        if len(rm_members) > 0:
            sh.removeUsers(long(share_id), rm_members)
        
        #send email if avtive
        if settings.EMAIL_NOTIFICATION:
		if len(add_members) > 0:
                    try:
	                from omeroweb.feedback.notification.sendemail import prepareRecipientsAsString
	                recipients = prepareRecipientsAsString(add_members)
	            except Exception, x:
	                logger.error(x)
	                logger.error(traceback.format_exc())
	            else:
	                from omeroweb.webadmin.models import Gateway
	                blitz = Gateway.objects.get(id=blitz_id)
	                from omeroweb.feedback.models import EmailToSend, EmailTemplate
	                t = EmailTemplate.objects.get(template="add_member_to_share")
	                message = t.content_txt % (settings.APPLICATION_HOST, share_id, blitz_id, self.getUser().getFullName())
                    message_html = t.content_html % (settings.APPLICATION_HOST, share_id, blitz_id, settings.APPLICATION_HOST, share_id, blitz_id, self.getUser().getFullName())
                    e = EmailToSend(recipients=recipients, template=t, sender=self.getUser().getFullName(), sender_email=self.getUser().email, message=message, message_html=message_html)
                    e.save()
			
                if len(rm_members) > 0:
                    try:
	                from omeroweb.feedback.notification.sendemail import prepareRecipientsAsString
	                recipients = prepareRecipientsAsString(rm_members)
	            except Exception, x:
	                logger.error(x)
	                logger.error(traceback.format_exc())
	            else:
	                from omeroweb.webadmin.models import Gateway
	                blitz = Gateway.objects.get(id=blitz_id)
	                from omeroweb.feedback.models import EmailToSend, EmailTemplate
	                t = EmailTemplate.objects.get(template="remove_member_from_share")
                    message = t.content_txt % (settings.APPLICATION_HOST, share_id, blitz_id)
                    message_html = t.content_html % (settings.APPLICATION_HOST, share_id, blitz_id, settings.APPLICATION_HOST, share_id, blitz_id)
                    e = EmailToSend(recipients=recipients, template=t, sender=self.getUser().getFullName(), sender_email=self.getUser().email, message=message, message_html=message_html)
                    e.save()
    
    def setFile(self, buf):
        f = self.createRawFileStore()
        f.write(buf)
    
    
    ##############################################
    ##  History methods                        ##
    
    def getLastAcquiredImages (self):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(6)
        p.theFilter = f
        for e in tm.getMostRecentObjects(['Image'], p, False)["Image"]:
            yield ImageWrapper(self, e)
    
    def getLastImportedImages (self):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(6)
        p.theFilter = f
        for e in tm.getMostRecentObjects(['Image'], p, False)["Image"]:
            yield ImageWrapper(self, e)
    
    def getMostRecentShares (self):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentShareCommentLinks(p):
            yield SessionAnnotationLinkWrapper(self, e)
    
    def getMostRecentSharesComments (self):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentShareCommentLinks(p):
            yield SessionAnnotationLinkWrapper(self, e)
    
    def getMostRecentComments (self):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentAnnotationLinks(None, ['CommentAnnotation'], None, p):
            yield AnnotationLinkWrapper(self, e)
    
    def getMostRecentTagLinks (self):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(200)
        p.theFilter = f
        for e in tm.getMostRecentAnnotationLinks(None, ['TagAnnotation'], None, p):
            yield AnnotationLinkWrapper(self, e)
    
    def getDataByPeriod (self, start, end, date_type=None, page=None):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        if page is not None:
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
        else:
            f.limit = rint(100)
        p.theFilter = f
        im_list = list()
        ds_list = list()
        pr_list = list()
        if date_type == 'image':
            try:
                for e in tm.getByPeriod(['Image'], rtime(long(start)), rtime(long(end)), p, True)['Image']:
                    im_list.append(ImageWrapper(self, e))
            except:
                pass
        elif date_type == 'dataset':
            try:
                for e in tm.getByPeriod(['Dataset'], rtime(long(start)), rtime(long(end)), p, True)['Dataset']:
                    ds_list.append(DatasetWrapper(self, e))
            except:
                pass
        elif date_type == 'project':
            try:
                for e in tm.getByPeriod(['Project'], rtime(long(start)), rtime(long(end)), p, True)['Project']:
                    pr_list.append(ImageWrapper(self, e))
            except:
                pass
        else:
            res = tm.getByPeriod(['Image', 'Dataset', 'Project'], rtime(long(start)), rtime(long(end)), p, True)
            try:
                for e in res['Image']:
                    im_list.append(ImageWrapper(self, e))
            except:
                pass
            try:
                for e in res['Dataset']:
                    ds_list.append(DatasetWrapper(self, e))
            except:
                pass
            try:
                for e in res['Project']:
                    pr_list.append(ProjectWrapper(self, e))
            except:
                pass
        return {'project': pr_list, 'dataset':ds_list, 'image':im_list}
    
    def countDataByPeriod (self, start, end, date_type=None):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        p.theFilter = f
        if date_type == 'image':
            return tm.countByPeriod(['Image'], rtime(long(start)), rtime(long(end)), p)['Image']
        elif date_type == 'dataset':
            return tm.countByPeriod(['Dataset'], rtime(long(start)), rtime(long(end)), p)['Dataset']
        elif date_type == 'project':
            return tm.countByPeriod(['Project'], rtime(long(start)), rtime(long(end)), p)['Project']
        else:
            c = tm.countByPeriod(['Image', 'Dataset', 'Project'], rtime(long(start)), rtime(long(end)), p)
            return c['Image']+c['Dataset']+c['Project']

    def getEventsByPeriod (self, start, end):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.limit = rint(100000)
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        p.theFilter = f
        return tm.getEventLogsByPeriod(rtime(start), rtime(end), p)
        #yield EventLogWrapper(self, e)
    
    ##############################################
    ##  Search methods                          ##

    def searchImages (self, query=None, created=None):
        search = self.createSearchService()
        search.onlyType('Image')
        search.addOrderByAsc("name")
        if created:
            search.onlyCreatedBetween(created[0], created[1]);
        if query:
           search.setAllowLeadingWildcard(True)
           search.byFullText(str(query))
        if search.hasNext():
            for e in search.results():
                yield ImageWrapper(self, e)

    def searchDatasets (self, query=None, created=None):
        search = self.createSearchService()
        search.onlyType('Dataset')
        search.addOrderByAsc("name")
        if query:
            search.setAllowLeadingWildcard(True)
            search.byFullText(str(query))
        if search.hasNext():
            for e in search.results():
                yield DatasetWrapper(self, e)

    def searchProjects (self, query=None, created=None):
        search = self.createSearchService()
        search.onlyType('Project')
        search.addOrderByAsc("name")
        if query:
           search.setAllowLeadingWildcard(True)
           search.byFullText(str(query))
        if search.hasNext():
            for e in search.results():
                yield ProjectWrapper(self, e)
    
    def downloadPlane(self, oid, z, c, t):
        p = ParametersI().leaves()        
        image = self.getContainerService().getImages('Image', [long(oid)], p)[0]
        pixels = image.getPrimaryPixels()
        rp = self.createRawPixelsStore()
        rp.setPixelsId(pixels.getId().val, True)
        from omero.util.script_utils import downloadPlane
    	return downloadPlane(rp, pixels, z,c,t-1);
    
    ##############################################
    ##  helpers                                 ##
    
    def bytesPerPixel(self, pixel_type):
        if pixel_type.val == "int8" or pixel_type.val == "uint8":
            return 1
        elif pixel_type.val == "int16" or pixel_type.val == "uint16":
            return 2
        elif pixel_type.val == "int32" or pixel_type.val == "uint32" or pixel_type.val == "float":
            return 4
        elif pixel_type.val == "double":
            return 8;
        else:
            logger.error("Error: Unknown pixel type: %s" %s (pixel_type.val))
            logger.error(traceback.format_exc())
            raise AttributeError("Unknown pixel type: %s" %s (pixel_type.val))
    

omero.gateway.BlitzGateway = OmeroWebGateway

#from webgateway.views import getBlitzConnection, timeit, _session_logout, _createConnection

#
###############################################

#def safeCallWrap (self, attr, f):
#    def wrapped (*args, **kwargs):
#        try:
#            return f(*args, **kwargs)
#        except omero.ResourceError, x:
#            logger.error(x.message)
#            raise AttributeError(x.message)
#        except omero.SecurityViolation, x:
#            logger.error(x.message)
#            if self._conn._shareId is not None:
#                self._conn._sessionUuid = None
#                self._connect(self._conn._shareId)
#                func = getattr(self._obj, attr)
#                return func(*args, **kwargs)
#        except Ice.Exception, x:
#            # Failed
#            logger.info("Ice.Exception (1) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs)))
#            ...

#class ProxyObjectWrapper (object):
#    ...
#    def _connect (self, share_id=None):
#        logger.info("proxy_connect: connect");
#        if share_id is not None:
#            if not self._conn.connectAsShare(share_id):
#                return False
#        else:
#            if not self._conn.connect():
#                return False
#        logger.info("proxy_connect: sf");
#        self._sf = self._conn.c.sf
#        logger.info("proxy_connect: create_func");
#        ...

##########################################################################
# Wrrapers

class OmeroWebObjectWrapper (object):

    child_counter = None
    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        try:
            self.child_counter = kwargs['child_counter']
        except:
            pass
        try:
            self.annotation_counter = kwargs['annotation_counter']
        except:
            pass
    
    def countChild (self):
        #return len(list(self.listChildren()))
        logger.debug(str(self)+'.countChild')
        if self.child_counter is not None:
            return self.child_counter
        else:
            container = self._conn.getContainerService()
            m = container.getCollectionCount(self._obj.__class__.__name__, (self.LINK_NAME[4].lower()+self.LINK_NAME[5:]), [self._oid], None)
            if m[self._oid] > 0:
                self.child_counter = m[self._oid]
                return self.child_counter
            else:
                return 0
    
    def listAnnotations (self):
        #container = self._conn.getContainerService()
        meta = self._conn.getMetadataService()
        self.annotations = meta.loadAnnotations(self._obj.__class__.__name__, [self._oid], None, None, None).get(self._oid, [])
        for ann in self.annotations:
            yield AnnotationWrapper(self._conn, ann)
    
    def countAnnotations (self):
        if self.annotation_counter is not None:
            return self.annotation_counter
        else:
            container = self._conn.getContainerService()
            m = container.getCollectionCount(self._obj.__class__.__name__, type(self._obj).ANNOTATIONLINKS, [self._oid], None)
            if m[self._oid] > 0:
                self.annotation_counter = m[self._oid]
                return self.annotation_counter
            else:
                return None
    
    def isOwned(self):
        return (self._obj.details.owner.id.val == self._conn.getEventContext().userId)
    
    def accessControll(self):
        if self._obj.details.permissions.isUserRead() and self._obj.details.permissions.isUserWrite():
            return '0'
        elif self._obj.details.permissions.isGroupRead() and self._obj.details.permissions.isGroupWrite():
            return '1'
        elif self._obj.details.permissions.isWorldRead() and self._obj.details.permissions.isWorldWrite():
            return '2'
        else:
            return '-1'

    def splitedName(self):
        try:
            name = self._obj.name.val
            l = len(name)
            if l < 45:
                return name
            elif l >= 45:
                splited = []
                for v in range(0,len(name),45):
                    splited.append(name[v:v+45]+"\n")
                return "".join(splited)
        except:
            logger.info(traceback.format_exc())
            return self._obj.name.val
    
    def fullNameWrapped(self):
        try:
            name = self._obj.name.val
            l = len(name)
            if l <= 60:
                return name
            elif l > 60:
                splited = []
                for v in range(0,len(name),60):
                    splited.append(name[v:v+60]+"\n")
                return "".join(splited)
        except:
            logger.info(traceback.format_exc())
            return self._obj.name.val
    
    def shortName(self):
        try:
            name = self._obj.name.val
            l = len(name)
            if l < 55:
                return name
            return "..." + name[l - 55:]
        except:
            logger.info(traceback.format_exc())
            return self._obj.name.val
    
    def tinyName(self):
        try:
            name = self._obj.name.val
            l = len(name)
            if l <= 20:
                return name
            elif l > 20 and l <= 40:
                splited = []
                for v in range(0,len(name),20):
                    splited.append(name[v:v+20]+"\n")
                return "".join(splited)
            elif l > 40:
                nname = "..." + name[l - 36:]
                splited = list()
                for v in range(0,len(nname),20):
                    splited.append(nname[v:v+20]+"\n")
                return "".join(splited)
        except:
            logger.info(traceback.format_exc())
            return self._obj.name.val
    
    def breadcrumbName(self):
        name = None
        try:
            name = self._obj.name.val
            l = len(name)
            if l <= 20:
                return name
            elif l > 20 and l < 30:
                splited = []
                for v in range(0,len(name),20):
                    splited.append(name[v:v+20])
                return "".join(splited)
            elif l >= 30:
                nname = "..." + name[l - 30:]
                return nname
        except:
            name = self._obj.textValue.val
            l = len(name)
            if l <= 100:
                return name
            elif l > 100:
                return name[:45] + "..." + name[l - 45:]
        return None
    
    def shortDescription(self):
        try:
            desc = self._obj.description
            if desc == None or desc.val == "":
                return None
            l = len(desc.val)
            if l < 550:
                return desc.val
            return desc.val[:550] + "..."
        except:
            logger.info(traceback.format_exc())
            return self._obj.description.val
    
    def tinyDescription(self):
        try:
            desc = self._obj.description
            if desc == None or desc.val == "":
                return None
            l = len(desc.val)
            if l <= 28:
                return desc.val
            return desc.val[:28] + "..."
        except:
            logger.info(traceback.format_exc())
            return self._obj.description.val

class ExperimenterWrapper (OmeroWebObjectWrapper, omero.gateway.ExperimenterWrapper):
#    LINK_NAME = "copyGroupExperimenterMap"
#    OMERO_CLASS = 'Experimetner'
#    PARENT_WRAPPER_CLASS = 'ExperimenterGroup'
#    
    def shortInstitution(self):
        try:
            inst = self._obj.institution
            if inst == None or inst.val == "":
                return "-"
            l = len(inst.val)
            if l < 30:
                return inst.val
            return inst.val[:30] + "..."
        except:
            logger.error(traceback.format_exc())
            return None

    def getFullName(self):
        try:
            if self.middleName is not None and self.middleName != '':
                name = "%s %s. %s" % (self.firstName, self.middleName[:1], self.lastName)
            else:
                name = "%s %s" % (self.firstName, self.lastName)
            
            l = len(name)
            if l < 40:
                return name
            return name[:40] + "..."
        except:
            logger.error(traceback.format_exc())
            return _("Unknown name")

omero.gateway.ExperimenterWrapper = ExperimenterWrapper

class ExperimenterGroupWrapper (OmeroWebObjectWrapper, omero.gateway.ExperimenterGroupWrapper):
    pass

omero.gateway.ExperimenterGroupWrapper = ExperimenterGroupWrapper

#    LINK_NAME = "copyGroupExperimenterMap"
#    OMERO_CLASS = 'ExperimenterGroup'
#    LINK_CLASS = 'GroupExperimenterMap'
#    CHILD_WRAPPER_CLASS = 'Experimenter'
#    
#class GroupWrapper (BlitzObjectWrapper):
#    LINK_CLASS = None
#    CHILD_WRAPPER_CLASS = None

class ScriptWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    pass

class AnnotationLinkWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):

    def getAnnotation(self):
        return omero.gateway.AnnotationWrapper(self, self.child)

class AnnotationWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
    def isOriginalMetadat(self):
        if isinstance(self._obj, FileAnnotationI):
            try:
                if self._obj.ns.val == omero.constants.namespaces.NSCOMPANIONFILE and self._obj.file.name.val.startswith("original_metadata"):
                    return True
            except:
                logger.info(traceback.format_exc())
        return False
     
    def getFileSize(self):
        if isinstance(self._obj, FileAnnotationI):
            return self._obj.file.size.val

    def getFileName(self):
        if isinstance(self._obj, FileAnnotationI):
            try:
                name = self._obj.file.name.val
                l = len(name)
                if l < 65:
                    return name
                return name[:30] + "..." + name[l - 30:] 
            except:
                logger.info(traceback.format_exc())
                return self._obj.file.name.val
    
    def shortTag(self):
        if isinstance(self._obj, TagAnnotationI):
            try:
                name = self._obj.textValue.val
                l = len(name)
                if l < 17:
                    return name
                return name[:7] + "..." + name[l - 7:] 
            except:
                logger.info(traceback.format_exc())
                return self._obj.textValue.val

class ImageImagingEnvironmentWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageObjectiveSettingsWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageMediumWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageObjectiveWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageImmersionWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageCorrectionWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageInstrumentWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageFilterWrapper (omero.gateway.BlitzObjectWrapper):
    
    def getTransmittanceRange(self):
        if self._obj.transmittanceRange is None:
            return None
        else:
            return FilterTransmittanceRangeWrapper(self._conn, self._obj.transmittanceRange)

    def getFilterType(self):
        if self._obj.type is None:
            return None
        else:
            return TypeWrapper(self._conn, self._obj.type)

class FilterTransmittanceRangeWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageDetectorWrapper (omero.gateway.BlitzObjectWrapper):
    
    def getDetectorType(self):
        if self._obj.type is None:
            return None
        else:
            return TypeWrapper(self._conn, self._obj.type)
   
class TypeWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageStageLabelWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageWrapper (OmeroWebObjectWrapper, omero.gateway.ImageWrapper):
    
    def getThumbnail (self, size=(120,120)):
        rv = super(omero.gateway.ImageWrapper, self).getThumbnail(size=size)
        if rv is None:
            try:
                rv = self.defaultThumbnail(size)
            except Exception, e:
                logger.info(traceback.format_exc())
                raise e
        return rv
    
    def defaultThumbnail(self, size=(120,120)):
        img = Image.open(settings.DEFAULT_IMG)
        img.thumbnail(size, Image.ANTIALIAS)
        draw = ImageDraw.Draw(img)
        f = cStringIO.StringIO()
        img.save(f, "PNG")
        f.seek(0)
        return f.read()
    
    def getDateAsTimestamp(self):
        try:
            return time.ctime(self._obj.acquisitionDate.val / 1000)
        except:
            logger.info(traceback.format_exc())
            return _("unknown")
    
    # metadata getters
    # from metadata service
    def getMicroscopInstruments(self):
        meta_serv = self._conn.getMetadataService()
        if self._obj.instrument is None:
            yield None
        else:
            for inst in meta_serv.loadInstrument(self._obj.instrument.id.val):
                if isinstance(inst, InstrumentI):
                    yield ImageInstrumentWrapper(self._conn, inst)
    
    def getMicroscopDetectors(self):
        meta_serv = self._conn.getMetadataService()
        if self._obj.instrument is None:
            yield None
        else:
            for inst in meta_serv.loadInstrument(self._obj.instrument.id.val):
                if isinstance(inst, DetectorI):
                    yield ImageDetectorWrapper(self._conn, inst)
    
    def getMicroscopFilters(self):
        meta_serv = self._conn.getMetadataService()
        if self._obj.instrument is None:
            yield None
        else:
            for inst in meta_serv.loadInstrument(self._obj.instrument.id.val):
                if isinstance(inst, FilterI):
                    yield ImageFilterWrapper(self._conn, inst)
    
    # from model
    def getImagingEnvironment(self):
        if self._obj.imagingEnvironment is None:
            return None
        else:
            return ImageImagingEnvironmentWrapper(self._conn, self._obj.imagingEnvironment)
    
    def getObjectiveSettings(self):
        if self._obj.objectiveSettings is None:
            return None
        else:
            return ImageObjectiveSettingsWrapper(self._conn, self._obj.objectiveSettings)
    
    def getMedium(self):
        if self._obj.objectiveSettings.medium is None:
            return None
        else:
            return ImageMediumWrapper(self._conn, self._obj.objectiveSettings.medium)

    def getObjective(self):
        if self._obj.objectiveSettings.objective is None:
            return None
        else:
            return ImageObjectiveWrapper(self._conn, self._obj.objectiveSettings.objective)
    
    def getImmersion(self):
        if self._obj.objectiveSettings.objective.immersion is None:
            return None
        else:
            return ImageImmersionWrapper(self._conn, self._obj.objectiveSettings.objective.immersion)
    
    def getCorrection(self):
        if self._obj.objectiveSettings.objective.correction is None:
            return None
        else:
            return ImageCorrectionWrapper(self._conn, self._obj.objectiveSettings.objective.correction)

    def getStageLabel (self):
        if self._obj.stageLabel is None:
            return None
        else:
            return ImageStageLabelWrapper(self._conn, self._obj.stageLabel)

omero.gateway.ImageWrapper = ImageWrapper
    
class ChannelWrapper (omero.gateway.ChannelWrapper):
            
    def getLogicalChannel(self):
        if self._obj.logicalChannel is None:
            return None
        else:
            return LogicalChannelWrapper(self._conn, self._obj.logicalChannel)
            
omero.gateway.ChannelWrapper = ChannelWrapper

class LogicalChannelWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    pass

class DatasetWrapper (OmeroWebObjectWrapper, omero.gateway.DatasetWrapper):
    LINK_NAME = "copyImageLinks"
    CHILD = 'Image'
    
    def __repr__ (self):
        if hasattr(self, '_oid'):
            return '<OmeroWeb%s id=%s>' % (self.__class__.__name__, str(self._oid))
        return super(DatasetWrapper, self).__repr__()
    
    def listChildren (self):
        """ return a generator yielding child objects """
        try:
            childnodes = [ x.child for x in getattr(self._obj, self.LINK_NAME)()]

            child_ids = [child.id.val for child in childnodes]
            child_counter = None
            if len(child_ids) > 0:
                child_annotation_counter = self._conn.getCollectionCount(self.CHILD, "annotationLinks", child_ids)
            for child in childnodes:
                kwargs = dict()
                if child_annotation_counter:
                    kwargs['annotation_counter'] = child_annotation_counter.get(child.id.val)
                yield ImageWrapper(self._conn, child, **kwargs)
        except:
            raise NotImplementedError

omero.gateway.DatasetWrapper = DatasetWrapper

class DatasetImageLinkWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    pass

class ProjectDatasetLinkWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    pass
    
class ProjectWrapper (OmeroWebObjectWrapper, omero.gateway.ProjectWrapper):
    LINK_NAME = "copyDatasetLinks"
    CHILD = 'Dataset'
    
    def listChildren (self):
        """ return a generator yielding child objects """
        try:
            childnodes = [ x.child for x in getattr(self._obj, self.LINK_NAME)()]

            child_ids = [child.id.val for child in childnodes]
            child_counter = None
            if len(child_ids) > 0:
                child_counter = self._conn.getCollectionCount(self.CHILD, \
                    (DatasetWrapper.LINK_NAME[4].lower() + \
                    DatasetWrapper.LINK_NAME[5:]), child_ids)
                child_annotation_counter = self._conn.getCollectionCount(self.CHILD, "annotationLinks", child_ids)
            for child in childnodes:
                kwargs = dict()
                if child_counter:
                    kwargs['child_counter'] = child_counter.get(child.id.val)
                if child_annotation_counter:
                    kwargs['annotation_counter'] = child_annotation_counter.get(child.id.val)
                yield DatasetWrapper(self._conn, child, **kwargs)
        except:
            raise NotImplementedError

omero.gateway.ProjectWrapper = ProjectWrapper

class ScreenWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    LINK_NAME = "copyPlateLinks"
    CHILD = 'Plate'
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Screen'
        self.LINK_CLASS = "ScreenPlateLink"
        self.CHILD_WRAPPER_CLASS = 'PlateWrapper'
        self.PARENT_WRAPPER_CLASS = None
        
    def listChildren (self):
        """ return a generator yielding child objects """
        try:
            childnodes = [ x.child for x in getattr(self._obj, self.LINK_NAME)()]

            child_ids = [child.id.val for child in childnodes]
            child_counter = None
            if len(child_ids) > 0:
                child_counter = self._conn.getCollectionCount(self.CHILD, \
                    (PlateWrapper.LINK_NAME[4].lower() + \
                    PlateWrapper.LINK_NAME[5:]), child_ids)
                child_annotation_counter = self._conn.getCollectionCount(self.CHILD, "annotationLinks", child_ids)
            for child in childnodes:
                kwargs = dict()
                if child_counter:
                    kwargs['child_counter'] = child_counter.get(child.id.val)
                if child_annotation_counter:
                    kwargs['annotation_counter'] = child_annotation_counter.get(child.id.val)
                yield PlateWrapper(self._conn, child, **kwargs)
        except:
            raise NotImplementedError

ScreenWrapper = ScreenWrapper

class PlateWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    LINK_NAME = None
    CHILD = None
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Plate'
        self.LINK_CLASS = None
        self.CHILD_WRAPPER_CLASS = None
        self.PARENT_WRAPPER_CLASS = "ScreenWrapper"

PlateWrapper = PlateWrapper

class ShareWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    members_counter = None
    annotation_counter = None
    
    def shortMessage(self):
        try:
            msg = self.getMessage().val
            l = len(msg)
            if l < 50:
                return msg
            return msg[:50] + "..."
        except:
            logger.info(traceback.format_exc())
            return None
    
    def tinyMessage(self):
        try:
            msg = self.getMessage().val
            l = len(msg)
            if l < 20:
                return msg
            elif l >= 20:
                return "%s..." % (msg[:20])
        except:
            logger.info(traceback.format_exc())
            return None
    
    def getShareType(self):
        if self.itemCount == 0:
            return "Discuss"
        else:
            return "Share"
    
    def getMembersCount(self):
        return "None"
        
    def getStartDate(self):
        return datetime.fromtimestamp(self.getStarted().val/1000)
        
    def getExpirationDate(self):
        try:
            return datetime.fromtimestamp((self.getStarted().val+self.getTimeToLive().val)/1000)
        except ValueError:
            return None
        except:
            return None
    
    def isExpired(self):
        try:
            if (self.getStarted().val+self.getTimeToLive().val)/1000 <= time.time():
                return True
            else:
                return False
        except:
            return True
        
    # Owner methods had to be updated because share.details.owner does not exist. Share.owner.
    def isOwned(self):
        if self.owner.id.val == self._conn.getEventContext().userId:
            return True
        else:
            return False
    
    def getOwnerAsExperimetner(self):
        return omero.gateway.ExperimenterWrapper(self, self.owner)
    
    def getShareOwnerFullName(self):
        try:
            lastName = self.owner.lastName and self.owner.lastName.val or ''
            firstName = self.owner.firstName and self.owner.firstName.val or ''
            middleName = self.owner.middleName and self.owner.middleName.val or ''
            
            if middleName is not None and middleName != '':
                name = "%s %s. %s" % (firstName, middleName, lastName)
            else:
                name = "%s %s" % (firstName, lastName)
            return name
        except:
            logger.info(traceback.format_exc())
            return None
    
class ShareContentWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    pass

class ShareCommentWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    pass
    
class SessionAnnotationLinkWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    def getComment(self):
        return ShareCommentWrapper(self._conn, self.child)
    
    def getShare(self):
        return ShareWrapper(self._conn, self.parent)
    
class EventLogWrapper (omero.gateway.BlitzObjectWrapper):
    LINK_CLASS = "EventLog"

class EnumerationWrapper (omero.gateway.BlitzObjectWrapper):
    
    def getType(self):
        return self._obj.__class__
