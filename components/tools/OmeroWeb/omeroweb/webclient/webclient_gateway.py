#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# webclient_gateway
#
# Copyright (c) 2008-2014 University of Dundee.
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

from StringIO import StringIO

import time
from datetime import datetime

import Ice
import omero.gateway
import omero.scripts

from omero.rtypes import rbool, rint, rstring, rlong, rlist, rtime, unwrap
from omero.model import ExperimenterI, ExperimenterGroupI
from omero.cmd import Chmod

from omero.gateway import TagAnnotationWrapper, AnnotationWrapper
from omero.gateway import OmeroGatewaySafeCallWrapper
from omero.gateway import CommentAnnotationWrapper

from omero.fs import TRANSFERS

from omero.gateway import KNOWN_WRAPPERS

from django.utils.encoding import smart_str
from django.conf import settings

from omero.gateway.utils import toBoolean

try:
    import hashlib
    hash_sha1 = hashlib.sha1
except:
    import sha
    hash_sha1 = sha.new

logger = logging.getLogger(__name__)

try:
    from PIL import Image  # see ticket:2597
except ImportError:
    try:
        import Image  # see ticket:2597
    except:
        logger.error(
            "You need to install the Python Imaging Library. Get it at"
            " http://www.pythonware.com/products/pil/")
        logger.error(traceback.format_exc())


def defaultThumbnail(size=(120, 120)):
    if isinstance(size, int):
        size = (size, size)
    if len(size) == 1:
        size = (size[0], size[0])
    img = Image.open(settings.DEFAULT_IMG)
    img.thumbnail(size, Image.ANTIALIAS)
    f = cStringIO.StringIO()
    img.save(f, "PNG")
    f.seek(0)
    return f.read()


