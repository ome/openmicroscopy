#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# webclient_gateway
#
# Copyright (c) 2008-2011 University of Dundee.
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
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2012
#
# Version: 1.0
#

import logging

logger = logging.getLogger(__name__)


class GatewayConfig(object):

    """
    Global Gateway configuration:

    - :attr:`IMG_RDEFNS`:  a namespace for annotations linked on images holding
                           the default rendering settings object id.
    - :attr:`IMG_ROPTSNS`: a namespace for annotations linked on images holding
                           default rendering options that don't get saved in
                           the rendering settings.
    """

    def __init__(self):
        self.IMG_RDEFNS = None
        self.IMG_ROPTSNS = None


class ServiceOptsDict(dict):

    def __new__(cls, *args, **kwargs):
        return super(ServiceOptsDict, cls).__new__(cls, *args, **kwargs)

    def __init__(self, data=None, *args, **kwargs):
        if data is None:
            data = dict()
        if len(kwargs) > 0:
            for key, val in dict(*args, **kwargs).iteritems():
                self[key] = val
        if isinstance(data, dict):
            for key in data:
                item = data[key]
                if self._testItem(item):
                    self[key] = str(item)
                else:
                    logger.debug(
                        "None or non- string, unicode or numeric type"
                        "values are ignored, (%r, %r)" % (key, item))
        else:
            raise AttributeError(
                "%s argument (%r:%s) must be a dictionary"
                % (self.__class__.__name__, data, type(data)))

    def __repr__(self):
        return "<%s: %s>" % (self.__class__.__name__,
                             super(ServiceOptsDict, self).__repr__())

    def __setitem__(self, key, item):
        """Set key to value as string."""
        if self._testItem(item):
            super(ServiceOptsDict, self).__setitem__(key, str(item))
            logger.debug("Setting %r to %r" % (key, item))
        else:
            raise AttributeError(
                "%s argument (%r:%s) must be a string, unicode or numeric type"
                % (self.__class__.__name__, item, type(item)))

    def __getitem__(self, key):
        """
        Return the value for key if key is in the dictionary.
        Raises a KeyError if key is not in the map.
        """
        try:
            return super(ServiceOptsDict, self).__getitem__(key)
        except KeyError:
            raise KeyError("Key %r not found in %r" % (key, self))

    def __delitem__(self, key):
        """
        Remove dict[key] from dict.
        Raises a KeyError if key is not in the map.
        """
        super(ServiceOptsDict, self).__delitem__(key)
        logger.debug("Deleting %r" % (key))

    def copy(self):
        """Returns a copy of this object."""
        return self.__class__(self)

    def clear(self):
        """Remove all items from the dictionary."""
        super(ServiceOptsDict, self).clear()

    def get(self, key, default=None):
        """
        Return the value for key if key is in the dictionary, else default.
        If default is not given, it defaults to None, so that this method
        never raises a KeyError.
        """
        try:
            return self.__getitem__(key)
        except KeyError:
            return default

    def set(self, key, value):
        """Set key to value as string."""
        return self.__setitem__(key, value)

    def getOmeroGroup(self):
        return self.get('omero.group')

    def setOmeroGroup(self, value=None):
        if value is not None:
            self.set('omero.group', value)
        else:
            try:
                del self['omero.group']
            except KeyError:
                logger.debug("Key 'omero.group' not found in %r" % self)

    def getOmeroUser(self):
        return self.get('omero.user')

    def setOmeroUser(self, value=None):
        if value is not None:
            self.set('omero.user', value)
        else:
            try:
                del self['omero.user']
            except KeyError:
                logger.debug("Key 'omero.user' not found in %r" % self)

    def getOmeroShare(self):
        return self.get('omero.share')

    def setOmeroShare(self, value=None):
        if value is not None:
            self.set('omero.share', value)
        else:
            try:
                del self['omero.share']
            except KeyError:  # pragma: no cover
                logger.debug("Key 'omero.share' not found in %r" % self)

    def _testItem(self, item):
        if item is not None and not isinstance(item, bool) and \
            (isinstance(item, basestring) or
             isinstance(item, int) or
             isinstance(item, long) or
             isinstance(item, float)):
            return True
        return False


def toBoolean(val):
    """
    Get the boolean value of the provided input.

        If the value is a boolean return the value.
        Otherwise check to see if the value is in
        ["true", "yes", "y", "t", "1"]
        and returns True if value is in the list
    """

    if val is True or val is False:
        return val

    trueItems = ["true", "yes", "y", "t", "1", "on"]

    return str(val).strip().lower() in trueItems
