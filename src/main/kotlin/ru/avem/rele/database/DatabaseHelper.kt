package ru.avem.rele.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.rele.database.entities.*
import ru.avem.rele.database.entities.Users.login
import java.sql.Connection

fun validateDB() {
    Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        SchemaUtils.create(Users, ProtocolsTable, ObjectsTypes)
    }

    transaction {
        if (User.all().count() < 2) {
            val admin = User.find {
                login eq "admin"
            }

            if (admin.empty()) {
                User.new {
                    login = "admin"
                    password = "avem"
                    fullName = "admin"
                }
            }

            if (TestObjectsType.all().count() < 1) {
                TestObjectsType.new {
                    serialNumber = "НМШ1-400"
                    resistanceCoil1 = "200"
                    resistanceCoil2 = "200"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.3"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.2"
                }
                TestObjectsType.new {
                    serialNumber = "НМШ1-1440"
                    resistanceCoil1 = "720"
                    resistanceCoil2 = "720"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "6"
                    voltageOrCurrentMin = "14.2"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НМШ1-7000"
                    resistanceCoil1 = "3500"
                    resistanceCoil2 = "3500"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "15"
                    voltageOrCurrentMin = "41"
                    voltageOrCurrentNom = "60"
                    voltageOrCurrentOverload = "100"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НМШМ1-11"
                    resistanceCoil1 = "11"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.05"
                    voltageOrCurrentMin = "0.16"
                    voltageOrCurrentNom = "0.25"
                    voltageOrCurrentOverload = "0.45"
                    timeOff = "0.45"
                }
                TestObjectsType.new {
                    serialNumber = "НМШМ1-22"
                    resistanceCoil1 = "11"
                    resistanceCoil2 = "11"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.025"
                    voltageOrCurrentMin = "0.08"
                    voltageOrCurrentNom = "0.125"
                    voltageOrCurrentOverload = "0.25"
                    timeOff = "0.2"
                }

                TestObjectsType.new {
                    serialNumber = "НМШМ1-180"
                    resistanceCoil1 = "180"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.45"
                }

                TestObjectsType.new {
                    serialNumber = "НМШМ1-360"
                    resistanceCoil1 = "180"
                    resistanceCoil2 = "180"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.2"
                }

                TestObjectsType.new {
                    serialNumber = "НМШМ1-560"
                    resistanceCoil1 = "560"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "4.6"
                    voltageOrCurrentMin = "14"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "0.45"
                }

                TestObjectsType.new {
                    serialNumber = "НМШМ1-1120"
                    resistanceCoil1 = "560"
                    resistanceCoil2 = "560"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "4.6"
                    voltageOrCurrentMin = "14"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "0.2"
                }

                TestObjectsType.new {
                    serialNumber = "НМШМ1-1000/560"
                    resistanceCoil1 = "1000"
                    resistanceCoil2 = "560"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "5"
                    voltageOrCurrentMin = "17"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "0.2"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ2-900"
                    resistanceCoil1 = "450"
                    resistanceCoil2 = "450"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ2-4000"
                    resistanceCoil1 = "2000"
                    resistanceCoil2 = "2000"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "5"
                    voltageOrCurrentMin = "16"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ2-12000"
                    resistanceCoil1 = "6000"
                    resistanceCoil2 = "6000"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "9"
                    voltageOrCurrentMin = "29"
                    voltageOrCurrentNom = "45"
                    voltageOrCurrentOverload = "75"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШМ2-1.5"
                    resistanceCoil1 = "1.5"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.076"
                    voltageOrCurrentMin = "0.25"
                    voltageOrCurrentNom = "0.35"
                    voltageOrCurrentOverload = "0.7"
                    timeOff = "0.55"
                }
                TestObjectsType.new {
                    serialNumber = "НМШМ2-320"
                    resistanceCoil1 = "320"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.3"
                }

                TestObjectsType.new {
                    serialNumber = "НМШМ2-640"
                    resistanceCoil1 = "320"
                    resistanceCoil2 = "320"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.3"
                }

                TestObjectsType.new {
                    serialNumber = "НМШМ2-1500"
                    resistanceCoil1 = "1500"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "5"
                    voltageOrCurrentMin = "16"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "0.6"
                }
                TestObjectsType.new {
                    serialNumber = "НМШМ2-3000"
                    resistanceCoil1 = "1500"
                    resistanceCoil2 = "1500"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "5"
                    voltageOrCurrentMin = "16"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "0.6"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ3-460/400"
                    resistanceCoil1 = "460"
                    resistanceCoil2 = "400"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.004"
                    voltageOrCurrentMin = "0.0134"
                    voltageOrCurrentNom = "0"
                    voltageOrCurrentOverload = "0.055"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ4-3"
                    resistanceCoil1 = "1.5"
                    resistanceCoil2 = "1.5"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.049"
                    voltageOrCurrentMin = "0.147"
                    voltageOrCurrentNom = "0.2"
                    voltageOrCurrentOverload = "0.8"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ4-3.4"
                    resistanceCoil1 = "1.7"
                    resistanceCoil2 = "1.7"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "0.045"
                    voltageOrCurrentMin = "0.135"
                    voltageOrCurrentNom = "0.2"
                    voltageOrCurrentOverload = "0.8"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ4-530"
                    resistanceCoil1 = "265"
                    resistanceCoil2 = "265"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2"
                    voltageOrCurrentMin = "6.8"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ4-600"
                    resistanceCoil1 = "300"
                    resistanceCoil2 = "300"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ4-2400"
                    resistanceCoil1 = "1200"
                    resistanceCoil2 = "1200"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "4.4"
                    voltageOrCurrentMin = "14.3"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "1"
                }

                TestObjectsType.new {
                    serialNumber = "НМШ4-3000"
                    resistanceCoil1 = "1500"
                    resistanceCoil2 = "1500"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "5"
                    voltageOrCurrentMin = "16"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "45"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НМШМ4-250"
                    resistanceCoil1 = "250"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.2"
                }
                TestObjectsType.new {
                    serialNumber = "НМШМ4-500"
                    resistanceCoil1 = "250"
                    resistanceCoil2 = "250"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.3"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.2"
                }
                TestObjectsType.new {
                    serialNumber = "АНШМ2-310"
                    resistanceCoil1 = "310"
                    resistanceCoil2 = "310"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "1.6"
                    voltageOrCurrentMin = "6.7"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.9"
                }
                TestObjectsType.new {
                    serialNumber = "АНШМ2-620"
                    resistanceCoil1 = "310"
                    resistanceCoil2 = "310"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "1.6"
                    voltageOrCurrentMin = "6.7"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.9"
                }
                TestObjectsType.new {
                    serialNumber = "АНШМ2-760"
                    resistanceCoil1 = "380"
                    resistanceCoil2 = "380"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "1.8"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "0.5"
                }
                TestObjectsType.new {
                    serialNumber = "АНШ2-2"
                    resistanceCoil1 = "1"
                    resistanceCoil2 = "1"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.055"
                    voltageOrCurrentMin = "0.135"
                    voltageOrCurrentNom = "0.2"
                    voltageOrCurrentOverload = "0.54"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "АНШ2-37"
                    resistanceCoil1 = "18.5"
                    resistanceCoil2 = "18.5"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "0.27"
                    voltageOrCurrentMin = "1.15"
                    voltageOrCurrentNom = "1.8"
                    voltageOrCurrentOverload = "3.5"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "АНШ2-40"
                    resistanceCoil1 = "20"
                    resistanceCoil2 = "20"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "0.29"
                    voltageOrCurrentMin = "1.2"
                    voltageOrCurrentNom = "1.8"
                    voltageOrCurrentOverload = "3.5"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "АНШ2-310"
                    resistanceCoil1 = "116"
                    resistanceCoil2 = "195"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "1.4"
                    voltageOrCurrentMin = "3.5"
                    voltageOrCurrentNom = "5.3"
                    voltageOrCurrentOverload = "10"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "АНШ2-700"
                    resistanceCoil1 = "350"
                    resistanceCoil2 = "350"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "1.9"
                    voltageOrCurrentMin = "5.3"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "АНШ2-1230"
                    resistanceCoil1 = "615"
                    resistanceCoil2 = "615"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.3"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "20"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "РЭЛ1-1600"
                    resistanceCoil1 = "800"
                    resistanceCoil2 = "800"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "5"
                    voltageOrCurrentMin = "16"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "32"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "РЭЛ1М-600"
                    resistanceCoil1 = "300"
                    resistanceCoil2 = "300"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "4"
                    voltageOrCurrentMin = "14.2"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "32"
                    timeOff = "0.2"
                }
                TestObjectsType.new {
                    serialNumber = "РЭЛ1-400"
                    resistanceCoil1 = "200"
                    resistanceCoil2 = "200"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.5"
                    voltageOrCurrentMin = "8"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "16"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "РЭЛ1М-160"
                    resistanceCoil1 = "80"
                    resistanceCoil2 = "80"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2"
                    voltageOrCurrentMin = "7.2"
                    voltageOrCurrentNom = "12"
                    voltageOrCurrentOverload = "16"
                    timeOff = "0.2"
                }
                TestObjectsType.new {
                    serialNumber = "РЭЛ2-2400"
                    resistanceCoil1 = "1200"
                    resistanceCoil2 = "1200"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "4.5"
                    voltageOrCurrentMin = "16"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "32"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "РЭЛ2М-1000"
                    resistanceCoil1 = "500"
                    resistanceCoil2 = "500"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "4"
                    voltageOrCurrentMin = "14.5"
                    voltageOrCurrentNom = "24"
                    voltageOrCurrentOverload = "32"
                    timeOff = "0.3"
                }
                TestObjectsType.new {
                    serialNumber = "РЭЛ1-6.8"
                    resistanceCoil1 = "3.4"
                    resistanceCoil2 = "3.4"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.042"
                    voltageOrCurrentMin = "0.145"
                    voltageOrCurrentNom = "0.22"
                    voltageOrCurrentOverload = "0.8"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "РЭЛ1М-10"
                    resistanceCoil1 = "5"
                    resistanceCoil2 = "5"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "0.05"
                    voltageOrCurrentMin = "0.176"
                    voltageOrCurrentNom = "0.26"
                    voltageOrCurrentOverload = "0.5"
                    timeOff = "0.2"
                }
//                TestObjectsType.new {
//                    serialNumber = "БН4М-360"
//                    resistanceCoil1 = "180"
//                    resistanceCoil2 = "180"
//                    voltageOrCurrent = "Напряжение"
//                    voltageOrCurrentMax = "2.5"
//                    voltageOrCurrentMin = "8"
//                    voltageOrCurrentNom = "12"
//                    voltageOrCurrentOverload = "16"
//                    timeOff = "0.2"
//                }
                TestObjectsType.new {
                    serialNumber = "НШ1-2"
                    resistanceCoil1 = "1"
                    resistanceCoil2 = "1"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.055"
                    voltageOrCurrentMin = "0.17"
                    voltageOrCurrentNom = "0.3"
                    voltageOrCurrentOverload = "0.68"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НШ1-400/30"
                    resistanceCoil1 = "400"
                    resistanceCoil2 = "30"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.5"
                    voltageOrCurrentMin = "8"
                    voltageOrCurrentNom = "16"
                    voltageOrCurrentOverload = "32"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НШ1-800"
                    resistanceCoil1 = "400"
                    resistanceCoil2 = "400"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.5"
                    voltageOrCurrentMin = "8"
                    voltageOrCurrentNom = "16"
                    voltageOrCurrentOverload = "32"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НШ1-2000"
                    resistanceCoil1 = "1000"
                    resistanceCoil2 = "1000"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "3"
                    voltageOrCurrentMin = "12"
                    voltageOrCurrentNom = "20"
                    voltageOrCurrentOverload = "36"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НШ1-9000"
                    resistanceCoil1 = "4500"
                    resistanceCoil2 = "4500"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "10"
                    voltageOrCurrentMin = "40"
                    voltageOrCurrentNom = "80"
                    voltageOrCurrentOverload = "144"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НШ1М-200/30"
                    resistanceCoil1 = "200"
                    resistanceCoil2 = "30"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "16"
                    voltageOrCurrentOverload = "30.8"
                    timeOff = "0.3"
                }
                TestObjectsType.new {
                    serialNumber = "НШ1М-400"
                    resistanceCoil1 = "200"
                    resistanceCoil2 = "200"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "16"
                    voltageOrCurrentOverload = "30"
                    timeOff = "0.55"
                }
                TestObjectsType.new {
                    serialNumber = "НШ1М-200/400"
                    resistanceCoil1 = "200"
                    resistanceCoil2 = "400"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "16"
                    voltageOrCurrentOverload = "30"
                    timeOff = "0.3"
                }
                TestObjectsType.new {
                    serialNumber = "НШ2-2"
                    resistanceCoil1 = "1"
                    resistanceCoil2 = "1"
                    voltageOrCurrent = "Ток"
                    voltageOrCurrentMax = "0.055"
                    voltageOrCurrentMin = "0.135"
                    voltageOrCurrentNom = "0.27"
                    voltageOrCurrentOverload = "0.54"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НШ2-40"
                    resistanceCoil1 = "20"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "0.3"
                    voltageOrCurrentMin = "1.2"
                    voltageOrCurrentNom = "2.5"
                    voltageOrCurrentOverload = "4.5"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НШ2-2000"
                    resistanceCoil1 = "1000"
                    resistanceCoil2 = "1000"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "2.2"
                    voltageOrCurrentMin = "7.5"
                    voltageOrCurrentNom = "15"
                    voltageOrCurrentOverload = "30"
                    timeOff = "1"
                }
                TestObjectsType.new {
                    serialNumber = "НПШ1-150"
                    resistanceCoil1 = "300"
                    resistanceCoil2 = "0"
                    voltageOrCurrent = "Напряжение"
                    voltageOrCurrentMax = "8"
                    voltageOrCurrentMin = "8"
                    voltageOrCurrentNom = "15"
                    voltageOrCurrentOverload = "32"
                    timeOff = "1"
                }
            }
        }
    }
}
