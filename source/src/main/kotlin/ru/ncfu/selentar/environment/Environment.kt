package ru.ncfu.selentar.environment

import ru.ncfu.selentar.view.EnvironmentView
import tornadofx.runLater
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Класс, имитирующий окружающую среду и действия автомобиля на ней.
 * Местность в данной среде разбита на квадраты.
 * Таким образом для для однозначной идентификации автомобиля достаточно трех параметров:
 *  координаты x, координаты y, и направления автомобиля, см. [Car].
 */
class Environment(private val mapView: EnvironmentView) {

    /**
     * Карта дорожного покрытия.
     * y, x
     */
    private val map = Array(12) {Array(13) { MapSurface.WASTELAND } }

    private val startPosition = Position(2, 10)
    private val startDirection = Direction.NORTH
    private val finishPosition = Position(8, 1)

    private val car = Car(startPosition, startDirection)
    var atomicIteration = AtomicInteger(1)
    var atomicTime = AtomicInteger(0)
    var atomicBestTime = AtomicInteger(-1)
    var turnsCount = AtomicInteger(0)
    var canceled = false
    var finished = false
    private var lost = false

    /**
     * Сбрасывает окружение, возвращает автомобиль на исходное значение
     */
    fun reset() {
        car.position = startPosition
        car.direction = startDirection
        atomicTime.set(0)
        atomicIteration.addAndGet(1)
        turnsCount.set(0)
        canceled = false
        finished = false
        lost = false
    }

    /**
     * Отображает текущее местоположение автомобиля на карте
     */
    fun render() {
        runLater {
            mapView.setCarAtPosition(car.position, car.direction)
        }
    }

    /**
     * Симулирует действие автомобиля.
     * @param action действие, которое необходимом соврешить автомобилю
     * @param test true в случае, если необходимо игнорировать возможные препятствия
     */
    fun step(action: Action, test: Boolean = false) {
        if (canceled && !test) throw IllegalStateException("Эксперемент уже закончился! ${getObservation()}")

        when(action) {
            Action.MOVE_FORWARD -> when(car.direction) {
                Direction.NORTH -> car.position = Position(car.position.x, car.position.y - 1)
                Direction.EAST -> car.position = Position(car.position.x + 1, car.position.y)
                Direction.SOUTH -> car.position = Position(car.position.x, car.position.y + 1)
                Direction.WEST -> car.position = Position(car.position.x - 1, car.position.y)
            }

            Action.TURN_LEFT -> when(car.direction) {
                Direction.NORTH -> car.direction = Direction.WEST
                Direction.EAST -> car.direction = Direction.NORTH
                Direction.SOUTH -> car.direction = Direction.EAST
                Direction.WEST -> car.direction = Direction.SOUTH
            }

            Action.TURN_RIGHT -> when(car.direction) {
                Direction.NORTH -> car.direction = Direction.EAST
                Direction.EAST -> car.direction = Direction.SOUTH
                Direction.SOUTH -> car.direction = Direction.WEST
                Direction.WEST -> car.direction = Direction.NORTH
            }

            Action.TURN -> when(car.direction) {
                Direction.NORTH -> car.direction = Direction.SOUTH
                Direction.EAST -> car.direction = Direction.WEST
                Direction.SOUTH -> car.direction = Direction.NORTH
                Direction.WEST -> car.direction = Direction.EAST
            }
        }

        atomicTime.addAndGet(1)
        if (action != Action.MOVE_FORWARD) {
            turnsCount.addAndGet(1)
        }
        setState()
    }

    /**
     * Возвращает известные автомобилю данные окружающей среды, см. [Observation]
     */
    fun getObservation() : Observation {
        var forward = MapSurface.WASTELAND
        try {
            forward = when (car.direction) {
                Direction.NORTH -> map[car.position.y - 1][car.position.x]
                Direction.EAST -> map[car.position.y][car.position.x + 1]
                Direction.SOUTH -> map[car.position.y + 1][car.position.x]
                Direction.WEST -> map[car.position.y][car.position.x - 1]
            }
        } catch (e: Exception) {
            //DO NOTHING, IT'S WASTELAND
        }

        return Observation(
            forward = forward,
            position = car.position,
            direction = car.direction,
            canceled = canceled,
            finished = finished,
            lost = lost,
            time = atomicTime.get(),
            iteration = atomicIteration.get(),
            turnsCount = turnsCount.get()
        )
    }

    private fun setState() {
        if (car.position == finishPosition) {
            canceled = true
            finished = true
            if (atomicTime.get() < atomicBestTime.get() || atomicBestTime.get() == -1) {
                atomicBestTime.set(atomicTime.get())
            }
        } else if (map[car.position.y][car.position.x] != MapSurface.ROAD) {
            canceled = true
            lost = true
        } else {
            canceled = false
            finished = false
            lost = false
        }
    }

    init {
        map[10][2] = MapSurface.ROAD

        map[9][2] = MapSurface.ROAD
        map[9][3] = MapSurface.ROAD
        map[9][4] = MapSurface.ROAD
        map[9][5] = MapSurface.ROAD
        map[9][6] = MapSurface.ROAD
        map[9][7] = MapSurface.ROAD
        map[9][8] = MapSurface.ROAD

        map[8][2] = MapSurface.STOP
        map[8][5] = MapSurface.ROAD
        map[8][8] = MapSurface.STOP

        map[7][2] = MapSurface.ROAD
        map[7][3] = MapSurface.ROAD
        map[7][4] = MapSurface.ROAD
        map[7][5] = MapSurface.ROAD
        map[7][6] = MapSurface.ROAD
        map[7][7] = MapSurface.ROAD
        map[7][8] = MapSurface.ROAD
        map[7][9] = MapSurface.ROAD
        map[7][10] = MapSurface.ROAD
        map[7][11] = MapSurface.ROAD

        map[6][2] = MapSurface.ROAD
        map[6][5] = MapSurface.STOP
        map[6][8] = MapSurface.ROAD
        map[6][11] = MapSurface.ROAD

        map[5][2] = MapSurface.STOP
        map[5][5] = MapSurface.ROAD
        map[5][8] = MapSurface.ROAD
        map[5][11] = MapSurface.ROAD

        map[4][1] = MapSurface.ROAD
        map[4][2] = MapSurface.ROAD
        map[4][3] = MapSurface.ROAD
        map[4][4] = MapSurface.ROAD
        map[4][5] = MapSurface.ROAD
        map[4][6] = MapSurface.ROAD
        map[4][7] = MapSurface.ROAD
        map[4][8] = MapSurface.ROAD
        map[4][9] = MapSurface.STOP
        map[4][10] = MapSurface.ROAD
        map[4][11] = MapSurface.ROAD

        map[3][5] = MapSurface.ROAD
        map[3][8] = MapSurface.STOP
        map[3][11] = MapSurface.ROAD

        map[2][5] = MapSurface.ROAD
        map[2][6] = MapSurface.ROAD
        map[2][7] = MapSurface.ROAD
        map[2][8] = MapSurface.ROAD
        map[2][9] = MapSurface.ROAD
        map[2][10] = MapSurface.ROAD
        map[2][11] = MapSurface.ROAD

        map[1][8] = MapSurface.ROAD
    }

}