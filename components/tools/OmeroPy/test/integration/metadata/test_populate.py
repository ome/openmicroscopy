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
import gzip
import os.path
import re
import shutil

from omero.api import RoiOptions
from omero.grid import ImageColumn
from omero.grid import RoiColumn
from omero.grid import StringColumn
from omero.model import OriginalFileI
from omero.model import FileAnnotationI, MapAnnotationI, PlateAnnotationLinkI
from omero.model import RoiAnnotationLinkI
from omero.model import RoiI, PointI, ProjectI, ScreenI
from omero.rtypes import rdouble, rstring, unwrap
from omero.sys import ParametersI

from omero.util.populate_metadata import (
    ParsingContext, BulkToMapAnnotationContext, DeleteMapAnnotationContext)
from omero.util.populate_roi import AbstractMeasurementCtx
from omero.util.populate_roi import AbstractPlateAnalysisCtx
from omero.util.populate_roi import MeasurementParsingResult
from omero.util.populate_roi import PlateAnalysisCtxFactory
from omero.constants.namespaces import NSBULKANNOTATIONS
from omero.constants.namespaces import NSMEASUREMENT
from omero.util.temp_files import create_path

from pytest import skip
from pytest import mark


def coord2offset(coord):
    """
    Convert a coordinate of the form AB12 into 0-based row-column indices

    TODO: This should go into a utils file somewhere
    """
    ALPHA = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    m = re.match('([A-Z]+)([0-9]+)$', coord.upper())
    assert m
    ra, ca = m.groups()
    r = 0
    for a in ra:
        r = r * 26 + ALPHA.find(a) + 1
    c = int(ca)
    return r - 1, c - 1


class Fixture(object):

    def init(self, test):
        self.test = test

    def setName(self, obj, name):
        q = self.test.client.sf.getQueryService()
        up = self.test.client.sf.getUpdateService()
        obj = q.get(obj.__class__.__name__, obj.id.val)
        obj.setName(rstring(name))
        return up.saveAndReturnObject(obj)

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

    def createProject(self, name, datasets=("D001", "D002"),
                      images=("A1", "A2")):
        prj = ProjectI()
        prj.setName(rstring(name))
        for x in datasets:
            ds = self.createDataset(names=images)
            ds = self.setName(ds, x)
            prj.linkDataset(ds.proxy())
        return self.test.client.sf.getUpdateService().saveAndReturnObject(prj)

    def createDataset(self, names=("A1", "A2")):
        ds = self.test.make_dataset()
        imgs = self.test.importMIF(
            seriesCount=len(names))
        for i, name in enumerate(names):
            # Name must match exactly. No ".fake"
            img = imgs[i]
            img = self.setName(img, name)
            self.test.link(ds, img)
        return ds.proxy()

    def createScreen(self, rowCount, colCount):
        plate1 = self.test.importPlates(plateRows=rowCount,
                                        plateCols=colCount)[0]
        plate2 = self.test.importPlates(plateRows=rowCount,
                                        plateCols=colCount)[0]
        plate1 = self.setName(plate1, "P001")
        plate2 = self.setName(plate2, "P002")
        screen = ScreenI()
        screen.name = rstring("Screen")
        screen.linkPlate(plate1.proxy())
        screen.linkPlate(plate2.proxy())
        return self.test.client.sf.getUpdateService().\
            saveAndReturnObject(screen)

    def createPlate(self, rowCount, colCount):
        plates = self.test.importPlates(plateRows=rowCount,
                                        plateCols=colCount)
        return plates[0]

    def get_csv(self):
        return self.csv

    def get_cfg(self):
        return os.path.join(
            os.path.dirname(__file__), 'bulk_to_map_annotation_context.yml')

    def assert_rows(self, rows):
        assert rows == self.rowCount * self.colCount

    def assert_child_annotations(self, oas):
        for ma, wid, wr, wc in oas:
            assert isinstance(ma, MapAnnotationI)
            assert unwrap(ma.getNs()) == NSBULKANNOTATIONS
            mv = ma.getMapValueAsMap()
            assert mv['Well'] == str(wid)
            assert coord2offset(mv['Well Name']) == (wr, wc)
            if (wr, wc) == (0, 0):
                assert mv['Well Type'] == 'Control'
                assert mv['Concentration'] == '0'
            else:
                assert mv['Well Type'] == 'Treatment'
                assert mv['Concentration'] == '10'


