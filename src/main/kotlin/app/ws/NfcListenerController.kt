package app.ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class NfcListenerController {

    @Autowired
    lateinit var template: SimpMessagingTemplate

    fun notify(notice: Notice) {
        template.convertAndSend("/topic/nfc", notice)
    }
}