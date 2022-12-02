import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

fun checkWithMessage(actual: Any, expected: Any) {
    check(actual == expected) { "expected $expected, but received $actual" }
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
