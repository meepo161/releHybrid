package ru.avem.rele.view

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.rele.controllers.MainViewController
import ru.avem.rele.controllers.ObjectTypeEditorController
import ru.avem.rele.database.entities.ObjectsTypes
import ru.avem.rele.database.entities.TestObjectsType
import tornadofx.*

class ObjectTypeEditorWindow : View("Редактор объектов") {
    var textfieldPressure: TextField by singleAssign()
    var textfieldTime: TextField by singleAssign()

    var tableViewObjects: TableView<TestObjectsType> by singleAssign()

    private val controller: ObjectTypeEditorController by inject()
    private val mainController: MainViewController by inject()

    override val root = anchorpane {
        prefWidth = 1920.0
        prefHeight = 1000.0
        hbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }
            alignment = Pos.CENTER

            tableViewObjects = tableview {
                columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY)
                prefWidth = 1600.0
                items = controller.getObjects()

                column("Серийный номер", TestObjectsType::serialNumber) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.serialNumber eq selectedItem!!.serialNumber
                            }) {
                                it[serialNumber] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()

                column("Сопротивление 1 катушки", TestObjectsType::resistanceCoil1) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.resistanceCoil1 eq selectedItem!!.resistanceCoil1
                            }) {
                                it[resistanceCoil1] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Сопротивление 2 катушки", TestObjectsType::resistanceCoil2) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.resistanceCoil2 eq selectedItem!!.resistanceCoil2
                            }) {
                                it[resistanceCoil2] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Управление", TestObjectsType::voltageOrCurrent) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.voltageOrCurrent eq selectedItem!!.voltageOrCurrent
                            }) {
                                it[voltageOrCurrent] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Ном напряжение", TestObjectsType::voltageOrCurrentNom) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.voltageOrCurrentNom eq selectedItem!!.voltageOrCurrentNom
                            }) {
                                it[voltageOrCurrentNom] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Мин напряжение", TestObjectsType::voltageOrCurrentMin) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.voltageOrCurrentMin eq selectedItem!!.voltageOrCurrentMin
                            }) {
                                it[voltageOrCurrentMin] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Макс напряжение", TestObjectsType::voltageOrCurrentMax) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.voltageOrCurrentMax eq selectedItem!!.voltageOrCurrentMax
                            }) {
                                it[voltageOrCurrentMax] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Перегрузка", TestObjectsType::voltageOrCurrentOverload) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.voltageOrCurrentOverload eq selectedItem!!.voltageOrCurrentOverload
                            }) {
                                it[voltageOrCurrentOverload] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
                column("Время", TestObjectsType::timeOff) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            ObjectsTypes.update({
                                ObjectsTypes.timeOff eq selectedItem!!.timeOff
                            }) {
                                it[timeOff] = cell.newValue
                            }
                        }
                        mainController.refreshObjectsTypes()
                    }
                    addClass(Styles.medium)
                }.makeEditable()
            }
        }.addClass(Styles.medium)
    }.addClass(Styles.medium, Styles.blueTheme)
}
