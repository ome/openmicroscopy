/*
 *
 *   Copyright 20013 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */


#include <omero/fixture.h>
#include <omero/RTypesI.h>

using namespace omero;
using namespace omero::rtypes;


TEST(RTypeTest, RBool)
{
    RBoolPtr t = rbool(true);
    ASSERT_TRUE(t->getValue());
    
    RBoolPtr f = rbool(false);
    ASSERT_FALSE(f->getValue());
    
    ASSERT_EQ(t, rbool(true));
    ASSERT_NE(t, f);
}

TEST(RTypesTest, RDouble) {
    RDoublePtr d1 = rdouble(1.1);
    RDoublePtr d2 = rdouble(2.5);
    
    RTypePtr d3 = rdouble(3.3);
    RFloatPtr rd3 = dynamic_cast<RFloat*>(d3.get());
    if (rd3)
        std::cout << "rd3  " << rd3->getValue() << std::endl;
    
    ASSERT_LT(d1, d2);
    ASSERT_NE(d1, d2);
    ASSERT_EQ(d1->compare(d2), -1);
    ASSERT_EQ(d2->compare(d1), 1);
    ASSERT_EQ(d2->compare(rdouble(2.5)), 0);
}

TEST(RTypesTest, RFloat) {
    RFloatPtr f1 = rfloat(1.1);
    RFloatPtr f2 = rfloat(2.5);
    
    ASSERT_LT(f1, f2);
    ASSERT_NE(f1, f2);
}

TEST(RTypesTest, RInt) {
}

TEST(RTypesTest, RLong) {
}

TEST(RTypesTest, RTime) {
}

TEST(RTypesTest, RInternal) {
}

TEST(RTypesTest, RString) {
}

TEST(RTypesTest, RArray) {
}

TEST(RTypesTest, RList) {
}

TEST(RTypesTest, RSet) {
}

TEST(RTypesTest, RMap) {
}


