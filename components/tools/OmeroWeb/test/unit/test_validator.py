#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
#
# Copyright (c) 2008-2014 University of Dundee.
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

import pytest
import tempfile

from omeroweb_validator import check_regex, check_web_template
from omeroweb_validator import check_url_suffix
from omeroweb_validator import check_version
from omeroweb_validator import check_variable


@pytest.mark.parametrize('attr', [
    ("{% static '3rdparty/lib.js'|add:url_suffix %}", True),
    ("{% static '3rdparty/lib.js' %}", False),
])
def test_check_url_suffix(attr):
    assert attr[1] == check_regex(attr[0], check_url_suffix)


@pytest.mark.parametrize('attr', [
    ("{% static '3rdparty/lib-1.0.1.js' %}", True),
    ("{% static '3rdparty/lib.js' %}", False),
])
def test_check_version(attr):
    assert attr[1] == check_regex(attr[0], check_version)


@pytest.mark.parametrize('attr', [
    ("{{ foo }}", True),
    ("{% foo %}", False),
])
def test_check_variable(attr):
    assert attr[1] == check_regex(attr[0], check_variable)


@pytest.mark.parametrize('content', [
    ('<link rel="stylesheet" href="{% static \'web/css/file-0.0.css\' %}"'
     ' type="text/css" />'),
    ('<link rel="stylesheet" href="{% static \'web/css/file-0.0.0.css\' %}"'
     ' type="text/css" />'),
    ('<link rel="stylesheet" href="{% static \'web/css/lib-0.0/file.css\' %}"'
     ' type="text/css" />'),
    ('<script src="{% static \'3rd/file-0.0.js\' %}"'
     ' type="text/javascript"/>'),
    ('<script src="{% static \'3rd/file-0.0.0.js\' %}"'
     ' type="text/javascript"/>'),
    ('<script src="{% static \'3rd/lib-0.0/file.js\' %}"'
     ' type="text/javascript"/>'),
    """<html><head>
       <script type="text/javascript"
        src="{% static '3rd/file-0.0.0.js' %}"></script>
       <link rel="stylesheet" href="{% static 'web/css/file-0.0.0.css' %}"
        type="text/css" ></link>
       </head></html>
    """,
    ("<script src='{% static \"3rd/lib-0.0/file.js\" %}'"
     " type='text/javascript'/>"),
])
def test_check_web_template(content):
    with tempfile.NamedTemporaryFile() as temp:
        temp.write(content)
        temp.flush()
        for r in check_web_template(temp.name):
            assert temp.name not in r


@pytest.mark.parametrize('content', [
    ('<link rel="shortcut icon" href="{% static "favicon.ico" %}"'
     ' type="image/x-icon" />'),
    ('<link href="{% static "web/css/file.css"|add:url_suffix %}"'
     ' rel="stylesheet" type="text/css" />'),
    ('<script type="text/javascript" '
     'src="{% static "3rd/file.js" %}"></script>'),
    ("<script src='{% static '3rd/lib-0.0/file.js' %}'"
     " type='text/javascript'/>"),
    ('<script src="{% static "3rd/lib-0.0/file.js" %}"'
     ' type="text/javascript"/>'),

])
def test_bad_check_web_template(content):
    with tempfile.NamedTemporaryFile() as temp:
        temp.write(content)
        temp.flush()
        for r in check_web_template(temp.name):
            assert temp.name in r
