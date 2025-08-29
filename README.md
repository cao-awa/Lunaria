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