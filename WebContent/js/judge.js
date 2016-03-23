var progressCircle;

function drawProgressCircle() {
	progressCircle = new ProgressBar.Circle('#progress', {
		color: '#71AAE3',
		strokeWidth: 3,
		trailWidth: 1,
		duration: 500,
		text: {
			value: '0',
			style : {
				color: '#5193D5',
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
	setHeight();
}

function showJudgeResult(hwID, numPassed, numTestCases, score, runTime) {
	removeProgressCircle();
	showStatus('Finished', '#11BA5D');
	
	// Show judge result
	var percentPassed = (numPassed/numTestCases * 100.0).toFixed(0);
	var div_judge_result = document.getElementById('judge_result');
	var json = hwID+".json";
	div_judge_result.innerHTML = 
		"Test cases: <a target='_blank' href='testcase/"+json+"'>"+json+"</a> <br>" +
		"Passed <font style='font-weight: bold; font-family:Lucida Console;'>"+numPassed+"/"+numTestCases+"</font> test cases ("+percentPassed+"%) <br>" + 
		"Score: <font style='color:#C38747; font-family:Comic Sans MS;'>"+score+"</font> " + 
		"<span style='color:#ABABAB; float:right'>(Runtime: "+runTime+" ms)</span>";
	$(div_judge_result).fadeIn(300);
	
	// Show summary
	var div_summary = document.getElementById("summary");
	$(div_summary).slideDown({duration: 500, progress: setHeight});
}

function showDetail(id) {
	var duration = 300;
	
	if (id == -1) {
		var isAllExpanded = true;
		$('[id^="io_show_"]').each(function() {
			if ($(this).css('display')=='none') {
				isAllExpanded = false;
				return false;
			}
		});
		if (isAllExpanded) {
			$('[id^="io_show_"]').each(function() {
				$(this).slideUp({duration: duration, progress: setHeight});
			});
			$('[id^="io_collapse"]').each(function() {
				$(this).slideDown({duration: duration, progress: setHeight});
			});
		} else {
			$('[id^="io_show_"]').each(function() {
				$(this).slideDown({duration: duration, progress: setHeight});
			});
			$('[id^="io_collapse"]').each(function() {
				$(this).slideUp({duration: duration, progress: setHeight});
			});
		}
	} else {
		var io_show = "io_show_"+id;
		var io_collapse = "io_collapse_"+id;
		$('[id='+io_show+']').slideToggle({duration: duration, progress: setHeight});
		$('[id='+io_collapse+']').slideToggle({duration: duration, progress: setHeight});
	}
}

function setHeight() {
	var height = $('.status').height() + $('#judge_error').height() + $('#judge_result').height() + $('#summary').height() + 100;
	height = Math.max(height, 500);
	window.parent.postMessage(["setHeight", height], "*"); 
}
