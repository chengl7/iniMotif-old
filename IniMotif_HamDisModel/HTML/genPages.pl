#!/usr/bin/perl -w

# this script generates a HTML pages for all barcodes under a batch directory
# The HTML pages include a homepage, which contains links to all barcode pages, and webpages for each barcode

# Author: Lu Cheng lu.cheng@cs.helsinki.fi
# Date: Apr 20th, 2009

# The $template_dir is set as current directory
# Date: May 14th, 2009

use strict;
use warnings;
use Cwd;

# $root_dir contains all data from all batches, $template_dir is the directory for HTML templates, $min_width and $max_width are motif widths
my ($root_dir, $min_width, $max_width) = @ARGV;

my $template_dir = getcwd();
$root_dir =~ s/\/$//;
$template_dir =~ s/\/$//;

my $Barcode_TF_file = $root_dir.'/Barcode-TfName.txt';
my $barcode_well_file = $root_dir.'/Well_Plate_Barcode.txt';

my %FullBarcode_TF = %{get_FullBarcode_TF($Barcode_TF_file)};
my %barcode_well = %{get_barcode_well($barcode_well_file)};
my %barcode_TF = ();
my @batches = sort {$a cmp $b} (@{get_batch_dirs($root_dir)});
my @barcodes = ();
my @cycles = ();

my $batch;
my $barcode;
my $cycle;
my $out_dir;
my $TF_name;
my $well;

#copy barcode.css file to the root dir
system("cp $template_dir/barcode.css $root_dir")==0 or die "cp $template_dir/barcode.css $root_dir failed: $?";

open(INDEX_TEMP, $template_dir.'/INDEX_TEMPLATE.html');
my @index_template = <INDEX_TEMP>;
close(INDEX_TEMP);

my @links = ();
foreach $batch (@batches){

  $out_dir = $root_dir.'/'.$batch.'/HTML';
  mkdir($out_dir);

  %barcode_TF = %{get_batch_barcode_TF($batch, \%FullBarcode_TF)};
  my @batch_links = ("<br>\n");

  foreach $barcode (sort {$a cmp $b} (keys(%barcode_TF)) ){

    @cycles = @{get_cycles($root_dir.'/'.$batch)};

    $TF_name = $barcode_TF{$barcode};

    if(exists $barcode_well{$barcode}){
      $well = $barcode_well{$barcode};
    }else{
      $well = "unknown";
    }

    push(@batch_links, insert_page_link("$batch/HTML/".$barcode.'.html', $batch, $barcode, $TF_name, $well));
    gen_page($batch, $barcode, $TF_name, \@cycles, $well , $template_dir.'/LINK_TEMPLATE.html', $min_width, $max_width, $out_dir);
  }

=for comment
  @batch_links = sort {
    my $tmp;
    $a =~ /(_+[1-9]\/[A-Z]\d+_+)/;
    $tmp = $1;
    $b =~ /(_+[1-9]\/[A-Z]\d+_+)/;
    return ($tmp cmp $1);
  } @batch_links;
=cut

  push(@links,@batch_links);
}

  open(INDEX_PAGE,">$root_dir/index.html");
  foreach(@index_template){
    if(/^\s*INSERT_SECTION_HERE\s*/){
      foreach(@links){
	print INDEX_PAGE $_;
      }
      next;
    }

    print INDEX_PAGE $_;
  }
  close(INDEX_PAGE);


# this subroutine insert a link for a barcode
sub insert_page_link{
  my ($link, $batch, $barcode, $TF_name, $well) = @_;

  my $line = "<a href=\"".$link."\"><span class=\"link\">".$barcode."__________".$well."____________".$batch."___________".$TF_name."</span></a><br>\n";
  return $line;
}

# this subrountine generates a webpage for one specific barcode
sub gen_page{

	# get necessary parameters
#	my ($batch_dir, $barcode, $TF_name, $batch_name, $PLATE_name, $template_path, $min_width, $max_width, $ref_cycles, $out_dir) = @_;

	my ($batch_name, $barcode, $TF_name, $ref_cycles, $plate_well, $template_path, $min_width, $max_width, $out_dir) = @_;
	my @cycles = @{$ref_cycles};
	$out_dir =~ s/(^.+)\/$/$1/; # trim the last '/'

#	if($out_dir =~ /(^.+)\/$/){
#		$out_dir = $1;
#	}

		
	# read the template
	open(TEMPLATE,$template_path) or die "Couldn't open template: $template_path\n";
	my @template = <TEMPLATE>;
	close(TEMPLATE);
	open(OUTPUT,">$out_dir/$barcode.html") or die "Couldn't create output file: $out_dir/$barcode.html";
	
	my $line = "";
	# print header information in this section
	foreach $line (@template){
		
		# substitute TITLE
		if($line =~ /<title>TEMPLATE<\/title>/){
			$line =~ s/TEMPLATE/$batch_name-$barcode-$TF_name/;
			print OUTPUT $line;
			next;
		}

                # substitute BARCODE
		if($line =~ /^\s*BARCODE\s*$/){
			$line =~ s/BARCODE/$barcode/;
			print OUTPUT $line;
			next;
		}

		# substitute TF_NAME
		if($line =~ /^\s*TF_NAME\s*$/){
			$line =~ s/TF_NAME/$TF_name/;
			print OUTPUT $line;
			next;
		}
		
		# substitute BATCH
		if($line =~ /^\s*BATCH\s*$/){
			$line =~ s/BATCH/$batch_name/;
			print OUTPUT $line;
			next;
		}
		
		# substitute PLATE_WELL
		if($line =~ /^\s*PLATE_WELL\s*$/){
			$line =~ s/PLATE_WELL/$plate_well/;
			print OUTPUT $line;
			next;
		}

		# substitute cycle
		if($line =~ /.+CYCLE.+/){

		  my $tmp_line = "";
		  foreach (@cycles){
		    $tmp_line = $line;
		    $tmp_line =~ s/CYCLE/CYCLE_$_/;
		    print OUTPUT $tmp_line;
		  }

		  next;
		}

		if($line =~ /^\s*INSERT_A_SECTION_HERE\s*$/){
			# insert a section for different width
			for(my $i=$min_width; $i<=$max_width; $i++){
				my $ref_res = insert_block($i,$batch_name, $barcode, $ref_cycles);	
				my @res = @{$ref_res};
				{
				  my $tmp = $";
				  $" = "\n";
				  print OUTPUT "@res";
				  $" = $tmp;
				}
			}
			next;
		}
		
		print OUTPUT $line;
	}	

	close(OUTPUT);
}

