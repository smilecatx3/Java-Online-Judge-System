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
<%@ page import = "org.apache.commons.lang3.exception.*" %>
<%@ page import = "org.json.*" %>
<%@ page import = "tw.edu.ncku.csie.selab.jojs.*" %>
<%@ page import = "tw.edu.ncku.csie.selab.jojs.judger.*" %>

<html>
<head>
    <link rel="stylesheet" type="text/css" href="css/judge.css">
</head>
<body onresize="setHeight()">

<%!
    String printJsonArray(JSONArray array) {
        StringBuilder result = new StringBuilder();
        for (int i=0; i<array.length(); i++)
            result.append(StringEscapeUtils.escapeHtml4(array.getString(i))).append("\n").append("<hr>");
        return result.substring(0, result.lastIndexOf("<hr>")).toString();
    }
%>

<script src="js/lib/progressbar.js"></script>
<script src="js/lib/jquery.min.js"></script>
<script src="js/judge.js"></script>

<div class="status"> 
    <font style="color:black;">Submission Status: </font> <span id="status"></span>
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
    Judger.Mode mode = parameters.get("mode").getString().equals("standard") ? Judger.Mode.STANDARD : Judger.Mode.STDIN;

    out.print("<script> drawProgressCircle(); </script>");
    out.print("<script> showProgress(0, \"Pending\"); </script>");
    out.flush();
    
    try {
        JudgeResult judgeResult = JOJS.judge(
				new OnlineJudger(
					hwID, studentID, 
					new ProgressReporter() {
						@Override
						public void reportProgress(double progress, String message) {
							try {
								PrintWriter writer = response.getWriter();
								writer.print(String.format("<script> showProgress(%f, \"%s\"); </script>", progress, message));
								writer.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					},
					parameters.get("file")
				), mode).get();
        ExecutionResult[] results = judgeResult.getResults();
        JSONArray inputs = judgeResult.getTestcase().getJSONArray("inputs");
        JSONArray outputs = judgeResult.getTestcase().getJSONArray("outputs");
        
		out.print("<div id='summary' style='display: none'> <table>");
        out.print("<tr class='tr1'>" +
                "<td onclick='showDetail(-1)'> # </td>" +
                "<td> Result </td>" +
				"<td> Input </td>" +
                "<td> Your Answer </td>" +
                "<td> Expected Answer </td>" +
                "</tr>");
        for (int i=0; i<results.length; i++) {
			String ioTag = "<td class='io'>" +
                "<div id='io_show_%d'>%s</div>" +
                "<div id='io_collapse_%d' style='color:#C0C0C0; text-align:center; display:none;'>...</div>" +
                "</td>";
            ExecutionResult result = results[i];
            out.print("<tr>");
            out.print(String.format("<td class='id' onclick='showDetail(%d)'>%s</td>", i, i+1));
            out.print(String.format("<td class='%s'>%s</td>", result.isPassed()?"accepted":"incorrect", result.isPassed()?"Accepted":"Incorrect"));
            out.print(String.format(ioTag, i, (inputs.length() > 0) ? printJsonArray(inputs.getJSONArray(i)) : "", i));
            out.print(String.format(ioTag, i, StringEscapeUtils.escapeHtml4(result.getAnswer()), i));
            out.print(String.format(ioTag, i, outputs.getString(i), i));
            out.print("</tr>");
        }
		out.print("</table> </div>");
		
        int base = 20;
        out.print(String.format("<script> showJudgeResult('%s', %d, %d, %d, %d); </script>", 
                    hwID, judgeResult.getNumPassed(), results.length, judgeResult.getScore(base), judgeResult.getRuntime()));
    } catch (Exception e) {
		String title, message;
		if (e.getCause() instanceof Exception)
            e = (Exception) e.getCause();
        if (e instanceof JudgeException) {
            JudgeException ex = (JudgeException) e;
            title = WordUtils.capitalizeFully(ex.getErrorCode().toString(), "_".toCharArray()).replace("_", " ");
            message = ex.getMessage();
        } else {
			title = "Unexpected Error";
			message = ExceptionUtils.getStackTrace(e);
        }
		message = StringEscapeUtils.escapeHtml4(message).replace("\r", "").replace("\n", "<br/>").replace("\\", "/");
		out.print(String.format("<script> showErrorMessage(\"%s\", \"%s\"); </script>", title, message));
    }
%>

</body>
</html>
