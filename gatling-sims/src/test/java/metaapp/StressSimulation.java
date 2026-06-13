package metaapp;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

/**
 * Stress test: constant arrival rate ABOVE the practical comfortable level,
 * sustained 5 min. Goal: observe degradation, queue growth, errors, and
 * how the app behaves when over-pressured.
 *
 * 200 new VUs/sec × 200 keep-alive requests = ~40,000 RPS sustained
 * for 5 min. This is roughly 2× the measured max-limit (20,800 RPS).
 *
 * We INTENTIONALLY expect this to violate the assertions — that's the
 * point. The Gatling report will show response time spikes and any errors
 * that appear, demonstrating how the system fails under stress.
 */
public class StressSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
        .acceptHeader("text/html,application/xhtml+xml")
        .userAgentHeader("Gatling-Stress/1.0")
        .maxConnectionsPerHost(4000);

    ScenarioBuilder scn = scenario("Heavy sustained load above max limit")
        .repeat(200).on(
            exec(http("GET /roi-shiraz-omri-noa-arbel-app/").get("/roi-shiraz-omri-noa-arbel-app/").check(status().is(200)))
        );

    {
        setUp(
            scn.injectOpen(
                constantUsersPerSec(200).during(Duration.ofMinutes(5))
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile3().lt(1000)
        );
    }
}
