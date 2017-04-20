package app.nfc

import app.ws.NfcListenerController
import app.ws.NoticeType
import app.ws.Notice
import spock.lang.Specification
import spock.lang.Subject

import javax.smartcardio.Card
import javax.smartcardio.CardChannel
import javax.smartcardio.CardTerminal
import javax.smartcardio.ResponseAPDU

class ListenerTest extends Specification {

	def socket = Mock(NfcListenerController)

	@Subject
			listener = new Listener(socket)

	def "uid serialized correctly for successful read"() {
		given:
		def rApdu = Mock(ResponseAPDU)
		rApdu.getSW2() >> 0
		rApdu.getData() >> [0xaa, 0xb0, 0x0c, 0x00]
		def channel = Mock(CardChannel)
		channel.transmit(_) >> rApdu
		def card = Mock(Card)
		card.basicChannel >> channel
		def terminal = Mock(CardTerminal)
		terminal.connect("*") >> card

		when:
		listener.readUid(terminal)

		then:
		1 * socket.notify(_) >> {
			Notice notice = it[0]
			with(notice){
				assert type == NoticeType.UID
				assert uid == "AAB00C00"
			} 
		}

	}
}
