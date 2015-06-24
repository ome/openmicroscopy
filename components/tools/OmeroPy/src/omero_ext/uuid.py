#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Python uuid extensions.

Use of these methods rather than those in uuid.py
ensures that on older Mac platforms Python bug #8621
does not cause child processes to receive the same
random number as the parent process.

See http://bugs.python.org/issue8621
See http://trac.openmicroscopy.org.uk/ome/ticket/3774

Josh Moore, josh at glencoesoftware.com
Copyright (c) 2010, Glencoe Software, Inc.
See LICENSE for details.

"""

import warnings
warnings.warn(
    "The omero_ext.uuid module is deprecated. Please use Python built-in"
    "uuid module instead", DeprecationWarning)

try:
    __uuid__ = __import__("uuid") # ok:3774
except ImportError:
    # Missing in earlier Python versions
    # Use our copy
    import omero_ext.pyuuid as __uuid__ # ok:3774


def __handle_8621__():
    """
    Called on import or reload to null
    the _uuid_generate_random and
    _uuid_generate_time fields of
    the original uuid module.
    """
    import sys
    if sys.platform == 'darwin':
        import os
        if int(os.uname()[2].split('.')[0]) >= 9:
            __uuid__._uuid_generate_random = __uuid__._uuid_generate_time = None


__handle_8621__()


def __mkdoc__(name):
    """
    Generates a doc string for the methods in this module.
    """
    return "(DELEGATES to uuid.%s) %s" % (name, getattr(__uuid__, name).__doc__)


def getnode(*args, **kwargs):
    return __uuid__.getnode(*args, **kwargs)
getnode.__doc__ = __mkdoc__("getnode")


def uuid1(*args, **kwargs):
    return __uuid__.uuid1(*args, **kwargs)
uuid1.__doc__ = __mkdoc__("uuid1")


def uuid3(*args, **kwargs):
    return __uuid__.uuid3(*args, **kwargs)
uuid3.__doc__ = __mkdoc__("uuid3")


def uuid4():
    return __uuid__.uuid4()
uuid4.__doc__ = __mkdoc__("uuid4")


def uuid5(*args, **kwargs):
    return __uuid__.uuid5(*args, **kwargs)
uuid5.__doc__ = __mkdoc__("uuid5")
