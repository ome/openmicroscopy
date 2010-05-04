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

from omero.rtypes import *
from omero.model import CommentAnnotationI, UriAnnotationI, FileAnnotationI

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
                if self.comment is None:
                    raise AttributeError("We are sorry, but that comment does not exist, or if it does, you have no permission to see it.")
            elif o_type == "url":
                self.url = self.conn.getUriAnnotation(long(oid))
                if self.url is None:
                    raise AttributeError("We are sorry, but that url does not exist, or if it does, you have no permission to see it.")
            elif o_type == "tag":
                self.tag = self.conn.getTagAnnotation(long(oid))
                if self.tag is None:
                    raise AttributeError("We are sorry, but that tag does not exist, or if it does, you have no permission to see it.")
    
    def saveCommentAnnotation(self, content):
        ann = self.comment._obj
        ann.textValue = rstring(str(content))
        self.conn.saveObject(ann)
    
    def saveUrlAnnotation(self, url):
        ann = self.url._obj
        ann.textValue = rstring(str(url))
        self.conn.saveObject(ann)
    
    def saveTagAnnotation(self, tag, description):
        ann = self.tag._obj
        ann.textValue = rstring(str(tag))
        if description != "" :
            ann.description = rstring(str(description))
        else:
            ann.description = None
        self.conn.saveObject(ann)
    
    def getFileAnnotation(self, iid):
        self.annotation = self.conn.getFileAnnotation(iid)
        self.ann_type = self.annotation.file.mimetype
        temp = self.conn.getFile(self.annotation.file.id.val, self.annotation.file.size.val)
        if temp.startswith(settings.FILE_UPLOAD_TEMP_DIR):
            from django.core.servers.basehttp import FileWrapper
            self.originalFile_data = FileWrapper(file(temp))
        else:
            self.originalFile_data = temp
        
