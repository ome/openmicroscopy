function saveStats(fout, stats)


fields = fieldnames(stats);

for i = 1:length(fields)
    fprintf(fout,'%d,',stats.(fields{i}));
end
fprintf(fout,'\n');