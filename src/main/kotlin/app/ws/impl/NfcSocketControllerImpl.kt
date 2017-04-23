package app.ws.impl

import app.ws.NfcSocketController
import app.ws.Notice
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
open class NfcSocketControllerImpl(private val template: SimpMessagingTemplate) : NfcSocketController {

    override fun notify(notice: Notice) {
        template.convertAndSend("/topic/nfc", notice)
    }
}