#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
   Test of the omero.cmd.Chgrp Request type.
"""

import omero
import unittest

from integration.library import ITest
from omero.callbacks import CmdCallbackI


class ChgrpTest(ITest):

    def testChgrpImage(self):

        # One user in two groups
        client, exp = self.new_client_and_user()
        grp = self.new_group([exp])
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()

        # Data Setup
        img = self.new_image()
        img = update.saveAndReturnObject(img)

        # New method
        chgrp = omero.cmd.Chgrp(type="/Image", id=img.id.val, options=None)
        handle = client.sf.submit(chgrp)
        cb = CmdCallbackI(client, handle)
        cb.loop(20, 750)

        # Check Data
        query.get("Image", img.id.val)

if __name__ == '__main__':
    unittest.main()
