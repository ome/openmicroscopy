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