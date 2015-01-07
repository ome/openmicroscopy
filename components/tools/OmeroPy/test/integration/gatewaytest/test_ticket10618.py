#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

"""
Code-generated tests for ticket 10618 which run through all the
sensible usages of ImageWrapper.getThumbnail()
"""


import library as lib


def generate_parameters():
    """
    Generate tuples of parameters for possible
    methods. Some may be skipped later.
    """
    for perms in ("rw", "rwr"):
        for testertype in ("root", "member"):
            for direct in (True, False):
                for grpctx in (True, False):
                    for size in (16, 96):
                        yield (perms, testertype, direct, grpctx, size)


class Test10618(lib.ITest):
    """
    Holder for all of the generated methods.
    """
    pass


#
# Primary method-generation loop
#
for perms, testertype, direct, grpctx, size in generate_parameters():

    if perms == "rw" and testertype == "member":
        # A member in a private group will never be able
        # to load the ImageWrapper and therefore need not
        # be tested.
        continue

    def dynamic_test(self, perms=perms, testertype=testertype,
                     direct=direct, size=size):

        group = self.new_group(perms=perms.ljust(6, "-"))
        owner = self.new_client(group=group)
        image = self.createTestImage(session=owner.sf)

        if testertype == "root":
            tester = self.root
        elif testertype == "member":
            tester = self.new_client(group=group)

        import omero.gateway
        conn = omero.gateway.BlitzGateway(client_obj=tester)

        if grpctx:
            conn.SERVICE_OPTS.setOmeroGroup(str(group.id.val))
        else:
            conn.SERVICE_OPTS.setOmeroGroup(str(-1))

        img = conn.getObject("Image", image.id)
        assert img.getThumbnail(size=size, direct=direct)

    test_name = "test_%s_%s_dir%s_grp%s_%s" % \
                (perms, testertype, direct, grpctx, size)
    setattr(Test10618, test_name, dynamic_test)
