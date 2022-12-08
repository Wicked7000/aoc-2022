package day8

import Day
import checkWithMessage
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import readInput
import runTimedPart
import java.util.*

@Suppress("unused")
class Day8 : Day() {

  data class Tree(val height: Int)
  data class Coord(val x: Int, val y: Int)

  //Indexed from top-left 0,0
  class TreeMap(){
    private var sizeX = 0;
    private var sizeY = 0;
    private val map = mutableMapOf<Coord, Tree>()

    private fun checkIndividualTree(thisTree: Tree, thisTreePos: Coord, rangeIdx: Int, isUsingXRange: Boolean): Boolean {
      val checkTreePos = if(isUsingXRange) Coord(rangeIdx, thisTreePos.y) else Coord(thisTreePos.x, rangeIdx)
      val tree = getTree(checkTreePos) ?: throw Error("Expected tree, got null (${checkTreePos.x}, ${checkTreePos.y})");
      if(tree.height >= thisTree.height){
        return false
      }
      return true
    }

    fun getScenicScore(treePos: Coord): Int {
      val thisTree = getTree(treePos) ?: throw Error("No tree at coordinate! (${treePos.x}, ${treePos.y})")

      if(treePos.x == 0 || treePos.y == 0 || treePos.x == sizeX  || treePos.y == sizeY){
        return 0
      }

      val scores = mutableListOf(true, false).flatMap {
        val viewRanges = mutableListOf<Int>()
        val untilPos = if (it) treePos.x else treePos.y

        //Before block
        var rangeIdxAfter = untilPos + 1
        var rangeIdxBefore = untilPos - 1
        while(rangeIdxBefore > 0){
          if (!checkIndividualTree(thisTree, treePos, rangeIdxBefore, it)) {
            break
          }
          rangeIdxBefore -= 1
        }
        viewRanges.add(untilPos - rangeIdxBefore)

        //After block
        val endPos = if (it) sizeX else sizeY
        while(rangeIdxAfter < endPos){
          if (!checkIndividualTree(thisTree, treePos, rangeIdxAfter, it)) {
            break
          }
          rangeIdxAfter += 1
        }
        viewRanges.add(rangeIdxAfter - untilPos)

        viewRanges
      }

      return scores.reduce { acc, next -> acc * next }
    }

    // Determines if all trees in a row/column are shorter than the current position
    private fun areAllTreesShorter(treePos: Coord, isUsingXRange: Boolean): Boolean {
      var isBeforeShorter = true
      var isAfterShorter = true

      val thisTree = getTree(treePos) ?: throw Error("No tree at coordinate! (${treePos.x}, ${treePos.y})")

      val untilPos = if(isUsingXRange) treePos.x else treePos.y
      for(rangeIdx in 0 until untilPos){
        if(!checkIndividualTree(thisTree, treePos, rangeIdx, isUsingXRange)) {
          isBeforeShorter = false
          break
        }
      }

      val endPos = if(isUsingXRange) sizeX else sizeY
      for(rangeIdx in untilPos+1 .. endPos){
        if(!checkIndividualTree(thisTree, treePos, rangeIdx, isUsingXRange)) {
          isAfterShorter = false
          break
        }
      }

      return isAfterShorter || isBeforeShorter;
    }

    fun mapKeys(): MutableSet<Coord> {
      return Collections.unmodifiableSet(map.keys)
    }

    fun isVisibleFromOneOrMoreSides(pos: Coord): Boolean{
      if(pos.x == 0 || pos.x == sizeX || pos.y == 0 || pos.y == sizeY){
        return true;
      }

      return areAllTreesShorter(pos, true) || areAllTreesShorter(pos, false)
    }

    fun addTree(pos: Coord, height: Int){
      this.map[pos] = Tree(height)
      if(pos.x > sizeX){
        sizeX = pos.x
      }
      if(pos.y > sizeY){
        sizeY = pos.y
      }
    }

    private fun getTree(pos: Coord): Tree? {
      if(pos in this.map){
        return this.map[pos]
      }
      return null
    }
  }

  private fun parseInput(input: List<String>): TreeMap {
    val map = TreeMap()
    for(yPos in input.indices){
      val line = input[yPos]
      for(xPos in line.indices){
        val height = line[xPos].digitToInt()
        map.addTree(Coord(xPos, yPos), height)
      }
    }
    return map;
  }

  private fun part1(input: List<String>): Int {
    var amountVisible = 0
    val map = parseInput(input);
    for(key in map.mapKeys()){
      if(map.isVisibleFromOneOrMoreSides(key)){
        amountVisible += 1
      }
    }
    return amountVisible
  }

  private fun part2(input: List<String>): Int {
    var highestScenicScore = Int.MIN_VALUE
    val map = parseInput(input);
    for(key in map.mapKeys()){
      val thisScore = map.getScenicScore(key)
      if(thisScore > highestScenicScore){
        highestScenicScore = thisScore
      }
    }
    return highestScenicScore
  }

  override fun run() {
    val testData = readInput(8,"test")
    val inputData = readInput(8, "input")

    val testResultP1 = part1(testData)
    checkWithMessage(testResultP1, 21)

    runTimedPart(1, { part1(it) }, inputData)

    val testResultP2 = part2(testData)
    checkWithMessage(testResultP2, 8)

    runTimedPart(2, { part2(it) }, inputData)
  }
}