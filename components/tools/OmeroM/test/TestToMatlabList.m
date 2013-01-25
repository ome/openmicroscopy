% Unit tests for the toMatlabList helper function
%
% Require MATLAB xUnit Test Framework to be installed
% http://www.mathworks.com/matlabcentral/fileexchange/22846-matlab-xunit-test-framework

classdef TestToMatlabList < TestJavaMatlabList
    methods
        function self = TestToMatlabList(name)
            self = self@TestJavaMatlabList(name);
        end
        
        function testEmptyArray(self)
            self.size = 0;
            self.javaList = java.util.ArrayList;
            self.compareLists();
            assertTrue(isempty(self.matlabList));
        end
        
        function testVector(self)
            self.size = 10;
            self.javaList = java.util.ArrayList;
            for i = 1 : self.size
                self.javaList.add(1);
            end
            self.matlabList = toMatlabList(self.javaList);
            self.compareLists();
        end
        
        function testWrongType(self)
            self.javaList = java.lang.String;
            assertExceptionThrown(@() toMatlabList(self.matlabList),...
                'OMERO:toMatlabList:wrongInputType');
        end
        
    end
    
end