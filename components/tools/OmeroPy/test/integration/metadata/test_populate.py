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

from omero.testlib import ITest
import string
import csv
import gzip
import os.path
import re
import shutil
import sys

from omero.api import RoiOptions
from omero.grid import ImageColumn
from omero.grid import RoiColumn
from omero.grid import StringColumn
from omero.model import OriginalFileI
from omero.model import FileAnnotationI, MapAnnotationI, PlateAnnotationLinkI
from omero.model import RoiAnnotationLinkI
from omero.model import RoiI, PointI, ProjectI, ScreenI
from omero.rtypes import rdouble, rlist, rstring, unwrap
from omero.sys import ParametersI

from omero.util.populate_metadata import (
    get_config,
    ParsingContext,
    BulkToMapAnnotationContext,
    DeleteMapAnnotationContext,
)
from omero.util.populate_roi import AbstractMeasurementCtx
from omero.util.populate_roi import AbstractPlateAnalysisCtx
from omero.util.populate_roi import MeasurementParsingResult
from omero.util.populate_roi import PlateAnalysisCtxFactory
from omero.constants.namespaces import NSBULKANNOTATIONS
from omero.constants.namespaces import NSMEASUREMENT
from omero.util.temp_files import create_path

from omero.util.metadata_mapannotations import MapAnnotationPrimaryKeyException
from omero.util.metadata_utils import NSBULKANNOTATIONSCONFIG

from pytest import mark
from pytest import raises

MAPR_NS_GENE = 'openmicroscopy.org/mapr/gene'

pythonminver = mark.skipif(sys.version_info < (2, 7),
                           reason="requires python2.7")


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
        imgs = self.test.import_fake_file(
            images_count=len(names))
        for i, name in enumerate(names):
            # Name must match exactly. No ".fake"
            img = imgs[i]
            img = self.setName(img, name)
            self.test.link(ds, img)
        return ds.proxy()

    def createScreen(self, rowCount, colCount):
        plate1 = self.test.import_plates(plate_rows=rowCount,
                                         plate_cols=colCount)[0]
        plate2 = self.test.import_plates(plate_rows=rowCount,
                                         plate_cols=colCount)[0]
        plate1 = self.setName(plate1, "P001")
        plate2 = self.setName(plate2, "P002")
        screen = ScreenI()
        screen.name = rstring("Screen")
        screen.linkPlate(plate1.proxy())
        screen.linkPlate(plate2.proxy())
        return self.test.client.sf.getUpdateService().\
            saveAndReturnObject(screen)

    def createPlate(self, rowCount, colCount):
        plates = self.test.import_plates(plate_rows=rowCount,
                                         plate_cols=colCount)
        return plates[0]

    def get_csv(self):
        return self.csv

    def get_cfg(self):
        return os.path.join(
            os.path.dirname(__file__), 'bulk_to_map_annotation_context.yml')

    def get_namespaces(self):
        return [NSBULKANNOTATIONS]

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

    def get_all_map_annotations(self):
        qs = self.test.client.sf.getQueryService()
        q = "FROM MapAnnotation WHERE ns in (:nss)"
        p = ParametersI()
        p.map['nss'] = rlist(rstring(ns) for ns in self.get_namespaces())
        r = qs.findAllByQuery(q, p)
        return r


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

    def get_child_annotations(self, ns=None):
        query = """
            SELECT wal.child,wal.parent.id,wal.parent.row,wal.parent.column
            FROM WellAnnotationLink wal
            WHERE wal.parent.plate.id=%d""" % self.plate.id.val
        if ns is not None:
            query += (" AND wal.child.ns='%s'" % ns)
        qs = self.test.client.sf.getQueryService()
        was = unwrap(qs.projection(query, None))
        return was


