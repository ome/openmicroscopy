#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""

   Function for enabling/disabling the bzip2.dll which
   comes with PyTables.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os, sys

def bzip2_tool(disable=False):
    """
    Renames the bzip2.dll library which comes with PyTables.
    """

    import tables
    f = tables.__file__
    p = os.path.dirname(f)
    p = os.path.abspath(p)
    b = os.path.join(p, "bzip2.dll")
    d = os.path.join(p, "bzip2_DISABLED.dll")
    if disable:
        _swap(b,d)
    else:
        _swap(d,b)

def _swap(f, t):
    if not os.path.exists(f):
        print "%s doesn't exist" % f
        sys.exit(0)
    os.rename(f, t)

if __name__ == "__main__":
    try:
        if len(sys.argv) == 2:
            which = sys.argv[1]
            if which == "disable":
                which = True
            elif which == "enable":
                which = False
            else:
                print "Unknown command: ", which
                sys.exit(2)
            bzip2_tool(disable=which)
            sys.exit(0)
    except Exception, e:
        print "bzip2_tool failed: ", e
        sys.exit(1)

    print "Usage: %s disable|enable" % sys.argv[0]
    sys.exit(2)
