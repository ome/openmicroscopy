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
import sys
import csv
import re
import json
import yaml
from getpass import getpass
from getopt import getopt, GetoptError
from itertools import izip

import omero.clients
from omero.callbacks import CmdCallbackI
from omero.rtypes import rstring, unwrap
from omero.model import DatasetAnnotationLinkI, DatasetI, FileAnnotationI
from omero.model import OriginalFileI, PlateI, PlateAnnotationLinkI, ScreenI
from omero.model import PlateAcquisitionI, WellI, WellSampleI, ImageI
from omero.model import ScreenAnnotationLinkI
from omero.model import MapAnnotationI, NamedValue
from omero.grid import ImageColumn, LongColumn, PlateColumn
from omero.grid import StringColumn, WellColumn
from omero.util.metadata_utils import KeyValueListPassThrough
from omero.util.metadata_utils import KeyValueListTransformer
from omero import client

from populate_roi import ThreadPool

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
  -w    OMERO password
  -k    OMERO session key to use
  -i    Dump measurement information and exit (no population)
  -d    Print debug statements
  -c    Use an alternative context (for expert users only)

Examples:
  %s -s localhost -p 14064 -u bob Plate:6 metadata.csv

Report bugs to ome-devel@lists.openmicroscopy.org.uk""" % (error, cmd, cmd)
    sys.exit(2)

# Global thread pool for use by workers
thread_pool = None

# Special column names we may add depending on the data type
PLATE_NAME_COLUMN = 'Plate Name'
WELL_NAME_COLUMN = 'Well Name'
IMAGE_NAME_COLUMN = 'Image Name'


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

    plate_keys = {
        'well': WellColumn,
        'field': ImageColumn,
        'row': LongColumn,
        'column': LongColumn,
        'wellsample': ImageColumn,
        'image': ImageColumn
    }

    screen_keys = dict({
        'plate': PlateColumn,
    }, **plate_keys)

    def __init__(self, target_object, headers):
        self.target_object = target_object
        self.headers = headers
        self.headers_as_lower = [v.lower() for v in self.headers]

    def create_columns(self):
        target_class = self.target_object.__class__
        target_id = self.target_object.id.val
        if ScreenI is target_class:
            log.debug('Creating columns for Screen:%d' % target_id)
            return self.create_columns_screen()
        if PlateI is target_class:
            log.debug('Creating columns for Plate:%d' % target_id)
            return self.create_columns_plate()
        if DatasetI is target_class:
            log.debug('Creating columns for Dataset:%d' % target_id)
            return self.create_columns_dataset()
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
            try:
                column = self.screen_keys[header_as_lower](name, description,
                                                           list())
            except KeyError:
                column = StringColumn(name, description,
                                      self.DEFAULT_COLUMN_SIZE, list())
            columns.append(column)
        for column in columns:
            if column.__class__ is PlateColumn:
                columns.append(StringColumn(PLATE_NAME_COLUMN, '',
                               self.DEFAULT_COLUMN_SIZE, list()))
            if column.__class__ is WellColumn:
                columns.append(StringColumn(WELL_NAME_COLUMN, '',
                               self.DEFAULT_COLUMN_SIZE, list()))
            if column.__class__ is ImageColumn:
                columns.append(StringColumn(IMAGE_NAME_COLUMN, '',
                               self.DEFAULT_COLUMN_SIZE, list()))
        self.columns_sanity_check(columns)
        return columns

    def create_columns_plate(self):
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
            try:
                column = self.plate_keys[header_as_lower](name, description,
                                                          list())
            except KeyError:
                column = StringColumn(name, description,
                                      self.DEFAULT_COLUMN_SIZE, list())
            columns.append(column)
        for column in columns:
            if column.__class__ is PlateColumn:
                columns.append(StringColumn(PLATE_NAME_COLUMN, '',
                               self.DEFAULT_COLUMN_SIZE, list()))
            if column.__class__ is WellColumn:
                columns.append(StringColumn(WELL_NAME_COLUMN, '',
                               self.DEFAULT_COLUMN_SIZE, list()))
            if column.__class__ is ImageColumn:
                columns.append(StringColumn(IMAGE_NAME_COLUMN, '',
                               self.DEFAULT_COLUMN_SIZE, list()))
        self.columns_sanity_check(columns)
        return columns

    def create_columns_dataset(self):
        raise Exception('To be implemented!')


class ValueResolver(object):
    """
    Value resolver for column types which is responsible for filling up
    non-metadata columns with their OMERO data model identifiers.
    """

    AS_ALPHA = [chr(v) for v in range(97, 122 + 1)]  # a-z
    WELL_REGEX = re.compile(r'^([a-zA-Z]+)(\d+)$')

    def __init__(self, client, target_object):
        self.client = client
        self.target_object = target_object
        self.target_class = self.target_object.__class__
        if PlateI is self.target_class:
            return self.load_plate()
        if DatasetI is self.target_class:
            return self.load_dataset()
        if ScreenI is self.target_class:
            return self.load_screen()
        raise MetadataError(
            'Unsupported target object class: %s' % self.target_class)

    def load_screen(self):
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
        self.images_by_id = dict()
        self.wells_by_location = dict()
        self.wells_by_id = dict()
        self.plates_by_name = dict()
        self.plates_by_id = dict()
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
            images_by_id = dict()
            self.wells_by_location[plate.name.val] = wells_by_location
            self.wells_by_id[plate.id.val] = wells_by_id
            self.images_by_id[plate.id.val] = images_by_id
            self.parse_plate(
                plate, wells_by_location, wells_by_id, images_by_id
            )

    def load_plate(self):
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

    def load_dataset(self):
        raise Exception('To be implemented!')

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
        if PlateColumn is column_class:
            try:
                return self.plates_by_name[value].id.val
            except KeyError:
                log.warn('Screen is missing plate: %s' % value)
                return Skip()
        if column_as_lower in ('row', 'column') \
           and column_class is LongColumn:
            try:
                # The value is not 0 offsetted
                return long(value) - 1
            except ValueError:
                return long(self.AS_ALPHA.index(value.lower()))
        if StringColumn is column_class:
            return value
        raise MetadataError('Unsupported column class: %s' % column_class)


class ParsingContext(object):
    """Generic parsing context for CSV files."""

    def __init__(self, client, target_object, file):
        self.client = client
        self.target_object = target_object
        self.file = file
        self.value_resolver = ValueResolver(self.client, self.target_object)

    def create_annotation_link(self):
        self.target_class = self.target_object.__class__
        if ScreenI is self.target_class:
            return ScreenAnnotationLinkI()
        if PlateI is self.target_class:
            return PlateAnnotationLinkI()
        if DatasetI is self.target_class:
            return DatasetAnnotationLinkI()
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
        log.debug('Header: %r' % rows[0])
        self.header_resolver = HeaderResolver(self.target_object, rows[0])
        self.columns = self.header_resolver.create_columns()
        log.debug('Columns: %r' % self.columns)
        self.populate(rows[1:])
        self.post_process()
        log.debug('Column widths: %r' % self.get_column_widths())
        log.debug('Columns: %r' % [
            (o.name, len(o.values)) for o in self.columns])

    def parse(self):
        data = open(self.file, 'U')
        try:
            return self.parse_from_handle(data)
        finally:
            data.close()

    def populate(self, rows):
        for row in rows:
            values = list()
            row = [(self.columns[i], value) for i, value in enumerate(row)]
            for column, original_value in row:
                log.debug('Original value %s, %s' % (original_value, column))
                value = self.value_resolver.resolve(
                    column, original_value, row)
                if value.__class__ is Skip:
                    break
                values.append(value)
                try:
                    if value.__class__ is not long:
                        column.size = max(column.size, len(value))
                except TypeError:
                    log.error('Original value "%s" now "%s" of bad type!' % (
                        original_value, value))
                    raise
            if value.__class__ is not Skip:
                values.reverse()
                for column in self.columns:
                    if column.name in (PLATE_NAME_COLUMN, WELL_NAME_COLUMN,
                                       IMAGE_NAME_COLUMN):
                        continue
                    try:
                        column.values.append(values.pop())
                    except IndexError:
                        log.error(
                            'Column %s has no values to pop.' % column.name)
                        raise

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
        for i in range(0, len(self.columns[0].values)):
            if well_name_column is not None:
                if PlateI is self.value_resolver.target_class:
                    plate = self.value_resolver.target_object.id.val
                elif ScreenI is self.value_resolver.target_class:
                    plate = columns_by_name['Plate'].values[i]
                v = ''
                try:
                    well = self.value_resolver.wells_by_id[plate]
                    well = well[well_column.values[i]]
                    row = well.row.val
                    col = well.column.val
                    row = self.value_resolver.AS_ALPHA[row]
                    v = '%s%d' % (row, col + 1)
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
                if PlateI is self.value_resolver.target_class:
                    plate = self.value_resolver.target_object.id.val
                elif ScreenI is self.value_resolver.target_class:
                    plate = columns_by_name['Plate'].values[i]
                try:
                    image = self.value_resolver.images_by_id[plate]
                    image = image[image_column.values[i]]
                except KeyError:
                    log.error(
                        'Missing row or column for image name population!')
                    raise
                name = image.name.val
                image_name_column.size = max(image_name_column.size, len(name))
                image_name_column.values.append(name)
            else:
                log.info('Missing image name column, skipping.')

            if plate_name_column is not None:
                plate = columns_by_name['Plate'].values[i]
                plate = self.value_resolver.plates_by_id[plate]
                v = plate.name.val
                plate_name_column.size = max(plate_name_column.size, len(v))
                plate_name_column.values.append(v)
            else:
                log.info('Missing plate name column, skipping.')

    def write_to_omero(self):
        sf = self.client.getSession()
        group = str(self.value_resolver.target_object.details.group.id.val)
        sr = sf.sharedResources()
        update_service = sf.getUpdateService()
        name = 'bulk_annotations'
        table = sr.newTable(1, name, {'omero.group': group})
        if table is None:
            raise MetadataError(
                "Unable to create table: %s" % name)
        original_file = table.getOriginalFile()
        log.info('Created new table OriginalFile:%d' % original_file.id.val)
        table.initialize(self.columns)
        log.info('Table initialized with %d columns.' % (len(self.columns)))
        table.addData(self.columns)
        log.info('Added data column data.')
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


class BulkToMapAnnotationContext(object):
    """
    Processor for creating MapAnnotations from BulkAnnotations.
    """

    def __init__(self, client, target_object, ofileid=None, cfgfileid=None):
        """
        :param client: OMERO client object
        :param target_object: The object to be annotated
        :param ofileid: The OriginalFile ID of the bulk-annotations table,
               default is to use the a bulk-annotation attached to
               target_object
        """
        self.client = client
        # Reload object to get .details
        self.target_object = self.get_target(target_object)
        if ofileid:
            self.ofileid = ofileid
        else:
            self.ofileid = self.get_bulk_annotation_file()
        if not self.ofileid:
            raise MetadataError("Unable to find bulk-annotations file")
        if cfgfileid:
            self.default_cfg, self.column_cfgs = self.get_config(cfgfileid)

    def get_target(self, target_object):
        qs = self.client.getSession().getQueryService()
        return qs.find(target_object.ice_staticId().split('::')[-1],
                       target_object.id.val)

    def get_config(self, cfgfileid):
        rfs = self.client.getSession().createRawFileStore()
        try:
            rfs.setFileId(cfgfileid)
            rawdata = rfs.read(0, rfs.size())
        finally:
            rfs.close()

        cfg = list(yaml.load_all(rawdata))
        if len(cfg) != 1:
            raise Exception(
                "Expected YAML file with one document, found %d" % len(cfg))
        cfg = cfg[0]

        default_cfg = cfg.get("defaults")
        column_cfgs = cfg.get("columns")
        if not default_cfg and not column_cfgs:
            raise Exception(
                "Configuration defaults and columns were both empty")
        return default_cfg, column_cfgs

    def get_bulk_annotation_file(self):
        otype = self.target_object.ice_staticId().split('::')[-1]
        q = """SELECT child.file.id FROM %sAnnotationLink link
               WHERE parent.id=:id AND child.ns=:ns ORDER by id""" % otype
        params = omero.sys.ParametersI()
        params.addId(unwrap(self.target_object.getId()))
        params.addString('ns', omero.constants.namespaces.NSBULKANNOTATIONS)
        qs = self.client.getSession().getQueryService()
        r = qs.projection(q, params)
        if r:
            return unwrap(r[-1][0])

    @staticmethod
    def create_map_annotation(targets, rowkvs):
        ma = MapAnnotationI()
        ma.setNs(rstring(omero.constants.namespaces.NSBULKANNOTATIONS))
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
            tr = KeyValueListTransformer(
                headers, self.default_cfg, self.column_cfgs)
        else:
            tr = KeyValueListPassThrough(headers)

        mas = []
        for row in izip(*(c.values for c in data.columns)):
            rowkvs = tr.transform(row)
            targets = []
            for omerotype, n in idcols:
                if row[n] > 0:
                    targets.append(omerotype(row[n], False))
                else:
                    log.warn("Invalid Id:%d found in row %s", row[n], row)
            if targets:
                malinks = self.create_map_annotation(targets, rowkvs)
                log.debug('\n\t'.join("%s=%s" % (v.name, v.value)
                          for v in malinks[0].getChild().getMapValue()))
                mas.extend(malinks)

        self.mapannotations = mas

    def write_to_omero(self):
        sf = self.client.getSession()
        group = str(self.target_object.details.group.id.val)
        update_service = sf.getUpdateService()
        ids = update_service.saveAndReturnIds(
            self.mapannotations, {'omero.group': group})
        log.info('Created %d MapAnnotations', len(ids))


class DeleteMapAnnotationContext(object):
    """
    Processor for deleting MapAnnotations in the BulkAnnotations namespace
    on these types: Image WellSample Well PlateAcquisition Plate Screen
    """

    def __init__(self, client, target_object, dummy=None):
        """
        :param client: OMERO client object
        :param target_object: The object to be processed
        """
        self.client = client
        self.target_object = target_object

    def parse(self):
        return self.populate()

    def populate(self):
        def projection(q, ids, ns=None):
            qs = self.client.getSession().getQueryService()
            params = omero.sys.ParametersI()
            params.addIds(ids)
            if ns:
                params.addString("ns", ns)
            log.debug("Query: %s len(IDs): %d", q, len(ids))
            rss = unwrap(qs.projection(q, params))
            return [r for rs in rss for r in rs]

        # Hierarchy: Screen, Plate, {PlateAcquistion, Well}, WellSample, Image
        parentids = {
            "Screen": None,
            "Plate":  None,
            "PlateAcquisition": None,
            "Well": None,
            "WellSample": None,
            "Image": None,
        }

        target = self.target_object
        ids = [unwrap(target.getId())]

        if isinstance(target, ScreenI):
            q = ("SELECT child.id FROM ScreenPlateLink "
                 "WHERE parent.id in (:ids)")
            parentids["Screen"] = ids
        if parentids["Screen"]:
            parentids["Plate"] = projection(q, parentids["Screen"])

        if isinstance(target, PlateI):
            parentids["Plate"] = ids
        if parentids["Plate"]:
            q = "SELECT id FROM PlateAcquisition WHERE plate.id IN (:ids)"
            parentids["PlateAcquisition"] = projection(q, parentids["Plate"])
            q = "SELECT id FROM Well WHERE plate.id IN (:ids)"
            parentids["Well"] = projection(q, parentids["Plate"])

        if isinstance(target, PlateAcquisitionI):
            parentids["PlateAcquisition"] = ids
        if parentids["PlateAcquisition"] and not isinstance(target, PlateI):
            # WellSamples are linked to PlateAcqs and Plates, so only get
            # if they ahven't been obtained via a Plate
            # Also note that we do not get Wells if the parent is a
            # PlateAcquisition since this only refers to the fields in
            # the well
            q = "SELECT id FROM WellSample WHERE plateAcquisition.id IN (:ids)"
            parentids["WellSample"] = projection(
                q, parentids["PlateAcquisition"])

        if isinstance(target, WellI):
            parentids["Well"] = ids
        if parentids["Well"]:
            q = "SELECT id FROM WellSample WHERE well.id IN (:ids)"
            parentids["WellSample"] = projection(q, parentids["Well"])

        if isinstance(target, WellSampleI):
            parentids["WellSample"] = ids
        if parentids["WellSample"]:
            q = "SELECT image.id FROM WellSample WHERE id IN (:ids)"
            parentids["Image"] = projection(q, parentids["WellSample"])

        if isinstance(target, ImageI):
            parentids["Image"] = ids

        log.debug("Parent IDs: %s", parentids)

        mapannids = []
        not_annotatable = ('WellSample',)
        ns = omero.constants.namespaces.NSBULKANNOTATIONS
        for objtype, objids in parentids.iteritems():
            r = []
            if objids and objtype not in not_annotatable:
                q = ("SELECT child.id FROM %sAnnotationLink WHERE "
                     "child.class=MapAnnotation AND parent.id in (:ids) "
                     "AND child.ns=:ns")
                r = projection(q % objtype, objids, ns)
                mapannids.extend(r)
            log.debug("%s: %d MapAnnotations", objtype, len(set(r)))

        log.info("Total: %d MapAnnotations in %s", len(set(mapannids)), ns)
        self.mapannids = mapannids

    def write_to_omero(self):
        to_delete = {"MapAnnotation": self.mapannids}
        delCmd = omero.cmd.Delete2(targetObjects=to_delete)
        handle = self.client.getSession().submit(delCmd)

        callback = None
        try:
            callback = CmdCallbackI(self.client, handle)
            loops = max(10, len(self.mapannids) / 10)
            delay = 500
            callback.loop(loops, delay)
            rsp = callback.getResponse()
            if isinstance(rsp, omero.cmd.OK):
                deleted = rsp.deletedObjects.get(
                    "ome.model.annotations.MapAnnotation", [])
                log.info("Deleted %d MapAnnotations", len(deleted))
            else:
                log.error("Delete failed: %s", rsp)
        finally:
            if callback:
                callback.close(True)
            else:
                handle.close()


def parse_target_object(target_object):
    type, id = target_object.split(':')
    if 'Dataset' == type:
        return DatasetI(long(id), False)
    if 'Plate' == type:
        return PlateI(long(id), False)
    if 'Screen' == type:
        return ScreenI(long(id), False)
    raise ValueError('Unsupported target object: %s' % target_object)

if __name__ == "__main__":
    try:
        options, args = getopt(sys.argv[1:], "s:p:u:w:k:c:id")
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
        ctx = context_class(client, target_object, file)
        ctx.parse()
        if not info:
            ctx.write_to_omero()
    finally:
        pass
        client.closeSession()
