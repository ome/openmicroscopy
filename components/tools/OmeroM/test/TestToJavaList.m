% Unit tests for the toJavaList helper function
%
% Require MATLAB xUnit Test Framework to be installed
% http://www.mathworks.com/matlabcentral/fileexchange/22846-matlab-xunit-test-framework

classdef TestToJavaList < TestJavaMatlabList
    
    methods
        function self = TestToJavaList(name)
            self = self@TestJavaMatlabList(name);
        end
        
        function testEmptyArray(self)
            self.size = 0;
            self.matlabList = ones(1, self.size);
            self.javaList = toJavaList(self.matlabList);
            self.compareLists();
            assertTrue(self.javaList.isEmpty);
        end
        
        function testRowVector(self)
            self.size = 10;
            self.matlabList = ones(1, self.size);
            self.javaList = toJavaList(self.matlabList);
            self.compareLists();
        end
        
        function testColumnVector(self)
            self.size = 10;
            self.matlabList = ones(self.size, 1);
            self.javaList = toJavaList(self.matlabList);
            self.compareLists();
        end
        
        function testMatrix(self)
            self.size = 10;
            self.matlabList = ones(self.size, self.size);
            assertExceptionThrown(@() toJavaList(self.matlabList),...
                'OMERO:toJavaList:wrongInputType');
        end
        
    end
    
end