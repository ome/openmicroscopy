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
from omero.constants import namespaces
import re


# Namespace for Bulk-Annotations configuration files
NSBULKANNOTATIONSCONFIG = namespaces.NSBULKANNOTATIONS + "/config"

# Namespace for raw input for a Bulk-Annotations table
NSBULKANNOTATIONSRAW = namespaces.NSBULKANNOTATIONS + "/raw"


class GroupConfig(object):

    def __init__(self, namespace, column_cfg):
        self.namespace = namespace
        self.columns = column_cfg

    def __eq__(self, other):
        return (isinstance(other, self.__class__)
                and self.__dict__ == other.__dict__)


class BulkAnnotationConfiguration(object):
    """
    Parent class for handling bulk-annotation column configurations
    """

    REQUIRED = set(["name"])
    OPTIONAL = set([
        "clientname",
        "clientvalue",
        "includeclient",
        "position",
        "include",
        "split",
        "type",
        "visible",
        "omitempty",
        ])
    GROUPREQUIRED = set(["namespace", "columns"])

    def __init__(self, default_cfg, column_cfgs):
        """
        :param default_cfg: Dict of default values, can be empty
        :params column_cfgs: Array of dicts of column configurations
        """
        self.default_cfg = self.get_default_cfg(default_cfg)
        self.column_cfgs = []
        self.group_cfgs = []
        if column_cfgs:
            for c in column_cfgs:
                cfg = self.get_column_config(c)
                if isinstance(cfg, GroupConfig):
                    self.group_cfgs.append(cfg)
                else:
                    self.column_cfgs.append(cfg)

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

    @classmethod
    def validate_group_config(cls, cfg):
        """
        Check whether a group config section is valid, throws Exception if not
        Not recursive (doesn't check columns for validity)
        """

        keys = set(cfg.keys())

        missing = cls.GROUPREQUIRED.difference(keys)
        if missing:
            raise Exception(
                "Required key(s) missing from group configuration: %s" %
                list(missing))

        if not cfg["namespace"]:
            raise Exception("Empty name in group configuration: %s" % cfg)

        if not cfg["columns"]:
            raise Exception("Empty columns in group configuration: %s" % cfg)

        invalid = keys.difference(cls.GROUPREQUIRED)
        if invalid:
            raise Exception(
                "Invalid key(s) in group configuration: %s" % list(invalid))

    def get_column_config(self, cfg):
        """
        Replace unspecified fields in a column config with defaults
        If this is a group return a GroupConfig object
        """
        if 'group' in cfg:
            gcfg = cfg['group']
            self.validate_group_config(gcfg)
            column_cfgs = [
                self.get_column_config(gc) for gc in gcfg['columns']]
            return GroupConfig(gcfg['namespace'], column_cfgs)

        self.validate_column_config(cfg)
        column_cfg = self.default_cfg.copy()
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

    def transform(self, rowvalues):
        """
        Pass through a table row unchanged
        :param values: A table rows
        :return: A row in the form
                 [(k1, v1), (k2, v2), ...]
        """
        assert len(rowvalues) == len(self.headers)
        return rowvalues


class KeyValueGroupList(BulkAnnotationConfiguration):
    """
    Converts bulk-annotation rows into key-value lists
    """

    def __init__(self, headers, default_cfg, column_cfgs):
        """
        :param headers: A list of table headers
        """
        # TODO: decide what to do with unmentioned columns
        super(KeyValueGroupList, self).__init__(
            default_cfg, column_cfgs)
        self.headers = headers
        self.headerindexmap = dict(
            (b, a) for (a, b) in enumerate(self.headers))
        self.checked = set()
        self.output_configs = self.get_output_configs()

    def get_output_configs(self):
        """
        Get the full set of output column configs including column groups
        The default set of column configs has an empty namespace
        """

        # First process groups in case the default is to include all
        # columns not explicitly specified
        output_configs = []
        for gcfg in self.group_cfgs:
            output_cfg = self.get_group_output_configs(gcfg.columns, False)
            output_configs.append(GroupConfig(gcfg.namespace, output_cfg))

        output_defcfg = self.get_group_output_configs(self.column_cfgs, True)
        output_configs.append(GroupConfig('', output_defcfg))
        return output_configs

    def get_group_output_configs(self, column_cfgs, isdefault):
        """
        Get the full set of output column configs for a single group,
        taking into account specified column positions and columns
        included/excluded according to the defaults:

        - positioned columns are at the specified index (1-based)
        - gaps between positioned columns are filled with unpositioned
          columns in order of
          - Configured but unpositioned columns
          - Unconfigured columns in order of headers (assuming the default
            config is for them to be included)
        - If there are gaps and no remaining columns to be included raise
          an exception
        """
        positioned = {}
        unpositioned = deque()

        # Specified columns
        for cfg in column_cfgs:
            self.checked.add(cfg["name"])
            if not cfg["include"]:
                continue

            pos = cfg["position"]
            if pos > 0:
                if pos in positioned:
                    raise Exception(
                        "Multiple columns specified for position: %d" % pos)
                positioned[pos] = (cfg, self.headerindexmap[cfg["name"]])
            else:
                unpositioned.append((cfg, self.headerindexmap[cfg["name"]]))

        # Unspecified Columns
        if isdefault and self.default_cfg["include"]:
            for name in self.headerindexmap.keys():
                if name not in self.checked:
                    cfg = self.get_column_config({"name": name})
                    assert not isinstance(cfg, GroupConfig)
                    unpositioned.append(
                        (cfg, self.headerindexmap[cfg["name"]]))

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

    def get_transformers(self):
        """
        Return a set of KeyValueListTransformer objects, one for each group
        """
        transformers = [KeyValueListTransformer(
            self.headers, gc.columns, gc.namespace)
            for gc in self.output_configs]
        return transformers


class KeyValueListTransformer(object):
    """
    Converts bulk-annotation rows into key-value lists
    """

    def __init__(self, headers, output_configs, name=None):
        """
        :param headers: A list of table headers
        :param output_configs: A list of output configurations
        :param name: The name for this group of keys/values, optional
        """
        self.headers = headers
        self.output_configs = output_configs
        self.name = name

    @staticmethod
    def transform1(value, cfg):
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
        Transform a table row
        :param rowvalues: A table row
        :return: The transformed rows in the form [(k1, v1), (k2, v2), ...].
                 v* will be a list of length:
                 - 1 in most cases
                 - 0 if `omitempty=True` and value was empty
                 - 1+ if `split` option is enabled
        """
        assert len(rowvalues) == len(self.headers)
        rowkvs = [self.transform1(rowvalues[i], c)
                  for (c, i) in self.output_configs]
        return rowkvs
