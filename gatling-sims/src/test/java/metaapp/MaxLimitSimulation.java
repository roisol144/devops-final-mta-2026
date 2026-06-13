package metaapp;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

/**
 * Max-limit: ramp arrival rate of new "sessions" while each session does a
 * short burst of requests on a single keep-alive connection, to find the
 * cliff of the public Oracle VM (1 OCPU) measured over the internet.
 *
 * Tuned for a remote target: a gentle ramp keeps the *client* from ever
 * becoming the bottleneck (the earlier 200 VUs/sec profile exhausted the
 * Mac's per-process file-descriptor cap before the server cliffed). Each VU
 * does 50 requests on one TCP connection; ramp 2 -> 80 new VUs/sec over 2 min.
 */
public class MaxLimitSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
        .acceptHeader("text/html,application/xhtml+xml")
        .userAgentHeader("Gatling-MaxLimit/1.0")
        .maxConnectionsPerHost(400);

    ScenarioBuilder scn = scenario("Ramp arrival rate to find the cliff")
        // Each VU does 50 requests back-to-back on its keep-alive connection
        .repeat(50).on(
            exec(http("GET /roi-shiraz-omri-noa-arbel-app/").get("/roi-shiraz-omri-noa-arbel-app/").check(status().is(200)))
        );

    {
        setUp(
            scn.injectOpen(
                rampUsersPerSec(10).to(160).during(Duration.ofMinutes(2))
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile3().lt(1000)
        );
    }
}
