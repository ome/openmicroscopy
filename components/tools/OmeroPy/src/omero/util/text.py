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

class TableBuilder(object):
    """
    OMERO-addition to make working with Tables easier
    """

    def __init__(self, *headers):
        self.headers = list(headers)
        self.results = [[] for x in self.headers]

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
            raise ValueError("Size mismatch: %s != %s" % (len(items), len(self.headers)))

        # Fill in all values, even if missing
        for idx in range(len(self.results)):
            value = None
            if idx < len(items):
                value = items[idx]
            self.results[idx].append(value)

        size = len(self.results[0])
        for k, v in by_name.items():
            if k not in self.headers:
                raise KeyError("%s not in %s" % (k, self.headers))
            idx = self.headers.index(k)
            self.results[idx][-1] = by_name[self.headers[idx]]

    def build(self):
        columns = []
        for i, x in enumerate(self.headers):
            columns.append(Column(x, self.results[i]))
        return Table(*columns)

    def __str__(self):
        return str(self.build())


class ALIGN:
    LEFT, RIGHT = '-', ''


class Column(list):

    def __init__(self, name, data, align=ALIGN.LEFT):
        list.__init__(self, data)
        self.name = name
        self.width = max(len(str(x).decode("utf-8")) for x in data + [name])
        self.format = ' %%%s%ds ' % (align, self.width)


class Table:

    def __init__(self, *columns):
        self.columns = columns
        self.length = max(len(x) for x in columns)

    def get_row(self, i=None):
        for x in self.columns:
            if i is None:
                yield x.format % x.name
            else:
                try:
                    x[i].decode("ascii")
                except UnicodeDecodeError: # Unicode characters are present
                    yield (x.format % x[i].decode("utf-8")).encode("utf-8")
                except AttributeError: # Unicode characters are present
                    yield x.format % x[i]
                else:
                    yield x.format % x[i]

    def get_rows(self):
        yield '|'.join(self.get_row(None))
        yield "+".join(["-"* (x.width+2) for x in self.columns])
        for i in range(0, self.length):
            yield '|'.join(self.get_row(i))
        yield "(%s %s)" % (self.length, (self.length == 1 and "row" or "rows"))

    def __str__(self):
        return '\n'.join(self.get_rows())


