#!/usr/bin/env python
# 
# webclient_gateway
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

try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    try:
        import Image, ImageDraw # see ticket:2597
    except:
        logger.error("You need to install the Python Imaging Library. Get it at http://www.pythonware.com/products/pil/")
        logger.error(traceback.format_exc())

from StringIO import StringIO

import time
from datetime import datetime
from types import IntType, ListType, TupleType, UnicodeType, StringType

import Ice
import Glacier2
import omero.gateway
import omero.scripts

import omero_api_IScript_ice

from omero.rtypes import *
from omero.model import FileAnnotationI, TagAnnotationI, \
                        DatasetI, ProjectI, ImageI, ScreenI, PlateI, \
                        DetectorI, FilterI, ObjectiveI, InstrumentI, \
                        LaserI
from omero.sys import ParametersI
from omero.gateway import AnnotationWrapper, FileAnnotationWrapper, \
                        TagAnnotationWrapper, CommentAnnotationWrapper, \
                        ExperimenterGroupWrapper, ExperimenterWrapper, \
                        EnumerationWrapper, BlitzObjectWrapper, \
                        ShareCommentWrapper, ShareWrapper, WellWrapper

from django.utils.translation import ugettext as _
from django.conf import settings
from django.core.mail import send_mail
from django.core.mail import EmailMultiAlternatives

try:
    PAGE = settings.PAGE
except:
    PAGE = 24

logger = logging.getLogger('webclient_gateway')

