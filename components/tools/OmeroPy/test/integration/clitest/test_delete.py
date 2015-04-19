#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

import omero
from omero.plugins.delete import DeleteControl
from test.integration.clitest.cli import CLITest
from omero.rtypes import rstring
import pytest

object_types = ["Image", "Dataset", "Project", "Plate", "Screen"]
permissions = ["rw----", "rwr---", "rwra--", "rwrw--"]
group_prefixes = ["", "Group:", "ExperimenterGroup:"]


class TestDelete(CLITest):

    def setup_method(self, method):
        super(TestDelete, self).setup_method(method)
        self.cli.register("delete", DeleteControl, "TEST")
        self.args += ["delete"]

    def create_object(self, object_type):
        # create object
        if object_type == 'Dataset':
            new_object = omero.model.DatasetI()
        elif object_type == 'Project':
            new_object = omero.model.ProjectI()
        elif object_type == 'Plate':
            new_object = omero.model.PlateI()
        elif object_type == 'Screen':
            new_object = omero.model.ScreenI()
        elif object_type == 'Image':
            new_object = self.new_image()
        new_object.name = rstring("")
        new_object = self.update.saveAndReturnObject(new_object)

        # check object has been created
        found_object = self.query.get(object_type, new_object.id.val)
        assert found_object.id.val == new_object.id.val

        return new_object.id.val

    @pytest.mark.parametrize("object_type", object_types)
    def testDeleteMyData(self, object_type):
        oid = self.create_object(object_type)

        # Delete the object
        self.args += ['/%s:%s' % (object_type, oid)]
        self.cli.invoke(self.args, strict=True)

        # Check the object has been deleted
        assert not self.query.find(object_type, oid)

    @pytest.mark.parametrize('nimages', [1, 2])
    @pytest.mark.parametrize('arguments', ['image', 'fileset'])
    def testFileset(self, nimages, arguments):
        # 2 images sharing a fileset
        images = self.importMIF(nimages)
        img = self.query.get('Image', images[0].id.val)
        filesetId = img.fileset.id.val
        fileset = self.query.get('Fileset', filesetId)
        assert fileset is not None

        # Delete the fileset
        if arguments == 'fileset':
            self.args += ['Fileset:%s' % filesetId]
        else:
            ids = [str(i.id.val) for i in images]
            self.args += ['Image:' + ",".join(ids)]
        self.cli.invoke(self.args, strict=True)

        # Check the fileset and images have been deleted
        assert not self.query.find('Fileset', filesetId)
        for i in images:
            assert not self.query.find('Image', i.id.val)

    def testFilesetPartialFailing(self):
        images = self.importMIF(2)  # 2 images sharing a fileset

        # create a new group and try to move only one image to the new group
        self.args += ['/Image:%s' % images[0].id.val]
        self.cli.invoke(self.args, strict=True)

        # Check the images are still valid
        for i in images:
            assert self.query.get('Image', i.id.val) is not None

    def testFilesetAllImagesMoveDataset(self):
        images = self.importMIF(2)  # 2 images sharing a fileset
        dataset_id = self.create_object('Dataset')  # ... in a dataset

        # put the images into the dataset
        for image in images:
            link = omero.model.DatasetImageLinkI()
            link.parent = omero.model.DatasetI(dataset_id, False)
            link.child = omero.model.ImageI(image.id.val, False)
            self.update.saveObject(link)

        # create a new group and try to move the dataset to the new group
        self.args += ['/Dataset:%s' % dataset_id]
        self.cli.invoke(self.args, strict=True)

        # check the dataset has been deleted
        assert not self.query.find('Dataset', dataset_id)

        # check the images have been deleted
        for image in images:
            assert not self.query.find('Image', image.id.val)
