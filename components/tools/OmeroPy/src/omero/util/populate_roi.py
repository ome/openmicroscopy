#!/usr/bin/env python
# encoding: utf-8
"""
...
"""

#
#  Copyright (C) 2009 University of Dundee. All rights reserved.
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


import time
import sys
import csv
import re
from tempfile import TemporaryFile
from StringIO import StringIO
from getpass import getpass
from getopt import getopt, GetoptError

import omero.clients
from omero.rtypes import rdouble, rstring, rint
from omero.model import OriginalFileI, PlateI, PlateAnnotationLinkI, ImageI, FileAnnotationI, RoiI, EllipseI
from omero.grid import ImageColumn, WellColumn, RoiColumn, DoubleColumn
from omero import client

# Handle Python 2.5 built-in ElementTree
try:
        from xml.etree.cElementTree import XML, Element, SubElement, ElementTree, dump, iterparse
except ImportError:
        from cElementTree import XML, Element, SubElement, ElementTree, dump, iterparse

def usage(error):
    """Prints usage so that we don't have to. :)"""
    cmd = sys.argv[0]
    print """%s
Usage: %s [-h hostname] [-u username | -k session_key] <-p port> [plate_id]
Runs measurement population code for a given plate.

Options:
  -u    OMERO username to use
  -k    OMERO session key to use
  -h    OMERO hostname to use
  -p    OMERO port to use [defaults to 4063]
  -m    Measurement index to populate
  -i    Dump measurement information and exit (no population)

Examples:
  %s -h localhost -p 4063 -u bob 27

Report bugs to ome-devel@lists.openmicroscopy.org.uk""" % (error, cmd, cmd)
    sys.exit(2)

class MeasurementError(Exception):
    """
    Raised by the analysis or measurement context when an error condition
    is reached.
    """
    pass

class DownloadingOriginalFileProvider(object):
    """
    Provides original file data by downloading it from an OMERO raw file store.
    """
    
    # Default raw file store buffer size
    BUFFER_SIZE = 1024 * 1024  # 1MB
    
    def __init__(self, service_factory):
        self.service_factory = service_factory
        self.raw_file_store = self.service_factory.createRawFileStore()

    def get_original_file_data(self, original_file):
        """
        Downloads an original file to a temporary file and returns an open
        file handle to that temporary file seeked to zero. The caller is
        responsible for closing the temporary file.
        """
        print "Reading from: %d" % original_file.id.val
        self.raw_file_store.setFileId(original_file.id.val)
        temporary_file = TemporaryFile()
        size = original_file.size.val
        for i in range((size / self.BUFFER_SIZE) + 1):
            index = i * self.BUFFER_SIZE
            data = self.raw_file_store.read(index, self.BUFFER_SIZE)
            temporary_file.write(data)
        temporary_file.seek(0L)
        temporary_file.truncate(size)
        return temporary_file

    def __delete__(self):
        self.raw_file_store.close()

class AbstractPlateAnalysisCtx(object):
    """
    Abstract class which aggregates and represents all measurement runs made on
    a given Plate.
    """
    
    DEFAULT_ORIGINAL_FILE_PROVIDER = DownloadingOriginalFileProvider
    
    def __init__(self, images, original_files, original_file_image_map,
                 plate_id, service_factory):
        super(AbstractPlateAnalysisCtx, self).__init__()
        self.images = images
        self.original_files = original_files
        self.original_file_image_map = original_file_image_map
        self.plate_id = plate_id
        self.service_factory = service_factory
        self.log_files = dict()
        self.detail_files = dict()
        self.measurements = dict()
    
    ###
    ### Abstract methods
    ### 

    def is_this_type(klass):
        """
        Concrete implementations are to return True if the class pertinent
        for the original files associated with the plate.
        """
        raise Exception("To be implemented by concrete implementations.")
    is_this_type = classmethod(is_this_type)

    def get_measurement_count(self):
        """Returns the number of recognized measurement runs."""
        raise Exception("To be implemented by concrete implementations.")

    def get_measurement_ctx(self, index):
        """Returns the measurement context for a given index."""
        raise Exception("To be implemented by concrete implementations.")

    def get_result_file_count(self, measurement_index):
        """
        Return the number of result files associated with a measurement run.
        """
        raise Exception("To be implemented by concrete implementations.")