class OmeroWebGateway (omero.gateway.BlitzGateway):

    def __init__ (self, *args, **kwargs):
        """
        Create the connection wrapper. Does not attempt to connect at this stage
        Initialises the omero.client
        
        @param username:    User name. If not specified, use 'omero.gateway.anon_user'
        @type username:     String
        @param passwd:      Password.
        @type passwd:       String
        @param client_obj:  omero.client
        @param group:       name of group to try to connect to
        @type group:        String
        @param clone:       If True, overwrite anonymous with False
        @type clone:        Boolean
        @param try_super:   Try to log on as super user ('system' group) 
        @type try_super:    Boolean
        @param host:        Omero server host. 
        @type host:         String
        @param port:        Omero server port. 
        @type port:         Integer
        @param extra_config:    Dictionary of extra configuration
        @type extra_config:     Dict
        @param secure:      Initial underlying omero.client connection type (True=SSL/False=insecure)
        @type secure:       Boolean
        @param anonymous:   
        @type anonymous:    Boolean
        @param useragent:   Log which python clients use this connection. E.g. 'OMERO.webadmin'
        @type useragent:    String
        
        @param _shareId:    Active share ID
        @type _shareId:     Long
        """
        
        super(OmeroWebGateway, self).__init__(*args, **kwargs)
        self._shareId = None

    def connect (self, *args, **kwargs):
        """
        Creates or retrieves connection for the given sessionUuid and
        removes some groups from the event context
        Returns True if connected.
        
        @param sUuid:       session uuid
        @type sUuid:        omero_model_SessionI
        @return:            Boolean
        """
        
        rv = super(OmeroWebGateway, self).connect(*args,**kwargs)
        if rv: # No _ctx available otherwise #3218
            if self._ctx.userName!="guest":
                self.removeGroupFromContext()
        return rv

    def attachToShare (self, share_id):
        """
        Turns on the access control lists attached to the given share for the
        current session. Warning: this will slow down the execution of the
        current session for all database reads. Writing to the database will not
        be allowed. If share does not exist or is not accessible (non-members) or
        is disabled, then an ValidationException is thrown.
        
        @param shareId:     share id
        @type shareId:      Long        
        """
        
        sh = self._proxies['share'].getShare(long(share_id))
        if self._shareId is None:
            self._proxies['share'].activate(sh.id.val)
        self._shareId = sh.id.val

    def getShareId(self):
        """
        Returns active share id .
         
        @return:    Share ID
        @rtype:     Long
        """
        
        if self.getEventContext().shareId is not None:
            if self.getEventContext().shareId != self._shareId and self._shareId > 0:
                self._shareId = self.getEventContext().shareId
        return self._shareId

    def removeGroupFromContext (self):
        """
        Removes group "User" from the current context.
        """

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
        """ 
        Every time session is created default group becomes active group 
        and is loaded with the security for the current user and thread.
        Public data has to be created in the context of the group where user,
        who would like to look at these data, is a member of.
        Public data can be only visible by the member of group and owners.
        
        @param gid:     New active group ID
        @type gid:      Long
        
        @return:        Boolean
        """        
        
        try:
            for k in self._proxies.keys():
                self._proxies[k].close()
                
            self.c.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid, False))
            admin_serv = self.getAdminService()
            admin_serv.setDefaultGroup(self.getUser()._obj, omero.model.ExperimenterGroupI(gid, False))
            self._ctx = self._proxies['admin'].getEventContext()
            return True
        except omero.SecurityViolation:
            logger.error(traceback.format_exc())
            return False
        except:
            logger.error(traceback.format_exc())
            return False
    
    ##############################################
    ##   Forgotten password                     ##
    
    def isForgottenPasswordSet(self):
        """
        Retrieves a configuration value "omero.resetpassword.config" for
        Forgotten password form from the backend store.
        
        @return:    Boolean
        """
        
        conf = self.getConfigService()
        try:
            return bool(conf.getConfigValue("omero.resetpassword.config").title())
        except:
            logger.error(traceback.format_exc())
            return False
    
    def reportForgottenPassword(self, username, email):
        """
        Allows to reset the password (temporary password is sent). The
        given email must match the email for the user listed under the name
        argument.
        
        @param username:    omename
        @type username:     String
        @param email:       email address
        @type email:        String
        
        """
        
        admin_serv = self.getAdminService()
        admin_serv.reportForgottenPassword(username, email)
    
    ##############################################
    ##   IAdmin                                 ##
    
    def isAnythingCreated(self):
        """
        Checks if any of the experimenter was created before
        
        @return:    Boolean
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["default_names"] = rlist([rstring("user"), rstring("system"), rstring("guest")])
        f = omero.sys.Filter()
        f.limit = rint(1)
        p.theFilter = f
        sql = "select g from ExperimenterGroup as g where g.name not in (:default_names)"
        if len(q.findAllByQuery(sql, p)) > 0:
            return False
        return True
    
    def getExperimenters(self, ids=None):
        """ 
        Get experimenters for the given user ids. If ID is not set, return current user.
        TODO: omero.gateway.BlitzGateway has getExperimenter(id) method
        
        @param ids:     List of experimenter IDs
        @type ids:      L{Long} 
        @return:        Generator yielding experimetners list
        @rtype:         L{ExperimenterWrapper} generator
        
        """
        
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
    
    def getExperimenterGroups(self, ids):
        """ 
        Get group for for the given group ids. 
        TODO: omero.gateway.BlitzGateway has getGroup(id) method
        
        @param ids:     List of group IDs
        @type ids:      L{Long} 
        @return:        Generator yielding groups list
        @rtype:         L{ExperimenterGroupWrapper} generator
        """
            
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in ids])
        sql = "select e from ExperimenterGroup as e where e.id in (:ids)"
        for e in q.findAllByQuery(sql, p):
            if e.name.val != 'user':
                yield ExperimenterGroupWrapper(self, e)
    
    def listLdapAuthExperimenters(self):
        """
        Lists all IDs of experimenters who are authenticated by LDAP
        (has set dn on password table).
        
        @return:    List of experimetner IDs
        @rtype:     L{Dict of String: Long}
        """
        
        admin_serv = self.getAdminService()
        return admin_serv.lookupLdapAuthExperimenters()
    
    def getLdapAuthExperimenter(self, eid):
        """
        Return DN of the specific experimenter if uses LDAP authentication 
        (has set dn on password table) or None.
        
        @param eid:     experimenter ID
        @type eid:      L{Long} 
        @return:        Distinguished Name 
        @rtype:         String
        """
        
        admin_serv = self.getAdminService()
        return admin_serv.lookupLdapAuthExperimenter(long(eid))
            
    def listExperimenters (self, start=''):
        """
        Return a generator for all Experimenters whose omeName starts with 'start'.
        The generated values follow the alphabetic order on omeName.
        TODO: omero.gateway.BlitzGateway has this exact same method & code already. 
        
        @param start:   Only if omero_model_ExperimenterI.omeName starts with. String.
        @return:        Generator yielding experimenter list
        @rtype:         L{ExperimenterWrapper} generator
        """
        
        if isinstance(start, UnicodeType):
            start = start.encode('utf8')
        params = omero.sys.Parameters()
        params.map = {'start': rstring('%s%%' % start.lower())}
        q = self.getQueryService()
        sql = "from Experimenter e where lower(e.omeName) like :start"
        rv = q.findAllByQuery(sql, params)
        rv.sort(lambda x,y: cmp(x.omeName.val,y.omeName.val))
        for e in rv:
            yield ExperimenterWrapper(self, e)

    #def getCurrentSupervisor(self):
    #    """
    #    Gets the owner of a group for current user.
    #    
    #    @return:        ExperimenterWrapper
    #    """
    #    
    #    p = omero.sys.ParametersI()
    #    p.map = {}
    #    p.map["id"] = rlong(self.getEventContext().groupId)

    #    # TODO: there can now be multiple supervisors
    #    p.page(0,1)
    #    supervisor = self.getQueryService().findByQuery(\
    #        """select e from ExperimenterGroup as g 
    #           join g.groupExperimenterMap as m join m.child as e
    #           where m.owner = true and g.id = :id""", p)
    #    return ExperimenterWrapper(self, supervisor)
    
    #def getScriptwithDetails(self, sid):
    #    script_serv = self.getScriptService()
    #    return script_serv.getScriptWithDetails(long(sid))
    
    #def lookupScripts(self):
    #    script_serv = self.getScriptService()
    #    return script_serv.getScripts()
    
    def getServerVersion(self):
        """
        Retrieves a configuration value "omero.version" from the backend store.
        
        @return:        String
        """
        
        conf = self.getConfigService()
        return conf.getConfigValue("omero.version")


    #########################################################
    ##  From Bram b(dot)gerritsen(at)nki(dot)nl            ##
    
    def findWellInPlate (self, plate_name, row, column):
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map['pname'] = rstring(str(plate_name))
        p.map['row'] = rint(int(row))
        p.map['column'] = rint(int(column))
    
        sql = """select well from Well as well 
              left outer join fetch well.plate as pt 
              left outer join fetch well.wellSamples as ws 
              inner join fetch ws.image as img 
              where well.plate.name = :pname and well.row = :row 
              and well.column = :column"""
        well = q.findByQuery(sql, p)
        if well is None:
            return None
        else:
            return WellWrapper(self, well, None)
    
    
    ##############################################
    ##   DATA RETRIVAL                          ##

    def listProjects (self, eid=None, page=None):
        """
        List every Project controlled by the security system, ordered by id
        If user id not set, owned by the current user.
        
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Generator yielding Projects
        @rtype:             L{ProjectWrapper} generator
        """
        
        """ 
        TODO: omero.gateway.BlitzGateway.listProjects(self, only_owned=False)  
        TODO: page ignored. 
        """
        
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
    
    def listOrphanedDatasets (self, eid=None, page=None):
        """
        List every orphaned Datasets controlled by the security system, 
        ordered by id. If user id not set, owned by the current user.
        
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Generator yielding Datasets
        @rtype:             L{DatasetWrapper} generator
        """
                
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
    
    def listOrphanedImages (self, eid=None, page=None):
        """
        List every orphaned Images controlled by the security system, 
        ordered by id. If user id not set, owned by the current user.
        
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Generator yielding Images
        @rtype:             L{ImageWrapper} generator
        """
        
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
                ") and not exists ( "\
                    "select ws from WellSample as ws "\
                    "where ws.image=im.id and ws.details.owner.id=:eid "\
                ") order by im.id asc"
        for e in q.findAllByQuery(sql,p):
            yield ImageWrapper(self, e)
    
    def listImagesInDataset (self, oid, eid=None, page=None):
        """
        List every Images in the given Dataset 
        controlled by the security system, ordered by id.
        If user id not set, owned by the current user.
        
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Generator yielding Images
        @rtype:             L{ImageWrapper} generator
        """
        
        
        """ 
        TODO: omero.gateway.DatasetWrapper.listChildren
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(PAGE)
            f.offset = rint((int(page)-1)*PAGE)
            p.theFilter = f
        sql = "select im from Image im "\
                "join fetch im.details.creationEvent "\
                "join fetch im.details.owner join fetch im.details.group " \
                "left outer join fetch im.datasetLinks dil "\
                "left outer join fetch dil.parent d " \
                "where d.id = :oid " \
                "order by im.id asc"
        for e in q.findAllByQuery(sql, p):
            kwargs = {'link': BlitzObjectWrapper(self, e.copyDatasetLinks()[0])}
            yield ImageWrapper(self, e, None, **kwargs)
    
    # SPW
    def listScreens(self, eid=None, page=None):
        """
        List every Screens controlled by the security system, ordered by id
        If user id not set, owned by the current user.
        
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Generator yielding Screens
        @rtype:             L{ScreenWrapper} generator
        """
        
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
            
    def listOrphanedPlates (self, eid=None, page=None):
        """
        List every orphaned Plates controlled by the security system, 
        ordered by id. If user id not set, owned by the current user.
        
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Generator yielding Plates
        @rtype:             L{PlateWrapper} generator
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select pl from Plate as pl " \
                "join fetch pl.details.creationEvent "\
                "join fetch pl.details.owner join fetch pl.details.group " \
                "where pl.details.owner.id=:eid and " \
                "not exists ( "\
                    "select spl from ScreenPlateLink as spl where spl.child=pl.id "\
                ") order by pl.id asc"
        for e in q.findAllByQuery(sql, p):
            yield DatasetWrapper(self, e)
    
    def listWellsInPlate(self, oid, index=None, eid=None):
        """
        List every Plates in the given Well 
        controlled by the security system, ordered by id.
        If user id not set, owned by the current user.
        
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Generator yielding Wells
        @rtype:             L{WellWrapper} generator
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["oid"] = rlong(long(oid))
        sql = "select well from Well as well "\
                "join fetch well.details.creationEvent "\
                "join fetch well.details.owner join fetch well.details.group " \
                "left outer join fetch well.plate as pt "\
                "left outer join fetch well.wellSamples as ws " \
                "left outer join fetch ws.image as img "\
                "where well.plate.id = :oid"
        index = index is None and 0 or index
        kwargs = {'index': index}
        for e in q.findAllByQuery(sql,p):
            yield WellWrapper(self, e, **kwargs)
    
    def getWell(self, oid, index=None, eid=None):
        """
        Get filed in the given Well with the specific index
        controlled by the security system, ordered by id.
        If user id not set, owned by the current user.
        
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Well
        @rtype:             WellWrapper
        """

        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        p.map["oid"] = rlong(long(oid))
        sql = "select well from Well as well "\
                "join fetch well.details.creationEvent "\
                "join fetch well.details.owner join fetch well.details.group " \
                "left outer join fetch well.wellSamples as ws " \
                "left outer join fetch ws.image as im "\
                "join fetch im.details.creationEvent "\
                "join fetch im.details.owner join fetch im.details.group " \
                "where well.id = :oid"
        res = q.findByQuery(sql,p)
        if res is None:
            return None
        index = index is None and 0 or index
        kwargs = {'index': index}
        return WellWrapper(self, res, **kwargs)
    
    
    # HIERARCHY RETRIVAL
    #def listContainerHierarchy(self, root, eid=None, gid=None):
    #    """ Retrieves hierarchy trees rooted by a given node - Project, 
    #        for the given user id linked to the objects in the tree,
    #        filter them by parameters."""
    #        
    #    q = self.getContainerService()
    #    if eid is not None: 
    #        p = ParametersI().orphan().exp(long(eid))
    #    elif gid is not None: 
    #        p = ParametersI().orphan().grp(self.getEventContext().groupId)
    #    else: 
    #        p = ParametersI().orphan().exp(self.getEventContext().userId)
    #    for e in q.loadContainerHierarchy(root, None,  p):
    #        if isinstance(e, ProjectI):
    #            yield ProjectWrapper(self, e)
    #        elif isinstance(e, DatasetI):
    #            yield DatasetWrapper(self, e)
    #        elif isinstance(e, ScreenI):
    #            yield ScreenWrapper(self, e)
    #        elif isinstance(e, PlateI):
    #            yield PlateWrapper(self, e)

    def findContainerHierarchies(self, nid):
        """ 
        Retrieves hierarchy trees in various hierarchies that contain the specified Images.
        This method will look for all the containers containing the specified
        Images and then for all containers containing those containers and on up
        the container hierarchy.
        
        @param nid      Contains the ids of the Objects that sit at the bottom of the
                        trees. Not null.
        @type nid       L{Long}
        @return:        Generator yielding Objects with all root nodes that were found.
        @rtype:         L{BlitzObjectWrapper} generator
        """
        """TODO: #1015
        It does not support SPW"""
            
        q = self.getContainerService()
        for e in q.findContainerHierarchies("Project", [long(nid)], None):
            if isinstance(e, ProjectI):
                yield ProjectWrapper(self, e)
            elif isinstance(e, DatasetI):
                yield DatasetWrapper(self, e)
            elif isinstance(e, ScreenI):
                yield ScreenWrapper(self, e)
            elif isinstance(e, PlateI):
                yield PlateWrapper(self, e)
    
    # DATA RETRIVAL BY TAGs
    def getProjectsByTag(self, tids):
        """
        Retrieve projects linked to the given tag IDs
        controlled by the security system, ordered by id.
        
        @param tids:        project IDs
        @type tids:         L{Long}
        @return:            Generator yielding Projects
        @rtype:             L{ProjectWrapper} generator
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["tids"] = rlist([rlong(a) for a in set(tids)])
        sql = "select pr from Project pr join fetch pr.details.creationEvent join fetch pr.details.owner join fetch pr.details.group " \
              "left outer join fetch pr.annotationLinks pal " \
              "left outer join fetch pal.child tag " \
              "where tag.id in (:tids)"
        for e in q.findAllByQuery(sql,p):
            #yield ProjectWrapper(self, e)
            kwargs = {'link': BlitzObjectWrapper(self, e.copyAnnotationLinks()[0])}
            yield ProjectWrapper(self, e, None, **kwargs)
    
    def getDatasetsByTag(self, tids):
        """
        Retrieve datasets linked to the given tag IDs
        controlled by the security system, ordered by id.
        
        @param tids:        dataset IDs
        @type tids:         L{Long}
        @return:            Generator yielding Datasets
        @rtype:             L{DatasetWrapper} generator
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["tids"] = rlist([rlong(a) for a in set(tids)])
        sql = "select ds from Dataset ds join fetch ds.details.creationEvent join fetch ds.details.owner join fetch ds.details.group " \
              "left outer join fetch ds.annotationLinks dal " \
              "left outer join fetch dal.child tag " \
              "where tag.id in (:tids) "
        for e in q.findAllByQuery(sql,p):
            #yield DatasetWrapper(self, e)
            kwargs = {'link': BlitzObjectWrapper(self, e.copyAnnotationLinks()[0])}
            yield DatasetWrapper(self, e, None, **kwargs)
    
    def getImagesByTag(self, tids):
        """
        Retrieve images linked to the given tag IDs
        controlled by the security system, ordered by id.
        
        @param tids:        images IDs
        @type tids:         L{Long}
        @return:            Generator yielding Images
        @rtype:             L{ImageWrapper} generator
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["tids"] = rlist([rlong(a) for a in set(tids)])
        sql = "select im from Image im join fetch im.details.creationEvent join fetch im.details.owner join fetch im.details.group " \
              "left outer join fetch im.annotationLinks ial " \
              "left outer join fetch ial.child tag " \
              "where tag.id in (:tids)"
        for e in q.findAllByQuery(sql,p):
            #yield ImageWrapper(self, e)
            kwargs = {'link': BlitzObjectWrapper(self, e.copyAnnotationLinks()[0])}
            yield ImageWrapper(self, e, None, **kwargs)
                
    def getScreensByTag(self, tids):
        """
        Retrieve screens linked to the given tag IDs
        controlled by the security system, ordered by id.
        
        @param tids:        screen IDs
        @type tids:         L{Long}
        @return:            Generator yielding Screens
        @rtype:             L{ScreenWrapper} generator
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["tids"] = rlist([rlong(a) for a in set(tids)])
        sql = "select sc from Screen sc join fetch sc.details.creationEvent join fetch sc.details.owner join fetch sc.details.group " \
              "left outer join fetch sc.annotationLinks sal " \
              "left outer join fetch sal.child tag " \
              "where tag.id in (:tids)"
        for e in q.findAllByQuery(sql,p):
            #yield ScreenWrapper(self, e)
            kwargs = {'link': BlitzObjectWrapper(self, e.copyAnnotationLinks()[0])}
            yield ScreenWrapper(self, e, None, **kwargs)
    
    def getPlatesByTag(self, tids):
        """
        Retrieve plates linked to the given tag IDs
        controlled by the security system, ordered by id.
        
        @param tids:        plate IDs
        @type tids:         L{Long}
        @return:            Generator yielding Plates
        @rtype:             L{PlateWrapper} generator
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["tids"] = rlist([rlong(a) for a in set(tids)])
        sql = "select pl from Plate pl join fetch pl.details.creationEvent join fetch pl.details.owner join fetch pl.details.group " \
              "left outer join fetch pl.annotationLinks pal " \
              "left outer join fetch pal.child tag " \
              "where tag.id in (:tids) "
        for e in q.findAllByQuery(sql,p):
            #yield PlateWrapper(self, e)
            kwargs = {'link': BlitzObjectWrapper(self, e.copyAnnotationLinks()[0])}
            yield PlateWrapper(self, e, None, **kwargs)
    
    def getTagsByObject(self, o_type, oid):
        """
        Retrieve tags linked to the given Project/Dataset/Image/Screen/Plate/Well ID
        controlled by the security system.
        
        @param o_type:      type of Object
        @type o_type:       String
        @param oid:         Object ID
        @type oid:          Long
        @return:            Generator yielding Tags
        @rtype:             L{TagAnnotationWrapper} generator
        """
        
        q = self.getQueryService()        
        if o_type == "image":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select ial from ImageAnnotationLink as ial where ial.child=a.id and ial.parent.id=:oid and ial.details.owner.id=:uid) " \
                "and a.details.group.id=:gid"
        elif o_type == "dataset":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select dal from DatasetAnnotationLink as dal where dal.child=a.id and dal.parent.id=:oid and dal.details.owner.id=:uid) " \
                "and a.details.group.id=:gid"
        elif o_type == "project":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select pal from ProjectAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid and pal.details.owner.id=:uid) " \
                "and a.details.group.id=:gid"
        elif o_type == "screen":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select sal from ScreenAnnotationLink as sal where sal.child=a.id and sal.parent.id=:oid and sal.details.owner.id=:uid) " \
                "and a.details.group.id=:gid"
        elif o_type == "plate":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select pal from PlateAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid and pal.details.owner.id=:uid) " \
                "and a.details.group.id=:gid"
        elif o_type == "well":
            sql = "select a from TagAnnotation as a " \
                "where not exists ( select wal from WellAnnotationLink as wal where wal.child=a.id and wal.parent.id=:oid and wal.details.owner.id=:uid) " \
                "and a.details.group.id=:gid"
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["gid"] = rlong(self.getEventContext().groupId)
        p.map["uid"] = rlong(self.getEventContext().userId)
        if self.getGroupFromContext().isReadOnly():
            p.map["eid"] = rlong(self.getEventContext().userId)
            sql += " and a.ns is null and a.details.owner.id=:eid"
        for e in q.findAllByQuery(sql,p):
            yield TagAnnotationWrapper(self, e)
    
    #def listComments(self, o_type, oid):
    #    """ Retrieves list of Comments not linked to the for the given Project/Dataset/Image id."""
    #    
    #    q = self.getQueryService()
    #    p = omero.sys.Parameters()
    #    p.map = {}
    #    p.map["oid"] = rlong(long(oid))
    #    p.map["eid"] = rlong(self.getEventContext().userId)
    #    if o_type == "image":
    #        sql = "select a from CommentAnnotation as a " \
    #            "where not exists ( select ial from ImageAnnotationLink as ial where ial.child=a.id and ial.parent.id=:oid ) " \
    #            "and a.details.owner.id=:eid and a.ns is null"
    #    elif o_type == "dataset":
    #        sql = "select a from CommentAnnotation as a " \
    #            "where not exists ( select dal from DatasetAnnotationLink as dal where dal.child=a.id and dal.parent.id=:oid ) " \
    #            "and a.details.owner.id=:eid and a.ns is null"
    #    elif o_type == "project":
    #        sql = "select a from CommentAnnotation as a " \
    #            "where not exists ( select pal from ProjectAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
    #            "and a.details.owner.id=:eid and a.ns is null"
    #    elif o_type == "screen":
    #        sql = "select a from CommentAnnotation as a " \
    #            "where not exists ( select sal from ScreenAnnotationLink as sal where sal.child=a.id and sal.parent.id=:oid ) " \
    #            "and a.details.owner.id=:eid and a.ns is null"
    #    elif o_type == "plate":
    #        sql = "select a from CommentAnnotation as a " \
    #            "where not exists ( select pal from PlateAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
    #            "and a.details.owner.id=:eid and a.ns is null"
    #    elif o_type == "well":
    #        sql = "select a from CommentAnnotation as a " \
    #            "where not exists ( select wal from WellAnnotationLink as wal where wal.child=a.id and wal.parent.id=:oid ) " \
    #            "and a.details.owner.id=:eid and a.ns is null"
    #    for e in q.findAllByQuery(sql,p):
    #        yield CommentAnnotationWrapper(self, e)
    
    def getFilesByObject(self, o_type, oid):
        """
        Retrieve files linked to the given Project/Dataset/Image/Screen/Plate/Well ID
        controlled by the security system.
        
        @param o_type:      type of Object
        @type o_type:       String
        @param oid:         Object ID
        @type oid:          Long
        @return:            Generator yielding Files
        @rtype:             L{FileAnnotationWrapper} generator
        """
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["gid"] = rlong(self.getEventContext().groupId)
        p.map["nss"] = rlist(rstring(omero.constants.namespaces.NSCOMPANIONFILE), rstring(omero.constants.namespaces.NSEXPERIMENTERPHOTO))
        if o_type == "image":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select ial from ImageAnnotationLink as ial where ial.child=a.id and ial.parent.id=:oid ) " \
                "and a.details.group.id=:gid and (a.ns not in (:nss) or a.ns is null) "
        elif o_type == "dataset":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select dal from DatasetAnnotationLink as dal where dal.child=a.id and dal.parent.id=:oid ) " \
                "and a.details.group.id=:gid and (a.ns not in (:nss) or a.ns is null) "
        elif o_type == "project":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select pal from ProjectAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
                "and a.details.group.id=:gid and (a.ns not in (:nss) or a.ns is null) "
        elif o_type == "screen":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select sal from ScreenAnnotationLink as sal where sal.child=a.id and sal.parent.id=:oid ) " \
                "and a.details.group.id=:gid and (a.ns not in (:nss) or a.ns is null) "
        elif o_type == "plate":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select pal from PlateAnnotationLink as pal where pal.child=a.id and pal.parent.id=:oid ) " \
                "and a.details.group.id=:gid and (a.ns not in (:nss) or a.ns is null) "
        elif o_type == "well":
            sql = "select a from FileAnnotation as a join fetch a.file " \
                "where not exists ( select wal from WellAnnotationLink as wal where wal.child=a.id and wal.parent.id=:oid ) " \
                "and a.details.group.id=:gid and (a.ns not in (:nss) or a.ns is null) "
        for e in q.findAllByQuery(sql,p):
            yield FileAnnotationWrapper(self, e)
    
    def getTagsById(self, ids):
        """ Retrieves list of for the given Tag ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from TagAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield TagAnnotationWrapper(self, e)
    
    def getCommentsById(self, ids):
        """ Retrieves list of for the given Comment ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from CommentAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield CommentAnnotationWrapper(self, e)
    
    def getFilesById(self, ids):
        """ Retrieves list of for the given Fiel ids."""
        
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["ids"] = rlist([rlong(a) for a in set(ids)])
        sql = "select a from FileAnnotation a where a.id in (:ids) "
        for e in q.findAllByQuery(sql,p):
            yield FileAnnotationWrapper(self, e)
    
    def listTags(self, eid=None):
        """
        List Tags controlled by the security system,
        ordered by id. If user id not set, owned by the current user.
        This method is used by autocomplite.
        
        @param eid:         experimenter id
        @type eid:          Long
        @return:            Generator yielding Tags
        @rtype:             L{TagAnnotationWrapper} generator
        """

        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["gid"] = rlong(self.getEventContext().groupId)
        
        #sql = "select tg from TagAnnotation tg " \
        #        "where not exists ( select aal from AnnotationAnnotationLink as aal where aal.child=tg.id) and " \
        #        "tg.details.group.id=:gid "
        sql = "select tg from TagAnnotation tg " \
                "where tg.ns is null and " \
                "tg.details.group.id=:gid "
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
            sql += "and tg.details.owner.id=:eid"
        elif self.getGroupFromContext().isReadOnly():
            p.map["eid"] = rlong(self.getEventContext().userId)
            sql += "and tg.details.owner.id=:eid"
        for e in q.findAllByQuery(sql,p):
            yield TagAnnotationWrapper(self, e)
    
    def listFiles(self, eid=None):
        """
        List Files controlled by the security system,
        ordered by id. If user id not set, owned by the current user.
        This method is used by autocomplite.
        
        @param eid:         experimenter id
        @type eid:          Long
        @return:            Generator yielding Files
        @rtype:             L{FileAnnotationWrapper} generator
        """

        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
        else:
            p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select f from FileAnnotation f join fetch f.file where f.details.owner.id=:eid and f.ns is null"
        for e in q.findAllByQuery(sql,p):
            yield FileAnnotationWrapper(self, e)
    
    ##############################################
    ##  Specific Object Getters                 ##
    
    # GETTERs
        
    def getDatasetImageLink (self, parent, oid):
        """
        Get link between then specific Dataset and Image
        controlled by the security system.
        
        @param parent:      Dataset ID
        @type parent:       Long
        @param oid:         Image ID
        @type oid:          Long
        @return:            DatasetImageLink
        @rtype:             BlitzObjectWrapper
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select dsl from DatasetImageLink as dsl left outer join fetch dsl.child as im \
                left outer join fetch dsl.parent as ds where ds.id=:parent and im.id=:oid"
        dsl = query_serv.findByQuery(sql, p)
        return BlitzObjectWrapper(self, dsl)

    def getDatasetImageLinks (self, oid):
        """
        Get links between then specific Image and Datasets with the given Image ID
        controlled by the security system.
        
        @param oid:         Image ID
        @type oid:          Long
        @return:            Generator yielding Dataset-Image links
        @rtype:             L{BlitzObjectWrapper} generator
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select dsl from DatasetImageLink as dsl left outer join fetch dsl.child as im \
                left outer join fetch dsl.parent as ds where im.id=:oid"
        for dsl in query_serv.findAllByQuery(sql, p):
            yield BlitzObjectWrapper(self, dsl)
    
    def getProjectDatasetLink (self, parent, oid):
        """
        Get link between then specific Project and Dataset
        controlled by the security system.
        
        @param parent:      Project ID
        @type parent:       Long
        @param oid:         Dataset ID
        @type oid:          Long
        @return:            ProjectDatasetLink
        @rtype:             BlitzObjectWrapper
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select pdl from ProjectDatasetLink as pdl left outer join fetch pdl.child as ds \
                left outer join fetch pdl.parent as pr where pr.id=:parent and ds.id=:oid"
        pdl = query_serv.findByQuery(sql, p)
        return BlitzObjectWrapper(self, pdl)
    
    def getScreenPlateLink (self, parent, oid):
        """
        Get link between then specific Screen and Plate
        controlled by the security system.
        
        @param parent:      Screen ID
        @type parent:       Long
        @param oid:         Plate ID
        @type oid:          Long
        @return:            ScreenPlateLink
        @rtype:             BlitzObjectWrapper
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        sql = "select spl from ScreenPlateLink as spl left outer join fetch spl.child as pl \
                left outer join fetch spl.parent as sc where sc.id=:parent and pl.id=:oid"
        pdl = query_serv.findByQuery(sql, p)
        return BlitzObjectWrapper(self, pdl)
    
    def getProjectDatasetLinks (self, oid):
        """
        Get links between then specific Dataset and Projects with the given Dataset ID
        controlled by the security system.
        
        @param oid:         Dataset ID
        @type oid:          Long
        @return:            Generator yielding Project-Dataset links
        @rtype:             L{BlitzObjectWrapper} generator
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select pdl from ProjectDatasetLink as pdl left outer join fetch pdl.child as ds \
                left outer join fetch pdl.parent as pr where ds.id=:oid"
        for pdl in query_serv.findAllByQuery(sql, p):
            yield BlitzObjectWrapper(self, pdl)
    
    def getScreenPlateLinks (self, oid):
        """
        Get links between then specific Plate and Screens with the given Plate ID
        controlled by the security system.
        
        @param oid:         Plate ID
        @type oid:          Long
        @return:            Generator yielding Screens-Plate links
        @rtype:             L{BlitzObjectWrapper} generator
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        sql = "select spl from ScreenPlateLink as spl left outer join fetch spl.child as pl \
                left outer join fetch spl.parent as sc where pl.id=:oid"
        for pdl in query_serv.findAllByQuery(sql, p):
            yield BlitzObjectWrapper(self, pdl)
        
    def getImageAnnotationLink (self, parent, oid):
        """
        Get link between then specific Image and Annotation
        controlled by the security system.
        
        @param parent:      Image ID
        @type parent:       Long
        @param oid:         Annotation ID
        @type oid:          Long
        @return:            ImageAnnotationLink
        @rtype:             BlitzObjectWrapper
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select ial from ImageAnnotationLink as ial left outer join fetch ial.child as an \
                left outer join fetch ial.parent as im where im.id=:parent and an.id=:oid and ial.details.owner.id=:eid"
        dsl = query_serv.findByQuery(sql, p)
        if dsl is not None:
            return BlitzObjectWrapper(self, dsl)
        return None
        
    def getDatasetAnnotationLink (self, parent, oid):
        """
        Get link between then specific Dataset and Annotation
        controlled by the security system.
        
        @param parent:      Dataset ID
        @type parent:       Long
        @param oid:         Annotation ID
        @type oid:          Long
        @return:            DatasetAnnotationLink
        @rtype:             BlitzObjectWrapper
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select dal from DatasetAnnotationLink as dal left outer join fetch dal.child as an \
                left outer join fetch dal.parent as ds where ds.id=:parent and an.id=:oid and dal.details.owner.id=:eid"
        dsl = query_serv.findByQuery(sql, p)
        if dsl is not None:
            return BlitzObjectWrapper(self, dsl)
        return None
    
    def getPlateAnnotationLink (self, parent, oid):
        """
        Get link between then specific Plate and Annotation
        controlled by the security system.
        
        @param parent:      Plate ID
        @type parent:       Long
        @param oid:         Annotation ID
        @type oid:          Long
        @return:            PlateAnnotationLink
        @rtype:             BlitzObjectWrapper
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select pal from PlateAnnotationLink as pal left outer join fetch pal.child as an \
                left outer join fetch pal.parent as pl where pl.id=:parent and an.id=:oid and pal.details.owner.id=:eid"
        dsl = query_serv.findByQuery(sql, p)
        if dsl is not None:
            return BlitzObjectWrapper(self, dsl)
        return None
    
    def getProjectAnnotationLink (self, parent, oid):
        """
        Get link between then specific Project and Annotation
        controlled by the security system.
        
        @param parent:      Project ID
        @type parent:       Long
        @param oid:         Annotation ID
        @type oid:          Long
        @return:            ProjectAnnotationLink
        @rtype:             BlitzObjectWrapper
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select pal from ProjectAnnotationLink as pal left outer join fetch pal.child as an \
                left outer join fetch pal.parent as pr where pr.id=:parent and an.id=:oid and pal.details.owner.id=:eid"
        dsl = query_serv.findByQuery(sql, p)
        if dsl is not None:
            return BlitzObjectWrapper(self, dsl)
        return None
    
    def getScreenAnnotationLink (self, parent, oid):
        """
        Get link between then specific Screen and Annotation
        controlled by the security system.
        
        @param parent:      Screen ID
        @type parent:       Long
        @param oid:         Annotation ID
        @type oid:          Long
        @return:            ScreenAnnotationLink
        @rtype:             BlitzObjectWrapper
        """
        
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oid"] = rlong(long(oid))
        p.map["parent"] = rlong(long(parent))
        p.map["eid"] = rlong(self.getEventContext().userId)
        sql = "select sal from ScreenAnnotationLink as sal left outer join fetch sal.child as an \
                left outer join fetch sal.parent as sc where pr.id=:parent and sc.id=:oid and sal.details.owner.id=:eid"
        dsl = query_serv.findByQuery(sql, p)
        if dsl is not None:
            return BlitzObjectWrapper(self, dsl)
        return None
    
    #def getDatasetsWithImages(self, oids):
    #    query_serv = self.getQueryService()
    #    p = omero.sys.Parameters()
    #    p.map = {} 
    #    p.map["ids"] = rlist([rlong(a) for a in oids])
    #    sql = "select ds from Dataset ds join fetch ds.details.owner join fetch ds.details.group " \
    #            "left outer join fetch ds.imageLinks dil left outer join fetch dil.child im " \
    #            "where ds.id in (:ids) order by ds.name"
    #    for e in query_serv.findAllByQuery(sql, p):
    #        yield DatasetWrapper(self, e)
    
    def findTag (self, name, desc=None):
        """ 
        Retrieves Tag by given Name and description
        
        @param name     name of tag
        @type name      String
        @param desc     description of tag
        @type desc      String
        @return:        TagAnnotation
        @rtype:         AnnotationWrapper
        """
        """TODO: #1015
        It does not support SPW"""
        
        query_serv = self.getQueryService()
        res = list()
        p = omero.sys.Parameters()
        p.map = {} 
        p.map["text"] = rstring(str(name))
        if desc is not None:
            p.map["desc"] = rstring(str(desc))
        #p.map["eid"] = rlong(self.getEventContext().userId)
        f = omero.sys.Filter()
        f.limit = rint(1)
        p.theFilter = f
        sql = "select tg from TagAnnotation tg " \
              "where tg.textValue=:text"
        if desc is not None:
            sql+= " and tg.description=:desc"
        sql+=" and tg.ns is null order by tg.textValue"
        res = query_serv.findAllByQuery(sql, p)
        if len(res) > 0:
            return TagAnnotationWrapper(self, res[0])
        return None
    
    # AVATAR #
    def uploadMyUserPhoto(self, filename, format, data):
        """
        Uploads a photo for the user which will be displayed on his/her profile.
        This photo will be saved as an OriginalFile object
        with the given format, and attached to the user's Experimenter
        object via an File Annotation with
        the namespace: "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO).
        If such an OriginalFile instance already exists,
        it will be overwritten. If more than one photo is present, the oldest
        version will be modified (i.e. the highest updateEvent id).
        
        Note: as outlined in ticket:1794, this photo will be placed in the "user"
        group and therefore will be visible to everyone on the system.
        
        @param filename     name which will be used.
        @type filename      String
        @param format       Format.value string. 'image/jpeg' and 'image/png' are common values.
        @type format        String
        @param data         Data from the image. This will be written to disk.
        @type data          String
        
        @return             ID of the overwritten or newly created user photo OriginalFile object.
        @rtype              Long
        """
        
        admin_serv = self.getAdminService()
        pid = admin_serv.uploadMyUserPhoto(filename, format, data)
        if pid is not None:
            return pid
    
    def hasExperimenterPhoto(self, oid=None):
        """
        Check if File annotation with the namespace: 
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO) is linked
        to the given user ID. If user id not set, owned by the current user.
        
        @param oid      experimenter ID
        @type oid       Long
        @return         True or False
        @rtype          Boolean
        """
        
        photo = None
        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations("Experimenter", [self.getEventContext().userId], None, None, None).get(self.getEventContext().userId, [])[0]
            else:
                ann = meta.loadAnnotations("Experimenter", [long(oid)], None, None, None).get(long(oid), [])[0]
            if ann is not None:
                return True
            else:
                return False
        except:
            return False
    
    def getExperimenterPhoto(self, oid=None):
        """
        Get File annotation with the namespace: 
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO) linked
        to the given user ID. If user id not set, owned by the current user.
        
        @param oid      experimenter ID
        @type oid       Long
        @return         Data from the image.
        @rtype          String
        """
        
        photo = None
        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations("Experimenter", [self.getEventContext().userId], None, None, None).get(self.getEventContext().userId, [])
            else:
                ann = meta.loadAnnotations("Experimenter", [long(oid)], None, None, None).get(long(oid), [])
            if len(ann) > 0:
                ann = ann[0]
                store = self.createRawFileStore()
                store.setFileId(ann.file.id.val)
                photo = store.read(0,long(ann.file.size.val))
            else:
                photo = self.getExperimenterDefaultPhoto()
        except:
            logger.error(traceback.format_exc())
            photo = self.getExperimenterDefaultPhoto()
        if photo == None:
            photo = self.getExperimenterDefaultPhoto()        
        return photo
    
    def getExperimenterPhotoSize(self, oid=None):
        """
        Get size of File annotation with the namespace: 
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO) linked
        to the given user ID. If user id not set, owned by the current user.
        
        @param oid      experimenter ID
        @type oid       Long
        @return         Tuple including dimention and size of the file
        @rtype          Tuple
        """
        
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
        """
        Crop File annotation with the namespace: 
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO) linked
        to the given user ID. If user id not set, owned by the current user.
        New dimentions are defined by squer positions box = (x1,y1,x2,y2)
        
        @param box      tuple of new square positions
        @type box       Tuple
        @param oid      experimenter ID
        @type oid       Long
        """
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
        else:
            region = None
            try:
                im = Image.open(StringIO(photo))
                region = im.crop(box)
            except IOError:
                raise IOError("Cannot open that photo.")
            else:
                imdata=StringIO()
                region.save(imdata, format=im.format)
                self.uploadMyUserPhoto(ann.file.name.val, ann.file.mimetype.val, imdata.getvalue())
            
    def getExperimenterDefaultPhoto(self):
        """
        If file annotation with the namespace: 
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO) 
        is not linked to experimenter this method generate default picture of the person.
        
        @return         Data from the image.
        @rtype          String
        """
        
        img = Image.open(settings.DEFAULT_USER)
        img.thumbnail((32,32), Image.ANTIALIAS)
        draw = ImageDraw.Draw(img)
        f = cStringIO.StringIO()
        img.save(f, "PNG")
        f.seek(0)
        return f.read()
    
    def getFileFormat(self, format):
        """
        Get file annotation format for the given value.
        
        @return         Omero File format
        @rtype          String
        """
        query_serv = self.getQueryService()
        return query_serv.findByString("Format", "value", format).getValue().val;
    
    ################################################
    ##   Counters
    
    def getCollectionCount(self, parent, child, ids):
        """
        Counts the number of members in a collection for a given object.
        
        @param parent       The fully-qualified classname of the object to be tested
        @type parent        String
        @param child        Name of the property on that class, omitting getters and setters.
        @type child         String
        @param ids          Set of Longs, the ids of the objects to test
        @type ids           L{Long}
        @return             A map from id integer to count integer
        @rtype              L{(Long, Long)}
        """
        container = self.getContainerService()
        return container.getCollectionCount(parent, child, ids, None)

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
        if email == "":
            return False
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
    
    def defaultThumbnail(self, size=(120,120)):
        if isinstance(size, int):
            size = (size,size)
        img = Image.open(settings.DEFAULT_IMG)
        img.thumbnail(size, Image.ANTIALIAS)
        draw = ImageDraw.Draw(img)
        f = cStringIO.StringIO()
        img.save(f, "PNG")
        f.seek(0)
        return f.read()
    
    ##############################################
    ##   Sets methods                           ##
    
    def changeUserPassword(self, omeName, password):
        """
        Change the password for the a given user.
        
        @param omeName      Experimetner omename
        @type omeName       String
        @param password     Must pass validation in the security sub-system.
        @type password      String
        """
        admin_serv = self.getAdminService()
        admin_serv.changeUserPassword(omeName, rstring(str(password)))
        
    def changeMyPassword(self, old_password, password):
        """
        Change the password for the current user by passing the old password.
        
        @param old_password     Old password
        @type old_password      String
        @param password         Must pass validation in the security sub-system.
        @type password          String
        @return                 None or error message if password could not be changed
        @rtype                  String
        """
        admin_serv = self.getAdminService() 
        try:
            admin_serv.changePasswordWithOldPassword(rstring(str(old_password)), rstring(str(password)))
        except omero.SecurityViolation, x:
            return x.message
        return None

    def createExperimenter(self, experimenter, defaultGroup, otherGroups, password):
        """
        Create and return a new user in the given groups with password.
        
        @param experimenter     A new Experimenter instance.
        @type experimenter      ExperimenterI
        @param defaultGroup     Instance of ExperimenterGroup selected as a first active group.
        @type defaultGroup      ExperimenterGroupI
        @param otherGroups      List of ExperimenterGroup instances. Can be empty.
        @type otherGroups       L{ExperimenterGroupI}
        @param password         Must pass validation in the security sub-system.
        @type password          String
        @return                 ID of the newly created Experimenter Not null.
        @rtype                  Long
        """
        admin_serv = self.getAdminService()
        return admin_serv.createExperimenterWithPassword(experimenter, rstring(str(password)), defaultGroup, otherGroups)
    
    def updateExperimenter(self, experimenter, defaultGroup, addGroups, rmGroups):
        """
        Update an existing user including groups user is a member of.
        Password cannot be changed by calling that method.
        
        @param experimenter     An existing Experimenter instance.
        @type experimenter      ExperimenterI
        @param defaultGroup     Instance of ExperimenterGroup selected as a new active group.
        @type defaultGroup      ExperimenterGroupI
        @param addGroups        List of new ExperimenterGroup instances user will be a member of. Can be empty.
        @type addGroups         L{ExperimenterGroupI}
        @param rmGroups         List of old ExperimenterGroup instances user no longer be a member of. Can be empty.
        @type rmGroups          L{ExperimenterGroupI}
        """
        
        admin_serv = self.getAdminService()
        admin_serv.updateExperimenter(experimenter)
        if len(addGroups) > 0:
            admin_serv.addGroups(experimenter, addGroups)
        admin_serv.setDefaultGroup(experimenter, defaultGroup)
        if len(rmGroups) > 0:
            admin_serv.removeGroups(experimenter, rmGroups)
    
    def setMembersOfGroup(self, group, add_exps, rm_exps):
        """
        Change members of the group.
        
        @param group            An existing ExperimenterGroup instance.
        @type group             ExperimenterGroupI
        @param add_exps         List of new Experimenters instances. Can be empty.
        @type add_exps          L{ExperimenterI}
        @param rm_exps          List of old Experimenters instances no longer be a member of that group. Can be empty.
        @type rm_exps           L{ExperimenterI}
        """
        
        admin_serv = self.getAdminService()
        for e in add_exps:
            admin_serv.addGroups(e, [group])
        for e in rm_exps:
            admin_serv.removeGroups(e, [group])
    
    #def deleteExperimenter(self, experimenter):
    #    """
    #    Removes a user by removing the password information for that user as well
    #    as all GroupExperimenterMap instances.
    #    
    #    @param user     Experimenter to be deleted. Not null.
    #    @type user      ExperimenterI
    #    """
    #    admin_serv = self.getAdminService()
    #    admin_serv.deleteExperimenter(experimenter)
    
    def createGroup(self, group, group_owners):
        """
        Create and return a new group with the given owners.
        
        @param group            A new ExperimenterGroup instance.
        @type group             ExperimenterGroupI
        @param group_owners     List of Experimenter instances. Can be empty.
        @type group_owners      L{ExperimenterI}
        @return                 ID of the newly created ExperimenterGroup Not null.
        @rtype                  Long
        """
        
        admin_serv = self.getAdminService()
        gr_id = admin_serv.createGroup(group)
        new_gr = admin_serv.getGroup(gr_id)
        admin_serv.addGroupOwners(new_gr, group_owners)
        return gr_id
    
    def updateGroup(self, group, add_exps, rm_exps, perm=None):
        """
        Update an existing user including groups user is a member of.
        Password cannot be changed by calling that method.
        
        @param group            An existing ExperimenterGroup instance.
        @type group             ExperimenterGroupI
        @param add_exps         List of new Experimenter instances. Can be empty.
        @type add_exps          L{ExperimenterI}
        @param rm_exps          List of old Experimenter instances who no longer will be a member of. Can be empty.
        @type rm_exps           L{ExperimenterI}
        @param perm             Permissions set on the given group
        @type perm              PermissionsI
        """
        
        admin_serv = self.getAdminService()
        # Should we update updateGroup so this would be atomic?
        admin_serv.updateGroup(group)
        if perm is not None:
            logger.warning("WARNING: changePermissions was called!!!")
            admin_serv.changePermissions(group, perm)
        self._user = self.getExperimenter(self._userid)
        admin_serv.addGroupOwners(group, add_exps)
        admin_serv.removeGroupOwners(group, rm_exps)
    
    def updateMyAccount(self, experimenter, defultGroup):
        """
        Allows a user to update his/her own information and set the default group for a given user.
        @param experimenter     A data transfer object. Only the fields: firstName, middleName, 
                                lastName, email, and institution are checked. Not null.
        @type experimenter      ExperimenterI
        @param defultGroup      The group which should be set as default group for this user. Not null
        @type defultGroup       ExperimenterGroupI
        """
        admin_serv = self.getAdminService()
        admin_serv.updateSelf(experimenter)
        admin_serv.setDefaultGroup(experimenter, defultGroup)
        self.changeActiveGroup(defultGroup.id.val)
        self._user = self.getExperimenter(self._userid)
    
    def updatePermissions(self, obj, perm):
        """
        Allow to change the permission on the object.
        
        @param obj      An entity or an unloaded reference to an entity. Not null.
        @type obj       ObjectI
        @param perm     The permissions value for this entity. Not null.
        @type perm      PermissionsI
        """
        admin_serv = self.getAdminService()
        if perm is not None:
            logger.warning("WARNING: changePermissions was called!!!")
            admin_serv.changePermissions(obj, perm)
            self._user = self.getExperimenter(self._userid)
    
    def saveObject (self, obj):
        """
        Provide method for directly updating object graphs. Act recursively on 
        the entire object graph, replacing placeholders and details where necessary, 
        and then "merging" the final graph. This means that the objects that are 
        passed into methods are copied over to new instances which are then returned. 
        The original objects should be discarded.
        
        @param obj      An entity or an unloaded reference to an entity. Not null.
        @type obj       ObjectI
        """
        u = self.getUpdateService()
        u.saveObject(obj)
    
    def saveArray (self, objs):
        """
        Provide method for directly updating list of object graphs. Act recursively on 
        the entire object graph, replacing placeholders and details where necessary, 
        and then "merging" the final graph. This means that the objects that are 
        passed into methods are copied over to new instances which are then returned. 
        The original objects should be discarded.
        
        @param obj      List of entities or an unloaded references to an entity. Not null.
        @type obj       L{ObjectI}
        """
        u = self.getUpdateService()
        u.saveArray(objs)
    
    def saveAndReturnObject (self, obj):
        """
        Provide method for directly updating object graphs and return it. Act recursively on 
        the entire object graph, replacing placeholders and details where necessary, 
        and then "merging" the final graph. This means that the objects that are 
        passed into methods are copied over to new instances which are then returned. 
        The original objects should be discarded.
        
        @param obj      An entity or an unloaded reference to an entity. Not null.
        @type obj       ObjectI
        @return         Saved object
        @rtype          ObjectI
        """
        u = self.getUpdateService()
        res = u.saveAndReturnObject(obj)
        res.unload()
        obj = BlitzObjectWrapper(self, res)
        return obj
    
    def saveAndReturnId (self, obj):
        """
        Provide method for directly updating object graphs and return ID. Act recursively on 
        the entire object graph, replacing placeholders and details where necessary, 
        and then "merging" the final graph. This means that the objects that are 
        passed into methods are copied over to new instances which are then returned. 
        The original objects should be discarded.

        @param obj      An entity or an unloaded reference to an entity. Not null.
        @type obj       ObjectI
        @return         ID of saved object
        @rtype          Long
        """
        u = self.getUpdateService()
        res = u.saveAndReturnObject(obj)
        res.unload()
        return res.id.val
    
    def saveAndReturnFile(self, binary, oFile_id):
        """
        Provide method for directly updating a file object and return binary.

        @param binary       Binary. Not null.
        @type binary        String
        @param oFile_id     File Id in order to manage the state of the service. Not null.
        @type oFile_id      Long
        @return             Shallow copy of file.
        """
        
        store = self.createRawFileStore()
        store.setFileId(oFile_id);
        pos = 0
        rlen = 0
        
        for chunk in binary.chunks():
            rlen = len(chunk)
            store.write(chunk, pos, rlen)
            pos = pos + rlen
        return store.save()

    def prepareRecipients(self, recipients):
        recps = list()
        for m in recipients:
            try:
                e = (m.email, m.email.val)[isinstance(m.email, omero.RString)]
                if e is not None and e!="":
                    recps.append(e)
            except:
                logger.error(traceback.format_exc())
        logger.info(recps)
        if len(recps) == 0:
            raise AttributeError("Recipients list is empty")
        return recps
        
    def addComment(self, host, blitz_id, share_id, comment):
        sh = self.getShareService()
        new_cm = sh.addComment(long(share_id), str(comment))
        
        members = list(self.getAllMembers(long(share_id)))
        sh = self.getShare(long(share_id))
        if self.getEventContext().userId != sh.owner.id.val:
            members.append(sh.getOwner())
        
        if sh.active:
            try:
                for m in members:
                    try:
                        if m.id == self.getEventContext().userId:
                            members.remove(m)
                    except:
                        logger.error(traceback.format_exc())
                recipients = self.prepareRecipients(members)
            except Exception, x:
                logger.error(traceback.format_exc())
            else:
                blitz = settings.SERVER_LIST.get(pk=blitz_id)
                t = settings.EMAIL_TEMPLATES["add_comment_to_share"]
                message = t['text_content'] % (settings.APPLICATION_HOST, blitz_id)
                message_html = t['html_content'] % (settings.APPLICATION_HOST, blitz_id, settings.APPLICATION_HOST, blitz_id)
                try:
                    title = 'OMERO.web - new comment for share %i' % share_id
                    text_content = message
                    html_content = message_html
                    msg = EmailMultiAlternatives(title, text_content, settings.SERVER_EMAIL, recipients)
                    msg.attach_alternative(html_content, "text/html")
                    msg.send()
                    logger.error("Email was sent")
                except:
                    logger.error(traceback.format_exc())
                
    def removeImage(self, share_id, image_id):
        sh = self.getShareService()
        img = self.getImage(image_id)
        sh.removeObject(long(share_id), img._obj)
            
    def createShare(self, host, blitz_id, image, message, members, enable, expiration=None):
        sh = self.getShareService()
        q = self.getQueryService()
        
        items = list()
        ms = list()
        p = omero.sys.Parameters()
        p.map = {} 
        #images
        if len(image) > 0:
            p.map["ids"] = rlist([rlong(long(a)) for a in image])
            sql = "select im from Image im join fetch im.details.owner join fetch im.details.group where im.id in (:ids) order by im.name"
            items.extend(q.findAllByQuery(sql, p))
        
        #members
        if members is not None:
            p.map["ids"] = rlist([rlong(long(a)) for a in members])
            sql = "select e from Experimenter e " \
                  "where e.id in (:ids) order by e.omeName"
            ms = q.findAllByQuery(sql, p)
        sid = sh.createShare(message, expiration, items, ms, [], enable)
        sh.addObjects(sid, items)
        
        #send email if avtive
        if enable:
            try:
                recipients = self.prepareRecipients(ms)
            except Exception, x:
                logger.error(traceback.format_exc())
            else:
                t = settings.EMAIL_TEMPLATES["create_share"]
                message = t['text_content'] % (settings.APPLICATION_HOST, blitz_id, self.getUser().getFullName())
                message_html = t['html_content'] % (settings.APPLICATION_HOST, blitz_id, settings.APPLICATION_HOST, blitz_id, self.getUser().getFullName())
                
                try:
                    title = 'OMERO.web - new share %i' % sid
                    text_content = message
                    html_content = message_html
                    msg = EmailMultiAlternatives(title, text_content, settings.SERVER_EMAIL, recipients)
                    msg.attach_alternative(html_content, "text/html")
                    msg.send()
                    logger.error("Email was sent")
                except:
                    logger.error(traceback.format_exc())                
    
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
        if len(add_members) > 0:
            try:
                recipients = self.prepareRecipients(add_members)
            except Exception, x:
                logger.error(traceback.format_exc())
            else:
                blitz = settings.SERVER_LIST.get(pk=blitz_id)
                t = settings.EMAIL_TEMPLATES["add_member_to_share"]
                message = t['text_content'] % (settings.APPLICATION_HOST, blitz_id, self.getUser().getFullName())
                message_html = t['html_content'] % (settings.APPLICATION_HOST, blitz_id, settings.APPLICATION_HOST, blitz_id, self.getUser().getFullName())
                try:
                    title = 'OMERO.web - update share %i' % share_id
                    text_content = message
                    html_content = message_html
                    msg = EmailMultiAlternatives(title, text_content, settings.SERVER_EMAIL, recipients)
                    msg.attach_alternative(html_content, "text/html")
                    msg.send()
                    logger.error("Email was sent")
                except:
                    logger.error(traceback.format_exc())
			
        if len(rm_members) > 0:
            try:
                recipients = self.prepareRecipients(rm_members)
            except Exception, x:
                logger.error(traceback.format_exc())
            else:
                blitz = settings.SERVER_LIST.get(pk=blitz_id)
                t = settings.EMAIL_TEMPLATES["remove_member_from_share"]
                message = t['text_content'] % (settings.APPLICATION_HOST, blitz_id)
                message_html = t['html_content'] % (settings.APPLICATION_HOST, blitz_id, settings.APPLICATION_HOST, blitz_id)
                
                try:
                    title = 'OMERO.web - update share %i' % share_id
                    text_content = message
                    html_content = message_html
                    msg = EmailMultiAlternatives(title, text_content, settings.SERVER_EMAIL, recipients)
                    msg.attach_alternative(html_content, "text/html")
                    msg.send()
                    logger.error("Email was sent")
                except:
                    logger.error(traceback.format_exc())
    

    ##############################################
    ##  History methods                        ##
    
    #def getLastAcquiredImages (self):
    #    tm = self.getTimelineService()
    #    p = omero.sys.Parameters()
    #    p.map = {}
    #    f = omero.sys.Filter()
    #    f.ownerId = rlong(self.getEventContext().userId)
    #    f.groupId = rlong(self.getEventContext().groupId)
    #    f.limit = rint(6)
    #    p.theFilter = f
    #    for e in tm.getMostRecentObjects(['Image'], p, False)["Image"]:
    #        yield ImageWrapper(self, e)
    
    def listLastImportedImages (self):
        """
        Retrieve most recent imported images 
        controlled by the security system.
        
        @return:            Generator yielding Images
        @rtype:             L{ImageWrapper} generator
        """
                
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentObjects(['Image'], p, False)["Image"]:
            yield ImageWrapper(self, e)
    
    def listMostRecentShares (self):
        """
        Retrieve most recent shares 
        controlled by the security system.
        
        @return:    Generator yielding SessionAnnotationLink
        @rtype:     L{SessionAnnotationLinkWrapper} generator
        """
        
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentShareCommentLinks(p):
            yield SessionAnnotationLinkWrapper(self, e)
    
    def listMostRecentShareCommentLinks (self):
        """
        Retrieve most recent share comments 
        controlled by the security system.
        
        @return:    Generator yielding SessionAnnotationLink
        @rtype:     L{SessionAnnotationLinkWrapper} generator
        """
        
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentShareCommentLinks(p):
            yield SessionAnnotationLinkWrapper(self, e)
    
    def listMostRecentComments (self):
        """
        Retrieve most recent comment annotations 
        controlled by the security system.
        
        @return:    Generator yielding BlitzObjectWrapper
        @rtype:     L{BlitzObjectWrapper} generator
        """
        
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentAnnotationLinks(None, ['CommentAnnotation'], None, p):
            yield BlitzObjectWrapper(self, e)
    
    def listMostRecentTags (self):
        """
        Retrieve most recent tag annotations 
        controlled by the security system.
        
        @return:    Generator yielding BlitzObjectWrapper
        @rtype:     L{BlitzObjectWrapper} generator
        """
        
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        #f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(200)
        p.theFilter = f
        for e in tm.getMostRecentAnnotationLinks(None, ['TagAnnotation'], None, p):
            yield BlitzObjectWrapper(self, e.child)
    
    def getDataByPeriod (self, start, end, otype=None, page=None):
        """
        Retrieve given data objects by the given period of time 
        controlled by the security system.
        
        @param start        Starting data
        @type start         Long
        @param end          Finishing data
        @type end           Long
        @param otype        Data type: Project, Dataset, Image
        @type otype         String
        @return:            Map of project, dataset and image lists
        @rtype:             Map
        """
        
        if not otype.lower() in ('project', 'dataset', 'image'):
            raise AttributeError('It only retrieves: Project, Dataset or Image')
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        if page is not None:
            f.limit = rint(PAGE)
            f.offset = rint((int(page)-1)*PAGE)
        else:
            f.limit = rint(100)
        p.theFilter = f
        im_list = list()
        ds_list = list()
        pr_list = list()
        
        if otype == 'image':
            try:
                for e in tm.getByPeriod(['Image'], rtime(long(start)), rtime(long(end)), p, True)['Image']:
                    im_list.append(ImageWrapper(self, e))
            except:
                pass
        elif otype == 'dataset':
            try:
                for e in tm.getByPeriod(['Dataset'], rtime(long(start)), rtime(long(end)), p, True)['Dataset']:
                    ds_list.append(DatasetWrapper(self, e))
            except:
                pass
        elif otype == 'project':
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
    
    def countDataByPeriod (self, start, end, otype=None):
        """
        Counts given data objects by the given period of time 
        controlled by the security system.
        
        @param start        Starting data
        @type start         Long
        @param end          Finishing data
        @type end           Long
        @param otype        Data type: Project, Dataset, Image
        @type otype         String
        @return:            Counter
        @rtype:             Long
        """
        
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        p.theFilter = f
        if otype == 'image':
            return tm.countByPeriod(['Image'], rtime(long(start)), rtime(long(end)), p)['Image']
        elif otype == 'dataset':
            return tm.countByPeriod(['Dataset'], rtime(long(start)), rtime(long(end)), p)['Dataset']
        elif otype == 'project':
            return tm.countByPeriod(['Project'], rtime(long(start)), rtime(long(end)), p)['Project']
        else:
            c = tm.countByPeriod(['Image', 'Dataset', 'Project'], rtime(long(start)), rtime(long(end)), p)
            return c['Image']+c['Dataset']+c['Project']

    def getEventsByPeriod (self, start, end):
        """
        Retrieve event log objects by the given period of time 
        controlled by the security system.
        
        @param start        Starting data
        @type start         Long
        @param end          Finishing data
        @type end           Long
        @return:            List of event logs
        @rtype:             List
        """
        
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

