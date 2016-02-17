#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Object Wrappers

   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.gateway.utils import ServiceOptsDict
from omero.gateway.utils import toBoolean
from omero.gateway.utils import propertiesToDict
import pytest


class TestServiceOptsDict (object):

    def test_constructor(self):
        assert ServiceOptsDict() == {}
        assert ServiceOptsDict() is not {}
        assert ServiceOptsDict() is not dict()

        d = {"omero.group": -1}
        d = ServiceOptsDict(d)

        resd = d.get("omero.group")
        assert isinstance(resd, str)
        assert d.get("omero.group") == str(d["omero.group"])

        d = ServiceOptsDict(x=1, y=2)
        assert d.get("x") == "1"
        assert d.get("y") == "2"

        # ServiceOptsDict can be passed initializing data, but it needs to be
        # a dict
        pytest.raises(AttributeError, ServiceOptsDict,
                      kwargs={"data": "data"})
        ServiceOptsDict(data={'option': 'a'})

    def test_keys(self):
        d = ServiceOptsDict()
        assert d.keys() == []

        d = ServiceOptsDict({"omero.group": -1})
        assert 'omero.group' in d

        pytest.raises(TypeError, d.keys, None)

    def test_values(self):
        d = ServiceOptsDict()
        assert d.values() == []

        d = ServiceOptsDict({"omero.group": -1})
        assert d.values() == ["-1"]

        pytest.raises(TypeError, d.values, None)

        d = ServiceOptsDict({
            "a": None, "b": True, "c": "foo", "d": 1, "e": 1.45, "f": [],
            "g": {}})
        assert d.values() == ['foo', '1.45', '1']

    def test_items(self):
        d = ServiceOptsDict()
        assert d.items() == []

        d = ServiceOptsDict({"omero.group": -1})
        assert d.items() == [("omero.group", "-1")]

        pytest.raises(TypeError, d.items, None)

    def test_has_key(self):
        d = ServiceOptsDict()
        assert 'omero' not in d

        d = ServiceOptsDict({"omero.group": -1, "omero.user": 1})
        k = d.keys()
        k.sort()
        assert k == ['omero.group', 'omero.user']

        pytest.raises(TypeError, d.has_key)

    def test_contains(self):
        d = ServiceOptsDict()
        assert not ('omero.group' in d)
        assert 'omero.group' not in d

        d = ServiceOptsDict({"omero.group": -1, "omero.user": 1})
        assert 'omero.group' in d
        assert 'omero.user' in d
        assert 'omero.share' not in d

    def test_len(self):
        d = ServiceOptsDict()
        assert len(d) == 0

        d = ServiceOptsDict({"omero.group": -1, "omero.user": 1})
        assert len(d) == 2

    def test_getitem(self):
        d = ServiceOptsDict({"omero.group": -1, "omero.user": 1})
        assert d["omero.group"] == "-1"
        assert d["omero.user"] == "1"

        d["omero.share"] = 2
        d["foo"] = "bar"
        assert d["omero.share"] == "2"
        assert d["foo"] == "bar"

        del d["omero.user"]

        assert d == {"omero.group": "-1", 'foo': 'bar', "omero.share": "2"}

        pytest.raises(TypeError, d.__getitem__)

        assert d.get("omero.user") is None
        assert d.get("omero.user", "5"), "5"

    def test_setitem(self):

        # string
        d = ServiceOptsDict({"omero.share": "2", "omero.user": "1"})
        d["omero.group"] = "-1"

        # unicode
        d = ServiceOptsDict({"omero.share": u'2', "omero.user": u'1'})
        d["omero.group"] = u'-1'

        # int
        d = ServiceOptsDict({"omero.share": 1, "omero.user": 2})
        d["omero.group"] = -1

        # long
        import sys
        maxint = sys.maxint
        d = ServiceOptsDict({"omero.group": (maxint + 1)})
        d["omero.user"] = (maxint + 1)

        # Setter passed None as value remove from internal dict
        d = ServiceOptsDict({"omero.share": "2", "omero.user": "1"})
        assert d.get("omero.share") is not None
        d.setOmeroShare()
        assert d.get("omero.share") is None
        assert d.get("omero.user") is not None
        d.setOmeroUser()
        assert d.get("omero.user") is None

        try:
            d = ServiceOptsDict({"omero.group": True})
            d["omero.user"] = True
        except:
            pass
        else:
            self.fail("AttributeError: ServiceOptsDict argument must be a"
                      " string, unicode or numeric type")

        try:
            d = ServiceOptsDict({"omero.group": []})
            d["omero.user"] = []
        except:
            pass
        else:
            self.fail("AttributeError: ServiceOptsDict argument must be a"
                      " string, unicode or numeric type")

        try:
            d = ServiceOptsDict({"omero.group": {}})
            d["omero.user"] = {}
        except:
            pass
        else:
            self.fail("AttributeError: ServiceOptsDict argument must be a"
                      " string, unicode or numeric type")

    def test_clear(self):
        d = ServiceOptsDict(
            {"omero.group": -1, "omero.user": 1, "omero.share": 2})
        d.clear()
        assert d == {}

        pytest.raises(TypeError, d.clear, None)

    def test_repr(self):
        d = ServiceOptsDict()
        assert repr(d) == '<ServiceOptsDict: {}>'
        d["omero.group"] = -1
        assert repr(d) == "<ServiceOptsDict: {'omero.group': '-1'}>"

    def test_copy(self):

        def getHash(obj):
            return hex(id(obj))

        d = ServiceOptsDict(
            {"omero.group": -1, "omero.user": 1, "omero.share": 2})
        assert d.copy() == d
        assert getHash(d.copy()) != getHash(d)
        assert ServiceOptsDict().copy() == ServiceOptsDict()
        assert getHash(ServiceOptsDict().copy()) != \
            getHash(ServiceOptsDict())
        pytest.raises(TypeError, d.copy, None)

    def test_setter_an_getter(self):
        group = -1
        user = 1
        share = 2

        d = ServiceOptsDict()
        d.set("omero.group", group)
        assert d.get("omero.group") == d.getOmeroGroup()

        d.setOmeroGroup(group)
        assert d.get("omero.group") == d.getOmeroGroup()

        d.set("omero.user", user)
        assert d.get("omero.user") == d.getOmeroUser()

        d.setOmeroUser(user)
        assert d.get("omero.user") == d.getOmeroUser()

        d.set("omero.share", share)
        assert d.get("omero.share") == d.getOmeroShare()

        d.setOmeroShare(share)
        assert d.get("omero.share") == d.getOmeroShare()


