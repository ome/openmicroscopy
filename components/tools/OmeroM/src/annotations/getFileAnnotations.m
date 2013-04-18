function fas = getFileAnnotations(session, ids)
% GETFILEANNOTATIONS Retrieve file annotations from the OMERO server
%
%   fas = getFileAnnotations(session, ids) returns all the file
%   annotations identified by the input ids in the context of the session
%   group.
%
%   Examples:
%
%      fas = getFileAnnotations(session, ids);
%
% See also: GETANNOTATIONTYPES, GETANNOTATIONS, GETTAGANNOTATIONS,
% GETCOMMENTANNOTATIONS, GETXMLANNOTATIONS

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

% Check input
ip = inputParser;
ip.addRequired('ids', @(x) isvector(x) || isempty(x));
ip.parse(ids);

% Return file annotations
fas = getAnnotations(session, ids, 'file');