package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.devices.avem.ikas.Ikas8Model
import ru.avem.rele.entities.TableValuesTest3
import ru.avem.rele.utils.*
import ru.avem.rele.view.MainView
import ru.avem.rele.view.Test3View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableListOf
import tornadofx.style
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test3Controller : TestController() {
    val view: Test3View by inject()
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    var tableValues = observableListOf(
        TableValuesTest3(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    @Volatile
    var isExperimentRunning: Boolean = false

    @Volatile
    var isExperimentEnded: Boolean = true

    @Volatile
    private var testItemSerial: String = ""

    @Volatile
    private var testItemVoltageOrCurrent: String = ""

    @Volatile
    private var measuringR: Float = 0f

    @Volatile
    private var statusIkas: Float = 0f

    @Volatile
    private var measuringR1: Double = 0.0

    @Volatile
    private var measuringR2: Double = 0.0

    @Volatile
    private var measuringR3: Double = 0.0

    @Volatile
    private var measuringR4: Double = 0.0

    @Volatile
    private var measuringR5: Double = 0.0

    @Volatile
    private var measuringR6: Double = 0.0

    @Volatile
    private var measuringR7: Double = 0.0

    @Volatile
    private var measuringR8: Double = 0.0

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
            it.resistanceContactGroupNC1.value = "0.0"
            it.resistanceContactGroupNC2.value = "0.0"
            it.resistanceContactGroupNC3.value = "0.0"
            it.resistanceContactGroupNC4.value = "0.0"
            it.resistanceContactGroupNC5.value = "0.0"
            it.resistanceContactGroupNC6.value = "0.0"
            it.resistanceContactGroupNC7.value = "0.0"
            it.resistanceContactGroupNC8.value = "0.0"
            it.result.value = ""
        }
        fillTableByEO()
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    fun fillTableByEO() {
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
            testItemSerial = Singleton.currentTestItem.serialNumber
            testItemVoltageOrCurrent = Singleton.currentTestItem.voltageOrCurrent
            clearLog()
            clearTable()

            appendMessageToLog(LogTag.MESSAGE, "Начало испытания")
            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
            }

            startPollDevices()
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
            appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")
            appendMessageToLog(LogTag.DEBUG, "Ожидание...")

            if (isExperimentRunning && isDevicesResponding()) {
                ikas1.stopSerialMeasuring()
                idcGV1.remoteControl()
                idcGV1.offVoltage()
                idcGV1.setVoltage(24.0)
                idcGV1.setMaxCurrent(1.0)
                offAllRele()
            }

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


            ikas1.stopSerialMeasuring()
            setResult()
            CommunicationModel.clearPollingRegisters()
            isExperimentRunning = false
            isExperimentEnded = true

            controller.tableValuesTest3[0].resistanceContactGroupNC1.value =
                tableValues[0].resistanceContactGroupNC1.value
            controller.tableValuesTest3[0].resistanceContactGroupNC2.value =
                tableValues[0].resistanceContactGroupNC2.value
            controller.tableValuesTest3[0].resistanceContactGroupNC3.value =
                tableValues[0].resistanceContactGroupNC3.value
            controller.tableValuesTest3[0].resistanceContactGroupNC4.value =
                tableValues[0].resistanceContactGroupNC4.value
            controller.tableValuesTest3[0].resistanceContactGroupNC5.value =
                tableValues[0].resistanceContactGroupNC5.value
            controller.tableValuesTest3[0].resistanceContactGroupNC6.value =
                tableValues[0].resistanceContactGroupNC6.value
            controller.tableValuesTest3[0].resistanceContactGroupNC7.value =
                tableValues[0].resistanceContactGroupNC7.value
            controller.tableValuesTest3[0].resistanceContactGroupNC8.value =
                tableValues[0].resistanceContactGroupNC8.value
            controller.tableValuesTest3[0].result.value = tableValues[0].result.value

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
        idcGV1.offVoltage()
        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(21)
            rele1.on(18)
            rele3.on(7)
            rele1.on(23)
            rele1.on(20)
            rele1.on(14)
            rele1.on(28)
            rele1.on(26)
            rele1.on(32)
            rele1.on(30)
            rele3.on(11)
            rele3.on(2)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(22)
            rele2.on(23)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC1.value = measuringR1.toString()
            } else {
                tableValues[0].resistanceContactGroupNC1.value = "-.--"
            }
            view.progressBarTime.progress = 1.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(22)
            rele2.off(23)
        }
        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroupNC2.value = "-.--"
            view.progressBarTime.progress = 2.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(28)
            rele2.on(29)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR3 = formatRealNumber(measuringR.toDouble())
            if (measuringR3 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroupNC3.value = "-.--"
            }
            view.progressBarTime.progress = 3.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(28)
            rele2.off(29)
            view.progressBarTime.progress = 4.0 / 8.0
            tableValues[0].resistanceContactGroupNC4.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(14)
            rele2.on(15)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR5 = formatRealNumber(measuringR.toDouble())
            if (measuringR5 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC5.value = measuringR5.toString()
            } else {
                tableValues[0].resistanceContactGroupNC5.value = "-.--"
            }
            view.progressBarTime.progress = 5.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(14)
            rele2.off(15)
            view.progressBarTime.progress = 6.0 / 8.0
            tableValues[0].resistanceContactGroupNC6.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(8)
            rele2.on(9)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR7 = formatRealNumber(measuringR.toDouble())
            if (measuringR7 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC7.value = measuringR7.toString()
            } else {
                tableValues[0].resistanceContactGroupNC7.value = "-.--"
            }
            view.progressBarTime.progress = 7.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(8)
            rele2.off(9)
            view.progressBarTime.progress = 8.0 / 8.0
            tableValues[0].resistanceContactGroupNC8.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            offAllRele()
        }
    }

    private fun nmsh1() {
        idcGV1.offVoltage()
        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(21)
            rele1.on(22)
            rele1.on(23)
            rele1.on(24)
            rele1.on(28)
            rele1.on(29)
            rele1.on(30)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(22)
            rele2.on(23)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC1.value = measuringR1.toString()
            } else {
                tableValues[0].resistanceContactGroupNC1.value = "-.--"
            }
            view.progressBarTime.progress = 1.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(22)
            rele2.off(23)
            rele2.on(25)
            rele2.on(26)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroupNC2.value = "-.--"
            }
            view.progressBarTime.progress = 2.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(25)
            rele2.off(26)
            rele2.on(28)
            rele2.on(29)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR3 = formatRealNumber(measuringR.toDouble())
            if (measuringR3 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroupNC3.value = "-.--"
            }
            view.progressBarTime.progress = 3.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(28)
            rele2.off(29)
            rele2.on(31)
            rele2.on(32)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR4 = formatRealNumber(measuringR.toDouble())
            if (measuringR4 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC4.value = measuringR4.toString()
            } else {
                tableValues[0].resistanceContactGroupNC4.value = "-.--"
            }
            view.progressBarTime.progress = 4.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(31)
            rele2.off(32)
            rele2.on(14)
            rele2.on(15)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR5 = formatRealNumber(measuringR.toDouble())
            if (measuringR5 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC5.value = measuringR5.toString()
            } else {
                tableValues[0].resistanceContactGroupNC5.value = "-.--"
            }
            view.progressBarTime.progress = 5.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(14)
            rele2.off(15)
            rele2.on(11)
            rele2.on(12)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR6 = formatRealNumber(measuringR.toDouble())
            if (measuringR6 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroupNC6.value = "-.--"
            }
            view.progressBarTime.progress = 6.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(11)
            rele2.off(12)
            rele2.on(8)
            rele2.on(9)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR7 = formatRealNumber(measuringR.toDouble())
            if (measuringR7 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC7.value = measuringR7.toString()
            } else {
                tableValues[0].resistanceContactGroupNC7.value = "-.--"
            }
            view.progressBarTime.progress = 7.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(8)
            rele2.off(9)
            rele2.on(5)
            rele2.on(6)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR8 = formatRealNumber(measuringR.toDouble())
            if (measuringR8 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroupNC8.value = "-.--"
            }
            view.progressBarTime.progress = 8.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            offAllRele()
        }
    }

    private fun nmsh2() {
        idcGV1.offVoltage()
        if (isExperimentRunning && isDevicesResponding()) {

            rele1.on(21)
            rele1.on(22)
            rele1.on(23)
            rele1.on(24)
            rele1.on(28)
            rele1.on(29)
            rele1.on(30)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroupNC1.value = "-.--"
            view.progressBarTime.progress = 1.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(25)
            rele2.on(26)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroupNC2.value = "-.--"
            }
            view.progressBarTime.progress = 2.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(25)
            rele2.off(26)
            tableValues[0].resistanceContactGroupNC3.value = "-.--"
            view.progressBarTime.progress = 3.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(31)
            rele2.on(32)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR4 = formatRealNumber(measuringR.toDouble())
            if (measuringR4 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC4.value = measuringR4.toString()
            } else {
                tableValues[0].resistanceContactGroupNC4.value = "-.--"
            }
            view.progressBarTime.progress = 4.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(31)
            rele2.off(32)
            view.progressBarTime.progress = 5.0 / 8.0
            tableValues[0].resistanceContactGroupNC5.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(11)
            rele2.on(12)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR6 = formatRealNumber(measuringR.toDouble())
            if (measuringR6 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroupNC6.value = "-.--"
            }
            view.progressBarTime.progress = 6.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(11)
            rele2.off(12)
            view.progressBarTime.progress = 7.0 / 8.0
            tableValues[0].resistanceContactGroupNC7.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(5)
            rele2.on(6)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR8 = formatRealNumber(measuringR.toDouble())
            if (measuringR8 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroupNC8.value = "-.--"
            }
            view.progressBarTime.progress = 8.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            offAllRele()
        }

    }

    private fun nmsh3() {
        idcGV1.offVoltage()
        if (isExperimentRunning && isDevicesResponding()) {
            rele3.on(9)
            rele1.on(22)
            rele3.on(8)
            rele1.on(20)
            rele1.on(14)
            rele1.on(15)
            rele1.on(26)
            rele1.on(32)
            rele1.on(31)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            view.progressBarTime.progress = 1.0 / 8.0
            tableValues[0].resistanceContactGroupNC1.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(25)
            rele2.on(26)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroupNC2.value = "-.--"
            }
            view.progressBarTime.progress = 2.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(25)
            rele2.off(26)
            view.progressBarTime.progress = 3.0 / 8.0
            tableValues[0].resistanceContactGroupNC3.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            view.progressBarTime.progress = 4.0 / 8.0
            tableValues[0].resistanceContactGroupNC4.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            view.progressBarTime.progress = 5.0 / 8.0
            tableValues[0].resistanceContactGroupNC5.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            view.progressBarTime.progress = 6.0 / 8.0
            tableValues[0].resistanceContactGroupNC6.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            view.progressBarTime.progress = 7.0 / 8.0
            tableValues[0].resistanceContactGroupNC7.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(5)
            rele2.on(6)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR8 = formatRealNumber(measuringR.toDouble())
            if (measuringR8 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroupNC8.value = "-.--"
            }
            view.progressBarTime.progress = 8.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            offAllRele()
        }

    }

    private fun nmsh4() {
        idcGV1.offVoltage()
        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(21)
            rele1.on(18)
            rele3.on(7)
            rele1.on(23)
            rele1.on(20)
            rele1.on(14)
            rele1.on(28)
            rele1.on(26)
            rele1.on(32)
            rele1.on(30)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(22)
            rele2.on(23)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC1.value = measuringR1.toString()
            } else {
                tableValues[0].resistanceContactGroupNC1.value = "-.--"
            }
            view.progressBarTime.progress = 1.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(22)
            rele2.off(23)
            view.progressBarTime.progress = 2.0 / 8.0
            tableValues[0].resistanceContactGroupNC2.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(28)
            rele2.on(29)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR3 = formatRealNumber(measuringR.toDouble())
            if (measuringR3 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroupNC3.value = "-.--"
            }
            view.progressBarTime.progress = 3.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(28)
            rele2.off(29)
            view.progressBarTime.progress = 4.0 / 8.0
            tableValues[0].resistanceContactGroupNC4.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(14)
            rele2.on(15)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR5 = formatRealNumber(measuringR.toDouble())
            if (measuringR5 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC5.value = measuringR5.toString()
            } else {
                tableValues[0].resistanceContactGroupNC5.value = "-.--"
            }
            view.progressBarTime.progress = 5.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(14)
            rele2.off(15)
            view.progressBarTime.progress = 6.0 / 8.0
            tableValues[0].resistanceContactGroupNC6.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(8)
            rele2.on(9)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR7 = formatRealNumber(measuringR.toDouble())
            if (measuringR7 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC7.value = measuringR7.toString()
            } else {
                tableValues[0].resistanceContactGroupNC7.value = "-.--"
            }
            view.progressBarTime.progress = 7.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(8)
            rele2.off(9)
            view.progressBarTime.progress = 8.0 / 8.0
            tableValues[0].resistanceContactGroupNC8.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            offAllRele()
        }
    }

    private fun rel1() {
        idcGV1.offVoltage()
        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(16)
            rele1.on(21)
            rele1.on(22)
            rele1.on(23)
            rele1.on(28)
            rele1.on(29)
            rele1.on(30)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(22)
            rele2.on(23)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC1.value = measuringR1.toString()
            } else {
                tableValues[0].resistanceContactGroupNC1.value = "-.--"
            }
            view.progressBarTime.progress = 1.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(22)
            rele2.off(23)
            rele2.on(25)
            rele2.on(26)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroupNC2.value = "-.--"
            }
            view.progressBarTime.progress = 2.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(25)
            rele2.off(26)
            rele2.on(28)
            rele2.on(29)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR3 = formatRealNumber(measuringR.toDouble())
            if (measuringR3 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroupNC3.value = "-.--"
            }
            view.progressBarTime.progress = 3.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
