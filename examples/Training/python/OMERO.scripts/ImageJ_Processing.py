
from omero.gateway import BlitzGateway
from omero.rtypes import rstring, rlong, robject
import omero.scripts as scripts
import os
import Image
from numpy import zeros, int32, asarray
from cStringIO import StringIO


# ** NEED to cofigure this with respect to your own server
# Path to ij.jar
IMAGEJPATH = "/Applications/ImageJ/ImageJ.app/Contents/Resources/Java/ij.jar"


def get_rects_from_rois(conn, imageId):
    """
    Returns a list of (x, y, width, height), one for each ROI with rectangle
    shape

    @param conn:        BlitzGateway connection
    @param imageId:     Image ID
    """

    # Using the underlying ROI service & omero.model objects (no ROI support
    # in Blitz Gateway yet)
    roiService = conn.getRoiService()
    result = roiService.findByImage(imageId, None)

    rects = []
    for roi in result.rois:
        # go through all the shapes of the ROI
        for shape in roi.copyShapes():
            if shape.__class__.__name__ == 'RectI':
                # Need getValue() for omero.model rtypes
                x = shape.getX().getValue()
                y = shape.getY().getValue()
                w = shape.getWidth().getValue()
                h = shape.getHeight().getValue()
                rects.append((x, y, w, h))
                break    # Only use the first Rect we find per ROI
    return rects


def download_rendered_planes(image, tiff_stack_dir, region=None):
    """
    Download the specified image as a Z-stack of 'rendered' RGB tiffs to local
    directory.
    NB: The pixel values in the downloaded tiff are NOT the raw data as in
    OMERO.

    @param image:               BlitzGateway imageWrapper
    @param tiff_stack_dir:      Path to directory we want to put out images in
    @param region:              Tuple of (x, y, width, height) if we want a
                                region of the image
    """

    sizeZ = image.getSizeZ()
    theT = 0

    # getPlane() will either return us the region, OR the whole plane.
    if region is not None:
        x, y, w, h = region

        def getPlane(z, t):
            print "Getting region", x, y, w, h
            rv = image.renderJpegRegion(z, t, x, y, w, h)   # returns jpeg data
            if rv is not None:
                i = StringIO(rv)
                return Image.open(i)
    else:
        def getPlane(z, t):
            return image.renderImage(z, t)      # returns PIL Image

    for z in range(sizeZ):
        img_path = os.path.join(tiff_stack_dir, "plane_%02d.tiff" % z)
        plane = getPlane(z, theT)   # get Plane (or region)
        plane.save(img_path)


def download_raw_planes(image, tiff_stack_dir, cIndex, region=None):
    """
    Download the specified image as a Z-stack of 'raw' tiffs to local
    directory.
    The pixel type and pixel values of the tiffs will be limited to int32

    @param image:               BlitzGateway imageWrapper
    @param tiff_stack_dir:      Path to directory we want to put out images in
    @param region:              Tuple of (x, y, width, height) if we want a
                                region of the image
    """

    sizeZ = image.getSizeZ()
    theT = 0
    theC = cIndex

    def numpyToImage(plane):
        """
        Converts the numpy plane to a PIL Image, scaling to cMinMax (minVal,
        maxVal) and changing data type if needed.
        Need plane dtype to be uint8 (or int8) for conversion to tiff by PIL
        """
        if plane.dtype.name not in ('uint8', 'int8'):
            # int32 is handled by PIL (not uint32 etc). TODO: support floats
            convArray = zeros(plane.shape, dtype=int32)
            convArray += plane
            return Image.fromarray(convArray)
        return Image.fromarray(plane)

    # We use getTiles() or getPlanes() to provide numpy 2D arrays for each
    # image plane
    if region is not None:
        zctTileList = [(z, theC, theT, region) for z in range(sizeZ)]
        # A generator (not all planes in hand)
        planes = image.getPrimaryPixels().getTiles(zctTileList)
    else:
        zctList = [(z, theC, theT) for z in range(sizeZ)]
        # A generator (not all planes in hand)
        planes = image.getPrimaryPixels().getPlanes(zctList)

    for z, plane in enumerate(planes):
        i = numpyToImage(plane)
        img_path = os.path.join(tiff_stack_dir, "plane_%02d.tiff" % z)
        i.save(img_path)


