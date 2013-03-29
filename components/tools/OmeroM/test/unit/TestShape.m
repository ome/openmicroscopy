% Unit tests for the Shape helper function
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

classdef TestShape < TestCase
    
    properties
        shape
        z = 2
        c = 1
        t = 3
    end
    
    methods
        function self = TestShape(name)
            self = self@TestCase(name);
        end
        
        function tearDown(self)
            self.shape = [];
        end
        
        % Shape coordinates test function
        function testDefaultCoordinates(self)
            if isempty(self.shape), return; end
            
            assertTrue(isempty(self.shape.getTheZ()));
            assertTrue(isempty(self.shape.getTheC()));
            assertTrue(isempty(self.shape.getTheT()));
        end
        
        function testNonEmptyCoordinates(self)
            if isempty(self.shape), return; end
            
            setShapeCoordinates(self.shape, self.z, self.c, self.t);
            assertEqual(self.shape.getTheZ().getValue(), self.z);
            assertEqual(self.shape.getTheC().getValue(), self.c);
            assertEqual(self.shape.getTheT().getValue(), self.t);
        end
        
        function testEmptyInput(self)
            if isempty(self.shape), return; end
            
            setShapeCoordinates(self.shape);
            assertTrue(isempty(self.shape.getTheZ()));
            assertTrue(isempty(self.shape.getTheC()));
            assertTrue(isempty(self.shape.getTheT()));
        end
        
        function testEmptyCoordinates(self)
            if isempty(self.shape), return; end
            
            setShapeCoordinates(self.shape, [], [], []);
            assertTrue(isempty(self.shape.getTheZ()));
            assertTrue(isempty(self.shape.getTheC()));
            assertTrue(isempty(self.shape.getTheT()));
        end
        
        function testNegativeZ(self)
            if isempty(self.shape), return; end
            
            f = @() setShapeCoordinates(self.shape, -1, self.c, self.t);
            assertExceptionThrown(f,...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testNegativeC(self)
            if isempty(self.shape), return; end
            
            f = @() setShapeCoordinates(self.shape, self.z, -1, self.t);
            assertExceptionThrown(f,...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testNegativeT(self)
            if isempty(self.shape), return; end
            
            f = @() setShapeCoordinates(self.shape, self.z, self.c, -1);
            assertExceptionThrown(f,...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
    end
end