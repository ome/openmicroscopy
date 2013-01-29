% Unit tests for the toMatlabList helper function
%
% Require MATLAB xUnit Test Framework to be installed
% http://www.mathworks.com/matlabcentral/fileexchange/22846-matlab-xunit-test-framework

classdef TestToMatrix < TestCase
    properties
        binaryData
        matrix
        pixels
        sizeX = 10
        sizeY = 10
        sizeZ = 10
        pixelsType
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
        
        function testPlane(self)
            self.binaryData = ones(self.sizeX * self.sizeY, 1);
            self.pixelsType.setValue(rstring('double'));
            self.pixels.setPixelsType(self.pixelsType);
            self.matrix = toMatrix(self.binaryData, self.pixels);
        end
        
        function testStack(self)
            self.binaryData = ones(self.sizeX * self.sizeY * self.sizeZ, 1);
            self.pixelsType.setValue(rstring('double'));
            self.pixels.setPixelsType(self.pixelsType);
            self.matrix = toMatrix(self.binaryData, self.pixels);
        end
        
        % Pixel type tests
        
        function testINT8(self)
            self.binaryData = int8(ones(self.sizeX * self.sizeY, 1));
            self.pixelsType.setValue(rstring('int8'));
            self.pixels.setPixelsType(self.pixelsType);
            self.matrix = toMatrix(self.binaryData, self.pixels);
        end
        
        function testUINT8(self)
            self.binaryData = uint8(ones(self.sizeX * self.sizeY, 1));
            self.pixelsType.setValue(rstring('uint8'));
            self.pixels.setPixelsType(self.pixelsType);
            self.matrix = toMatrix(self.binaryData, self.pixels);
        end
        
        function testINT16(self)
            self.binaryData = int16(ones(self.sizeX * self.sizeY, 1));
            self.pixelsType.setValue(rstring('int16'));
            self.pixels.setPixelsType(self.pixelsType);
            self.matrix = toMatrix(self.binaryData, self.pixels);
        end
        
        function testUINT16(self)
            self.binaryData = uint16(ones(self.sizeX * self.sizeY, 1));
            self.pixelsType.setValue(rstring('uint16'));
            self.pixels.setPixelsType(self.pixelsType);
            self.matrix = toMatrix(self.binaryData, self.pixels);
        end


        

    end
    
end