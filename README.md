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

-—-—-—-—-—-—-—-—-—-—-—-—-
        
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
```