omero.gateway.BlitzGateway = OmeroWebGateway

class OmeroWebObjectWrapper (object):
    
    def listChildrenWithLinks (self):
        """
        Lists available child objects.

        @return     Generator yielding child objects and link to parent.
        """

        childw = self._getChildWrapper()
        klass = childw().OMERO_CLASS
        params = omero.sys.Parameters()
        params.map = {}
        params.map["dsid"] = rlong(self._oid)
        query = "select c from %s as c " \
                "join fetch c.child as ch " \
                "where c.parent.id=:dsid " \
                "order by c.child.name" % self.LINK_CLASS
        for link in self._conn.getQueryService().findAllByQuery(query, params):
            kwargs = {'link': BlitzObjectWrapper(self._conn, link)}
            yield childw(self._conn, link.child, None, **kwargs)
    
    def countAnnotations (self):
        """
        Count on annotations linked to the object and set the value
        on the custom fiels 'annotation_counter'.

        @return     Counter
        """
        
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
    
    def warpName(self):
        """
        Warp name of the object if names is longer then 30 characters.

        @return     Warped string.
        """
        
        try: 
            l = len(self.name) 
            if l < 30: 
                return self.name 
            elif l >= 30: 
                splited = [] 
                for v in range(0,len(self.name),30): 
                    splited.append(self.name[v:v+30]+"\n") 
                return "".join(splited) 
        except: 
            logger.info(traceback.format_exc()) 
            return self.name

