# Java Online Judge System  


## Dependencies

#### Required Libraries

- [Apache Commons FileUpload] (http://commons.apache.org/proper/commons-fileupload/) (for online judgement)
- [Apache Commons IO] (http://commons.apache.org/proper/commons-io/)
- [JSON] (https://github.com/douglascrockford/JSON-java)
- [Zip4j] (http://www.lingala.net/zip4j/)

#### Build Tool

- [Apache Ant] (http://ant.apache.org/)  
  (Only the files in the *lib* directory is required.)

<br/>

## Usage

Firstly, you should configure the program settings in **jojs.jar/data/config.json**.  
- **java** is the path to the java program (under the jdk/bin directory, do not use the jre's one)  
- **ant_home** is the path to the Apache Ant home directory  
- **testcase_dir** is the path to the directory where the test case files are put here  
- **timeout** is the execution timeout (in seconds)  

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
Usage: ```java -cp jojs.jar tw.edu.ncku.csie.selab.jojs.util.TestcaseCreator INPUT_FILE OUTPUT_FILE INPUT_PER_TESTCASE OUTPUT_PER_TESTCASE```

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
```java -cp jojs.jar tw.edu.ncku.csie.selab.jojs.util.TestcaseCreator input.txt output.txt 1 2```
  
<br/>
  
### Input Format
  
An input for a judgement is a zip file with the following structure:
```
input.zip
|- src
|- META-INF
|  |- MANIFEST.MF
```
All the source files (i.e. \*.java) are put in the *src* folder.
MANIFEST.MF specifies the enrty point (i.e. main class) of the program.

The zip file name should be in the format `[A-Z][0-9]{8}`, i.e. NCKU student ID format.  
You can refer to [demo](https://drive.google.com/folderview?id=0B6go6tO3TUxuVi16bHdnUEJRSkU&usp=sharing) for an example.
  
<br/>
  
### Offline Judgement

Execute the following command with all the required libraries put in the same folder.  
```java -jar jojs.jar HW_ID MODE(standard|stdin)```  
For example: ```java -jar jojs.jar hw1 standard```  

A window will be prompted and ask you to select a directory which contains zip files (i.e. source files).  
A summary file will be produced after execution. The file is in the directory you selected and it contains a list of student id, score, and the testcase number that the answer is incorrect.  
  
<br/>
  
### Online Judgement

The web interface is written in JSP.  
Install [Tomcat](http://tomcat.apache.org/) and copy **WebContent** to **Tomcat/webapps/**.  
Put jojs.jar and all the required libraries into **WEB-INF/lib**".