def do_processing(tiff_stack_dir, destination, sizeX, axis="Y"):
    """
    Here we set-up the ImageJ macro and run it from the command line.
    We need to know the path to ImageJ jar.
    The macro text is written to the temp folder that we're running the script
    in, and the path to the macro is passed to the command line.
    """

    rotation_ijm = """
str=getArgument();
args=split(str,"*");
ippath='%s';
slices=%s;
opname="rot_frame";
oppath=args[1];

run("Image Sequence...", "open=&ippath number=&slices starting=1 increment=1 \
scale=100 file=[] or=[] sort");
run("3D Project...", "projection=[Brightest Point] axis=%s-Axis slice=1 \
initial=0 total=360 rotation=10 lower=1 upper=255 opacity=0 surface=100 \
interior=50");
run("Image Sequence... ", "format=JPEG name=[&opname] start=0 digits=4 \
save="+oppath );
    """ % (tiff_stack_dir, sizeX, axis)

    ijm_path = "rotation.ijm"

    # write the macro to a known location that we can pass to ImageJ
    f = open(ijm_path, 'w')
    f.write(rotation_ijm)
    f.close()

    # Call ImageJ via command line, with macro ijm path & parameters
    # can't use ";" on Mac / Linu. Use "*"
    macro_args = "*".join([tiff_stack_dir, destination])
    cmd = "java -jar %s -batch %s %s -Xmx1000m" % (
        IMAGEJPATH, ijm_path, macro_args)
    # this calls the imagej macro and creates the 36 frames at each 10% and
    # are then saved in the destination folder
    os.system(cmd)


def upload_to_omero(conn, destination, imageName, dataset=None):
    """
    This creates a new Image in OMERO using all the images in destination
    folder as Z-planes
    """

    image_list = os.listdir(destination)
    image_list = [i for i in image_list if not i.startswith(".")]
    print "Upload to OMERO:", image_list

    # Create a new Image in OMERO, with the jpeg images as a Z-stack.
    # We need a generator to produce numpy planes in the order Z, C, T.
    def plane_generator():
        # Start by iterating through Z
        for i in image_list:
            print "  Uploading ", i
            img = Image.open(os.path.join(destination, i))
            img.load()      # need to get the data in hand before...
            channels = img.split()
            for channel in channels:
                numpyPlane = asarray(channel)
                yield numpyPlane

    # We're making a Z-stack, so we know the sizeZ
    newSizeZ = len(image_list)

    # Need to check whether we're dealing with RGB images (3 channels) or
    # greyscale (1 channel)
    img = Image.open(os.path.join(destination, image_list[0]))
    img.load()
    sizeC = len(img.split())

    # Set group context for Image creation. Shouldn't be needed in scripts
    # conn.SERVICE_OPTS.setOmeroGroup( image.getDetails().group.id.val )

    # Create the image
    plane_gen = plane_generator()
    dsName = dataset is not None and dataset.getName() or 'None'
    print ("Creating a NEW image: %s  sizeZ: %s  sizeC: %s  in Dataset: %s"
           % (imageName, newSizeZ, sizeC, dsName))
    newImg = conn.createImageFromNumpySeq(
        plane_gen, imageName, sizeZ=newSizeZ, sizeC=sizeC, dataset=dataset)
    print "New Image ID", newImg.getId()
    return newImg


def process_image(conn, image, tiff_stack_dir, processed_img_dir, axis,
                  useRawData=False, cIndex=0, region=None):
    """
    Do the whole process for a single image.
    Download the Z-planes of the input image as separate tiffs into
    tiff_stack_dir/ call the processing step (output images should be put into
    processed_img_dir/ ) and then create a new Image in OMERO from these
    images
    """

    dataset = image.getParent()
    sizeX = image.getSizeX()

    # Depending on what our processing needs, use rendered data or raw pixels
    # as tiffs
    if useRawData:
        download_raw_planes(image, tiff_stack_dir, cIndex, region)
    else:
        download_rendered_planes(image, tiff_stack_dir, region)

    # Generate a stack of processed images from input tiffs.
    do_processing(tiff_stack_dir, processed_img_dir, sizeX, axis)

    # Create new Image from the processed stack of images
    newImageName = "%s-3D" % image.getName()
    newImg = upload_to_omero(conn, processed_img_dir, newImageName, dataset)

    return newImg


