package ru.avem.rele.communication.model

import ru.avem.kserialpooler.communication.Connection
import ru.avem.kserialpooler.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.kserialpooler.communication.utils.SerialParameters
import ru.avem.rele.app.Rele.Companion.isAppRunning
import ru.avem.rele.communication.model.devices.avem.avem4.Avem4Controller
import ru.avem.rele.communication.model.devices.avem.ikas.Ikas8Controller
import ru.avem.rele.communication.model.devices.idc.IDCController
import ru.avem.rele.communication.model.devices.rele.ReleController
import java.lang.Thread.sleep
import kotlin.concurrent.thread

object CommunicationModel {
    @Suppress("UNUSED_PARAMETER")
    enum class DeviceID(description: String) {
        RELE1("РЕЛЕ1"),
        RELE2("РЕЛЕ2"),
        RELE3("РЕЛЕ3"),
        GV1("ИБП"),
        AVEM41("АВЭМ"),
        IKAS1("ИКАС")
    }

    private var isConnected = false

    private val connection = Connection(
        adapterName = "CP2103 USB to RS-485",
        serialParameters = SerialParameters(8, 0, 1, 9600),
        timeoutRead = 200,
        timeoutWrite = 200,
        attemptCount = 10
    ).apply {
        connect()
        isConnected = true
    }

    private val connectionForAvem = Connection(
        adapterName = "CP2103 USB to AVEM",
        serialParameters = SerialParameters(8, 0, 1, 115200),
        timeoutRead = 200,
        timeoutWrite = 200
    ).apply {
        connect()
        isConnected = true
    }


    private val modbusAdapter = ModbusRTUAdapter(connection)
    private val modbusAdapterForAvem = ModbusRTUAdapter(connectionForAvem)

    private val deviceControllers: Map<DeviceID, IDeviceController> = mapOf(
        DeviceID.IKAS1 to Ikas8Controller(DeviceID.IKAS1.toString(), modbusAdapter, 4),
        DeviceID.RELE1 to ReleController(DeviceID.RELE1.toString(), modbusAdapter, 1),
        DeviceID.RELE2 to ReleController(DeviceID.RELE2.toString(), modbusAdapter, 2),
        DeviceID.RELE3 to ReleController(DeviceID.RELE3.toString(), modbusAdapter, 3),
        DeviceID.GV1 to IDCController(DeviceID.GV1.toString(), modbusAdapter, 5),
        DeviceID.AVEM41 to Avem4Controller(DeviceID.AVEM41.toString(), modbusAdapterForAvem, 6)
    )

    init {
        thread(isDaemon = true) {
            while (isAppRunning) {
                if (isConnected) {
                    deviceControllers.values.forEach {
                        it.readPollingRegisters()
                    }
                }
                sleep(1)
            }
        }
        thread(isDaemon = true) {
            while (isAppRunning) {
                if (isConnected) {
                    deviceControllers.values.forEach {
                        it.writeWritingRegisters()
                    }
                }
                sleep(1)
            }
        }
    }

    fun getDeviceById(deviceID: DeviceID) = deviceControllers[deviceID] ?: error("Не определено $deviceID")

    fun startPoll(deviceID: DeviceID, registerID: String, block: (Number) -> Unit) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        register.addObserver { _, arg ->
            block(arg as Number)
        }
        device.addPollingRegister(register)
    }

    fun clearPollingRegisters() {
        deviceControllers.values.forEach(IDeviceController::removeAllPollingRegisters)
    }

    fun clearWritingRegisters() {
        deviceControllers.values.forEach(IDeviceController::removeAllWritingRegisters)
    }

    fun removePollingRegister(deviceID: DeviceID, registerID: String) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        register.deleteObservers()
        device.removePollingRegister(register)
        register.value = -1
    }

    fun removePollingRegisters() {
        deviceControllers.values.forEach(IDeviceController::removeAllPollingRegisters)
    }

    fun checkDevices(): List<DeviceID> {
        deviceControllers.values.forEach(IDeviceController::checkResponsibility)
        return deviceControllers.filter { !it.value.isResponding }.keys.toList()
    }

    fun addWritingRegister(deviceID: DeviceID, registerID: String, value: Number) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        device.addWritingRegister(register to value)
    }
}
