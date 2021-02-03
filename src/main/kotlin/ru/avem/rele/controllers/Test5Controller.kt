package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.entities.TableValuesTest5
import ru.avem.rele.utils.*
import ru.avem.rele.view.MainView
import ru.avem.rele.view.Test5View
import tornadofx.*
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test5Controller : TestController() {

    val view: Test5View by inject()
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

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
    var testItemVoltageMax: Double = 0.0

    @Volatile
    var testItemVoltagetOverload: Double = 0.0

    @Volatile
    var testItemVoltageMin: Double = 0.0

    @Volatile
    var testItemCurrentOverload: Double = 0.0

    @Volatile
    var testItemCurrentMax: Double = 0.0

    @Volatile
    var testItemResistanceCoil: Double = 0.0

    @Volatile
    private var testItemSerial: String = ""

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
            it.voltage.value = 0.0
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
        tableValues[0].voltage.value = Singleton.currentTestItem.voltageOrCurrentMax.replace(",", ".").toDouble()
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
            testItemVoltageMax = Singleton.currentTestItem.voltageOrCurrentMax.replace(",", ".").toDouble()
            testItemVoltagetOverload = Singleton.currentTestItem.voltageOrCurrentOverload.replace(",", ".").toDouble()
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
            }
//            startPollDevices()
            isExperimentRunning = true
            isExperimentEnded = false

            appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
            CommunicationModel.checkDevices()
            var timeToStart = 300
            while (isExperimentRunning && !isDevicesResponding() && timeToStart-- > 0) {
                sleep(100)
            }

            if (!isDevicesResponding()) {
                cause = "Приборы не отвечают"
            }
            view.progressBarTime.progress = 0.2
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

            appendMessageToLog(LogTag.DEBUG, "Испытание завершено")
            setResult()

            view.progressBarTime.progress = 1.0
            idcGV1.offVoltage()
            offAllRele()
