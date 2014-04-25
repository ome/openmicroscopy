#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# OMERO Text handling utilities
#
# Copyright 2010 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

#
# The following classes (ALIGN, Column, Table) were originally from
# http://code.activestate.com/recipes/577202-render-tables-for-text-interface/
#


class Style(object):

    NAME = "unknown"

    def headers(self, table):
        return self.SEPARATOR.join(table.get_row(None))

    def width(self, name, decoded_data):
        return max(len(x) for x in decoded_data + [name])

    def __str__(self):
        return self.NAME


class SQLStyle(Style):

    NAME = "sql"
    SEPARATOR = "|"

    def format(self, width, align):
        return ' %%%s%ds ' % (align, width)

    def line(self, table):
        return "+".join(["-" * (x.width+2) for x in table.columns])

    def status(self, table):
        return "(%s %s)" % (
            table.length,
            (table.length == 1 and "row" or "rows"))

    def get_rows(self, table):
        yield self.headers(table)
        yield self.line(table)
        for i in range(0, table.length):
            yield self.SEPARATOR.join(table.get_row(i))
        yield self.status(table)


class PlainStyle(Style):

    NAME = "plain"
    SEPARATOR = ","

    def format(self, width, align):
        return '%s'

    def get_rows(self, table):
        for i in range(0, table.length):
            yield self.SEPARATOR.join(table.get_row(i))


class CSVStyle(PlainStyle):

    NAME = "csv"

    def get_rows(self, table):
        yield self.headers(table)
        for row in PlainStyle.get_rows(self, table):
            yield row


class StyleRegistry(dict):

    def __init__(self):
        dict.__init__(self)
        self["csv"] = CSVStyle()
        self["sql"] = SQLStyle()
        self["plain"] = PlainStyle()


STYLE_REGISTRY = StyleRegistry()


def find_style(style):
    """
    Lookup method for well-known styles by name.
    None may be returned.
    """
    if isinstance(style, Style):
        return style
    else:
        return STYLE_REGISTRY.get(style, None)


def list_styles():
    """
    List the styles that are known by find_style
    """
    return STYLE_REGISTRY.values()


class TableBuilder(object):
    """
    OMERO-addition to make working with Tables easier
    """

    def __init__(self, *headers):
        self.style = SQLStyle()
        self.headers = list(headers)
        self.results = [[] for x in self.headers]

    def set_style(self, style):
        self.style = find_style(style)

    def col(self, name):
        """
        Add a new column and back fill spaces
        """
        self.headers.append(name)
        self.results.append(["" for x in range(len(self.results[0]))])

    def cols(self, names):
        """
        Similar to col() but only adds unknown columns
        """
        for name in names:
            if name not in self.headers:
                self.col(name)

    def row(self, *items, **by_name):

        if len(items) > len(self.headers):
            raise ValueError("Size mismatch: %s != %s" %
                             (len(items), len(self.headers)))

        # Fill in all values, even if missing
        for idx in range(len(self.results)):
            value = None
            if idx < len(items):
                value = items[idx]
            self.results[idx].append(value)

        for k, v in by_name.items():
            if k not in self.headers:
                raise KeyError("%s not in %s" % (k, self.headers))
            idx = self.headers.index(k)
            self.results[idx][-1] = by_name[self.headers[idx]]

    def build(self):
        columns = []
        for i, x in enumerate(self.headers):
            columns.append(Column(x, self.results[i], style=self.style))
        table = Table(*columns)
        table.set_style(self.style)
        return table

    def __str__(self):
        return str(self.build())


class ALIGN:
    LEFT, RIGHT = '-', ''


class Column(list):

    def __init__(self, name, data, align=ALIGN.LEFT, style=SQLStyle()):
        def tostring(x):
            try:
                return str(x).decode("utf-8")
            except UnicodeDecodeError:
                return '<Invalid UTF-8>'

        decoded = [tostring(d) for d in data]
        list.__init__(self, decoded)
        self.name = name
        self.width = style.width(name, decoded)
        self.format = style.format(self.width, align)


class Table:

    def __init__(self, *columns):
        self.style = SQLStyle()
        self.columns = columns
        self.length = max(len(x) for x in columns)

    def set_style(self, style):
        self.style = find_style(style)

    def get_row(self, i=None):
        for x in self.columns:
            if i is None:
                yield x.format % x.name
            else:
                try:
                    x[i].decode("ascii")
                except UnicodeDecodeError:  # Unicode characters are present
                    yield (x.format % x[i].decode("utf-8")).encode("utf-8")
                except AttributeError:  # Unicode characters are present
                    yield x.format % x[i]
                else:
                    yield x.format % x[i]

    def get_rows(self):
        for row in self.style.get_rows(self):
            yield row

    def __str__(self):
        return '\n'.join(self.get_rows())
