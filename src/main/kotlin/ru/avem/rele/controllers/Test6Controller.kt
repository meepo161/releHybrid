package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.chart.XYChart
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.devices.avem.avem4.Avem4Model.Companion.GET_CHART_TIME
import ru.avem.rele.communication.model.devices.avem.avem4.Avem4Model.Companion.STATE_CHART
import ru.avem.rele.entities.TableValuesTest6
import ru.avem.rele.utils.LogTag
import ru.avem.rele.utils.Singleton
import ru.avem.rele.utils.State
import ru.avem.rele.utils.formatRealNumber
import ru.avem.rele.view.Test6View
import tornadofx.*
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread
import kotlin.math.sqrt

class Test6Controller : TestController() {

    private lateinit var factoryNumber: String
    val view: Test6View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableList(
        TableValuesTest6(
            SimpleStringProperty("Заданные"),
            SimpleDoubleProperty(0.0),
            SimpleStringProperty("")
        ),

        TableValuesTest6(
            SimpleStringProperty("Измеренные"),
            SimpleDoubleProperty(0.0),
            SimpleStringProperty("")
        )
    )

    @Volatile
    var isExperimentRunning: Boolean = false

    @Volatile
    var isExperimentEnded: Boolean = true

    fun clearTable() {
        tableValues.forEach {
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
        tableValues[0].time.value = Singleton.currentTestItem.timeOff.toDouble()
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
//        CommunicationModel.startPoll(CommunicationModel.DeviceID.PR1, Ikas8Model.STATUS) { value ->
//            statusIkas = value.toFloat()
//        }
//        CommunicationModel.startPoll(CommunicationModel.DeviceID.PR1, Ikas8Model.RESIST_MEAS) { value ->
//            measuringR = value.toFloat()
//        }
    }

    fun startTest() {
        thread(isDaemon = true) {
//            while (true) {

            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
                view.series.data.clear()
            }

//            startPollDevices()
            isExperimentRunning = true
            isExperimentEnded = false

            clearLog()
            clearTable()
            appendMessageToLog(LogTag.DEBUG, "Инициализация")
            sleep(1000)
            appendMessageToLog(LogTag.DEBUG, "Сбор схемы")

//            offAllRele()
//            releDD2.on(1")
//            releDD2.on(2")
//            releDD3.on(10")
//
//            avem4.startChart(0)
//            sleep(50)
//            avem4.setChartTime(5000)
//            sleep(50)
//            avem4.startChart(1)
//            appendMessageToLog(LogTag.DEBUG, "Сбор точек")
//            do {
//                val s = avem4.getRegisterById(STATE_CHART)
//                avem4.readRegister(s)
//                val stateValue = s.value
//                sleep(100)
//                appendMessageToLog(LogTag.DEBUG, "Статус = $stateValue")
//            } while (stateValue != 3.toShort() && isExperimentRunning)
//
//
//            val listOfDots = avem4.readChartPoints()
//            var realTime = 0.0
//
//            runLater {
//                try {
//                    for (element in listOfDots) {
//                        view.series.data.add(XYChart.Data(realTime++, element))
//                    }
//                } catch (e: Exception) {
//                }
//            }

            if (isExperimentRunning && isDevicesResponding()) {
                idcGV1.remoteControl()
                idcGV1.offVoltage()
                idcGV1.setVoltage(24.0)
                idcGV1.setMaxCurrent(1.0)
                offAllRele()
            }

            if (isExperimentRunning && isDevicesResponding()) {
                avem4.startChart(0)
                avem4.setChartTime(240)
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
            }

            if (isExperimentRunning && isDevicesResponding()) {
                idcGV1.onVoltage()
            }

            if (isExperimentRunning && isDevicesResponding()) {
                rele3.on(10)
                sleep(2000)
            }

            if (isExperimentRunning && isDevicesResponding()) {
                avem4.startChart(1)
                sleep(50)
            }

            if (isExperimentRunning && isDevicesResponding()) {
                rele3.off(10)
            }

            do {
                val s = avem4.getRegisterById(STATE_CHART)
                avem4.readRegister(s)
                val stateValue = s.value
                sleep(50)
//                appendMessageToLog(LogTag.DEBUG, "Статус = $stateValue")
            } while (stateValue != 3.toShort() && isExperimentRunning)

            var listOfDots: List<Float>
            do {
                listOfDots = avem4.readChartPoints()
            } while (listOfDots.size < 3999)

            var realTime = 0.0

            runLater {
                try {
                    for (element in listOfDots) {
                        view.series.data.add(XYChart.Data(realTime++, element))
                    }
                } catch (e: Exception) {
                }
            }

            var firstDot = 0
            var secondDot = 0
            var isFirstDotFinded = false
            var isSecondDotFinded = true
            for (i in 0..listOfDots.size - 2) {
                if (listOfDots[i] > (listOfDots[i + 1]) * 1.1 && !isFirstDotFinded) {
                    isFirstDotFinded = true
                    firstDot = i
                }
                if (listOfDots[i] < 0.1 && !isSecondDotFinded) {
                    secondDot = i
                    break
                }
                if (isFirstDotFinded) {
                    isSecondDotFinded = false
                }
            }

            val chartTime = avem4.getRegisterById(GET_CHART_TIME)
            avem4.readRegister(chartTime)
            val timeDisconnection = ((secondDot - firstDot) * (chartTime.value.toInt() / 4000.0))
            appendMessageToLog(LogTag.MESSAGE, "Первая точка: $firstDot")
            appendMessageToLog(LogTag.MESSAGE, "Вторая точка: $secondDot")
            appendMessageToLog(LogTag.MESSAGE, "Время измерения всех точек: ${chartTime.value.toInt()}")
            appendMessageToLog(LogTag.MESSAGE, "Время одной точки: ${chartTime.value.toInt() / 4000.0}")
            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")


            appendMessageToLog(LogTag.DEBUG, "Испытание завершено")
            tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
            controller.tableValuesTest6[0].time.value = tableValues[0].time.value
            controller.tableValuesTest6[1].time.value = tableValues[1].time.value
            controller.tableValuesTest6[1].result.value = tableValues[1].result.value
            setResult()

            avem4.startChart(0)
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
//        }
    }

    private fun setResult() {
        if (tableValues[1].time.value > tableValues[0].time.value) {
            appendMessageToLog(LogTag.ERROR, "Измеренное значение времени больше заданного")
            tableValues[1].result.value = "Не успешно"
        } else {
            appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
            tableValues[1].result.value = "Успешно"
        }
    }
}