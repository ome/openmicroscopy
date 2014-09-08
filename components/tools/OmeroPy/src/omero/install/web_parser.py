#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 
# Copyright (c) 2014 University of Dundee. 
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
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2014.
# 
# Version: 1.0
#

"""
Parser for the omeroweb.settings file to generate RST
mark up.
"""

import os
class WebSettings(object):

    def __init__(self, module='omeroweb.settings'):
        os.environ['DJANGO_SETTINGS_MODULE'] = module

    def print_rst(self):
        def underline(size):
            return '-' * size

        print ""
        print "OMERO.web properties"
        print "===================="
        print ""
        print ""

        from django.conf import settings

        for key, values in \
            sorted(settings.CUSTOM_SETTINGS_MAPPINGS.iteritems(), key=lambda k: k):

            global_name, default_value, mapping, description, config = tuple(values)

            if description is None:
                continue
            print ".. setting:: %s " % (global_name)
            print ""
            print global_name
            print underline(len(global_name))
            print ""
            print "``bin/omero config set %s``" % key
            print ""
            print "Default: ``%s`` " % (default_value)
            print ""
            print "Description: %s" % description
            print ""
            print ""

if __name__ == "__main__":
    ws = WebSettings()
    ws.print_rst()