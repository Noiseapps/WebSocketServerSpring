package com.noiseapps.websockets

import com.google.gson.Gson
import com.noiseapps.websockets.models.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.socket.TextMessage

@RestController
class RootController {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @RequestMapping("/pushMultiple/{count}")
    fun pushMultipleNotificationsToAll(@PathVariable count: Int?): Message {
        if(count == null || count <= 0) {
            val status = Message(title = "Hello push", message = "Invalid count ")
            return status
        }

        if (SocketHandler.sessions.isEmpty()) {
            val status = Message(title = "Hello push", message = "No active sessions")
            return status
        }

        for (i in 1..count) {
            notifyAllUsers()
            Thread.sleep(1000)
        }

        val status = Message(title = "Hello push", message = "Pushed notifications to $count devices")
        return status
    }

    @RequestMapping("/push")
    fun notifyAllUsers(): Message {
        if (SocketHandler.sessions.isEmpty()) {
            val status = Message(title = "Hello push", message = "No active sessions")
            return status
        }
        val msg = Message(title = "Hello push", message = "Some random string ${Math.random().format(2)}")
        val msgString = Gson().toJson(msg)
        logger.info("Pushing message to all devices: $msgString")
        SocketHandler.sessions.forEach { _, session ->
            session.sendMessage(TextMessage(msgString))
        }
        return msg
    }

    @RequestMapping("/push/{nick}")
    fun notifySingleUserByNickname(@PathVariable nick: String): ResponseEntity<Message> {
        val msg = Message(title = "Hello $nick", message = "Some random string ${Math.random()}")
        val msgString = Gson().toJson(msg)
        val sid = SocketHandler.sessionMapping[nick]
        if (sid != null) {
            logger.info("Pushing message to device with sid $sid: $msgString")
            SocketHandler.sessions[sid]?.sendMessage(TextMessage(msgString))
            return ResponseEntity(msg, HttpStatus.OK)
        }
        logger.warn("Trying to push message to non-existing user '$nick'")
        val err = Message(title = "Error", message = "User with nickname '$nick' not found")
        return ResponseEntity(err, HttpStatus.NOT_FOUND)
    }


    @RequestMapping("/sessions")
    fun listAllActiveSessions(): ResponseEntity<Map<String, String>> {
        logger.info("Listing all active sessions")
        return ResponseEntity(SocketHandler.sessionMapping, HttpStatus.OK)
    }

}