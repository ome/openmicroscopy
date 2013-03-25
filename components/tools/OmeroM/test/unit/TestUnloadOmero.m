% Unit tests for the unloadOmero function
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

classdef TestUnloadOmero < TestCase
    properties
        omeropath
    end
    
    methods
        function self = TestUnloadOmero(name)
            self = self@TestCase(name);
        end
        
        % Arguement constructor
        function setUp(self)
            loadOmero(); % Ensure OMERO.matlab is loaded
            self.omeropath = findOmero();
        end
        
        function tearDown(self)
            currentFolder = pwd;
            cd(self.omeropath)
            loadOmero(); % Reload OMERO.matlab
            cd(currentFolder);
        end
        
        function testMatlabPath(self)
            matlabPath = regexp(path(), pathsep, 'split');
            assertTrue(ismember(self.omeropath, matlabPath));

            unloadOmero();
            matlabPath = regexp(path(), pathsep, 'split');
            assertFalse(ismember(self.omeropath, matlabPath));
        end
        
        function testJavaClassPath(self)
            jarpath = fullfile(self.omeropath, 'libs', 'omero_client.jar');
            
            javapath = javaclasspath;
            assertTrue(ismember(jarpath, javapath));

            unloadOmero();
            javapath = javaclasspath;
            assertFalse(ismember(jarpath, javapath));
        end
    end
end