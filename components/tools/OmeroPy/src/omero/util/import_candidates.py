#!/usr/bin/env python
"""
   Utility method for calling the equivalent of "bin/omero import -f".
   Results are parsed when using as_dictionary.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys, os
import tempfile
import omero
import omero_ServerErrors_ice

from omero.cli import CLI

def _to_list(path):
        if isinstance(path,str):
            path = [path]
        else:
            path = list(path)
	return path

def as_stdout(path):
        path = _to_list(path)
        cli = CLI()
        cli.loadplugins()
        cli.invoke(["import", "-f"]+path)
        if cli.rv != 0:
            raise omero.InternalException(None, None, "'import -f' failed")

def as_dictionary(path):
    """
    Run as_stdout, parses the output and returns a dictionary of the form:
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

    t = tempfile.NamedTemporaryFile()
    t.close() # Forcing deletion due to Windows sharing issues

    path = _to_list(path)
    path.insert(0, "---file=%s" % t.name)
    try:
        as_stdout(path)
        f = open(t.name,"r")
        output = f.readlines()
        f.close()
    finally:
        if os.path.exists(t.name):
            os.remove(t.name)

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
