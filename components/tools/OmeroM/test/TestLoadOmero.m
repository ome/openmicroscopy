classdef TestLoadOmero < TestCase
    properties
        host
        port
        rootpasswd
        user
        passwd
    end
    
    methods
        function self = TestLoadOmero(name)
            self = self@TestCase(name);
        end
        
        function setUp(self)
            c=loadOmero();
            self.host = c.getProperty('omero.host');
            self.port = str2double(c.getProperty('omero.port'));
            self.rootpasswd = c.getProperty('omero.rootpass');
            self.user = c.getProperty('omero.user');
            self.passwd = c.getProperty('omero.pass');
        end
        
        function testHostConstructor(self)
            c=loadOmero(self.host);
            assertEqual(c.getProperty('omero.host'),self.host);
        end
        
        
        function testHostPortConstructor(self)
            c=loadOmero(self.host,self.port);
            assertEqual(c.getProperty('omero.host'),self.host);
            assertEqual(str2double(c.getProperty('omero.port')),self.port);
        end
        
        function testPropsConstructor(self)
            props = java.util.Properties();
            props.setProperty('omero.host', self.host);
            props.setProperty('omero.port', num2str(self.port));
            c=loadOmero(props);
            assertEqual(c.getProperty('omero.host'),self.host);
            assertEqual(str2double(c.getProperty('omero.port')),self.port);
        end
        
        function testPorts(self)
            c = loadOmero('localhost',self.port);
            assertEqual(str2double(c.getProperty('omero.port')),self.port);
            
            testPort=2222;
            c = loadOmero('localhost',testPort);
            assertEqual(str2double(c.getProperty('omero.port')),testPort);
        end
        
        function testConfigFile(self)
            ice_config_list=javaArray('java.io.File',1);
            ice_config_list(1)=java.io.File(which('ice.config'));
            c=loadOmero(ice_config_list);
        end
        
    end
    
end