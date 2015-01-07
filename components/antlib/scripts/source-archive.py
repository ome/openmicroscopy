#!/usr/bin/python

from __future__ import print_function

import os
from subprocess import call, Popen, PIPE
import sys
import zipfile

# Due to "git archive" not supporting archiving of submodules in
# addition to the base tree, this requires additional support in order
# to create a complete and functional source archive.
#
# This script archives the base tree, and then uses "git submodule
# foreach" to archive each submodule separately, setting the correct
# path prefix for each archive, so that they may all be unpacked in
# the same root to result in a complete and functional source tree.
# It then repacks each of these zip files into a single zip which is
# the source release, taking care to preserve timestamps and exectute
# permissions, etc.  This is done via ZipInfo objects, and the
# repacking is done entirely in memory so that this should work on any
# platform irrespective of filesystem support for the archive
# attributes.  It excludes .gitignore files at this point to avoid
# polluting the release with version control files.

if __name__ == "__main__":
    if len(sys.argv) != 9:
        raise Exception('Usage: %s releasename shortversion fullversion vcs-revision vcs-date versionfile cppversionfile targetdir')

    release = sys.argv[1]
    shortversion = sys.argv[2]
    version = sys.argv[3]
    vcs_revision = sys.argv[4]
    vcs_date = sys.argv[5]
    versionfile = sys.argv[6]
    cppversionfile = sys.argv[7]
    target = os.path.abspath(sys.argv[8])
    release = "%s-%s" % (release,version)

    if not os.path.isdir('.git'):
        raise Exception('Releasing is only possible from a git repository')

    print("Releasing %s" % (release))
    sys.stdout.flush()

    # Create base archive
    base_archive_status = call(['git', 'archive', '--format', 'zip',
                                '--prefix', "%s/" % (release),
                                '--output', "%s/%s-base.zip" % (target, release),
                                'HEAD'])
    if base_archive_status != 0:
        raise Exception('Failed to create git base archive')

    zips = list(["%s/%s-base.zip" % (target, release)])

    # Create submodule archives
    submodule_archive = Popen(['git', 'submodule', 'foreach', '--quiet', '--recursive',
                               "npath=\"$(echo \"$path\" | tr / _)\"; \
                                zip=\"%s/%s-submod-${npath}.zip\"; \
                                git archive --format zip --prefix \"%s/${path}/\" --output \"${zip}\" HEAD || exit 1; \
                                echo \"${zip}\"" % (target, release, release)],
                              stdout=PIPE)
    submodule_zips = submodule_archive.communicate()[0]
    if submodule_archive.returncode != 0:
        raise Exception('Failed to create git submodule archives')

    zips.extend(submodule_zips.splitlines())

    # Create destination zip file
    print("  - creating %s/%s.zip" % (target, release))
    sys.stdout.flush()
    basezip = zipfile.ZipFile("%s/%s.zip" % (target, release), 'w')

    # Repack each of the separate zips into the destination zip
    for name in zips:
        subzip = zipfile.ZipFile(name, 'r')
        print("  - repacking %s" % (name))
        sys.stdout.flush()
        # Iterate over the ZipInfo objects from the archive
        for info in subzip.infolist():
            # Skip unwanted git and travis files
            if os.path.basename(info.filename) == '.gitignore' or os.path.basename(info.filename) == '.gitmodule' or os.path.basename(info.filename) == '.travis.yml':
                continue
            # Repack a single zip object; preserve the metadata
            # directly via the ZipInfo object and rewrite the content
            # (which unfortunately requires decompression and
            # recompression rather than a direct copy)
            basezip.writestr(info, subzip.open(info.filename).read())

        # Remove repacked zip
        os.remove(name)

    # Embed release number
    basezip.writestr("%s/%s" % (release, versionfile),
"""<?xml version="1.0" encoding="utf-8"?>
<project name="gitversion" basedir=".">
        <property name="omero.shortversion" value="%s"/>
        <property name="omero.plainversion" value="%s"/>
        <property name="omero.vcs.revision" value="%s"/>
        <property name="omero.vcs.date" value="%s"/>

        <!-- Properties for detecting where these properties were read from -->
        <property name="omero.shortversion.source" value="embedded"/>
        <property name="omero.plainversion.source" value="embedded"/>
        <property name="omero.vcs.revision.source" value="embedded"/>
        <property name="omero.vcs.date.source" value="embedded"/>
</project>
""" % (shortversion, version, vcs_revision, vcs_date))
    basezip.writestr("%s/%s" % (release, cppversionfile),
"""set(OME_VERSION "%s")
set(OME_VERSION_SHORT "%s")
set(OME_VCS_REVISION "%s")
set(OME_VCS_DATE "%s")
""" % (version, shortversion, vcs_revision, vcs_date))
