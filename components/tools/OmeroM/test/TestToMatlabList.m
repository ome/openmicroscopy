% Unit tests for the toMatlabList helper function
%
% Require MATLAB xUnit Test Framework to be installed
% http://www.mathworks.com/matlabcentral/fileexchange/22846-matlab-xunit-test-framework

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
        
        function testEmptyArray(self)
            self.initArrayList(0);
            self.compareLists();
            assertTrue(isempty(self.matlabList));
        end
        
        function testVector(self)
            self.initArrayList(10);
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
        end
        
    end
end