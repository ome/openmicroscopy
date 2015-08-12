#! /usr/bin/env python
# Script to bump Bio-Formats version

import os
import re
import argparse

etc_dir = os.path.join(os.path.dirname(__file__), "..", "..", "etc")
properties_file = os.path.join(etc_dir, "omero.properties")
properties_pattern = re.compile(
    r"(?P<base>versions.bioformats=)(\d+.\d+.\d+.*)")
localproperties_file = os.path.join(etc_dir, "local.properties.example")
resolver_pattern = re.compile(r"(?P<base>ome\.resolver=)([a-z\-]+)")


def check_version_format(version):
    """Check version is either x.y.z or x.y.z-SNAPSHOT"""
    pattern1 = re.compile('^[0-9]+[\.][0-9]+[\.][0-9]$')
    pattern2 = re.compile('^[0-9]+[\.][0-9]+[\.][0-9]-SNAPSHOT$')
    return (pattern1.match(version) or pattern2.match(version))


def replace_file(input_path, pattern, version):
    """Substitute a pattern with version in a file"""
    with open(input_path, "r") as infile:
        new_content = pattern.sub(r"\g<base>%s" % version, infile.read())
        with open(input_path, "w") as output:
            output.write(new_content)
            output.close()
        infile.close()


def bump_bf_version(version):
    """Replace versions in documentation links"""

    replace_file(properties_file, properties_pattern, version)
    if version.endswith('SNAPSHOT'):
        resolver = 'ome-simple-artifactory'
    else:
        resolver = 'ome-resolver'
    replace_file(localproperties_file, resolver_pattern, resolver)


if __name__ == "__main__":
    # Input check
    parser = argparse.ArgumentParser()
    parser.add_argument("version", type=str)
    ns = parser.parse_args()

    if not check_version_format(ns.version):
        print ("Invalid version format: should be either x.y.z or"
               " x.y.z-SNAPSHOT")
    else:
        bump_bf_version(ns.version)