class AnnotationLinkWrapper (omero.gateway.BlitzObjectWrapper):
    """
    omero_model_AnnotationLinkI class wrapper extends omero.gateway.BlitzObjectWrapper.
    """
    
    def getAnnotation(self):
        return AnnotationWrapper(self, self.child)

class ProjectWrapper (OmeroWebObjectWrapper, omero.gateway.ProjectWrapper): 
    """
    omero_model_ProjectI class wrapper overwrite omero.gateway.ProjectWrapper
    and extend OmeroWebObjectWrapper.
    """

    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
 	 
omero.gateway.ProjectWrapper = ProjectWrapper 
 	 
class DatasetWrapper (OmeroWebObjectWrapper, omero.gateway.DatasetWrapper): 
    """
    omero_model_DatasetI class wrapper overwrite omero.gateway.DatasetWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None    
	 
omero.gateway.DatasetWrapper = DatasetWrapper

class ImageWrapper (OmeroWebObjectWrapper, omero.gateway.ImageWrapper):
    """
    omero_model_ImageI class wrapper overwrite omero.gateway.ImageWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None
    
    def getThumbnailOrDefault (self, size=(120,120)):
        rv = super(ImageWrapper, self).getThumbnail(size=size)
        if rv is None:
            try:
                rv = self._conn.defaultThumbnail(size)
            except Exception, e:
                logger.info(traceback.format_exc())
                raise e
        return rv

