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

    private static final Map<String, Conversion> conversions;
    static {
        Map<String, Conversion> c = new HashMap<String, Conversion>();

        c.put("ATTOHERTZ:CENTIHERTZ", Mul(Pow(10, 16), Sym("attohz")));
        c.put("ATTOHERTZ:DECAHERTZ", Mul(Pow(10, 19), Sym("attohz")));
        c.put("ATTOHERTZ:DECIHERTZ", Mul(Pow(10, 17), Sym("attohz")));
        c.put("ATTOHERTZ:EXAHERTZ", Mul(Pow(10, 36), Sym("attohz")));
        c.put("ATTOHERTZ:FEMTOHERTZ", Mul(Int(1000), Sym("attohz")));
        c.put("ATTOHERTZ:GIGAHERTZ", Mul(Pow(10, 27), Sym("attohz")));
        c.put("ATTOHERTZ:HECTOHERTZ", Mul(Pow(10, 20), Sym("attohz")));
        c.put("ATTOHERTZ:HERTZ", Mul(Pow(10, 18), Sym("attohz")));
        c.put("ATTOHERTZ:KILOHERTZ", Mul(Pow(10, 21), Sym("attohz")));
        c.put("ATTOHERTZ:MEGAHERTZ", Mul(Pow(10, 24), Sym("attohz")));
        c.put("ATTOHERTZ:MICROHERTZ", Mul(Pow(10, 12), Sym("attohz")));
        c.put("ATTOHERTZ:MILLIHERTZ", Mul(Pow(10, 15), Sym("attohz")));
        c.put("ATTOHERTZ:NANOHERTZ", Mul(Pow(10, 9), Sym("attohz")));
        c.put("ATTOHERTZ:PETAHERTZ", Mul(Pow(10, 33), Sym("attohz")));
        c.put("ATTOHERTZ:PICOHERTZ", Mul(Pow(10, 6), Sym("attohz")));
        c.put("ATTOHERTZ:TERAHERTZ", Mul(Pow(10, 30), Sym("attohz")));
        c.put("ATTOHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("attohz")));
        c.put("ATTOHERTZ:YOTTAHERTZ", Mul(Pow(10, 42), Sym("attohz")));
        c.put("ATTOHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("attohz")));
        c.put("ATTOHERTZ:ZETTAHERTZ", Mul(Pow(10, 39), Sym("attohz")));
        c.put("CENTIHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 16)), Sym("centihz")));
        c.put("CENTIHERTZ:DECAHERTZ", Mul(Int(1000), Sym("centihz")));
        c.put("CENTIHERTZ:DECIHERTZ", Mul(Int(10), Sym("centihz")));
        c.put("CENTIHERTZ:EXAHERTZ", Mul(Pow(10, 20), Sym("centihz")));
        c.put("CENTIHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 13)), Sym("centihz")));
        c.put("CENTIHERTZ:GIGAHERTZ", Mul(Pow(10, 11), Sym("centihz")));
        c.put("CENTIHERTZ:HECTOHERTZ", Mul(Pow(10, 4), Sym("centihz")));
        c.put("CENTIHERTZ:HERTZ", Mul(Int(100), Sym("centihz")));
        c.put("CENTIHERTZ:KILOHERTZ", Mul(Pow(10, 5), Sym("centihz")));
        c.put("CENTIHERTZ:MEGAHERTZ", Mul(Pow(10, 8), Sym("centihz")));
        c.put("CENTIHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 4)), Sym("centihz")));
        c.put("CENTIHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Int(10)), Sym("centihz")));
        c.put("CENTIHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 7)), Sym("centihz")));
        c.put("CENTIHERTZ:PETAHERTZ", Mul(Pow(10, 17), Sym("centihz")));
        c.put("CENTIHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 10)), Sym("centihz")));
        c.put("CENTIHERTZ:TERAHERTZ", Mul(Pow(10, 14), Sym("centihz")));
        c.put("CENTIHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 22)), Sym("centihz")));
        c.put("CENTIHERTZ:YOTTAHERTZ", Mul(Pow(10, 26), Sym("centihz")));
        c.put("CENTIHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 19)), Sym("centihz")));
        c.put("CENTIHERTZ:ZETTAHERTZ", Mul(Pow(10, 23), Sym("centihz")));
        c.put("DECAHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 19)), Sym("decahz")));
        c.put("DECAHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("decahz")));
        c.put("DECAHERTZ:DECIHERTZ", Mul(Rat(Int(1), Int(100)), Sym("decahz")));
        c.put("DECAHERTZ:EXAHERTZ", Mul(Pow(10, 17), Sym("decahz")));
        c.put("DECAHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 16)), Sym("decahz")));
        c.put("DECAHERTZ:GIGAHERTZ", Mul(Pow(10, 8), Sym("decahz")));
        c.put("DECAHERTZ:HECTOHERTZ", Mul(Int(10), Sym("decahz")));
        c.put("DECAHERTZ:HERTZ", Mul(Rat(Int(1), Int(10)), Sym("decahz")));
        c.put("DECAHERTZ:KILOHERTZ", Mul(Int(100), Sym("decahz")));
        c.put("DECAHERTZ:MEGAHERTZ", Mul(Pow(10, 5), Sym("decahz")));
        c.put("DECAHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 7)), Sym("decahz")));
        c.put("DECAHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 4)), Sym("decahz")));
        c.put("DECAHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 10)), Sym("decahz")));
        c.put("DECAHERTZ:PETAHERTZ", Mul(Pow(10, 14), Sym("decahz")));
        c.put("DECAHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 13)), Sym("decahz")));
        c.put("DECAHERTZ:TERAHERTZ", Mul(Pow(10, 11), Sym("decahz")));
        c.put("DECAHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 25)), Sym("decahz")));
        c.put("DECAHERTZ:YOTTAHERTZ", Mul(Pow(10, 23), Sym("decahz")));
        c.put("DECAHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 22)), Sym("decahz")));
        c.put("DECAHERTZ:ZETTAHERTZ", Mul(Pow(10, 20), Sym("decahz")));
        c.put("DECIHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 17)), Sym("decihz")));
        c.put("DECIHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Int(10)), Sym("decihz")));
        c.put("DECIHERTZ:DECAHERTZ", Mul(Int(100), Sym("decihz")));
        c.put("DECIHERTZ:EXAHERTZ", Mul(Pow(10, 19), Sym("decihz")));
        c.put("DECIHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 14)), Sym("decihz")));
        c.put("DECIHERTZ:GIGAHERTZ", Mul(Pow(10, 10), Sym("decihz")));
        c.put("DECIHERTZ:HECTOHERTZ", Mul(Int(1000), Sym("decihz")));
        c.put("DECIHERTZ:HERTZ", Mul(Int(10), Sym("decihz")));
        c.put("DECIHERTZ:KILOHERTZ", Mul(Pow(10, 4), Sym("decihz")));
        c.put("DECIHERTZ:MEGAHERTZ", Mul(Pow(10, 7), Sym("decihz")));
        c.put("DECIHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 5)), Sym("decihz")));
        c.put("DECIHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Int(100)), Sym("decihz")));
        c.put("DECIHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 8)), Sym("decihz")));
        c.put("DECIHERTZ:PETAHERTZ", Mul(Pow(10, 16), Sym("decihz")));
        c.put("DECIHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 11)), Sym("decihz")));
        c.put("DECIHERTZ:TERAHERTZ", Mul(Pow(10, 13), Sym("decihz")));
        c.put("DECIHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 23)), Sym("decihz")));
        c.put("DECIHERTZ:YOTTAHERTZ", Mul(Pow(10, 25), Sym("decihz")));
        c.put("DECIHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 20)), Sym("decihz")));
        c.put("DECIHERTZ:ZETTAHERTZ", Mul(Pow(10, 22), Sym("decihz")));
        c.put("EXAHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 36)), Sym("exahz")));
        c.put("EXAHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 20)), Sym("exahz")));
        c.put("EXAHERTZ:DECAHERTZ", Mul(Rat(Int(1), Pow(10, 17)), Sym("exahz")));
        c.put("EXAHERTZ:DECIHERTZ", Mul(Rat(Int(1), Pow(10, 19)), Sym("exahz")));
        c.put("EXAHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 33)), Sym("exahz")));
        c.put("EXAHERTZ:GIGAHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("exahz")));
        c.put("EXAHERTZ:HECTOHERTZ", Mul(Rat(Int(1), Pow(10, 16)), Sym("exahz")));
        c.put("EXAHERTZ:HERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("exahz")));
        c.put("EXAHERTZ:KILOHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("exahz")));
        c.put("EXAHERTZ:MEGAHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("exahz")));
        c.put("EXAHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("exahz")));
        c.put("EXAHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("exahz")));
        c.put("EXAHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 27)), Sym("exahz")));
        c.put("EXAHERTZ:PETAHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("exahz")));
        c.put("EXAHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 30)), Sym("exahz")));
        c.put("EXAHERTZ:TERAHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("exahz")));
        c.put("EXAHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 42)), Sym("exahz")));
        c.put("EXAHERTZ:YOTTAHERTZ", Mul(Pow(10, 6), Sym("exahz")));
        c.put("EXAHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 39)), Sym("exahz")));
        c.put("EXAHERTZ:ZETTAHERTZ", Mul(Int(1000), Sym("exahz")));
        c.put("FEMTOHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("femtohz")));
        c.put("FEMTOHERTZ:CENTIHERTZ", Mul(Pow(10, 13), Sym("femtohz")));
        c.put("FEMTOHERTZ:DECAHERTZ", Mul(Pow(10, 16), Sym("femtohz")));
        c.put("FEMTOHERTZ:DECIHERTZ", Mul(Pow(10, 14), Sym("femtohz")));
        c.put("FEMTOHERTZ:EXAHERTZ", Mul(Pow(10, 33), Sym("femtohz")));
        c.put("FEMTOHERTZ:GIGAHERTZ", Mul(Pow(10, 24), Sym("femtohz")));
        c.put("FEMTOHERTZ:HECTOHERTZ", Mul(Pow(10, 17), Sym("femtohz")));
        c.put("FEMTOHERTZ:HERTZ", Mul(Pow(10, 15), Sym("femtohz")));
        c.put("FEMTOHERTZ:KILOHERTZ", Mul(Pow(10, 18), Sym("femtohz")));
        c.put("FEMTOHERTZ:MEGAHERTZ", Mul(Pow(10, 21), Sym("femtohz")));
        c.put("FEMTOHERTZ:MICROHERTZ", Mul(Pow(10, 9), Sym("femtohz")));
        c.put("FEMTOHERTZ:MILLIHERTZ", Mul(Pow(10, 12), Sym("femtohz")));
        c.put("FEMTOHERTZ:NANOHERTZ", Mul(Pow(10, 6), Sym("femtohz")));
        c.put("FEMTOHERTZ:PETAHERTZ", Mul(Pow(10, 30), Sym("femtohz")));
        c.put("FEMTOHERTZ:PICOHERTZ", Mul(Int(1000), Sym("femtohz")));
        c.put("FEMTOHERTZ:TERAHERTZ", Mul(Pow(10, 27), Sym("femtohz")));
        c.put("FEMTOHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("femtohz")));
        c.put("FEMTOHERTZ:YOTTAHERTZ", Mul(Pow(10, 39), Sym("femtohz")));
        c.put("FEMTOHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("femtohz")));
        c.put("FEMTOHERTZ:ZETTAHERTZ", Mul(Pow(10, 36), Sym("femtohz")));
        c.put("GIGAHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 27)), Sym("gigahz")));
        c.put("GIGAHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 11)), Sym("gigahz")));
        c.put("GIGAHERTZ:DECAHERTZ", Mul(Rat(Int(1), Pow(10, 8)), Sym("gigahz")));
        c.put("GIGAHERTZ:DECIHERTZ", Mul(Rat(Int(1), Pow(10, 10)), Sym("gigahz")));
        c.put("GIGAHERTZ:EXAHERTZ", Mul(Pow(10, 9), Sym("gigahz")));
        c.put("GIGAHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("gigahz")));
        c.put("GIGAHERTZ:HECTOHERTZ", Mul(Rat(Int(1), Pow(10, 7)), Sym("gigahz")));
        c.put("GIGAHERTZ:HERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("gigahz")));
        c.put("GIGAHERTZ:KILOHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("gigahz")));
        c.put("GIGAHERTZ:MEGAHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("gigahz")));
        c.put("GIGAHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("gigahz")));
        c.put("GIGAHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("gigahz")));
        c.put("GIGAHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("gigahz")));
        c.put("GIGAHERTZ:PETAHERTZ", Mul(Pow(10, 6), Sym("gigahz")));
        c.put("GIGAHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("gigahz")));
        c.put("GIGAHERTZ:TERAHERTZ", Mul(Int(1000), Sym("gigahz")));
        c.put("GIGAHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 33)), Sym("gigahz")));
        c.put("GIGAHERTZ:YOTTAHERTZ", Mul(Pow(10, 15), Sym("gigahz")));
        c.put("GIGAHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 30)), Sym("gigahz")));
        c.put("GIGAHERTZ:ZETTAHERTZ", Mul(Pow(10, 12), Sym("gigahz")));
        c.put("HECTOHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 20)), Sym("hectohz")));
        c.put("HECTOHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 4)), Sym("hectohz")));
        c.put("HECTOHERTZ:DECAHERTZ", Mul(Rat(Int(1), Int(10)), Sym("hectohz")));
        c.put("HECTOHERTZ:DECIHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("hectohz")));
        c.put("HECTOHERTZ:EXAHERTZ", Mul(Pow(10, 16), Sym("hectohz")));
        c.put("HECTOHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 17)), Sym("hectohz")));
        c.put("HECTOHERTZ:GIGAHERTZ", Mul(Pow(10, 7), Sym("hectohz")));
        c.put("HECTOHERTZ:HERTZ", Mul(Rat(Int(1), Int(100)), Sym("hectohz")));
        c.put("HECTOHERTZ:KILOHERTZ", Mul(Int(10), Sym("hectohz")));
        c.put("HECTOHERTZ:MEGAHERTZ", Mul(Pow(10, 4), Sym("hectohz")));
        c.put("HECTOHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 8)), Sym("hectohz")));
        c.put("HECTOHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 5)), Sym("hectohz")));
        c.put("HECTOHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 11)), Sym("hectohz")));
        c.put("HECTOHERTZ:PETAHERTZ", Mul(Pow(10, 13), Sym("hectohz")));
        c.put("HECTOHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 14)), Sym("hectohz")));
        c.put("HECTOHERTZ:TERAHERTZ", Mul(Pow(10, 10), Sym("hectohz")));
        c.put("HECTOHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 26)), Sym("hectohz")));
        c.put("HECTOHERTZ:YOTTAHERTZ", Mul(Pow(10, 22), Sym("hectohz")));
        c.put("HECTOHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 23)), Sym("hectohz")));
        c.put("HECTOHERTZ:ZETTAHERTZ", Mul(Pow(10, 19), Sym("hectohz")));
        c.put("HERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("hz")));
        c.put("HERTZ:CENTIHERTZ", Mul(Rat(Int(1), Int(100)), Sym("hz")));
        c.put("HERTZ:DECAHERTZ", Mul(Int(10), Sym("hz")));
        c.put("HERTZ:DECIHERTZ", Mul(Rat(Int(1), Int(10)), Sym("hz")));
        c.put("HERTZ:EXAHERTZ", Mul(Pow(10, 18), Sym("hz")));
        c.put("HERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("hz")));
        c.put("HERTZ:GIGAHERTZ", Mul(Pow(10, 9), Sym("hz")));
        c.put("HERTZ:HECTOHERTZ", Mul(Int(100), Sym("hz")));
        c.put("HERTZ:KILOHERTZ", Mul(Int(1000), Sym("hz")));
        c.put("HERTZ:MEGAHERTZ", Mul(Pow(10, 6), Sym("hz")));
        c.put("HERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("hz")));
        c.put("HERTZ:MILLIHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("hz")));
        c.put("HERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("hz")));
        c.put("HERTZ:PETAHERTZ", Mul(Pow(10, 15), Sym("hz")));
        c.put("HERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("hz")));
        c.put("HERTZ:TERAHERTZ", Mul(Pow(10, 12), Sym("hz")));
        c.put("HERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("hz")));
        c.put("HERTZ:YOTTAHERTZ", Mul(Pow(10, 24), Sym("hz")));
        c.put("HERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("hz")));
        c.put("HERTZ:ZETTAHERTZ", Mul(Pow(10, 21), Sym("hz")));
        c.put("KILOHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("kilohz")));
        c.put("KILOHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 5)), Sym("kilohz")));
        c.put("KILOHERTZ:DECAHERTZ", Mul(Rat(Int(1), Int(100)), Sym("kilohz")));
        c.put("KILOHERTZ:DECIHERTZ", Mul(Rat(Int(1), Pow(10, 4)), Sym("kilohz")));
        c.put("KILOHERTZ:EXAHERTZ", Mul(Pow(10, 15), Sym("kilohz")));
        c.put("KILOHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("kilohz")));
        c.put("KILOHERTZ:GIGAHERTZ", Mul(Pow(10, 6), Sym("kilohz")));
        c.put("KILOHERTZ:HECTOHERTZ", Mul(Rat(Int(1), Int(10)), Sym("kilohz")));
        c.put("KILOHERTZ:HERTZ", Mul(Rat(Int(1), Int(1000)), Sym("kilohz")));
        c.put("KILOHERTZ:MEGAHERTZ", Mul(Int(1000), Sym("kilohz")));
        c.put("KILOHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("kilohz")));
        c.put("KILOHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("kilohz")));
        c.put("KILOHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("kilohz")));
        c.put("KILOHERTZ:PETAHERTZ", Mul(Pow(10, 12), Sym("kilohz")));
        c.put("KILOHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("kilohz")));
        c.put("KILOHERTZ:TERAHERTZ", Mul(Pow(10, 9), Sym("kilohz")));
        c.put("KILOHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 27)), Sym("kilohz")));
        c.put("KILOHERTZ:YOTTAHERTZ", Mul(Pow(10, 21), Sym("kilohz")));
        c.put("KILOHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("kilohz")));
        c.put("KILOHERTZ:ZETTAHERTZ", Mul(Pow(10, 18), Sym("kilohz")));
        c.put("MEGAHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("megahz")));
        c.put("MEGAHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 8)), Sym("megahz")));
        c.put("MEGAHERTZ:DECAHERTZ", Mul(Rat(Int(1), Pow(10, 5)), Sym("megahz")));
        c.put("MEGAHERTZ:DECIHERTZ", Mul(Rat(Int(1), Pow(10, 7)), Sym("megahz")));
        c.put("MEGAHERTZ:EXAHERTZ", Mul(Pow(10, 12), Sym("megahz")));
        c.put("MEGAHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("megahz")));
        c.put("MEGAHERTZ:GIGAHERTZ", Mul(Int(1000), Sym("megahz")));
        c.put("MEGAHERTZ:HECTOHERTZ", Mul(Rat(Int(1), Pow(10, 4)), Sym("megahz")));
        c.put("MEGAHERTZ:HERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("megahz")));
        c.put("MEGAHERTZ:KILOHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("megahz")));
        c.put("MEGAHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("megahz")));
        c.put("MEGAHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("megahz")));
        c.put("MEGAHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("megahz")));
        c.put("MEGAHERTZ:PETAHERTZ", Mul(Pow(10, 9), Sym("megahz")));
        c.put("MEGAHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("megahz")));
        c.put("MEGAHERTZ:TERAHERTZ", Mul(Pow(10, 6), Sym("megahz")));
        c.put("MEGAHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 30)), Sym("megahz")));
        c.put("MEGAHERTZ:YOTTAHERTZ", Mul(Pow(10, 18), Sym("megahz")));
        c.put("MEGAHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 27)), Sym("megahz")));
        c.put("MEGAHERTZ:ZETTAHERTZ", Mul(Pow(10, 15), Sym("megahz")));
        c.put("MICROHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("microhz")));
        c.put("MICROHERTZ:CENTIHERTZ", Mul(Pow(10, 4), Sym("microhz")));
        c.put("MICROHERTZ:DECAHERTZ", Mul(Pow(10, 7), Sym("microhz")));
        c.put("MICROHERTZ:DECIHERTZ", Mul(Pow(10, 5), Sym("microhz")));
        c.put("MICROHERTZ:EXAHERTZ", Mul(Pow(10, 24), Sym("microhz")));
        c.put("MICROHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("microhz")));
        c.put("MICROHERTZ:GIGAHERTZ", Mul(Pow(10, 15), Sym("microhz")));
        c.put("MICROHERTZ:HECTOHERTZ", Mul(Pow(10, 8), Sym("microhz")));
        c.put("MICROHERTZ:HERTZ", Mul(Pow(10, 6), Sym("microhz")));
        c.put("MICROHERTZ:KILOHERTZ", Mul(Pow(10, 9), Sym("microhz")));
        c.put("MICROHERTZ:MEGAHERTZ", Mul(Pow(10, 12), Sym("microhz")));
        c.put("MICROHERTZ:MILLIHERTZ", Mul(Int(1000), Sym("microhz")));
        c.put("MICROHERTZ:NANOHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("microhz")));
        c.put("MICROHERTZ:PETAHERTZ", Mul(Pow(10, 21), Sym("microhz")));
        c.put("MICROHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("microhz")));
        c.put("MICROHERTZ:TERAHERTZ", Mul(Pow(10, 18), Sym("microhz")));
        c.put("MICROHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("microhz")));
        c.put("MICROHERTZ:YOTTAHERTZ", Mul(Pow(10, 30), Sym("microhz")));
        c.put("MICROHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("microhz")));
        c.put("MICROHERTZ:ZETTAHERTZ", Mul(Pow(10, 27), Sym("microhz")));
        c.put("MILLIHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("millihz")));
        c.put("MILLIHERTZ:CENTIHERTZ", Mul(Int(10), Sym("millihz")));
        c.put("MILLIHERTZ:DECAHERTZ", Mul(Pow(10, 4), Sym("millihz")));
        c.put("MILLIHERTZ:DECIHERTZ", Mul(Int(100), Sym("millihz")));
        c.put("MILLIHERTZ:EXAHERTZ", Mul(Pow(10, 21), Sym("millihz")));
        c.put("MILLIHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("millihz")));
        c.put("MILLIHERTZ:GIGAHERTZ", Mul(Pow(10, 12), Sym("millihz")));
        c.put("MILLIHERTZ:HECTOHERTZ", Mul(Pow(10, 5), Sym("millihz")));
        c.put("MILLIHERTZ:HERTZ", Mul(Int(1000), Sym("millihz")));
        c.put("MILLIHERTZ:KILOHERTZ", Mul(Pow(10, 6), Sym("millihz")));
        c.put("MILLIHERTZ:MEGAHERTZ", Mul(Pow(10, 9), Sym("millihz")));
        c.put("MILLIHERTZ:MICROHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("millihz")));
        c.put("MILLIHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("millihz")));
        c.put("MILLIHERTZ:PETAHERTZ", Mul(Pow(10, 18), Sym("millihz")));
        c.put("MILLIHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("millihz")));
        c.put("MILLIHERTZ:TERAHERTZ", Mul(Pow(10, 15), Sym("millihz")));
        c.put("MILLIHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("millihz")));
        c.put("MILLIHERTZ:YOTTAHERTZ", Mul(Pow(10, 27), Sym("millihz")));
        c.put("MILLIHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("millihz")));
        c.put("MILLIHERTZ:ZETTAHERTZ", Mul(Pow(10, 24), Sym("millihz")));
        c.put("NANOHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("nanohz")));
        c.put("NANOHERTZ:CENTIHERTZ", Mul(Pow(10, 7), Sym("nanohz")));
        c.put("NANOHERTZ:DECAHERTZ", Mul(Pow(10, 10), Sym("nanohz")));
        c.put("NANOHERTZ:DECIHERTZ", Mul(Pow(10, 8), Sym("nanohz")));
        c.put("NANOHERTZ:EXAHERTZ", Mul(Pow(10, 27), Sym("nanohz")));
        c.put("NANOHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("nanohz")));
        c.put("NANOHERTZ:GIGAHERTZ", Mul(Pow(10, 18), Sym("nanohz")));
        c.put("NANOHERTZ:HECTOHERTZ", Mul(Pow(10, 11), Sym("nanohz")));
        c.put("NANOHERTZ:HERTZ", Mul(Pow(10, 9), Sym("nanohz")));
        c.put("NANOHERTZ:KILOHERTZ", Mul(Pow(10, 12), Sym("nanohz")));
        c.put("NANOHERTZ:MEGAHERTZ", Mul(Pow(10, 15), Sym("nanohz")));
        c.put("NANOHERTZ:MICROHERTZ", Mul(Int(1000), Sym("nanohz")));
        c.put("NANOHERTZ:MILLIHERTZ", Mul(Pow(10, 6), Sym("nanohz")));
        c.put("NANOHERTZ:PETAHERTZ", Mul(Pow(10, 24), Sym("nanohz")));
        c.put("NANOHERTZ:PICOHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("nanohz")));
        c.put("NANOHERTZ:TERAHERTZ", Mul(Pow(10, 21), Sym("nanohz")));
        c.put("NANOHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("nanohz")));
        c.put("NANOHERTZ:YOTTAHERTZ", Mul(Pow(10, 33), Sym("nanohz")));
        c.put("NANOHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("nanohz")));
        c.put("NANOHERTZ:ZETTAHERTZ", Mul(Pow(10, 30), Sym("nanohz")));
        c.put("PETAHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 33)), Sym("petahz")));
        c.put("PETAHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 17)), Sym("petahz")));
        c.put("PETAHERTZ:DECAHERTZ", Mul(Rat(Int(1), Pow(10, 14)), Sym("petahz")));
        c.put("PETAHERTZ:DECIHERTZ", Mul(Rat(Int(1), Pow(10, 16)), Sym("petahz")));
        c.put("PETAHERTZ:EXAHERTZ", Mul(Int(1000), Sym("petahz")));
        c.put("PETAHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 30)), Sym("petahz")));
        c.put("PETAHERTZ:GIGAHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("petahz")));
        c.put("PETAHERTZ:HECTOHERTZ", Mul(Rat(Int(1), Pow(10, 13)), Sym("petahz")));
        c.put("PETAHERTZ:HERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("petahz")));
        c.put("PETAHERTZ:KILOHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("petahz")));
        c.put("PETAHERTZ:MEGAHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("petahz")));
        c.put("PETAHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("petahz")));
        c.put("PETAHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("petahz")));
        c.put("PETAHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("petahz")));
        c.put("PETAHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 27)), Sym("petahz")));
        c.put("PETAHERTZ:TERAHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("petahz")));
        c.put("PETAHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 39)), Sym("petahz")));
        c.put("PETAHERTZ:YOTTAHERTZ", Mul(Pow(10, 9), Sym("petahz")));
        c.put("PETAHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 36)), Sym("petahz")));
        c.put("PETAHERTZ:ZETTAHERTZ", Mul(Pow(10, 6), Sym("petahz")));
        c.put("PICOHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("picohz")));
        c.put("PICOHERTZ:CENTIHERTZ", Mul(Pow(10, 10), Sym("picohz")));
        c.put("PICOHERTZ:DECAHERTZ", Mul(Pow(10, 13), Sym("picohz")));
        c.put("PICOHERTZ:DECIHERTZ", Mul(Pow(10, 11), Sym("picohz")));
        c.put("PICOHERTZ:EXAHERTZ", Mul(Pow(10, 30), Sym("picohz")));
        c.put("PICOHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("picohz")));
        c.put("PICOHERTZ:GIGAHERTZ", Mul(Pow(10, 21), Sym("picohz")));
        c.put("PICOHERTZ:HECTOHERTZ", Mul(Pow(10, 14), Sym("picohz")));
        c.put("PICOHERTZ:HERTZ", Mul(Pow(10, 12), Sym("picohz")));
        c.put("PICOHERTZ:KILOHERTZ", Mul(Pow(10, 15), Sym("picohz")));
        c.put("PICOHERTZ:MEGAHERTZ", Mul(Pow(10, 18), Sym("picohz")));
        c.put("PICOHERTZ:MICROHERTZ", Mul(Pow(10, 6), Sym("picohz")));
        c.put("PICOHERTZ:MILLIHERTZ", Mul(Pow(10, 9), Sym("picohz")));
        c.put("PICOHERTZ:NANOHERTZ", Mul(Int(1000), Sym("picohz")));
        c.put("PICOHERTZ:PETAHERTZ", Mul(Pow(10, 27), Sym("picohz")));
        c.put("PICOHERTZ:TERAHERTZ", Mul(Pow(10, 24), Sym("picohz")));
        c.put("PICOHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("picohz")));
        c.put("PICOHERTZ:YOTTAHERTZ", Mul(Pow(10, 36), Sym("picohz")));
        c.put("PICOHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("picohz")));
        c.put("PICOHERTZ:ZETTAHERTZ", Mul(Pow(10, 33), Sym("picohz")));
        c.put("TERAHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 30)), Sym("terahz")));
        c.put("TERAHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 14)), Sym("terahz")));
        c.put("TERAHERTZ:DECAHERTZ", Mul(Rat(Int(1), Pow(10, 11)), Sym("terahz")));
        c.put("TERAHERTZ:DECIHERTZ", Mul(Rat(Int(1), Pow(10, 13)), Sym("terahz")));
        c.put("TERAHERTZ:EXAHERTZ", Mul(Pow(10, 6), Sym("terahz")));
        c.put("TERAHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 27)), Sym("terahz")));
        c.put("TERAHERTZ:GIGAHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("terahz")));
        c.put("TERAHERTZ:HECTOHERTZ", Mul(Rat(Int(1), Pow(10, 10)), Sym("terahz")));
        c.put("TERAHERTZ:HERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("terahz")));
        c.put("TERAHERTZ:KILOHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("terahz")));
        c.put("TERAHERTZ:MEGAHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("terahz")));
        c.put("TERAHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("terahz")));
        c.put("TERAHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("terahz")));
        c.put("TERAHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("terahz")));
        c.put("TERAHERTZ:PETAHERTZ", Mul(Int(1000), Sym("terahz")));
        c.put("TERAHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("terahz")));
        c.put("TERAHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 36)), Sym("terahz")));
        c.put("TERAHERTZ:YOTTAHERTZ", Mul(Pow(10, 12), Sym("terahz")));
        c.put("TERAHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 33)), Sym("terahz")));
        c.put("TERAHERTZ:ZETTAHERTZ", Mul(Pow(10, 9), Sym("terahz")));
        c.put("YOCTOHERTZ:ATTOHERTZ", Mul(Pow(10, 6), Sym("yoctohz")));
        c.put("YOCTOHERTZ:CENTIHERTZ", Mul(Pow(10, 22), Sym("yoctohz")));
        c.put("YOCTOHERTZ:DECAHERTZ", Mul(Pow(10, 25), Sym("yoctohz")));
        c.put("YOCTOHERTZ:DECIHERTZ", Mul(Pow(10, 23), Sym("yoctohz")));
        c.put("YOCTOHERTZ:EXAHERTZ", Mul(Pow(10, 42), Sym("yoctohz")));
        c.put("YOCTOHERTZ:FEMTOHERTZ", Mul(Pow(10, 9), Sym("yoctohz")));
        c.put("YOCTOHERTZ:GIGAHERTZ", Mul(Pow(10, 33), Sym("yoctohz")));
        c.put("YOCTOHERTZ:HECTOHERTZ", Mul(Pow(10, 26), Sym("yoctohz")));
        c.put("YOCTOHERTZ:HERTZ", Mul(Pow(10, 24), Sym("yoctohz")));
        c.put("YOCTOHERTZ:KILOHERTZ", Mul(Pow(10, 27), Sym("yoctohz")));
        c.put("YOCTOHERTZ:MEGAHERTZ", Mul(Pow(10, 30), Sym("yoctohz")));
        c.put("YOCTOHERTZ:MICROHERTZ", Mul(Pow(10, 18), Sym("yoctohz")));
        c.put("YOCTOHERTZ:MILLIHERTZ", Mul(Pow(10, 21), Sym("yoctohz")));
        c.put("YOCTOHERTZ:NANOHERTZ", Mul(Pow(10, 15), Sym("yoctohz")));
        c.put("YOCTOHERTZ:PETAHERTZ", Mul(Pow(10, 39), Sym("yoctohz")));
        c.put("YOCTOHERTZ:PICOHERTZ", Mul(Pow(10, 12), Sym("yoctohz")));
        c.put("YOCTOHERTZ:TERAHERTZ", Mul(Pow(10, 36), Sym("yoctohz")));
        c.put("YOCTOHERTZ:YOTTAHERTZ", Mul(Pow(10, 48), Sym("yoctohz")));
        c.put("YOCTOHERTZ:ZEPTOHERTZ", Mul(Int(1000), Sym("yoctohz")));
        c.put("YOCTOHERTZ:ZETTAHERTZ", Mul(Pow(10, 45), Sym("yoctohz")));
        c.put("YOTTAHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 42)), Sym("yottahz")));
        c.put("YOTTAHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 26)), Sym("yottahz")));
        c.put("YOTTAHERTZ:DECAHERTZ", Mul(Rat(Int(1), Pow(10, 23)), Sym("yottahz")));
        c.put("YOTTAHERTZ:DECIHERTZ", Mul(Rat(Int(1), Pow(10, 25)), Sym("yottahz")));
        c.put("YOTTAHERTZ:EXAHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("yottahz")));
        c.put("YOTTAHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 39)), Sym("yottahz")));
        c.put("YOTTAHERTZ:GIGAHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("yottahz")));
        c.put("YOTTAHERTZ:HECTOHERTZ", Mul(Rat(Int(1), Pow(10, 22)), Sym("yottahz")));
        c.put("YOTTAHERTZ:HERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("yottahz")));
        c.put("YOTTAHERTZ:KILOHERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("yottahz")));
        c.put("YOTTAHERTZ:MEGAHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("yottahz")));
        c.put("YOTTAHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 30)), Sym("yottahz")));
        c.put("YOTTAHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 27)), Sym("yottahz")));
        c.put("YOTTAHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 33)), Sym("yottahz")));
        c.put("YOTTAHERTZ:PETAHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("yottahz")));
        c.put("YOTTAHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 36)), Sym("yottahz")));
        c.put("YOTTAHERTZ:TERAHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("yottahz")));
        c.put("YOTTAHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 48)), Sym("yottahz")));
        c.put("YOTTAHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 45)), Sym("yottahz")));
        c.put("YOTTAHERTZ:ZETTAHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("yottahz")));
        c.put("ZEPTOHERTZ:ATTOHERTZ", Mul(Int(1000), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:CENTIHERTZ", Mul(Pow(10, 19), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:DECAHERTZ", Mul(Pow(10, 22), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:DECIHERTZ", Mul(Pow(10, 20), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:EXAHERTZ", Mul(Pow(10, 39), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:FEMTOHERTZ", Mul(Pow(10, 6), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:GIGAHERTZ", Mul(Pow(10, 30), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:HECTOHERTZ", Mul(Pow(10, 23), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:HERTZ", Mul(Pow(10, 21), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:KILOHERTZ", Mul(Pow(10, 24), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:MEGAHERTZ", Mul(Pow(10, 27), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:MICROHERTZ", Mul(Pow(10, 15), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:MILLIHERTZ", Mul(Pow(10, 18), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:NANOHERTZ", Mul(Pow(10, 12), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:PETAHERTZ", Mul(Pow(10, 36), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:PICOHERTZ", Mul(Pow(10, 9), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:TERAHERTZ", Mul(Pow(10, 33), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:YOTTAHERTZ", Mul(Pow(10, 45), Sym("zeptohz")));
        c.put("ZEPTOHERTZ:ZETTAHERTZ", Mul(Pow(10, 42), Sym("zeptohz")));
        c.put("ZETTAHERTZ:ATTOHERTZ", Mul(Rat(Int(1), Pow(10, 39)), Sym("zettahz")));
        c.put("ZETTAHERTZ:CENTIHERTZ", Mul(Rat(Int(1), Pow(10, 23)), Sym("zettahz")));
        c.put("ZETTAHERTZ:DECAHERTZ", Mul(Rat(Int(1), Pow(10, 20)), Sym("zettahz")));
        c.put("ZETTAHERTZ:DECIHERTZ", Mul(Rat(Int(1), Pow(10, 22)), Sym("zettahz")));
        c.put("ZETTAHERTZ:EXAHERTZ", Mul(Rat(Int(1), Int(1000)), Sym("zettahz")));
        c.put("ZETTAHERTZ:FEMTOHERTZ", Mul(Rat(Int(1), Pow(10, 36)), Sym("zettahz")));
        c.put("ZETTAHERTZ:GIGAHERTZ", Mul(Rat(Int(1), Pow(10, 12)), Sym("zettahz")));
        c.put("ZETTAHERTZ:HECTOHERTZ", Mul(Rat(Int(1), Pow(10, 19)), Sym("zettahz")));
        c.put("ZETTAHERTZ:HERTZ", Mul(Rat(Int(1), Pow(10, 21)), Sym("zettahz")));
        c.put("ZETTAHERTZ:KILOHERTZ", Mul(Rat(Int(1), Pow(10, 18)), Sym("zettahz")));
        c.put("ZETTAHERTZ:MEGAHERTZ", Mul(Rat(Int(1), Pow(10, 15)), Sym("zettahz")));
        c.put("ZETTAHERTZ:MICROHERTZ", Mul(Rat(Int(1), Pow(10, 27)), Sym("zettahz")));
        c.put("ZETTAHERTZ:MILLIHERTZ", Mul(Rat(Int(1), Pow(10, 24)), Sym("zettahz")));
        c.put("ZETTAHERTZ:NANOHERTZ", Mul(Rat(Int(1), Pow(10, 30)), Sym("zettahz")));
        c.put("ZETTAHERTZ:PETAHERTZ", Mul(Rat(Int(1), Pow(10, 6)), Sym("zettahz")));
        c.put("ZETTAHERTZ:PICOHERTZ", Mul(Rat(Int(1), Pow(10, 33)), Sym("zettahz")));
        c.put("ZETTAHERTZ:TERAHERTZ", Mul(Rat(Int(1), Pow(10, 9)), Sym("zettahz")));
        c.put("ZETTAHERTZ:YOCTOHERTZ", Mul(Rat(Int(1), Pow(10, 45)), Sym("zettahz")));
        c.put("ZETTAHERTZ:YOTTAHERTZ", Mul(Int(1000), Sym("zettahz")));
        c.put("ZETTAHERTZ:ZEPTOHERTZ", Mul(Rat(Int(1), Pow(10, 42)), Sym("zettahz")));
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
            Conversion conversion = conversions.get(source + ":" + target);
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
            setUnit(UnitsFrequency.valueOf(target));
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

