import org.junit.experimental.runners.Enclosed
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.runner.RunWith
import parserCombinators.*

enum class LetterEnumTest(private var value: Char) {
    A_TEST('a'),
    B_TEST('b'),
    C_TEST('c');

    companion object: ParsableEnum<LetterEnumTest> {
        override fun toMap(): Map<String, LetterEnumTest> {
            return enumValues<LetterEnumTest>().associateBy { "" + it.value }
        }
    }
}

@RunWith(value = Enclosed::class)
class ParserCombinatorsKtTest {
    @Nested
    @DisplayName("newLine()")
    inner class NewLineParser() {
        @Test
        fun newLineParser(){
            val testParser = BaseParser("\n")
            val parser = newLine()
            val result = parser(testParser)
            assertEquals("newLine()", result.lastParserName)
            assertEquals(false, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @Test
        fun newLineParserCarriageReturn(){
            val testParser = BaseParser("\r\n")
            val parser = newLine()
            val result = parser(testParser)
            assertEquals("newLine()", result.lastParserName)
            assertEquals(false, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @Test
        fun newLineParserCarriageError(){
            val testParser = BaseParser("\r")
            val parser = newLine()
            val result = parser(testParser)
            assertEquals("newLine()", result.lastParserName)
            assertEquals(false, result.hasNext)
            assertEquals(true, result.hasError)
            assertEquals("Expected '\\n' but received 'null'", result.error)
        }
    }

    @Nested
    @DisplayName("space()")
    inner class SpaceParser() {
        @Test
        fun shouldPass(){
            val testParser = BaseParser(" ")
            val parser = space()
            val result = parser(testParser)
            assertEquals("space()", result.lastParserName)
            assertEquals(false, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @Test
        fun shouldPassMultiple(){
            val testParser = BaseParser("   ")
            val parser = space(3)
            val result = parser(testParser)
            assertEquals("space()", result.lastParserName)
            assertEquals(false, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @Test
        fun newLineParserCarriageError(){
            val testParser = BaseParser("a ")
            val parser = space()
            val result = parser(testParser)
            assertEquals("space()", result.lastParserName)
            assertEquals(true, result.hasNext)
            assertEquals(true, result.hasError)
            assertEquals("Expected ' ' but received 'a'", result.error)
        }
    }



    @Nested
    @DisplayName("anyLetter()")
    inner class AnyLetterParser() {
        @ParameterizedTest
        @ValueSource(chars = ['a', 'b', 'c', 'x', 'y'])
        fun shouldPass(input: Char){
            val testParser = BaseParser("" + input)
            val parser = anyLetter()
            val result = parser(testParser)
            assertEquals("anyLetter()", result.lastParserName)
            assertEquals(input, result.results[0])
            assertEquals(false, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @ParameterizedTest
        @ValueSource(chars = ['_', '@', ' ', ';', '¬'])
        fun shouldFailOnSpecialCharacters(input: Char){
            val testParser = BaseParser(""+input)
            val parser = anyLetter()
            val result = parser(testParser)
            assertEquals("anyLetter()", result.lastParserName)
            assertEquals(false, result.hasNext)
            assertEquals(true, result.hasError)
            assertEquals("Expected letter but received '$input'", result.error)
        }
    }

    @Nested
    @DisplayName("char()")
    inner class CharParser() {
        @ParameterizedTest
        @ValueSource(chars = ['a', 'b', 'c', 'x', 'y'])
        fun shouldPass(input: Char){
            val testParser = BaseParser("" + input)
            val parser = char(input)
            val result = parser(testParser)
            assertEquals("char($input)", result.lastParserName)
            assertEquals(input, result.results[0])
            assertEquals(false, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @Test
        fun shouldFailCorrectly(){
            val testParser = BaseParser(" a")
            val parser = char('a')
            val result = parser(testParser)
            assertEquals("char(a)", result.lastParserName)
            assertEquals(true, result.hasNext)
            assertEquals(true, result.hasError)
            assertEquals("Expected 'a' but received ' '", result.error)
        }
    }

    @Nested
    @DisplayName("string()")
    inner class StringParser() {
        @ParameterizedTest
        @ValueSource(strings = ["test", "__te st ", "123test"])
        fun shouldPass(input: String){
            val testParser = BaseParser("" + input + "50")
            val parser = string(input)
            val result = parser(testParser)
            assertEquals("string($input)", result.lastParserName)
            assertEquals(input, result.results[0])
            assertEquals(true, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @ParameterizedTest
        @ValueSource(strings = ["test", "__te st ", "123test"])
        fun shouldRespectShouldCapture(input: String){
            val testParser = BaseParser("" + input + "50")
            val parser = string(input, shouldCapture = false)
            val result = parser(testParser)
            assertEquals("string($input)", result.lastParserName)
            assertTrue(result.results.isEmpty())
            assertEquals(true, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @Test
        fun shouldFailCorrectly(){
            val testParser = BaseParser(" hello")
            val parser = string("hello")
            val result = parser(testParser)
            assertEquals("string(hello)", result.lastParserName)
            assertEquals(true, result.hasNext)
            assertEquals(true, result.hasError)
            assertEquals("Expected 'hello' but received ' hell'", result.error)
        }
    }

    @Nested
    @DisplayName("anyLengthString()")
    inner class AnyLengthString() {
        @ParameterizedTest
        @ValueSource(strings = ["test", "__test", "123test", "a", "okay"])
        fun shouldPass(input: String){
            val testParser = BaseParser("" + input)
            val parser = anyLengthString()
            val result = parser(testParser)
            assertEquals("anyLengthString()", result.lastParserName)
            assertEquals(input, result.results[0])
            assertEquals(false, result.hasNext)
            assertEquals(false, result.hasError)
            assertEquals(null, result.error)
        }

        @Test
        fun shouldFailCorrectly(){
            val testParser = BaseParser(" hello")
            val parser = string("hello")
            val result = parser(testParser)
            assertEquals("string(hello)", result.lastParserName)
            assertEquals(true, result.hasNext)
            assertEquals(true, result.hasError)
            assertEquals("Expected 'hello' but received ' hell'", result.error)
        }
    }

    @Nested
    @DisplayName("oneOf()")
    inner class OneOfParser() {
        @Test
        fun shouldPass(){
            val input1 = BaseParser("a")
            val input2 = BaseParser(" ")

            val parser = oneOf(space(shouldCapture = true), anyLetter())
            val resultOne = parser(input1)
            assertEquals("oneOf(space(), anyLetter())", resultOne.lastParserName)
            assertEquals('a', resultOne.results[0])
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)

            val resultTwo = parser(input2)
            assertEquals("oneOf(space(), anyLetter())", resultOne.lastParserName)
            assertEquals(" ", resultTwo.results[0])
            assertEquals(false, resultTwo.hasNext)
            assertEquals(false, resultTwo.hasError)
            assertEquals(null, resultTwo.error)
        }

        @Test
        fun shouldFailIfTwoPass(){
            val input1 = BaseParser("a")

            val parser = oneOf(anyLetter(), anyLetter())
            val resultOne = parser(input1)
            assertEquals("oneOf(anyLetter(), anyLetter())", resultOne.lastParserName)
            assertEquals('a', resultOne.results[0])
            assertEquals(true, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Expected only one parser to match but received: anyLetter(), anyLetter()", resultOne.error)
        }

        @Test
        fun shouldFailIfNonePass(){
            val input1 = BaseParser("1")

            val parser = oneOf(space(shouldCapture = true), anyLetter())
            val resultOne = parser(input1)
            assertEquals("oneOf(space(), anyLetter())", resultOne.lastParserName)
            assertEquals(true, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Expected only one parser to match but received none: \nExpected ' ' but received '1' (space())\nExpected letter but received '1' (anyLetter())", resultOne.error)
        }
    }

    @Nested
    @DisplayName("sequenceOf()")
    inner class SequenceOf() {
        @Test
        fun shouldPass(){
            val input1 = BaseParser("a + b")

            val parser = sequenceOf(anyLetter(), space(), char('+'), space(), anyLetter())
            val resultOne = parser(input1)
            assertEquals("sequenceOf(anyLetter(), space(), char(+), space(), anyLetter())", resultOne.lastParserName)
            assertEquals(listOf('a', '+', 'b'), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldFailIfSequenceIsBroken(){
            val input1 = BaseParser("a  + b")

            val parser = sequenceOf(anyLetter(), space(), char('+'), space(), anyLetter())
            val resultOne = parser(input1)
            assertEquals("sequenceOf(anyLetter(), space(), char(+), space(), anyLetter())", resultOne.lastParserName)
            assertEquals(true, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Expected '+' but received ' '", resultOne.error)
        }
    }

    @Nested
    @DisplayName("number()")
    inner class Number() {
        @Test
        fun shouldPass(){
            val input1 = BaseParser("10+523+3")

            val parser = sequenceOf(number(Int::class), char('+'), number(Int::class), char('+'), number(Int::class))
            val resultOne = parser(input1)
            assertEquals("sequenceOf(number(Int), char(+), number(Int), char(+), number(Int))", resultOne.lastParserName)
            assertEquals(listOf(10, '+', 523, '+', 3), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldParseNegative(){
            val input1 = BaseParser("-10+-523")

            val parser = sequenceOf(number(Int::class), char('+'), number(Int::class))
            val resultOne = parser(input1)
            assertEquals("sequenceOf(number(Int), char(+), number(Int))", resultOne.lastParserName)
            assertEquals(listOf(-10, '+', -523), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldFailWithDot(){
            val input1 = BaseParser(".")

            val parser = sequenceOf(number(Int::class))
            val resultOne = parser(input1)
            assertEquals("sequenceOf(number(Int))", resultOne.lastParserName)
            assertEquals(true, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Expected number but got '.'", resultOne.error)
        }

        @Test
        fun shouldParseLong(){
            val input1 = BaseParser((1L + Int.MAX_VALUE).toString())

            val parser = number(Long::class)
            val resultOne = parser(input1)
            assertEquals("number(Long)", resultOne.lastParserName)
            assertEquals(listOf(1L + Int.MAX_VALUE), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
        }

        @Test
        fun shouldParseDouble(){
            val input1 = BaseParser("1.5")

            val parser = number(Double::class)
            val resultOne = parser(input1)
            assertEquals("number(Double)", resultOne.lastParserName)
            assertEquals(listOf(1.5), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
        }

        @Test
        fun shouldParseNegativeDouble(){
            val input1 = BaseParser("-1.5")

            val parser = number(Double::class)
            val resultOne = parser(input1)
            assertEquals("number(Double)", resultOne.lastParserName)
            assertEquals(listOf(-1.5), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
        }

        @Test
        fun shouldFailIfNoDigitCharacterFound(){
            val input1 = BaseParser("ab+10")

            val parser = sequenceOf(number(Int::class), char('+'), number(Int::class), char('+'), number(Int::class))
            val resultOne = parser(input1)
            assertEquals("sequenceOf(number(Int), char(+), number(Int), char(+), number(Int))", resultOne.lastParserName)
            assertEquals(true, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Expected number but got 'a'", resultOne.error)
        }
    }

    @Nested
    @DisplayName("parseTillEnd()")
    inner class ParseTillEnd() {
        @Test
        fun shouldParseTillEnd(){
            val input1 = BaseParser("10 15 20 25 30 35 40 45 50 ")

            val parser = parseTillEnd(sequenceOf(number(Int::class), space()))
            val resultOne = parser(input1)
            assertEquals("parseTillEnd(sequenceOf(number(Int), space()))", resultOne.lastParserName)
            assertEquals(listOf(10, 15, 20, 25, 30, 35, 40, 45, 50), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldErrorIfNoMovementBetweenIteration(){
            val input1 = BaseParser("100 100  ")

            val parser = parseTillEnd(oneOrMoreTimes(sequenceOf(number(Int::class), space())))
            val resultOne = parser(input1)
            assertEquals("parseTillEnd(oneOrMoreTimes(sequenceOf(number(Int), space())))", resultOne.lastParserName)
            assertEquals(listOf(100, 100), resultOne.results)
            assertEquals(true, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Parser did not move between iterations (warning: Expected number but got ' ')", resultOne.error)
        }

        @Test
        fun shouldFailIfSubParserFails(){
            val input1 = BaseParser("10 15 20 25 30 35 40 45 50 ")

            val parser = parseTillEnd(sequenceOf(number(Int::class), space()))
            val resultOne = parser(input1)
            assertEquals("parseTillEnd(sequenceOf(number(Int), space()))", resultOne.lastParserName)
            assertEquals(listOf(10, 15, 20, 25, 30, 35, 40, 45, 50), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }
    }

    @Nested
    @DisplayName("oneOrMoreTimes()")
    inner class OneOrMoreTimes() {
        @Test
        fun shouldParseUntilFailure(){
            val input1 = BaseParser("10 15 20 25 30 35 40 45 50 hello world!")

            val parser = sequenceOf(oneOrMoreTimes(sequenceOf(number(Int::class), space())), string("hello world!"))
            val resultOne = parser(input1)
            assertEquals("sequenceOf(oneOrMoreTimes(sequenceOf(number(Int), space())), string(hello world!))", resultOne.lastParserName)
            assertEquals(listOf(10, 15, 20, 25, 30, 35, 40, 45, 50, "hello world!"), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldParseUntilEnd(){
            val input1 = BaseParser("10 15 20 25 30 35 40 45 50 ")

            val parser = oneOrMoreTimes(sequenceOf(number(Int::class), space()))
            val resultOne = parser(input1)
            assertEquals("oneOrMoreTimes(sequenceOf(number(Int), space()))", resultOne.lastParserName)
            assertEquals(listOf(10, 15, 20, 25, 30, 35, 40, 45, 50), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldStopAtFailure(){
            val input1 = BaseParser("10 15 20  ")

            val parser = oneOrMoreTimes(sequenceOf(number(Int::class), space()))
            val resultOne = parser(input1)
            assertEquals("oneOrMoreTimes(sequenceOf(number(Int), space()))", resultOne.lastParserName)
            assertEquals(listOf(10, 15, 20), resultOne.results)
            assertEquals(true, resultOne.hasNext)
            assertEquals("Expected number but got ' '", resultOne.warning)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }
    }

    @Nested
    @DisplayName("group()")
    inner class Group() {
        @Test
        fun shouldPassAndGroup(){
            val input1 = BaseParser("a+bz+d")

            val parser = oneOrMoreTimes(group(sequenceOf(anyLetter(), char('+'), anyLetter())))
            val resultOne = parser(input1)
            assertEquals("oneOrMoreTimes(group(sequenceOf(anyLetter(), char(+), anyLetter())))", resultOne.lastParserName)
            assertEquals(listOf(listOf('a', '+', 'b'), listOf('z', '+', 'd')), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldShortCircuitOnFailure(){
            val input1 = BaseParser("a+bz+")

            val parser = oneOrMoreTimes(group(sequenceOf(anyLetter(), char('+'), anyLetter())))
            val resultOne = parser(input1)
            assertEquals("oneOrMoreTimes(group(sequenceOf(anyLetter(), char(+), anyLetter())))", resultOne.lastParserName)
            assertEquals(listOf(listOf('a', '+', 'b'), listOf('z', '+')), resultOne.results)
            assertEquals(true, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldNotGroupIfNoResults(){
            val input1 = BaseParser("ab2345")

            val parser = parseTillEnd(group(anyLetter()))
            val resultOne = parser(input1)
            assertEquals("parseTillEnd(group(anyLetter()))", resultOne.lastParserName)
            assertEquals(listOf(listOf('a'), listOf('b')), resultOne.results)
            assertEquals(true, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Expected letter but received '2'", resultOne.error)
        }
    }

    @Nested
    @DisplayName("optional()")
    inner class Optional() {
        @Test
        fun shouldPassEvenIfFailing(){
            val input1 = BaseParser("a\nb")

            val parser = parseTillEnd(sequenceOf(anyLetter(), optional(newLine())))
            val resultOne = parser(input1)
            assertEquals("parseTillEnd(sequenceOf(anyLetter(), optional(newLine())))", resultOne.lastParserName)
            assertEquals(listOf('a', 'b'), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }
    }

    @Nested
    @DisplayName("stringMap()")
    inner class StringMap() {
        @Test
        fun shouldPassAndMapValueCorrectly(){
            val input1 = BaseParser("abc")

            val parser = parseTillEnd(stringMap(mapOf("a" to 1, "b" to 2, "c" to 3)))
            val resultOne = parser(input1)
            assertEquals("parseTillEnd(oneOf(string(a), string(b), string(c)))", resultOne.lastParserName)
            assertEquals(listOf(1,2,3), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }
    }

    @Nested
    @DisplayName("enum()")
    inner class EnumParser() {
        @Test
        fun shouldParseEnumCorrectly(){
            val input1 = BaseParser("abc")

            val parser = parseTillEnd(enum(LetterEnumTest))
            val resultOne = parser(input1)
            assertEquals("parseTillEnd(enum(oneOf(string(a), string(b), string(c))))", resultOne.lastParserName)
            assertEquals(listOf(LetterEnumTest.A_TEST,LetterEnumTest.B_TEST,LetterEnumTest.C_TEST), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldFailCorrectly(){
            val input1 = BaseParser("abe")

            val parser = parseTillEnd(enum(LetterEnumTest))
            val resultOne = parser(input1)
            assertEquals("parseTillEnd(enum(oneOf(string(a), string(b), string(c))))", resultOne.lastParserName)
            assertEquals(listOf(LetterEnumTest.A_TEST,LetterEnumTest.B_TEST), resultOne.results)
            assertEquals(true, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("""Expected only one parser to match but received none: 
            |Expected 'a' but received 'e' (string(a))
            |Expected 'b' but received 'e' (string(b))
            |Expected 'c' but received 'e' (string(c))
            """.trimMargin(), resultOne.error)
        }
    }

    data class StringIntClass(val firstString: String = "", val num: Int = 0)

    @Nested
    @DisplayName("toClass()")
    inner class ClassParser() {
        @Test
        fun shouldParseClassCorrectly(){
            val input1 = BaseParser("hello 10")

            val parser = toClass(sequenceOf(anyLengthString(), space(), number(Int::class)), StringIntClass::class)
            val resultOne = parser(input1)
            assertEquals("toClass(StringIntClass, sequenceOf(anyLengthString(), space(), number(Int)))", resultOne.lastParserName)
            assertEquals(listOf(StringIntClass("hello", 10)), resultOne.results)
            assertEquals(false, resultOne.hasNext)
            assertEquals(false, resultOne.hasError)
            assertEquals(null, resultOne.error)
        }

        @Test
        fun shouldFailIfTypesDoNotMatch(){
            val input1 = BaseParser("10 hello")

            val parser = toClass(sequenceOf(number(Int::class), space(), anyLengthString()), StringIntClass::class)
            val resultOne = parser(input1)
            assertEquals("toClass(StringIntClass, sequenceOf(number(Int), space(), anyLengthString()))", resultOne.lastParserName)
            assertEquals(false, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Expected parameter of type kotlin.String but got: Int", resultOne.error)
        }

        @Test
        fun shouldFailIfNotEnoughArgs(){
            val input1 = BaseParser("10")

            val parser = toClass(sequenceOf(number(Int::class)), StringIntClass::class)
            val resultOne = parser(input1)
            assertEquals("toClass(StringIntClass, sequenceOf(number(Int)))", resultOne.lastParserName)
            assertEquals(false, resultOne.hasNext)
            assertEquals(true, resultOne.hasError)
            assertEquals("Expected parser results to contain 2 results but contained: 1", resultOne.error)
        }
    }
}