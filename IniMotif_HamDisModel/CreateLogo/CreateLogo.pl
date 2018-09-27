#!/usr/bin/perl -w

use strict;
use warnings;
use File::Find;

# this script draws logos for all .pwm files under a directory
# Lu Cheng lu.cheng@cs.helsinki.fi
# Apr 20th, 2009


# input parameters
if(scalar(@ARGV) < 3){
	print "Please input the mode (-NORMAL|-REVCOM), program (DrawLogo) direcory, input directory of motifs, output directory for logos!\n";
	exit 0;
}

# current directory (CreateLogo, motif_input_dir, logo_output_dir
my $mode = $ARGV[0];
my $dir = $ARGV[1];
my $input_dir = $ARGV[2];
my $output_dir = $ARGV[3];

$dir =~ s/^(.+)(\/*)$/$1\//; # add a '/' to the end of the dir
$input_dir =~ s/^(.+)(\/*)$/$1\//;
$output_dir =~ s/^(.+)(\/*)$/$1\//;


	#############################
	chdir($dir);
	find(\&produceLogo,$input_dir);

############################################################################
sub produceLogo() {

	if(-f and /.pwm/) {
 		my $file = $_;

		my $out_file = $file;
		$out_file =~ s/\.pwm$//;
		$out_file = $out_file.".png";

		my $title = "";
		my $matrix_ref = "";
		
		open(MOTIF,"$file");
		my @motif = <MOTIF>;
		close(MOTIF);
		
		for(my $i=0; $i<scalar(@motif); $i++){
			chomp($motif[$i]);
			if($motif[$i] eq "Final Motif for logo"){
				for(my $j=1 ; $j<5; $j++){
					chomp($motif[$i+$j]);
					$matrix_ref = join("",$matrix_ref,$motif[$i+$j],"\n");
				}
				last;
			}
		}

		$title = $motif[0];
		$out_file = join("",$output_dir,$out_file);

		my $para_cmd = $dir."DrawLogo.pl";

		my $cmd = "perl $para_cmd $mode \"$title\" \"$matrix_ref\" $out_file";
 		#print "$cmd\n";

		system($cmd)==0 or die "system $cmd failed: $?";
	}
}
