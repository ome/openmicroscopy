#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Workaround for 3.3 / 3.4 differences in Ice importing.
"""

import Ice
update = getattr(Ice, "updateModules", None)

def load(target):
    """
    uses __import__ followed by Ice.updateModules if available.

    In 3.4, the Ice.updateModules method was introduced which
    must be called after every explicit Ice import:

        import Ice
        import omero_ServerErrors_Ice
        Ice.updateModules()

    Since we are trying to stay 3.4 and 3.3 compatible, this
    method should be used instead.
    """
    __import__(target)
    if update:
        update()
