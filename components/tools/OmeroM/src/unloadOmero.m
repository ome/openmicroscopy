function unloadOmero(varargin)

OmeroClient_Jar=fullfile(findOmero, 'omero_client.jar');
javarmpath(OmeroClient_Jar);
rmpath(genpath(findOmero)) % OmeroM and subdirectories
clear('java');