#/bin/env python -i

#
# blitz_gateway - python bindings and wrappers to access an OMERO blitz server
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

#WEBLITZ_ANN_NS = {}#'PART':    'com.glencoesoftware.journal_bridge:part'}

#class STATIC_DEFS:
#    MAXIMUM_INTENSITY = 1
#    MEAN_INTENSITY    = 2
#    SUM_INTENSITY     = 3

    

# Set up the python include paths
import logging
import os,sys
THISPATH = os.path.dirname(os.path.abspath(__file__))
#sys.path.append(os.path.join(p,'icepy'))
#sys.path.append(os.path.join(p,'lib'))

from types import IntType, LongType, ListType, TupleType, UnicodeType, StringType, StringTypes
from datetime import datetime

import omero
import Ice
import Glacier2

import traceback
#import threading
import time
import array

logger = logging.getLogger('blitz_gateway')

try:
    import Image, ImageDraw, ImageFont
except:
    logger.error('No PIL installed, line plots and split channel will fail!')
from cStringIO import StringIO
from math import sqrt
from omero.rtypes import rstring, rint, rlong, rbool, rtime

import omero_Constants_ice  
import omero_ROMIO_ice

# TODO: what is this for?
SLEEPTIME = 30


def timeit (func):
    """
    Measures the execution time of a function using time.time() 
    and the a @ function decorator.

    @param func:    function
    @return:        wrapped
    """
    
    def wrapped (*args, **kwargs):
        logger.debug("timing %s" % (func.func_name))
        now = time.time()
        rv = func(*args, **kwargs)
        logger.debug("timed %s: %f" % (func.func_name, time.time()-now))
        return rv
    return wrapped


def omero_type(val):
    """
    Converts rtypes from static factory methods:
      - StringType to rstring
      - UnicodeType to rstring
      - IntType to rint
      - LongType to rlong
    elswere return the argument itself

    @param val: value 
    @return:    matched RType or value
    """
    
    if isinstance(val, StringType):
        return rstring(val)
    elif isinstance(val, UnicodeType):
        return rstring(val.encode('utf-8'))
    elif isinstance(val, IntType):
        return rint(val)
    elif isinstance(val, LongType):
        return rlong(val)
    else:
        return val


class NoProxies (object):
    def __getitem__ (self, k):
        raise Ice.ConnectionLostException

class _BlitzGateway (object):
    """
    ICE_CONFIG - Defines the path to the Ice configuration
    """
    
    ICE_CONFIG = None#os.path.join(p,'etc/ice.config')
