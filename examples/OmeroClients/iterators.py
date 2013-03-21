# -*- coding: utf-8 -*-
import omero
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

image = ImageI()
dataset = DatasetI()
link = dataset.linkImage(image)

for link in image.iterateDatasetLinks():
    link.getChild().getName();
