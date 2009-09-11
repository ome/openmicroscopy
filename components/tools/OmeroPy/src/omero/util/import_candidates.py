#!/usr/bin/env python
"""
   Startup plugin for command-line importer.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import CLI
import tempfile

def as_stdout(path):
        if isinstance(path,str):
            path = [str]
        else:
            path = list(path)

        cli = CLI()
        cli.loadplugins()
        cli.invoke(["import", "-f"]+path)

def as_dictionary(path):
        old_stdout = sys.stdout
        try:
            t = tempfile.TemporaryFile()
            sys.stdout = t
            as_stdout(path)
        finally:
            sys.stdout = old_stdout

        t.seek(0)
        output = t.readlines()
        t.close()

        gline = -1
        key = None
        groups = {}
        for line in output:
            line = line.strip()
            if line.startswith("#"):
                gline = -1
            else:
                if gline == -1:
                    gline = 1
                    key = line
                    groups[key] = [line]
                else:
                    groups[key].append(line)

        from pprint import pprint
        pprint(groups)
        return groups


if __name__ == "__main__":
    import sys
    as_stdout(sys.argv[1:])
