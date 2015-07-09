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
import pytest
import omero
import omero.model  # For Image
from omero.rtypes import rint, rlong, rstring, rmap, rdouble, rclass, robject
from omero.rtypes import rlist, rfloat, rbool, rset, rtime, rinternal, rarray
from omero.rtypes import rtype, wrap, unwrap

# Data
ids = [rlong(1)]


class TestModel(object):

    def testConversionMethod(self):
        assert None == rtype(None)
        assert rlong(1) == rtype(rlong(1))  # Returns self
        assert rbool(True) == rtype(True)
        # Unsupported
        # assert rdouble(0) == rtype(Double.valueOf(0))
        assert rfloat(0) == rtype(float(0))
        assert rlong(0) == rtype(long(0))
        assert rint(0) == rtype(int(0))
        assert rstring("string") == rtype("string")
        # Unsupported
        # assert rtime(time) == rtype(new Timestamp(time))
        rtype(omero.model.ImageI())
        rtype(omero.grid.JobParams())
        rtype(set([rlong(1)]))
        rtype(list([rlong(2)]))
        rtype({})
        # Unsupported
        # rtype(array)
        try:
            rtype(())
            assert False, "Shouldn't be able to handle this yet"
        except omero.ClientError:
            pass

    def testObjectCreationEqualsAndHash(self):

        # RBool
        true1 = rbool(True)
        true2 = rbool(True)
        false1 = rbool(False)
        false2 = rbool(False)
        assert true1 == true2
        assert false1 == false2
        assert true1.getValue()
        assert not false1.getValue()
        assert true1 == true2
        assert true1 != false1

        # RDouble
        double_zero1 = rdouble(0.0)
        double_zero2 = rdouble(0.0)
        double_notzero1 = rdouble(1.1)
        double_notzero1b = rdouble(1.1)
        double_notzero2 = rdouble(2.2)
        assert double_zero1.getValue() == 0.0
        assert double_notzero1.getValue() == 1.1
        assert double_zero1 == double_zero2
        assert double_zero1 != double_notzero1
        assert double_notzero1 == double_notzero1b
        assert double_notzero1 != double_notzero2

        # RFloat
        float_zero1 = rfloat(0.0)
        float_zero2 = rfloat(0.0)
        float_notzero1 = rfloat(1.1)
        float_notzero1b = rfloat(1.1)
        float_notzero2 = rfloat(2.2)
        assert float_zero1.getValue() == 0.0
        assert float_notzero1.getValue() == 1.1
        assert float_zero1 == float_zero2
        assert float_zero1 != float_notzero1
        assert float_notzero1 == float_notzero1b
        assert float_notzero1 != float_notzero2

        # RInt
        int_zero1 = rint(0)
        int_zero2 = rint(0)
        int_notzero1 = rint(1)
        int_notzero1b = rint(1)
        int_notzero2 = rint(2)
        assert int_zero1.getValue() == 0
        assert int_notzero1.getValue() == 1
        assert int_zero1 == int_zero2
        assert int_zero1 != int_notzero1
        assert int_notzero1 == int_notzero1b
        assert int_notzero1 != int_notzero2

        # RLong
        long_zero1 = rlong(0)
        long_zero2 = rlong(0)
        long_notzero1 = rlong(1)
        long_notzero1b = rlong(1)
        long_notzero2 = rlong(2)
        assert long_zero1.getValue() == 0
        assert long_notzero1.getValue() == 1
        assert long_zero1 == long_zero2
        assert long_zero1 != long_notzero1
        assert long_notzero1 == long_notzero1b
        assert long_notzero1 != long_notzero2

        # RTime
        time_zero1 = rtime(0)
        time_zero2 = rtime(0)
        time_notzero1 = rtime(1)
        time_notzero1b = rtime(1)
        time_notzero2 = rtime(2)
        assert time_zero1.getValue() == 0
        assert time_notzero1.getValue() == 1
        assert time_zero1 == time_zero2
        assert time_zero1 != time_notzero1
        assert time_notzero1 == time_notzero1b
        assert time_notzero1 != time_notzero2

        # RInternal
        internal_null1 = rinternal(None)
        internal_null2 = rinternal(None)
        internal_notnull1 = rinternal(omero.grid.JobParams())
        internal_notnull2 = rinternal(omero.grid.JobParams())
        assert internal_null1 == internal_null2
        assert internal_null1 == internal_null2
        assert internal_null1 != internal_notnull2
        assert internal_notnull1 == internal_notnull1
        assert internal_notnull1 != internal_notnull2

        # RObject
        object_null1 = robject(None)
        object_null2 = robject(None)
        object_notnull1 = robject(omero.model.ImageI())
        object_notnull2 = robject(omero.model.ImageI())
        assert object_null1 == object_null2
        assert object_null1 == object_null2
        assert object_null1 != object_notnull2
        assert object_notnull1 == object_notnull1
        assert object_notnull1 != object_notnull2

        # RString
        string_null1 = rstring(None)
        string_null2 = rstring(None)
        string_notnull1 = rstring("str1")
        string_notnull1b = rstring("str1")
        string_notnull2 = rstring("str2")
        assert string_null1 == string_null2
        assert string_null1 == string_null2
        assert string_null1 != string_notnull2
        assert string_notnull1 == string_notnull1
        assert string_notnull1 != string_notnull2
        assert string_notnull1 == string_notnull1b

        # RClass
        class_null1 = rclass(None)
        class_null2 = rclass(None)
        class_notnull1 = rclass("str1")
        class_notnull1b = rclass("str1")
        class_notnull2 = rclass("str2")
        assert class_null1 == class_null2
        assert class_null1 == class_null2
        assert class_null1 != class_notnull2
        assert class_notnull1 == class_notnull1
        assert class_notnull1 != class_notnull2
        assert class_notnull1 == class_notnull1b

    def testArrayCreationEqualsHash(self):

        array_notnull1 = rarray(ids)
        array_notnull2 = rarray(ids)
        # Equals based on content
        assert array_notnull1 == array_notnull2
        # But content is copied!
        assert not array_notnull1.getValue() is array_notnull2.getValue()

        array_null1 = rarray()
        array_null2 = rarray(None)
        array_null3 = rarray(*[])

        # All different since the contents are mutable.
        assert array_null1 is not array_notnull1
        assert array_null1 is not array_null2
        assert array_null1 is not array_null3

    def testListCreationEqualsHash(self):

        list_notnull1 = rlist(ids)
        list_notnull2 = rlist(ids)
        # Equals based on content
        assert list_notnull1 == list_notnull2
        # But content is copied!
        assert not list_notnull1.getValue() is list_notnull2.getValue()

        list_null1 = rlist()
        list_null2 = rlist(None)
        list_null3 = rlist(*[])

        # All different since the contents are mutable.
        assert list_null1 is not list_notnull1
        assert list_null1 is not list_null2
        assert list_null1 is not list_null3

    def testSetCreationEqualsHash(self):

        set_notnull1 = rset(ids)
        set_notnull2 = rset(ids)
        # Equals based on content
        assert set_notnull1 == set_notnull2
        # But content is copied!
        assert not set_notnull1.getValue() is set_notnull2.getValue()

        set_null1 = rset()
        set_null2 = rset(None)
        set_null3 = rset(*[])

        # All different since the contents are mutable.
        assert set_null1 is not set_notnull1
        assert set_null1 is not set_null2
        assert set_null1 is not set_null3

    def testMapCreationEqualsHash(self):

        id = rlong(1L)
        map_notnull1 = rmap({"ids": id})
        map_notnull2 = rmap({"ids": id})
        # Equals based on content
        assert map_notnull1 == map_notnull2

        # But content is copied!
        assert not map_notnull1.getValue() is map_notnull2.getValue()

        map_null1 = rmap()
        map_null2 = rmap(None)
        map_null3 = rmap(**{})

        # All different since the contents are mutable.
        assert map_null1 is not map_notnull1
        assert map_null1 is not map_null2  # TODO Different with maps
        assert map_null1 is not map_null3  # TODO Different with maps

    #
    # Python only
    #

    def testGetAttrWorks(self):
        rbool(True).val
        rdouble(0.0).val
        rfloat(0.0).val
        rint(0).val
        rlong(0).val
        rtime(0).val
        rinternal(None).val
        robject(None).val
        rstring("").val
        rclass("").val
        rarray().val
        rlist().val
        rset().val
        rmap().val

    def testPassThroughNoneAndRTypes(self):
        """
        To prevent having to check for isintance(int,...) or
        isintance(RInt,...) all over the place, the static methods
        automatically check for acceptable
        types and simply pass them through. Similarly, the primitive types all
        check for None and return a null RType if necessary.
        """
        # Bool
        assert None == rbool(None)
        assert rbool(True) == rbool(rbool(True))
        assert rbool(True) == rbool(1)
        assert rbool(False) == rbool(0)
        # Double
        assert None == rdouble(None)
        assert rdouble(0.0) == rdouble(rdouble(0.0))
        assert rdouble(0.0) == rdouble(rdouble(0))
        assert rdouble(0.0) == rdouble(rdouble("0.0"))
        pytest.raises(ValueError, lambda: rdouble("string"))
        # Float
        assert None == rfloat(None)
        assert rfloat(0.0) == rfloat(rfloat(0.0))
        assert rfloat(0.0) == rfloat(rfloat(0))
        assert rfloat(0.0) == rfloat(rfloat("0.0"))
        pytest.raises(ValueError, lambda: rfloat("string"))
        # Long
        assert None == rlong(None)
        assert rlong(0) == rlong(rlong(0))
        assert rlong(0) == rlong(rlong(0.0))
        assert rlong(0) == rlong(rlong("0"))
        pytest.raises(ValueError, lambda: rlong("string"))
        # Time
        assert None == rtime(None)
        assert rtime(0) == rtime(rtime(0))
        assert rtime(0) == rtime(rtime(0.0))
        assert rtime(0) == rtime(rtime("0"))
        pytest.raises(ValueError, lambda: rtime("string"))
        # Int
        assert None == rint(None)
        assert rint(0) == rint(rint(0))
        assert rint(0) == rint(rint(0.0))
        assert rint(0) == rint(rint("0"))
        pytest.raises(ValueError, lambda: rint("string"))
        #
        # Starting here handling of null is different.
        #
        # String
        assert rstring("") == rstring(None)
        assert rstring("a") == rstring(rstring("a"))
        assert rstring("0") == rstring(0)
        # Class
        assert rclass("") == rclass(None)
        assert rclass("c") == rclass(rclass("c"))
        pytest.raises(ValueError, lambda: rclass(0))
        # Internal
        internal = omero.Internal()
        assert rinternal(None) == rinternal(None)
        assert rinternal(internal) == rinternal(rinternal(internal))
        pytest.raises(ValueError, lambda: rinternal("string"))
        # Object
        obj = omero.model.ImageI()
        assert robject(None) == robject(None)
        assert robject(obj) == robject(robject(obj))
        pytest.raises(ValueError, lambda: robject("string"))
        #
        # Same does not hold for collections
        #
        # Array
        assert rarray([]) == rarray(None)
        # assert rarray(obj) == rarray(rarray(obj))
        # pytest.raises(ValueError, lambda : rarray("string"))
        # List
        assert rlist([]) == rlist(None)
        # assert rlist(obj) == rlist(rlist(obj))
        # pytest.raises(ValueError, lambda : rlist("string"))
        # Set
        assert rset([]) == rset(None)
        # assert rset(obj) == rset(rset(obj))
        # pytest.raises(ValueError, lambda : rset("string"))
        # Map
        assert rmap({}) == rmap(None)
        # assert rmap(obj) == rmap(rmap(obj))
        # pytest.raises(ValueError, lambda : rmap("string"))

    def testUnwrap(self):
        # NUMS plain
        assert 0 == unwrap(0)
        assert 1 == unwrap(1)
        assert 0.0 == unwrap(0.0)
        assert 1.0 == unwrap(1.0)
        # NUMS rtyped
        assert 0 == unwrap(rint(0))
        assert 0 == unwrap(rlong(0))
        assert 1 == unwrap(rint(1))
        assert 1 == unwrap(rlong(1))
        assert 0.0 == unwrap(rfloat(0.0))
        assert 0.0 == unwrap(rdouble(0.0))
        assert 1.0 == unwrap(rfloat(1.0))
        assert 1.0 == unwrap(rdouble(1.0))

        # STRINGS
        assert "" == unwrap("")
        assert "str" == unwrap("str")

        # BOOL
        assert True == unwrap(True)
        assert False == unwrap(False)
        assert True == unwrap(rbool(True))
        assert False == unwrap(rbool(False))

        # TIME
        # Bit odd, do we want the long for time, or transformed?
        assert 0 == unwrap(rtime(0))
        assert 1 == unwrap(rtime(1))

        # CLASS
        # same for class, should we map it?
        assert "k" == unwrap(rclass("k"))

        # INTERNAL
        color = omero.Color()
        assert color == unwrap(rinternal(color))

        # OBJECT
        image = omero.model.ImageI()
        assert image == unwrap(robject(image))

        # COLLECTIONS
        # empty
        assert [] == unwrap([])
        assert {} == unwrap({})
        assert set() == unwrap(set())
        # plain in collection
        assert [1] == unwrap([1])
        # rtype in collection
        assert [1] == unwrap([rint(1)])
        assert {"a": 1} == unwrap({"a": 1})
        # plain in rcollection ILLEGAL
        # assert [1] == unwrap(rlist([1]))
        # assert {"a":1} == unwrap(rmap({"a":1}))
        # rtype in rcollection
        assert [1] == unwrap(rlist([rint(1)]))
        assert {"a": 1} == unwrap(rmap({"a": rint(1)}))
        # rtype keys ILLEGAL
        # assert {"a":1} == unwrap(rmap({rstring("a"):rint(1)}))
        # recursion, ticket:1977
        m1 = rmap({"a": rint(1)})
        m1.val["m1"] = m1
        m2 = {"a": 1}
        m2["m1"] = m2
        unwrap(m1)
        assert m2["a"] == unwrap(m1)["a"]
        # Can't compare directly "maximum recursion depth exceeded in cmp"
        assert type(m2["m1"]) == type(unwrap(m1)["m1"])

    def testWrap(self):
        rv = wrap([1, 2, 3])
        assert isinstance(rv, omero.RList)
        assert [1 == 2, 3], unwrap(rv)
        for x in rv.val:
            assert isinstance(x, omero.RInt)

        rv = wrap({"a": 1})
        assert isinstance(rv, omero.RMap)
        assert "a" in rv.val
        assert isinstance(rv.val["a"], omero.RInt)
        assert 1 == rv.val["a"].val

        pytest.raises(ValueError, wrap, {1: 2})

    def testResuingClass(self):
        myLong = rlong(5)
        myLongFromString = rlong("5")
        assert myLong.val == myLongFromString.val

        ctor1 = myLong.__class__(5)
        ctor2 = myLongFromString.__class__("5")
        assert ctor1.val == ctor2.val

    u = unicode("u")

    class UStr(object):

        def __init__(self, rv):
            self.rv = rv

        def __str__(self):
            return self.rv

    @pytest.mark.parametrize("data", (
        (rstring, 1, "1"),
        (rstring, u, "u"),
        (rstring, u.encode("utf-8"), "u"),
        (rstring, UStr(u), "u"),
    ))
    def testMoreConversions(self, data):
        # Some useful conversions were not supported in 5.1.2 and
        # earlier. This tests each of those which should no longer
        # be an error condition.
        meth, arg, expected = data
        assert meth(arg).val == expected
