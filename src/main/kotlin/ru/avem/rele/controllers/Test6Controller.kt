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
import ru.avem.rele.utils.*
import ru.avem.rele.view.MainView
import ru.avem.rele.view.Test6View
import tornadofx.*
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test6Controller : TestController() {
    val view: Test6View by inject()
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    var tableValues = observableListOf(
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

    @Volatile
    var testItemSerial: String = ""

    @Volatile
    var testItemVoltageNom: Double = 0.0

    @Volatile
    var testItemCurrentOverload: Double = 0.0

    @Volatile
    var testItemCurrentMax: Double = 0.0

    @Volatile
    var testItemResistanceCoil: Double = 0.0

    @Volatile
    private var testItemVoltageOrCurrent: String = ""

    @Volatile
    var cause: String = ""
        set(value) {
            if (value != "") {
                isExperimentRunning = false
            }
            field = value
        }

    fun clearTable() {
        tableValues.forEach {
            it.time.value = 0.0
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
        tableValues[0].time.value = Singleton.currentTestItem.timeOff.replace(",", ".").toDouble()
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
        if (avem4.isResponding) {
            view.circleComStatus.fill = State.OK.c
        } else {
            view.circleComStatus.fill = State.BAD.c
        }
        return avem4.isResponding
    }

    init {
    }

    fun startTest() {
        thread(isDaemon = true) {
            sleep(400)
            cause = ""
            testItemVoltageNom = Singleton.currentTestItem.voltageOrCurrentNom.replace(",", ".").toDouble()
            testItemSerial = Singleton.currentTestItem.serialNumber
            testItemVoltageOrCurrent = Singleton.currentTestItem.voltageOrCurrent
            testItemResistanceCoil = Singleton.currentTestItem.resistanceCoil1.replace(",", ".").toDouble()
            testItemCurrentOverload = Singleton.currentTestItem.voltageOrCurrentOverload.replace(",", ".").toDouble()
            testItemCurrentMax = Singleton.currentTestItem.voltageOrCurrentMax.replace(",", ".").toDouble()
            clearLog()
            clearTable()

            appendMessageToLog(LogTag.MESSAGE, "Начало испытания")

            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
                view.series.data.clear()
            }

//            startPollDevices()
            isExperimentRunning = true
            isExperimentEnded = false

            appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
            if (CommunicationModel.checkDevices().isNotEmpty()) {
                cause = "Не отвечают : ${CommunicationModel.checkDevices()}"
            }
            while (!isDevicesResponding() && isExperimentRunning) {
                CommunicationModel.checkDevices()
                sleep(100)
            }
            appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")
            appendMessageToLog(LogTag.DEBUG, "Ожидание...")

            when (Singleton.currentTestItemType) {
                "НМШ1" -> {
                    if (testItemVoltageOrCurrent == "Напряжение") {
                        nmsh1()
                    } else if (testItemVoltageOrCurrent == "Ток") {
                        nmsh1i()
                    }
                }
                "НМШ2" -> {
                    if (testItemVoltageOrCurrent == "Напряжение") {
                        nmsh2()
                    } else if (testItemVoltageOrCurrent == "Ток") {
                        nmsh2i()
                    }
                }
                "НМШ3" -> {
                    if (testItemVoltageOrCurrent == "Напряжение") {
                    } else if (testItemVoltageOrCurrent == "Ток") {
                        nmsh3i()
                    }
                }
                "НМШ4" -> {
                    if (testItemVoltageOrCurrent == "Напряжение") {
                        nmsh4()
                    } else if (testItemVoltageOrCurrent == "Ток") {
                        nmsh4i()
                    }
                }
                "АНШ2" -> {
                    if (testItemVoltageOrCurrent == "Напряжение") {
                        ansh2()
                    } else if (testItemVoltageOrCurrent == "Ток") {
                        ansh2i()
                    }
                }
                "РЭЛ1" -> {
                    if (testItemVoltageOrCurrent == "Напряжение") {
                        rel1()
                    } else if (testItemVoltageOrCurrent == "Ток") {
                        rel1i()
                    }
                }
                "РЭЛ2" -> {
                    if (testItemVoltageOrCurrent == "Напряжение") {
                        rel2()
                    } else if (testItemVoltageOrCurrent == "Ток") {
                        rel2i()
                    }

                }
                else -> {
                    Toast.makeText("Ошибка, нет такого типа объекта испытания").show(Toast.ToastType.ERROR)
                }
            }

            setResult()

            avem4.startChart(0)
            idcGV1.offVoltage()
            offAllRele()
//            CommunicationModel.clearPollingRegisters()
            isExperimentRunning = false
            isExperimentEnded = true

            controller.tableValuesTest6[0].time.value = tableValues[0].time.value
            controller.tableValuesTest6[1].time.value = tableValues[1].time.value
            controller.tableValuesTest6[1].result.value = tableValues[1].result.value

            Platform.runLater {
                view.buttonBack.isDisable = false
                view.buttonStartStopTest.text = "Старт"
                view.buttonStartStopTest.isDisable = false
                view.buttonNextTest.isDisable = false
            }
//
            if (controller.auto) {
                view.startNextExperiment()
            }
        }
    }


    private fun ansh2() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            avem4.startChart(0)
            avem4.setChartTime(240)
            rele1.on(1)
            rele1.on(4)
            rele3.on(5)
            rele3.on(6)
            rele3.on(9)
            rele1.on(18)
            rele3.on(8)
            rele1.on(20)
            rele1.on(15)
            rele1.on(26)
            rele1.on(31)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun ansh2i() {
        runLater {
            mainView.tableView6Result.columns[1].text = "I, А"
            view.tableView6Test.columns[1].text = "I, А"
        }
        val maxU = testItemResistanceCoil * testItemCurrentOverload
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            avem4.startChart(0)
            avem4.setChartTime(240)
            rele1.on(1)
            rele1.on(4)
            rele3.on(5)
            rele3.on(6)
            rele3.on(9)
            rele1.on(18)
            rele3.on(8)
            rele1.on(20)
            rele1.on(15)
            rele1.on(26)
            rele1.on(31)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun nmsh1() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun nmsh1i() {
        runLater {
            mainView.tableView6Result.columns[1].text = "I, А"
            view.tableView6Test.columns[1].text = "I, А"
        }
        val maxU = testItemResistanceCoil * testItemCurrentOverload
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            avem4.startChart(0)
            avem4.setChartTime(240)
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

            rele1.on(2)
            rele1.on(10)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun nmsh2() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
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
            rele3.on(7)
            rele1.on(19)
            rele1.on(20)
            rele1.on(14)
            rele1.on(25)
            rele1.on(26)
            rele1.on(32)
            rele1.on(27)
            rele2.on(1)
            rele2.on(2)
            rele3.on(6)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun nmsh2i() {
        runLater {
            mainView.tableView6Result.columns[1].text = "I, А"
            view.tableView6Test.columns[1].text = "I, А"
        }
        val maxU = testItemResistanceCoil * testItemCurrentOverload
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            avem4.startChart(0)
            avem4.setChartTime(240)
            rele1.on(1)
            rele1.on(4)
            rele1.on(17)
            rele1.on(18)
            rele3.on(7)
            rele1.on(19)
            rele1.on(20)
            rele1.on(14)
            rele1.on(25)
            rele1.on(26)
            rele1.on(32)
            rele1.on(27)
            rele2.on(1)
            rele2.on(2)
            rele3.on(6)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun nmsh3i() {
        runLater {
            mainView.tableView6Result.columns[1].text = "I, А"
            view.tableView6Test.columns[1].text = "I, А"
        }
        val maxU = testItemResistanceCoil * testItemCurrentOverload
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            avem4.startChart(0)
            avem4.setChartTime(240)
            rele1.on(1)
            rele1.on(4)
            rele1.on(17)
            rele1.on(18)
            rele3.on(7)
            rele1.on(19)
            rele1.on(20)
            rele1.on(14)
            rele1.on(25)
            rele1.on(26)
            rele1.on(32)
            rele1.on(27)
            rele2.on(1)
            rele2.on(2)
            rele3.on(6)
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
        if (secondDot != 0) {
            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }

        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun nmsh4() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun nmsh4i() {
        runLater {
            mainView.tableView6Result.columns[1].text = "I, А"
            view.tableView6Test.columns[1].text = "I, А"
        }
        val maxU = testItemResistanceCoil * testItemCurrentOverload
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun rel1() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun rel1i() {
        runLater {
            mainView.tableView6Result.columns[1].text = "I, А"
            view.tableView6Test.columns[1].text = "I, А"
        }
        val maxU = testItemResistanceCoil * testItemCurrentOverload
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun rel2() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            avem4.startChart(0)
            avem4.setChartTime(240)
            rele1.on(1)
            rele1.on(4)
            rele3.on(9)
            rele1.on(18)
            rele1.on(19)
            rele1.on(20)
            rele1.on(25)
            rele1.on(26)
            rele1.on(32)
            rele1.on(31)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun rel2i() {
        runLater {
            mainView.tableView6Result.columns[1].text = "I, А"
            view.tableView6Test.columns[1].text = "I, А"
        }
        val maxU = testItemResistanceCoil * testItemCurrentOverload
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            avem4.startChart(0)
            avem4.setChartTime(240)
            rele1.on(1)
            rele1.on(4)
            rele3.on(9)
            rele1.on(18)
            rele1.on(19)
            rele1.on(20)
            rele1.on(25)
            rele1.on(26)
            rele1.on(32)
            rele1.on(31)
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
        if (secondDot != 0) {

            appendMessageToLog(LogTag.MESSAGE, "Время размыкания: ${formatRealNumber(timeDisconnection)} мс")
        } else {
            cause = "Не удалось определить время размыкания"
        }


        
        tableValues[1].time.value = formatRealNumber(timeDisconnection / 1000)
    }

    private fun setResult() {
        when {
            cause.isNotEmpty() -> {
                tableValues[1].result.value = "Прервано"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: $cause")
            }
            !isDevicesResponding() -> {
                controller.tableValuesTest1[1].result.value = "Прервано"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: потеряна связь с устройствами")
            }
            tableValues[1].time.value > tableValues[0].time.value -> {
                appendMessageToLog(LogTag.ERROR, "Измеренное значение времени больше заданного")
                tableValues[1].result.value = "Не успешно"
            }
            else -> {
                appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
                tableValues[1].result.value = "Годен"
            }
        }
    }
}