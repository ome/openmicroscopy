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
from StringIO import StringIO
from getpass import getpass
from getopt import getopt, GetoptError

# XXX: Haxxor
import omero.clients
import omero_Tables_ice
import omero_SharedResources_ice
from omero.rtypes import rdouble, rstring, rint
from omero.model import OriginalFileI, PlateI, PlateAnnotationLinkI, ImageI, FileAnnotationI, RoiI, EllipseI
from omero.grid import ImageColumn, RoiColumn, DoubleColumn
from omero import client

# Handle Python 2.5 built-in ElementTree
try:
        from xml.etree.ElementTree import XML, Element, SubElement, ElementTree, dump
except ImportError:
        from elementtree.ElementTree import XML, Element, SubElement, ElementTree, dump

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

class AbstractPlateAnalysisCtx(object):
    def __init__(self, original_files, original_file_image_map, plate_id,
                 service_factory):
        super(AbstractPlateAnalysisCtx, self).__init__()
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
        raise Exception("To be implemented by concrete implementations.")
    is_this_type = classmethod(is_this_type)

    def get_measurement_count(self):
        raise Exception("To be implemented by concrete implementations.")

    def get_measurement_ctx(self, index):
        raise Exception("To be implemented by concrete implementations.")

    def get_result_file_count(self, measurement_index):
        raise Exception("To be implemented by concrete implementations.")

class MIASPlateAnalysisCtx(AbstractPlateAnalysisCtx):
    datetime_format = '%Y-%m-%d-%Hh%Mm%Ss'
    
    log_regex = re.compile('.*log(\d+-\d+-\d+-\d+h\d+m\d+s).txt$')
    
    detail_regex = re.compile(
        '^Well\d+_.*_detail_(\d+-\d+-\d+-\d+h\d+m\d+s).txt$')
        
    def __init__(self, original_files, original_file_image_map, plate_id,
                 service_factory):
        super(MIASPlateAnalysisCtx, self).__init__(
                original_files, original_file_image_map, plate_id,
                service_factory)
        self._populate_log_and_detail_files()
        self._populate_measurements()

    def is_this_type(klass, original_files):
        for original_file in original_files.values():
            if klass.log_regex.match(original_file.name.val):
                return True
    is_this_type = classmethod(is_this_type)
    
    def _populate_log_and_detail_files(self):
        for original_file in self.original_files.values():
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
    
    def get_measurement_count(self):
        return len(self.measurements.keys())

    def get_measurement_ctx(self, index):
        key = self.log_files.keys()[index]
        sf = self.service_factory
        original_file = self.log_files[key]
        result_files = self.measurements[key]
        return MIASMeasurementCtx(self, sf, original_file, result_files)
    
    def get_result_file_count(self, measurement_index):
        key = self.log_files.keys()[measurement_index]
        return len(self.measurements[key])

class FlexPlateAnalysisCtx(AbstractPlateAnalysisCtx):
    def __init__(self, original_files, original_file_image_map, plate_id,
                 service_factory):
        super(FlexPlateAnalysisCtx, self).__init__(
                original_files, original_file_image_map, plate_id,
                service_factory)
        path_original_file_map = dict()
        for original_file in original_files.values():
            path = original_file.path.val
            format = original_file.format.value.val
            if format == 'Companion/Flex' and path.endswith('.res'):
                path_original_file_map[path] = original_file
        self.measurements = path_original_file_map.values()

    def is_this_type(klass, original_files):
        for original_file in original_files.values():
            path = original_file.path.val
            format = original_file.format.value.val
            if format == 'Companion/Flex' and path.endswith('.res'):
                return True
        return False
    is_this_type = classmethod(is_this_type)
    
    def get_measurement_count(self):
        return len(self.measurements)

    def get_measurement_ctx(self, index):
        sf = self.service_factory
        original_file = self.measurements[index]
        result_files = []
        return FlexMeasurementCtx(self, sf, original_file, result_files)

    def get_result_file_count(self, measurement_index):
        return 1

class PlateAnalysisCtxFactory(object):

    implementations = [FlexPlateAnalysisCtx, MIASPlateAnalysisCtx]

    def __init__(self, service_factory):
        self.service_factory = service_factory
        self.query_service = self.service_factory.getQueryService()
    
    def find_images_for_plate(self, plate_id):
        return self.query_service.findAllByQuery(
            'select img from Image as img ' \
            'left outer join fetch img.annotationLinks as a_links ' \
            'join img.wellSamples as ws ' \
            'join ws.well as w ' \
            'join w.plate as p ' \
            'join fetch a_links.child as a ' \
            'join fetch a.file as o_file ' \
            'join fetch o_file.format ' \
            'where p.id = %d' % plate_id, None)

    def get_analysis_ctx(self, plate_id):
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
                return klass(original_files,
                             original_file_image_map,
                             plate_id, service_factory)
        raise MeasurementError(
                "Unable to find suitable analysis context for plate: %d" % \
                        plate_id)

