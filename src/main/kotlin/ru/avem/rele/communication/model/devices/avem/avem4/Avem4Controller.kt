package ru.avem.rele.communication.model.devices.avem.avem4

import ru.avem.kserialpooler.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.kserialpooler.communication.adapters.utils.ModbusRegister
import ru.avem.kserialpooler.communication.utils.TransportException
import ru.avem.kserialpooler.communication.utils.TypeByteOrder
import ru.avem.kserialpooler.communication.utils.allocateOrderedByteBuffer
import ru.avem.rele.communication.model.DeviceRegister
import ru.avem.rele.communication.model.IDeviceController
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Avem4Controller(
    override val name: String, override val protocolAdapter: ModbusRTUAdapter, override val id: Byte
) : IDeviceController {
    private val model = Avem4Model()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val pollingMutex = Any()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()

    override fun readRegister(register: DeviceRegister) {
        isResponding = try {
            transactionWithAttempts {
                when (register.valueType) {
                    DeviceRegister.RegisterValueType.SHORT -> {
                        val value = protocolAdapter.readHoldingRegisters(id, register.address, 1).first().toShort()
                        register.value = value
                    }
                    DeviceRegister.RegisterValueType.FLOAT -> {
                        val modbusRegister =
                            protocolAdapter.readHoldingRegisters(id, register.address, 2).map(ModbusRegister::toShort)
                        register.value = allocateOrderedByteBuffer(modbusRegister, TypeByteOrder.LITTLE_ENDIAN, 4).float
                    }
                    DeviceRegister.RegisterValueType.INT32 -> {
                        val modbusRegister =
                            protocolAdapter.readHoldingRegisters(id, register.address, 2).map(ModbusRegister::toShort)
                        register.value = allocateOrderedByteBuffer(modbusRegister, TypeByteOrder.LITTLE_ENDIAN, 4).int
                    }
                }
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun <T : Number> writeRegister(register: DeviceRegister, value: T) {
        isResponding = try {
            when (value) {
                is Float -> {
                    val bb = ByteBuffer.allocate(4).putFloat(value).order(ByteOrder.LITTLE_ENDIAN)
                    val registers = listOf(ModbusRegister(bb.getShort(2)), ModbusRegister(bb.getShort(0)))
                    transactionWithAttempts {
                        protocolAdapter.presetMultipleRegisters(id, register.address, registers)
                    }
                }
                is Int -> {
                    val bb = ByteBuffer.allocate(4).putInt(value).order(ByteOrder.LITTLE_ENDIAN)
                    val registers = listOf(ModbusRegister(bb.getShort(2)), ModbusRegister(bb.getShort(0)))
                    transactionWithAttempts {
                        protocolAdapter.presetMultipleRegisters(id, register.address, registers)
                    }
                }
                is Short -> {
                    transactionWithAttempts {
                        protocolAdapter.presetSingleRegister(id, register.address, ModbusRegister(value))
                    }
                }
                else -> {
                    throw UnsupportedOperationException("Method can handle only with Float, Int and Short")
                }
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
        val registers = values.map { ModbusRegister(it) }
        isResponding = try {
            transactionWithAttempts {
                protocolAdapter.presetMultipleRegisters(id, register.address, registers)
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)

    override fun checkResponsibility() {
        model.registers.values.firstOrNull()?.let {
            readRegister(it)
        }
    }

    fun toggleProgrammingMode() {
        val serialNumberRegister = getRegisterById(Avem4Model.SERIAL_NUMBER)
        readRegister(serialNumberRegister)
        val serialNumber = serialNumberRegister.value.toShort()
        writeRegister(serialNumberRegister, serialNumber)
    }

    fun writeRuntimeKTR(ktr: Float) {
        writeRegister(getRegisterById(Avem4Model.KTR_RUNTIME), ktr)
    }

    fun writeKTR(ktr: Float) {
        writeRegister(getRegisterById(Avem4Model.KTR_FLASH), ktr)
    }

    fun setChartTime(chartTime: Int) {
        writeRegister(getRegisterById(Avem4Model.SET_CHART_TIME), chartTime)
    }

    fun setTriggerMode(mode: Short) {
        writeRegister(getRegisterById(Avem4Model.TRIGGER_MODE), mode)
    }

    fun setTriggerValue(value: Float) {
        writeRegister(getRegisterById(Avem4Model.TRIGGER_VALUE), value)
    }

    fun startChart(control: Short) {
        writeRegister(getRegisterById(Avem4Model.START_CHART), control)
    }

    fun readChartPoints(): List<Float> {
        val startRegister = 0xE00A.toShort()
        val endRegister = startRegister + 4000 * 2
        val dotList = mutableListOf<Float>()
        try {
            for (registerIndex in startRegister..endRegister - 50 step 50) {
                val dots =
                    protocolAdapter.readHoldingRegisters(id, registerIndex.toShort(), 50).map(ModbusRegister::toShort)
                for (index in dots.indices step 2) {
                    dotList.add(allocateOrderedByteBuffer(dots, TypeByteOrder.LITTLE_ENDIAN, 4).float)
                }
            }
        } catch (e: Exception) {

        }
        return dotList
    }

    fun getRMSVoltage(): Float {
        readRegister(getRegisterById(Avem4Model.RMS_VOLTAGE))
        return getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat()
    }
}