//            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления четвертой контактной группы")
            rele2.off(28)
            rele2.off(29)
//            rele2.on(31)
//            rele2.on(32)
//            sleep(500)
//            ikas1.startSerialMeasuring()
//            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

//        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
//            sleep(100)
//            if (statusIkas == 138f) {
//                cause = "Ошибка 138"
//            }
//        }
//
//        if (isExperimentRunning && isDevicesResponding()) {
//            measuringR4 = formatRealNumber(measuringR.toDouble())
//            if (measuringR4 != BREAK_IKAS) {
//                tableValues[0].resistanceContactGroupNC4.value = measuringR4.toString()
//            } else {
        view.progressBarTime.progress = 4.0 / 8.0
        tableValues[0].resistanceContactGroupNC4.value = "-.--"
//            }
//        }

//        if (isExperimentRunning && isDevicesResponding()) {
//            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления пятой контактной группы")
//            rele2.off(31)
//            rele2.off(32)
//            rele2.on(14)
//            rele2.on(15)
//            sleep(500)
//            ikas1.startSerialMeasuring()
//            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
//        }

//        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
//            sleep(100)
//            if (statusIkas == 138f) {
//                cause = "Ошибка 138"
//            }
//        }
//
//        if (isExperimentRunning && isDevicesResponding()) {
//            measuringR5 = formatRealNumber(measuringR.toDouble())
//            if (measuringR5 != BREAK_IKAS) {
//                tableValues[0].resistanceContactGroupNC5.value = measuringR5.toString()
//            } else {
        view.progressBarTime.progress = 5.0 / 8.0
        tableValues[0].resistanceContactGroupNC5.value = "-.--"
