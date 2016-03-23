window.addEventListener('message', function(e) {
	var eventName = e.data[0];
	var data = e.data[1];
	if (eventName === 'setHeight') {
		$('#judge').css("height", data+"px");
	}
}, false);

function setIFrameHeight() {
	var padding = $("#top").height()+30;
    $('#judge').css("padding-top", padding+"px");
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
	if (file.length > 0)
	    alert("Wrong Student ID");
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

function upload() {
	document.getElementById("file").click();
}
