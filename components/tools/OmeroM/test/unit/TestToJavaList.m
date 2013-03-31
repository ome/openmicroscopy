% Unit tests for the toJavaList helper function
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

classdef TestToJavaList < TestJavaMatlabList
    
    methods
        function self = TestToJavaList(name)
            self = self@TestJavaMatlabList(name);
        end
        
        % Input parsing test
        function testWrongMatlabListType(self)
            self.initMatlabArray(10, 10, 'double');
            assertExceptionThrown(@() toJavaList(self.matlabList),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testWrongCastFunType(self)
            self.initMatlabArray(10, 1, 'double');
            assertExceptionThrown(@() toJavaList(self.matlabList, 10),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        % MatlabList tests
        function testEmptyArray(self)
            self.initMatlabArray(1, 0, 'double');
            self.javaList = toJavaList(self.matlabList);
            assertTrue(self.javaList.isEmpty);
        end
        
        function testRowVector(self)
            self.initMatlabArray(1, 10, 'double');
            self.javaList = toJavaList(self.matlabList);
            self.javaValue = java.lang.Double(self.matlabList(1));
            self.compareLists();
        end
        
        function testColumnVector(self)
            self.initMatlabArray(10, 1, 'double');
            self.javaList = toJavaList(self.matlabList);
            self.javaValue = java.lang.Double(self.matlabList(1));
            self.compareLists();
        end
        
        % Numeric input tests
        function checkNumericInput(self, matlabclass, javaclass)
            self.initMatlabArray(1, 1, matlabclass);
            javaConstructor = str2func(javaclass);
            self.javaValue = javaConstructor(self.matlabList(1));
            self.javaList = toJavaList(self.matlabList);
            self.compareLists();
        end
        
        function testDOUBLE(self)
            self.checkNumericInput('double', 'java.lang.Double');
        end
        
        function testSINGLE(self)
            self.checkNumericInput('single', 'java.lang.Float');
        end
        
        function testUINT8(self)
            self.checkNumericInput('uint8', 'java.lang.Byte');
        end
        
        function testINT8(self)
            self.checkNumericInput('int8', 'java.lang.Byte');
        end
        
        function testUINT16(self)
            self.checkNumericInput('uint16', 'java.lang.Short');
        end
        
        function testINT16(self)
            self.checkNumericInput('int16', 'java.lang.Short');
        end
        
        function testUINT32(self)
            self.checkNumericInput('uint32', 'java.lang.Integer');
        end
        
        function testINT32(self)
            self.checkNumericInput('int32', 'java.lang.Integer');
        end
        
        function testUINT64(self)
            self.checkNumericInput('uint64', 'java.lang.Long');
        end
        
        function testINT64(self)
            self.checkNumericInput('int64', 'java.lang.Long');
        end
        
        % String cell array test
        function testCellArrayStrings(self)
            self.initMatlabCellArray(10, 1);
            self.javaValue = java.lang.String(self.matlabList{1});
            self.javaList = toJavaList(self.matlabList);
            self.compareLists();
        end
        
        function testString(self)
            self.initMatlabCellArray(1, 1);
            self.javaValue = java.lang.String(self.matlabList{1});
            self.javaList = toJavaList(self.matlabList{1});
            self.compareLists();
        end
        
        % Casting function
        function checkNumericCasting(self, classname)
            self.initMatlabArray(1, 1, 'double');
            castFun = str2func(classname);
            self.javaValue = castFun(self.matlabList(1));
            self.javaList = toJavaList(self.matlabList, castFun);
            self.compareLists();
            self.javaList = toJavaList(self.matlabList, classname);
            self.compareLists();
        end
        
        function testDouble(self)
            self.checkNumericCasting('java.lang.Double');
        end
        
        function testFloat(self)
            self.checkNumericCasting('java.lang.Float');
        end
        
        function testByte(self)
            self.checkNumericCasting('java.lang.Byte');
        end
        
        function testInteger(self)
            self.checkNumericCasting('java.lang.Integer');
        end
        
        function testLong(self)
            self.checkNumericCasting('java.lang.Long');
        end
        
        function testShort(self)
            self.checkNumericCasting('java.lang.Short');
        end
    end
end