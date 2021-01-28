package ru.avem.rele.communication.model.devices.idc

import ru.avem.kserialpooler.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.kserialpooler.communication.utils.TransportException
import ru.avem.kserialpooler.communication.utils.toHexStr
import ru.avem.rele.communication.model.DeviceRegister
import ru.avem.rele.communication.model.IDeviceController


class IDCController(
    override val name: String,
    override val protocolAdapter: ModbusRTUAdapter,
    override val id: Byte
) : IDeviceController {
    val model = IDCModel()
    override var isResponding = true //TODO
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


    override fun readRequest(request: String): String {
        val requestString = StringBuilder()
        requestString.append("A00").append(id).append(" ").append(request).append("\n")
        protocolAdapter.connection.write(requestString.toString().toByteArray())
        val inputBytes = ByteArray(13)
        protocolAdapter.connection.read(inputBytes).toString()
        val output = StringBuilder()
        val toHexStr = toHexStr(inputBytes).replace(" ", "")
        var i = 0
        while (i < toHexStr.length) {
            val str: String = toHexStr.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.dropLast(1).toString()
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

    fun getVoltage(): Double {
        return readRequest("MEASure:VOLTage?").toDouble()
    }

    fun getCurrent(): Double {
        return readRequest("MEASure:CURRent?").toDouble()
    }

//    var voltage = idcGV1.getVoltage()
//    appendMessageToLog(LogTag.MESSAGE, "voltage = ${formatRealNumber(voltage)}")
//    var current = idcGV1.getCurrent()
//    appendMessageToLog(LogTag.MESSAGE, "current = ${String.format("%.4f", formatRealNumber(current))}")
}
