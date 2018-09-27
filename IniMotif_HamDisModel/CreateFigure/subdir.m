function dirs = subdir(depth, CurrPath)
% this function return all directories of certain depth under Current path
% dirs are 
% Input: depth, integer, depth of subfolders
%        CurrPath, string(array), current path
% Output: dirs, cell, all the depth-level subfolders of CurrPath, dirs are 
%         relative paths under CurrPath, the dirs are sorted in
%         alphabetical order

% Author: Lu Cheng (lu.cheng@cs.helsinki.fi)
% Date: March 27, 2009

if CurrPath(end) == '/'
    CurrPath(end) = '';
end

dirs = {};
depth = depth-1;
if depth<0
    return
end

tmp = dir(CurrPath);
tmp = tmp(~ismember({tmp.name},{'.','..'}));

for i = {tmp([tmp.isdir]).name}
    next_dir = [CurrPath '/' i{:}];
    sub_dirs = subdir(depth,next_dir);
    
    % only in the case that the subdirs are not empty and special for depth
    % 0
    if ~isempty(sub_dirs) || depth==0
        sub_dirs = strcat('/',i,sub_dirs);
        dirs = {dirs{:} sub_dirs{:}};
    end
end

dirs = sort(dirs);