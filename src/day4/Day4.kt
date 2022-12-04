package day4

import Day
import checkWithMessage
import readInput
import runTimedPart

@Suppress("unused")
class Day4(): Day() {

    data class Range(val start: Int, val end: Int)
    private fun processRange(rangeStr: String): Range {
        val (start, end) = rangeStr.split("-").map { it.toInt() }
        return Range(start, end)
    }

    private fun doesRangeOverlap(a: Range, b: Range): Boolean {
        // Overlaps at boundary
        if(a.start == b.end || a.start == b.start || b.start == a.end){
            return true
        }

        // B starting position overlaps A
        if(b.start >= a.start && b.start <= a.end){
            return true
        }

        // A starting position overlaps B
        if(a.start >= b.start && a.start <= b.end){
            return true;
        }
        return false
    }

    private fun doesRangeFullyContainOther(a: Range, b: Range): Boolean {
        // A is fully contained by B
        if(a.start >= b.start && a.end <= b.end){
            return true;
        }

        // B is fully contained by A
        if(b.start >= a.start && b.end <= a.end){
            return true;
        }

        return false;
    }

    private fun part1(input: List<String>): Int {
        var totalOverlapPairs = 0
        for(line in input){
            val (elfA, elfB) = line.split(",").map { processRange(it) }
            if(doesRangeFullyContainOther(elfA, elfB)){
                totalOverlapPairs += 1
            }
        }
        return totalOverlapPairs;
    }

    private fun part2(input: List<String>): Int {
        var totalOverlapPairs = 0
        for(line in input){
            val (elfA, elfB) = line.split(",").map { processRange(it) }
            if(doesRangeOverlap(elfA, elfB)){
                totalOverlapPairs += 1
            }
        }
        return totalOverlapPairs;
    }

    override fun run(){
        val testData = readInput(4,"test")
        val inputData = readInput(4, "input1")

        val testResult1 = part1(testData)
        checkWithMessage(testResult1, 2)

        runTimedPart(1, { part1(it) }, inputData)

        val testResult2 = part2(testData)
        checkWithMessage(testResult2, 4)

        runTimedPart(2, { part2(it) }, inputData)
    }
}

