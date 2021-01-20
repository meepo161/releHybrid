package ru.avem.rele.communication.model.devices.idc

import ru.avem.kserialpooler.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.kserialpooler.communication.utils.TransportException
import ru.avem.rele.communication.adapters.stringascii.StringASCIIAdapter
import ru.avem.rele.communication.model.DeviceRegister
import ru.avem.rele.communication.model.IDeviceController

class IDCController(
    override val name: String,
    override val protocolAdapter: ModbusRTUAdapter,
    override val id: Byte
) : IDeviceController {
    val model = IDCModel()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override fun readRegister(register: DeviceRegister) {

    }

    override fun <T : Number> writeRegister(register: DeviceRegister, value: T) {
    }

    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()

    companion object {
    }


    override fun readRequest(request: String): Int {
        val requestString = StringBuilder()
        requestString.append("A00").append(id).append(" ").append(request).append("\n")
        return protocolAdapter.connection.read(requestString.toString().toByteArray())
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
        TODO("Not yet implemented")
    }

    override fun writeRequest(request: String) {
        val requestString = StringBuilder()
        requestString.append("A00").append(id).append(" ").append(request).append("\n")
        protocolAdapter.connection.write(requestString.toString().toByteArray())
//        sleep(300)
    }

    override fun checkResponsibility() {
        try {
            model.registers.values.firstOrNull()?.let {
                readRegister(it)
            }
        } catch (ignored: TransportException) {
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)

    fun remoteControl() {
        writeRequest("SYSTem:REMote")
    }

    fun localControl() {
        writeRequest("SYSTem:LOCal")
    }

    fun setVoltage(voltage: Double) {
        writeRequest("SOUR:VOLT $voltage")
    }

    fun onVoltage() {
        writeRequest("OUTP ON")
    }

    fun offVoltage() {
        writeRequest("OUTP OFF")
    }

    fun setMaxCurrent(current: Double) {
        writeRequest("SOUR:CURR $current")
    }


    fun getVolatage(): Int {
        return readRequest("MEASure:VOLTage?")
    }

}
