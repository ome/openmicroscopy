% Unit tests for the toMatrix() helper function
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

classdef TestToMatrix < TestCase
    properties
        binaryData
        matrix
        pixels
        sizeX = 10
        sizeY = 15
        sizeZ = 20
        pixelsType
        type
    end
    
    methods
        function self = TestToMatrix(name)
            self = self@TestCase(name);
            self.pixels = omero.model.PixelsI();
            self.pixels.setSizeX(rint(self.sizeX));
            self.pixels.setSizeY(rint(self.sizeY));
            self.pixels.setSizeZ(rint(self.sizeZ));
            self.pixelsType = omero.model.PixelsTypeI();
        end
        
        function tearDown(self)
            self.pixels = [];
            self.pixelsType = [];
        end
        
        % Wrong input tests
        function testWrongBinaryInput(self)
            self.binaryData = 'test';
            assertExceptionThrown(@() toMatrix(self.binaryData, self.pixels),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testWrongPixelsInput(self)
            self.binaryData = 1;
            assertExceptionThrown(@() toMatrix(self.binaryData, 1),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        
        function testWrongSize(self)
            self.binaryData = 1;
            assertExceptionThrown(@() toMatrix(self.binaryData, self.pixels,...
                [self.sizeX + 1, self.sizeY]),...
                'MATLAB:InputParser:ArgumentFailedValidation');
            assertExceptionThrown(@() toMatrix(self.binaryData, self.pixels,...
                [self.sizeX, self.sizeY + 1]),...
                'MATLAB:InputParser:ArgumentFailedValidation');
            assertExceptionThrown(@() toMatrix(self.binaryData, self.pixels,...
                [self.sizeX, self.sizeY,self.sizeZ + 1]),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        % Pixel type tests
        function setUpPixelsType(self)
            self.binaryData = ones(self.sizeX * self.sizeY, 1, self.type);
            self.pixelsType.setValue(rstring(self.type));
            self.pixels.setPixelsType(self.pixelsType);
            self.matrix = toMatrix(self.binaryData, self.pixels);
            assertEqual(class(self.matrix), self.type);
        end
        
        function testINT8(self)
            self.type = 'int8';
            self.setUpPixelsType();
        end
        
        function testUINT8(self)
            self.type = 'uint8';
            self.setUpPixelsType();
        end
        
        function testINT16(self)
            self.type = 'int16';
            self.setUpPixelsType();
        end
        
        function testUINT16(self)
            self.type = 'uint16';
            self.setUpPixelsType();
        end
        
        function testDOUBLE(self)
            self.type = 'double';
            self.setUpPixelsType();
        end
        
        function testFLOAT(self)
            self.type = 'single';
            self.setUpPixelsType();
        end
        
        % Dimension tests
        function setUpDimension(self, matrix_size, pass)
            self.binaryData = ones(prod(matrix_size), 1);
            self.pixelsType.setValue(rstring('double'));
            self.pixels.setPixelsType(self.pixelsType);
            if pass
                self.matrix = toMatrix(self.binaryData, self.pixels, matrix_size);
            else
                self.matrix = toMatrix(self.binaryData, self.pixels);
            end
            assertEqual(size(self.matrix), matrix_size);
        end
        
        function testPlaneWithNoSizeArgument(self)
            self.setUpDimension([self.sizeX, self.sizeY], false);
        end
        
        function testPlaneWithSizeArgument(self)
            self.setUpDimension([self.sizeX, self.sizeY], true);
        end
        
        function testStackWithNoSizeArgument(self)
            self.setUpDimension([self.sizeX, self.sizeY, self.sizeZ], false);
        end
        
        function testStackWithSizeArgument(self)
            self.setUpDimension([self.sizeX, self.sizeY, self.sizeZ], true);
        end
        
        function testTile(self)
            w = 10;
            h = 10;
            self.setUpDimension([w, h], true);
        end
        
        function testHypercube(self)
            w = 10;
            h = 10;
            d = 5;
            self.setUpDimension([w, h, d], true);
        end
        
        function testScalar(self)
            w = 1;
            h = 1;
            self.setUpDimension([w, h], true);
        end
    end
    
end