package day3

import Day
import checkWithMessage
import readInput
import runTimedPart

@Suppress("unused")
class Day3(): Day() {
    private fun getItemPriority(item: Char): Int {
        var score = item.code
        if(score >= 97){
            //Lowercase
            score -= 96
        } else if(score >= 65){
            score -= 38
        }
        return score;
    }

    data class Rucksack(val firstCompartment: Set<Char>, val secondCompartment: Set<Char>)

    private fun part1(input: List<String>): Int {
        var totalPriorities = 0
        for(line in input){
            val firstHalf = mutableSetOf<Char>()
            val secondHalf = mutableSetOf<Char>()
            for(idx in line.indices){
                val element = line[idx]
                if(idx < line.length / 2){
                    firstHalf.add(element)
                } else {
                    secondHalf.add(element)
                }
            }

            val overlap = firstHalf.intersect(secondHalf)
            if(overlap.isEmpty() || overlap.size > 1){
                throw Error("Too many elements or the overlap set is empty! $line $overlap")
            }
            overlap.firstNotNullOf {
                totalPriorities += getItemPriority(it)
            }
        }
        return totalPriorities;
    }

    private fun part2(input: List<String>): Int {
        var totalPriorities = 0
        val groups = input.chunked(3)
        for(groupIdx in groups.indices){
            val groupContents = List(3) { mutableSetOf<Char>() }
            for(lineIdx in groups[groupIdx].indices){
                groups[groupIdx][lineIdx].map { groupContents[lineIdx].add(it) }
            }
            val intersection = groupContents[0].intersect(groupContents[1]).intersect(groupContents[2])
            if(intersection.isEmpty() || intersection.size > 1){
                throw Error("Too many elements or the overlap set is empty! ${groups[groupIdx]} $intersection")
            }
            intersection.firstNotNullOf {
                totalPriorities += getItemPriority(it)
            }
        }
        return totalPriorities
    }

    override fun run(){
        val testData = readInput(3,"test")
        val inputData = readInput(3, "input")

        val testResult1 = part1(testData)
        checkWithMessage(testResult1, 157)

        runTimedPart(1, { part1(it) }, inputData)

        val testResult2 = part2(testData)
        checkWithMessage(testResult2, 70)

        runTimedPart(2, { part2(it) }, inputData)
    }
}

