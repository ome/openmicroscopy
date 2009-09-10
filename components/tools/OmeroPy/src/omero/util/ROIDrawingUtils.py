from PIL import ImageDraw;
from PIL import Image;

class DrawingCanvas:
    
    def __init__(self):
        self.width = 0;
        self.height = 0;
        self.image = 0;
        self.draw = 0;    
        
    def createImage(self, width, height):
        self.image = Image.new('RGBA', (w, h), (0, 0, 0, 0));
        self.width = width;
        self.height = height;
        
    def setImage(self, image, width, height):
        self.image = image;
        self.width = width;
        self.height = height;
        
    def drawElements(self, elementList):
        self.draw = ImageDraw.Draw(self.image);
        for element in elementList:
            element.acceptVisitor(self);
        return self.image;
    
    def drawEllipse(self, cx, cy, rx, ry, shapeSettings, affineTransform = None):
        x = cx-rx;
        y = cy-ry;
        w = rx*2;
        h = ry*2;
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        self.draw.ellipse((x,y,w,h), fill=fillColor, outline= strokeColour);
              
    def drawRectangle(self, x, y, w, h, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        self.draw.rectangle((x,y,w,h), fill=fillColor, outline = strokeColour);
        
    def drawPolygon(self, pointTupleList, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        self.draw.polygon(pointTupleList, fill=fillColor, outline = strokeColour);

    def drawLine(self, x1, y1, x2, y2, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        self.draw.line([(x1, y1), (x2, y2)], fill = strokeColour, width = strokeWidth);
       
    def drawPolyline(self, pointTupleList, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        self.draw.line(pointTupleList, fill=fillColor, outline = strokeColour);
    
    def drawMask(self, x, y, width, height, bytes, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        mask = Image.fromstring('1', (width, height), bytes);
        self.draw.bitmap(x, y, mask, fill=fillColor);
        
    def drawText(self, x, y, text, shapeSettings, affineTransform = None):
        textColour = self.getStrokeColour(shapeSettings);
        self.draw.text((x, y), text, fill = textColour); 