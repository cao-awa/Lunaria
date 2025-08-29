import com.github.cao.awa.lunaria.Lunaria
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

fun main() {
    // Initial test runs to warm up the JVM
    println("Initial warm-up tests")
    testLunaria()
    testJavaConcurrent()

    println("-----------------------------------")

    println("Start testing Lunaria")
    testLunaria()
    println("Start testing Java Concurrent")
    testJavaConcurrent()
    println("Start testing Lunaria Cancel")
    testLunariaCancel()
    println("Start testing Java Cancel")
    testJavaCancel()
}

fun testLunariaCancel() {
    val action: Lunaria<String> = Lunaria.of {
        for (i in 1..10) {
            println("Running $i")
            Thread.sleep(100)
        }
        "Result"
    }.withExceptionHandler<InterruptedException> {
        println("Caught InterruptedException: ${it.message}")
    }
    Thread.sleep(300)
    action.cancel()
    val result = action.get()
    println("Result: $result")
}

fun testJavaCancel() {
    val action: CompletableFuture<String> = CompletableFuture.supplyAsync {
        for (i in 1..10) {
            println("Running $i")
            Thread.sleep(100)
        }
        "Result"
    }
    Thread.sleep(300)
    action.cancel(false)
    val result = action.get()
    println("Result: $result")
}

fun testLunaria() {
    val start: Long = System.nanoTime()
    val count = 100
    println("Test Lunaria concurrent with $count tasks")
    for (i: Int in 0..count) {
        val action: Lunaria<String> = Lunaria.of {
            "Result"
        }
        val result = action.get()
    }
    println("Lunaria take ${(System.nanoTime() - start) / 1_000_000} ms")
}

fun testJavaConcurrent() {
    val count = 100
    println("Test Java concurrent with $count tasks")
    val start: Long = System.nanoTime()
    for (i: Int in 0..count) {
        val action: Future<String> = CompletableFuture.supplyAsync {
            "Result"
        }
        val result = action.get()
    }
    println("Java take ${(System.nanoTime() - start) / 1_000_000} ms")
}