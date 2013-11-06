% Unit tests for the toByteArray() helper function
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

classdef TestToByteArray < TestCase
    properties
        byteArray
        matrix
        pixels
        pixelsType
        sizeX = 10
        sizeY = 15
        type
    end
    
    methods
        function self = TestToByteArray(name)
            self = self@TestCase(name);
            self.pixels = omero.model.PixelsI();
            self.pixels.setSizeX(rint(self.sizeX));
            self.pixels.setSizeY(rint(self.sizeY));
            self.pixelsType = omero.model.PixelsTypeI();
        end
        
        function tearDown(self)
            self.pixels = [];
            self.pixelsType = [];
        end
        
        % Wrong input tests
        function testEmptyInput(self)
            self.matrix = [];
            assertExceptionThrown(@() toByteArray(self.matrix, self.pixels),...
                'OMERO:byteArray:sizeMismatch');
        end
        
        function testNonePixelsInput(self)
            self.matrix = 1;
            assertExceptionThrown(@() toByteArray(self.matrix, 1),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testWrongSizeX(self)
            assertExceptionThrown(@() toByteArray(zeros(self.sizeX, 1), self.pixels),...
                'OMERO:byteArray:sizeMismatch');
        end
        
        function testWrongSizeY(self)
            assertExceptionThrown(@() toByteArray(zeros(1, self.sizeY), self.pixels),...
                'OMERO:byteArray:sizeMismatch');
        end
        
        function testStack(self)
            self.matrix =  zeros(self.sizeX, self.sizeY, 10);
            assertExceptionThrown(@() toByteArray(self.matrix, self.pixels),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testWrongPixelType(self)
            self.matrix =  zeros(self.sizeX, self.sizeY, 'int8');
            self.pixelsType.setValue(rstring('uint8'));
            self.pixels.setPixelsType(self.pixelsType);
            assertExceptionThrown(@() toByteArray(self.matrix, self.pixels),...
                'OMERO:byteArray:typeMismatch');
        end
        
        % Pixel type tests
        function checkBytes(self, nBytes)
            self.matrix = ones(self.sizeX, self.sizeY, self.type);
            self.pixelsType.setValue(rstring(self.type));
            self.pixels.setPixelsType(self.pixelsType);
            self.byteArray = toByteArray(self.matrix, self.pixels);
            assertEqual(class(self.byteArray), 'int8');
            assertEqual(numel(self.byteArray),...
                self.sizeX * self.sizeY * nBytes);
        end
        
        function testINT8(self)
            self.type = 'int8';
            self.checkBytes(1);
        end
        
        function testUINT8(self)
            self.type = 'uint8';
            self.checkBytes(1);
        end
        
        function testINT16(self)
            self.type = 'int16';
            self.checkBytes(2);
        end
        
        function testUINT16(self)
            self.type = 'uint16';
            self.checkBytes(2);
        end
        
        function testINT32(self)
            self.type = 'int32';
            self.checkBytes(4);
        end
        
        function testUINT32(self)
            self.type = 'uint32';
            self.checkBytes(4);
        end
        
        function testFLOAT(self)
            self.type = 'single';
            self.checkBytes(4);
        end
        
        function testDOUBLE(self)
            self.type = 'double';
            self.checkBytes(8);
        end
    end
end