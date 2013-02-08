function [javaList] = toJavaList(matlabList, varargin)
% Convert a MATLAB vector into a Java ArrayList

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
ip.addRequired('matlabList', @(x) isvector(x) || isempty(x));
ip.addOptional('castFun', @(x) x, @(x) ischar(x) || isa(x, 'function_handle'));
ip.parse(matlabList, varargin{:})

% Read casting function
castFun = ip.Results.castFun;
if ischar(castFun), castFun = str2func(castFun); end

% Create Java list
javaList = java.util.ArrayList;
for i=1:length(matlabList)
    javaList.add(castFun(matlabList(i)));
end
