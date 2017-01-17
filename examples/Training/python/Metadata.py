#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import imageId


# Create a connection
# ===================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Load Instrument
# ===============
image = conn.getObject("Image", imageId)
instrument = image.getInstrument()
if instrument is not None:
    # Instrument contains links to all filters,
    # objectives etc. (not just the ones used for this image)
    if instrument.getMicroscope() is not None:
        print "Instrument:"
        microscope = instrument.getMicroscope()
        print "  Model: %s Type: %s " % (
            microscope.getModel(), microscope.getType() and
            microscope.getType().getValue())


# Load ObjectiveSettings
# ======================
if image.getObjectiveSettings():
    obj_set = image.getObjectiveSettings()
    print "Objective Settings:"
    print "  Correction Collar: %s Medium: %s Refractive Index: %s" % (
        obj_set.getCorrectionCollar(), obj_set.getMedium(),
        obj_set.getRefractiveIndex())
    if obj_set.getObjective():
        obj = obj_set.getObjective()
        print "Objective:"
        print "  Model: %s Nominal Mag: %s Calibrated Mag: %s" % (
            obj.getModel(), obj.getNominalMagnification(),
            obj.getCalibratedMagnification())
        print "  LensNA:", obj.getLensNA(), "Immersion",
        print (obj.getImmersion() and obj.getImmersion().getValue(),
               "Correction:", obj.getCorrection() and
               obj.getCorrection().getValue())
        print "  Working Distance:", obj.getWorkingDistance()


# Load Channels, LogicalChannels, and LightSourceSettings
# =======================================================
for ch in image.getChannels():
    print "Channel: ", ch.getLabel()
    logical_channel = ch.getLogicalChannel()
    light_path = logical_channel.getLightPath()
    if light_path is not None:
        light_path_dichroic = light_path.getDichroic()
        if (light_path_dichroic is not None and
                light_path_dichroic._obj is not None):
            print "  Dichroic:"
            print "    Model:", light_path_dichroic.getModel()
        print "  Emission Filters:"
        for f in light_path.getEmissionFilters():
            print "    Model:", f.getModel(),
            print "    Type:", f.getType() and f.getType().getValue(),
            tr = f.getTransmittanceRange()
            print "    Transmittance range:: %s-%s " % (
                tr.getCutIn(), tr.getCutOut())
        print "  Excitation Filters:"
        for f in light_path.getExcitationFilters():
            print "    Model:", f.getModel(),
            print "    Type:", f.getType() and f.getType().getValue(),
            tr = f.getTransmittanceRange()
            print "    Transmittance range: %s-%s " % (
                tr.getCutIn(), tr.getCutOut())
    if logical_channel.getDetectorSettings()._obj is not None:
        print "  Detector Settings:"
        dset = logical_channel.getDetectorSettings()
        print "    Voltage: %s Gain: %s Offset: %s" % (
            dset.getVoltage(), dset.getGain(), dset.getOffsetValue())
        print "Readout rate:", dset.getReadOutRate(),
        print "Binning:", (dset.getBinning()._obj is not None
                           and dset.getBinning().getValue())
        if logical_channel.getDetectorSettings().getDetector():
            print "  Detector:"
            det = logical_channel.getDetectorSettings().getDetector()
            print "    Model: %s Gain: %s Voltage: %s Offset: %s" % (
                det.getModel(), det.getGain(), det.getVoltage(),
                det.getOffsetValue())
    light_source_settings = logical_channel.getLightSourceSettings()
    if (light_source_settings is not None
            and light_source_settings._obj is not None):
        print "  Light Source:"
        if light_source_settings.getLightSource() is not None:
            ls = light_source_settings.getLightSource()
            print "    Model: %s Manufacturer: %s Power: %s" % (
                ls.getModel(), ls.getManufacturer(), ls.getPower())
            # TODO: Check whether this is Arc etc.
            try:
                wl = ls.getWavelength()
                print "    Laser Wavelength:", wl
            except AttributeError:
                # this is not a laser
                pass


# Load the 'Original Metadata' for the image
# ==========================================
om = image.loadOriginalMetadata()
if om is not None:
    print "\n\noriginal_metadata"
    print "global_metadata"
    for key_value in om[1]:
        if len(key_value) > 1:
            print "   ", key_value[0], key_value[1]
        else:
            print "   ", key_value[0], "NOT FOUND"
    print "series_metadata"
    for key_value in om[2]:
        if len(key_value) > 1:
            print "   ", key_value[0], key_value[1]
        else:
            print "   ", key_value[0], "NOT FOUND"


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