class OmeroWebGateway(omero.gateway.BlitzGateway):

    def __init__(self, *args, **kwargs):
        """
        Create the connection wrapper. Does not attempt to connect at this
        stage
        Initialises the omero.client

        @param username:    User name. If not specified, use
                            'omero.gateway.anon_user'
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
        @param secure:      Initial underlying omero.client connection type
                            (True=SSL/False=insecure)
        @type secure:       Boolean
        @param anonymous:
        @type anonymous:    Boolean
        @param useragent:   Log which python clients use this connection. E.g.
                            'OMERO.webadmin'
        @type useragent:    String

        @param _shareId:    Active share ID
        @type _shareId:     Long
        """

        super(OmeroWebGateway, self).__init__(*args, **kwargs)
        self._shareId = None

    def getShareId(self):
        """
        Returns active share id .

        @return:    Share ID
        @rtype:     Long
        """

        if self.getEventContext().shareId is not None:
            if (self.getEventContext().shareId != self._shareId and
                    self._shareId > 0):
                self._shareId = self.getEventContext().shareId
        return self._shareId

    ##############################################
    #    Session methods                         #

    def changeActiveGroup(self, gid):  # TODO: should be moved to ISession
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

            self.c.sf.setSecurityContext(ExperimenterGroupI(gid, False))
            self.getAdminService().setDefaultGroup(
                self.getUser()._obj, ExperimenterGroupI(gid, False))
            self._ctx = self.getAdminService().getEventContext()
            return True
        except omero.SecurityViolation:
            logger.error(traceback.format_exc())
            return False
        except:
            logger.error(traceback.format_exc())
            return False

    def getOrphanedContainerSettings(self):
        name = (self.getConfigService().getConfigValue(
                "omero.client.ui.tree.orphans.name") or "Orphaned image")
        description = (self.getConfigService().getConfigValue(
                       "omero.client.ui.tree.orphans.description") or
                       "This is a virtual container with orphaned images.")
        return name, description

    def getDropdownMenuSettings(self):
        dropdown_menu = dict()
        if toBoolean(self.getConfigService().getConfigValue(
                     "omero.client.ui.menu.dropdown.leaders.enabled")):
            dropdown_menu["leaders"] = self.getConfigService().getConfigValue(
                "omero.client.ui.menu.dropdown.leaders")
        if toBoolean(self.getConfigService().getConfigValue(
                "omero.client.ui.menu.dropdown.colleagues.enabled")):
            dropdown_menu["colleagues"] = \
                self.getConfigService().getConfigValue(
                    "omero.client.ui.menu.dropdown.colleagues")
        if toBoolean(self.getConfigService().getConfigValue(
                "omero.client.ui.menu.dropdown.everyone.enabled")):
            dropdown_menu["everyone"] = \
                self.getConfigService().getConfigValue(
                    "omero.client.ui.menu.dropdown.everyone")
        return dropdown_menu

    ##############################################
    #   IAdmin                                  ##

    def getEmailSettings(self):
        """
        Retrieves a configuration value "omero.mail.config" from the backend
        store.

        @return:        String
        """

        conf = self.getConfigService()
        return toBoolean(conf.getConfigValue("omero.mail.config"))

    def isAnythingCreated(self):
        """
        Checks if any of the experimenter was created before

        @return:    Boolean
        """

        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["default_names"] = rlist(
            [rstring("user"), rstring("system"), rstring("guest")])
        f = omero.sys.Filter()
        f.limit = rint(1)
        p.theFilter = f
        sql = ("select g from ExperimenterGroup as g "
               "where g.name not in (:default_names)")
        if len(q.findAllByQuery(sql, p, self.SERVICE_OPTS)) > 0:
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
        for e in q.findAllByQuery(sql, p, self.SERVICE_OPTS):
            yield ExperimenterWrapper(self, e)

    # def getCurrentSupervisor(self):
    #    """
    #    Gets the owner of a group for current user.
    #
    #    @return:        ExperimenterWrapper
    #    """
    #
    #    p = omero.sys.ParametersI()
    #    p.map = {}
    #    p.map["id"] = rlong(self.getEventContext().groupId)
    #
    #    # TODO: there can now be multiple supervisors
    #    p.page(0,1)
    #    supervisor = self.getQueryService().findByQuery(\
    #        """select e from ExperimenterGroup as g
    #           join g.groupExperimenterMap as m join m.child as e
    #           where m.owner = true and g.id = :id""", p)
    #    return ExperimenterWrapper(self, supervisor)
    #
    # def getScriptwithDetails(self, sid):
    #    script_serv = self.getScriptService()
    #    return script_serv.getScriptWithDetails(long(sid))
    #
    # def lookupScripts(self):
    #    script_serv = self.getScriptService()
    #    return script_serv.getScripts()

    def getServerVersion(self):
        """
        Retrieves a configuration value "omero.version" from the backend
        store.

        @return:        String
        """

        conf = self.getConfigService()
        return conf.getConfigValue("omero.version")

    def getUpgradesUrl(self):
        """
        Retrieves a configuration value "omero.upgrades.url" from
        the backend store.

        @return:        String
        """

        conf = self.getConfigService()
        return conf.getConfigValue("omero.upgrades.url")

    #########################################################
    #   From Bram b(dot)gerritsen(at)nki(dot)nl            ##

    def findWellInPlate(self, plate_name, row, column):
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
        well = q.findByQuery(sql, p, self.SERVICE_OPTS)
        if well is None:
            return None
        else:
            return WellWrapper(self, well, None)

    ##########################################################################
    #   Container Queries                                                    #
    ##########################################################################

    def listTags(self, eid=None):
        params = omero.sys.ParametersI()
        params.orphan()
        params.map = {}
        params.map['ns'] = rstring(omero.constants.metadata.NSINSIGHTTAGSET)

        sql = ("select tg from TagAnnotation tg"
               " where ((ns=:ns)"
               "or (not exists ( select aal from AnnotationAnnotationLink"
               " as aal where aal.child=tg.id))) ")
        if eid is not None:
            params.map["eid"] = rlong(long(eid))
            sql += " and tg.details.owner.id = :eid"

        q = self.getQueryService()
        for ann in q.findAllByQuery(sql, params, self.SERVICE_OPTS):
            yield TagAnnotationWrapper(self, ann)

    def listTagsRecursive(self, eid=None, offset=None, limit=1000):
        """
        Returns a two-element tuple:
        * A list of tags, where each tag is a list with the following
          elements:
          0: id
          1: description
          2: text
          3: owner id
          4: list of child tag ids (possibly empty), or 0 if this is not a tag
             set
        * A dictionary of user ids mapped to user names
        """

        params = omero.sys.ParametersI()
        params.orphan()
        params.map = {}
        params.map['ns'] = rstring(omero.constants.metadata.NSINSIGHTTAGSET)
        if eid is not None:
            params.map["eid"] = rlong(long(eid))

        q = self.getQueryService()

        if offset is not None:
            params.page(offset, limit)

        sql = """
            select ann.id, ann.description, ann.textValue, ann.ns,
            ann.details.owner.id,
            ann.details.owner.firstName, ann.details.owner.lastName
            from TagAnnotation ann
            """
        if eid is not None:
            sql += " where ann.details.owner.id = :eid"
        sql += " order by ann.id"

        tags = []
        owners = dict()
        for element in q.projection(sql, params, self.SERVICE_OPTS):
            tag_id, description, text, ns, owner, first, last = map(
                unwrap, element)
            tags.append([
                tag_id,
                description,
                text,
                owner,
                # if tagset, list to be filled in later, otherwise 0
                [] if ns == omero.constants.metadata.NSINSIGHTTAGSET else 0,
            ])
            owners[owner] = "%s %s" % (first, last)

        ids = [tag[0] for tag in tags]

        params.noPage()
        params.addIds(ids)

        sql = """
            select aal.parent.id, aal.child.id
            from AnnotationAnnotationLink aal
            inner join aal.parent ann
            where ann.ns=:ns
            and aal.parent.id in (:ids)
            """
        if eid is not None:
            sql += " and ann.details.owner.id = :eid"

        mapping = dict()
        if ids:
            for element in q.projection(sql, params, self.SERVICE_OPTS):
                parent = unwrap(element[0])
                child = unwrap(element[1])
                mapping.setdefault(parent, []).append(child)

        for idx in range(len(tags)):
            children = mapping.get(tags[idx][0])
            if children:
                tags[idx][4] = children

        return tags, owners

    def getTagCount(self, eid=None):
        params = omero.sys.ParametersI()
        params.orphan()
        params.map = {}

        q = self.getQueryService()

        sql = """
            select count(ann)
            from TagAnnotation ann
            """

        if eid is not None:
            params.map["eid"] = rlong(long(eid))
            sql += " where ann.details.owner.id = :eid"

        return unwrap(q.projection(sql, params, self.SERVICE_OPTS)[0][0])

    def countOrphans(self, obj_type, eid=None):
        links = {'Dataset': ('ProjectDatasetLink', DatasetWrapper),
                 'Image': ('DatasetImageLink', ImageWrapper),
                 'Plate': ('ScreenPlateLink', PlateWrapper)}

        if obj_type not in links:
            raise TypeError(
                "'%s' is not valid object type. Must use one of %s"
                % (obj_type, links.keys()))

        q = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}

        links = {'Dataset': ('ProjectDatasetLink', DatasetWrapper),
                 'Image': ('DatasetImageLink', ImageWrapper),
                 'Plate': ('ScreenPlateLink', PlateWrapper)}

        if obj_type not in links:
            raise TypeError(
                "'%s' is not valid object type. Must use one of %s"
                % (obj_type, links.keys()))

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

        sql = ("select count(obj.id) from %s as obj "
               "join obj.details.creationEvent "
               "join obj.details.owner join obj.details.group "
               "where %s"
               "not exists (select obl from %s as obl where "
               "obl.child=obj.id)"
               % (obj_type, eidFilter, links[obj_type][0]))
        if obj_type == 'Image':
            sql += ("and not exists ( "
                    "select ws from WellSample as ws "
                    "where ws.image=obj.id %s)" % eidWsFilter)

        rslt = q.projection(sql, p, self.SERVICE_OPTS)
        if len(rslt) > 0:
            if len(rslt[0]) > 0:
                return rslt[0][0].val
        return 0

    def listImagesInDataset(self, oid, eid=None, page=None,
                            load_pixels=False):
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
        p = omero.sys.ParametersI()
        p.map["oid"] = rlong(long(oid))
        if page is not None:
            p.page(((int(page)-1)*settings.PAGE), settings.PAGE)
        if load_pixels:
            pixels = ("join fetch im.pixels as pix"
                      " left outer join fetch pix.thumbnails ")
        else:
            pixels = ""
        sql = ("select im from Image im "
               "join fetch im.details.creationEvent "
               "join fetch im.details.owner join fetch im.details.group "
               "left outer join fetch im.datasetLinks dil "
               "left outer join fetch dil.parent d %s"
               "where d.id = :oid" % pixels)
        if eid is not None:
            p.map["eid"] = rlong(long(eid))
            sql += " and im.details.owner.id=:eid"
        sql += " order by im.name ASC"

        for e in q.findAllByQuery(sql, p, self.SERVICE_OPTS):
            kwargs = {'link': omero.gateway.BlitzObjectWrapper(
                self, e.copyDatasetLinks()[0])}
            yield ImageWrapper(self, e, None, **kwargs)

    def createDataset(self, name, description=None, img_ids=None):
        """
        Creates a Dataset and adds images if img_ids is specified.
        Returns the new Dataset ID.
        """
        ds = omero.model.DatasetI()
        ds.name = rstring(str(name))
        if description is not None and description != "":
            ds.description = rstring(str(description))
        dsid = self.saveAndReturnId(ds)
        if img_ids is not None:
            iids = [int(i) for i in img_ids.split(",")]
            links = []
            for iid in iids:
                link = omero.model.DatasetImageLinkI()
                link.setParent(omero.model.DatasetI(dsid, False))
                link.setChild(omero.model.ImageI(iid, False))
                links.append(link)
            self.saveArray(links)
        return dsid

    def createProject(self, name, description=None):
        """ Creates new Project and returns ID """

        pr = omero.model.ProjectI()
        pr.name = rstring(str(name))
        if description is not None and description != "":
            pr.description = rstring(str(description))
        return self.saveAndReturnId(pr)

    def createScreen(self, name, description=None):
        """ Creates new Screen and returns ID """

        sc = omero.model.ScreenI()
        sc.name = rstring(str(name))
        if description is not None and description != "":
            sc.description = rstring(str(description))
        return self.saveAndReturnId(sc)

    def createContainer(self, dtype, name, description=None):
        """ Creates new Project, Dataset or Screen and returns ID """

        dtype = dtype.lower()
        if dtype == "project":
            return self.createProject(name, description)
        elif dtype == "dataset":
            return self.createDataset(name, description)
        elif dtype == "screen":
            return self.createScreen(name, description)

    # DATA RETRIVAL BY TAGs
    def findTag(self, name, desc=None):
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
        # p.map["eid"] = rlong(self.getEventContext().userId)
        f = omero.sys.Filter()
        f.limit = rint(1)
        p.theFilter = f
        sql = "select tg from TagAnnotation tg " \
              "where tg.textValue=:text"
        if desc is not None:
            sql += " and tg.description=:desc"
        sql += " and tg.ns is null order by tg.textValue"
        res = query_serv.findAllByQuery(sql, p, self.SERVICE_OPTS)
        if len(res) > 0:
            return TagAnnotationWrapper(self, res[0])
        return None

    # AVATAR #
    def uploadMyUserPhoto(self, filename, format, data):
        """
        Uploads a photo for the user which will be displayed on his/her
        profile.
        This photo will be saved as an OriginalFile object
        with the given format, and attached to the user's Experimenter
        object via an File Annotation with
        the namespace: "openmicroscopy.org/omero/experimenter/photo"
        (NSEXPERIMENTERPHOTO).
        If such an OriginalFile instance already exists,
        it will be overwritten. If more than one photo is present, the oldest
        version will be modified (i.e. the highest updateEvent id).

        Note: as outlined in ticket:1794, this photo will be placed in the
        "user" group and therefore will be visible to everyone on the system.

        @param filename     name which will be used.
        @type filename      String
        @param format       Format.value string. 'image/jpeg' and 'image/png'
                            are common values.
        @type format        String
        @param data         Data from the image. This will be written to disk.
        @type data          String

        @return             ID of the overwritten or newly created user photo
                            OriginalFile object.
        @rtype              Long
        """

        admin_serv = self.getAdminService()
        pid = admin_serv.uploadMyUserPhoto(filename, format, data)
        if pid is not None:
            return pid

    def hasExperimenterPhoto(self, oid=None):
        """
        Check if File annotation with the namespace:
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO) is
        linked to the given user ID. If user id not set, owned by the current
        user.

        @param oid      experimenter ID
        @type oid       Long
        @return         True or False
        @rtype          Boolean
        """

        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations(
                    "Experimenter", [self.getEventContext().userId], None,
                    None, None).get(self.getEventContext().userId, [])
            else:
                ann = meta.loadAnnotations(
                    "Experimenter", [long(oid)], None, None,
                    None).get(long(oid), [])
            if len(ann) > 0:
                return True
            else:
                return False
        except:
            logger.error(traceback.format_exc())
            return False

    def getExperimenterPhoto(self, oid=None):
        """
        Get File annotation with the namespace:
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO)
        linked to the given user ID. If user id not set, owned by the current
        user.

        @param oid      experimenter ID
        @type oid       Long
        @return         Data from the image.
        @rtype          String
        """

        photo = None
        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations(
                    "Experimenter", [self.getEventContext().userId], None,
                    None, None).get(self.getEventContext().userId, [])
            else:
                ann = meta.loadAnnotations(
                    "Experimenter", [long(oid)], None, None,
                    None).get(long(oid), [])
            if len(ann) > 0:
                ann = ann[0]
                store = self.createRawFileStore()
                try:
                    store.setFileId(ann.file.id.val)
                    photo = store.read(0, long(ann.file.size.val))
                finally:
                    store.close()
            else:
                photo = self.getExperimenterDefaultPhoto()
        except:
            logger.error(traceback.format_exc())
            photo = self.getExperimenterDefaultPhoto()
        if photo is None:
            photo = self.getExperimenterDefaultPhoto()
        return photo

    def getExperimenterPhotoSize(self, oid=None):
        """
        Get size of File annotation with the namespace:
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO)
        linked to the given user ID. If user id not set, owned by the current
        user.

        @param oid      experimenter ID
        @type oid       Long
        @return         Tuple including dimention and size of the file
        @rtype          Tuple
        """

        photo = None
        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations(
                    "Experimenter", [self.getEventContext().userId], None,
                    None, None).get(self.getEventContext().userId, [])[0]
            else:
                ann = meta.loadAnnotations(
                    "Experimenter", [long(oid)], None, None,
                    None).get(long(oid), [])[0]
            store = self.createRawFileStore()
            try:
                store.setFileId(ann.file.id.val)
                photo = store.read(0, long(ann.file.size.val))
            finally:
                store.close()
            try:
                im = Image.open(StringIO(photo))
            except:
                logger.error(traceback.format_exc())
                return None
            else:
                return (im.size, ann.file.size.val)
        except:
            return None

    def deleteExperimenterPhoto(self, oid=None):
        ann = None
        meta = self.getMetadataService()
        try:
            if oid is None:
                ann = meta.loadAnnotations(
                    "Experimenter", [self.getEventContext().userId], None,
                    None, None).get(self.getEventContext().userId, [])[0]
            else:
                ann = meta.loadAnnotations(
                    "Experimenter", [long(oid)], None, None,
                    None).get(long(oid), [])[0]
        except:
            logger.error(traceback.format_exc())
            raise IOError("Photo does not exist.")
        else:
            exp = self.getUser()
            links = exp._getAnnotationLinks()
            # there should be only one ExperimenterAnnotationLink
            # but if there is more then one all of them should be deleted.
            for l in links:
                self.deleteObjectDirect(l)
            # No error handling?
            self.deleteObjects("/Annotation", [ann.id.val])

    def cropExperimenterPhoto(self, box, oid=None):
        """
        Crop File annotation with the namespace:
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO)
        linked to the given user ID. If user id not set, owned by the current
        user.
        New dimensions are defined by squer positions box = (x1,y1,x2,y2)

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
                ann = meta.loadAnnotations(
                    "Experimenter", [self.getEventContext().userId], None,
                    None, None).get(self.getEventContext().userId, [])[0]
            else:
                ann = meta.loadAnnotations(
                    "Experimenter", [long(oid)], None, None,
                    None).get(long(oid), [])[0]
            store = self.createRawFileStore()
            try:
                store.setFileId(ann.file.id.val)
                photo = store.read(0, long(ann.file.size.val))
            finally:
                store.close()
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
                imdata = StringIO()
                region.save(imdata, format=im.format)
                self.uploadMyUserPhoto(
                    ann.file.name.val, ann.file.mimetype.val,
                    imdata.getvalue())

    def getExperimenterDefaultPhoto(self):
        """
        If file annotation with the namespace:
        "openmicroscopy.org/omero/experimenter/photo" (NSEXPERIMENTERPHOTO)
        is not linked to experimenter this method generate default picture of
        the person.

        @return         Data from the image.
        @rtype          String
        """

        img = Image.open(settings.DEFAULT_USER)
        img.thumbnail((150, 150), Image.ANTIALIAS)
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
        return query_serv.findByString(
            "Format", "value", format).getValue().val

    ################################################
    #   Counters

    def getCollectionCount(self, parent, child, ids):
        """
        Counts the number of members in a collection for a given object.

        @param parent       The fully-qualified classname of the object to be
                            tested
        @type parent        String
        @param child        Name of the property on that class, omitting
                            getters and setters.
        @type child         String
        @param ids          Set of Longs, the ids of the objects to test
        @type ids           L{Long}
        @return             A map from id integer to count integer
        @rtype              L{(Long, Long)}
        """
        container = self.getContainerService()
        return container.getCollectionCount(
            parent, child, ids, None, self.SERVICE_OPTS)

    ################################################
    #   Validators

    def checkOmeName(self, ome_name, old_omeName=None):
        if ome_name == old_omeName:
            return False
        query_serv = self.getQueryService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map["omeName"] = rstring(smart_str(ome_name))
        sql = "select e from Experimenter as e where e.omeName = (:omeName)"
        exps = query_serv.findAllByQuery(sql, p, self.SERVICE_OPTS)
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
        grs = query_serv.findAllByQuery(sql, p, self.SERVICE_OPTS)
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
        exps = query_serv.findAllByQuery(sql, p, self.SERVICE_OPTS)
        if len(exps) > 0:
            return True
        else:
            return False

    ##############################################
    #   Sets methods                           ##

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

        @param password         Must pass validation in the security
                                sub-system.
        @type password          String
        @param old_password     Old password
        @type old_password      String
        @return                 None or error message if password could not be
                                changed
        @rtype                  String
        """
        admin_serv = self.getAdminService()
        admin_serv.changePasswordWithOldPassword(
            rstring(str(old_password)), rstring(str(password)))

    def createExperimenter(self, omeName, firstName, lastName, email, isAdmin,
                           isActive, defaultGroup, otherGroups, password,
                           middleName=None, institution=None):
        """
        Create and return a new user in the given groups with password.
        @param omeName A new username.
        @type omeName String
        @param firstName A new first name.
        @type firstName String
        @param lastName A new last name.
        @type lastName String
        @param email A new email.
        @type email String
        @param isAdmin An Admin permission.
        @type isAdmin Boolean
        @param isActive Active user (user can log in).
        @type isActive Boolean
        @param defaultGroup Instance of ExperimenterGroup selected as a first
                            active group.
        @type defaultGroup ExperimenterGroupI
        @param otherGroups List of ExperimenterGroup instances. Can be empty.
        @type otherGroups L{ExperimenterGroupI}
        @param password Must pass validation in the security sub-system.
        @type password String
        @param middleName A middle name.
        @type middleName String
        @param institution An institution.
        @type institution String
        @return ID of the newly created Experimenter Not null.
        @rtype Long
        """
        experimenter = ExperimenterI()
        experimenter.omeName = rstring(str(omeName))
        experimenter.firstName = rstring(str(firstName))
        experimenter.middleName = (middleName is not None and
                                   rstring(str(middleName)) or None)
        experimenter.lastName = rstring(str(lastName))
        experimenter.email = rstring(str(email))
        experimenter.institution = ((institution != "" and
                                     institution is not None) and
                                    rstring(str(institution)) or None)
        experimenter.ldap = rbool(False)

        listOfGroups = list()
        # system group
        if isAdmin:
            g = self.getObject("ExperimenterGroup",
                               attributes={'name': 'system'})
            listOfGroups.append(g._obj)

        # user group
        if isActive:
            g = self.getObject("ExperimenterGroup",
                               attributes={'name': 'user'})
            listOfGroups.append(g._obj)

        for g in otherGroups:
            listOfGroups.append(g._obj)

        admin_serv = self.getAdminService()
        return admin_serv.createExperimenterWithPassword(
            experimenter, rstring(str(password)), defaultGroup._obj,
            listOfGroups)

    def updateExperimenter(self, experimenter, omeName, firstName, lastName,
                           email, isAdmin, isActive, defaultGroup,
                           otherGroups, middleName=None, institution=None):
        """
        Update an existing user including groups user is a member of.
        Password cannot be changed by calling that method.
        @param experimenter An existing Experimenter instance.
        @type experimenter ExperimenterWrapper
        @param omeName A new username.
        @type omeName String
        @param firstName A new first name.
        @type firstName String
        @param lastName A new last name.
        @type lastName String
        @param email A new email.
        @type email String
        @param isAdmin An Admin permission.
        @type isAdmin Boolean
        @param isActive Active user (user can log in).
        @type isActive Boolean
        @param defaultGroup Instance of ExperimenterGroup selected as a first
                            active group.
        @type defaultGroup ExperimenterGroupI
        @param otherGroups List of ExperimenterGroup instances. Can be empty.
        @type otherGroups L{ExperimenterGroupI}
        @param middleName A middle name.
        @type middleName String
        @param institution An institution.
        @type institution String
        """
        up_exp = experimenter._obj
        up_exp.omeName = rstring(str(omeName))
        up_exp.firstName = rstring(str(firstName))
        up_exp.middleName = (middleName is not None and
                             rstring(str(middleName)) or None)
        up_exp.lastName = rstring(str(lastName))
        up_exp.email = rstring(str(email))
        up_exp.institution = (
            (institution != "" and institution is not None) and
            rstring(str(institution)) or None)

        # old list of groups
        old_groups = list()
        for ogr in up_exp.copyGroupExperimenterMap():
            if ogr is None:
                continue
            old_groups.append(ogr.parent)

        # create list of new groups
        new_groups = list()

        # default group
        new_groups.append(defaultGroup._obj)

        # system group
        if isAdmin:
            g = self.getObject("ExperimenterGroup",
                               attributes={'name': 'system'})
            if defaultGroup.id != g.id:
                new_groups.append(g._obj)

        # user group
        if isActive:
            g = self.getObject("ExperimenterGroup",
                               attributes={'name': 'user'})
            new_groups.append(g._obj)

        # rest of groups
        for g in otherGroups:
            new_groups.append(g._obj)

        addGroups = list()
        rmGroups = list()

        # remove
        for ogr in old_groups:
            flag = False
            for ngr in new_groups:
                if ngr.id.val == ogr.id.val:
                    flag = True
            if not flag:
                rmGroups.append(ogr)

        # add
        for ngr in new_groups:
            flag = False
            for ogr in old_groups:
                if ogr.id.val == ngr.id.val:
                    flag = True
            if not flag:
                addGroups.append(ngr)

        admin_serv = self.getAdminService()
        admin_serv.updateExperimenter(up_exp)
        if len(addGroups) > 0:
            admin_serv.addGroups(up_exp, addGroups)
        admin_serv.setDefaultGroup(up_exp, defaultGroup._obj)
        if len(rmGroups) > 0:
            admin_serv.removeGroups(up_exp, rmGroups)

    def setMembersOfGroup(self, group, new_members):
        """
        Change members of the group. Returns a list of existing group members
        that could not be removed from the group because it is their only
        group.

        @param group            An existing ExperimenterGroup instance.
        @type group             ExperimenterGroupI
        @param new_members      List of new new Experimenter Ids.
        @type new_members       L{Long}
        @return                 List of Experimenters not removed from group
        @rtype                  List of L{ExperimenterWrapper}
        """

        experimenters = list(self.getObjects("Experimenter"))

        new_membersIds = [nm.id for nm in new_members]

        old_members = group.getMembers()
        old_membersIds = [om.id for om in old_members]

        old_available = list()
        for e in experimenters:
            if e.id not in old_membersIds:
                old_available.append(e)
        old_availableIds = [oa.id for oa in old_available]

        new_available = list()
        for e in experimenters:
            if e.id not in new_membersIds:
                new_available.append(e)

        new_availableIds = [na.id for na in new_available]

        rm_exps = list(set(old_membersIds) - set(new_membersIds))
        add_exps = list(set(old_availableIds) - set(new_availableIds))

        to_remove = list()
        to_add = list()
        for e in experimenters:
            if e.id in rm_exps:
                # removing user from their default group #9193
                # if e.getDefaultGroup().id != group.id:
                to_remove.append(e._obj)
            if e.id in add_exps:
                to_add.append(e._obj)

        admin_serv = self.getAdminService()
        userGid = admin_serv.getSecurityRoles().userGroupId
        failures = []
        for e in to_add:
            admin_serv.addGroups(e, [group._obj])
        for e in to_remove:
            # Experimenter needs to stay in at least 1 non-user group
            gs = [l.parent.id.val for l in e.copyGroupExperimenterMap()
                  if l.parent.id.val != userGid]
            if len(gs) == 1:
                failures.append(ExperimenterWrapper(self, e))
                continue
            admin_serv.removeGroups(e, [group._obj])
        return failures

    def setOwnersOfGroup(self, group, new_owners):
        """
        Change members of the group.

        @param group            An existing ExperimenterGroup instance.
        @type group             ExperimenterGroupI
        @param new_members      List of new new Experimenter Ids.
        @type new_members       L{Long}
        """

        experimenters = list(self.getObjects("Experimenter"))

        new_ownersIds = [no.id for no in new_owners]

        old_owners = group.getOwners()
        old_ownersIds = [oo.id for oo in old_owners]

        old_available = list()
        for e in experimenters:
            if e.id not in old_ownersIds:
                old_available.append(e)
        old_availableIds = [oa.id for oa in old_available]

        new_available = list()
        for e in experimenters:
            if e.id not in new_ownersIds:
                new_available.append(e)

        new_availableIds = [na.id for na in new_available]

        rm_exps = list(set(old_ownersIds) - set(new_ownersIds))
        add_exps = list(set(old_availableIds) - set(new_availableIds))

        to_remove = list()
        to_add = list()
        for e in experimenters:
            if e.id in rm_exps:
                # removing user from their default group #9193
                # if e.getDefaultGroup().id != group.id:
                to_remove.append(e._obj)
            if e.id in add_exps:
                to_add.append(e._obj)

        admin_serv = self.getAdminService()
        admin_serv.addGroupOwners(group._obj, to_add)
        admin_serv.removeGroupOwners(group._obj, to_remove)

    # def deleteExperimenter(self, experimenter):
    #    """
    #    Removes a user by removing the password information for that user as
    #    well as all GroupExperimenterMap instances.
    #
    #    @param user     Experimenter to be deleted. Not null.
    #    @type user      ExperimenterI
    #    """
    #    admin_serv = self.getAdminService()
    #    admin_serv.deleteExperimenter(experimenter)

    def createGroup(self, name, permissions, owners=list(), description=None):
        """
        Create and return a new group with the given owners.

        @param group        A new ExperimenterGroup instance.
        @type group         ExperimenterGroupI
        @param owners       List of Experimenter instances. Can be empty.
        @type owners        L{ExperimenterI}
        @param permissions  Permissions instances.
        @type permissions   L{PermissionsI}
        @return             ID of the newly created ExperimenterGroup Not
                            null.
        @rtype              Long
        """
        new_gr = ExperimenterGroupI()
        new_gr.name = rstring(str(name))
        new_gr.description = (
            (description != "" and description is not None) and
            rstring(str(description)) or None)
        new_gr.ldap = rbool(False)
        new_gr.details.permissions = permissions

        admin_serv = self.getAdminService()
        gr_id = admin_serv.createGroup(new_gr)
        group = admin_serv.getGroup(gr_id)

        listOfOwners = list()
        for exp in owners:
            listOfOwners.append(exp._obj)

        admin_serv.addGroupOwners(group, listOfOwners)
        return gr_id

    def updateGroup(self, group, name, permissions, owners=list(),
                    description=None):
        """
        Update an existing user including groups user is a member of.
        Password cannot be changed by calling that method.

        @param group        A new ExperimenterGroup instance.
        @type group         ExperimenterGroupI
        @param name         A new group name.
        @type name          String
        @param permissions  Permissions instances.
        @type permissions   L{PermissionsI}
        @param owners       List of Experimenter instances. Can be empty.
        @type owners        L{ExperimenterI}
        @param description  A description.
        @type description   String

        """

        up_gr = group._obj
        up_gr.name = rstring(str(name))
        up_gr.description = (
            (description != "" and description is not None) and
            rstring(str(description)) or None)

        # old list of owners
        old_owners = list()
        for oex in up_gr.copyGroupExperimenterMap():
            if oex is None:
                continue
            if oex.owner.val:
                old_owners.append(oex.child)

        add_exps = list()
        rm_exps = list()

        # remove
        for oex in old_owners:
            flag = False
            for nex in owners:
                if nex._obj.id.val == oex.id.val:
                    flag = True
            if not flag:
                rm_exps.append(oex)

        # add
        for nex in owners:
            flag = False
            for oex in old_owners:
                if oex.id.val == nex._obj.id.val:
                    flag = True
            if not flag:
                add_exps.append(nex._obj)

        admin_serv = self.getAdminService()
        # Should we update updateGroup so this would be atomic?
        admin_serv.updateGroup(up_gr)
        if permissions is not None:
            self.updatePermissions(group, permissions)
        admin_serv.addGroupOwners(up_gr, add_exps)
        admin_serv.removeGroupOwners(up_gr, rm_exps)

    def updateMyAccount(self, experimenter, firstName, lastName, email,
                        defaultGroupId, middleName=None, institution=None):
        """
        Allows a user to update his/her own information and set the default
        group for a given user.
        @param experimenter     A data transfer object. Only the fields:
                                firstName, middleName, lastName, email, and
                                institution are checked. Not null.
        @type experimenter      ExperimenterWrapper
        @param firstName        A new first name.
        @type firstName         String
        @param lastName         A new last name.
        @type lastName          String
        @param email            A new email.
        @type email             String
        @param defaultGroup     Instance of ExperimenterGroup selected as a
                                first active group.
        @type defaultGroup      ExperimenterGroupI
        @param middleName       A middle name.
        @type middleName        String
        @param institution      An institution.
        @type institution       String
        """

        up_exp = experimenter._obj
        up_exp.firstName = rstring(str(firstName))
        up_exp.middleName = (middleName is not None and
                             rstring(str(middleName)) or None)
        up_exp.lastName = rstring(str(lastName))
        up_exp.email = rstring(str(email))
        up_exp.institution = (
            (institution != "" and institution is not None) and
            rstring(str(institution)) or None)

        admin_serv = self.getAdminService()
        admin_serv.updateSelf(up_exp)
        defaultGroup = self.getObject(
            "ExperimenterGroup", long(defaultGroupId))._obj
        admin_serv.setDefaultGroup(up_exp, defaultGroup)
        self.changeActiveGroup(defaultGroup.id)

    def setDefaultGroup(self, group_id, exp_id=None):
        """
        Sets the default group for the specified experimenter, or current user
        if not specified.
        """
        group_id = long(group_id)
        exp_id = (exp_id is not None and long(exp_id) or
                  self.getEventContext().userId)
        admin_serv = self.getAdminService()
        admin_serv.setDefaultGroup(
            ExperimenterI(exp_id, False), ExperimenterGroupI(group_id, False))

    def updatePermissions(self, group, permissions):
        """
        Allow to change the permission of the group.

        @param group      A wrapped entity or an unloaded reference to an
                        entity. Not null.
        @type group       BlitzObjectWrapper
        @param perm     The permissions value for this entity. Not null.
        @type perm      PermissionsI
        """

        perms = str(permissions)
        logger.debug("Chmod of group ID: %s to %s" % (group.id, perms))
        command = Chmod(type="/ExperimenterGroup",
                        id=group.id,
                        permissions=perms)
        cb = self.c.submit(command)
        cb.close(True)

    def saveObject(self, obj):
        """
        Provide method for directly updating object graphs. Act recursively on
        the entire object graph, replacing placeholders and details where
        necessary, and then "merging" the final graph. This means that the
        objects that are passed into methods are copied over to new instances
        which are then returned.
        The original objects should be discarded.

        @param obj      An entity or an unloaded reference to an entity. Not
                        null.
        @type obj       ObjectI
        """
        u = self.getUpdateService()
        u.saveObject(obj, self.SERVICE_OPTS)

    def saveArray(self, objs):
        """
        Provide method for directly updating list of object graphs. Act
        recursively on the entire object graph, replacing placeholders and
        details where necessary, and then "merging" the final graph. This
        means that the objects that are passed into methods are copied over to
        new instances which are then returned.
        The original objects should be discarded.

        @param obj      List of entities or an unloaded references to an
                        entity. Not null.
        @type obj       L{ObjectI}
        """
        u = self.getUpdateService()
        u.saveArray(objs, self.SERVICE_OPTS)

    def saveAndReturnObject(self, obj):
        """
        Provide method for directly updating object graphs and return it. Act
        recursively on the entire object graph, replacing placeholders and
        details where necessary, and then "merging" the final graph. This
        means that the objects that are passed into methods are copied over to
        new instances which are then returned.
        The original objects should be discarded.

        @param obj      An entity or an unloaded reference to an entity. Not
                        null.
        @type obj       ObjectI
        @return         Saved object
        @rtype          ObjectI
        """
        u = self.getUpdateService()
        res = u.saveAndReturnObject(obj, self.SERVICE_OPTS)
        res.unload()
        obj = omero.gateway.BlitzObjectWrapper(self, res)
        return obj

    def saveAndReturnId(self, obj):
        """
        Provide method for directly updating object graphs and return ID. Act
        recursively on the entire object graph, replacing placeholders and
        details where necessary, and then "merging" the final graph. This
        means that the objects that are passed into methods are copied over to
        new instances which are then returned.
        The original objects should be discarded.

        @param obj      An entity or an unloaded reference to an entity. Not
                        null.
        @type obj       ObjectI
        @return         ID of saved object
        @rtype          Long
        """
        u = self.getUpdateService()
        res = u.saveAndReturnObject(obj, self.SERVICE_OPTS)
        res.unload()
        return res.id.val

    def saveAndReturnFile(self, binary, oFile_id):
        """
        Provide method for directly updating a file object and return binary.
        Assumes that the checksum algorithm used for file integrity
        verification is SHA-1.

        @param binary       Binary. Not null.
        @type binary        String
        @param oFile_id     File Id in order to manage the state of the
                            service. Not null.
        @type oFile_id      Long
        @return             Shallow copy of file.
        """

        store = self.createRawFileStore()
        store.setFileId(oFile_id, self.SERVICE_OPTS)
        pos = 0
        rlen = 0
        hash = hash_sha1()

        for chunk in binary.chunks():
            rlen = len(chunk)
            store.write(chunk, pos, rlen)
            hash.update(chunk)
            pos = pos + rlen
        ofile = store.save(self.SERVICE_OPTS)
        store.close()

        serverhash = ofile.hash.val
        clienthash = hash.hexdigest()

        if serverhash != clienthash:
            msg = ("SHA-1 checksums do not match in file upload: client has"
                   " %s but server has %s" % (clienthash, serverhash))
            logger.error(msg)
            raise Exception(msg)

        return ofile

    ##############################################
    # IShare

    def getShare(self, oid):
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
        Returns a map from share id to the count of total members (including
        the owner). This is represented by ome.model.meta.ShareMember links.

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
            if isinstance(e, omero.model.ImageI):
                try:
                    obj = omero.gateway.ImageWrapper(self, e)
                except omero.ValidationException:
                    # If Object deleted, simply return placeholder
                    # ID used to generate placeholder thumbnail
                    obj = {'id': e.id.val}
                yield obj

    def getComments(self, share_id):
        """
        Looks up all comments which belong to the share, wrapped in object
        wrapper

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
        Get a single set containing the login names of the users as well email
        addresses for guests.

        @param share_id:    share ID
        @type share_id:     Long
        @return:            List of usernames and e-mail addresses
        @rtype:             List of Strings
        """

        sh = self.getShareService()
        return sh.getAllUsers(long(share_id))

    def addComment(self, host, share_id, comment):
        sh = self.getShareService()
        new_cm = sh.addComment(long(share_id), str(comment))

        self.getShare(long(share_id))
        sh_type = sh.getContentSize(share_id) > 0 and "share" or "discussion"
        subject = "OMERO.%s %s" % (sh_type, share_id)
        body = "%s added new comment.\n\n%s\n\n%s URL: %s\n" % (
            self.getUser().getFullName(), str(comment), sh_type.title(), host)
        sh.notifyMembersOfShare(long(share_id), subject, body, False)
        return CommentAnnotationWrapper(self, new_cm)

    def removeImage(self, share_id, image_id):
        sh = self.getShareService()
        img = self.getObject("Image", image_id)
        sh.removeObject(long(share_id), img._obj)

    def createShare(self, host, images, message, members, enable,
                    expiration=None):
        sh = self.getShareService()
        items = [i._obj for i in images]
        ms = [m._obj for m in members]
        sid = sh.createShare(
            message, rtime(expiration), items, ms, [], enable)
        sh_type = len(images) > 0 and "share" or "discussion"
        body = "%s\n\n%s URL: %s\n" % (message, sh_type.title(), host)
        subject = "OMERO.%s %s" % (sh_type, sid)
        sh.notifyMembersOfShare(sid, subject, body, False)
        return sid

    def updateShareOrDiscussion(self, host, share_id, message, add_members,
                                rm_members, enable, expiration=None):
        sh = self.getShareService()
        sh.setDescription(long(share_id), message)
        sh.setExpiration(long(share_id), rtime(expiration))
        sh.setActive(long(share_id), enable)
        sh_type = sh.getContentSize(share_id) > 0 and "share" or "discussion"
        if len(add_members) > 0:
            sh.addUsers(long(share_id), add_members)
            share = self.getShare(long(share_id))
            body = "%s\n\n%s URL: %s\n" % (
                share.message, sh_type.title(), host)
            subject = "OMERO.%s %s" % (sh_type, share_id)
            sh.notifyMembersOfShare(share_id, subject, body, False)
        if len(rm_members) > 0:
            sh.removeUsers(long(share_id), rm_members)
        return share_id

    ##############################################
    # History methods                        ##

    # def getLastAcquiredImages (self):
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

    def listLastImportedImages(self):
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
        for e in tm.getMostRecentObjects(
                ['Image'], p, False, self.SERVICE_OPTS)["Image"]:
            yield ImageWrapper(self, e)

    def listMostRecentShares(self):
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
        for e in tm.getMostRecentShareCommentLinks(p, self.SERVICE_OPTS):
            yield ShareWrapper(self, e.parent)

    def listMostRecentShareComments(self):
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
        for e in tm.getMostRecentShareCommentLinks(p, self.SERVICE_OPTS):
            yield AnnotationWrapper(
                self, e.child, link=ShareWrapper(self, e.parent))

    def listMostRecentComments(self):
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
        for e in tm.getMostRecentAnnotationLinks(
                None, ['CommentAnnotation'], None, p, self.SERVICE_OPTS):
            yield omero.gateway.BlitzObjectWrapper(self, e)

    def listMostRecentTags(self):
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
        # f.ownerId = rlong(self.getEventContext().userId)
        f.groupId = rlong(self.getEventContext().groupId)
        f.limit = rint(200)
        p.theFilter = f
        for e in tm.getMostRecentAnnotationLinks(
                None, ['TagAnnotation'], None, p, self.SERVICE_OPTS):
            yield omero.gateway.BlitzObjectWrapper(self, e.child)

    def getDataByPeriod(self, start, end, eid, otype=None, page=None):
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

        p = omero.sys.ParametersI()
        p.exp(eid)
        if page is not None:
            p.page(((int(page)-1)*settings.PAGE), settings.PAGE)
        else:
            p.page(None, 100)

        im_list = list()
        ds_list = list()
        pr_list = list()

        if otype is not None and otype in ("Image", "Dataset", "Project"):
            otype = otype.title()
            for e in tm.getByPeriod(
                    [otype], rtime(long(start)), rtime(long(end)), p, True,
                    self.SERVICE_OPTS)[otype]:
                wrapper = KNOWN_WRAPPERS.get(otype.title(), None)
                im_list.append(wrapper(self, e))
        else:
            res = tm.getByPeriod(
                ['Image', 'Dataset', 'Project'], rtime(long(start)),
                rtime(long(end)), p, True, self.SERVICE_OPTS)
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
        return {'project': pr_list, 'dataset': ds_list, 'image': im_list}

    def countDataByPeriod(self, start, end, eid, otype=None):
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
        # f.groupId = rlong(self.getEventContext().groupId)
        p.theFilter = f
        if otype == 'image':
            return tm.countByPeriod(
                ['Image'], rtime(long(start)), rtime(long(end)), p,
                self.SERVICE_OPTS)['Image']
        elif otype == 'dataset':
            return tm.countByPeriod(
                ['Dataset'], rtime(long(start)), rtime(long(end)), p,
                self.SERVICE_OPTS)['Dataset']
        elif otype == 'project':
            return tm.countByPeriod(
                ['Project'], rtime(long(start)), rtime(long(end)), p,
                self.SERVICE_OPTS)['Project']
        else:
            c = tm.countByPeriod(
                ['Image', 'Dataset', 'Project'], rtime(long(start)),
                rtime(long(end)), p, self.SERVICE_OPTS)
            return c['Image']+c['Dataset']+c['Project']

    def getEventsByPeriod(self, start, end, eid):
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
        try:
            f.groupId = rlong(self.SERVICE_OPTS.getOmeroGroup())
        except:
            f.groupId = rlong(self.getEventContext().groupId)
        f.ownerId = rlong(eid or self.getEventContext().userId)
        p.theFilter = f
        service_opts = self.createServiceOptsDict()
        service_opts.setOmeroGroup(str(f.groupId.val))
        return tm.getEventLogsByPeriod(rtime(start), rtime(end), p,
                                       service_opts)
        # yield EventLogWrapper(self, e)

    def regroupFilesets(self, dsIds, fsIds):
        """
        For each Fileset, make sure all the Images are in the same Dataset
        as each other.
        We choose a 'target' Dataset that already has one of the Fileset
        Images in it, and is in the dsIds list.
        This method is used when preparing to chgrp the specified datasets
        """

        for fileset in self.getObjects("Fileset", fsIds):
            gid = fileset.getDetails().group.id.val
            self.SERVICE_OPTS.setOmeroGroup(gid)
            # all these need to be in same Dataset
            fsImgs = fileset.copyImages()
            # find one of the Datasets (obj_ids) that contains one of these
            # images...
            target_ds = None
            for i in fsImgs:
                for d in i.listParents():
                    if d.id in dsIds:
                        target_ds = d.id
                        break
                if target_ds is not None:
                    break
            # move ALL fs images into that Dataset
            for i in fsImgs:
                correct_dataset = False
                for plink in i.getParentLinks():
                    if plink.parent.id != target_ds:
                        self.deleteObjectDirect(plink._obj)
                    else:
                        correct_dataset = True
                if not correct_dataset:
                    link = omero.model.DatasetImageLinkI()
                    link.setChild(omero.model.ImageI(i.getId(), False))
                    link.setParent(omero.model.DatasetI(target_ds, False))
                    self.saveObject(link)

        # conn.chgrpObjects(dtype, obj_ids, group_id, container_id)

