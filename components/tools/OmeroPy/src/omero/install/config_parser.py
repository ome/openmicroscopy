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


class Header(object):
    def __init__(self, name, reference=None, description=""):
        """Initialize new configuration property"""
        self.name = name
        self.reference = reference
        self.description = description

    def get_reference(self):

        if not self.reference:
            return self.name.lower()
        else:
            return self.reference

DB_HEADER = Header("Database", reference="db")
FS_HEADER = Header("Binary repository", reference="fs")
GRID_HEADER = Header("Grid", reference="grid")
ICE_HEADER = Header("Ice", reference="ice")
LDAP_HEADER = Header("LDAP", reference="ldap")
JVM_HEADER = Header("JVM", reference="jvm")
MISC_HEADER = Header("Misc", reference="misc")
PERFORMANCE_HEADER = Header("Performance", reference="performance")
SCRIPTS_HEADER = Header("Scripts", reference="scripts")
SECURITY_HEADER = Header("Security", reference="security")

HEADER_MAPPING = {
    "omero.data": FS_HEADER,
    "omero.db": DB_HEADER,
    "omero.cluster": GRID_HEADER,
    "omero.grid": GRID_HEADER,
    "omero.checksum": FS_HEADER,
    "omero.fs": FS_HEADER,
    "omero.managed": FS_HEADER,
    "omero.ldap": LDAP_HEADER,
    "omero.jvmcfg": JVM_HEADER,
    "omero.sessions": PERFORMANCE_HEADER,
    "omero.threads": PERFORMANCE_HEADER,
    "omero.throttling": PERFORMANCE_HEADER,
    "omero.launcher": SCRIPTS_HEADER,
    "omero.process": SCRIPTS_HEADER,
    "omero.scripts": SCRIPTS_HEADER,
    "omero.security": SECURITY_HEADER,
    "omero.resetpassword": SECURITY_HEADER,
    "omero.upgrades": MISC_HEADER,
    "Ice": ICE_HEADER,
}


TOP = \
    """.. This file is auto-generated from omero.properties. DO NOT EDIT IT

Configuration properties glossary
=================================

.. contents::
  :depth: 1
  :local:

.. _introduction_configuration:

Introduction
------------

The primary form of configuration is via the use of key/value properties,
stored in :file:`etc/grid/config.xml` and read on server startup. Backing up
and copying these properties is as easy as copying this file to a new server
version.

The :source:`etc/omero.properties` file of your distribution defines all the
default configuration properties used by the server. Changes made to the file
are *not* recognized by the server. Instead, configuration options can be set
using the :omerocmd:`config set` command:

::

    $ bin/omero config set <parameter> <value>

When supplying a value with spaces or multiple elements, use **single
quotes**. The quotes will not be saved as part of the value (see below).

To remove a configuration option (to return to default values where
mentioned), simply omit the value:

::

    $ bin/omero config set <parameter>

These options will be stored in a file: ``etc/grid/config.xml`` which
you can read for reference. **DO NOT** edit this file directly.

You can also review all your settings by using:

::

    $ bin/omero config get

which should return values without quotation marks.

A final useful option of :omerocmd:`config edit` is:

::

    $ bin/omero config edit

which will allow for editing the configuration in a system-default text
editor.

.. note::
    Please use the **escape sequence** ``\\"`` for nesting double quotes (e.g.
    ``"[\\"foo\\", \\"bar\\"]"``) or wrap with ``'`` (e.g. ``'["foo",
    "bar"]'``).

Examples of doing this are on the main :doc:`Unix <unix/server-installation>`
and :doc:`Windows <windows/server-installation>` pages, as well as the
:doc:`LDAP installation <server-ldap>` page.

.. _core_configuration:

Core
----

- :property:`omero.data.dir`
- :property:`omero.db.host`
- :property:`omero.db.name`
- :property:`omero.db.pass`

"""

HEADER = \
    """.. _%(reference)s_configuration:

%(header)s
%(hline)s

%(properties)s"""

BLACK_LIST = ("##", "versions", "omero.upgrades")

STOP = "### END"

import os
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


def underline(size):
    """Create underline for reStructuredText headings"""
    return '^' * size


class Property(object):

    def __init__(self, key=None, val=None, txt=""):
        """Initialize new configuration property"""
        self.key = key
        self.val = val
        self.txt = txt

    def append(self, line):
        """Append line to property description"""
        dbg("append:" + line)
        self.txt += line
        self.txt += "\n"

    def detect(self, line):
        dbg("detect:" + line)
        idx = line.index("=")
        self.key = line[0:idx]
        self.val = line[idx + 1:]

    def cont(self, line):
        dbg("cont:  " + line)
        if self.key is None:
            fail("key is none on line: " + line)
        self.val += line

    def __str__(self):
        return ("Property(key='%s', val='%s', txt='%s')"
                % (self.key, self.val, self.txt))


IN_PROGRESS = "in_progress_action"
ESCAPED = "escaped_action"


