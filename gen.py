#!/usr/bin/env python
# -*- coding: utf-8 -*-


import os
import sys
import glob
import hashlib
import httplib
import urlparse
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


def get_server_status_code(url):
    """
    Download just the header of a URL and
    return the server's status code.
    See: http://pythonadventures.wordpress.com/2010/10/17/check-if-url-exists/ (Josh)
    """
    # http://stackoverflow.com/questions/1140661
    host, path = urlparse.urlparse(url)[1:3]    # elems [1] and [2]
    try:
        conn = httplib.HTTPConnection(host)
        conn.request('HEAD', path)
        status = conn.getresponse().status
        return status
    except StandardError:
        return None

def check_url(url):
    """
    Check if a URL exists without downloading the whole file.
    We only check the URL header.
    See: http://pythonadventures.wordpress.com/2010/10/17/check-if-url-exists/ (Josh)
    """
    # see also http://stackoverflow.com/questions/2924422
    good_codes = [httplib.OK, httplib.FOUND, httplib.MOVED_PERMANENTLY]
    return get_server_status_code(url) in good_codes

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

def repl_all(line, check_http=False):
    for k, v in repl.items():
        line = line.replace(k, v)
    if check_http:
        for part in line.split():
            if part.startswith("href="):
                part = part[6:]
                part = part[0: part.find('"')]
                if not check_url(part):
                    raise Exception("Found bad URL: %s", part)
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
    print repl_all(line, check_http=True),
