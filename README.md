# Lunaria
The Lunaria is a powerful and flexible concurrent library.

# Performance
|  Tasks   | Lunaria | Java concurrent |
|:--------:|:-------:|:---------------:|
|    1     |  0 ms   |      0 ms       |
|    10    |  0 ms   |      0 ms       |
|   100    |  1 ms   |      1 ms       |
|   1000   |  5 ms   |      13 ms      |
|  10000   |  19 ms  |     106 ms      |
|  100000  |  20 ms  |     793 ms      |

# Application
The Lunaria is could use in task dense application, such as large HTTP server, large game server, etc.