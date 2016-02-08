# Java Online Judge System  


## Dependencies

#### Libraries

- [Apache Commons FileUpload] (http://commons.apache.org/proper/commons-fileupload/) (for online judgement)
- [Apache Commons IO] (http://commons.apache.org/proper/commons-io/)
- [JSON] (https://github.com/douglascrockford/JSON-java)
- [Zip4j] (http://www.lingala.net/zip4j/)

#### Build Tool

- [Apache ANT] (http://ant.apache.org/)

<br/>

## Usage

Firstly, You should configure the program settings in **jojs.jar/data/config.json**.  
- **ant_path** is the path to the ANT program  
- **timeout** is the timeout (in seconds) of each execution  
- **testcase_folder** is the path to a directory that contains testcase files.  

<br/>

### Testcase Format and Testcase Creator

The testcases for a judgement are written in a json file with the following format:  
```
{
    "inputs" : [
        [A_LIST_OF_INPUTS]
    ],
    "outputs" : [
        [A_LIST_OF_OUTPUTS]
    ],
}
```
  
For example, the following json file has two testcases. Each testcase has an input and two outputs.  
```
{
    "inputs" : [
        ["input_1"],
        ["input_2"]
    ],
    "outputs" : [
        ["output_1-1", "output_1-2"],
        ["output_2-1", "output_2-2"],
    ],
}
```
  
You can use ```TestcaseCreator``` in jojs.jar to create a testcase file for convenience.  
Usage: ```java -cp jojs.jar tw.edu.ncku.csie.selab.jojs.TestcaseCreator INPUT_FILE OUTPUT_FILE INPUT_PER_TESTCASE OUTPUT_PER_TESTCASE```  

For example, the following command and input/output files can create a testcase file in the example above.  

input.txt:
```
input_1
input_2
```
  
output.txt:
```
output_1-1
output_1-2
output_2-1
output_2-2
```
  
Command:
```java -cp jojs.jar tw.edu.ncku.csie.selab.jojs.TestcaseCreator input.txt output.txt 1 2```  
  
<br/>
  
### Offline Judgement

Execute the following command with all the required libraries put in the same folder.  
```java -jar jojs.jar HW_ID STDIN(true|false)```  
For example: ```java -jar jojs.jar hw1 false```  

A window will be prompted and ask you to select a directory contains zip files (i.e. source files).  
The summary file will be produced after execution. The file is in the directory you selected and it contains a list of student id, score, and the testcase number that the answer is incorrect.  
  
<br/>
  
### Online Judgement

The web interface is written in JSP.  
Install [Tomcat](http://tomcat.apache.org/) and copy **WebContent** to **Tomcat/webapps/**, that's all.
