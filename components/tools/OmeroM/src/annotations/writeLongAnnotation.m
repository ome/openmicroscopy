function la = writeLongAnnotation(session, value, varargin)
% WRITELONGANNOTATION Create and upload a double annotation onto OMERO
%
%    la = writeLongAnnotation(session, value) creates and uploads an
%    long annotation of input value owned by the session user.
%
%    la = writeLongAnnotation(session, value, 'description',
%    description) also set the description of the annotation.
%
%    la = writeLongAnnotation(session, value, 'namespace', namespace)
%    also sets the namespace of the annotation.
%
%    la = writeLongAnnotation(session, value, 'group', groupid)
%    sets the group.
%
%    Examples:
%
%        la = writeLongAnnotation(session, value)
%        la = writeLongAnnotation(session, value, 'description',
%        description)
%        la = writeLongAnnotation(session, value, 'namespace', namespace)
%
% See also: WRITECOMMENTANNOTATION, WRITEDOUBLEANNOTATION,
% WRITEFILEANNOTATION, WRITETAGANNOTATION, WRITETEXTANNOTATION,
% WRITETIMESTAMPANNOTATION, WRITEXMLANNOTATION

% Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

% Input check
ip = inputParser;
ip.addRequired('session');
ip.addRequired('value', @isscalar);
ip.addParamValue('description', '', @ischar);
ip.addParamValue('namespace', '', @ischar);
ip.addParamValue('group', [], @(x) isscalar(x) && isnumeric(x));
ip.parse(session, value, varargin{:});

% Create double annotation
la = omero.model.LongAnnotationI;

% Set annotation properties
la.setLongValue(rlong(value));
if ~isempty(ip.Results.description),
    la.setDescription(rstring(ip.Results.description));
end
if ~isempty(ip.Results.namespace),
    la.setNs(rstring(ip.Results.namespace));
end

% Upload and return the annotation
context = java.util.HashMap;
if ~isempty(ip.Results.group)
    context.put(...
        'omero.group', java.lang.String(num2str(ip.Results.group)));
end
la = session.getUpdateService().saveAndReturnObject(la, context);
