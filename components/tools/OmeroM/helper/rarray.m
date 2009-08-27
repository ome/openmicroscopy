function rv=rarray(varargin)
% omero.rtypes.rarray static method workaround
% varargin converted to javaArray of type 'omero.RType'
ja=javaArray('omero.RType',nargin);
for i=1:nargin,
    ja(i)=varargin{i};
end
rv=omero.rtypes.rarray(ja);
