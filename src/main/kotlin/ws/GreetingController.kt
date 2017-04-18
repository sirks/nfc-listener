package ws

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class GreetingController {

    @Autowired
    lateinit var template: SimpMessagingTemplate

    @MessageMapping("/app/hello")
    fun hello(message: HelloMessage) {
        Thread.sleep(1000) // simulated delay
        greeting("Hello, " + message.name + "1!")
        Thread.sleep(1000) // simulated delay
        greeting("Hello, " + message.name + "2!")
        Thread.sleep(1000) // simulated delay
        greeting("Hello, " + message.name + "3!")
    }

    private fun greeting(greeting: String) {
        template.convertAndSend("/topic/greetings", Greeting(greeting))
    }
}