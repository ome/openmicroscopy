#!/usr/bin/env python
# 
# webclient_gateway
# 
# Copyright (c) 2008-2011 University of Dundee.
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

logger = logging.getLogger('webclient_gateway')

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

from omero.rtypes import *
from omero.model import FileAnnotationI, TagAnnotationI, \
                        DatasetI, ProjectI, ImageI, ScreenI, PlateI, \
                        DetectorI, FilterI, ObjectiveI, InstrumentI, \
                        LaserI

from omero.gateway import TagAnnotationWrapper, ExperimenterWrapper, \
                ExperimenterGroupWrapper, WellWrapper, AnnotationWrapper, \
                OmeroGatewaySafeCallWrapper

from omero.sys import ParametersI

from django.utils.encoding import smart_str
from django.utils.translation import ugettext as _
from django.conf import settings
from django.core.mail import send_mail
from django.core.mail import EmailMultiAlternatives

from omeroweb.webadmin.custom_models import Server

try:
    PAGE = settings.PAGE
except:
    PAGE = 200

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
            self.getAdminService().setDefaultGroup(self.getUser()._obj, omero.model.ExperimenterGroupI(gid, False))
            self._ctx = self.getAdminService().getEventContext()
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

    def getExperimenters(self):
        """
        Return all experimenters apart from current user.

        @return:        Generator yielding experimetners list
        @rtype:         L{ExperimenterWrapper} generator

        """

        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["id"] = rlong(self.getEventContext().userId)
        sql = "select e from Experimenter as e where e.id != :id "
        for e in q.findAllByQuery(sql, p):
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
    
    
    ####################################################################################
    ##   Container Queries                                                           ###
    ####################################################################################

    def listTags(self, eid=None):
        params = omero.sys.ParametersI()
        params.orphan()
        params.map = {}
        params.map['ns'] = rstring(omero.constants.metadata.NSINSIGHTTAGSET)
        
        sql = "select tg from TagAnnotation tg where ((ns=:ns) or (ns is null and not exists ( select aal from AnnotationAnnotationLink as aal where aal.child=tg.id))) "
        if eid is not None:
            params.map["eid"] = rlong(long(eid))
            sql+=" and tg.details.owner.id = :eid"
            
        q = self.getQueryService()
        for ann in q.findAllByQuery(sql, params):
            yield TagAnnotationWrapper(self, ann)
    
    def countOrphans (self, obj_type, eid=None):
        links = {'Dataset':('ProjectDatasetLink', DatasetWrapper), 
                'Image':('DatasetImageLink', ImageWrapper),
                'Plate':('ScreenPlateLink', PlateWrapper)}
        
        if obj_type not in links:
            raise TypeError("'%s' is not valid object type. Must use one of %s" % (obj_type, links.keys()) )
            
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        
        links = {'Dataset':('ProjectDatasetLink', DatasetWrapper), 
                'Image':('DatasetImageLink', ImageWrapper),
                'Plate':('ScreenPlateLink', PlateWrapper)}
        
        if obj_type not in links:
            raise TypeError("'%s' is not valid object type. Must use one of %s" % (obj_type, links.keys()) )
            
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
            eidFilter = "obj.details.owner.id=:eid and " 
            eidWsFilter = " and ws.details.owner.id=:eid"
        else:
            eidFilter = ""
            eidWsFilter = ""
        
        sql = "select count(obj.id) from %s as obj " \
                "join obj.details.creationEvent "\
                "join obj.details.owner join obj.details.group " \
                "where %s" \
                "not exists (select obl from %s as obl where " \
                "obl.child=obj.id)" % (obj_type, eidFilter, links[obj_type][0])
        if obj_type == 'Image':
            sql += "and not exists ( "\
                "select ws from WellSample as ws "\
                "where ws.image=obj.id %s)" % eidWsFilter
        
        rslt = q.projection(sql, p)
        if len(rslt) > 0:
            if len(rslt[0]) > 0:
                return rslt[0][0].val
        return 0
            
    
    def listOrphans (self, obj_type, eid=None, page=None):
        """
        List orphaned Datasets, Images, Plates controlled by the security system, 
        Optionally filter by experimenter 'eid'
        
        @param obj_type:    'Dataset', 'Image', 'Plate'
        @param eid:         experimenter id
        @type eid:          Long
        @param page:        page number
        @type page:         Long
        @return:            Generator yielding Datasets
        @rtype:             L{DatasetWrapper} generator
        """
                
        links = {'Dataset':('ProjectDatasetLink', DatasetWrapper), 
                'Image':('DatasetImageLink', ImageWrapper),
                'Plate':('ScreenPlateLink', PlateWrapper)}
        
        if obj_type not in links:
            raise TypeError("'%s' is not valid object type. Must use one of %s" % (obj_type, links.keys()) )
            
        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
            eidFilter = "obj.details.owner.id=:eid and " 
            eidWsFilter = " and ws.details.owner.id=:eid"
        else:
            eidFilter = ""
            eidWsFilter = ""
        
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(PAGE)
            f.offset = rint((int(page)-1)*PAGE)
            p.theFilter = f
        
        sql = "select obj from %s as obj " \
                "join fetch obj.details.creationEvent "\
                "join fetch obj.details.owner join fetch obj.details.group " % (obj_type)
        
        sql += "where %s" \
                "not exists (select obl from %s as obl where " \
                "obl.child=obj.id)" % (eidFilter, links[obj_type][0])
        
        if obj_type == 'Image':
            sql += "and not exists ( "\
                "select ws from WellSample as ws "\
                "where ws.image=obj.id %s)" % eidWsFilter
        for e in q.findAllByQuery(sql, p):
            yield links[obj_type][1](self, e)
    
    def listImagesInDataset (self, oid, eid=None, page=None):
        """
        List Images in the given Dataset.
        Optinally filter by experimenter 'eid'
        
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
                "where d.id = :oid"
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
            sql += " and im.details.owner.id=:eid"
        sql+=" order by im.name ASC"
        
        for e in q.findAllByQuery(sql, p):
            kwargs = {'link': omero.gateway.BlitzObjectWrapper(self, e.copyDatasetLinks()[0])}
            yield ImageWrapper(self, e, None, **kwargs)
    
    # DATA RETRIVAL BY TAGs
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
            except:
                logger.error(traceback.format_exc())
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
            logger.error(traceback.format_exc())
            raise IOError("Photo does not exist.")
        else:
            region = None
            try:
                im = Image.open(StringIO(photo))
                region = im.crop(box)
            except IOError:
                logger.error(traceback.format_exc())
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
        p.map["omeName"] = rstring(smart_str(ome_name))
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
        p.map["name"] = rstring(smart_str(name))
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
        p.map["email"] = rstring(smart_str(email))
        sql = "select e from Experimenter as e where e.email = (:email)"
        exps = query_serv.findAllByQuery(sql, p)
        if len(exps) > 0:
            return True
        else:
            return False
    
    def defaultThumbnail(self, size=(120,120)):
        if isinstance(size, int):
            size = (size,size)
        if len(size) == 1:
            size = (size[0],size[0])
        img = Image.open(settings.DEFAULT_IMG)
        img.thumbnail(size, Image.ANTIALIAS)
        draw = ImageDraw.Draw(img)
        f = cStringIO.StringIO()
        img.save(f, "PNG")
        f.seek(0)
        return f.read()
    
    ##############################################
    ##   Sets methods                           ##
    
    def changeUserPassword(self, omeName, password, my_password):
        """
        Change the password for the a given user.
        
        @param omeName      Experimetner omename
        @type omeName       String
        @param password     Must pass validation in the security sub-system.
        @type password      String
        @param my_password  Must pass validation in the security sub-system.
        @type my_password   String
        """
        admin_serv = self.getAdminService()
        self.c.sf.setSecurityPassword(my_password)
        admin_serv.changeUserPassword(omeName, rstring(str(password)))
        
    def changeMyPassword(self, password, old_password):
        """
        Change the password for the current user by passing the old password.
        
        @param password         Must pass validation in the security sub-system.
        @type password          String
        @param old_password     Old password
        @type old_password      String
        @return                 None or error message if password could not be changed
        @rtype                  String
        """
        admin_serv = self.getAdminService() 
        admin_serv.changePasswordWithOldPassword(rstring(str(old_password)), rstring(str(password)))
        
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
        self._user = self.getObject("Experimenter", self._userid)
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
        self._user = self.getObject("Experimenter", self._userid)
    
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
            self._user = self.getObject("Experimenter", self._userid)
    
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
        obj = omero.gateway.BlitzObjectWrapper(self, res)
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
    
    ##############################################
    ##   IShare
    
    def getShare (self, oid):
           """
           Gets share for the given share id.

           @param oid:     Share ID.
           @type oid:      Long
           @return:        ShareWrapper or None
           @rtype:         L{ShareWrapper}
           """

           sh_serv = self.getShareService()
           sh = sh_serv.getShare(long(oid))
           if sh is not None:
               return ShareWrapper(self, sh)
           else:
               return None
    
    def getOwnShares(self):
        """
        Gets all owned shares for the current user.
        
        @return:    Shares that user owns
        @rtype:     L{ShareWrapper} generator
        """
        
        sh = self.getShareService()
        for e in sh.getOwnShares(False):
            yield ShareWrapper(self, e)
    
    def getMemberShares(self):
        """
        Gets all shares where current user is a member.
        
        @return:    Shares that user is a member of
        @rtype:     L{ShareWrapper} generator
        """
        
        sh = self.getShareService()
        for e in sh.getMemberShares(False):
            yield ShareWrapper(self, e)
    
    def getMemberCount(self, share_ids):
        """
        Returns a map from share id to the count of total members (including the
        owner). This is represented by ome.model.meta.ShareMember links.
        
        @param share_ids:   List of IDs
        @type share_ids:    List of Longs
        @return:            Dict of shareId: member-count
        @rtype:             Dict of long: long
        """
        
        sh = self.getShareService()
        return sh.getMemberCount(share_ids)
    
    def getCommentCount(self, share_ids):
        """ 
        Returns a map from share id to comment count.
        
        @param share_ids:   List of IDs
        @type share_ids:    List of Longs
        @return:            Dict of shareId: comment-count
        @rtype:             Dict of long: long 
        """
        
        sh = self.getShareService()
        return sh.getCommentCount(share_ids)
    
    def getContents(self, share_id):
        """ 
        Looks up all items belonging to the share, wrapped in object wrapper
        
        @param share_id:    share ID
        @type share_id:     Long
        @return:            Share contents
        @rtype:             L{omero.gateway.BlitzObjectWrapper} generator
        """
        
        sh = self.getShareService()
        for e in sh.getContents(long(share_id)):
            try:
                obj = omero.gateway.BlitzObjectWrapper(self, e)
            except:
                obj = omero.gateway.BlitzObjectWrapper(self,None)
                obj._obj = e
            yield obj
                
    def getComments(self, share_id):
        """
        Looks up all comments which belong to the share, wrapped in object wrapper
        
        @param share_id:    share ID
        @type share_id:     Long
        @return:            Share comments
        @rtype:             L{AnnotationWrapper} generator
        """
        
        sh = self.getShareService()
        for e in sh.getComments(long(share_id)):
            yield AnnotationWrapper(self, e)
    
    def getAllMembers(self, share_id):
        """
        Get all {@link Experimenter users} who are a member of the share.
        
        @param share_id:    share ID
        @type share_id:     Long
        @return:            Members of share
        @rtype:             L{ExperimenterWrapper} generator
        """
        
        sh = self.getShareService()
        for e in sh.getAllMembers(long(share_id)):
            yield ExperimenterWrapper(self, e)

    def getAllGuests(self, share_id):
        """
        Get the email addresses for all share guests.
        
        @param share_id:    share ID
        @type share_id:     Long
        @return:            List of e-mail addresses
        @rtype:             List of Strings
        """
        
        sh = self.getShareService()
        return sh.getAllGuests(long(share_id))

    def getAllUsers(self, share_id):
        """
        Get a single set containing the login names of the users as well email addresses for guests.
        
        @param share_id:    share ID
        @type share_id:     Long
        @return:            List of usernames and e-mail addresses
        @rtype:             List of Strings
        """
        
        sh = self.getShareService()
        return sh.getAllUsers(long(share_id))
    
    def prepareRecipients(self, recipients):
        recps = list()
        for m in recipients:
            try:
                if m.email is not None and m.email!="":
                    recps.append(m.email)
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
                blitz = Server.get(pk=blitz_id)
                t = settings.EMAIL_TEMPLATES["add_comment_to_share"]
                message = t['text_content'] % (host, blitz_id)
                message_html = t['html_content'] % (host, blitz_id, host, blitz_id)
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
        img = self.getObject("Image", image_id)
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
        sid = sh.createShare(message, rtime(expiration), items, ms, [], enable)
        sh.addObjects(sid, items)
        
        #send email if avtive
        if enable:
            try:
                recipients = self.prepareRecipients(ms)
            except Exception, x:
                logger.error(traceback.format_exc())
            else:
                t = settings.EMAIL_TEMPLATES["create_share"]
                message = t['text_content'] % (host, blitz_id, self.getUser().getFullName())
                message_html = t['html_content'] % (host, blitz_id, host, blitz_id, self.getUser().getFullName())
                
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
        sh.setExpiration(long(share_id), rtime(expiration))
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
                blitz = Server.get(pk=blitz_id)
                t = settings.EMAIL_TEMPLATES["add_member_to_share"]
                message = t['text_content'] % (host, blitz_id, self.getUser().getFullName())
                message_html = t['html_content'] % (host, blitz_id, host, blitz_id, self.getUser().getFullName())
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
                blitz = Server.get(pk=blitz_id)
                t = settings.EMAIL_TEMPLATES["remove_member_from_share"]
                message = t['text_content'] % (host, blitz_id)
                message_html = t['html_content'] % (host, blitz_id, host, blitz_id)
                
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
        @rtype:     L{ShareWrapper} generator
        """
        
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentShareCommentLinks(p):
            yield ShareWrapper(self, e.parent)
    
    def listMostRecentShareComments (self):
        """
        Retrieve most recent share comments 
        controlled by the security system.
        
        @return:    Generator yielding SessionAnnotationLink
        @rtype:     L{SessionCommentWrapper} generator
        """
        
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(self.getEventContext().userId)
        f.limit = rint(10)
        p.theFilter = f
        for e in tm.getMostRecentShareCommentLinks(p):
            yield AnnotationWrapper(self, e.child, link=ShareWrapper(self, e.parent))
    
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
            yield omero.gateway.BlitzObjectWrapper(self, e)
    
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
            yield omero.gateway.BlitzObjectWrapper(self, e.child)
    
    def getDataByPeriod (self, start, end, eid, otype=None, page=None):
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
        tm = self.getTimelineService()
        p = omero.sys.Parameters()
        p.map = {}
        f = omero.sys.Filter()
        f.ownerId = rlong(eid)
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
    
    def countDataByPeriod (self, start, end, eid, otype=None):
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
        f.ownerId = rlong(eid)
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

    def getEventsByPeriod (self, start, end, eid):
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
        f.ownerId = rlong(eid)
        f.groupId = rlong(self.getEventContext().groupId)
        p.theFilter = f
        return tm.getEventLogsByPeriod(rtime(start), rtime(end), p)
        #yield EventLogWrapper(self, e)

