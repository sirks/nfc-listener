package app.nfc

import org.springframework.stereotype.Component

@Component
class ListenerInitializer(nfcListener: NfcListener) {

  init {
    nfcListener.listen()
  }
}