package ru.avem.rele.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ObjectsTypes:  IntIdTable() {
    val serialNumber = varchar("serialNumber", 1024)
    val resistanceCoil1 = varchar("resistanceCoil1", 1024)
    val resistanceCoil2 = varchar("resistanceCoil2", 1024)
    val voltageOrCurrent = varchar("voltageOrCurrent", 1024)
    val voltageOrCurrentNom = varchar("voltageOrCurrentNom", 1024)
    val voltageOrCurrentMin = varchar("voltageOrCurrentMin", 1024)
    val voltageOrCurrentMax = varchar("voltageOrCurrentMax", 1024)
    val voltageOrCurrentOverload = varchar("voltageOrCurrentOverload", 1024)
    val timeOff = varchar("timeOff", 1024)
}

class TestObjectsType(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<TestObjectsType>(ObjectsTypes)
    var serialNumber               by ObjectsTypes.serialNumber
    var resistanceCoil1           by ObjectsTypes.resistanceCoil1
    var resistanceCoil2           by ObjectsTypes.resistanceCoil2
    var voltageOrCurrent          by ObjectsTypes.voltageOrCurrent
    var voltageOrCurrentNom      by ObjectsTypes.voltageOrCurrentNom
    var voltageOrCurrentMin      by ObjectsTypes.voltageOrCurrentMin
    var voltageOrCurrentMax      by ObjectsTypes.voltageOrCurrentMax
    var voltageOrCurrentOverload by ObjectsTypes.voltageOrCurrentOverload
    var timeOff                  by ObjectsTypes.timeOff

    override fun toString(): String {
        return serialNumber
    }
}
