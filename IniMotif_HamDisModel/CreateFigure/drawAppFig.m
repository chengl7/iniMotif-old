function drawAppFig(DIR_LIST,FullBarcodeList,WIDTH,out_dir)

%% defining input parameters
% 'DIR_LIST' includes all paths for the outputs of several SELEX rounds
%DIR_LIST{1} = '/home/lcheng/testdata/simulate/out1/1';
%DIR_LIST{2} = '/home/lcheng/testdata/simulate/out1/2';
%DIR_LIST{3} = '/home/lcheng/testdata/simulate/out1/3';
%
%FullBarcodeList, is a struct list which is consistent with the DIRs
% it contain the four fields: batch, barcode, cycle, FullBarcode
%
%WIDTH fo the motif, should be only a number
% WIDTH = 9;
%
% output directory for the TrendChange figure
%out_dir = '/home/lcheng/Desktop/';

% the A_ACCCT_3.cnt file starts with a header, followed by lines like this:
% SubStrIndex SubStrCount otherstuff
% The SubStrIndex is in ascending order as the line number growing
% Note that we assume a substr and its Reverse Complementary sequence are of the same count

% The FullBarcodeList should be in numerical ascending order, i.e. the cycles

% Lu Cheng 2009.04.17 lu.cheng@cs.helsinki.fi

% Skip mode has been added
% Date: 14.05.2009


%% Initialize global variable
global IS_SKIP;  % skip processing already existed files

if isempty(IS_SKIP)
    IS_SKIP = false(1);
end

%% read in the data, each column represents a SELEX round, each row represents a w-width substring
round_num = length(FullBarcodeList);
TITLE = '';

if round_num ~= length(DIR_LIST)
    disp('DIR_LIST and FullBarcodeList should be with the same length!');
    return;
end

matrixes = cell(1,round_num);
ROUNDS = [FullBarcodeList.cycle];
for i=1:round_num
    
    if DIR_LIST{i}(end) ~= '/'
        DIR_LIST{i}(end+1) = '/';
    end
    
    batch = FullBarcodeList(i).batch;
    barcode = FullBarcodeList(i).barcode;
    cycle = num2str(FullBarcodeList(i).cycle);
    FullBarcode = FullBarcodeList(i).FullBarcode;
    
    tmp_dir = strcat(DIR_LIST{i},FullBarcode,'.cnt');
    tmp_mat = dlmread(tmp_dir,'\t',1,0);

    if isempty(tmp_mat)
        disp(strcat(FullBarcode,'.cnt is empty!'));
        return;
    end
     
    if IS_SKIP && exist(strcat(out_dir,barcode,'.png'),'file') ~= 0
        return;
    end
    
    matrixes{i} = tmp_mat(:,[1 2]);
    
    if i==2
        tmp_fid = fopen(tmp_dir);
        TITLE = fgetl(tmp_fid);
        TITLE = regexprep(TITLE,'_','\\_');
        fclose(tmp_fid);
        
        TITLE = strcat(TITLE,'; WIDTH',num2str(WIDTH),'; Batch: ',batch,' Rounds: 0,',cycle);
    else
        TITLE = strcat(TITLE,',',cycle);
    end
end

clear tmp_dir tmp_mat tmp_fid;

[substr_indexes count] = mergeList(matrixes);

% for test purpose
if length(substr_indexes)<400
    disp(strcat(FullBarcode,':  too less substrings'));
    for k=1:length(matrixes)
        mat = matrixes{k};
        disp(strcat('mat',num2str(k),' length: ',num2str(length(mat))));
    end
    disp(strcat('common length: ',num2str(length(substr_indexes))));
    
    return;
end

%% pick out 100 substrings with the highest average counts and 200 rand sequences, plot the relative_frac-round figure
subplot(2,2,[1 2]);

% in case that no substring appears at least once in each round
if isempty(substr_indexes)
    return;
end

rand_ind = randperm(size(count,1));

if length(rand_ind)>200
    rand_ind = rand_ind(1:200);