#    def __init__ (self, username, passwd, server, port, client_obj=None, group=None, clone=False):
    
    def __init__ (self, username=None, passwd=None, client_obj=None, group=None, clone=False, try_super=False, host=None, port=None, extra_config=[]):
        """
        TODO: Constructor
        
        @param username:    User name. String
        @param passwd:      Password. String
        @param client_obj:  omero.client 
        @param group:       admin group
        @param clone:       Boolean
        @param try_super:   Boolean
        @param host:        Omero server host. String
        @param port:        Omero server port. Integer
        @param extra_config:
        """
        
        super(_BlitzGateway, self).__init__()
        self.client = client_obj
        if not type(extra_config) in (type(()), type([])):
            extra_config=[extra_config]
        self.extra_config = extra_config
        self.ice_config = [self.ICE_CONFIG]
        self.ice_config.extend(extra_config)
        self.ice_config = map(lambda x: str(x), filter(None, self.ice_config))

        self.host = host
        self.port = port

        self._resetOmeroClient()
        if not username:
            username = self.c.ic.getProperties().getProperty('weblitz.anon_user')
            passwd = self.c.ic.getProperties().getProperty('weblitz.anon_pass')
        if try_super:
            if not group:
                group = self.c.ic.getProperties().getProperty('weblitz.admin_group')
            self.group = group and group or None
        else:
            self.group = None
        self._sessionUuid = None
        self._session_cb = None
        self._session = None
        self._lastGroup = None
        self._anonymous = True

        # The properties we are setting through the interface
        self.setIdentity(username, passwd, not clone)

        self._connected = False
        self._user = None
        self._userid = None
        self._proxies = NoProxies()

    def getProperty(self, k):
        return self.c.getProperty(k)

    def clone (self):
        return self.__class__(self._ic_props[omero.constants.USERNAME],
                              self._ic_props[omero.constants.PASSWORD],
                              extra_config=self.extra_config,
                              clone=True)
                              #self.server, self.port, clone=True)

    def setIdentity (self, username, passwd, _internal=False):
        """
        TODO: description
        
        @param username:    User name. String
        @param passwd:      Password. String
        @param _internal:   Boolean
        """
        
        self._ic_props = {omero.constants.USERNAME: username,
                          omero.constants.PASSWORD: passwd}
        self._anonymous = _internal
    
    def keepAlive (self):
        """
        Keeps service alive. 
        Returns True if connected. If connection was lost, reconnecting.
        
        @return:    Boolean
        """
        
        try:
            logger.debug('connected? %s' % str(self._connected))
            logger.debug('... sending keepalive to %s' % self._proxies['admin']._obj)
            if self.c.sf is None: #pragma: no cover
                logger.debug('... c.sf is None, reconnecting')
                return self.connect()
            return self.c.sf.keepAlive(self._proxies['admin']._obj)
        except Ice.ObjectNotExistException: #pragma: no cover
            # The connection is there, but it has been reset, because the proxy no longer exists...
            logger.debug(traceback.format_exc())
            logger.debug("... reset, not reconnecting")
            return False
        except Ice.ConnectionLostException: #pragma: no cover
            # The connection was lost. This shouldn't happen, as we keep pinging it, but does so...
            logger.debug(traceback.format_exc())
            logger.debug("... lost, reconnecting")
            return self.connect()
        except Ice.ConnectionRefusedException: #pragma: no cover
            # The connection was refused. We lost contact with glacier2router...
            logger.debug(traceback.format_exc())
            logger.debug("... refused, not reconnecting")
            return False
        except omero.RemovedSessionException: #pragma: no cover
            # Session died on us
            logger.debug(traceback.format_exc())
            logger.debug("... session has left the building, not reconnecting")
            return False
        except Ice.UnknownException, x: #pragma: no cover
            # Probably a wrapped RemovedSession
            logger.debug(traceback.format_exc())
            logger.debug('Ice.UnknownException: %s' % str(x))
            logger.debug("... ice says something bad happened, not reconnecting")
            return False

    def seppuku (self, softclose=False): #pragma: no cover
        """
        Terminates connection. If softclose is False, the session is really
        terminate disregarding its connection refcount. 
        
        @param softclose:   Boolean
        """
        
        self._connected = False
        if self.c:
            try:
                self.c.sf.closeOnDestroy()
            except Ice.ConnectionLostException:
                pass 
            except omero.Glacier2.SessionNotExistException:
                pass
            except AttributeError:
                pass
            try:
                if softclose:
                    try:
                        r = self.c.sf.getSessionService().getReferenceCount(self._sessionUuid)
                        self.c.closeSession()
                        if r < 2:
                            self._session_cb and self._session_cb.close(self)
                    except Ice.OperationNotExistException:
                        self.c.closeSession()
                else:
                    self._closeSession()
            except omero.Glacier2.SessionNotExistException:
                pass
            except Ice.ConnectionLostException:
                pass 
            self.c = None
        self._proxies = NoProxies()
        logger.info("closed connecion (uuid=%s)" % str(self._sessionUuid))

    def __del__ (self):
        logger.debug("##GARBAGE COLLECTOR KICK IN")
    
    def _createProxies (self):
        """
        Creates proxies to the server services.
        """
        
        if not isinstance(self._proxies, NoProxies):
            logger.debug("## Reusing proxies")
            for k, p in self._proxies.items():
                p._resyncConn(self)
        else:
            logger.debug("## Creating proxies")
            self._proxies = {}
            self._proxies['admin'] = ProxyObjectWrapper(self, 'getAdminService')
            self._proxies['query'] = ProxyObjectWrapper(self, 'getQueryService')
            self._proxies['rendering'] = ProxyObjectWrapper(self, 'createRenderingEngine')
            self._proxies['rendsettings'] = ProxyObjectWrapper(self, 'getRenderingSettingsService')
            #self._proxies['projection'] = ProxyObjectWrapper(self, 'getProjectionService')
            self._proxies['rawpixels'] = ProxyObjectWrapper(self, 'createRawPixelsStore')
            self._proxies['thumbs'] = ProxyObjectWrapper(self, 'createThumbnailStore')
            self._proxies['container'] = ProxyObjectWrapper(self, 'getContainerService')
            self._proxies['pixel'] = ProxyObjectWrapper(self, 'getPixelsService')
    #            self._proxies['ldap'] = ProxyObjectWrapper(self, 'getLdapService')
            self._proxies['metadata'] = ProxyObjectWrapper(self, 'getMetadataService')
            self._proxies['rawfile'] = ProxyObjectWrapper(self, 'createRawFileStore')
            self._proxies['repository'] = ProxyObjectWrapper(self, 'getRepositoryInfoService')
    #            self._proxies['script'] = ProxyObjectWrapper(self, 'getScriptService')
    #            self._proxies['search'] = ProxyObjectWrapper(self, 'createSearchService')
    #            self._proxies['session'] = ProxyObjectWrapper(self, 'getSessionService')
            self._proxies['share'] = ProxyObjectWrapper(self, 'getShareService')
    #            self._proxies['thumbs'] = ProxyObjectWrapper(self, 'createThumbnailStore')
            self._proxies['timeline'] = ProxyObjectWrapper(self, 'getTimelineService')
            self._proxies['types'] = ProxyObjectWrapper(self, 'getTypesService')
    #            self._proxies['update'] = ProxyObjectWrapper(self, 'getUpdateService')
            self._proxies['config'] = ProxyObjectWrapper(self, 'getConfigService')
        self._ctx = self._proxies['admin'].getEventContext()
        self._userid = self._ctx.userId
        self._user = self.getExperimenter(self._userid)
        if self._session_cb: #pragma: no cover
            if self._was_join:
                self._session_cb.join(self)
            else:
                self._session_cb.create(self)
    
    def _createSession (self):
        """
        Creates a new session for the principal given in the constructor.
        """
        
        s = self.c.createSession(self._ic_props[omero.constants.USERNAME],
                                 self._ic_props[omero.constants.PASSWORD])
        self._sessionUuid = self.c.sf.ice_getIdentity().name
        ss = self.c.sf.getSessionService()
        self._session = ss.getSession(self._sessionUuid)
        self._lastGroup = None
        s.detachOnDestroy()
        self._was_join = False
        if self.group is not None:
            # try something that fails if the user don't have permissions on the group
            self.c.sf.getAdminService().getEventContext()
    
    def _closeSession (self):
        """
        Close session.
        """
        
        self._session_cb and self._session_cb.close(self)
        if self._sessionUuid:
            s = omero.model.SessionI()
            s._uuid = omero_type(self._sessionUuid)
            try:
                r = 1
                while r:
                    r = self.c.sf.getSessionService().closeSession(s)
            except Ice.ObjectNotExistException:
                pass
            except omero.RemovedSessionException:
                pass
            except ValueError:
                raise
            except: #pragma: no cover
                logger.warn(traceback.format_exc())
        try:
            self.c.closeSession()
        except omero.Glacier2.SessionNotExistException: #pragma: no cover
            pass
    
    def _resetOmeroClient (self):
        """
        Resets omero.client object.
        """
        
        if self.host is not None:
            logger.info('host: %s, port: %i' % (str(self.host), int(self.port)))
            self.c = omero.client(host=str(self.host), port=int(self.port))#, pmap=['--Ice.Config='+','.join(self.ice_config)])
        else:
            logger.info('--Ice.Config='+','.join(self.ice_config))
            self.c = omero.client(pmap=['--Ice.Config='+','.join(self.ice_config)])
    
    def connect (self, sUuid=None):
        """
        Creates or retrieves connection for the given sessionUuid.
        Returns True if connected.
        
        @param sUuid:   omero_model_SessionI
        @return:        Boolean
        """
        
        logger.debug("Connect attempt, sUuid=%s, group=%s, self.sUuid=%s" % (str(sUuid), str(self.group), self._sessionUuid))
        if not self.c: #pragma: no cover
            self._connected = False
            logger.debug("Ooops. no self._c")
            return False
        try:
            if self._sessionUuid is None and sUuid:
                self._sessionUuid = sUuid
            if self._sessionUuid is not None:
                try:
                    logger.debug('connected? %s' % str(self._connected))
                    if self._connected:
                        self._connected = False
                        logger.debug("was connected, creating new omero.client")
                        self._resetOmeroClient()
                    logger.debug('joining session %s' % self._sessionUuid)
                    s = self.c.joinSession(self._sessionUuid)
                    logger.debug('setting detachOnDestroy for %s' % str(s))
                    s.detachOnDestroy()
                    logger.debug('joinSession(%s)' % self._sessionUuid)
                    self._was_join = True
                except Ice.SyscallException: #pragma: no cover
                    raise
                except Exception, x: #pragma: no cover
                    logger.debug("Error: " + str(x))
                    self._sessionUuid = None
                    if sUuid:
                        return False
            if self._sessionUuid is None:
                if sUuid: #pragma: no cover
                    logger.debug("Uncaptured sUuid failure!") 
                if self._connected:
                    self._connected = False
                    try:
                        #args = self.c._ic_args
                        #logger.debug(str(args))
                        self._closeSession()
                        logger.info("called closeSession()")
                        self._resetOmeroClient()
                        #self.c = omero.client(*args)
                    except omero.Glacier2.SessionNotExistException: #pragma: no cover
                        pass
                setprop = self.c.ic.getProperties().setProperty
                map(lambda x: setprop(x[0],str(x[1])), self._ic_props.items())
                if self._anonymous:
                    self.c.ic.getImplicitContext().put(omero.constants.EVENT, 'Internal')
                if self.group is not None:
                    self.c.ic.getImplicitContext().put(omero.constants.GROUP, self.group)
                try:
                    logger.info("(1) calling createSession()")
                    self._createSession()
                except omero.SecurityViolation:
                    if self.group is not None:
                        # User don't have access to group
                        logger.info("## User not in '%s' group" % self.group)
                        self.group = None
                        self._closeSession()
                        self._sessionUuid = None
                        self._connected=True
                        return self.connect()
                    else: #pragma: no cover
                        logger.debug("BlitzGateway.connect().createSession(): " + traceback.format_exc())
                        logger.info('first create session threw SecurityViolation, hold off 10 secs and retry (but only once)')
                        time.sleep(10)
                        try:
                            logger.info("(2) calling createSession()")
                            self._createSession()
                        except omero.SecurityViolation:
                            if self.group is not None:
                                # User don't have access to group
                                logger.info("## User not in '%s' group" % self.group)
                                self.group = None
                                self._connected=True
                                return self.connect()
                            else:
                                raise
                except Ice.SyscallException: #pragma: no cover
                    raise
                except:
                    logger.info("BlitzGateway.connect().createSession(): " + traceback.format_exc())
                    logger.info(str(self._ic_props))
                    logger.info('first create session had errors, hold off 10 secs and retry (but only once)')
                    time.sleep(10)
                    logger.info("(3) calling createSession()")
                    self._createSession()

            self._last_error = None
            self._createProxies()
            self._connected = True
            logger.info('created connection (uuid=%s)' % str(self._sessionUuid))
        except Ice.SyscallException: #pragma: no cover
            logger.debug('This one is a SyscallException')
            raise
        except Ice.LocalException, x: #pragma: no cover
            logger.debug("connect(): " + traceback.format_exc())
            self._last_error = x
            return False
        except Exception, x: #pragma: no cover
            logger.debug("connect(): " + traceback.format_exc())
            self._last_error = x
            return False
        logger.debug(".. connected!")
        return True

    def getLastError (self): #pragma: no cover
        """
        Returns error if thrown by _BlitzGateway.connect connect.
        
        @return: String
        """
        
        return self._last_error

    def isConnected (self):
        """
        Returns last status of connection.
        
        @return:    Boolean
        """
        
        return self._connected

    ######################
    ## Connection Stuff ##

    def getEventContext (self):
        """
        Returns omero_System_ice.EventContext.
        It containes: 
            shareId, sessionId, sessionUuid, userId, userName, 
            groupId, groupName, isAdmin, isReadOnly, 
            eventId, eventType, eventType,
            memberOfGroups, leaderOfGroups
        
        @return: omero.sys.EventContext
        """
        
        return self._ctx

    def getUser (self):
        """
        Returns current omero_model_ExperimenterI.
         
        @return:    omero.model.ExperimenterI
        """
        
        return self._user
    
    def isAdmin (self):
        """
        Checks if a user has administration privileges.
        
        @return:    Boolean
        """
        
        return self.getEventContext().isAdmin

    def canWrite (self, obj):
        """
        Checks if a user has write privileges to the given object.
        
        @param obj: Given object
        @return:    Boolean
        """
        
        return self.isAdmin() or (self._userid == obj.details.owner.id.val and obj.details.permissions.isUserWrite())

    def setGroupForSession (self, group):
        if self._session is None:
            ss = self.c.sf.getSessionService()
            self._session = ss.getSession(self._sessionUuid)
        if self._session.getDetails().getGroup() == group:
            # Already correct
            return
        a = self.getAdminService()
        if not group.name in [x.name.val for x in a.containedGroups(self._userid)]:
            # User not in this group
            return
        self._lastGroup = self._session.getDetails().getGroup()
        self._session.getDetails().setGroup(group._obj)
        self.getSessionService().updateSession(self._session)

    def revertGroupForSession (self):
        if self._lastGroup:
            self.setGroupForSession(self._lastGroup)
            self._lastGroup = None

    ##############
    ## Services ##

    def getAdminService (self):
        """
        Gets reference to the admin service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['admin']

    def getQueryService (self):
        """
        Gets reference to the query service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        return self._proxies['query']

    def getContainerService (self):
        """
        Gets reference to the container service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['container']

    def getPixelsService (self):
        """
        Gets reference to the pixels service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return ProxyObjectWrapper(self, 'getPixelsService')
    
    def getMetadataService (self):
        """
        Gets reference to the metadata service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['metadata']
    
    def createRawFileStore (self):
        """
        Creates a new raw file store.
        This service is special in that it does not get cached inside BlitzGateway so every call to this function
        returns a new object, avoiding unexpected inherited states.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['rawfile']

    def getRepositoryInfoService (self):
        """
        Gets reference to the repository info service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['repository']

    def getShareService(self):
        """
        Gets reference to the share service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['share']

    def getTimelineService (self):
        """
        Gets reference to the timeline service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['timeline']
    
    def getTypesService(self):
        """
        Gets reference to the types service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['types']

    def getConfigService (self):
        """
        Gets reference to the config service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['config']

    def createRenderingEngine (self):
        """
        Creates a new rendering engine.
        This service is special in that it does not get cached inside BlitzGateway so every call to this function
        returns a new object, avoiding unexpected inherited states.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['rendering']

    def getRenderingSettingsService (self):
        return self._proxies['rendsettings']
   
    def createRawPixelsStore (self):
        """
        Creates a new raw pixels store.
        This service is special in that it does not get cached inside BlitzGateway so every call to this function
        returns a new object, avoiding unexpected inherited states.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['rawpixels']

    def createThumbnailStore (self):
        """
        Creates a new thumbnail store.
        This service is special in that it does not get cached inside BlitzGateway so every call to this function
        returns a new object, avoiding unexpected inherited states.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        
        return self._proxies['thumbs']
    
    def createSearchService (self):
        """
        Creates a new search service.
        This service is special in that it does not get cached inside BlitzGateway so every call to this function
        returns a new object, avoiding unexpected inherited states.
        
        @return omero.gateway.ProxyObjectWrapper
        """
        return ProxyObjectWrapper(self, 'createSearchService')

    def getUpdateService (self):
        """
        Gets reference to the update service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        return ProxyObjectWrapper(self, 'getUpdateService')

    def getDeleteService (self):
        """
        Gets reference to the delete service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        return ProxyObjectWrapper(self, 'getDeleteService')

    def getSessionService (self):
        """
        Gets reference to the session service from ProxyObjectWrapper.
        
        @return:    omero.gateway.ProxyObjectWrapper
        """
        return ProxyObjectWrapper(self, 'getSessionService')

    #############################
    # Top level object fetchers #
    
    def listProjects (self, only_owned=False):
        """
        List every Projects controlled by the security system.
        
        @param only_owned:  Only owned by the logged user. Boolean.
        @return:            Generator yielding _ProjectWrapper
        """
        
        q = self.getQueryService()
        cache = {}
        if only_owned:
            params = omero.sys.Parameters()
            params.map = {'owner_id': rlong(self._userid)}
            for e in q.findAllByQuery("from Project as p where p.details.owner.id=:owner_id order by p.name", params):
                yield ProjectWrapper(self, e, cache)
        else:
            for e in q.findAll('Project', None):
                yield ProjectWrapper(self, e, cache)

