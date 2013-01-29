% Unit tests for the toJavaList helper function
%
% Require MATLAB xUnit Test Framework to be installed
% http://www.mathworks.com/matlabcentral/fileexchange/22846-matlab-xunit-test-framework

classdef TestToJavaList < TestJavaMatlabList
    
    properties
        castFun
    end
    
    methods
        function self = TestToJavaList(name)
            self = self@TestJavaMatlabList(name);
        end
        
        % Input parsing test
        function testWrongMatlabListType(self)
            self.initMatlabArray(10, 10);
            assertExceptionThrown(@() toJavaList(self.matlabList),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        function testWrongCastFunType(self)
            self.initMatlabArray(10, 1);
            self.castFun = 10;
            assertExceptionThrown(@() toJavaList(self.matlabList, self.castFun),...
                'MATLAB:InputParser:ArgumentFailedValidation');
        end
        
        % MatlabList tests
        function testEmptyArray(self)
            self.initMatlabArray(1, 0);
            self.javaList = toJavaList(self.matlabList);
            self.compareLists();
            assertTrue(self.javaList.isEmpty);
        end
        
        function testRowVector(self)
            self.initMatlabArray(1, 10);
            self.javaList = toJavaList(self.matlabList);
            self.compareLists();
        end
        
        function testColumnVector(self)
            self.initMatlabArray(10, 1);
            self.javaList = toJavaList(self.matlabList);
            self.compareLists();
        end
        
        % Casting function
        function testCastFunHandle(self)
            self.initMatlabArray(1, 10);
            self.castFun = @java.lang.Integer;
            self.javaList = toJavaList(self.matlabList, self.castFun);
            self.compareLists();
        end
        
        function testCastFunString(self)
            self.initMatlabArray(1, 10);
            self.castFun = 'java.lang.Integer';
            self.javaList = toJavaList(self.matlabList, self.castFun);
            self.compareLists();
        end
    end
end