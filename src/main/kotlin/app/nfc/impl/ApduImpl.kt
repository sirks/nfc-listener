package app.nfc.impl

import app.nfc.Apdu
import org.springframework.stereotype.Component
import javax.smartcardio.CardChannel
import javax.smartcardio.CommandAPDU
import javax.smartcardio.ResponseAPDU

@Component
open class ApduImpl : Apdu {
    override fun execute(bytes: ByteArray, channel: CardChannel): ByteArray? {
        val command = CommandAPDU(bytes)
        val response = channel.transmit(command)
        return data(response)
    }

    private fun data(response: ResponseAPDU): ByteArray? {
        if (response.sW2 == 0) {
            return response.data
        }
        return null
    }
}