/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.model.utests;

import org.testng.annotations.Test;

import ome.model.acquisition.Laser;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.TransmittanceRange;
import ome.model.core.LogicalChannel;
import ome.model.core.Pixels;
import ome.model.enums.UnitsLength;
import ome.model.units.Length;

/**
 * Test that client-side code enforces the special cases of column domain constraints enumerated in <tt>psql-footer.vm</tt>.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.1
 */
public class PropertyConstraintTest {

    /* Laser.wavelength is a positive float */

    @Test
    public void testPositiveLaserWavelength() {
        new Laser().setWavelength(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroLaserWavelength() {
        new Laser().setWavelength(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeLaserWavelength() {
        new Laser().setWavelength(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* LightSettings.wavelength is a positive float */

    @Test
    public void testPositiveLightSettingsWavelength() {
        new LightSettings().setWavelength(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroLightSettingsWavelength() {
        new LightSettings().setWavelength(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeLightSettingsWavelength() {
        new LightSettings().setWavelength(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* LogicalChannel.emissionWave is a positive float */

    @Test
    public void testPositiveLogicalChannelEmissionWave() {
        new LogicalChannel().setEmissionWave(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroLogicalChannelEmissionWave() {
        new LogicalChannel().setEmissionWave(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeLogicalChannelEmissionWave() {
        new LogicalChannel().setEmissionWave(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* LogicalChannel.excitationWave is a positive float */

    @Test
    public void testPositiveLogicalChannelExcitationWave() {
        new LogicalChannel().setExcitationWave(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroLogicalChannelExcitationWave() {
        new LogicalChannel().setExcitationWave(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeLogicalChannelExcitationWave() {
        new LogicalChannel().setExcitationWave(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* Pixels.physicalSizeX is a positive float */

    @Test
    public void testPositivePixelsPhysicalSizeX() {
        new Pixels().setPhysicalSizeX(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroPixelsPhysicalSizeX() {
        new Pixels().setPhysicalSizeX(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativePixelsPhysicalSizeX() {
        new Pixels().setPhysicalSizeX(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* Pixels.physicalSizeY is a positive float */

    @Test
    public void testPositivePixelsPhysicalSizeY() {
        new Pixels().setPhysicalSizeY(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroPixelsPhysicalSizeY() {
        new Pixels().setPhysicalSizeY(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativePixelsPhysicalSizeY() {
        new Pixels().setPhysicalSizeY(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* Pixels.physicalSizeZ is a positive float */

    @Test
    public void testPositivePixelsPhysicalSizeZ() {
        new Pixels().setPhysicalSizeZ(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroPixelsPhysicalSizeZ() {
        new Pixels().setPhysicalSizeZ(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativePixelsPhysicalSizeZ() {
        new Pixels().setPhysicalSizeZ(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* TransmittanceRange.cutIn is a positive float */

    @Test
    public void testPositiveTransmittanceRangeCutIn() {
        new TransmittanceRange().setCutIn(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroTransmittanceRangeCutIn() {
        new TransmittanceRange().setCutIn(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeTransmittanceRangeCutIn() {
        new TransmittanceRange().setCutIn(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* TransmittanceRange.cutOut is a positive float */

    @Test
    public void testPositiveTransmittanceRangeCutOut() {
        new TransmittanceRange().setCutOut(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testZeroTransmittanceRangeCutOut() {
        new TransmittanceRange().setCutOut(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeTransmittanceRangeCutOut() {
        new TransmittanceRange().setCutOut(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* TransmittanceRange.cutInTolerance is a nonnegative float */

    @Test
    public void testPositiveTransmittanceRangeCutInTolerance() {
        new TransmittanceRange().setCutInTolerance(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test
    public void testZeroTransmittanceRangeCutInTolerance() {
        new TransmittanceRange().setCutInTolerance(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeTransmittanceRangeCutInTolerance() {
        new TransmittanceRange().setCutInTolerance(new Length(-1, UnitsLength.CENTIMETER));
    }

    /* TransmittanceRange.cutOutTolerance is a nonnegative float */

    @Test
    public void testPositiveTransmittanceRangeCutOutTolerance() {
        new TransmittanceRange().setCutOutTolerance(new Length(1, UnitsLength.CENTIMETER));
    }

    @Test
    public void testZeroTransmittanceRangeCutOutTolerance() {
        new TransmittanceRange().setCutOutTolerance(new Length(0, UnitsLength.CENTIMETER));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeTransmittanceRangeCutOutTolerance() {
        new TransmittanceRange().setCutOutTolerance(new Length(-1, UnitsLength.CENTIMETER));
    }
}
