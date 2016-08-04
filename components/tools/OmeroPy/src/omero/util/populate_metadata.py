#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Populate bulk metadata tables from delimited text files.
"""

#
#  Copyright (C) 2011-2014 University of Dundee. All rights reserved.
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


import logging
import gzip
import sys
import csv
import re
import json
from getpass import getpass
from getopt import getopt, GetoptError
from itertools import izip
from collections import defaultdict

import omero.clients
from omero import CmdError
from omero.rtypes import rlist, rstring, unwrap
from omero.model import DatasetAnnotationLinkI, DatasetI, FileAnnotationI
from omero.model import OriginalFileI, PlateI, PlateAnnotationLinkI, ScreenI
from omero.model import PlateAcquisitionI, WellI, WellSampleI, ImageI
from omero.model import ProjectAnnotationLinkI, ProjectI
from omero.model import ScreenAnnotationLinkI
from omero.model import MapAnnotationI, NamedValue
from omero.grid import ImageColumn, LongColumn, PlateColumn, RoiColumn
from omero.grid import StringColumn, WellColumn, DoubleColumn, BoolColumn
from omero.grid import DatasetColumn
from omero.util.metadata_utils import (
    KeyValueListPassThrough, KeyValueGroupList, NSBULKANNOTATIONSCONFIG)
from omero import client

from populate_roi import ThreadPool

try:
    import yaml
    YAML_ENABLED = True
except ImportError:
    YAML_ENABLED = False


log = logging.getLogger("omero.util.populate_metadata")


def usage(error):
    """Prints usage so that we don't have to. :)"""
    cmd = sys.argv[0]
    print """%s
Usage: %s [options] <target_object> <file>
Runs metadata population code for a given object.

Options:
  -s            OMERO hostname to use [defaults to "localhost"]
  -p            OMERO port to use [defaults to 4064]
  -u            OMERO username to use
  -w            OMERO password
  -k            OMERO session key to use
  --columns     Column configuration, Specify as comma separated list.
                Supported types: plate, well, image, roi,
                                 d (double), l (long), s (string), b (boolean)
                Supported Boolean True Values: "yes", "true", "t", "1".
  -i            Dump measurement information and exit (no population)
  -d            Print debug statements
  -c            Use an alternative context (for expert users only)

Examples:
  %s -s localhost -p 14064 -u bob --columns l,image,d,l Plate:6 metadata.csv

Report bugs to ome-devel@lists.openmicroscopy.org.uk""" % (error, cmd, cmd)
    sys.exit(2)

# Global thread pool for use by workers
thread_pool = None

# Special column names we may add depending on the data type
BOOLEAN_TRUE = ["yes", "true", "t", "1"]

PLATE_NAME_COLUMN = 'Plate Name'
WELL_NAME_COLUMN = 'Well Name'
DATASET_NAME_COLUMN = 'Dataset Name'
IMAGE_NAME_COLUMN = 'Image Name'

COLUMN_TYPES = {
    'plate': PlateColumn, 'well': WellColumn, 'image': ImageColumn,
    'roi': RoiColumn, 'd': DoubleColumn, 'l': LongColumn, 's': StringColumn,
    'b': BoolColumn
}

REGEX_HEADER_SPECIFIER = r'# header '


class Skip(object):
    """Instance to denote a row skip request."""
    pass


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

    DEFAULT_COLUMN_SIZE = 1

    dataset_keys = {
        'image': ImageColumn,
        'image_name': StringColumn,
    }

    project_keys = {
        'dataset': DatasetColumn,
        'dataset_name': StringColumn,
        'image': ImageColumn,
        'image_name': StringColumn,
    }

    plate_keys = dict({
        'well': WellColumn,
        'field': ImageColumn,
        'row': LongColumn,
        'column': LongColumn,
        'wellsample': ImageColumn,
    })

    screen_keys = dict({
        'plate': PlateColumn,
    }, **plate_keys)

    def __init__(self, target_object, headers, column_types=None):
        self.target_object = target_object
        self.headers = headers
        self.headers_as_lower = [v.lower() for v in self.headers]
        self.types = column_types

    @staticmethod
    def is_row_column_types(row):
        if "# header" in row[0]:
            return True
        return False

    @staticmethod
    def get_column_types(row):
        if "# header" not in row[0]:
            return None
        get_first_type = re.compile(REGEX_HEADER_SPECIFIER)
        column_types = [get_first_type.sub('', row[0])]
        for column in row[1:]:
            column_types.append(column)
        column_types = parse_column_types(column_types)
        return column_types

    def create_columns(self):
        target_class = self.target_object.__class__
        target_id = self.target_object.id.val
        if ScreenI is target_class:
            log.debug('Creating columns for Screen:%d' % target_id)
            return self.create_columns_screen()
        elif PlateI is target_class:
            log.debug('Creating columns for Plate:%d' % target_id)
            return self.create_columns_plate()
        elif DatasetI is target_class:
            log.debug('Creating columns for Dataset:%d' % target_id)
            return self.create_columns_dataset()
        elif ProjectI is target_class:
            log.debug('Creating columns for Project:%d' % target_id)
            return self.create_columns_project()
        raise MetadataError(
            'Unsupported target object class: %s' % target_class)

    def columns_sanity_check(self, columns):
        column_types = [column.__class__ for column in columns]
        if WellColumn in column_types and ImageColumn in column_types:
            log.debug(column_types)
            raise MetadataError(
                ('Well Column and Image Column cannot be resolved at '
                 'the same time. Pick one.'))
        log.debug('Sanity check passed')

    def create_columns_screen(self):
        return self._create_columns("screen")

    def create_columns_plate(self):
        return self._create_columns("plate")

    def create_columns_dataset(self):
        return self._create_columns("dataset")

    def create_columns_project(self):
        return self._create_columns("project")

    def _create_columns(self, klass):
        if self.types is not None and len(self.types) != len(self.headers):
            message = "Number of columns and column types not equal."
            raise MetadataError(message)
        columns = list()
        for i, header_as_lower in enumerate(self.headers_as_lower):
            name = self.headers[i]
            description = ""
            if "%%" in name:
                name, description = name.split("%%", 1)
                name = name.strip()
                # description is key=value. Convert to json
                if "=" in description:
                    k, v = description.split("=", 1)
                    k = k.strip()
                    description = json.dumps({k: v.strip()})
            # HDF5 does not allow / in column names
            name = name.replace('/', '\\')
            if self.types is not None and \
                    COLUMN_TYPES[self.types[i]] is StringColumn:
                column = COLUMN_TYPES[self.types[i]](
                    name, description, self.DEFAULT_COLUMN_SIZE, list())
            elif self.types is not None:
                column = COLUMN_TYPES[self.types[i]](name, description, list())
            else:
                try:
                    keys = getattr(self, "%s_keys" % klass)
                    column = keys[header_as_lower](
                        name, description, list())
                except KeyError:
                    column = StringColumn(
                        name, description, self.DEFAULT_COLUMN_SIZE, list())
            columns.append(column)
        append = []
        for column in columns:
            if column.__class__ is PlateColumn:
                append.append(StringColumn(PLATE_NAME_COLUMN, '',
                              self.DEFAULT_COLUMN_SIZE, list()))
            if column.__class__ is WellColumn:
                append.append(StringColumn(WELL_NAME_COLUMN, '',
                              self.DEFAULT_COLUMN_SIZE, list()))
            if column.__class__ is ImageColumn:
                append.append(StringColumn(IMAGE_NAME_COLUMN, '',
                              self.DEFAULT_COLUMN_SIZE, list()))
            # Currently hard-coded, but "if image name, then add image id"
            if column.name == IMAGE_NAME_COLUMN:
                append.append(ImageColumn("image", '', list()))
        columns.extend(append)
        self.columns_sanity_check(columns)
        return columns


