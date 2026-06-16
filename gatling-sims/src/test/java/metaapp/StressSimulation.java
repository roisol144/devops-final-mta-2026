package metaapp;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

/**
 * Stress test: triangular spike pattern — two cycles of ramp-up-to-peak /
 * ramp-down-to-baseline, matching the spike-test diagram (Lecture 9, p.5).
 *
 * Sustainable max N = ~70 u/s. Baseline = 35 u/s (50% of N, healthy).
 * Spike peak = 140 u/s (200% of N, intentional overload).
 *
 * Profile (5 min total):
 *   1 min  baseline      35 u/s
 *   1 min  ramp 35→140   spike 1 rising
 *   1 min  ramp 140→35   spike 1 falling / recovery
 *   1 min  ramp 35→140   spike 2 rising
 *   1 min  ramp 140→35   spike 2 falling / recovery
 *
 * Goal: show failure at the peaks and automatic recovery as load returns
 * to baseline — two full cycles to prove it is not a one-off.
 */
public class StressSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
        .acceptHeader("text/html,application/xhtml+xml")
        .userAgentHeader("Gatling-Stress/1.0")
        .maxConnectionsPerHost(300);

    ScenarioBuilder scn = scenario("Spike/recovery cycles around max limit")
        .repeat(50).on(
            exec(http("GET /roi-shiraz-omri-noa-arbel-app/").get("/roi-shiraz-omri-noa-arbel-app/").check(status().is(200)))
        );

    {
        setUp(
            scn.injectOpen(
                constantUsersPerSec(35).during(Duration.ofMinutes(1)),          // baseline: 50% of max -> healthy
                rampUsersPerSec(35).to(140).during(Duration.ofMinutes(1)),      // spike 1: rising to 2x max
                rampUsersPerSec(140).to(35).during(Duration.ofMinutes(1)),      // spike 1: falling + recovery
                rampUsersPerSec(35).to(140).during(Duration.ofMinutes(1)),      // spike 2: rising to 2x max
                rampUsersPerSec(140).to(35).during(Duration.ofMinutes(1))       // spike 2: falling + recovery
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile3().lt(1000)
        );
    }
}
