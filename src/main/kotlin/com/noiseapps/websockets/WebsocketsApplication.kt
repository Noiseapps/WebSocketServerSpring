package com.noiseapps.websockets

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class WebsocketsApplication {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(WebsocketsApplication::class.java, *args)
        }
    }
}