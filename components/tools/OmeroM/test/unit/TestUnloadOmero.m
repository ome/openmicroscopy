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
        matlabpath
        javapath
        omeropath
        jarpath
        client
    end
    
    methods
        function self = TestUnloadOmero(name)
            self = self@TestCase(name);
        end
        
        % Arguement constructor
        function setUp(self)
            loadOmero(); % Ensure OMERO.matlab is loaded
            self.omeropath = findOmero();
            self.jarpath = fullfile(self.omeropath, 'libs', 'omero_client.jar');
            self.updatePaths();
        end
        
        function tearDown(self)
            currentFolder = pwd;
            cd(self.omeropath);
            loadOmero(); % Reload OMERO.matlab
            cd(currentFolder);
        end
        
        function testMatlabPath(self)
            assertTrue(ismember(self.omeropath, self.matlabpath));
            
            unloadOmero();
            self.updatePaths();
            assertFalse(ismember(self.omeropath, self.matlabpath));
        end
        
        function testJavaClassPath(self)
            assertTrue(ismember(self.jarpath, self.javapath));
            
            unloadOmero();
            self.updatePaths();
            assertFalse(ismember(self.jarpath, self.javapath));
        end
        
        function testUnloadedWorkspace(self)
            
            self.client = omero.client('test');
            unloadOmero();
            self.updatePaths();
            assertFalse(ismember(self.jarpath, self.javapath));
            assertTrue(ismember(self.omeropath, self.matlabpath));
            
            self.client = [];
            unloadOmero();
            self.updatePaths();
            assertFalse(ismember(self.jarpath, self.javapath));
            assertFalse(ismember(self.omeropath, self.matlabpath));
        end
        
        function updatePaths(self)
            self.javapath = javaclasspath;
            self.matlabpath = regexp(path(), pathsep, 'split');
        end
    end
end