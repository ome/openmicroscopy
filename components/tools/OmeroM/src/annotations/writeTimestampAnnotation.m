function ta = writeTimestampAnnotation(session, value, varargin)
% WRITETIMESTAMPANNOTATION Create and upload a timestamp annotation onto OMERO
%
%    ta = writeTimestampAnnotation(session, value) creates and uploads an
%    timestamp annotation of input value owned by the session user.
%
%    ta = writeTimestampAnnotation(session, value, 'description',
%    description) also sets the description of the annotation.
%
%    ta = writeTimestampAnnotation(session, value, 'namespace', namespace)
%    also sets the namespace of the annotation.
%
%    Examples:
%
%        ta = writeTimestampAnnotation(session, value)
%        ta = writeTimestampAnnotation(session, value, 'description',
%        description)
%        ta = writeTimestampAnnotation(session, value, 'namespace',
%        namespace)
%
% See also: WRITECOMMENTANNOTATION, WRITEDOUBLEANNOTATION,
% WRITEFILEANNOTATION, WRITELONGANNOTATION, WRITETAGANNOTATION,
% WRITETEXTANNOTATION, WRITEXMLANNOTATION

% Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Fountation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Fountation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% Input check
ip = inputParser;
ip.addRequired('session');
ip.addRequired('value', @isscalar);
ip.addParamValue('description', '', @ischar);
ip.addParamValue('namespace', '', @ischar);
ip.parse(session, value, varargin{:});

% Create double annotation
ta = omero.model.TimestampAnnotationI();

% Set time value
ta.setTimeValue(rtime(value));

% Set annotation properties
if ~isempty(ip.Results.description),
    ta.setDescription(rstring(ip.Results.description));
end
if ~isempty(ip.Results.namespace),
    ta.setNs(rstring(ip.Results.namespace));
end

% Upload and return the annotation
ta = session.getUpdateService().saveAndReturnObject(ta);
