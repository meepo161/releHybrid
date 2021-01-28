package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.devices.avem.ikas.Ikas8Model
import ru.avem.rele.entities.TableValuesTest2
import ru.avem.rele.utils.*
import ru.avem.rele.view.Test2View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableListOf
import tornadofx.style
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test2Controller : TestController() {
    val view: Test2View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableListOf(
        TableValuesTest2(
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
    private var testItemVoltageNom: Double = 0.0

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
            it.resistanceContactGroup1.value = "0.0"
            it.resistanceContactGroup2.value = "0.0"
            it.resistanceContactGroup3.value = "0.0"
            it.resistanceContactGroup4.value = "0.0"
            it.resistanceContactGroup5.value = "0.0"
            it.resistanceContactGroup6.value = "0.0"
            it.resistanceContactGroup7.value = "0.0"
            it.resistanceContactGroup8.value = "0.0"
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


    fun startTest() {
        thread(isDaemon = true) {
            sleep(400)
            cause = ""
            clearLog()
            clearTable()

            appendMessageToLog(LogTag.MESSAGE, "Начало испытания")
            cause = ""
            Platform.runLater {
                view.buttonBack.isDisable = true
                view.buttonStartStopTest.text = "Остановить"
                view.buttonNextTest.isDisable = true
            }

            if (CommunicationModel.checkDevices().isNotEmpty()) {
                cause = "Не отвечают : ${CommunicationModel.checkDevices()}"
            }

            startPollDevices()
            isExperimentRunning = true
            isExperimentEnded = false

            testItemSerial = Singleton.currentTestItem.serialNumber
            testItemVoltageNom = Singleton.currentTestItem.voltageOrCurrentNom.replace(",", ".").toDouble()

            appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
            while (!isDevicesResponding() && isExperimentRunning) {
                CommunicationModel.checkDevices()
                sleep(100)
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

            controller.tableValuesTest2[0].resistanceContactGroup1.value = tableValues[0].resistanceContactGroup1.value
            controller.tableValuesTest2[0].resistanceContactGroup2.value = tableValues[0].resistanceContactGroup2.value
            controller.tableValuesTest2[0].resistanceContactGroup3.value = tableValues[0].resistanceContactGroup3.value
            controller.tableValuesTest2[0].resistanceContactGroup4.value = tableValues[0].resistanceContactGroup4.value
            controller.tableValuesTest2[0].resistanceContactGroup5.value = tableValues[0].resistanceContactGroup5.value
            controller.tableValuesTest2[0].resistanceContactGroup6.value = tableValues[0].resistanceContactGroup6.value
            controller.tableValuesTest2[0].resistanceContactGroup7.value = tableValues[0].resistanceContactGroup7.value
            controller.tableValuesTest2[0].resistanceContactGroup8.value = tableValues[0].resistanceContactGroup8.value
            controller.tableValuesTest2[0].result.value = tableValues[0].result.value

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
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            idcGV1.onVoltage()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(1)
            rele1.on(4)
            rele3.on(9)
            rele1.on(18)
            rele3.on(8)
            rele1.on(20)
            rele1.on(15)
            rele1.on(26)
            rele1.on(31)
            rele3.on(1)
            rele3.on(2)
            rele3.on(3)
            rele3.on(4)
            rele3.on(10)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(21)
            rele2.on(22)
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
                tableValues[0].resistanceContactGroup1.value = measuringR1.toString()
            } else {
                tableValues[0].resistanceContactGroup1.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(22)
            rele2.off(21)
            tableValues[0].resistanceContactGroup2.value = "-.--"
        }


        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(27)
            rele2.on(28)
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
                tableValues[0].resistanceContactGroup3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroup3.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(27)
            rele2.off(28)
            tableValues[0].resistanceContactGroup4.value = "-.--"
        }


        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(15)
            rele2.on(16)
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
                tableValues[0].resistanceContactGroup5.value = measuringR5.toString()
            } else {
                tableValues[0].resistanceContactGroup5.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(15)
            rele2.off(16)
            tableValues[0].resistanceContactGroup6.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(9)
            rele2.on(10)
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
                tableValues[0].resistanceContactGroup7.value = measuringR7.toString()
            } else {
                tableValues[0].resistanceContactGroup7.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(9)
            rele2.off(10)
            tableValues[0].resistanceContactGroup8.value = "-.--"
        }


        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.offVoltage()
            offAllRele()
        }
    }

    private fun nmsh1() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            idcGV1.onVoltage()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(1)
            rele1.on(4)
//            rele1.on(7)
//            rele1.on(10)
            rele1.on(17)
            rele1.on(18)
            rele1.on(19)
            rele1.on(20)
            rele1.on(25)
            rele1.on(26)
            rele1.on(27)

            rele3.on(1)
            rele3.on(2)
            rele3.on(3)
            rele3.on(4)
            rele3.on(11)
            rele3.on(10)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(22)
            rele2.on(21)
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
                tableValues[0].resistanceContactGroup1.value = measuringR1.toString()
            } else {
                tableValues[0].resistanceContactGroup1.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(22)
            rele2.off(21)
            rele2.on(24)
            rele2.on(25)
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
                tableValues[0].resistanceContactGroup2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroup2.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(24)
            rele2.off(25)
            rele2.on(27)
            rele2.on(28)
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
                tableValues[0].resistanceContactGroup3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroup3.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(27)
            rele2.off(28)
            rele2.on(30)
            rele2.on(31)
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
                tableValues[0].resistanceContactGroup4.value = measuringR4.toString()
            } else {
                tableValues[0].resistanceContactGroup4.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(30)
            rele2.off(31)
            rele2.on(15)
            rele2.on(16)
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
                tableValues[0].resistanceContactGroup5.value = measuringR5.toString()
            } else {
                tableValues[0].resistanceContactGroup5.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(15)
            rele2.off(16)
            rele2.on(12)
            rele2.on(13)
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
                tableValues[0].resistanceContactGroup6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroup6.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(12)
            rele2.off(13)
            rele2.on(9)
            rele2.on(10)
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
                tableValues[0].resistanceContactGroup7.value = measuringR7.toString()
            } else {
                tableValues[0].resistanceContactGroup7.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(9)
            rele2.off(10)
            rele2.on(6)
            rele2.on(7)
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
                tableValues[0].resistanceContactGroup8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroup8.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.offVoltage()
            offAllRele()
        }
    }

    private fun nmsh2() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            idcGV1.onVoltage()
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
            rele3.on(1)
            rele3.on(2)
            rele3.on(3)
            rele3.on(4)
            rele3.on(6)
            rele3.on(10)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroup1.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(24)
            rele2.on(25)
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
                tableValues[0].resistanceContactGroup2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroup2.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(24)
            rele2.off(25)
            tableValues[0].resistanceContactGroup3.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(30)
            rele2.on(31)
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
                tableValues[0].resistanceContactGroup4.value = measuringR4.toString()
            } else {
                tableValues[0].resistanceContactGroup4.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(30)
            rele2.off(31)
            tableValues[0].resistanceContactGroup5.value = "-.--"
        }


        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(12)
            rele2.on(13)
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
                tableValues[0].resistanceContactGroup6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroup6.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(12)
            rele2.off(13)
            tableValues[0].resistanceContactGroup7.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(6)
            rele2.on(7)
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
                tableValues[0].resistanceContactGroup8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroup8.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.offVoltage()
            offAllRele()
        }
    }

    private fun nmsh3() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            idcGV1.onVoltage()
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
            rele3.on(1)
            rele3.on(2)
            rele3.on(3)
            rele3.on(4)
            rele3.on(6)
            rele3.on(10)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroup1.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(24)
            rele2.on(25)
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
                tableValues[0].resistanceContactGroup2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroup2.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(24)
            rele2.off(25)
            tableValues[0].resistanceContactGroup3.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(30)
            rele2.on(31)
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
                tableValues[0].resistanceContactGroup4.value = measuringR4.toString()
            } else {
                tableValues[0].resistanceContactGroup4.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(30)
            rele2.off(31)
            tableValues[0].resistanceContactGroup5.value = "-.--"
        }


        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(12)
            rele2.on(13)
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
                tableValues[0].resistanceContactGroup6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroup6.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(12)
            rele2.off(13)
            tableValues[0].resistanceContactGroup7.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(6)
            rele2.on(7)
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
                tableValues[0].resistanceContactGroup8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroup8.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.offVoltage()
            offAllRele()
        }
    }

    private fun nmsh4() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            idcGV1.onVoltage()
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
            rele3.on(1)
            rele3.on(2)
            rele3.on(3)
            rele3.on(4)
            rele3.on(11)
            rele3.on(10)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(21)
            rele2.on(22)
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
                tableValues[0].resistanceContactGroup1.value = measuringR1.toString()
            } else {
                tableValues[0].resistanceContactGroup1.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(22)
            rele2.off(21)
            rele2.on(24)
            rele2.on(25)
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
                tableValues[0].resistanceContactGroup2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroup2.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(24)
            rele2.off(25)
            rele2.on(27)
            rele2.on(28)
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
                tableValues[0].resistanceContactGroup3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroup3.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(27)
            rele2.off(28)
            rele2.on(30)
            rele2.on(31)
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
                tableValues[0].resistanceContactGroup4.value = measuringR4.toString()
            } else {
                tableValues[0].resistanceContactGroup4.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(30)
            rele2.off(31)
            rele2.on(15)
            rele2.on(16)
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
                tableValues[0].resistanceContactGroup5.value = measuringR5.toString()
            } else {
                tableValues[0].resistanceContactGroup5.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(15)
            rele2.off(16)
            rele2.on(12)
            rele2.on(13)
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
                tableValues[0].resistanceContactGroup6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroup6.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(12)
            rele2.off(13)
            rele2.on(9)
            rele2.on(10)
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
                tableValues[0].resistanceContactGroup7.value = measuringR7.toString()
            } else {
                tableValues[0].resistanceContactGroup7.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(9)
            rele2.off(10)
            rele2.on(6)
            rele2.on(7)
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
                tableValues[0].resistanceContactGroup8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroup8.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.offVoltage()
            offAllRele()
        }
    }

    private fun rel1() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            idcGV1.onVoltage()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele1.on(1)
            rele1.on(4)
