package app.nfc

import app.ws.NfcListenerController
import app.ws.NfcStatus
import app.ws.Notice
import org.springframework.stereotype.Component
import java.lang.Thread.sleep
import javax.smartcardio.*
import kotlin.concurrent.thread

@Component
open class Card(socket: NfcListenerController) {
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    var socket: NfcListenerController = socket

    init {
        nfcListener()
    }

    private fun getTerminal(): CardTerminal {
        val terminals = TerminalFactory.getDefault().terminals()
        while (true) {
            try {
                return terminals.list().first()
            } catch(e: CardException) {
                sleep(1000)
            }
        }
    }

    private fun getUID(channel: CardChannel): ByteArray? {
        return RunCommand(byteArrayOf(0xff.toByte(), 0xca.toByte(), 0x00, 0x00, 0x00), channel)
    }

    private fun RunCommand(bytes: ByteArray, channel: CardChannel): ByteArray? {
        val capdu = CommandAPDU(bytes)
        val rapdu = channel.transmit(capdu)
        if (rapdu.sW2 == 0) {
            return rapdu.data
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

    fun nfcListener() {
        thread(start = true, isDaemon = true)
        {
            scan()
        }
    }

    private fun scan() {
        val terminal = getTerminal()
        socket.notify(Notice(NfcStatus.TERMINAL, ""))
        while (true) {
            readUid(terminal)
        }
    }

    private fun readUid(terminal: CardTerminal) {
        terminal.waitForCardPresent(0)
        val card = terminal.connect("*")
        val channel = card.basicChannel
        getUID(channel)?.let {
            val uid = it.toHex()
            socket.notify(Notice(NfcStatus.UID, uid))
        }
        terminal.waitForCardAbsent(0)
    }
}