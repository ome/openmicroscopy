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
        configFileRoot = 'test.config';
    end
    
    methods
        function self = TestLoadOmero(name)
            self = self@TestCase(name);
        end
        
        function configFilePath = createConfigFile(self, configString)
            
            nConfFiles = numel(dir([self.configFileRoot '*']));
            configFilePath = fullfile(pwd, ['test.config-' num2str(nConfFiles + 1)]);
            fid = fopen(configFilePath, 'w+');
            fprintf(fid, configString);
            fclose(fid);
        end
        
        function tearDown(self)
            self.client = [];
            delete(fullfile(pwd, [self.configFileRoot '*']))
        end
        
        % Default configuration (default ice.config file)
        function testNoInput(self)
            self.client = loadOmero();
            self.checkClientProperties();
        end
        
        % ICE_CONFIG constructor
        function testICECONFIG(self)
            self.host = 'my_server';
            self.port = 80;
            config = sprintf('omero.host=%s\nomero.port=%g\n',...
                self.host, self.port);
            configFilePath = self.createConfigFile(config);
            
            oldenvvar = getenv('ICE_CONFIG');
            setenv('ICE_CONFIG', configFilePath);
            self.client = loadOmero();
            self.checkClientProperties();
            
            setenv('ICE_CONFIG', oldenvvar);
            assertEqual(getenv('ICE_CONFIG'), oldenvvar);
        end
        
        % Server name/port constructor
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
        
        % Configuration file constructor
        function testConfigFile(self)
            self.host = 'my_server';
            self.port = 80;
            config = sprintf('omero.host=%s\nomero.port=%g\n',...
                self.host, self.port);
            configFilePath = self.createConfigFile(config);
            
            self.client = loadOmero(configFilePath);
            self.checkClientProperties();
        end
        
        function testMultipleIdenticalFiles(self)
            self.host = 'my_server';
            self.port = 80;
            config = sprintf('omero.host=%s\nomero.port=%g\n',...
                self.host, self.port);
            configFilePath = self.createConfigFile(config);
            
            self.client = loadOmero(configFilePath, configFilePath);
            self.checkClientProperties();
        end
        
        function testMultipleConfigFiles(self)
            self.host = 'my_server';
            self.port = 80;
            config = sprintf('omero.host=%s', self.host);
            configFilePath1 = self.createConfigFile(config);
            config = sprintf('omero.port=%g', self.port);
            configFilePath2 = self.createConfigFile(config);
            
            self.client = loadOmero(configFilePath1, configFilePath2);
            self.checkClientProperties();
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