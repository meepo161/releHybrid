package ru.avem.rele.controllers

import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.CommunicationModel.getDeviceById
import ru.avem.rele.communication.model.devices.avem.avem4.Avem4Controller
import ru.avem.rele.communication.model.devices.avem.ikas.Ikas8Controller
import ru.avem.rele.communication.model.devices.idc.IDCController
import ru.avem.rele.communication.model.devices.rele.ReleController
import tornadofx.Controller

abstract class TestController : Controller() {
    protected val rele1 = getDeviceById(CommunicationModel.DeviceID.RELE1) as ReleController
    protected val rele2 = getDeviceById(CommunicationModel.DeviceID.RELE2) as ReleController
    protected val rele3 = getDeviceById(CommunicationModel.DeviceID.RELE3) as ReleController

    //    protected val owenPrDD3 = getDeviceById(CommunicationModel.DeviceID.DD3) as OwenPrController
    protected val ikas1 = getDeviceById(CommunicationModel.DeviceID.IKAS1) as Ikas8Controller

    //    protected val ack3002 = getDeviceById(CommunicationModel.DeviceID.ACK1) as ACK3002Controller
    protected val idcGV1 = getDeviceById(CommunicationModel.DeviceID.GV1) as IDCController
    protected val avem4 = getDeviceById(CommunicationModel.DeviceID.AVEM41) as Avem4Controller

    fun onAllRele() {
        var i = 1
        while (i < 33) {
            rele1.on(i)
            i++
        }
        i = 1
        while (i < 33) {
            rele2.on(i)
            i++
        }
        i = 1
        while (i < 33) {
            rele3.on(i)
            i++
        }
    }

    fun offAllRele() {
        var i = 1
        while (i < 33) {
            rele1.off(i)
            i++
        }
        i = 1
        while (i < 33) {
            rele2.off(i)
            i++
        }
        i = 1
        while (i < 33) {
            rele3.off(i)
            i++
        }
    }
}