//            CommunicationModel.clearPollingRegisters()
            isExperimentRunning = false
            isExperimentEnded = true

            controller.tableValuesTest5[0].voltage.value = tableValues[0].voltage.value
            controller.tableValuesTest5[1].voltage.value = tableValues[1].voltage.value
            controller.tableValuesTest5[1].result.value = tableValues[1].result.value

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
        var maxU = testItemVoltagetOverload
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
            rele3.on(5)
            rele3.on(6)
            rele3.on(10)
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
            sleep(6000)
        }



        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemVoltagetOverload / 100
            idcGV1.setVoltage(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemVoltageMax * 1.2) {
                cause = "Напряжение размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Напряжение размыкания: ${formatRealNumber(maxU)}")
    }

    private fun ansh2i() {
        runLater {
            mainView.tableView5Result.columns[1].text = "I, А"
            view.tableView5Test.columns[1].text = "I, А"
        }
        var maxU = testItemResistanceCoil * testItemCurrentOverload

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(1)
            rele1.on(4)
            rele3.on(5)
            rele3.on(6)
            rele3.on(10)
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
            sleep(6000)
        }

        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemCurrentOverload / 100
            idcGV1.setMaxCurrent(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemCurrentMax * 0.8) {
                cause = "Ток размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Ток размыкания: ${formatRealNumber(maxU)}")
    }

    private fun nmsh1() {
        var maxU = testItemVoltagetOverload
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
            rele1.on(2)
            rele1.on(10)
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
            sleep(6000)
        }



        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemVoltagetOverload / 100
            idcGV1.setVoltage(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemVoltageMax * 0.8) {
                cause = "Напряжение размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Напряжение размыкания: ${formatRealNumber(maxU)}")
    }

    private fun nmsh1i() {

        runLater {
            mainView.tableView5Result.columns[1].text = "I, А"
            view.tableView5Test.columns[1].text = "I, А"
        }
        var maxU = testItemResistanceCoil * testItemCurrentOverload

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(1)
            rele1.on(4)
            rele3.on(5)
            rele3.on(6)
            rele3.on(10)
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
            sleep(6000)
        }



        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemCurrentOverload / 100
            idcGV1.setMaxCurrent(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemCurrentMax * 0.8) {
                cause = "Ток размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Ток размыкания: ${formatRealNumber(maxU)}")
    }

    private fun nmsh4() {
        var maxU = testItemVoltagetOverload
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
            sleep(6000)
        }

        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemVoltagetOverload / 100
            idcGV1.setVoltage(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemVoltageMax * 0.8) {
                cause = "Напряжение размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Напряжение размыкания: ${formatRealNumber(maxU)}")
    }

    private fun nmsh4i() {

        runLater {
            mainView.tableView5Result.columns[1].text = "I, А"
            view.tableView5Test.columns[1].text = "I, А"
        }
        var maxU = testItemResistanceCoil * testItemCurrentOverload

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
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
            sleep(6000)
        }



        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemCurrentOverload / 100
            idcGV1.setMaxCurrent(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemCurrentMax * 0.8) {
                cause = "Ток размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Ток размыкания: ${formatRealNumber(maxU)}")
    }

    private fun nmsh2() {
        var maxU = testItemVoltagetOverload
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
            rele3.on(10)
        }


        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.onVoltage()
            sleep(6000)
        }



        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemVoltagetOverload / 100
            idcGV1.setVoltage(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemVoltageMax * 0.8) {
                cause = "Напряжение размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Напряжение размыкания: ${formatRealNumber(maxU)}")


    }

    private fun nmsh2i() {

        runLater {
            mainView.tableView5Result.columns[1].text = "I, А"
            view.tableView5Test.columns[1].text = "I, А"
        }
        var maxU = testItemResistanceCoil * testItemCurrentOverload

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
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
            rele3.on(10)
        }


        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.onVoltage()
            sleep(6000)
        }



        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemCurrentOverload / 100
            idcGV1.setMaxCurrent(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemCurrentMax * 0.8) {
                cause = "Ток размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Ток размыкания: ${formatRealNumber(maxU)}")
    }

    private fun nmsh3i() {

        runLater {
            mainView.tableView5Result.columns[1].text = "I, А"
            view.tableView5Test.columns[1].text = "I, А"
        }
        var maxU = testItemResistanceCoil * testItemCurrentOverload

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
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
            rele3.on(10)
        }


        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.onVoltage()
            sleep(6000)
        }



        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemCurrentOverload / 100
            idcGV1.setMaxCurrent(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemCurrentMax * 0.8) {
                cause = "Ток размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Ток размыкания: ${formatRealNumber(maxU)}")
    }

    private fun rel1() {
        var maxU = testItemVoltagetOverload
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
            sleep(6000)
        }

        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemVoltagetOverload / 100
            idcGV1.setVoltage(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemVoltageMax * 0.8) {
                cause = "Напряжение размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Напряжение размыкания: ${formatRealNumber(maxU)}")
    }

    private fun rel1i() {

        runLater {
            mainView.tableView5Result.columns[1].text = "I, А"
            view.tableView5Test.columns[1].text = "I, А"
        }
        var maxU = testItemResistanceCoil * testItemCurrentOverload

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
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
            sleep(6000)
        }





        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemCurrentOverload / 100
            idcGV1.setMaxCurrent(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemCurrentMax * 0.8) {
                cause = "Ток размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Ток размыкания: ${formatRealNumber(maxU)}")

    }

    private fun rel2() {
        var maxU = testItemVoltagetOverload
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
            rele3.on(10)
        }


        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.onVoltage()
            sleep(6000)
        }

        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemVoltagetOverload / 100
            idcGV1.setVoltage(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemVoltageMax * 0.8) {
                cause = "Напряжение размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Напряжение размыкания: ${formatRealNumber(maxU)}")
    }

    private fun rel2i() {

        runLater {
            mainView.tableView5Result.columns[1].text = "I, А"
            view.tableView5Test.columns[1].text = "I, А"
        }
        var maxU = testItemResistanceCoil * testItemCurrentOverload

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(maxU)
            idcGV1.setMaxCurrent(testItemCurrentOverload)
            offAllRele()
        }

        if (isExperimentRunning && isDevicesResponding()) {
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
            rele3.on(10)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.onVoltage()
            sleep(6000)
        }

        while (avem4.getRMSVoltage() > 3.0 && isExperimentRunning && isDevicesResponding()) {
            maxU -= testItemCurrentOverload / 100
            idcGV1.setMaxCurrent(maxU)
            tableValues[1].voltage.value = formatRealNumber(maxU)
            sleep(300)
            if (maxU < testItemCurrentMax * 0.8) {
                cause = "Ток размыкания больше заданного более, чем на 20%"
            }
        }

        appendMessageToLog(LogTag.MESSAGE, "Ток размыкания: ${formatRealNumber(maxU)}")

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
            tableValues[1].voltage.value * 0.8 > tableValues[0].voltage.value -> {
                appendMessageToLog(LogTag.ERROR, "Напряжение или ток отпускания больше заданного")
                tableValues[1].result.value = "Не годен"
            }
            else -> {
                appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
                tableValues[1].result.value = "Годен"
            }
        }
    }
}