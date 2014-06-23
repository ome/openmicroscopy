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

import logging

LOGGER = logging.getLogger("omero.install.memory")


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
        self.sources = dict()
        self.settings_map = settings_map
        self.default_map = default_map
        for name, default in (
                ("strategy", AdaptiveStrategy),
                ("append", ""),
                ("perm_gen", "128m"),
                ("heap_dump", "off"),
                ("heap_size", "512m"),
                ("use_total", None),
                ("max_total", "16000"),
                ("min_total", "3413"),
        ):
            setattr(self, name, self.lookup(name, default))

        if default_map is not None:
            self.collapsed = dict(default_map)
        else:
            self.collapsed = dict()

        if settings_map is not None:
            for k, v in settings_map.items():
                self.collapsed[k] = v

    def lookup(self, name, default=None):
        if self.settings_map and name in self.settings_map:
            self.sources[name] = "server"
            return self.settings_map[name]
        elif self.default_map and name in self.default_map:
            self.sources[name] = "global"
            return self.default_map[name]
        else:
            self.sources[name] = "default"
            return default

    def overwrite(self, key, value, always=False):
        if self.was_set(key) and not always:
            # Then we leave it as the user requested
            return
        # Otherwise, we want to update the value
        setattr(self, key, value)
        self.sources[key] = "overwrite"

    def was_set(self, key):
        return self.sources.get(key, "unknown") != "default"

    def get_strategy(self):
        return STRATEGY_REGISTRY.get(self.strategy, self.strategy)

    def __str__(self):
        return 'Settings(%s)' % self.collapsed


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
        return [x for x in values if x]


class ManualStrategy(Strategy):
    """
    Simplest strategy which assumes all values have
    been set and simply uses them or their defaults.
    """


class PercentStrategy(Strategy):
    """
    Strategy based on a percent of available memory.
    """

    PERCENT_DEFAULTS = (
        ("blitz", 15),
        ("pixeldata", 15),
        ("indexer", 10),
        ("repository", 10),
        ("other", 1),
    )

    def __init__(self, name, settings=None):
        super(PercentStrategy, self).__init__(name, settings)
        self.defaults = dict(self.PERCENT_DEFAULTS)
        self.settings.overwrite("heap_size",
                                "%sm" % self.calculate_heap_size())

    def get_percent(self):
        other = self.defaults.get("other", "1")
        default = self.defaults.get(self.name, other)
        percent = self.settings.lookup("percent", default)
        return percent

    def calculate_heap_size(self, method=None):
        """
        Re-calculates the appropriate heap size based on some metric
        and sets the value in the settings.
        """
        if method is None:
            method = self.system_memory_mb
        available, active, total = method()

        percent = self.get_percent()
        calculated = active * int(percent) / 100
        return calculated

    def system_memory_mb(self):
        """
        Returns a tuple, in MB, of available and total memory.
        """

        available, total = None, None
        if self.settings.use_total is not None:
            total = int(self.settings.use_total)
            available = total
        else:
            pymem = self._system_memory_mb_psutil()
            if pymem is not None:
                available, total = pymem
            else:
                available, total = self._system_memory_mb_java()

        max_total = int(self.settings.max_total)
        min_total = int(self.settings.min_total)
        active = max(min(total, max_total), min_total)
        return available, active, total

    def _system_memory_mb_psutil(self):
        try:
            import psutil
            pymem = psutil.virtual_memory()
            return (pymem.free/1000000, pymem.total/1000000)
        except ImportError:
            LOGGER.debug("No psutil installed")
            return None

    def _system_memory_mb_java(self):
        import omero.cli
        import omero.java

        # Copied from db.py. Needs better dir detection
        cwd = omero.cli.CLI().dir
        server_jar = cwd / "lib" / "server" / "server.jar"
        cmd = ["ome.services.util.JvmSettingsCheck", "--psutil"]
        p = omero.java.popen(["-cp", str(server_jar)] + cmd)
        o, e = p.communicate()

        if p.poll() != 0:
            LOGGER.warn("Failed to invoke java:\nout:%s\nerr:%s",
                        o, e)

        rv = dict()
        for line in o.split("\n"):
            line = line.strip()
            if not line:
                continue
            parts = line.split(":")
            if len(parts) == 1:
                parts.append("")
            rv[parts[0]] = parts[1]

        try:
            free = long(rv["Free"]) / 1000000
        except:
            LOGGER.warn("Failed to parse Free from %s", rv)
            free = 2000

        try:
            total = long(rv["Total"]) / 1000000
        except:
            LOGGER.warn("Failed to parse Total from %s", rv)
            total = 4000

        return (free, total)

    def usage_table(self, min=10, max=20):
        total_mb = [2**x for x in range(min, max)]
        for total in total_mb:
            method = lambda: (total, total, total)
            yield total, self.calculate_heap_size(method)


