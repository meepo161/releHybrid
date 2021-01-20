package ru.avem.rele.communication.model.devices.avem.avem4

import ru.avem.rele.communication.model.DeviceRegister
import ru.avem.rele.communication.model.IDeviceModel

class Avem4Model : IDeviceModel {
    companion object {
        const val RMS_VOLTAGE = "RMS_VOLTAGE"
        const val AMP_VOLTAGE = "AMP_VOLTAGE"
        const val KTR_RUNTIME = "KTR_RUNTIME"
        const val SERIAL_NUMBER = "SERIAL_NUMBER"
        const val SOFTWARE_DATE = "SOFTWARE_DATE"
        const val KTR_FLASH = "KTR_FLASH"
        const val SHOW_VALUE = "SHOW_VALUE"
        const val SET_CHART_TIME = "SET_CHART_TIME"
        const val TRIGGER_VALUE = "TRIGGER_VALUE"
        const val TRIGGER_MODE = "TRIGGER_MODE"
        const val START_CHART = "START_CHART"
        const val STATE_CHART = "STATE_CHART"
        const val GET_CHART_TIME = "GET_CHART_TIME"
        const val CHART_POINTS = "CHART_POINTS"
    }

    override val registers: Map<String, DeviceRegister> = mapOf(
        RMS_VOLTAGE to DeviceRegister(0x1004, DeviceRegister.RegisterValueType.FLOAT, "В"),
        AMP_VOLTAGE to DeviceRegister(0x1002, DeviceRegister.RegisterValueType.FLOAT, "В"),
        KTR_RUNTIME to DeviceRegister(0x10BC, DeviceRegister.RegisterValueType.FLOAT),
        SERIAL_NUMBER to DeviceRegister(0x1108, DeviceRegister.RegisterValueType.SHORT),
        SOFTWARE_DATE to DeviceRegister(0x1022, DeviceRegister.RegisterValueType.INT32),
        KTR_FLASH to DeviceRegister(0x10CE, DeviceRegister.RegisterValueType.FLOAT),
        SHOW_VALUE to DeviceRegister(0x10D8, DeviceRegister.RegisterValueType.INT32),
        TRIGGER_VALUE to DeviceRegister(0xE000.toShort(), DeviceRegister.RegisterValueType.FLOAT),
        SET_CHART_TIME to DeviceRegister(0xE002.toShort(), DeviceRegister.RegisterValueType.INT32),
        TRIGGER_MODE to DeviceRegister(0xE004.toShort(), DeviceRegister.RegisterValueType.SHORT),
        START_CHART to DeviceRegister(0xE005.toShort(), DeviceRegister.RegisterValueType.SHORT),
        STATE_CHART to DeviceRegister(0xE006.toShort(), DeviceRegister.RegisterValueType.SHORT),
        GET_CHART_TIME to DeviceRegister(0xE008.toShort(), DeviceRegister.RegisterValueType.INT32),
        CHART_POINTS to DeviceRegister(0xE00A.toShort(), DeviceRegister.RegisterValueType.FLOAT)
    )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")
}
