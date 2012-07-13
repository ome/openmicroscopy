#!/usr/bin/env python

"""
   gateway tests - Object Wrappers

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
from omero.gateway.utils import ServiceOptsDict

class ServiceOptsDictTest(unittest.TestCase):
    
    def setUp (self):
        super(ServiceOptsDictTest, self).setUp()
    
    def test_constructor(self):
        self.assertEqual(ServiceOptsDict(), {})
        self.assert_(ServiceOptsDict() is not {})
        self.assert_(ServiceOptsDict() is not dict())
        
        d = {"omero.group":-1}
        d = ServiceOptsDict(d)
        
        resd = d.get("omero.group")
        self.assert_(isinstance(resd, str))
        self.assertEqual(d.get("omero.group"), str(d["omero.group"]))
        
        d = ServiceOptsDict(x=1,y=2)
        self.assertEqual(d.get("x"), "1")
        self.assertEqual(d.get("y"), "2")
        
    def test_keys(self):
        d = ServiceOptsDict()
        self.assertEqual(d.keys(), [])
        
        d = ServiceOptsDict({"omero.group":-1})
        k = d.keys()
        self.assert_(d.has_key('omero.group'))
        
        self.assertRaises(TypeError, d.keys, None)
    
    def test_values(self):
        d = ServiceOptsDict()
        self.assertEqual(d.values(), [])
        
        d = ServiceOptsDict({"omero.group":-1})
        self.assertEqual(d.values(), ["-1"])
        
        self.assertRaises(TypeError, d.values, None)
        
        d = ServiceOptsDict({"a":None, "b":True, "c":"foo", "d":1, "e":1.45, "f":[], "g":{}})
        self.assertEqual(d.values(), ['foo', '1.45', '1'])
    
    def test_items(self):
        d = ServiceOptsDict()
        self.assertEqual(d.items(), [])

        d = ServiceOptsDict({"omero.group":-1})
        self.assertEqual(d.items(), [("omero.group", "-1")])

        self.assertRaises(TypeError, d.items, None)
    
    def test_has_key(self):
        d = ServiceOptsDict()
        self.assert_(not d.has_key('omero'))
        
        d = ServiceOptsDict({"omero.group":-1, "omero.user": 1})
        k = d.keys()
        k.sort()
        self.assertEqual(k, ['omero.group', 'omero.user'])
        
        self.assertRaises(TypeError, d.has_key)
    
    def test_contains(self):
        d = ServiceOptsDict()
        self.assert_(not ('omero.group' in d))
        self.assert_('omero.group' not in d)
        
        d = ServiceOptsDict({"omero.group":-1, "omero.user": 1})
        self.assert_('omero.group' in d)
        self.assert_('omero.user' in d)
        self.assert_('omero.share' not in d)
    
    def test_len(self):
        d = ServiceOptsDict()
        self.assertEqual(len(d), 0)
        
        d = ServiceOptsDict({"omero.group":-1, "omero.user": 1})
        self.assertEqual(len(d), 2)
    
    def test_getitem(self):
        d = ServiceOptsDict({"omero.group":-1, "omero.user": 1})
        self.assertEqual(d["omero.group"], "-1")
        self.assertEqual(d["omero.user"], "1")
        
        d["omero.share"] = 2
        d["foo"] = "bar"
        self.assertEqual(d["omero.share"], "2")
        self.assertEqual(d["foo"], "bar")
        
        del d["omero.user"]
        
        self.assertEqual(d, {"omero.group": "-1", 'foo': 'bar', "omero.share": "2"})
        
        self.assertRaises(TypeError, d.__getitem__)
        
        self.assertEqual(d.get("omero.user"), None)
        self.assertEqual(d.get("omero.user", "5"), "5")
    
    def test_setitem(self):
        
        # string
        d = ServiceOptsDict({"omero.share": "2","omero.user": "1"})
        d["omero.group"] = "-1"
        
        # unicode
        d = ServiceOptsDict({"omero.share": u'2',"omero.user": u'1'})
        d["omero.group"] = u'-1'
        
        # int
        d = ServiceOptsDict({"omero.share": 1,"omero.user": 2})
        d["omero.group"] = -1
        
        # long
        
        import sys
        maxint = sys.maxint
        d = ServiceOptsDict({"omero.group": (maxint+1)})
        d["omero.user"] = (maxint+1)
        
        try:
            d = ServiceOptsDict({"omero.group": True})
            d["omero.user"] = True
        except:
            pass
        else:
            self.fail("AttributeError: ServiceOptsDict argument must be a string, unicode or numeric type")
        
        try:
            d = ServiceOptsDict({"omero.group": []})
            d["omero.user"] = []
        except:
            pass
        else:
            self.fail("AttributeError: ServiceOptsDict argument must be a string, unicode or numeric type")
        
        try:
            d = ServiceOptsDict({"omero.group": {}})
            d["omero.user"] = {}
        except:
            pass
        else:
            self.fail("AttributeError: ServiceOptsDict argument must be a string, unicode or numeric type")
    
    def test_clear(self):
        d = ServiceOptsDict({"omero.group":-1, "omero.user": 1, "omero.share": 2})
        d.clear()
        self.assertEqual(d, {})
        
        self.assertRaises(TypeError, d.clear, None)
    
    def test_repr(self):
        d = ServiceOptsDict()
        self.assertEqual(repr(d), '<ServiceOptsDict: {}>')
        d["omero.group"] = -1
        self.assertEqual(repr(d), "<ServiceOptsDict: {'omero.group': '-1'}>")
    
    def test_copy(self):
        
        def getHash(obj):
            return hex(id(obj))
        
        d = ServiceOptsDict({"omero.group":-1, "omero.user": 1, "omero.share": 2})
        self.assertEqual(d.copy(), d)
        self.assertNotEqual(getHash(d.copy()), getHash(d))
        self.assertEqual(ServiceOptsDict().copy(), ServiceOptsDict())
        self.assertEqual(getHash(ServiceOptsDict().copy()), getHash(ServiceOptsDict()))
        self.assertRaises(TypeError, d.copy, None)
    
    def test_setter_an_getter(self):
        group = -1
        user = 1
        share = 2
        
        d = ServiceOptsDict()
        d.set("omero.group", group)
        self.assertEqual(d.get("omero.group"), d.getOmeroGroup())
        
        d.setOmeroGroup(group)
        self.assertEqual(d.get("omero.group"), d.getOmeroGroup())
        
        d.set("omero.user", user)
        self.assertEqual(d.get("omero.user"), d.getOmeroUser())
        
        d.setOmeroUser(user)
        self.assertEqual(d.get("omero.user"), d.getOmeroUser())
        
        d.set("omero.share", share)
        self.assertEqual(d.get("omero.share"), d.getOmeroShare())
        
        d.setOmeroShare(share)
        self.assertEqual(d.get("omero.share"), d.getOmeroShare())


if __name__ == '__main__':
    unittest.main()
