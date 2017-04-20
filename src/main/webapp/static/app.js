var stompClient = null;

function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
}

function connect() {
	var socket = new SockJS('http://127.0.0.1:8123/nfc');
	stompClient = Stomp.over(socket);
	stompClient.connect({},
		function (frame) {
		setConnected(true);
			showText('CONNECTED');
		console.log('Connected: ' + frame);
		stompClient.subscribe('/topic/nfc', function (nfcNotice) {
			var nfcJson = JSON.parse(nfcNotice.body);
			showText('type=' + nfcJson.status + '; uid=' + nfcJson.uid);
		});
		},
		function (message) {
			showText('DISCONNECTED');
		});
}

function disconnect() {
	if (stompClient != null) {
		stompClient.disconnect();
	}
	setConnected(false);
	showText('DISCONNECTED');
	console.log("Disconnected");
}

function showText(message) {
	$("#response").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
	$("form").on('submit', function (e) {
		e.preventDefault();
	});
	$("#connect").click(function () {
		connect();
	});
	$("#disconnect").click(function () {
		disconnect();
	});
});