class ValueResolver(object):
    """
    Value resolver for column types which is responsible for filling up
    non-metadata columns with their OMERO data model identifiers.
    """

    AS_ALPHA = [chr(v) for v in range(97, 122 + 1)]  # a-z
    # Support more than 26 rows
    for v in range(97, 122 + 1):
        AS_ALPHA.append('a' + chr(v))
    WELL_REGEX = re.compile(r'^([a-zA-Z]+)(\d+)$')

    def __init__(self, client, target_object):
        self.client = client
        self.target_object = target_object
        self.target_class = self.target_object.__class__
        self.target_type = self.target_object.ice_staticId().split('::')[-1]
        self.target_id = self.target_object.id.val
        q = "select x.details.group.id from %s x where x.id = %d " % (
            self.target_type, self.target_id
        )
        self.target_group = unwrap(
            self.client.sf.getQueryService().projection(q, None))
        # The goal is to make this the only instance of
        # a if/elif/else block on the target_class. All
        # logic should be placed in a the concrete wrapper
        # implementation
        if PlateI is self.target_class:
            self.wrapper = PlateWrapper(self)
        elif DatasetI is self.target_class:
            self.wrapper = DatasetWrapper(self)
        elif ScreenI is self.target_class:
            self.wrapper = ScreenWrapper(self)
        elif ProjectI is self.target_class:
            self.wrapper = ProjectWrapper(self)
        else:
            raise MetadataError(
                'Unsupported target object class: %s' % self.target_class)

    def get_plate_name_by_id(self, plate):
        return self.wrapper.get_plate_name_by_id(plate)

    def get_well_name(self, well_id, plate=None):
        well = self.wrapper.get_well_by_id(well_id, plate)
        row = well.row.val
        col = well.column.val
        row = self.AS_ALPHA[row]
        return '%s%d' % (row, col + 1)

    def get_image_id_by_name(self, iname, dname=None):
        return self.wrapper.get_image_id_by_name(iname, dname)

    def subselect(self, valuerows, names):
        return self.wrapper.subselect(valuerows, names)

    def resolve(self, column, value, row):
        column_class = column.__class__
        column_as_lower = column.name.lower()
        if ImageColumn is column_class:
            if len(self.images_by_id) == 1:
                images_by_id = self.images_by_id.values()[0]
            else:
                for column, plate in row:
                    if column.__class__ is PlateColumn:
                        images_by_id = self.images_by_id[
                            self.plates_by_name[plate].id.val
                        ]
                        log.debug(
                            "Got plate %i", self.plates_by_name[plate].id.val
                        )
                    break
            if images_by_id is None:
                raise MetadataError(
                    'Unable to locate Plate column in Row: %r' % row
                )
            try:
                return images_by_id[long(value)].id.val
            except KeyError:
                log.debug('Image Id: %i not found!' % (value))
                return -1L
            return
        if WellColumn is column_class:
            return self.wrapper.resolve_well(column, row, value)
        if PlateColumn is column_class:
            return self.wrapper.resolve_plate(column, row, value)
        if column_as_lower in ('row', 'column') \
           and column_class is LongColumn:
            try:
                # The value is not 0 offsetted
                return long(value) - 1
            except ValueError:
                return long(self.AS_ALPHA.index(value.lower()))
        if StringColumn is column_class:
            return value
        if LongColumn is column_class:
            return long(value)
        if DoubleColumn is column_class:
            return float(value)
        if BoolColumn is column_class:
            return value.lower() in BOOLEAN_TRUE
        raise MetadataError('Unsupported column class: %s' % column_class)


class ValueWrapper(object):

    def __init__(self, value_resolver):
        self.resolver = value_resolver
        self.client = value_resolver.client
        self.target_object = value_resolver.target_object
        self.target_class = value_resolver.target_class

    def subselect(self, rows, names):
        return rows


