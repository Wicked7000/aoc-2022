package day2

import Day
import checkWithMessage
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match
import readInput
import runTimedPart

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

        companion object {
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

    private fun part1(input: List<String>): Int {
        val parsedInput = mutableListOf<MatchUp>()
        for(item in input){
            val (a, b) = item.split(" ");
            parsedInput.add(MatchUp(Action.getEnum(a), Action.getEnum(b)))
        }

        return tallyScoreForMatchUps(parsedInput);
    }

    private fun part2(input: List<String>): Int {
        val parsedInput = mutableListOf<MatchUp>()
        for(item in input){
            val (a, b) = item.split(" ");
            val opponentAction = Action.getEnum(a)
            val strategyAction: Action = when (MatchUpResult.getEnum(b)) {
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

            parsedInput.add(MatchUp(opponentAction, strategyAction))
        }
        return tallyScoreForMatchUps(parsedInput);
    }

    override fun run(){
        val testData = readInput(2,"test")
        val inputData = readInput(2, "input1")

        val testResult1 = part1(testData)
        checkWithMessage(testResult1, 15)

        runTimedPart(1, { part1(it) }, inputData)

        val testResult2 = part2(testData)
        checkWithMessage(testResult2, 12)

        runTimedPart(2, { part2(it) }, inputData)
    }
}

