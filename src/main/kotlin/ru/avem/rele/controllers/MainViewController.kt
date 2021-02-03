package ru.avem.rele.controllers

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ButtonType
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.rele.database.entities.ObjectsTypes
import ru.avem.rele.database.entities.TestObjectsType
import ru.avem.rele.entities.*
import ru.avem.rele.utils.Singleton
import ru.avem.rele.utils.Toast
import ru.avem.rele.view.MainView
import tornadofx.*


class MainViewController : Controller() {
    val view: MainView by inject()
    var position1 = ""
    var maskTests = 0
    var auto = false

    var tableValuesTest1 = observableListOf(
        TableValuesTest1(
            SimpleStringProperty("Заданные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        ),
        TableValuesTest1(
            SimpleStringProperty("Измеренные"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("")
        )
    )

    var tableValuesTest2 = observableListOf(
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
    var tableValuesTest3 = observableListOf(
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

    var tableValuesTest4 = observableListOf(
        TableValuesTest4(
            SimpleStringProperty("Заданные"),
            SimpleDoubleProperty(0.0),
            SimpleStringProperty("")
        ),

        TableValuesTest4(
            SimpleStringProperty("Измеренные"),
            SimpleDoubleProperty(0.0),
            SimpleStringProperty("")
        )
    )

    var tableValuesTest5 = observableListOf(
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

    var tableValuesTest6 = observableListOf(
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

    fun handleStartTest() {
        if (view.textFieldSerialNumber.text.isEmpty() || view.comboBoxTestItem.selectionModel.isEmpty || view.comboBoxTypeItem.selectionModel.isEmpty) {
            Toast.makeText("Введите заводской номер и выберите объект испытания").show(Toast.ToastType.WARNING)
        } else {
            Singleton.currentTestItem = transaction {
                TestObjectsType.find {
                    ObjectsTypes.serialNumber eq view.comboBoxTestItem.selectedItem.toString()
                }.toList().asObservable()
            }.first()
            Singleton.currentTestItemType = view.comboBoxTypeItem.selectedItem.toString()

            if (!isAtLeastOneIsSelected()) {
                Toast.makeText("Выберите хотя бы одно испытание из списка").show(Toast.ToastType.WARNING)
            } else if (!Singleton.currentTestItem.resistanceCoil1.replace(",", ".").isDouble() ||
                !Singleton.currentTestItem.resistanceCoil2.replace(",", ".").isDouble() ||
                !Singleton.currentTestItem.voltageOrCurrentNom.replace(",", ".").isDouble() ||
                !Singleton.currentTestItem.voltageOrCurrentMin.replace(",", ".").isDouble() ||
                !Singleton.currentTestItem.voltageOrCurrentMax.replace(",", ".").isDouble() ||
                !Singleton.currentTestItem.voltageOrCurrentOverload.replace(",", ".").isDouble() ||
                !Singleton.currentTestItem.timeOff.replace(",", ".").isDouble()
            ) {
                Toast.makeText("Проверьте правильнсть введенных данных в объекте испытания")
                    .show(Toast.ToastType.WARNING)
            } else {
                maskTests = 0
                maskTests = maskTests or if (view.checkBoxTest1.isSelected) 1 else 0
                maskTests = maskTests or if (view.checkBoxTest2.isSelected) 2 else 0
                maskTests = maskTests or if (view.checkBoxTest3.isSelected) 4 else 0
                maskTests = maskTests or if (view.checkBoxTest4.isSelected) 8 else 0
                maskTests = maskTests or if (view.checkBoxTest5.isSelected) 16 else 0
                maskTests = maskTests or if (view.checkBoxTest6.isSelected) 32 else 0

                clearTableResults()
                auto = false

                confirm(
                    "Автоматическое выполнение опытов",
                    "Провести опыты в автоматическом режиме?",
                    ButtonType.YES, ButtonType.NO,
                    owner = view.currentWindow,
                    title = ""
                ) {
                    auto = true
                }


                when {
                    maskTests and 1 > 0 -> {
                        view.start1Test()
                    }
                    maskTests and 2 > 0 -> {
                        view.start2Test()
                    }
                    maskTests and 4 > 0 -> {
                        view.start3Test()
                    }
                    maskTests and 8 > 0 -> {
                        view.start4Test()
                    }
                    maskTests and 16 > 0 -> {
                        view.start5Test()
                    }
                    maskTests and 32 > 0 -> {
                        view.start6Test()
                    }
                }
            }
        }
    }

    private fun clearTableResults() {
        tableValuesTest1[0].resistanceCoil1.value = ""
        tableValuesTest1[0].resistanceCoil2.value = ""
        tableValuesTest1[1].resistanceCoil1.value = ""
        tableValuesTest1[1].resistanceCoil2.value = ""
        tableValuesTest1[1].result.value = ""

        tableValuesTest2[0].resistanceContactGroup1.value = ""
        tableValuesTest2[0].resistanceContactGroup2.value = ""
        tableValuesTest2[0].resistanceContactGroup3.value = ""
        tableValuesTest2[0].resistanceContactGroup4.value = ""
        tableValuesTest2[0].resistanceContactGroup5.value = ""
        tableValuesTest2[0].resistanceContactGroup6.value = ""
        tableValuesTest2[0].resistanceContactGroup7.value = ""
        tableValuesTest2[0].resistanceContactGroup8.value = ""
        tableValuesTest2[0].result.value = ""

        tableValuesTest3[0].resistanceContactGroupNC1.value = ""
        tableValuesTest3[0].resistanceContactGroupNC2.value = ""
        tableValuesTest3[0].resistanceContactGroupNC3.value = ""
        tableValuesTest3[0].resistanceContactGroupNC4.value = ""
        tableValuesTest3[0].resistanceContactGroupNC5.value = ""
        tableValuesTest3[0].resistanceContactGroupNC6.value = ""
        tableValuesTest3[0].resistanceContactGroupNC7.value = ""
        tableValuesTest3[0].resistanceContactGroupNC8.value = ""
        tableValuesTest3[0].result.value = ""

        tableValuesTest4[0].voltage.value = 0.0
        tableValuesTest4[1].voltage.value = 0.0
        tableValuesTest4[1].result.value = ""

        tableValuesTest5[0].voltage.value = 0.0
        tableValuesTest5[1].voltage.value = 0.0
        tableValuesTest5[1].result.value = ""

        tableValuesTest6[0].time.value = 0.0
        tableValuesTest6[1].time.value = 0.0
        tableValuesTest6[1].result.value = ""
    }

    private fun isAtLeastOneIsSelected(): Boolean {
        return view.checkBoxTest1.isSelected ||
                view.checkBoxTest2.isSelected ||
                view.checkBoxTest3.isSelected ||
                view.checkBoxTest4.isSelected ||
                view.checkBoxTest5.isSelected ||
                view.checkBoxTest6.isSelected
    }

    fun refreshObjectsTypes() {
//        val selectedIndex = view.comboBoxTestItem.selectionModel.selectedIndex
//        view.comboBoxTestItem.items = transaction {
//            TestObjectsType.all().toList().asObservable()
//        }
//        view.comboBoxTestItem.selectionModel.select(selectedIndex)
    }

    fun showAboutUs() {
        Toast.makeText("Версия ПО: 1.0.0\nДата: 28.01.2021").show(Toast.ToastType.INFORMATION)
    }
}
