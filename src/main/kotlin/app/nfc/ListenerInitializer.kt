package app.nfc

import org.springframework.stereotype.Component

@Component
class ListenerInitializer(listener: Listener) {

    init {
        listener.listen()
    }
}