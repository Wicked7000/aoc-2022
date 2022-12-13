package parserCombinators

interface ParsableEnum<EnumOut> {
    fun toMap(): Map<String, EnumOut>
}

// Very helpful base for combinator parsers: https://en.wikipedia.org/wiki/Parser_combinator
open class BaseParser(
    private val input: String,
    private var index: Int = 0,
    val context: MutableMap<String, Any> = mutableMapOf(),
    val results: MutableList<Any> = mutableListOf(),
    var lastParserName: String = "",
    var error: String? = null,
    var warning: String? = null
) {
    var hasError: Boolean = false
        private set
        get() {
            return error != null
        }

    val hasParsed: Boolean
        get() {
            return index > input.lastIndex
        }

    val hasNext: Boolean
        get() {
            return index <= input.lastIndex
        }

    fun nextChar(): Pair<BaseParser, Char?> {
        val newParser = copy()
        if(input.lastIndex >= index){
            val nextChar = input[index]
            newParser.index += 1
            return Pair(newParser, nextChar)
        }
        newParser.index += 1
        return Pair(newParser, null);
    }

    fun nextString(amount: Int): Pair<BaseParser, String> {
        val newParser = copy()
        if(input.lastIndex >= index+amount-1) {
            val nextSection = input.substring(index, index+amount)
            newParser.index += amount
            return Pair(newParser, nextSection)
        }
        newParser.index += 1
        return Pair(newParser, "")
    }

    fun peek(amount: Int): String {
        if(input.lastIndex >= index+amount){
            return input.substring(index, index+amount)
        }
        return ""
    }

    fun copy(
        input: String = this.input,
        index: Int = this.index,
        context: MutableMap<String, Any> = this.context.toMutableMap(),
        results: MutableList<Any> = this.results.toMutableList(),
        lastParserName: String = this.lastParserName,
        error: String? = this.error,
        warning: String? = this.warning
    ) = BaseParser(input, index, context, results, lastParserName, error, warning)

    fun popFirstResult(): Any {
        return results.removeAt(0)
    }

    fun popLast(): Any {
        return results.removeLast()
    }

    fun sliceLast(amount: Int): List<Any>? {
        val resultsSize = results.size
        if(amount <= resultsSize){
            val resultsClone = results.toList()
            val returnVal = resultsClone.slice(resultsSize - amount until resultsSize).toList()
            results.clear()
            results.addAll(resultsClone.slice(0 until resultsSize - amount).toMutableList())
            return returnVal
        }
        return null
    }

    fun peekChar(): Char? {
        if(hasNext){
            return input[index]
        }
        return null
    }

    fun advance(amount: Int): BaseParser{
        val newParser = copy()
        newParser.index += amount
        return newParser
    }

    companion object {
        fun isEqualPositions(a: BaseParser, b: BaseParser): Boolean{
            return a.index == b.index
        }
    }
}