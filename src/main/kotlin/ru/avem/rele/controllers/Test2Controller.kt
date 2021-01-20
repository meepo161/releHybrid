package ru.avem.rele.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import ru.avem.rele.communication.model.CommunicationModel
import ru.avem.rele.communication.model.devices.avem.ikas.Ikas8Model
import ru.avem.rele.entities.TableValuesTest2
import ru.avem.rele.utils.BREAK_IKAS
import ru.avem.rele.utils.LogTag
import ru.avem.rele.utils.Singleton.currentTestItem
import ru.avem.rele.utils.State
import ru.avem.rele.utils.formatRealNumber
import ru.avem.rele.view.Test2View
import tornadofx.add
import tornadofx.clear
import tornadofx.observableList
import tornadofx.style
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class Test2Controller : TestController() {
    private lateinit var factoryNumber: String
    val view: Test2View by inject()
    val controller: MainViewController by inject()

    var tableValues = observableList(
        TableValuesTest2(
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
    private var cause: String = ""


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
        tableValues[0].resistanceContactGroup1.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroup2.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroup3.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroup4.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroup5.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroup6.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroup7.value = currentTestItem.resistanceContactGroup
        tableValues[0].resistanceContactGroup8.value = currentTestItem.resistanceContactGroup
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
        return true
//        return ikasPR1.isResponding
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

            startOpenContactTest()

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


    private fun startOpenContactTest() {

        if (isExperimentRunning && isDevicesResponding()) {
            idcGV1.remoteControl()
            idcGV1.offVoltage()
            idcGV1.setVoltage(24.0)
            idcGV1.setMaxCurrent(1.0)
            idcGV1.onVoltage()
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Сбор схемы для нормально открытых контактов")
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
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления первой контактной группы")
            rele2.on(22)
            rele2.on(21)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR1 = formatRealNumber(measuringR.toDouble())
            if (measuringR1 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroup1.value = measuringR1.toString()
            } else {
                tableValues[1].resistanceContactGroup1.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления второй контактной группы")
            rele2.off(22)
            rele2.off(21)
            rele2.on(24)
            rele2.on(25)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR2 = formatRealNumber(measuringR.toDouble())
            if (measuringR2 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroup2.value = measuringR2.toString()
            } else {
                tableValues[1].resistanceContactGroup2.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления третьей контактной группы")
            rele2.off(24)
            rele2.off(25)
            rele2.on(27)
            rele2.on(28)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR3 = formatRealNumber(measuringR.toDouble())
            if (measuringR3 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroup3.value = measuringR3.toString()
            } else {
                tableValues[1].resistanceContactGroup3.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
//            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления четвертой контактной группы")
            rele2.off(27)
            rele2.off(28)
//            rele2.on(30)
//            rele2.on(31)
//            sleep(2000)
//            ikas1.startSerialMeasuring()
//            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

//        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
//            sleep(100)
//            if (statusIkas == 138f) {
//                setCause("Ошибка 138")
//            }
//        }
//
//        if (isExperimentRunning && isDevicesResponding()) {
//            measuringR4 = formatRealNumber(measuringR.toDouble())
//            if (measuringR4 != BREAK_IKAS) {
//                tableValues[1].resistanceContactGroup4.value = measuringR4.toString()
//            } else {
                tableValues[1].resistanceContactGroup4.value = "-.--"
//            }
//        }

//        if (isExperimentRunning && isDevicesResponding()) {
//            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления пятой контактной группы")
//            rele2.off(30)
//            rele2.off(31)
//            rele2.on(15)
//            rele2.on(16)
//            sleep(2000)
//            ikas1.startSerialMeasuring()
//            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
//        }
//
//        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
//            sleep(100)
//            if (statusIkas == 138f) {
//                setCause("Ошибка 138")
//            }
//        }
//
//        if (isExperimentRunning && isDevicesResponding()) {
//            measuringR5 = formatRealNumber(measuringR.toDouble())
//            if (measuringR5 != BREAK_IKAS) {
//                tableValues[1].resistanceContactGroup5.value = measuringR5.toString()
//            } else {
                tableValues[1].resistanceContactGroup5.value = "-.--"
//            }
//        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления шестой контактной группы")
//            rele2.off(15)
//            rele2.off(16)
            rele2.on(12)
            rele2.on(13)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR6 = formatRealNumber(measuringR.toDouble())
            if (measuringR6 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroup6.value = measuringR6.toString()
            } else {
                tableValues[1].resistanceContactGroup6.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления седьмой контактной группы")
            rele2.off(12)
            rele2.off(13)
            rele2.on(9)
            rele2.on(10)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR7 = formatRealNumber(measuringR.toDouble())
            if (measuringR7 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroup7.value = measuringR7.toString()
            } else {
                tableValues[1].resistanceContactGroup7.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Измерение сопротивления восьмой контактной группы")
            rele2.off(9)
            rele2.off(10)
            rele2.on(6)
            rele2.on(7)
            sleep(2000)
            ikas1.startSerialMeasuring()
            appendMessageToLog(LogTag.DEBUG, "Ожидаем, пока измерение закончится")
        }

        while (isExperimentRunning && statusIkas != 0f && statusIkas != 101f && isDevicesResponding()) {
            sleep(100)
            if (statusIkas == 138f) {
                setCause("Ошибка 138")
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            measuringR8 = formatRealNumber(measuringR.toDouble())
            if (measuringR8 != BREAK_IKAS) {
                tableValues[1].resistanceContactGroup8.value = measuringR8.toString()
            } else {
                tableValues[1].resistanceContactGroup8.value = "Обрыв"
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(LogTag.DEBUG, "Разбор схемы для нормально открытых контактов")
            idcGV1.offVoltage()
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
