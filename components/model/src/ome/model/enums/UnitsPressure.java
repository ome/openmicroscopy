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


package ome.model.enums;

import ome.model.units.UnitEnum;

import java.util.HashMap;
import java.util.Map;

public enum UnitsPressure implements UnitEnum {

    YOTTAPASCAL("YPa"),
    ZETTAPASCAL("ZPa"),
    EXAPASCAL("EPa"),
    PETAPASCAL("PPa"),
    TERAPASCAL("TPa"),
    GIGAPASCAL("GPa"),
    MEGAPASCAL("MPa"),
    KILOPASCAL("kPa"),
    HECTOPASCAL("hPa"),
    DECAPASCAL("daPa"),
    Pascal("Pa"),
    DECIPASCAL("dPa"),
    CENTIPASCAL("cPa"),
    MILLIPASCAL("mPa"),
    MICROPASCAL("µPa"),
    NANOPASCAL("nPa"),
    PICOPASCAL("pPa"),
    FEMTOPASCAL("fPa"),
    ATTOPASCAL("aPa"),
    ZEPTOPASCAL("zPa"),
    YOCTOPASCAL("yPa"),
    BAR("bar"),
    MEGABAR("Mbar"),
    KILOBAR("kbar"),
    DECIBAR("dbar"),
    CENTIBAR("cbar"),
    MILLIBAR("mbar"),
    ATMOSPHERE("atm"),
    PSI("psi"),
    TORR("Torr"),
    MILLITORR("mTorr"),
    MMHG("mm Hg");

    private static final Map<String, UnitsPressure> bySymbol
        = new HashMap<String, UnitsPressure>();

    static {
        for (UnitsPressure t : UnitsPressure.values()) {
            bySymbol.put(t.symbol, t);
        }
    }

    protected String symbol;

    private UnitsPressure(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static UnitsPressure bySymbol(String symbol) {
        return bySymbol.get(symbol);
    }

};

