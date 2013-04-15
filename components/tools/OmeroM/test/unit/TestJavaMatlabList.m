% Helper for the java/Matlab list conversion unit tests

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

classdef TestJavaMatlabList < TestCase
    properties
        size
        matlabList
        matlabClass
        javaList
        javaValue
    end
    
    methods
        function self = TestJavaMatlabList(name)
            self = self@TestCase(name);
        end
        
        function tearDown(self)
            self.matlabList = [];
            self.javaList = [];
            self.javaValue = [];
        end
        
        % Helper functions
        function initMatlabArray(self, sizeX, sizeY, class)
            self.size = sizeX * sizeY;
            self.matlabList = ones(sizeX, sizeY, class);
            self.matlabClass = class;
        end
        
        function initMatlabCellArray(self, sizeX, sizeY)
            self.size = sizeX * sizeY;
            self.matlabList = repmat({'test'}, sizeX, sizeY);
            self.matlabClass = 'cell';
        end
        
        function initArrayList(self, size, value)
            self.size = size;
            self.javaValue = value;
            self.javaList = java.util.ArrayList;
            for i = 1 : self.size
                self.javaList.add(value);
            end
        end
        
        
        function compareLists(self)
            % Check list types
            assertTrue(isa(self.javaList, 'java.util.ArrayList'));
            assertTrue(isvector(self.matlabList) || isempty(self.matlabList));
            
            % Check element classes
            assertTrue(self.javaList.contains(self.javaValue));
            assertTrue(isa(self.matlabList, self.matlabClass));
            
            % Check list sizes
            assertEqual(self.javaList.size(), self.size);
            assertEqual(numel(self.matlabList),self.size);
            
            % Compare list elements
            for i = 1 : self.size
                if iscell(self.matlabList)
                    matlabValue = self.matlabList{i};
                else
                    matlabValue = self.matlabList(i);
                end
                assertTrue(isequal(self.javaList.get(i-1), matlabValue));
            end
        end
    end
end