//            rele1.on(7)
//            rele1.on(10)
            rele1.on(17)
            rele1.on(18)
            rele1.on(19)
            rele1.on(20)
            rele1.on(25)
            rele1.on(26)
            rele1.on(27)

            rele3.on(1)
            rele3.on(2)
            rele3.on(3)
            rele3.on(4)
            rele3.on(10)
            rele3.on(11)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(22)
            rele2.on(21)
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
                tableValues[0].resistanceContactGroup1.value = measuringR1.toString()
            } else {
                tableValues[0].resistanceContactGroup1.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(22)
            rele2.off(21)
            rele2.on(24)
            rele2.on(25)
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
                tableValues[0].resistanceContactGroup2.value = measuringR2.toString()
            } else {
                tableValues[0].resistanceContactGroup2.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(24)
            rele2.off(25)
            rele2.on(27)
            rele2.on(28)
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
            measuringR3 = formatRealNumber(measuringR.toDouble())
            if (measuringR3 != BREAK_IKAS) {
                tableValues[0].resistanceContactGroup3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroup3.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(27)
            rele2.off(28)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroup4.value = "-.--"
        }
        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroup5.value = "-.--"
        }
        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(12)
            rele2.on(13)
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
                tableValues[0].resistanceContactGroup6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroup6.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(12)
            rele2.off(13)
            rele2.on(9)
            rele2.on(10)
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
                tableValues[0].resistanceContactGroup7.value = measuringR7.toString()
            } else {
                tableValues[0].resistanceContactGroup7.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(9)
            rele2.off(10)
            rele2.on(6)
            rele2.on(7)
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
                tableValues[0].resistanceContactGroup8.value = measuringR8.toString()
            } else {
                tableValues[0].resistanceContactGroup8.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.offVoltage()
            offAllRele()
        }
    }

    private fun rel2() {
        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(testItemVoltageNom)
            idcGV1.setMaxCurrent(1.0)
            idcGV1.onVoltage()
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
            rele3.on(1)
            rele3.on(2)
            rele3.on(3)
            rele3.on(4)
            rele3.on(11)
            rele3.on(10)
        }

        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroup1.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroup2.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.on(27)
            rele2.on(28)
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
                tableValues[0].resistanceContactGroup3.value = measuringR3.toString()
            } else {
                tableValues[0].resistanceContactGroup3.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(27)
            rele2.off(28)
            rele2.on(30)
            rele2.on(31)
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
                tableValues[0].resistanceContactGroup4.value = measuringR4.toString()
            } else {
                tableValues[0].resistanceContactGroup4.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(30)
            rele2.off(31)
            rele2.on(15)
            rele2.on(16)
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
                tableValues[0].resistanceContactGroup5.value = measuringR5.toString()
            } else {
                tableValues[0].resistanceContactGroup5.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(15)
            rele2.off(16)
            rele2.on(12)
            rele2.on(13)
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
                tableValues[0].resistanceContactGroup6.value = measuringR6.toString()
            } else {
                tableValues[0].resistanceContactGroup6.value = "-.--"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            rele2.off(12)
            rele2.off(13)
            tableValues[0].resistanceContactGroup7.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            tableValues[0].resistanceContactGroup8.value = "-.--"
        }

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.offVoltage()
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
