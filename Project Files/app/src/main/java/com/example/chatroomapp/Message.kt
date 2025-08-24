package com.example.chatroomapp

import java.text.DateFormat

class Message {
    var name: String? = null
    var message: String? = null
    var senderID: String? = null
    var time: String? = null


    constructor(){}

    constructor(name: String?, message: String?, senderID: String?, time: String?){
        this.name = name
        this.message = message
        this.senderID = senderID
        this.time = time
    }

}