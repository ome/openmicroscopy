

import json

from datetime import datetime

import omero.scripts as scripts

from cStringIO import StringIO
try:
    from PIL import Image # see ticket:2597
except ImportError:
    import Image

from reportlab.pdfgen import canvas
# from reportlab.lib.pagesizes import letter, A4


from omero.gateway import BlitzGateway
from omero.rtypes import rstring, robject
# conn = BlitzGateway('will', 'ome')
# conn.connect()


def applyRdefs(image, channels):

    cIdxs = []
    windows = []
    colors = []

    for i, c in enumerate(channels):
        if c['active']:
            cIdxs.append(i+1)
            windows.append([c['window']['start'], c['window']['end']])
            colors.append(c['color'])

    print "setActiveChannels", cIdxs, windows, colors
    image.setActiveChannels(cIdxs, windows, colors)


def get_panel_region_xywh(panel):

    zoom = float(panel['zoom'])
    frame_w = panel['width']
    frame_h = panel['height']
    dx = panel['dx']
    dy = panel['dy']
    orig_w = panel['orig_width']
    orig_h = panel['orig_height']

    # need tile_x, tile_y, tile_w, tile_h

    # img_x = 0
    # img_y = 0
    # img_w = frame_w * (zoom/100)
    # img_h = frame_h * (zoom/100)

    tile_w = orig_w / (zoom/100)
    tile_h = orig_h / (zoom/100)

    print " ---------------- "
    print "IMAGE", panel['imageId']

    print 'zoom', zoom
    print 'frame_w', frame_w, 'frame_h', frame_h, 'orig', orig_w, orig_h
    print "Initial tile w, h", tile_w, tile_h

    orig_ratio = float(orig_w) / orig_h
    wh = float(frame_w) / frame_h

    print "ratios", wh, orig_ratio

    if abs(orig_ratio - wh) > 0.01:
        # if viewport is wider than orig...
        if (orig_ratio < wh):
            print "viewport wider"
            # tile_w = orig_ratio
            tile_h = tile_w / wh
        else:
            print "viewport longer"
            tile_w = tile_h * wh

    print 'tile_w', tile_w, 'tile_h', tile_h

    print 'dx', dx, 'dy', dy

    print 'orig_w - tile_w', orig_w - tile_w

    tile_x = (orig_w - tile_w)/2 - (dx / (zoom/100))
    tile_y = (orig_h - tile_h)/2 - (dy / (zoom/100))

    return {'x': tile_x, 'y': tile_y, 'width': tile_w, 'height': tile_h}


def drawLabels(conn, c, panel, pageHeight):

    labels = panel['labels']
    x = panel['x']
    y = panel['y']
    width = panel['width']
    height = panel['height']

    spacer = 5

    # group by 'position':
    positions = {'top': [], 'bottom': [], 'left': [], 'right': [],
                'topleft': [], 'topright': [],
                'bottomleft': [], 'bottomright': []}

    print "sorting labels..."
    for l in labels:
        print l
        pos = l['position']
        l['size'] = int(l['size'])   # make sure 'size' is number
        if pos in positions:
            positions[pos].append(l)

    def drawLab(c, label, lx, ly, align='left'):
        label_h = label['size']
        c.setFont("Helvetica", label_h)
        color = label['color']
        red = int(color[0:2], 16)
        green = int(color[2:4], 16)
        blue = int(color[4:6], 16)
        c.setFillColorRGB(red, green, blue)
        if align == 'left':
            c.drawString(lx, pageHeight - label_h - ly, label['text'])
        elif align == 'right':
            c.drawRightString(lx, pageHeight - label_h - ly, label['text'])
        elif align == 'center':
            c.drawCentredString(lx, pageHeight - label_h - ly, label['text'])

        return label_h

    # Render each position:
    for key, labels in positions.items():
        if key == 'topleft':
            lx = x + spacer
            ly = y + spacer
            for l in labels:
                label_h = drawLab(c, l, lx, ly)
                ly += label_h + spacer
        elif key == 'topright':
            lx = x + width - spacer
            ly = y + spacer
            for l in labels:
                label_h = drawLab(c, l, lx, ly, align='right')
                ly += label_h + spacer
        elif key == 'bottomleft':
            lx = x + spacer
            ly = y + height
            labels.reverse()  # last item goes bottom
            for l in labels:
                ly = ly - l['size'] - spacer
                drawLab(c, l, lx, ly)
        elif key == 'bottomright':
            lx = x + width - spacer
            ly = y + height
            labels.reverse()  # last item goes bottom
            for l in labels:
                ly = ly - l['size'] - spacer
                drawLab(c, l, lx, ly, align='right')
        elif key == 'top':
            lx = x + (width/2)
            ly = y
            labels.reverse()
            for l in labels:
                ly = ly - l['size'] - spacer
                drawLab(c, l, lx, ly, align='center')
        elif key == 'bottom':
            lx = x + (width/2)
            ly = y + height + spacer
            for l in labels:
                label_h = drawLab(c, l, lx, ly, align='center')
                ly += label_h + spacer
        elif key == 'left':
            lx = x - spacer
            total_h = sum([l['size'] for l in labels]) + spacer * (len(labels)-1)
            ly = y + (height-total_h)/2
            for l in labels:
                label_h = drawLab(c, l, lx, ly, align='right')
                ly += label_h + spacer
        elif key == 'right':
            lx = x + width + spacer
            total_h = sum([l['size'] for l in labels]) + spacer * (len(labels)-1)
            ly = y + (height-total_h)/2
            for l in labels:
                label_h = drawLab(c, l, lx, ly)
                ly += label_h + spacer