class TestHelpers (object):

    @pytest.mark.parametrize('true_val',
                             [True, "true", "yes", "y", "t", "1", "on"])
    def test_toBoolean_true(self, true_val):
        assert toBoolean(true_val)

    @pytest.mark.parametrize(
        'false_val',
        [False, "false", "f", "no", "n", "none", "0", "[]", "{}", "", "off"])
    def test_toBoolean_false(self, false_val):
        assert not toBoolean(false_val)

    def test_propertiesToDict(self):
        d = {
            '1.1': '11',
            '1.2': '12',
            '2.1': '21',
            '3': '3'
        }
        dictprop = propertiesToDict(d)
        assert len(dictprop) == 3
        assert set(['1', '2', '3']) - set(dictprop.keys()) == set()
        assert len(dictprop['1']) == 2
        assert len(dictprop['2']) == 1
        assert dictprop['1']['1'] == '11'
        assert dictprop['1']['2'] == '12'
        assert dictprop['2']['1'] == '21'
        assert dictprop['3'] == '3'

    def test_propertiesToDictWithPrefix(self):
        d = {
            'omero.prefix.str.1': 'mystring',
            'omero.prefix.str.2': '1',
            'omero.prefix.int.1': 1
        }
        dictprop = propertiesToDict(d, prefix='omero.prefix.')
        assert len(dictprop) == 2
        assert set(['str', 'int']) - set(dictprop.keys()) == set()
        assert len(dictprop['str']) == 2
        assert len(dictprop['int']) == 1
        assert dictprop['int']['1'] == 1
        assert dictprop['str']['1'] == 'mystring'
        assert dictprop['str']['2'] == '1'

    def test_propertiesToDictBool(self):
        d = {
            'omero.prefix.strbool.false1.enabled': 'False',
            'omero.prefix.strbool.false2.enabled': 'false',
            'omero.prefix.strbool.true1.enabled': 'True',
            'omero.prefix.strbool.true2.enabled': 'true',
            'omero.prefix.bool1.enabled': False,
            'omero.prefix.bool2.enabled': True
        }
        dictprop = propertiesToDict(d, prefix='omero.prefix.')
        assert len(dictprop) == 3
        assert set(['strbool', 'bool1', 'bool2']) - set(dictprop.keys()) == \
            set()
        assert not dictprop['strbool']['false1']['enabled']
        assert not dictprop['strbool']['false2']['enabled']
        assert dictprop['strbool']['true1']['enabled']
        assert dictprop['strbool']['true2']['enabled']
        assert not dictprop['bool1']['enabled']
        assert dictprop['bool2']['enabled']
