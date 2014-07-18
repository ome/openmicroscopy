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

    ASSERT_TRUE(t == rbool(true));
    ASSERT_TRUE(t != f);
}

template <typename T>
void assertRValues(const T rv1, const T rv2) {
    ASSERT_TRUE(-1 == rv1->compare(rv2));
    ASSERT_TRUE(1 == rv2->compare(rv1));
    ASSERT_FALSE(rv1 == rv2);
    ASSERT_TRUE(rv1 != rv2);
    ASSERT_TRUE(rv1 < rv2);
    ASSERT_TRUE(rv2 > rv1);

    ASSERT_TRUE(rv1->getValue() < rv2->getValue());
    ASSERT_TRUE(rv1->getValue() < rv2->getValue());
    ASSERT_TRUE(rv1->getValue() != rv2->getValue());

    ASSERT_EQ(rv1->compare(rv2), -1);
    ASSERT_EQ(rv2->compare(rv1), 1);
    ASSERT_EQ(rv2->compare(rv2), 0);
}


TEST(RTypesTest, RDouble) {
    RDoublePtr d1 = rdouble(1.1);
    RDoublePtr d2 = rdouble(2.5);

    RTypePtr d3 = rdouble(3.3);
    RFloatPtr rd3 = dynamic_cast<RFloat*>(d3.get());
    if (rd3)
        std::cout << "rd3  " << rd3->getValue() << std::endl;

    assertRValues<RDoublePtr>(d1, d2);
}

TEST(RTypesTest, RFloat) {
    RFloatPtr f1 = rfloat(1.1);
    RFloatPtr f2 = rfloat(2.5);

    assertRValues<RFloatPtr>(f1, f2);
}

TEST(RTypesTest, RInt) {
    RIntPtr i1 = rint(1);
    RIntPtr i2 = rint(2);

    assertRValues<RIntPtr>(i1, i2);
}

TEST(RTypesTest, RLong) {
    RLongPtr l1 = rlong(1);
    RLongPtr l2 = rlong(2);

    assertRValues<RLongPtr>(l1, l2);
}

TEST(RTypesTest, RTime) {
    RTimePtr t1 = rtime(1);
    RTimePtr t2 = rtime(2);

    assertRValues<RTimePtr>(t1, t2);
}

TEST(RTypesTest, RString) {
    RStringPtr s1 = rstring("abc");
    RStringPtr s2 = rstring("def");

    assertRValues<RStringPtr>(s1, s2);
}

TEST(RTypesTest, RArray) {
    RArrayPtr a1 = rarray();
    a1->add(rint(1));

    RArrayPtr a2 = rarray();
    a2->add(rint(2));

    assertRValues<RArrayPtr>(a1, a2);
}

TEST(RTypesTest, RList) {
    RListPtr l1 = rlist();
    l1->add(rint(1));

    RListPtr l2 = rlist();
    l2->add(rint(2));

    assertRValues<RListPtr>(l1, l2);
}

TEST(RTypesTest, RSet) {
    RSetPtr s1 = rset();
    s1->add(rint(1));

    RSetPtr s2 = rset();
    s2->add(rint(2));

    assertRValues<RSetPtr>(s1, s2);
}

TEST(RTypesTest, RMap) {
    RMapPtr m1 = rmap();
    m1->put("a", rint(1));

    RMapPtr m2 = rmap();
    m2->put("b", rint(2));

    assertRValues<RMapPtr>(m1, m2);
}
