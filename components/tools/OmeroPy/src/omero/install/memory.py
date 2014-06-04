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
Automatic configuration of memory settings for Java servers.
"""

from types import StringType


def strip_prefix(map, prefix=("omero", "mem")):
    """
    For the given dictionary, remove a copy of the
    dictionary where all entries not matching the
    prefix have been removed and where all remaining
    keys have had the prefix stripped.
    """
    if isinstance(prefix, StringType):
        prefix = tuple(prefix.split("."))
    rv = dict()
    if not map:
        return dict()

    def __strip_prefix(k, v, prefix, rv):
        key = tuple(k.split("."))
        ksz = len(key)
        psz = len(prefix)
        if ksz <= psz:
            return  # No way to strip if smaller
        if key[0:psz] == prefix:
            newkey = ".".join(key[psz:])
            rv[newkey] = v

    for k, v in map.items():
        __strip_prefix(k, v, prefix, rv)
    return rv


class Settings(object):
    """
    Container for the config options found in etc/grid/config.xml
    """

    def __init__(self, settings_map=None, default_map=None):
        for name, default in (
                ("perm_gen", "128m"),
                ("heap_dump", "off"),
                ("heap_size", "512m")):
            if settings_map and name in settings_map:
                setattr(self, name, settings_map[name])
            elif default_map and name in default_map:
                setattr(self, name, default_map[name])
            else:
                setattr(self, name, default)


class Strategy(object):
    """
    Strategy for calculating memory settings. Primary
    class of the memory module.
    """

    def __init__(self, settings_map=None, default_map=None):
        self.settings = Settings(settings_map, default_map)
        if type(self) == Strategy:
            raise Exception("Must subclass!")

    def get_heap_size(self):
        sz = self.settings.heap_size
        if sz.startswith("-X"):
            return sz
        else:
            return "-Xmx%s" % sz

    def get_heap_dump(self):
        hd = self.settings.heap_dump
        if hd == "off":
            return ""
        elif hd in ("on", "cwd"):
            return "-XX:+HeapDumpOnOutOfMemoryError"
        elif hd in ("tmp",):
            import tempfile
            tmp = tempfile.gettempdir()
            return ("-XX:+HeapDumpOnOutOfMemoryError "
                    "-XX:HeapDumpPath=%s") % tmp

    def get_perm_gen(self):
        pg = self.settings.perm_gen
        if pg.startswith("-XX"):
            return pg
        else:
            return "-XX:MaxPermSize=%s" % pg

    def get_memory_settings(self):
        values = [
            self.get_heap_size(),
            self.get_heap_dump(),
            self.get_perm_gen(),
        ]
        return " ".join([x for x in values if x])


class HardCodedStrategy(Strategy):
    """
    Simplest strategy which assumes all values have
    been set and simply uses them or their defaults.
    """
    pass


def adjust_settings(config, strategy=None):
    """
    Takes an omero.config.ConfigXml object and adjusts
    the memory settings. Primary entry point to the
    memory module.
    """
    pass