class Plate2WellsNs(Plate2Wells):
    # For this test use explicit files instead of generating them as an
    # additional safeguard against changes in the test code

    def __init__(self):
        self.count = 8
        self.annCount = 8 * 2  # Two namespaces
        self.rowCount = 2
        self.colCount = 4
        d = os.path.dirname(__file__)
        self.csv = os.path.join(d, 'bulk_to_map_annotation_context_ns.csv')
        self.plate = None

    def get_cfg(self):
        return os.path.join(os.path.dirname(__file__),
                            'bulk_to_map_annotation_context_ns.yml')

    def get_namespaces(self):
        return [NSBULKANNOTATIONS, MAPR_NS_GENE]

    def assert_row_values(self, rowvalues):
        # First column is the WellID
        assert rowvalues[0][1:] == (
            "FBgn0004644", "hh", "hedgehog;bar-3;CG4637", "a1")
        assert rowvalues[1][1:] == (
            "FBgn0003656", "sws", "swiss cheese;olfE;CG2212", "a2")
        assert rowvalues[2][1:] == (
            "FBgn0011236", "ken", "ken and barbie;CG5575", "a3")
        assert rowvalues[3][1:] == (
            "FBgn0086378", "", "Alg-2", "a4")
        assert rowvalues[4][1:] == (
            "", "hh",
            "DHH;IHH;SHH;Desert hedgehog;Indian hedgehog;Sonic hedgehog", "b1")
        assert rowvalues[5][1:] == (
            "", "sws",
            "PNPLA6;patatin like phospholipase domain containing 6", "b2")
        assert rowvalues[6][1:] == (
            "", "ken", "BCL6;B-cell lymphoma 6 protein", "b3")
        assert rowvalues[7][1:] == (
            "", "", "Alg-2", "b4")

    def assert_child_annotations(self, oas):
        wellrcs = [coord2offset(c) for c in (
            'a1', 'a2', 'a3', 'a4', 'b1', 'b2', 'b3', 'b4')]
        nss = [NSBULKANNOTATIONS, MAPR_NS_GENE]
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

        assert check[(wellrcs[3], nss[0])] == [
            ('Gene', ''),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0086378.html'),
        ]
        assert check[(wellrcs[3], nss[1])] == [
            ('Gene', ''),
            ('Gene name', 'Alg-2'),
        ]

        # Row b

        assert check[(wellrcs[4], nss[0])] == [
            ('Gene', 'hh'),
        ]
        assert check[(wellrcs[4], nss[1])] == [
            ('Gene', 'hh'),
            ('Gene name', 'DHH'),
            ('Gene name', 'IHH'),
            ('Gene name', 'SHH'),
            ('Gene name', 'Desert hedgehog'),
            ('Gene name', 'Indian hedgehog'),
            ('Gene name', 'Sonic hedgehog'),
        ]
        assert check[(wellrcs[5], nss[0])] == [
            ('Gene', 'sws'),
        ]
        assert check[(wellrcs[5], nss[1])] == [
            ('Gene', 'sws'),
            ('Gene name', 'PNPLA6'),
            ('Gene name', 'patatin like phospholipase domain containing 6'),
        ]

        assert check[(wellrcs[6], nss[0])] == [
            ('Gene', 'ken'),
        ]
        assert check[(wellrcs[6], nss[1])] == [
            ('Gene', 'ken'),
            ('Gene name', 'BCL6'),
            ('Gene name', 'B-cell lymphoma 6 protein'),
        ]

        assert check[(wellrcs[7], nss[0])] == [
            ('Gene', ''),
        ]
        assert check[(wellrcs[7], nss[1])] == [
            ('Gene', ''),
            ('Gene name', 'Alg-2'),
        ]

        assert len(annids) == 16
        assert len(set(annids)) == 16


