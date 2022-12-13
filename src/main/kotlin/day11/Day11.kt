package day11

import Day
import checkWithMessage
import parserCombinators.*
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import readInput
import readInputString
import runTimedPart

// TODO: Got stuck on this problem P2 for quite a while, time to learn about modular arithmetic more 0__0

@Suppress("unused")
class Day11 : Day() {

  enum class Operation(val string: String){
    ADD("+"),
    MULTIPLY("*");

    companion object: ParsableEnum<Operation> {
      override fun toMap(): Map<String, Operation> {
        return enumValues<Operation>().associateBy { it.string }
      }
    }
  }

  data class ChangeOperation(val operation: Operation, val operand: Any = 0) {
    fun performOperation(oldValue: Long): Long{
      val operandValue = if(operand is Long) operand else oldValue
      return when(operation){
        Operation.ADD -> oldValue + operandValue
        Operation.MULTIPLY -> oldValue * operandValue
      }
    }
  }

  class Monkey(
    var monkeyNumber: Int,
    private var items: MutableList<Long>,
    private var changeOperation: ChangeOperation,
    private var testAmount: Long,
    private var trueMonkey: Int,
    private var falseMonkey: Int
  ) {
    var inspectCount: Long = 0
    var roundInspectionsState: MutableList<Long> = mutableListOf()
    var roundInspections: Long = 0

    fun getSuperModulo(monkeyMap: Map<Int, Monkey>): Long {
      return monkeyMap.values.map { it.testAmount }.reduce { acc, i -> i * acc }
    }

    fun inspect(monkeyMap: Map<Int, Monkey>, shouldDivide: Boolean = true) {
      for(itemIdx in items.indices){
        val currentVal = items[itemIdx]
        items[itemIdx] = changeOperation.performOperation(currentVal)
        if(shouldDivide){
          items[itemIdx] = items[itemIdx] / 3
        }
        val passedTest = test(items[itemIdx])
        val toThrowTo = if(passedTest) trueMonkey else falseMonkey
        if(!shouldDivide){
          items[itemIdx] = items[itemIdx] % getSuperModulo(monkeyMap)
        }

        val throwMonkey = monkeyMap[toThrowTo] ?: throw Error("Expected throw monkey not to be null when running round!")
        throwMonkey.items.add(items[itemIdx])
        inspectCount += 1
        roundInspections += 1
      }
      items.clear()
    }

    fun test(value: Long): Boolean {
      return value % testAmount == 0L
    }
  }

  private fun parseInput(input: String): Map<Int, Monkey> {
    val parseTree = parseTillEnd(
      toClass(
        sequenceOf(
          //Parses 'Monkey x:'
          sequenceOf(
            string("Monkey ", false),
            number(Int::class),
            char(':', false),
            newLine()
          ),
          //Parses 'Starting items: x, x
          sequenceOf(
            space(2),
            string("Starting items: ", false),
            list(
              oneOrMoreTimes(
                sequenceOf(
                  number(Long::class),
                  optional(
                    sequenceOf(
                      char(',', false),
                      space()
                    )
                  )
                )
              )
            ),
            newLine()
          ),
          //Parses 'Operation: new = old (enum operation) number
          toClass(
            sequenceOf(
              space(2),
              string("Operation: new = old ", false),
              enum(Operation),
              space(),
              oneOf(number(Long::class), string("old")),
              newLine()
            ),
            ChangeOperation::class
          ),
          //Parses 'Test: divisible by number
          sequenceOf(
            space(2),
            string("Test: divisible by ", false),
            number(Long::class),
            newLine()
          ),
          //Parses 'If true: throw to monkey X' and 'If false: throw to monkey X'
          repeat(2,
            sequenceOf(
              space(4),
              string("If ", false),
              oneOf(string("true: ", false), string("false: ", false)),
              string("throw to monkey ", false),
              number(Int::class),
              optional(newLine())
            )
          ),
          optional(newLine())
        ),
        Monkey::class
      )
    )

    val result = parseTree(BaseParser(input))
    if(result.hasError){
      throw Error(result.error)
    }

    @Suppress("UNCHECKED_CAST")
    val monkeys = result.results as List<Monkey>

    return monkeys.associateBy { it.monkeyNumber }
  }

  private fun runRound(monkeyMap: Map<Int, Monkey>, shouldDivide: Boolean){
    var currentMonkeyIdx = 0
    while(currentMonkeyIdx in monkeyMap){
      val monkey = monkeyMap[currentMonkeyIdx] ?: throw Error("Expected monkey not to be null when running round!")
      monkey.roundInspections = 0
      monkey.inspect(monkeyMap, shouldDivide)
      currentMonkeyIdx += 1
      monkey.roundInspectionsState.add(monkey.roundInspections)
    }
  }

  private fun part1(input: String): Long {
    val monkeyMap = parseInput(input)
    kotlin.repeat(20) {
      runRound(monkeyMap, true)
    }
    return monkeyMap.values.map{it.inspectCount}.sortedDescending().subList(0, 2).reduce { acc, i -> acc * i }
  }

  private fun part2(input: String): Long {
    val monkeyMap = parseInput(input)
    kotlin.repeat(10000) {
      runRound(monkeyMap, false)
    }
    return monkeyMap.values.map{it.inspectCount}.sortedDescending().subList(0, 2).reduce { acc, i -> acc * i }
  }

  override fun run() {
    val testData = readInputString(11,"test")
    val inputData = readInputString(11, "input")

    val testResultP1 = part1(testData)
    checkWithMessage(testResultP1, 10605L)

    runTimedPart(1, { part1(it) }, inputData)

    val testResultP2 = part2(testData)
    checkWithMessage(testResultP2, 2713310158L)

    runTimedPart(2, { part2(it) }, inputData)
  }
}