class MIASPlateAnalysisCtx(AbstractPlateAnalysisCtx):
    """
    MIAS dataset concrete class implementation of an analysis context. MIAS
    measurements are aggregated based on a single "log" file. A result
    file is present for each stitched (of multiple fields) mosaic and
    contains the actual measured results and ROI.
    """

    # Python datetime format string of the log filename completion date/time
    datetime_format = '%Y-%m-%d-%Hh%Mm%Ss'
    
    # Regular expression matching a log filename
    log_regex = re.compile('.*log(\d+-\d+-\d+-\d+h\d+m\d+s).txt$')
    
    # Regular expression matching a result filename
    detail_regex = re.compile(
        '^Well\d+_.*_detail_(\d+-\d+-\d+-\d+h\d+m\d+s).txt$')

    # Companion file format
    companion_format = 'Companion/MIAS'
        
    def __init__(self, images, original_files, original_file_image_map,
                 plate_id, service_factory):
        super(MIASPlateAnalysisCtx, self).__init__(
                images, original_files, original_file_image_map, plate_id,
                service_factory)
        self._populate_log_and_detail_files()
        self._populate_measurements()

    def _populate_log_and_detail_files(self):
        """
        Strips out erroneous files and collects the log and result original
        files based on regular expression matching.
        """
        for original_file in self.original_files.values():
            if original_file.format.value.val != self.companion_format:
                continue
            name = original_file.name.val
            match = self.log_regex.match(name)
            if match:
                d = time.strptime(match.group(1), self.datetime_format)
                self.log_files[d] = original_file
                continue
            match = self.detail_regex.match(name)
            if match:
                d = time.strptime(match.group(1), self.datetime_format)
                self.detail_files[d] = original_file
                continue
    
    def _populate_measurements(self):
        """
        Result original files are only recognizable as part of a given
        measurement (declared by a log file) based upon their parsed
        date/time of completion as encoded in the filename. This method
        collects result original files and groups them by collective
        parsed date/time of completion.
        """
        log_timestamps = list(self.log_files.keys())
        log_timestamps.sort()
        detail_timestamps = list(self.detail_files.keys())
        detail_timestamps.sort()
        for log_timestamp in log_timestamps:
            self.measurements[log_timestamp] = list()
        for detail_timestamp in detail_timestamps:
            for log_timestamp in log_timestamps:
                if detail_timestamp < log_timestamp:
                    self.measurements[log_timestamp].append(
                        self.detail_files[detail_timestamp])
                    break
    
    ###
    ### Abstract method implementations
    ### 
    
    def is_this_type(klass, original_files):
        for original_file in original_files.values():
            format = original_file.format.value.val
            if format == klass.companion_format \
               and klass.log_regex.match(original_file.name.val):
                return True
    is_this_type = classmethod(is_this_type)
    
    def get_measurement_count(self):
        return len(self.measurements.keys())

    def get_measurement_ctx(self, index):
        key = self.log_files.keys()[index]
        sf = self.service_factory
        original_file = self.log_files[key]
        result_files = self.measurements[key]
        provider = self.DEFAULT_ORIGINAL_FILE_PROVIDER(sf)
        return MIASMeasurementCtx(self, sf, provider, original_file,
                                  result_files)
    
    def get_result_file_count(self, measurement_index):
        key = self.log_files.keys()[measurement_index]
        return len(self.measurements[key])

