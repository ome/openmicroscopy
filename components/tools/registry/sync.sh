TARGET=hudson:.hudson/jobs/REGISTRY/workspace/registry/
rsync Makefile $TARGET
rsync *.py $TARGET
rsync -r ourmap/site/*.png $TARGET/ourmap/site/
