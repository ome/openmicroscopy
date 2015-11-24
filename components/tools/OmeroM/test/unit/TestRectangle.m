% Unit tests for the Rectangle shape helper function
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

classdef TestRectangle < TestShape
    
    properties
        x = 10
        y = 20
        w = 10
        h = 20
    end
    
    methods
        function self = TestRectangle(name)
            self = self@TestShape(name);
        end
        
        function setUp(self)
            self.createRectangle()
        end
        
        function createRectangle(self)
            self.shape = createRectangle(self.x, self.y, self.w, self.h);
        end
        
        function testValidRectangle(self)
            assertTrue(isa(self.shape, 'omero.model.RectangleI'));
            assertEqual(self.shape.getX().getValue(), self.x);
            assertEqual(self.shape.getY().getValue(), self.y);
            assertEqual(self.shape.getWidth().getValue(), self.w);
            assertEqual(self.shape.getHeight().getValue(), self.h);
        end
        
        function testNegativeHeight(self)
            self.h = -1;
            assertExceptionThrown(@() self.createRectangle(), 'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testNegativeWidth(self)
            self.w = -1;
            assertExceptionThrown(@() self.createRectangle(), 'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testZeroHeight(self)
            self.h = 0;
            assertExceptionThrown(@() self.createRectangle(), 'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testZeroWidth(self)
            self.w = 0;
            assertExceptionThrown(@() self.createRectangle(), 'MATLAB:InputParser:ArgumentFailedValidation');
        end
    end
end
