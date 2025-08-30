# Lunaria
The Lunaria is a powerful and flexible concurrent library.

# Performance
|  Tasks   | Lunaria | Java concurrent |
|:--------:|:-------:|:---------------:|
|    1     |  0 ms   |      0 ms       |
|    10    |  0 ms   |      0 ms       |
|   100    |  1 ms   |      1 ms       |
|   1000   |  3 ms   |      12 ms      |
|  10000   |  10 ms  |     111 ms      |
|  100000  |  63 ms  |     860 ms      |

# Application
The Lunaria is could use in task dense application, such as large HTTP server, large game server, etc.

### Build requirements

Lunaria has configured whole project, just clone the repository, and reload project then run the gradle task ```build```.

| Requirement | Version |             Notes             |
|------------:|:-------:|:-----------------------------:|
|        Java |   21+   |         21 Or higher          |
|      Gradle | 9.0.0+  | 9.0.0 or higher could be use  |
|      Kotlin | 2.2.21+ | 2.1.21 or higher could be use |

# Sample
## Kotlin
```kotlin
val task: SupplierLunaria<Double> = SupplierLunaria {
    var result = 0.0
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

// -—-—-—-—-—-—-—-—-—-—-—-—-
        
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

// -—-—-—-—-—-—-—-—-—-—-—-—-

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
```