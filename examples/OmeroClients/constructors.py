#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
import omero.clients

image = omero.model.ImageI()
dataset = omero.model.DatasetI(long(1), False)
image.linkDataset(dataset)