#    def listCategoryGroups (self):
#        q = self.getQueryService()
#        cache = {}
#        for e in q.findAll("CategoryGroup", None):
#            yield CategoryGroupWrapper(self, e, cache)

    def listExperimenters (self, start=''):
        """
        Return a generator for all Experimenters whose omeName starts with 'start'.
        The generated values follow the alphabetic order on omeName.
        
        @param start:   Only if omero_model_ExperimenterI.omeName starts with. String.
        @return:        Generator yielding _ExperimenterWrapper
        """
        
        if isinstance(start, UnicodeType):
            start = start.encode('utf8')
        params = omero.sys.Parameters()
        params.map = {'start': rstring('%s%%' % start.lower())}
        q = self.getQueryService()
        rv = q.findAllByQuery("from Experimenter e where lower(e.omeName) like :start", params)
        rv.sort(lambda x,y: cmp(x.omeName.val,y.omeName.val))
        for e in rv:
            yield ExperimenterWrapper(self, e)

    def getExperimenter(self, eid):
        """
        Return an Experimenter for the given ID.
        
        @param eid: User ID.
        @return:    _ExperimenterWrapper or None
        """
        
        admin_serv = self.getAdminService()
        try:
            exp = admin_serv.getExperimenter(long(eid))
            return ExperimenterWrapper(self, exp)
        except omero.ApiUsageException:
            return None

    def lookupExperimenter(self, name):
        """
        Return an Experimenter for the given username.
        
        @param name:    Username. String
        @return:        _ExperimenterWrapper or None
        """
        
        admin_serv = self.getAdminService()
        try:
            exp = admin_serv.lookupExperimenter(str(name))
            return ExperimenterWrapper(self, exp)
        except omero.ApiUsageException:
            return None

    
    ###########################
    # Specific Object Getters #

    def getProject (self, oid):
        """
        Return Project for the given ID.
        
        @param oid: Project ID.
        @return:    _ProjectWrapper or None
        """
        
        q = self.getQueryService()
        pr = q.find("Project", long(oid))
        if pr is not None:
            pr = ProjectWrapper(self, pr)
        return pr
    
    def getDataset (self, oid):
        """
        Return Dataset for the given ID.
        
        @param oid: Dataset ID.
        @return:    _DatasetWrapper or None
        """
        
        q = self.getQueryService()
        ds = q.find("Dataset", long(oid))
        if ds is not None:
            ds = DatasetWrapper(self, ds)
        return ds

    def getImage (self, oid):
        """
        Return Image for the given ID.
        
        @param oid: Image ID.
        @return:    _ImageWrapper or None
        """
        
        q = self.getQueryService()
        img = q.find("Image", long(oid))
        if img is not None:
            img = ImageWrapper(self, img)
        return img

    ##############################
    # Annotation based iterators #
    
    def listImages (self, ns, params=None):
        """
        TODO: description
        
        @return:    Generator yielding _ImageWrapper
        """
        
        if not params:
            params = omero.sys.Parameters()
        if not params.map:
            params.map = {}
        params.map["ns"] = omero_type(ns)
        query = """
                 select i
                   from Image i
                   join i.annotationLinks ial
                   join ial.child as a
                   where a.ns = :ns
                   order by a.id desc """
        for i in self.getQueryService().findAllByQuery(query, params):
            yield ImageWrapper(self, i)


    ###################
    # Searching stuff #


    def searchImages (self, text):
        """
        Fulltext search for images
        """
        return self.simpleSearch(text,(ImageWrapper,))


    def simpleSearch (self, text, types=None):
        """
        Fulltext search on Projects, Datasets and Images.
        TODO: search other object types?
        TODO: batch support.
        """
        if not text:
            return []
        if isinstance(text, UnicodeType):
            text = text.encode('utf8')
        if types is None:
            types = (ProjectWrapper, DatasetWrapper, ImageWrapper)
        search = self.createSearchService()
        if text[0] in ('?','*'):
            search.setAllowLeadingWildcard(True)
        rv = []
        for t in types:
            def actualSearch ():
                search.onlyType(t().OMERO_CLASS)
                search.byFullText(text)
            timeit(actualSearch)()
            if search.hasNext():
                def searchProcessing ():
                    rv.extend(map(lambda x: t(self, x), search.results()))
                timeit(searchProcessing)()
        search.close()
        return rv

