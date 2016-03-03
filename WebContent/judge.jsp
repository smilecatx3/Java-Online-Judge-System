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
		table td, table tr {
			border: 1px solid #8BABEB;
			padding: 5px;
		}
		table tr:first-child td {
			font-weight: bold;
			font-family: Lucida Sans Unicode;
		}
		.td {
			max-width:500px; 
			word-wrap: break-word;
		}
		pre {
			font-size: 16px;
			font-family: Consolas;
		}
		.progress {
            height: 250px;
			font-size: 40px;
        }
		.progress > svg {
			height: 100%;
			display: block;
		}
	</style>
	<script type="text/javascript" src="js/progressbar.js"></script>
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

<script>
	var progressCircle;
	
	function drawProgressCircle() {
		progressCircle = new ProgressBar.Circle('#progress', {
			color: '#15CB08',
			strokeWidth: 3,
			trailWidth: 1,
			duration: 500,
			text: {
				value: '0',
				style : {
					color: '#4ACF1E',
					position: 'absolute',
					left: '50%',
					top: '50%',
					padding: 0,
					margin: 0
				}
			},
			step: function(state, bar) {
				bar.setText((bar.value() * 100).toFixed(0)+"%");
			}
		});
	}
	
	function removeProgressCircle() {
		// TODO may need delete object
		var div = document.getElementById('progress');
		if (div) {
			div.parentNode.removeChild(div);
		}
	}
	
	function setStatus(message) {
		document.getElementById("status").innerHTML = message;
	}

	function setProgress(progress, message) {
		var speed = message.includes("Compiling") ? 1000 : 200;
		setStatus(message);
		progressCircle.animate(progress, {duration: speed});
	}
</script>
	
<div id="status" style="font-size: 20px;"> </div>
<p>
<div class="progress" id="progress"> </div>
<p>

<%
	// Get POST parameters
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
	String studentID = parameters.get("studentID").getString();
	ExecutionTask.Mode mode = ExecutionTask.Mode.parseMode(parameters.get("mode").getString());
	
	out.print("<script> drawProgressCircle(); </script>");
	out.print("<script> setProgress(0, \"Pending ...\"); </script>");
	out.flush();
	
	try {
		JudgeResult judgeResult = JOJS.judge(new OnlineJudgement(hwID, studentID, mode, parameters.get("file"), out)).get();
		out.print("<script> removeProgressCircle(); </script>");
		
		// Show results
		ExecutionResult[] results = judgeResult.getResults();
		JSONArray inputs = judgeResult.getTestcase().getJSONArray("inputs");
		JSONArray outputs = judgeResult.getTestcase().getJSONArray("outputs");
		
		out.print(String.format("<script> setStatus(\"%s\"); </script>", 
				String.format("<b>Score:</b> <font face='Comic Sans MS' color='#D5841A'> %d </font>　　<font size='3' color='gray'>(Runtime: %.0f ms)</font>", 
				judgeResult.getScore(), judgeResult.getRuntime())));
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
		out.println("<pre style='color:red; font-family:Consolas; text-align:left; padding-left:300px; padding-right:300px;'>");
		if (e.getCause() instanceof JudgeException)
			out.println(e.getMessage());
		else
			e.printStackTrace(new PrintWriter(out, true));
		out.println("</pre>");
	}
%>

</body>
</html>
