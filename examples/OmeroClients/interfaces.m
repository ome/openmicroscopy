import omero.model.*;

o = EventI();
assert( ~ o.isMutable() );

o = ExperimenterI();
assert( o.isMutable() );
assert( o.isGlobal() );
assert( o.isAnnotated() );

o = ExperimenterGroupExperimenterLinkI();
assert( o.isLink() );

someObject = ExperimenterI();
% Some method call and you no longer know what someObject is
if (~ someObject.isMutable() )
    % No need to update
elseif (someObject.isAnnotated())
    % deleteAnnotations(someObject);
end