def safeCallWrap (self, attr, f): #pragma: no cover
    """
    Captures called function. Throws an exception.
    
    @return:
    """
    
    def inner (*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except omero.ResourceError:
            logger.debug('captured resource error')
            raise
        except omero.SecurityViolation:
            raise
        except omero.ApiUsageException:
            raise
        except Ice.MemoryLimitException:
            raise
        except omero.InternalException:
            raise
        except Ice.Exception, x:
            # Failed
            logger.debug( "Ice.Exception (1) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs)))
            logger.debug(traceback.format_exc())
            # Recreate the proxy object
            try:
                self._obj = self._create_func()
                func = getattr(self._obj, attr)
                return func(*args, **kwargs)
            except Ice.MemoryLimitException:
                raise
            except Ice.Exception, x:
                # Still Failed
                logger.debug("Ice.Exception (2) on safe call %s(%s,%s)" % (attr, str(args), str(kwargs)))
                logger.debug(traceback.format_exc())
                try:
                    # Recreate connection
                    self._connect()
                    logger.debug('last try for %s' % attr)
                    # Last try, don't catch exception
                    func = getattr(self._obj, attr)
                    return func(*args, **kwargs)
                except:
                    raise

    def wrapped (*args, **kwargs): #pragma: no cover
        try:
            return inner(*args, **kwargs)
        except Ice.MemoryLimitException:
            logger.debug("MemoryLimitException! abort, abort...")
            raise
        except omero.SecurityViolation:
            logger.debug("SecurityViolation, bailing out")
            raise
        except omero.ApiUsageException:
            logger.debug("ApiUsageException, bailing out")
            raise
        except Ice.Exception, x:
            if hasattr(x, 'serverExceptionClass'):
                if x.serverExceptionClass == 'ome.conditions.InternalException' and \
                        x.message.find('java.lang.NullPointerException') > 0:
                    logger.debug("NullPointerException, bailing out")
                    raise
            logger.debug("exception caught, first time we back off for 10 secs")
            logger.debug(traceback.format_exc())
            time.sleep(10)
            return inner(*args, **kwargs)
    return wrapped


BlitzGateway = _BlitzGateway


def splitHTMLColor (color):
    """ splits an hex stream of characters into an array of bytes in format (R,G,B,A).
    - abc      -> (0xAA, 0xBB, 0xCC, 0xFF)
    - abcd     -> (0xAA, 0xBB, 0xCC, 0xDD)
    - abbccd   -> (0xAB, 0xBC, 0xCD, 0xFF)
    - abbccdde -> (0xAB, 0xBC, 0xCD, 0xDE)
    """
    try:
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
    except:
        pass
    return None


class ProxyObjectWrapper (object):
    def __init__ (self, conn, func_str):
        self._obj = None
        self._func_str = func_str
        self._resyncConn(conn)
    
    def _connect (self): #pragma: no cover
        """
        Returns True if connected.
        
        @return:    Boolean
        """
        
        logger.debug("proxy_connect: a");
        if not self._conn.connect():
            logger.debug('connect failed')
            logger.debug('/n'.join(traceback.format_stack()))
            return False
        logger.debug("proxy_connect: b");
        self._resyncConn(self._conn)
        logger.debug("proxy_connect: c");
        self._obj = self._create_func()
        logger.debug("proxy_connect: d");
        return True

    def close (self):
        """
        Closes the underlaying service, so next call to the proxy will create a new
        instance of it.
        """
        
        if self._obj:
            self._obj.close()
        self._obj = None
    
    def _resyncConn (self, conn):
        """
        
        @param conn:    Connection
        """
        
        self._conn = conn
        self._sf = conn.c.sf
        self._create_func = getattr(self._sf, self._func_str)
        if self._obj is not None:
            logger.debug("## - refreshing %s" % (self._func_str))
            obj = conn.c.ic.stringToProxy(str(self._obj))
            self._obj = self._obj.checkedCast(obj)

    def _getObj (self):
        """
        
        @return:    obj
        """
        if not self._obj:
            self._obj = self._create_func()
        else:
            self._ping()
        return self._obj

    def _ping (self): #pragma: no cover
        """
        For some reason, it seems that keepAlive doesn't, so every so often I need to recreate the objects.
        
        @return:    Boolean
        """
        
        try:
            if not self._sf.keepAlive(self._obj):
                logger.debug("... died, recreating ...")
                self._obj = self._create_func()
        except Ice.ObjectNotExistException:
            # The connection is there, but it has been reset, because the proxy no longer exists...
            logger.debug("... reset, reconnecting")
            self._connect()
            return False
        except Ice.ConnectionLostException:
            # The connection was lost. This shouldn't happen, as we keep pinging it, but does so...
            logger.debug(traceback.format_stack())
            logger.debug("... lost, reconnecting")
            self._conn._connected = False
            self._connect()
            return False
        except Ice.ConnectionRefusedException:
            # The connection was refused. We lost contact with glacier2router...
            logger.debug(traceback.format_stack())
            logger.debug("... refused, reconnecting")
            self._connect()
            return False
        except omero.RemovedSessionException:
            # Session died on us
            logger.debug(traceback.format_stack())
            logger.debug("... session has left the building, reconnecting")
            self._connect()
            return False
        except Ice.UnknownException:
            # Probably a wrapped RemovedSession
            logger.debug(traceback.format_stack())
            logger.debug("... ice says something bad happened, reconnecting")
            self._connect()
            return False
        return True

    def __getattr__ (self, attr):
        """
        
        @param attr:    Connection
        @return: rv
        """
        # safe call wrapper
        obj = self._obj or self._getObj()
        rv = getattr(obj, attr)
        if callable(rv):
            rv = safeCallWrap(self, attr, rv)
        #self._conn.updateTimeout()
        return rv


class BlitzObjectWrapper (object):
    """
    Object wrapper class.
    """
    
    OMERO_CLASS = None
    LINK_CLASS = None
    CHILD_WRAPPER_CLASS = None
    PARENT_WRAPPER_CLASS = None
    
    def __init__ (self, conn=None, obj=None, cache={}, **kwargs):
        self.__bstrap__()
        self._obj = obj
        self._cache = cache
        self._conn = conn
        if conn is None:
            return
        if hasattr(obj, 'id') and obj.id is not None:
            self._oid = obj.id.val
            if not self._obj.loaded:
                self._obj = self._conn.getQueryService().get(self._obj.__class__.__name__, self._oid)
        self.__prepare__ (**kwargs)

    def __eq__ (self, a):
        return type(a) == type(self) and self._obj.id == a._obj.id and self.getName() == a.getName()

    def __bstrap__ (self):
        pass

    def __prepare__ (self, **kwargs):
        pass

    def __repr__ (self):
        if hasattr(self, '_oid'):
            return '<%s id=%s>' % (self.__class__.__name__, str(self._oid))
        return super(BlitzObjectWrapper, self).__repr__()

    def _getChildWrapper (self):
        if self.CHILD_WRAPPER_CLASS is None:
            raise NotImplementedError
        if type(self.CHILD_WRAPPER_CLASS) is type(''):
            # resolve class
            g = globals()
            logger.debug('C:' + str(g[self.CHILD_WRAPPER_CLASS]))
            if not g.has_key(self.CHILD_WRAPPER_CLASS): #pragma: no cover
                raise NotImplementedError
            self.__class__.CHILD_WRAPPER_CLASS = self.CHILD_WRAPPER_CLASS = g[self.CHILD_WRAPPER_CLASS]
        return self.CHILD_WRAPPER_CLASS

    def _getParentWrapper (self):
        if self.PARENT_WRAPPER_CLASS is None:
            raise NotImplementedError
        if type(self.PARENT_WRAPPER_CLASS) is type(''):
            # resolve class
            g = globals()
            if not g.has_key(self.PARENT_WRAPPER_CLASS): #pragma: no cover
                raise NotImplementedError
            self.__class__.PARENT_WRAPPER_CLASS = self.PARENT_WRAPPER_CLASS = g[self.PARENT_WRAPPER_CLASS]
        return self.PARENT_WRAPPER_CLASS

    def __loadedHotSwap__ (self):
        self._obj = self._conn.getContainerService().loadContainerHierarchy(self.OMERO_CLASS, (self._oid,), None)[0]

    def _moveLink (self, newParent):
        """ moves this object from the current parent container to a new one """
        p = self.listParents()
        if type(p) == type(newParent):
            link = self._conn.getQueryService().findAllByQuery("select l from %s as l where l.parent.id=%i and l.child.id=%i" % (p.LINK_CLASS, p.id, self.id), None)
            if len(link):
                link[0].parent = newParent._obj
                self._conn.getUpdateService().saveObject(link[0])
                return True
        return False

    def getDetails (self):
        return DetailsWrapper (self._conn, self._obj.getDetails())

    def save (self):
        self._obj = self._conn.getUpdateService().saveAndReturnObject(self._obj)

    def saveAs (self, details):
        """ Save this object, keeping the object owner the same as the one on provided details """
        if self._conn.isAdmin():
            d = self.getDetails()
            if d.getOwner() and \
                    d.getOwner().omeName == details.getOwner().omeName and \
                    d.getGroup().name == details.getGroup().name:
                return self.save()
            else:
                p = omero.sys.Principal()
                p.name = details.getOwner().omeName
                p.group = details.getGroup().name
                p.eventType = "User"
                newConnId = self._conn.getSessionService().createSessionWithTimeout(p, 60000)
                newConn = self._conn.clone()
                newConn.connect(sUuid=newConnId.getUuid().val)
            clone = self.__class__(newConn, self._obj)
            clone.save()
            self._obj = clone._obj
            return
        else:
            return self.save()

    def canWrite (self):
        return self._conn.canWrite(self._obj)

    def canOwnerWrite (self):
        return self._obj.details.permissions.isUserWrite()

    #@timeit
    #def getUID (self):
    #    p = self.listParents()
    #    return p and '%s:%s' % (p.getUID(), str(self.id)) or str(self.id)

    #def getChild (self, oid):
    #    q = self._conn.getQueryService()
    #    ds = q.find(self.CHILD_WRAPPER_CLASS.OMERO_CLASS, long(oid))
    #    if ds is not None:
    #        ds = self.CHILD_WRAPPER_CLASS(self._conn, ds)
    #    return ds
    
    def countChildren (self):
        """
        Counts available number of child objects.
        
        @return: Long. The number of child objects available
        """
        
        self._cached_countChildren = len(self._conn.getQueryService().findAllByQuery("from %s as c where c.parent.id=%i" % (self.LINK_CLASS, self._oid), None))
        return self._cached_countChildren

    def countChildren_cached (self):
        """
        countChildren, but caching the first result, useful if you need to call this multiple times in
        a single sequence, but have no way of storing the value between them.
        It is actually a hack to support django template's lack of break in for loops
        
        @return: Long
        """
        
        if not hasattr(self, '_cached_countChildren'):
            return self.countChildren()
        return self._cached_countChildren

    def listChildren (self, ns=None, val=None, params=None):
        """
        Lists available child objects.
        
        @return: Generator yielding child objects.
        """
        
        childw = self._getChildWrapper()
        if not params:
            params = omero.sys.Parameters()
        if not params.map:
            params.map = {}
        params.map["dsid"] = omero_type(self._oid)
        query = "select c from %s as c" % self.LINK_CLASS
        if ns is not None:
            params.map["ns"] = omero_type(ns)
            query += """ join c.child.annotationLinks ial
                         join ial.child as a """
        query += " where c.parent.id=:dsid"
        if ns is not None:
            query += " and a.ns=:ns"
            if val is not None:
                if isinstance(val, StringTypes):
                    params.map["val"] = omero_type(val)
                    query +=" and a.textValue=:val"
        query += " order by c.child.name"
        childnodes = [ x.child for x in self._conn.getQueryService().findAllByQuery(query, params)]
        for child in childnodes:
            yield childw(self._conn, child, self._cache)

    #def listChildren_cached (self):
    #    """ This version caches all child nodes for all parents, so next parent does not need to search again.
    #    Good for full depth traversal, but a waste of time otherwise """
    #    if self.CHILD_WRAPPER_CLASS is None: #pragma: no cover
    #        raise NotImplementedError
    #    if not self._cache.has_key(self.LINK_CLASS):
    #        pdl = {}
    #        for link in self._conn.getQueryService().findAll(self.LINK_CLASS, None):
    #            pid = link.parent.id.val
    #            if pdl.has_key(pid):
    #                pdl[pid].append(link.child)
    #            else:
    #                pdl[pid] = [link.child]
    #        self._cache[self.LINK_CLASS] = pdl
    #    for child in self._cache[self.LINK_CLASS].get(self._oid, ()):
    #        yield self.CHILD_WRAPPER_CLASS(self._conn, child, self._cache)

    @timeit
    def listParents (self, single=True):
        """
        Lists available parent objects.
        
        @return: Generator yielding parent objects
        """
        
        if self.PARENT_WRAPPER_CLASS is None:
            return ()
        parentw = self._getParentWrapper()
        param = omero.sys.Parameters() # TODO: What can I use this for?
        parentnodes = [ x.parent for x in self._conn.getQueryService().findAllByQuery("from %s as c where c.child.id=%i" % (parentw().LINK_CLASS, self._oid), param)]
        if single:
            return len(parentnodes) and parentw(self._conn, parentnodes[0], self._cache) or None
        return map(lambda x: parentw(self._conn, x, self._cache), parentnodes)


    @timeit
    def getAncestry (self):
        rv = []
        p = self.listParents()
        while p:
            rv.append(p)
            p = p.listParents()
        return rv


    def _loadAnnotationLinks (self):
        if not hasattr(self._obj, 'isAnnotationLinksLoaded'): #pragma: no cover
            raise NotImplementedError
        if not self._obj.isAnnotationLinksLoaded():
            links = self._conn.getQueryService().findAllByQuery("select l from %sAnnotationLink as l join fetch l.child as a where l.parent.id=%i" % (self.OMERO_CLASS, self._oid), None)
            self._obj._annotationLinksLoaded = True
            self._obj._annotationLinksSeq = links


    def _getAnnotationLinks (self, ns=None):
        self._loadAnnotationLinks()
        rv = self.copyAnnotationLinks()
        if ns is not None:
            rv = filter(lambda x: x.getChild().getNs() and x.getChild().getNs().val == ns, rv)
        return rv


    def removeAnnotations (self, ns):
        for al in self._getAnnotationLinks(ns=ns):
            a = al.child
            update = self._conn.getUpdateService()
            update.deleteObject(al)
            update.deleteObject(a)
        self._obj.unloadAnnotationLinks()
    
    def getAnnotation (self, ns=None):
        """
        ets the first annotation in the ns namespace, linked to this object
        
        @return: #AnnotationWrapper or None
        """
        rv = self._getAnnotationLinks(ns)
        if len(rv):
            return AnnotationWrapper._wrap(self._conn, rv[0].child)
        return None

    @timeit
    def listAnnotations (self, ns=None):
        """
        List annotations in the ns namespace, linked to this object
        
        @return: Generator yielding AnnotationWrapper
        """
        
        for ann in self._getAnnotationLinks(ns):
            yield AnnotationWrapper._wrap(self._conn, ann.child)


    def _linkAnnotation (self, ann):
        if not ann.getId():
            # Not yet in db, save it
            ann.details.setPermissions(omero.model.PermissionsI())
            ann.details.permissions.setWorldRead(True)
            ann = ann.__class__(self._conn, self._conn.getUpdateService().saveAndReturnObject(ann._obj))
        #else:
        #    ann.save()
        lnktype = "%sAnnotationLinkI" % self.OMERO_CLASS
        lnk = getattr(omero.model, lnktype)()
        lnk.details.setPermissions(omero.model.PermissionsI())
        lnk.details.permissions.setWorldRead(True)
        #lnk.details.permissions.setUserWrite(True)
        lnk.setParent(self._obj.__class__(self._obj.id, False))
        lnk.setChild(ann._obj.__class__(ann._obj.id, False))
        self._conn.getUpdateService().saveObject(lnk)
        return ann


    def linkAnnotation (self, ann, sameOwner=True):
        if sameOwner:
            d = self.getDetails()
            ad = ann.getDetails()
            if self._conn.isAdmin():
                # Keep the annotation owner the same as the linked of object's
                if ad.getOwner() and d.getOwner().omeName == ad.getOwner().omeName and d.getGroup().name == ad.getGroup().name:
                    newConn = ann._conn
                else:
                    p = omero.sys.Principal()
                    p.name = d.getOwner().omeName
                    p.group = d.getGroup().name
                    p.eventType = "User"
                    newConnId = self._conn.getSessionService().createSessionWithTimeout(p, 60000)
                    newConn = self._conn.clone()
                    newConn.connect(sUuid=newConnId.getUuid().val)
                clone = self.__class__(newConn, self._obj)
                ann = clone._linkAnnotation(ann)
            else:
                # Try to match group
                self._conn.setGroupForSession(d.getGroup())
                ann = self._linkAnnotation(ann)
                self._conn.revertGroupForSession()
        else:
            ann = self._linkAnnotation(ann)
        self.unloadAnnotationLinks()
        return ann


    def simpleMarshal (self, xtra=None, parents=False):
        rv = {'type': self.OMERO_CLASS,
              'id': self.getId(),
              #'uid': self.getUID(),
              'name': self.getName(),
              'description': self.getDescription(),
              'xref': {},} # TODO remove xref?
        if xtra: # TODO check if this can be moved to a more specific place
            if xtra.has_key('childCount'):
                rv['child_count'] = self.countChildren()
        if parents:
            def marshalParents ():
                return map(lambda x: x.simpleMarshal(), self.getAncestry())
            p = timeit(marshalParents)()
            rv['parents'] = p
        return rv

    #def __str__ (self):
    #    if hasattr(self._obj, 'value'):
    #        return str(self.value)
    #    return str(self._obj)

    def __getattr__ (self, attr):
        if hasattr(self._obj, attr):
            rv = getattr(self._obj, attr)
            if hasattr(rv, 'val'):
                return isinstance(rv.val, StringType) and rv.val.decode('utf8') or rv.val
            return rv
        raise AttributeError("'%s' object has no attribute '%s'" % (self._obj.__class__.__name__, attr))


    # some methods are accessors in _obj and return and omero:: type. The obvious ones we wrap to return a python type
    
    def getId (self):
        """
        Gets this object ID
        
        @return: Long or None
        """
        
        oid = self._obj.getId()
        return oid is not None and oid.val or None

    def getName (self):
        """
        Gets this object name
        
        @return: String or None
        """
        
        return self._obj.getName().val

    def getDescription (self):
        """
        Gets this object description
        
        @return: String
        """
        
        rv = self._obj.getDescription()
        return rv and rv.val or ''

    def getOwner (self):
        """
        Gets user who is the owner of this object.
        
        @return: _ExperimenterWrapper
        """
        
        return self.getDetails().getOwner()

    def getOwnerFullName (self):
        """
        Gets full name of the owner of this object.
        
        @return: String or None
        """
        
        try:
            lastName = self.getDetails().getOwner().lastName
            firstName = self.getDetails().getOwner().firstName
            middleName = self.getDetails().getOwner().middleName
            
            if middleName is not None and middleName != '':
                name = "%s %s. %s" % (firstName, middleName, lastName)
            else:
                name = "%s %s" % (firstName, lastName)
            return name
        except:
            logger.error(traceback.format_exc())
            return None

    def getOwnerOmeName (self):
        """
        Gets omeName of the owner of this object.
        
        @return: String
        """
        return self.getDetails().getOwner().omeName

    def creationEventDate(self):
        """
        Gets event time in timestamp format (yyyy-mm-dd hh:mm:ss.fffffff) when object was created.
        
        @return: Long
        """
        
        try:
            if self._obj.details.creationEvent.time is not None:
                t = self._obj.details.creationEvent.time.val
            else:
                t = self._conn.getQueryService().get("Event", self._obj.details.creationEvent.id.val).time.val
        except:
            t = self._conn.getQueryService().get("Event", self._obj.details.creationEvent.id.val).time.val
        return datetime.fromtimestamp(t/1000)

    def updateEventDate(self):
        """
        Gets event time in timestamp format (yyyy-mm-dd hh:mm:ss.fffffff) when object was updated.
        
        @return: Long
        """
        
        try:
            if self._obj.details.updateEvent.time is not None:
                t = self._obj.details.updateEvent.time.val
            else:
                t = self._conn.getQueryService().get("Event", self._obj.details.updateEvent.id.val).time.val
        except:
            t = self._conn.getQueryService().get("Event", self._obj.details.updateEvent.id.val).time.val
        return datetime.fromtimestamp(t/1000)


    # setters are also provided
    
    def setName (self, value):
        self._obj.setName(omero_type(value))


class AnnotationWrapper (BlitzObjectWrapper):
    """
    omero_model_AnnotationI class wrapper extends BlitzObjectWrapper.
    """
    registry = {}
    OMERO_TYPE = None

    def __init__ (self, *args, **kwargs):
        super(AnnotationWrapper, self).__init__(*args, **kwargs)
        if self._obj is None and self.OMERO_TYPE is not None:
            self._obj = self.OMERO_TYPE()

    def __eq__ (self, a):
        return type(a) == type(self) and self._obj.id == a._obj.id and self.getValue() == a.getValue() and self.getNs() == a.getNs()

    @classmethod
    def _register (klass, regklass):
        klass.registry[regklass.OMERO_TYPE] = regklass

    @classmethod
    def _wrap (klass, conn, obj):
        if obj.__class__ in klass.registry:
            return klass.registry[obj.__class__](conn, obj)
        else: #pragma: no cover
            return None

    @classmethod
    def createAndLink (klass, target, ns, val=None):
        this = klass()
        this.setNs(ns)
        if val is not None:
            this.setValue(val)
        target.linkAnnotation(this)

    def getNs (self):
        return self._obj.ns.val

    def setNs (self, val):
        self._obj.ns = omero_type(val)
    
    def getValue (self): #pragma: no cover
        raise NotImplementedError

    def setValue (self, val): #pragma: no cover
        raise NotImplementedError


from omero_model_TimestampAnnotationI import TimestampAnnotationI

class TimestampAnnotationWrapper (AnnotationWrapper):
    """
    omero_model_TimestampAnnotatio class wrapper extends AnnotationWrapper.
    """
    
    OMERO_TYPE = TimestampAnnotationI

    def getValue (self):
        return datetime.fromtimestamp(self._obj.timeValue.val / 1000.0)

    def setValue (self, val):
        if isinstance(val, datetime):
            self._obj.timeValue = rtime(long(time.mktime(val.timetuple())*1000))
        elif isinstance(val, omero.RTime):
            self._obj.timeValue = val
        else:
            self._obj.timeValue = rtime(long(val * 1000))

AnnotationWrapper._register(TimestampAnnotationWrapper)

from omero_model_BooleanAnnotationI import BooleanAnnotationI

class BooleanAnnotationWrapper (AnnotationWrapper):
    """
    omero_model_BooleanAnnotationI class wrapper extends AnnotationWrapper.
    """
    
    OMERO_TYPE = BooleanAnnotationI

    def getValue (self):
        return self._obj.boolValue.val

    def setValue (self, val):
        self._obj.boolValue = rbool(not not val)

AnnotationWrapper._register(BooleanAnnotationWrapper)

from omero_model_CommentAnnotationI import CommentAnnotationI

class CommentAnnotationWrapper (AnnotationWrapper):
    """
    omero_model_CommentAnnotationI class wrapper extends AnnotationWrapper.
    """
    
    OMERO_TYPE = CommentAnnotationI

    def getValue (self):
        return self._obj.textValue.val

    def setValue (self, val):
        self._obj.textValue = omero_type(val)

AnnotationWrapper._register(CommentAnnotationWrapper)

from omero_model_LongAnnotationI import LongAnnotationI

class LongAnnotationWrapper (AnnotationWrapper):
    """
    omero_model_LongAnnotationI class wrapper extends AnnotationWrapper.
    """
    OMERO_TYPE = LongAnnotationI

    def getValue (self):
        return self._obj.longValue.val

    def setValue (self, val):
        self._obj.longValue = rlong(val)

AnnotationWrapper._register(LongAnnotationWrapper)

class _ExperimenterWrapper (BlitzObjectWrapper):
    """
    omero_model_ExperimenterI class wrapper extends BlitzObjectWrapper.
    """
    
    def getDetails (self):
        if not self._obj.details.owner:
            details = omero.model.DetailsI()
            details.owner = self._obj
            self._obj._details = details
        return DetailsWrapper(self._conn, self._obj.details)

ExperimenterWrapper = _ExperimenterWrapper

class _ExperimenterGroupWrapper (BlitzObjectWrapper):
    """
    omero_model_ExperimenterGroupI class wrapper extends BlitzObjectWrapper.
    """
    
    pass

ExperimenterGroupWrapper = _ExperimenterGroupWrapper

class DetailsWrapper (BlitzObjectWrapper):
    """
    omero_model_DetailsI class wrapper extends BlitzObjectWrapper.
    """
    
    def __init__ (self, *args, **kwargs):
        super(DetailsWrapper, self).__init__ (*args, **kwargs)
        owner = self._obj.getOwner()
        group = self._obj.getGroup()
        self._owner = owner and ExperimenterWrapper(self._conn, self._obj.getOwner()) or None
        self._group = group and ExperimenterGroupWrapper(self._conn, self._obj.getGroup()) or None

    def getOwner (self):
        return self._owner

    def getGroup (self):
        return self._group

class ColorHolder (object):
    """
    Stores color internally as (R,G,B,A) and allows setting and getting in multiple formats
    """
    
    _color = {'red': 0, 'green': 0, 'blue': 0, 'alpha': 255}

    def __init__ (self, colorname=None):
        self._color = {'red': 0, 'green': 0, 'blue': 0, 'alpha': 255}
        if colorname and colorname.lower() in self._color.keys():
            self._color[colorname.lower()] = 255

    @classmethod
    def fromRGBA(klass,r,g,b,a):
        rv = klass()
        rv.setRed(r)
        rv.setGreen(g)
        rv.setBlue(b)
        rv.setAlpha(a)
        return rv

    def getRed (self):
        return self._color['red']

    def setRed (self, val):
        """
        Set red, as int 0..255 
        
        @param val: value of Red.
        """
        
        self._color['red'] = max(min(255, int(val)), 0)

    def getGreen (self):
        return self._color['green']

    def setGreen (self, val):
        """
        Set green, as int 0..255 
        
        @param val: value of Green.
        """
        
        self._color['green'] = max(min(255, int(val)), 0)

    def getBlue (self):
        return self._color['blue']

    def setBlue (self, val):
        """
        Set Blue, as int 0..255 
        
        @param val: value of Blue.
        """
        
        self._color['blue'] = max(min(255, int(val)), 0)

    def getAlpha (self):
        return self._color['alpha']

    def setAlpha (self, val):
        """
        Set alpha, as int 0..255.
        @param val: value of alpha.
        """
        
        self._color['alpha'] = max(min(255, int(val)), 0)

    def getHtml (self):
        """
        @return: String. The html usable color. Dumps the alpha information.
        """
        
        return "%(red)0.2X%(green)0.2X%(blue)0.2X" % (self._color)

    def getCss (self):
        """
        @return: String. rgba(r,g,b,a) for this color.
        """
        
        c = self._color.copy()
        c['alpha'] /= 255.0
        return "rgba(%(red)i,%(green)i,%(blue)i,%(alpha)0.3f)" % (c)

    def getRGB (self):
        """
        @return: list. A list of (r,g,b) values
        """
        
        return (self._color['red'], self._color['green'], self._color['blue'])

class ChannelWrapper (BlitzObjectWrapper):
    """
    omero_model_ChannelI class wrapper extends BlitzObjectWrapper.
    """
    
    BLUE_MIN = 400
    BLUE_MAX = 500
    GREEN_MIN = 501
    GREEN_MAX = 600
    RED_MIN = 601
    RED_MAX = 700
    COLOR_MAP = ((BLUE_MIN, BLUE_MAX, ColorHolder('Blue')),
                 (GREEN_MIN, GREEN_MAX, ColorHolder('Green')),
                 (RED_MIN, RED_MAX, ColorHolder('Red')),
                 )
    def __prepare__ (self, idx, re):
        self._re = re
        self._idx = idx

    def save (self):
        self._obj.setPixels(omero.model.PixelsI(self._obj.getPixels().getId(), False))
        return super(ChannelWrapper, self).save()

    def isActive (self):
        return self._re.isActive(self._idx)

    def getEmissionWave (self):
        lc = self._obj.getLogicalChannel()
        emWave = lc.getEmissionWave()
        if emWave is None: #pragma: no cover
            # This is probably deprecated, as even tinyTest now gets an emissionWave
            return self._idx
        else:
            return emWave.val

    def getColor (self):
        return ColorHolder.fromRGBA(*self._re.getRGBA(self._idx))

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
        if not self._loadPixels():
            return None
        return func(self, *args, **kwargs)
    return wrapped

class _ImageWrapper (BlitzObjectWrapper):
    """
    omero_model_ImageI class wrapper extends BlitzObjectWrapper.
    """
    
    _re = None
    _pd = None
    _rm = {}
    _pixels = None

    _pr = None # projection

    PROJECTIONS = {
        'normal': -1,
        'intmax': omero.constants.projection.ProjectionType.MAXIMUMINTENSITY,
        'intmean': omero.constants.projection.ProjectionType.MEANINTENSITY,
        'intsum': omero.constants.projection.ProjectionType.SUMINTENSITY,
        }
    
    PLANEDEF = omero.romio.XY

    def __bstrap__ (self):
        self.OMERO_CLASS = 'Image'
        self.LINK_CLASS = None
        self.CHILD_WRAPPER_CLASS = None
        self.PARENT_WRAPPER_CLASS = 'DatasetWrapper'


    def __loadedHotSwap__ (self):
        self._obj = self._conn.getContainerService().getImages(self.OMERO_CLASS, (self._oid,), None)[0]

    def _loadPixels (self):
        if not self._obj.pixelsLoaded:
            self.__loadedHotSwap__()
        return self._obj.sizeOfPixels() > 0

    def _prepareRE (self):
        re = self._conn.createRenderingEngine()
        pixels_id = self._obj.getPrimaryPixels().id.val
        re.lookupPixels(pixels_id)
        if re.lookupRenderingDef(pixels_id) == False: #pragma: no cover
            try:
                re.resetDefaults()
            except omero.ResourceError:
                # broken image
                return False
            re.lookupRenderingDef(pixels_id)
        re.load()
        return re

    def _prepareRenderingEngine (self):
        self._loadPixels()
        if self._re is None:
            if self._obj.sizeOfPixels() < 1:
                return False
            if self._pd is None:
                self._pd = omero.romio.PlaneDef(self.PLANEDEF)
            self._re = self._prepareRE()
        return self._re is not None

    def simpleMarshal (self, xtra=None, parents=False):
        rv = super(_ImageWrapper, self).simpleMarshal(xtra=xtra, parents=parents)
        rv.update({'author': self.getAuthor(),
                   'date': time.mktime(self.getDate().timetuple()),})
        if xtra and xtra.has_key('thumbUrlPrefix'):
            rv['thumb_url'] = xtra['thumbUrlPrefix'] + str(self.id) + '/'
        return rv

    def shortname(self, length=20, hist=5):
        name = self.name
        if not name:
            return ""
        l = len(name)
        if l < length+hist:
            return name
        return "..." + name[l - length:]

    def getAuthor(self):
        q = self._conn.getQueryService()
        e = q.findByQuery("select e from Experimenter e where e.id = %i" % self._obj.details.owner.id.val,None)
        self._author = e.firstName.val + " " + e.lastName.val
        return self._author

    def getDataset(self):
        try:
            q = """
            select ds from Image i join i.datasetLinks dl join dl.parent ds
            where i.id = %i
            """ % self._obj.id.val
            query = self._conn.getQueryService()
            ds = query.findByQuery(q,None)
            return ds and DatasetWrapper(self._conn, ds) or None
        except: #pragma: no cover
            logger.debug(traceback.format_exc())
            return None
        
    def getProject(self):
        try:
            q = """
            select p from Image i join i.datasetLinks dl join dl.parent ds join ds.projectLinks pl join pl.parent p
            where i.id = %i
            """ % self._obj.id.val
            query = self._conn.getQueryService()
            prj = query.findByQuery(q,None)
            return prj and ProjectWrapper(self._conn, prj) or None
        except: #pragma: no cover
            logger.debug(traceback.format_exc())
            return None
        
    def getDate(self):
        try:
            if self._obj.acquisitionDate.val is not None:
                t = self._obj.acquisitionDate.val
            else:
                t = self._obj.details.creationEvent.time.val
        except:
            t = self._conn.getQueryService().get("Event", self._obj.details.creationEvent.id.val).time.val
        return datetime.fromtimestamp(t/1000)
    

#    def getDate(self):
#        try:
#            import time
#            query = self._conn.getQueryService()
#            event = query.findByQuery("select e from Event e where id = %i" % self._obj.details.creationEvent.id.val, None)
#            return time.ctime(event.time.val / 1000)
#        except: # pragma: no cover
#            logger.debug(traceback.format_exc())
#            self._date = "Today"
#            return "Today"

    def _prepareTB (self):
        if not self._loadPixels():
            return None
        pixels_id = self._obj.getPrimaryPixels().getId().val
        tb = self._conn.createThumbnailStore()
        try:
            rv = tb.setPixelsId(pixels_id)
        except omero.InternalException:
            rv = False
        if not rv: #pragma: no cover
            tb.resetDefaults()
            tb.close()
            tb.setPixelsId(pixels_id)
        return tb

    def getThumbnail (self, size=(64,64)):
        try:
            tb = self._prepareTB()
            if tb is None:
                return None
            if isinstance(size, IntType):
                size = (size,)
            if len(size) == 1:
                thumb = tb.getThumbnailByLongestSide
            else:
                thumb = tb.getThumbnail
            size = map(lambda x: rint(x), size)
            rv = thumb(*size)
            return rv
        except Ice.Exception: #pragma: no cover
            traceback.print_exc()
            return None

    @assert_re
    def getChannels (self):
        return [ChannelWrapper(self._conn, c, idx=n, re=self._re) for n,c in enumerate(self._re.getPixels().iterateChannels())]

    def setActiveChannels(self, channels, windows=None, colors=None):
        for c in range(len(self.getChannels())):
            self._re.setActive(c, (c+1) in channels)
            if (c+1) in channels:
                if windows is not None and windows[c][0] is not None and windows[c][1] is not None:
                    self._re.setChannelWindow(c, *windows[c])
                if colors is not None and colors[c]:
                    rgba = splitHTMLColor(colors[c])
                    if rgba:
                        self._re.setRGBA(c, *rgba)
        return True

    def getProjections (self):
        return self.PROJECTIONS.keys()

    def setProjection (self, proj):
        self._pr = proj

    LINE_PLOT_DTYPES = {
        (4, True, True): 'f', # signed float
        (2, False, False): 'H', # unsigned short
        (2, False, True): 'h',  # signed short
        (1, False, False): 'B', # unsigned char
        (1, False, True): 'b',  # signed char
        }

    def getPixelLine (self, z, t, pos, axis, channels=None, range=None):
        """
        Grab a horizontal or vertical line from the image pixel data, for the specified channels
        (or all if not specified) and using the specified range (or 1:1 relative to the image size).
        Axis may be 'h' or 'v', for horizontal or vertical respectively.
        
        @param z:
        @param t:
        @param pos:
        @param axis:
        @param channels:
        @param range:
        @return: rv
        """
        
        if not self._loadPixels():
            logger.debug( "No pixels!")
            return None
        axis = axis.lower()[:1]
        if channels is None:
            channels = map(lambda x: x._idx, filter(lambda x: x.isActive(), self.getChannels()))
        if range is None:
            range = axis == 'h' and self.getHeight() or self.getWidth()
        if not isinstance(channels, (TupleType, ListType)):
            channels = (channels,)
        chw = map(lambda x: (x.getWindowMin(), x.getWindowMax()), self.getChannels())
        rv = []
        pixels_id = self._obj.getPrimaryPixels().getId().val
        rp = self._conn.createRawPixelsStore()
        rp.setPixelsId(pixels_id, True)
        for c in channels:
            bw = rp.getByteWidth()
            key = self.LINE_PLOT_DTYPES.get((bw, rp.isFloat(), rp.isSigned()), None)
            if key is None:
                logger.error("Unknown data type: " + str((bw, rp.isFloat(), rp.isSigned())))
            plot = array.array(key, axis == 'h' and rp.getRow(pos, z, c, t) or rp.getCol(pos, z, c, t))
            plot.byteswap() # TODO: Assuming ours is a little endian system
            # now move data into the windowMin..windowMax range
            offset = -chw[c][0]
            if offset != 0:
                plot = map(lambda x: x+offset, plot)
            normalize = 1.0/chw[c][1]*(range-1)
            if normalize != 1.0:
                plot = map(lambda x: x*normalize, plot)
            if isinstance(plot, array.array):
                plot = plot.tolist()
            rv.append(plot)
        return rv
        

    def getRow (self, z, t, y, channels=None, range=None):
        return self.getPixelLine(z,t,y,'h',channels,range)

    def getCol (self, z, t, x, channels=None, range=None):
        return self.getPixelLine(z,t,x,'v',channels,range)

    @assert_re
    def getRenderingModels (self):
        if not len(self._rm):
            for m in [BlitzObjectWrapper(self._conn, m) for m in self._re.getAvailableModels()]:
                self._rm[m.value.lower()] = m
        return self._rm.values()

    @assert_re
    def getRenderingModel (self):
        return BlitzObjectWrapper(self._conn, self._re.getModel())

    def setGreyscaleRenderingModel (self):
        """
        Sets the Greyscale rendering model on this image's current renderer
        """
        
        rm = self.getRenderingModels()
        self._re.setModel(self._rm.get('greyscale', rm[0])._obj)

    def setColorRenderingModel (self):
        """
        Sets the HSB rendering model on this image's current renderer
        """
        
        rm = self.getRenderingModels()
        self._re.setModel(self._rm.get('rgb', rm[0])._obj)

    def isGreyscaleRenderingModel (self):
        return self.getRenderingModel().value.lower() == 'greyscale'
        
    @assert_re
    def renderJpeg (self, z, t, compression=0.9):
        self._pd.z = long(z)
        self._pd.t = long(t)
        try:
            if compression is not None:
                try:
                    self._re.setCompressionLevel(float(compression))
                except omero.SecurityViolation: #pragma: no cover
                    self._obj.clearPixels()
                    self._obj.pixelsLoaded = False
                    self._re = None
                    return self.renderJpeg(z,t,None)
            projection = self.PROJECTIONS.get(self._pr, -1)
            if not isinstance(projection, omero.constants.projection.ProjectionType):
                rv = self._re.renderCompressed(self._pd)
            else:
                rv = self._re.renderProjectedCompressed(projection, self._pd.t, 1, 0, self.z_count()-1)
            return rv
        except omero.InternalException: #pragma: no cover
            logger.debug(traceback.format_exc())
            return None
        except Ice.MemoryLimitException: #pragma: no cover
            # Make sure renderCompressed isn't called again on this re, as it hangs
            self._obj.clearPixels()
            self._obj.pixelsLoaded = False
            self._re = None
            raise

    def renderImage (self, z, t, compression=0.9):
        rv = self.renderJpeg(z,t,compression)
        if rv is not None:
            i = StringIO(rv)
            rv = Image.open(i)
        return rv

    def renderSplitChannel (self, z, t, compression=0.9, border=2):
        """
        Prepares a jpeg representation of a 2d grid holding a render of each channel, 
        along with one for all channels at the set Z and T points.
        
        @param z:
        @param t:
        @param compression:
        @param border:
        @return: value
        """
        
        img = self.renderSplitChannelImage(z,t,compression, border)
        rv = StringIO()
        img.save(rv, 'jpeg', quality=int(compression*100))
        return rv.getvalue()

    def splitChannelDims (self, border=2):
        c = self.c_count()
        # Greyscale, no channel overlayed image
        x = sqrt(c)
        y = int(round(x))
        if x > y:
            x = y+1
        else:
            x = y
        rv = {'g':{'width': self.getWidth()*x + border*(x+1),
              'height': self.getHeight()*y+border*(y+1),
              'border': border,
              'gridx': x,
              'gridy': y,}
              }
        # Color, one extra image with all channels overlayed
        c += 1
        x = sqrt(c)
        y = int(round(x))
        if x > y:
            x = y+1
        else:
            x = y
        rv['c'] = {'width': self.getWidth()*x + border*(x+1),
              'height': self.getHeight()*y+border*(y+1),
              'border': border,
              'gridx': x,
              'gridy': y,}
        return rv

    def renderSplitChannelImage (self, z, t, compression=0.9, border=2):
        """
        Prepares a PIL Image with a 2d grid holding a render of each channel, 
        along with one for all channels at the set Z and T points.
        
        @param z:
        @param t:
        @param compression:
        @param border:
        @return: canvas
        """
                
        dims = self.splitChannelDims(border=border)[self.isGreyscaleRenderingModel() and 'g' or 'c']
        canvas = Image.new('RGBA', (dims['width'], dims['height']), '#fff')
        cmap = [ch.isActive() and i+1 or 0 for i,ch in enumerate(self.getChannels())]
        c = self.c_count()
        pxc = 0
        px = dims['border']
        py = dims['border']
        
        # Font sizes depends on image width
        w = self.getWidth()
        if w >= 640:
            fsize = (int((w-640)/128)*8) + 24
            if fsize > 64:
                fsize = 64
        elif w >= 512:
            fsize = 24
        elif w >= 384: #pragma: no cover
            fsize = 18
        elif w >= 298: #pragma: no cover
            fsize = 14
        elif w >= 256: #pragma: no cover
            fsize = 12
        elif w >= 213: #pragma: no cover
            fsize = 10
        elif w >= 96: #pragma: no cover
            fsize = 8
        else: #pragma: no cover
            fsize = 0
        if fsize > 0:
            font = ImageFont.load('%s/pilfonts/B%0.2d.pil' % (THISPATH, fsize) )


        for i in range(c):
            if cmap[i]:
                self.setActiveChannels((i+1,))
                img = self.renderImage(z,t, compression)
                if fsize > 0:
                    draw = ImageDraw.ImageDraw(img)
                    draw.text((2,2), "w=%i" % (self.getChannels()[i].getEmissionWave()), font=font, fill="#fff")
                canvas.paste(img, (px, py))
            pxc += 1
            if pxc < dims['gridx']:
                px += self.getWidth() + border
            else:
                pxc = 0
                px = border
                py += self.getHeight() + border
        if not self.isGreyscaleRenderingModel():
            self.setActiveChannels(cmap)
            img = self.renderImage(z,t, compression)
            if fsize > 0:
                draw = ImageDraw.ImageDraw(img)
                draw.text((2,2), "combined", font=font, fill="#fff")
            canvas.paste(img, (px, py))
        return canvas

    LP_PALLETE = [0,0,0,0,0,0,255,255,255]
    LP_TRANSPARENT = 0 # Some color
    LP_BGCOLOR = 1 # Black
    LP_FGCOLOR = 2 # white
    def prepareLinePlotCanvas (self, z, t):
        """
        Common part of horizontal and vertical line plot rendering.
        @returns: (Image, width, height).
        """
        channels = filter(lambda x: x.isActive(), self.getChannels())
        width = self.getWidth()
        height = self.getHeight()

        pal = list(self.LP_PALLETE)
        # Prepare the palette taking channel colors in consideration
        for channel in channels:
            pal.extend(channel.getColor().getRGB())

        # Prepare the PIL classes we'll be using
        im = Image.new('P', (width, height))
        im.putpalette(pal)
        return im, width, height


    @assert_re
    def renderRowLinePlotGif (self, z, t, y, linewidth=1):
        self._pd.z = long(z)
        self._pd.t = long(t)

        im, width, height = self.prepareLinePlotCanvas(z,t)
        base = height - 1

        draw = ImageDraw.ImageDraw(im)
        # On your marks, get set... go!
        draw.rectangle([0, 0, width-1, base], fill=self.LP_TRANSPARENT, outline=self.LP_TRANSPARENT)
        draw.line(((0,y),(width, y)), fill=self.LP_FGCOLOR, width=linewidth)

        # Grab row data
        rows = self.getRow(z,t,y)

        for r in range(len(rows)):
            chrow = rows[r]
            color = r + self.LP_FGCOLOR + 1
            last_point = base-chrow[0]
            for i in range(len(chrow)):
                draw.line(((i, last_point), (i, base-chrow[i])), fill=color, width=linewidth)
                last_point = base-chrow[i]
        del draw
        out = StringIO()
        im.save(out, format="gif", transparency=0)
        return out.getvalue()

    @assert_re
    def renderColLinePlotGif (self, z, t, x, linewidth=1):
        self._pd.z = long(z)
        self._pd.t = long(t)

        im, width, height = self.prepareLinePlotCanvas(z,t)

        draw = ImageDraw.ImageDraw(im)
        # On your marks, get set... go!
        draw.rectangle([0, 0, width-1, height-1], fill=self.LP_TRANSPARENT, outline=self.LP_TRANSPARENT)
        draw.line(((x,0),(x, height)), fill=self.LP_FGCOLOR, width=linewidth)

        # Grab col data
        cols = self.getCol(z,t,x)

        for r in range(len(cols)):
            chcol = cols[r]
            color = r + self.LP_FGCOLOR + 1
            last_point = chcol[0]
            for i in range(len(chcol)):
                draw.line(((last_point, i), (chcol[i], i)), fill=color, width=linewidth)
                last_point = chcol[i]
        del draw
        out = StringIO()
        im.save(out, format="gif", transparency=0)
        return out.getvalue()

    @assert_re
    def getZ (self):
        return self._pd.z

    @assert_re
    def getT (self):
        return self._pd.t

    @assert_pixels
    def getPixelSizeX (self):
        rv = self._obj.getPrimaryPixels().getPhysicalSizeX()
        return rv is not None and rv.val or None

    @assert_pixels
    def getPixelSizeY (self):
        rv = self._obj.getPrimaryPixels().getPhysicalSizeY()
        return rv is not None and rv.val or None

    @assert_pixels
    def getPixelSizeZ (self):
        rv = self._obj.getPrimaryPixels().getPhysicalSizeZ()
        return rv is not None and rv.val or None

    @assert_pixels
    def getWidth (self):
        return self._obj.getPrimaryPixels().getSizeX().val

    @assert_pixels
    def getHeight (self):
        return self._obj.getPrimaryPixels().getSizeY().val

    @assert_pixels
    def z_count (self):
        return self._obj.getPrimaryPixels().getSizeZ().val

    @assert_pixels
    def t_count (self):
        return self._obj.getPrimaryPixels().getSizeT().val

    @assert_pixels
    def c_count (self):
        return self._obj.getPrimaryPixels().getSizeC().val

    def clearDefaults (self):
        """
        Removes specific color settings from channels
        
        @return: Boolean
        """
        
        if not self.canWrite():
            return False
        for c in self.getChannels():
            c.unloadRed()
            c.unloadGreen()
            c.unloadBlue()
            c.unloadAlpha()
            c.save()
        self._conn.getDeleteService().deleteSettings(self.getId())
        return True

    @assert_re
    def saveDefaults (self):
        """
        Limited support for saving the current prepared image rendering defs.
        Right now only channel colors are saved back.
        
        @return: Boolean
        """
        
        if not self.canWrite():
            return False
        self._re.saveCurrentSettings()
        return True

ImageWrapper = _ImageWrapper

class _DatasetWrapper (BlitzObjectWrapper):
    """
    omero_model_DatasetI class wrapper extends BlitzObjectWrapper.
    """
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Dataset'
        self.LINK_CLASS = "DatasetImageLink"
        self.CHILD_WRAPPER_CLASS = 'ImageWrapper'
        self.PARENT_WRAPPER_CLASS = 'ProjectWrapper'

    def __loadedHotSwap__ (self):
        super(_DatasetWrapper, self).__loadedHotSwap__()
        if not self._obj.isImageLinksLoaded():
            links = self._conn.getQueryService().findAllByQuery("select l from DatasetImageLink as l join fetch l.child as a where l.parent.id=%i" % (self._oid), None)
            self._obj._imageLinksLoaded = True
            self._obj._imageLinksSeq = links

DatasetWrapper = _DatasetWrapper

class _ProjectWrapper (BlitzObjectWrapper):
    """
    omero_model_ProjectI class wrapper extends BlitzObjectWrapper.
    """
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Project'
        self.LINK_CLASS = "ProjectDatasetLink"
        self.CHILD_WRAPPER_CLASS = 'DatasetWrapper'
        self.PARENT_WRAPPER_CLASS = None

ProjectWrapper = _ProjectWrapper

#class CategoryWrapper (BlitzObjectWrapper):
#    def __bstrap__ (self):
#        self.LINK_CLASS = "CategoryImageLink"
#        self.CHILD_WRAPPER_CLASS = ImageWrapper
#        self.PARENT_WRAPPER_CLASS= 'CategoryGroupWrapper'
#
#class CategoryGroupWrapper (BlitzObjectWrapper):
#    def __bstrap__ (self):
#        self.LINK_CLASS = "CategoryGroupCategoryLink"
#        self.CHILD_WRAPPER_CLASS = CategoryWrapper
#        self.PARENT_WRAPPER_CLASS = None
