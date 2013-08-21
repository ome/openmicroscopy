#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero.util.script_utils as scriptUtil
from numpy import arange, uint8

def createTestImage(session):
    
    plane2D = arange(256, dtype=uint8).reshape(16,16)
    image = scriptUtil.createNewImage(session, [plane2D], "imageName", "description", dataset=None)

    return image.getId().getValue()
