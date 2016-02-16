% Unit tests for the Point shape helper function
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

classdef TestPoint < TestShape
    
    properties
        x = 10
        y = 20
    end
    
    methods
        function self = TestPoint(name)
            self = self@TestShape(name);
        end
        
        function setUp(self)
            self.createPoint()
        end
        
        function createPoint(self)
            self.shape = createPoint(self.x, self.y);
        end
        
        function testValidPoint(self)
            assertTrue(isa(self.shape, 'omero.model.PointI'));
            assertEqual(self.shape.getX().getValue(), self.x);
            assertEqual(self.shape.getY().getValue(), self.y);
        end
        
        function testEmptyInput(self)
            self.x = [];
            assertExceptionThrown(@() self.createPoint(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testVectorInput(self)
            self.x = 1:10;
            assertExceptionThrown(@() self.createPoint(),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
    end
end
