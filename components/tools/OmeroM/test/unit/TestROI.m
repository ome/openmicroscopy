% Unit tests for the ROI helper function
%
% Require MATLAB xUnit Test Framework to be installed
% http://www.mathworks.com/matlabcentral/fileexchange/22846-matlab-xunit-test-framework

% Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

classdef TestROI < TestCase
    
    properties
        shape
        z = 2
        c = 1
        t = 3
    end
    
    methods
        function self = TestROI(name)
            self = self@TestCase(name);
        end
        
        % Point
        function testPoint(self)
            x = 10;
            y = 10;
            self.shape = createPoint(x, y);
            
            assertTrue(isa(self.shape, 'omero.model.PointI'));
            assertEqual(self.shape.getCx().getValue(), x);
            assertEqual(self.shape.getCy().getValue(), y);

            self.setShapeCoordinates();
        end
        
        % Rectangle
        function testRectangle(self)
            x = 10;
            y = 5;
            w = 20;
            h = 15;
            self.shape = createRectangle(x, y, w, h);
            
            assertTrue(isa(self.shape, 'omero.model.RectI'));
            assertEqual(self.shape.getX().getValue(), x);
            assertEqual(self.shape.getY().getValue(), y);
            assertEqual(self.shape.getWidth().getValue(), w);
            assertEqual(self.shape.getHeight().getValue(), h);

            self.setShapeCoordinates();
        end
        
        % Line
        function testLine(self)
            x = [10 20];
            y = [5 8];
            self.shape = createLine(x, y);
            
            assertTrue(isa(self.shape, 'omero.model.LineI'));
            assertEqual(self.shape.getX1().getValue(), x(1));
            assertEqual(self.shape.getX2().getValue(), x(2));
            assertEqual(self.shape.getY1().getValue(), y(1));
            assertEqual(self.shape.getY2().getValue(), y(2));

            self.setShapeCoordinates();
        end
        
        % Polyline
        function testPolyline(self)
            x = 1:5;
            y = 6:10;
            self.shape = createPolyline(x, y);
            
            assertTrue(isa(self.shape, 'omero.model.PolylineI'));
            assertEqual(char(self.shape.getPoints().getValue()),...
                sprintf('%g,%g ', x, y));

            self.setShapeCoordinates();
        end
        
        % Polygon
        function testPolygon(self)
            x = 1:5;
            y = 6:10;
            self.shape = createPolygon(x, y);
            
            assertTrue(isa(self.shape, 'omero.model.PolygonI'));
            assertEqual(char(self.shape.getPoints().getValue()),...
                sprintf('%g,%g ', x, y));

            self.setShapeCoordinates();
        end
        
        % Ellipse
        function testEllipse(self)
            x = 10;
            y = 20;
            rx = 5;
            ry = 8;
            self.shape = createEllipse(x, y, rx, ry);
            
            assertTrue(isa(self.shape, 'omero.model.EllipseI'));
            assertEqual(self.shape.getCx().getValue(), x);
            assertEqual(self.shape.getCy().getValue(), y);
            assertEqual(self.shape.getRx().getValue(), rx);
            assertEqual(self.shape.getRy().getValue(), ry);

            self.setShapeCoordinates();
        end
        
        function testCircle(self)
            x = 10;
            y = 20;
            rx = 5;
            self.shape = createEllipse(x, y, rx);
            
            assertTrue(isa(self.shape, 'omero.model.EllipseI'));
            assertEqual(self.shape.getCx().getValue(), x);
            assertEqual(self.shape.getCy().getValue(), y);
            assertEqual(self.shape.getRx().getValue(), rx);
            assertEqual(self.shape.getRy().getValue(), rx);
            
            self.setShapeCoordinates();
        end
        
        % Mask
        function testSimpleMask(self)
            w = 30;
            h = 20;
            mask = ones(h, w);
            self.shape = createMask(mask);
            
            assertTrue(isa(self.shape, 'omero.model.MaskI'));
            assertEqual(self.shape.getX().getValue(), 0);
            assertEqual(self.shape.getY().getValue(), 0);
            assertEqual(self.shape.getWidth().getValue(), w);
            assertEqual(self.shape.getHeight().getValue(), h);
            assertEqual(self.shape.getBytes(), int8(mask(:)));

            self.setShapeCoordinates();
        end
        
        function testMask(self)
            x = 5;
            y = 10;
            w = 30;
            h = 20;
            mask = ones(h, w);
            self.shape = createMask(x, y, mask);
            
            assertTrue(isa(self.shape, 'omero.model.MaskI'));
            assertEqual(self.shape.getX().getValue(), x);
            assertEqual(self.shape.getY().getValue(), y);
            assertEqual(self.shape.getWidth().getValue(), w);
            assertEqual(self.shape.getHeight().getValue(), h);
            assertEqual(self.shape.getBytes(), int8(mask(:)));

            self.setShapeCoordinates();
        end
        
        % Shape coordinates test function
        function setShapeCoordinates(self)
            assertTrue(isempty(self.shape.getTheZ()));
            assertTrue(isempty(self.shape.getTheC()));
            assertTrue(isempty(self.shape.getTheT()));
            
            setShapeCoordinates(self.shape, self.z, self.c, self.t);
            assertEqual(self.shape.getTheZ().getValue(), self.z);
            assertEqual(self.shape.getTheC().getValue(), self.c);
            assertEqual(self.shape.getTheT().getValue(), self.t);
        end
        
    end
end