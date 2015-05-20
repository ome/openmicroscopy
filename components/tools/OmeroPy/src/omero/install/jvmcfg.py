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
from shlex import split

import logging

LOGGER = logging.getLogger("omero.install.jvmcfg")


def strip_dict(map, prefix=("omero", "jvmcfg"), suffix=(), limit=1):
    """
    For the given dictionary, return a copy of the
    dictionary where all entries not matching the
    prefix, suffix, and limit have been removed and
    where all remaining keys have had the prefix and
    suffix stripped. The limit describes the number
    of elements that are allowed in the new key after
    stripping prefix and suffix.
    """
    if isinstance(prefix, StringType):
        prefix = tuple(prefix.split("."))
    if isinstance(suffix, StringType):
        suffix = tuple(suffix.split("."))
    rv = dict()
    if not map:
        return dict()

    def __strip_dict(k, v, prefix, suffix, rv):
        key = tuple(k.split("."))
        ksz = len(key)
        psz = len(prefix)
        ssz = len(suffix)
        if ksz <= (psz + ssz):
            return  # No way to strip if smaller
        if key[0:psz] == prefix and key[ksz-ssz:] == suffix:
            newkey = key[psz:ksz-ssz]
            if len(newkey) == limit:
                newkey = ".".join(newkey)
                rv[newkey] = v

    for k, v in map.items():
        __strip_dict(k, v, prefix, suffix, rv)
    return rv


class StrategyRegistry(dict):

    def __init__(self, *args, **kwargs):
        super(dict, self).__init__(*args, **kwargs)


STRATEGY_REGISTRY = StrategyRegistry()


class Settings(object):
    """
    Container for the config options found in etc/grid/config.xml
    """

    def __init__(self, server_values=None, global_values=None):
        if server_values is None:
            self.__server = dict()
        else:
            self.__server = server_values
        if global_values is None:
            self.__global = dict()
        else:
            self.__global = global_values
        self.__static = {
            "strategy": PercentStrategy,
            "append": "",
            "perm_gen": "128m",
            "heap_dump": "off",
            "heap_size": "512m",
            "system_memory": None,
            "max_system_memory": "48000",
            "min_system_memory": "3414",
        }
        self.__manual = dict()

    def __getattr__(self, key):
        return self.lookup(key)

    def lookup(self, key, default=None):
        if key in self.__manual:
            return self.__manual[key]
        elif key in self.__server:
            return self.__server[key]
        elif key in self.__global:
            return self.__global[key]
        elif key in self.__static:
            return self.__static[key]
        else:
            return default

    def overwrite(self, key, value, always=False):
        if self.was_set(key) and not always:
            # Then we leave it as the user requested
            return
        else:
            self.__manual[key] = value

    def was_set(self, key):
        return key in self.__server or key in self.__global

    def get_strategy(self):
        return STRATEGY_REGISTRY.get(self.strategy, self.strategy)

    def __str__(self):
        rv = dict()
        rv.update(self.__server)
        rv.update(self.__global)
        if not rv:
            rv = ""
        return 'Settings(%s)' % rv


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

    # Memory helpers

    def system_memory_mb(self):
        """
        Returns a tuple, in MB, of available, active, and total memory.

        "total" memory is found by calling to first a Python library
        (if installed) and otherwise a Java class. If
        "system_memory" is set, it will short-circuit both methods.

        "active" memory is set to "total" but limited by "min_system_memory"
        and "max_system_memory".

        "available" may not be accurate, and in some cases will be
        set to total.
        """

        available, total = None, None
        if self.settings.system_memory is not None:
            total = int(self.settings.system_memory)
            available = total
        else:
            pymem = self._system_memory_mb_psutil()
            if pymem is not None:
                available, total = pymem
            else:
                available, total = self._system_memory_mb_java()

        max_system_memory = int(self.settings.max_system_memory)
        min_system_memory = int(self.settings.min_system_memory)
        active = max(min(total, max_system_memory), min_system_memory)
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

    # API Getters

    def get_heap_size(self, sz=None):
        if sz is None or self.settings.was_set("heap_size"):
            sz = self.settings.heap_size
        if str(sz).startswith("-X"):
            return sz
        else:
            rv = "-Xmx%s" % sz
            if rv[-1].lower() not in ("b", "k", "m", "g"):
                rv = "%sm" % rv
            return rv

    def get_heap_dump(self):
        hd = self.settings.heap_dump
        if hd == "off":
            return ""
        elif hd in ("on", "cwd", "tmp"):
            return "-XX:+HeapDumpOnOutOfMemoryError"

    def get_perm_gen(self):
        pg = self.settings.perm_gen
        if str(pg).startswith("-XX"):
            return pg
        else:
            return "-XX:MaxPermSize=%s" % pg

    def get_append(self):
        values = []
        if self.settings.heap_dump == "tmp":
            import tempfile
            tmp = tempfile.gettempdir()
            values.append("-XX:HeapDumpPath=%s" % tmp)
        return values + split(self.settings.append)

    def get_memory_settings(self):
        values = [
            self.get_heap_size(),
            self.get_heap_dump(),
            self.get_perm_gen(),
        ]
        if any([x.startswith("-XX:MaxPermSize") for x in values]):
            values.append("-XX:+IgnoreUnrecognizedVMOptions")
        values += self.get_append()
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
        self.use_active = True

    def get_heap_size(self):
        """
        Uses the results of the default settings of
        calculate_heap_size() as an argument to
        get_heap_size(), in other words some percent
        of the active memory.
        """
        sz = self.calculate_heap_size()
        return super(PercentStrategy, self).get_heap_size(sz)

    def get_percent(self):
        other = self.defaults.get("other", "1")
        default = self.defaults.get(self.name, other)
        percent = int(self.settings.lookup("percent", default))
        return percent

    def get_perm_gen(self):
        available, active, total = self.system_memory_mb()
        choice = self.use_active and active or total

        if choice <= 4000:
            if choice >= 2000:
                self.settings.overwrite("perm_gen", "256m")
        elif choice <= 8000:
            self.settings.overwrite("perm_gen", "512m")
        else:
            self.settings.overwrite("perm_gen", "1g")

        return super(PercentStrategy, self).get_perm_gen()

    def calculate_heap_size(self, method=None):
        """
        Re-calculates the appropriate heap size based on the
        value of get_percent(). The "active" memory returned
        by method() will be used by default, but can be modified
        to use "total" via the "use_active" flag.
        """
        if method is None:
            method = self.system_memory_mb

        available, active, total = method()
        choice = self.use_active and active or total

        percent = self.get_percent()
        calculated = choice * int(percent) / 100
        return calculated

    def usage_table(self, min=10, max=20):
        total_mb = [2**x for x in range(min, max)]
        for total in total_mb:
            method = lambda: (total, total, total)
            yield total, self.calculate_heap_size(method)


