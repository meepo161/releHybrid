package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.devices.avem.ikas.Ikas8Model
import ru.avem.rele.entities.TableValuesTest1
import ru.avem.rele.utils.BREAK_IKAS
import ru.avem.rele.utils.LogTag
import ru.avem.rele.utils.Singleton.currentTestItem
import ru.avem.rele.utils.State
import ru.avem.rele.utils.formatRealNumber
import ru.avem.rele.view.Test1View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableList
import tornadofx.style
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test1Controller : TestController() {
    private lateinit var factoryNumber: String
    val view: Test1View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableList(
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
    private var isIkasResponding: Boolean = false

    @Volatile
    var isExperimentRunning: Boolean = false

    @Volatile
    var isExperimentEnded: Boolean = true

    @Volatile
    private var ikasReadyParam: Float = 0f

    @Volatile
    private var measuringR: Float = 0f

    @Volatile
    private var statusIkas: Float = 0f

    @Volatile
    private var testItemR: Double = 0.0

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
        tableValues[0].resistanceCoil1.value = currentTestItem.resistanceCoil
        tableValues[0].resistanceCoil2.value = currentTestItem.resistanceCoil
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

    fun startTest() {
        thread {
            cause = ""
            testItemR = currentTestItem.resistanceCoil.toDouble()

            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
            }

            startPollDevices()
            isExperimentRunning = true
            isExperimentEnded = false

            clearLog()
            clearTable()

            appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
            appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")

            while (!isDevicesResponding() && isExperimentRunning) {
                CommunicationModel.checkDevices()
                sleep(100)
            }

            if (isExperimentRunning && isDevicesResponding()) {
                ikas1.stopSerialMeasuring()
                idcGV1.remoteControl()
                idcGV1.offVoltage()
                idcGV1.setVoltage(24.0)
                idcGV1.setMaxCurrent(1.0)
                offAllRele()
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendMessageToLog(LogTag.DEBUG, "Идет сбор схемы")
                rele1.on(2)
                rele1.on(3)
                rele1.on(5)
                rele1.on(6)
                view.progressBarTime.progress = 0.2

            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления первой обмотки катушки")
                ikas1.startMeasuringAA()
                appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока 1 измерение закончится")
            }

            while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
                sleep(100)
                if (statusIkas == 138f) {
                    setCause("Ошибка 138. Неизв.ошибка")
                }
            }

            if (isExperimentRunning && isDevicesResponding()) {
                sleep(2000)
                appendMessageToLog(LogTag.MESSAGE, "Измерение первой обмотки катушки реле завершено")
                measuringR1 = formatRealNumber(measuringR.toDouble())
                if (measuringR1 != BREAK_IKAS) {
                    tableValues[1].resistanceCoil1.value = measuringR1.toString()
                } else {
                    tableValues[1].resistanceCoil1.value = "Обрыв"
                }
                view.progressBarTime.progress = 0.5
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendMessageToLog(LogTag.DEBUG, "Идет разбор схемы для первой обмотки катушки")
                rele1.off(2)
                rele1.off(3)
                rele1.off(5)
                rele1.off(6)
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendMessageToLog(LogTag.DEBUG, "Идет сбор схемы для второй обмотки катушки")
                rele1.on(8)
                rele1.on(9)
                rele1.on(11)
                rele1.on(12)
                view.progressBarTime.progress = 0.7
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления катушки 2")
                ikas1.startMeasuringAA()
                appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока 2 измерение закончится")
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
                appendMessageToLog(LogTag.MESSAGE, "Измерение обмотки второй катушки реле завершено")
                if (measuringR2 != BREAK_IKAS) {
                    tableValues[1].resistanceCoil2.value = measuringR2.toString()
                } else {
                    tableValues[1].resistanceCoil2.value = "Обрыв"
                }
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendMessageToLog(LogTag.DEBUG, "Идет разбор схемы для второй обмотки катушки")
                rele1.off(8)
                rele1.off(9)
                rele1.off(11)
                rele1.off(12)
                view.progressBarTime.progress = 1.0
            }

            setResult()
            appendMessageToLog(LogTag.DEBUG, "Испытание завершено")

            controller.tableValuesTest1[0].resistanceCoil1.value = tableValues[0].resistanceCoil1.value
            controller.tableValuesTest1[0].resistanceCoil2.value = tableValues[0].resistanceCoil2.value
            controller.tableValuesTest1[1].resistanceCoil1.value = tableValues[1].resistanceCoil1.value
            controller.tableValuesTest1[1].resistanceCoil2.value = tableValues[1].resistanceCoil2.value
            controller.tableValuesTest1[1].result.value = tableValues[1].result.value

            isExperimentRunning = false
            isExperimentEnded = true
            CommunicationModel.clearPollingRegisters()

            Platform.runLater {
                view.buttonBack.isDisable = false
                view.buttonStartStopTest.text = "Старт"
                view.buttonStartStopTest.isDisable = false
                view.buttonNextTest.isDisable = false
            }
        }
    }

    private fun setResult() {
        if (cause.isNotEmpty()) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(LogTag.ERROR, "Причина: $cause")
        } else if (measuringR1 > testItemR * 0.8 && measuringR1 < testItemR * 1.2 && measuringR2 > testItemR * 0.8 && measuringR2 < testItemR * 1.2) {
            tableValues[1].result.value = "Успешно"
            appendMessageToLog(LogTag.MESSAGE, "Результат: Успешно")
        } else if ((measuringR1 < testItemR * 0.8 || measuringR1 > testItemR * 1.2) && (measuringR2 < testItemR * 0.8 || measuringR2 > testItemR * 1.2)) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Сопротивления катушек отличаются более, чем на 20%"
            )
        } else if (measuringR1 < testItemR * 0.8 || measuringR1 > testItemR * 1.2) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Сопротивление первой катушки отличается более, чем на 20%"
            )
        } else if (measuringR2 < testItemR * 0.8 || measuringR2 > testItemR * 1.2) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Сопротивление второй катушки отличается более, чем на 20%"
            )
        } else if (measuringR1 == BREAK_IKAS && measuringR2 == BREAK_IKAS) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Обрыв катушек"
            )
        } else if (measuringR1 == BREAK_IKAS) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Обрыв первой катушки"
            )
        } else if (measuringR2 == BREAK_IKAS) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Обрыв второй катушки"
            )
        } else {
            tableValues[1].result.value = "Неизвестно"
            appendMessageToLog(
                LogTag.ERROR, "Результат: Неизвестно"
            )
        }
    }
}