class Screen2Plates(Fixture):

    def __init__(self):
        self.count = 6
        self.annCount = 4
        self.rowCount = 1
        self.colCount = 2
        self.csv = self.createCsv(
            colNames="Plate,Well,Well Type,Concentration",
            rowData=("P001,A1,Control,0", "P001,A2,Treatment,10",
                     "P002,A1,Control,0", "P002,A2,Treatment,10"))
        self.screen = None

    def assert_rows(self, rows):
        """
        Double the number of rows due to 2 plates.
        """
        assert rows == 2 * self.rowCount * self.colCount

    def get_target(self):
        if not self.screen:
            self.screen = self.createScreen(self.rowCount, self.colCount)
        return self.screen

    def get_annotations(self):
        query = """select s from Screen s
            left outer join fetch s.annotationLinks links
            left outer join fetch links.child
            where s.id=%s""" % self.screen.id.val
        qs = self.test.client.sf.getQueryService()
        screen = qs.findByQuery(query, None)
        anns = screen.linkedAnnotationList()
        return anns

    def get_child_annotations(self):
        query = """
            SELECT wal.child,wal.parent.id,wal.parent.row,wal.parent.column
            FROM WellAnnotationLink wal join wal.parent well
                    join well.plate p join p.screenLinks l join l.parent s
            WHERE s.id=%d""" % self.screen.id.val
        qs = self.test.client.sf.getQueryService()
        was = unwrap(qs.projection(query, None))
        return was


class Plate2Wells(Fixture):

    def __init__(self):
        self.count = 4
        self.annCount = 2
        self.rowCount = 1
        self.colCount = 2
        self.csv = self.createCsv()
        self.plate = None

    def get_target(self):
        if not self.plate:
            self.plate = self.createPlate(self.rowCount, self.colCount)
        return self.plate

    def get_annotations(self):
        query = """select p from Plate p
            left outer join fetch p.annotationLinks links
            left outer join fetch links.child
            where p.id=%s""" % self.plate.id.val
        qs = self.test.client.sf.getQueryService()
        plate = qs.findByQuery(query, None)
        anns = plate.linkedAnnotationList()
        return anns

    def get_child_annotations(self):
        query = """
            SELECT wal.child,wal.parent.id,wal.parent.row,wal.parent.column
            FROM WellAnnotationLink wal
            WHERE wal.parent.plate.id=%d""" % self.plate.id.val
        qs = self.test.client.sf.getQueryService()
        was = unwrap(qs.projection(query, None))
        return was


