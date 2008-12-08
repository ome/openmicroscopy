#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
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


import datetime

from django.conf import settings
from django import template

register = template.Library()

@register.simple_tag
def get_webadmin_root_url():
    base = "/%s" % (settings.WEBADMIN_ROOT_BASE)
    return str( base )

@register.simple_tag
def get_webadmin_static_url():
    base = "/%s/static" % (settings.WEBADMIN_ROOT_BASE)
    return str( base )

@register.simple_tag
def get_webclient_root_url():
    base = "/%s" % (settings.WEBCLIENT_ROOT_BASE)
    return str( base )

@register.simple_tag
def get_webclient_static_url():
    base = "/%s/static" % (settings.WEBCLIENT_ROOT_BASE)
    return str( base )

@register.simple_tag
def get_calendar_url():
    today = datetime.datetime.today()
    path = "%d/%d/" % (today.year, today.month)
    return str( path )


# makes settings available in template
@register.tag
def setting ( parser, token ): 
    try:
        tag_name, option = token.split_contents()
    except ValueError:
        raise template.TemplateSyntaxError, "%r tag requires a single argument" % token.contents[0]
    return SettingNode( option )

class SettingNode ( template.Node ): 
    def __init__ ( self, option ): 
        self.option = option

    def render ( self, context ): 
        # if FAILURE then FAIL silently
        try:
            return str(settings.__getattr__(self.option))
        except:
            return ""
