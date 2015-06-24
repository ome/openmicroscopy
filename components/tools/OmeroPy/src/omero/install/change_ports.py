#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""

   Function for changing the ports used by Glacier2
   and the IceGrid registry. To run more than one OMERO
   instance on a machine, it's necessary to modify these.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   :author: Josh Moore <josh@glencoesoftware.com>

"""


import re
import sys
import fileinput
from path import path


def line_has_port(line, port):
    m = re.match("^.*?\D%s\D.*?$" % port, line)
    if not m:
        m = re.match("^.*?\D%s$" % port, line)
    return m


def update_ice_config(f, f_glacier2, t_glacier2):
    old_port = "omero.port=%s" % f_glacier2
    new_port = "omero.port=%s" % t_glacier2
    glacier_port_pattern = re.compile('^%s$' % old_port, re.MULTILINE)
    general_port_pattern = re.compile('^omero.port=\d+$', re.MULTILINE)
    commented_port_pattern = re.compile('^## omero.port=\d+$', re.MULTILINE)

    with open(f, 'r') as r:
        text = r.read()

    (new_text, n) = glacier_port_pattern.subn(new_port, text)
    if n != 0:
        return (new_text, "Converted: %s => %s in %s"
                % (old_port, new_port, f))

    (new_text, n) = general_port_pattern.subn(new_port, text)
    if n != 0:
        return None, "No values found for %s in %s" % (old_port, f)

    (new_text, n) = commented_port_pattern.subn(new_port, text)
    if n != 0:
        return (new_text, "Uncommented: %s in %s" % (new_port, f))

    new_text = text + "\n%s\n" % new_port
    return (new_text, "Appended: %s to %s" % (new_port, f))


def change_ports(glacier2, glacier2insecure, registry, revert=False, dir="."):
    """
    Parses the etc configuration files to change
    the current port values. If the files have
    been noticeably changed, this method may fail.

    Example::

        ./etc/ice.config: ## omero.port=4064 (default)
        or
        ./etc/ice.config: omero.port=4064
        ./grid/default.xml:    <variable name="ROUTERPORT"   value="4064"/>
        ./grid/windefault.xml:    <variable name="ROUTERPORT"   value="4064"/>
        ./internal.cfg:Ice.Default.Locator=IceGrid/Locator:tcp -h 127.0.0.1 \
-p 4061
        ./master.cfg:IceGrid.Registry.Client.Endpoints=tcp -h 127.0.0.1 \
-p 4061

    """
    DIR = path(dir)
    ETC = DIR / "etc"
    GRID = ETC / "grid"

    if revert:
        f_glacier2 = str(int(glacier2))
        f_glacier2insecure = str(int(glacier2insecure))
        f_registry = str(int(registry))
        t_glacier2 = "4064"
        t_glacier2insecure = "4063"
        t_registry = "4061"
    else:
        t_glacier2 = str(int(glacier2))
        t_glacier2insecure = str(int(glacier2insecure))
        t_registry = str(int(registry))
        f_glacier2 = "4064"
        f_glacier2insecure = "4063"
        f_registry = "4061"

    def check_line(l, s, f, t, done):
        """
        @param l: the line
        @param s: the string that denotes this line is supposed to change
        @param f: from port
        @param t: to port
        @return: the line, changed if needed
        """
        if l.find(s) >= 0 and line_has_port(l, f):
            print l.replace(f, t),
            done.add(fileinput.filename())
            return True
        return False

    cfgs = [str(x) for x in ETC.files("*.cfg")]
    found_reg = set()
    for line in fileinput.input(cfgs, inplace=1):
        if check_line(line, "Ice.Default.Locator", f_registry, t_registry,
                      found_reg):
            continue
        elif check_line(line, "IceGrid.Registry.Client.Endpoints", f_registry,
                        t_registry, found_reg):
            continue
        print line,
    fileinput.close()

    xmls = [str(x) for x in GRID.files("*.xml")]

    found_ssl = set()
    found_tcp = set()
    for line in fileinput.input(xmls, inplace=1):
        if check_line(line, "ROUTERPORT", f_glacier2, t_glacier2, found_ssl):
            continue
        elif check_line(line, "ROUTER", f_glacier2insecure,
                        t_glacier2insecure, found_tcp):
            continue
        print line,
    fileinput.close()

    ice_cfg = ETC.files("ice.config")
    for x in ice_cfg:
        (text, msg) = update_ice_config(x, f_glacier2, t_glacier2)
        if text:
            x.write_text(text)
        print msg

    for x in ((found_reg, f_registry, t_registry),
              (found_tcp, f_glacier2insecure, t_glacier2insecure),
              (found_ssl, f_glacier2, t_glacier2)):
        if x[0]:
            print "Converted: %s=>%s in %s" % (x[1], x[2], ", ".join(x[0]))
        else:
            print "No values found for %s" % x[1]

if __name__ == "__main__":
    try:
        if len(sys.argv) < 3 or len(sys.argv) > 5:
            print """ %s [--revert] <glacier2 port> <icegrid registry port> \
[<glacier2 insecure port>]

    Changes all 4064 ports to the given glacier2 port
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
            glacier2insecure = len(args) > 2 and int(args[2]) or 4063
            change_ports(glacier2, glacier2insecure, registry, revert)
            sys.exit(0)
    except Exception, e:
        print "Failed to set ports: ", e
        sys.exit(1)
