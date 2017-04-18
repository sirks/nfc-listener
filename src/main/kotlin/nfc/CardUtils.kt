package nfc

import org.apache.tomcat.util.codec.DecoderException
import java.awt.SystemColor.window
import java.io.PrintStream
import java.net.NetworkInterface
import javax.smartcardio.*

object CardUtils {

    var b: ByteArray = 
    var card: Card? = null
    var channel: CardChannel? = null
    var terminal: CardTerminal
    var looper: Boolean = false
    var ps: PrintStream
    var uid: String, var str_resp:String, var sw2:String? = null

    fun Utils(): ??? {
    }

    
    
    
    @Throws(CardException::class)
    fun GetTerminal() {
        ps.println("getting terminals")
        val factory = TerminalFactory.getDefault()
        val terminals = factory.terminals().list()
        ps.println("Terminals: " + terminals)
        // get the first terminal
        terminal = terminals[0]
        ps.println("your card please...")
    }

    fun myMac(): String {
        var mac: String? = null
        try {
            val enumNi = NetworkInterface.getNetworkInterfaces()
            var ni: NetworkInterface? = null
            val i = 1
            while (enumNi.hasMoreElements()) {
                try {
                    ni = enumNi.nextElement()
                    if (!ni!!.isLoopback) {
                        mac = ByteToHex(ni.hardwareAddress)
                        break
                    }
                } catch (e: Exception) {
                }

            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return mac
    }

    @Throws(CardException::class, DecoderException::class)
    fun GetUID() {
        RunCommand("FFCA000000")
        uid = str_resp
    }

    @Throws(CardException::class, DecoderException::class)
    fun RunCommand(s: String) {
        //data is actual text. others are 2byte hex
        val capdu = CommandAPDU(HexToByte(s))
        val rapdu = channel!!.transmit(capdu)
        b = rapdu.data
        str_resp = ByteToHex(b)
        //sw2;af=have more data;9d=not authorized
        if (Integer.toHexString(rapdu.sW1) == "63") {
            throw CardException("incorrect card response=" + Integer.toHexString(rapdu.sW1))
        }
    }

    fun ByteToHex(bb: ByteArray): String {
        return String.valueOf(Hex.encodeHex(bb)).toUpperCase()
    }

    @Throws(DecoderException::class)
    fun HexToByte(hex: String): ByteArray {//"FF 01 02 03 FF "
        return Hex.decodeHex(hex.toCharArray())
    }

    fun nfcListener() {
        Thread(Runnable {
            try {
                window.call("java_talk", *arrayOf<Any>("1", "Searching for terminals..."))
                utils.GetTerminal()
                window.call("java_talk", *arrayOf<Any>("1", "Got terminal " + utils.terminal.getName() + "; Listening for cards..."))
                while (utils.looper) {
                    val i = 0
                    try {
                        utils.card = utils.terminal.connect("*")
                        utils.channel = utils.card.getBasicChannel()
                        utils.GetUID()
                        window.call("java_nfc", *arrayOf<Any>(utils.uid))
                        ps.println("got your card uid:" + utils.uid)
                        for (j in 0..599) {
                            try {
                                utils.card = utils.terminal.connect("*")
                                Thread.sleep(100)
                            } catch (e: CardException) {
                                break
                            }

                        }
                    } catch (e: CardException) {
                    }

                    Thread.sleep(100)
                }
            } catch (ex: CardException) {
                window.call("java_talk", *arrayOf<Any>("100", "No terminals found."))
            } catch (ex: Exception) {
                ps.println("card exception=" + ex.message)
            } finally {
                utils.looper = false
                ps.println("Stopped listening to cards")
            }
        }).start()
    }
    
}