class Plate2WellsNs2(Plate2WellsNs):
    # For this test use explicit files instead of generating them as an
    # additional safeguard against changes in the test code

    def __init__(self):
        super(Plate2WellsNs2, self).__init__()

    def get_cfg(self):
        return os.path.join(os.path.dirname(__file__),
                            'bulk_to_map_annotation_context_ns2.yml')

    def assert_child_annotations(self, oas, onlyns=None):
        """
        Pass onlyns to check that only annotations in that namespace exist
        """
        wellrcs = [coord2offset(c) for c in (
            'a1', 'a2', 'a3', 'a4', 'b1', 'b2', 'b3', 'b4')]
        nss = [NSBULKANNOTATIONS, MAPR_NS_GENE]
        if onlyns:
            nss = [onlyns]
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

        if onlyns == NSBULKANNOTATIONS:
            self._assert_nsbulkann(check, wellrcs)
            assert len(annids) == 8
            assert len(set(annids)) == 8
        if onlyns == MAPR_NS_GENE:
            self._assert_nsgeneann(check, wellrcs)
            assert len(annids) == 8
            assert len(set(annids)) == 4
        if not onlyns:
            self._assert_nsgeneann(check, wellrcs)
            assert len(annids) == 16
            assert len(set(annids)) == 12

    def _assert_nsbulkann(self, check, wellrcs):
        # Row a
        assert check[(wellrcs[0], NSBULKANNOTATIONS)] == [
            ('Gene', 'hh'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0004644.html'),
        ]
        assert check[(wellrcs[1], NSBULKANNOTATIONS)] == [
            ('Gene', 'sws'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0003656.html'),
        ]
        assert check[(wellrcs[2], NSBULKANNOTATIONS)] == [
            ('Gene', 'ken'),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0011236.html'),
        ]
        assert check[(wellrcs[3], NSBULKANNOTATIONS)] == [
            ('Gene', ''),
            ('FlyBase URL', 'http://flybase.org/reports/FBgn0086378.html'),
        ]

        # Row b
        assert check[(wellrcs[4], NSBULKANNOTATIONS)] == [
            ('Gene', 'hh'),
        ]
        assert check[(wellrcs[5], NSBULKANNOTATIONS)] == [
            ('Gene', 'sws'),
        ]
        assert check[(wellrcs[6], NSBULKANNOTATIONS)] == [
            ('Gene', 'ken'),
        ]
        assert check[(wellrcs[7], NSBULKANNOTATIONS)] == [
            ('Gene', ''),
        ]

    def _assert_nsgeneann(self, check, wellrcs):
        # Row a
        assert check[(wellrcs[0], MAPR_NS_GENE)] == [
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
        assert check[(wellrcs[1], MAPR_NS_GENE)] == [
            ('Gene', 'sws'),
            ('Gene name', 'swiss cheese'),
            ('Gene name', 'olfE'),
            ('Gene name', 'CG2212'),
            ('Gene name', 'PNPLA6'),
            ('Gene name', 'patatin like phospholipase domain containing 6'),
        ]

        assert check[(wellrcs[2], MAPR_NS_GENE)] == [
            ('Gene', 'ken'),
            ('Gene name', 'ken and barbie'),
            ('Gene name', 'CG5575'),
            ('Gene name', 'BCL6'),
            ('Gene name', 'B-cell lymphoma 6 protein'),
        ]

        assert check[(wellrcs[3], MAPR_NS_GENE)] == [
            ('Gene', ''),
            ('Gene name', 'Alg-2'),
        ]

        # Row b
        assert check[(wellrcs[4], MAPR_NS_GENE)] == check[
            (wellrcs[0], MAPR_NS_GENE)]
        assert check[(wellrcs[5], MAPR_NS_GENE)] == check[
            (wellrcs[1], MAPR_NS_GENE)]
        assert check[(wellrcs[6], MAPR_NS_GENE)] == check[
            (wellrcs[2], MAPR_NS_GENE)]
        assert check[(wellrcs[7], MAPR_NS_GENE)] == [
            ('Gene', ''),
            ('Gene name', 'Alg-2'),
        ]


class Plate2WellsNs2UnavailableHeader(Plate2WellsNs2):
    # For this test use explicit files instead of generating them as an
    # additional safeguard against changes in the test code

    def __init__(self):
        self.count = 4
        self.annCount = 4*2
        self.rowCount = 2
        self.colCount = 2
        self.csv = self.createCsv(
            colNames="Well,Gene,Gene Names",
            rowData=("a1,gene-a1,a1-name", "a2,gene-a2,a2-name",
                     "b1,gene-a1,a1-name", "b2,gene-a2,a2-name")
        )
        self.plate = None

    def get_cfg(self):
        return os.path.join(os.path.dirname(__file__),
                            'bulk_to_map_annotation_context_ns2_empty.yml')

    def assert_child_annotations(self, oas):
        wellrcs = [coord2offset(c) for c in (
            'a1', 'a2', 'b1', 'b2')]
        nss = [NSBULKANNOTATIONS, MAPR_NS_GENE]
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
            ('Gene', 'gene-a1'),
            ('Gene name', 'a1-name'),
            ('Gene ID', '')
        ]

        assert check[(wellrcs[1], nss[0])] == [
            ('Gene', 'gene-a2'),
            ('Gene name', 'a2-name'),
            ('Gene ID', '')
        ]

        assert check[(wellrcs[0], nss[1])] == [
            ('Gene', 'gene-a1'),
            ('Gene name', 'a1-name'),
            ('Gene ID', '')
        ]


class Plate2WellsNs2Fail(Plate2WellsNs2):
    # For this test use explicit files instead of generating them as an
    # additional safeguard against changes in the test code

    def __init__(self):
        self.count = 4
        self.annCount = 2
        self.rowCount = 1
        self.colCount = 2
        self.csv = self.createCsv(
            colNames="Well,Gene,Gene Names",
            rowData=("a1,,ABC", "A2,,ABC")
        )
        self.plate = None

    def get_cfg(self):
        return os.path.join(os.path.dirname(__file__),
                            'bulk_to_map_annotation_context_ns2_fail.yml')

    def get_namespaces(self):
        return 'openmicroscopy.org/mapr/gene_fail'


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


class Dataset2Images1Missing(Dataset2Images):

    def __init__(self):
        super(Dataset2Images1Missing, self).__init__()
        self.annCount = 1

    def get_target(self):
        """
        Temporarily alter self.names so that the super
        invocation creates fewer images than are expected.
        """
        old = self.names
        try:
            self.names = old[0:-1]  # Skip last
            return super(Dataset2Images1Missing, self).get_target()
        finally:
            self.names = old


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
        # failing on python 2.6
        # the following workaround can be reverted once py26 is dropped
        # with open(csvFileName, 'rb') as f_in:
        #      with gzip.open(gzipFileName, 'wb') as f_out:
        #          shutil.copyfileobj(f_in, f_out)
        f_in = open(csvFileName, 'rb')
        try:
            try:
                try:
                    f_out = gzip.open(gzipFileName, 'wb')
                except:
                    f_out = gzip(gzipFileName, 'wb')
                shutil.copyfileobj(f_in, f_out)
            finally:
                f_out.close()
        finally:
            f_in.close()

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


@pythonminver
class TestPopulateMetadataConfigLoad(ITest):

    def get_cfg_filepath(self):
        return os.path.join(os.path.dirname(__file__),
                            'bulk_to_map_annotation_context.yml')

    def _assert_configs(self, default_cfg, column_cfgs, advanced_cfgs):
        assert default_cfg == {"include": True}
        assert column_cfgs is None
        assert advanced_cfgs == {}

    def test_get_config_local(self):
        default_cfg, column_cfgs, advanced_cfgs = get_config(
            None, cfg=self.get_cfg_filepath())
        self._assert_configs(default_cfg, column_cfgs, advanced_cfgs)

    def test_get_config_remote(self):
        ofile = self.client.upload(self.get_cfg_filepath()).proxy()
        cfgid = unwrap(ofile.getId())
        default_cfg, column_cfgs, advanced_cfgs = get_config(
            self.client.getSession(), cfgid=cfgid)
        self._assert_configs(default_cfg, column_cfgs, advanced_cfgs)


@pythonminver
class TestPopulateMetadataHelper(ITest):

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
            ctx.write_to_omero(batch_size=batch_size, loops=10, ms=250)

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
        assert len(fixture.get_all_map_annotations()) == 0
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
        assert len(fixture.get_all_map_annotations()) == 0


@pythonminver
class TestPopulateMetadataHelperPerMethod(TestPopulateMetadataHelper):

    # Some tests in this file check the counts of annotations in a fixed
    # namespace, and therefore require a new client for each test method

    def setup_class(cls):
        pass

    def teardown_class(cls):
        pass

    def setup_method(self, method):
        super(TestPopulateMetadataHelperPerMethod, self).setup_class()

    def teardown_method(self, method):
        super(TestPopulateMetadataHelperPerMethod, self).teardown_class()


@pythonminver
class TestPopulateMetadata(TestPopulateMetadataHelper):

    METADATA_FIXTURES = (
        Screen2Plates(),
        Plate2Wells(),
        Dataset2Images(),
        Dataset2Images1Missing(),
        Dataset101Images(),
        Project2Datasets(),
        GZIP(),
    )
    METADATA_IDS = [x.__class__.__name__ for x in METADATA_FIXTURES]

    METADATA_NS_FIXTURES = (
        Plate2WellsNs(),
        Plate2WellsNs2(),
    )
    METADATA_NS_IDS = [x.__class__.__name__ for x in METADATA_NS_FIXTURES]

    @mark.parametrize("fixture", METADATA_FIXTURES, ids=METADATA_IDS)
    @mark.parametrize("batch_size", (None, 1, 10))
    def testPopulateMetadata(self, fixture, batch_size):
        """
        We should really test each of the parsing contexts in separate tests
        but in practice each one uses data created by the others, so for
        now just run them all together
        """
        fixture.init(self)
        t = self._test_parsing_context(fixture, batch_size)
        self._assert_parsing_context_values(t, fixture)
        self._test_bulk_to_map_annotation_context(fixture, batch_size)
        self._test_delete_map_annotation_context(fixture, batch_size)

    @mark.parametrize("fixture", METADATA_NS_FIXTURES, ids=METADATA_NS_IDS)
    def testPopulateMetadataNsAnns(self, fixture):
        """
        Test complicated annotations (multiple ns/groups) on a single OMERO
        data type, as opposed to testPopulateMetadata which tests simple
        annotations on multiple OMERO data types
        """
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

    def testPopulateMetadataNsAnnsUnavailableHeader(self):
        """
        Similar to testPopulateMetadataNsAnns but use two plates and check
        MapAnnotations aren't duplicated
        """
        fixture_empty = Plate2WellsNs2UnavailableHeader()
        fixture_empty.init(self)
        self._test_parsing_context(fixture_empty, 2)
        self._test_bulk_to_map_annotation_context(fixture_empty, 2)

    def testPopulateMetadataNsAnnsFail(self):
        """
        Similar to testPopulateMetadataNsAnns but use two plates and check
        MapAnnotations aren't duplicated
        """
        fixture_fail = Plate2WellsNs2Fail()
        fixture_fail.init(self)
        self._test_parsing_context(fixture_fail, 2)
        with raises(MapAnnotationPrimaryKeyException):
            self._test_bulk_to_map_annotation_context(fixture_fail, 2)


@pythonminver
class TestPopulateMetadataDedup(TestPopulateMetadataHelperPerMethod):

    # Hard-code the number of expected map-annotations in these tests
    # since the code in this file is complicated enough without trying
    # to parameterise this

    def _test_bulk_to_map_annotation_dedup(self, fixture1, fixture2, ns):
        options = {}
        if ns:
            options['ns'] = ns

        ann_count = fixture1.annCount
        assert fixture2.annCount == ann_count
        assert len(fixture1.get_child_annotations()) == ann_count
        assert len(fixture2.get_child_annotations()) == 0

        cfg = fixture2.get_cfg()

        target = fixture2.get_target()
        anns = fixture2.get_annotations()
        fileid = anns[0].file.id.val
        ctx = BulkToMapAnnotationContext(
            self.client, target, fileid=fileid, cfg=cfg, options=options)
        ctx.parse()
        assert len(fixture1.get_child_annotations()) == ann_count
        assert len(fixture2.get_child_annotations()) == 0

        ctx.write_to_omero()

        oas1 = fixture1.get_child_annotations()
        oas2 = fixture2.get_child_annotations()
        assert len(oas1) == ann_count

        if ns == NSBULKANNOTATIONS:
            assert len(oas2) == 8
            fixture1.assert_child_annotations(oas1)
            fixture2.assert_child_annotations(oas2, ns)
        if ns == MAPR_NS_GENE:
            assert len(oas2) == 8
            fixture1.assert_child_annotations(oas1)
            fixture2.assert_child_annotations(oas2, ns)
        if ns is None:
            assert len(oas2) == ann_count
            fixture1.assert_child_annotations(oas1)
            fixture2.assert_child_annotations(oas2)

        # The gene mapannotations should be common
        ids1 = set(unwrap(o[0].getId()) for o in oas1)
        ids2 = set(unwrap(o[0].getId()) for o in oas2)
        common = ids1.intersection(ids2)
        if ns == NSBULKANNOTATIONS:
            assert len(common) == 0
        else:
            assert len(common) == 4

    def _test_delete_map_annotation_context_dedup(
            self, fixture1, fixture2, ns):

        # Sanity checks in case the test code or fixtures are modified
        assert fixture1.annCount == 16
        assert fixture2.annCount == 16

        options = {}
        if ns:
            options['ns'] = ns

        assert len(fixture1.get_child_annotations()) == 16
        assert len(fixture1.get_child_annotations(NSBULKANNOTATIONS)) == 8
        assert len(fixture1.get_child_annotations(MAPR_NS_GENE)) == 8

        assert len(fixture2.get_child_annotations()) == 16
        assert len(fixture2.get_child_annotations(NSBULKANNOTATIONS)) == 8
        assert len(fixture2.get_child_annotations(MAPR_NS_GENE)) == 8

        assert len(fixture2.get_all_map_annotations()) == 20

        ctx = DeleteMapAnnotationContext(
            self.client, fixture1.get_target(), cfg=fixture1.get_cfg(),
            options=options)
        ctx.parse()
        ctx.write_to_omero(loops=10, ms=250)

        if ns == NSBULKANNOTATIONS:
            assert len(fixture1.get_child_annotations()) == 8
            assert len(fixture2.get_child_annotations()) == 16
            assert len(fixture2.get_all_map_annotations()) == 12
        if ns == MAPR_NS_GENE:
            assert len(fixture1.get_child_annotations()) == 8
            assert len(fixture2.get_child_annotations()) == 16
            assert len(fixture2.get_all_map_annotations()) == 20
        if ns is None:
            assert len(fixture1.get_child_annotations()) == 0
            assert len(fixture2.get_child_annotations()) == 16
            assert len(fixture2.get_all_map_annotations()) == 12

        ctx = DeleteMapAnnotationContext(
            self.client, fixture2.get_target(), cfg=fixture2.get_cfg(),
            options=options)
        ctx.parse()
        ctx.write_to_omero(loops=10, ms=250)

        if ns == NSBULKANNOTATIONS:
            assert len(fixture1.get_child_annotations()) == 8
            assert len(fixture1.get_child_annotations(MAPR_NS_GENE)) == 8
            assert len(fixture2.get_child_annotations()) == 8
            assert len(fixture2.get_child_annotations(MAPR_NS_GENE)) == 8
            assert len(fixture2.get_all_map_annotations()) == 4
        if ns == MAPR_NS_GENE:
            assert len(fixture1.get_child_annotations()) == 8
            assert len(fixture1.get_child_annotations(NSBULKANNOTATIONS)) == 8
            assert len(fixture2.get_child_annotations()) == 8
            assert len(fixture2.get_child_annotations(NSBULKANNOTATIONS)) == 8
            assert len(fixture2.get_all_map_annotations()) == 16
        if ns is None:
            assert len(fixture1.get_child_annotations()) == 0
            assert len(fixture2.get_child_annotations()) == 0
            assert len(fixture2.get_all_map_annotations()) == 0

    @mark.parametrize("ns", [None, NSBULKANNOTATIONS, MAPR_NS_GENE])
    def testPopulateMetadataNsAnnsDedup(self, ns):
        """
        Similar to testPopulateMetadataNsAnns but use two plates, check
        MapAnnotations aren't duplicated, and filter by namespace
        """
        fixture1 = Plate2WellsNs2()
        fixture1.init(self)
        self._test_parsing_context(fixture1, 2)
        self._test_bulk_to_map_annotation_context(fixture1, 2)

        fixture2 = Plate2WellsNs2()
        fixture2.init(self)
        self._test_parsing_context(fixture2, 2)
        self._test_bulk_to_map_annotation_dedup(fixture1, fixture2, ns)

    @mark.parametrize("ns", [None, NSBULKANNOTATIONS, MAPR_NS_GENE])
    def testPopulateMetadataNsAnnsDedupDelete(self, ns):
        """
        Similar to testPopulateMetadataNsAnns but use two plates, check
        MapAnnotations aren't duplicated, and delete by namespace
        """
        fixture1 = Plate2WellsNs2()
        fixture1.init(self)
        self._test_parsing_context(fixture1, 2)
        self._test_bulk_to_map_annotation_context(fixture1, 2)

        fixture2 = Plate2WellsNs2()
        fixture2.init(self)
        self._test_parsing_context(fixture2, 2)
        self._test_bulk_to_map_annotation_dedup(fixture1, fixture2, None)
        self._test_delete_map_annotation_context_dedup(
            fixture1, fixture2, ns)


@pythonminver
class TestPopulateMetadataConfigFiles(TestPopulateMetadataHelperPerMethod):

    def _init_fixture_attach_cfg(self):
        fixture = Plate2Wells()
        fixture.init(self)
        target = fixture.get_target()
        ofile = self.client.upload(fixture.get_cfg()).proxy()
        link = PlateAnnotationLinkI()
        link.parent = target.proxy()
        link.child = FileAnnotationI()
        link.child.ns = rstring(NSBULKANNOTATIONSCONFIG)
        link.child.file = ofile
        link = self.client.sf.getUpdateService().saveAndReturnObject(link)
        return fixture

    def _get_annotations_config(self, fixture):
        anns = []
        for ann in fixture.get_annotations():
            if not isinstance(ann, FileAnnotationI):
                pass
            if unwrap(ann.getNs()) == NSBULKANNOTATIONS:
                continue

            assert unwrap(ann.ns) == NSBULKANNOTATIONSCONFIG
            anns.append(ann)
        return anns

    @mark.parametrize("ns", [None, NSBULKANNOTATIONS, NSBULKANNOTATIONSCONFIG])
    @mark.parametrize("attach", [True, False])
    def test_delete_attach(self, ns, attach):
        fixture = self._init_fixture_attach_cfg()
        target = fixture.get_target()
        before = self._get_annotations_config(fixture)
        assert len(before) == 1

        options = {}
        if ns:
            options['ns'] = ns
        ctx = DeleteMapAnnotationContext(
            self.client, target, attach=attach, options=options)
        ctx.parse()
        ctx.write_to_omero()
        after = self._get_annotations_config(fixture)

        if attach and ns != NSBULKANNOTATIONS:
            assert len(after) == 0
        else:
            assert before[0].id == after[0].id
            assert before[0].file.id == after[0].file.id


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
            shape.x = rdouble(float(row[2]))
            shape.y = rdouble(float(row[3]))
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


@pythonminver
class TestPopulateRois(ITest):

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
