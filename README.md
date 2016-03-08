# Java Online Judge System  

Please check the [wiki page](https://github.com/smilecatx3/Java-Online-Judge-System/wiki) for more information.

## Usage

Firstly, you should configure the program settings in **jojs.jar/data/config.json**.  
- **java** is the path to the java program (under the jdk/bin directory, do not use the jre's one)  
- **ant_home** is the path to the Apache Ant home directory  
- **testcase_dir** is the path to the directory where the test case files are put here  
- **timeout** is the execution timeout (in seconds)  
- **max_thread** is the maximum number of simultaneous judgements  

<br/>
  
### Online Judgement

The web interface is written in JSP.  
Install [Tomcat](http://tomcat.apache.org/) and copy **WebContent** to **Tomcat/webapps/**.  
Put jojs.jar and all the required libraries into **WEB-INF/lib**".

<br/>

### Offline Judgement

Execute the following command with all the required libraries put in the same folder.  
```java -cp jojs.jar tw.edu.ncku.csie.selab.jojs.util.OfflineJudgement {HW_ID} {MODE(standard|stdin)} {BASE_SCORE} [MOSS_USER_ID]```  
For example: ```java -cp jojs.jar tw.edu.ncku.csie.selab.jojs.util.OfflineJudgement hw1 standard 20```  

A window will be prompted and ask you to select a directory which contains students' homeworks (i.e. zip files).  
A summary file will be produced after execution in the directory you selected.  
If you provide a moss user id, the plagiarism detection will be applied.