class SPWWrapper(ValueWrapper):

    def __init__(self, value_resolver):
        super(SPWWrapper, self).__init__(value_resolver)
        self.AS_ALPHA = value_resolver.AS_ALPHA
        self.WELL_REGEX = value_resolver.WELL_REGEX

    def get_well_by_id(self, well_id, plate=None):
        raise Exception("to be implemented by subclasses")

    def parse_plate(self, plate, wells_by_location, wells_by_id, images_by_id):
        # TODO: This should use the PlateNamingConvention. We're assuming rows
        # as alpha and columns as numeric.
        for well in plate.copyWells():
            wells_by_id[well.id.val] = well
            row = well.row.val
            # 0 offsetted is not what people use in reality
            column = str(well.column.val + 1)
            try:
                columns = wells_by_location[self.AS_ALPHA[row]]
            except KeyError:
                wells_by_location[self.AS_ALPHA[row]] = columns = dict()
            columns[column] = well

            for well_sample in well.copyWellSamples():
                image = well_sample.getImage()
                images_by_id[image.id.val] = image
        log.debug('Completed parsing plate: %s' % plate.name.val)
        for row in wells_by_location:
            log.debug('%s: %r' % (row, wells_by_location[row].keys()))

    def resolve_well(self, column, row, value):
            m = self.WELL_REGEX.match(value)
            if m is None or len(m.groups()) != 2:
                msg = 'Cannot parse well identifier "%s" from row: %r'
                msg = msg % (value, [o[1] for o in row])
                raise MetadataError(msg)
            plate_row = m.group(1).lower()
            plate_column = str(long(m.group(2)))
            wells_by_location = None
            if len(self.wells_by_location) == 1:
                wells_by_location = self.wells_by_location.values()[0]
                log.debug(
                    'Parsed "%s" row: %s column: %s' % (
                        value, plate_row, plate_column))
            else:
                for column, plate in row:
                    if column.__class__ is PlateColumn:
                        wells_by_location = self.wells_by_location[plate]
                        log.debug(
                            'Parsed "%s" row: %s column: %s plate: %s' % (
                                value, plate_row, plate_column, plate))
                        break
            if wells_by_location is None:
                raise MetadataError(
                    'Unable to locate Plate column in Row: %r' % row
                )
            try:
                return wells_by_location[plate_row][plate_column].id.val
            except KeyError:
                log.debug('Row: %s Column: %s not found!' % (
                    plate_row, plate_column))
                return -1L


class ScreenWrapper(SPWWrapper):

    def __init__(self, value_resolver):
        super(ScreenWrapper, self).__init__(value_resolver)
        self._load()

    def get_plate_name_by_id(self, plate):
        plate = self.plates_by_id[plate]
        return plate.name.val

    def get_well_by_id(self, well_id, plate=None):
        wells = self.wells_by_id[plate]
        return wells[well_id]

    def resolve_plate(self, column, row, value):
        try:
            return self.plates_by_name[value].id.val
        except KeyError:
            log.warn('Screen is missing plate: %s' % value)
            return Skip()

    def _load(self):
        query_service = self.client.getSession().getQueryService()
        parameters = omero.sys.ParametersI()
        parameters.addId(self.target_object.id.val)
        log.debug('Loading Screen:%d' % self.target_object.id.val)
        self.target_object = query_service.findByQuery((
            'select s from Screen as s '
            'join fetch s.plateLinks as p_link '
            'join fetch p_link.child as p '
            'where s.id = :id'), parameters, {'omero.group': '-1'})
        if self.target_object is None:
            raise MetadataError('Could not find target object!')
        self.target_name = unwrap(self.target_object.getName())
        self.images_by_id = dict()
        self.wells_by_location = dict()
        self.wells_by_id = dict()
        self.plates_by_name = dict()
        self.plates_by_id = dict()
        images_by_id = dict()
        self.images_by_id[self.target_object.id.val] = images_by_id
        for plate in (l.child for l in self.target_object.copyPlateLinks()):
            parameters = omero.sys.ParametersI()
            parameters.addId(plate.id.val)
            plate = query_service.findByQuery((
                'select p from Plate as p '
                'join fetch p.wells as w '
                'join fetch w.wellSamples as ws '
                'join fetch ws.image as i '
                'where p.id = :id'), parameters, {'omero.group': '-1'})
            self.plates_by_name[plate.name.val] = plate
            self.plates_by_id[plate.id.val] = plate
            wells_by_location = dict()
            wells_by_id = dict()
            self.wells_by_location[plate.name.val] = wells_by_location
            self.wells_by_id[plate.id.val] = wells_by_id
            self.parse_plate(
                plate, wells_by_location, wells_by_id, images_by_id
            )


class PlateWrapper(SPWWrapper):

    def __init__(self, value_resolver):
        super(PlateWrapper, self).__init__(value_resolver)
        self._load()

    def get_well_by_id(self, well_id, plate=None):
        plate = self.target_object.id.val
        wells = self.wells_by_id[plate]
        return wells[well_id]

    def subselect(self, rows, names):
        """
        If we're processing a plate but the bulk-annotations file contains
        a plate column then select rows for this plate only
        """
        for i, name in enumerate(names):
            if name.lower() == 'plate':
                valuerows = [row for row in rows if row[i] ==
                             self.value_resolver.target_name]
                log.debug(
                    'Selected %d/%d rows for plate "%s"', len(valuerows),
                    len(rows), self.value_resolver.target_name)
                return valuerows
        return rows

    def _load(self):
        query_service = self.client.getSession().getQueryService()
        parameters = omero.sys.ParametersI()
        parameters.addId(self.target_object.id.val)
        log.debug('Loading Plate:%d' % self.target_object.id.val)
        self.target_object = query_service.findByQuery((
            'select p from Plate as p '
            'join fetch p.wells as w '
            'join fetch w.wellSamples as ws '
            'join fetch ws.image as i '
            'where p.id = :id'), parameters, {'omero.group': '-1'})
        if self.target_object is None:
            raise MetadataError('Could not find target object!')
        self.target_name = unwrap(self.target_object.getName())
        self.wells_by_location = dict()
        self.wells_by_id = dict()
        wells_by_location = dict()
        wells_by_id = dict()

        self.images_by_id = dict()
        images_by_id = dict()

        self.wells_by_location[self.target_object.name.val] = wells_by_location
        self.wells_by_id[self.target_object.id.val] = wells_by_id
        self.images_by_id[self.target_object.id.val] = images_by_id
        self.parse_plate(
            self.target_object, wells_by_location, wells_by_id, images_by_id
        )


