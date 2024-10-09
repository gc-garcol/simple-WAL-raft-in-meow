# Simple WAL for Raft in meow

A simple demo Append-log for `Raft In Meow`.

## Benchmark

Result (batching 10k write commands into a single log entry)

- throughput: 29.5 million commands / s
![benchmark.png](benchmark-throughput.png)

- average time: 0.00042669055694420746 s / log-entry
![benchmark-average-time.png](benchmark-average-time.png)
