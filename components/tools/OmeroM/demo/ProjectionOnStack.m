function [resultImage] = ProjectionOnStack(imageStack,type)
% Calculates the projection for the given image stack, as
% returned by getPlaneStack. See PerformProjection for more
% information.

[zSections, X, Y] = size(imageStack);

if(strcmp(type,'mean') || strcmp(type, 'sum'))
    resultImage = squeeze(sum(imageStack));
    if(strcmp(type,'mean'))
        resultImage = resultImage./zSections;
    end
end
if(strcmp(type,'max'))
    resultImage = squeeze(max(imageStack,[],1));
end
