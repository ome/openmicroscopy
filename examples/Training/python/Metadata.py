#!/usr/bin/env python
# 
# Copyright (c) 2011 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Version: 1.0
#
# This script shows a simple connection to OMERO, printing details of the connection.
# NB: You will need to edit the config.py before running.
# 
# 
from omero.gateway import BlitzGateway
from Connecting import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
imageId = 101


image = conn.getObject("Image", imageId)
instrument = image.getInstrument()
if instrument is not None:
    # Instrument contains links to all filters, objectives etc (not just the ones used for this image)
    if instrument.getMicroscope() is not None:
        print "Instrument:"
        microscope = instrument.getMicroscope()
        print "  Model:", microscope.getModel(), "Type:", microscope.getType() and microscope.getType().getValue()

if image.getObjectiveSettings():
    objSet = image.getObjectiveSettings()
    print "Objective Settings:"
    print "  Correction Collar:", objSet.getCorrectionCollar(), "Medium:", objSet.getMedium(), "Refractive Index:", objSet.getRefractiveIndex()
    if objSet.getObjective():
        obj = objSet.getObjective()
        print "Objective:"
        print "  Model:", obj.getModel(), "Nominal Mag:", obj.getNominalMagnification(), "Calibrated Mag:", obj.getCalibratedMagnification()
        print "  LensNA:", obj.getLensNA(), "Immersion", 
        print obj.getImmersion() and obj.getImmersion().getValue(), "Correction:", obj.getCorrection() and obj.getCorrection().getValue() 
        print "  Working Distance:", obj.getWorkingDistance()

for ch in image.getChannels():
    print "Channel: ", ch.getLabel()
    logicalChannel = ch.getLogicalChannel()
    
    lightPath = logicalChannel.getLightPath()
    if lightPath is not None:
        lightPathDichroic = lightPath.getDichroic()
        print "  Emission Filters:"
        for f in lightPath.copyEmissionFilters():
            print "    Model:", f.getModel(),
            print "    Type:", f.getType() and f.getType().getValue(),
            tr = f.getTransmittanceRange()
            print "    Transmittance range:", tr.getCutIn(), "-", tr.getCutOut()
        print "  Excitation Filters:"
        for f in lightPath.copyExcitationFilters():
            print "    Model:", f.getModel(),
            print "    Type:", f.getType() and f.getType().getValue(),
            tr = f.getTransmittanceRange()
            print "    Transmittance range:", tr.getCutIn(), "-", tr.getCutOut()

    if logicalChannel.getDetectorSettings()._obj is not None:
        print "  Detector Settings:"
        dset = logicalChannel.getDetectorSettings()
        print "    Voltage:", dset.getVoltage(), "Gain:", dset.getGain(), "Offset:", dset.getOffsetValue(),
        print "Readout rate:", dset.getReadOutRate(),
        print "Binning:", dset.getBinning()._obj is not None and dset.getBinning().getValue()
        if logicalChannel.getDetectorSettings().getDetector():
            print "  Detector:"
            det = logicalChannel.getDetectorSettings().getDetector()
            print "    Model:", det.getModel(), "Gain:", det.getGain(), "Voltage:", det.getVoltage(), "Offset:", det.getOffsetValue()

    lightSourceSettings = logicalChannel.getLightSourceSettings()
    if lightSourceSettings is not None and lightSourceSettings._obj is not None:
        print "  Light Source:"
        if lightSourceSettings.getLightSource() is not None:
            ls = lightSourceSettings.getLightSource()
            print "    Model:", ls.getModel(), "Manufacturer:", ls.getManufacturer(), "Power:", ls.getPower()
            # TODO: Check whether this is Arc etc. 
            try:
                wl = ls.getWavelength()
                print "    Laser Wavelength:", wl
            except AttributeError:
                # this is not a laser
                pass

# Load the 'Original Metadata' for the image

om = image.loadOriginalMetadata()
if om is not None:
    print "\n\noriginal_metadata"
    print "    File Annotation ID:", om[0].getId()
    print "global_metadata"
    for keyValue in om[1]:
        if len(keyValue) > 1:
            print "   ", keyValue[0], keyValue[1]
        else:
            print "   ", keyValue[0], "NOT FOUND"
    print "series_metadata"
    for keyValue in om[2]:
        if len(keyValue) > 1:
            print "   ", keyValue[0], keyValue[1]
        else:
            print "   ", keyValue[0], "NOT FOUND" 


conn._closeSession()