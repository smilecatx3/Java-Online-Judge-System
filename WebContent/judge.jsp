<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page import = "java.io.*" %>
<%@ page import = "java.util.*" %>
<%@ page import = "java.util.regex.*" %>
<%@ page import = "java.util.concurrent.*" %>
<%@ page import = "org.apache.commons.fileupload.*" %>
<%@ page import = "org.apache.commons.fileupload.servlet.*" %>
<%@ page import = "org.apache.commons.fileupload.disk.*" %>
<%@ page import = "org.apache.commons.lang3.*" %>
<%@ page import = "org.apache.commons.lang3.text.*" %>
<%@ page import = "org.json.*" %>
<%@ page import = "tw.edu.ncku.csie.selab.jojs.*" %>

<html>
<head>
	<link rel="stylesheet" type="text/css" href="css/judge.css">
</head>
<body>

<%!
	String printJsonArray(JSONArray array) {
		StringBuilder result = new StringBuilder();
		for (int i=0; i<array.length(); i++)
			result.append(array.getString(i)).append("\n");
		return StringEscapeUtils.escapeHtml4(result.toString());
	}
%>

<script src="js/progressbar.js"></script>
<script src="js/jquery.min.js"></script>
<script src="js/judge.js"></script>

<div class="status"> 
	<font style="color:black;">Run Code Status: </font> <span id="status"></span>
</div>
<div id="progress"></div>
<div id="judge_error"></div>
<div id="judge_result"></div>

<%
	// Get POST parameters
	request.setCharacterEncoding("UTF-8");
	Map<String, FileItem> parameters = new HashMap<>();
	try {
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		for (FileItem item : items)
			parameters.put(item.getFieldName(), item);
	} catch (FileUploadException e) {
		response.sendRedirect("403.html");
		return;
	}
	String hwID = parameters.get("hwID").getString();
	String studentID = new String(parameters.get("studentID").getString().getBytes("ISO-8859-1"), "UTF-8");
	ExecutionTask.Mode mode = ExecutionTask.Mode.parseMode(parameters.get("mode").getString());

	out.print("<script> drawProgressCircle(); </script>");
	out.print("<script> showProgress(0, \"Pending ...\"); </script>");
	out.flush();
	
	try {
		JudgeResult judgeResult = JOJS.judge(new OnlineJudgement(hwID, studentID, mode, parameters.get("file"), out)).get();
		ExecutionResult[] results = judgeResult.getResults();
		JSONArray inputs = judgeResult.getTestcase().getJSONArray("inputs");
		JSONArray outputs = judgeResult.getTestcase().getJSONArray("outputs");
		
		int base = 20;
		out.print(String.format("<script> showJudgeResult(%d, %d, %d, %d, %d); </script>", 
					judgeResult.getNumPassed(), results.length, base, judgeResult.getScore(base), judgeResult.getRuntime()));
					
		out.print("<table>");
		out.print("<tr>  <td align='center' valign='top'> # </td>  <td align='center'> Result </td>  <td align='center'> Input </td>  <td align='center'> Your Answer </td>  <td align='center'> Expected Answer </td>  </tr>");
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
	} catch (Exception e) {
		if (e.getCause() instanceof JudgeException) {
			JudgeException ex = (JudgeException) e.getCause();
			String errorCode = WordUtils.capitalizeFully(ex.getErrorCode().toString(), "_".toCharArray()).replace("_", " ");
			String message = StringEscapeUtils.escapeHtml4(ex.getMessage()).replace("\r", "").replace("\n", "<br/>");
			out.print(String.format("<script> showErrorMessage('%s', '%s'); </script>", errorCode, message));
		} else {
			out.println("<pre style='color:red; font-family:Consolas; text-align:left; padding-left:300px; padding-right:300px;'>");
			e.printStackTrace(new PrintWriter(out, true));
			out.println("</pre>");
		}
	}
%>

</body>
</html>
