package app.ws

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
open class NfcListenerController(val template: SimpMessagingTemplate) {

    fun notify(notice: Notice) {
        template.convertAndSend("/topic/nfc", notice)
    }
}