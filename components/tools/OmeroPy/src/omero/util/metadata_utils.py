#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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
Utilities for manipulating bulk-annotations

Includes classes to help with basic data-munging (TODO), and for formatting
data for clients.
"""

from collections import deque
import re


class BulkAnnotationConfiguration(object):
    """
    Parent class for handling bulk-annotation column configurations
    """

    REQUIRED = {"name"}
    OPTIONAL = {
        "clientname",
        "clientvalue",
        "includeclient",
        "position",
        "include",
        "split",
        "type",
        "visible",
        "omitempty",
        }

    def __init__(self, default_cfg, column_cfgs):
        """
        :param default_cfg: Dict of default values, can be empty
        :params column_cfgs: Array of dicts of column configurations
        """
        self.defaults = self.get_default_cfg(default_cfg)
        self.column_cfgs = [self.get_column_config(c) for c in column_cfgs]

    @staticmethod
    def get_default_cfg(cfg):
        """
        Get the default column configuration, fill in unspecified fields
        """
        default_defaults = {
            "clientvalue": None,
            "includeclient": True,
            "include": True,
            "split": False,
            "type": "string",
            "visible": True,
            "position": -1,
            "clientname": None,
            "omitempty": False
        }
        if not cfg:
            cfg = {}

        invalid = set(cfg.keys()).difference(default_defaults.keys())
        if invalid:
            raise Exception(
                "Invalid key(s) in column defaults: %s" % list(invalid))

        defaults = default_defaults.copy()
        defaults.update(cfg)
        return defaults

    @classmethod
    def validate_column_config(cls, cfg):
        """
        Check whether a column config section is valid, throws Exception if not
        """

        keys = set(cfg.keys())

        missing = cls.REQUIRED.difference(keys)
        if missing:
            raise Exception(
                "Required key(s) missing from column configuration: %s" %
                list(missing))

        if not cfg["name"]:
            raise Exception("Empty name in column configuration: %s" % cfg)

        invalid = keys.difference(cls.OPTIONAL.union(cls.REQUIRED))
        if invalid:
            raise Exception(
                "Invalid key(s) in column configuration: %s" % list(invalid))

    @classmethod
    def validate_filled_column_config(cls, cfg):
        """
        Check whether a column config section is valid after filling all
        optional fields with defaults
        """
        cls.validate_column_config(cfg)
        allfields = cls.OPTIONAL.union(cls.REQUIRED)
        keys = set(cfg.keys())

        missing = allfields.difference(keys)
        if missing:
            raise Exception("Required key(s) missing from column+defaults "
                            "configuration: %s" % list(missing))

        if cfg["includeclient"] and not cfg["include"]:
            raise Exception("Option `includeclient` requires option `include`")

        if not isinstance(cfg["position"], int):
            raise Exception("Option `position` must be an int")

        if cfg["clientvalue"]:
            subbed = re.sub("\{\{\s*value\s*\}\}", '', cfg["clientvalue"])
            m = re.search("\{\{[\s\w]*}\}", subbed)
            if m:
                raise Exception(
                    "clientvalue template parameter not found: %s" % m.group())

    def get_column_config(self, cfg):
        """
        Replace unspecified fields in a column config with defaults
        """
        self.validate_column_config(cfg)
        column_cfg = self.defaults.copy()
        column_cfg.update(cfg)
        self.validate_filled_column_config(column_cfg)
        return column_cfg


class KeyValueListPassThrough(object):
    """
    Converts bulk-annotation rows into key-value lists without any
    transformation
    """

    def __init__(self, headers, default_cfg=None, column_cfgs=None):
        """
        :param headers: A list of table headers
        """
        self.headers = headers

    def transform_gen(self, values):
        """
        Generator which transforms table rows
        :param values: An iterable of table rows
        :return: A generator which returns rows in the form
                 [(k1, v1), (k2 v2), ...]
        """
        for rowvals in values:
            assert len(rowvals) == len(self.headers)
            yield zip(self.headers, rowvals)


class KeyValueListTransformer(BulkAnnotationConfiguration):
    """
    Converts bulk-annotation rows into key-value lists
    """

    def __init__(self, headers, default_cfg, column_cfgs):
        """
        :param headers: A list of table headers
        """
        # TODO: decide what to do with unmentioned columns
        super(KeyValueListTransformer, self).__init__(
            default_cfg, column_cfgs)
        self.headers = headers
        self.output_configs = self.get_output_configs()
        print self.output_configs[0]
        print self.output_configs[1]

    def get_output_configs(self):
        """
        Get the full set of output column configs, taking into account
        specified column positions and columns include/excluded according
        to the defaults
        """
        headerindexmap = dict((b, a) for (a, b) in enumerate(self.headers))
        positioned = {}
        unpositioned = deque()

        checked = set()

        # Specified columns
        for cfg in self.column_cfgs:
            checked.add(cfg["name"])
            if not cfg["include"]:
                print "Ignoring %s" % cfg["name"]
                continue
            print "Including %s" % cfg["name"]

            pos = cfg["position"]
            if pos > 0:
                if pos in positioned:
                    raise Exception(
                        "Multiple columns specified for position: %d" % pos)
                positioned[pos] = (cfg, headerindexmap[cfg["name"]])
            else:
                unpositioned.append((cfg, headerindexmap[cfg["name"]]))

        # Unspecified Columns
        for name in self.headers:
            if name not in checked and self.defaults["include"]:
                cfg = self.get_column_config({"name": name})
                unpositioned.append((cfg, headerindexmap[cfg["name"]]))

        # The dance- put positioned columns in the right place and fill
        # any gaps with unpositioned columns (otherwise append to end)
        output_configs = []
        if positioned:
            for pos in xrange(1, max(positioned.keys()) + 1):
                if pos in positioned:
                    output_configs.append(positioned.pop(pos))
                elif not unpositioned:
                    raise Exception("No column found for position: %d" % pos)
                else:
                    output_configs.append(unpositioned.popleft())
        output_configs.extend(unpositioned)
        return output_configs

    def transform1(self, value, cfg):
        """
        Process a single value corresponding to a single table row-column

        :return: The key and a list of [value]. In general [values] will be
                 a single item unless `split` is specified in cfg in which
                 case there may be multiple.
        """

        def valuesub(v, cv):
            return re.sub("\{\{\s*value\s*\}\}", v, cv)

        key = cfg["name"]
        if cfg["clientname"]:
            key = cfg["clientname"]

        if not cfg["visible"]:
            key = "__%s" % key

        if cfg["split"]:
            values = [v.strip() for v in value.split(cfg["split"])]
        else:
            values = [value]

        if cfg["omitempty"]:
            values = [v for v in values if v is not None and (
                not isinstance(v, basestring) or v.strip())]

        if cfg["clientvalue"]:
            values = [valuesub(v, cfg["clientvalue"]) for v in values]
        return key, values

    def transform(self, rowvalues):
        """
        Transform a table rows
        :param rowvalues: A table row
        :return: The transformed rows in the form [(k1, v1), (k2 v2), ...].
                 v* will be a list of length:
                 - 1 in most cases
                 - 0 if `omitempty=True` and value was empty
                 - 1+ if `split` option is enabled
        """
        assert len(rowvalues) == len(self.headers)
        rowkvs = [self.transform1(rowvalues[i], c)
                  for (c, i) in self.output_configs]
        return rowkvs


def print_kvs(headers, values, default_cfg, column_cfgs):
    tr = KeyValueListTransformer(headers, default_cfg, column_cfgs)
    n = -1
    for row in values:
        n += 1
        transformed = tr.transform(row)
        for k, vs in transformed:
            if not isinstance(vs, list):
                vs = [vs]
            for v in vs:
                print "% 2d % 10s : %s" % (n, k, v)
