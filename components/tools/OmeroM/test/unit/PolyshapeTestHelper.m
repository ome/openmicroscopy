% Unit tests for the Polyline/Polygon helper function
%
% Require MATLAB xUnit Test Framework to be installed
% http://www.mathworks.com/matlabcentral/fileexchange/22846-matlab-xunit-test-framework

% Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

classdef PolyshapeTestHelper < handle
    
    properties
        x
        y
        class
    end
    
    methods
        function testRows(self)
            self.x = [10 20 30 40];
            self.y = [5 10 15 20];
            self.createShape();
            assertTrue(isa(self.shape, self.class));
            assertEqual(char(self.shape.getPoints().getValue()),...
                sprintf('%g,%g ', [self.x; self.y]));
        end
        
        function testColumns(self)
            self.x = [10 20 30 40]';
            self.y = [5 10 15 20]';
            self.createShape();
            assertTrue(isa(self.shape, self.class));
            assertEqual(char(self.shape.getPoints().getValue()),...
                sprintf('%g,%g ', [self.x'; self.y']));
        end
        
        function testNonMatchingDimensions(self)
            self.x = [10 20 30 40];
            self.y = [5 10];
            assertExceptionThrown(@() self.createShape(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testScalarX(self)
            self.x = 1;
            self.y = [5 10];
            assertExceptionThrown(@() self.createShape(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testScalarY(self)
            self.x = [5 10];
            self.y = 1;
            assertExceptionThrown(@() self.createShape(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
    end
    
    methods(Abstract)
        createShape(self, x, y)
    end
end
