#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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

"""
   gateway tests - Testing the gateway.searchObject() method

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg_generated
   - author_testimg_tiny

"""


class TestGetObject (object):

    def testSearchObjects(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()

        # search for Projects
        pros = list(gatewaywrapper.gateway.searchObjects(["Project"],
                                                         "weblitz"))
        for p in pros:
            # assert p.getId() in projectIds
            assert p.OMERO_CLASS == "Project", "Should only return Projects"

        # P/D/I is default objects to search
        # pdis = list( gatewaywrapper.gateway.simpleSearch("weblitz") )
        # method removed from blitz gateway
        # pdis.sort(key=lambda r: "%s%s"%(r.OMERO_CLASS, r.getId()) )
        pdiResult = list(gatewaywrapper.gateway.searchObjects(None,
                                                              "weblitz"))
        pdiResult.sort(key=lambda r: "%s%s" % (r.OMERO_CLASS, r.getId()))
        # can directly check that sorted lists are the same
        # for r1, r2 in zip(pdis, pdiResult):
        #    assert r1.OMERO_CLASS ==  r2.OMERO_CLASS
        #    assert r1.getId() ==  r2.getId()
