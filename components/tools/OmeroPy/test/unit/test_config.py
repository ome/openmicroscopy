#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
/*
 *   $Id$
 *
 *   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
"""

import os
import pytest
from omero.config import ConfigXml, xml
from omero.util.temp_files import create_path
from omero_ext import portalocker

from xml.etree.ElementTree import XML, Element, SubElement, tostring


def pair(x):
    """
    Takes an XML element and returns a tuple of
    the form either '(id, x)' or '(name, x)'.
    """
    key = x.get("id")
    if not key:
        key = x.get("name")
    return (key, x)


def assertXml(elementA, elementB):
    """
    Recursively descend through two XML elements
    comparing them for equality.
    """

    assert elementA.tag == elementB.tag

    A_attr = elementA.attrib
    B_attr = elementB.attrib
    assert len(A_attr) == len(B_attr)
    for k in A_attr:
        assert A_attr[k] == B_attr[k], cf(elementA, elementB)
    for k in B_attr:
        assert A_attr[k] == B_attr[k], cf(elementA, elementB)

    A_kids = dict([pair(x) for x in elementA.getchildren()])
    B_kids = dict([pair(x) for x in elementB.getchildren()])
    for k in A_attr:
        assertXml(A_kids[k], B_kids[k])
    for k in B_attr:
        assertXml(A_kids[k], B_kids[k])


def cf(elemA, elemB):
    """
    Print out a visual comparison of two
    XML elements.
    """
    return """
    %s

    === v ===

    %s""" % (totext(elemA), totext(elemB))


def initial(default="default"):
    """
    Produce a basic icegrid XML element
    """
    icegrid = Element("icegrid")
    properties = SubElement(icegrid, "properties", id=ConfigXml.INTERNAL)
    SubElement(properties, "property", name=ConfigXml.DEFAULT,
               value=default)
    SubElement(properties, "property", name=ConfigXml.KEY,
               value=ConfigXml.VERSION)
    properties = SubElement(icegrid, "properties", id=default)
    SubElement(properties, "property", name=ConfigXml.KEY,
               value=ConfigXml.VERSION)
    return icegrid


def totext(elem):
    """
    Use minidom to generate a string representation
    of an XML element.
    """
    string = tostring(elem, 'utf-8')
    return xml.dom.minidom.parseString(string).toxml()


class TestConfig(object):

    def testBasic(self):
        p = create_path()
        config = ConfigXml(filename=str(p))
        config.close()
        assertXml(initial(), XML(p.text()))

    def testWithEnv(self):
        p = create_path()
        config = ConfigXml(filename=str(p), env_config="FOO")
        config.close()
        assertXml(initial("FOO"), XML(p.text()))

    def testWithEnvThenWithoutEnv(self):
        """
        This test shows that if you create the config using an env
        setting, then without, the __ACTIVE__ block should reflect
        "default" and not the intermediate "env" setting.
        """
        def get_profile_name(p):
            """
            Takes a path object to the config xml
            """
            xml = XML(p.text())
            props = xml.findall("./properties")
            for x in props:
                id = x.attrib["id"]
                if id == "__ACTIVE__":
                    for y in x.getchildren():
                        if y.attrib["name"] == "omero.config.profile":
                            return y.attrib["value"]

        p = create_path()

        current = os.environ.get("OMERO_CONFIG", "default")
        assert current != "FOO"  # Just in case.

        config = ConfigXml(filename=str(p))
        config.close()
        assert current == get_profile_name(p)

        config = ConfigXml(filename=str(p), env_config="FOO")
        config.close()
        assert "FOO" == get_profile_name(p)

        # Still foo
        config = ConfigXml(filename=str(p))
        config.close()
        assert "FOO" == get_profile_name(p)

        # Re-setting with os.environ won't work
        try:
            old = os.environ.get("OMERO_CONFIG", None)
            os.environ["OMERO_CONFIG"] = "ABC"
            config = ConfigXml(filename=str(p))
            config.close()
            assert "FOO" == get_profile_name(p)
        finally:
            if old is None:
                del os.environ["OMERO_CONFIG"]
            else:
                os.environ["OMERO_CONFIG"] = old

        # But we can reset it with env_config
        config = ConfigXml(filename=str(p), env_config="ABC")
        config.close()
        assert "ABC" == get_profile_name(p)

        # or manually. ticket:7343
        config = ConfigXml(filename=str(p))
        config.default("XYZ")
        config.close()
        assert "XYZ" == get_profile_name(p)

    def testAsDict(self):
        p = create_path()
        config = ConfigXml(filename=str(p), env_config="DICT")
        config["omero.data.dir"] = "HOME"
        config.close()
        i = initial("DICT")
        _ = SubElement(i[0][0], "property", name="omero.data.dir",
                       value="HOME")
        _ = SubElement(i, "properties", id="DICT")
        _ = SubElement(_, "property", name="omero.data.dir", value="HOME")
        assertXml(i, XML(p.text()))

    def testLocking(self):
        p = create_path()
        config1 = ConfigXml(filename=str(p))
        try:
            ConfigXml(filename=str(p))
            assert False, "No exception"
        except portalocker.LockException:
            pass
        config1.close()

    def testNewVersioning(self):
        """
        All property blocks should always have a version set.
        """
        p = create_path()
        config = ConfigXml(filename=str(p))
        m = config.as_map()
        for k, v in m.items():
            assert "5.1.0" == v

    def testOldVersionDetected(self):
        p = create_path()
        config = ConfigXml(filename=str(p))
        X = config.XML
        O = SubElement(X, "properties", {"id": "old"})
        SubElement(O, "property", {"omero.ldap.keystore": "/Foo"})
        config.close()

        try:
            config = ConfigXml(filename=str(p))
            assert False, "Should throw"
        except:
            pass

    def test421Upgrade(self):
        """
        When upgraded 4.2.0 properties to 4.2.1,
        ${dn} items in omero.ldap.* properties are
        changed to @{dn}
        """
        p = create_path()

        # How config was written in 4.2.0
        XML = Element("icegrid")
        active = SubElement(XML, "properties", id="__ACTIVE__")
        default = SubElement(XML, "properties", id="default")
        for properties in (active, default):
            SubElement(
                properties, "property", name="omero.config.version",
                value="4.2.0")
            SubElement(
                properties, "property", name="omero.ldap.new_user_group",
                value="member=${dn}")
            SubElement(
                properties, "property", name="omero.ldap.new_user_group_2",
                value="member=$${omero.dollar}{dn}")
        string = tostring(XML, 'utf-8')
        txt = xml.dom.minidom.parseString(string).toprettyxml("  ", "\n", None)
        p.write_text(txt)

        config = ConfigXml(filename=str(p), env_config="default")
        try:
            m = config.as_map()
            assert "member=@{dn}" == m["omero.ldap.new_user_group"]
            assert "member=@{dn}" == m["omero.ldap.new_user_group_2"]
        finally:
            config.close()

    def testSettings510Upgrade(self):
        """
        When upgraded 5.0.x properties to 5.1.0 or later,
        if omero.web.ui.top_links is set, we need to prepend
        'Data', 'History' and 'Help' links
        """

        beforeUpdate = '[["Figure", "figure_index"]]'
        afterUpdate = '[["Data", "webindex", ' \
            '{"title": "Browse Data via Projects, Tags etc"}], ' \
            '["History", "history", ' \
            '{"title": "History"}], ' \
            '["Help", "http://help.openmicroscopy.org/", ' \
            '{"target": "new", "title": ' \
            '"Open OMERO user guide in a new tab"}], ' \
            '["Figure", "figure_index"]]'
        p = create_path()

        XML = Element("icegrid")
        active = SubElement(XML, "properties", id="__ACTIVE__")
        default = SubElement(XML, "properties", id="default")
        for properties in (active, default):
            # Include a property to indicate version is post-4.2.0
            SubElement(
                properties, "property", name="omero.config.version",
                value="4.2.1")
            SubElement(
                properties, "property", name="omero.web.ui.top_links",
                value=beforeUpdate)
        string = tostring(XML, 'utf-8')
        txt = xml.dom.minidom.parseString(string).toprettyxml("  ", "\n", None)
        p.write_text(txt)

        config = ConfigXml(filename=str(p), env_config="default")
        try:
            m = config.as_map()
            assert m["omero.web.ui.top_links"] == afterUpdate
        finally:
            config.close()

        # After config.close() calls config.save() new version should be 5.1.0
        config = ConfigXml(filename=str(p), env_config="default")
        try:
            # Check version has been updated
            assert config.version() == "5.1.0"
            m = config.as_map()
            # And that top_links has not been modified further
            assert m["omero.web.ui.top_links"] == afterUpdate
        finally:
            config.close()


    def testReadOnlyConfigSimple(self):
        p = create_path()
        p.chmod(0444)  # r--r--r--
        config = ConfigXml(filename=str(p), env_config=None)  # Must be None
        config.close()  # Shouldn't save

    def testReadOnlyConfigPassesOnExplicitReadOnly(self):
        p = create_path()
        p.chmod(0444)  # r--r--r--
        ConfigXml(filename=str(p),
                  env_config="default",
                  read_only=True).close()

    def testReadOnlyConfigFailsOnEnv1(self):
        p = create_path()
        p.chmod(0444)  # r--r--r--
        pytest.raises(Exception, ConfigXml, filename=str(p),
                      env_config="default")

    def testReadOnlyConfigFailsOnEnv2(self):
        old = os.environ.get("OMERO_CONFIG")
        os.environ["OMERO_CONFIG"] = "default"
        try:
            p = create_path()
            p.chmod(0444)  # r--r--r--
            pytest.raises(Exception, ConfigXml, filename=str(p))
        finally:
            if old is None:
                del os.environ["OMERO_CONFIG"]
            else:
                os.environ["OMERO_CONFIG"] = old
