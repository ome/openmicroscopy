/*
 * Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment
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

package ome.formats.model;

import ome.model.units.BigResult;
import ome.units.unit.Unit;
import omero.model.ElectricPotential;
import omero.model.ElectricPotentialI;
import omero.model.Frequency;
import omero.model.FrequencyI;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Power;
import omero.model.PowerI;
import omero.model.Pressure;
import omero.model.PressureI;
import omero.model.Temperature;
import omero.model.TemperatureI;
import omero.model.Time;
import omero.model.TimeI;
import omero.model.enums.UnitsElectricPotential;
import omero.model.enums.UnitsFrequency;
import omero.model.enums.UnitsLength;
import omero.model.enums.UnitsPower;
import omero.model.enums.UnitsPressure;
import omero.model.enums.UnitsTemperature;
import omero.model.enums.UnitsTime;


/**
 * Utility class to generate and convert unit objects.
 *
 * Be especially careful when using methods which take a string
 * since there are 2 types of enumerations, CODE-based and
 * SYMBOL-based.
 */
public class UnitsFactory {


    //
    // ElectricPotential
    //

    public static ome.xml.model.enums.UnitsElectricPotential makeElectricPotentialUnitXML(String unit) {
        return ElectricPotentialI.makeXMLUnit(unit);
    }

    public static ome.units.quantity.ElectricPotential makeElectricPotentialXML(double d, String unit) {
        return ElectricPotentialI.makeXMLQuantity(d, unit);
    }

    public static ElectricPotential makeElectricPotential(double d,
            Unit<ome.units.quantity.ElectricPotential> unit) {
        return new ElectricPotentialI(d, unit);
    }

    public static ElectricPotential makeElectricPotential(double d, UnitsElectricPotential unit) {
        return new ElectricPotentialI(d, unit);
    }

    /**
     * Convert a Bio-Formats {@link ElectricPotential} to an OMERO ElectricPotential. A null will be
     * returned if the input is null.
     */
    public static ElectricPotential convertElectricPotential(ome.units.quantity.ElectricPotential value) {
        if (value == null)
            return null;
        String internal = xmlElectricPotentialEnumToOMERO(value.unit().getSymbol());
        UnitsElectricPotential ul = UnitsElectricPotential.valueOf(internal);
        return new omero.model.ElectricPotentialI(value.value().doubleValue(), ul);
    }

    public static ome.units.quantity.ElectricPotential convertElectricPotential(ElectricPotential t) {
        return ElectricPotentialI.convert(t);
    }

    public static ElectricPotential convertElectricPotential(ElectricPotential value, Unit<ome.units.quantity.ElectricPotential> ul) throws BigResult {
        return convertElectricPotentialXML(value, ul.getSymbol());
    }

    public static ElectricPotential convertElectricPotentialXML(ElectricPotential value, String xml) throws BigResult {
        String omero = xmlElectricPotentialEnumToOMERO(xml);
        return new ElectricPotentialI(value, omero);
    }

    public static String xmlElectricPotentialEnumToOMERO(Unit<ome.units.quantity.ElectricPotential> xml) {
        return ome.model.enums.UnitsElectricPotential.bySymbol(xml.getSymbol()).toString();
    }

    public static String xmlElectricPotentialEnumToOMERO(String xml) {
        return ome.model.enums.UnitsElectricPotential.bySymbol(xml).toString();
    }


    //
    // Frequency
    //

    public static ome.xml.model.enums.UnitsFrequency makeFrequencyUnitXML(String unit) {
        return FrequencyI.makeXMLUnit(unit);
    }

    public static ome.units.quantity.Frequency makeFrequencyXML(double d, String unit) {
        return FrequencyI.makeXMLQuantity(d, unit);
    }

    public static Frequency makeFrequency(double d,
            Unit<ome.units.quantity.Frequency> unit) {
        return new FrequencyI(d, unit);
    }

    public static Frequency makeFrequency(double d, UnitsFrequency unit) {
        return new FrequencyI(d, unit);
    }