end

avg = sum(count,2)/round_num;
[B IX] = sort(avg,'descend');
if length(IX)>200
    top_ind = IX((1:100)*2-1);   % since both seq and its revcom are with the same count
                                 % but the palindrome may not be displayed here
else
    top_ind = IX(1:2:length(IX));
end

frac_mat = count;
relative_frac = count;

for i=1:round_num
    frac_mat(:,i) = count(:,i)/sum(count(:,i));   % nomalization
    relative_frac(:,i) = log(frac_mat(:,i)./(1-frac_mat(:,i)));
end

%plot(1:round_num,log(frac_mat(plot_ind,1:round_num)),'--s');
plot(ROUNDS,relative_frac(rand_ind,1:round_num),'--x','color',ones(1,3)*0.7);
hold on
plot(ROUNDS,relative_frac(top_ind,1:round_num),'-o');
hold off

title(TITLE,'FontSize',10);
xlabel('SELEX round','FontSize',14);
ylabel('log(f/(1-f))','FontSize',12);

clear B avg;

%% highlight the top 6 sequences
% color list
ColorArray = {[0.5 0.5 0],[0 0 1],[0 0.8 0],[1 0 0],[0 0.5 0.7],[1 0 1],[0 0 0]};
TopInd = IX((1:6)*2-1);
TopSeqInfo = cell(1,length(TopInd));

% put sequence information in the highlighted dots
for j=1:length(TopInd)
    color = ColorArray{mod(j,7)+1};
    tmpx = ROUNDS(round_num);
%   tmpy = log(frac_mat(TopInd(j),round_num));
    tmpy = relative_frac(TopInd(j),round_num);
    
    seqind = substr_indexes(TopInd(j));
    revcomseq = getRevCom(seqind,WIDTH);
    seqname = Index2Seq(seqind,WIDTH);
    revcomseqname = Index2Seq(revcomseq,WIDTH);
    
    text(tmpx,tmpy,cat(2,'\leftarrow ',num2str(j)),'horizontalAlignment', 'left','FontSize',10,'Color',color);
    if(seqind == revcomseq)
        TopSeqInfo{j} = cat(2,'\color[rgb]{',num2str(color),'}',num2str(j),': ',seqname,'; ');
    else
        TopSeqInfo{j} = cat(2,'\color[rgb]{',num2str(color),'}',num2str(j),': ',seqname,'; ',revcomseqname);
    end
end

txt_x = ROUNDS(round_num)+0.5;
txt_y = ylim;
txt_y = (txt_y(2)-txt_y(1))*0.75+txt_y(1);

text(txt_x,txt_y,TopSeqInfo);
xlim([ROUNDS(1)-0.5 (ROUNDS(round_num)+2)]);
grid on

clear color tmpx tmpy seqind revcomseq seqname revcomseqname txt_x txt_y;

%% plot the frac-round figure
subplot(2,2,3);

plot(ROUNDS,frac_mat(rand_ind,1:round_num),'--x','color',ones(1,3)*0.7);
hold on
plot(ROUNDS,frac_mat(top_ind,1:round_num),'-o');
hold off

ylabel('f = SubStrCount/TotalCount','FontSize',12);

% put sequence information in the highlighted dots
for j=1:length(TopInd)
    color = ColorArray{mod(j,7)+1};
    tmpx = ROUNDS(round_num);
    tmpy = frac_mat(TopInd(j),round_num);
    
    seqind = substr_indexes(TopInd(j));
    seqname = Index2Seq(seqind,WIDTH);
    text(tmpx,tmpy,cat(2,'\leftarrow ',num2str(j)),'horizontalAlignment', 'left','FontSize',10,'Color',color);
    TopSeqInfo{j} = cat(2,'\color[rgb]{',num2str(color),'}',num2str(j),': ',seqname,'; ');
end

txt_x = ROUNDS(round_num)+0.5;
txt_y = ylim;
txt_y = (txt_y(2)-txt_y(1))*0.75+txt_y(1);

