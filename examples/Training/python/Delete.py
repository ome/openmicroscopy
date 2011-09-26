#!/usr/bin/env python
# 
# Copyright (c) 2011 University of Dundee. 
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
# Version: 1.0
#
# This script shows a simple connection to OMERO, printing details of the connection.
# NB: You will need to edit the config.py before running.
import my_omero_config as conf
from omero.gateway import BlitzGateway
from omero.rtypes import *
from omero.model import *
conn = BlitzGateway(conf.USERNAME, conf.PASSWORD, host=conf.HOST, port=conf.PORT)
conn.connect()
# Let's set up an Image to delete (NB this image has no Pixel data etc)
image = omero.model.ImageI()
image.setName(omero.rtypes.rstring("test_delete"))
image.setAcquisitionDate(omero.rtypes.rtime(2000000))
image = conn.getUpdateService().saveAndReturnObject(image)
imageId = image.getId().getValue()
# OR, you could put the ID of an image you want to delte here:
#imageId = 101


# Delete Image

# You can delete a number of objects of the same type at the same time. In this case 'Image'

obj_ids = [imageId]
# use deleteChildren=True if you are E.g. deleting a Dataset and you want to delete Images.
conn.deleteObjects("Image", obj_ids, deleteAnns=True, deleteChildren=False)