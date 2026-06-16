package metaapp;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

/**
 * Load test: trapezoid profile — ramp-up → steady state → ramp-down.
 * Shape matches the lecture's load test diagram (Moshe Mamia, Lecture 9).
 *
 * Sustainable max N = ~70 u/s (constant-rate calibration). Load runs at 90%
 * of that = 63 u/s. Each VU does 40 keep-alive requests.
 *
 * Profile (5 min total):
 *   1 min ramp-up  0 -> 63 u/s
 *   3 min steady       63 u/s
 *   1 min ramp-down 63 -> 0 u/s
 *
 * Expectation: < 1% errors and low, stable latency throughout.
 */
public class LoadSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
        .acceptHeader("text/html,application/xhtml+xml")
        .userAgentHeader("Gatling-Load/1.0")
        .maxConnectionsPerHost(300);

    ScenarioBuilder scn = scenario("Steady load below max limit")
        .repeat(40).on(
            exec(http("GET /roi-shiraz-omri-noa-arbel-app/").get("/roi-shiraz-omri-noa-arbel-app/").check(status().is(200)))
        );

    {
        setUp(
            scn.injectOpen(
                rampUsersPerSec(0).to(63).during(Duration.ofMinutes(1)),    // ramp-up
                constantUsersPerSec(63).during(Duration.ofMinutes(3)),       // steady state (90% of max)
                rampUsersPerSec(63).to(0).during(Duration.ofMinutes(1))     // ramp-down
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile3().lt(1000)
        );
    }
}
