package day2

import Day
import checkWithMessage
import parserCombinators.*
import readInput
import readInputString
import runTimedPart
import kotlin.sequences.sequenceOf

@Suppress("unused")
class Day2(): Day() {
    private val winningMatchUps = mapOf(
        Action.SCISSORS to Action.ROCK,
        Action.PAPER to Action.SCISSORS,
        Action.ROCK to Action.PAPER
    )

    private val losingMatchUps = mapOf(
        Action.ROCK to Action.SCISSORS,
        Action.SCISSORS to Action.PAPER,
        Action.PAPER to Action.ROCK
    )
    enum class MatchUpResult(val action: Char) {
        WIN('Z'),
        LOSE('X'),
        DRAW('Y');

        companion object {
            fun getEnum(value: String): MatchUpResult {
                if(value.length > 1){
                    throw Error("Supplied string is too long $value")
                }
                val character = value[0]

                for(result in MatchUpResult.values()){
                    if(result.action == character){
                        return result
                    }
                }
                throw Error("Could not match supplied character $value")
            }
        }
    }

    enum class Action(val action: Char, val altAction: Char, val score: Int) {
        ROCK('A', 'X', 1),
        PAPER('B', 'Y', 2),
        SCISSORS('C', 'Z', 3);

        companion object: ParsableEnum<Action> {
            fun getEnum(value: String): Action {
                if(value.length > 1){
                    throw Error("Supplied string is too long $value")
                }
                val character = value[0]

                for(action in Action.values()){
                    if(action.action == character || action.altAction == character){
                        return action
                    }
                }
                throw Error("Could not match supplied character $value")
            }

            override fun toMap(): Map<String, Action> {
                val firstMap = enumValues<Action>().associateBy { ""+it.action }
                val secondMap = enumValues<Action>().associateBy { ""+it.altAction }
                return firstMap + secondMap
            }
        }
    }
    private fun getMatchUpResult(matchUp: MatchUp): MatchUpResult {
        return if(matchUp.opponentAction == matchUp.strategyAction){
            MatchUpResult.DRAW
        } else if(winningMatchUps[matchUp.opponentAction] == matchUp.strategyAction){
            MatchUpResult.WIN
        } else {
            MatchUpResult.LOSE
        }
    }

    data class MatchUp(val opponentAction: Action, val strategyAction: Action)
    private fun actionPairToString(first: Action, second: Action): String {
        return "$first-$second";
    }
    private fun tallyScoreForMatchUps(input: List<MatchUp>): Int {
        var totalScore = 0
        for(matchUp in input){
            val result = getMatchUpResult(matchUp)
            if(result == MatchUpResult.WIN){
                totalScore += 6
            } else if(result == MatchUpResult.DRAW){
                totalScore += 3
            }
            totalScore += matchUp.strategyAction.score;
        }
        return totalScore;
    }

    private fun parseInput(input: String): List<MatchUp> {
        val parseTree = parseTillEnd(toClass(parserCombinators.sequenceOf(enum(Action), space(), enum(Action), optional(newLine())), MatchUp::class))
        val result = parseTree(BaseParser(input))
        if(result.hasError){
            throw Error(result.error)
        }

        @Suppress("UNCHECKED_CAST")
        return result.results.toList() as List<MatchUp>
    }

    private fun parseInputP2(input: String): List<MatchUp> {
        val parseTree = parseTillEnd(toClass(parserCombinators.sequenceOf(enum(Action), space(), strategyAction(), optional(newLine())), MatchUp::class))
        val result = parseTree(BaseParser(input))
        if(result.hasError){
            throw Error(result.error)
        }

        @Suppress("UNCHECKED_CAST")
        return result.results.toList() as List<MatchUp>
    }

    private fun strategyAction(): ParserFn {
        return newParser({ parser ->
            val newParser = anyLetter()(parser)
            newParser.lastParserName = "strategyAction(${newParser.lastParserName})"
            if(newParser.hasError){
                return@newParser newParser
            }

            val enumResult = newParser.popLast()
            val opponentAction = newParser.popLast()

            if(opponentAction !is Action){
                newParser.error = "Expected result of type Action but got: ${opponentAction::class.simpleName}"
                return@newParser newParser
            }

            val newResult = when (MatchUpResult.getEnum(""+enumResult as Char)) {
                MatchUpResult.DRAW -> {
                    opponentAction
                }

                MatchUpResult.WIN -> {
                    winningMatchUps[opponentAction]!!
                }

                MatchUpResult.LOSE -> {
                    losingMatchUps[opponentAction]!!
                }
            }
            newParser.results.add(opponentAction)
            newParser.results.add(newResult)

            return@newParser newParser
        }, "strategyAction(?)")
    }

    private fun part1(input: String): Int {
        val parsedInput = parseInput(input)
        return tallyScoreForMatchUps(parsedInput);
    }

    private fun part2(input: String): Int {
        val parsedInput = parseInputP2(input)
        return tallyScoreForMatchUps(parsedInput);
    }

    override fun run(){
        val testData = readInputString(2,"test")
        val inputData = readInputString(2, "input")

        val testResult1 = part1(testData)
        checkWithMessage(testResult1, 15)

        runTimedPart(1, { part1(it) }, inputData)

        val testResult2 = part2(testData)
        checkWithMessage(testResult2, 12)

        runTimedPart(2, { part2(it) }, inputData)
    }
}