class AbstractMeasurementCtx(object):
    def __init__(self, analysis_ctx, service_factory, original_file,
                 result_files):
        super(AbstractMeasurementCtx, self).__init__()
        self.analysis_ctx = analysis_ctx
        self.service_factory = service_factory
        self.update_service = self.service_factory.getUpdateService()
        self.original_file = original_file
        self.result_files = result_files

        # Create a file annotation to represent our measurement
        self.file_annotation = FileAnnotationI()
        self.file_annotation.ns = \
            rstring('openmicroscopy.org/omero/measurement')
        name = self.get_name()
        self.file_annotation.description = rstring(name)
        
        # Create a new OMERO table to store our measurement results
        sr = self.service_factory.sharedResources()
        self.table = sr.newTable(1, '/%s.r5' % name)
        if self.table is None:
            raise MeasurementException(
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
        
        # Establish the rest of our initial state
        self.table_initialized = False
        self.n_columns = None
    
    def update_table(self, columns):
        if not self.table_initialized:
            t0 = int(time.time() * 1000)
            self.table.initialize(columns)
            print "Table init took %sms" % (int(time.time() * 1000) - t0)
        t0 = int(time.time() * 1000)
        self.table.addData(columns)
        print "Table update took %sms" % (int(time.time() * 1000) - t0)
        self.table_initialized = True
    
    def image_from_original_file(self, original_file):
        m= self.analysis_ctx.original_file_image_map
        return m[original_file.id.val]
   
    ###
    ### Abstract methods
    ### 

    def get_name(self):
        raise Exception("To be implemented by concrete implementations.")

    def parse_and_populate(self): 
        raise Exception("To be implemented by concrete implementations.")

class MIASMeasurementCtx(AbstractMeasurementCtx):
    
    IMAGE_COL = 0
    
    ROI_COL = 1

    def __init__(self, analysis_ctx, service_factory, original_file,
                 result_files):
        super(MIASMeasurementCtx, self).__init__(
                analysis_ctx, service_factory, original_file, result_files)
    
    def get_name(self):
        return self.original_file.name.val[:-4]
    
    def get_empty_columns(self, n_columns):
        if not self.table_initialized:
            columns = [ImageColumn('Image', '', list()),
                       RoiColumn('ROI', '', list())]
            for i in range(n_columns):
                columns.append(DoubleColumn('', '', list()))
            return columns
        else:
            return self.table.getHeaders()

    def parse_roi(self, image, row):
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
        unloaded_file_annotation = \
                FileAnnotationI(self.file_annotation.id, False)
        roi.linkAnnotation(unloaded_file_annotation)
        return roi

    def parse_and_populate(self):
        raw_file_store = self.service_factory.createRawFileStore()
        try:
            rois = list()
            columns = None
            for result_file in self.result_files:
                print "Parsing: %s" % result_file.name.val
                image = self.image_from_original_file(result_file)
                unloaded_image = ImageI(image.id.val, False)
                raw_file_store.setFileId(result_file.id.val)
                data = raw_file_store.read(0L, result_file.size.val)
                rows = list(csv.reader(StringIO(data), delimiter='\t'))
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
            n_roi = len(rois)
            print "Total ROI count: %d" % n_roi
            for i in range((len(rois) / 1000) + 1):
                t0 = int(time.time() * 1000)
                a = i * 1000
                b = (i + 1) * 1000
                to_save = rois[a:b]
                print "Saving %d ROI at [%d:%d]" % (len(to_save), a, b)
                to_save = self.update_service.saveAndReturnIds(to_save)
                print "ROI update took %sms" % (int(time.time() * 1000) - t0)
                for roi in to_save:
                    columns[self.ROI_COL].values.append(roi)
            self.update_table(columns)
        finally:
            raw_file_store.close()

class FlexMeasurementCtx(AbstractMeasurementCtx):
    def __init__(self, analysis_ctx, service_factory, original_file,
                 result_files):
        super(FlexMeasurementCtx, self).__init__(
                analysis_ctx, service_factory, original_file, result_files)
    
    def get_name(self):
        return self.original_file.name.val[:-4]
    
    def get_empty_columns(self, headers):
        columns = {'Image': ImageColumn('Image', '', list()}
        for header in headers:
            columns[header] = DoubleColumn(header, '', list())
        return columns
    
    def parse_and_populate(self):
        raw_file_store = self.service_factory.createRawFileStore()
        try:
            rois = list()
            columns = None
            print "Parsing: %s" % self.original_file.name.val
            image = self.image_from_original_file(self.original_file)
            unloaded_image = ImageI(image.id.val, False)
            raw_file_store.setFileId(self.original_file.id.val)
            data = raw_file_store.read(0L, self.original_file.size.val)
            et = ElementTree(file=StringIO(data))
            root = et.getroot()
            areas = root.findall('.//Areas/Area')
            print "Area count: %d" % len(areas)
            for i, area in enumerate(areas):
                result_parameters = \
                        area.findall('.//Wells/ResultParameters/Parameter')
                print "Area %d result children: %d" % (i, len(result_parameters))
                if len(result_parameters) == 0:
                    print "%s contains no analysis data." % self.get_name()
                    return
                headers = list()
                for result_parameter in result_parameters:
                    headers.append(result_parameter.text)
                columns = self.get_empty_columns(headers)
                wells = area.findall('.//Wells/Well')
                for well in wells:
                    results = well.findall('.//Result')
                    for result in results:
                        name = result.get('name')
                        columns[name].values.append(float(result.text))
                print columns
            print "Root: %s" % root
        finally:
            raw_file_store.close()

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
