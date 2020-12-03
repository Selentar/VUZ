package ru.ncfu.selentar.environment

import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment
import java.util.*

enum class Action {
    MOVE_FORWARD,
    TURN_LEFT,
    TURN_RIGHT,
    TURN
}

public fun main(vararg args: String) {
    val configuration = Configuration()
    val prop = Properties()
    prop.put(Environment.URL, "jdbc:h2:mem:test")

    configuration.setProperties(prop)

    val sFactory = configuration.buildSessionFactory()
    val sesssion = sFactory.openSession()


    sesssion.close()
    sFactory.close()
}