class PDIWrapper(ValueWrapper):

    def get_image_id_by_name(self, iname, dname=None):
        raise Exception("to be implemented by subclasses")


class DatasetWrapper(PDIWrapper):

    def __init__(self, value_resolver):
        super(DatasetWrapper, self).__init__(value_resolver)
        self.images_by_id = dict()
        self.images_by_name = dict()
        self._load()

    def get_image_id_by_name(self, iname, dname=None):
        return self.images_by_name[iname]

    def _load(self):
        query_service = self.client.getSession().getQueryService()
        parameters = omero.sys.ParametersI()
        parameters.addId(self.target_object.id.val)
        log.debug('Loading Dataset:%d' % self.target_object.id.val)

        parameters.page(0, 1)
        self.target_object = unwrap(query_service.findByQuery(
            'select d from Dataset d where d.id = :id',
            parameters, {'omero.group': '-1'}))
        self.target_name = self.target_object.name.val

        data = list()
        while True:
            parameters.page(len(data), 1000)
            rv = unwrap(query_service.projection((
                'select distinct i.id, i.name from Dataset as d '
                'join d.imageLinks as l '
                'join l.child as i '
                'where d.id = :id order by i.id desc'),
                parameters, {'omero.group': '-1'}))
            if len(rv) == 0:
                break
            else:
                data.extend(rv)
        if not data:
            raise MetadataError('Could not find target object!')

        for iid, iname in data:
            self.images_by_id[iid] = iname
            if iname in self.images_by_name:
                raise Exception("Image named %s(id=%d) present. (id=%s)" % (
                    iname, self.images_by_name[iname], iid
                ))
            self.images_by_name[iname] = iid
        log.debug('Completed parsing dataset: %s' % self.target_name)


class ProjectWrapper(PDIWrapper):

    def __init__(self, value_resolver):
        super(ProjectWrapper, self).__init__(value_resolver)
        self.graph_by_id = defaultdict(lambda: dict())
        self.graph_by_name = defaultdict(lambda: dict())
        self._load()

    def get_image_id_by_name(self, iname, dname=None):
        return self.graph_by_name[dname][iname][2]

    def _load(self):
        query_service = self.client.getSession().getQueryService()
        parameters = omero.sys.ParametersI()
        parameters.addId(self.target_object.id.val)
        log.debug('Loading Project:%d' % self.target_object.id.val)

        parameters.page(0, 1)
        self.target_object = unwrap(query_service.findByQuery(
            'select p from Project p where p.id = :id',
            parameters, {'omero.group': '-1'}))
        self.target_name = self.target_object.name.val

        data = list()
        while True:
            parameters.page(len(data), 1000)
            rv = unwrap(query_service.projection((
                'select distinct d.id, d.name, i.id, i.name '
                'from Project p '
                'join p.datasetLinks as pdl '
                'join pdl.child as d '
                'join d.imageLinks as l '
                'join l.child as i '
                'where p.id = :id order by i.id desc'),
                parameters, {'omero.group': '-1'}))
            if len(rv) == 0:
                break
            else:
                data.extend(rv)
        if not data:
            raise MetadataError('Could not find target object!')

        seen = dict()
        for row in data:
            did, dname, iid, iname = row

            if dname in seen and seen[dname] != did:
                raise Exception("Duplicate datasets: '%s' = %s, %s" % (
                    dname, seen[dname], did
                ))
            else:
                seen[dname] = did

            ikey = (did, iname)
            if ikey in seen and iid != seen[ikey]:
                raise Exception("Duplicate image: '%s' = %s, %s (Dataset:%s)"
                                % (iname, seen[ikey], iid, did))
            else:
                seen[ikey] = iid

            self.graph_by_id[did][iid] = row
            self.graph_by_name[dname][iname] = row
        log.debug('Completed parsing project: %s' % self.target_object.id.val)


