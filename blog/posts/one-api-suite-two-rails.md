# One API Suite, Two Rails: Functional Tests That Feed Load

The release pipeline is green. The API suite passed in parallel. An hour later, production is slow under traffic nobody rehearsed. The team did not miss a test. They missed a second question: did we ever stress the *same journey* at a fixed rate?

That question is old. What changed is the cost of keeping two separate worlds—functional API tests in one repo, load scripts in another—when both describe the same HTTP surface.

## The disguise

It looks like discipline to maintain a dedicated load team and a dedicated automation team. Each group has its own tools, its own heroes, its own sprint leftovers. Functional tests prove correctness. Load tests prove capacity. On paper, that split is clean.

In practice, the load script is a photocopy of the API suite that started true in January and lies by March. Endpoints move. Auth headers change. A query parameter becomes required. The functional suite catches the drift. The load suite still hits 200 on a mock until someone notices the graph flatlined.

The disguise is progress without a single owner for the HTTP contract.

## The old habit at human speed

When releases were weekly and APIs were small, you could afford duplicate effort. An engineer wrote TestNG methods with REST Assured. Another exported Postman or wrote JMeter by hand. The duplication hurt, but the blast radius was contained.

The functional suite answered: does this change break a contract? The load suite answered: can we survive peak traffic? Different questions. Different artifacts. Survivable.

## Why it breaks now

APIs are graphs, not lists. Login returns a token. The token gates the next call. Create returns an id. Update uses that id. Functional tests encode that graph with assertions. Load tests need the same graph at volume, or they measure noise.

Three failure shapes show up repeatedly.

**Parallel functional tests are not a journey.** Running every `@Test` method in parallel finishes the suite quickly. That is a win for the release pipeline. It is not a user session. Exporting those methods as independent samplers fires unrelated endpoints at once. Throughput rises. Realism drops.

**Record-only load drifts with test order.** Recording HTTP during a functional run is seductive. Flip a flag, run the suite, emit YAML. You get truth for what ran. If tests are isolated per method, you get a shuffle of calls, not a checkout flow. Recording is excellent raw material. It is not automatically an end-to-end journey.

**Hand-written YAML journeys do not track the suite.** A curated journey file is clear and reviewable. It is also another file to maintain. When the API team ships without updating YAML, load tests greenwash the past.

Each shape fails for the same reason: two representations of one system without a bridge.

## Two rails on one bridge

The Hybrid Framework treats API automation as one asset with two execution modes—not one test type pretending to be both.

**Rail one — functional:** TestNG, REST Assured, parallel methods, Allure evidence, twenty-minute SLA. This is the release gate.

**Rail two — load:** The same HTTP steps compiled to JMeter `.jmx` with a Constant Throughput Timer and JEXL so `-Jtps=50` means fifty transactions per second, not fifty times thread count. Extra users wait. They do not inflate throughput.

Between the rails sits the **journey**: ordered steps, JSON extractors, variables. You can author it three ways: YAML journeys, `@ApiLoadStep` on existing tests (`export-suite` CLI), or record during a functional run then refine YAML.

## An architect’s read: is this a good approach?

**Yes—with boundaries.** Treating a shared *HTTP journey* as the integration contract, with two adapters (TestNG for correctness, JMeter for capacity), is sound systems thinking. One canonical description, multiple runners. You reduce duplicate authoring without pretending functional tests *are* load tests.

**Where it shines.** API-heavy products with Java automation already in place. Teams that lose weeks syncing Postman, JMeter, and REST Assured after every auth or routing change. Organizations that need a *declared* TPS target for regression.

**Where it is not enough.** Full performance engineering still needs think time, realistic data volume, cache warm-up, soak and spike profiles, and environment parity. Export compiles static steps—it does not execute dynamic request logic hidden only in test code. Non-HTTP surfaces stay outside this bridge unless you extend the model.

**The real risk is coupling without governance.** If annotations rot, you have two lies instead of one. The approach is good when the journey is owned in review and load scope is intentional: one critical path, fixed throughput, evidence on a schedule.

**Verdict:** Prefer this over orphan JMeter scripts for sync and cost. Do not replace a performance program with export alone.

## Pros and cons

| Approach | Effort | Stays in sync | Journey realism |
|----------|--------|---------------|-----------------|
| Separate JMeter scripts | High | Low | High if maintained |
| YAML journey only | Medium | Medium | High |
| `@ApiLoadStep` export | Low | High if PRs enforce annotations | Medium (static order) |
| Record + refine YAML | Low initial | High per record run | Medium until curated |
| Parallel functional only | None | N/A | Not a load test |

## Who owns what

Mechanism belongs to the framework: export CLI, JMX generation, throughput timer math, warnings when thread count cannot sustain TPS.

Outcome belongs to engineering leadership. If pull requests that touch API tests must update `@ApiLoadStep` or journey YAML, the bridge holds.

## What to do Monday

1. Pick **one** critical journey—not the whole parallel suite.
2. Annotate or write YAML; run `explain` before `generate`.
3. Record once and diff YAML against your mental model of the flow.
4. Schedule load from exported JMX in CI monthly, not yearly.

## Bottom line

You do not need two unrelated suites to answer correctness and capacity. You need one HTTP story, two runners, and a fixed throughput contract that does not lie when managers add virtual users.

*Who on your team owns the journey when the API changes—and what happens to load the week they forget?*

**Try it:** https://github.com/shreenivash/HybridFramework
