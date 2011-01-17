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
from omero.model import CommentAnnotationI, FileAnnotationI

from webclient.controller import BaseController

class BaseAnnotation(BaseController):
    
    annotation = None
    
    def __init__(self, conn, o_type=None, oid=None, **kw):
        BaseController.__init__(self, conn)
        if oid is not None:
            self.annotation = self.conn.getAnnotation(long(oid))
            if self.annotation is None:
                raise AttributeError("We are sorry, but that annotation does not exist, or if it does, you have no permission to see it.")
    
    def remove(self, parent):
        for al in self.annotation.getParentLinks(str(parent[0]), [long(parent[1])]):
            if al is not None:
                self.conn.deleteObject(al._obj)
                        
    def saveCommentAnnotation(self, content):
        ann = self.comment._obj
        ann.textValue = rstring(str(content))
        self.conn.saveObject(ann)
    
    def saveTagAnnotation(self, tag, description):
        ann = self.tag._obj
        ann.textValue = rstring(str(tag))
        if description != "" :
            ann.description = rstring(str(description))
        else:
            ann.description = None
        self.conn.saveObject(ann)
    
    def deleteItem(self, child=False, anns=False):
        handle = None
        if self.comment:
            handle = self.conn.deleteAnnotation(self.comment.id)
        elif self.tag:
            handle = self.conn.deleteAnnotation(self.tag.id)
        elif self.attachment:
            handle = self.conn.deleteAnnotation(self.attachment.id)
        return handle
    