class ParsingContext(object):
    """Generic parsing context for CSV files."""

    def __init__(self, client, target_object, file=None, fileid=None,
                 cfg=None, cfgid=None, attach=False, column_types=None):
        '''
        This lines should be handled outside of the constructor:

        if not file:
            raise MetadataError('file required for %s' % type(self))
        if fileid and not file:
            raise MetadataError('fileid not supported for %s' % type(self))
        if cfg:
            raise MetadataError('cfg not supported for %s' % type(self))
        if cfgid:
            raise MetadataError('cfgid not supported for %s' % type(self))
        '''

        self.client = client
        self.target_object = target_object
        self.file = file
        self.column_types = column_types
        self.value_resolver = ValueResolver(self.client, self.target_object)

    def create_annotation_link(self):
        self.target_class = self.target_object.__class__
        if ScreenI is self.target_class:
            return ScreenAnnotationLinkI()
        if PlateI is self.target_class:
            return PlateAnnotationLinkI()
        if DatasetI is self.target_class:
            return DatasetAnnotationLinkI()
        if ProjectI is self.target_class:
            return ProjectAnnotationLinkI()
        raise MetadataError(
            'Unsupported target object class: %s' % self.target_class)

    def get_column_widths(self):
        widths = list()
        for column in self.columns:
            try:
                widths.append(column.size)
            except AttributeError:
                widths.append(None)
        return widths

    def parse_from_handle(self, data):
        rows = list(csv.reader(data, delimiter=','))
        first_row_is_types = HeaderResolver.is_row_column_types(rows[0])
        header_index = 0
        rows_index = 1
        if first_row_is_types:
            header_index = 1
            rows_index = 2
        log.debug('Header: %r' % rows[header_index])
        for h in rows[0]:
            if not h:
                raise Exception('Empty column header in CSV: %s'
                                % rows[header_index])
        if self.column_types is None and first_row_is_types:
            self.column_types = HeaderResolver.get_column_types(rows[0])
        log.debug('Column types: %r' % self.column_types)
        self.header_resolver = HeaderResolver(
            self.target_object, rows[header_index],
            column_types=self.column_types)
        self.columns = self.header_resolver.create_columns()
        log.debug('Columns: %r' % self.columns)

        valuerows = rows[rows_index:]
        log.debug('Got %d rows', len(valuerows))
        valuerows = self.value_resolver.subselect(
            valuerows, rows[header_index])
        self.populate(valuerows)
        self.post_process()
        log.debug('Column widths: %r' % self.get_column_widths())
        log.debug('Columns: %r' % [
            (o.name, len(o.values)) for o in self.columns])

    def parse(self):
        if self.file.endswith(".gz"):
            data = gzip.open(self.file, "rb")
        else:
            data = open(self.file, 'U')

        try:
            return self.parse_from_handle(data)
        finally:
            data.close()

    def populate(self, rows):
        nrows = len(rows)
        for (r, row) in enumerate(rows):
            values = list()
            row = [(self.columns[i], value) for i, value in enumerate(row)]
            for column, original_value in row:
                log.debug('Row %d/%d Original value %s, %s',
                          r + 1, nrows, original_value, column.name)
                value = self.value_resolver.resolve(
                    column, original_value, row)
                if value.__class__ is Skip:
                    break
                values.append(value)
                try:
                    log.debug("Value's class: %s" % value.__class__)
                    if value.__class__ is str:
                        column.size = max(column.size, len(value))
                except TypeError:
                    log.error('Original value "%s" now "%s" of bad type!' % (
                        original_value, value))
                    raise
            if value.__class__ is not Skip:
                values.reverse()
                for column in self.columns:
                    if not values:
                        if isinstance(column, ImageColumn) or \
                           column.name in (PLATE_NAME_COLUMN,
                                           WELL_NAME_COLUMN,
                                           IMAGE_NAME_COLUMN):
                            # Then assume that the values will be calculated
                            # later based on another column.
                            continue
                        else:
                            msg = 'Column %s has no values.' % column.name
                            log.error(msg)
                            raise IndexError(msg)
                    else:
                        column.values.append(values.pop())

    def post_process(self):
        columns_by_name = dict()
        well_column = None
        well_name_column = None
        plate_name_column = None
        image_column = None
        image_name_column = None
        for column in self.columns:
            columns_by_name[column.name] = column
            if column.__class__ is PlateColumn:
                log.warn("PlateColumn is unimplemented")
            elif column.__class__ is WellColumn:
                well_column = column
            elif column.name == WELL_NAME_COLUMN:
                well_name_column = column
            elif column.name == PLATE_NAME_COLUMN:
                plate_name_column = column
            elif column.name == IMAGE_NAME_COLUMN:
                image_name_column = column
            elif column.__class__ is ImageColumn:
                image_column = column

        if well_name_column is None and plate_name_column is None \
                and image_name_column is None:
            log.info('Nothing to do during post processing.')
            return

        sz = max([len(x.values) for x in self.columns])
        for i in range(0, sz):
            if well_name_column is not None:

                v = ''
                try:
                    well_id = well_column.values[i]
                    plate = None
                    if "Plate" in columns_by_name:  # FIXME
                        plate = columns_by_name["Plate"].values[i]
                    v = self.value_resolver.get_well_name(well_id, plate)
                except KeyError:
                    log.warn(
                        'Skipping table row %d! Missing well row or column '
                        'for well name population!' % i, exc_info=True
                    )
                well_name_column.size = max(well_name_column.size, len(v))
                well_name_column.values.append(v)
            else:
                log.info('Missing well name column, skipping.')

            if image_name_column is not None:
                try:
                    iname = image_name_column.values[i]
                    did = None
                    if "Dataset Name" in columns_by_name:  # FIXME
                        did = columns_by_name["Dataset Name"].values[i]
                    iid = self.value_resolver.get_image_id_by_name(iname, did)
                    assert i == len(image_column.values)
                    image_column.values.append(iid)
                    image_name_column.size = max(
                        image_name_column.size, len(iname))
                except KeyError:
                    log.error(
                        "%s not found in image names" % iname)
                    raise
            else:
                log.info('Missing image name column, skipping.')

            if plate_name_column is not None:
                plate = columns_by_name['Plate'].values[i]   # FIXME
                v = self.value_resolver.get_plate_name_by_id(plate)
                plate_name_column.size = max(plate_name_column.size, len(v))
                plate_name_column.values.append(v)
            else:
                log.info('Missing plate name column, skipping.')

    def write_to_omero(self, batch_size=1000):
        sf = self.client.getSession()
        group = str(self.value_resolver.target_group)
        sr = sf.sharedResources()
        update_service = sf.getUpdateService()
        name = 'bulk_annotations'
        table = sr.newTable(1, name, {'omero.group': group})
        if table is None:
            raise MetadataError(
                "Unable to create table: %s" % name)
        original_file = table.getOriginalFile()
        log.info('Created new table OriginalFile:%d' % original_file.id.val)

        values = []
        length = -1
        for x in self.columns:
            if length < 0:
                length = len(x.values)
            else:
                assert length == len(x.values)
            values.append(x.values)
            x.values = None

        table.initialize(self.columns)
        log.info('Table initialized with %d columns.' % (len(self.columns)))

        i = 0
        for pos in xrange(0, length, batch_size):
            i += 1
            for idx, x in enumerate(values):
                self.columns[idx].values = x[pos:pos+batch_size]
            table.addData(self.columns)
            count = min(batch_size, length - pos)
            log.info('Added %s rows of column data (batch %s)', count, i)

        table.close()
        file_annotation = FileAnnotationI()
        file_annotation.ns = rstring(
            'openmicroscopy.org/omero/bulk_annotations')
        file_annotation.description = rstring(name)
        file_annotation.file = OriginalFileI(original_file.id.val, False)
        link = self.create_annotation_link()
        link.parent = self.target_object
        link.child = file_annotation
        update_service.saveObject(link, {'omero.group': group})


