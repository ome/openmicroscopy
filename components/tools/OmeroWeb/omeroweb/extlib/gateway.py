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

import sys
sys.path.append('icepy')
sys.path.append('lib')

import cStringIO
import traceback
import logging

logger = logging.getLogger('gateway')

try:
    import Image,ImageDraw
except:
    logger.error(traceback.format_exc())

import threading
import time
from datetime import datetime
from types import IntType, ListType, TupleType, UnicodeType, StringType

from django.utils.translation import ugettext as _
from django.conf import settings

import Ice
import Glacier2
import omero
import omero_api_IScript_ice
from omero.rtypes import *

from omero_model_FileAnnotationI import FileAnnotationI
from omero_model_TagAnnotationI import TagAnnotationI
from omero_model_DatasetI import DatasetI
from omero_model_ProjectI import ProjectI
from omero_model_ImageI import ImageI

TIMEOUT = 580 #sec
SLEEPTIME = 30

class BlitzGateway (threading.Thread):

    def __init__ (self, host, port, username, passwd, sessionUuid=None):
        super(BlitzGateway, self).__init__()
        self.setDaemon(True)
        self.client = None
        self.c = omero.client(host=str(host), port=int(port))
        self._sessionUuid = sessionUuid

        # The properties we are setting through the interface
        self._props = {'host': str(host), 'port': int(port),
                        omero.constants.USERNAME: str(username), omero.constants.PASSWORD: str(passwd)}
        
        self._connected = False
        self._user = None
        self._eventContext = None
        self._shareId = None
        self.allow_thread_timeout = True
        self.updateTimeout()
        self.start()
    
    def updateTimeout (self):
        self._timeout = time.time() + TIMEOUT
    
    def isTimedout (self):
        if self._timeout < time.time():
            return True
        #self.updateTimeout()
        return False
    
    def run (self):
        """ this thread lives forever, pinging whatever connection exists to keep it's services alive """
        logger.info("Starting thread...")
        while not (self.allow_thread_timeout and self.isTimedout()):
            try:
                time.sleep(SLEEPTIME)
                if self._connected:
                    logger.info("Ping...")
                    for k,v in self._proxies.items():
                        logger.info("Sending keepalive to '%s'" % k)
                        if not v._ping():
                            logger.info("... some error sending keepalive to '%s'" % k)
                            # connection should have been recreated and proxies are different now, so start all over
                            break
            except:
                logger.error("Something bad on the client proxy keepalive thread")
                logger.error(traceback.format_exc())
        if self._connected:
            self.seppuku()
        logger.info("Thred death")
    
    def seppuku (self):
        try:
            logger.info("Connection will be closed [%s]" % (self.c.getRouter(self.c.ic)))
        except:
            logger.info("Connection will be closed.")
            logger.info(traceback.format_exc())
        self._connected = False
        self._timeout = 0
        if self.c:
            try:
                self.c.sf.closeOnDestroy()
            except:
                logger.info(traceback.format_exc())
            self.c = None
        self._proxies = None
        self._eventContext = None
        logger.info("Connection deleted")
    
    def __del__ (self):
        logger.info("Garbage collector KICK IN")
    
    def connect (self):
        logger.info("Connecting...")
        if not self.c:
            self._connected = False
            return False
        try:
            if self._sessionUuid is not None:
                try:
                    self.c.joinSession(self._sessionUuid)
                except:
                    self._sessionUuid = None
            if self._sessionUuid is None:
                if self._connected:
                    self._connected = False
                    try:
                        self.c.closeSession()
                        self.c = omero.client(host=self._props['host'], port=self._props['port'])
                    except omero.Glacier2.SessionNotExistException:
                        pass
                self.c.createSession(self._props[omero.constants.USERNAME], self._props[omero.constants.PASSWORD])
            
            self._last_error = None
            self._proxies = {}
            self._proxies['admin'] = ProxyObjectWrapper(self, 'getAdminService')
            self._proxies['query'] = ProxyObjectWrapper(self, 'getQueryService')
            self._proxies['ldap'] = ProxyObjectWrapper(self, 'getLdapService')
            self._proxies['container'] = ProxyObjectWrapper(self, 'getContainerService')
            self._proxies['metadata'] = ProxyObjectWrapper(self, 'getMetadataService')
            self._proxies['rawfile'] = ProxyObjectWrapper(self, 'createRawFileStore')
            self._proxies['rendering'] = ProxyObjectWrapper(self, 'createRenderingEngine')
            self._proxies['repository'] = ProxyObjectWrapper(self, 'getRepositoryInfoService')
            self._proxies['script'] = ProxyObjectWrapper(self, 'getScriptService')
            self._proxies['search'] = ProxyObjectWrapper(self, 'createSearchService')
            self._proxies['session'] = ProxyObjectWrapper(self, 'getSessionService')
            self._proxies['share'] = ProxyObjectWrapper(self, 'getShareService')
            self._proxies['thumbs'] = ProxyObjectWrapper(self, 'createThumbnailStore')
            self._proxies['timeline'] = ProxyObjectWrapper(self, 'getTimelineService')
            self._proxies['types'] = ProxyObjectWrapper(self, 'getTypesService')
            self._proxies['update'] = ProxyObjectWrapper(self, 'getUpdateService')
            self._eventContext = self._proxies['admin'].getEventContext()
            self.removeUserGroups()
            self._sessionUuid = self._eventContext.sessionUuid
            self._user = self._proxies['admin'].getExperimenter(self._eventContext.userId)
            self._connected = True
        except Exception, x:
            logger.error(traceback.format_exc())
            self._last_error = x
            raise x
        else:
            logger.info("'%s' (id:%i) is connected to %s sessionUuid: %s" % (self._eventContext.userName, self._eventContext.userId, self.c.getRouter(self.c.ic), self._eventContext.sessionUuid))
            return True
    
    def connectAsShare (self):
        logger.info("Connecting...")
        if not self.c:
            self._connected = False
            return False
        try:
            if self._sessionUuid is not None:
                try:
                    self.c.joinSession(self._sessionUuid)
                except:
                    self._sessionUuid = None
            if self._sessionUuid is None:
                if self._connected:
                    self._connected = False
                    try:
                        self.c.closeSession()
                        self.c = omero.client(host=self._props['host'], port=self._props['port'])
                    except omero.Glacier2.SessionNotExistException:
                        pass
                self.c.createSession(self._props[omero.constants.USERNAME], self._props[omero.constants.PASSWORD])
            
            self._last_error = None
            self._proxies = {}
            self._proxies['admin'] = ProxyObjectWrapper(self, 'getAdminService')
            self._proxies['query'] = ProxyObjectWrapper(self, 'getQueryService')
            self._proxies['container'] = ProxyObjectWrapper(self, 'getContainerService')
            self._proxies['metadata'] = ProxyObjectWrapper(self, 'getMetadataService')
            self._proxies['rawfile'] = ProxyObjectWrapper(self, 'createRawFileStore')
            self._proxies['rendering'] = ProxyObjectWrapper(self, 'createRenderingEngine')
            self._proxies['share'] = ProxyObjectWrapper(self, 'getShareService')
            self._proxies['thumbs'] = ProxyObjectWrapper(self, 'createThumbnailStore')
            self._proxies['timeline'] = ProxyObjectWrapper(self, 'getTimelineService')
            self._eventContext = self._proxies['admin'].getEventContext()
            self.removeUserGroups()
            self._sessionUuid = self._eventContext.sessionUuid
            self._user = self._proxies['admin'].getExperimenter(self._eventContext.userId)
            self._connected = True
        except Exception, x:
            logger.error(traceback.format_exc())
            self._last_error = x
            raise x
        else:
            logger.info("'%s' (id:%i) is connected to %s sessionUuid: %s" % (self._eventContext.userName, self._eventContext.userId, self.c.getRouter(self.c.ic), self._eventContext.sessionUuid))
            return True
    
    def connectAsGuest (self):
        logger.info("Connecting as Guest...")
        if not self.c:
            self._connected = False
            return False
        try:
            if self._sessionUuid is not None:
                try:
                    self.c.joinSession(self._sessionUuid)
                except:
                    self._sessionUuid = None
            if self._sessionUuid is None:
                if self._connected:
                    self._connected = False
                    try:
                        self.c.closeSession()
                        self.c = omero.client(host=self._props['host'], port=self._props['port'])
                    except omero.Glacier2.SessionNotExistException:
                        pass
                self.c.createSession(self._props[omero.constants.USERNAME], self._props[omero.constants.PASSWORD])
            
            self._last_error = None
            self._proxies = {}
            self._proxies['admin'] = self.c.sf.getAdminService()
            self._proxies['config'] = self.c.sf.getConfigService()
            self._eventContext = None #self._proxies['admin'].getEventContext()
            self._sessionUuid = None #self._eventContext.sessionUuid
            self._connected = True
        except Exception, x:
            logger.error(traceback.format_exc())
            self._last_error = x
            raise x
        else:
            logger.info("Guest is connected to %s" % (self.c.getRouter(self.c.ic)))
            return True
    
    def isForgottenPasswordSet(self):
        conf = self.getConfigService()
        try:
            return bool(conf.getConfigValue("omero.resetpassword.config").title())
        except:
            logger.error(traceback.format_exc())
            return False
    
    def getLastError (self):
        return self._last_error
    
    def isConnected (self):
        return self._connected
    
    # userName, userId, sessionUuid, sessionId, isAdmin, memberOfGroups, leaderOfGroups
    def getEventContext (self):
        return self._eventContext
    
    def getUser(self):
        return self._user
    
    def getUserWrapped(self):
        return ExperimenterWrapper(self, self._user)
    
    def removeUserGroups (self):
        a = self.getAdminService()
        gr_u = a.lookupGroup('user')
        try:
            self._eventContext.memberOfGroups.remove(gr_u.id.val)
            self._eventContext.leaderOfGroups.remove(gr_u.id.val)
        except:
            pass
    
    ##############################################
    ##  Services                                ##
    
    def getSessionService (self):
        return self._proxies['session']
    
    def getAdminService (self):
        return self._proxies['admin']
    
    def getConfigService (self):
        return self._proxies['config']
    
    def getQueryService (self):
        return self._proxies['query']
    
    def getUpdateService (self):
        return self._proxies['update']
    
    def getMetadataService (self):
        return self._proxies['metadata']
    
    def getRepositoryInfoService (self):
        return self._proxies['repository']
    
    def getContainerService (self):
        return self._proxies['container']
    
    def createRenderingEngine (self):
        return self._proxies['rendering']
    
    def createRawFileStore (self):
        return self._proxies['rawfile']
    
    def getScriptService(self):
        return self._proxies['script']
    
    def getShareService(self):
        return self._proxies['share']
    
    def createSearchService (self):
        return self._proxies['search']
    
    def createThumbnailStore (self):
        return self._proxies['thumbs']
    
    def getTimelineService (self):
        return self._proxies['timeline']
    
    def getTypesService(self):
        return self._proxies['types']
    
    def getLdapService(self):
        return self._proxies['ldap']
    
    ##############################################
    #    Session methods                         #
    
    def changeActiveGroup(self, gid): # TODO: should be moved to ISession
        s = self.getSessionService()
        a = self.getAdminService()
        gr = a.getGroup(long(gid))
        session = s.getSession(self._sessionUuid)
        session.details.group = gr
        s.updateSession(session)
        self._eventContext = self._proxies['admin'].getEventContext()
    
    ##############################################
    ##   Forgotten password                     ##
    
    def reportForgottenPassword(self, username, email):
        admin_serv = self.getAdminService()
        return admin_serv.reportForgottenPassword(username, email)
    
    ##############################################
    ##   Gets methods                           ##
    
    def lookupExperimenters(self):
        admin_serv = self.getAdminService()
        for exp in admin_serv.lookupExperimenters():
            yield ExperimenterWrapper(self, exp)
    
    def getExperimenters(self, ids=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        if ids is not None:
            p.map = {}
            p.map["ids"] = rlist([rlong(a) for a in ids])
            sql = "select e from Experimenter as e where e.id in (:ids)"
        else:
            p.map = {}
            p.map["id"] = rlong(self.getEventContext().userId)
            sql = "select e from Experimenter as e where e.id != :id "
        for e in q.findAllByQuery(sql, p):
            yield ExperimenterWrapper(self, e)
    
    def lookupLdapAuthExperimenters(self):
        admin_serv = self.getAdminService()
        return admin_serv.lookupLdapAuthExperimenters()
    
    def lookupGroups(self):
        admin_serv = self.getAdminService()
        for gr in admin_serv.lookupGroups():
            yield ExperimenterGroupWrapper(self, gr)
    
    def getExperimenterGroups(self, ids):
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
        rep_serv = self.getRepositoryInfoService()
        return rep_serv.getUsedSpaceInKilobytes()
    
    def getFreeSpaceInKilobytes(self):
        rep_serv = self.getRepositoryInfoService()
        return rep_serv.getFreeSpaceInKilobytes()
    
    def getUsage(self):
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
    
    
    # My data
    def listProjectsMine (self):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select pr from Project pr " \
                "join fetch pr.details.creationEvent join fetch pr.details.owner join fetch pr.details.group " \
                "where pr.details.owner.id=:eid order by pr.name"
        for e in q.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)

    def listDatasetsOutoffProjectMine (self):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select ds from Dataset as ds " \
                "join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
                "where ds.details.owner.id=:eid and " \
                "not exists ( select pld from ProjectDatasetLink as pld where pld.child=ds.id ) order by ds.name"
        for e in q.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)

    def listImagesOutoffDatasetMine (self, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select im from Image as im join fetch im.details.owner join fetch im.details.group " \
                "where im.details.owner.id=:eid and "\
                "not exists ( select dsl from DatasetImageLink as dsl where dsl.child=im.id) order by im.id asc"
        for e in q.findAllByQuery(sql,p):
            yield ImageWrapper(self, e)

    def listDatasetsInProjectMine (self, oid, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select ds from Dataset ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
              "left outer join fetch ds.projectLinks pdl left outer join fetch pdl.parent p " \
              "where p.id=:oid and ds.details.owner.id=:eid order by ds.id asc"
        for e in q.findAllByQuery(sql,p):
            yield DatasetWrapper(self, e)

    def listImagesInDatasetMine (self, oid, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group " \
              "left outer join fetch im.datasetLinks dil left outer join fetch dil.parent d " \
              "where d.id = :oid and im.details.owner.id=:eid order by im.id asc"
        for e in q.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)
    
    
    # As a User
    def listProjectsAsUser (self, eid):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(long(eid))
        sql = "select pr from Project pr join fetch pr.details.creationEvent join fetch pr.details.owner join fetch pr.details.group " \
              "left outer join fetch pr.datasetLinks pdl left outer join fetch pdl.child ds " \
              "where (pr.details.owner.id=:eid or ds.details.owner.id=:eid) " \
              "or (exists ( select im from Image as im where im.details.owner.id=:eid and " \
              "exists ( select dil from DatasetImageLink as dil where dil.child.id=im.id and dil.parent.id=ds.id))) order by pr.name"
        for e in q.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)

    def listDatasetsOutoffProjectAsUser (self, eid):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(long(eid))
        sql = "select ds from Dataset as ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
                "where ds.details.owner.id=:eid and " \
                "not exists ( select pld from ProjectDatasetLink as pld where pld.child=ds.id ) order by ds.name"
        for e in q.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)

    def listImagesOutoffDatasetAsUser (self, eid, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(long(eid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select im from Image as im join fetch im.details.owner join fetch im.details.group " \
                "where im.details.owner.id=:eid and "\
                "not exists ( select dsl from DatasetImageLink as dsl where dsl.child=im.id) order by im.id asc"
        for e in q.findAllByQuery(sql,p):
            yield ImageWrapper(self, e)

    def listDatasetsInProjectAsUser (self, oid, eid, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(long(eid))
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select ds from Dataset ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
              "left outer join fetch ds.projectLinks pdl left outer join fetch pdl.parent p " \
              "where p.id=:oid and ds.details.owner.id=:eid order by ds.id asc"
        for e in q.findAllByQuery(sql,p):
            yield DatasetWrapper(self, e)

    def listImagesInDatasetAsUser (self, oid, eid, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(long(eid))
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group " \
              "left outer join fetch im.datasetLinks dil left outer join fetch dil.parent d " \
              "where d.id = :oid and im.details.owner.id=:eid order by im.id asc"
        for e in q.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)
    
    
    # COLLABORATION
    def listProjectsInGroup (self, gid):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["gid"] = rlong(long(gid))
        sql = "select pr from Project pr join fetch pr.details.creationEvent join fetch pr.details.owner join fetch pr.details.group " \
              "where pr.details.permissions > '-103' and pr.details.group.id=:gid order by pr.name"
        for e in q.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)

    def listDatasetsOutoffProjectInGroup(self, gid):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["gid"] = rlong(long(gid))
        sql = "select ds from Dataset as ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
                "where ds.details.permissions > '-103' and ds.details.group.id=:gid and " \
                "not exists ( select pld from ProjectDatasetLink as pld where pld.child=ds.id)) order by ds.name"
        for e in q.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)

    def listImagesOutoffDatasetInGroup(self, gid, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["gid"] = rlong(long(gid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select im from Image as im join fetch im.details.owner join fetch im.details.group " \
                "where im.details.permissions > '-103' and im.details.group.id=:gid and " \
                "not exists ( select dsl from DatasetImageLink as dsl where dsl.child=im.id ) order by im.id asc"
        for e in q.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)

    def listDatasetsInProjectInGroup (self, oid, gid, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["gid"] = rlong(long(gid))
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select ds from Dataset ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
              "left outer join fetch ds.projectLinks pdl left outer join fetch pdl.parent p " \
              "where p.id=:oid and ds.details.group.id=:gid order by ds.id asc"
        for e in q.findAllByQuery(sql,p):
            yield DatasetWrapper(self, e)

    def listImagesInDatasetInGroup (self, oid, gid, page=None):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["gid"] = rlong(long(gid))
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f
        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group " \
              "left outer join fetch im.datasetLinks dil left outer join fetch dil.parent d " \
              "where d.id=:oid and im.details.group.id=:gid order by im.id asc"
        for e in q.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)
    
    # LISTS selections
    def listSelectedImages(self, ids):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in ids])
        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group where im.id in (:ids) order by im.name"
        for e in q.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)

    def listSelectedDatasets(self, ids):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in ids])
        sql = "select ds from Dataset ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group where ds.id in (:ids) order by ds.name"
        for e in q.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)

    def listSelectedProjects(self, ids):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in ids])
        sql = "select pr from Project pr join fetch pr.details.creationEvent join fetch pr.details.owner join fetch pr.details.group where pr.id in (:ids) order by pr.name"
        for e in q.findAllByQuery(sql, p):
            yield ProjectWrapper(self, e)
    
    # HIERARCHY
    def loadMyContainerHierarchy(self):
        q = self.getContainerService()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map[omero.constants.POJOEXPERIMENTER] = rlong(self.getEventContext().userId)
        p.map[omero.constants.POJOORPHAN] = rbool(True)
        for e in q.loadContainerHierarchy('Project', None,  p.map):
            if isinstance(e, ProjectI):
                yield ProjectWrapper(self, e)
            if isinstance(e, DatasetI):
                yield DatasetWrapper(self, e)

    def loadUserContainerHierarchy(self, eid=None):
        q = self.getContainerService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid == None: p.map[omero.constants.POJOEXPERIMENTER] = rlong(self.getEventContext().userId)
        else: p.map[omero.constants.POJOEXPERIMENTER] = rlong(long(eid))
        p.map[omero.constants.POJOORPHAN] = rbool(True)
        for e in q.loadContainerHierarchy('Project', None,  p.map):
            if isinstance(e, ProjectI):
                yield ProjectWrapper(self, e)
            if isinstance(e, DatasetI):
                yield DatasetWrapper(self, e)

    def loadGroupContainerHierarchy(self, gid=None):
        q = self.getContainerService()
        p = omero.sys.Parameters()
        p.map = {} 
        if gid == None: p.map[omero.constants.POJOGROUP] = rlong(self.getEventContext().groupId)
        else: p.map[omero.constants.POJOGROUP] = rlong(long(gid))
        p.map[omero.constants.POJOORPHAN] = rbool(True)
        for e in q.loadContainerHierarchy('Project', None,  p.map):
            if isinstance(e, ProjectI):
                yield ProjectWrapper(self, e)
            if isinstance(e, DatasetI):
                yield DatasetWrapper(self, e)

    def findContainerHierarchies(self, nid):
        q = self.getContainerService()
        return q.findContainerHierarchies("Project", [long(nid)], None)
    
    # By tag
    def listProjectsByTag(self, tids):
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
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listComments(self, o_type, oid):
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
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listUrls(self, o_type, oid):
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
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listFiles(self, o_type, oid):
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
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listSpecifiedTags(self, ids):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from TagAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listSpecifiedComments(self, ids):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from CommentAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listSpecifiedFiles(self, ids):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from FileAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def listSpecifiedUrls(self, ids):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from UriAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield AnnotationWrapper(self, e)
    
    def getAllTags(self):
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
    def getAllShares(self):
        sh = self.getShareService()
        for e in sh.getAllShares(False):
            yield ShareWrapper(self, e)

    def getOwnShares(self):
        sh = self.getShareService()
        for e in sh.getOwnShares(False):
            yield ShareWrapper(self, e)
    
    def getMemberShares(self):
        sh = self.getShareService()
        for e in sh.getMemberShares(False):
            yield ShareWrapper(self, e)
    
    def getContents(self, share_id):
        sh = self.getShareService()
        for e in sh.getContents(long(share_id)):
            yield ShareContentWrapper(self, e)
    
    def getComments(self, share_id):
        sh = self.getShareService()
        for e in sh.getComments(long(share_id)):
            yield ShareCommentWrapper(self, e)
    
    def getAllMembers(self, share_id):
        sh = self.getShareService()
        for e in sh.getAllMembers(long(share_id)):
            yield ExperimenterWrapper(self, e)

    def getAllGuests(self, share_id):
        sh = self.getShareService()
        return sh.getAllGuests(long(share_id))

    def getAllUsers(self, share_id):
        sh = self.getShareService()
        return sh.getAllUsers(long(share_id))
    
    ##############################################
    ##  Specific Object Getters                 ##
    
    def getGroup(self, gid):
        admin_service = self.getAdminService()
        group = admin_service.getGroup(long(gid))
        return ExperimenterGroupWrapper(self, group)
    
    def lookupGroup(self, name):
        admin_service = self.getAdminService()
        group = admin_service.lookupGroup(str(name))
        return ExperimenterGroupWrapper(self, group)
    
    def getExperimenter(self, eid):
        admin_serv = self.getAdminService()
        exp = admin_serv.getExperimenter(long(eid))
        return ExperimenterWrapper(self, exp)
    
    def lookupExperimenter(self, name):
        admin_serv = self.getAdminService()
        exp = admin_serv.lookupExperimenter(str(name))
        return ExperimenterWrapper(self, exp)
    
    def lookupLdapAuthExperimenter(self, eid):
        admin_serv = self.getAdminService()
        return admin_serv.lookupLdapAuthExperimenter(long(eid))
    
    def getDefaultGroup(self, eid):
        admin_serv = self.getAdminService()
        dgr = admin_serv.getDefaultGroup(long(eid))
        return ExperimenterGroupWrapper(self, dgr)
    
    def getOtherGroups(self, eid):
        admin_serv = self.getAdminService()
        for gr in admin_serv.containedGroups(long(eid)):
            yield ExperimenterGroupWrapper(self, gr)
    
    def containedExperimenters(self, gid):
        admin_serv = self.getAdminService()
        for exp in admin_serv.containedExperimenters(long(gid)):
            yield ExperimenterWrapper(self, exp)
    
    def getGroupsLeaderOf(self):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in self.getEventContext().leaderOfGroups])
        sql = "select e from ExperimenterGroup as e where e.id in (:ids)"
        for e in q.findAllByQuery(sql, p):
            yield ExperimenterGroupWrapper(self, e)

    def getGroupsMemberOf(self):
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
        #default = self.getAdminService().getGroup(self.getEventContext().groupId)
        p = omero.sys.Parameters()
        p.map = {}
        p.map["id"] = rlong(self.getEventContext().groupId)
        default = self.getQueryService().findByQuery("select e from ExperimenterGroup as e join fetch e.details.owner where e.id = :id", p)
        return ExperimenterWrapper(self, default.details.owner)
    
    def getColleagues(self):
        a = self.getAdminService()
        default = self.getAdminService().getGroup(self.getEventContext().groupId)
        for d in default.copyGroupExperimenterMap():
            if d.child.id.val != self.getEventContext().userId:
                yield ExperimenterWrapper(self, d.child)

    def getStaffs(self):
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
    
    def getShare (self, oid):
        sh_serv = self.getShareService()
        sh = None
        sh = sh_serv.getShare(long(oid))
        if sh is not None:
            return ShareWrapper(self, sh)
        else:
            return None
    
    def activateShare (self, oid):
        sh_serv = self.getShareService()
        sh = sh_serv.getShare(long(oid))
        sh_serv.activate(long(oid))
        self._shareId = sh.id.val
        self._eventContext = self._proxies['admin'].getEventContext()
    
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
            logger.error("Project not exist: %i" % long(oid))
            raise AttributeError("Project does not exist.")

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
            logger.error("Dataset not exist: %i" % long(oid))
            raise AttributeError("Dataset does not exist.")

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
            logger.error("Image not exist: %i" % long(oid))
            raise AttributeError("Image does not exist.")
    
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
            logger.error("Image not exist: %i" % long(oid))
            raise AttributeError("Image does not exist.")

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
        #container = self.getContainerService()
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
        #container = self.getContainerService()
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
        return types.getEnumeration(str(klass), str(string))
    
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
        admin_serv.createExperimenterWithPassword(experimenter, password, defaultGroup, otherGroups)
    
    def updateExperimenter(self, experimenter, defaultGroup, addGroups, rmGroups, password=None):
        admin_serv = self.getAdminService()
        if password is not None:
            admin_serv.updateExperimenterWithPassword(experimenter, password)
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
        if password is not None:
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
        obj = BlitzObjectWrapper(self, res)
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
            #send email
            sender = None
            try:
                if settings.EMAIL_NOTIFICATION:
                    import omeroweb.extlib.notification.handlesender as sender
            except:
                logger.error(traceback.format_exc())
            else:
                recipients = list()
                for m in members:
                    try:
                        recipients.append(m.email)
                    except:
                        logger.error(traceback.format_exc())
                if sender is not None:
                    try:
                        sender.handler().create_sharecomment_message(host, blitz_id, share_id, recipients)
                    except:
                        logger.error(traceback.format_exc())
    
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
            sender = None
            try:
                if settings.EMAIL_NOTIFICATION:
                    import omeroweb.extlib.notification.handlesender as sender
            except:
                logger.error(traceback.format_exc())
            else:
                recipients = list()
                if ms is not None:
                    for m in ms:
                        try:
                            recipients.append(m.email.val)
                        except:
                            logger.error(traceback.format_exc())
                if sender is not None:
                    try:
                        sender.handler().create_share_message(host, blitz_id, self.getUser(), sid, recipients)
                    except:
                        logger.error(traceback.format_exc())
    
    def updateShareOrDiscussion (self, share_id, message, members, enable, expiration=None):
        sh = self.getShareService()
        sh.setDescription(long(share_id), message)
        sh.setExpiration(long(share_id), expiration)
        sh.setActive(long(share_id), enable)
    
    def setFile(self, buf):
        f = self.createRawFileStore()
        f.write(buf)
    
    
    ##############################################
    ##  History methods                        ##
    
    def getLastImportedImages (self):
        q = self.getQueryService()
        sql = "select i from Image i join fetch i.details.owner join fetch i.details.group where i.details.owner.id =:id and i.details.group.id =:gid order by i.details.creationEvent.time desc"
        p = omero.sys.Parameters()
        p.map = {}
        p.map["id"] = rlong(self.getEventContext().userId)
        p.map["gid"] = rlong(self.getEventContext().groupId)
        f = omero.sys.Filter()
        f.limit = rint(6)
        p.theFilter = f
        for e in q.findAllByQuery(sql, p):
            yield ImageWrapper(self, e)
        # getMostRecentObjects - missed order by
        ''' tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(6)
        p.theFilter = f
        for e in tm.getMostRecentObjects(['Image'], p, False)["Image"]:
            yield ImageWrapper(self, e)'''
    
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
        f.limit = rint(20)
        p.theFilter = f
        for e in tm.getMostRecentAnnotationLinks(None, ['TagAnnotation'], None, p):
            yield AnnotationLinkWrapper(self, e)
    
    def getDataByPeriod (self, start, end, date_type=None):
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
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
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        for e in tm.getEventLogsByPeriod(rtime(start), rtime(end), p):
            yield EventLogWrapper(self, e)
    
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
    
