function stats = imageStats(i, cellCnt, x, y, imagePatch)

stats.image = i;
stats.cellCnt = cellCnt;
stats.X = x;
stats.Y = y;
stats.mean = mean(imagePatch);
stats.stddev = std(imagePatch);
stats.skew = skewness(imagePatch);
stats.kurtosis = kurtosis(imagePatch);
stats.sum = sum(imagePatch);