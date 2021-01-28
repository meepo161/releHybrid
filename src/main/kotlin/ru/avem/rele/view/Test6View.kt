package ru.avem.rele.view

import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import ru.avem.rele.controllers.MainViewController
import ru.avem.rele.controllers.Test6Controller
import ru.avem.rele.entities.TableValuesTest6
import ru.avem.rele.utils.LogTag
import ru.avem.rele.utils.State
import ru.avem.rele.utils.Toast
import ru.avem.rele.utils.transitionRight
import tornadofx.*

class Test6View : View("Тест6") {
    private val controller: Test6Controller by inject()
    private val mainController: MainViewController by inject()
    private val mainView: MainView by inject()

    var tableView6Test: TableView<TableValuesTest6> by singleAssign()

    var vBoxLog: VBox by singleAssign()
    var circleComStatus: Circle by singleAssign()

    var buttonBack: Button by singleAssign()
    var buttonStartStopTest: Button by singleAssign()
    var buttonNextTest: Button by singleAssign()

    var progressBarTime: ProgressBar by singleAssign()

//    private var lineChart: LineChart<Number, Number> by singleAssign()
    var series = XYChart.Series<Number, Number>()


    override fun onBeforeShow() {
    }

    override fun onDock() {
        controller.setExperimentProgress(0)
        controller.clearTable()
        controller.clearLog()
        controller.appendMessageToLog(LogTag.MESSAGE, "Нажмите <Старт> для начала испытания")
        circleComStatus.fill = State.BAD.c
        if (mainController.auto) {
            controller.startTest()
        }
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

            label("Определение времени отпускания реле") {

                alignmentProperty().set(Pos.CENTER)
            }.addClass(Styles.megaHard)

            tableView6Test = tableview(controller.tableValues) {

                minHeight = 146.0
                maxHeight = 146.0

                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                mouseTransparentProperty().set(true)

                column("", TableValuesTest6::descriptor.getter)
                column("t, с", TableValuesTest6::time.getter)
                column("Результат", TableValuesTest6::result.getter)
            }

//            lineChart = linechart("", NumberAxis(), NumberAxis()) {
//                xAxis.label = "xAxis"
//                minHeight = 300.0
//                data.add(series)
//                animated = false
//                createSymbols = false
//                isLegendVisible = false
//            }.addClass(Styles.lineChart)

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

    fun startNextExperiment() {
        runLater {
            replaceWith<MainView>(transitionRight)
            Toast.makeText("Выбранные испытания завершены").show(Toast.ToastType.INFORMATION)
        }
    }
}
