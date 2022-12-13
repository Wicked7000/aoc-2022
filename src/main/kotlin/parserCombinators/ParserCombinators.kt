package parserCombinators

import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

typealias ParserFn = (BaseParser) -> BaseParser

private fun errorCheckAndNameSet(parser: BaseParser, name: String): BaseParser {
    val newParser = parser.copy()
    newParser.lastParserName = name
    return newParser
}

// Convince function to check errors and set the 'error' name
fun newParser(parserFn: ParserFn, name: String): ParserFn {
    return {
        val parser = errorCheckAndNameSet(it, name)
        when {
            parser.hasError -> parser
            else -> parserFn(parser)
        }
    }
}

fun stringMap(map: Map<String, Any>): ParserFn {
    return newParser({parser ->
        val innerTree = oneOf(*map.keys.toList().map { string(it ) }.toTypedArray())
        val newParser = innerTree(parser)
        if(!newParser.hasError){
            val mapKey = newParser.popLast()
            val mappedValue = map[mapKey as String]
            if(mappedValue != null){
                newParser.results.add(mappedValue)
            } else {
                newParser.error = "Expected map ($map) to contain key $mapKey"
            }
        }
        return@newParser newParser
    }, "stringMap()")
}
fun enum(enum: ParsableEnum<*>): ParserFn {
    return newParser({parser ->
        @Suppress("UNCHECKED_CAST")
        val enumMap = enum.toMap() as Map<String, Any>

        val innerTree = stringMap(enumMap)
        val parsedEnum = innerTree(parser)
        parsedEnum.lastParserName = "enum(${parsedEnum.lastParserName})"
        return@newParser parsedEnum
    }, "enum(?)")
}

fun newLine(): ParserFn {
    return newParser({ parser ->
        return@newParser with(parser.nextChar()) {
            var innerParser = first
            val nextChar = second
            if(nextChar != '\r' && nextChar != '\n'){
                innerParser.error = "Expected '\\r' or '\\n' but received '$nextChar'"
                return@with innerParser
            }
            val peekChar = innerParser.peekChar()
            if(peekChar == '\n'){
                innerParser = innerParser.advance(1)
            }
            return@with innerParser;
        }
    }, "newLine()")
}

fun optional(parserFn: ParserFn): ParserFn {
    return newParser({parser ->
        val state = parserFn(parser)
        val functionName = "optional(${state.lastParserName})"
        if(state.hasError){
            parser.lastParserName = functionName
            return@newParser parser
        } else {
            state.lastParserName = functionName
            return@newParser state
        }
    }, "optional()")
}

fun numberInt(): ParserFn {
    return newParser({ parser ->
        val bufferNum = StringBuilder()
        var currentChar: Char?
        var newParser = parser

        do {
            currentChar = newParser.peekChar()
            if(currentChar?.isDigit() == true){
                bufferNum.append(currentChar)
                newParser = newParser.advance(1)
                currentChar = newParser.peekChar()
            }
        } while(currentChar?.isDigit() == true)

        if(bufferNum.isEmpty()){
            newParser.error = "Expected number but got '$currentChar'"
        } else {
            newParser.results.add(bufferNum.toString().toInt())
        }

        newParser
    }, "numberInt()")
}

fun numberLong(): ParserFn {
    return newParser({ parser ->
        val bufferNum = StringBuilder()
        var currentChar: Char?
        var newParser = parser

        do {
            currentChar = newParser.peekChar()
            if(currentChar?.isDigit() == true){
                bufferNum.append(currentChar)
                newParser = newParser.advance(1)
                currentChar = newParser.peekChar()
            }
        } while(currentChar?.isDigit() == true)

        if(bufferNum.isEmpty()){
            newParser.error = "Expected number but got '$currentChar'"
        } else {
            newParser.results.add(bufferNum.toString().toLong())
        }

        newParser
    }, "numberLong()")
}