omero.gateway.BlitzGateway = OmeroWebGateway


class OmeroWebSafeCallWrapper(OmeroGatewaySafeCallWrapper):  # pragma: no cover
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

    def countParents(self):
        l = self.listParents()
        if l is not None:
            return len(l)

    def countAnnotations(self):
        """
        Count on annotations linked to the object and set the value
        on the custom fiels 'annotation_counter'.

        @return     Counter
        """

        if self.annotation_counter is not None:
            return self.annotation_counter
        else:
            container = self._conn.getContainerService()
            m = container.getCollectionCount(
                self._obj.__class__.__name__,
                type(self._obj).ANNOTATIONLINKS, [self._oid], None)
            if m[self._oid] > 0:
                self.annotation_counter = m[self._oid]
                return self.annotation_counter
            else:
                return None

    def getPermissions(self):
        p = None
        if self.details.getPermissions() is None:
            return 'unknown'
        else:
            p = self.details.getPermissions()
        if p.isGroupWrite():
            flag = 'Read-Write'
        elif p.isGroupAnnotate():
            flag = 'Read-Annotate'
        elif p.isGroupRead():
            flag = 'Read-Only'
        elif p.isUserRead():
            flag = 'Private'
        else:
            flag = p
        return flag

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
                for v in range(0, len(self.name), 30):
                    splited.append(self.name[v:v+30]+"\n")
                return "".join(splited)
        except:
            logger.info(traceback.format_exc())
            return self.name

    def getPermsCss(self):
        """
        Returns a string that can be used as classes on an html element to
        indicate the permissions flags of the object. E.g. "canEdit canLink"
        Flags/classes are canEdit, canAnnotate, canLink, canDelete
        """
        flags = []
        if self.canEdit():
            flags.append("canEdit")
        if self.canAnnotate():
            flags.append("canAnnotate")
        if self.canLink():
            flags.append("canLink")
        if self.canDelete():
            flags.append("canDelete")
        if self.canChgrp():
            flags.append("canChgrp")
        return " ".join(flags)

    def setRating(self, rating):
        """
        Add a rating (long) annotation to the object, or update an existing
        rating.
        If rating is zero, we remove any existing rating annotation.
        """
        rating_ns = omero.constants.metadata.NSINSIGHTRATING
        parent_type = self.OMERO_CLASS
        userid = self._conn.getUserId()

        ratingAnns = list(self._conn.getAnnotationLinks(
            parent_type, parent_ids=[self.id], ns=rating_ns))
        # filter for links I own
        ratingAnns = [r for r in ratingAnns
                      if r.getDetails().owner.id.val == userid]
        ratingLink = ratingAnns and ratingAnns[0] or None

        def getLinkCount(annId):
            linkCount = 0
            for parentType in ["Project", "Dataset", "Image", "Screen",
                               "Plate", "PlateAcquisition", "Well"]:
                annLinks = list(self._conn.getAnnotationLinks(
                    parentType, ann_ids=[annId]))
                linkCount += len(annLinks)
            return linkCount

        def addRating(value):
            if value == 0:
                return
            ratingAnn = omero.model.LongAnnotationI()
            ratingAnn.setLongValue(rlong(value))
            ratingAnn.setNs(rstring(rating_ns))
            self._conn.SERVICE_OPTS.setOmeroGroup(
                self.getDetails().group.id.val)
            ratingAnn = self._conn.getUpdateService().saveAndReturnObject(
                ratingAnn, self._conn.SERVICE_OPTS)
            self.linkAnnotation(AnnotationWrapper(self._conn, ratingAnn))

        if ratingLink is not None:
            ratingAnn = ratingLink.getChild()
            # if rating is only used by this object, we can update or delete
            if getLinkCount(ratingAnn.id) == 1:
                if rating > 0:
                    ratingAnn.setLongValue(rlong(rating))
                    ratingAnn.save()
                else:
                    self._conn.deleteObjectDirect(ratingLink._obj)
                    self._conn.deleteObjectDirect(ratingAnn._obj)
            # otherwise, unlink and create a new rating
            else:
                self._conn.deleteObjectDirect(ratingLink._obj)
                addRating(rating)
        else:
            addRating(rating)


