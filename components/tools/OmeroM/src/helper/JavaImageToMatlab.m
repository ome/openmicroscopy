function [C] = JavaImageToMatlab(javaImage)
% Convert a Java Image

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

H = javaImage.getHeight;
W = javaImage.getWidth;
C=uint8(zeros([H,W,3]));
B= javaImage.getData.getPixels(0,0,W,H,[]);
bidx = 1;
for i=1:H
    for j=1:W
        C(i,j,:)=B(bidx:(bidx+2));
        bidx=bidx+3;
    end
end
