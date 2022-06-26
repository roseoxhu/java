## ActiveJ poll example for MySQL version

[ActiveJ](https://activej.io/) official [poll example](https://github.com/activej/activej/tree/master/examples/tutorials/template-engine/)

## Changes
- [rocker template](https://github.com/fizzed/rocker)
- lombok for POJO Getters/Setters
- MySQL store: CRUD operations
- Hikari connection pool

## Performance for pool version

- pool + non-promise
```bash
λ bombardier-windows-amd64.exe -l -c100 -d20s http://localhost:8080/now
Bombarding http://localhost:8080/now for 20s using 100 connection(s)
[=========================================================================================================] 20s Done!
Statistics        Avg      Stdev        Max
  Reqs/sec      3636.00     200.52    3950.99
  Latency       27.48ms     1.14ms   116.01ms
  Latency Distribution
     50%    27.00ms
     75%    28.00ms
     90%    29.00ms
     95%    30.00ms
     99%    36.00ms
  HTTP codes:
    1xx - 0, 2xx - 72805, 3xx - 0, 4xx - 0, 5xx - 0
    others - 0
  Throughput:   663.97KB/s
```

- pool + promise

```bash
λ bombardier-windows-amd64.exe -l -c100 -d20s http://localhost:8080/asyncNow
Bombarding http://localhost:8080/asyncNow for 20s using 100 connection(s)
[=========================================================================================================] 20s Done!
Statistics        Avg      Stdev        Max
  Reqs/sec      6236.83    1159.32   13443.70
  Latency       16.01ms    31.00ms      2.08s
  Latency Distribution
     50%    11.00ms
     75%    16.00ms
     90%    29.00ms
     95%    44.00ms
     99%   140.01ms
  HTTP codes:
    1xx - 0, 2xx - 124511, 3xx - 0, 4xx - 0, 5xx - 0
    others - 0
  Throughput:     1.14MB/s
```