class FlexPlateAnalysisCtx(AbstractPlateAnalysisCtx):
    """
    Flex dataset concrete class implementation of an analysis context. Flex
    measurements are aggregated in a single ".res" XML file and contain no
    ROI.
    """
    
    # Companion file format
    companion_format = 'Companion/Flex'

    def __init__(self, images, original_files, original_file_image_map,
                 plate_id, service_factory):
        super(FlexPlateAnalysisCtx, self).__init__(
                images, original_files, original_file_image_map, plate_id,
                service_factory)
        path_original_file_map = dict()
        for original_file in original_files.values():
            path = original_file.path.val
            format = original_file.format.value.val
            if format == self.companion_format and path.endswith('.res'):
                path_original_file_map[path] = original_file
        self.measurements = path_original_file_map.values()
    
    ###
    ### Abstract method implementations
    ### 

    def is_this_type(klass, original_files):
        for original_file in original_files.values():
            path = original_file.path.val
            format = original_file.format.value.val
            if format == klass.companion_format and path.endswith('.res'):
                return True
        return False
    is_this_type = classmethod(is_this_type)
    
    def get_measurement_count(self):
        return len(self.measurements)

    def get_measurement_ctx(self, index):
        sf = self.service_factory
        original_file = self.measurements[index]
        result_files = []
        provider = self.DEFAULT_ORIGINAL_FILE_PROVIDER(sf)
        return FlexMeasurementCtx(self, sf, provider, original_file,
                                  result_files)

    def get_result_file_count(self, measurement_index):
        return 1

class InCellPlateAnalysisCtx(AbstractPlateAnalysisCtx):
    """
    InCell dataset concrete class implementation of an analysis context.
    InCell measurements are from InCell analyzer and are aggregated in a
    single gargantuan (often larger than 100MB per plate) XML file.
    """

    # Companion file format
    companion_format = 'Companion/InCell'

    def __init__(self, images, original_files, original_file_image_map,
                 plate_id, service_factory):
        super(InCellPlateAnalysisCtx, self).__init__(
                images, original_files, original_file_image_map, plate_id,
                service_factory)
        path_original_file_map = dict()
        for original_file in original_files.values():
            path = original_file.path.val
            format = original_file.format.value.val
            if format == self.companion_format and path.endswith('.xml'):
                path_original_file_map[path] = original_file
        self.measurements = path_original_file_map.values()

    ###
    ### Abstract method implementations
    ### 

    def is_this_type(klass, original_files):
        for original_file in original_files.values():
            path = original_file.path.val
            format = original_file.format.value.val
            if format == klass.companion_format and path.endswith('.xml'):
                return True
        return False
    is_this_type = classmethod(is_this_type)

    def get_measurement_count(self):
        return len(self.measurements)

    def get_measurement_ctx(self, index):
        sf = self.service_factory
        original_file = self.measurements[index]
        result_files = []
        provider = self.DEFAULT_ORIGINAL_FILE_PROVIDER(sf)
        return InCellMeasurementCtx(self, sf, provider, original_file,
                                    result_files)

    def get_result_file_count(self, measurement_index):
        return 1

