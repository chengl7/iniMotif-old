function drawAllBatches(ROOT_DIR, WIDTH, MODE)
% ROOT_DIR contains directories for all batches
% ROOT_DIR |- 0
%          |- A
%             |-- 1
%             |-- 2
%             |-- 3
%          |- B
% WIDTH specifies the widths of all the substrings, such as [6 7 8 9]

% Author: Lu Cheng lu.cheng@cs.helsinki.fi
% Date: 17.04.2009

% Skip mode has been added
% Date: 14.05.2009

%% Initialize global variable
global IS_SKIP;  % skip processing already existed files
IS_SKIP = false(1);

if strcmp(MODE,'skip')
    IS_SKIP = true(1);
end

if ROOT_DIR(end)=='/'
    ROOT_DIR(end) = '';
end

% This section draws the SubStrDis-HamDis figures and PosDis figures
drawAllCycles(ROOT_DIR, WIDTH);

% This section draws all SubStr Changing Trend figures
drawAllTrendFigs(ROOT_DIR, WIDTH);

clear global IS_SKIP;
        
%% Function-1
function WIDTH = getWidth(cycle_dir)
% return the widths under the cycle directory
subdirs = dir(cycle_dir);
subdirs = subdirs(~ismember({subdirs.name},{'.' '..'}));  %struct

WIDTH = [];
for j = {subdirs([subdirs.isdir]).name}
    [token match] = regexp(j{:},'WIDTH(\d+)','once','tokens','match');
    if ~isempty(match)
        WIDTH(end+1) = str2num(token{:}); %#ok<ST2NM,AGROW>
    end
end

WIDTH = sort(WIDTH,'ascending');
return;


%% Function-2
function drawAllTrendFigs(ROOT_DIR, WIDTH)
% This section draws SubStr Changing Trend figure

batch_dirs = subdir(1,ROOT_DIR);
for i=1:length(batch_dirs)
    tmp_dir = batch_dirs{i};
    if ~isempty(regexp(tmp_dir,'/[A-Z]+','once','match'))   % '/A', '/B', '/C'
        batch_dir = strcat(ROOT_DIR,tmp_dir);
        back_dir = strcat(ROOT_DIR,'/0/0');
        
        mkdir(strcat(batch_dir,'/TrendFig'));
        for width = WIDTH
            out_dir = strcat(batch_dir,'/TrendFig/WIDTH',num2str(width));
            mkdir(out_dir);
            drawTrendFig(batch_dir,back_dir,width,out_dir);
        end
    end
end
return;

%% Function-3
function drawAllCycles(ROOT_DIR, WIDTH)
% This section draws the SubStrDis-HamDis figures and PosDis figures
cycle_dirs = subdir(2,ROOT_DIR); % only folder on the second level of the given ROOT_DIR, '/A/0', '/B/3' etc.

for i = 1:length(cycle_dirs)
    tmp_dir = cycle_dirs{i};
    
    if isempty(regexp(tmp_dir,'/[0A-Z][A-Z]*/[0-9]+$','once','match'))
        continue;
    end

    tmp_dir = strcat(ROOT_DIR, tmp_dir);
    %WIDTH = getWidth(tmp_dir);

    drawOneCycle(tmp_dir,WIDTH);
end
return;
