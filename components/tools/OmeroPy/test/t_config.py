"""
/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
"""

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
        for k, v in config:
            self.assertEquals("4.2.0", config.version(k))

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


if __name__ == '__main__':
    logging.basicConfig()
    unittest.main()
