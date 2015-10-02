function xa = writeXmlAnnotation(session, xml, varargin)
% WRITEXMLANNOTATION Create and upload a XML annotation onto OMERO
%
%    xa = writeXmlAnnotation(session, xml) creates and uploads a XML
%    comment annotation owned by the session user.
%
%    xa = writeXmlAnnotation(session, xml, 'description', description) also
%    specifies the description of the XML annotation.
%
%    xa = writeXmlAnnotation(session, xml, 'namespace', namespace) also
%    sets the namespace of the XML annotation.
%
%    xa = writeXmlAnnotation(session, xml, 'group', groupid) also
%    sets the group.
%
%    Examples:
%
%        xa = writeXmlAnnotation(session, xml)
%        xa = writeXmlAnnotation(session, xml, 'description', description)
%        xa = writeXmlAnnotation(session, xml, 'namespace', namespace)
%
% See also: WRITECOMMENTANNOTATION, WRITEDOUBLEANNOTATION,
% WRITEFILEANNOTATION, WRITELONGANNOTATION, WRITETAGANNOTATION,
% WRITETEXTANNOTATION, WRITETIMESTAMPANNOTATION

% Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
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
xa = writeTextAnnotation(session, 'xml', xml, varargin{:});