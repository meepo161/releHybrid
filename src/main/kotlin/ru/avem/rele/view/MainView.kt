package ru.avem.rele.view

import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.shape.Circle
import javafx.stage.Modality
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import ru.avem.rele.controllers.*
import ru.avem.rele.database.entities.TestObjectsType
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
    var comIndicate: Circle by singleAssign()

    private var imgPressure: ImageView by singleAssign()
    private var img: ImageView by singleAssign()

    private var addIcon = ImageView("ru/avem/rele/icon/add.png")
    private var deleteIcon = ImageView("ru/avem/rele/icon/delete.png")
    private var editIcon = ImageView("ru/avem/rele/icon/edit.png")
    private var pressureJPG = Image("ru/avem/rele/icon/pressure.jpg", 400.0, 280.0, false, true)


    var comboBoxTestItem: ComboBox<TestObjectsType> by singleAssign()

    var buttonStart: Button by singleAssign()
    var buttonSelectAll: Button by singleAssign()
    var checkBoxTest1: CheckBox by singleAssign()
    var checkBoxTest2: CheckBox by singleAssign()
    var checkBoxTest3: CheckBox by singleAssign()
    var checkBoxTest4: CheckBox by singleAssign()
    var checkBoxTest5: CheckBox by singleAssign()
    var checkBoxTest6: CheckBox by singleAssign()
    var textFieldSerialNumber: TextField by singleAssign()

    var test1Modal: Stage = Stage()

    var test1Controller = Test1Controller()
    var test2Controller = Test2Controller()
    var test3Controller = Test4Controller()
    var test4Controller = Test5Controller()
    var test5Controller = Test6Controller()


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
        controller.refreshObjectsTypes()
        comboBoxTestItem.selectionModel.selectFirst()
    }

    override val root = borderpane {
        top {
            mainMenubar = menubar {
                menu("Меню") {
                    item("Очистить") {
                        action {}
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
                    item("Протоколы") {
                        action {
                            find<ProtocolListWindow>().openModal(
                                modality = Modality.APPLICATION_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
                    }
                }
                menu("Отладка") {
                    item("Связь с приборами") {
                        action {
                            find<DevicesView>().openModal(
                                modality = Modality.APPLICATION_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
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
                                        text = "1234"
                                    }
                                    label("Выберите тип реле:")
                                    combobox<String> {
                                        items = observableListOf("1", "2", "3", "4")
                                        selectionModel.selectFirst()
                                        prefWidth = 640.0
                                    }
                                    label("Выберите реле:")
                                    comboBoxTestItem = combobox {
                                        prefWidth = 640.0
                                    }
                                }
                                vbox(spacing = 16.0) {
                                    alignmentProperty().set(Pos.CENTER_LEFT)
                                    label("Выберите опыты:")
//                            checkbox("Выбрать все") {
//
//                            }
//                                    buttonSelectAll = button("Выбрать все") {
//                                        action {
//                                            if (text == "Выбрать все") {
//                                                checkBoxTest1.isSelected = true
//                                                checkBoxTest2.isSelected = true
//                                                checkBoxTest3.isSelected = true
//                                                checkBoxTest4.isSelected = true
//                                                checkBoxTest5.isSelected = true
//                                                checkBoxTest6.isSelected = true
//                                                text = "Развыбрать все"
//                                            } else {
//                                                checkBoxTest1.isSelected = false
//                                                checkBoxTest2.isSelected = false
//                                                checkBoxTest3.isSelected = false
//                                                checkBoxTest4.isSelected = false
//                                                checkBoxTest5.isSelected = false
//                                                checkBoxTest6.isSelected = false
//                                                text = "Выбрать все"
//                                            }
//                                        }
//                                    }
                                    checkBoxTest1 = checkbox("1. ИКАС катушки") {}
                                    checkBoxTest2 = checkbox("2. ИКАС (переходного контакта NO)") {}
                                    checkBoxTest3 = checkbox("3. ИКАС (переходного контакта NC)") {}
                                    checkBoxTest4 = checkbox("4. Опыт минимального срабатывания реле") {}
                                    checkBoxTest5 = checkbox("5. Опыт максимального отпускания реле") {}
                                    checkBoxTest6 = checkbox("6. Время размыкания реле") {}
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

                            tableview(controller.tableValuesTest1) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest1::descriptor.getter)
                                column("R1, Ом", TableValuesTest1::resistanceCoil1.getter)
                                column("R2, Ом", TableValuesTest1::resistanceCoil2.getter)
                                column("Результат", TableValuesTest1::result.getter)
                            }
                            tableview(controller.tableValuesTest2) {
                                minHeight = 146.0
                                maxHeight = 146.0
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
                            tableview(controller.tableValuesTest3) {
                                minHeight = 146.0
                                maxHeight = 146.0
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
                            tableview(controller.tableValuesTest4) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest4::descriptor.getter)
                                column("U, В", TableValuesTest4::voltage.getter)
                                column("Результат", TableValuesTest4::result.getter)
                            }
                            tableview(controller.tableValuesTest5) {
                                minHeight = 146.0
                                maxHeight = 146.0
                                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                mouseTransparentProperty().set(true)

                                column("", TableValuesTest5::descriptor.getter)
                                column("U, В", TableValuesTest5::voltage.getter)
                                column("Результат", TableValuesTest5::result.getter)
                            }
                            tableview(controller.tableValuesTest6) {
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
