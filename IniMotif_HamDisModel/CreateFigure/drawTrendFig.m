function drawTrendFig(batch_dir,back_dir,WIDTH,out_dir)
% this function draws figures describing the changing trend for each substring. 
% batch_dir: the directory which contains several cycles of sequence data (1,2...), such as ~/IniMotifOutput/B
% back_dir: the directory which contains the background sequences, such as ~/IniMotifOutput/0/0
% WIDTH: the width of substrings, should be an integer
% out_dir: directory storing the changing trend figures

% Author: Lu Cheng
% Date: April 17, 2009

%% process input parameters

if batch_dir(end) == '/'
    batch_dir(end) = '';
end

if back_dir(end) == '/'
    back_dir(end) = '';
end

if out_dir(end) ~= '/'
    out_dir(end+1) = '/';
end

if length(WIDTH)>1 || ~isnumeric(WIDTH)
    disp('The parameter WIDTH should be an integer!');
    return;
end

%% initiate ROUNDS, round_dirs, FullBarcodes under round_dirs

ROUNDS = 0;
dirs = subdir(1,batch_dir);  % '/1','/10','/2','/3','/4','/5', in alphabetical ascending order
% sort the cycles in numerical ascending order
for i=1:length(dirs)
    cycle = regexp(dirs{i},'/(\d+)','once','tokens');
    if ~isempty(cycle)
         ROUNDS(end+1) = str2num(cycle{:});         %#ok<ST2NM,AGROW>
    end
end 
ROUNDS = sort(ROUNDS,'ascend');

% construct a list of diffent round dirs
round_dir = cell(1,length(ROUNDS)); % input dirs for different cycles
round_dir{1} = strcat(back_dir,'/WIDTH',num2str(WIDTH),'/SubStrDis/');
for i=2:length(ROUNDS)
    round_dir{i} = strcat(batch_dir,'/',num2str(ROUNDS(i)),'/WIDTH',num2str(WIDTH),'/SubStrDis/');
end

% get lists of FullBarcodes for each round_dir
round_FullBarcodes = cell(1,length(ROUNDS));
for i=1:length(ROUNDS)
    round_FullBarcodes{i} = getFullBarcodes(fuf(round_dir{i})); %#ok<AGROW>
end

% get lists of barcodes for each round_dir
round_barcodes = cell(1,length(ROUNDS));
for i=1:length(ROUNDS)
    round_barcodes{i} = {round_FullBarcodes{i}.barcode};
end

%% draw the changing trend figure for each barocode

barcode_list = mergeBarcodes(round_barcodes);
for i=1:length(barcode_list)
    barcode = barcode_list{i};
    [round_ind list_ind] = inLists(barcode,round_barcodes);    
    
    if length(round_ind)>=3
        for j=1:length(round_ind)
            FullBarcodeList(j) = round_FullBarcodes{round_ind(j)}(list_ind(j)); %#ok<AGROW>
        end
        drawAppFig(round_dir(round_ind),FullBarcodeList,WIDTH,out_dir);
        clear FullBarcodeList;
    else
        disp(strcat('The changing trend is not plotted for this barcode: ',barcode));
    end
end

%% Function-1
function barcode_list = getFullBarcodes(str_list)
% this function constructs a FullBarcode list from a string(cell) list
% str_list: a cell list, each cell contains a file name
% barcode_list: a struct list, each element represents a FullBarcode

barcode_list = struct('batch',{},'barcode',{},'cycle',{},'FullBarcode',{});
for i=1:length(str_list)
    str = str_list{i};
    
    [fullbarcode tokens] = regexp(str,'([0A-Z][A-Z]*)_([ACGT]+)_([0-9]+)','once','match','tokens');
    if ~isempty(fullbarcode)
        [batch barcode cycle] = tokens{:};
        barcode_list(end+1) = struct('batch',{batch},'barcode',{barcode},'cycle',{str2num(cycle)},'FullBarcode',{fullbarcode}); %#ok<AGROW>
    else
        str
    end
end
return;

%% Function-2
function [round_ind list_ind] = inLists(barcode, round_barcodes)
%this function check whether the barcode belong to each round_barcode list,
%and returns the indexes
% barcode: a cell indicating the barcode
% round_barcodes: each cell contains the barcodes list belongs to each
% round
% OUTPUT:
% round_ind: round indexes contain the given barcode
% list_ind: barcode index within the barcode list of a round in round_ind

round_ind = [];
list_ind = [];
for i=1:length(round_barcodes)
    [tf loc] = ismember(barcode,round_barcodes{i});
    if tf
        round_ind(end+1) = i; %#ok<AGROW>
        list_ind(end+1) = loc;  %#ok<AGROW>
    end
end

return;

%% Function-3
function barcode_list = mergeBarcodes(round_barcodes)
barcode_list = {};
for i = 1:length(round_barcodes)
    round_barcode_list = round_barcodes{i};
    barcode_list = union(barcode_list,round_barcode_list);
end
barcode_list = sort(barcode_list);
return;
