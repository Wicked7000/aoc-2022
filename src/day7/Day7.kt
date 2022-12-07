package day7

import BaseParser
import Day
import checkWithMessage
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import readInput
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

  class ParserState(input: List<String>): BaseParser(input) {
    private var index: Int = 0

    var rootFile: File? = null
      private set

    var currentFile: File? = null
      private set


    fun setRootFile(file: File) {
      rootFile = file
    }

    fun setCurrentFile(file: File) {
      currentFile = file
    }
  }

  data class Command(val type: CommandType, val operand: String? = null)
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

  private fun parseListDirectory(state: ParserState) {
    while(state.peek() != null && state.peek()!![0] != '$'){
      val (sizeOrType, name) = state.next().split(" ")
      val size = if (sizeOrType == "dir") 0 else sizeOrType.toLong()
      val type = if (sizeOrType == "dir") FileType.DIRECTORY else FileType.FILE
      state.currentFile?.addChildFile(File(name, type, size, state.currentFile))
    }
  }

  private fun parseChangeDirectory(data: String, state: ParserState) {
    when (data) {
      "/" -> {
        if(state.rootFile != null){
          throw Error("Mutable root directories found!");
        }
        val newRootFile = File(data, FileType.DIRECTORY, 0, null)
        state.setRootFile(newRootFile)
        state.setCurrentFile(newRootFile)
      }
      ".." -> {
        if(state.currentFile?.parent == null){
          throw Error("No parent for the current file!")
        }
        state.setCurrentFile(state.currentFile?.parent!!)
      }
      else -> {
        val matchingDirectory = state.currentFile?.getChildFile(data)
        if(matchingDirectory == null){
          val childDirectory = File(data, FileType.DIRECTORY, 0, state.currentFile)
          state.currentFile?.addChildFile(childDirectory)
          state.setCurrentFile(childDirectory)
        } else {
          state.setCurrentFile(matchingDirectory)
        }
      }
    }
  }

  private fun parseCommand(state: ParserState): Boolean {
    if(state.peek() != null && state.peek()!![0] == '$') {
      // Parse command
      val cmdStr = state.next().substring(2)
      when {
        cmdStr.startsWith("cd") -> {
          val (_, operand) = cmdStr.split(" ")
          parseChangeDirectory(operand, state)
        }
        cmdStr.startsWith("ls") -> parseListDirectory(state)
        else -> throw Error("Unknown command")
      }
      return true
    }
    throw Error("Expected a command but received ${state.peek()}")
  }

  private fun parseInput(input: List<String>): File {
    val parserState = ParserState(input)

    while(!parserState.hasParsed()){
      parseCommand(parserState)
    }

    if(parserState.rootFile == null){
      throw Error("No root file was found while parsing!")
    } else {
      return parserState.rootFile!!
    }
  }

  private fun part1(input: List<String>): Long {
    val rootFile = parseInput(input)
    return File.getAllDirectoriesOfSize(rootFile, 100000, false).sumOf { it.size };
  }

  private fun part2(input: List<String>): Long {
    val rootFile = parseInput(input)
    val spaceNeededToFreeUp = 30000000 - (70000000 - rootFile.size)
    return File.getAllDirectoriesOfSize(rootFile, spaceNeededToFreeUp, true).minOf { it.size };
  }

  override fun run() {
    val testData = readInput(7,"test")
    val inputData = readInput(7, "input")

    val testResultP1 = part1(testData)
    checkWithMessage(testResultP1, 95437L)

    runTimedPart(1, { part1(it) }, inputData)

    val testResultP2 = part2(testData)
    checkWithMessage(testResultP2, 24933642L)

    runTimedPart(2, { part2(it) }, inputData)
  }
}