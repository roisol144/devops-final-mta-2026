# MeTA Greeting App — DevOps Final Project

MTA 2026 Semester B — Intro to DevOps, lecturer: Moshe Mamia.

## What this is

A small JSP web application demonstrating a full CI/CD pipeline:

```
   YOU                GITHUB              JENKINS                TOMCAT             BROWSER
 (write code) ──push──► (stores it) ──pull──► (deploys it) ──serves──► (runs JSP) ──visits──► (user)
                                                  │
                                                  ├──► SELENIUM (functional tests)
                                                  ├──► GATLING  (load / stress / max-limit)
                                                  └──► UPTIMEROBOT (availability monitor)
```

## Repo layout

| Path | Contents |
|---|---|
| `app/` | The JSP source — what Jenkins deploys to Tomcat |
| `selenium/` | Selenium IDE `.side` files for functional testing |
| `gatling-sims/` | Gatling Java simulations (max-limit / load / stress) |
| `jenkins-jobs/` | Jenkins job XML exports + setup docs |
| `docs/` | Architecture notes and defense recap cards |

## How to deploy locally

The Jenkins job does this automatically, but for manual reproduction:

```bash
cp app/index.jsp /opt/homebrew/opt/tomcat/libexec/webapps/roi-shiraz-omri-noa-arbel-app/
# then visit http://localhost:8080/roi-shiraz-omri-noa-arbel-app/
```