fun group(vararg parsers: ParserFn): ParserFn {
    return newParser({ parser ->
        var currentParser = parser;
        val parserStates = mutableListOf<BaseParser>()
        val previousResultsArray = currentParser.results.toCollection(mutableListOf()) //copy
        currentParser.results.clear()

        for(parserFn in parsers){
            currentParser = parserFn(parser)
            parserStates.add(currentParser)
        }

        val groupedResults = mutableListOf<Any>()
        groupedResults.addAll(currentParser.results)
        currentParser.results.clear()
        currentParser.results.addAll(previousResultsArray)
        if(groupedResults.size > 0){
            currentParser.results.add(groupedResults)
        }

        currentParser.lastParserName = "group(${parserStates.joinToString(", ") { it.lastParserName }})"
        return@newParser currentParser

    },"group()")
}

fun space(amount: Int = 1, shouldCapture: Boolean = false): ParserFn {
    return newParser({ parser ->
        return@newParser with(parser.nextString(amount)) {
            val newParser = first
            val nextChar = second
            val expect = " ".repeat(amount)
            if(nextChar != expect){
                newParser.error = "Expected '$expect' but received '$nextChar'"
            } else if(shouldCapture){
                newParser.results.add(expect)
            }
            return@with newParser;
        }
    }, "space()")
}

// Effectively parses until it reaches an error or end of input
fun oneOrMoreTimes(parseTree: ParserFn): ParserFn {
    return newParser({parser ->
        var previousParseState = parser
        var currentParseState = parser
        while(!(currentParseState.hasError || currentParseState.hasParsed)){
            previousParseState = currentParseState
            currentParseState = parseTree(currentParseState)
        }
        val functionName = "oneOrMoreTimes(${currentParseState.lastParserName})"
        if(currentParseState.hasError){
            previousParseState.lastParserName = functionName
            previousParseState.warning = currentParseState.error
            return@newParser previousParseState
        } else {
            currentParseState.lastParserName = functionName
            return@newParser currentParseState
        }
    }, "oneOrMoreTimes(?)")
}

fun parseTillEnd(parseTree: ParserFn): ParserFn {
    return newParser({parser ->
        var currentParseState = parser
        var previousParseState: BaseParser? = null
        while(!currentParseState.hasParsed){
            if(currentParseState.hasError){
                break;
            }
            if(previousParseState != null && BaseParser.isEqualPositions(currentParseState, previousParseState)){
                currentParseState.error = "Parser did not move between iterations (warning: ${currentParseState.warning})"
                break;
            }
            previousParseState = currentParseState
            currentParseState = parseTree(currentParseState)
        }
        currentParseState.warning = null
        currentParseState.lastParserName = "parseTillEnd(${currentParseState.lastParserName})"
        return@newParser currentParseState
    }, "parseTillEnd(?)")
}

fun sequenceOf(vararg parsers: ParserFn): ParserFn {
    return newParser({ parser ->
        var currentState = parser
        val parserStates = mutableListOf<BaseParser>()
        for(parserFn in parsers){
            parserStates.add(parserFn(currentState))
            currentState = parserStates[parserStates.lastIndex]
        }
        currentState.lastParserName = "sequenceOf(${parserStates.joinToString(", ") { it.lastParserName }})"
        currentState
    }, "sequenceOf(?)")
}

fun oneOf(vararg parsers: ParserFn): ParserFn {
    return newParser({ parser ->
        val errorStates: MutableList<BaseParser> = mutableListOf()
        val passedStates: MutableList<BaseParser> = mutableListOf()

        for(parserFn in parsers){
            val possibleNextState = parserFn(parser)
            if(!possibleNextState.hasError){
                passedStates.add(possibleNextState)
            } else {
                errorStates.add(possibleNextState)
            }
        }

        val functionName = "oneOf(${(errorStates+passedStates).joinToString(", ") { it.lastParserName }})"
        if(passedStates.size > 1){
            val combinedParsers = passedStates.joinToString(", ") { it.lastParserName }
            parser.lastParserName = functionName
            parser.error = "Expected only one parser to match but received: $combinedParsers"
            parser
        } else if(passedStates.size == 0) {
            val combinedErrors = errorStates.joinToString("\n") { "${it.error} (${it.lastParserName})" }
            parser.lastParserName = functionName
            parser.error = "Expected only one parser to match but received none: \n$combinedErrors"
            parser
        } else {
            passedStates[0].lastParserName = functionName
            passedStates[0]
        }
    }, "oneOf(?)")
}

