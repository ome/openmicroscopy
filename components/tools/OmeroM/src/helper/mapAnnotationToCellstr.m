function str = mapAnnotationToCellstr(ma)
% mapAnnotationToStr returns MapAnnotation object of OMERO from
% cell array of strings
%
% SYNTAX
% str = mapAnnotationToStr(ma)
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
% ma          MapAnnotationI object
%
% OUTPUT ARGUMENTS
% str         cell array of strings
%             The number of columns is 2.
%
% Written by Kouichi C. Nakamura Ph.D.
% MRC Brain Network Dynamics Unit
% University of Oxford
% kouichi.c.nakamura@gmail.com
% 26-Jun-2018 17:22:17
%
% See also
% linkAnnotation, strToMapAnnotation

p = inputParser;
p.addRequired('ma',@(x) isa(x,'omero.model.MapAnnotationI'));
p.parse(ma);


mv = ma.getMapValue;

str = cell(size(mv),2);

for i = 1:size(mv)
    
   str{i,1} = char(mv.get(i-1).name);
   str{i,2} = char(mv.get(i-1).value);
    
end

