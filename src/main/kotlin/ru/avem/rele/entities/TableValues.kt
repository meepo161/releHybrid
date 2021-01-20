package ru.avem.rele.entities

import javafx.beans.property.DoubleProperty
import javafx.beans.property.StringProperty

data class TableValuesTest1(
    var descriptor: StringProperty,
    var resistanceCoil1: StringProperty,
    var resistanceCoil2: StringProperty,
    var result: StringProperty
)

data class TableValuesTest2(
    var descriptor: StringProperty,
    var resistanceContactGroup1: StringProperty,
    var resistanceContactGroup2: StringProperty,
    var resistanceContactGroup3: StringProperty,
    var resistanceContactGroup4: StringProperty,
    var resistanceContactGroup5: StringProperty,
    var resistanceContactGroup6: StringProperty,
    var resistanceContactGroup7: StringProperty,
    var resistanceContactGroup8: StringProperty,
    var result: StringProperty
)

data class TableValuesTest3(
    var descriptor: StringProperty,
    var resistanceContactGroupNC1: StringProperty,
    var resistanceContactGroupNC2: StringProperty,
    var resistanceContactGroupNC3: StringProperty,
    var resistanceContactGroupNC4: StringProperty,
    var resistanceContactGroupNC5: StringProperty,
    var resistanceContactGroupNC6: StringProperty,
    var resistanceContactGroupNC7: StringProperty,
    var resistanceContactGroupNC8: StringProperty,
    var result: StringProperty
)

data class TableValuesTest4(
    var descriptor: StringProperty, var voltage: DoubleProperty, var result: StringProperty
)

data class TableValuesTest5(
    var descriptor: StringProperty, var voltage: DoubleProperty, var result: StringProperty
)

data class TableValuesTest6(
    var descriptor: StringProperty, var time: DoubleProperty, var result: StringProperty
)

data class TableValuesResult(
    var descriptor: StringProperty,
    var resistanceCoil1: DoubleProperty,
    var resistanceCoil2: DoubleProperty,
    var result1: StringProperty,
    var resistanceContactGroup1: DoubleProperty,
    var resistanceContactGroup2: DoubleProperty,
    var resistanceContactGroup3: DoubleProperty,
    var resistanceContactGroup4: DoubleProperty,
    var resistanceContactGroup5: DoubleProperty,
    var resistanceContactGroup6: DoubleProperty,
    var resistanceContactGroup7: DoubleProperty,
    var resistanceContactGroup8: DoubleProperty,
    var result2: StringProperty,
    var voltageMin: DoubleProperty,
    var result3: StringProperty,
    var voltageMax: DoubleProperty,
    var result4: StringProperty,
    var time: DoubleProperty,
    var result5: StringProperty
)
