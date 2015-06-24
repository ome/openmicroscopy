#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
c = omero.client("localhost")
s = c.createSession("root", "ome")

admin = s.getAdminService()

_ = omero.rtypes.rstring

# Users
try:
    owner = admin.lookupExperimenter("owner")
except omero.ApiUsageException:
    owner = omero.model.ExperimenterI()
    owner.omeName = _("owner")
    owner.firstName = _("first")
    owner.lastName = _("last")
try:
    nonOwner = admin.lookupExperimenter("nonOwner")
except omero.ApiUsageException:
    nonOwner = omero.model.ExperimenterI()
    nonOwner.omeName = _("nonOwner")
    nonOwner.firstName = _("first")
    nonOwner.lastName = _("last")
try:
    group = admin.lookupGroup("group")
except omero.ApiUsageException:
    group = omero.model.ExperimenterGroupI()
    group.name = _("group")

if not group.id:
    gid = admin.createGroup(group)
    group = admin.getGroup(gid)

if not owner.id:
    oid = admin.createUser(owner, "group")
    owner = admin.getExperimenter(oid)
    admin.setGroupOwner(group, owner)

if not nonOwner.id:
    nid = admin.createUser(nonOwner, "group")
    nonOwner = admin.getExperimenter(nid)

c.closeSession()


# Now as the non-owner
c = omero.client("localhost")
s = c.createSession("nonOwner", "x")

shares = s.getShareService()
updates = s.getUpdateService()


##
# SPW
##

# Objects
screen = omero.model.ScreenI()
sa = omero.model.ScreenAcquisitionI()
plate = omero.model.PlateI()
well = omero.model.WellI()
wellsample = omero.model.WellSampleI()
image = omero.model.ImageI()
an = omero.model.TagAnnotationI()

# Primitives
screen.name = _("test data")
sa.startTime = omero.rtypes.rtime(0)
sa.endTime = omero.rtypes.rtime(0)
plate.name = omero.rtypes.rstring("test data")
well.row = omero.rtypes.rint(0)
well.column = omero.rtypes.rint(0)
# wellsample.timepoint = omero.rtypes.rint(0) Not supported.
image.name = _("test data")
image.acquisitionDate = omero.rtypes.rtime(0)

# Links
image.addWellSample(wellsample)
well.addWellSample(wellsample)
sa.linkWellSample(wellsample)

well.plate = plate
plate.linkScreen(screen)
sa.screen = screen
sa.linkAnnotation(an)

well = updates.saveAndReturnObject(well)

# Filters
i = omero.model.InstrumentI()

fs = omero.model.FilterSetI()
fs.instrument = i
di = omero.model.DichroicI()
di.instrument = i

filters = [omero.model.FilterI() for x in range(10)]


def f():
    filter = filters.pop()
    filter.instrument = i
    return filter

fs.exFilter = f()
fs.emFilter = f()
fs.dichroic = di

lc = omero.model.LogicalChannelI()
lc.filterSet = fs
lc.secondaryEmissionFilter = f()
lc.secondaryExcitationFilter = f()
lc.mode = omero.model.AcquisitionModeI()
lc.mode.value = _("LaserScanningConfocal")

updates.saveObject(lc)

# Plates for #2428
plates = []
plates.append(omero.model.PlateI())
plates[-1].name = _("test data")
plates[-1].setColumnNamingConvention(_("a"))
plates.append(omero.model.PlateI())
plates[-1].name = _("test data")
plates[-1].setColumnNamingConvention(_("A"))
plates.append(omero.model.PlateI())
plates[-1].name = _("test data")
plates[-1].setColumnNamingConvention(_("1"))
plates.append(omero.model.PlateI())
plates[-1].name = _("test data")
plates[-1].setRowNamingConvention(_("a"))
plates.append(omero.model.PlateI())
plates[-1].name = _("test data")
plates[-1].setRowNamingConvention(_("A"))
plates.append(omero.model.PlateI())
plates[-1].name = _("test data")
plates[-1].setRowNamingConvention(_("1"))

for plate in plates:
    w = omero.model.WellI()
    w.row = omero.rtypes.rint(1)
    w.column = omero.rtypes.rint(2)
    plate.addWell(w)
updates.saveArray(plates)

# Rois
img = omero.model.ImageI()
img.name = _("test data")
img.acquisitionDate = omero.rtypes.rtime(0)
roi = omero.model.RoiI()

shape0 = omero.model.EllipseI()
shape0.fillColor = _("#111111")
shape0.fillOpacity = omero.rtypes.rfloat("0.5")
shape0.strokeColor = _("#CCCCCC")
shape0.strokeOpacity = omero.rtypes.rfloat("0.5")

shape1 = omero.model.RectI()
shape1.fillColor = _("#000000")
shape1.fillOpacity = omero.rtypes.rfloat("0.0")
shape1.strokeColor = _("#ffffff")
shape1.strokeOpacity = omero.rtypes.rfloat("1.0")

shape2 = omero.model.LineI()
shape2.fillColor = _("#000000")
shape2.fillOpacity = omero.rtypes.rfloat("1.0")
shape2.strokeColor = _("#ffffff")
shape2.strokeOpacity = omero.rtypes.rfloat("0.0")

roi.image = img
roi.addShape(shape0)
roi.addShape(shape1)
roi.addShape(shape2)
roi = updates.saveAndReturnObject(roi)

# Shares
sid = shares.createShare(
    "my description", None, [well], [owner, nonOwner], [], True)

print "done"
