package day6

import Day
import checkWithMessage
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import readInput
import runTimedPart
import java.lang.NullPointerException

@Suppress("unused")
class Day6 : Day() {

  private fun findMarkerIndex(input: String, recentCharactersAmount: Int): Int {
    for(idx in recentCharactersAmount .. input.lastIndex){
      if((1 .. recentCharactersAmount).map { input[idx-it] }.toSet().size == recentCharactersAmount){
        return idx
      }
    }
    return -1
  }

  private fun part1(input: List<String>): List<Int> {
    return input.map { findMarkerIndex(it, 4) }
  }

  private fun part2(input: List<String>): List<Int> {
    return input.map { findMarkerIndex(it, 14) }
  }

  override fun run() {
    val testData = readInput(6,"test")
    val inputData = readInput(6, "input")

    val testResultP1 = part1(testData)
    checkWithMessage(testResultP1, listOf(7, 5, 6, 10, 11))

    runTimedPart(1, { part1(it)[0] }, inputData)

    val testResultP2 = part2(testData)
    checkWithMessage(testResultP2, listOf(19, 23, 23, 29, 26))

    runTimedPart(2, { part2(it)[0] }, inputData)
  }
}