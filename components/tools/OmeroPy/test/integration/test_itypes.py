#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2009-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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

"""
   Integration test focused on the omero.api.ITypes interface.

"""

import test.integration.library as lib
from omero.rtypes import rstring


class TestTypes(lib.ITest):

    # ticket:1436
    def testGetEnumerationTypes(self):
        self.client.sf.getTypesService().getEnumerationTypes()

    def testAllEnumerations(self):
        types = self.root.sf.getTypesService()
        rv = dict()
        for e in types.getOriginalEnumerations():
            if rv.get(e.__class__.__name__) is None:
                rv[e.__class__.__name__] = list()
            rv[e.__class__.__name__].append(e)

        for r in rv:
            types.allEnumerations(str(r))

    def testGetEnumerationWithEntries(self):
        self.root.sf.getTypesService().getEnumerationsWithEntries().items()

    def testManageEnumeration(self):
        from omero_model_ExperimentTypeI import ExperimentTypeI
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        types = self.root.sf.getTypesService()

        # create enums
        obj = ExperimentTypeI()
        obj.setValue(rstring("test_value_%s" % uuid))
        enum = types.createEnumeration(obj)
        types.deleteEnumeration(enum)

        obj = ExperimentTypeI()
        obj.setValue(rstring("test_value2_%s" % (uuid)))
        new_entries = [obj]
        types.updateEnumerations(new_entries)

        types.resetEnumerations("ExperimentTypeI")