    /**
     * Convert a Bio-Formats {@link Frequency} to an OMERO Frequency. A null will be
     * returned if the input is null.
     */
    public static Frequency convertFrequency(ome.units.quantity.Frequency value) {
        if (value == null)
            return null;
        String internal = xmlFrequencyEnumToOMERO(value.unit().getSymbol());
        UnitsFrequency ul = UnitsFrequency.valueOf(internal);
        return new omero.model.FrequencyI(value.value().doubleValue(), ul);
    }

    public static ome.units.quantity.Frequency convertFrequency(Frequency t) {
        return FrequencyI.convert(t);
    }

    public static Frequency convertFrequency(Frequency value, Unit<ome.units.quantity.Frequency> ul) throws BigResult {
        return convertFrequencyXML(value, ul.getSymbol());
    }

    public static Frequency convertFrequencyXML(Frequency value, String xml) throws BigResult {
        String omero = xmlFrequencyEnumToOMERO(xml);
        return new FrequencyI(value, omero);
    }

    public static String xmlFrequencyEnumToOMERO(Unit<ome.units.quantity.Frequency> xml) {
        return ome.model.enums.UnitsFrequency.bySymbol(xml.getSymbol()).toString();
    }

    public static String xmlFrequencyEnumToOMERO(String xml) {
        return ome.model.enums.UnitsFrequency.bySymbol(xml).toString();
    }


    //
    // Length
    //

    public static ome.xml.model.enums.UnitsLength makeLengthUnitXML(String unit) {
        return LengthI.makeXMLUnit(unit);
    }

    public static ome.units.quantity.Length makeLengthXML(double d, String unit) {
        return LengthI.makeXMLQuantity(d, unit);
    }

    public static Length makeLength(double d,
            Unit<ome.units.quantity.Length> unit) {
        return new LengthI(d, unit);
    }

