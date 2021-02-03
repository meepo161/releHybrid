package ru.avem.rele.app

import javafx.scene.image.Image
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import javafx.stage.StageStyle
import ru.avem.rele.database.validateDB
import ru.avem.rele.view.MainView
import ru.avem.rele.view.Styles
import tornadofx.App
import tornadofx.FX

class Rele : App(MainView::class, Styles::class) {

    companion object {
        var isAppRunning = true
    }

    override fun init() {
        validateDB()
    }

    override fun start(stage: Stage) {
        stage.isFullScreen = true
        stage.isResizable = false
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.fullScreenExitKeyCombination = KeyCombination.NO_MATCH
        super.start(stage)
        FX.primaryStage.icons += Image("icon.png")
    }


    override fun stop() {
        isAppRunning = false
    }
}
