package day10

import Day
import checkWithMessage
import parserCombinators.*
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import readInputString
import runTimedPart
import kotlin.math.abs
import kotlin.text.StringBuilder

@Suppress("unused")
class Day10 : Day() {
  enum class OpCode(val stringVal: String) {
    NO_OP("noop"),
    ADD_X("addx");

    companion object: ParsableEnum<OpCode> {
      override fun toMap(): Map<String, OpCode> {
        return enumValues<OpCode>().associateBy { it.stringVal }
      }
    }
  }

  class Command(val code: OpCode, val operand: Int? = null);

  class Computer(private val commands: List<Command>, private val signalCheckpoints: MutableList<Int> = mutableListOf(20,60,100,140,180,220)) {
    private var registerX = 1
    private var currentCommandIndex = 0
    private var currentCycle = 0
    private var currentSpritePosition = 0
    private var sprite: StringBuilder = StringBuilder()
    var signalCheckpointValues: MutableList<Int> = mutableListOf()


    fun step() {
      val command = commands[currentCommandIndex]
      val beforeValue = registerX

      currentCycle += when(command.code) {
          OpCode.NO_OP -> {
            setSpritePosition()
            1
          }
          OpCode.ADD_X -> {
            setSpritePosition()
            setSpritePosition()
            registerX += command.operand!!
            2
          }
      }

      if(signalCheckpoints.size > 0){
        val nextSignal = signalCheckpoints.first()
        if(currentCycle >= nextSignal){
          signalCheckpointValues.add(signalStrength(nextSignal, beforeValue))
          signalCheckpoints.removeFirst()
        }
      }

      currentCommandIndex += 1
    }

    fun hasReachedAllCheckpoints(): Boolean{
      return signalCheckpoints.size == 0
    }

    fun signalStrength(cycle: Int = currentCycle, registerValue: Int = registerX): Int {
      return cycle * registerValue
    }

    private fun setSpritePosition(){
      val horizontalPos = currentSpritePosition % 40
      sprite.append(if(abs(horizontalPos - registerX) <= 1) '#' else '.')
      currentSpritePosition += 1
    }

    fun hasReachedSpriteRenderAmount(): Boolean {
      return currentCycle >= 240
    }

    fun renderSprite(){
      var start = 0
      val fullSprite = sprite.toString()
      for(idx in 0 until 6){
        println(fullSprite.substring(start, start+40))
        start += 40
      }
    }

    fun getSpriteString(): String {
      return sprite.toString()
    }
  }

  private fun parseInput(input: String): List<Command> {
    val parseTree = parseTillEnd(
      toClass(
        sequenceOf(
          enum(OpCode),
          optional(sequenceOf(space(), number(Int::class))),
          optional(newLine())
        ),
        Command::class
      )
    )
    val result = parseTree(BaseParser(input))
    if(result.hasError){
      throw Error(result.error)
    }

    @Suppress("UNCHECKED_CAST")
    return result.results as List<Command>
  }

  private fun part1(input: String): Int {
    val commands = parseInput(input)
    val computer = Computer(commands)
    while(!computer.hasReachedAllCheckpoints()){
      computer.step()
    }
    return computer.signalCheckpointValues.sum();
  }

  private fun part2(input: String, shouldRender: Boolean = true): String {
    val commands = parseInput(input)
    val computer = Computer(commands)
    while(!computer.hasReachedSpriteRenderAmount()){
      computer.step()
    }
    if(shouldRender){
      computer.renderSprite()
    }

    return computer.getSpriteString()
  }

  override fun run() {
    val testData = readInputString(10,"test")
    val inputData = readInputString(10, "input")

    val testResultP1 = part1(testData)
    checkWithMessage(testResultP1, 13140)

    runTimedPart(1, { part1(it) }, inputData)

    val testResultP2 = part2(testData, shouldRender = false)
    checkWithMessage(testResultP2,
      """##..##..##..##..##..##..##..##..##..##..
         |###...###...###...###...###...###...###.
         |####....####....####....####....####....
         |#####.....#####.....#####.....#####.....
         |######......######......######......####
         |#######.......#######.......#######.....""".trimMargin().replace("\n", ""))

    runTimedPart(2, { part2(it) }, inputData, shouldPrintOutput = false)
  }
}