package ru.avem.rele.communication.model

interface IDeviceModel {
    val registers: Map<String, DeviceRegister>

    fun getRegisterById(idRegister: String): DeviceRegister
}
