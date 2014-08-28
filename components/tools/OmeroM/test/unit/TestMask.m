% Unit tests for the Mask shape helper function
%
% Require MATLAB xUnit Test Framework to be installed
% http://www.mathworks.com/matlabcentral/fileexchange/22846-matlab-xunit-test-framework

% Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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
        
        function createMaskArray(self, type)
            if nargin < 2 || strcmp(type, 'logical')
                self.mask = false(self.height, self.width);
            else
                self.mask = zeros(self.height, self.width, type);
            end
            self.bytes = zeros(self.height * self.width / 8, 1, 'int8');
        end
        
        % Input check
        function testNoInput(self)
            assertExceptionThrown(@() createMask(),...
                'MATLAB:minrhs');
        end
        
        function testNullMask(self)
            self.mask = ones(0, 0);
            assertExceptionThrown(@() createMask(self.mask),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        % Mask
        function checkMaskShape(self)
            assertTrue(isa(self.shape, 'omero.model.MaskI'));
            assertEqual(self.shape.getX().getValue(), self.x);
            assertEqual(self.shape.getY().getValue(), self.y);
            assertEqual(self.shape.getWidth().getValue(), self.width);
            assertEqual(self.shape.getHeight().getValue(), self.height);
            assertEqual(self.shape.getBytes(), self.bytes);
        end
        
        function testDefault(self)
            self.createMaskArray();
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testZeroCoordinates(self)
            self.createMaskArray();
            self.shape = createMask(self.x, self.y, self.mask);
            self.checkMaskShape();
        end
        
        function testINT8(self)
            self.createMaskArray('int8');
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testUINT8(self)
            self.createMaskArray('uint8');
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testINT16(self)
            self.createMaskArray('int16');
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testUINT16(self)
            self.createMaskArray('uint16');
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testINT32(self)
            self.createMaskArray('int32');
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testUINT32(self)
            self.createMaskArray('uint32');
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testFLOAT(self)
            self.createMaskArray('single');
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testDOUBLE(self)
            self.createMaskArray('double');
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
        
        function testNonZeroCoordinates(self)
            self.x = 10;
            self.y = 20;
            self.createMaskArray();
            self.shape = createMask(self.x, self.y, self.mask);
            self.checkMaskShape();
        end
        
        function testNonSquareMask(self)
            self.height = self.width / 2;
            self.createMaskArray();
            self.shape = createMask(self.mask);
            self.checkMaskShape();
        end
    end
end
