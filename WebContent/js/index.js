function setIFrameHeight() {
	var iFrame = document.getElementById("judge");
	var frameTop = iFrame.getBoundingClientRect().top;
	var bodyBottom = document.body.getBoundingClientRect().bottom;
	var height = bodyBottom - frameTop - 50;
	iFrame.setStyle({height: height+"px"});
}

function validateForm() {
	var form = document.forms["form"];
	var button = document.getElementById("btn_submit");
	var file = form["file"].value;
	
	if (file.length >= 13) {
		var studentID = file.substring(file.length-13, file.length-4);
		if (new RegExp(/[A-Z][0-9]{8}/).test(studentID)) {
			form["studentID"].value = studentID;
			button.removeAttribute("disabled");
			button.className = "btn_submit";
			return;
		}
	}
	form["studentID"].value = "";
	button.setAttribute("disabled", true);
	button.className = "btn_disabled";
}

function submitForm() {
	var form = document.forms["form"];
	var button = document.getElementById("btn_submit");
	
	// Disable submit button for a while
	button.setAttribute("disabled", true);
	button.className = "btn_disabled";
	setTimeout(function() {
			button.removeAttribute("disabled");
			button.className = "btn_submit";
		}, 1000);
	// Submit form
	form.action = "judge.jsp";
}
