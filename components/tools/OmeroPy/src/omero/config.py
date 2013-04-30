#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
::

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

"""
Module which parses an icegrid XML file for configuration settings.

see ticket:800
see ticket:2213 - Replacing Java Preferences API
"""

import re
import os
import path
import time
import logging
import portalocker

import xml.dom.minidom

try:
    from xml.etree.ElementTree import XML, Element, SubElement, Comment, ElementTree, tostring
except ImportError:
    from elementtree.ElementTree import XML, Element, SubElement, Comment, ElementTree, tostring


class Environment(object):
    """
    Object to record all the various locations
    that the active configuration can come from.
    """

    def __init__(self, user_specified = None):
        self.fallback = "default"
        self.user_specified = user_specified
        self.from_os_environ = os.environ.get("OMERO_CONFIG", None)

    def is_non_default(self):
        if self.user_specified is not None:
            return self.user_specified
        elif self.from_os_environ is not None:
            return self.from_os_environ
        return None

    def set_by_user(self, value):
        self.user_specified = value

    def internal_value(self, config):
        props = config.props_to_dict(config.internal())
        return props.get(config.DEFAULT, self.fallback)

    def for_save(self, config):
        """
        In some cases the environment chosen
        should not be persisted.
        """
        if self.user_specified:
            return self.user_specified
        else:
            return self.internal_value(config)

    def for_default(self, config):
        if self.user_specified:
            return self.user_specified
        elif self.from_os_environ:
            return self.from_os_environ
        else:
            return self.internal_value(config)


