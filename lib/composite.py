#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Creates composite builds from the various client build artifacts/platforms.
"""

# Copyright (C) 2009-2014 University of Dundee

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


import time
import sys
import re
import os

from getopt import getopt, GetoptError
from glob import glob
from zipfile import ZipFile

NOW = time.ctime()

VERSION = os.environ["OMERO_VERSION"]

print "="*40
print " Version: %s" % VERSION
print "="*40

TARGET_PREFIX = 'OMERO.clients-%s' % VERSION

INSIGHT = 'OMERO.insight'

IMPORTER = 'OMERO.importer'

IGNORE = []

def find(pattern):
    """Grabs platform specific distribution targets from target"""
    target = os.path.join(os.path.dirname(os.path.dirname(__file__)), "target")
    artifacts = glob(os.path.join(target, pattern))
    if not artifacts and pattern.find("*win.zip") == -1:
        raise Exception("Nothing found! %s" % pattern)
    return artifacts

def extract(artifact, target, ignore):
    """
    Extracts all files of a given artifact to a target directory filtering and
    mangling paths as necessary.
    """
    zip_file = ZipFile(artifact, 'r')
    try:
        for zip_info in zip_file.infolist():
            name = zip_info.filename
            out_name = name[name.find('/') + 1:]

            skip = 0
            if len(out_name) == 0:
                skip += 1
            if os.path.split(name)[1] in ignore:
                skip += 1
            for pattern in ignore:
                if re.match(pattern, os.path.split(name)[1]):
                    skip += 1
            if skip:
                print "Skipping %s" % name
                continue

            out = None
            try:
                path = "/".join([target, out_name])
                if path[-1:] == '/':
                    if not os.path.exists(path):
                        os.makedirs(path)
                    continue
                print "Extracting: %s" % path
                out = open(path, 'w')
                txt = zip_file.read(name)
                if path.endswith("about.xml"):
                    print "Filtering: %s" % path
                    txt = txt.replace("@DATE@", NOW)
                    txt = txt.replace("@VERSION@", VERSION)
                out.write(txt)
                os.chmod(path, zip_info.external_attr >> 16L)
            finally:
                if out is not None:
                    out.close()
    finally:
        zip_file.close()

def compress(target, base):
    """Creates a ZIP recursively from a given base directory."""
    zip_file = ZipFile(target, 'w')
    try:
        for root, dirs, names in os.walk(base):
            for name in names:
                path = os.path.join(root, name)
                print "Compressing: %s" % path
                zip_file.write(path)
    finally:
        zip_file.close()


#
# Create the composite Windows client build
#
target_artifacts = list()
target_artifacts += find(INSIGHT + "*win.zip")
target_artifacts += find(IMPORTER + "*win.zip")
target = '%s.win' % TARGET_PREFIX
ignore = ['omero_client.jar',
          'omero-clients-util-r\d+-b\d+.jar'] + IGNORE

os.makedirs(target)
for artifact in target_artifacts:
    extract(artifact, target, ignore)
compress('%s.zip' % target, target)

#
# Create the composite Mac OS X client build
#
target_artifacts = list()
target_artifacts += find(INSIGHT + "*mac.zip")
target_artifacts += find(IMPORTER + "*mac.zip")
target = '%s.mac' % TARGET_PREFIX

os.makedirs(target)
for artifact in target_artifacts:
    extract(artifact, target, IGNORE)
compress('%s.zip' % target, target)

#
# Create the composite Linux client build
#
target_artifacts = list()
target_artifacts += find(INSIGHT + "*linux.zip")
target_artifacts += find(IMPORTER + "*linux.zip")
target = '%s.linux' % TARGET_PREFIX

os.makedirs(target)
for artifact in target_artifacts:
    extract(artifact, target, ignore)
compress('%s.zip' % target, target)

# Insight no longer uses omero_client.jar

