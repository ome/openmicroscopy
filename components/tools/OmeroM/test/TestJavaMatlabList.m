classdef TestJavaMatlabList < TestCase
    properties
        size
        matlabList
        javaList
    end
    
    methods
        function self = TestJavaMatlabList(name)
            self = self@TestCase(name);
        end
        
        % Helper functions
        function initMatlabArray(self, sizeX, sizeY)
            self.size = sizeX * sizeY;
            self.matlabList = ones(sizeX, sizeY);
        end
        
        function initArrayList(self, size)
            self.size = size;
            self.javaList = java.util.ArrayList;
            for i = 1 : self.size
                self.javaList.add(1);
            end
        end
        
        
        function compareLists(self)
            % Check list types
            assertTrue(isa(self.javaList, 'java.util.ArrayList'));
            assertTrue(isvector(self.matlabList) || isempty(self.matlabList));
            
            % Check list sizes
            assertEqual(self.javaList.size(), self.size);
            assertEqual(numel(self.matlabList),self.size);
            
            % Check list elements
            for i = 1 : self.size
                assertEqual(self.javaList.get(i-1), self.matlabList(i));
            end
        end
    end
    
end