def drawScalebar(c, panel, region_width, pageHeight):

    x = panel['x']
    y = panel['y']
    width = panel['width']
    height = panel['height']
    if not ('scalebar' in panel and 'show' in panel['scalebar'] and panel['scalebar']['show']):
        return

    if not ('pixel_size' in panel and panel['pixel_size'] > 0):
        print "Can't show scalebar - pixel_size is not defined for panel"

    sb = panel['scalebar']

    spacer = 0.05 * max(height, width)

    c.setLineWidth(2)
    color = sb['color']
    red = int(color[0:2], 16)
    green = int(color[2:4], 16)
    blue = int(color[4:6], 16)
    c.setStrokeColorRGB(red, green, blue)

    def draw_sb(sb_x, sb_y, align='left'):

        print "Adding Scalebar of %s microns. Pixel size is %s microns" % (sb['length'], panel['pixel_size'])
        pixels_length = sb['length'] / panel['pixel_size']
        scale_to_canvas = panel['width'] / region_width
        canvas_length = pixels_length * scale_to_canvas
        print 'Scalebar length (panel pixels):', pixels_length
        print 'Scale by %s to page coordinate length: %s' % (scale_to_canvas, canvas_length)
        sb_y = pageHeight - sb_y
        if align == 'left':
            c.line(sb_x, sb_y, sb_x + canvas_length, sb_y)
        else:
            c.line(sb_x, sb_y, sb_x - canvas_length, sb_y)

    position = sb['position']
    print 'position', position

    if position == 'topleft':
        lx = x + spacer
        ly = y + spacer
        draw_sb(lx, ly)
    elif position == 'topright':
        lx = x + width - spacer
        ly = y + spacer
        draw_sb(lx, ly, align="right")
    elif position == 'bottomleft':
        lx = x + spacer
        ly = y + height - spacer
        draw_sb(lx, ly)
    elif position == 'bottomright':
        lx = x + width - spacer
        ly = y + height - spacer
        draw_sb(lx, ly, align="right")


def drawPanel(conn, c, panel, pageHeight, idx):

    imageId = panel['imageId']
    channels = panel['channels']
    x = panel['x']
    y = panel['y']
    width = panel['width']
    height = panel['height']

    # Since coordinate system is 'bottom-up', convert from 'top-down'
    y = pageHeight - height - y

    image = conn.getObject("Image", imageId)
    applyRdefs(image, channels)

    tile = get_panel_region_xywh(panel)

    print "TILE", tile

    z = panel['theZ']     # image._re.getDefaultZ()
    t = panel['theT']     # image._re.getDefaultT()

    # pilImg = image.renderImage(z, t)
    imgData = image.renderJpegRegion(z, t, tile['x'], tile['y'], tile['width'], tile['height'], compression=1.0)
    i = StringIO(imgData)
    pilImg = Image.open(i)
    tempName = str(idx) + ".jpg"
    pilImg.save(tempName)

    c.drawImage(tempName, x, y, width, height)

    drawScalebar(c, panel, tile['width'], pageHeight)


def create_pdf(conn, scriptParams):

    n = datetime.now()
    # time-stamp name by default: WebFigure_2013-10-29_22-43-53.pdf (tried : but they get replaced)
    figureName = "WebFigure_%s-%s-%s_%s-%s-%s.pdf" % (n.year, n.month, n.day, n.hour, n.minute, n.second)
    pageWidth = scriptParams['Page_Width']
    pageHeight = scriptParams['Page_Height']
    if 'Figure_Name' in scriptParams:
        figureName = scriptParams['Figure_Name']
    if not figureName.endswith('.pdf'):
        figureName = "%s.pdf" % figureName

    c = canvas.Canvas(figureName, pagesize=(pageWidth, pageHeight))

    panels_json_string = scriptParams['Panels_JSON']
    panels_json = json.loads(panels_json_string)

    for i, panel in enumerate(panels_json):

        drawPanel(conn, c, panel, pageHeight, i)
        drawLabels(conn, c, panel, pageHeight)

    # complete page and save
    c.showPage()
    c.save()

    ns = "omero.web.figure.pdf"
    fileAnn = conn.createFileAnnfromLocalFile(figureName, mimetype="application/pdf", ns=ns, desc=panels_json_string)
    return fileAnn


def runScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters.
    """

    client = scripts.client('Figure_To_Pdf.py', """Used by web.figure to generate pdf figures from json data""",

        scripts.Int("Page_Width", optional=False, grouping="1", default=612),

        scripts.Int("Page_Height", optional=False, grouping="2", default=792),

        scripts.String("Panels_JSON", optional=False, grouping="3",
            description="All Panel Data as json stringified"),

        scripts.String("Figure_Name", grouping="4", description="Name of the Pdf Figure")
    ) 

    try:
        session = client.getSession()
        scriptParams = {}

        conn = BlitzGateway(client_obj=client)

        # process the list of args above.
        for key in client.getInputKeys():
            if client.getInput(key):
                scriptParams[key] = client.getInput(key, unwrap=True)
        print scriptParams

        # call the main script - returns a file annotation wrapper
        fileAnnotation = create_pdf(conn, scriptParams)

        # return this fileAnnotation to the client.
        client.setOutput("Message", rstring("Pdf Figure created"))
        if fileAnnotation is not None:
            client.setOutput("File_Annotation", robject(fileAnnotation._obj))

    finally:
        client.closeSession()

if __name__ == "__main__":
    runScript()
