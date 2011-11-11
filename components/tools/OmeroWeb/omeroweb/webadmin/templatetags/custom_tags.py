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
import traceback
import logging

from django.conf import settings
from django import template
from django.templatetags.static import PrefixNode

register = template.Library()

logger = logging.getLogger('custom_tags')

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

    This simple version only works with template variable since we will use blocktrans for strings.
    """
    
    try:
        # split_contents() knows not to split quoted strings.
        tag_name, quantity, single, plural = token.split_contents()
    except ValueError:
        raise template.TemplateSyntaxError, "%r tag requires exactly three arguments" % token.contents.split()[0]

    return PluralNode(quantity, single, plural)

@register.tag()
def get_static_webadmin_prefix(parser, token):
    """
    Populates a template variable with the static prefix,
    ``settings.WEBADMIN_STATIC_URL``.

    Usage::

        {% get_static_webadmin_prefix [as varname] %}

    Examples::

        {% get_static_webadmin_prefix %}
        {% get_static_webadmin_prefix as STATIC_WEBADMIN_PREFIX %}

    """
    return PrefixNode.handle_token(parser, token, "STATIC_WEBADMIN_URL")

@register.tag()
def get_static_feedback_prefix(parser, token):
    """
    Populates a template variable with the static prefix,
    ``settings.FEEDBACK_STATIC_URL``.

    Usage::

        {% get_static_feedback_prefix [as varname] %}

    Examples::

        {% get_static_feedback_prefix %}
        {% get_static_feedback_prefix as STATIC_FEEDBACK_PREFIX %}

    """
    return PrefixNode.handle_token(parser, token, "STATIC_FEEDBACK_URL")