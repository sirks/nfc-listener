package app.nfc

import javax.smartcardio.CardChannel

interface Apdu {
    fun execute(bytes: ByteArray, channel: CardChannel): ByteArray?
}