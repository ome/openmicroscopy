% Unit tests for the Line shape helper function
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

classdef TestLine < TestShape
    
    properties
        x = [10 20];
        y = [5 8];
    end
    
    methods
        function self = TestLine(name)
            self = self@TestShape(name);
        end
        
        function setUp(self)
            self.createLine();
        end
        
        function createLine(self)
            self.shape = createLine(self.x, self.y);
        end
        
        function testSimpleLine(self)
            assertTrue(isa(self.shape, 'omero.model.LineI'));
            assertEqual(self.shape.getX1().getValue(), self.x(1));
            assertEqual(self.shape.getX2().getValue(), self.x(2));
            assertEqual(self.shape.getY1().getValue(), self.y(1));
            assertEqual(self.shape.getY2().getValue(), self.y(2));
        end
        
        function testScalarX(self)
            self.x = 1;
            assertExceptionThrown(@() self.createLine(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testScalarY(self)
            self.y = 1;
            assertExceptionThrown(@() self.createLine(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testDimensionX(self)
            self.x = zeros(3, 1);
            assertExceptionThrown(@() self.createLine(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testDimensionY(self)
            self.y = zeros(3, 1);
            assertExceptionThrown(@() self.createLine(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
    end
end