omero.gateway.BlitzGateway = OmeroWebGateway

class OmeroWebSafeCallWrapper(OmeroGatewaySafeCallWrapper): #pragma: no cover
    """
    Function or method wrapper that handles L{Ice.ObjectNotExistException}
    by re-creating the server side proxy.
    """

    def handle_exception(self, e, *args, **kwargs):
        if e.__class__ is Ice.ObjectNotExistException:
            # Restored proxy object re-creation logic from the pre-#5835
            # version of # _safeCallWrap() from omero.gateway. (See #6365)
            logger.warn('Attempting to re-create proxy and re-call method.')
            try:
                self.proxyObjectWrapper._obj = \
                        self.proxyObjectWrapper._create_func()
                func = getattr(self.proxyObjectWrapper._obj, self.attr)
                return func(*args, **kwargs)
            except Exception, e:
                self.debug(e.__class__.__name__, args, kwargs)
                raise
        else:
            super(OmeroWebSafeCallWrapper, self).handle_exception(
                    e, *args, **kwargs)


omero.gateway.SafeCallWrapper = OmeroWebSafeCallWrapper

class OmeroWebObjectWrapper (object):
    
    annotation_counter = None
    
    def countParents (self):
        l = self.listParents()
        if l is not None:
            return len(l)
    
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

