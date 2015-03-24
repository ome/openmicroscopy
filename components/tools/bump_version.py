#! /usr/bin/env python
# Script to bump major.minor version across the code

import os
import re
import argparse


def check_version_format(version):
    """Check format of major minor number"""
    pattern = '^[0-9]+[\.][0-9]+$'
    return re.match(pattern, version) is not None


def replace_file(input_path, pattern, version):
    """Substitute a pattern with version in a file"""
    with open(input_path, "r") as infile:
        regexp = re.compile(pattern)
        new_content = regexp.sub(r"\g<baseurl>%s" % version, infile.read())
        with open(input_path, "w") as output:
            output.write(new_content)
            output.close()
        infile.close()

docs_pattern = r"(?P<baseurl>site/support/omero)(\d+(.\d+)?)"
extensions = ('.txt', '.md', '.java', '.ice', '.html', '.xml', '.py', '.rst')

def bump_version(version):
    """Replace versions in documentation links"""

    # Replace versions in components pom.xml
    for base, dirs, files in os.walk('.'):
        for file in files:
            if file.endswith(extensions):
                replace_file(os.path.join(base, file), docs_pattern, version)


if __name__ == "__main__":
    # Input check
    parser = argparse.ArgumentParser()
    parser.add_argument("version", type=str)
    ns = parser.parse_args()

    if not check_version_format(ns.version):
        print "Invalid version format"
    else:
        bump_version(ns.version)
