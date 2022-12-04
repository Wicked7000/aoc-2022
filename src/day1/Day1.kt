package day1

import Day
import checkWithMessage
import readInput
import runTimedPart

@Suppress("unused")
class Day1(): Day() {
    private fun getCorrectedInput(name: String): MutableList<String> {
        val input = readInput(1, name).toMutableList()
        input.add("") // Allows for easier processing
        return input
    }

    private fun part1(input: List<String>): Long {
        var currentBufferAmount = 0L;
        var currentMaxCalories = Long.MIN_VALUE;
        for(idx in input.indices){
            val item = input[idx]
            if(item.isEmpty()){
                //End of calorie group
                if(currentMaxCalories < currentBufferAmount){
                    currentMaxCalories = currentBufferAmount
                }
                currentBufferAmount = 0L;
            } else {
                val caloriesItem = item.toInt();
                currentBufferAmount += caloriesItem;
            }
        }
        return currentMaxCalories
    }

    private fun part2(input: List<String>): Long {
        var currentBufferAmount = 0L;
        val currentTopThree = MutableList(3) { _: Int -> Long.MIN_VALUE }
        for(idx in input.indices){
            val item = input[idx]
            if(item.isEmpty()){
                //End of calorie group
                for(topThreeIdx in currentTopThree.size-1 downTo 0){
                    val topAmount = currentTopThree[topThreeIdx]
                    if(topAmount <= currentBufferAmount){
                        if(topThreeIdx + 1 < currentTopThree.size){
                            val tempVal = currentTopThree[topThreeIdx]
                            currentTopThree[topThreeIdx] = currentBufferAmount
                            currentTopThree[topThreeIdx+1] = tempVal
                        } else {
                            currentTopThree[topThreeIdx] = currentBufferAmount
                        }
                    }
                }
                currentBufferAmount = 0L;
            } else {
                val caloriesItem = item.toInt();
                currentBufferAmount += caloriesItem;
            }
        }
        return currentTopThree.sum()
    }

    override fun run(){
        val testInput = getCorrectedInput("test")
        val p1Input = getCorrectedInput("input1")

        val testResult1 = part1(testInput)
        checkWithMessage(testResult1, 24000L)

        runTimedPart(1, { part1(it) }, p1Input)

        val testResult2 = part2(testInput)
        checkWithMessage(testResult2,45000L)

        runTimedPart(2, { part2(it) }, p1Input)
    }
}
