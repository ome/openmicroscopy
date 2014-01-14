% Unit tests for the Polygon shape helper function
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

classdef TestPolygon < TestShape
    
    properties
        x = [10 20 30 40];
        y = [5 10 15 20];
    end
    
    methods
        function self = TestPolygon(name)
            self = self@TestShape(name);
        end
        
        function setUp(self)
            self.createPolygon()
        end
        
        function createPolygon(self)
            self.shape = createPolygon(self.x, self.y);
        end
        
        function testSimplePolygon(self)
            assertTrue(isa(self.shape, 'omero.model.PolygonI'));
            assertEqual(char(self.shape.getPoints().getValue()),...
                sprintf('%g,%g ', [self.x; self.y]));
        end
        
        function testNonMatchingDimensions(self)
            self.x = [10 20];
            assertExceptionThrown(@() self.createPolygon(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testScalarX(self)
            self.x = 1;
            assertExceptionThrown(@() self.createPolygon(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testScalarY(self)
            self.y = 1;
            assertExceptionThrown(@() self.createPolygon(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
    end
end
