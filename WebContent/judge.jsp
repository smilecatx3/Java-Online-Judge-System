<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page import = "java.io.*" %>
<%@ page import = "java.util.*" %>
<%@ page import = "java.util.regex.*" %>
<%@ page import = "org.apache.commons.fileupload.*" %>
<%@ page import = "org.apache.commons.fileupload.servlet.*" %>
<%@ page import = "org.apache.commons.fileupload.disk.*" %>
<%@ page import = "org.apache.commons.lang3.*" %>
<%@ page import = "org.json.*" %>
<%@ page import = "tw.edu.ncku.csie.selab.jojs.*" %>

<html>
<head>
	<style type="text/css">
		html {
			height: 100%; 
			text-align: center;
		}
		body {
			text-align: center;
			font-size:16;
		}
		table {
			border: 1px solid #0755F2;
			margin-left: auto;
			margin-right: auto;
		}
		table td, table tr{
			border: 1px solid #8BABEB;
			padding: 5px;
		}
		table tr:first-child td {
			font-weight: bold;
			font-family: Lucida Sans Unicode;
		}
		.td {
			max-width:450px; 
			word-wrap: break-word;
		}
		pre {
			font-size:16;
		}
	</style>
</head>
<body>

<%!
	void setStatus(JspWriter out, String text) throws IOException {
		out.println(String.format("<script> document.getElementById(\"status\").innerHTML = \"%s<p>\"; </script>", text));
		out.flush();
	}
	
	void printErrorMessage(JspWriter out, String msg) throws IOException {
		out.println(String.format(
				"<pre style='font-size:20px; color:red; text-align:%s; font-family: Consolas;'>", 
				msg.contains("Please fill all blanks") ? "center" : "left"));
		out.println(msg);
		out.println("</pre>");
		out.flush();
	}
	
	String printJsonArray(JSONArray array) {
		StringBuilder result = new StringBuilder();
		for (int i=0; i<array.length(); i++)
			result.append(array.getString(i)).append("\n");
		return StringEscapeUtils.escapeHtml4(result.toString());
	}
%>

<div id="status" style="font-size:20px;"> </div>

<%
	// Get POST parameters
	Map<String, FileItem> parameters = new HashMap<>();
	try {
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		for (FileItem item : items) {
			if (item.getSize() == 0) {
				printErrorMessage(out, "Please fill all blanks.");
				return;
			}
			parameters.put(item.getFieldName(), item);
		}
	} catch (FileUploadException e) {
		response.sendRedirect("403.html");
		return;
	}
	String hwID = parameters.get("hwID").getString();
	String studentID = parameters.get("studentID").getString();
	ExecutionTask.Mode mode = ExecutionTask.Mode.parseMode(parameters.get("mode").getString());
	
	Judger judger = null;
	try {
		judger = new Judger(hwID, studentID);
		// Upload 
		setStatus(out, "Uploading ...");
		File zipFile = new File(judger.getWorkingDirectory(), parameters.get("file").getName());
		parameters.get("file").write(zipFile);
	
		// Compile
		setStatus(out, "Compiling ...");
		judger.compile(zipFile);
		
		// Execute
		setStatus(out, "Executing ...");
		JudgeResult judgeResult = judger.execute(mode);
		
		// Show results
		ExecutionResult[] results = judgeResult.getResults();
		JSONArray inputs = judgeResult.getTestcase().getJSONArray("inputs");
		JSONArray outputs = judgeResult.getTestcase().getJSONArray("outputs");
		
		setStatus(out, String.format("<b>Score:</b> <font face='Comic Sans MS' color='#D5841A'> %d </font>　　<font size='3' color='gray'>(elpased time: %.0f ms)</font>", judgeResult.getScore(), judgeResult.getRuntime()));
		out.print("<table>");
		out.print("<tr>  <td align='center' valign='top'> # </td>  <td align='center'> Result </td>  <td align='center'> Input </td>  <td align='center'> Your Output </td>  <td align='center'> Expected Output </td>  </tr>");
		for (int i=0; i<results.length; i++) {
			ExecutionResult result = results[i];
			out.print(String.format("<tr>  <td align='center' valign='top'> %s </td>  <td align='center' valign='top'> %s </td>  <td valign='top' class='td'> %s </td>  <td valign='top' class='td'> %s </td>  <td valign='top' class='td'> %s </td>  </tr>", 
					String.format("<font face='Comic Sans MS'>%d</font>", i+1),
					String.format("<b><font color='%s'>%s</font></b>", result.isPassed()?"green":"red", result.isPassed()?"Accepted":"Incorrect"),
					(inputs.length() > 0) ? String.format("<pre style='font-family: Consolas;'>%s</pre>", printJsonArray(inputs.getJSONArray(i))) : "",
					String.format("<pre style='font-family: Consolas;'>%s</pre>", result.getAnswer()),
					String.format("<pre style='font-family: Consolas;'>%s</pre>", printJsonArray(outputs.getJSONArray(i)))
			));
		}
		out.print("</table>");
	} catch (JudgeException e) {
		printErrorMessage(out, e.getMessage());
	} catch (Exception e) {
		out.println("<pre style='color:red; text-align:left;'>");
		e.printStackTrace(new PrintWriter(out));
		out.println("</pre>");
	} finally {
		judger.cleanWorkingDirectory();
	}
%>

</body>
</html>
