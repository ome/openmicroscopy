#!/usr/bin/env python
# encoding: utf-8
"""
Populate bulk metadata tables from delimited text files.
"""

#
#  Copyright (C) 2011 University of Dundee. All rights reserved.
#
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#


import exceptions
import tempfile
import logging
import time
import sys
import csv
import re
from threading import Thread
from StringIO import StringIO
from getpass import getpass
from getopt import getopt, GetoptError
from Queue import Queue

import omero.clients
from omero.rtypes import rdouble, rstring, rint
from omero.model import DatasetAnnotationLink, DatasetI, FileAnnotationI, \
                        OriginalFileI, PlateI, PlateAnnotationLinkI
from omero.grid import ImageColumn, LongColumn, StringColumn, WellColumn
from omero.util.temp_files import create_path, remove_path
from omero import client

from populate_roi import ThreadPool

# Handle Python 2.5 built-in ElementTree
try:
        from xml.etree.cElementTree import XML, Element, SubElement, ElementTree, dump, iterparse
except ImportError:
        from cElementTree import XML, Element, SubElement, ElementTree, dump, iterparse

log = logging.getLogger("omero.util.populate_metadata")

def usage(error):
    """Prints usage so that we don't have to. :)"""
    cmd = sys.argv[0]
    print """%s
Usage: %s [options] <target_object> <file>
Runs metadata population code for a given object.

Options:
  -s    OMERO hostname to use [defaults to "localhost"]
  -p    OMERO port to use [defaults to 4064]
  -u    OMERO username to use
  -k    OMERO session key to use
  -i    Dump measurement information and exit (no population)
  -d    Print debug statements

Examples:
  %s -s localhost -p 14064 -u bob Plate:6 metadata.csv

Report bugs to ome-devel@lists.openmicroscopy.org.uk""" % (error, cmd, cmd)
    sys.exit(2)

# Global thread pool for use by workers
thread_pool = None

class MetadataError(Exception):
    """
    Raised by the metadata parsing context when an error condition
    is reached.
    """
    pass

class HeaderResolver(object):
    """
    Header resolver for known header names which is responsible for creating
    the column set for the OMERO.tables instance.
    """

    DEFAULT_COLUMN_SIZE = 1024

    plate_keys = {
            'well': WellColumn,
            'field': ImageColumn,
            'row': LongColumn,
            'column': LongColumn,
            'wellsample': ImageColumn
    }

    def __init__(self, target_object, headers):
        self.target_object = target_object
        self.headers = [v.replace('/', '\\') for v in headers]
        self.headers_as_lower = [v.lower() for v in self.headers]

    def create_columns(self):
        target_class = self.target_object.__class__
        if PlateI is target_class:
            return self.create_columns_plate()
        if DatasetI is target_class:
            return self.create_columns_dataset()
        raise MetadataError('Unsupported target object class: %s' \
                            % target_class)

    def create_columns_plate(self):
        columns = list()
        for i, header_as_lower in enumerate(self.headers_as_lower):
            name = self.headers[i]
            try:
                column = self.plate_keys[header_as_lower](name, '', list())
            except KeyError:
                column = StringColumn(name, '', self.DEFAULT_COLUMN_SIZE,
                                      list())
            columns.append(column)
        return columns

    def create_columns_dataset(self):
        raise Exception('To be implemented!')

class ValueResolver(object):
    """
    Value resolver for column types which is responsible for filling up
    non-metadata columns with their OMERO data model identifiers.
    """
    
    AS_ALPHA = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l']
    WELL_REGEX = re.compile(r'^([a-zA-Z]+)(\d+)$')

    def __init__(self, client, target_object):
        self.client = client
        self.target_object = target_object
        self.target_class = self.target_object.__class__
        if PlateI is self.target_class:
            return self.load_plate()
        if DatasetI is self.target_class:
            return self.load_dataset()
        raise MetadataError('Unsupported target object class: %s' \
                            % target_class)

    def load_plate(self):
        query_service = self.client.getSession().getQueryService()
        parameters = omero.sys.ParametersI()
        parameters.addId(self.target_object.id.val)
        self.target_object = query_service.findByQuery(
                'select p from Plate as p '
                'join fetch p.wells as w '
                'join fetch w.wellSamples as ws '
                'where p.id = :id', parameters)
        self.wells_by_location = dict()
        # TODO: This should use the PlateNamingConvention. We're assuming rows
        # as alpha and columns as numeric.
        for well in self.target_object.copyWells():
            row = well.row.val
            # 0 offsetted is not what people use in reality
            column = str(well.column.val + 1)
            try:
                columns = self.wells_by_location[self.AS_ALPHA[row]]
            except KeyError:
                self.wells_by_location[self.AS_ALPHA[row]] = columns = dict()
            columns[column] = well
        for row in self.wells_by_location:
            log.debug('%s: %r' % (row, self.wells_by_location[row].keys()))

    def load_dataset(self):
        raise Exception('To be implemented!')

    def resolve(self, column, value, row):
        column_class = column.__class__
        if WellColumn is column_class:
            m = self.WELL_REGEX.match(value)
            if m is None or len(m.groups()) != 2:
                raise MetadataError('Cannot parse well identifier: %s' % value)
            row = m.group(1).lower()
            column = m.group(2)
            log.debug('Parsed "%s" row: %s column: %s' % (value, row, column))
            return self.wells_by_location[row][column].id.val
        if column.name.lower() in ('row', 'column') \
           and column_class is LongColumn:
            try:
                # The value is not 0 offsetted
                return long(value) - 1
            except ValueError:
                return self.AS_ALPHA.index(value.lower())
        if StringColumn is column_class:
            return value
        raise MetadataError('Unsupported column class: %s' % column_class)

