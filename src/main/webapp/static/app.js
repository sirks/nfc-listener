let stompClient = null;
let numberTag = null;
let number = 0;

function setConnected(connected) {
  $('#connect').prop('disabled', connected);
  $('#disconnect').prop('disabled', !connected);
}

function connect() {
  let socket = new SockJS('http://127.0.0.1:8123/nfc');
  stompClient = Stomp.over(socket);
  stompClient.connect({},
    function (frame) {
      setConnected(true);
      showText('CONNECTED');
      console.log('Connected: ' + frame);
      stompClient.subscribe('/topic/nfc', function (nfcNotice) {
        let nfcJson = JSON.parse(nfcNotice.body);
        showText(`${nfcJson.type},${number},${nfcJson.uid}`);
        incrementNumber();
      });
    },
    function (message) {
      showText('DISCONNECTED');
    });
}

function incrementNumber() {
  number++;
  numberTag.val(number);
}

function disconnect() {
  if (stompClient != null) {
    stompClient.disconnect();
  }
  setConnected(false);
  showText('DISCONNECTED');
  console.log('Disconnected');
}

function showText(message) {
  $('#response').append('<tr><td>' + message + '</td></tr>');
}

$(function () {
  numberTag = $('#number');
  numberTag.change(event => {
    const val = event.currentTarget.value;
    if (isNaN(val)) {
      numberTag.val(number);
      return;
    }
    number = parseInt(event.currentTarget.value)
  });
  incrementNumber();
  $('form').on('submit', function (e) {
    e.preventDefault();
  });
  $('#connect').click(function () {
    connect();
  });
  $('#disconnect').click(function () {
    disconnect();
  });
})
;

