#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
import omero.clients

ImageI = omero.model.ImageI
DatasetI = omero.model.DatasetI
EventI = omero.model.EventI
PixelsI = omero.model.PixelsI

image = ImageI(long(1), True)
image.getDetails().setUpdateEvent(EventI(1L, False))

# On creation, all collections are
# initialized to empty, and can be added
# to.
assert image.sizeOfDatasetLinks() == 0
dataset = DatasetI(long(1), False)
link = image.linkDataset(dataset)
assert image.sizeOfDatasetLinks() == 1

# If you want to work with this collection,
# you'll need to get a copy.
links = image.copyDatasetLinks()

# When you are done working with it, you can
# unload the datasets, assuming the changes
# have been persisted to the server.
image.unloadDatasetLinks()
assert image.sizeOfDatasetLinks() < 0
try:
    image.linkDataset(DatasetI())
except:
    # Can't access an unloaded collection
    pass

# The reload...() method allows one instance
# to take over a collection from another, if it
# has been properly initialized on the server.
# sameImage will have it's collection unloaded.
sameImage = ImageI(1L, True)
sameImage.getDetails().setUpdateEvent(EventI(1L, False))
sameImage.linkDataset(DatasetI(long(1), False))
image.reloadDatasetLinks(sameImage)
assert image.sizeOfDatasetLinks() == 1
assert sameImage.sizeOfDatasetLinks() < 0

# If you would like to remove all the member
# elements from a collection, don't unload it
# but "clear" it.
image.clearDatasetLinks()
# Saving this to the database will remove
# all dataset links!

# Finally, all collections can be unloaded
# to use an instance as a single row in the db.
image.unloadCollections()

# Ordered collections have slightly different methods.
image = ImageI(long(1), True)
image.addPixels(PixelsI())
image.getPixels(0)
image.getPrimaryPixels()  # Same thing
image.removePixels(image.getPixels(0))
