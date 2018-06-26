function ma = strToMapAnnotation(str, varargin)
% strToMapAnnotation returns MapAnnotation object of OMERO from
% string array or cell array of strings
%
% SYNTAX
% ma = strToMapAnnotation(str)
% ma = strToMapAnnotation(str,iseditable)
%
% REQUIREMENTS
%
%   OMERO.matlab toolbox
%   https://docs.openmicroscopy.org/latest/omero/developers/Matlab.html
%
%   Before using this function, you need to run an equivalent of the
%   following command.
%
%     client = loadOmero('demo.openmicroscopy.org', 4064)
%
% INPUT ARGUMENTS
% str         string array | cell array of strings
%             Number of columns must be 2.
%
% iseditable  false (default) | true | 0 | 1 
%             (Optional) If true or 1, MapAnnotation (Key-Value Pairs) will
%             be editable via GUI (OMERO.web or OMERO.insight)
%
%
%
% OUTPUT ARGUMENTS
% ma          MapAnnotationI object
%             To link ma to an image in OMERO, identify image ID from OMERO
%             GUI and execute the following command
%     
%               client = loadOmero('demo.openmicroscopy.org', 4064)
%               session = client.createSession(username, password)
%
%               link1 = linkAnnotation(session, ma, 'image', imageID);
%
%               clear
%               unloadOmero
%
% Written by Kouichi C. Nakamura Ph.D.
% MRC Brain Network Dynamics Unit
% University of Oxford
% kouichi.c.nakamura@gmail.com
% 09-Jun-2018 15:20:17
%
% See also
% linkAnnotation

p = inputParser;
p.addRequired('str',@(x) size(str,2) ==2 && iscellstr(x) || isstring(x) );
p.addOptional('iseditable',false,@(x) isscalar(x) && x == 1 || x == 0);

p.parse(str,varargin{:});

iseditable = p.Results.iseditable;


%% Job

if iscellstr(str) %#ok<ISCLSTR>
    
   str = string(str);
    
end


import java.util.ArrayList
eval('import omero.model.NamedValue')

li = ArrayList;

for r = 1:size(str,1)
    
    li.add(NamedValue(str{r,1},str{r,2}));

end

eval('import omero.model.MapAnnotationI')

ma = MapAnnotationI(int64(1),true); % 'false' results in Java exception occurred: omero.UnloadedEntityException:
ma.setMapValue(li);

if iseditable
    %NOTE this is required to make it editable from GUI
    eval('import omero.constants.metadata.NSCLIENTMAPANNOTATION')
    ma.setNs(rstring(NSCLIENTMAPANNOTATION.value));
end




end

