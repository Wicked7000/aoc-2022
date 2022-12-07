
// Very helpful base for combinator parsers: https://en.wikipedia.org/wiki/Parser_combinator
open class BaseParser(private val input: List<String>) {
    private var index: Int = 0

    fun next(): String {
        val nextLine = input[index]
        index += 1
        return nextLine
    }

    fun peek(): String? {
        if(index <= input.lastIndex){
            return input[index]
        }
        return null
    }

    fun hasParsed(): Boolean {
        return index >= input.lastIndex
    }

    fun hasNext(): Boolean {
        return index < input.lastIndex
    }
}