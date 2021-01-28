package ru.avem.rele.communication.model

import mu.KotlinLogging
import ru.avem.kserialpooler.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.kserialpooler.communication.utils.TransportException
import java.lang.Thread.sleep

interface IDeviceController {
    val name: String

    val protocolAdapter: ModbusRTUAdapter

    val id: Byte

    var isResponding: Boolean

    var requestTotalCount: Int
    var requestSuccessCount: Int

    fun readRegister(register: DeviceRegister) {

    }

    fun readRequest(request: String): String {
        return "0"
    }

    fun <T : Number> writeRegister(register: DeviceRegister, value: T) {

    }

    fun readAllRegisters() {

    }

    fun writeRegisters(register: DeviceRegister, values: List<Short>) {

    }

    fun writeRequest(request: String) {

    }

    val pollingRegisters: MutableList<DeviceRegister>
    val writingRegisters: MutableList<Pair<DeviceRegister, Number>>
    val pollingMutex: Any
    val writingMutex: Any

    fun IDeviceController.transactionWithAttempts(block: () -> Unit) {
        var attempt = 0
        val connection = protocolAdapter.connection

        while (attempt++ < connection.attemptCount) {
            requestTotalCount++

            try {
                block()
                requestSuccessCount++
                break
            } catch (e: TransportException) {
                val message =
                    "repeat $attempt/${connection.attemptCount} attempts with common success rate = ${(requestSuccessCount) * 100 / requestTotalCount}%"
                KotlinLogging.logger(name).info(message)

                if (attempt == connection.attemptCount) {
                    throw TransportException(message)
                }
            }
            sleep(10)
        }
    }

    fun getRegisterById(idRegister: String): DeviceRegister

    fun addPollingRegister(register: DeviceRegister) {
        synchronized(pollingMutex) {
            pollingRegisters.add(register)
        }
    }

    fun addWritingRegister(writingPair: Pair<DeviceRegister, Number>) {
        synchronized(writingMutex) {
            writingRegisters.add(writingPair)
        }
    }

    fun removePollingRegister(register: DeviceRegister) {
        synchronized(pollingMutex) {
            pollingRegisters.remove(register)
        }
    }

    fun removeAllPollingRegisters() {
        synchronized(pollingMutex) {
            pollingRegisters.forEach(DeviceRegister::deleteObservers)
            pollingRegisters.clear()
        }
    }

    fun readPollingRegisters() {
        synchronized(pollingMutex) {
            pollingRegisters.forEach {
                isResponding = try {
                    readRegister(it)
                    true
                } catch (e: TransportException) {
                    false
                }
            }
        }
    }

    fun removeAllWritingRegisters() {
        synchronized(writingMutex) {
            writingRegisters.forEach {
                it.first.deleteObservers()
            }
            writingRegisters.clear()
        }
    }

    fun writeWritingRegisters() {
        synchronized(writingMutex) {
            writingRegisters.forEach {
                isResponding = try {
                    writeRegister(it.first, it.second)
                    true
                } catch (e: TransportException) {
                    false
                }
            }
        }
    }

    fun checkResponsibility()
}
