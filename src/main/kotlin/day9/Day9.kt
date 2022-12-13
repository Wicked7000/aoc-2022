package day9

import Day
import checkWithMessage
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import readInput
import runTimedPart
import kotlin.math.abs

@Suppress("unused")
class Day9 : Day() {

  enum class Direction(val x: Int, val y: Int) {
    Right(1, 0),
    Up(0, 1),
    UpRight(1, 1),
    UpLeft(-1, 1),
    Left(-1, 0),
    Down(0, -1),
    DownRight(1, -1),
    DownLeft(-1, -1);

    companion object {
      fun fromValue(str: String): Direction {
        return when(str[0]) {
          'R' -> Right
          'U' -> Up
          'L' -> Left
          'D' -> Down
          else -> throw Error("Unknown character ${str[0]}")
        }
      }
    }
  }

  class Knot(var pos: Position, var name: Char)

  class Position(var x: Int, var y: Int) {
    fun applyDirection(direction: Direction): Position {
      return Position(x + direction.x, y + direction.y)
    }

    override fun equals(other: Any?): Boolean {
      if(other is Position){
        return other.x == x && other.y == y
      }
      return false
    }

    override fun hashCode(): Int {
      return x.hashCode() * 31 + y.hashCode() * 31
    }

    companion object {
      fun clone(pos: Position): Position{
        return Position(pos.x, pos.y)
      }
    }
  };

  private fun isDiagonalFromNextKnot(nextKnot: Knot, knot: Knot): Boolean {
    return listOf(Direction.UpLeft, Direction.UpRight, Direction.DownLeft, Direction.DownRight).any {
      val newPosX = knot.pos.x + it.x
      val newPosY = knot.pos.y + it.y
      newPosX == nextKnot.pos.x && newPosY == nextKnot.pos.y
    }
  }

  private fun makeValidOrthogonalMove(nextKnot: Knot, knot: Knot): Knot {
    listOf(Direction.Left, Direction.Up, Direction.Right, Direction.Down).forEach {
      val newPosX = knot.pos.x + it.x
      val newPosY = knot.pos.y + it.y
      if(abs(nextKnot.pos.x - newPosX) <= 1 && abs(nextKnot.pos.y - newPosY) <= 1){
        return Knot(Position(newPosX, newPosY), knot.name)
      }
    }
    throw Error("Unable to find valid orthogonal move!")
  }

  private fun makeValidDiagonalMove(nextKnot: Knot, knot: Knot): Knot {
    listOf(Direction.UpLeft, Direction.UpRight, Direction.DownLeft, Direction.DownRight).forEach {
      val newPosX = knot.pos.x + it.x
      val newPosY = knot.pos.y + it.y
      if(abs(nextKnot.pos.x - newPosX) <= 1 && abs(nextKnot.pos.y - newPosY) <= 1){
        return Knot(Position(newPosX, newPosY), knot.name)
      }
    }
    throw Error("Unable to find valid diagonal move!")
  }

  private fun parseInput(input: List<String>): List<Direction> {
    val parsedInput = mutableListOf<Direction>()
    for(line in input){
      val (directionStr, amount) = line.split(" ")
      repeat(amount.toInt()) {
        parsedInput.add(Direction.fromValue(directionStr))
      }
    }
    return parsedInput
  }

  private fun updateKnot(knot: Knot, nextKnot: Knot): Knot {
    // We are touching the head
    if(abs(nextKnot.pos.x - knot.pos.x) <= 1 && abs(nextKnot.pos.y - knot.pos.y) <= 1){
      return knot
    }

    if((abs(nextKnot.pos.x - knot.pos.x) == 2 && knot.pos.y == nextKnot.pos.y) || (abs(nextKnot.pos.y - knot.pos.y) == 2 && knot.pos.x == nextKnot.pos.x)) {
      return makeValidOrthogonalMove(nextKnot, knot)
    } else if(!isDiagonalFromNextKnot(nextKnot, knot)) {
      return makeValidDiagonalMove(nextKnot, knot)
    }

    // Don't change
    return knot
  }

  private fun renderCurrentView(pastPositions: Set<Position>, knots: List<Knot>) {
    var minX = Int.MAX_VALUE
    var maxX = Int.MIN_VALUE

    var minY = Int.MAX_VALUE
    var maxY = Int.MIN_VALUE

    for(pos in pastPositions + knots.map { it.pos }.toSet()){
      if(pos.x < minX){
        minX = pos.x
      }
      if(pos.x > maxX){
        maxX = pos.x
      }
      if(pos.y < minY){
        minY = pos.y
      }
      if(pos.y > maxY) {
        maxY = pos.y
      }
    }

    for(y in maxY downTo  minY){
      for(x in minX .. maxX){
        var renderChar = '.'
        if(pastPositions.contains(Position(x, y))){
          renderChar = '#'
        }
        for(idx in knots.lastIndex downTo 0){
          val knot = knots[idx]
          if(x == knot.pos.x && y == knot.pos.y){
            renderChar = knot.name
          }
        }
        if(x == 0 && y == 0){
          renderChar = 's'
        }
        print(renderChar)
      }
      println()
    }
    println()
  }

  private fun part1(input: List<String>): Int {
    val positionSet = mutableSetOf<Position>()
    var tail = Knot(Position(0,0), 'T')
    var head = Knot(Position(0, 0), 'H')

    val parsedInput = parseInput(input)
    for(direction in parsedInput){
      head = Knot(head.pos.applyDirection(direction), head.name)
      tail = updateKnot(tail, head)

      positionSet.add(Position(tail.pos.x, tail.pos.y))

    }

    return positionSet.size;
  }

  private fun part2(input: List<String>): Int {
    val parsedInput = parseInput(input)
    val tailPositionSet = mutableSetOf<Position>()
    val knots = mutableListOf<Knot>(Knot(Position(0, 0), 'H'))
    for(idx in 1..9){
      knots.add(Knot(Position(0, 0), "$idx"[0]))
    }
    for(directionIdx in 0 .. parsedInput.lastIndex){
      val direction = parsedInput[directionIdx]
      knots[0] = Knot(knots[0].pos.applyDirection(direction), knots[0].name)
      for(idx in 1 .. knots.lastIndex){
        val frontKnot = knots[idx-1]
        val behindKnot = knots[idx]
        knots[idx] = updateKnot(behindKnot, frontKnot)

        if(idx == knots.lastIndex){
          tailPositionSet.add(knots[idx].pos)
        }
      }
    }

    return tailPositionSet.size;
  }

  override fun run() {
    val testData = readInput(9,"test")
    val inputData = readInput(9, "input")

    val testResultP1 = part1(testData)
    checkWithMessage(testResultP1, 13)

    runTimedPart(1, { part1(it) }, inputData)

    val testResultP2 = part2(testData)
    checkWithMessage(testResultP2, 1)

    runTimedPart(2, { part2(it) }, inputData)
  }
}