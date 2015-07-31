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

public enum UnitsPower implements UnitEnum {

    YOTTAWATT("YW"),
    ZETTAWATT("ZW"),
    EXAWATT("EW"),
    PETAWATT("PW"),
    TERAWATT("TW"),
    GIGAWATT("GW"),
    MEGAWATT("MW"),
    KILOWATT("kW"),
    HECTOWATT("hW"),
    DECAWATT("daW"),
    WATT("W"),
    DECIWATT("dW"),
    CENTIWATT("cW"),
    MILLIWATT("mW"),
    MICROWATT("µW"),
    NANOWATT("nW"),
    PICOWATT("pW"),
    FEMTOWATT("fW"),
    ATTOWATT("aW"),
    ZEPTOWATT("zW"),
    YOCTOWATT("yW");

    private static final Map<String, UnitsPower> bySymbol
        = new HashMap<String, UnitsPower>();

    static {
        for (UnitsPower t : UnitsPower.values()) {
            bySymbol.put(t.symbol, t);
        }
    }

    protected String symbol;

    private UnitsPower(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static UnitsPower bySymbol(String symbol) {
        return bySymbol.get(symbol);
    }

};