class _QueryContext(object):
    """
    Helper class container query methods
    """
    def __init__(self, client):
        self.client = client

    def _batch(self, i, sz=1000):
        """
        Generate batches of size sz (by default 1000) from the input
        iterable `i`.
        """
        i = list(i)  # Copying list to handle sets and modifications
        for batch in (i[pos:pos + sz] for pos in xrange(0, len(i), sz)):
            yield batch

    def projection(self, q, ids, nss=None, batch_size=None):
        """
        Run a projection query designed to return scalars only
        :param q: The query to be projected, should contain either `:ids`
               or `:id` as a parameter
        :param: ids: Either a list of IDs to be passed as `:ids` parameter or
                a single scalar id to be passed as `:id` parameter in query
        :nss: Optional, Either a list of namespaces to be passed as `:nss`
                parameter or a single string to be passed as `:ns` parameter
                in query
        :batch_size: Optional batch_size (default: all) defining the number
                of IDs that will be queried at once. Methods that expect to
                have more than several thousand input IDs should consider an
                appropriate batch size. By default, however, no batch size is
                applied since this could change the interpretation of the
                query string (e.g. use of `distinct`).
        """
        qs = self.client.getSession().getQueryService()
        params = omero.sys.ParametersI()

        try:
            nids = len(ids)
            single_id = None
        except TypeError:
            nids = 1
            single_id = ids

        if isinstance(nss, basestring):
            params.addString("ns", nss)
        elif nss:
            params.map['nss'] = rlist(rstring(s) for s in nss)

        log.debug("Query: %s len(IDs): %d namespace(s): %s", q, nids, nss)

        if single_id is not None:
            params.addId(single_id)
            rss = unwrap(qs.projection(q, params))
        elif batch_size is None:
            params.addIds(ids)
            rss = unwrap(qs.projection(q, params))
        else:
            rss = []
            for batch in self._batch(ids, sz=batch_size):
                params.addIds(batch)
                rss.extend(unwrap(qs.projection(q, params)))

        return [r for rs in rss for r in rs]


def get_config(session, cfg=None, cfgid=None):
    if not YAML_ENABLED:
        raise ImportError("yaml (PyYAML) module required")

    if cfgid:
        try:
            rfs = session.createRawFileStore()
            rfs.setFileId(cfgid)
            rawdata = rfs.read(0, rfs.size())
        finally:
            rfs.close()
    elif cfg:
        with open(cfg, 'r') as f:
            rawdata = f.read()
    else:
        raise Exception("Configuration file required")

    cfg = list(yaml.load_all(rawdata))
    if len(cfg) != 1:
        raise Exception(
            "Expected YAML file with one document, found %d" % len(cfg))
    cfg = cfg[0]

    default_cfg = cfg.get("defaults")
    column_cfgs = cfg.get("columns")
    advanced_cfgs = cfg.get("advanced", {})
    if not default_cfg and not column_cfgs:
        raise Exception(
            "Configuration defaults and columns were both empty")
    return default_cfg, column_cfgs, advanced_cfgs


class BulkToMapAnnotationContext(_QueryContext):
    """
    Processor for creating MapAnnotations from BulkAnnotations.
    """

    def __init__(self, client, target_object, file=None, fileid=None,
                 cfg=None, cfgid=None, attach=False):
        """
        :param client: OMERO client object
        :param target_object: The object to be annotated
        :param file: Not supported
        :param fileid: The OriginalFile ID of the bulk-annotations table,
               default is to use the a bulk-annotation attached to
               target_object
        :param cfg: Path to a configuration file, ignored if cfgid given
        :param cfgid: OriginalFile ID of configuration file, either cfgid or
               cfg must be given
        """
        super(BulkToMapAnnotationContext, self).__init__(client)

        if file and not fileid:
            raise MetadataError('file not supported for %s' % type(self))

        # Reload object to get .details
        self.target_object = self.get_target(target_object)
        if fileid:
            self.ofileid = fileid
        else:
            self.ofileid = self.get_bulk_annotation_file()
        if not self.ofileid:
            raise MetadataError("Unable to find bulk-annotations file")

        self.default_cfg, self.column_cfgs, self.advanced_cfgs = \
            get_config(self.client.getSession(), cfg=cfg, cfgid=cfgid)

    def get_target(self, target_object):
        qs = self.client.getSession().getQueryService()
        return qs.find(target_object.ice_staticId().split('::')[-1],
                       target_object.id.val)

    def get_bulk_annotation_file(self):
        otype = self.target_object.ice_staticId().split('::')[-1]
        q = """SELECT child.file.id FROM %sAnnotationLink link
               WHERE parent.id=:id AND child.ns=:ns ORDER by id""" % otype
        r = self.projection(q, unwrap(self.target_object.getId()),
                            omero.constants.namespaces.NSBULKANNOTATIONS)
        if r:
            return r[-1]

    @staticmethod
    def create_map_annotation(
            targets, rowkvs, ns=omero.constants.namespaces.NSBULKANNOTATIONS):
        ma = MapAnnotationI()
        ma.setNs(rstring(ns))
        mv = []
        for k, vs in rowkvs:
            if not isinstance(vs, (tuple, list)):
                vs = [vs]
            mv.extend(NamedValue(k, str(v)) for v in vs)
        ma.setMapValue(mv)

        links = []
        for target in targets:
            otype = target.ice_staticId().split('::')[-1]
            link = getattr(omero.model, '%sAnnotationLinkI' % otype)()
            link.setParent(target)
            link.setChild(ma)
            links.append(link)
        return links

    def parse(self):
        tableid = self.ofileid
        sr = self.client.getSession().sharedResources()
        log.debug('Loading table OriginalFile:%d', self.ofileid)
        table = sr.openTable(omero.model.OriginalFileI(tableid, False))
        assert table

        try:
            return self.populate(table)
        finally:
            table.close()

    def _get_additional_targets(self, target):
        iids = []
        if self.advanced_cfgs.get('well_to_images') and isinstance(
                target, omero.model.Well):
            q = 'SELECT image.id FROM WellSample WHERE well.id=:id'
            iids = self.projection(q, unwrap(target.getId()))
        return [omero.model.ImageI(i, False) for i in iids]

    def populate(self, table):
        def idcolumn_to_omeroclass(col):
            clsname = re.search('::(\w+)Column$', col.ice_staticId()).group(1)
            return getattr(omero.model, '%sI' % clsname)

        nrows = table.getNumberOfRows()
        data = table.readCoordinates(range(nrows))

        # Don't create annotations on higher-level objects
        # idcoltypes = set(HeaderResolver.screen_keys.values())
        idcoltypes = set((ImageColumn, WellColumn))
        idcols = []
        for n in xrange(len(data.columns)):
            col = data.columns[n]
            if col.__class__ in idcoltypes:
                omeroclass = idcolumn_to_omeroclass(col)
                idcols.append((omeroclass, n))

        headers = [c.name for c in data.columns]
        if self.default_cfg or self.column_cfgs:
            kvgl = KeyValueGroupList(
                headers, self.default_cfg, self.column_cfgs)
            trs = kvgl.get_transformers()
        else:
            trs = [KeyValueListPassThrough(headers)]

        mas = []
        for row in izip(*(c.values for c in data.columns)):
            targets = []
            for omerotype, n in idcols:
                if row[n] > 0:
                    obj = omerotype(row[n], False)
                    additional = self._get_additional_targets(obj)
                    targets.extend(self._get_additional_targets(obj))
                    # Josh: disabling to prevent duplication in UI
                    # if there are other targets to be used. FIXME
                    if not additional:
                        targets.append(obj)
                else:
                    log.warn("Invalid Id:%d found in row %s", row[n], row)
            if targets:
                for tr in trs:
                    rowkvs = tr.transform(row)
                    ns = tr.name
                    if not ns:
                        ns = omero.constants.namespaces.NSBULKANNOTATIONS
                    malinks = self.create_map_annotation(targets, rowkvs, ns)
                    log.debug('Map:\n\t' + ('\n\t'.join("%s=%s" % (
                        v.name, v.value) for v in
                        malinks[0].getChild().getMapValue())))
                    log.debug('Targets:\n\t' + ('\n\t'.join("%s:%d" % (
                        t.ice_staticId().split('::')[-1], t.id._val)
                        for t in targets)))
                    mas.extend(malinks)

        self.mapannotations = mas

    def write_to_omero(self, batch_size=1000):
        sf = self.client.getSession()
        group = str(self.target_object.details.group.id)
        update_service = sf.getUpdateService()
        i = 0
        for batch in self._batch(self.mapannotations, sz=batch_size):
            i += 1
            ids = update_service.saveAndReturnIds(
                batch, {'omero.group': group})
            log.info('Created %d MapAnnotations (batch %s)', len(ids), i)