class ExperimenterWrapper (OmeroWebObjectWrapper, omero.gateway.ExperimenterWrapper): 
    """
    omero_model_ExperimenterI class wrapper overwrite omero.gateway.ExperimenterWrapper
    and extend OmeroWebObjectWrapper.
    """
    
    def isEditable(self):
        return self.omeName.lower() not in ('guest')

omero.gateway.ExperimenterWrapper = ExperimenterWrapper 

class ExperimenterGroupWrapper (OmeroWebObjectWrapper, omero.gateway.ExperimenterGroupWrapper): 
    """
    omero_model_ExperimenterGroupI class wrapper overwrite omero.gateway.ExperimenterGroupWrapper
    and extend OmeroWebObjectWrapper.
    """
    
    def isEditable(self):
        return self.name.lower() not in ('guest', 'user')

omero.gateway.ExperimenterGroupWrapper = ExperimenterGroupWrapper 

class ProjectWrapper (OmeroWebObjectWrapper, omero.gateway.ProjectWrapper): 
    """
    omero_model_ProjectI class wrapper overwrite omero.gateway.ProjectWrapper
    and extend OmeroWebObjectWrapper.
    """

    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        super(ProjectWrapper, self).__prepare__(**kwargs)
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
        super(DatasetWrapper, self).__prepare__(**kwargs)
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
        super(ImageWrapper, self).__prepare__(**kwargs)
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None
    
    """
    This override standard omero.gateway.ImageWrapper.getChannels 
    and catch exceptions.
    """
    def getChannels (self):
        try:
            return super(ImageWrapper, self).getChannels()
        except Exception, x:
            logger.error('Failed to load channels:', exc_info=True)
            return None


