package ws

import java.net.Socket

class Session constructor(socket: Socket) {
    init {
        println("client Connected: ${socket}")
    }
}