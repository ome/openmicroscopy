function rv=rset(varargin)
% omero.rtypes.rsetg static method workaround
% varargin converted to javaset of type 'omero.RType'
ja=javaset('omero.RType',nargin);
for i=1:nargin,
    ja(i)=varargin{i};
end
rv=omero.rtypes.rset(ja);