def rotation_proj_stitch(conn, scriptParams):
    """
    Get the images and other data from scriptParams, then call the
    process_image for each image, passing in other parameters as needed
    """

    axis = scriptParams['Rotation_Axis']
    useRawData = scriptParams['Use_Raw_Data']
    # Convert to zero-based index
    cIndex = scriptParams['Channel_To_Analyse'] - 1
    use_rois = scriptParams['Analyse_ROI_Regions']

    current_dir = os.getcwd()
    tiff_stack_dir = os.path.join(current_dir, "tiff_stack")
    processed_img_dir = os.path.join(current_dir, "processed_img")

    try:
        os.mkdir(tiff_stack_dir)
        os.mkdir(processed_img_dir)
    except:
        pass

    def empty_dir(dir_path):
        for old_file in os.listdir(dir_path):
            file_path = os.path.join(dir_path, old_file)
            os.unlink(file_path)

    newImages = []
    for image in conn.getObjects("Image", scriptParams['IDs']):

        # remove input and processed images
        empty_dir(tiff_stack_dir)
        empty_dir(processed_img_dir)

        if use_rois:
            print "Analysing regions:", get_rects_from_rois(
                conn, image.getId())
            for r in get_rects_from_rois(conn, image.getId()):
                newImg = process_image(
                    conn, image, tiff_stack_dir, processed_img_dir, axis,
                    useRawData, cIndex, r)
                newImages.append(newImg)

        else:
            newImg = process_image(
                conn, image, tiff_stack_dir, processed_img_dir, axis,
                useRawData, cIndex)
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


def runScript():
    """
    The main entry point of the script, as called by the client via the
    scripting service, passing the required parameters.
    """

    dataTypes = [rstring('Image')]
    axes = [rstring('Y'), rstring('X')]

    client = scripts.client(
        'ImageJ_Processing.py',
        ("Does ImageJ 'Rotation Projection' macro processing, creating a new"
         " Image in OMERO"),

        scripts.String(
            "Data_Type", optional=False, grouping="1",
            description="The data you want to work with.", values=dataTypes,
            default="Image"),

        scripts.List(
            "IDs", optional=False, grouping="2",
            description="List of Dataset IDs or Image IDs").ofType(rlong(0)),

        scripts.String(
            "Rotation_Axis", optional=False, grouping="3",
            description="3D rotation over Y or X axis", values=axes,
            default="Y"),

        scripts.Bool(
            "Use_Raw_Data", grouping="4", default=False,
            description="Convert raw pixel data to Tiff for processing?"
            " Otherwise use rendered data"),

        scripts.Int(
            "Channel_To_Analyse", grouping="4.1", default=1, min=1,
            description="This channel will be analysed as greyscale Tiffs"),

        scripts.Bool(
            "Analyse_ROI_Regions", grouping="5", default=False,
            description="Use Rectangle ROIs to define regions to analyse. By"
            " default analyse whole image"),

        authors=["William Moore", "Asmi Shah"],
        institutions=["University of Dundee", "KIT"],
        contact="ome-users@lists.openmicroscopy.org.uk",
    )

    try:
        client.getSession()
        scriptParams = {}

        conn = BlitzGateway(client_obj=client)

        # process the list of args above.
        for key in client.getInputKeys():
            if client.getInput(key):
                scriptParams[key] = client.getInput(key, unwrap=True)
        print scriptParams

        robj, message = rotation_proj_stitch(conn, scriptParams)

        client.setOutput("Message", rstring(message))
        if robj is not None:
            client.setOutput("Result", robject(robj))

    finally:
        client.closeSession()

if __name__ == "__main__":
    runScript()
