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

#include <omero/model/LengthI.h>
#include <omero/model/PowerI.h>
#include <omero/model/TemperatureI.h>
#include <omero/fixture.h>
#include <string>

using namespace omero::model;
using namespace omero::model::enums;

typedef omero::conversion_types::ConversionPtr ConversionPtr;

TEST(UnitsTest, testPowerConversion)
{
    PowerPtr p1 = new PowerI(100.1, CENTIWATT);
    PowerPtr p2 = new PowerI(p1, WATT);
    ASSERT_NEAR(1.001, p2->getValue(), 1e-5);
}

TEST(UnitsTest, testLengthSymbol)
{
    LengthPtr l = new LengthI(100.1, MICROMETER);
    ASSERT_EQ("µm", l->getSymbol());
}

TEST(UnitsTest, testBoiling)
{
    TemperaturePtr f = new TemperatureI(212, FAHRENHEIT);
    TemperaturePtr c = new TemperatureI(f, CELSIUS);

    ASSERT_NEAR(100.0, c->getValue(), 1e-5);
}

TEST(UnitsTest, testTemperatureConversion)
{
    TemperaturePtr f = new TemperatureI(32, FAHRENHEIT);
    TemperaturePtr c = new TemperatureI(f, CELSIUS);
    TemperaturePtr k = new TemperatureI(c, KELVIN);

    ASSERT_NEAR(0, c->getValue(), 1e-5);
    ASSERT_NEAR(273.15, k->getValue(), 1e-5);
}
