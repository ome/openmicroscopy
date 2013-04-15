% Unit tests for the toMatlabList helper function
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

classdef TestToMatlabList < TestJavaMatlabList
    methods
        function self = TestToMatlabList(name)
            self = self@TestJavaMatlabList(name);
        end
        
        function testWrongInputType(self)
            self.javaList = java.lang.String;
            assertExceptionThrown(@() toMatlabList(self.matlabList),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        % Test empty input
        function testEmptyArray(self)
            self.initArrayList(0, 1);
            self.matlabList = toMatlabList(self.javaList);
            assertTrue(isempty(self.matlabList));
        end
        
        function testEmptyImageList(self)
            self.initArrayList(0, omero.model.ImageI());
            self.matlabList = toMatlabList(self.javaList);
            assertTrue(isempty(self.matlabList));
        end
        
        function checkNumericInput(self, class)
            constructor = str2func(class);
            self.initArrayList(10, constructor(1));
            self.matlabList = toMatlabList(self.javaList);
            self.matlabClass = 'double';
            self.compareLists();
        end
        
        % Test Matlab numeric input
        function testDOUBLE(self)
            self.checkNumericInput('double');
        end
        
        function testSINGLE(self)
            self.checkNumericInput('single');
        end
        
        function testUINT8(self)
            self.checkNumericInput('uint8');
        end
        
        function testINT8(self)
            self.checkNumericInput('int8');
        end
        
        function testUINT16(self)
            self.checkNumericInput('uint16');
        end
        
        function testINT16(self)
            self.checkNumericInput('int16');
        end
        
        function testUINT32(self)
            self.checkNumericInput('uint32');
        end
        
        function testINT32(self)
            self.checkNumericInput('int32');
        end
        
        function testUINT64(self)
            self.checkNumericInput('uint16');
        end
        
        function testINT64(self)
            self.checkNumericInput('int64');
        end
        
        % Test Java numeric input
        function testDouble(self)
            self.checkNumericInput('java.lang.Double');
        end
        
        function testFloat(self)
            self.checkNumericInput('java.lang.Float');
        end
        
        function testByte(self)
            self.checkNumericInput('java.lang.Byte');
        end
        
        function testInteger(self)
            self.checkNumericInput('java.lang.Integer');
        end
        
        function testLong(self)
            self.checkNumericInput('java.lang.Long');
        end
        
        function testShort(self)
            self.checkNumericInput('java.lang.Short');
        end
        
        % String cell array test
        function testStringCellArray(self)
            self.initArrayList(10, java.lang.String('test'))
            
            self.matlabList = toMatlabList(self.javaList);
            self.matlabClass = 'cell';
            self.compareLists();
        end
        
        % Test OMERO objects
        function checkOmeroObjectList(self, class, n)
            constructor = str2func(class);
            self.initArrayList(n, constructor());
            self.matlabList = toMatlabList(self.javaList);
            if n > 1
                self.matlabClass = [class '[]'];
            else
                self.matlabClass = class;
            end
            self.compareLists();
        end
        
        function testSingleImage(self)
            self.checkOmeroObjectList('omero.model.ImageI', 1);
        end
        
        function testImageList(self)
            self.checkOmeroObjectList('omero.model.ImageI', 10);
        end
        
        function testSingleDataset(self)
            self.checkOmeroObjectList('omero.model.ImageI', 1);
        end
        
        function testDatasetList(self)
            self.checkOmeroObjectList('omero.model.DatasetI', 10);
        end
        
        function testSingleProject(self)
            self.checkOmeroObjectList('omero.model.ProjectI', 1);
        end
        
        function testProjectList(self)
            self.checkOmeroObjectList('omero.model.ProjectI', 10);
        end
        
        function testSinglePlate(self)
            self.checkOmeroObjectList('omero.model.PlateI', 1);
        end
        
        function testPlateList(self)
            self.checkOmeroObjectList('omero.model.PlateI', 10);
        end
        
        function testSingleScreen(self)
            self.checkOmeroObjectList('omero.model.ScreenI', 10);
        end
        
        function testScreenList(self)
            self.checkOmeroObjectList('omero.model.ScreenI', 10);
        end
        
        
        % Test casting functions
        function checkNumericCasting(self, class)
            self.initArrayList(1, 1);
            self.matlabList = toMatlabList(self.javaList, class);
            self.matlabClass = class;
            
            self.compareLists();
        end
        
        function testToINT8(self)
            self.checkNumericCasting('int8');
        end
        
        function testToUINT8(self)
            self.checkNumericCasting('uint8');
        end
        
        function testToINT16(self)
            self.checkNumericCasting('int16');
        end
        
        function testToUINT16(self)
            self.checkNumericCasting('uint16');
        end
        
        function testToINT32(self)
            self.checkNumericCasting('int32');
        end
        
        function testToUINT32(self)
            self.checkNumericCasting('uint32');
        end
        
        function testToINT64(self)
            self.checkNumericCasting('int64');
        end
        
        function testToUINT64(self)
            self.checkNumericCasting('uint64');
        end
        
        function testToSINGLE(self)
            self.checkNumericCasting('single');
        end
        
        function testToDOUBLE(self)
            self.checkNumericCasting('double');
        end
    end
end