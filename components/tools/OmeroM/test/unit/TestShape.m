% Unit tests for the Shape helper function
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

classdef TestShape < TestCase
    
    properties
        shape
        z = 2
        c = 1
        t = 3
    end
    
    methods
        function self = TestShape(name)
            self = self@TestCase(name);
        end
               
        
        % Shape coordinates test function
        function setShapeCoordinates(self)
            assertTrue(isempty(self.shape.getTheZ()));
            assertTrue(isempty(self.shape.getTheC()));
            assertTrue(isempty(self.shape.getTheT()));
            
            setShapeCoordinates(self.shape, self.z, self.c, self.t);
            assertEqual(self.shape.getTheZ().getValue(), self.z);
            assertEqual(self.shape.getTheC().getValue(), self.c);
            assertEqual(self.shape.getTheT().getValue(), self.t);
        end
        
    end
end