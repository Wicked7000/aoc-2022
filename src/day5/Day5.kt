package day5

import Day
import checkWithMessage
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import readInput
import runTimedPart

@Suppress("unused")
class Day5 : Day() {
  data class StackCommand(val amount: Int, val fromIndex: Int, val toIndex: Int)
  data class StacksAndInstructions(val stacks: MutableList<String>, val instructions: List<StackCommand>)

  // Simulates moving one at a time by using reverse
  private fun moveBlocks(stacks: MutableList<String>, command: StackCommand, reverse: Boolean = true){
    var toMove = stacks[command.fromIndex].substring(0, command.amount)
    if(reverse){
      toMove = toMove.reversed()
    }
    stacks[command.fromIndex] = stacks[command.fromIndex].substring(command.amount)
    stacks[command.toIndex] = toMove + stacks[command.toIndex]
  }

  private fun parseInput(input: List<String>): StacksAndInstructions {
    var isProcessingStacks = true
    val stacks = mutableListOf<String>()
    val stackCommands = mutableListOf<StackCommand>()

    for(line in input){
      val innerLine = line + " "

      if(line.isEmpty()){
        isProcessingStacks = false
      }

      //Process stacks
      if(isProcessingStacks){
        if(!line.contains("[")){
          continue;
        }
        var index = 0
        val iter = innerLine.chunkedSequence(4).iterator()
        while(iter.hasNext()){
          val item = iter.next()
          if(stacks.size - 1 < index){
            stacks.add("")
          }
          if(item != "    "){
            stacks[index] += item[1].toString()
          }
          index += 1
        }
      }else{
        //Process commands
        val result = """move (\d+) from (\d+) to (\d+)""".toRegex().find(line)
        result?.let {
          val moveAmount = it.groupValues[1].toInt()
          val from = it.groupValues[2].toInt()-1
          val to = it.groupValues[3].toInt()-1
          stackCommands.add(StackCommand(moveAmount, from, to))
        }
      }
    }

    return StacksAndInstructions(stacks, stackCommands)
  }

  private fun part1(input: List<String>): String {
    val data = parseInput(input)
    for(command in data.instructions){
      moveBlocks(data.stacks, command, true)
    }
    return data.stacks.map { it[0] }.joinToString("");
  }

  private fun part2(input: List<String>): String {
    val data = parseInput(input)
    for(command in data.instructions){
      moveBlocks(data.stacks, command, false)
    }
    return data.stacks.map { it[0] }.joinToString("");
  }

  override fun run() {
    val testData = readInput(5,"test")
    val inputData = readInput(5, "input")

    val testResultP1 = part1(testData)
    checkWithMessage(testResultP1, "CMZ")

    runTimedPart(1, { part1(it) }, inputData)

    val testResultP2 = part2(testData)
    checkWithMessage(testResultP2, "MCD")

    runTimedPart(2, { part2(it) }, inputData)
  }
}