class ExperimenterWrapper(OmeroWebObjectWrapper,
                          omero.gateway.ExperimenterWrapper):
    """
    omero_model_ExperimenterI class wrapper overwrite
    omero.gateway.ExperimenterWrapper and extend OmeroWebObjectWrapper.
    """

    ldapUser = None

    def __prepare__(self, **kwargs):
        super(ExperimenterWrapper, self).__prepare__(**kwargs)
        if 'ldapUser' in kwargs:
            self.annotation_counter = kwargs['ldapUser']

    def isEditable(self):
        return self.omeName.lower() not in ('guest')

    def isLdapUser(self):
        """
        Return DN of the specific experimenter if uses LDAP authentication
        (has set dn on password table) or None.
        @param eid: experimenter ID
        @type eid: L{Long}
        @return: Distinguished Name
        @rtype: String
        """

        if self.ldap:
            admin_serv = self._conn.getAdminService()
            self.ldapUser = admin_serv.lookupLdapAuthExperimenter(self.id)
        return self.ldapUser

    def getDefaultGroup(self):
        geMap = self.copyGroupExperimenterMap()
        if self.sizeOfGroupExperimenterMap() > 0 and geMap[0] is not None:
            return ExperimenterGroupWrapper(self._conn, geMap[0].parent)
        return None

    def getOtherGroups(self, excluded_names=("user", "guest"),
                       excluded_ids=list()):
        for gem in self.copyGroupExperimenterMap():
            if gem is None:
                continue
            flag = False
            if gem.parent.name.val in excluded_names:
                flag = True
            if gem.parent.id.val in excluded_ids:
                flag = True
            if not flag:
                yield ExperimenterGroupWrapper(self._conn, gem.parent)

