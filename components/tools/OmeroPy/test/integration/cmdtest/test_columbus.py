#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
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
   Test of the omero.cmd.Chgrp and Chown Request types
   as specified by Bernhard Hollaender of Perkin Elmer:

"These are quite complex demands and I reckon and probably not the most
obvious ones. Anyways, I'd really like the sharing options to work a bit
more like in social networks. In principle it should be possible to share
data with anyone and it should be possible to share data with groups and
individuals in the same way. It should also be possible to revoke access to
datasets for individuals and groups. In case something should be deleted,
there should be no restrictions because other users may have linked anything
to the dataset in question. The special cases fro group administrators are
not so important I would guess it is ok not to have extra access rights,
except that they should be able to delete users and data from their group."

"""

import library as lib


class TestColumbus(lib.ITest):
    """
    The following tests all assume the following
    user configuration:

        Users: A, B, C, D, E
        Groups: a{A,B} b{C,D} c{E}
        Group admin: a:A b:D

    """

    def userconfig(self, perms="rwr---"):
        self.group_a = self.new_group(perms=perms)
        self.client_A, self.user_A = \
            self.new_client_and_user(group=self.group_a, owner=True)
        self.client_B, self.user_B = \
            self.new_client_and_user(group=self.group_a, owner=False)

        self.group_b = self.new_group(perms=perms)
        self.client_C, self.user_C = \
            self.new_client_and_user(group=self.group_b, owner=True)
        self.client_D, self.user_D = \
            self.new_client_and_user(group=self.group_b, owner=False)

        self.group_c = self.new_group(perms=perms)
        self.client_E, self.user_E = \
            self.new_client_and_user(group=self.group_c)

    def data(self, client):
        up = client.sf.getUpdateService()
        img = self.new_image()
        return up.saveAndReturnObject(img)

    def test01(self):
        """1. share data with group user

        user A imports data and user B should work on it (creates attachments
        etc) - this should not render it impossible for user A to delete the
        data

        For this test, the group admin position of A is to be neglected
        """
        pass

    def test02(self):
        """2. pass data to other user

        user A imports data and wants to hand it over to user B, so that B now
        has full responsibility over it (B owns the data)

        For this test, the group admin position of A is to be neglected
        """
        pass  # is it ok for non-admin to pass

    def test03(self):
        """3. pass data to other group

        user A imports data and want to hand it over to user C, so that  C now
        has full responsibility over it (C owns the data) and group a has no
        access to it

            3.1 user D should have access to the data, too (similar to 1.)
            3.2 user A should still have access to the data, too

        For this test, the group admin position of A is to be neglected
        """
        pass

    def test04(self):
        """4. share data with non-group users

        user A should be able to make data available for C,D or E, so that they
        can work with the data (similar to 1.)

        For this test, the group admin position of A is to be neglected
        """
        pass

    def test05(self):
        """5. share data with other group

        user A should be able to make data available for group c

        For this test, the group admin position of A is to be neglected
        """
        pass

    def test06(self):
        """6. hide data from own group

        user A should be able to have data not available to other members in
        group a except for the group admin (with this data all of the previous
        operations should be possible)

        For this test, the group admin position of A is to be neglected
        """
        pass

    def test07(self):
        """7. admin has full rights

        group admin a:A should have full rights on all data in a group a, i.e.
        all data of user A,B
        """
        pass

    def test08(self):
        """8. delete user in group

        group admin a:A should be able to delete users in the group which
        removes all data that is owned by this user
        """
        pass

    def test09(self):
        """9. ???

        group admin a:A may be able to intervene re-assignment or sharing of
        data in group a when it is made available to users outside of group a
        """

    def test10(self):
        """10. ???

        group admin b:D should not have access to data that is owned by A and
        only shared with users in group b but not with group b.
        """
