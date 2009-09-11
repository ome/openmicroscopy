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

from omero import client, rdouble, rstring, rint
from omero.model import OriginalFileI, PlateI, PlateAnnotationLinkI, ImageI, FileAnnotationI, RoiI, EllipseI
from omero.grid import ImageColumn, RoiColumn, DoubleColumn


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

class MIASPlateAnalysisCtx(object):
    datetime_format = '%Y-%m-%d-%Hh%Mm%Ss'
    
    log_regex = re.compile('.*log(\d+-\d+-\d+-\d+h\d+m\d+s).txt$')
    
    detail_regex = re.compile(
        '^Well\d+_.*_detail_(\d+-\d+-\d+-\d+h\d+m\d+s).txt$')
        
    def __init__(self, query_service, plate_id):
        self.query_service = query_service
        self.plate_id = plate_id
        self.original_files = dict()
        self.log_files = dict()
        self.detail_files = dict()
        self.measurements = dict()
        self.original_file_image_map = dict()
        self._find_images_for_plate(plate_id)
        for i, image in enumerate(self.images):
            for annotation_link in image.copyAnnotationLinks():
                annotation = annotation_link.child
                if isinstance(annotation, FileAnnotationI):
                    f = annotation.file
                    self.original_files[f.id.val] = f
                    self.original_file_image_map[f.id.val] = image
        self._populate_log_and_detail_files()
        self._populate_measurements()
    
    def _find_images_for_plate(self, plate_id):
        self.images = self.query_service.findAllByQuery(
            'select img from Image as img ' \
            'left outer join fetch img.annotationLinks as a_links ' \
            'join img.wellSamples as ws ' \
            'join ws.well as w ' \
            'join w.plate as p ' \
            'join fetch a_links.child as a ' \
            'join fetch a.file as o_file ' \
            'where p.id = %d' % plate_id, None)
    
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

class MeasurementCtx(object):
    
    IMAGE_COL = 0
    
    ROI_COL = 1
    
    def __init__(self, analysis_ctx, service_factory, original_file,
                 result_files):
        self.analysis_ctx = analysis_ctx
        self.service_factory = service_factory
        self.update_service = self.service_factory.getUpdateService()
        self.original_file = original_file
        self.result_files = result_files
        
        # Create a file annotation to represent our measurement
        self.file_annotation = FileAnnotationI()
        self.file_annotation.ns = \
            rstring('openmicroscopy.org/omero/measurement')
        description = self.original_file.name.val[:-4]
        self.file_annotation.description = rstring(description)
        
        # Create a new OMERO table to store our measurement results
        sr = self.service_factory.sharedResources()
        self.table = sr.newTable(1, '/%s.r5' % description)
        if self.table is None:
            raise MeasurementException(
                "Unable to create table: %s" % description)
                
        # Retrieve the original file corresponding to the table for the
        # measurement, link it to the file annotation representing the
        # umbrella measurement run, link the annotation to the plate from
        # which it belongs and save the file annotation.
        table_original_file_id = self.table.getOriginalFile().id.val
        unloaded_o_file = OriginalFileI(table_original_file_id, False)
        self.file_annotation.file = unloaded_o_file
        unloaded_plate = PlateI(self.analysis_ctx.plate_id, False)
        plate_annotation_link = PlateAnnotationLinkI()
        plate_annotation_link.parent = unloaded_plate
        plate_annotation_link.child = self.file_annotation
        self.update_service.saveObject(plate_annotation_link)
        
        # Establish the rest of our initial state
        self.table_initialized = False
        self.n_columns = None
    
    def get_empty_columns(self, n_columns):
        if not self.table_initialized:
            columns = [ImageColumn('Image', '', list()),
                       RoiColumn('ROI', '', list())]
            for i in range(n_columns):
                columns.append(DoubleColumn('', '', list()))
            return columns
        else:
            return self.table.getHeaders()
    
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
        map = self.analysis_ctx.original_file_image_map
        return map[original_file.id.val]

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
        return roi

    def parse_result_files(self):
        raw_file_store = self.service_factory.createRawFileStore()
        try:
            for result_file in result_files:
                print "Parsing: %s" % result_file.name.val
                image = self.image_from_original_file(result_file)
                unloaded_image = ImageI(image.id.val, False)
                raw_file_store.setFileId(result_file.id.val)
                data = raw_file_store.read(0L, result_file.size.val)
                rows = list(csv.reader(StringIO(data), delimiter='\t'))
                rows.reverse()
                columns = self.get_empty_columns(len(rows[0]))
                rois = list()
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
                print "ROI count: %d" % len(rois)
                t0 = int(time.time() * 1000)
                rois = self.update_service.saveAndReturnArray(rois)
                print "ROI update took %sms" % (int(time.time() * 1000) - t0)
                for roi in rois:
                    columns[self.ROI_COL].values.append(roi.id.val)
                self.update_table(columns)
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
    if session_key is not None:
        service_factory = c.createSession(session_key)
    else:
        service_factory = c.createSession(username, password)
    query_service = service_factory.getQueryService()
    
    analysis_ctx = MIASPlateAnalysisCtx(query_service, plate_id)
    n_measurements = len(analysis_ctx.measurements.keys())
    if measurement is not None and measurement >= n_measurements:
        usage("Measurement %d not a valid index!")
    if info:
        print "Found %d measurements." % n_measurements
        for i, value in enumerate(analysis_ctx.measurements.values()):
            print "Measurement %d has %d result files." % (i, len(value))
        sys.exit(0)
    for key in analysis_ctx.measurements.keys():
        log_file = analysis_ctx.log_files[key]
        result_files = analysis_ctx.measurements[key]
        measurement_ctx = MeasurementCtx(analysis_ctx, service_factory,
                                         log_file, result_files)
        measurement_ctx.parse_result_files()
