#!/usr/bin/env venv/bin/python
# -*- coding: utf-8 -*-


import os
import sys
import glob
import hashlib
import httplib
import datetime
import urlparse
import fileinput


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


def repl_all(repl, line, check_http=False):
    for k, v in repl.items():
        line = line.replace(k, v)
    if check_http:
        for part in line.split():
            if part.startswith("href="):
                part = part[6:]
                part = part[0: part.find('"')]
                if not check_url(part):
                    raise Exception("Found bad URL: %s" % part)
    return line


def find_pkg(repl, fingerprint_url, snapshot_path, snapshot_url, name, path, ignore_md5=[]):
    """
    Mutates the repl argument
    """
    path = repl_all(repl, path)
    rv = glob.glob(snapshot_path + path)
    if len(rv) != 1:
        raise Exception("Results!=1 for %s (%s): %s" % (name, path, rv))
    path = rv[0]
    hash = hashfile(path)
    if hash not in ignore_md5:
        furl = "/".join([fingerprint_url, hash, "api", "xml"])
        if not check_url(furl):
            raise Exception("Error accessing %s for %s" % (furl, path))
    repl["@%s@" % name] = snapshot_url + path[len(snapshot_path):]
    repl["@%s_MD5@" % name] = hash
    repl["@%s_BASE@" % name] = os.path.basename(path)
