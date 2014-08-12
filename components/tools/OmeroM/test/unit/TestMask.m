% Unit tests for the Mask shape helper function
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

classdef TestMask < TestShape
    
    properties
        x = 0
        y = 0
        width = 4
        height = 4
        mask
        bytes
    end
    
    methods
        function self = TestMask(name)
            self = self@TestShape(name);
        end
        
        % Input check
        function testNoInput(self)
            assertExceptionThrown(@() createMask(),...
                'MATLAB:minrhs');
        end
        
        function testNullMask(self)
            self.mask = ones(0,0);
            assertExceptionThrown(@() createMask(ones(0, 0)),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        % Mask
        function checkMask(self)
            assertTrue(isa(self.shape, 'omero.model.MaskI'));
            assertEqual(self.shape.getX().getValue(), self.x);
            assertEqual(self.shape.getY().getValue(), self.y);
            assertEqual(self.shape.getWidth().getValue(), self.width);
            assertEqual(self.shape.getHeight().getValue(), self.height);
            assertEqual(self.shape.getBytes(), self.bytes);
        end
        
        function testDefault(self)
            self.mask = zeros(self.width, self.height);
            self.bytes = zeros(self.width * self.height / 8, 1, 'int8');
            self.shape = createMask(self.mask);
            self.checkMask();
        end
        
        function testZeroCoordinates(self)
            self.mask = zeros(self.width, self.height);
            self.bytes = zeros(self.width * self.height / 8, 1, 'int8');
            self.shape = createMask(0, 0, self.mask);
            self.checkMask();
        end
        
        function testLogicalInput(self)
            self.mask = false(self.width, self.height);
            self.bytes = zeros(self.width * self.height / 8, 1, 'int8');
            self.shape = createMask(self.mask);
            self.checkMask();
        end
        
        function testNonZeroCoordinates(self)
            self.x = 10;
            self.y = 20;
            self.mask = false(self.width, self.height);
            self.bytes = zeros(self.width * self.height / 8, 1, 'int8');
            self.shape = createMask(self.x, self.y, self.mask);
            self.checkMask();
        end
    end
end
