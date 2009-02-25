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

import omero
from omero.rtypes import *
from omero_model_ExperimenterAnnotationLinkI import ExperimenterAnnotationLinkI
from omero_model_FileAnnotationI import FileAnnotationI
from omero_model_OriginalFileI import OriginalFileI

from webadmin.controller import BaseController

class BaseUploadFile(BaseController):

    def __init__(self, conn):
        BaseController.__init__(self, conn)

    def attach_photo(self, newFile):
        has = self.conn.hasExperimenterPhoto()
        if has is not None:
            if newFile.content_type.startswith("image"):
                f = newFile.content_type.split("/") 
                format = self.conn.getFileFormt(f[1].upper())
            else:
                format = self.conn.getFileFormt(newFile.content_type)
            oFile = OriginalFileI()
            oFile.setName(rstring(str(newFile.name)));
            oFile.setPath(rstring(str(newFile.name)));
            oFile.setSize(rlong(long(newFile.size)));
            oFile.setSha1(rstring("pending"));
            oFile.setFormat(format);
            self.objectPermissions(oFile, {'owner':'rw', 'group':'r', 'world':'r'})

            of = self.conn.saveAndReturnObject(oFile);
            self.conn.saveFile(newFile, of.id)
            
            has._obj.setFile(of._obj)
            self.conn.saveObject(has._obj)
            
        else:
            if newFile.content_type.startswith("image"):
                f = newFile.content_type.split("/") 
                format = self.conn.getFileFormt(f[1].upper())
            else:
                format = self.conn.getFileFormt(newFile.content_type)
            oFile = OriginalFileI()
            oFile.setName(rstring(str(newFile.name)));
            oFile.setPath(rstring(str(newFile.name)));
            oFile.setSize(rlong(long(newFile.size)));
            oFile.setSha1(rstring("pending"));
            oFile.setFormat(format);
            self.objectPermissions(oFile, {'owner':'rw', 'group':'r', 'world':'r'})

            of = self.conn.saveAndReturnObject(oFile);
            self.conn.saveFile(newFile, of.id)

            fa = FileAnnotationI()
            fa.setFile(of._obj)
            fa.setNs(rstring("openmicroscopy.org/omero/experimenter/photo"))
            self.objectPermissions(fa, {'owner':'rw', 'group':'r', 'world':'r'})
            l_ea = ExperimenterAnnotationLinkI()
            l_ea.setParent(self.conn.getUser())
            l_ea.setChild(fa)
            self.objectPermissions(l_ea, {'owner':'rw', 'group':'r', 'world':'r'})
            self.conn.saveObject(l_ea)
        

        