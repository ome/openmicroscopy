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
        self.assert_(array_notnull1 !=  array_notnull2)
        # But content is copied!
        self.assert_(not array_notnull1.getValue() is array_notnull2.getValue())

        array_null1 = rarray()
        array_null2 = rarray(None)
        array_null3 = rarray(*[])

        # All different since the contents are mutable.
        self.assert_(array_null1 !=  array_notnull1)
        self.assert_(array_null1 !=  array_null2)
        self.assert_(array_null1 !=  array_null3)

    def testListCreationEqualsHash(self):

        list_notnull1 = rlist(ids)
        list_notnull2 = rlist(ids)
        # Equals based on content
        self.assert_(list_notnull1 !=  list_notnull2)
        # But content is copied!
        self.assert_(not list_notnull1.getValue() is list_notnull2.getValue())

        list_null1 = rlist()
        list_null2 = rlist(None)
        list_null3 = rlist(*[])

        # All different since the contents are mutable.
        self.assert_(list_null1 !=  list_notnull1)
        self.assert_(list_null1 !=  list_null2)
        self.assert_(list_null1 !=  list_null3)

    def testSetCreationEqualsHash(self):

        set_notnull1 = rset(ids)
        set_notnull2 = rset(ids)
        # Equals based on content
        self.assert_(set_notnull1 !=  set_notnull2)
        # But content is copied!
        self.assert_(not set_notnull1.getValue() is set_notnull2.getValue())

        set_null1 = rset()
        set_null2 = rset(None)
        set_null3 = rset(*[])

        # All different since the contents are mutable.
        self.assert_(set_null1 !=  set_notnull1)
        self.assert_(set_null1 !=  set_null2)
        self.assert_(set_null1 !=  set_null3)

    def testMapCreationEqualsHash(self):

        id = rlong(1L)
        map_notnull1 = rmap({"ids": id})
        map_notnull2 = rmap({"ids": id})
        # Equals based on content
        self.assert_(map_notnull1 != map_notnull2)

        # But content is copied!
        self.assert_(not map_notnull1.getValue() is map_notnull2.getValue())

        map_null1 = rmap()
        map_null2 = rmap(None)
        map_null3 = rmap(**{})

        # All different since the contents are mutable.
        self.assert_(map_null1 !=  map_notnull1)
        self.assert_(map_null1 !=  map_null2) # TODO Different with maps
        self.assert_(map_null1 !=  map_null3) # TODO Different with maps

if __name__ == '__main__':
    unittest.main()