//            }
//        }

        if (isExperimentRunning && isDevicesResponding()) {
//            rele2.off(14)
//            rele2.off(15)
            rele2.on(11)
            rele2.on(12)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR6 = formatRealNumber(measuringR.toDouble())
            if (measuringR6 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroupNC6.value = "-.--"
            }
            view.progressBarTime.progress = 6.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(11)
            rele2.off(12)
            rele2.on(8)
            rele2.on(9)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR7 = formatRealNumber(measuringR.toDouble())
            if (measuringR7 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC7.value = measuringR7.toString()
            } else {
                tableValues[0].resistanceContactGroupNC7.value = "-.--"
            }
            view.progressBarTime.progress = 7.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(8)
            rele2.off(9)
            rele2.on(5)
            rele2.on(6)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR8 = formatRealNumber(measuringR.toDouble())
            if (measuringR8 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroupNC8.value = "-.--"
            }
            view.progressBarTime.progress = 8.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            offAllRele()
        }
    }

    private fun rel2() {
        idcGV1.offVoltage()
        if (isExperimentRunning && isDevicesResponding()) {
            rele3.on(9)
            rele1.on(18)
            rele3.on(7)
            rele1.on(23)
            rele1.on(24)
            rele1.on(28)
            rele1.on(29)
            rele1.on(31)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            view.progressBarTime.progress = 1.0 / 8.0
            tableValues[0].resistanceContactGroupNC1.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            view.progressBarTime.progress = 2.0 / 8.0
            tableValues[0].resistanceContactGroupNC2.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(25)
            rele2.off(26)
            rele2.on(28)
            rele2.on(29)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR3 = formatRealNumber(measuringR.toDouble())
            if (measuringR3 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroupNC3.value = "-.--"
            }
            view.progressBarTime.progress = 3.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(28)
            rele2.off(29)
            rele2.on(31)
            rele2.on(32)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR4 = formatRealNumber(measuringR.toDouble())
            if (measuringR4 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC4.value = measuringR4.toString()
            } else {
                tableValues[0].resistanceContactGroupNC4.value = "-.--"
            }
            view.progressBarTime.progress = 4.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(31)
            rele2.off(32)
            rele2.on(14)
            rele2.on(15)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR5 = formatRealNumber(measuringR.toDouble())
            if (measuringR5 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC5.value = measuringR5.toString()
            } else {
                tableValues[0].resistanceContactGroupNC5.value = "-.--"
            }
            view.progressBarTime.progress = 5.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(14)
            rele2.off(15)
            rele2.on(11)
            rele2.on(12)
            sleep(500)
            ikas1.startSerialMeasuring()
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500)
            measuringR6 = formatRealNumber(measuringR.toDouble())
            if (measuringR6 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroupNC6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroupNC6.value = "-.--"
            }
            view.progressBarTime.progress = 6.0 / 8.0
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(11)
            rele2.off(12)
            view.progressBarTime.progress = 7.0 / 8.0
            tableValues[0].resistanceContactGroupNC7.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            view.progressBarTime.progress = 8.0 / 8.0
            tableValues[0].resistanceContactGroupNC8.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            offAllRele()
        }
    }

    private fun setResult() {
        when {
            cause.isNotEmpty() -> {
                tableValues[0].result.value = "Прервано"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: $cause")
            }
            !isDevicesResponding() -> {
                controller.tableValuesTest1[0].result.value = "Прервано"
                appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: потеряна связь с устройствами")
            }
            else -> {
                appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
                tableValues[0].result.value = "Годен"
            }
        }
    }
}