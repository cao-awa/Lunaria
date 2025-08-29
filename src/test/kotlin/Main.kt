import com.github.cao.awa.lunaria.consumer.ConsumerLunaria
import com.github.cao.awa.lunaria.supplier.SupplierLunaria
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.math.sqrt

fun main() {
    // Initial test runs to warm up the JVM
    println("Initial warm-up tests")
    testLunaria()
    testJavaConcurrent()

    println("-----------------------------------")

//    println("Start testing Lunaria")
//    testLunaria()
//    println("Start testing Java Concurrent")
//    testJavaConcurrent()
//    println("Start testing Lunaria Cancel")
//    testLunariaCancel()
//    println("Start testing Java Cancel")
//    testJavaCancel()

//    val start: Long = System.nanoTime()
//
//    val task: SupplierLunaria<Double> = SupplierLunaria {
//        var result: Double = 0.0
//        for (i: Int in 1..5000000) {
//            result += sqrt(Double.MAX_VALUE) / i
//        }
//        result
//    }
//    // Do other things...
//    println("* Other things...")
//    // Got result when you need to use.
//    val sqrtTimes: Double? = task.get()
//    println(sqrtTimes)
//    println("Lunaria total take ${(System.nanoTime() - start) / 1_000_000} ms")


    val start: Long = System.nanoTime()

    val task: ConsumerLunaria<Double> = ConsumerLunaria(Double.MAX_VALUE) { input: Double ->
        println("* Calculating...")
        Thread.sleep(1000)
        sqrt(input)
        println("* Calculated: ${sqrt(input)}")
        Thread.sleep(1000)
    }
    // Do other things...
    println("* Other things...")
    println("* Other things...")
    println("* Other things...")
    println("* Other things...")
    task.await()
    println("* Next things...")
//
//    println("Lunaria total take ${(System.nanoTime() - start) / 1_000_000} ms")
}

fun testLunariaCancel() {
    val action: SupplierLunaria<String> = SupplierLunaria.of {
        for (i in 1..10) {
            println("Running $i")
            Thread.sleep(100)
        }

        "Result"
    }.withException<InterruptedException> {
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
        val action: SupplierLunaria<String> = SupplierLunaria.of {
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