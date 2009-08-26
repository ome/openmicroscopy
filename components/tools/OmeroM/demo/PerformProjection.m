function projectedImage = PerformProjection(gateway, pixelsId, channel, timePoint)
% Retrieves a stack of z-slices for the given pixels id at the given channel
% and time point, and then calculates the mean projection in memory.
%
% The Gateway object is the service as returned by loadOmero

stack = getPlaneStack(gateway, pixelsId, channel, timePoint);
projectedImage = ProjectionOnStack(stack,'mean');