STRATEGY_REGISTRY["manual"] = ManualStrategy
STRATEGY_REGISTRY["percent"] = PercentStrategy


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

    replacements = dict()
    options = dict()
    for template in template_xml.findall("server-template"):
        for server in template.findall("server"):
            for option in server.findall("option"):
                o = option.text
                if o.startswith("MEMORY:"):
                    options[o[7:]] = (server, option)
            for props in server.findall("properties"):
                for prop in props.findall("property"):
                    name = prop.attrib.get("name", "")
                    if name.startswith("REPLACEMENT:"):
                        replacements[name[12:]] = (server, prop)

    rv = defaultdict(list)
    m = config.as_map()
    loop = (("blitz", blitz), ("indexer", indexer),
            ("pixeldata", pixeldata), ("repository", repository))

    for name, StrategyType in loop:
        if name not in options:
            raise Exception(
                "Cannot find %s option. Make sure templates.xml was "
                "not copied from an older server" % name)

    for name, StrategyType in loop:
        specific = strip_dict(m, suffix=name)
        defaults = strip_dict(m)
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
            if idx == 0:
                option.text = v
            else:
                elem = Element("option")
                elem.text = v
                server.insert(idx, elem)
            idx += 1

        # Now we check for any other properties and
        # put them where the replacement should go.
        for k, v in m.items():
            r = []
            suffix = ".%s" % name
            size = len(suffix)
            if k.endswith(suffix):
                k = k[:-size]
                r.append((k, v))

        server, replacement = replacements[name]
        idx = 0
        for k, v in r:
            if idx == 0:
                replacement.attrib["name"] = k
                replacement.attrib["value"] = v
            else:
                elem = Element("property", name=k, value=v)
                server.append(elem)

    return rv


def usage_charts(path,
                 min=0, max=20,
                 Strategy=PercentStrategy, name="blitz"):
    # See http://matplotlib.org/examples/pylab_examples/anscombe.html

    from pylab import array
    from pylab import axis
    from pylab import gca
    from pylab import subplot
    from pylab import plot
    from pylab import setp
    from pylab import savefig
    from pylab import text

    points = 200
    x = array([2 ** (x / points) / 1000
               for x in range(min*points, max*points)])
    y_configs = (
        (Settings({}), 'A'),
        (Settings({"percent": "20"}), 'B'),
        (Settings({}), 'C'),
        (Settings({"max_system_memory": "10000"}), 'D'),
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

    axis_values = [0, 20, 0, 6]

    def ticks_f():
        setp(gca(), xticks=(8, 16), yticks=(2, 4))

    def text_f(which):
        cfg = y_configs[which]
        # s = cfg[0]
        txt = "%s" % (cfg[1],)
        text(2, 2, txt, fontsize=20)

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