class PlateAnalysisCtxFactory(object):
    """
    The plate analysis context factory is responsible for detecting and
    returning a plate analysis context instance for a given plate.
    """

    implementations = [FlexPlateAnalysisCtx, MIASPlateAnalysisCtx]

    def __init__(self, service_factory):
        self.service_factory = service_factory
        self.query_service = self.service_factory.getQueryService()
    
    def find_images_for_plate(self, plate_id):
        """
        Retrieves all the images associated with a given plate. Fetched
        are the Image's WellSample, the WellSample's Well, the annotation
        stack associated with the Image and each annotation's linked
        original file.
        """
        return self.query_service.findAllByQuery(
            'select img from Image as img ' \
            'left outer join fetch img.annotationLinks as a_links ' \
            'join fetch img.wellSamples as ws ' \
            'join fetch ws.well as w ' \
            'join w.plate as p ' \
            'join fetch a_links.child as a ' \
            'join fetch a.file as o_file ' \
            'join fetch o_file.format ' \
            'where p.id = %d' % plate_id, None)

    def get_analysis_ctx(self, plate_id):
        """Retrieves a plate analysis context for a given plate."""
        original_files = dict()
        original_file_image_map = dict()
        images = self.find_images_for_plate(plate_id)
        for i, image in enumerate(images):
            for annotation_link in image.copyAnnotationLinks():
                annotation = annotation_link.child
                if isinstance(annotation, FileAnnotationI):
                    f = annotation.file
                    original_files[f.id.val] = f
                    original_file_image_map[f.id.val] = image
        for klass in self.implementations:
            if klass.is_this_type(original_files):
                return klass(images, original_files,
                             original_file_image_map,
                             plate_id, self.service_factory)
        raise MeasurementError(
                "Unable to find suitable analysis context for plate: %d" % \
                        plate_id)

class AbstractMeasurementCtx(object):
    """
    Abstract class which aggregates and represents all the results produced
    from a given measurement run. It also provides a scaffold for interacting
    with the OmeroTables infrastructure.
    """
    
    def __init__(self, analysis_ctx, service_factory, original_file_provider,
                 original_file, result_files):
        super(AbstractMeasurementCtx, self).__init__()
        self.analysis_ctx = analysis_ctx
        self.service_factory = service_factory
        self.original_file_provider = original_file_provider
        self.query_service = self.service_factory.getQueryService()
        self.update_service = self.service_factory.getUpdateService()
        self.original_file = original_file
        self.result_files = result_files

        # Create a file annotation to represent our measurement
        self.file_annotation = FileAnnotationI()
        self.file_annotation.ns = \
            rstring('openmicroscopy.org/omero/measurement')
        name = self.get_name()
        self.file_annotation.description = rstring(name)
        
        # Establish the rest of our initial state
        self.n_columns = None

    def update_table(self, columns):
        """Updates the OmeroTables instance backing our results."""
        # Create a new OMERO table to store our measurement results
        sr = self.service_factory.sharedResources()
        name = self.get_name()
        self.table = sr.newTable(1, '/%s.r5' % name)
        if self.table is None:
            raise MeasurementError(
                "Unable to create table: %s" % name)
                
        # Retrieve the original file corresponding to the table for the
        # measurement, link it to the file annotation representing the
        # umbrella measurement run, link the annotation to the plate from
        # which it belongs and save the file annotation.
        table_original_file = self.table.getOriginalFile()
        table_original_file_id = table_original_file.id.val
        print "Created new table: %d" % table_original_file_id
        unloaded_o_file = OriginalFileI(table_original_file_id, False)
        self.file_annotation.file = unloaded_o_file
        unloaded_plate = PlateI(self.analysis_ctx.plate_id, False)
        plate_annotation_link = PlateAnnotationLinkI()
        plate_annotation_link.parent = unloaded_plate
        plate_annotation_link.child = self.file_annotation
        plate_annotation_link = \
                self.update_service.saveAndReturnObject(plate_annotation_link)
        self.file_annotation = plate_annotation_link.child
        
        t0 = int(time.time() * 1000)
        self.table.initialize(columns)
        print "Table init took %sms" % (int(time.time() * 1000) - t0)
        t0 = int(time.time() * 1000)
        self.table.addData(columns)
        print "Table update took %sms" % (int(time.time() * 1000) - t0)
    
    def image_from_original_file(self, original_file):
        """Returns the image from wich an original file has originated."""
        m = self.analysis_ctx.original_file_image_map
        return m[original_file.id.val]
        
    def parse_and_populate(self):
        """
        Calls parse and populate, updating the OmeroTables instance backing
        our results and the OMERO database itself.
        """
        columns = self.parse()
        if columns is not None:
            self.populate(columns)
   
    ###
    ### Abstract methods
    ### 

    def get_name(self):
        """Returns the name of the measurement."""
        raise Exception("To be implemented by concrete implementations.")
    
    def parse(self):
        """Parses result files, returning a list of OmeroTables columns."""
        raise Exception("To be implemented by concrete implementations.")
        
    def populate(self, columns):
        """
        Populates an OmeroTables instance backing our results and the OMERO
        database itself.
        """
        raise Exception("To be implemented by concrete implementations.")

