package metaapp;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

/**
 * Load test: constant arrival rate BELOW the measured max-limit, sustained 5 min,
 * against the public Oracle VM (1 OCPU) over the internet.
 *
 * 50 new VUs/sec × 40 keep-alive requests ≈ ~2,000-2,500 RPS sustained —
 * roughly 50% of the measured cliff (~4,000-5,000 RPS), i.e. the production
 * "normal traffic" envelope. Connections capped low so the load generator
 * never becomes the bottleneck.
 *
 * Expectation: 0 errors, p99 well under 1s. If assertions fail, the
 * app has regressed under expected load.
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
                constantUsersPerSec(50).during(Duration.ofMinutes(5))
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile3().lt(1000)
        );
    }
}