class AdaptiveStrategy(PercentStrategy):
    """
    PercentStrategy which modifies its default
    values based on the available memory
    """

    def __init__(self, name, settings=None):
        super(AdaptiveStrategy, self).__init__(name, settings)
        available, active, total = self.system_memory_mb()  # max_total is set

        if settings is None:
            settings = Settings()

        if total <= 4000:
            settings.overwrite("max_total", .75 * total)
            if total >= 2000:
                settings.overwrite("perm_gen", "256m")
        elif total <= 8000:
            settings.overwrite("max_total", .50 * total)
            settings.overwrite("perm_gen", "512m")
        else:
            settings.overwrite("max_total", .33 * total)
            settings.overwrite("perm_gen", "1g")


STRATEGY_REGISTRY["manual"] = ManualStrategy
STRATEGY_REGISTRY["percent"] = PercentStrategy
STRATEGY_REGISTRY["adaptive"] = AdaptiveStrategy


def adjust_settings(config, template_xml,
                    blitz=None, indexer=None,
                    pixeldata=None, repository=None):
    """
    Takes an omero.config.ConfigXml object and adjusts
    the memory settings. Primary entry point to the
    memory module.
    """

    from xml.etree.ElementTree import Element
    from collections import defaultdict

    options = dict()
    for template in template_xml.findall("server-template"):
        for server in template.findall("server"):
            for option in server.findall("option"):
                o = option.text
                if o.startswith("MEMORY:"):
                    options[o[7:]] = (server, option)

    rv = defaultdict(list)
    m = config.as_map()
    loop = (("blitz", blitz), ("indexer", indexer),
            ("pixeldata", pixeldata), ("repository", repository))

    for name, StrategyType in loop:
        prefix = "omero.mem.%s" % name
        specific = strip_prefix(m, prefix=prefix)
        defaults = strip_prefix(m, prefix="omero.mem")
        settings = Settings(specific, defaults)
        rv[name].append(settings)
        if StrategyType is None:
            StrategyType = settings.get_strategy()

        if not callable(StrategyType):
            raise Exception("Bad strategy: %s" % StrategyType)

        strategy = StrategyType(name, settings)
        settings = strategy.get_memory_settings()
        server, option = options[name]
        idx = 0
        for v in settings:
            rv[name].append(v)
            if idx == 1:
                option.text = v
            else:
                elem = Element("option")
                elem.text = v
                server.insert(idx, elem)
            idx += 1
    return rv


def usage_charts(path,
                 min=8, max=16,
                 Strategy=AdaptiveStrategy, name="blitz"):
    # See http://matplotlib.org/examples/pylab_examples/anscombe.html

    from pylab import array
    from pylab import axis
    from pylab import gca
    from pylab import subplot
    from pylab import plot
    from pylab import setp
    from pylab import savefig
    from pylab import text

    points = 50
    x = array([2**(x/points)/1000 \
               for x in range(min*points, max*points)])
    y_configs = (
        (Settings({}), 'A'),
        (Settings({"percent": "20"}), 'B'),
        (Settings({}), 'C'),
        (Settings({"max_total": "10000"}), 'D'),
    )

    def f(cfg):
        s = Strategy(name, settings=cfg[0])
        y = []
        for total in x:
            method = lambda: (total, total, total)
            y.append(s.calculate_heap_size(method))
        return y

    y1 = f(y_configs[0])
    y2 = f(y_configs[1])
    y3 = f(y_configs[2])
    y4 = f(y_configs[3])

    axis_values = [0, 20, 2, 20]
    tick_values = (4, 8, 12, 16)

    def ticks_f():
        setp(gca(), yticks=tick_values, xticks=tick_values)

    def text_f(which):
        cfg = y_configs[which]
        s = cfg[0]
        txt = "%s %sGB\n" % (
            cfg[1], int(s.max_total)/1000,
        )
        text(2, 14, txt, fontsize=20)

    subplot(221)
    plot(x, y1)
    axis(axis_values)
    text_f(0)
    ticks_f()

    subplot(222)
    plot(x, y2)
    axis(axis_values)
    text_f(1)
    ticks_f()

    subplot(223)
    plot(x, y3)
    axis(axis_values)
    text_f(2)
    ticks_f()

    subplot(224)
    plot(x, y4)
    axis(axis_values)
    text_f(3)
    ticks_f()

    savefig(path)