class ConfigXml(object):
    """
    dict-like wrapper around the config.xml file usually stored
    in etc/grid. For a copy of the dict, use "as_map"
    """
    KEY = "omero.config.version"
    VERSION = "4.2.1"
    INTERNAL = "__ACTIVE__"
    DEFAULT = "omero.config.profile"
    IGNORE = (KEY, DEFAULT)

    def __init__(self, filename, env_config = None, exclusive = True):
        self.logger = logging.getLogger(self.__class__.__name__)    #: Logs to the class name
        self.XML = None                                             #: Parsed XML Element
        self.filename = filename                                    #: Path to the file to be read and written
        self.env_config = Environment(env_config)                   #: Environment override
        self.exclusive = exclusive                                  #: Whether or not an exclusive lock should be acquired
        self.save_on_close = True

        try:
            # Try to open the file for modification
            # If this fails, then the file is readonly
            self.source = open(filename, "a+")                      #: Open file handle
            self.lock = self._open_lock()                           #: Open file handle for lock
        except IOError:
            self.logger.debug("open('%s', 'a+') failed" % filename)
            self.lock = None
            self.exclusive = False
            self.save_on_close = False

            # Before we open the file read-only we need to check
            # that no other configuration has been requested because
            # it will not be possible to modify the __ACTIVE__ setting
            # once it's read-only
            val = self.env_config.is_non_default()
            if val is not None:
                raise Exception("Non-default OMERO_CONFIG on read-only: %s" % val)

            self.source = open(filename, "r")                       #: Open file handle read-only

        if self.exclusive:  # must be "a+"
            try:
                portalocker.lock(self.lock, portalocker.LOCK_NB|portalocker.LOCK_EX)
            except portalocker.LockException, le:
                self.lock = None # Prevent deleting of the file
                self.close()
                raise

        self.source.seek(0)
        text = self.source.read()

        if text:
            self.XML = XML(text)
            try:
                self.version_check()
            except:
                self.close()
                raise

        # Nothing defined, so create a new tree
        if self.XML is None:
            default = self.default()
            self.XML = Element("icegrid")
            properties = SubElement(self.XML, "properties", id=self.INTERNAL)
            _ = SubElement(properties, "property", name=self.DEFAULT, value=default)
            _ = SubElement(properties, "property", name=self.KEY, value=self.VERSION)
            properties = SubElement(self.XML, "properties", id=default)
            _ = SubElement(properties, "property", name=self.KEY, value=self.VERSION)

    def _open_lock(self):
        return open("%s.lock" % self.filename, "a+")

    def _close_lock(self):
        if self.lock is not None:
            self.lock.close()
            self.lock = None
            try:
                os.remove("%s.lock" % self.filename)
            except:
                # On windows a WindowsError 32 can happen (file opened by another process), ignoring
                self.logger.error("Failed to removed lock file, ignoring", exc_info=True)
                pass

    def version(self, id = None):
        if id is None:
            id = self.default()
        properties = self.properties(id)
        if properties is not None:
            for x in properties.getchildren():
                if x.get("name") == self.KEY:
                    return x.get("value")

    def version_check(self):
        for k, v in self.properties(None, True):
            version = self.version(k)
            if version != self.VERSION:
                self.version_fix(v, version)

    def version_fix(self, props, version):
        """
        Currently we are assuming that all blocks without a 4.2.0 version
        are bogus. The configuration script when it generates an initial
        config.xml will use prefs.class to parse the existing values and
        immediately do the upgrade.
        """
        if version == "4.2.0":
            # http://trac.openmicroscopy.org.uk/ome/ticket/2613
            # Remove any reference to the ${omero.dollar} workaround
            # then map anything of the form: ${...} to @{...}
            if props:
                for x in props.getchildren():
                    if x.get("name", "").startswith("omero.ldap"):
                        orig = x.get("value", "")
                        val = orig.replace("${omero.dollar}", "")
                        val = val.replace("${", "@{")
                        x.set("value", val)
                        self.logger.info("Upgraded 4.2.0 property:  %s => %s", orig, val)
        else:
            raise Exception("Version mismatch: %s has %s" % (props.get("id"), version))

    def internal(self):
        return self.properties(self.INTERNAL)

    def properties(self, id = None, filter_internal = False):

        if self.XML is None:
            return None

        props = self.XML.findall("./properties")
        if id is None:
            rv = list()
            for x in props:
                id = x.attrib["id"]
                if filter_internal:
                    if id == self.INTERNAL:
                        continue
                rv.append((id, x))
            return rv
        for p in props:
            if "id" in p.attrib and p.attrib["id"] == id:
                return p

    def remove(self, id = None):
        if id is None:
            id = self.default()
        properties = self.properties(id)
        if properties is None:
            raise KeyError("No such configuration: %s" % id)
        self.XML.remove(properties)

    def default(self, value = None):
        if value:
            self.env_config.set_by_user(value)

        return self.env_config.for_default(self)

    def dump(self):
        prop_list = self.properties()
        for id, p in prop_list:
            props = self.props_to_dict(p)
            print "# ===> %s <===" % id
            print self.dict_to_text(props)

    def save(self):
        """
        Creates a fresh <icegrid> block (removing any unwanted
        intra-element whitespace) and overwrites the file on disk.
        """
        icegrid = Element("icegrid")
        comment = Comment("\n".join(["\n",
        "\tThis file was generated at %s by the OmeroConfig system.",
        "\tDo not edit directly but see bin/omero config for details.",
        "\tThis file may be included into your IceGrid application.",
        "\n"]) % time.ctime())
        icegrid.append(comment)
        # First step is to add a new self.INTERNAL block to it
        # which has self.DEFAULT set to the current default,
        # and then copies all the values from that profile.
        default = self.env_config.for_save(self)
        internal = SubElement(icegrid, "properties", id=self.INTERNAL)
        SubElement(internal, "property", name=self.DEFAULT, value=default)
        SubElement(internal, "property", name=self.KEY, value=self.VERSION)
        to_copy = self.properties(default)
        if to_copy is not None:
            for x in to_copy.getchildren():
                if x.get("name") != self.DEFAULT and x.get("name") != self.KEY:
                    SubElement(internal, "property", x.attrib)
        else:
            # Doesn't exist, create it
            properties = SubElement(icegrid, "properties", id=default)
            SubElement(properties, "property", name=self.KEY, value=self.VERSION)
        # Now we simply reproduce all the other blocks
        prop_list = self.properties(None, True)
        for k, p in prop_list:
            self.clear_text(p)
            icegrid.append(p)
        self.source.seek(0)
        self.source.truncate()
        self.source.write(self.element_to_xml(icegrid))
        self.source.flush()

    def close(self):
        try:
            # If we didn't get an XML instance, then something has gone wrong
            # and we should exit. Similarly, if save_on_close is False, then we
            # couldn't open the file "a+"
            if self.XML is not None and self.save_on_close:
                self.save()
                self.XML = None
        finally:
            try:
                if self.source is not None:
                    self.source.close()
                    self.source = None
            finally:
                self._close_lock()

    def props_to_dict(self, c):

        if c is None:
            return {}

        rv = dict()
        props = c.findall("./property")
        for p in props:
            if "name" in p.attrib:
                rv[p.attrib["name"]] = p.attrib.get("value", "")
        return rv

    def dict_to_text(self, parsed = None):

        if parsed is None:
            return

        rv = ""
        for k, v in parsed.items():
            rv += "%s=%s" % (k, v)
        return rv

    def element_to_xml(self, elem):
        string = tostring(elem, 'utf-8')
        return xml.dom.minidom.parseString(string).toprettyxml("  ", "\n", None)

    def clear_text(self, p):
        """
        To prevent the accumulation of text outside of elements (including whitespace)
        we walk the given element and remove tail from it and it's children.
        """
        p.tail = ""
        p.text = ""
        for p2 in p.getchildren():
            self.clear_text(p2)

    #
    # Map interface on the default properties element
    #
    def as_map(self):
        return self.props_to_dict(self.properties(self.default()))

    def keys(self):
        return self.as_map().keys()

    def __getitem__(self, key):
        return self.props_to_dict(self.properties(self.default()))[key]

    def __setitem__(self, key, value):
        default = self.default()
        props = self.properties(default)

        if props == None:
            props = SubElement(self.XML, "properties", {"id":default})
            SubElement(props, "property", name=self.KEY, value=self.VERSION)

        for x in props.findall("./property"):
            if x.attrib["name"] == key:
                x.attrib["value"] = value
                return
        SubElement(props, "property", {"name":key, "value":value})

    def __delitem__(self, key):
        default = self.default()
        props = self.properties(default)
        to_remove = []
        for p in props.getchildren():
            if p.get("name") == key:
                to_remove.append(p)
        for x in to_remove:
            props.remove(x)

