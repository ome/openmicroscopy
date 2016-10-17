#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   windows helper plugin

   Copyright 2009-2016 University of Dundee. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt
"""

from functools import wraps

WINDOWS_WARNING = ("ERROR: OMERO.server support for Windows was removed"
                   " in OMERO 5.3, see http://blog.openmicroscopy.org/"
                   "tech-issues/future-plans/deployment/2016/03/22/"
                   "windows-support/")


def windows_warning(func):
    """
    Support for Windows will be removed
    """
    def win_warn(func):
        def wrapper(self, *args, **kwargs):
            if self._isWindows():
                self.ctx.die(20, WINDOWS_WARNING)
            return func(self, *args, **kwargs)
        return wrapper
    return wraps(func)(win_warn(func))