class PropertyParser(object):

    def __init__(self):
        """Initialize a property set"""
        self.properties = []
        self.curr_p = None
        self.curr_a = None

    def parse_file(self, argv=None):
        """Parse the properties from the input configuration file"""
        try:
            for line in fileinput.input(argv):
                if line.endswith("\n"):
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
                elif "=" in line and self.curr_a != ESCAPED:
                    self.detect(line)
                elif line.endswith("\\"):
                    self.cont(line[:-1])
                else:
                    self.cont(line)
            self.cleanup()  # Handle no newline at end of file
        finally:
            fileinput.close()
        return self.properties

    def black_list(self, line):
        for x in BLACK_LIST:
            if line.startswith(x):
                return True

    def cleanup(self):
        if self.curr_p is not None:
            if self.curr_p.key is not None:  # Handle ending '####'
                self.properties.append(self.curr_p)
                self.curr_p = None
                self.curr_a = None

    def init(self):
        if self.curr_p is None:
            self.curr_p = Property()

    def ignore(self):
        self.cleanup()

    def append(self, line):
        self.init()
        # Assume line starts with "# " and strip
        self.curr_p.append(line[2:])

    def detect(self, line):
        if line.endswith("\\"):
            line = line[:-1]
            self.curr_a = ESCAPED
        else:
            self.cleanup()
            self.curr_a = IN_PROGRESS

        self.init()
        self.curr_p.detect(line)
        if self.curr_a != ESCAPED:
            self.cleanup()

    def cont(self, line):
        self.curr_p.cont(line)

    def __iter__(self):
        return iter(self.properties)

    def data(self):
        data = defaultdict(list)
        for x in self:
            if x.key is None:
                raise Exception("Bad key: %s" % x)
            parts = x.key.split(".")
            if parts[0] == "Ice":
                continue
            if parts[0] != "omero":
                raise Exception("Bad key: %s" % x)

            parts = parts[1:]
            data[parts[0]].append(".".join(parts[1:]))
        return data

    def parse_module(self, module='omeroweb.settings'):
        """Parse the properties from the setting module"""

        os.environ['DJANGO_SETTINGS_MODULE'] = module

        from django.conf import settings

        for key, values in sorted(
                settings.CUSTOM_SETTINGS_MAPPINGS.iteritems(),
                key=lambda k: k):

            p = Property()
            global_name, default_value, mapping, description, config = \
                tuple(values)
            p.val = str(default_value)
            p.key = key
            p.txt = (description or "") + "\n"
            self.properties.append(p)

    def headers(self):
        headers = defaultdict(list)
        additional_headers = {}
        for x in self:
            found = False
            for header in HEADER_MAPPING:
                if x.key.startswith(header):
                    headers.setdefault(HEADER_MAPPING[header], []).append(x)
                    found = True
                    break

            if not found and x.key.startswith('omero.'):
                parts = x.key.split(".")
                section = parts[1].title()
                if section not in additional_headers:
                    additional_headers[section] = Header(section)
                headers.setdefault(additional_headers[section], []).append(x)

        for key in headers.iterkeys():
            headers[key].sort(lambda a, b: cmp(a.key, b.key))
        return headers

    def print_defaults(self):
        """Print all keys and their default values"""
        values = ["%s=%s" % (p.key, p.val) for p in self]
        for x in sorted(values):
            print x

    def print_keys(self):
        """Print all keys"""
        data = self.data()
        for k, v in sorted(data.items()):
            print "%s (%s)" % (k, len(v))
            for i in v:
                print "\t", i

    def print_headers(self):
        """Print headers and number of keys"""
        headers = self.headers()
        for k, v in sorted(headers.items(), key=lambda x: x[0].name):
            print "%s (%s)" % (k.name, len(v))

    def print_rst(self):
        """Print configuration in reStructuredText format"""
        print TOP
        headers = self.headers()
        for header in sorted(headers, key=lambda x: x.name):
            properties = ""
            # Filter properties marked as DEVELOPMENT
            props = [p for p in headers[header] if
                     not p.txt.startswith('DEVELOPMENT')]
            for p in props:
                properties += ".. property:: %s\n" % (p.key)
                properties += "\n"
                properties += "%s\n" % p.key
                properties += "%s\n" % underline(len(p.key))
                for line in p.txt.split("\n"):
                    if line:
                        properties += "%s\n" % (line)
                    else:
                        properties += "\n"
                v = p.val
                if not p.val:
                    v = "[empty]"

                if ',' in v and ', ' not in v:
                    properties += "Default: `%s`\n\n" % (
                        ",\n".join(v.split(',')))
                else:
                    properties += "Default: `%s`\n\n" % v

            hline = "-" * len(header.name)
            m = {"header": header.name,
                 "hline": hline,
                 "properties": properties,
                 "reference": header.get_reference()}
            print HEADER % m

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
    pp.parse_file(ns.files)
    pp.parse_module('omeroweb.settings')

    if ns.dbg:
        print "Found:", len(list(pp))

    elif ns.keys:
        pp.print_keys()
    elif ns.headers:
        pp.print_headers()
    elif ns.rst:
        pp.print_rst()
    else:
        raise Exception(ns)
