#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Retrieves the JARs from the latest Hudson based build of the LOCI software
repository; predominently Bio-Formats.
"""

# Copyright (C) 2009 University of Dundee

# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.


import urllib
import sys
import re
import os

from subprocess import Popen
from getopt import getopt, GetoptError
from glob import glob

# Handle Python 2.5 built-in ElementTree
try:
        from xml.etree.ElementTree import XML, ElementTree, tostring
except ImportError:
        from elementtree.ElementTree import XML, ElementTree, tostring

def usage(error=None):
    """Prints usage so that we don't have to. :)"""
    cmd = sys.argv[0]
    if error is not None:
        print error
    print """Usage: %s
Retrieves the latest metadata validator test report from Hudson and extracts
some statistics for each file.

Options:
    --report              Prints tab seperated statistics for each file's
                          setId() timings
    --summary             Prints a summary of all files [default]
    --help                Prints this help text
    --config=[4.1|TRUNK]  Sets the metadata validator instance to use
                          [defaults to TRUNK]

Examples:
    %s --summary --config=4.1
    %s --report --config=TRUNK > report.csv

Report bugs to ome-devel@lists.openmicroscopy.org.uk""" % (cmd, cmd, cmd)
    sys.exit(2)

# OME Hudson last successful metadata validator run test report URL
URLs = {
    '4.1': "http://hudson.openmicroscopy.org.uk/job/omero-metadata-validator-Beta4.1/lastSuccessfulBuild/",
    'TRUNK': "http://hudson.openmicroscopy.org.uk/job/omero-metadata-validator/lastSuccessfulBuild/",
}

# OME Hudson test report URL suffix
TEST_SUFFIX = "testReport/"

# Hudson XML API suffix
API_SUFFIX = "api/xml"

def download(config):
    url = urllib.urlopen("%s%s" % (URLs[config], API_SUFFIX))
    hudson_xml = url.read()
    url.close()
    root = XML(hudson_xml)
    build_no = root.findtext("./number")
    bioformats_rev = 'Unknown'
    for artifact in root.findall("./artifact"):
        file_name = artifact.findtext("./fileName")
        match = re.match(r'bio-formats-r(\d+).jar', file_name)
        if match:
            bioformats_rev = match.group(1)
            break
    omero_rev = root.findtext("./changeSet/revision/revision")
    url = urllib.urlopen("%s%s%s" % (URLs[config], TEST_SUFFIX, API_SUFFIX))
    hudson_xml = url.read()
    url.close()
    return { 'root': XML(hudson_xml), 'build_no': build_no,
             'bioformats_rev': bioformats_rev, 'omero_rev': omero_rev }

def report(root, build_no, bioformats_rev, omero_rev):
    suites = root.findall("./suite")
    print "filename\ttestMetadataLevelMinimumSetId\ttestMetadataLevelAllSetId"
    for suite in suites:
        suite_name = suite.findtext("name")
        cases = suite.findall("case")
        minimum_set_id = None
        all_set_id = None
        # Parse the Hudson XML
        for case in cases:
            case_name = case.findtext("name")
            if "testMetadataLevelMinimumSetId" == case_name:
                minimum_set_id = case.findtext("duration")
            if "testMetadataLevelAllSetId" == case_name:
                all_set_id = case.findtext("duration")
        if minimum_set_id is not None and all_set_id is not None:
            print "\t".join([suite_name, minimum_set_id, all_set_id])

def summary(root, build_no, bioformats_rev, omero_rev):
    suites = root.findall("./suite")

    minimum_set_id_min = { 'filename': None, 'min': float('inf') }
    minimum_set_id_max = { 'filename': None, 'max': None }
    minimum_set_id_total = 0.0
    all_set_id_min = { 'filename': None, 'min': float('inf') }
    all_set_id_max = { 'filename': None, 'max': None }
    all_set_id_total = 0.0
    # Parse the Hudson XML
    suite_count = 0
    for suite in suites:
        suite_name = suite.findtext("name")
        cases = suite.findall("case")
        for case in cases:
            case_name = case.findtext("name")
            if "testMetadataLevelMinimumSetId" == case_name:
                minimum_set_id = float(case.findtext("duration"))
                if minimum_set_id < minimum_set_id_min['min']:
                    minimum_set_id_min['min'] = minimum_set_id
                    minimum_set_id_min['filename'] = suite_name
                if minimum_set_id > minimum_set_id_max['max']:
                    minimum_set_id_max['max'] = minimum_set_id
                    minimum_set_id_max['filename'] = suite_name
                minimum_set_id_total += minimum_set_id
            if "testMetadataLevelAllSetId" == case_name:
                all_set_id = float(case.findtext("duration"))
                if all_set_id < all_set_id_min['min']:
                    all_set_id_min['min'] = all_set_id
                    all_set_id_min['filename'] = suite_name
                if all_set_id > all_set_id_max['max']:
                    all_set_id_max['max'] = all_set_id
                    all_set_id_max['filename'] = suite_name
                all_set_id_total += all_set_id
                # Only needs to happen once :)
                suite_count += 1
    # Print our report
    print "Metadata validator build: %s" % build_no
    print "Bio-Formats revision: %s" % bioformats_rev
    print "OMERO.server revision: %s" % omero_rev
    print "Suite count: %d" % suite_count
    x = minimum_set_id_min
    if x['filename'] is not None:
        print " ---- "
        print "Minimum MINIMUM setId() time %fsec: %s" % (x['min'], x['filename'])
    x = minimum_set_id_max
    if x['filename'] is not None:
        print "Maximum MINIMUM setId() time %fsec: %s" % (x['max'], x['filename'])
        print "Total MINIMUM setId() time %fsec" % (minimum_set_id_total)
        print "Average MINIMUM setId() time %fsec" % \
            (minimum_set_id_total / suite_count)
    print " ---- "
    x = all_set_id_min
    print "Minimum ALL setId() time %fsec: %s" % (x['min'], x['filename'])
    x = all_set_id_max
    print "Maximum ALL setId() time %fsec: %s" % (x['max'], x['filename'])
    print "Total ALL setId() time %fsec" % (all_set_id_total)
    print "Average ALL setId() time %fsec" % (all_set_id_total / suite_count)

if __name__ == "__main__":
    try:
        options, args = getopt(sys.argv[1:], "", 
                ['summary', 'report', 'help', 'config='])
    except GetoptError, (msg, opt):
        usage(msg)

    config = 'TRUNK'
    to_do = summary
    for option, argument in options:
        if '--help' == option:
            usage()
        if '--report' == option:
            to_do = report
        if '--config' == option:
            config = argument
    metadata = download(config)
    to_do(metadata['root'], metadata['build_no'],
          metadata['bioformats_rev'], metadata['omero_rev'])
