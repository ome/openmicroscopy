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

    """
    __import__(target)
    Ice.updateModules()
