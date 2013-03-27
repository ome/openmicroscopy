#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Utility method for calling the equivalent of "bin/omero import -f".
   Results are parsed when using as_dictionary.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys, os
import omero

from omero.util.temp_files import create_path, remove_path
from omero.cli import CLI

def _to_list(path):
    """
    Guarantees that a list of strings will be returned.
    Handles unicode caused by "%s" % path.path.
    """
    if isinstance(path,str) or isinstance(path,unicode):
        path = [str(path)]
    else:
        path = [str(x) for x in path]
        return path

def as_stdout(path, readers=""):
        path = _to_list(path)
        readers = str(readers)
        cli = CLI()
        cli.loadplugins()
        if readers:
            cli.invoke(["import", "-l", readers, "-f"]+path)
        else:
            cli.invoke(["import", "-f"]+path)
        if cli.rv != 0:
            raise omero.InternalException(None, None, "'import -f' exited with a rc=%s. See console for more information" % cli.rv)

def as_dictionary(path, readers=""):
    """
    Run as_stdout, parses the output and returns a dictionary of the form::
        {
            some_file_in_group : \
                [
                    some_file_in_group
                    some_other_file_in_group
                    ...
                    last_file_in_group
                ],
            some_file_in_second_group : ...
        }
    """

    t = create_path("candidates", "err")

    path = _to_list(path)
    path.insert(0, "---file=%s" % t)
    try:
        as_stdout(path, readers=readers)
        f = open(str(t),"r")
        output = f.readlines()
        f.close()
    finally:
        remove_path(t)

    gline = -1
    key = None
    groups = {}
    for line in output:
        line = line.strip()
        if len(line) == 0:
            continue
        if line.startswith("#"):
            gline = -1
        else:
            if gline == -1:
                gline = 1
                key = line
                groups[key] = [line]
            else:
                groups[key].append(line)

    return groups


if __name__ == "__main__":
    import sys
    as_stdout(sys.argv[1:])
