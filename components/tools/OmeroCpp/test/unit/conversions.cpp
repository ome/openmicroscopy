/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#include <omero/conversions.h>
#include <omero/fixture.h>
#include <string>

using namespace omero::conversions;

typedef omero::conversion_types::ConversionPtr ConversionPtr;

TEST(ConversionsTest, testSimplAdd)
{
    ConversionPtr add = Add(Rat(1, 2), Rat(1,2));
    double whole = add->convert(-1); // -1 is ignored
    ASSERT_NEAR(1.0, whole, 0.0001);
}

TEST(ConversionsTest, testSimpleMul)
{
    ConversionPtr mul = Mul(Int(1000000), Sym("megas"));
    double seconds = mul->convert(5.0);
    ASSERT_NEAR(5000000.0, seconds, 0.0001);
}

TEST(ConversionsTest, testSimpleInt)
{
    ConversionPtr i = Int(123);
    double x = i->convert(-1); // -1 is ignored
    ASSERT_NEAR(123.0, x, 0.0001);
}

TEST(ConversionsTest, testBigInt)
{
    std::string big = "123456789012345678901234567891234567890";
    big = big + big + big + big + big;
    ConversionPtr i = Mul(Int(big), Int(big));
    i->convert(-1);
    // TODO: This should throw an exception for "no conversion possible"
    // ASSERT_NEAR(Double.POSITIVE_INFINITY, rv);
}

TEST(ConversionsTest, testSimplePow)
{
    ConversionPtr p = Pow(3, 2);
    double x = p->convert(-1); // -1 is ignored
    ASSERT_NEAR(9.0, x, 0.0001);
}

TEST(ConversionsTest, testSimpleRat)
{
    ConversionPtr r = Rat(1, 3);
    double x = r->convert(-1); // -1 is ignored
    ASSERT_NEAR(0.33333333, x, 0.0001);
}

TEST(ConversionsTest, testDelayedRat)
{
    ConversionPtr r = Rat(Int(1), Int(3));
    double x = r->convert(-1); // -1 is ignored
    ASSERT_NEAR(0.33333333, x, 0.0001);
}

TEST(ConversionsTest, testSimpleSym)
{
    ConversionPtr sym = Sym("x");
    double x = sym->convert(5.0);
    ASSERT_NEAR(5.0, x, 0.0001);
}

TEST(ConversionsTest, testFahrenheit)
{
    ConversionPtr ftoc = Add(Mul(Rat(5, 9), Sym("f")), Rat(-160, 9));
    ASSERT_NEAR(0.0, ftoc->convert(32.0), 0.0001);
    ASSERT_NEAR(100.0, ftoc->convert(212.0), 0.0001);
    ASSERT_NEAR(-40.0, ftoc->convert(-40.0), 0.0001);
}
