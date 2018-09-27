function drawOneCycle(CYCLE_DIR,WIDTH)
% CYCLE_DIR contains necessary IniMotif output for different barcodes
% CYCLE_DIR |-- WIDTH6
%                  |------ A_CGCGT_3.TXT
%                  |------ A_GCGAT_3.TXT
%                  |------ A_CGTAG_3.TXT
%           |-- WIDTH7
% This function draws figures such as SubStrCount-HamDisFig, PosDisFig for
% all different barcodes

% Author: Lu Cheng (lu.cheng@cs.helsinki.fi)
% version: March 26th, 2009

% the program searches the WIDTHs automatically
% modified by Lu Cheng, April 16th, 2009

if CYCLE_DIR(end) ~= '/'
    CYCLE_DIR(end+1) = '/';
end

% in case only the directory is provided
if nargin == 1
    subfolders = subdir(1,CYCLE_DIR);
    WIDTH = [];
    for folder = subfolders
        if isempty(regexp(folder,'WIDTH\d+','once'))
            WIDTH(end+1) = str2num(regexp(folder,'\d+','match','once')); %#ok<AGROW,ST2NM>
        end
    end
end

dir0 = strcat(CYCLE_DIR,'WIDTH');
dir1 = '/SubStrDis-HamDis/';
dir2 = '/SubStrDis-HamDis-Figure/';
dir3 = '/BindSitePosDis/';
dir4 = '/PosDis_Figure/';
dir5 = '/SeqBias/';

for i = 1:length(WIDTH)
    para1 = strcat(dir0,num2str(WIDTH(i)),dir1);
    para2 = strcat(dir0,num2str(WIDTH(i)),dir2);
    para3 = strcat(dir0,num2str(WIDTH(i)),dir3);
    para4 = strcat(dir0,num2str(WIDTH(i)),dir4);
    para5 = strcat(dir0,num2str(WIDTH(i)),dir5);
    para6 = WIDTH(i);
    para7 = [-1,(para6+1)];
    para8 = 6;  % Num of Hightlighted sequences
    para9 = round(WIDTH(i)/4);  % HamDis cutoff
      
    drawSubHamFig(para1,para2,para3,para4,para5,para6,para7,para8,para9);
end