omero.gateway.ExperimenterWrapper = ExperimenterWrapper


class ExperimenterGroupWrapper(OmeroWebObjectWrapper,
                               omero.gateway.ExperimenterGroupWrapper):
    """
    omero_model_ExperimenterGroupI class wrapper overwrite
    omero.gateway.ExperimenterGroupWrapper
    and extend OmeroWebObjectWrapper.
    """

    def isEditable(self):
        return self.name.lower() not in ('guest', 'user')

    def loadLeadersAndMembers(self):
        """
        Returns lists of 'leaders' and 'members' of the specified group
        (default is current group) as a dict with those keys.

        @return:    {'leaders': list L{ExperimenterWrapper},
                     'colleagues': list L{ExperimenterWrapper}}
        @rtype:     dict
        """
        self.leaders, self.colleagues = self.groupSummary()
        self.leaders.sort(key=lambda x: x.getLastName().lower())
        self.colleagues.sort(key=lambda x: x.getLastName().lower())
        # Only show 'All Members' option if configured, and we're not in a
        # private group
        if (self.details.permissions.isGroupRead() or self._conn.isAdmin() or
                self.isOwner()):
            self.all = True

    def getOwners(self):
        for gem in self.copyGroupExperimenterMap():
            if gem is None:
                continue
            if gem.owner.val:
                yield ExperimenterWrapper(self._conn, gem.child)

    def getOwnersNames(self):
        owners = list()
        for e in self.getOwners():
            owners.append(e.getFullName())
        return ", ".join(owners)

    def getMembers(self, excluded_omename=list(), excluded_ids=list()):
        for gem in self.copyGroupExperimenterMap():
            if gem is None:
                continue
            flag = False
            if gem.child.omeName.val in excluded_omename:
                flag = True
            if gem.parent.id.val in excluded_ids:
                flag = True
            if not flag:
                yield ExperimenterWrapper(self._conn, gem.child)

    def isOwner(self):
        """ Returns True if current user is Owner of this group """
        return self.getId() in self._conn.getEventContext().leaderOfGroups

    def isLocked(self):
        if self.name == "user":
            return True
        elif self.name == "system":
            return True
        elif self.name == "guest":
            return True
        else:
            False

