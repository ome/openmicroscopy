function x = randint(a,b)
% Simple random integer function which selects a value
% from the range a-b, inclusive. If n+1 is >= b-a, an exception
% will be thrown.

if (b<a)
  throw(MException('OMERO:Helper','b<a'));
end

if (a<0)
  throw(MException('OMERO:Helper','a<0'));
end

if (b<0)
  throw(MException('OMERO:Helper','b<0'));
end

x = ceil(rand()*b);
x = x - a;
