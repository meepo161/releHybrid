package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.devices.avem.ikas.Ikas8Model
import ru.avem.rele.entities.TableValuesTest3
import ru.avem.rele.utils.BREAK_IKAS
import ru.avem.rele.utils.LogTag
import ru.avem.rele.utils.Singleton.currentTestItem
import ru.avem.rele.utils.State
import ru.avem.rele.utils.formatRealNumber
import ru.avem.rele.view.Test3View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableList
import tornadofx.style
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test3Controller : TestController() {

    private lateinit var factoryNumber: String
    val view: Test3View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableList(
        TableValuesTest3(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),

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
    private var openContact = true

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
        tableValues[0].resistanceContactGroupNC1.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroupNC2.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroupNC3.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroupNC4.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroupNC5.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroupNC6.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroupNC7.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroupNC8.value = currentTestItem.resistanceContactGroup
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

    private fun startPollDevices() {
        CommunicationModel.startPoll(CommunicationModel.DeviceID.IKAS1, Ikas8Model.STATUS) { value ->
            statusIkas = value.toFloat()
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.IKAS1, Ikas8Model.RESIST_MEAS) { value ->
            measuringR = value.toFloat()
        }
    }

    fun startTest() {
        testItemR = currentTestItem.resistanceCoil.toDouble()
        thread(isDaemon = true) {
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

//            while (!isDevicesResponding() && isExperimentRunning) {
//                CommunicationModel.checkDevices()
//                sleep(100)
//            }

            if (isExperimentRunning && isDevicesResponding()) {
                ikas1.stopSerialMeasuring()
                idcGV1.remoteControl()
                idcGV1.offVoltage()
                idcGV1.setVoltage(24.0)
                idcGV1.setMaxCurrent(1.0)
                offAllRele()
            }

            startCloseContactTest()

            ikas1.stopSerialMeasuring()
            setResult()
            CommunicationModel.clearPollingRegisters()
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

    private fun startCloseContactTest() {
        idcGV1.offVoltage()
        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Сбор схемы для нормально закрытых контактов")
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
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления первой контактной группы")
            rele2.on(22)
            rele2.on(23)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroupNC1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceContactGroupNC1.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления второй контактной группы")
            rele2.off(22)
            rele2.off(23)
            rele2.on(25)
            rele2.on(26)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroupNC2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceContactGroupNC2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления третьей контактной группы")
            rele2.off(25)
            rele2.off(26)
            rele2.on(28)
            rele2.on(29)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
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
                tableValues[1].resistanceContactGroupNC3.value = measuringR3.toString()
            } else {
                tableValues[1].resistanceContactGroupNC3.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
//            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления четвертой контактной группы")
            rele2.off(28)
            rele2.off(29)
//            rele2.on(31)
//            rele2.on(32)
//            sleep(2000)
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
//                tableValues[1].resistanceContactGroupNC4.value = measuringR4.toString()
//            } else {
                tableValues[1].resistanceContactGroupNC4.value = "-.--"
//            }
//        }

//        if (isExperimentRunning && isDevicesResponding()) {
//            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления пятой контактной группы")
//            rele2.off(31)
//            rele2.off(32)
//            rele2.on(14)
//            rele2.on(15)
//            sleep(2000)
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
//                tableValues[1].resistanceContactGroupNC5.value = measuringR5.toString()
//            } else {
                tableValues[1].resistanceContactGroupNC5.value = "-.--"
//            }
//        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления шестой контактной группы")
//            rele2.off(14)
//            rele2.off(15)
            rele2.on(11)
            rele2.on(12)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR6 = formatRealNumber(measuringR.toDouble())
            if (measuringR6 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroupNC6.value = measuringR6.toString()
            } else {
                tableValues[1].resistanceContactGroupNC6.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления седьмой контактной группы")
            rele2.off(11)
            rele2.off(12)
            rele2.on(8)
            rele2.on(9)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR7 = formatRealNumber(measuringR.toDouble())
            if (measuringR7 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroupNC7.value = measuringR7.toString()
            } else {
                tableValues[1].resistanceContactGroupNC7.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления восьмой контактной группы")
            rele2.off(8)
            rele2.off(19)
            rele2.on(5)
            rele2.on(6)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                cause = "Ошибка 138"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR8 = formatRealNumber(measuringR.toDouble())
            if (measuringR8 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroupNC8.value = measuringR8.toString()
            } else {
                tableValues[1].resistanceContactGroupNC8.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Разбор схемы для нормально открытых контактов")
            offAllRele()
        }
    }

    private fun setResult() {
        if (cause.isNotEmpty()) {
            tableValues[1].result.value = "Неуспешно"
            appendMessageToLog(LogTag.ERROR, "Причина: $cause")
        } else {
            appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
            tableValues[1].result.value = "Успешно"
        }
    }
}