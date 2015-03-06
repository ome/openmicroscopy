/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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



#ifndef CLASS_UNITS
#define CLASS_UNITS

module omero {

    module model {

        module enums {

            enum UnitsElectricPotential {
                YOTTAVOLT,
                ZETTAVOLT,
                EXAVOLT,
                PETAVOLT,
                TERAVOLT,
                GIGAVOLT,
                MEGAVOLT,
                KILOVOLT,
                HECTOVOLT,
                DECAVOLT,
                VOLT,
                DECIVOLT,
                CENTIVOLT,
                MILLIVOLT,
                MICROVOLT,
                NANOVOLT,
                PICOVOLT,
                FEMTOVOLT,
                ATTOVOLT,
                ZEPTOVOLT,
                YOCTOVOLT
            };

            enum UnitsFrequency {
                YOTTAHERTZ,
                ZETTAHERTZ,
                EXAHERTZ,
                PETAHERTZ,
                TERAHERTZ,
                GIGAHERTZ,
                MEGAHERTZ,
                KILOHERTZ,
                HECTOHERTZ,
                DECAHERTZ,
                HERTZ,
                DECIHERTZ,
                CENTIHERTZ,
                MILLIHERTZ,
                MICROHERTZ,
                NANOHERTZ,
                PICOHERTZ,
                FEMTOHERTZ,
                ATTOHERTZ,
                ZEPTOHERTZ,
                YOCTOHERTZ
            };

            enum UnitsLength {
                YOTTAMETER,
                ZETTAMETER,
                EXAMETER,
                PETAMETER,
                TERAMETER,
                GIGAMETER,
                MEGAMETER,
                KILOMETER,
                HECTOMETER,
                DECAMETER,
                METER,
                DECIMETER,
                CENTIMETER,
                MILLIMETER,
                MICROMETER,
                NANOMETER,
                PICOMETER,
                FEMTOMETER,
                ATTOMETER,
                ZEPTOMETER,
                YOCTOMETER,
                ANGSTROM,
                ASTRONOMICALUNIT,
                LIGHTYEAR,
                PARSEC,
                THOU,
                LINE,
                INCH,
                FOOT,
                YARD,
                MILE,
                POINT,
                PIXEL,
                REFERENCEFRAME
            };

            enum UnitsPower {
                YOTTAWATT,
                ZETTAWATT,
                EXAWATT,
                PETAWATT,
                TERAWATT,
                GIGAWATT,
                MEGAWATT,
                KILOWATT,
                HECTOWATT,
                DECAWATT,
                WATT,
                DECIWATT,
                CENTIWATT,
                MILLIWATT,
                MICROWATT,
                NANOWATT,
                PICOWATT,
                FEMTOWATT,
                ATTOWATT,
                ZEPTOWATT,
                YOCTOWATT
            };

            enum UnitsPressure {
                YOTTAPASCAL,
                ZETTAPASCAL,
                EXAPASCAL,
                PETAPASCAL,
                TERAPASCAL,
                GIGAPASCAL,
                MEGAPASCAL,
                KILOPASCAL,
                HECTOPASCAL,
                DECAPASCAL,
                Pascal,
                DECIPASCAL,
                CENTIPASCAL,
                MILLIPASCAL,
                MICROPASCAL,
                NANOPASCAL,
                PICOPASCAL,
                FEMTOPASCAL,
                ATTOPASCAL,
                ZEPTOPASCAL,
                YOCTOPASCAL,
                BAR,
                MEGABAR,
                KILOBAR,
                DECIBAR,
                CENTIBAR,
                MILLIBAR,
                ATMOSPHERE,
                PSI,
                TORR,
                MILLITORR,
                MMHG
            };

            enum UnitsTemperature {
                KELVIN,
                CELSIUS,
                FAHRENHEIT,
                RANKINE
            };

            enum UnitsTime {
                YOTTASECOND,
                ZETTASECOND,
                EXASECOND,
                PETASECOND,
                TERASECOND,
                GIGASECOND,
                MEGASECOND,
                KILOSECOND,
                HECTOSECOND,
                DECASECOND,
                SECOND,
                DECISECOND,
                CENTISECOND,
                MILLISECOND,
                MICROSECOND,
                NANOSECOND,
                PICOSECOND,
                FEMTOSECOND,
                ATTOSECOND,
                ZEPTOSECOND,
                YOCTOSECOND,
                MINUTE,
                HOUR,
                DAY
            };

        };

    };
};
#endif

