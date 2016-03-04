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

function showStatus(message, color) {
	var status = document.getElementById("status");
	status.style.color = color;
	status.innerHTML = message;
}

function showProgress(progress, message) {
	var speed = message.includes("Compiling") ? 1000 : 200;
	showStatus(message, "#0B9FC9");
	progressCircle.animate(progress, {duration: speed});
}

function showErrorMessage(errorCode, message) {
	removeProgressCircle();
	showStatus(errorCode, '#D50E33');
	var div = document.getElementById('judge_error');
	$(div).fadeIn(300);
	div.innerHTML = message;
}

function showJudgeResult(numPassed, numTestCases, base, score, runTime) {
	removeProgressCircle();
	showStatus('Finished', '#11BA5D');
	var percentPassed = (numPassed/numTestCases * 100.0).toFixed(0);
	var div = document.getElementById('judge_result');
	$(div).fadeIn(300);
	div.innerHTML = 
		"<font style='font-weight: bold; font-family:Lucida Console;'>"+numPassed+"/"+numTestCases+" </font>test cases passed. ("+percentPassed+"%) <br>" + 
		"Score: <font style='color:#C38747; font-family:Comic Sans MS;'>"+score+"</font> " + 
		"<span style='color:#ABABAB; float:right'>(Runtime: "+runTime+" ms)</span>";
}