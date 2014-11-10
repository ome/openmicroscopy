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

import java.util.HashMap;
import java.util.Map;

public enum UnitsPower {

    YOTTAW("YW"),
    ZETTAW("ZW"),
    EXAW("EW"),
    PETAW("PW"),
    TERAW("TW"),
    GIGAW("GW"),
    MEGAW("MW"),
    KW("kW"),
    HW("hW"),
    DAW("daW"),
    W("W"),
    DW("dW"),
    CW("cW"),
    MW("mW"),
    MICROW("µW"),
    NW("nW"),
    PW("pW"),
    FW("fW"),
    AW("aW"),
    ZW("zW"),
    YW("yW");

    private static final Map<String, UnitsPower> bySymbol
        = new HashMap<String, UnitsPower>();

    static {
        for (UnitsPower t : UnitsPower.values()) {
            bySymbol.put(t.value, t);
        }
    }

    protected String value;

    private UnitsPower(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UnitsPower bySymbol(String symbol) {
        return bySymbol.get(symbol);
    }

};

