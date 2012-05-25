/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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

import Ice.Current;
import Ice.Object;

import ome.model.ModelBased;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.util.Utils;
import omero.RInt;
import omero.RLong;
import static omero.rtypes.rint;

/**
 * Blitz representation of AffineTransform.
 *
 * @since Beta4.4
 */
public class AffineTransformI extends AffineTransform implements ModelBased {

    public final static Ice.ObjectFactory Factory = new Ice.ObjectFactory() {

        public Object create(String arg0) {
            return new AffineTransformI();
        }

        public void destroy() {
            // no-op
        }

    };

    public AffineTransformI() {
        this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public AffineTransformI(double a00, double a01,double a10, double a11,double a02, double a12) {
        this.a00 = a00;
        this.a01 = a01;
        this.a10 = a10;
        this.a11 = a11;
        this.a02 = a02;
        this.a12 = a12;
    }

    public double getA00(Ice.Current current) {
        return a00;
    }

    public void setA00(double a00, Ice.Current current) {
        this.a00 = a00;
    }

    public double getA01(Ice.Current current) {
        return a01;
    }

    public void setA01(double a01, Ice.Current current) {
        this.a01 = a01;
    }

    public double getA10(Ice.Current current) {
        return a10;
    }

    public void setA10(double a10, Ice.Current current) {
        this.a10 = a10;
    }

    public double getA11(Ice.Current current) {
        return a11;
    }

    public void setA11(double a11, Ice.Current current) {
        this.a11 = a11;
    }

    public double getA02(Ice.Current current) {
        return a02;
    }

    public void setA02(double a02, Ice.Current current) {
        this.a02 = a02;
    }

    public double getA12(Ice.Current current) {
        return a12;
    }

    public void setA12(double a12, Ice.Current current) {
        this.a12 = a12;
    }

    public AffineTransform proxy(Current __current) {
        return new AffineTransformI(a00, a01, a10, a11, a02, a12);
    }

    public void copyObject(Filterable model, ModelMapper _mapper) {
        if (model instanceof ome.model.core.AffineTransform) {
            ome.model.core.AffineTransform source = (ome.model.core.AffineTransform) model;
            this.setA00(source.getA00());
            this.setA01(source.getA01());
            this.setA10(source.getA10());
            this.setA11(source.getA11());
            this.setA02(source.getA02());
            this.setA12(source.getA12());
        } else {
            throw new IllegalArgumentException("AffineTransform cannot copy from "
                    + (model == null ? "null" : model.getClass().getName()));
        }
    }

    public ome.util.Filterable fillObject(ome.util.ReverseModelMapper _mapper) {
        omero.util.IceMapper mapper = (omero.util.IceMapper) _mapper;
        ome.model.core.AffineTransform target = new ome.model.core.AffineTransform(
                a00, a01, a10, a11, a02, a12);
        mapper.store(this, target);
        return target;
    }

}
