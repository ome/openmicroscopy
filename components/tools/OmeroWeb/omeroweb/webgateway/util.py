#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2008-2014 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

import os
import tempfile
import zipfile
import shutil
import logging

logger = logging.getLogger(__name__)

LUTS_IN_PNG = [
    '/luts/ncsa_paledit/16_colors.lut',
    '/luts/3-3-2_rgb.lut',
    '/luts/ncsa_paledit/5_ramps.lut',
    '/luts/ncsa_paledit/6_shades.lut',
    '/luts/ncsa_paledit/blue_orange_icb.lut',
    '/luts/ncsa_paledit/brgbcmyw.lut',
    '/luts/ncsa_paledit/cool.lut',
    '/luts/wcif/cyan_hot.lut',
    '/luts/ncsa_paledit/edges.lut',
    '/luts/fire.lut',
    '/luts/ncsa_paledit/gem.lut',
    '/luts/glasbey.lut',
    '/luts/glasbey_inverted.lut',
    '/luts/glow.lut',
    '/luts/grays.lut',
    '/luts/wcif/green_fire_blue.lut',
    '/luts/wcif/hilo.lut',
    '/luts/wcif/ica.lut',
    '/luts/wcif/ica2.lut',
    '/luts/wcif/ica3.lut',
    '/luts/ice.lut',
    '/luts/wcif/magenta_hot.lut',
    '/luts/wcif/orange_hot.lut',
    '/luts/ncsa_paledit/phase.lut',
    '/luts/physics.lut',
    '/luts/janelia/pup_br.lut',
    '/luts/janelia/pup_nr.lut',
    '/luts/wcif/rainbow_rgb.lut',
    '/luts/red-green.lut',
    '/luts/wcif/red_hot.lut',
    '/luts/ncsa_paledit/royal.lut',
    '/luts/ncsa_paledit/sepia.lut',
    '/luts/ncsa_paledit/smart.lut',
    '/luts/spectrum.lut',
    '/luts/ncsa_paledit/thal.lut',
    '/luts/ncsa_paledit/thallium.lut',
    '/luts/thermal.lut',
    '/luts/ncsa_paledit/unionjack.lut',
    '/luts/wcif/yellow_hot.lut',
    # NB: this is not a LUT but it is in the current png
    # Used for channel slider gradient when no lut chosen
    'gradient.png',
]


# helper method
def getIntOrDefault(request, name, default):
    index = request.GET.get(name, request.POST.get(name, default))
    if index is not None:
        index = int(index)
    return index


def get_longs(request, name):
    """
    Retrieves parameters from the request. If the parameters are not present
    an empty list is returned

    This does not catch exceptions as it makes sense to throw exceptions if
    the arguments provided do not pass basic type validation
    """
    vals = []
    vals_raw = request.GET.getlist(name)
    for val_raw in vals_raw:
        vals.append(long(val_raw))
    return vals


def zip_archived_files(images, temp, zipName, buf=2621440):
    """
    Util function to download original files from a list of images
    and arrange them within a temp file, such that there are no
    name clashes and multi-image filesets are kept distict.
    Handles archived files from OMERO 4 images and fileset files
    for OMERO 5 images.

    @param images:      Images as source of original files.
    @type               List of ImageWrappers
    @param temp:        Directory for creating Zip file
    @param zipName:     Name of zip
    """

    # ',' in name causes duplicate headers
    zipName = zipName.replace(" ", "_").replace(",", ".")
    if not zipName.endswith('.zip'):
        zipName = "%s.zip" % zipName

    def getTargetPath(fsFile, templatePrefix):
        if fsFile.getPath() == templatePrefix or templatePrefix == "":
            return fsFile.getName()
        relPath = os.path.relpath(fsFile.getPath(), templatePrefix)
        return os.path.join(relPath, fsFile.getName())

    def split_path(p):
        a, b = os.path.split(p)
        return (split_path(a) if len(a) and len(b) else []) + [b]

    fsIds = set()
    fIds = set()

    temp_zip_dir = tempfile.mkdtemp()
    logger.debug("download dir: %s" % temp_zip_dir)
    try:
        new_dir_idx = 1     # if needed to avoid file name clashes
        for image in images:
            new_dir = ""
            templatePrefix = ""
            fs = image.getFileset()
            if fs is not None:
                # Make sure we've not processed this fileset before.
                if fs.id in fsIds:
                    continue
                fsIds.add(fs.id)
                templatePrefix = fs.getTemplatePrefix()
            files = list(image.getImportedImageFiles())

            # check if ANY of the files will overwrite exising file
            for f in files:
                target_path = getTargetPath(f, templatePrefix)
                base_file = os.path.join(
                    temp_zip_dir, split_path(target_path)[0])
                if os.path.exists(base_file):
                    new_dir = str(new_dir_idx)
                    new_dir_idx += 1
                    break

            for a in files:
                # check for duplicate files for OMERO 4.4 images (no fileset)
                if a.id in fIds:
                    continue
                fIds.add(a.id)
                temp_f = getTargetPath(a, templatePrefix)
                temp_f = os.path.join(temp_zip_dir, new_dir, temp_f)
                temp_d = os.path.dirname(temp_f)
                if not os.path.exists(temp_d):
                    os.makedirs(temp_d)

                # Need to be sure that the zip name does not match any file
                # within it since OS X will unzip as a single file instead of
                # a directory
                if zipName == "%s.zip" % a.name:
                    zipName = "%s_folder.zip" % a.name

                f = open(str(temp_f), "wb")
                try:
                    for chunk in a.getFileInChunks(buf=buf):
                        f.write(chunk)
                finally:
                    f.close()

        # create zip
        zip_file = zipfile.ZipFile(temp, 'w', zipfile.ZIP_DEFLATED)
        try:
            for root, dirs, files in os.walk(temp_zip_dir):
                archive_root = os.path.relpath(root, temp_zip_dir)
                for f in files:
                    fullpath = os.path.join(root, f)
                    archive_name = os.path.join(archive_root, f)
                    zip_file.write(fullpath, archive_name)
        finally:
            zip_file.close()
            # delete temp dir
    finally:
        shutil.rmtree(temp_zip_dir, ignore_errors=True)

    return zipName