omero.gateway.ExperimenterGroupWrapper = ExperimenterGroupWrapper


class ProjectWrapper(OmeroWebObjectWrapper, omero.gateway.ProjectWrapper):
    """
    omero_model_ProjectI class wrapper overwrite omero.gateway.ProjectWrapper
    and extend OmeroWebObjectWrapper.
    """

    annotation_counter = None

    def __prepare__(self, **kwargs):
        super(ProjectWrapper, self).__prepare__(**kwargs)
        if 'annotation_counter' in kwargs:
            self.annotation_counter = kwargs['annotation_counter']

omero.gateway.ProjectWrapper = ProjectWrapper


class DatasetWrapper(OmeroWebObjectWrapper, omero.gateway.DatasetWrapper):
    """
    omero_model_DatasetI class wrapper overwrite omero.gateway.DatasetWrapper
    and extends OmeroWebObjectWrapper.
    """

    annotation_counter = None

    def __prepare__(self, **kwargs):
        super(DatasetWrapper, self).__prepare__(**kwargs)
        if 'annotation_counter' in kwargs:
            self.annotation_counter = kwargs['annotation_counter']
        if 'link' in kwargs:
            self.link = 'link' in kwargs and kwargs['link'] or None

omero.gateway.DatasetWrapper = DatasetWrapper


