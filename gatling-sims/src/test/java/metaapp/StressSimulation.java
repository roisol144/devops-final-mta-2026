package metaapp;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class StressSimulation extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl(System.getProperty("baseUrl", "http://151.145.91.183:8080"))
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-HK,en;q=0.9,he-IL;q=0.8,he;q=0.7,en-GB;q=0.6,en-US;q=0.5")
    .contentTypeHeader("application/x-www-form-urlencoded")
    .originHeader("http://151.145.91.183:8080")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36");

  private Map<CharSequence, String> headers_0 = Map.of("Cache-Control", "max-age=0");

  private ScenarioBuilder scn = scenario("StressSimulation")
    .exec(
      http("request_0")
        .post("/roi-shiraz-omri-noa-arbel-app/index.jsp")
        .headers(headers_0)
        .formParam("name", "roi")
    );

  {
    setUp(
      scn.injectOpen(
            rampUsersPerSec(1).to(50).during(Duration.ofSeconds(30)),
            constantUsersPerSec(50).during(Duration.ofMinutes(1)),
            rampUsersPerSec(50).to(100).during(Duration.ofSeconds(30)),
            constantUsersPerSec(100).during(Duration.ofMinutes(1)),
            rampUsersPerSec(100).to(150).during(Duration.ofSeconds(30)),
            constantUsersPerSec(150).during(Duration.ofMinutes(1)),
            rampUsersPerSec(150).to(200).during(Duration.ofSeconds(30)),
            constantUsersPerSec(200).during(Duration.ofMinutes(1)),
            rampUsersPerSec(200).to(0).during(Duration.ofSeconds(30))
      )
    ).protocols(httpProtocol);
  }
}
