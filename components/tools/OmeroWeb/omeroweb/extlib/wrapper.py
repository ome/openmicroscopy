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
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError;
    try:
        import Image, ImageDraw # see ticket:2597
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
from omero.gateway import AnnotationWrapper, BlitzObjectWrapper

class OmeroWebObjectWrapper (object):
    
    def listChildrenWithLinks (self):
        """
        Lists available child objects.

        @return: Generator yielding child objects and link to parent.
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

    def getAnnotation(self):
        return AnnotationWrapper(self, self.child)

class ProjectWrapper (OmeroWebObjectWrapper, omero.gateway.ProjectWrapper): 

    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
 	 
omero.gateway.ProjectWrapper = ProjectWrapper 
 	 
class DatasetWrapper (OmeroWebObjectWrapper, omero.gateway.DatasetWrapper): 
    
    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None    
	 
omero.gateway.DatasetWrapper = DatasetWrapper

class ImageWrapper (OmeroWebObjectWrapper, omero.gateway.ImageWrapper):
    
    annotation_counter = None
    
    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None
    
    def getThumbnailOrDefault (self, size=(120,120)):
        rv = super(omero.gateway.ImageWrapper, self).getThumbnail(size=size)
        if rv is None:
            try:
                rv = self._conn.defaultThumbnail(size)
            except Exception, e:
                logger.info(traceback.format_exc())
                raise e
        return rv

omero.gateway.ImageWrapper = ImageWrapper

class PlateWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
    annotation_counter = None
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Plate'
        self.LINK_CLASS = None
        self.CHILD_WRAPPER_CLASS = None
        self.PARENT_WRAPPER_CLASS = 'ScreenWrapper'

    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']
        if kwargs.has_key('link'):
            self.link = kwargs.has_key('link') and kwargs['link'] or None

omero.gateway.PlateWrapper = PlateWrapper

class ScreenWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
    annotation_counter = None
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'Screen'
        self.LINK_CLASS = "ScreenPlateLink"
        self.CHILD_WRAPPER_CLASS = 'PlateWrapper'
        self.PARENT_WRAPPER_CLASS = None

    def __prepare__ (self, **kwargs):
        if kwargs.has_key('annotation_counter'):
            self.annotation_counter = kwargs['annotation_counter']

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

omero.gateway.WellWrapper = WellWrapper

class WellSampleWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    
    def __bstrap__ (self):
        self.OMERO_CLASS = 'WellSample'
        self.CHILD_WRAPPER_CLASS = "ImageWrapper"
        self.PARENT_WRAPPER_CLASS = 'WellWrapper'
        
    def image(self):
        return ImageWrapper(self._conn, self._obj.image)

omero.gateway.WellSampleWrapper = WellSampleWrapper

class ShareWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
                
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
    
    def getStartDate(self):
        return datetime.fromtimestamp(self.getStarted().val/1000)
        
    def getExpirationDate(self):
        try:
            return datetime.fromtimestamp((self.getStarted().val+self.getTimeToLive().val)/1000)
        except ValueError:
            pass
        return None
    
    def isExpired(self):
        try:
            if (self.getStarted().val+self.getTimeToLive().val)/1000 <= time.time():
                return True
            else:
                return False
        except:
            return True
    
    def isOwned(self):
        if self.owner.id.val == self._conn.getEventContext().userId:
            return True
        else:
            return False
    
    def getOwner(self):
        return omero.gateway.ExperimenterWrapper(self, self.owner)
    
class ShareContentWrapper (OmeroWebObjectWrapper, omero.gateway.BlitzObjectWrapper):
    pass

class ShareCommentWrapper (OmeroWebObjectWrapper, omero.gateway.AnnotationWrapper):
    pass
    
class SessionAnnotationLinkWrapper (omero.gateway.BlitzObjectWrapper):
    
    def getComment(self):
        return ShareCommentWrapper(self._conn, self.child)
    
    def getShare(self):
        return ShareWrapper(self._conn, self.parent)
    
class EventLogWrapper (omero.gateway.BlitzObjectWrapper):
    LINK_CLASS = "EventLog"

