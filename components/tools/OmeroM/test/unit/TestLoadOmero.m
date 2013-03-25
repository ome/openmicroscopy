% Unit tests for the loadOmero function
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

classdef TestLoadOmero < TestCase
    properties
        host = 'localhost'
        port = 4064
        user
        client
    end
    
    methods
        function self = TestLoadOmero(name)
            self = self@TestCase(name);
        end
        
        function tearDown(self)
            self.client = [];
        end
        
        % Default configuration (default ice.config file)
        function testNoInput(self)
            self.client = loadOmero();
            self.checkClientProperties();
        end
        
        % Arguement constructor
        function testHostName(self)
            self.host = 'my_server';
            self.client = loadOmero(self.host);
            self.checkClientProperties();
        end
        
        function testPortNumber(self)
            self.port = 4444;
            self.client = loadOmero(self.host, self.port);
            self.checkClientProperties();
        end
        
        % Property constructor
        function testHostProps(self)
            props = java.util.Properties();
            props.setProperty('omero.host', self.host);
            
            self.client = loadOmero(props);
            self.checkClientProperties();
        end
        
        function testDefaultPortProps(self)
            props = java.util.Properties();
            props.setProperty('omero.host', self.host);
            props.setProperty('omero.port', num2str(self.port));
            
            self.client = loadOmero(props);
            self.checkClientProperties();
        end
        
        function testUserProps(self)
            self.user = 'testuser';
            props = java.util.Properties();
            props.setProperty('omero.host', self.host);
            props.setProperty('omero.user', self.user);
            
            self.client = loadOmero(props);
            self.checkClientProperties();
        end
        
        % Config file constructor
        function testConfigFile(self)
            configFilePath = fullfile(pwd, 'test.config');
            fid = fopen(configFilePath, 'w+');
            fprintf(fid, 'omero.host=%s\n', self.host);
            fprintf(fid, 'omero.port=%g\n', self.port);
            fclose(fid);

            self.client = loadOmero(configFilePath);
            self.checkClientProperties();
            
            delete(configFilePath);
        end
        
        function testMultipleConfigFiles(self)
            configFilePath1 = fullfile(pwd, 'test.config-1');
            fid = fopen(configFilePath1, 'w+');
            fprintf(fid, 'omero.host=%s\n', self.host);
            fclose(fid);
            
            configFilePath2 = fullfile(pwd, 'test.config-2');
            fid = fopen(configFilePath2, 'w+');
            fprintf(fid, 'omero.port=%g\n', self.port);
            fclose(fid);
            
            self.client = loadOmero(configFilePath1, configFilePath2);
            self.checkClientProperties();
            
            delete(configFilePath1);
            delete(configFilePath2);
        end
        
        function checkClientProperties(self)
            assertTrue(isa(self.client, 'omero.client'));
            
            client_host = char(self.client.getProperty('omero.host'));
            assertEqual(client_host, self.host);
            
            client_port = str2double(self.client.getProperty('omero.port'));
            assertEqual(client_port, self.port);
            
            if ~isempty(self.user)
                client_user = char(self.client.getProperty('omero.user'));
                assertEqual(client_user, self.user);
            end
        end
    end
end