#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
#
# Copyright (c) 2016 University of Dundee.
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
#
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
#
# Version: 1.0
#

import fnmatch
import os
import re
import sys
from lxml.html import parse

tags = {'script': 'src', 'link': 'href'}

check_url_suffix = re.compile(r"{% static [\'|\"](.*?)[\'|\"]\|"
                              r"add:url_suffix %}")
check_version = re.compile(r"{% static [\'|\"].*(\d\.\d).*[\'|\"] %}")
check_variable = re.compile(r"{{(.*?)}}")


def check_regex(attr, regexp):
    match = regexp.search(attr)
    if match:
        return True
    return False


def find_templates(path="."):
    if os.path.isdir(path) and os.path.exists(path):
        for root, dirs, files in os.walk(path):
            # fidn template dirs only
            for dirname in fnmatch.filter(dirs, "templates"):
                template_dir = os.path.join(root, dirname)
                # any file
                for r, d, f in os.walk(template_dir):
                    templates = list(fnmatch.filter(f, "*"))
                    for _f in templates:
                        yield os.path.join(r, _f)


def check_web_template(filename):
    with open(filename) as infile:
        root = parse(infile).getroot()
        # parse to find sensitive tags
        for t in tags:
            for e in root.iter(t):
                # find atribute
                attr = e.get(tags[t])
                if attr:
                    if not check_regex(attr, check_variable):
                        if not check_regex(attr, check_url_suffix):
                            if not check_regex(attr, check_version):
                                sline = e.sourceline
                                yield '{0}:{1} {2}'.format(
                                    filename, sline, attr)


if __name__ == "__main__":

    fail = False
    for f in find_templates():
        for r in check_web_template(f):
            if not fail:
                print "Template error."
            print r
            fail = True

    if fail:
        sys.exit(1)
