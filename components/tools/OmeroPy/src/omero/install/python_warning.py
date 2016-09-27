#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   python helper plugin

   Copyright 2016 University of Dundee. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt
"""

import sys
import platform


PYTHON_WARNING = ("Python version %s is not "
                  "supported!" % platform.python_version())


def py27_only():
    if sys.version_info < (2, 7) or sys.version_info >= (2, 8):
        return False
    return True
