package metaapp;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

/**
 * Load test: constant arrival rate BELOW measured max-limit, sustained 5 min.
 *
 * 50 new VUs/sec × 200 keep-alive requests = ~10,000 RPS sustained
 * (max-limit measured at ≥20,800 RPS, so this is roughly 50% of capacity —
 * the production "normal traffic" envelope).
 *
 * Expectation: 0 errors, p99 well under 1s. If assertions fail, the
 * app has regressed under expected load.
 */
public class LoadSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("text/html,application/xhtml+xml")
        .userAgentHeader("Gatling-Load/1.0")
        .maxConnectionsPerHost(2000);

    ScenarioBuilder scn = scenario("Steady load below max limit")
        .repeat(200).on(
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
