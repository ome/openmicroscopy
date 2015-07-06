#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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
   Test of the Tables service with the populate_metadata.py
   and populate_roi.py scripts.
"""

import library as lib
import string
import csv
import re

from omero.api import RoiOptions
from omero.grid import ImageColumn
from omero.grid import RoiColumn
from omero.grid import StringColumn
from omero.model import PlateI, WellI, WellSampleI, OriginalFileI
from omero.model import FileAnnotationI, PlateAnnotationLinkI
from omero.model import RoiAnnotationLinkI
from omero.model import RoiI, PointI
from omero.rtypes import rdouble, rint, rstring, unwrap
from omero.sys import ParametersI
from omero.util.populate_metadata import ParsingContext
from omero.util.populate_roi import AbstractMeasurementCtx
from omero.util.populate_roi import AbstractPlateAnalysisCtx
from omero.util.populate_roi import MeasurementParsingResult
from omero.util.populate_roi import PlateAnalysisCtxFactory
from omero.constants.namespaces import NSBULKANNOTATIONS
from omero.constants.namespaces import NSMEASUREMENT
from omero.util.temp_files import create_path


class BasePopulate(lib.ITest):

    def createCsv(
        self,
        colNames="Well,Well Type,Concentration",
        rowData=("A1,Control,0", "A2,Treatment,10")
    ):

        csvFileName = create_path("test", ".csv")
        csvFile = open(csvFileName, 'w')
        try:
            csvFile.write(colNames)
            csvFile.write("\n")
            csvFile.write("\n".join(rowData))
        finally:
            csvFile.close()
        return str(csvFileName)

    def createPlate(self, rowCount, colCount):
        plates = self.importPlates(plateRows=rowCount,
                                   plateCols=colCount)
        return plates[0]

    def createPlate1(self, rowCount, colCount):
        uuid = self.ctx.sessionUuid

        def createWell(row, column):
            well = WellI()
            well.row = rint(row)
            well.column = rint(column)
            ws = WellSampleI()
            image = self.new_image(name=uuid)
            ws.image = image
            well.addWellSample(ws)
            return well

        plate = PlateI()
        plate.name = rstring("TestPopulateMetadata%s" % uuid)
        for row in range(rowCount):
            for col in range(colCount):
                well = createWell(row, col)
                plate.addWell(well)
        return self.client.sf.getUpdateService().saveAndReturnObject(plate)


class TestPopulateMetadata(BasePopulate):

    def testPopulateMetadataPlate(self):
        """
            Create a small csv file, use populate_metadata.py to parse and
            attach to Plate. Then query to check table has expected content.
        """

        csvName = self.createCsv()
        rowCount = 1
        colCount = 2
        plate = self.createPlate(rowCount, colCount)
        ctx = ParsingContext(self.client, plate, csvName)
        ctx.parse()
        ctx.write_to_omero()

        # Get file annotations
        query = """select p from Plate p
            left outer join fetch p.annotationLinks links
            left outer join fetch links.child
            where p.id=%s""" % plate.id.val
        qs = self.client.sf.getQueryService()
        plate = qs.findByQuery(query, None)
        anns = plate.linkedAnnotationList()
        # Only expect a single annotation which is a 'bulk annotation'
        assert len(anns) == 1
        tableFileAnn = anns[0]
        assert unwrap(tableFileAnn.getNs()) == NSBULKANNOTATIONS
        fileid = tableFileAnn.file.id.val

        # Open table to check contents
        r = self.client.sf.sharedResources()
        t = r.openTable(OriginalFileI(fileid), None)
        cols = t.getHeaders()
        rows = t.getNumberOfRows()
        assert rows == rowCount * colCount
        for hit in range(rows):
            rowValues = [col.values[0] for col in t.read(range(len(cols)),
                                                         hit, hit+1).columns]
            assert len(rowValues) == 4
            if "a1" in rowValues:
                assert "Control" in rowValues
            elif "a2" in rowValues:
                assert "Treatment" in rowValues
            else:
                assert False, "Row does not contain 'a1' or 'a2'"


class MockMeasurementCtx(AbstractMeasurementCtx):

    def well_name_to_number(self, well):
        m = re.match("(?P<COL>[a-z]+)(?P<ROW>\d+)",
                     well, re.IGNORECASE)
        if not m:
            raise Exception("Bad well: %s" % well)

        col = m.group("COL").upper()
        row = m.group("ROW")

        row_num = int(row) - 1
        col_num = 0
        for c in col:
            i = string.ascii_uppercase.find(c)
            col_num += i + 1

        # wellnumber_from_colrow
        numcols = self.analysis_ctx.numcols
        return (col_num * numcols) + row_num

    def parse(self):
        provider = self.original_file_provider
        data = provider.get_original_file_data(self.original_file)
        try:
            rows = list(csv.reader(data, delimiter=","))
        finally:
            data.close()

        columns = [
            ImageColumn("Image", "", list()),
            RoiColumn("ROI", "", list()),
            StringColumn("Type", "", 12, list()),
        ]

        for row in rows[1:]:
            wellnumber = self.well_name_to_number(row[0])
            image = self.analysis_ctx.\
                image_from_wellnumber(wellnumber)
            #  TODO: what to do with the field?!
            #  field = int(row[1])
            #  image = images[field]
            roi = RoiI()
            shape = PointI()
            shape.cx = rdouble(float(row[2]))
            shape.cy = rdouble(float(row[3]))
            shape.textValue = rstring(row[4])
            roi.addShape(shape)
            roi.image = image.proxy()
            rid = self.update_service\
                .saveAndReturnIds([roi])[0]

            columns[0].values.append(image.id.val)
            columns[1].values.append(rid)
            columns[2].values.append(row[4])

        return MeasurementParsingResult([columns])

    def get_name(self, *args, **kwargs):
        # Strip .csv
        return self.original_file.name.val[:-4]

    def parse_and_populate_roi(self, columns):
        # Remove from interface
        # Using this as a place to set file annotation
        self.file_annotation =\
            self.update_service.saveAndReturnObject(
                self.file_annotation)
        rois = columns[1].values
        for roi in rois:
            link = RoiAnnotationLinkI()
            link.parent = RoiI(roi, False)
            link.child = self.file_annotation.proxy()
            self.update_service\
                .saveObject(link)

    def populate(self, columns):
        self.update_table(columns)


class MockPlateAnalysisCtx(AbstractPlateAnalysisCtx):

    def __init__(self, images, original_files,
                 original_file_image_map,
                 plate_id, service_factory):

        super(MockPlateAnalysisCtx, self).__init__(
            images, original_files, original_file_image_map,
            plate_id, service_factory
        )
        for original_file in original_files:
            name = original_file.name.val
            if name.endswith("csv"):
                self.measurements[len(self.measurements)] = \
                    original_file

    def is_this_type(klass, original_files):
        for original_file in original_files:
            name = unwrap(original_file.name)
            if name.endswith(".csv"):
                return True
    is_this_type = classmethod(is_this_type)

    def get_measurement_count(self):
        return len(self.measurements)

    def get_measurement_ctx(self, index):
        sf = self.service_factory
        provider = self.DEFAULT_ORIGINAL_FILE_PROVIDER(sf)
        return MockMeasurementCtx(
            self, sf, provider,
            self.measurements[index], None)

    def get_result_file_count(self, index):
        return 1


class TestPopulateRois(BasePopulate):

    def testPopulateRoisPlate(self):
        """
            Create a small csv file, use populate_roi.py to parse and
            attach to Plate. Then query to check table has expected content.
        """

        csvName = self.createCsv(
            colNames="Well,Field,X,Y,Type",
            rowData=("A1,0,15,15,Test",))

        rowCount = 1
        colCount = 1
        plate = self.createPlate(rowCount, colCount)

        # As opposed to the ParsingContext, here we are expected
        # to link the file ourselves
        ofile = self.client.upload(csvName).proxy()
        ann = FileAnnotationI()
        ann.file = ofile
        link = PlateAnnotationLinkI()
        link.parent = plate.proxy()
        link.child = ann
        link = self.client.sf.getUpdateService()\
            .saveAndReturnObject(link)
        # End linking

        factory = PlateAnalysisCtxFactory(self.client.sf)
        factory.implementations = (MockPlateAnalysisCtx,)
        ctx = factory.get_analysis_ctx(plate.id.val)
        assert 1 == ctx.get_measurement_count()
        meas = ctx.get_measurement_ctx(0)
        meas.parse_and_populate()

        # Get file annotations
        query = """select p from Plate p
            left outer join fetch p.annotationLinks links
            left outer join fetch links.child as ann
            left outer join fetch ann.file as file
            where p.id=%s""" % plate.id.val
        qs = self.client.sf.getQueryService()
        plate = qs.findByQuery(query, None)
        anns = plate.linkedAnnotationList()
        # Only expect a single annotation which is a 'bulk annotation'
        # the other is the original CSV
        assert len(anns) == 2
        files = dict(
            [(a.ns.val, a.file.id.val) for a in anns if a.ns])
        fileid = files[NSMEASUREMENT]

        # Open table to check contents
        r = self.client.sf.sharedResources()
        t = r.openTable(OriginalFileI(fileid), None)
        cols = t.getHeaders()
        rows = t.getNumberOfRows()
        assert rows == 1

        data = t.read(range(len(cols)), 0, 1)
        imag = data.columns[0].values[0]
        rois = self.client.sf.getRoiService()
        anns = rois.getRoiMeasurements(imag, RoiOptions())
        assert anns
