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

class StrategyRegistry(dict):

    def __init__(self, *args, **kwargs):
        super(dict, self).__init__(*args, **kwargs)


STRATEGY_REGISTRY = StrategyRegistry()


class Settings(object):
    """
    Container for the config options found in etc/grid/config.xml
    """

    def __init__(self, settings_map=None, default_map=None):
        self.settings_map = settings_map
        self.default_map = default_map
        for name, default in (
                ("strategy", PercentStrategy),
                ("perm_gen", "128m"),
                ("heap_dump", "off"),
                ("heap_size", "512m"),
                ("append", ""),
                ):
            setattr(self, name, self.lookup(name, default))

    def lookup(self, name, default=None):
        if self.settings_map and name in self.settings_map:
            return self.settings_map[name]
        elif self.default_map and name in self.default_map:
            return self.default_map[name]
        else:
            return default

    def get_strategy(self):
        return STRATEGY_REGISTRY.get(self.strategy, self.strategy)


class Strategy(object):
    """
    Strategy for calculating memory settings. Primary
    class of the memory module.
    """

    def __init__(self, name, settings=None):
        """
        'name' argument should likely be one of:
        ('blitz', 'indexer', 'pixeldata', 'repository')
        """
        if settings is None:
            settings = Settings()
        self.name = name
        self.settings = settings
        if type(self) == Strategy:
            raise Exception("Must subclass!")

    def get_heap_size(self):
        sz = self.settings.heap_size
        if str(sz).startswith("-X"):
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
        if str(pg).startswith("-XX"):
            return pg
        else:
            return "-XX:MaxPermSize=%s" % pg

    def get_append(self):
        return self.settings.append

    def get_memory_settings(self):
        values = [
            self.get_heap_size(),
            self.get_heap_dump(),
            self.get_perm_gen(),
            self.get_append(),
        ]
        return " ".join([x for x in values if x])


class HardCodedStrategy(Strategy):
    """
    Simplest strategy which assumes all values have
    been set and simply uses them or their defaults.
    """


class PercentStrategy(Strategy):
    """
    Strategy based on a percent of available memory.
    """

    PERCENT_DEFAULTS = {
        "blitz": 40,
        "pixeldata": 20,
        "indexer": 10,
        "repository": 10,
        "other": 1,
    }

    def __init__(self, name, settings=None):
        super(PercentStrategy, self).__init__(name, settings)
        self.settings.heap_size = self.calculate_heap_size()

    def calculate_heap_size(self, method=None):
        """
        Re-calculates the appropriate heap size based on some metric
        and sets the value in the settings.
        """
        if method is None:
            method = self.system_memory_mb
        available, total = method()
        other = self.PERCENT_DEFAULTS.get("other", "1")
        default = self.PERCENT_DEFAULTS.get(self.name, other)
        percent = self.settings.lookup("percent", default)
        return total * percent / 100

    def system_memory_mb(self):
        """
        Returns a tuple, in MB, of available and total memory.
        """
        return (4000, 8000)  # FIXME

    def usage_table(self, min=10, max=20):
        total_mb = [2**x for x in range(min, max)]
        for total in total_mb:
            method = lambda: (total, total)
            yield total, self.calculate_heap_size(method)


STRATEGY_REGISTRY["hardcoded"] = HardCodedStrategy
STRATEGY_REGISTRY["percent"] = PercentStrategy


def adjust_settings(config,
                    blitz=None, indexer=None,
                    pixeldata=None, repository=None):
    """
    Takes an omero.config.ConfigXml object and adjusts
    the memory settings. Primary entry point to the
    memory module.
    """
    rv = dict()
    m = config.as_map()
    loop = (("blitz", blitz), ("indexer", indexer),
            ("pixeldata", pixeldata), ("repository", repository))

    for name, StrategyType in loop:
        prefix = "omero.mem.%s" % name
        specific = strip_prefix(m, prefix=prefix)
        defaults = strip_prefix(m, prefix="omero.mem")
        settings = Settings(specific, defaults)
        if StrategyType is None:
            StrategyType = settings.get_strategy()

        strategy = StrategyType(name, settings)
        for x in (config, rv):
            x["%s.option" % prefix] = strategy.get_memory_settings()
            x["%s.option" % prefix] = strategy.get_memory_settings()
    return rv
