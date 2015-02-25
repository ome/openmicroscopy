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

package omero.model;

import static ome.model.units.Conversion.Mul;
import static ome.model.units.Conversion.Add;
import static ome.model.units.Conversion.Int;
import static ome.model.units.Conversion.Pow;
import static ome.model.units.Conversion.Rat;
import static ome.model.units.Conversion.Sym;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;

import ome.model.ModelBased;
import ome.model.units.BigResult;
import ome.model.units.Conversion;
import ome.units.unit.Unit;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.xml.model.enums.EnumerationException;

import omero.model.enums.UnitsFrequency;

/**
 * Blitz wrapper around the {@link ome.model.units.Frequency} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class FrequencyI extends Frequency implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static final Map<UnitsFrequency, Map<UnitsFrequency, Conversion>> conversions;
    static {

        EnumMap<UnitsFrequency, EnumMap<UnitsFrequency, Conversion>> c
            = new EnumMap<UnitsFrequency, EnumMap<UnitsFrequency, Conversion>>(UnitsFrequency.class);

        for (UnitsFrequency e : UnitsFrequency.values()) {
            c.put(e, new EnumMap<UnitsFrequency, Conversion>(UnitsFrequency.class));
        }

        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Pow(10, 16), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Pow(10, 19), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Pow(10, 17), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 36), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Int(1000), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 27), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 20), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.HERTZ, Mul(Pow(10, 18), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 21), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 24), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Pow(10, 12), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Pow(10, 15), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Pow(10, 9), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 33), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Pow(10, 6), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 30), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 42), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("attohz")));
        c.get(UnitsFrequency.ATTOHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 39), Sym("attohz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 16)), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Int(1000), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Int(10), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 20), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 13)), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 11), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 4), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.HERTZ, Mul(Int(100), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 5), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 8), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 4)), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Int(10)), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 7)), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 17), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 10)), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 14), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 22)), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 26), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 19)), Sym("centihz")));
        c.get(UnitsFrequency.CENTIHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 23), Sym("centihz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 19)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Int(100)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 17), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 16)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 8), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Int(10), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Int(10)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Int(100), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 5), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 7)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 4)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 10)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 14), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 13)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 11), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 25)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 23), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 22)), Sym("decahz")));
        c.get(UnitsFrequency.DECAHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 20), Sym("decahz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 17)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Int(10)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Int(100), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 19), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 14)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 10), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Int(1000), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.HERTZ, Mul(Int(10), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 4), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 7), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 5)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Int(100)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 8)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 16), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 11)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 13), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 23)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 25), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 20)), Sym("decihz")));
        c.get(UnitsFrequency.DECIHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 22), Sym("decihz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 36)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 20)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Pow(10, 17)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Pow(10, 19)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 33)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Rat(Int(1), Pow(10, 16)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 27)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 30)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 42)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 6), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 39)), Sym("exahz")));
        c.get(UnitsFrequency.EXAHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Int(1000), Sym("exahz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Pow(10, 13), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Pow(10, 16), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Pow(10, 14), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 33), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 24), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 17), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.HERTZ, Mul(Pow(10, 15), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 18), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 21), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Pow(10, 9), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Pow(10, 12), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Pow(10, 6), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 30), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Int(1000), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 27), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 39), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("femtohz")));
        c.get(UnitsFrequency.FEMTOHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 36), Sym("femtohz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 27)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 11)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Pow(10, 8)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Pow(10, 10)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 9), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Rat(Int(1), Pow(10, 7)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 6), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Int(1000), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 33)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 15), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 30)), Sym("gigahz")));
        c.get(UnitsFrequency.GIGAHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 12), Sym("gigahz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 20)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 4)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Int(10)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 16), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 17)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 7), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Int(100)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Int(10), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 4), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 8)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 5)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 11)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 13), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 14)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 10), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 26)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 22), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 23)), Sym("hectohz")));
        c.get(UnitsFrequency.HECTOHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 19), Sym("hectohz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Int(100)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Int(10), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Int(10)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 18), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 9), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Int(100), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Int(1000), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 6), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 15), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 12), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 24), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("hz")));
        c.get(UnitsFrequency.HERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 21), Sym("hz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 5)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Int(100)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Pow(10, 4)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 15), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 6), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Rat(Int(1), Int(10)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Int(1000)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Int(1000), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 12), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 9), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 27)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 21), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("kilohz")));
        c.get(UnitsFrequency.KILOHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 18), Sym("kilohz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 8)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Pow(10, 5)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Pow(10, 7)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 12), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Int(1000), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Rat(Int(1), Pow(10, 4)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 9), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 6), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 30)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 18), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 27)), Sym("megahz")));
        c.get(UnitsFrequency.MEGAHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 15), Sym("megahz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Pow(10, 4), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Pow(10, 7), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Pow(10, 5), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 24), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 15), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 8), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.HERTZ, Mul(Pow(10, 6), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 9), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 12), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Int(1000), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 21), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 18), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 30), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("microhz")));
        c.get(UnitsFrequency.MICROHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 27), Sym("microhz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Int(10), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Pow(10, 4), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Int(100), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 21), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 12), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 5), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.HERTZ, Mul(Int(1000), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 6), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 9), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 18), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 15), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 27), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("millihz")));
        c.get(UnitsFrequency.MILLIHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 24), Sym("millihz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Pow(10, 7), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Pow(10, 10), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Pow(10, 8), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 27), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 18), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 11), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.HERTZ, Mul(Pow(10, 9), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 12), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 15), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Int(1000), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Pow(10, 6), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 24), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 21), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 33), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("nanohz")));
        c.get(UnitsFrequency.NANOHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 30), Sym("nanohz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 33)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 17)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Pow(10, 14)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Pow(10, 16)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Int(1000), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 30)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Rat(Int(1), Pow(10, 13)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 27)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 39)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 9), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 36)), Sym("petahz")));
        c.get(UnitsFrequency.PETAHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 6), Sym("petahz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Pow(10, 10), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Pow(10, 13), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Pow(10, 11), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 30), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 21), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 14), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.HERTZ, Mul(Pow(10, 12), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 15), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 18), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Pow(10, 6), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Pow(10, 9), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Int(1000), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 27), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 24), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 36), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("picohz")));
        c.get(UnitsFrequency.PICOHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 33), Sym("picohz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 30)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 14)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Pow(10, 11)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Pow(10, 13)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 6), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 27)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Rat(Int(1), Pow(10, 10)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Int(1000), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 36)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 12), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 33)), Sym("terahz")));
        c.get(UnitsFrequency.TERAHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 9), Sym("terahz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Pow(10, 6), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Pow(10, 22), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Pow(10, 25), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Pow(10, 23), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 42), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Pow(10, 9), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 33), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 26), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.HERTZ, Mul(Pow(10, 24), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 27), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 30), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Pow(10, 18), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Pow(10, 21), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Pow(10, 15), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 39), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Pow(10, 12), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 36), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 48), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Int(1000), Sym("yoctohz")));
        c.get(UnitsFrequency.YOCTOHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 45), Sym("yoctohz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 42)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 26)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Pow(10, 23)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Pow(10, 25)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 39)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Rat(Int(1), Pow(10, 22)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 30)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 27)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 33)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 36)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 48)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 45)), Sym("yottahz")));
        c.get(UnitsFrequency.YOTTAHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("yottahz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Int(1000), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Pow(10, 19), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Pow(10, 22), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Pow(10, 20), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Pow(10, 39), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Pow(10, 6), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Pow(10, 30), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Pow(10, 23), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.HERTZ, Mul(Pow(10, 21), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Pow(10, 24), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Pow(10, 27), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Pow(10, 15), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Pow(10, 18), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Pow(10, 12), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Pow(10, 36), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Pow(10, 9), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Pow(10, 33), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Pow(10, 45), Sym("zeptohz")));
        c.get(UnitsFrequency.ZEPTOHERTZ).put(UnitsFrequency.ZETTAHERTZ, Mul(Pow(10, 42), Sym("zeptohz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.ATTOHERTZ, Mul(Rat(Int(1), Pow(10, 39)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.CENTIHERTZ, Mul(Rat(Int(1), Pow(10, 23)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.DECAHERTZ, Mul(Rat(Int(1), Pow(10, 20)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.DECIHERTZ, Mul(Rat(Int(1), Pow(10, 22)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.EXAHERTZ, Mul(Rat(Int(1), Int(1000)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.FEMTOHERTZ, Mul(Rat(Int(1), Pow(10, 36)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.GIGAHERTZ, Mul(Rat(Int(1), Pow(10, 12)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.HECTOHERTZ, Mul(Rat(Int(1), Pow(10, 19)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.HERTZ, Mul(Rat(Int(1), Pow(10, 21)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.KILOHERTZ, Mul(Rat(Int(1), Pow(10, 18)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.MEGAHERTZ, Mul(Rat(Int(1), Pow(10, 15)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.MICROHERTZ, Mul(Rat(Int(1), Pow(10, 27)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.MILLIHERTZ, Mul(Rat(Int(1), Pow(10, 24)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.NANOHERTZ, Mul(Rat(Int(1), Pow(10, 30)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.PETAHERTZ, Mul(Rat(Int(1), Pow(10, 6)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.PICOHERTZ, Mul(Rat(Int(1), Pow(10, 33)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.TERAHERTZ, Mul(Rat(Int(1), Pow(10, 9)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.YOCTOHERTZ, Mul(Rat(Int(1), Pow(10, 45)), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.YOTTAHERTZ, Mul(Int(1000), Sym("zettahz")));
        c.get(UnitsFrequency.ZETTAHERTZ).put(UnitsFrequency.ZEPTOHERTZ, Mul(Rat(Int(1), Pow(10, 42)), Sym("zettahz")));
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsFrequency, String> SYMBOLS;
    static {
        Map<UnitsFrequency, String> s = new HashMap<UnitsFrequency, String>();
        s.put(UnitsFrequency.ATTOHERTZ, "aHz");
        s.put(UnitsFrequency.CENTIHERTZ, "cHz");
        s.put(UnitsFrequency.DECAHERTZ, "daHz");
        s.put(UnitsFrequency.DECIHERTZ, "dHz");
        s.put(UnitsFrequency.EXAHERTZ, "EHz");
        s.put(UnitsFrequency.FEMTOHERTZ, "fHz");
        s.put(UnitsFrequency.GIGAHERTZ, "GHz");
        s.put(UnitsFrequency.HECTOHERTZ, "hHz");
        s.put(UnitsFrequency.HERTZ, "Hz");
        s.put(UnitsFrequency.KILOHERTZ, "kHz");
        s.put(UnitsFrequency.MEGAHERTZ, "MHz");
        s.put(UnitsFrequency.MICROHERTZ, "µHz");
        s.put(UnitsFrequency.MILLIHERTZ, "mHz");
        s.put(UnitsFrequency.NANOHERTZ, "nHz");
        s.put(UnitsFrequency.PETAHERTZ, "PHz");
        s.put(UnitsFrequency.PICOHERTZ, "pHz");
        s.put(UnitsFrequency.TERAHERTZ, "THz");
        s.put(UnitsFrequency.YOCTOHERTZ, "yHz");
        s.put(UnitsFrequency.YOTTAHERTZ, "YHz");
        s.put(UnitsFrequency.ZEPTOHERTZ, "zHz");
        s.put(UnitsFrequency.ZETTAHERTZ, "ZHz");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsFrequency unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new FrequencyI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsFrequency makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsFrequency
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Frequency unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Frequency makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Frequency> units =
                ome.xml.model.enums.handlers.UnitsFrequencyEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Frequency(d, units);
    }

   /**
    * FIXME: this should likely take a default so that locations which don't
    * want an exception can have
    *
    * log.warn("Using new PositiveFloat(1.0)!", e); return new
    * PositiveFloat(1.0);
    *
    * or similar.
    */
   public static ome.units.quantity.Frequency convert(Frequency t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsFrequency.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsFrequency units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Frequency> units2 =
               ome.xml.model.enums.handlers.UnitsFrequencyEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Frequency(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public FrequencyI() {
        super();
    }

    public FrequencyI(double d, UnitsFrequency unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public FrequencyI(double d,
            Unit<ome.units.quantity.Frequency> unit) {
        this(d, ome.model.enums.UnitsFrequency.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given ome-xml enum
    */
   public FrequencyI(Frequency value, Unit<ome.units.quantity.Frequency> ul) throws BigResult {
       this(value,
            ome.model.enums.UnitsFrequency.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given ome.model enum
    */
   public FrequencyI(double d, ome.model.enums.UnitsFrequency ul) {
        this(d, UnitsFrequency.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Frequency}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public FrequencyI(Frequency value, String target) throws BigResult {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
           setUnit(value.getUnit());
        } else {
            UnitsFrequency targetUnit = UnitsFrequency.valueOf(target);
            Conversion conversion = conversions.get(value.getUnit()).get(targetUnit);
            if (conversion == null) {
                throw new RuntimeException(String.format(
                    "%f %s cannot be converted to %s",
                        value.getValue(), value.getUnit(), target));
            }
            double orig = value.getValue();
            BigDecimal big = conversion.convert(orig);
            double converted = big.doubleValue();
            if (converted == Double.NEGATIVE_INFINITY ||
                    converted == Double.POSITIVE_INFINITY) {
                throw new BigResult(big,
                        "Failed to convert " + source + ":" + target);
            }

            setValue(converted);
            setUnit(targetUnit);
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public FrequencyI(Frequency value, UnitsFrequency target) throws BigResult {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public FrequencyI(ome.units.quantity.Frequency value) {
        ome.model.enums.UnitsFrequency internal =
            ome.model.enums.UnitsFrequency.bySymbol(value.unit().getSymbol());
        UnitsFrequency ul = UnitsFrequency.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsFrequency getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsFrequency unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Frequency copy(Ice.Current ignore) {
        FrequencyI copy = new FrequencyI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Frequency) {
            ome.model.units.Frequency t = (ome.model.units.Frequency) model;
            this.value = t.getValue();
            this.unit = UnitsFrequency.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Frequency cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsFrequency ut = ome.model.enums.UnitsFrequency.valueOf(getUnit().toString());
        ome.model.units.Frequency t = new ome.model.units.Frequency(getValue(), ut);
        return t;
    }

    // ~ Java overrides
    // =========================================================================

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Frequency(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Frequency other = (Frequency) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