    public static Length makeLength(double d, UnitsLength unit) {
        return new LengthI(d, unit);
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length. A null will be
     * returned if the input is null.
     */
    public static Length convertLength(ome.units.quantity.Length value) {
        if (value == null)
            return null;
        String internal = xmlLengthEnumToOMERO(value.unit().getSymbol());
        UnitsLength ul = UnitsLength.valueOf(internal);
        return new omero.model.LengthI(value.value().doubleValue(), ul);
    }

    public static ome.units.quantity.Length convertLength(Length t) {
        return LengthI.convert(t);
    }

    public static Length convertLength(Length value, Unit<ome.units.quantity.Length> ul) throws BigResult {
        return convertLengthXML(value, ul.getSymbol());
    }

    public static Length convertLengthXML(Length value, String xml) throws BigResult {
        String omero = xmlLengthEnumToOMERO(xml);
        return new LengthI(value, omero);
    }

    public static String xmlLengthEnumToOMERO(Unit<ome.units.quantity.Length> xml) {
        return ome.model.enums.UnitsLength.bySymbol(xml.getSymbol()).toString();
    }

    public static String xmlLengthEnumToOMERO(String xml) {
        return ome.model.enums.UnitsLength.bySymbol(xml).toString();
    }


    //
    // Power
    //

    public static ome.xml.model.enums.UnitsPower makePowerUnitXML(String unit) {
        return PowerI.makeXMLUnit(unit);
    }

    public static ome.units.quantity.Power makePowerXML(double d, String unit) {
        return PowerI.makeXMLQuantity(d, unit);
    }

    public static Power makePower(double d,
            Unit<ome.units.quantity.Power> unit) {
        return new PowerI(d, unit);
    }

    public static Power makePower(double d, UnitsPower unit) {
        return new PowerI(d, unit);
    }

    /**
     * Convert a Bio-Formats {@link Power} to an OMERO Power. A null will be
     * returned if the input is null.
     */
    public static Power convertPower(ome.units.quantity.Power value) {
        if (value == null)
            return null;
        String internal = xmlPowerEnumToOMERO(value.unit().getSymbol());
        UnitsPower ul = UnitsPower.valueOf(internal);
        return new omero.model.PowerI(value.value().doubleValue(), ul);
    }

    public static ome.units.quantity.Power convertPower(Power t) {
        return PowerI.convert(t);
    }

    public static Power convertPower(Power value, Unit<ome.units.quantity.Power> ul) throws BigResult {
        return convertPowerXML(value, ul.getSymbol());
    }

    public static Power convertPowerXML(Power value, String xml) throws BigResult {
        String omero = xmlPowerEnumToOMERO(xml);
        return new PowerI(value, omero);
    }

    public static String xmlPowerEnumToOMERO(Unit<ome.units.quantity.Power> xml) {
        return ome.model.enums.UnitsPower.bySymbol(xml.getSymbol()).toString();
    }

    public static String xmlPowerEnumToOMERO(String xml) {
        return ome.model.enums.UnitsPower.bySymbol(xml).toString();
    }


    //
    // Pressure
    //

    public static ome.xml.model.enums.UnitsPressure makePressureUnitXML(String unit) {
        return PressureI.makeXMLUnit(unit);
    }

    public static ome.units.quantity.Pressure makePressureXML(double d, String unit) {
        return PressureI.makeXMLQuantity(d, unit);
    }

    public static Pressure makePressure(double d,
            Unit<ome.units.quantity.Pressure> unit) {
        return new PressureI(d, unit);
    }

    public static Pressure makePressure(double d, UnitsPressure unit) {
        return new PressureI(d, unit);
    }

    /**
     * Convert a Bio-Formats {@link Pressure} to an OMERO Pressure. A null will be
     * returned if the input is null.
     */
    public static Pressure convertPressure(ome.units.quantity.Pressure value) {
        if (value == null)
            return null;
        String internal = xmlPressureEnumToOMERO(value.unit().getSymbol());
        UnitsPressure ul = UnitsPressure.valueOf(internal);
        return new omero.model.PressureI(value.value().doubleValue(), ul);
    }

    public static ome.units.quantity.Pressure convertPressure(Pressure t) {
        return PressureI.convert(t);
    }

    public static Pressure convertPressure(Pressure value, Unit<ome.units.quantity.Pressure> ul) throws BigResult {
        return convertPressureXML(value, ul.getSymbol());
    }

    public static Pressure convertPressureXML(Pressure value, String xml) throws BigResult {
        String omero = xmlPressureEnumToOMERO(xml);
        return new PressureI(value, omero);
    }

    public static String xmlPressureEnumToOMERO(Unit<ome.units.quantity.Pressure> xml) {
        return ome.model.enums.UnitsPressure.bySymbol(xml.getSymbol()).toString();
    }

    public static String xmlPressureEnumToOMERO(String xml) {
        return ome.model.enums.UnitsPressure.bySymbol(xml).toString();
    }


    //
    // Temperature
    //

    public static ome.xml.model.enums.UnitsTemperature makeTemperatureUnitXML(String unit) {
        return TemperatureI.makeXMLUnit(unit);
    }

    public static ome.units.quantity.Temperature makeTemperatureXML(double d, String unit) {
        return TemperatureI.makeXMLQuantity(d, unit);
    }

    public static Temperature makeTemperature(double d,
            Unit<ome.units.quantity.Temperature> unit) {
        return new TemperatureI(d, unit);
    }

    public static Temperature makeTemperature(double d, UnitsTemperature unit) {
        return new TemperatureI(d, unit);
    }

    /**
     * Convert a Bio-Formats {@link Temperature} to an OMERO Temperature. A null will be
     * returned if the input is null.
     */
    public static Temperature convertTemperature(ome.units.quantity.Temperature value) {
        if (value == null)
            return null;
        String internal = xmlTemperatureEnumToOMERO(value.unit().getSymbol());
        UnitsTemperature ul = UnitsTemperature.valueOf(internal);
        return new omero.model.TemperatureI(value.value().doubleValue(), ul);
    }

    public static ome.units.quantity.Temperature convertTemperature(Temperature t) {
        return TemperatureI.convert(t);
    }

    public static Temperature convertTemperature(Temperature value, Unit<ome.units.quantity.Temperature> ul) throws BigResult {
        return convertTemperatureXML(value, ul.getSymbol());
    }

    public static Temperature convertTemperatureXML(Temperature value, String xml) throws BigResult {
        String omero = xmlTemperatureEnumToOMERO(xml);
        return new TemperatureI(value, omero);
    }

    public static String xmlTemperatureEnumToOMERO(Unit<ome.units.quantity.Temperature> xml) {
        return ome.model.enums.UnitsTemperature.bySymbol(xml.getSymbol()).toString();
    }

    public static String xmlTemperatureEnumToOMERO(String xml) {
        return ome.model.enums.UnitsTemperature.bySymbol(xml).toString();
    }


    //
    // Time
    //

    public static ome.xml.model.enums.UnitsTime makeTimeUnitXML(String unit) {
        return TimeI.makeXMLUnit(unit);
    }

    public static ome.units.quantity.Time makeTimeXML(double d, String unit) {
        return TimeI.makeXMLQuantity(d, unit);
    }

    public static Time makeTime(double d,
            Unit<ome.units.quantity.Time> unit) {
        return new TimeI(d, unit);
    }

    public static Time makeTime(double d, UnitsTime unit) {
        return new TimeI(d, unit);
    }

    /**
     * Convert a Bio-Formats {@link Time} to an OMERO Time. A null will be
     * returned if the input is null.
     */
    public static Time convertTime(ome.units.quantity.Time value) {
        if (value == null)
            return null;
        String internal = xmlTimeEnumToOMERO(value.unit().getSymbol());
        UnitsTime ul = UnitsTime.valueOf(internal);
        return new omero.model.TimeI(value.value().doubleValue(), ul);
    }

    public static ome.units.quantity.Time convertTime(Time t) {
        return TimeI.convert(t);
    }

    public static Time convertTime(Time value, Unit<ome.units.quantity.Time> ul) throws BigResult {
        return convertTimeXML(value, ul.getSymbol());
    }

    public static Time convertTimeXML(Time value, String xml) throws BigResult {
        String omero = xmlTimeEnumToOMERO(xml);
        return new TimeI(value, omero);
    }

    public static String xmlTimeEnumToOMERO(Unit<ome.units.quantity.Time> xml) {
        return ome.model.enums.UnitsTime.bySymbol(xml.getSymbol()).toString();
    }

    public static String xmlTimeEnumToOMERO(String xml) {
        return ome.model.enums.UnitsTime.bySymbol(xml).toString();
    }



    public static UnitsLength Plane_PositionX = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Plane.getPositionXUnitXsdDefault()));
    public static UnitsLength Plane_PositionZ = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Plane.getPositionZUnitXsdDefault()));
    public static UnitsLength Plane_PositionY = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Plane.getPositionYUnitXsdDefault()));
    public static UnitsTime Plane_DeltaT = UnitsTime.valueOf(xmlTimeEnumToOMERO(ome.xml.model.Plane.getDeltaTUnitXsdDefault()));
    public static UnitsTime Plane_ExposureTime = UnitsTime.valueOf(xmlTimeEnumToOMERO(ome.xml.model.Plane.getExposureTimeUnitXsdDefault()));
    public static UnitsLength Shape_StrokeWidth = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Shape.getStrokeWidthUnitXsdDefault()));
    public static UnitsLength Shape_FontSize = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Shape.getFontSizeUnitXsdDefault()));
    public static UnitsElectricPotential DetectorSettings_Voltage = UnitsElectricPotential.valueOf(xmlElectricPotentialEnumToOMERO(ome.xml.model.DetectorSettings.getVoltageUnitXsdDefault()));
    public static UnitsFrequency DetectorSettings_ReadOutRate = UnitsFrequency.valueOf(xmlFrequencyEnumToOMERO(ome.xml.model.DetectorSettings.getReadOutRateUnitXsdDefault()));
    public static UnitsTemperature ImagingEnvironment_Temperature = UnitsTemperature.valueOf(xmlTemperatureEnumToOMERO(ome.xml.model.ImagingEnvironment.getTemperatureUnitXsdDefault()));
    public static UnitsPressure ImagingEnvironment_AirPressure = UnitsPressure.valueOf(xmlPressureEnumToOMERO(ome.xml.model.ImagingEnvironment.getAirPressureUnitXsdDefault()));
    public static UnitsLength LightSourceSettings_Wavelength = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.LightSourceSettings.getWavelengthUnitXsdDefault()));
    public static UnitsLength Plate_WellOriginX = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Plate.getWellOriginXUnitXsdDefault()));
    public static UnitsLength Plate_WellOriginY = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Plate.getWellOriginYUnitXsdDefault()));
    public static UnitsLength Objective_WorkingDistance = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Objective.getWorkingDistanceUnitXsdDefault()));
    public static UnitsLength Pixels_PhysicalSizeX = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Pixels.getPhysicalSizeXUnitXsdDefault()));
    public static UnitsLength Pixels_PhysicalSizeZ = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Pixels.getPhysicalSizeZUnitXsdDefault()));
    public static UnitsLength Pixels_PhysicalSizeY = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Pixels.getPhysicalSizeYUnitXsdDefault()));
    public static UnitsTime Pixels_TimeIncrement = UnitsTime.valueOf(xmlTimeEnumToOMERO(ome.xml.model.Pixels.getTimeIncrementUnitXsdDefault()));
    public static UnitsLength StageLabel_Z = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.StageLabel.getZUnitXsdDefault()));
    public static UnitsLength StageLabel_Y = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.StageLabel.getYUnitXsdDefault()));
    public static UnitsLength StageLabel_X = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.StageLabel.getXUnitXsdDefault()));
    public static UnitsPower LightSource_Power = UnitsPower.valueOf(xmlPowerEnumToOMERO(ome.xml.model.LightSource.getPowerUnitXsdDefault()));
    public static UnitsElectricPotential Detector_Voltage = UnitsElectricPotential.valueOf(xmlElectricPotentialEnumToOMERO(ome.xml.model.Detector.getVoltageUnitXsdDefault()));
    public static UnitsLength WellSample_PositionX = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.WellSample.getPositionXUnitXsdDefault()));
    public static UnitsLength WellSample_PositionY = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.WellSample.getPositionYUnitXsdDefault()));
    public static UnitsLength Channel_EmissionWavelength = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Channel.getEmissionWavelengthUnitXsdDefault()));
    public static UnitsLength Channel_PinholeSize = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Channel.getPinholeSizeUnitXsdDefault()));
    public static UnitsLength Channel_ExcitationWavelength = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Channel.getExcitationWavelengthUnitXsdDefault()));
    public static UnitsLength TransmittanceRange_CutOutTolerance = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.TransmittanceRange.getCutOutToleranceUnitXsdDefault()));
    public static UnitsLength TransmittanceRange_CutInTolerance = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.TransmittanceRange.getCutInToleranceUnitXsdDefault()));
    public static UnitsLength TransmittanceRange_CutOut = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.TransmittanceRange.getCutOutUnitXsdDefault()));
    public static UnitsLength TransmittanceRange_CutIn = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.TransmittanceRange.getCutInUnitXsdDefault()));
    public static UnitsFrequency Laser_RepetitionRate = UnitsFrequency.valueOf(xmlFrequencyEnumToOMERO(ome.xml.model.Laser.getRepetitionRateUnitXsdDefault()));
    public static UnitsLength Laser_Wavelength = UnitsLength.valueOf(xmlLengthEnumToOMERO(ome.xml.model.Laser.getWavelengthUnitXsdDefault()));

}

