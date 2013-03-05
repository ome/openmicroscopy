% Unit tests for the Mask shape helper function
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

classdef TestMask < TestShape
    
    properties
        x = 10;
        y = 10;
        mask = ones(20, 30);
        w
        h
    end
    
    methods
        function self = TestMask(name)
            self = self@TestShape(name);
        end
        
        
        function createMask(self, coordinates)
            if nargin < 2 || coordinates
                self.shape = createMask(self.x, self.y, self.mask);
            else
                self.shape = createMask(self.mask);
            end
            self.w = size(self.mask, 2);
            self.h = size(self.mask, 1);
        end   
        
        % Mask
        function testSimpleMask(self)

            self.createMask();
            
            assertTrue(isa(self.shape, 'omero.model.MaskI'));
            assertEqual(self.shape.getX().getValue(), self.x);
            assertEqual(self.shape.getY().getValue(), self.y);
            assertEqual(self.shape.getWidth().getValue(), self.w);
            assertEqual(self.shape.getHeight().getValue(), self.h);
            assertEqual(self.shape.getBytes(), int8(self.mask(:)));
        end
        
        function testMaskWithoutCoordinates(self)
            self.createMask(false);
            
            assertTrue(isa(self.shape, 'omero.model.MaskI'));
            assertEqual(self.shape.getX().getValue(), 0);
            assertEqual(self.shape.getY().getValue(), 0);
            assertEqual(self.shape.getWidth().getValue(), self.w);
            assertEqual(self.shape.getHeight().getValue(), self.h);
            assertEqual(self.shape.getBytes(), int8(self.mask(:)));
        end
        

        function testShapeCoordinates(self)
            self.createMask();
            self.setShapeCoordinates();
        end
        
    end
end

