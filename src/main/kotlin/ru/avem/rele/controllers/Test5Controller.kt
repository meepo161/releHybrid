package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.rele.entities.TableValuesTest5
import ru.avem.rele.utils.LogTag
import ru.avem.rele.utils.Singleton
import ru.avem.rele.utils.State
import ru.avem.rele.utils.formatRealNumber
import ru.avem.rele.view.Test5View
import tornadofx.*
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test5Controller : TestController() {

    val view: Test5View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableListOf(
        TableValuesTest5(
            SimpleStringProperty("Заданные"),
            SimpleDoubleProperty(0.0),
            SimpleStringProperty("")
        ),

        TableValuesTest5(
            SimpleStringProperty("Измеренные"),
            SimpleDoubleProperty(0.0),
            SimpleStringProperty("")
        )
    )


    @Volatile
    var isExperimentRunning: Boolean = false

    @Volatile
    var isExperimentEnded: Boolean = true

    @Volatile
    var measuringU: Double = 0.0

    fun clearTable() {
        tableValues.forEach {
            it.voltage.value = 0.0
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
        tableValues[0].voltage.value = Singleton.currentTestItem.voltageMax.toDouble()
    }

    fun setExperimentProgress(currentTime: Int, time: Int = 1) {
        Platform.runLater {
            view.progressBarTime.progress = currentTime.toDouble() / time
        }
    }

    fun appendMessageToLog(tag: LogTag, _msg: String) {
        val msg = Text("${SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())} | $_msg")
        msg.style {
            fill =
                when (tag) {
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
//        return ikasPR1.isResponding
        return true
    }

    fun setCause(cause: String) {
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
        view.buttonStartStopTest.isDisable = true
    }

    private fun startPollDevices() {
        CommunicationModel.startPoll(CommunicationModel.DeviceID.AVEM41, Avem4Model.RMS_VOLTAGE) { value ->
            measuringU = value.toDouble()
        }
//        CommunicationModel.startPoll(CommunicationModel.DeviceID.PR1, Ikas8Model.RESIST_MEAS) { value ->
//            measuringR = value.toFloat()
//        }
    }

    fun startTest() {
        thread(isDaemon = true) {

            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
            }
            var maxU = Singleton.currentTestItem.voltageMax.toDouble()
//            startPollDevices()
            isExperimentRunning = true
            isExperimentEnded = false

            clearLog()
            clearTable()
            appendMessageToLog(LogTag.DEBUG, "Инициализация")
            sleep(1000)
            appendMessageToLog(LogTag.DEBUG, "Сбор схемы")

            if (isExperimentRunning && isDevicesResponding()) {
                idcGV1.remoteControl()
                idcGV1.offVoltage()
                idcGV1.setVoltage(maxU)
                idcGV1.setMaxCurrent(1.0)
                offAllRele()
            }

            if (isExperimentRunning && isDevicesResponding()) {
                rele1.on(1)
                rele1.on(4)
                rele1.on(17)
                rele1.on(18)
                rele1.on(19)
                rele1.on(20)
                rele1.on(25)
                rele1.on(26)
                rele1.on(27)
                rele3.on(5)
                rele3.on(6)
                rele2.on(1)
                rele2.on(2)
                rele3.on(10)
            }
            if (isExperimentRunning && isDevicesResponding()) {
                idcGV1.onVoltage()
                sleep(4000)
            }

            val avemU = measuringU

            while (avem4.getRMSVoltage() > 3.0) {
                maxU -= 0.1
                idcGV1.setVoltage(maxU)
                tableValues[1].voltage.value = formatRealNumber(maxU)
                sleep(100)
                if (maxU < 0.1) {
                    break
                }
            }

            appendMessageToLog(LogTag.MESSAGE, "Напряжение размыкания: ${formatRealNumber(maxU)}")

            appendMessageToLog(LogTag.DEBUG, "Испытание завершено")
            controller.tableValuesTest5[0].voltage.value = tableValues[0].voltage.value
            controller.tableValuesTest5[1].voltage.value = tableValues[1].voltage.value
            controller.tableValuesTest5[1].result.value = tableValues[1].result.value
            setResult()

            idcGV1.offVoltage()
            offAllRele()
//            CommunicationModel.clearPollingRegisters()
            isExperimentRunning = false
            isExperimentEnded = true

            Platform.runLater {
                view.buttonBack.isDisable = false
                view.buttonStartStopTest.text = "Старт"
                view.buttonStartStopTest.isDisable = false
                view.buttonNextTest.isDisable = false
            }
        }
    }

    private fun setResult() {
        if (tableValues[1].voltage.value > tableValues[0].voltage.value) {
            appendMessageToLog(LogTag.ERROR, "Напряжение срабатывания больше заданного")
            tableValues[1].result.value = "Не успешно"
        } else {
            appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
            tableValues[1].result.value = "Успешно"
        }
    }
}