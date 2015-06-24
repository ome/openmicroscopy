#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
import omero.clients

from omero.rtypes import rstring, rtime

c = omero.client()
s = c.createSession()

# Create a table
table = s.sharedResources().newTable(1, "iroi.h5")
cols = []
cols.append(omero.grid.RoiColumn('roi_id', 'Roi ID', None))
cols.append(omero.grid.DoubleColumn('area', 'Area of ROI', None))
cols.append(omero.grid.LongColumn('intensity', 'Intensity of ROI', None))
table.initialize(cols)
file = table.getOriginalFile()

# Setup data
plate = omero.model.PlateI()
plate.name = rstring("iroi.py")
well = omero.model.WellI()
sample = omero.model.WellSampleI()
image = omero.model.ImageI()
image.name = rstring("iroi.py")
image.acquisitionDate = rtime(0)
image.addWellSample(sample)
well.addWellSample(sample)
plate.addWell(well)

measurement = omero.model.FileAnnotationI()
measurement.ns = rstring(omero.constants.namespaces.NSMEASUREMENT)
measurement.file = file
plate.linkAnnotation(measurement)
plate = s.getUpdateService().saveAndReturnObject(plate)
image_id = plate.copyWells()[0].copyWellSamples()[0].image.id.val

roi_svc = s.getRoiService()
roi_meas = roi_svc.getRoiMeasurements(image_id, None)

if len(roi_meas) > 0:
    roi_result = roi_svc.getMeasuredRois(image_id, roi_meas[0].id.val, None)
    table = roi_svc.getTable(roi_meas[0].id.val)
    print table.slice([0], None)  # All of column 1
