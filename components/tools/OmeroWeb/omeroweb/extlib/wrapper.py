#!/usr/bin/env python
# 
# Wrapper
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

logger = logging.getLogger('wrapper')

try:
    import Image,ImageDraw
except:
    logger.error("You need to install the Python Imaging Library. Get it at http://www.pythonware.com/products/pil/")
    logger.error(traceback.format_exc())
from StringIO import StringIO

import time
from datetime import datetime
from types import IntType, ListType, TupleType, UnicodeType, StringType

from django.utils.translation import ugettext as _
from django.conf import settings
from django.core.mail import send_mail
from django.core.mail import EmailMultiAlternatives

import Ice
import Glacier2
import omero.gateway

import omero_api_IScript_ice
from omero.rtypes import *
from omero.model import FileAnnotationI, TagAnnotationI, DatasetI, ProjectI, ImageI, \
                        DetectorI, FilterI, ObjectiveI, InstrumentI, LaserI
from omero.sys import ParametersI
from omero.gateway import AnnotationWrapper, DatasetWrapper, ProjectWrapper

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
    
    def countChildren2 (self):
        #return len(list(self.listChildren()))
        logger.debug(str(self)+'.countChildren2')
        if self.child_counter is not None:
            return self.child_counter
        else:
            try:
                a = self.countChildren()
            except:
                logger.error(traceback.format_exc())
                a = None
                
            return a
    
    '''def listAnnotations (self):
        #container = self._conn.getContainerService()
        meta = self._conn.getMetadataService()
        self.annotations = meta.loadAnnotations(self._obj.__class__.__name__, [self._oid], None, None, None).get(self._oid, [])
        for ann in self.annotations:
            yield AnnotationWrapper(self._conn, ann)'''
    
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
    
    def isLeaded(self):
        if self._obj.details.group.id.val in self._conn.getEventContext().leaderOfGroups:
            return True
        return False
    
    def isEditable(self):
        if self.isOwned() or not self.isReadOnly():
            return True
        return False
    
    def isPublic(self):
        if self._obj.details.permissions.isWorldRead():
            return True
        return False
    
    def isShared(self):
        if self._obj.details.permissions.isGroupRead():
            return True
        return False
    
    def isPrivate(self):
        if self._obj.details.permissions.isUserRead():
            return True
        return False
    
    def isReadOnly(self):
        if self.isPublic() and not self._obj.details.permissions.isWorldWrite():
            return True
        elif self.isShared() and not self._obj.details.permissions.isGroupWrite():
            return True
        elif self.isPrivate() and not self._obj.details.permissions.isUserWrite():
            return True
        return False
    
    def warpName(self):
        try: 
            l = len(self.name) 
            if l < 27: 
                return self.name 
            elif l >= 27: 
                splited = [] 
                for v in range(0,len(self.name),27): 
                    splited.append(self.name[v:v+27]+"\n") 
                return "".join(splited) 
        except: 
            logger.info(traceback.format_exc()) 
            return self.name
        
    def shortName(self):
        try:
            name = self._obj.name.val
            l = len(name)
            if l < 38:
                return name
            return "..." + name[l - 35:]
        except:
            logger.info(traceback.format_exc())
            return self._obj.name.val
    
    def shortName2(self):
        try:
            name = self._obj.name.val
            l = len(name)
            if l < 68:
                return name
            return "..." + name[l - 65:]
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
            if l <= 28:
                return desc.val
            return desc.val[:28] + "..."
        except:
            logger.info(traceback.format_exc())
            return self._obj.description.val

class ExperimenterWrapper (OmeroWebObjectWrapper, omero.gateway.ExperimenterWrapper):

    def __bstrap__ (self):
        self.OMERO_CLASS = 'Experimenter'
        self.LINK_CLASS = "copyGroupExperimenterMap"
        self.CHILD_WRAPPER_CLASS = None
        self.PARENT_WRAPPER_CLASS = 'ExperimenterGroupWrapper'
    
    def isAdmin(self):
        for ob in self._obj.copyGroupExperimenterMap():
            if ob.parent.name.val == "system":
                return True
        return False
    
    def isActive(self):
        for ob in self._obj.copyGroupExperimenterMap():
            if ob.parent.name.val == "user":
                return True
        return False
    
    def isGuest(self):
        for ob in self._obj.copyGroupExperimenterMap():
            if ob.parent.name.val == "guest":
                return True
        return False
    
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
    
    def getInitialName(self):
        try:
            if self.firstName is not None and self.lastName is not None:
                name = "%s. %s" % (self.firstName[:1], self.lastName)
            else:
                name = self.omeName
            return name
        except:
            logger.error(traceback.format_exc())
            return _("Unknown name")

