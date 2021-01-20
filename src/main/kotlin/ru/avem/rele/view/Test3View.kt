package ru.avem.rele.view

import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import ru.avem.rele.controllers.MainViewController
import ru.avem.rele.controllers.Test3Controller
import ru.avem.rele.entities.TableValuesTest3
import ru.avem.rele.utils.*
import tornadofx.*

class Test3View : View("Тест3") {
    private val controller: Test3Controller by inject()
    private val mainController: MainViewController by inject()


    var vBoxLog: VBox by singleAssign()
    var circleComStatus: Circle by singleAssign()

    var buttonBack: Button by singleAssign()
    var buttonStartStopTest: Button by singleAssign()
    var buttonNextTest: Button by singleAssign()

    var progressBarTime: ProgressBar by singleAssign()

    override fun onBeforeShow() {
    }

    override fun onDock() {
        controller.setExperimentProgress(0)
        controller.clearTable()
        controller.clearLog()
        controller.appendMessageToLog(LogTag.MESSAGE, "Нажмите <Старт> для начала испытания")
        circleComStatus.fill = State.BAD.c
//        controller.fillTableByEO(mainView.comboBoxTestItem as TestObjectsType, mainView.textFieldSerialNumber.toString())
    }

    private val topSide = anchorpane {
        vbox(spacing = 48.0) {
            anchorpaneConstraints {
                leftAnchor = 32.0
                rightAnchor = 32.0
                topAnchor = 32.0
                bottomAnchor = 32.0
            }
            alignment = Pos.CENTER

            label("Измерение сопротивления переходных контактов NC") {

                alignmentProperty().set(Pos.CENTER)
            }.addClass(Styles.megaHard)

            tableview(controller.tableValues) {

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

            hbox(spacing = 48.0) {

                alignmentProperty().set(Pos.CENTER)

                buttonBack = button("В меню") {
                    action {
                        replaceWith<MainView>(
                            transitionRight
                        )
                    }
                }.addClass(Styles.megaHard)

                buttonStartStopTest = button("Старт") {
                    action {
                        if (controller.isExperimentEnded) {
                            controller.startTest()
                        } else {
                            controller.cause = "Отменено оператором"
                        }
                    }
                }.addClass(Styles.megaHard)

                buttonNextTest = button("Далее") {
                    action {
                        startNextExperiment()
                    }
                }.addClass(Styles.megaHard)
            }
        }
    }

    private val bottomSide = anchorpane {
        tabpane {
            anchorpaneConstraints {
                leftAnchor = 0.0
                rightAnchor = 0.0
                topAnchor = 0.0
                bottomAnchor = 0.0
            }

            tabClosingPolicyProperty().set(TabPane.TabClosingPolicy.UNAVAILABLE)

            tab("Ход испытания") {
                anchorpane {
                    scrollpane {
                        anchorpaneConstraints {
                            leftAnchor = 16.0
                            rightAnchor = 16.0
                            topAnchor = 16.0
                            bottomAnchor = 16.0
                        }

                        vBoxLog = vbox { }.addClass(Styles.megaHard)

                        vvalueProperty().bind(vBoxLog.heightProperty())
                    }
                }
            }.addClass(Styles.medium)
        }
    }

    override val root = borderpane {
        center = splitpane(Orientation.VERTICAL, topSide, bottomSide) {
            prefWidth = 1200.0
            prefHeight = 700.0

            setDividerPositions(0.6)
        }

        bottom = anchorpane {
            label("Состояние RS-485:") {
                anchorpaneConstraints {
                    leftAnchor = 16.0
                    topAnchor = 4.0
                    bottomAnchor = 4.0
                }
            }

            circleComStatus = circle(radius = 8.0) {
                anchorpaneConstraints {
                    leftAnchor = 125.0
                    topAnchor = 5.0
                    bottomAnchor = 2.0
                }

                stroke = c("black")
            }

            label("Прогресс испытания:") {
                anchorpaneConstraints {
                    leftAnchor = 200.0
                    topAnchor = 4.0
                    bottomAnchor = 4.0
                }
            }

            progressBarTime = progressbar {
                anchorpaneConstraints {
                    leftAnchor = 330.0
                    rightAnchor = 16.0
                    topAnchor = 6.0
                    bottomAnchor = 4.0
                }
                progress = 0.0
            }
        }.addClass(Styles.anchorPaneBorders)
    }.addClass(Styles.blueTheme)

    private fun startNextExperiment() {
        when {
            mainController.maskTests and 8 > 0 -> {
                replaceWith<Test4View>(transitionLeft)
            }
            mainController.maskTests and 16 > 0 -> {
                replaceWith<Test5View>(transitionLeft)
            }
            mainController.maskTests and 32 > 0 -> {
                replaceWith<Test6View>(transitionLeft)
            }
            else -> {
                replaceWith<MainView>(transitionRight)
                Toast.makeText("Выбранные испытания завершены").show(Toast.ToastType.INFORMATION)
            }
        }
    }

}