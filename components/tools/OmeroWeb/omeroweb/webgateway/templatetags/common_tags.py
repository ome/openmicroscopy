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


import logging

from django.conf import settings
from django import template

register = template.Library()

logger = logging.getLogger(__name__)


# makes settings available in template
@register.tag
def setting(parser, token):
    try:
        tag_name, option = token.split_contents()
    except ValueError:
        raise template.TemplateSyntaxError(
            "%r tag requires a single argument" % token.contents[0])
    return SettingNode(option)


class SettingNode (template.Node):
    def __init__(self, option):
        self.option = option

    def render(self, context):
        try:
            setting = settings
            for name in self.option.split('.'):
                if name.isdigit():
                    setting = setting[int(name)]
                else:
                    if type(setting) == dict:
                        setting = setting.get(name)
                    else:
                        setting = setting.__getattr__(name)
            if setting is None:
                return ""
            return str(setting)
        except:
            # if FAILURE then FAIL silently
            return ""


class PluralNode(template.Node):
    def __init__(self, quantity, single, plural):
        self.quantity = template.Variable(quantity)
        self.single = template.Variable(single)
        self.plural = template.Variable(plural)

    def render(self, context):
        if self.quantity.resolve(context) == 1:
            return u'%s' % self.single.resolve(context)
        else:
            return u'%s' % self.plural.resolve(context)


@register.tag(name="plural")
def do_plural(parser, token):
    """
    Usage: {% plural quantity name_singular name_plural %}

    This simple version only works with template variable since we will use
    blocktrans for strings.
    """

    try:
        # split_contents() knows not to split quoted strings.
        tag_name, quantity, single, plural = token.split_contents()
    except ValueError:
        raise template.TemplateSyntaxError(
            "%r tag requires exactly three arguments"
            % token.contents.split()[0])

    return PluralNode(quantity, single, plural)
