#!/usr/bin/env python

"""

   Function for setting the working directory for an
   Omero installation on Windows, since relative paths
   are not supported.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   :author: Josh Moore <josh@glencoesoftware.com>

"""


import sys, exceptions
from xml.dom import minidom
from path import path
import fileinput

dir = path(".")
cfg = dir / "etc" / "Windows.cfg"
xml = dir / "etc" / "grid" / "windefault.xml"

def win_set_path(new_name = dir.abspath(), old_name = r"c:\omero_dist"):
    """
    Parses the Windows cfg and xml files and
    replaces the default "c:\omero_dist" with the
    given value.
    """

    if new_name is None or old_name is None:
        raise exceptions.Exception("Arguments cannot be None")

    if new_name.find(" ") >= 0:
        raise exceptions.Exception("Contains whitespace: '%s'" % new_name)

    new_name = path(new_name).abspath()
    old_name = path(old_name).abspath()

    print "Converting from %s to %s" % (old_name, new_name)

    new_name2 = new_name.replace("\\","\\\\")
    old_name2 = old_name.replace("\\","\\\\")

    count = 0
    for line in fileinput.input([str(cfg),str(xml)], inplace=1):
        if line.find(old_name) >= 0:
            count += 1
            print line.replace(old_name,new_name),
        elif line.find(old_name2) >= 0:
            count += 1
            print line.replace(old_name2,new_name2),
        else:
            print line,

    fileinput.close()
    print "Changes made: %s" % count
    return count

if __name__ == "__main__":
    try:
        if "-h" in sys.argv or "--help" in sys.argv:
            pass
        elif len(sys.argv) == 1:
            win_set_path()
            sys.exit(0)
        elif len(sys.argv) == 2:
            win_set_path(new_name = sys.argv[1])
            sys.exit(0)
        elif len(sys.argv) == 3:
            win_set_path(old_name = sys.argv[1], new_name = sys.argv[2])
            sys.exit(0)
    except exceptions.Exception, e:
        print "Failed to set path: ", e
        sys.exit(1)

    print """Usage: %s [oldname] newname

Replaces the [oldname] entries in the Windows configuration files
with [newname]. By default, [oldname] is set to "c:\omero_dist"
        """ % sys.argv[0]
    sys.exit(2)
