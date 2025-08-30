import com.github.cao.awa.lunaria.consumer.ConsumerLunaria
import com.github.cao.awa.lunaria.consumer.group.GroupConsumerLunaria
import com.github.cao.awa.lunaria.supplier.SupplierLunaria
import com.github.cao.awa.lunaria.supplier.group.GroupSupplierLunaria
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

    for (i: Int in 0..100) {
        testGroupConsumer()
    }
}

fun testGroupSuppliers() {
    var index: Int = 0
    val tasks: GroupSupplierLunaria<Int> = GroupSupplierLunaria(20, 4) {
        println("* index Calculating...")
        Thread.sleep(100)
        index ++
    }

    // Do other things...
    println("* Other things...")
    println("* Other things...")
    println("* Other things...")
    println("* Other things...")
    val result: List<Int?> = tasks.get()
    println("Result: $result")
    println("* Next things...")
}

fun testGroupConsumer() {
    val inputs: List<Double> = listOf(
        1.0, 4.0, 9.0, 16.0, 25.0,
        36.0, 49.0, 64.0, 81.0, 100.0,
        121.0, 144.0, 169.0, 196.0, 225.0,
        256.0, 289.0, 324.0, 361.0, 400.0,
        441.0, 484.0, 529.0, 576.0, 625.0,
        676.0, 729.0, 784.0, 841.0, 900.0,
        961.0, 1024.0, 1089.0, 1156.0, 1225.0
    )
    val tasks: GroupConsumerLunaria<Double> = GroupConsumerLunaria(inputs, 5) { input ->
        println("* $input sqrt Calculating...")
        Thread.sleep(100)
        sqrt(input)
        println("* $input sqrt Calculated: ${sqrt(input)}")
        Thread.sleep(100)
    }

    // Do other things...
    println("* Other things...")
    println("* Other things...")
    println("* Other things...")
    println("* Other things...")
    tasks.await()
    println("* Next things...")
}

fun testLunariaSupplier() {
    val task: SupplierLunaria<Double> = SupplierLunaria {
        var result: Double = 0.0
        for (i: Int in 1..5000000) {
            result += sqrt(Double.MAX_VALUE) / i
        }
        result
    }
    // Do other things...
    println("* Other things...")
    // Got result when you need to use.
    val sqrtTimes: Double? = task.get()
    println(sqrtTimes)
}

fun testLunariaConsumer() {
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