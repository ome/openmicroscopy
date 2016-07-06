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
from omeroweb_validator import check_regex
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
