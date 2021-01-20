package ru.avem.rele.communication.adapters.stringascii

import ru.avem.kserialpooler.communication.Connection

class StringASCIIAdapter(val connection: Connection){
    fun read(
        deviceId: Byte,
        request: String
    ) : Int {
        val requestString = StringBuilder()
        requestString.append("A00").append(deviceId).append(" ").append(request).append("\n")
        return connection.read(requestString.toString().toByteArray())
    }

    fun write(
        deviceId: Byte,
        request: String
    ) {
        val requestString = StringBuilder()
        requestString.append("A00").append(deviceId).append(" ").append(request).append("\n")
        connection.write(requestString.toString().toByteArray())
    }
}
