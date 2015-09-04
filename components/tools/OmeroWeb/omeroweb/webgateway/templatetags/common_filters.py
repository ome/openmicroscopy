#!/usr/bin/env python
# -*- coding: utf-8 -*-
# #!/usr/bin/env python
#
#
#
# Copyright (c) 2008-2013 University of Dundee.
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
import json
import random

from django import template

register = template.Library()

logger = logging.getLogger(__name__)


@register.filter()
def jsonify(obj):
    """Simple template filter to encode a variable to JSON format"""
    return json.dumps(obj)


@register.filter
def hash(value, key):
    return value[key]


@register.filter
def random_if_none(value):
    if value is None:
        value = str(random.random())[2:]
    return value


@register.filter
def random_if_minus_one(value):
    """ Used for thumbnail versions """
    if value == -1:
        value = str(random.random())[2:]
    return value


@register.filter
def ago(value):
    """
    Formats a datetime.datetime object as time Ago.
    E.g. '3 days 2 hours 10 minutes'
    """
    try:
        ago = datetime.datetime.now() - value
    except TypeError:
        return str(value)

    def plurals(val):
        return val != 1 and "s" or ""
    hours, remainder = divmod(ago.seconds, 3600)
    mins, secs = divmod(remainder, 60)
    if ago.days >= 365:
        years = ago.days / 365
        return "%s year%s" % (years, plurals(years))
    if ago.days > 28:
        months = ago.days / 30
        return "%s month%s" % (months, plurals(months))
    if ago.days > 0:
        return "%s day%s" % (ago.days, plurals(ago.days))
    if hours > 0:
        return "%s hour%s" % (hours, plurals(hours))
    if mins > 1:
        return "%s minutes" % (mins)
    if mins == 1:
        return "a minute"
    return "less than a minute"


@register.filter
def truncateafter(value, arg):
    """
    Truncates a string after a given number of chars
    Argument: Number of chars to truncate after
    """
    try:
        length = int(arg)
    except ValueError:  # invalid literal for int()
        return value  # Fail silently.
    if not isinstance(value, basestring):
        value = str(value)
    if (len(value) > length):
        return value[:length] + "..."
    else:
        return value


@register.filter
def truncatebefor(value, arg):
    """
    Truncates a string after a given number of chars
    Argument: Number of chars to truncate befor
    """
    try:
        length = int(arg)
    except ValueError:  # invalid literal for int()
        return value  # Fail silently.
    if not isinstance(value, basestring):
        value = str(value)
    if (len(value) > length):
        return "..."+value[len(value)-length:]
    else:
        return value


@register.filter
def shortening(value, arg):
    try:
        length = int(arg)
    except ValueError:  # invalid literal for int()
        return value  # Fail silently.
    front = length/2-3
    end = length/2-3

    if not isinstance(value, basestring):
        value = str(value)
    try:
        l = len(value)
        if l < length:
            return value
        elif l >= length:
            return value[:front]+"..."+value[l-end:]
    except Exception:
        logger.error(traceback.format_exc())
        return value


# See https://code.djangoproject.com/ticket/361
@register.filter
def subtract(value, arg):
    "Subtracts the arg from the value"
    return int(value) - int(arg)


# From http://djangosnippets.org/snippets/1357/
@register.filter
def get_range(value):
    """
    Filter - returns a list containing range made from given value
    Usage (in template):

    <ul>{% for i in 3|get_range %}
      <li>{{ i }}. Do something</li>
    {% endfor %}</ul>

    Results with the HTML:
    <ul>
      <li>0. Do something</li>
      <li>1. Do something</li>
      <li>2. Do something</li>
    </ul>

    Instead of 3 one may use the variable set in the views
    """
    return range(value)


@register.filter
def lengthformat(value):
    """
    Filter - returns the converted value
    all values are in micrometers
    """
    try:
        value = float(value)
    except (TypeError, ValueError, UnicodeDecodeError):
        return value

    if value < 0.001:
        return value * 1000 * 10
    elif value < 0.01:
        return value * 1000
    elif value < 1000:
        return value
    elif value < 1000 * 100:
        return value / 1000
    elif value < 1000 * 100 * 10:
        return value / 1000 / 100
    elif value < 1000 * 100 * 10 * 100:
        return value / 1000 / 100 / 10
    else:
        return value / 1000 / 100 / 10 / 1000


@register.filter
def lengthunit(value):
    """
    Filter - returns th emost suitable length units
    all values are in micrometers
    """

    if value == 0:
        return u'\u00B5m'
    elif value < 0.001:
        return u"\u212B"
    elif value < 0.01:
        return u"nm"
    elif value < 1000:
        return u'\u00B5m'
    elif value < 1000 * 100:
        return "mm"
    elif value < 1000 * 100 * 10:
        return "cm"
    elif value < 1000 * 100 * 10 * 100:
        return "m"
    else:
        return "km"


@register.filter
def timeformat(value):
    """
    Filter - returns the converted value with units
    all values are in seconds
    """
    from decimal import Decimal, InvalidOperation
    from django.utils.encoding import force_unicode

    if value is None:
        return ''
    try:
        value = Decimal(force_unicode(value))
    except UnicodeEncodeError:
        return u''
    except InvalidOperation:
        try:
            value = Decimal(force_unicode(float(value)))
        except (ValueError, InvalidOperation, TypeError, UnicodeEncodeError):
            return u'%s s' % str(value)
    if value == 0:
        return u'%d\u00A0s' % value
    if value < Decimal("0.001"):
        return u'%d\u00A0\u00B5s' % (value * 1000 * 1000)
    elif value < 1:
        return u'%d\u00A0ms' % (value * 1000)
    elif value < 60:
        return u'%d\u00A0s' % value
    elif value < 60 * 60:
        return u'%d\u00A0min\u00A0%d\u00A0s' % (value / 60, value % 60)
    else:
        return u'%d\u00A0h\u00A0%d\u00A0min' % (value / 3600, round((value % 3600)/60))
