package ws

import java.net.ServerSocket

object Server {

    val sessions = mutableListOf<Session>()


    @JvmStatic fun main(args: Array<String>) {
        val server = ServerSocket(8123)
        println("Server has started on 127.0.0.1:80.\r\nWaiting for a connection...")
        runWsServer(server)
    }

    private fun runWsServer(server: ServerSocket) {
        while (true) {
            val socket = server.accept()
            sessions.add(Session(socket))
        }
    }

}