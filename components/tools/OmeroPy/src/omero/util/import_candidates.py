#!/usr/bin/env python
"""
   Utility method for calling the equivalent of "bin/omero import -f".
   Results are parsed when using as_dictionary.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys
from omero.cli import CLI
import tempfile

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

def as_dictionary(path):

        t = tempfile.NamedTemporaryFile()
        path = _to_list(path)
	path.insert(0, "---file=%s" % t.name)
	as_stdout(path)
	f = open(t.name,"r")
	output = f.readlines()
	f.close()
	t.close()

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