class ImageWrapper (OmeroWebObjectWrapper,
                    omero.gateway.ImageWrapper):
    """
    omero_model_ImageI class wrapper overwrite omero.gateway.ImageWrapper
    and extends OmeroWebObjectWrapper.
    """

    annotation_counter = None

    def __prepare__(self, **kwargs):
        super(ImageWrapper, self).__prepare__(**kwargs)
        if 'annotation_counter' in kwargs:
            self.annotation_counter = kwargs['annotation_counter']
        if 'link' in kwargs:
            self.link = 'link' in kwargs and kwargs['link'] or None

    """
    This override standard omero.gateway.ImageWrapper.getChannels
    and catch exceptions.
    """

    def getPixelSizeXMicrons(self):
        """
        Helper for calling getPixelSizeX(units="MICROMETER") in templates
        """
        size = self.getPixelSizeX(units="MICROMETER")
        if size is None:
            return 0
        return size.getValue()

    def getPixelSizeYMicrons(self):
        """
        Helper for calling getPixelSizeX(units="MICROMETER") in templates
        """
        size = self.getPixelSizeY(units="MICROMETER")
        if size is None:
            return 0
        return size.getValue()

    def getPixelSizeZMicrons(self):
        """
        Helper for calling getPixelSizeX(units="MICROMETER") in templates
        """
        size = self.getPixelSizeZ(units="MICROMETER")
        if size is None:
            return 0
        return size.getValue()

    def getChannels(self, *args, **kwargs):
        try:
            return super(ImageWrapper, self).getChannels(*args, **kwargs)
        except Exception:
            logger.error('Failed to load channels:', exc_info=True)
            return None

    def showOriginalFilePaths(self):
        """
        This determines whether we want to show the paths of
        Original Imported Files.
        """
        return super(ImageWrapper, self).countFilesetFiles() > 0

    def getFilesetImages(self):
        """
        Returns a list of the 'sibling' images (including this one) that
        belong to this image's Fileset. Sorted by name.
        If Image has no Fileset, return an empty list.

        @rtype:     List of {ImageWrapper}
        """
        fileset = self.getFileset()
        if fileset is not None:
            fsImgs = fileset.copyImages()
            fsImgs.sort(key=lambda x: x.getName().lower())
            return fsImgs
        return []

    def getInplaceImportCmd(self):
        """
        Returns the command used to do import transfer for this image,
        E.g. 'ln', or empty string if not in-place imported.
        """
        inplace = self.getInplaceImport()
        if inplace and inplace in TRANSFERS:
            return TRANSFERS[inplace]
        return ""


