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

        module units {

            enum UNITSELECTRICPOTENTIAL {
                YOTTAV,
                ZETTAV,
                EXAV,
                PETAV,
                TERAV,
                GIGAV,
                MEGAV,
                KV,
                HV,
                DAV,
                V,
                DV,
                CV,
                MV,
                MICROV,
                NV,
                PV,
                FV,
                AV,
                ZV,
                YV
            };

            enum UNITSFREQUENCY {
                YOTTAHZ,
                ZETTAHZ,
                EXAHZ,
                PETAHZ,
                TERAHZ,
                GIGAHZ,
                MEGAHZ,
                KHZ,
                HHZ,
                DAHZ,
                HZ,
                DHZ,
                CHZ,
                MHZ,
                MICROHZ,
                NHZ,
                PHZ,
                FHZ,
                AHZ,
                ZHZ,
                YHZ
            };

            enum UNITSLENGTH {
                YOTTAM,
                ZETTAM,
                EXAM,
                PETAM,
                TERAM,
                GIGAM,
                MEGAM,
                KM,
                HM,
                DAM,
                M,
                DM,
                CM,
                MM,
                MICROM,
                NM,
                PM,
                FM,
                AM,
                ZM,
                YM,
                ANGSTROM,
                UA,
                LY,
                PC,
                THOU,
                LI,
                IN,
                FT,
                YD,
                MI,
                PT,
                PIXEL,
                REFERENCEFRAME
            };

            enum UNITSPOWER {
                YOTTAW,
                ZETTAW,
                EXAW,
                PETAW,
                TERAW,
                GIGAW,
                MEGAW,
                KW,
                HW,
                DAW,
                W,
                DW,
                CW,
                MW,
                MICROW,
                NW,
                PW,
                FW,
                AW,
                ZW,
                YW
            };

            enum UNITSPRESSURE {
                YOTTAPA,
                ZETTAPA,
                EXAPA,
                PETAPA,
                TERAPA,
                GIGAPA,
                MEGAPA,
                KPA,
                HPA,
                DAPA,
                PA,
                DPA,
                CPA,
                MPA,
                MICROPA,
                NPA,
                PPA,
                FPA,
                APA,
                ZPA,
                YPA,
                BAR,
                MEGABAR,
                KBAR,
                DBAR,
                CBAR,
                MBAR,
                ATM,
                PSI,
                TORR,
                MTORR,
                MMHG
            };

            enum UNITSTEMPERATURE {
                K,
                DEGREEC,
                DEGREEF,
                DEGREER
            };

            enum UNITSTIME {
                YOTTAS,
                ZETTAS,
                EXAS,
                PETAS,
                TERAS,
                GIGAS,
                MEGAS,
                KS,
                HS,
                DAS,
                S,
                DS,
                CS,
                MS,
                MICROS,
                NS,
                PS,
                FS,
                AS,
                ZS,
                YS,
                MIN,
                H,
                D
            };

        };

    };
};
#endif

