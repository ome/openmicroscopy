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

from omero.rtypes import *
from omero.model import ExperimenterAnnotationLinkI

from django.utils.encoding import smart_str

from webadmin.controller import BaseController

class BaseUploadFile(BaseController):

    def __init__(self, conn):
        BaseController.__init__(self, conn)

    def attach_photo(self, newFile):
        if newFile.content_type.startswith("image"):
            f = newFile.content_type.split("/") 
            format = f[1].upper()
        else:
            format = newFile.content_type
        
        self.conn.uploadMyUserPhoto(smart_str(newFile.name), format, newFile.read())