omero.gateway.ExperimenterWrapper = ExperimenterWrapper

class ExperimenterGroupWrapper (OmeroWebObjectWrapper, omero.gateway.ExperimenterGroupWrapper):

    def __bstrap__ (self):
        self.OMERO_CLASS = 'ExperimenterGroup'
        self.LINK_CLASS = "copyGroupExperimenterMap"
        self.CHILD_WRAPPER_CLASS = 'ExperimenterWrapper'
        self.PARENT_WRAPPER_CLASS = None

omero.gateway.ExperimenterGroupWrapper = ExperimenterGroupWrapper

class ScriptWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    pass

class AnnotationLinkWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):

    def getAnnotation(self):
        return AnnotationWrapper(self, self.child)

class ImageStageLabelWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ImageWrapper (OmeroWebObjectWrapper, omero.gateway.ImageWrapper):
    
    def getThumbnailOrDefault (self, size=(120,120)):
        rv = super(omero.gateway.ImageWrapper, self).getThumbnail(size=size)
        if rv is None:
            try:
                rv = self.defaultThumbnail(size)
            except Exception, e:
                logger.info(traceback.format_exc())
                raise e
        return rv
    
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

    def getStageLabel (self):
        if self._obj.stageLabel is None:
            return None
        else:
            return ImageStageLabelWrapper(self._conn, self._obj.stageLabel)

omero.gateway.ImageWrapper = ImageWrapper

class DatasetImageLinkWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class ProjectDatasetLinkWrapper (omero.gateway.BlitzObjectWrapper):
    pass

class PlateWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Plate'
        self.LINK_CLASS = None
        self.CHILD_WRAPPER_CLASS = None
        self.PARENT_WRAPPER_CLASS = 'ScreenWrapper'

omero.gateway.PlateWrapper = PlateWrapper
    
class ScreenWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Screen'
        self.LINK_CLASS = "ScreenPlateLink"
        self.CHILD_WRAPPER_CLASS = 'PlateWrapper'
        self.PARENT_WRAPPER_CLASS = None
        
omero.gateway.ScreenWrapper = ScreenWrapper

class WellWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Well'
        self.LINK_CLASS = "WellSample"
        self.CHILD_WRAPPER_CLASS = "ImageWrapper"
        self.PARENT_WRAPPER_CLASS = 'PlateWrapper'
    
    def __prepare__ (self, **kwargs):
        try:
            self.index = int(kwargs['index'])
        except:
            self.index = 0
    
    def isWellSample (self):
        """ return boolean if object exist """
        if getattr(self, 'isWellSamplesLoaded')():
            childnodes = getattr(self, 'copyWellSamples')()
            logger.debug('listChildren for %s %d: already loaded, %d samples' % (self.OMERO_CLASS, self.getId(), len(childnodes)))
            if len(childnodes) > 0:
                return True
        return False
    
    def countWellSample (self):
        """ return boolean if object exist """
        if getattr(self, 'isWellSamplesLoaded')():
            childnodes = getattr(self, 'copyWellSamples')()
            logger.debug('countChildren for %s %d: already loaded, %d samples' % (self.OMERO_CLASS, self.getId(), len(childnodes)))
            size = len(childnodes)
            if size > 0:
                return size
        return 0
    
    def selectedWellSample (self):
        """ return a wrapped child object """
        if getattr(self, 'isWellSamplesLoaded')():
            childnodes = getattr(self, 'copyWellSamples')()
            logger.debug('listSelectedChildren for %s %d: already loaded, %d samples' % (self.OMERO_CLASS, self.getId(), len(childnodes)))
            if len(childnodes) > 0:
                return WellSampleWrapper(self._conn, childnodes[self.index])
        return None
    
    def loadWellSamples (self):
        """ return a generator yielding child objects """
        if getattr(self, 'isWellSamplesLoaded')():
            childnodes = getattr(self, 'copyWellSamples')()
            logger.debug('listChildren for %s %d: already loaded, %d samples' % (self.OMERO_CLASS, self.getId(), len(childnodes)))
            for ch in childnodes:
                yield WellSampleWrapper(self._conn, ch)
    
    def plate(self):
        return PlateWrapper(self._conn, self._obj.plate)
    
WellWrapper = WellWrapper

class WellSampleWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
    def image(self):
        return ImageWrapper(self._conn, self._obj.image)

WellSampleWrapper = WellSampleWrapper

class ShareWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
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

