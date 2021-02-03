package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.CommunicationModel.checkDevices
import ru.avem.rele.communication.model.devices.avem.ikas.Ikas8Model
import ru.avem.rele.entities.TableValuesTest1
import ru.avem.rele.utils.*
import ru.avem.rele.utils.Singleton.currentTestItem
import ru.avem.rele.view.Test1View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableListOf
import tornadofx.style
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test1Controller : TestController() {
    val view: Test1View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableListOf(
        TableValuesTest1(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("Неизвестно")
        ),

        TableValuesTest1(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("Неизвестно")
        )
    )

    @Volatile
    var isExperimentRunning: Boolean = false

    @Volatile
    var isExperimentEnded: Boolean = true

    @Volatile
    private var measuringR: Float = 0f

    @Volatile
    private var statusIkas: Float = 0f

    @Volatile
    private var testItemR1: Double = 0.0

    @Volatile
    private var testItemR2: Double = 0.0

    @Volatile
    private var testItemSerial: String = ""

    @Volatile
    private var measuringR1: Double = 0.0

    @Volatile
    private var measuringR2: Double = 0.0

    @Volatile
    private var cause: String = ""

    fun clearTable() {
        tableValues.forEach {
            it.resistanceCoil1.value = "0.0"
            it.resistanceCoil2.value = "0.0"
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
        tableValues[0].resistanceCoil1.value = currentTestItem.resistanceCoil1
        tableValues[0].resistanceCoil2.value = currentTestItem.resistanceCoil2
    }

    fun setExperimentProgress(currentTime: Int, time: Int = 1) {
        Platform.runLater {
            view.progressBarTime.progress = currentTime.toDouble() / time
        }
    }

    fun appendMessageToLog(tag: LogTag, _msg: String) {
        val msg = Text("${SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())} | $_msg")
        msg.style {
            fill = when (tag) {
                LogTag.MESSAGE -> tag.c
                LogTag.ERROR -> tag.c
                LogTag.DEBUG -> tag.c
            }
        }

        Platform.runLater {
            view.vBoxLog.add(msg)
        }
    }

    private fun isDevicesResponding(): Boolean {
        if (ikas1.isResponding) {
            view.circleComStatus.fill = State.OK.c
        } else {
            view.circleComStatus.fill = State.BAD.c
        }
        return ikas1.isResponding
    }

    fun setCause(cause: String) {
        this.cause = cause
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
        view.buttonStartStopTest.isDisable = true
    }

    private fun startPollDevices() {
        CommunicationModel.startPoll(CommunicationModel.DeviceID.IKAS1, Ikas8Model.STATUS) { value ->
            statusIkas = value.toFloat()
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.IKAS1, Ikas8Model.RESIST_MEAS) { value ->
            measuringR = value.toFloat()
        }
    }

    init {
    }

    fun startTest() {
        thread(isDaemon = true) {
            sleep(400)
            cause = ""
            clearLog()
            clearTable()

            appendMessageToLog(LogTag.MESSAGE, "Начало испытания")
            cause = ""
            testItemR1 = currentTestItem.resistanceCoil1.replace(",", ".").toDouble()
            testItemR2 = currentTestItem.resistanceCoil2.replace(",", ".").toDouble()
            testItemSerial = currentTestItem.serialNumber

            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
            }

            startPollDevices()
            isExperimentRunning = true
            isExperimentEnded = false

            appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")

            checkDevices()
            var timeToStart = 300
            while (isExperimentRunning && !isDevicesResponding() && timeToStart-- > 0) {
                sleep(100)
            }

            if (!isDevicesResponding()) {
                cause = "Приборы не отвечают"
            }

            appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")
            appendMessageToLog(LogTag.DEBUG, "Ожидание...")

            when (Singleton.currentTestItemType) {
                "НМШ1" -> {
                    nmsh1()
                }
                "НМШ2" -> {
                    nmsh2()
                }
                "НМШ3" -> {
                    nmsh3()
                }
                "НМШ4" -> {
                    nmsh4()
                }
                "АНШ2" -> {
                    ansh2()
                }
                "РЭЛ1" -> {
                    rel1()
                }
                "РЭЛ2" -> {
                    rel2()
                }
                else -> {
                    Toast.makeText("Ошибка, нет такого типа объекта испытания").show(Toast.ToastType.ERROR)
                }
            }

            if (testItemR2 == 0.0) {
                setResultFor1()
            } else {
                setResultFor2()
            }
            appendMessageToLog(LogTag.DEBUG, "Испытание завершено")


            isExperimentRunning = false
            isExperimentEnded = true
            CommunicationModel.clearPollingRegisters()

            controller.tableValuesTest1[0].resistanceCoil1.value = tableValues[0].resistanceCoil1.value
            controller.tableValuesTest1[0].resistanceCoil2.value = tableValues[0].resistanceCoil2.value
            controller.tableValuesTest1[1].resistanceCoil1.value = tableValues[1].resistanceCoil1.value
            controller.tableValuesTest1[1].resistanceCoil2.value = tableValues[1].resistanceCoil2.value
            controller.tableValuesTest1[1].result.value =          tableValues[1].result.value

            Platform.runLater {
                view.buttonBack.isDisable = false
                view.buttonStartStopTest.text = "Старт"
                view.buttonStartStopTest.isDisable = false
                view.buttonNextTest.isDisable = false
            }
            if (controller.auto) {
                view.startNextExperiment()
            }
        }
    }

    private fun ansh2() {
        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.stopSerialMeasuring()
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(0.0)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(2)
            rele1.on(3)
            rele1.on(5)
            rele1.on(6)
            view.progressBarTime.progress = 0.2
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceCoil1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceCoil1.value = "Обрыв"
            }
            view.progressBarTime.progress = 0.5
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(2)
            rele1.off(3)
            rele1.off(5)
            rele1.off(6)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(8)
            rele1.on(9)
            rele1.on(11)
            rele1.on(12)
            view.progressBarTime.progress = 0.7
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceCoil2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceCoil2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(8)
            rele1.off(9)
            rele1.off(11)
            rele1.off(12)
            view.progressBarTime.progress = 1.0
        }

    }

    private fun nmsh1() {
        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.stopSerialMeasuring()
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(0.0)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(2)
            rele1.on(3)
            rele1.on(5)
            rele1.on(6)
            view.progressBarTime.progress = 0.2
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceCoil1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceCoil1.value = "Обрыв"
            }
            view.progressBarTime.progress = 0.5
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(2)
            rele1.off(3)
            rele1.off(5)
            rele1.off(6)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(8)
            rele1.on(9)
            rele1.on(11)
            rele1.on(12)
            view.progressBarTime.progress = 0.7
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceCoil2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceCoil2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(8)
            rele1.off(9)
            rele1.off(11)
            rele1.off(12)
            view.progressBarTime.progress = 1.0
        }
    }

    private fun nmsh2() {
        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.stopSerialMeasuring()
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(0.0)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(2)
            rele1.on(3)
            rele1.on(5)
            rele1.on(6)
            view.progressBarTime.progress = 0.2
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceCoil1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceCoil1.value = "Обрыв"
            }
            view.progressBarTime.progress = 0.5
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(2)
            rele1.off(3)
            rele1.off(5)
            rele1.off(6)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(8)
            rele1.on(9)
            rele1.on(11)
            rele1.on(12)
            view.progressBarTime.progress = 0.7
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceCoil2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceCoil2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(8)
            rele1.off(9)
            rele1.off(11)
            rele1.off(12)
            view.progressBarTime.progress = 1.0
        }
    }

    private fun nmsh3() {
        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.stopSerialMeasuring()
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(0.0)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(2)
            rele1.on(3)
            rele1.on(5)
            rele1.on(6)
            view.progressBarTime.progress = 0.2
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceCoil1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceCoil1.value = "Обрыв"
            }
            view.progressBarTime.progress = 0.5
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(2)
            rele1.off(3)
            rele1.off(5)
            rele1.off(6)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(8)
            rele1.on(9)
            rele1.on(11)
            rele1.on(12)
            view.progressBarTime.progress = 0.7
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceCoil2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceCoil2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(8)
            rele1.off(9)
            rele1.off(11)
            rele1.off(12)
            view.progressBarTime.progress = 1.0
        }
    }

    private fun nmsh4() {
        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.stopSerialMeasuring()
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(0.0)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(2)
            rele1.on(3)
            rele1.on(5)
            rele1.on(6)
            view.progressBarTime.progress = 0.2
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceCoil1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceCoil1.value = "Обрыв"
            }
            view.progressBarTime.progress = 0.5
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(2)
            rele1.off(3)
            rele1.off(5)
            rele1.off(6)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(8)
            rele1.on(9)
            rele1.on(11)
            rele1.on(12)
            view.progressBarTime.progress = 0.7
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceCoil2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceCoil2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(8)
            rele1.off(9)
            rele1.off(11)
            rele1.off(12)
            view.progressBarTime.progress = 1.0
        }
    }

    private fun rel1() {
        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.stopSerialMeasuring()
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(0.0)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(2)
            rele1.on(3)
            rele1.on(5)
            rele1.on(6)
            view.progressBarTime.progress = 0.2
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceCoil1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceCoil1.value = "Обрыв"
            }
            view.progressBarTime.progress = 0.5
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(2)
            rele1.off(3)
            rele1.off(5)
            rele1.off(6)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(8)
            rele1.on(9)
            rele1.on(11)
            rele1.on(12)
            view.progressBarTime.progress = 0.7
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceCoil2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceCoil2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(8)
            rele1.off(9)
            rele1.off(11)
            rele1.off(12)
            view.progressBarTime.progress = 1.0
        }
    }

    private fun rel2() {
        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.stopSerialMeasuring()
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(0.0)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(2)
            rele1.on(3)
            rele1.on(5)
            rele1.on(6)
            view.progressBarTime.progress = 0.2
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceCoil1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceCoil1.value = "Обрыв"
            }
            view.progressBarTime.progress = 0.5
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(2)
            rele1.off(3)
            rele1.off(5)
            rele1.off(6)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(8)
            rele1.on(9)
            rele1.on(11)
            rele1.on(12)
            view.progressBarTime.progress = 0.7
        }

        if (isExperimentRunning && isDevicesResponding()) {
            ikas1.startMeasuringAA()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138. Неизв.ошибка")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(2000)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceCoil2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceCoil2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.off(8)
            rele1.off(9)
            rele1.off(11)
            rele1.off(12)
            view.progressBarTime.progress = 1.0
        }
    }

    private fun setResultFor2() {
        when {
            cause.isNotEmpty() -> {
                tableValues[1].result.value = "Прервано"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: $cause")
            }
            !isDevicesResponding() -> {
                tableValues[1].result.value = "Прервано"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: потеряна связь с устройствами")
            }
            measuringR1 > testItemR1 * 0.8 && measuringR1 < testItemR1 * 1.2 && measuringR2 > testItemR2 * 0.8 && measuringR2 < testItemR2 * 1.2 -> {
                tableValues[1].result.value = "Годен"
                appendMessageToLog(LogTag.MESSAGE, "Результат: Успешно")
            }
            (measuringR1 < testItemR1 * 0.8 || measuringR1 > testItemR1 * 1.2) && (measuringR2 < testItemR2 * 0.8 || measuringR2 > testItemR2 * 1.2) -> {
                tableValues[1].result.value = "Не годен"
                appendMessageToLog(
                    LogTag.ERROR, "Результат: Сопротивления катушек отличаются более, чем на 20%"
                )
            }
            measuringR1 < testItemR1 * 0.8 || measuringR1 > testItemR1 * 1.2 -> {
                tableValues[1].result.value = "Не годен"
                appendMessageToLog(
                    LogTag.ERROR, "Результат: Сопротивление первой катушки отличается более, чем на 20%"
                )
            }
            measuringR2 < testItemR2 * 0.8 || measuringR2 > testItemR2 * 1.2 -> {
                tableValues[1].result.value = "Не годен"
                appendMessageToLog(
                    LogTag.ERROR, "Результат: Сопротивление второй катушки отличается более, чем на 20%"
                )
            }
            measuringR1 == BREAK_IKAS && measuringR2 == BREAK_IKAS -> {
                tableValues[1].result.value = "Не годен"
                appendMessageToLog(
                    LogTag.ERROR, "Результат: Обрыв катушек"
                )
            }
            measuringR1 == BREAK_IKAS -> {
                tableValues[1].result.value = "Не годен"
                appendMessageToLog(
                    LogTag.ERROR, "Результат: Обрыв первой катушки"
                )
            }
            measuringR2 == BREAK_IKAS -> {
                tableValues[1].result.value = "Не годен"
                appendMessageToLog(
                    LogTag.ERROR, "Результат: Обрыв второй катушки"
                )
            }
            else -> {
                tableValues[1].result.value = "Неизвестно"
                appendMessageToLog(
                    LogTag.ERROR, "Результат: Неизвестно"
                )
            }
        }
    }

    private fun setResultFor1() {
        if (cause.isNotEmpty()) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(LogTag.ERROR, "Причина: $cause")
        } else if (measuringR1 > testItemR1 * 0.8 && measuringR1 < testItemR1 * 1.2) {
            tableValues[1].result.value = "Годен"
            appendMessageToLog(LogTag.MESSAGE, "Результат: Успешно")
        } else if (measuringR1 < testItemR1 * 0.8 || measuringR1 > testItemR1 * 1.2) {
            tableValues[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Сопротивление первой катушки отличается более, чем на 20%"
            )
        } else if (measuringR1 == BREAK_IKAS) {
            tableValues[1].result.value = "Не годен"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Обрыв первой катушки"
            )
        } else {
            tableValues[1].result.value = "Неизвестно"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Неизвестно"
            )
        }
    }
}
