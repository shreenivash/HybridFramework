# One API Suite, Two Rails: Functional Tests That Feed Load

The release pipeline is green. The API suite passed in parallel. An hour later, production is slow under traffic nobody rehearsed. The team did not miss a test. They missed a second question: did we ever stress the same journey at a fixed rate?

That question is old. What changed is the cost of keeping two separate worlds—functional API tests in one repo, load scripts in another—when both describe the same HTTP surface.

## The disguise

It looks like discipline to maintain a dedicated load team and a dedicated automation team. Each group has its own tools, its own heroes, its own sprint leftovers. Functional tests prove correctness. Load tests prove capacity. On paper, that split is clean.

In practice, the load script is a photocopy of the API suite that started true in January and lies by March. Endpoints move. Auth headers change. A query parameter becomes required. The functional suite catches the drift. The load suite still hits 200 on a mock until someone notices the graph flatlined.

The disguise is progress without a single owner for the HTTP contract.

## The old habit at human speed

When releases were weekly and APIs were small, you could afford duplicate effort. A engineer wrote TestNG methods with REST Assured. Another exported Postman or wrote JMeter by hand. The duplication hurt, but the blast radius was contained.

The functional suite answered: does this change break a contract? The load suite answered: can we survive Black Friday? Different questions. Different artifacts. Survivable.

## Why it breaks now

APIs are graphs, not lists. Login returns a token. The token gates the next call. Create returns an id. Update uses that id. Functional tests encode that graph with assertions. Load tests need the same graph at volume, or they measure noise.

Three failure shapes show up repeatedly.

**Shape one: parallel functional tests are not a journey.** Running every `@Test` method in parallel finishes the suite in twenty minutes. That is a win for the release pipeline. It is not a user session. Exporting those methods as independent samplers fires unrelated endpoints at once. Throughput rises. Realism drops.

**Shape two: record-only load drifts with test order.** Recording HTTP during a functional run is seductive. Flip a flag, run the suite, emit YAML. You get truth for what ran. If tests are isolated per method, you get a shuffle of calls, not a checkout flow. Recording is excellent raw material. It is not automatically an end-to-end journey.

**Shape three: hand-written YAML journeys do not track the suite.** A curated journey file is clear and reviewable. It is also another file to maintain. When the API team ships without updating YAML, load tests greenwash the past.

Each shape fails for the same reason: two representations of one system without a bridge.

## Two rails on one bridge

The Hybrid Framework treats API automation as one asset with two execution modes.

**Rail one — functional:** TestNG, REST Assured, parallel methods, Allure evidence, twenty-minute SLA. This is the release gate.

**Rail two — load:** The same HTTP steps compiled to JMeter `.jmx` with a Constant Throughput Timer and JEXL so `-Jtps=50` means fifty transactions per second across the cluster, not fifty times however many threads you added. Extra users wait. They do not inflate throughput.

Between the rails sits the journey: ordered steps, JSON extractors, variables. You can author it three ways.

### YAML journeys

You write `journeys/checkout.yaml` with explicit steps. Export to JMX. Pros: readable, reviewable, perfect for the one flow that must survive CFO review. Cons: another source of truth unless tied to code review rituals.

### `@ApiLoadStep` on existing tests

You keep functional tests. You add annotations with method, path, body, and extractors. A CLI scans `BaseApiTest` subclasses and builds one JMX. Pros: lowest friction for teams already invested in Java API tests; skipped methods are explicit. Cons: annotations can diverge from method bodies if someone edits code and forgets the annotation; ordering is class order, not runtime order.

### Record during functional runs

Enable `-Dapi.load.record=true`, run the suite, emit YAML, then `export-suite --recorded`. Pros: no duplicate authoring; captures real URLs and bodies. Cons: per-method tests produce per-method traffic; you still curate for true E2E; sensitive data in bodies needs redaction discipline.

None of these abolish judgment. They reduce the tax of maintaining a second suite from scratch.

## Pros and cons in one table

| Approach | Effort | Fidelity to prod journeys | Stays in sync with API tests | Load realism |
|----------|--------|---------------------------|------------------------------|--------------|
| Separate JMeter scripts | High | High if maintained | Low | High |
| YAML journey only | Medium | High | Medium | High |
| `@ApiLoadStep` export | Low | Medium (order is static) | High if annotations enforced in review | High with fixed TPS |
| Record + refine YAML | Low initial | Medium until curated | High on each record run | Medium until curated |
| Functional parallel only (no export) | None | Low | N/A | Not a load test |

## Who owns what

Mechanism belongs to the framework: export CLI, JMX generation, throughput timer math, warnings when thread count cannot sustain TPS.

Outcome belongs to engineering leadership. If load is a optional skunkworks file, duplication returns. If pull requests that touch API tests must update `@ApiLoadStep` or the journey YAML, the bridge holds. Put it in the checklist next to schema validation.

## What to do Monday

Pick one critical journey—not the whole suite. Annotate or write YAML. Run `explain` before `generate` so the team sees TPS, users, and samples per minute in plain language. Run functional tests with recording once and diff the YAML against your mental model of the flow. Schedule load from the exported JMX in CI monthly, not yearly.

Evidence you can defend beats another slide that says we have JMeter somewhere.

## Bottom line

You do not need two unrelated suites to answer correctness and capacity. You need one HTTP story, two runners, and a fixed throughput contract that does not lie when managers add virtual users.

*Who on your team owns the journey file when the API changes—and what happens to load the week they forget?*
