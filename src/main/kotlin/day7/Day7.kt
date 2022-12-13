package day7

import Day
import checkWithMessage
import parserCombinators.*
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import readInputString
import runTimedPart

@Suppress("unused")
class Day7 : Day() {

  enum class CommandType {
    CHANGE_DIRECTORY,
    LIST_DIRECTORY
  }

  enum class FileType {
    DIRECTORY,
    FILE,
  }

  class File (val name: String, val type: FileType = FileType.FILE, size: Long?, val parent: File?){
    private val contents: MutableList<File> = mutableListOf()

    private var cachedSize: Long? = null
    var size: Long = size ?: 0
      private set
      get() {
        if (cachedSize != null) {
          return cachedSize as Long
        }

        val value = if(this.type == FileType.DIRECTORY) {
          contents.map { it.size }.sumOf { it }
        } else {
          field;
        }
        cachedSize = value
        return value
      }

    fun getChildFile(fileName: String): File? {
      return contents.firstOrNull { it.name == fileName }
    }

    fun addChildFile(file: File){
      contents.add(file)
    }

    companion object {
      fun typeToString(file: File) = if (file.type == FileType.FILE) "(file, size=${file.size})" else "(dir, total_size=${file.size})"
      fun getAllDirectoriesOfSize(currentFile: File, size: Long, greaterThan: Boolean): List<File> {
        val totalSizeList = mutableListOf<File>()

        // Greater than
        if(greaterThan && currentFile.size >= size){
          totalSizeList.add(currentFile)
        }

        // Less than
        if(!greaterThan && currentFile.size <= size){
          totalSizeList.add(currentFile)
        }

        for(file in currentFile.contents){
          if(file.type == FileType.DIRECTORY){
            totalSizeList += getAllDirectoriesOfSize(file, size, greaterThan)
          }
        }

        return totalSizeList
      }
    }

    fun toHierarchyString(indentAmount: Int = 2): String {
      var str = toString()
      for(childFile in contents){
        str += "${" ".repeat(indentAmount)}${childFile.toHierarchyString(indentAmount + 2)}"
      }
      return str
    }

    override fun toString(): String {
      return "- $name (${typeToString(this)}\n"
    }
  }


  private fun parseListDirectory(): ParserFn {
      return newParser({ parser ->
        val innerParse = sequenceOf(
          string("ls", shouldCapture = false),
          newLine(),
          oneOrMoreTimes(
            group(
              sequenceOf(
                oneOf(string("dir"), numberLong()),
                space(),
                anyLengthString(),
                optional(newLine())
              )
            )
          )
        )
        val newParser = innerParse(parser)
        if(newParser.hasError){
          return@newParser newParser
        }

        val currentDirectory = newParser.context["CURRENT_DIRECTORY"]
        if(currentDirectory !is File?){
          newParser.error = "Expected current directory to be null or a File but got: $currentDirectory"
          return@newParser newParser
        }

        newParser.lastParserName = "parseListDirectory(${newParser.lastParserName})"
        while(newParser.results.size > 0){
          val groupResults = newParser.popLast()
          if(groupResults is List<*>){
            val fileName = groupResults[1]
            if(fileName !is String){
              newParser.error = "Expected last result to be string but got: $fileName"
              return@newParser newParser
            }

            if(groupResults[0] == "dir"){
              currentDirectory?.addChildFile(File(fileName, FileType.DIRECTORY, 0, currentDirectory))
            } else {
              val size = groupResults[0]
              if(size !is Long){
                newParser.error = "Expected last result to be Long but got: $size"
                return@newParser newParser
              }

              currentDirectory?.addChildFile(File(fileName, FileType.FILE, size, currentDirectory))
            }
          } else {
            newParser.error = "Expected group result to be of List<*> but got: $groupResults"
            return@newParser newParser
          }
        }

        return@newParser newParser
      }, "parseListDirectory(?)")
  }

  private fun parseChangeDirectory(): ParserFn {
    return newParser({ parser ->
      val innerParse = sequenceOf(string("cd", shouldCapture = false), space(), anyLengthString(), newLine())
      val newParser = innerParse(parser)
      if(newParser.hasError){
        newParser.lastParserName = "parseChangeDirectory(${newParser.lastParserName})"
        return@newParser newParser
      }
      val directoryName = newParser.popLast()
      if(directoryName !is String){
        newParser.error = "Expected last result to be string but got: $directoryName"
        return@newParser newParser
      }
      val currentDirectory = newParser.context["CURRENT_DIRECTORY"]
      if(currentDirectory !is File?){
        newParser.error = "Expected current directory to be null or a File but got: $currentDirectory"
        return@newParser newParser
      }

      if(directoryName == ".."){
        if(currentDirectory?.parent == null){
          newParser.error = "Expected parent of current directory to not be null but got: null"
          return@newParser newParser
        }
        newParser.context["CURRENT_DIRECTORY"] = currentDirectory?.parent
      } else {
        if(currentDirectory == null){
          val newFile = File(directoryName, FileType.DIRECTORY, 0, null)
          newParser.context["ROOT"] = newFile
          newParser.context["CURRENT_DIRECTORY"] = newFile
        } else {
          newParser.context["CURRENT_DIRECTORY"] = currentDirectory.getChildFile(directoryName) as Any
        }

      }


      return@newParser newParser
    }, "parseChangeDirectory(?)")
  }

  private fun parseCommand(): ParserFn {
    return sequenceOf(
      char('$', shouldCapture = false),
      space(),
      oneOf(
        parseChangeDirectory(),
        parseListDirectory()
      )
    )
  }

  private fun parseInput(input: String): File {
    val parserState = BaseParser(input)
    val parseTree = parseTillEnd(parseCommand())

    val result = parseTree(parserState)

    if(result.context["ROOT"] != null && result.context["ROOT"] is File){
      return result.context["ROOT"] as File
    } else {
      throw Error("No root file found!")
    }
  }

  private fun part1(input: String): Long {
    val rootFile = parseInput(input)
    return File.getAllDirectoriesOfSize(rootFile, 100000, false).sumOf { it.size };
  }

  private fun part2(input: String): Long {
    val rootFile = parseInput(input)
    val spaceNeededToFreeUp = 30000000 - (70000000 - rootFile.size)
    return File.getAllDirectoriesOfSize(rootFile, spaceNeededToFreeUp, true).minOf { it.size };
  }

  override fun run() {
    val testData = readInputString(7,"test")
    val inputData = readInputString(7, "input")

    val testResultP1 = part1(testData)
    checkWithMessage(testResultP1, 95437L)

    runTimedPart(1, { part1(it) }, inputData)

    val testResultP2 = part2(testData)
    checkWithMessage(testResultP2, 24933642L)

    runTimedPart(2, { part2(it) }, inputData)
  }
}