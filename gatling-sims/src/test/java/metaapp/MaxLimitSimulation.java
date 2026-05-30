package metaapp;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

/**
 * Max-limit: ramp arrival rate of new "sessions" while each session does many
 * requests on a single keep-alive connection. This finds Tomcat's real cliff
 * without exhausting macOS ephemeral ports (~16k available).
 *
 * Each VU does 200 requests on one TCP connection.
 * Ramp from 5 to 60 new VUs/sec over 2 min → effective load ramps from
 * 1000 RPS to 12000 RPS at peak.
 */
public class MaxLimitSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("text/html,application/xhtml+xml")
        .userAgentHeader("Gatling-MaxLimit/1.0")
        .maxConnectionsPerHost(2000);

    ScenarioBuilder scn = scenario("Ramp arrival rate to find the cliff")
        // Each VU does 200 requests back-to-back on its keep-alive connection
        .repeat(200).on(
            exec(http("GET /roi-shiraz-omri-noa-arbel-app/").get("/roi-shiraz-omri-noa-arbel-app/").check(status().is(200)))
        );

    {
        setUp(
            scn.injectOpen(
                rampUsersPerSec(10).to(200).during(Duration.ofMinutes(2))
            )
        )
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile3().lt(1000)
        );
    }
}