omero.gateway.ImageWrapper = ImageWrapper


class PlateWrapper(OmeroWebObjectWrapper, omero.gateway.PlateWrapper):

    """
    omero_model_PlateI class wrapper overwrite omero.gateway.PlateWrapper
    and extends OmeroWebObjectWrapper.
    """

    annotation_counter = None

    def __prepare__(self, **kwargs):
        super(PlateWrapper, self).__prepare__(**kwargs)
        if 'annotation_counter' in kwargs:
            self.annotation_counter = kwargs['annotation_counter']
        if 'link' in kwargs:
            self.link = 'link' in kwargs and kwargs['link'] or None

omero.gateway.PlateWrapper = PlateWrapper


class WellWrapper(OmeroWebObjectWrapper, omero.gateway.WellWrapper):
    """
    omero_model_ImageI class wrapper overwrite omero.gateway.ImageWrapper
    and extends OmeroWebObjectWrapper.
    """

    annotation_counter = None

    def __prepare__(self, **kwargs):
        super(WellWrapper, self).__prepare__(**kwargs)
        if 'annotation_counter' in kwargs:
            self.annotation_counter = kwargs['annotation_counter']
        if 'link' in kwargs:
            self.link = 'link' in kwargs and kwargs['link'] or None

omero.gateway.WellWrapper = WellWrapper


class PlateAcquisitionWrapper(OmeroWebObjectWrapper,
                              omero.gateway.PlateAcquisitionWrapper):

    """
    omero_model_PlateI class wrapper overwrite omero.gateway.PlateWrapper
    and extends OmeroWebObjectWrapper.
    """

    annotation_counter = None

    def __prepare__(self, **kwargs):
        super(PlateAcquisitionWrapper, self).__prepare__(**kwargs)
        if 'annotation_counter' in kwargs:
            self.annotation_counter = kwargs['annotation_counter']

omero.gateway.PlateAcquisitionWrapper = PlateAcquisitionWrapper


class ScreenWrapper (OmeroWebObjectWrapper, omero.gateway.ScreenWrapper):
    """
    omero_model_ScreenI class wrapper overwrite omero.gateway.ScreenWrapper
    and extends OmeroWebObjectWrapper.
    """

    annotation_counter = None

    def __prepare__(self, **kwargs):
        super(ScreenWrapper, self).__prepare__(**kwargs)
        if 'annotation_counter' in kwargs:
            self.annotation_counter = kwargs['annotation_counter']

omero.gateway.ScreenWrapper = ScreenWrapper


class EventLogWrapper (omero.gateway.BlitzObjectWrapper):
    """
    omero_model_EventLogI class wrapper extends
    omero.gateway.BlitzObjectWrapper.
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
        """
        Gets the end date for the share

        @return:    End Date-time
        @rtype:     datetime object
        """

        # workaround for problem of year 2038
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

    def isExpired(self):
        """
        Returns True if we are past the end date of the share

        @return:    True if share expired
        @rtype:     Boolean
        """

        # workaround for problem of year 2038
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

        return omero.gateway.ExperimenterWrapper(self._conn, self.owner)

omero.gateway.refreshWrappers()
