"""
/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
"""

import os
import logging
import unittest
import portalocker
from omero.config import *
from omero.util.temp_files import create_path

try:
    from xml.etree.ElementTree import XML, Element, SubElement, Comment, ElementTree, tostring, dump
except ImportError:
    from elementtree.ElementTree import XML, Element, SubElement, Comment, ElementTree, tostring, dump


class TestConfig(unittest.TestCase):

    def pair(self, x):
        key = x.get("id")
        if not key:
            key = x.get("name")
        return (key, x)

    def assertXml(self, elementA, elementB):

        self.assertEquals(elementA.tag, elementB.tag)

        A_attr = elementA.attrib
        B_attr = elementB.attrib
        self.assertEquals(len(A_attr), len(B_attr))
        for k in A_attr:
            self.assertEquals(A_attr[k], B_attr[k], self.cf(elementA, elementB))
        for k in B_attr:
            self.assertEquals(A_attr[k], B_attr[k], self.cf(elementA, elementB))

        A_kids = dict([self.pair(x) for x in elementA.getchildren()])
        B_kids = dict([self.pair(x) for x in elementB.getchildren()])
        for k in A_attr:
            self.assertXml(A_kids[k], B_kids[k])
        for k in B_attr:
            self.assertXml(A_kids[k], B_kids[k])

    def cf(self, elemA, elemB):
        return """
        %s

        === v ===

        %s""" % (self.totext(elemA), self.totext(elemB))

    def initial(self, default = "default"):
        icegrid = Element("icegrid")
        properties = SubElement(icegrid, "properties", id=ConfigXml.INTERNAL)
        _ = SubElement(properties, "property", name=ConfigXml.DEFAULT, value=default)
        _ = SubElement(properties, "property", name=ConfigXml.KEY, value=ConfigXml.VERSION)
        properties = SubElement(icegrid, "properties", id=default)
        _ = SubElement(properties, "property", name=ConfigXml.KEY, value=ConfigXml.VERSION)
        return icegrid

    def totext(self, elem):
        string = tostring(elem, 'utf-8')
        return xml.dom.minidom.parseString(string).toxml()

    def testBasic(self):
        p = create_path()
        config = ConfigXml(filename=str(p))
        config.close()
        self.assertXml(self.initial(), XML(p.text()))

    def testWithEnv(self):
        p = create_path()
        config = ConfigXml(filename=str(p), env_config="FOO")
        config.close()
        self.assertXml(self.initial("FOO"), XML(p.text()))

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
        self.assertTrue(current != "FOO") # Just in case.

        config = ConfigXml(filename=str(p))
        config.close()
        self.assertEquals(current, get_profile_name(p))

        config = ConfigXml(filename=str(p), env_config="FOO")
        config.close()
        self.assertEquals("FOO", get_profile_name(p))

        # Still foo
        config = ConfigXml(filename=str(p))
        config.close()
        self.assertEquals("FOO", get_profile_name(p))

        # Re-setting with os.environ won't work
        try:
            old = os.environ.get("OMERO_CONFIG", None)
            os.environ["OMERO_CONFIG"] = "ABC"
            config = ConfigXml(filename=str(p))
            config.close()
            self.assertEquals("FOO", get_profile_name(p))
        finally:
            if old is None:
                del os.environ["OMERO_CONFIG"]
            else:
                os.environ["OMERO_CONFIG"] = old

        # But we can reset it with env_config
        config = ConfigXml(filename=str(p), env_config="ABC")
        config.close()
        self.assertEquals("ABC", get_profile_name(p))

        # or manually. ticket:7343
        config = ConfigXml(filename=str(p))
        config.default("XYZ")
        config.close()
        self.assertEquals("XYZ", get_profile_name(p))

    def testAsDict(self):
        p = create_path()
        config = ConfigXml(filename=str(p), env_config="DICT")
        config["omero.data.dir"] = "HOME"
        config.close()
        initial = self.initial("DICT")
        _ = SubElement(initial[0][0], "property", name="omero.data.dir", value="HOME")
        _ = SubElement(initial, "properties", id="DICT")
        _ = SubElement(_, "property", name="omero.data.dir", value="HOME")
        self.assertXml(initial, XML(p.text()))

    def testLocking(self):
        p = create_path()
        config1 = ConfigXml(filename=str(p))
        try:
            config2 = ConfigXml(filename=str(p))
            self.fail("No exception")
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
            self.assertEquals("4.2.1", v)

    def testOldVersionDetected(self):
        p = create_path()
        config = ConfigXml(filename=str(p))
        X = config.XML
        O = SubElement(X, "properties", {"id":"old"})
        SubElement(O, "property", {"omero.ldap.keystore":"/Foo"})
        config.close()

        try:
            config = ConfigXml(filename=str(p))
            self.fail("Should throw")
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
            SubElement(properties, "property", name="omero.config.version", value="4.2.0")
            SubElement(properties, "property", name="omero.ldap.new_user_group", value="member=${dn}")
            SubElement(properties, "property", name="omero.ldap.new_user_group_2", value="member=$${omero.dollar}{dn}")
        string = tostring(XML, 'utf-8')
        txt = xml.dom.minidom.parseString(string).toprettyxml("  ", "\n", None)
        p.write_text(txt)

        config = ConfigXml(filename=str(p), env_config="default")
        try:
            m = config.as_map()
            self.assertEquals("member=@{dn}", m["omero.ldap.new_user_group"])
            self.assertEquals("member=@{dn}", m["omero.ldap.new_user_group_2"])
        finally:
            config.close()

if __name__ == '__main__':
    logging.basicConfig()
    unittest.main()
