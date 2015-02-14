/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.file.modulo;


//Java imports
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

/**
 * Holds information about the modulo tag.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ModuloInfo {

    /** Identifies modulo along C.*/
    public static final int C = 0;

    /** Identifies modulo along Z.*/
    public static final int Z = 1;

    /** Identifies modulo along T.*/
    public static final int T = 2;

    /** Identifies the <code>ModuloAlongT</code>.*/
    static final String MODULO_T = "ModuloAlongT";

    /** Identifies the <code>ModuloAlongZ</code>.*/
    static final String MODULO_Z = "ModuloAlongZ";

    /** Identifies the <code>ModuloAlongC</code>.*/
    static final String MODULO_C = "ModuloAlongC";

    /** Identifies the <code>End</code>.*/
    static final String END = "End";

    /** Identifies the <code>Start</code>.*/
    static final String START = "Start";

    /** Identifies the <code>Step</code>.*/
    static final String STEP = "Step";

    /** Identifies the <code>Type</code>.*/
    static final String TYPE = "Type";

    /** Identifies the <code>TypeDescription</code>.*/
    static final String TYPE_DESCRIPTION = "TypeDescription";

    /** Identifies the <code>Unit</code>.*/
    static final String UNIT = "Unit";

    /** Identifies the <code>Label</code>.*/
    static final String LABEL = "Label";

    /** The value of the <code>End</code> tag.*/
    private double end;

    /** The value of the <code>Start</code> tag.*/
    private double start;

    /** The value of the <code>Unit</code> tag.*/
    private String unit;

    /** The value of the <code>Type</code> tag.*/
    private String type;

    /** The value of the <code>Step</code> tag.*/
    private double step;

    /** The value of the <code>TypeDescription</code> tag.*/
    private String typeDescription;

    /** Indicates along which dimension the modulo applies./*/
    private int modulo;

    /** The value of the label tags.*/
    private List<Double> labels;

    /** 
     * Creates a new instance and determines the dimension the modulo property
     * applies to.
     *
     * @param moduloAlong
     */
    ModuloInfo(String moduloAlong)
    {
        if (CommonsLangUtils.isEmpty(moduloAlong))
            throw new IllegalArgumentException("Dimension not supported.");
        modulo = -1;
        if (MODULO_C.equals(moduloAlong))
            modulo = C;
        else if (MODULO_Z.equals(moduloAlong))
            modulo = Z;
        else if (MODULO_T.equals(moduloAlong))
            modulo = T;
        if (modulo == -1)
            throw new IllegalArgumentException("Dimension not supported.");
    }

    /**
     * Sets the labels.
     *
     * @param labels The value to set.
     */
    public void setLabels(List<Double> labels)
    {
        this.labels = labels;
    }

    /**
     * Returns the direction along which the modulo applies.
     *
     * @return See above.
     */
    public int getModuloIndex() { return modulo; }

    /**
     * Sets the value of the <code>End</code> tag.
     *
     * @param value The value to set.
     */
    public void setEnd(double value) { end = value; }

    /**
     * Sets the value of the <code>Start</code> tag.
     *
     * @param value The value to set.
     */
    public void setStart(double value) { start = value; }

    /**
     * Sets the value of the <code>Type</code> tag.
     *
     * @param value The value to set.
     */
    public void setType(String value) { type = value; }

    /**
     * Sets the value of the <code>TypeDescription</code> tag.
     *
     * @param value The value to set.
     */
    public void setTypeDescription(String value) { typeDescription = value; }

    /**
     * Sets the value of the <code>Unit</code> tag.
     *
     * @param value The value to set.
     */
    public void setUnit(String value) { unit = value; }

    /**
     * Sets the value of the <code>Step</code> tag.
     *
     * @param value The value to set.
     */
    public void setStep(double value) { step = value; }
    
    /**
     * Returns value of the <code>End</code> tag.
     *
     * @return See above.
     */
    public double getEnd() { return end; }

    /**
     * Returns value of the <code>Start</code> tag.
     *
     * @return See above.
     */
    public double getStart() { return start; }

    /**
     * Returns value of the <code>Unit</code> tag.
     *
     * @return See above.
     */
    public String getUnit() { return unit; }

    /**
     * Returns value of the <code>Step</code> tag.
     *
     * @return See above.
     */
    public double getStep() { return step; }

    /**
     * Returns value of the <code>Type</code> tag.
     *
     * @return See above.
     */
    public String getType() { return type; }

    /**
     * Returns value of the <code>TypeDescription</code> tag.
     *
     * @return See above.
     */
    public String getTypeDescription() { return typeDescription; }

    /**
     * Returns the real value corresponding to the specified bin.
     * 
     * @param bin The selected bin.
     * @return See above.
     */
    public double getRealValue(int bin)
    {
        if (CollectionUtils.isEmpty(labels)) return start+bin*step;
        if (bin < 0) bin = 0;
        if (bin >= labels.size()) bin = labels.size()-1;
        return start+labels.get(bin);
    }

    /**
     * Determines the modulo size.
     * 
     * @return See above.
     */
    public int getSize()
    {
        if (!CollectionUtils.isEmpty(labels)) return labels.size();
        if (step == 0) step = 1;
        return (int) ((end-start)/step)+1;
    }
}
