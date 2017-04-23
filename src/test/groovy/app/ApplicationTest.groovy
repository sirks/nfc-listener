package app

import app.ws.NfcSocketController
import app.ws.Notice
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import spock.lang.Specification

import java.lang.reflect.Type
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

import static app.ws.NoticeType.UID
import static java.util.concurrent.TimeUnit.SECONDS

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ApplicationTest extends Specification {

	static final String WEBSOCKET_URI = "ws://127.0.0.1:8123/nfc"
	static final String WEBSOCKET_TOPIC = "/topic/nfc"

	@Autowired
	NfcSocketController socket

	BlockingQueue<Notice> blockingQueue
	WebSocketStompClient stompClient

	def setup() {
		blockingQueue = new LinkedBlockingDeque<>()
		stompClient = new WebSocketStompClient(
				new SockJsClient([(new WebSocketTransport(new StandardWebSocketClient()))]))
	}

	def "should receive uid"() {
		given:
		def session = stompClient
				.connect(WEBSOCKET_URI, new StompSessionHandlerAdapter() {})
				.get(1, SECONDS);
		session.subscribe(WEBSOCKET_TOPIC, new DefaultStompFrameHandler());
		def uid = "0123456789ABCDEF"

		when:
		socket.notify(new Notice(UID, uid))
		def notice = blockingQueue.poll(1, SECONDS)

		then:
		notice.type == "UID"
		notice.uid == uid
	}

	class DefaultStompFrameHandler implements StompFrameHandler {
		@Override
		Type getPayloadType(StompHeaders headers) {
			return byte[].class;
		}

		@Override
		void handleFrame(StompHeaders headers, Object payload) {
			def notice = new JsonSlurper().parseText(new String(payload))
			blockingQueue.offer(notice);
		}
	}
}
