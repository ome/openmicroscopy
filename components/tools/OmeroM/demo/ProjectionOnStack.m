function [resultImage] = ProjectionOnStack(imageStack,type)

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

    