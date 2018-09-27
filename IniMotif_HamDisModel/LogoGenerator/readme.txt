Lu Cheng
lu.cheng@cs.helsinki.fi
May 28th, 2009

To run the program: 

make compile
java -jar LogoFactory.jar -[file|dir] -[jarspar|uniprobe|transfac] input output

If you select -file option, input means input pwm file, output means output file. If no output is provided, the output will be named with the input file name with an suffix ".png".

If you select -dir option, the input pwm files are supposed to be under the input directory, and their output will be put into the output directory.

-jarpar, -uniprobe and - transfac are designed to processed different pwm files.

Example:
java -jar LogoFactory.jar -file -transfac /home/lcheng/m0018.txt /wrk/data/lu_data/testlogo.png


NOTE:
The background probability is [0.25 0.25 0.25 0.25] by default, which could be changed if necessary.
