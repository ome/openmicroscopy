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
        
        % Test input type
        function testVector(self)
            self.initArrayList(10, 1);
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
        end
        
        function testLong(self)
            self.initArrayList(10, java.lang.Long(1));
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
        end
        

        function testSingleImage(self)
            self.initArrayList(1, omero.model.ImageI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.ImageI'));
        end
        
        function testImageList(self)
            self.initArrayList(10, omero.model.ImageI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.ImageI[]'));
        end
        
        
        function testSingleDataset(self)
            self.initArrayList(1, omero.model.DatasetI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.DatasetI'));
        end
        
        function testDatasetList(self)
            self.initArrayList(10, omero.model.DatasetI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.DatasetI[]'));
        end
        
        function testSingleProject(self)
            self.initArrayList(1, omero.model.ProjectI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.ProjectI'));
        end
        
        function testProjectList(self)
            self.initArrayList(10, omero.model.ProjectI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.ProjectI[]'));
        end
        
        function testSinglePlate(self)
            self.initArrayList(1, omero.model.PlateI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.PlateI'));
        end
        
        function testPlateList(self)
            self.initArrayList(10, omero.model.PlateI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.PlateI[]'));
        end
        
        function testSingleScreen(self)
            self.initArrayList(1, omero.model.ScreenI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.ScreenI'));
        end
        
        function testScreenList(self)
            self.initArrayList(10, omero.model.ScreenI());
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
            assertTrue(isa(self.matlabList, 'omero.model.ScreenI[]'));
        end
        
        
        % Test casting functions
        function testINT8(self)
            self.initArrayList(1, 1);
            self.matlabList = toMatlabList(self.javaList, 'int8');
            assertTrue(isa(self.matlabList, 'int8'));
            assertExceptionThrown(@(x) self.compareLists(), 'assertEqual:classNotEqual');
        end
        
        function testUINT8(self)
            self.initArrayList(1, 1);
            self.matlabList = toMatlabList(self.javaList, 'uint8');
            assertTrue(isa(self.matlabList, 'uint8'));
            assertExceptionThrown(@(x) self.compareLists(), 'assertEqual:classNotEqual');
        end
        
        function testINT16(self)
            self.initArrayList(1, 1);
            self.matlabList = toMatlabList(self.javaList, 'int16');
            assertTrue(isa(self.matlabList, 'int16'));
            assertExceptionThrown(@(x) self.compareLists(), 'assertEqual:classNotEqual');
        end
        
        function testUINT16(self)
            self.initArrayList(1, 1);
            self.matlabList = toMatlabList(self.javaList, 'uint16');
            assertTrue(isa(self.matlabList, 'uint16'));
            assertExceptionThrown(@(x) self.compareLists(), 'assertEqual:classNotEqual');
        end
        
        function testSINGLE(self)
            self.initArrayList(1, 1);
            self.matlabList = toMatlabList(self.javaList, 'single');
            assertTrue(isa(self.matlabList, 'single'));
            assertExceptionThrown(@(x) self.compareLists(), 'assertEqual:classNotEqual');
        end
        
        function testDOUBLE(self)
            self.initArrayList(1, 1);
            self.matlabList = toMatlabList(self.javaList, 'double');
            assertTrue(isa(self.matlabList, 'double'));
            self.compareLists();
        end
        
        function testImageListWithCast(self)
            self.initArrayList(10, omero.model.ImageI());
            self.matlabList = toMatlabList(self.javaList, 'omero.model.ImageI');
            assertTrue(isa(self.matlabList, 'omero.model.ImageI[]'));
            self.compareLists();
        end
    end
end