def splitHTMLColor (color):
    out = []
    if len(color) in (3,4):
        c = color
        color = ''
        for e in c:
            color += e + e
    if len(color) == 6:
        color += 'FF'
    if len(color) == 8:
        for i in range(0, 8, 2):
            out.append(int(color[i:i+2], 16))
        return out
    return None

###############################################

def safeCallWrap (self, attr, f):
    def wrapped (*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except omero.ValidationException, x:
            logger.error(x.message)
            raise AttributeError(x.message)
        except omero.ResourceError, x:
            logger.error(x.message)
            raise AttributeError(x.message)
        except Ice.Exception, x:
            # Failed
            logger.info("Ice.Exception (1) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs)))
            logger.error(traceback.format_exc())
            # Recreate the proxy object
            try:
                self._obj = self._create_func()
                func = getattr(self._obj, attr)
                return func(*args, **kwargs)
            except Ice.Exception, x:
                # Still Failed
                logger.info("Ice.Exception (2) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs)))
                logger.error(traceback.format_exc())
                try:
                    # Recreate connection
                    self._connect()
                    # Last try, don't catch exception
                    func = getattr(self._obj, attr)
                    return func(*args, **kwargs)
                except:
                    logger.error(traceback.format_exc())
                    raise
    return wrapped

class ProxyObjectWrapper (object):
    def __init__ (self, conn, func_str):
        self._obj = None
        self._conn = conn
        self._func_str = func_str
        self._sf = conn.c.sf
        self._create_func = getattr(self._sf, self._func_str)
        self._obj = self._create_func()
    
    def _connect (self):
        logger.info("proxy_connect: connect");
        if not self._conn.connect():
            return False
        logger.info("proxy_connect: sf");
        self._sf = self._conn.c.sf
        logger.info("proxy_connect: create_func");
        self._create_func = getattr(self._sf, self._func_str)
        logger.info("proxy_connect: _obj");
        self._obj = self._create_func()
        logger.info("proxy_connect: true");
        return True
    
    def _getObj (self):
        self._ping()
        return self._obj
    
    def _ping (self):
        """ For some reason, it seems that keepAlive doesn't, so every so often I need to recreate the objects """
        try:
            if not self._sf.keepAlive(self._obj):
                logger.info("... died, recreating")
                self._obj = self._create_func()
        except Ice.ObjectNotExistException:
            # The connection is there, but it has been reset, because the proxy no longer exists...
            logger.info("Ice.ObjectNotExistException... reset, reconnecting")
            logger.info(traceback.format_stack())
            self._connect()
            return False
        except Ice.ConnectionLostException:
            # The connection was lost. This shouldn't happen, as we keep pinging it, but does so...
            logger.info("Ice.ConnectionLostException... lost, reconnecting")
            logger.info(traceback.format_stack())
            self._connect()
            return False
        except Ice.ConnectionRefusedException:
            # The connection was refused. We lost contact with glacier2router...
            logger.info("Ice.ConnectionRefusedException... refused, reconnecting")
            logger.info(traceback.format_stack())
            self._connect()
            return False
        except:
            logger.info("UnknownException")
            logger.info(traceback.format_stack())
            return False
        return True
    
    def __getattr__ (self, attr):
        # safe call wrapper
        rv = getattr(self._obj, attr)
        if callable(rv):
            rv = safeCallWrap(self, attr, rv)
        self._conn.updateTimeout()
        return rv


##########################################################################
# Wrrapers

class BlitzObjectWrapper (object):
    OMERO_CLASS = None
    LINK_CLASS = None
    LINK_NAME = None
    CHILD_WRAPPER_CLASS = None
    PARENT_WRAPPER_CLASS = None
    CHILD = None
    
    child_counter = None
    annotation_counter = None
    
    def __init__ (self, conn=None, obj=None, **kwargs):
        if conn is None:
            return None
        self._conn = conn
        self._obj = obj
        if hasattr(obj, 'id') and obj.id is not None:
            self._oid = obj.id.val
            if not self._obj.loaded:
                self._obj = self._conn.getQueryService().get(self._obj.__class__.__name__, self._oid)
        self.__prepare__ (**kwargs)
    
    def __prepare__ (self, **kwargs):
        try:
            self.child_counter = kwargs['child_counter']
        except:
            pass
        try:
            self.annotation_counter = kwargs['annotation_counter']
        except:
            pass
    
    def listChildren (self):
        """ return a generator yielding child objects """
        if self.CHILD_WRAPPER_CLASS is not None:
            try:
                childnodes = [ x.child for x in getattr(self._obj, self.LINK_NAME)()]

                child_ids = [child.id.val for child in childnodes]
                child_counter = None
                if len(child_ids) > 0:
                    child_counter = self._conn.getCollectionCount(self.CHILD, (self.CHILD_WRAPPER_CLASS.LINK_NAME[4].lower()+self.CHILD_WRAPPER_CLASS.LINK_NAME[5:]), child_ids)
                    child_annotation_counter = self._conn.getCollectionCount(self.CHILD, "annotationLinks", child_ids)
                for child in childnodes:
                    kwargs = dict()
                    if child_counter:
                        kwargs['child_counter'] = child_counter.get(child.id.val)
                    if child_annotation_counter:
                        kwargs['annotation_counter'] = child_annotation_counter.get(child.id.val)
                    yield self.CHILD_WRAPPER_CLASS(self._conn, child, **kwargs)
            except:
                raise NotImplementedError
    
    def countChild (self):
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
        if self._obj.details.owner.id.val == self._conn.getEventContext().userId:
            return True
        else:
            return False
    
    def isEditable(self):
        return (self._conn.getEventContext().userId == self._obj.details.owner.id.val and self._obj.details.permissions.isUserWrite())
    
    def getOwner(self):
        try:
            # lastName = self._obj.details.owner.lastName.val if hasattr(self._obj.details.owner.lastName, 'val') else ""
            # firstName = self._obj.details.owner.firstName.val if hasattr(self._obj.details.owner.firstName, 'val') else ""
            # middleName = self._obj.details.owner.middleName.val if hasattr(self._obj.details.owner.middleName, 'val') else ""
            lastName = None
            if hasattr(self._obj.details.owner.lastName, 'val'):
                lastName = self._obj.details.owner.lastName.val
            else:
                if self._obj.details.owner.lastName is not None:
                    lastName = self._obj.details.owner.lastName
            firstName = None
            if hasattr(self._obj.details.owner.firstName, 'val'):
                firstName = self._obj.details.owner.firstName.val
            else:
                if self._obj.details.owner.firstName is not None:
                    firstName = self._obj.details.owner.firstName
            middleName = None
            if hasattr(self._obj.details.owner.middleName, 'val'):
                middleName = self._obj.details.owner.middleName.val
            else:
                if self._obj.details.owner.middleName is not None:
                    middleName = self._obj.details.owner.middleName
            
            if middleName != '' and middleName is not None:
                name = "%s %s. %s" % (firstName, middleName[:1], lastName)
            else:
                name = "%s %s" % (firstName, lastName)
            l = len(name)
            if l < 40:
                return name
            return name[:40] + "..."
        except:
            logger.info(traceback.format_exc())
            return _("Unknown")
    
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
            if l <= 65:
                return name
            elif l > 65:
                splited = []
                for v in range(0,len(name),65):
                    splited.append(name[v:v+65]+"\n")
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
    
    def creationEventDate(self):
        try:
            if self._obj.details.creationEvent.time is not None:
                t = self._obj.details.creationEvent.time.val
            else:
                t = self._conn.getQueryService().get("Event", self._obj.details.creationEvent.id.val).time.val
        except:
            t = self._conn.getQueryService().get("Event", self._obj.details.creationEvent.id.val).time.val
        return datetime.fromtimestamp(t/1000)
    
    def updateEventDate(self):
        try:
            if self._obj.details.updateEvent.time is not None:
                t = self._obj.details.updateEvent.time.val
            else:
                t = self._conn.getQueryService().get("Event", self._obj.details.updateEvent.id.val).time.val
        except:
            t = self._conn.getQueryService().get("Event", self._obj.details.updateEvent.id.val).time.val
        return datetime.fromtimestamp(t/1000)
    
    def __str__ (self):
        if hasattr(self._obj, 'value'):
            return str(self.value)
        return str(self._obj)
    
    def __getattr__ (self, attr):
        if hasattr(self._obj, attr):
            rv = getattr(self._obj, attr)
            if hasattr(rv, 'val'):
                return isinstance(rv.val, StringType) and rv.val.decode('utf8') or rv.val
            elif hasattr(rv, 'value'):
                return isinstance(rv.value.val, StringType) and rv.value.val.decode('utf8') or rv.value.val
            return rv
        #logger.error("AttributeError: '%s' object has no attribute '%s'" % (self._obj.__class__.__name__, attr))
        raise AttributeError("'%s' object has no attribute '%s'" % (self._obj.__class__.__name__, attr))

class ExperimenterWrapper (BlitzObjectWrapper):
    LINK_NAME = "copyGroupExperimenterMap"
    OMERO_CLASS = 'Experimetner'
    PARENT_WRAPPER_CLASS = 'ExperimenterGroup'
    
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
            # lastName = self._obj.lastName.val if hasattr(self._obj.lastName, 'val') else ""
            # firstName = self._obj.firstName.val if hasattr(self._obj.firstName, 'val') else ""
            # middleName = self._obj.middleName.val if hasattr(self._obj.middleName, 'val') else ""
            lastName = None
            if hasattr(self._obj.lastName, 'val'):
                lastName = self._obj.lastName.val
            else:
                if self._obj.lastName is not None:
                    lastName = self._obj.lastName
            firstName = None
            if hasattr(self._obj.firstName, 'val'):
                firstName = self._obj.firstName.val
            else:
                if self._obj.firstName is not None:
                    firstName = self._obj.firstName
            middleName = None
            if hasattr(self._obj.middleName, 'val'):
                middleName = self._obj.middleName.val
            else:
                if self._obj.middleName is not None:
                    middleName = self._obj.middleName
            
            if middleName != '' and middleName!='  ' and middleName is not None:
                name = "%s %s. %s" % (firstName, middleName[:1], lastName)
            else:
                name = "%s %s" % (firstName, lastName)
            l = len(name)
            if l < 40:
                return name
            return name[:40] + "..."
        except:
            logger.error(traceback.format_exc())
            return _("Unknown name")

class ExperimenterGroupWrapper (BlitzObjectWrapper):
    LINK_NAME = "copyGroupExperimenterMap"
    OMERO_CLASS = 'ExperimenterGroup'
    LINK_CLASS = 'GroupExperimenterMap'
    CHILD_WRAPPER_CLASS = 'Experimenter'
    
class GroupWrapper (BlitzObjectWrapper):
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None

class ScriptWrapper (BlitzObjectWrapper):
    pass

class AnnotationLinkWrapper (BlitzObjectWrapper):
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None

    def getAnnotation(self):
        return AnnotationWrapper(self, self.child)

class AnnotationWrapper (BlitzObjectWrapper):
    
    def getFileSize(self):
        if isinstance(self._obj, FileAnnotationI):
            return self._obj.file.size.val
        else:
            return None
    
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
        else:
            return None
    
    def shortTag(self):
        if isinstance(self._obj, TagAnnotationI):
            try:
                name = self._obj.textValue.val
                l = len(name)
                if l < 11:
                    return name
                return name[:5] + ".." + name[l - 5:] 
            except:
                logger.info(traceback.format_exc())
                return self._obj.textValue.val
        else:
            return None
    
class OriginalFileWrapper (BlitzObjectWrapper):
    pass

class ColorWrapper (object):
    
    RED = 'Red'
    GREEN = 'Green'
    BLUE = 'Blue'
    DEFAULT_ALPHA = rint(255)
    
    def __init__ (self,  colorname=None, **kwargs):
        r,g,b,a = colorname
        self.red = r and 255 or 0
        self.green = g and 255 or 0
        self.blue = b and 255 or 0
        self.alpha = a and self.DEFAULT_ALPHA
        

    def getHtml (self):
        """ Return the html usable color. Dumps the alpha information. """
        return "%0.2X%0.2X%0.2X" % (self.red,self.green,self.blue)

    def getCss (self):
        """ Return rgba(r,g,b,a) for this color """
        return "rgba(%i,%i,%i,%0.3f)" % (self.red,self.green,self.blue, self.alpha/255.0)

class ChannelWrapper (BlitzObjectWrapper):
    BLUE_MIN = 400
    BLUE_MAX = 500
    GREEN_MIN = 501
    GREEN_MAX = 600
    RED_MIN = 601
    RED_MAX = 700
    COLOR_MAP = ((BLUE_MIN, BLUE_MAX, ColorWrapper.BLUE),
                 (GREEN_MIN, GREEN_MAX, ColorWrapper.GREEN),
                 (RED_MIN, RED_MAX, ColorWrapper.RED),
                 )
    def __prepare__ (self, idx, re):
        self._re = re
        self._idx = idx

    def isActive (self):
        return self._re.isActive(self._idx)

    def getEmissionWave (self):
        lc = self._obj.getLogicalChannel()
        emWave = lc.getEmissionWave()
        if emWave is None:
            return self._idx
        else:
            return emWave.val

    def getColor (self):
        return ColorWrapper(self._re.getRGBA(self._idx))

    def getWindowStart (self):
        return int(self._re.getChannelWindowStart(self._idx))

    def setWindowStart (self, val):
        self.setWindow(val, self.getWindowEnd())

    def getWindowEnd (self):
        return int(self._re.getChannelWindowEnd(self._idx))

    def setWindowEnd (self, val):
        self.setWindow(self.getWindowStart(), val)

    def setWindow (self, minval, maxval):
        self._re.setChannelWindow(self._idx, float(minval), float(maxval))

    def getWindowMin (self):
        return self._obj.getStatsInfo().getGlobalMin().val

    def getWindowMax (self):
        return self._obj.getStatsInfo().getGlobalMax().val

def assert_re (func):
    def wrapped (self, *args, **kwargs):
        if not self._prepareRenderingEngine():
            return None
        return func(self, *args, **kwargs)
    return wrapped

def assert_pixels (func):
    def wrapped (self, *args, **kwargs):
        self._loadPixels()
        if self._obj.sizeOfPixels() < 1:
            logger.info("No pixels!")
            return None
        return func(self, *args, **kwargs)
    return wrapped

class ImageImagingEnvironmentWrapper (BlitzObjectWrapper):
    pass

class ImageObjectiveSettingsWrapper (BlitzObjectWrapper):
    pass

class ImageObjectiveWrapper (BlitzObjectWrapper):
    pass

class ImageInstrumentWrapper (BlitzObjectWrapper):
    pass

class ImageStageLabelWrapper (BlitzObjectWrapper):
    pass

class ImageWrapper (BlitzObjectWrapper):
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None

    _re = None
    _pd = None
    _rm = {}
    
    PLANEDEF = omero.romio.XY

    def _loadPixels (self):
        if not self._obj.pixelsLoaded:
            param = omero.sys.Parameters() # TODO: What can I use this for?
            param.map = {} 
            param.map["id"] = rlong(long(self._oid))
            pixels = self._conn.getQueryService().findAllByQuery("from Pixels as p where p.image.id=:id", param)
            #for p in pixels:
            #    p.setPixelsDimensions(self._conn.getQueryService().find('PixelsDimensions', p.pixelsDimensions.id.val))
            self._obj._pixelsLoaded = True
            self._obj.addPixelsSet(pixels)

    def _prepareRenderingEngine (self):
        self._loadPixels()
        if self._re is None:
            if self._obj.sizeOfPixels() < 1:
                logger.info("No pixels!")
                return False
            pixels_id = self._obj.copyPixels()[0].id.val
            if self._pd is None:
                self._pd = omero.romio.PlaneDef(self.PLANEDEF)
            if self._re is None:
                self._re = self._conn.createRenderingEngine()
                self._re.lookupPixels(pixels_id)
                if self._re.lookupRenderingDef(pixels_id) == False:
                    self._re.resetDefaults()
                    self._re.lookupRenderingDef(pixels_id)
                self._re.load()
        return True

    def getDateAsTimestamp(self):
        try:
            return time.ctime(self._obj.acquisitionDate.val / 1000)
        except:
            logger.info(traceback.format_exc())
            return "unknown"

    def getDate(self):
        try:
            if self._obj.acquisitionDate.val is not None:
                t = self._obj.acquisitionDate.val
            else:
                t = self._obj.details.creationEvent.time.val
        except:
            t = self._conn.getQueryService().get("Event", self._obj.details.creationEvent.id.val).time.val
        return datetime.fromtimestamp(t/1000)
    
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
    
    def getObjective(self):
        if self._obj.objectiveSettings is None:
            return None
        elif self._obj.objectiveSettings.objective is None:
            return None
        else:
            return ImageObjectiveWrapper(self._conn, self._obj.objectiveSettings.objective)
        
    def getInstrument(self):
        if self._obj.objectiveSettings is None:
            return None
        elif self._obj.objectiveSettings.objective is None:
            return None
        elif self._obj.objectiveSettings.objective.instrument is None:
            return None
        else:
            return ImageInstrumentWrapper(self._conn, self._obj.objectiveSettings.objective.instrument)
    
    def getStageLabel (self):
        if self._obj.stageLabel is None:
            return None
        else:
            return ImageStageLabelWrapper(self._conn, self._obj.stageLabel)
    
    def getThumbnail (self, size=(120,120)):
        try:
            self._loadPixels()
            if self._obj.sizeOfPixels() < 1:
                logger.info("No pixels!")
                return None
            pixels_id = self._obj.copyPixels()[0].id.val
            tb = self._conn.createThumbnailStore()
            if not tb.setPixelsId(pixels_id):
                tb.resetDefaults()
                tb.setPixelsId(pixels_id)
            t = tb.getThumbnail(rint(size[0]),rint(size[1]))
        except Exception, x:
            try:
                t = self.defaultThumbnail(size)
            except Exception, e:
                logger.info(traceback.format_exc())
                raise e
        return t

    def getThumbnailByLongestSide (self, size=120):
        try:
            self._loadPixels()
            if self._obj.sizeOfPixels() < 1:
                logger.info("No pixels!")
                return None
            pixels_id = self._obj.copyPixels()[0].id.val
            tb = self._conn.createThumbnailStore()
            if not tb.setPixelsId(pixels_id):
                tb.resetDefaults()
                tb.setPixelsId(pixels_id)
            t = tb.getThumbnailByLongestSide(rint(size))
        except Exception, x:
            try:
                t = self.defaultThumbnail((size, size))
            except Exception, e:
                logger.info(traceback.format_exc())
        return t

    def defaultThumbnail(self, size=(120,120)):
        img = Image.open(settings.DEFAULT_IMG)
        img.thumbnail(size, Image.ANTIALIAS)
        draw = ImageDraw.Draw(img)
        f = cStringIO.StringIO()
        img.save(f, "PNG")
        f.seek(0)
        return f.read()

    @assert_re
    def getChannels (self):
        return [ChannelWrapper(self._conn, c, idx=n, re=self._re) for n,c in enumerate(self._re.getPixels().copyChannels())]

    def setActiveChannels(self, channels, windows, colors):
        for c in range(len(self.getChannels())):
            self._re.setActive(c, (c+1) in channels)
            if (c+1) in channels:
                if windows[c][0] and windows[c][1]:
                    self._re.setChannelWindow(c, *windows[c])
                if colors[c]:
                    rgba = splitHTMLColor(colors[c])
                    logger.info('rgba[%i]=%s' %(c, str(rgba)))
                    if rgba:
                        self._re.setRGBA(c, *rgba)
            #print "Channel %i active: %s" % (c, str(self._re.isActive(c)))
        return True

    @assert_re
    def getRenderingModels (self):
        if not len(self._rm):
            for m in [BlitzObjectWrapper(self._conn, m) for m in self._re.getAvailableModels()]:
                self._rm[m.value.lower()] = m
        return self._rm.values()

    @assert_re
    def getRenderingModel (self):
        return BlitzObjectWrapper(self._conn, self._re.getModel())

    @assert_re
    def setRenderingModel (self, model_idx):
        models = self.getRenderingModels()
        if model_idx < len(models):
            self._re.setModel(models[model_idx]._obj)
        return True

    def setGreyscaleRenderingModel (self):
        """ Sets the Greyscale rendering model on this image's current renderer """
        rm = self.getRenderingModels()
        self._re.setModel(self._rm.get('greyscale', rm[0])._obj)

    def setColorRenderingModel (self):
        """ Sets the HSB rendering model on this image's current renderer """
        rm = self.getRenderingModels()
        self._re.setModel(self._rm.get('rgb', rm[0])._obj)

    def isGreyscaleRenderingModel (self):
        return self.getRenderingModel().value.lower() == 'greyscale'
        
    @assert_re
    def renderJpeg (self, z, t, active_channels=(), compression=0.9):
        self._pd.z = long(z)
        self._pd.t = long(t)
        try:
            if compression is not None:
                try:
                    self._re.setCompressionLevel(float(compression))
                    #print "CompressionLevel = " + str(compression)
                except omero.SecurityViolation:
                    self._obj.clearPixels()
                    self._obj.pixelsLoaded = False
                    self._re = None
                    return self.renderJpeg(z,t,active_channels, None)
            rv = self._re.renderCompressed(self._pd)
            return rv
        except omero.InternalException:
            logger.info(traceback.format_exc())
            return None

    @assert_re
    def getZ (self):
        return self._pd.z

    @assert_re
    def getT (self):
        return self._pd.t

    @assert_pixels
    def getPixelSizeX (self):
        x = self._obj.copyPixels()[0].getPhysicalSizeX()
        if x is not None:
            return x.val
        else:
            return None

    @assert_pixels
    def getPixelSizeY (self):
        y = self._obj.copyPixels()[0].getPhysicalSizeY()
        if y is not None:
            return y.val
        else:
            return None

    @assert_pixels
    def getPixelSizeZ (self):
        z = self._obj.copyPixels()[0].getPhysicalSizeZ()
        if z is not None:
            return z.val
        else:
            return None

    @assert_pixels
    def getWidth (self):
        return self._obj.copyPixels()[0].getSizeX().val

    @assert_pixels
    def getHeight (self):
        return self._obj.copyPixels()[0].getSizeY().val

    @assert_pixels
    def z_count (self):
        return self._obj.copyPixels()[0].getSizeZ().val

    @assert_pixels
    def t_count (self):
        return self._obj.copyPixels()[0].getSizeT().val

    @assert_pixels
    def c_count (self):
        return self._obj.copyPixels()[0].getSizeC().val

class DatasetWrapper (BlitzObjectWrapper):
    OMERO_CLASS = 'Dataset'
    LINK_NAME = "copyImageLinks"
    LINK_CLASS = "DatasetImageLink"
    CHILD_WRAPPER_CLASS = ImageWrapper
    #PARENT_WRAPPER_CLASS = ProjectWrapper
    CHILD = 'Image'    
    
    def __init__ (self, conn=None, obj=None, **kwargs):
        super(DatasetWrapper, self).__init__(conn, obj, **kwargs)
        self.OMERO_CLASS = 'Dataset'
        self.LINK_NAME = "copyImageLinks"
        self.LINK_CLASS = "DatasetImageLink"
        self.CHILD_WRAPPER_CLASS = ImageWrapper
        #PARENT_WRAPPER_CLASS = ProjectWrapper
        self.CHILD = 'Image'
    
    def getProject(self):
        try:
            q = "select p from Dataset ds join ds.projectLinks pl join pl.parent p where ds.id = %i"% self._obj.id.val
            query = self._conn.getQueryService()
            prj = query.findByQuery(q,None)
            return  prj
        except:
            logger.info(traceback.format_exc())
            self._pub = "Muliple"
            self._pubId = "Multiple"
            return "Multiple"

class DatasetImageLinkWrapper (BlitzObjectWrapper):
    pass

class ProjectDatasetLinkWrapper (BlitzObjectWrapper):
    pass

class ProjectWrapper (BlitzObjectWrapper):
    OMERO_CLASS = 'Project'
    LINK_NAME = "copyDatasetLinks"
    LINK_CLASS = "ProjectDatasetLink"
    CHILD_WRAPPER_CLASS = DatasetWrapper
    CHILD = 'Dataset'
    
class ShareWrapper (BlitzObjectWrapper):
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None
    
    def shortMessage(self):
        try:
            msg = self._obj.message
            if msg == None or msg.val == "":
                return "-"
            l = len(msg.val)
            if l < 50:
                return msg.val
            return msg.val[:50] + "..."
        except:
            logger.info(traceback.format_exc())
            return self._obj.message.val
    
    def tinyMessage(self):
        try:
            msg = self._obj.message.val
            l = len(msg)
            if l < 20:
                return msg
            elif l >= 20:
                return "%s..." % (msg[:20])
        except:
            logger.info(traceback.format_exc())
            return self._obj.message.val
    
    def getShareType(self):
        if self.itemCount == 0:
            return "Discuss"
        else:
            return "Share"
    
    def getMembersCount(self):
        return "None"
    
    def getCommentsSize(self):
        return len(list(self._conn.getComments(self.id)))
        
    def getStartDate(self):
        return datetime.fromtimestamp(self._obj.started.val/1000)
        
    def getExpirationDate(self):
        try:
            return datetime.fromtimestamp((self._obj.started.val+self._obj.timeToLive.val)/1000)
        except ValueError:
            return None
        except:
            return None
    
    # Owner methods has to be updated because share.details.owner does not exist. Share.owner.
    def isOwned(self):
        if self._obj.owner.id.val == self._conn.getEventContext().userId:
            return True
        else:
            return False
    
    def getOwnerAsExperimetner(self):
        return ExperimenterWrapper(self, self._obj.owner)
    
    def getShareOwner(self):
        try:
            # lastName = self._obj.details.owner.lastName.val if hasattr(self._obj.details.owner.lastName, 'val') else ""
            # firstName = self._obj.details.owner.firstName.val if hasattr(self._obj.details.owner.firstName, 'val') else ""
            # middleName = self._obj.details.owner.middleName.val if hasattr(self._obj.details.owner.middleName, 'val') else ""
            lastName = None
            if hasattr(self._obj.owner.lastName, 'val'):
                lastName = self._obj.owner.lastName.val
            else:
                if self._obj.owner.lastName is not None:
                    lastName = self._obj.owner.lastName
            firstName = None
            if hasattr(self._obj.owner.firstName, 'val'):
                firstName = self._obj.owner.firstName.val
            else:
                if self._obj.owner.firstName is not None:
                    firstName = self._obj.owner.firstName
            middleName = None
            if hasattr(self._obj.owner.middleName, 'val'):
                middleName = self._obj.owner.middleName.val
            else:
                if self._obj.owner.middleName is not None:
                    middleName = self._obj.owner.middleName
            
            if middleName != '' and middleName is not None:
                name = "%s %s. %s" % (firstName, middleName[:1], lastName)
            else:
                name = "%s %s" % (firstName, lastName)
            
            l = len(name)
            if l < 40:
                return name
            return name[:40] + "..."
        except:
            logger.info(traceback.format_exc())
            return _("Unknown")
    
class ShareContentWrapper (BlitzObjectWrapper):
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None


class ShareCommentWrapper (BlitzObjectWrapper):
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None
    
class SessionAnnotationLinkWrapper (BlitzObjectWrapper):
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None
    
    def getComment(self):
        return ShareCommentWrapper(self, self.child)
    
    def getShare(self):
        return ShareWrapper(self, self.parent)
    
class EventLogWrapper (BlitzObjectWrapper):
    LINK_CLASS = "EventLog"
    CHILD_WRAPPER_CLASS = None

class EnumerationWrapper (BlitzObjectWrapper):
    pass