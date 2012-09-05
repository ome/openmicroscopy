#!/usr/bin/env python
# -*- coding: utf-8 -*-


import os
import sys
import glob
import hashlib
import fileinput


def usage():
    print "gen.py version build"
    sys.exit(1)

try:
    version = sys.argv[1]
    build = sys.argv[2]
except:
    usage()

repl = {"@VERSION@": version,
        "@BUILD@": build}

def hashfile(filename, blocksize=65536):
    m = hashlib.md5()
    fileobj = open(filename, "r")
    try:
        buf = fileobj.read(blocksize)
        while len(buf) > 0:
            m.update(buf)
            buf = fileobj.read(blocksize)
        return m.hexdigest()
    finally:
        fileobj.close()

def repl_all(line):
    for k, v in repl.items():
        line = line.replace(k, v)
    return line

def find_pkg(name, path):
    path = repl_all(path)
    rv = glob.glob(path)
    if len(rv) != 1:
        raise Exception("Results!=1 for %s (%s): %s", name, path, rv)
    path = rv[0]
    repl["@%s@" % name] = path
    repl["@%s_MD5@" % name] = hashfile(path)
    repl["@%s_BASE@" % name] = os.path.basename(path)

find_pkg("LINUX_CLIENTS", "@VERSION@/pkg/OMERO.clients-@VERSION@-ice33-@BUILD@.mac.zip")
find_pkg("MAC_CLIENTS", "@VERSION@/pkg/OMERO.clients-@VERSION@-ice33-@BUILD@.linux.zip")
find_pkg("WIN_CLIENTS", "@VERSION@/pkg/OMERO.clients-@VERSION@-ice33-@BUILD@.win.zip")
find_pkg("IJ_CLIENTS", "@VERSION@/OMERO.insight-ij-@VERSION@-ice33-@BUILD@.zip")
find_pkg("MATLAB_CLIENTS", "@VERSION@/OMERO.matlab-@VERSION@-ice33-@BUILD@.zip")
find_pkg("SERVER33", "@VERSION@/OMERO.server-@VERSION@-ice33-@BUILD@.zip")
find_pkg("SERVER34", "@VERSION@/OMERO.server-@VERSION@-ice34-@BUILD@.zip")
find_pkg("DOCS", "@VERSION@/OMERO.docs-@VERSION@-ice33-@BUILD@.zip")
find_pkg("VM", "virtualbox/omero-vm-@VERSION@.ova")


for line in fileinput.input(["tmpl.txt"]):
    print repl_all(line),
