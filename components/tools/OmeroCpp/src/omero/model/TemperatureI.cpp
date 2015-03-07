/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment
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

#include <omero/model/TemperatureI.h>
#include <omero/ClientErrors.h>

::Ice::Object* IceInternal::upCast(::omero::model::TemperatureI* t) { return t; }

using namespace omero::conversions;

typedef omero::model::enums::UnitsTemperature UnitsTemperature;

namespace omero {

    namespace model {

        static std::map<UnitsTemperature, ConversionPtr> createMapCELSIUS() {
            std::map<UnitsTemperature, ConversionPtr> c;
            c[enums::FAHRENHEIT] = Add(Mul(Rat(Int(9), Int(5)), Sym("c")), Int(32));
            c[enums::KELVIN] = Add(Sym("c"), Rat(Int(5463), Int(20)));
            c[enums::RANKINE] = Add(Mul(Rat(Int(9), Int(5)), Sym("c")), Rat(Int(49167), Int(100)));
            return c;
        }

        static std::map<UnitsTemperature, ConversionPtr> createMapFAHRENHEIT() {
            std::map<UnitsTemperature, ConversionPtr> c;
            c[enums::CELSIUS] = Add(Mul(Rat(Int(5), Int(9)), Sym("f")), Rat(Int(-160), Int(9)));
            c[enums::KELVIN] = Add(Mul(Rat(Int(5), Int(9)), Sym("f")), Rat(Int(45967), Int(180)));
            c[enums::RANKINE] = Add(Sym("f"), Rat(Int(45967), Int(100)));
            return c;
        }

        static std::map<UnitsTemperature, ConversionPtr> createMapKELVIN() {
            std::map<UnitsTemperature, ConversionPtr> c;
            c[enums::CELSIUS] = Add(Sym("k"), Rat(Int(-5463), Int(20)));
            c[enums::FAHRENHEIT] = Add(Mul(Rat(Int(9), Int(5)), Sym("k")), Rat(Int(-45967), Int(100)));
            c[enums::RANKINE] = Mul(Rat(Int(9), Int(5)), Sym("k"));
            return c;
        }

        static std::map<UnitsTemperature, ConversionPtr> createMapRANKINE() {
            std::map<UnitsTemperature, ConversionPtr> c;
            c[enums::CELSIUS] = Add(Mul(Rat(Int(5), Int(9)), Sym("r")), Rat(Int(-5463), Int(20)));
            c[enums::FAHRENHEIT] = Add(Sym("r"), Rat(Int(-45967), Int(100)));
            c[enums::KELVIN] = Mul(Rat(Int(5), Int(9)), Sym("r"));
            return c;
        }

        static std::map<UnitsTemperature,
            std::map<UnitsTemperature, ConversionPtr> > makeConversions() {
            std::map<UnitsTemperature, std::map<UnitsTemperature, ConversionPtr> > c;
            c[enums::CELSIUS] = createMapCELSIUS();
            c[enums::FAHRENHEIT] = createMapFAHRENHEIT();
            c[enums::KELVIN] = createMapKELVIN();
            c[enums::RANKINE] = createMapRANKINE();
            return c;
        }

        static std::map<UnitsTemperature, std::string> makeSymbols(){
            std::map<UnitsTemperature, std::string> s;
            s[enums::CELSIUS] = "°C";
            s[enums::FAHRENHEIT] = "°F";
            s[enums::KELVIN] = "K";
            s[enums::RANKINE] = "°R";
            return s;
        }

        std::map<UnitsTemperature,
            std::map<UnitsTemperature, ConversionPtr> > TemperatureI::CONVERSIONS = makeConversions();

        std::map<UnitsTemperature, std::string> TemperatureI::SYMBOLS = makeSymbols();

        TemperatureI::~TemperatureI() {}

        TemperatureI::TemperatureI() : Temperature() {
        }

        TemperatureI::TemperatureI(const double& value, const UnitsTemperature& unit) : Temperature() {
            setValue(value);
            setUnit(unit);
        }

        TemperatureI::TemperatureI(const TemperaturePtr& value, const UnitsTemperature& target) : Temperature() {
            double orig = value->getValue();
            UnitsTemperature source = value->getUnit();
            if (target == source) {
                // No conversion needed
                setValue(orig);
                setUnit(target);
            } else {
                ConversionPtr conversion = CONVERSIONS[source][target];
                if (!conversion) {
                    std::stringstream ss;
                    ss << orig << " " << source;
                    ss << "cannot be converted to " << target;
                    throw omero::ClientError(__FILE__, __LINE__, ss.str().c_str());
                }
                double converted = conversion->convert(orig);
                setValue(converted);
                setUnit(target);
            }
        }

        Ice::Double TemperatureI::getValue(const Ice::Current& /* current */) {
            return value;
        }

        void TemperatureI::setValue(Ice::Double _value, const Ice::Current& /* current */) {
            value = _value;
        }

        UnitsTemperature TemperatureI::getUnit(const Ice::Current& /* current */) {
            return unit;
        }

        void TemperatureI::setUnit(UnitsTemperature _unit, const Ice::Current& /* current */) {
            unit = _unit;
        }

        std::string TemperatureI::getSymbol(const Ice::Current& /* current */) {
            return SYMBOLS[unit];
        }

        TemperaturePtr TemperatureI::copy(const Ice::Current& /* current */) {
            TemperaturePtr copy = new TemperatureI();
            copy->setValue(getValue());
            copy->setUnit(getUnit());
            return copy;
        }
    }
}

