import com.squareup.kotlinpoet.typeNameOf
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.typeOf
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun checkWithMessage(actual: Any, expected: Any) {
    check(actual == expected) { "expected $expected (${expected::class.simpleName}), but received $actual (${actual::class.simpleName})" }
}

fun runTimedPart(partNum: Int, part: (input: List<String>) -> Any, input: List<String>) {
    val result: Any;
    var elapsed = measureNanoTime {
        result = part(input)
    }
    println("Part $partNum: $result (elapsed: ${elapsed/1e+6}ms)")
}

/**
 * Reads lines from the given input txt file.
 */
fun readInput(day: Int, name: String) = File("src/day$day/data", "$name.txt")
    .readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')
