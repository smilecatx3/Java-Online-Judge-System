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
	var studentID = form["studentID"].value;
	var file = form["file"].value;
	if ((studentID != null) && (file != null) && (studentID.length==9) && (file.length > 0)) {
		button.removeAttribute("disabled");
		button.className = "btn_submit";
	} else {
		button.setAttribute("disabled", true);
		button.className = "btn_disabled";
	}
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