# insert a block of information into a webpage
sub insert_block{
	my ($width,$batch, $barcode, $ref_cycles) = @_;
	my @cycles = @{$ref_cycles};
	
	my $ROWSPAN = 3;

	my @res = ();
	my $ref;
	my $FullBarcode;
	
	# print the first row (logo) of the block, the head and end should span several rows
	push(@res,'<tr>');
	push(@res,"<td align=center rowspan=\"$ROWSPAN\">$width</td>");

	foreach(@cycles){
	        $FullBarcode = $batch."_".$barcode."_".$_;
		$ref = insert_img("../$_/WIDTH$width/logo/$FullBarcode.png","logo");
		push(@res,@{$ref});
	}

	push(@res,"<td align=center rowspan=\"$ROWSPAN\">");
	$ref = insert_img("../TrendFig/WIDTH$width/$barcode.png","TrendFig");
	shift(@{$ref});        #------------------------ why ?--------------------------
	push(@res,@{$ref});
	push(@res,'</tr>');

	# print the SubHamFig row
	push(@res,'<tr>');
	foreach(@cycles){
       	        $FullBarcode = $batch."_".$barcode."_".$_;
		$ref = insert_img("../$_/WIDTH$width/SubStrDis-HamDis-Figure/$FullBarcode.png","SubHamFig");
		push(@res,@{$ref});
	}
	push(@res,'</tr>');

        # print the PosDis row
	push(@res,'<tr>');
	foreach(@cycles){
	        $FullBarcode = $batch."_".$barcode."_".$_;
		$ref = insert_img("../$_/WIDTH$width/PosDis_Figure/$FullBarcode.png","PosDis");
		push(@res,@{$ref});
	}
	push(@res,'</tr>');

	# print an empty row
	push(@res,'<tr></tr>');
	
	return \@res;
}

# insert an img HTML code
sub insert_img{
	my ($link, $class) = @_;

	my @res = ();
	push(@res,'<td>');
	push(@res,"<a href=\"$link\">");
	push(@res,"<img src=\"$link\" alt=\"$class\" class=\"$class\"><\/a><\/td>");

	return \@res;
}

# get batch names under the root directory
sub get_batch_dirs{
  my ($dir) = @_;

#  print "batch dir: $dir\n";

  $dir =~ s/\/$//;  #eliminate the last '/' sign

  opendir(DIR,$dir);
  my @files = grep( (-d $dir.'/'.$_) && /^[A-Z]+$/ , readdir(DIR) );
  chomp(@files);
  closedir(DIR);

  return \@files;
}

# get barcode-TF under one batch directory
sub get_batch_barcode_TF{
  # batch name, and fullbarcodes for all batches
  my ($batch, $ref_fullbarcode_tf) = @_;

  # extract all fullbarcodes start with the given batch
  my %FullBarcode_TF = %{$ref_fullbarcode_tf};
  my @FullBarcodes = grep(/^($batch)_([A-Z]+)_[0-9]+/,keys(%FullBarcode_TF));
  my %barcode_tf = ();

  # extract the barocdes and put it in a hash so that they are distinct
  my $full_barcode;
  foreach $full_barcode (@FullBarcodes){
    $full_barcode =~ /[0A-Z][A-Z]*_([A-Z]+)_[0-9]+/ ;
    $barcode_tf{$1} = $FullBarcode_TF{$full_barcode};
  }

  return \%barcode_tf;
}

# get the names of cycles under a batch directory in ascending order
sub get_cycles{
  my ($batch_dir) = @_;

  opendir(DIR, $batch_dir);
  my @cycles = grep( (-d $batch_dir.'/'.$_) && /^\d+$/ , readdir(DIR) );
  chomp(@cycles);
  closedir(DIR);

  #sort the cycles
  @cycles = sort { $a <=> $b }(@cycles);

  return \@cycles;
}


# get the FullBarcode-TF table
sub get_FullBarcode_TF{
  my ($Barcode_TF_file) = @_;

  open(BARCODE_TF, $Barcode_TF_file);
  my @lines = <BARCODE_TF>;
  chomp(@lines);
  close(BARCODE_TF);

  my %table = ();

  my ($full_barcode,$TF);
  foreach(@lines){
    ($full_barcode, $TF) = split(/\t+/,$_);
    $full_barcode =~ s/([0A-Z][A-Z]*_[ACGT]+_[0-9]+).*$/$1/;
    $table{$full_barcode} = $TF;
  }

  return \%table;
}

# get the barcode-Well table
sub get_barcode_well{
  my ($table_file) = @_;

  open(TABLE, $table_file);
  my @lines = <TABLE>;
  chomp(@lines);
  close(TABLE);

  my %table = ();
  foreach(@lines){
    $_ =~ /([A-Z]\d+)\t(\d+)\t([ACGT]+)/;
    $table{$3} = $2.'/'.$1;
  }

  return \%table;
}