class DeleteMapAnnotationContext(_QueryContext):
    """
    Processor for deleting MapAnnotations in the BulkAnnotations namespace
    on these types: Image WellSample Well PlateAcquisition Plate Screen
    """

    def __init__(self, client, target_object, file=None, fileid=None,
                 cfg=None, cfgid=None, attach=False):
        """
        :param client: OMERO client object
        :param target_object: The object to be processed
        :param file, fileid, cfg, cfgid: Ignored
        :param attach: Delete all attached config files (recursive,
               default False)
        """
        super(DeleteMapAnnotationContext, self).__init__(client)
        self.target_object = target_object
        self.attach = attach

        if cfg or cfgid:
            self.default_cfg, self.column_cfgs, self.advanced_cfgs = \
                get_config(self.client.getSession(), cfg=cfg, cfgid=cfgid)
        else:
            self.default_cfg = None
            self.column_cfgs = None
            self.advanced_cfgs = None

    def parse(self):
        return self.populate()

    def _get_annotations_for_deletion(self, objtype, objids, anntype, nss):
        r = []
        if objids:
            q = ("SELECT child.id FROM %sAnnotationLink WHERE "
                 "child.class=%s AND parent.id in (:ids) "
                 "AND child.ns in (:nss)")
            r = self.projection(q % (objtype, anntype), objids, nss,
                                batch_size=10000)
            log.debug("%s: %d %s(s)", objtype, len(set(r)), anntype)
        return r

    def _get_configured_namespaces(self):
        nss = set([omero.constants.namespaces.NSBULKANNOTATIONS])
        if self.column_cfgs:
            for c in self.column_cfgs:
                try:
                    ns = c['group']['groupname']
                    nss.add(ns)
                except KeyError:
                    continue
        return list(nss)

    def populate(self):
        # Hierarchy: Screen, Plate, {PlateAcquistion, Well}, WellSample, Image
        parentids = {
            "Screen": None,
            "Plate":  None,
            "PlateAcquisition": None,
            "Well": None,
            "WellSample": None,
            "Image": None,
            "Dataset": None,
            "Project": None,
        }

        target = self.target_object
        ids = [unwrap(target.getId())]

        if isinstance(target, ScreenI):
            q = ("SELECT child.id FROM ScreenPlateLink "
                 "WHERE parent.id in (:ids)")
            parentids["Screen"] = ids
        if parentids["Screen"]:
            parentids["Plate"] = self.projection(q, parentids["Screen"])

        if isinstance(target, PlateI):
            parentids["Plate"] = ids
        if parentids["Plate"]:
            q = "SELECT id FROM PlateAcquisition WHERE plate.id IN (:ids)"
            parentids["PlateAcquisition"] = self.projection(
                q, parentids["Plate"])
            q = "SELECT id FROM Well WHERE plate.id IN (:ids)"
            parentids["Well"] = self.projection(q, parentids["Plate"])

        if isinstance(target, PlateAcquisitionI):
            parentids["PlateAcquisition"] = ids
        if parentids["PlateAcquisition"] and not isinstance(target, PlateI):
            # WellSamples are linked to PlateAcqs and Plates, so only get
            # if they haven't been obtained via a Plate
            # Also note that we do not get Wells if the parent is a
            # PlateAcquisition since this only refers to the fields in
            # the well
            q = "SELECT id FROM WellSample WHERE plateAcquisition.id IN (:ids)"
            parentids["WellSample"] = self.projection(
                q, parentids["PlateAcquisition"])

        if isinstance(target, WellI):
            parentids["Well"] = ids
        if parentids["Well"]:
            q = "SELECT id FROM WellSample WHERE well.id IN (:ids)"
            parentids["WellSample"] = self.projection(
                q, parentids["Well"], batch_size=10000)

        if isinstance(target, WellSampleI):
            parentids["WellSample"] = ids
        if parentids["WellSample"]:
            q = "SELECT image.id FROM WellSample WHERE id IN (:ids)"
            parentids["Image"] = self.projection(
                q, parentids["WellSample"], batch_size=10000)

        if isinstance(target, ProjectI):
            parentids["Project"] = ids
        if parentids["Project"]:
            q = ("SELECT ds.id FROM ProjectDatasetLink link "
                 "join link.parent prj "
                 "join link.child as ds WHERE prj.id IN (:ids)")
            parentids["Dataset"] = self.projection(q, parentids["Project"])

        if isinstance(target, DatasetI):
            parentids["Dataset"] = ids
        if parentids["Dataset"]:
            q = ("SELECT i.id FROM DatasetImageLink link "
                 "join link.parent ds "
                 "join link.child as i WHERE ds.id IN (:ids)")
            parentids["Image"] = self.projection(q, parentids["Dataset"])

        if isinstance(target, ImageI):
            parentids["Image"] = ids

        # TODO: This should really include:
        #    raise Exception("Unknown target: %s" % target.__class__.__name__)

        log.debug("Parent IDs: %s",
                  ["%s:%s" % (k, v is not None and len(v) or "NA")
                   for k, v in parentids.items()])

        self.mapannids = set()
        self.fileannids = set()
        not_annotatable = ('WellSample',)

        nss = self._get_configured_namespaces()
        for objtype, objids in parentids.iteritems():
            if objtype in not_annotatable:
                continue
            r = self._get_annotations_for_deletion(
                objtype, objids, 'MapAnnotation', nss)
            self.mapannids.update(r)

        log.info("Total: %d MapAnnotation(s) in %s",
                 len(set(self.mapannids)), nss)

        if self.attach:
            nss = [NSBULKANNOTATIONSCONFIG]
            for objtype, objids in parentids.iteritems():
                if objtype in not_annotatable:
                    continue
                r = self._get_annotations_for_deletion(
                    objtype, objids, 'FileAnnotation', nss)
                self.fileannids.update(r)

            log.info("Total: %d FileAnnotation(s) in %s",
                     len(set(self.fileannids)), nss)

    def write_to_omero(self, batch_size=1000):
        for batch in self._batch(self.mapannids, sz=batch_size):
            self._write_to_omero_batch({"MapAnnotation": batch})
        for batch in self._batch(self.fileannids, sz=batch_size):
            self._write_to_omero_batch({"FileAnnotation": batch})

    def _write_to_omero_batch(self, to_delete):
        delCmd = omero.cmd.Delete2(targetObjects=to_delete)
        try:
            callback = self.client.submit(
                delCmd, loops=100, failontimeout=True)
        except CmdError, ce:
            log.error("Failed to delete: %s" % to_delete)
            raise Exception(ce.err)

        # At this point, we're sure that there's a response OR
        # an exception has been thrown (likely LockTimeout)
        rsp = callback.getResponse()
        if isinstance(rsp, omero.cmd.OK):
            ndma = len(rsp.deletedObjects.get(
                "ome.model.annotations.MapAnnotation", []))
            ndfa = len(rsp.deletedObjects.get(
                "ome.model.annotations.FileAnnotation", []))
            if ndma:
                log.info("Deleted %d MapAnnotation(s)", ndma)
            if ndfa:
                log.info("Deleted %d FileAnnotation(s)", ndfa)
        else:
            log.error("Delete failed: %s", rsp)


