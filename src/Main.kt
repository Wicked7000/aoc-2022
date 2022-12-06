import java.io.*;

fun main() {
    while(true){
        println("Please enter the number of the day to Run: ")
        val day = readln().toInt();
        val dayFile = File("./src/day$day/Day$day.kt")
        if (dayFile.exists()) {
            val dayKClass = Class.forName("day$day.Day$day");
            val dayInstance = dayKClass.getDeclaredConstructor().newInstance()
            val asDayInstance = dayInstance as? Day

            try {
                asDayInstance?.run()
            } catch (e: Exception){
                println("Exception occurred while running day $day")
                e.printStackTrace()
            } catch (e: Error) {
                println("Error (usually test failure) occurred while running day: $day")
                e.printStackTrace()
            }
        } else {
            println("Day not recognised, please try another!")
        }
    }
}