from PIL import ImageDraw;
from PIL import Image;

class DrawingCanvas:
    
    def __init__(self):
        self.width = 0;
        self.height = 0;
        self.image = None;
        self.draw = None;    
        
    def createImage(self, width, height):
        self.image = Image.new('RGBA', (width, height), (0, 0, 0, 255));
        self.width = width;
        self.height = height;
        
    def setImage(self, image, width, height):
        self.image = image;
        self.width = width;
        self.height = height;
        
    def drawElements(self, elementList):
        if(self.draw == None):
            self.draw = ImageDraw.Draw(self.image);
        for element in elementList:
            element.acceptVisitor(self);
        return self.image;
    
    def getFillColour(self, shapeSettings):
        return shapeSettings[1][0];

    def getStrokeColour(self, shapeSettings):
        return shapeSettings[0][0];

    def getStrokeWidth(self, shapeSettings):
        return shapeSettings[0][1];
    
    def drawEllipse(self, cx, cy, rx, ry, shapeSettings, affineTransform = None):
        x = cx-rx;
        y = cy-ry;
        w = x+rx*2;
        h = y+ry*2;
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        self.draw.ellipse((x,y,w,h), fill = fillColour, outline = strokeColour);
                      
    def drawRectangle(self, x, y, w, h, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        if(affineTransform==None):
            self.draw.rectangle((x,y,w,h), fill = fillColour, outline = strokeColour);
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0));
            newDraw = ImageDraw.Draw(im);
            newDraw.rectangle((x,y,w,h), fill = fillColour, outline = strokeColour);
            newImage = im.transform((self.width,self.height), Image.AFFINE, affineTransform);
            self.image.paste(newImage);
        
        
    def drawPolygon(self, pointTupleList, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        if(affineTransform==None):
            self.draw.polygon(pointTupleList, fill = fillColour, outline = strokeColour);
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0));
            newDraw = ImageDraw.Draw(im);
            self.draw.polygon(pointTupleList, fill = fillColour, outline = strokeColour);
            newImage = im.transform((self.width,self.height), Image.AFFINE, affineTransform);
            self.image.paste(newImage);
 
    def drawLine(self, x1, y1, x2, y2, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        if(affineTransform==None):
            self.draw.line([(x1, y1), (x2, y2)], fill = strokeColour, width = strokeWidth);
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0));
            newDraw = ImageDraw.Draw(im);
            self.draw.line([(x1, y1), (x2, y2)], fill = strokeColour, width = strokeWidth);
            newImage = im.transform((self.width,self.height), Image.AFFINE, affineTransform);
            self.image.paste(newImage);
       
    def drawPolyline(self, pointTupleList, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        strokeColour = self.getStrokeColour(shapeSettings);
        strokeWidth = self.getStrokeWidth(shapeSettings);
        if(affineTransform==None):
            self.draw.line(pointTupleList, fill = fillColour, width = strokeWidth);
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0));
            newDraw = ImageDraw.Draw(im);
            self.draw.line(pointTupleList, fill = strokeColour, width = strokeColour);
            newImage = im.transform((self.width,self.height), Image.AFFINE, affineTransform);
            self.image.paste(newImage);
    
    def drawMask(self, x, y, width, height, bytes, shapeSettings, affineTransform = None):
        fillColour = self.getFillColour(shapeSettings);
        mask = Image.fromstring('1', (width, height), bytes);
        if(affineTransform==None):
            self.draw.bitmap(x, y, mask, fill = fillColour);
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0));
            newDraw = ImageDraw.Draw(im);
            self.draw.bitmap(x, y, mask, fill = fillColour);
            newImage = im.transform((self.width,self.height), Image.AFFINE, affineTransform);
            self.image.paste(newImage);
        
    def drawText(self, x, y, text, shapeSettings, affineTransform = None):
        textColour = self.getStrokeColour(shapeSettings);
        if(affineTransform==None):
            self.draw.text((x, y), text, fill = textColour); 
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0));
            newDraw = ImageDraw.Draw(im);
            self.draw.text((x, y), text, fill = textColour); 
            newImage = im.transform((self.width,self.height), Image.AFFINE, affineTransform);
            self.image.paste(newImage);