package com.noiseapps.websockets;

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

    @RequestMapping("/push")
    fun onGetRequest(): Message {
        val msg = Message(title = "Hello push", message = "Some random string ${Math.random()}")
        val msgString = Gson().toJson(msg)
        logger.info("Pushing message to all devices: $msgString")
        SocketHandler.sessions.forEach { _, session ->
            session.sendMessage(TextMessage(msgString))
        }
        return msg
    }

    @RequestMapping("/push/{nick}")
    fun onGetRequest(@PathVariable nick : String): ResponseEntity<Message> {
        val msg = Message(title = "Hello $nick", message = "Some random string ${Math.random()}")
        val msgString = Gson().toJson(msg)
        val sid = SocketHandler.sessionMapping[nick]
        if(sid != null) {
            logger.info("Pushing message to device with sid $sid: $msgString")
            SocketHandler.sessions[sid]?.sendMessage(TextMessage(msgString))
            return ResponseEntity(msg, HttpStatus.OK)
        }
        logger.warn("Trying to push message to non-existing user '$nick'")
        val err = Message(title = "Error", message = "User with nickname '$nick' not found")
        return ResponseEntity(err, HttpStatus.NOT_FOUND)
    }
}