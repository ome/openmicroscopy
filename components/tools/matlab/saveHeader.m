function saveHeader(fout, statList)


fields = fieldnames(statList);

for i = 1:length(fields)
    fprintf(fout,'%s,',fields{i});
end
fprintf(fout,'\n');;