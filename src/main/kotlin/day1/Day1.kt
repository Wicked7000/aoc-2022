package day1

import Day
import checkWithMessage
import parserCombinators.*
import parserCombinators.newLine
import readInput
import readInputString
import runTimedPart

@Suppress("unused")
class Day1(): Day() {
    private fun parseInput(input: String): List<List<Int>> {
        val parseTree = parseTillEnd(sequenceOf(group(oneOrMoreTimes(sequenceOf(number(Int::class), optional(newLine())))), optional(newLine())))
        val result = parseTree(BaseParser(input))
        if(result.hasError){
            throw Error(result.error)
        }
        return result.results as List<List<Int>>
    }

    private fun part1(input: String): Int {
        val elves = parseInput(input)
        var currentMaxCalories = Int.MIN_VALUE;
        for(elf in elves){
            val summedVal = elf.sum()
            if(summedVal > currentMaxCalories){
                currentMaxCalories = summedVal
            }
        }
        return currentMaxCalories
    }

    private fun part2(input: String): Int {
        val elves = parseInput(input)
        val currentTopThree = MutableList(3) { _: Int -> Int.MIN_VALUE }
        for(elf in elves){
            val summedVal = elf.sum()
            for(topThreeIdx in currentTopThree.size-1 downTo 0){
                val topAmount = currentTopThree[topThreeIdx]
                if(topAmount <= summedVal){
                    if(topThreeIdx + 1 < currentTopThree.size){
                        val tempVal = currentTopThree[topThreeIdx]
                        currentTopThree[topThreeIdx] = summedVal
                        currentTopThree[topThreeIdx+1] = tempVal
                    } else {
                        currentTopThree[topThreeIdx] = summedVal
                    }
                }
            }
        }
        return currentTopThree.sum()
    }

    override fun run(){
        val testInput = readInputString(1, "test")
        val p1Input = readInputString(1, "input")

        val testResult1 = part1(testInput)
        checkWithMessage(testResult1, 24000)

        runTimedPart(1, { part1(it) }, p1Input)

        val testResult2 = part2(testInput)
        checkWithMessage(testResult2,45000)

        runTimedPart(2, { part2(it) }, p1Input)
    }
}
