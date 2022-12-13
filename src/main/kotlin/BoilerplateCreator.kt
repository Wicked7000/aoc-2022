import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

const val REMOVE_THIS_LINE = "//REMOVE_THIS_LINE_COMMENT"

fun stripRedundantTypeInformation(day: Int){
    val lines = File("./src/main/kotlin/day$day/Day$day.kt").readLines().toMutableList()
    var previousLine = ""

    val resultLines = mutableListOf<String>()

    for(lineIdx in lines.indices){
        if(previousLine == lines[lineIdx] || lines[lineIdx].contains(REMOVE_THIS_LINE)){
            previousLine = lines[lineIdx]
            continue;
        }

        resultLines.add(lines[lineIdx]
            .replace("public ", "")
            .replace(": Unit", ""))
        previousLine = lines[lineIdx]
    }

    File("./src/main/kotlin/day$day/Day$day.kt").writeText(resultLines.joinToString("\n"))
}
fun main(args: Array<String>) {
    val day = args[0].toInt()
    val dayFile = FileSpec.builder("day$day", "Day$day")
        .addImport("", "Day", "checkWithMessage", "readInput", "runTimedPart")
        .addType(
            TypeSpec.classBuilder("Day$day")
                .superclass(Day::class)
                .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember(CodeBlock.of("\"unused\"")).build())
                .addFunction(
                    FunSpec.builder("part1")
                        .addParameter(ParameterSpec.builder("input", List::class.plusParameter(String::class)).build())
                        .returns(Int::class)
                        .addModifiers(KModifier.PRIVATE)
                        .addCode(CodeBlock.of("""
                    $REMOVE_THIS_LINE
                    return input.size;
                """.trimIndent()))
                        .build()
                )
                .addFunction(
                    FunSpec.builder("part2")
                        .addParameter(ParameterSpec.builder("input", List::class.plusParameter(String::class)).build())
                        .returns(Int::class)
                        .addModifiers(KModifier.PRIVATE)
                        .addCode(CodeBlock.of("""
                    $REMOVE_THIS_LINE
                    return input.size;
                """.trimIndent()))
                        .build()
                )
                .addFunction(
                    FunSpec.builder("run")
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode(CodeBlock.of("""
                    val testData = readInput($day,"test")
                    val inputData = readInput($day, "input")
            
                    val testResultP1 = part1(testData)
                    checkWithMessage(testResultP1, 0)
            
                    runTimedPart(1, { part1(it) }, inputData)
            
                    val testResultP2 = part2(testData)
                    checkWithMessage(testResultP2, 0)
            
                    runTimedPart(2, { part2(it) }, inputData)
                """.trimIndent()))
                        .build()
                )
                .build()
        ).build()
    if(!Files.exists(Path("./src/main/kotlin/day$day/Day$day.kt"))){
        dayFile.writeTo(File("./src/main/kotlin"))
        Files.createDirectories(Path("./src/main/kotlin/day$day/data"))
        Files.createFile(Path("./src/main/kotlin/day$day/data/test.txt"))
        Files.createFile(Path("./src/main/kotlin/day$day/data/input.txt"))
        stripRedundantTypeInformation(day)
        println("Class file for day $day created!")
    } else {
        println("Unable to generate class for $day as it already exists!")
    }

}