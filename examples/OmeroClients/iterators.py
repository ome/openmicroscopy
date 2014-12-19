#!/usr/bin/env python
# -*- coding: utf-8 -*-
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI

image = ImageI()
dataset = DatasetI()
link = dataset.linkImage(image)

for link in image.iterateDatasetLinks():
    link.getChild().getName()
