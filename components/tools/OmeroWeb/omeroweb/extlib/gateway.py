#!/usr/bin/env python
# 
# Gateway
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# This file is distributed under the same license as the OMERO package.
# Use is subject to license terms supplied in LICENSE.txt
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
# 

import sys
sys.path.append('icepy')
sys.path.append('lib')

import cStringIO
import Image,ImageDraw
import omero
import logging
import threading
import time
import traceback

import Ice
import Glacier2
import omero_api_IScript_ice
from omero.rtypes import *

from datetime import datetime
from django.utils.translation import ugettext as _
from types import IntType, ListType, TupleType, UnicodeType, StringType

logger = logging.getLogger('gateway')

TIMEOUT = 580 #sec
SLEEPTIME = 30

class BlitzGateway (threading.Thread):

    def __init__ (self, server, port, username, passwd, sessionUuid=None):
        super(BlitzGateway, self).__init__()
        self.setDaemon(True)
        self.client = None
        self.c = omero.client(host=str(server), port=int(port))
        self._sessionUuid = sessionUuid

        # The properties we are setting through the interface
        self._props = {'host': str(server), 'port': int(port),
                        omero.constants.USERNAME: str(username), omero.constants.PASSWORD: str(passwd)}
        
        self._connected = False
        self._user = None
        self._eventContext = None
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
        logger.debug("Starting thread...")
        while not (self.allow_thread_timeout and self.isTimedout()):
            try:
                time.sleep(SLEEPTIME)
                if self._connected:
                    logger.debug("Ping...")
                    #self.c._sf.keepAllAlive([x.obj for x in self._proxies.items()])
                    for k,v in self._proxies.items():
                        logger.debug("Sending keepalive to '%s'" % k)
                        if not v._ping():
                            logger.debug("... some error sending keepalive to '%s'" % k)
                            # connection should have been recreated and proxies are different now, so start all over
                            break
            except:
                logger.error("Something bad on the client proxy keepalive thread")
                logger.error(traceback.format_exc())
        if self._connected:
            self.seppuku()
        logger.debug("Thred death")
    
    def seppuku (self):
        try:
            logger.debug("Connection will be closed [%s]" % (self.c.getRouter()))
        except:
            logger.debug("Connection will be closed.")
            logger.debug(traceback.format_exc())
        self._connected = False
        self._timeout = 0
        if self.c:
            try:
                self.c.sf.closeOnDestroy()
            except:
                logger.debug(traceback.format_exc())
            self.c = None
        self._proxies = None
        self._eventContext = None
        logger.info("Connection deleted")
    
    def __del__ (self):
        logger.debug("Garbage collector KICK IN")
    
    def connect (self):
        logger.debug("Connecting...")
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
            self._proxies['repository'] = ProxyObjectWrapper(self, 'getRepositoryInfoService')
            self._proxies['script'] = ProxyObjectWrapper(self, 'getScriptService')
            self._proxies['session'] = ProxyObjectWrapper(self, 'getSessionService')
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
            logger.info("'%s' (id:%i) is connected to %s sessionUuid: %s" % (self._eventContext.userName, self._eventContext.userId, self.c.getRouter(), self._eventContext.sessionUuid))
            return True
    
    def getLastError (self):
        return self._last_error
    
    def isConnected (self):
        return self._connected
    
    # userName, userId, sessionUuid, sessionId, isAdmin, memberOfGroups, leaderOfGroups
    def getEventContext (self):
        return self._eventContext
    
    def getUser(self):
        return self._user
    
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
    
    def getQueryService (self):
        return self._proxies['query']
    
    def getUpdateService (self):
        return self._proxies['update']
    
    def getRepositoryInfoService (self):
        return self._proxies['repository']
    
    def getScriptService(self):
        return self._proxies['script']
    
    def getTypesService(self):
        return self._proxies['types']
    
    def getLdapService(self):
        return self._proxies['ldap']
    
    ##############################################
    ##   Gets methods                           ##
    
    def lookupScripts(self):
        script_serv = self.getScriptService()
        return script_serv.getScripts()
    
    def lookupExperimenters(self):
        admin_serv = self.getAdminService()
        for exp in admin_serv.lookupExperimenters():
            yield ExperimenterWrapper(self, exp)
    
    def lookupGroups(self):
        admin_serv = self.getAdminService()
        for gr in admin_serv.lookupGroups():
            yield ExperimenterGroupWrapper(self, gr)
    
    def lookupLdapAuthExperimenters(self):
        admin_serv = self.getAdminService()
        return admin_serv.lookupLdapAuthExperimenters()
    
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
    
    ##############################################
    ##  Specific Object Getters                 ##
    
    def getGroup(self, gid):
        admin_service = self.getAdminService()
        group = admin_service.getGroup(long(gid))
        return ExperimenterGroupWrapper(self, group)
    
    def lookupGroup(self, name):
        admin_service = self.getAdminService()
        group = admin_service.lookupGroup(name)
        return ExperimenterGroupWrapper(self, group)
    
    def getExperimenter(self, eid):
        admin_serv = self.getAdminService()
        exp = admin_serv.getExperimenter(long(eid))
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
    
    def getScriptwithDetails(self, sid):
        script_serv = self.getScriptService()
        return script_serv.getScriptWithDetails(long(sid))
    
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
        print name, old_name
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
        admin_serv.createExperimenterWithPassword(experimenter, rstring(password), defaultGroup, otherGroups)
    
    def updateExperimenter(self, experimenter, defaultGroup, addGroups, rmGroups, password=None):
        admin_serv = self.getAdminService()
        if password is not None:
            admin_serv.updateExperimenterWithPassword(experimenter, rstring(password))
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
            admin_serv.changePassword(rstring(password))
    
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

