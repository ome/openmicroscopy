#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Flip or Rotate an Image and create a new Image in OMERO
"""

import omero
import omero.scripts as scripts
from numpy import rot90, fliplr, flipud
from omero.gateway import BlitzGateway
from omero.rtypes import rstring, rint, rlong, robject


def rotate90(plane):
    return rot90(plane)


def rotate180(plane):
    return rot90(plane, 2)


def rotate270(plane):
    return rot90(plane, 3)


def flipHorizontal(plane):
    return fliplr(plane)


def flipVertical(plane):
    return flipud(plane)

# The transforms that we support
actions = {
    "Rotate_Left": rotate90,
    "Rotate_180": rotate180,
    "Rotate_Right": rotate270,
    "Flip_Vertical": flipVertical,
    "Flip_Horizontal": flipHorizontal}

# Options for Script Parameters
actionOptions = omero.rtypes.wrap(list(actions.keys()))
dataTypes = omero.rtypes.wrap(['Image'])


def createImageFromTransform(conn, image, transforms):
    """
    Apply the listed transforms to each plane of the image and
    create a new Image.

    @param conn:        BlitzGateway connection
    @param image:       ImageWrapper
    @param transforms:  List of strings ["Rotate_Left", "Flip_Horizontal"]
                        etc.
    @return:            New ImageWrapper
    """

    print "Processing image: ", image.getName()

    # Get channel Colors and Names to apply to the new Image
    colors = [c.getColor().getRGB() for c in image.getChannels()]
    cNames = [c.getLabel() for c in image.getChannels()]

    # Prepare plane list...
    sizeZ = image.getSizeZ()
    sizeC = image.getSizeC()
    sizeT = image.getSizeT()
    zctList = []
    for z in range(sizeZ):
        for c in range(sizeC):
            for t in range(sizeT):
                zctList.append((z, c, t))

    # This generator will get each plane as needed and apply transforms
    def planeGen():
        planes = image.getPrimaryPixels().getPlanes(zctList)
        for p in planes:
            for t in transforms:
                print "  Transform plane with...", t,
                action = actions[t]
                p = action(p)
            yield p

    # Create new image with the plane generator prepared above (don't need all
    # the planes in memory at once)
    imageName = "%s-transformed" % image.getName()
    dataset = image.getParent()
    tfList = "\n".join(transforms)
    description = ("Created from Image ID: %s by applying the following"
                   " transforms:\n%s" % (image.id, tfList))

    newImg = conn.createImageFromNumpySeq(
        planeGen(), imageName, sizeZ=sizeZ, sizeC=sizeC, sizeT=sizeT,
        description=description, dataset=dataset)

    # Apply colors from the original image to the new one
    for i, c in enumerate(newImg.getChannels()):
        lc = c.getLogicalChannel()
        lc.setName(cNames[i])
        lc.save()
        r, g, b = colors[i]
        # need to reload channels to avoid optimistic lock on update
        cObj = conn.getQueryService().get("Channel", c.id)
        cObj.red = rint(r)
        cObj.green = rint(g)
        cObj.blue = rint(b)
        cObj.alpha = rint(255)
        conn.getUpdateService().saveObject(cObj)

    newImg.resetRDefs()  # reset based on colors above

    return newImg


def transformImages(conn, scriptParams):
    """
    Processes the list of Images and returns a single Image or Dataset and
    message

    @param conn:            BlitzGateway conn
    @param scriptParams     Dictionary of script parameters
    @return:                omero.model.Dataset or Image, Message
    """

    transforms = scriptParams["Transforms"]

    newImages = []
    for image in conn.getObjects("Image", scriptParams["IDs"]):

        newImg = createImageFromTransform(conn, image, transforms)
        newImages.append(newImg)

    # Handle what we're returning to client
    if len(newImages) == 0:
        return None, "No images created"
    if len(newImages) == 1:
        new = newImages[0]
        msg = "New Image: %s" % new.getName()
        return new._obj, msg
    else:
        ds = newImages[0].getParent()
        if ds is not None:
            return ds._obj, "%s New Images in Dataset:" % len(newImages)
        else:
            return None, "Created %s New Images" % len(newImages)


def runAsScript():
    client = scripts.client(
        'Transform_Image.py',
        "Flip or Rotate an Image and create a new Image in OMERO",

        scripts.String(
            "Data_Type", optional=False, grouping="1",
            description="The data you want to work with.", values=dataTypes,
            default="Image"),

        scripts.List(
            "IDs", optional=False, grouping="2",
            description="List of Dataset IDs or Image IDs").ofType(rlong(0)),

        scripts.List(
            "Transforms", optional=False, grouping="3",
            description="List of transforms to apply to the Image",
            values=actionOptions),
    )

    try:

        conn = BlitzGateway(client_obj=client)

        scriptParams = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                scriptParams[key] = client.getInput(key, unwrap=True)
        print scriptParams

        robj, message = transformImages(conn, scriptParams)

        client.setOutput("Message", rstring(message))
        if robj is not None:
            client.setOutput("Result", robject(robj))

    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()
