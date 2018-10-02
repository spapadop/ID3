Implementation Notes:
1. HEADER: I assume that the data input has no header column, so header column is created by the program itself. 
Column numbers starts counting from zero (0) and they are named like: attr<col_number>. 
That means that "attr0" is the header name of first column of data table, "attr1" is the second column, etc.

2. INPUT ARGUMENTS CONTROL: The program controls all conditions to make sure that you have entered the arguments correctly.

--- Running the program ---
Run the jar file provided using the appropriate arguments.
Example execute command: java -jar ID3.jar 6 C:\Users\.....\ID3\car.data

Program accepts two arguments: 
	1. the index of the output/label attribute, starting counting from zero (0). 
	  For example if you provide number "6" it will select the 7th column as label.
	2. the absolute path of the data file. 

A. open a command line window on the folder of submitted files.
B. execute the jar providing the 2 arguments as described.



--- Program Output ---
The program outputs the decision tree with the following structure:

root attrX condition
|	child attrY condition
|	.
|	.
|	child attrZ condition
|	|	Leaf: {classValue1 = <number of occurancies>, classValue2 = <number of occurancies>, ... } 
|	.
|	.
|	child attrK condition
|	|	Leaf: {classValue1 = <number of occurancies>, classValue2 = <number of occurancies>, ... } 
root attrX condition
.
.


example output for weather.data input:
attr0=rainy
| attr3=false
| | Leaf: {yes=3, no=0}
| attr3=true
| | Leaf: {no=2, yes=0}
attr0=overcast
| Leaf: {yes=4, no=0}
attr0=sunny
| attr2=normal
| | Leaf: {yes=2, no=0}
| attr2=high
| | Leaf: {no=3, yes=0}
