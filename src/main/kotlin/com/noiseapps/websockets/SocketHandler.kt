package com.noiseapps.websockets

import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import com.noiseapps.websockets.models.Message
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

fun Double.format(digits: Int) : String = java.lang.String.format("%.${digits}f", this)

class SocketHandler : AbstractWebSocketHandler() {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    companion object {
        val sessions: ConcurrentMap<String, WebSocketSession> = ConcurrentHashMap()
        val sessionMapping : ConcurrentMap<String, String> = ConcurrentHashMap()
    }

    override fun afterConnectionEstablished(session: WebSocketSession?) {
        logger.info("New session opened with ${session?.id}")
        sessions.put(session?.id, session)
    }

    override fun afterConnectionClosed(session: WebSocketSession?, closeStatus: CloseStatus?) {
        logger.info("Session with id ${session?.id} has been closed")
        sessions.remove(session?.id)
        sessionMapping.values.remove(session?.id)
    }

    override fun handleTextMessage(session: WebSocketSession?, message: TextMessage?) {
        val msgContent = Gson().fromJson(message?.payload, Message::class.java)
        if(msgContent.title.equals("handshake")) {
            logger.info("Handshake for session (sid:${session?.id}) with nickname ${msgContent.message}")
            sessionMapping.put(msgContent.message, session?.id)
        } else {
            logger.info("Received message from client (sid:${session?.id}) : ${msgContent.title}")
            val newMsg = Message(title = "Answering to ${msgContent.title}", message = "Random message ${Math.random().format(2)}")
            sessions[session?.id]?.sendMessage(TextMessage(Gson().toJson(newMsg)))
        }
    }
}