###############################################

def safeCallWrap (self, attr, f):
    def wrapped (*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except Ice.Exception, x:
            # Failed
            logger.info("Ice.Exception (1) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs)))
            logger.debug(traceback.format_exc())
            # Recreate the proxy object
            try:
                self._obj = self._create_func()
                func = getattr(self._obj, attr)
                return func(*args, **kwargs)
            except Ice.Exception, x:
                # Still Failed
                logger.info("Ice.Exception (2) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs)))
                logger.debug(traceback.format_exc())
                try:
                    # Recreate connection
                    self._connect()
                    # Last try, don't catch exception
                    func = getattr(self._obj, attr)
                    return func(*args, **kwargs)
                except:
                    logger.debug(traceback.format_exc())
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
        logger.debug("proxy_connect: connect");
        if not self._conn.connect():
            return False
        logger.debug("proxy_connect: sf");
        self._sf = self._conn.c.sf
        logger.debug("proxy_connect: create_func");
        self._create_func = getattr(self._sf, self._func_str)
        logger.debug("proxy_connect: _obj");
        self._obj = self._create_func()
        logger.debug("proxy_connect: true");
        return True
    
    def _getObj (self):
        self._ping()
        return self._obj
    
    def _ping (self):
        """ For some reason, it seems that keepAlive doesn't, so every so often I need to recreate the objects """
        try:
            if not self._sf.keepAlive(self._obj):
                logger.debug("... died, recreating")
                self._obj = self._create_func()
        except Ice.ObjectNotExistException:
            # The connection is there, but it has been reset, because the proxy no longer exists...
            logger.debug("Ice.ObjectNotExistException... reset, reconnecting")
            logger.debug(traceback.format_stack())
            self._connect()
            return False
        except Ice.ConnectionLostException:
            # The connection was lost. This shouldn't happen, as we keep pinging it, but does so...
            logger.debug("Ice.ConnectionLostException... lost, reconnecting")
            logger.debug(traceback.format_stack())
            self._connect()
            return False
        except Ice.ConnectionRefusedException:
            # The connection was refused. We lost contact with glacier2router...
            logger.debug("Ice.ConnectionRefusedException... refused, reconnecting")
            logger.debug(traceback.format_stack())
            self._connect()
            return False
        except:
            logger.debug("UnknownException")
            logger.debug(traceback.format_stack())
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
    CHILD_WRAPPER_CLASS = None
    PARENT_WRAPPER_CLASS = None
    
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
        pass
    
    def getOwner(self):
        try:
            # only for python 2.5
            # lastName = self._obj.details.owner.lastName.val if hasattr(self._obj.details.owner.lastName, 'val') else ""
            # firstName = self._obj.details.owner.firstName.val if hasattr(self._obj.details.owner.firstName, 'val') else ""
            # middleName = self._obj.details.owner.middleName.val if hasattr(self._obj.details.owner.middleName, 'val') else ""
            lastName = ""
            if hasattr(self._obj.details.owner.lastName, 'val'):
                lastName = self._obj.details.owner.lastName.val
            else:
                if self._obj.lastName is not None:
                    lastName = self._obj.details.owner.lastName
            firstName = ""
            if hasattr(self._obj.details.owner.firstName, 'val'):
                firstName = self._obj.details.owner.firstName.val
            else:
                if self._obj.firstName is not None:
                    firstName = self._obj.details.owner.firstName
            middleName = ""
            if hasattr(self._obj.details.owner.middleName, 'val'):
                middleName = self._obj.details.owner.middleName.val
            else:
                if self._obj.details.owner.middleName is not None:
                    middleName = self._obj.details.owner.middleName
            name = "%s %s, %s" % (lastName, firstName, middleName)
            l = len(name)
            if l < 40:
                return name
            return name[:40] + "..."
        except:
            print traceback.format_exc()
            
            logger.debug(traceback.format_exc())
            return _("Unknown")
    
    def __str__ (self):
        if hasattr(self._obj, 'value'):
            return str(self.value)
        return str(self._obj)
    
    def __getattr__ (self, attr):
        if hasattr(self._obj, attr):
            rv = getattr(self._obj, attr)
            if hasattr(rv, 'val'):
                return isinstance(rv.val, StringType) and rv.val.decode('utf8') or rv.val
            return rv
        logger.error("AttributeError: '%s' object has no attribute '%s'" % (self._obj.__class__.__name__, attr))
        logger.error(traceback.format_stack())
        raise AttributeError("'%s' object has no attribute '%s'" % (self._obj.__class__.__name__, attr))

class ExperimenterWrapper (BlitzObjectWrapper):
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
            # only for python 2.5
            # lastName = self._obj.lastName.val if hasattr(self._obj.lastName, 'val') else ""
            # firstName = self._obj.firstName.val if hasattr(self._obj.firstName, 'val') else ""
            # middleName = self._obj.middleName.val if hasattr(self._obj.middleName, 'val') else ""
            lastName = ""
            if hasattr(self._obj.lastName, 'val'):
                lastName = self._obj.lastName.val
            else:
                if self._obj.lastName is not None:
                    lastName = self._obj.lastName
            firstName = ""
            if hasattr(self._obj.firstName, 'val'):
                firstName = self._obj.firstName.val
            else:
                if self._obj.firstName is not None:
                    firstName = self._obj.firstName
            middleName = ""
            if hasattr(self._obj.middleName, 'val'):
                middleName = self._obj.middleName.val
            else:
                if self._obj.middleName is not None:
                    middleName = self._obj.middleName
            name = "%s %s, %s" % (lastName, firstName, middleName)
            l = len(name)
            if l < 40:
                return name
            return name[:40] + "..."
        except:
            logger.error(traceback.format_exc())
            return _("Unknown name")

class ExperimenterGroupWrapper (BlitzObjectWrapper):
    OMERO_CLASS = 'ExperimenterGroup'
    LINK_CLASS = 'GroupExperimenterMap'
    CHILD_WRAPPER_CLASS = 'Experimenter'
    
    def shortDescription(self):
        try:
            desc = self._obj.description
            if desc == None or desc.val == "":
                return "-"
            l = len(desc.val)
            if l < 40:
                return desc.val
            return desc.val[:40] + "..."
        except:
            logger.error(traceback.format_exc())
            return None
            
class ScriptWrapper (BlitzObjectWrapper):
    pass
