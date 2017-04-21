package app.nfc

import app.ws.NfcListenerController
import app.ws.Notice
import app.ws.NoticeType
import org.springframework.stereotype.Component
import java.lang.Thread.sleep
import javax.smartcardio.CardChannel
import javax.smartcardio.CardTerminal
import javax.smartcardio.CommandAPDU
import javax.smartcardio.TerminalFactory
import kotlin.concurrent.thread

@Component
class Listener(val socket: NfcListenerController) {
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    fun listen() {
        thread(start = true, isDaemon = true)
        {
            val terminal = getTerminal()
            socket.notify(Notice(NoticeType.TERMINAL, ""))
            while (true) {
                readUid(terminal)
            }
        }
    }

    private fun getTerminal(): CardTerminal {
        val terminals = TerminalFactory.getDefault().terminals()
        while (true) {
            try {
                return terminals.list().first()
            } catch (e: Exception) {
                sleep(1000)
            }
        }
    }

    private fun readUid(terminal: CardTerminal) {
        terminal.waitForCardPresent(0)
        val card = terminal.connect("*")
        val channel = card.basicChannel
        getUID(channel)?.let {
            val uid = it.toHex()
            socket.notify(Notice(NoticeType.UID, uid))
        }
        terminal.waitForCardAbsent(0)
    }

    private fun getUID(channel: CardChannel): ByteArray? {
        return runCommand(byteArrayOf(0xff.toByte(), 0xca.toByte(), 0x00, 0x00, 0x00), channel)
    }

    private fun runCommand(bytes: ByteArray, channel: CardChannel): ByteArray? {
        val commandAPDU = CommandAPDU(bytes)
        val responseAPDU = channel.transmit(commandAPDU)
        if (responseAPDU.sW2 == 0) {
            return responseAPDU.data
        }
        return null
    }

    private fun ByteArray.toHex(): String {
        val result = StringBuffer()
        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }

        return result.toString()
    }
}