omero.gateway.ImageWrapper = ImageWrapper

class PlateWrapper (OmeroWebObjectWrapper, omero.gateway.PlateWrapper):
    
    """
    omero_model_PlateI class wrapper overwrite omero.gateway.PlateWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    annotation_counter = None

    def __prepare__ (self, **kwargs):
        super(PlateWrapper, self).__prepare__(**kwargs)
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None

omero.gateway.PlateWrapper = PlateWrapper

class WellWrapper (OmeroWebObjectWrapper, omero.gateway.WellWrapper):
    """
    omero_model_ImageI class wrapper overwrite omero.gateway.ImageWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        super(WellWrapper, self).__prepare__(**kwargs)
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None

omero.gateway.WellWrapper = WellWrapper

class PlateAcquisitionWrapper (OmeroWebObjectWrapper, omero.gateway.PlateAcquisitionWrapper):
    
    """
    omero_model_PlateI class wrapper overwrite omero.gateway.PlateWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    annotation_counter = None

    def __prepare__ (self, **kwargs):
        super(PlateAcquisitionWrapper, self).__prepare__(**kwargs)
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']

omero.gateway.PlateAcquisitionWrapper = PlateAcquisitionWrapper

class ScreenWrapper (OmeroWebObjectWrapper, omero.gateway.ScreenWrapper):
    """
    omero_model_ScreenI class wrapper overwrite omero.gateway.ScreenWrapper
    and extends OmeroWebObjectWrapper.
    """
    
    annotation_counter = None

    def __prepare__ (self, **kwargs):
        super(ScreenWrapper, self).__prepare__(**kwargs)
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']

omero.gateway.ScreenWrapper = ScreenWrapper

class EventLogWrapper (omero.gateway.BlitzObjectWrapper):
    """
    omero_model_EventLogI class wrapper extends omero.gateway.BlitzObjectWrapper.
    """
    
    LINK_CLASS = "EventLog"

class ShareWrapper (omero.gateway.BlitzObjectWrapper):
    """
    omero_model_ShareI class wrapper extends BlitzObjectWrapper.
    """
    
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
            if d > 2051222400000:
                return datetime(2035, 1, 1, 0, 0, 0)            
            return datetime.fromtimestamp(d / 1000)
        except:
            logger.info(traceback.format_exc())
        return None
        
    def getStartDate(self):
        """
        Gets the start date of the share
        
        @return:    Start Date-time
        @rtype:     datetime object
        """
        
        return datetime.fromtimestamp(self.getStarted()/1000)
        
    def getExpirationDate(self):
        """
        Gets the end date for the share
        
        @return:    End Date-time
        @rtype:     datetime object
        """
        
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
        """
        Returns True if we are past the end date of the share
        
        @return:    True if share expired
        @rtype:     Boolean
        """
        
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
    
    def isOwned(self):
        """
        Returns True if share is owned by the current user
        
        @return:    True if owned
        @rtype:     Boolean
        """
        
        try:
            if self.owner.id.val == self._conn.getEventContext().userId:
                return True
        except:
            logger.error(traceback.format_exc())
        return False
    
    def getOwner(self):
        """
        The owner of this share
        
        @return:    Owner
        @rtype:     L{ExperimenterWrapper}
        """
        
        return omero.gateway.ExperimenterWrapper(self, self.owner)

omero.gateway.refreshWrappers()
