#!/usr/bin/perl -w

use strict;
use warnings;
use File::Find;

our $root_dir = $ARGV[0];   #IniMotif Output directory which contains all batches
my $out_dir = $ARGV[1];    #Output directory for compacted data

$root_dir =~ s/\/*$//;
$out_dir =~ s/\/*$//;


our %dirs_width = ();
our %dirs_Trend_HTML = ();


find(\&storeDir, $root_dir);



my $tmp_dir;
#=for comment #=cut
foreach $tmp_dir (sort {$a cmp $b} keys(%dirs_width)){
  system("mkdir -p $out_dir$tmp_dir");
  system("cp -r $root_dir$tmp_dir/logo $out_dir$tmp_dir");
  system("cp -r $root_dir$tmp_dir/PosDis_Figure $out_dir$tmp_dir");
  system("cp -r $root_dir$tmp_dir/SubStrDis-HamDis-Figure $out_dir$tmp_dir");
}

foreach $tmp_dir (sort {$a cmp $b} keys(%dirs_Trend_HTML)){
#  print "$tmp_dir\n";
  system("cp -r $root_dir$tmp_dir $out_dir$tmp_dir");
}


system("cp $root_dir/barcode.css $out_dir");
system("cp $root_dir/index.html $out_dir");


sub storeDir{
  my $dir = $File::Find::dir;

  if($dir =~ /(\/[A-Z]+\/\d+\/WIDTH\d+)$/){
    $dirs_width{$1} = 1;
  }elsif($dir =~ /(\/[0A-Z]+\/(TrendFig|HTML))$/){
    $dirs_Trend_HTML{$1} = 1;
  }
}


