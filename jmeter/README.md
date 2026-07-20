# JMeter Drill — Trade API Load Test

Hands-on companion to the practice-bank question *"Walk me through building a
JMeter test plan from scratch for a calculation API"* (Luxoft VR-123702 gap).
The target is the repo's own `MockTradeApi` — no internet, no Spring Boot.

## Plan anatomy (recite this in the interview)

| Component | In this plan | Why it exists |
|---|---|---|
| Thread Group | `${__P(users,5)}` users, 5s ramp-up, 10 loops | Sized from a traffic profile, overridable from the CLI |
| HTTP Request Defaults | `localhost:${__P(port,8099)}` | Host/port in ONE place |
| Header Manager | `Content-Type` + `X-API-KEY` | POST returns 401 without the key — delete it to see assertions fail |
| CSV Data Set Config | `trade-inputs.csv` | Unique inputs per thread; identical inputs hit caches and measure nothing |
| Samplers | GET trade, POST trade (CSV body), GET valuation | The three endpoint shapes: read, create, calculation |
| Response Assertions | 200/201 + body contains | Status alone isn't correctness |
| JSON Assertions | `$.totals.grossMarketValue == 44615.0` | A fast wrong answer is still a failure |
| Duration Assertion | 500 ms on the GET | An SLA check, not just an observation |
| Summary Report | GUI only | Build in GUI, run in CLI |

## Run it

**1. Start the target** (terminal 1, from the repo root):

```powershell
mvn test-compile exec:java "-Dexec.classpathScope=test" "-Dexec.mainClass=com.sampleselenium.drills.d09_api.RunMockTradeApi"
```

Sanity check: `curl http://localhost:8099/api/trades/1001`

**2a. GUI mode** (build/debug only — JMeter must be installed and on PATH):

```powershell
jmeter -t jmeter\trade-api-load-test.jmx
```

Click the green start arrow, watch View Results Tree and Summary Report.

**2b. CLI mode** (the real run — GUI mode distorts load):

```powershell
cd jmeter
jmeter -n -t trade-api-load-test.jmx -l results.jtl -e -o report
```

- `-n` non-GUI, `-t` test plan, `-l` raw results, `-e -o` HTML dashboard into `report/`
- Scale it up without touching the plan: `-Jusers=20 -Jrampup=10 -Jloops=50 -Jport=8099`

**3. Read the results:** open `report/index.html`. Look at **p90/p95 and error
rate, not averages** — one 10-second outlier disappears in an average and
dominates a p99. Correlate any spike with server-side metrics before blaming
the app.

## Drill variations

1. Remove the `X-API-KEY` header row → run → explain the 401s in the report.
2. Bump `-Jusers` until error rate or p95 degrades → practice the "response
   times spike at 50 users" troubleshooting narrative (practice bank #132).
3. Change the expected `grossMarketValue` → run → show how a calculation
   assertion failure looks different from an HTTP failure in the dashboard.

## No JMeter installed?

Download the binary zip from <https://jmeter.apache.org/download_jmeter.cgi>,
unzip, and use `bin\jmeter.bat`. (Java 17 in this repo satisfies JMeter 5.6's
Java 8+ requirement.)

`results.jtl` and `report/` are throwaway output — don't commit them.
