package app.nfc.impl

import app.nfc.Apdu
import app.ws.NfcSocketController
import app.ws.Notice
import app.ws.NoticeType
import spock.lang.Specification
import spock.lang.Subject

import javax.smartcardio.Card
import javax.smartcardio.CardChannel
import javax.smartcardio.CardTerminal

class NfcListenerImplTest extends Specification {

	def socket = Mock(NfcSocketController)
	def apdu = Mock(Apdu)

	@Subject
	def listener = new NfcListenerImpl(socket, apdu)

	def "uid serialized correctly happy path"() {
		given:
		def channel = Mock(CardChannel)
		def card = Mock(Card)
		card.basicChannel >> channel
		def terminal = Mock(CardTerminal)
		terminal.connect("*") >> card

		and:
		apdu.execute(*_) >> ([0xaa, 0xb0, 0x0c, 0x00] as byte[])

		when:
		listener.readUid(terminal)

		then:
		1 * socket.notify(_) >> {
			Notice notice = it[0]
			with(notice) {
				assert type == NoticeType.UID
				assert uid == "AAB00C00"
			}
		}
	}

	def "do not notify on unsuccessful uid read"() {
		given:
		def channel = Mock(CardChannel)
		def card = Mock(Card)
		card.basicChannel >> channel
		def terminal = Mock(CardTerminal)
		terminal.connect("*") >> card

		when:
		listener.readUid(terminal)

		then:
		0 * socket.notify(_)
	}
}
