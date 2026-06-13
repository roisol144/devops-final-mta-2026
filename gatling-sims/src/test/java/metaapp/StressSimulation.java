package metaapp;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

/**
 * Stress test: constant arrival rate ABOVE the measured max-limit, sustained
 * 5 min, against the public Oracle VM (1 OCPU). Goal: observe degradation,
 * queue growth, rising response times and errors under over-pressure.
 *
 * 140 new VUs/sec × 50 keep-alive requests pushes the offered load past the
 * ~4,000-5,000 RPS cliff. The connection pool is capped low so the SERVER is
 * the thing that saturates (response-time tails balloon, queueing), rather
 * than the load generator exhausting its own sockets.
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
        .maxConnectionsPerHost(300);

    ScenarioBuilder scn = scenario("Heavy sustained load above max limit")
        .repeat(50).on(
            exec(http("GET /roi-shiraz-omri-noa-arbel-app/").get("/roi-shiraz-omri-noa-arbel-app/").check(status().is(200)))
        );

    {
        setUp(
            scn.injectOpen(
                constantUsersPerSec(140).during(Duration.ofMinutes(5))
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile3().lt(1000)
        );
    }
}
