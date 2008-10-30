"""
/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
"""

import unittest, omero
from omero.rtypes import *

# Data
ids = [rlong(1)]

class TestModel(unittest.TestCase):

    def testConversionMethod(self):
        self.assertEquals(None, rtype(None))
        self.assertEquals(rbool(True), rtype(True))
        # Unsupported
        # self.assertEquals(rdouble(0), rtype(Double.valueOf(0)))
        self.assertEquals(rfloat(0), rtype(float(0)))
        self.assertEquals(rlong(0), rtype(long(0)))
        self.assertEquals(rint(0), rtype(int(0)))
        self.assertEquals(rstring("string"), rtype("string"))
        # Unsupported
        # self.assertEquals(rtime(time), rtype(new Timestamp(time)))
        rtype(omero.model.ImageI())
        rtype(omero.grid.JobParams())
        rtype(set([rlong(1)]))
        rtype(list([rlong(2)]))
        rtype({})
        # Unsupported
        # rtype(array)
        try:
            rtype(())
            self.fail("Shouldn't be able to handle this yet")
        except omero.ClientError:
            pass

    def testObjectCreationEqualsAndHash(self):

        # RBool
        true1 = rbool(True);
        true2 = rbool(True);
        false1 = rbool(False);
        false2 = rbool(False);
        self.assert_(true1 == true2);
        self.assert_(false1 == false2);
        self.assert_(true1.getValue());
        self.assert_( not false1.getValue());
        self.assert_(true1 == true2);
        self.assert_( true1 != false1);

        # RDouble
        double_zero1 = rdouble(0.0);
        double_zero2 = rdouble(0.0);
        double_notzero1 = rdouble(1.1);
        double_notzero1b = rdouble(1.1);
        double_notzero2 = rdouble(2.2)
        self.assert_(double_zero1.getValue() == 0.0)
        self.assert_(double_notzero1.getValue() == 1.1)
        self.assert_(double_zero1 ==  double_zero2)
        self.assert_(double_zero1 !=  double_notzero1)
        self.assert_(double_notzero1 ==  double_notzero1b)
        self.assert_(double_notzero1 !=  double_notzero2)

        # RFloat
        float_zero1 = rfloat(0.0)
        float_zero2 = rfloat(0.0)
        float_notzero1 = rfloat(1.1)
        float_notzero1b = rfloat(1.1)
        float_notzero2 = rfloat(2.2)
        self.assert_(float_zero1.getValue() == 0.0)
        self.assert_(float_notzero1.getValue() == 1.1)
        self.assert_(float_zero1 ==  float_zero2)
        self.assert_(float_zero1 !=  float_notzero1)
        self.assert_(float_notzero1 ==  float_notzero1b)
        self.assert_(float_notzero1 !=  float_notzero2)

        # RInt
        int_zero1 = rint(0)
        int_zero2 = rint(0)
        int_notzero1 = rint(1)
        int_notzero1b = rint(1)
        int_notzero2 = rint(2)
        self.assert_(int_zero1.getValue() == 0)
        self.assert_(int_notzero1.getValue() == 1)
        self.assert_(int_zero1 ==  int_zero2)
        self.assert_(int_zero1 !=  int_notzero1)
        self.assert_(int_notzero1 ==  int_notzero1b)
        self.assert_(int_notzero1 !=  int_notzero2)

        # RLong
        long_zero1 = rlong(0)
        long_zero2 = rlong(0)
        long_notzero1 = rlong(1)
        long_notzero1b = rlong(1)
        long_notzero2 = rlong(2)
        self.assert_(long_zero1.getValue() == 0)
        self.assert_(long_notzero1.getValue() == 1)
        self.assert_(long_zero1 ==  long_zero2)
        self.assert_(long_zero1 !=  long_notzero1)
        self.assert_(long_notzero1 ==  long_notzero1b)
        self.assert_(long_notzero1 !=  long_notzero2)

        # RTime
        time_zero1 = rtime(0)
        time_zero2 = rtime(0)
        time_notzero1 = rtime(1)
        time_notzero1b = rtime(1)
        time_notzero2 = rtime(2)
        self.assert_(time_zero1.getValue() == 0)
        self.assert_(time_notzero1.getValue() == 1)
        self.assert_(time_zero1 ==  time_zero2)
        self.assert_(time_zero1 !=  time_notzero1)
        self.assert_(time_notzero1 ==  time_notzero1b)
        self.assert_(time_notzero1 !=  time_notzero2)

        # RInternal
        internal_null1 = rinternal(None)
        internal_null2 = rinternal(None)
        internal_notnull1 = rinternal(omero.grid.JobParams())
        internal_notnull2 = rinternal(omero.grid.JobParams())
        self.assert_(internal_null1 == internal_null2)
        self.assert_(internal_null1 ==  internal_null2)
        self.assert_(internal_null1 !=  internal_notnull2)
        self.assert_(internal_notnull1 ==  internal_notnull1)
        self.assert_(internal_notnull1 !=  internal_notnull2)

        # RObject
        object_null1 = robject(None)
        object_null2 = robject(None)
        object_notnull1 = robject(omero.model.ImageI())
        object_notnull2 = robject(omero.model.ImageI())
        self.assert_(object_null1 == object_null2)
        self.assert_(object_null1 ==  object_null2)
        self.assert_(object_null1 !=  object_notnull2)
        self.assert_(object_notnull1 ==  object_notnull1)
        self.assert_(object_notnull1 !=  object_notnull2)

        # RString
        string_null1 = rstring(None)
        string_null2 = rstring(None)
        string_notnull1 = rstring("str1")
        string_notnull1b = rstring("str1")
        string_notnull2 = rstring("str2")
        self.assert_(string_null1 == string_null2)
        self.assert_(string_null1 ==  string_null2)
        self.assert_(string_null1 !=  string_notnull2)
        self.assert_(string_notnull1 ==  string_notnull1)
        self.assert_(string_notnull1 !=  string_notnull2)
        self.assert_(string_notnull1 ==  string_notnull1b)

        # RClass
        class_null1 = rclass(None)
        class_null2 = rclass(None)
        class_notnull1 = rclass("str1")
        class_notnull1b = rclass("str1")
        class_notnull2 = rclass("str2")
        self.assert_(class_null1 == class_null2)
        self.assert_(class_null1 ==  class_null2)
        self.assert_(class_null1 !=  class_notnull2)
        self.assert_(class_notnull1 ==  class_notnull1)
        self.assert_(class_notnull1 !=  class_notnull2)
        self.assert_(class_notnull1 ==  class_notnull1b)

    def testArrayCreationEqualsHash(self):

        array_notnull1 = rarray(ids)
        array_notnull2 = rarray(ids)
        # Equals based on content
        self.assert_(array_notnull1 ==  array_notnull2)
        # But content is copied!
        self.assert_(not array_notnull1.getValue() is array_notnull2.getValue())

        array_null1 = rarray()
        array_null2 = rarray(None)
        array_null3 = rarray(*[])

        # All different since the contents are mutable.
        self.assert_(not array_null1 is  array_notnull1)
        self.assert_(not array_null1 is  array_null2)
        self.assert_(not array_null1 is  array_null3)

    def testListCreationEqualsHash(self):

        list_notnull1 = rlist(ids)
        list_notnull2 = rlist(ids)
        # Equals based on content
        self.assert_(list_notnull1 ==  list_notnull2)
        # But content is copied!
        self.assert_(not list_notnull1.getValue() is list_notnull2.getValue())

        list_null1 = rlist()
        list_null2 = rlist(None)
        list_null3 = rlist(*[])

        # All different since the contents are mutable.
        self.assert_(not list_null1 is  list_notnull1)
        self.assert_(not list_null1 is  list_null2)
        self.assert_(not list_null1 is  list_null3)

    def testSetCreationEqualsHash(self):

        set_notnull1 = rset(ids)
        set_notnull2 = rset(ids)
        # Equals based on content
        self.assert_(set_notnull1 ==  set_notnull2)
        # But content is copied!
        self.assert_(not set_notnull1.getValue() is set_notnull2.getValue())

        set_null1 = rset()
        set_null2 = rset(None)
        set_null3 = rset(*[])

        # All different since the contents are mutable.
        self.assert_(not set_null1 is  set_notnull1)
        self.assert_(not set_null1 is  set_null2)
        self.assert_(not set_null1 is  set_null3)

    def testMapCreationEqualsHash(self):

        id = rlong(1L)
        map_notnull1 = rmap({"ids": id})
        map_notnull2 = rmap({"ids": id})
        # Equals based on content
        self.assert_(map_notnull1 == map_notnull2)

        # But content is copied!
        self.assert_(not map_notnull1.getValue() is map_notnull2.getValue())

        map_null1 = rmap()
        map_null2 = rmap(None)
        map_null3 = rmap(**{})

        # All different since the contents are mutable.
        self.assert_(not map_null1 is  map_notnull1)
        self.assert_(not map_null1 is  map_null2) # TODO Different with maps
        self.assert_(not map_null1 is  map_null3) # TODO Different with maps

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
        To prevent having to check for isintance(int,...) or isintance(RInt,...)
        all over the place, the static methods automaticalyl check for acceptable
        types and simply pass them through. Similarly, the primitive types all
        check for None and return a null RType if necessary.
        """
        # Bool
        self.assertEquals(None, rbool(None))
        self.assertEquals(rbool(True), rbool(rbool(True)))
        self.assertEquals(rbool(True), rbool(1))
        self.assertEquals(rbool(False), rbool(0))
        # Double
        self.assertEquals(None, rdouble(None))
        self.assertEquals(rdouble(0.0), rdouble(rdouble(0.0)))
        self.assertEquals(rdouble(0.0), rdouble(rdouble(0)))
        self.assertEquals(rdouble(0.0), rdouble(rdouble("0.0")))
        self.assertRaises(ValueError, lambda : rdouble("string"))
        # Float
        self.assertEquals(None, rfloat(None))
        self.assertEquals(rfloat(0.0), rfloat(rfloat(0.0)))
        self.assertEquals(rfloat(0.0), rfloat(rfloat(0)))
        self.assertEquals(rfloat(0.0), rfloat(rfloat("0.0")))
        self.assertRaises(ValueError, lambda : rfloat("string"))
        # Long
        self.assertEquals(None, rlong(None))
        self.assertEquals(rlong(0), rlong(rlong(0)))
        self.assertEquals(rlong(0), rlong(rlong(0.0)))
        self.assertEquals(rlong(0), rlong(rlong("0")))
        self.assertRaises(ValueError, lambda : rlong("string"))
        # Time
        self.assertEquals(None, rtime(None))
        self.assertEquals(rtime(0), rtime(rtime(0)))
        self.assertEquals(rtime(0), rtime(rtime(0.0)))
        self.assertEquals(rtime(0), rtime(rtime("0")))
        self.assertRaises(ValueError, lambda : rtime("string"))
        # Int
        self.assertEquals(None, rint(None))
        self.assertEquals(rint(0), rint(rint(0)))
        self.assertEquals(rint(0), rint(rint(0.0)))
        self.assertEquals(rint(0), rint(rint("0")))
        self.assertRaises(ValueError, lambda : rint("string"))
        #
        # Starting here handling of null is different.
        #
        # String
        self.assertEquals(rstring(""), rstring(None))
        self.assertEquals(rstring("a"), rstring(rstring("a")))
        self.assertRaises(ValueError, lambda : rstring(0))
        # Class
        self.assertEquals(rclass(""), rclass(None))
        self.assertEquals(rclass("c"), rclass(rclass("c")))
        self.assertRaises(ValueError, lambda : rclass(0))
        # Internal
        internal = omero.Internal()
        self.assertEquals(rinternal(None), rinternal(None))
        self.assertEquals(rinternal(internal), rinternal(rinternal(internal)))
        self.assertRaises(ValueError, lambda : rinternal("string"))
        # Object
        obj = omero.model.ImageI()
        self.assertEquals(robject(None), robject(None))
        self.assertEquals(robject(obj), robject(robject(obj)))
        self.assertRaises(ValueError, lambda : robject("string"))
        #
        # Same does not hold for collections
        #
        # Array
        self.assertEquals(rarray([]), rarray(None))
        ## self.assertEquals(rarray(obj), rarray(rarray(obj)))
        ## self.assertRaises(ValueError, lambda : rarray("string"))
        # List
        self.assertEquals(rlist([]), rlist(None))
        ## self.assertEquals(rlist(obj), rlist(rlist(obj)))
        ## self.assertRaises(ValueError, lambda : rlist("string"))
        # Set
        self.assertEquals(rset([]), rset(None))
        ## self.assertEquals(rset(obj), rset(rset(obj)))
        ## self.assertRaises(ValueError, lambda : rset("string"))
        # Map
        self.assertEquals(rmap({}), rmap(None))
        ## self.assertEquals(rmap(obj), rmap(rmap(obj)))
        ## self.assertRaises(ValueError, lambda : rmap("string"))


if __name__ == '__main__':
    unittest.main()