class MIASMeasurementCtx(AbstractMeasurementCtx):
    """
    MIAS measurements are a set of tab delimited text files per well. Each
    TSV file's content is prefixed by the analysis parameters.
    """
    
    # The OmeroTable ImageColumn index
    IMAGE_COL = 0
    
    # The OmeroTable RoiColumn index
    ROI_COL = 1

    def __init__(self, analysis_ctx, service_factory, original_file_provider,
                 original_file, result_files):
        super(MIASMeasurementCtx, self).__init__(
                analysis_ctx, service_factory, original_file_provider,
                original_file, result_files)
    
    def get_empty_columns(self, n_columns):
        """
        Retrieves a set of empty OmeroTables columns for the analysis results
        prefixed by an ImageColumn and RoiColumn to handle these linked
        object indexes.
        """
        columns = [ImageColumn('Image', '', list()),
                   RoiColumn('ROI', '', list())]
        for i in range(n_columns):
            columns.append(DoubleColumn('', '', list()))
        return columns

    def parse_roi(self, image, row):
        """
        Parses an ROI from a given row of tab delimited results linking the
        resulting ROI to the umbrella file annotation and the source image.
        """
        roi = RoiI()
        ellipse = EllipseI()
        diameter = rdouble(float(row[4]))
        ellipse.theZ = rint(0)
        ellipse.theT = rint(0)
        ellipse.cx = rdouble(float(row[2]))
        ellipse.cy = rdouble(float(row[1]))
        ellipse.rx = diameter
        ellipse.ry = diameter
        roi.addShape(ellipse)
        roi.image = image
        roi.linkAnnotation(self.file_annotation)
        return roi
    
    ###
    ### Abstract method implementations
    ### 

    def get_name(self):
        return self.original_file.name.val[:-4]
        
    def parse(self):
        rois = list()
        columns = None
        for result_file in self.result_files:
            print "Parsing: %s" % result_file.name.val
            image = self.image_from_original_file(result_file)
            unloaded_image = ImageI(image.id.val, False)
            provider = self.original_file_provider
            data = provider.get_original_file_data(result_file)
            try:
                rows = list(csv.reader(data, delimiter='\t'))
            finally:
                data.close()
            rows.reverse()
            if columns is None:
                columns = self.get_empty_columns(len(rows[0]))
            for row in rows:
                try:
                    for i, value in enumerate(row):
                        value = float(value)
                        columns[i + 2].values.append(value)
                    columns[self.IMAGE_COL].values.append(image.id.val)
                    rois.append(self.parse_roi(unloaded_image, row))
                except ValueError:
                    for i, value in enumerate(row):
                        columns[i + 2].name = value
                    break
        self.rois = rois
        return columns

    def populate(self, columns):
        n_roi = len(self.rois)
        print "Total ROI count: %d" % n_roi
        for i in range((len(self.rois) / 1000) + 1):
            t0 = int(time.time() * 1000)
            a = i * 1000
            b = (i + 1) * 1000
            to_save = self.rois[a:b]
            print "Saving %d ROI at [%d:%d]" % (len(to_save), a, b)
            to_save = self.update_service.saveAndReturnIds(to_save)
            print "ROI update took %sms" % (int(time.time() * 1000) - t0)
            for roi in to_save:
                columns[self.ROI_COL].values.append(roi)
        first_roi = columns[self.ROI_COL].values[0]
        first_roi = self.query_service.findByQuery(
                'select roi from Roi as roi ' \
                'join fetch roi.annotationLinks as link ' \
                'join fetch link.child ' \
                'where roi.id = %d' % first_roi, None)
        self.file_annotation = first_roi.copyAnnotationLinks()[0].child
        self.update_table(columns)

