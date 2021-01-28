package ru.avem.rele.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.stage.Modality
import org.slf4j.LoggerFactory
import ru.avem.rele.controllers.*
import ru.avem.rele.entities.*
import ru.avem.rele.utils.transitionLeft
import ru.avem.rele.view.Styles.Companion.megaHard
import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class MainView : View("Комплексный стенд проверки реле") {
    override val configPath: Path = Paths.get("./app.conf")

    //    var ackViewFXML: FXMLLoader = FXMLLoader(URL("file:///C:/Users/meepo/IdeaProjects/rele2/src/main/resources/ru/avem/rele/layout/ackView.fxml"))
//    var ackViewFXML: InputStream = this::class.java.classLoader.getResourceAsStream("./layout/ackView.fxml")

    private val controller: MainViewController by inject()

    var mainMenubar: MenuBar by singleAssign()

    private var addIcon = ImageView("ru/avem/rele/icon/add.png")
    private var deleteIcon = ImageView("ru/avem/rele/icon/delete.png")
    private var editIcon = ImageView("ru/avem/rele/icon/edit.png")

    var comboBoxTestItem: ComboBox<String> by singleAssign()
    var comboBoxTypeItem: ComboBox<String> by singleAssign()

    var buttonStart: Button by singleAssign()
    var buttonSelectAll: Button by singleAssign()
    var checkBoxTest1: CheckBox by singleAssign()
    var checkBoxTest2: CheckBox by singleAssign()
    var checkBoxTest3: CheckBox by singleAssign()
    var checkBoxTest4: CheckBox by singleAssign()
    var checkBoxTest5: CheckBox by singleAssign()
    var checkBoxTest6: CheckBox by singleAssign()
    var textFieldSerialNumber: TextField by singleAssign()

    var tableView1Result: TableView<TableValuesTest1> by singleAssign()
    var tableView2Result: TableView<TableValuesTest2> by singleAssign()
    var tableView3Result: TableView<TableValuesTest3> by singleAssign()
    var tableView4Result: TableView<TableValuesTest4> by singleAssign()
    var tableView5Result: TableView<TableValuesTest5> by singleAssign()
    var tableView6Result: TableView<TableValuesTest6> by singleAssign()

    var test1Controller = Test1Controller()
    var test2Controller = Test2Controller()
    var test3Controller = Test4Controller()
    var test4Controller = Test5Controller()
    var test5Controller = Test6Controller()

    private var selectedItemProperty = SimpleStringProperty()

    val nmsh1 = observableListOf(
        "НМШ1-400",
        "НМШ1-1440",
        "НМШ1-7000",
        "НМШМ1-11",
        "НМШМ1-22",
        "НМШМ1-180",
        "НМШМ1-360",
        "НМШМ1-560",
        "НМШМ1-1120",
        "НМШМ1-1000/560",
        "НШ1-2",
        "НШ1-400/30",
        "НШ1-800",
        "НШ1-2000",
        "НШ1-9000",
        "НШ1М-200/30",
        "НШ1М-400",
        "НШ1М-200/400"
    )

    val nmsh2 = observableListOf(
        "НМШ2-900",
        "НМШ2-4000",
        "НМШ2-12000",
        "НМШМ2-1.5",
        "НМШМ2-320",
        "НМШМ2-640",
        "НМШМ2-1500",
        "НМШМ2-3000",
        "НШ2-2",
        "НШ2-40",
        "НШ2-2000"
    )

    val ansh2 = observableListOf(
        "АНШМ2-310",
        "АНШМ2-620",
        "АНШМ2-760",
        "АНШ2-2",
        "АНШ2-37",
        "АНШ2-40",
        "АНШ2-310",
        "АНШ2-700",
        "АНШ2-1230"
    )

    val rel1 = observableListOf(
        "РЭЛ1-1600",
        "РЭЛ1М-600",
        "РЭЛ1-400",
        "РЭЛ1М-160",
        "РЭЛ1-6.8"
    )
    val nmsh3 = observableListOf(
        "НМШ3-460/400"
    )

    val nmsh4 = observableListOf(
        "НМШ4-3",
        "НМШ4-3.4",
        "НМШ4-530",
        "НМШ4-600",
        "НМШ4-2400",
        "НМШ4-3000",
        "НМШМ4-250",
        "НМШМ4-500"
    )
    val rel2 = observableListOf(
        "РЭЛ2-2400",
        "РЭЛ2М-1000"
    )

    companion object {
        private val logger = LoggerFactory.getLogger(MainView::class.java)
    }

    override fun onBeforeShow() {
        addIcon.fitHeight = 16.0
        addIcon.fitWidth = 16.0
        deleteIcon.fitHeight = 16.0
        deleteIcon.fitWidth = 16.0
        editIcon.fitHeight = 16.0
        editIcon.fitWidth = 16.0
    }

    override fun onDock() {
        selectedItemProperty.onChange {
            when (it) {
                "НМШ1" -> {
                    comboBoxTestItem.items = nmsh1
                }
                "НМШ2" -> {
                    comboBoxTestItem.items = nmsh2
                }
                "НМШ3" -> {
                    comboBoxTestItem.items = nmsh3
                }
                "НМШ4" -> {
                    comboBoxTestItem.items = nmsh4
                }
                "АНШ2" -> {
                    comboBoxTestItem.items = ansh2
                }
                "РЭЛ1" -> {
                    comboBoxTestItem.items = rel1
                }
                "РЭЛ2" -> {
                    comboBoxTestItem.items = rel2
                }
                else -> {
                    comboBoxTestItem.items = observableListOf()
                }
            }
            runLater {
                comboBoxTestItem.show()
                comboBoxTestItem.hide()
            }
        }

        controller.refreshObjectsTypes()
        comboBoxTestItem.selectionModel.selectFirst()
    }

    override val root = borderpane {
        top {
            mainMenubar = menubar {
                menu("Меню") {
                    item("Очистить") {
                        action {
                            textFieldSerialNumber.text = ""
                            comboBoxTestItem.selectionModel.clearSelection()
                            comboBoxTypeItem.selectionModel.clearSelection()
                        }
                    }
                    item("Выход") {
                        action {
                            exitProcess(0)
                        }
                    }
                }
                menu("База данных") {
                    item("Объекты испытания") {
                        action {
                            find<ObjectTypeEditorWindow>().openModal(
                                modality = Modality.WINDOW_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
                    }
                    menu("Информация") {
                        item("Версия ПО") {
                            action {
                                controller.showAboutUs()
                            }
                        }
                    }
                }.addClass(megaHard)
            }
        }
        center {
            tabpane {
                tab("Испытания") {
                    isClosable = false
                    anchorpane {
//                img = imageview(Image("back2.png"))
//                img.fitHeight = Screen.getPrimary().bounds.height - 400
                        vbox(spacing = 32.0) {
                            anchorpaneConstraints {
                                leftAnchor = 16.0
                                rightAnchor = 16.0
                                topAnchor = 16.0
                                bottomAnchor = 16.0
                            }
                            alignmentProperty().set(Pos.CENTER)
                            hbox(spacing = 64.0) {
                                alignmentProperty().set(Pos.CENTER)
                                vbox(spacing = 16.0) {
                                    alignmentProperty().set(Pos.CENTER)
                                    label("Серийный номер:")
                                    textFieldSerialNumber = textfield {
                                        prefWidth = 640.0
                                        text = ""
                                    }
                                    label("Выбор типа реле:")
                                    comboBoxTypeItem = combobox(selectedItemProperty) {
                                        items = observableListOf("НМШ1", "НМШ2", "НМШ3", "НМШ4", "АНШ2", "РЭЛ1", "РЭЛ2")
                                        selectionModel.clearSelection()
                                        prefWidth = 640.0
                                    }
                                    label("Выбор модели реле:")
                                    comboBoxTestItem = combobox {
                                        prefWidth = 640.0
                                    }
                                }
                                vbox(spacing = 16.0) {
                                    alignmentProperty().set(Pos.CENTER_LEFT)
                                    label("Выберите опыты:")
                                    checkBoxTest1 = checkbox("1. Сопротивление катушек") {}
                                    checkBoxTest2 = checkbox("2. КПС НО контактов") {}
                                    checkBoxTest3 = checkbox("3. КПС НЗ контактов") {}
                                    checkBoxTest4 = checkbox("4. Напряжение(ток) срабатывания реле") {}
                                    checkBoxTest5 = checkbox("5. Напряжение(ток) отпускания реле") {}
                                    checkBoxTest6 = checkbox("6. Время размыкания реле") {}
                                    buttonSelectAll = button("Выбрать все") {
                                        action {
                                            if (text == "Выбрать все") {
                                                checkBoxTest1.isSelected = true
                                                checkBoxTest2.isSelected = true
                                                checkBoxTest3.isSelected = true
                                                checkBoxTest4.isSelected = true
                                                checkBoxTest5.isSelected = true
                                                checkBoxTest6.isSelected = true
                                                text = "Снять все"
                                            } else {
                                                checkBoxTest1.isSelected = false
                                                checkBoxTest2.isSelected = false
                                                checkBoxTest3.isSelected = false
                                                checkBoxTest4.isSelected = false
                                                checkBoxTest5.isSelected = false
                                                checkBoxTest6.isSelected = false
                                                text = "Выбрать все"
                                            }
                                        }
                                    }
                                }
                            }
                            buttonStart = button("Запустить") {
                                prefWidth = 640.0
                                prefHeight = 128.0
                                action {
                                    controller.handleStartTest()
                                }
                            }.addClass(megaHard)
                        }
                    }
                }
                tab("Результаты") {
                    isClosable = false
                    anchorpane {
                        vbox(spacing = 8) {
                            anchorpaneConstraints {
                                leftAnchor = 16.0
                                rightAnchor = 16.0
                                topAnchor = 16.0
                                bottomAnchor = 16.0
                            }
                            alignment = Pos.CENTER

                            tableView1Result = tableview(controller.tableValuesTest1) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest1::descriptor.getter)
                                column("R1, Ом", TableValuesTest1::resistanceCoil1.getter)
                                column("R2, Ом", TableValuesTest1::resistanceCoil2.getter)
                                column("Результат", TableValuesTest1::result.getter)
                            }
                            tableView2Result = tableview(controller.tableValuesTest2) {
                                minHeight = 96.0
                                maxHeight = 96.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest2::descriptor.getter)
                                column("R1, Ом", TableValuesTest2::resistanceContactGroup1.getter)
                                column("R2, Ом", TableValuesTest2::resistanceContactGroup2.getter)
                                column("R3, Ом", TableValuesTest2::resistanceContactGroup3.getter)
                                column("R4, Ом", TableValuesTest2::resistanceContactGroup4.getter)
                                column("R5, Ом", TableValuesTest2::resistanceContactGroup5.getter)
                                column("R6, Ом", TableValuesTest2::resistanceContactGroup6.getter)
                                column("R7, Ом", TableValuesTest2::resistanceContactGroup7.getter)
                                column("R8, Ом", TableValuesTest2::resistanceContactGroup8.getter)
                                column("Результат", TableValuesTest2::result.getter)
                            }
                            tableView3Result = tableview(controller.tableValuesTest3) {
                                minHeight = 98.0
                                maxHeight = 96.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest3::descriptor.getter)
                                column("R1, Ом", TableValuesTest3::resistanceContactGroupNC1.getter)
                                column("R2, Ом", TableValuesTest3::resistanceContactGroupNC2.getter)
                                column("R3, Ом", TableValuesTest3::resistanceContactGroupNC3.getter)
                                column("R4, Ом", TableValuesTest3::resistanceContactGroupNC4.getter)
                                column("R5, Ом", TableValuesTest3::resistanceContactGroupNC5.getter)
                                column("R6, Ом", TableValuesTest3::resistanceContactGroupNC6.getter)
                                column("R7, Ом", TableValuesTest3::resistanceContactGroupNC7.getter)
                                column("R8, Ом", TableValuesTest3::resistanceContactGroupNC8.getter)
                                column("Результат", TableValuesTest3::result.getter)
                            }
                            tableView4Result = tableview(controller.tableValuesTest4) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest4::descriptor.getter)
                                column("", TableValuesTest4::voltage.getter)
                                column("Результат", TableValuesTest4::result.getter)
                            }
                            tableView5Result = tableview(controller.tableValuesTest5) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest5::descriptor.getter)
                                column("", TableValuesTest5::voltage.getter)
                                column("Результат", TableValuesTest5::result.getter)
                            }
                            tableView6Result = tableview(controller.tableValuesTest6) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest6::descriptor.getter)
                                column("t, с", TableValuesTest6::time.getter)
                                column("Результат", TableValuesTest6::result.getter)
                            }
                        }
                    }
                }
            }
        }
//        bottom = hbox {
//            alignment = Pos.CENTER_LEFT
//            comIndicate = circle(radius = 18) {
//                hboxConstraints {
//                    hGrow = Priority.ALWAYS
//                    marginLeft = 14.0
//                    marginBottom = 8.0
//                }
//                fill = c("cyan")
//                stroke = c("black")
//                isSmooth = true
//            }
//            label(" Связь со стендом") {
//                hboxConstraints {
//                    hGrow = Priority.ALWAYS
//                    marginBottom = 8.0
//                }
//            }
//        }
    }.addClass(Styles.blueTheme, megaHard)

    fun start1Test() {
        replaceWith<Test1View>(transitionLeft)
    }

    fun start2Test() {
        replaceWith<Test2View>(transitionLeft)
    }

    fun start4Test() {
        replaceWith<Test4View>(transitionLeft)
    }

    fun start3Test() {
        replaceWith<Test3View>(transitionLeft)
    }

    fun start5Test() {
        replaceWith<Test5View>(transitionLeft)
    }

    fun start6Test() {
        replaceWith<Test6View>(transitionLeft)
    }


}
