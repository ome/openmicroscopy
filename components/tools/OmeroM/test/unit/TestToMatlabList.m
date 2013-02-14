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