omero.gateway.ImageWrapper = ImageWrapper

class PlateWrapper (OmeroWebObjectWrapper, omero.gateway.PlateWrapper):
    
    """
    omero_model_PlateI class wrapper overwrite omero.gateway.PlateWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    annotation_counter = None

    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None

omero.gateway.PlateWrapper = PlateWrapper

class ScreenWrapper (OmeroWebObjectWrapper, omero.gateway.ScreenWrapper):
    """
    omero_model_ScreenI class wrapper overwrite omero.gateway.ScreenWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    annotation_counter = None

    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']

omero.gateway.ScreenWrapper = ScreenWrapper

class ShareWrapper (OmeroWebObjectWrapper, omero.gateway.ShareWrapper):
    """
    omero_model_ShareI class wrapper overwrite omero.gateway.ShareWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    def truncateMessageForTree(self):
        try:
            msg = self.getMessage().val
            l = len(msg)
            if l < 38:
                return msg
            return "..." + msg[l - 35:]
        except:
            logger.info(traceback.format_exc())
            return self.getMessage().val
    
    def getShareType(self):
        if self.itemCount == 0:
            return "Discussion"
        else:
            return "Share"
    
    def isEmpty(self):
        if self.itemCount == 0:
            return True
        return False
    
    def getExpireDate(self):
        #workaround for problem of year 2038
        try:
            d = self.started+self.timeToLive
            if d > 2051222400:
                return datetime(2035, 1, 1, 0, 0, 0)            
            return datetime.fromtimestamp(d / 1000)
        except:
            logger.info(traceback.format_exc())
        return None
    
    def isExpired(self):
        #workaround for problem of year 2038
        now = time.time()
        try:
            d = long(self.started+self.timeToLive)
            if (d / 1000) > now:
                return False
            return True
        except:
            logger.info(traceback.format_exc())
        return None
    
omero.gateway.ShareWrapper = ShareWrapper

class SessionAnnotationLinkWrapper (omero.gateway.BlitzObjectWrapper):
    """
    omero_model_AnnotationLinkI class wrapper extends omero.gateway.BlitzObjectWrapper.
    """
    
    def getComment(self):
        return ShareCommentWrapper(self._conn, self.child)
    
    def getShare(self):
        return ShareWrapper(self._conn, self.parent)
    
class EventLogWrapper (omero.gateway.BlitzObjectWrapper):
    """
    omero_model_EventLogI class wrapper extends omero.gateway.BlitzObjectWrapper.
    """
    
    LINK_CLASS = "EventLog"