text(txt_x,txt_y,TopSeqInfo);
xlim([ROUNDS(1)-0.5 (ROUNDS(round_num)+2)]);
grid on

clear color tmpx tmpy seqind seqname txt_x txt_y;

%% plot the number of sequences in each round
subplot(2,2,4);
bar(ROUNDS, sum(count));
title('SeqCount distribution');
xlabel('SELEX round','FontSize',14);
ylabel('TotalCount(substr)','FontSize',14);

saveas(gcf,strcat(out_dir,barcode),'png');


%% usefulfunctions

%%%%%%%-----------------FUN-1-----BEGIN--------------------------------%%%%%%%%%%
function str = Index2Seq(index, WIDTH)
% this function will turn the index into an DNA string
% index is a vector which are indexs of different DNA sequences, Num*1
% or 1*Num
% WIDTH is the length of the DNA sequence
% str is Num*WIDTH

if(size(index,2)~=1)
    index = index';
end

Num = length(index);
str = char(zeros(Num,WIDTH));

remainder = mod(index,4);
quotient = floor(index./4);
str(:,WIDTH) = Index2Base(remainder);

for i=1:(WIDTH-1)
    remainder = mod(quotient,4);
    quotient = floor(quotient/4);
    str(:,WIDTH-i) = Index2Base(remainder);
end
return;

function y = Index2Base(x)
% this function turns X into bases
y = char(x);
y(x==0) = 'A';
y(x==1) = 'C';
y(x==2) = 'G';
y(x==3) = 'T';
return;
%%%%%%%---------------- FUN-1 --------END------------------------------%%%%%%%%%%


%%%%%%%-----------------FUN-2---------BEGIN----------------------------%%%%%%%%%%
function index = Seq2Index(seq, WIDTH)
% this function will turn the index of DNA strings
% seq is a vector which are indexs of different DNA sequences, Num*WIDTH
% WIDTH is the length of the DNA sequence
% index is Num*1

index = Base2Index(seq(:,1));

for i=2:WIDTH
    index = index*4 + Base2Index(seq(:,i));
end
return;

function y = Base2Index(x)
% this function turns X into bases
y = zeros(size(x));
y(x=='A' | x=='a') = 0;
y(x=='C' | x=='c') = 1;
y(x=='G' | x=='g') = 2;
y(x=='T' | x=='t') = 3;
return;
%%%%%%%-----------------FUN-2--------END-------------------------------%%%%


%%%%%%%-----------------FUN-3---------BEGIN----------------------------%%%%%%%%%%
function y = getRevCom(index,WIDTH)
% this function turn index of DNA to its reverse complementary
% index is a Num*1 vector, WIDTH is te length of the DNA sequence
% y is the same dimesion as index
    tmp = mod(index,4);
    res = 3 - tmp;
    
    for i=2:WIDTH
        index = floor(index./4);
        res = res * 4;
        tmp = 3 - mod(index,4);
        res = res+tmp;
    end
    
    y=res;
return;
%%%%%%%-----------------FUN-3--------END-------------------------------%%%%


%%%%%%%-----------------FUN-4---------BEGIN----------------------------%%%%
function [index count] = mergeList(matrix_list)
% this function calculates the union of the indexes of the matrix_list
% matrix_list are useful information extracted from these .cnt files
% The first column is the substr index, the second is the counts of the substrings
% OUTPUT
% index is the union of all the matrixes
% count contains the counts of each substring in each cycle

matrix_num = length(matrix_list);

index_col = 1;
count_col = 2;

if matrix_num == 0
    return
end

index = matrix_list{1}(:,index_col);
for i = 1:matrix_num
    mat = matrix_list{i};
    index = union(index,mat(:,index_col));
end

count = zeros(length(index),matrix_num);
for i = 1:matrix_num
    mat = matrix_list{i};
    [c ia ib] = intersect(index,mat(:,index_col));
    count(ia,i) = mat(ib,count_col);
end

return;
%%%%%%%-----------------FUN-4--------END-------------------------------%%%%