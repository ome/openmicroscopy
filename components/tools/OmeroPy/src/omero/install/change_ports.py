#!/usr/bin/env python

"""

   Function for changing the ports used by Glacier2
   and the IceGrid registry. To run more than one OMERO
   instance on a machine, it's necessary to modify these.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   :author: Josh Moore <josh@glencoesoftware.com>

"""


import sys, exceptions
import fileinput
from path import path

dir = path(".")
etc = dir / "etc"
grid = etc / "grid"

def change_ports(glacier2, registry, revert = False):
    """
    Parses the etc configuration files to change
    the current port values. If the files have
    been noticeably changed, this method may fail.

    Example::

        ./grid/default.xml:    <variable name="ROUTERPORT"   value="4063"/>
        ./grid/windefault.xml:    <variable name="ROUTERPORT"   value="4063"/>
        ./internal.cfg:Ice.Default.Locator=IceGrid/Locator:tcp -h 127.0.0.1 -p 4061
        ./master.cfg:IceGrid.Registry.Client.Endpoints=tcp -h 127.0.0.1 -p 4061

    """

    if revert:
        f_glacier2 = str(int(glacier2))
        f_registry = str(int(registry))
        t_glacier2 = "4063"
        t_registry = "4061"
    else:
        t_glacier2 = str(int(glacier2))
        t_registry = str(int(registry))
        f_glacier2 = "4063"
        f_registry = "4061"

    cfgs = [ str(x) for x in etc.files("*.cfg") ]
    done = set()
    for line in fileinput.input(cfgs, inplace=1):
        if line.find("Ice.Default.Locator") >= 0:
            if line.find(f_registry) >= 0:
                print line.replace(f_registry, t_registry),
                done.add(fileinput.filename())
                continue
        elif line.find("IceGrid.Registry.Client.Endpoints") >= 0:
            if line.find(f_registry) >= 0:
                print line.replace(f_registry, t_registry),
                done.add(fileinput.filename())
                continue
        print line,
    fileinput.close()
    if done:
        print "Converted: %s=>%s in %s" % (f_registry, t_registry, ", ".join(done))
    else:
        print "No values found for %s" % f_registry

    xmls = [ str(x) for x in grid.files("*.xml") ]
    done = set()
    for line in fileinput.input(xmls, inplace=1):
        if line.find("ROUTERPORT") >= 0:
            if line.find(f_glacier2) >= 0:
                print line.replace(f_glacier2, t_glacier2),
                done.add(fileinput.filename())
                continue
        print line,
    fileinput.close()
    if done:
        print "Converted: %s=>%s in %s" % (f_glacier2, t_glacier2, ", ".join(done))
    else:
        print "No values found for %s" % f_glacier2

if __name__ == "__main__":
    try:
        if len(sys.argv) < 3 or len(sys.argv) > 4:
            print """ %s [--revert] <glacier2 port> <icegrid registry port>

    Changes all 4063 ports to the given glacier2 port
    and all 4061 ports to the given registry port. You will
    need to give your clients the new glacier2 port.""" % sys.argv[0]
            sys.exit(2)
        else:
            args = sys.argv[1:]
            try:
                idx = args.index("--revert")
                args.pop(idx)
                revert = True
            except ValueError:
                revert = False
            glacier2 = int(args[0])
            registry = int(args[1])
            change_ports(glacier2, registry, revert)
            sys.exit(0)
    except exceptions.Exception, e:
        print "Failed to set ports: ", e
        sys.exit(1)