class ParsingContext(object):
    """Generic parsing context for CSV files."""

    def __init__(self, client, target_object, file):
        self.client = client
        self.target_object = target_object
        self.file = file

    def create_annotation_link(self):
        self.target_class = self.target_object.__class__
        if PlateI is self.target_class:
            return PlateAnnotationLinkI()
        if DatasetI is self.target_class:
            return DatasetAnnotationLinkI()
        raise MetadataError('Unsupported target object class: %s' \
                            % target_class)

    def parse(self):
        data = open(self.file, 'U')
        try:
            rows = list(csv.reader(data, delimiter=','))
        finally:
            data.close()
        log.debug('Header: %r' % rows[0])
        header_resolver = HeaderResolver(self.target_object, rows[0])
        self.columns = header_resolver.create_columns()
        log.debug('Columns: %r' % self.columns)
        self.populate(rows[1:])
        self.post_process()
        log.debug('Columns: %r' % \
                [(o.name, len(o.values)) for o in self.columns])

    def populate(self, rows):
        self.column_widths = [None] * len(self.columns)
        value_resolver = ValueResolver(self.client, self.target_object)
        for row in rows:
            for i, value in enumerate(row):
                column = self.columns[i]
                value = value_resolver.resolve(column, value, row)
                column.values.append(value)
                try:
                    self.column_widths[i] = \
                            max(self.column_widths[i], len(value))
                except TypeError:
                    pass
        log.debug('Column widths: %r' % self.column_widths)

    def post_process(self):
        for i, column in enumerate(self.columns):
            column_width = self.column_widths[i]
            if column_width is not None:
                column.size = column_width

    def write_to_omero(self):
        sf = self.client.getSession()
        sr = sf.sharedResources()
        update_service = sf.getUpdateService()
        name = 'bulk_annotations'
        table = sr.newTable(1, name)
        original_file = table.getOriginalFile()
        if table is None:
            raise MetadataError(
                "Unable to create table: %s" % name)
        log.info('Created new table OriginalFile:%d' % original_file.id.val)
        table.initialize(self.columns)
        log.info('Table initialized with %d columns.' % (len(self.columns)))
        table.addData(self.columns)
        log.info('Added data column data.')
        file_annotation = FileAnnotationI()
        file_annotation.ns = \
                rstring('openmicroscopy.org/omero/bulk_annotations')
        file_annotation.description = rstring(name)
        file_annotation.file = OriginalFileI(original_file.id.val, False)
        link = self.create_annotation_link()
        link.parent = self.target_object
        link.child = file_annotation
        update_service.saveObject(link)

def parse_target_object(target_object):
    type, id = target_object.split(':')
    if 'Dataset' == type:
        return DatasetI(long(id), False)
    if 'Plate' == type:
        return PlateI(long(id), False)
    raise ValueError('Unsupported target object: %s' % target_object)

if __name__ == "__main__":
    try:
        options, args = getopt(sys.argv[1:], "s:p:u:k:id")
    except GetoptError, (msg, opt):
        usage(msg)

    try:
        target_object, file = args
        target_object = parse_target_object(target_object)
    except ValueError:
        usage('Target object and file must be a specified!')
    
    username = None
    hostname = 'localhost'
    port = 4064  # SSL
    info = False
    session_key = None
    logging_level = logging.INFO
    thread_count = 1
    for option, argument in options:
        if option == "-u":
            username = argument
        if option == "-s":
            hostname = argument
        if option == "-p":
            port = int(argument)
        if option == "-i":
            info = True
        if option == "-k":
            session_key = argument
        if option == "-d":
            logging_level = logging.DEBUG
        if option == "-t":
            thread_count = int(argument)
    if session_key is None and username is None:
        usage("Username must be specified!")
    if session_key is None and hostname is None:
        usage("Host name must be specified!")
    if session_key is None:
        password = getpass()
    
    logging.basicConfig(level = logging_level)
    client = client(hostname, port)
    client.setAgent("OMERO.populate_metadata")
    client.enableKeepAlive(60)
    try:
        if session_key is not None:
            client.joinSession(session_key)
        else:
            client.createSession(username, password)

        log.debug('Creating pool of %d threads' % thread_count)
        thread_pool = ThreadPool(thread_count)
        ctx = ParsingContext(client, target_object, file)
        ctx.parse()
        if not info:
            ctx.write_to_omero()
    finally:
        client.closeSession()
