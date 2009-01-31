#!/usr/bin/env python
# 
# 
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
# 
# Version: 1.0
#

from django.conf import settings

import omero
from omero.rtypes import *

from omero_model_CommentAnnotationI import CommentAnnotationI
from omero_model_UriAnnotationI import UriAnnotationI
from omero_model_FileAnnotationI import FileAnnotationI

from webclient.controller import BaseController

class BaseAnnotation(BaseController):
    
    comment = None
    url = None
    tag = None
    
    def __init__(self, conn, o_type=None, oid=None, **kw):
        BaseController.__init__(self, conn)
        if oid is not None:
            if o_type == "comment":
                self.comment = self.conn.getCommentAnnotation(long(oid))
            elif o_type == "url":
                self.url = self.conn.getUriAnnotation(long(oid))
            elif o_type == "tag":
                self.tag = self.conn.getTagAnnotation(long(oid))
    
    def buildBreadcrumb(self, action):
        if self.comment is not None:
            self.eContext['breadcrumb'] = ['Edit: comment']
        elif self.url is not None:
            self.eContext['breadcrumb'] = ['Edit: URL']
        elif self.tag is not None:
            self.eContext['breadcrumb'] = ['Edit: %s' % self.tag.breadcrumbName()]
    
    def saveCommentAnnotation(self, content):
        ann = self.comment._obj
        ann.textValue = rstring(str(content))
        self.conn.saveObject(ann)
    
    def saveUrlAnnotation(self, url):
        ann = self.url._obj
        ann.textValue = rstring(str(url))
        self.conn.saveObject(ann)
    
    def saveTagAnnotation(self, tag):
        ann = self.tag._obj
        ann.textValue = rstring(str(tag))
        self.conn.saveObject(ann)
    
    def getFileAnnotation(self, iid):
        self.annotation = self.conn.getFileAnnotation(iid)
        self.ann_type = self.annotation.file.format.value.val
        self.originalFile_data = self.conn.getFile(self.annotation.file.id.val)
    
    