#!/usr/bin/perl -w

use strict;
use warnings;

use TFBS::Matrix::PFM;

# this program draws logos for motifs
my $mode;
my $title;
my $matrix_ref;
my $out_file;

# input parameters
if(scalar(@ARGV)<3){
  print "Please input the mode(-NORMAL|-REVCOM) , logo title, motif(PSSM) and file name for the logo!\n";
  exit 0;
}

# logo title, motif, file name for logo
$mode = $ARGV[0];
$title = $ARGV[1];
$matrix_ref = $ARGV[2];
$out_file = $ARGV[3];



#print "RevCom $title\n";
#print "$matrix_ref\n\n\n";

if($mode eq "-NORMAL"){
  # do nothing
}elsif($mode eq "-REVCOM"){
  $matrix_ref = RevCom($ARGV[2]);
}else{
  print "Please input the mode(-NORMAL|-REVCOM) , logo title, motif(PSSM) and file name for the logo!\n";
  exit 0;
}

my $icm = TFBS::Matrix::ICM->new(-matrixstring => $matrix_ref,
				 -name   => "",
				 -ID     => ""
				);


$icm->draw_logo(-file=> $out_file,
		-full_scale =>2.25,
		-xsize=>400,
		-ysize =>200,
		-graph_title=> $title,
		-x_title=>"position",
		-y_title=>"bits");
 

sub RevCom{
	my ($str) = @_;

	my @mat_rows = split(/\n+/,$str);
	my @tmp = ();

	for(my $i=0; $i<scalar(@mat_rows); $i++){
		next if $mat_rows[$i] =~ /^\s*$/;   #skip blank lines
		@tmp = split(/\t+/,$mat_rows[$i]);
		@tmp = reverse(@tmp);
		$mat_rows[$i] = join("\t",@tmp);
	}
	
	@mat_rows = reverse(@mat_rows);
	my $ret = join("\n",@mat_rows);
	$ret =~ s/\n+/\n/g;
	return $ret;	
}
