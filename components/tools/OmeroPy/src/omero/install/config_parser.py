#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
Parser for the omero.properties file to generate RST
mark up.
"""

HEADER_MAPPING = {
    "data": "Core",
    "db": "Core",
    "cluster": "Grid",
    "grid": "Grid",
    "checksum": "FS",
    "fs": "FS",
    "managed": "FS",
    "ldap": "LDAP",
    "sessions": "Performance",
    "threads": "Performance",
    "throttling": "Performance",
    "launcher": "Scripts",
    "process": "Scripts",
    "scripts": "Scripts",
    "security": "Security",
    "resetpassword": "Security",
    "upgrades": "Misc",
}


TOP = \
    """Configuration properties
========================

The primary form of configuration is via the use of key/value properties,
stored in :file:`etc/grid/config.xml` and read on server startup. Backing up
and copying these properties is as easy as copying this file to a new server
version.

The :source:`etc/omero.properties` file of your distribution defines all the
default configuration properties used by the server. Changes made to the file
are *not* recognized by the server. Instead, the :omerocmd:`config` command is
used to change those properties that you would like to customize.

Examples of doing this are on the main :doc:`Unix <unix/server-installation>`
and :doc:`Windows <windows/server-installation>` pages, as well as the
:doc:`LDAP installation <server-ldap>` page.
"""

HEADER = \
    """
%(header)s
%(hline)s

.. glossary::

    %(properties)s

.. _%(reference)s_configuration:
"""

PROPERTY = \
    """
    %(key)s
%(txt)s
        Default: "%(val)s"
"""

BLACK_LIST = ("##", "versions", "omero.upgrades")

STOP = "### END"


import argparse
import fileinput
import logging

from collections import defaultdict


def fail(msg="", debug=0):
    if debug:
        import pdb
        pdb.set_trace()
    else:
        raise Exception(msg)


def dbg(msg):
    logging.debug(msg)


class Property(object):

    def __init__(self, key=None, val=None, txt=""):
        self.key = key
        self.val = val
        self.txt = txt

    def append(self, line):
        dbg("append:" + line)
        self.txt += line
        self.txt += "\n"

    def detect(self, line):
        dbg("detect:" + line)
        idx = line.index("=")
        self.key = line[0:idx]
        self.val = line[idx+1:]

    def cont(self, line):
        dbg("cont:  " + line)
        if self.key is None:
            fail("key is none on line: " + line)
        self.val += line

    def __str__(self):
        return "Property(key='%s', val='%s', txt='%s')" % (
            self.key, self.val, self.txt
        )


class PropertyParser(object):

    def __init__(self):
        self.l = []
        self.p = None
        self.in_progress = False

    def parse(self, argv=None):
        for line in fileinput.input(argv):
            line = line[:-1]

            if line.startswith(STOP):
                self.cleanup()
                break
            if self.black_list(line):
                self.cleanup()
                continue
            elif not line.strip():
                self.cleanup()
                continue
            elif line.startswith("#"):
                self.append(line)
            elif "=" in line:
                self.detect(line)
            elif line.endswith("\\"):
                self.cont(line[1:])
            else:
                if self.in_progress:
                    self.cont(line)
                else:
                    fail("unknown line: %s" % line)
        return self.l

    def black_list(self, line):
        for x in BLACK_LIST:
            if line.startswith(x):
                return True

    def cleanup(self):
        if self.p is not None:
            if self.p.key is not None:  #: Handle ending '####'
                self.l.append(self.p)
                self.p = None
                self.in_progress = False

    def init(self):
        if self.p is None:
            self.p = Property()

    def ignore(self):
        self.cleanup()

    def append(self, line):
        self.init()
        self.p.append(line[1:])

    def detect(self, line):
        if self.in_progress:
            self.cleanup()
        self.init()
        self.p.detect(line)
        self.in_progress = True

    def cont(self, line):
        self.p.cont(line)

    def __iter__(self):
        return iter(self.l)

    def data(self):
        data = defaultdict(list)
        for x in self:
            if x.key is None:
                raise Exception("Bad key: %s" % x)
            parts = x.key.split(".")
            if parts[0] != "omero":
                raise Exception("Bad key: %s" % x)

            parts = parts[1:]
            data[parts[0]].append(".".join(parts[1:]))
        return data

    def headers(self):
        data = list(self)
        data.sort(lambda a, b: cmp(a.key, b.key))
        headers = defaultdict(list)
        for x in data:
            key = x.key.split(".")[1]
            key = HEADER_MAPPING.get(key, key.title())
            headers[key].append(x)
        return headers


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    g = ap.add_mutually_exclusive_group()
    g.add_argument("--rst", action="store_true")
    g.add_argument("--dbg", action="store_true")
    g.add_argument("--keys", action="store_true")
    g.add_argument("--headers", action="store_true")
    ap.add_argument("files", nargs="+")
    ns = ap.parse_args()


    if ns.dbg:
        logging.basicConfig(level=10)
    else:
        logging.basicConfig(level=20)


    pp = PropertyParser()
    pp.parse(ns.files)

    if ns.dbg:
        print "Found:", len(list(pp))

    elif ns.keys:
        data = pp.data()
        for k, v in sorted(data.items()):
            print "%s (%s)" % (k, len(v))
            for i in v:
                print "\t", i

    elif ns.headers:
        headers = pp.headers()
        for k, v in sorted(headers.items()):
            print "%s (%s)" % (k, len(v))

    elif ns.rst:
        print TOP
        headers = pp.headers()
        for header in sorted(headers):
            properties = ""

            for p in headers[header]:
                t = p.txt
                t = ["%6s%s" % (" ", x) for x in t.split("\n")]
                t = "\n".join(t)
                m = {"key": p.key,
                     "txt": t,
                     "val": p.val}
                properties += PROPERTY % m

            hline = "-" * len(header)
            m = {"header": header,
                 "hline": hline,
                 "properties": properties,
                 "reference": header.lower()}
            print HEADER % m

    else:
        raise Exception(ns)
