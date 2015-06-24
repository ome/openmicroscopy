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
   Test of the omero.cmd.Chgrp2 Request type.
"""

import omero
import library as lib

from omero.callbacks import CmdCallbackI


class TestChgrp(lib.ITest):

    def testChgrpImage(self):

        # Data Setup
        img = self.new_image()
        img = self.update.saveAndReturnObject(img)

        # New method
        chgrp = omero.cmd.Chgrp2(targetObjects={'Image': [img.id.val]})
        handle = self.sf.submit(chgrp)
        cb = CmdCallbackI(self.client, handle)
        cb.loop(20, 750)

        # Check Data
        self.query.get("Image", img.id.val)
