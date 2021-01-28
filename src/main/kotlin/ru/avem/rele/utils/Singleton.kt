package ru.avem.rele.utils

import ru.avem.rele.database.entities.Protocol
import ru.avem.rele.database.entities.TestObjectsType


object Singleton {
    lateinit var currentProtocol: Protocol
    lateinit var currentTestItem: TestObjectsType
    lateinit var currentTestItemType: String
}