fun anyLetter(): ParserFn {
    return newParser({ parser ->
        return@newParser with(parser.nextChar()) {
            val newParser = first
            val next = second

            val nextIsLetter = next?.isLetter()
            if(nextIsLetter == false || nextIsLetter == null){
                newParser.error = "Expected letter but received '$next'"
            } else {
                newParser.results.add(next)
            }
            return@with newParser
        }
    }, "anyLetter()")
}

fun char(toMatch: Char, shouldCapture: Boolean = true): ParserFn {
    return newParser({parser ->
        return@newParser with(parser.nextChar()) {
            val newParser = first
            val nextChar = second
            if(nextChar != toMatch){
                newParser.error = "Expected '$toMatch' but received '$nextChar'"
            } else if(shouldCapture) {
                newParser.results.add(toMatch)
            }
            return@with newParser
        }
    }, "char($toMatch)")
}

fun toClass(innerParse: ParserFn, instanceClass: KClass<*>): ParserFn {
    return newParser({parser ->
        val newParser = innerParse(parser)
        newParser.lastParserName = "toClass(${instanceClass.simpleName}, ${newParser.lastParserName})"
        if(newParser.hasError){
            return@newParser newParser
        }

        if(instanceClass.constructors.size > 1 || instanceClass.constructors.isEmpty()){
            newParser.error = "Expected class to only have 1 constructor"
            return@newParser newParser
        }

        val constructorFn = instanceClass.constructors.first()
        val numOfParams = constructorFn.parameters.size

        if(newParser.results.size < numOfParams){
            newParser.error = "Expected parser results to contain $numOfParams results but contained: ${newParser.results.size}"
            return@newParser newParser
        }
        val resultArgs = newParser.sliceLast(numOfParams)
        if(resultArgs == null){
            newParser.error = "Expected list of params but got: null"
            return@newParser newParser
        }

        for(paramIdx in 0 until numOfParams){
            if(!constructorFn.parameters[paramIdx].type.isSupertypeOf(resultArgs[paramIdx]::class.createType())){
                newParser.error = "Expected parameter of type ${constructorFn.parameters[paramIdx].type.asTypeName()} but got: ${resultArgs[paramIdx]::class.simpleName}"
                return@newParser newParser
            }
        }

        try {
            val instance = constructorFn.call(*resultArgs.toTypedArray())
            newParser.results.add(instance)
        } catch(e: Exception) {
            newParser.error = "Exception occurred when constructing class: ${e.message}"
        }

        return@newParser newParser
    }, "toClass(${instanceClass.simpleName}, ?)")
}

fun anyLengthString(): ParserFn {
    return newParser({parser ->
        val stringBuffer = StringBuilder()
        var currentParser = parser
        var currentChar = currentParser.peekChar()

        while (currentChar?.isWhitespace() == false && currentChar != '\n' && currentChar != '\r') {
            currentParser = currentParser.advance(1)
            stringBuffer.append(currentChar)
            currentChar = currentParser.peekChar()
        }

        if(stringBuffer.isEmpty()){
            currentParser.error = "Expected string of any length but received '$currentChar'"
        } else {
            currentParser.results.add(stringBuffer.toString())
        }
        currentParser
    }, "anyLengthString()")
}

fun string(toMatch: String, shouldCapture: Boolean = true): ParserFn {
    return newParser({parser ->
        return@newParser with(parser.nextString(toMatch.length)){
            val newParser = first
            val nextString = second
            if(nextString != toMatch){
                newParser.error = "Expected '$toMatch' but received '$nextString'"
            } else if(shouldCapture) {
                newParser.results.add(toMatch)
            }
            return@with newParser
        }

    }, "string($toMatch)")
}