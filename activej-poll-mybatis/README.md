## ActiveJ poll example for MyBatis version

[ActiveJ](https://activej.io/) official [poll example](https://github.com/activej/activej/tree/master/examples/tutorials/template-engine/)

## Changes
- [rocker template](https://github.com/fizzed/rocker)
- lombok for POJO Getters/Setters
- MySQL store: CRUD operations
- Hikari connection pool
- MyBatis framework integrated

## Performance for pool version

- pool + non-promise + <transactionManager type="JDBC">

```bash
λ bombardier-windows-amd64.exe -l -c100 -d20s http://localhost:8080/now
Bombarding http://localhost:8080/now for 20s using 100 connection(s)
[=========================================================================================================] 20s Done!
Statistics        Avg      Stdev        Max
  Reqs/sec      1469.23      94.45    1599.91
  Latency       67.95ms     1.75ms   171.01ms
  Latency Distribution
     50%    67.00ms
     75%    69.00ms
     90%    72.00ms
     95%    75.00ms
     99%    88.00ms
  HTTP codes:
    1xx - 0, 2xx - 29479, 3xx - 0, 4xx - 0, 5xx - 0
    others - 0
  Throughput:   268.31KB/s
```

- pool + non-promise + <transactionManager type="MANAGED">

```bash
λ bombardier-windows-amd64.exe -l -c100 -d20s http://localhost:8080/now
Bombarding http://localhost:8080/now for 20s using 100 connection(s)
[=========================================================================================================] 20s Done!
Statistics        Avg      Stdev        Max
  Reqs/sec      3341.11     227.43    3699.80
  Latency       29.91ms     1.01ms   131.01ms
  Latency Distribution
     50%    29.00ms
     75%    30.00ms
     90%    32.00ms
     95%    37.00ms
     99%    43.00ms
  HTTP codes:
    1xx - 0, 2xx - 66908, 3xx - 0, 4xx - 0, 5xx - 0
    others - 0
  Throughput:   610.01KB/s
```

- pool + promise + <transactionManager type="JDBC">

```bash
λ bombardier-windows-amd64.exe -l -c100 -d20s http://localhost:8080/asyncNow
Bombarding http://localhost:8080/asyncNow for 20s using 100 connection(s)
[=========================================================================================================] 20s Done!
Statistics        Avg      Stdev        Max
  Reqs/sec      2762.67     801.99   16749.16
  Latency       37.11ms    23.31ms      0.95s
  Latency Distribution
     50%    33.00ms
     75%    40.00ms
     90%    73.00ms
     95%   103.00ms
     99%   194.01ms
  HTTP codes:
    1xx - 0, 2xx - 53903, 3xx - 0, 4xx - 0, 5xx - 0
    others - 0
  Throughput:   504.61KB/s
```

- pool + promise + <transactionManager type="MANAGED">

```bash
λ bombardier-windows-amd64.exe -l -c100 -d20s http://localhost:8080/asyncNow
Bombarding http://localhost:8080/asyncNow for 20s using 100 connection(s)
[=========================================================================================================] 20s Done!
Statistics        Avg      Stdev        Max
  Reqs/sec      5916.39     936.35   11332.77
  Latency       16.91ms    11.73ms      1.08s
  Latency Distribution
     50%    14.00ms
     75%    19.00ms
     90%    29.00ms
     95%    41.00ms
     99%    82.00ms
  HTTP codes:
    1xx - 0, 2xx - 118243, 3xx - 0, 4xx - 0, 5xx - 0
    others - 0
  Throughput:     1.08MB/s
```