class FlexMeasurementCtx(AbstractMeasurementCtx):
    """
    Flex measurements are located deep within a ".res" XML file container
    and contain no ROI.
    """

    # The XPath to the <Area> which aggregate an acquisition
    AREA_XPATH = './/Areas/Area'

    # The XPath to the an analysis <Parameter>; will become a column header
    # and is below AREA_XPATH
    PARAMETER_XPATH = './/Wells/ResultParameters/Parameter'

    # The XPath to a <Well> which has had at least one acquisition event
    # within and is below AREA_XPATH
    WELL_XPATH = './/Wells/Well'

    # The XPath to a <Result> for a given well and is below WELL_XPATH
    RESULT_XPATH = './/Result'

    def __init__(self, analysis_ctx, service_factory, original_file_provider,
                 original_file, result_files):
        super(FlexMeasurementCtx, self).__init__(
                analysis_ctx, service_factory, original_file_provider,
                original_file, result_files)
        self.wells = dict()
        for image in self.analysis_ctx.images:
            for well_sample in image.copyWellSamples():
                well = well_sample.well
                row = well.row.val
                column = well.column.val
                if row not in self.wells.keys():
                    self.wells[row] = dict()
                self.wells[row][column] = well
    
    def get_empty_columns(self, headers):
        """
        Retrieves a set of empty OmeroTables columns for the analysis results
        prefixed by a WellColumn to handle linked object indexes.
        """
        columns = {'Well': WellColumn('Well', '', list())}
        for header in headers:
            columns[header] = DoubleColumn(header, '', list())
        return columns
    
    ###
    ### Abstract method implementations
    ### 
    
    def get_name(self):
        return self.original_file.name.val[:-4]

    def parse(self):
        print "Parsing: %s" % self.original_file.name.val
        image = self.image_from_original_file(self.original_file)
        unloaded_image = ImageI(image.id.val, False)
        provider = self.original_file_provider
        data = provider.get_original_file_data(self.original_file)
        try:
            et = ElementTree(file=data)
        finally:
            data.close()
        root = et.getroot()
        areas = root.findall(self.AREA_XPATH)
        print "Area count: %d" % len(areas)
        for i, area in enumerate(areas):
            result_parameters = area.findall(self.PARAMETER_XPATH)
            print "Area %d result children: %d" % (i, len(result_parameters))
            if len(result_parameters) == 0:
                print "%s contains no analysis data." % self.get_name()
                return
            headers = list()
            for result_parameter in result_parameters:
                headers.append(result_parameter.text)
            columns = self.get_empty_columns(headers)
            wells = area.findall(self.WELL_XPATH)
            for well in wells:
                # Rows and columns are 1-indexed, OMERO wells are 0-indexed
                row = int(well.get('row')) - 1
                column = int(well.get('col')) - 1
                try:
                    v = columns['Well'].values
                    v.append(self.wells[row][column].id.val)
                except KeyError:
                    # This has the potential to happen alot with the
                    # datasets we have given the split machine acquisition
                    # ".flex" file storage.
                    print "WARNING: Missing data for row %d column %d" % \
                            (row, column)
                    continue
                results = well.findall(self.RESULT_XPATH)
                for result in results:
                    name = result.get('name')
                    columns[name].values.append(float(result.text))
        return columns.values()

    def populate(self, columns):
        self.update_table(columns)