class Plate2WellsGroups(Plate2Wells):
    # For this test use explicit files instead of generating them as an
    # additional safeguard against changes in the test code

    def __init__(self):
        self.count = 6
        self.annCount = 6 * 2  # Two groups
        self.rowCount = 2
        self.colCount = 3
        d = os.path.dirname(__file__)
        self.csv = os.path.join(d, 'bulk_to_map_annotation_context_groups.csv')
        self.plate = None

    def get_cfg(self):
        return os.path.join(os.path.dirname(__file__),
                            'bulk_to_map_annotation_context_groups.yml')

    def assert_row_values(self, rowvalues):
        # First column is the WellID
        assert rowvalues[0][1:] == (
            "FBgn0004644", "hh", "hedgehog;bar-3;CG4637", "a1")
        assert rowvalues[1][1:] == (
            "FBgn0003656", "sws", "swiss cheese;olfE;CG2212", "a2")
        assert rowvalues[2][1:] == (
            "FBgn0011236", "ken", "ken and barbie;CG5575", "a3")
        assert rowvalues[3][1:] == (
            "", "hh",
            "DHH;IHH;SHH;Desert hedgehog;Indian hedgehog;Sonic hedgehog", "b1")
        assert rowvalues[4][1:] == (
            "", "sws",
            "PNPLA6;patatin like phospholipase domain containing 6", "b2")
        assert rowvalues[5][1:] == (
            "", "ken", "BCL6;B-cell lymphoma 6 protein", "b3")

    def assert_child_annotations(self, oas):
        wellrcs = [coord2offset(c) for c in (
            'a1', 'a2', 'a3', 'b1', 'b2', 'b3')]
        nss = [NSBULKANNOTATIONS, 'openmicroscopy.org/mapr/gene']
        wellrc_ns = [(wrc, ns) for wrc in wellrcs for ns in nss]
        check = dict((k, None) for k in wellrc_ns)
        annids = []

        for ma, wid, wr, wc in oas:
            assert isinstance(ma, MapAnnotationI)
            annids.append(unwrap(ma.getId()))
            ns = unwrap(ma.getNs())
            wrc = (wr, wc)

            # Well names/ids aren't included in this test, because this also
            # test that annotations are combined by primary key
            assert (wrc, ns) in check, 'Unexpected well/namespace'
            assert check[(wrc, ns)] is None, 'Duplicate annotation'

            # Use getMapValue to check ordering and duplicates
            check[(wrc, ns)] = [(p.name, p.value) for p in ma.getMapValue()]

        # Row a

        assert check[(wellrcs[0], nss[0])] == [
            ('Gene', 'hh'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0004644.html'),
        ]
        assert check[(wellrcs[0], nss[1])] == [
            ('Gene', 'hh'),
            ('Gene name', 'hedgehog'),
            ('Gene name', 'bar-3'),
            ('Gene name', 'CG4637'),
        ]

        assert check[(wellrcs[1], nss[0])] == [
            ('Gene', 'sws'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0003656.html'),
        ]
        assert check[(wellrcs[1], nss[1])] == [
            ('Gene', 'sws'),
            ('Gene name', 'swiss cheese'),
            ('Gene name', 'olfE'),
            ('Gene name', 'CG2212'),
        ]

        assert check[(wellrcs[2], nss[0])] == [
            ('Gene', 'ken'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0011236.html'),
        ]
        assert check[(wellrcs[2], nss[1])] == [
            ('Gene', 'ken'),
            ('Gene name', 'ken and barbie'),
            ('Gene name', 'CG5575'),
        ]

        # Row b

        assert check[(wellrcs[3], nss[0])] == [
            ('Gene', 'hh'),
        ]
        assert check[(wellrcs[3], nss[1])] == [
            ('Gene', 'hh'),
            ('Gene name', 'DHH'),
            ('Gene name', 'IHH'),
            ('Gene name', 'SHH'),
            ('Gene name', 'Desert hedgehog'),
            ('Gene name', 'Indian hedgehog'),
            ('Gene name', 'Sonic hedgehog'),
        ]
        assert check[(wellrcs[4], nss[0])] == [
            ('Gene', 'sws'),
        ]
        assert check[(wellrcs[4], nss[1])] == [
            ('Gene', 'sws'),
            ('Gene name', 'PNPLA6'),
            ('Gene name', 'patatin like phospholipase domain containing 6'),
        ]

        assert check[(wellrcs[5], nss[0])] == [
            ('Gene', 'ken'),
        ]
        assert check[(wellrcs[5], nss[1])] == [
            ('Gene', 'ken'),
            ('Gene name', 'BCL6'),
            ('Gene name', 'B-cell lymphoma 6 protein'),
        ]

        assert len(annids) == 12
        assert len(set(annids)) == 12


class Plate2WellsGroups2(Plate2WellsGroups):
    # For this test use explicit files instead of generating them as an
    # additional safeguard against changes in the test code

    def __init__(self):
        super(Plate2WellsGroups2, self).__init__()

    def get_cfg(self):
        return os.path.join(os.path.dirname(__file__),
                            'bulk_to_map_annotation_context_groups2.yml')

    def assert_child_annotations(self, oas):
        wellrcs = [coord2offset(c) for c in (
            'a1', 'a2', 'a3', 'b1', 'b2', 'b3')]
        nss = [NSBULKANNOTATIONS, 'openmicroscopy.org/mapr/gene']
        wellrc_ns = [(wrc, ns) for wrc in wellrcs for ns in nss]
        check = dict((k, None) for k in wellrc_ns)
        annids = []

        for ma, wid, wr, wc in oas:
            assert isinstance(ma, MapAnnotationI)
            annids.append(unwrap(ma.getId()))
            ns = unwrap(ma.getNs())
            wrc = (wr, wc)

            # Well names/ids aren't included in this test, because this also
            # test that annotations are combined by primary key
            assert (wrc, ns) in check, 'Unexpected well/namespace'
            assert check[(wrc, ns)] is None, 'Duplicate annotation'

            # Use getMapValue to check ordering and duplicates
            check[(wrc, ns)] = [(p.name, p.value) for p in ma.getMapValue()]

        # Row a

        assert check[(wellrcs[0], nss[0])] == [
            ('Gene', 'hh'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0004644.html'),
        ]
        assert check[(wellrcs[0], nss[1])] == [
            ('Gene', 'hh'),
            ('Gene name', 'hedgehog'),
            ('Gene name', 'bar-3'),
            ('Gene name', 'CG4637'),
            ('Gene name', 'DHH'),
            ('Gene name', 'IHH'),
            ('Gene name', 'SHH'),
            ('Gene name', 'Desert hedgehog'),
            ('Gene name', 'Indian hedgehog'),
            ('Gene name', 'Sonic hedgehog'),
        ]
        assert check[(wellrcs[1], nss[0])] == [
            ('Gene', 'sws'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0003656.html'),
        ]
        assert check[(wellrcs[1], nss[1])] == [
            ('Gene', 'sws'),
            ('Gene name', 'swiss cheese'),
            ('Gene name', 'olfE'),
            ('Gene name', 'CG2212'),
            ('Gene name', 'PNPLA6'),
            ('Gene name', 'patatin like phospholipase domain containing 6'),
        ]

        assert check[(wellrcs[2], nss[0])] == [
            ('Gene', 'ken'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0011236.html'),
        ]
        assert check[(wellrcs[2], nss[1])] == [
            ('Gene', 'ken'),
            ('Gene name', 'ken and barbie'),
            ('Gene name', 'CG5575'),
            ('Gene name', 'BCL6'),
            ('Gene name', 'B-cell lymphoma 6 protein'),
        ]

        # Row b

        assert check[(wellrcs[3], nss[0])] == [
            ('Gene', 'hh'),
        ]
        assert check[(wellrcs[3], nss[1])] == check[(wellrcs[0], nss[1])]

        assert check[(wellrcs[4], nss[0])] == [
            ('Gene', 'sws'),
        ]
        assert check[(wellrcs[4], nss[1])] == check[(wellrcs[1], nss[1])]

        assert check[(wellrcs[5], nss[0])] == [
            ('Gene', 'ken'),
        ]
        assert check[(wellrcs[5], nss[1])] == check[(wellrcs[2], nss[1])]

        assert len(annids) == 12
        assert len(set(annids)) == 9


class Dataset2Images(Fixture):

    def __init__(self):
        self.count = 4
        self.annCount = 2
        self.csv = self.createCsv(
            colNames="Image Name,Type,Concentration",
        )
        self.dataset = None
        self.images = None
        self.names = ("A1", "A2")

    def assert_rows(self, rows):
        # Hard-coded in createCsv's arguments
        assert rows == 2

    def get_target(self):
        if not self.dataset:
            self.dataset = self.createDataset(self.names)
            self.images = self.get_dataset_images()
        return self.dataset

    def get_dataset_images(self):
        if not self.dataset:
            return []
        query = """select i from Image i
            left outer join fetch i.datasetLinks links
            left outer join fetch links.parent d
            where d.id=%s""" % self.dataset.id.val
        qs = self.test.client.sf.getQueryService()
        return qs.findAllByQuery(query, None)

    def get_annotations(self):
        query = """select d from Dataset d
            left outer join fetch d.annotationLinks links
            left outer join fetch links.child
            where d.id=%s""" % self.dataset.id.val
        qs = self.test.client.sf.getQueryService()
        ds = qs.findByQuery(query, None)
        anns = ds.linkedAnnotationList()
        return anns

    def get_child_annotations(self):
        if not self.images:
            return []
        params = ParametersI()
        params.addIds([x.id for x in self.images])
        query = """ select a, i.id, 'NA', 'NA'
            from Image i
            left outer join i.annotationLinks links
            left outer join links.child as a
            where i.id in (:ids) and a <> null"""
        qs = self.test.client.sf.getQueryService()
        return unwrap(qs.projection(query, params))

    def assert_child_annotations(self, oas):
        for ma, iid, na1, na2 in oas:
            assert isinstance(ma, MapAnnotationI)
            assert unwrap(ma.getNs()) == NSBULKANNOTATIONS
            mv = ma.getMapValueAsMap()
            img = mv['Image Name']
            con = mv['Concentration']
            typ = mv['Type']
            assert img[0] in ("A", "a")
            which = long(img[1:])
            if which % 2 == 1:
                assert con == '0'
                assert typ == 'Control'
            elif which % 2 == 0:
                assert con == '10'
                assert typ == 'Treatment'


class Dataset101Images(Dataset2Images):

    def __init__(self):
        self.count = 4
        self.annCount = 102
        self.names = []
        rowData = []
        for x in range(0, 101, 2):
            name = "A%s" % (x+1)
            self.names.append(name)
            rowData.append("%s,Control,0" % name)
            name = "A%s" % (x+2)
            self.names.append(name)
            rowData.append("A%s,Treatment,10" % (x+2))
        self.csv = self.createCsv(
            colNames="Image Name,Type,Concentration",
            rowData=rowData,
        )
        self.dataset = None
        self.images = None

    def assert_rows(self, rows):
        assert rows == 102


class GZIP(Dataset2Images):

    def createCsv(self, *args, **kwargs):
        csvFileName = super(GZIP, self).createCsv(*args, **kwargs)
        gzipFileName = "%s.gz" % csvFileName
        with open(csvFileName, 'rb') as f_in, \
                gzip.open(gzipFileName, 'wb') as f_out:
            shutil.copyfileobj(f_in, f_out)

        return gzipFileName


class Project2Datasets(Fixture):

    def __init__(self):
        self.count = 5
        self.annCount = 4
        self.csv = self.createCsv(
            colNames="Dataset Name,Image Name,Type,Concentration",
            rowData=("D001,A1,Control,0", "D001,A2,Treatment,10",
                     "D002,A1,Control,0", "D002,A2,Treatment,10"))
        self.project = None

    def assert_rows(self, rows):
        # Hard-coded in createCsv's arguments
        assert rows == 4

    def get_target(self):
        if not self.project:
            self.project = self.createProject("P123")
            self.images = self.get_project_images()
        return self.project

    def get_project_images(self):
        if not self.project:
            return []
        query = """select i from Image i
            left outer join fetch i.datasetLinks dil
            left outer join fetch dil.parent d
            left outer join fetch d.projectLinks pdl
            left outer join fetch pdl.parent p
            where p.id=%s""" % self.project.id.val
        qs = self.test.client.sf.getQueryService()
        return qs.findAllByQuery(query, None)

    def get_annotations(self):
        query = """select p from Project p
            left outer join fetch p.annotationLinks links
            left outer join fetch links.child
            where p.id=%s""" % self.project.id.val
        qs = self.test.client.sf.getQueryService()
        ds = qs.findByQuery(query, None)
        anns = ds.linkedAnnotationList()
        return anns

    def get_child_annotations(self):
        if not self.images:
            return []
        params = ParametersI()
        params.addIds([x.id for x in self.images])
        query = """ select a, i.id, 'NA', 'NA'
            from Image i
            left outer join i.annotationLinks links
            left outer join links.child as a
            where i.id in (:ids) and a <> null"""
        qs = self.test.client.sf.getQueryService()
        return unwrap(qs.projection(query, params))

    def assert_child_annotations(self, oas):
        for ma, iid, na1, na2 in oas:
            assert isinstance(ma, MapAnnotationI)
            assert unwrap(ma.getNs()) == NSBULKANNOTATIONS
            mv = ma.getMapValueAsMap()
            ds = mv['Dataset Name']
            img = mv['Image Name']
            con = mv['Concentration']
            typ = mv['Type']
            if ds == 'D001' or ds == 'D002':
                if img == "A1":
                    assert con == '0'
                    assert typ == 'Control'
                elif img == "A2":
                    assert con == '10'
                    assert typ == 'Treatment'
                else:
                    raise Exception("Unknown img: %s" % img)
            else:
                raise Exception("Unknown dataset: %s" % ds)


class TestPopulateMetadata(lib.ITest):

    METADATA_FIXTURES = (
        Screen2Plates(),
        Plate2Wells(),
        Dataset2Images(),
        Dataset101Images(),
        Project2Datasets(),
        GZIP(),
    )
    METADATA_IDS = [x.__class__.__name__ for x in METADATA_FIXTURES]

    METADATA_NS_FIXTURES = (
        Plate2WellsGroups(),
        Plate2WellsGroups2(),
    )
    METADATA_NS_IDS = [x.__class__.__name__ for x in METADATA_NS_FIXTURES]

    @mark.parametrize("fixture", METADATA_FIXTURES, ids=METADATA_IDS)
    @mark.parametrize("batch_size", (None, 10, 1000))
    def testPopulateMetadata(self, fixture, batch_size):
        """
        We should really test each of the parsing contexts in separate tests
        but in practice each one uses data created by the others, so for
        now just run them all together
        """
        try:
            import yaml
            print yaml, "found"
        except Exception:
            skip("PyYAML not installed.")

        fixture.init(self)
        t = self._test_parsing_context(fixture, batch_size)
        self._assert_parsing_context_values(t, fixture)
        self._test_bulk_to_map_annotation_context(fixture, batch_size)
        self._test_delete_map_annotation_context(fixture, batch_size)

    @mark.parametrize("fixture", METADATA_NS_FIXTURES, ids=METADATA_NS_IDS)
    def testPopulateMetadataGroupAnns(self, fixture):
        """
        Test complicated annotations (multiple groups) on a single OMERO
        data type, as opposed to testPopulateMetadata which tests simple
        annotations on multiple OMERO data types
        """
        try:
            import yaml
            print yaml, "found"
        except Exception:
            skip("PyYAML not installed.")

        fixture.init(self)
        t = self._test_parsing_context(fixture, 2)

        cols = t.getHeaders()
        rows = t.getNumberOfRows()
        fixture.assert_rows(rows)
        data = [c.values for c in t.read(range(len(cols)), 0, rows).columns]
        rowValues = zip(*data)
        assert len(rowValues) == fixture.count
        fixture.assert_row_values(rowValues)

        self._test_bulk_to_map_annotation_context(fixture, 2)
        self._test_delete_map_annotation_context(fixture, 2)

    def _test_parsing_context(self, fixture, batch_size):
        """
            Create a small csv file, use populate_metadata.py to parse and
            attach to Plate. Then query to check table has expected content.
        """

        target = fixture.get_target()
        # Deleting anns so that we can re-use the same user
        self.delete(fixture.get_annotations())
        child_anns = fixture.get_child_annotations()
        child_anns = [x[0] for x in child_anns]
        self.delete(child_anns)

        csv = fixture.get_csv()
        ctx = ParsingContext(self.client, target, file=csv)
        ctx.parse()
        if batch_size is None:
            ctx.write_to_omero()
        else:
            ctx.write_to_omero(batch_size=batch_size)

        # Get file annotations
        anns = fixture.get_annotations()
        # Only expect a single annotation which is a 'bulk annotation'
        assert len(anns) == 1
        tableFileAnn = anns[0]
        assert unwrap(tableFileAnn.getNs()) == NSBULKANNOTATIONS
        fileid = tableFileAnn.file.id.val

        # Open table to check contents
        r = self.client.sf.sharedResources()
        t = r.openTable(OriginalFileI(fileid), None)
        return t

    def _assert_parsing_context_values(self, t, fixture):
        cols = t.getHeaders()
        rows = t.getNumberOfRows()
        fixture.assert_rows(rows)
        for hit in range(rows):
            rowValues = [col.values[0] for col in t.read(range(len(cols)),
                                                         hit, hit+1).columns]
            assert len(rowValues) == fixture.count
            # Unsure where the lower-casing is happening
            if "A1" in rowValues or "a1" in rowValues:
                assert "Control" in rowValues
            elif "A2" in rowValues or "a2" in rowValues:
                assert "Treatment" in rowValues

    def _test_bulk_to_map_annotation_context(self, fixture, batch_size):
        # self._testPopulateMetadataPlate()
        assert len(fixture.get_child_annotations()) == 0

        cfg = fixture.get_cfg()

        target = fixture.get_target()
        anns = fixture.get_annotations()
        fileid = anns[0].file.id.val
        ctx = BulkToMapAnnotationContext(
            self.client, target, fileid=fileid, cfg=cfg)
        ctx.parse()
        assert len(fixture.get_child_annotations()) == 0

        if batch_size is None:
            ctx.write_to_omero()
        else:
            ctx.write_to_omero(batch_size=batch_size)
        oas = fixture.get_child_annotations()
        assert len(oas) == fixture.annCount
        fixture.assert_child_annotations(oas)

    def _test_delete_map_annotation_context(self, fixture, batch_size):
        # self._test_bulk_to_map_annotation_context()
        assert len(fixture.get_child_annotations()) == fixture.annCount

        cfg = fixture.get_cfg()

        target = fixture.get_target()
        ctx = DeleteMapAnnotationContext(self.client, target, cfg=cfg)
        ctx.parse()
        assert len(fixture.get_child_annotations()) == fixture.annCount

        if batch_size is None:
            ctx.write_to_omero()
        else:
            ctx.write_to_omero(batch_size=batch_size)
        assert len(fixture.get_child_annotations()) == 0


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


class ROICSV(Fixture):

    def __init__(self):
        self.count = 1
        self.annCount = 2
        self.csvName = self.createCsv(
            colNames="Well,Field,X,Y,Type",
            rowData=("A1,0,15,15,Test",))

        self.rowCount = 1
        self.colCount = 1
        self.plate = None

    def get_target(self):
        if not self.plate:
            self.plate = self.createPlate(
                self.rowCount, self.colCount)
        return self.plate


class TestPopulateRois(lib.ITest):

    def testPopulateRoisPlate(self):
        """
            Create a small csv file, use populate_roi.py to parse and
            attach to Plate. Then query to check table has expected content.
        """

        fixture = ROICSV()
        fixture.init(self)
        plate = fixture.get_target()

        # As opposed to the ParsingContext, here we are expected
        # to link the file ourselves
        ofile = self.client.upload(fixture.csvName).proxy()
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
