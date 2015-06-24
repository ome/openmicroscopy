
import Image
from numpy import asarray


def crop_image(path, cropX, cropY, cropW, cropH):

    image = Image.open(path)
    x = int(cropX)
    y = int(cropY)
    x2 = int(cropW) + x
    y2 = int(cropH) + y
    img = image.crop((x, y, x2, y2))
    img.save(path)


def image_should_be_blank(path, expected=True):

    image = Image.open(path)
    image.save(path)    # avoids errors on .split
    blank = True
    minVals = []
    maxVals = []

    for channel in image.split():
        plane = asarray(channel)
        pMin = plane.min()
        pMax = plane.max()
        minVals.append(pMin)
        maxVals.append(pMax)
        if pMin != pMax:
            blank = False

    if expected:
        if not blank:
            raise AssertionError("Image %s is not blank. min: %s, max: %s"
                                 % (path, minVals, maxVals))
    else:
        if blank:
            raise AssertionError("Image %s is blank. min: %s, max: %s"
                                 % (path, minVals, maxVals))


def image_should_not_be_blank(path):

    image_should_be_blank(expected=False)