class InCellMeasurementCtx(AbstractMeasurementCtx):
    """
    InCell analyzer measurements are located deep within an XML file container.
    """

    def __init__(self, analysis_ctx, service_factory, original_file_provider,
                 original_file, result_files):
        super(InCellMeasurementCtx, self).__init__(
                analysis_ctx, service_factory, original_file_provider,
                original_file, result_files)
        self.wells = dict()
        for image in self.analysis_ctx.images:
            for well_sample in image.copyWellSamples():
                well = well_sample.well
                row = well.row.val
                column = well.column.val
                if row not in self.wells.keys():
                    self.wells[row] = dict()
                self.wells[row][column] = well

    ###
    ### Abstract method implementations
    ### 

    def get_name(self):
        return self.original_file.name.val[:-4]

    def parse(self):
        print "Parsing: %s" % self.original_file.name.val
        image = self.image_from_original_file(self.original_file)
        unloaded_image = ImageI(image.id.val, False)
        provider = self.original_file_provider
        data = provider.get_original_file_data(self.original_file)
        try:
            events = ('start', 'end')
            well_data = None
            n_roi = 0
            n_measurements = 0
            columns = {'Image': ImageColumn('Image', '', list())}
            for event, element in iterparse(data, events=events):
                if event == 'start' and element.tag == 'WellData' \
                   and element.get('cell') != 'Summary':
                    well_data = element
                    # FIXME: Haxxor
                    columns['Image'].values.append(n_roi)
                elif well_data is not None and event == 'start' \
                     and element.tag == 'Measure':
                    key = element.get('key')
                    value = float(element.get('value'))
                    if n_roi == 0:
                        columns[key] = DoubleColumn(key, '', list())
                    columns[key].values.append(value)
                    n_measurements += 1
                elif event == 'end' and element.tag == 'WellData':
                    if well_data is not None:
                        n_roi += 1
                        well_data.clear()
                        well_data = None
                else:
                    element.clear()
            print "Total ROI: %d" % n_roi
            print "Total measurements: %d" % n_measurements
            return columns.values()
        finally:
            data.close()

    def populate(self, columns):
        self.update_table(columns)

if __name__ == "__main__":
    try:
        options, args = getopt(sys.argv[1:], "h:p:u:m:k:i")
    except GetoptError, (msg, opt):
        usage(msg)

    try:
        plate_id, = args
        plate_id = long(plate_id)
    except ValueError:
        usage("Plate ID must be a specified and a number!")
    
    username = None
    hostname = None
    port = 4063
    measurement = None
    info = False
    session_key = None
    for option, argument in options:
        if option == "-u":
            username = argument
        if option == "-h":
            hostname = argument
        if option == "-p":
            port = int(argument)
        if option == "-m":
            measurement = int(argument)
        if option == "-i":
            info = True
        if option == "-k":
            session_key = argument
    if session_key is None and username is None:
        usage("Username must be specified!")
    if session_key is None and hostname is None:
        usage("Host name must be specified!")
    if session_key is None:
        password = getpass()
    
    c = client(hostname, port)
    c.enableKeepAlive(60)
    try:
        if session_key is not None:
            service_factory = c.createSession(session_key)
        else:
            service_factory = c.createSession(username, password)
    
        factory = PlateAnalysisCtxFactory(service_factory)
        analysis_ctx = factory.get_analysis_ctx(plate_id)
        n_measurements = analysis_ctx.get_measurement_count()
        if measurement is not None and measurement >= n_measurements:
            usage("measurement %d not a valid index!")
        if info:
            for i in range(n_measurements):
                n_result_files = analysis_ctx.get_result_file_count(i)
                print "Measurement %d has %d result files." % \
                        (i, n_result_files)
            sys.exit(0)
        if measurement is not None:
            measurement_ctx = analysis_ctx.get_measurement_ctx(measurement)
            measurement_ctx.parse_and_populate()
        else:
            for i in range(n_measurements):
                measurement_ctx = analysis_ctx.get_measurement_ctx(i)
                measurement_ctx.parse_and_populate()
    finally:
        c.closeSession()