def parse_target_object(target_object):
    type, id = target_object.split(':')
    if 'Dataset' == type:
        return DatasetI(long(id), False)
    if 'Plate' == type:
        return PlateI(long(id), False)
    if 'Screen' == type:
        return ScreenI(long(id), False)
    raise ValueError('Unsupported target object: %s' % target_object)


def parse_column_types(column_type_list):
    column_types = []
    for column_type in column_type_list:
        if column_type.lower() in COLUMN_TYPES:
            column_types.append(column_type.lower())
        else:
            column_types = []
            message = "\nColumn type '%s' unknown.\nChoose from following: " \
                "%s" % (column_type, ",".join(COLUMN_TYPES.keys()))
            raise MetadataError(message)
    return column_types


if __name__ == "__main__":
    try:
        options, args = getopt(sys.argv[1:], "s:p:u:w:k:c:id", ["columns="])
    except GetoptError, (msg, opt):
        usage(msg)

    try:
        target_object, file = args
        target_object = parse_target_object(target_object)
    except ValueError:
        usage('Target object and file must be a specified!')

    username = None
    password = None
    hostname = 'localhost'
    port = 4064  # SSL
    info = False
    session_key = None
    logging_level = logging.INFO
    thread_count = 1
    column_types = None
    context_class = ParsingContext
    for option, argument in options:
        if option == "-u":
            username = argument
        if option == "-w":
            password = argument
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
        if option == "--columns":
            column_types = parse_column_types(argument.split(','))
        if option == "-c":
            try:
                context_class = globals()[argument]
            except KeyError:
                usage("Invalid context class")
    if session_key is None and username is None:
        usage("Username must be specified!")
    if session_key is None and hostname is None:
        usage("Host name must be specified!")
    if session_key is None and password is None:
        password = getpass()

    logging.basicConfig(level=logging_level)
    client = client(hostname, port)
    client.setAgent("OMERO.populate_metadata")
    client.enableKeepAlive(60)
    try:
        if session_key is not None:
            client.joinSession(session_key)
            client.sf.detachOnDestroy()
        else:
            client.createSession(username, password)

        log.debug('Creating pool of %d threads' % thread_count)
        thread_pool = ThreadPool(thread_count)
        ctx = context_class(
            client, target_object, file, column_types=column_types)
        ctx.parse()
        if not info:
            ctx.write_to_omero()
    finally:
        pass
        client.closeSession()
