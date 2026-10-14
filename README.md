# cs2114-project2-group26 — Loose Ends

**Team:** Ashna, Prateek

Loose Ends is a to-do list for people who are good at writing tasks down and
bad at deciding what to do next. You dump tasks in with a rough time estimate,
an energy level (low/high), and labels for where they can be done (`@laptop`,
`@phone`, `@errand`). When you have time, tell it your situation ("20 minutes,
at my laptop, tired") and **What now?** hands you one random task that fits.
Finished tasks go into a done pile that shows what you closed this week and
which labels you keep avoiding.

## How to compile and run

All source files are in the repository root (default package). Tests use
**JUnit 4.13**; the jars are in `lib/` (`junit-4.13.2.jar`,
`hamcrest-core-1.3.jar`).

### In Eclipse
1. File → Import → General → *Existing Projects into Workspace* (or create a
   new Java project and point it at this folder).
2. If the tests show red import errors: right-click the project → Build Path
   → Configure Build Path → Libraries → *Add JARs…* and pick both jars in
   `lib/` (or *Add Library…* → JUnit → **JUnit 4**).
3. Run the app: right-click `LooseEnds.java` → Run As → Java Application.
   Type into the Console.
4. Run the tests: right-click the project → Run As → JUnit Test.

### From the command line
```bash
# compile and run the app
javac LinkedChain.java Label.java Task.java CompletedEntry.java LooseEnds.java
java LooseEnds

# compile and run the tests (on Windows use ; instead of : in -cp)
javac -cp .:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar *.java
java -cp .:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore \
    LinkedChainTest LabelTest TaskTest CompletedEntryTest LooseEndsTest
```

### Menu
```
 1) Add a task                     8) Remove a label from a task
 2) List open tasks                9) Count open tasks with a label
 3) What now? (bag roulette)      10) Done pile and weekly summary
 4) Give me a different one       11) Clear the done pile
 5) Complete a task               12) Avoidance report        (stretch goal)
 6) Delete a task                 13) Undo last delete/complete (stretch goal)
 7) Add a label to a task          0) Quit
```
Type `cancel` at any prompt inside an action to go back to the menu.

## Files

| File | What it is |
| --- | --- |
| `LinkedChain.java` | Provided generic chain, used unchanged for every collection |
| `Label.java` | A normalized tag (`" @Laptop "` → `@laptop`) |
| `Task.java` | A to-do item; holds a `LinkedChain<Label>` with one or more labels |
| `CompletedEntry.java` | A finished task plus the `LocalDate` it was finished |
| `LooseEnds.java` | The app: owns the three bags, all logic, and the text menu |
| `LinkedChainTest.java` | Test plan §1: chain tests with `String` and `Integer` |
| `LabelTest.java`, `TaskTest.java`, `CompletedEntryTest.java` | Test plan §2: class tests |
| `LooseEndsTest.java` | Test plan §3 and §4: logic tests and scripted menu input |
| `lib/` | JUnit 4.13.2 and Hamcrest 1.3 jars for running the tests |

**Reuse of the chain:** one `LinkedChain<T>` class stores **five** element
types: `Task` (open bag), `Label` (label bag and each task's labels),
`CompletedEntry` (done pile), plus `String` and `Integer` in
`LinkedChainTest`. There are no type-specific copies. `LooseEnds` and `Task`
use the chain only through its public methods (`add`, `addToEnd`, `remove`,
`contains`, `count`, `size`, `isEmpty`, `get`, `toArray`, `clear`).

## Reflection

### What changed from the specification?
Each change says where it came from.

**Classes and method signatures**
- **`addTask` returns the new `Task` instead of `boolean`.** The menu and tests
  need the actual task to complete or label it afterwards. `null` still means
  the input was rejected. *(GenAI suggestion while implementing, reviewed by
  Prateek)*
- **`CompletedEntry` uses `java.time.LocalDate` instead of `Date`.** We only
  need the day, `Date` is mostly deprecated, and `LocalDate` makes "the last 7
  days" a simple comparison. *(GenAI suggestion, reviewed by Prateek)*
- **Added overloads `completeTask(Task, LocalDate)` and
  `weeklySummary(LocalDate)`.** The no-argument versions use today. The
  overloads let tests use fixed dates, e.g. a task finished 7 days ago must not
  count this week. *(GenAI suggestion, reviewed by Prateek)*
- **New public methods not in the spec:** `isOpen` (for the "add anyway?"
  check), `countDone`, `doneSummary` (the scope's "show the done pile" MVP
  feature, which the spec folded into `weeklySummary`), `mostRestrictiveFilter`
  (see below), `getOpenTasks`/`getDonePile` (so the menu can number tasks),
  `run(Scanner, PrintStream)` (so tests can type into the menu), and the
  stretch-goal methods `undo`, `undoDescription`, `mostAvoidedLabel`, and
  `avoidanceReport`. On `Task` we added `hasLabel`, `addLabel`, `removeLabel`,
  and `isValidEnergy`. *(From re-reading our scope while implementing)*
- **A task needs one or more labels.** Our scope's MVP said "one or more
  labels", but the spec didn't enforce it. Now the `Task` constructor rejects
  an empty label chain, and `removeLabel` won't remove a task's last label.
  Ashna's first `Task` draft already blocked removing the last label; we kept
  that rule. *(Own reflection: compared the code to the scope)*
- **Label normalization goes further.** All leading `@` signs are stripped
  (as well as trimming and lower-casing), so `" @Laptop "`, `"laptop"`, and
  `"@@laptop"` are one label. Labels can't contain spaces or be longer than 30
  characters, which covers the scope's "200-character label" case. `toString`
  returns `@laptop` (the spec said "the name"). *(GenAI suggestion, reviewed by
  Prateek)*
- **Energy matching in bag roulette.** Low energy matches only low-energy
  tasks, but high energy matches any task: if you have energy, you can still
  do an easy task. *(Group discussion — confirm with Ashna)*
- **Blank label in "What now?" means any label.** The scope asked for a label
  every time, but sometimes you can do things anywhere. *(GenAI suggestion,
  reviewed by Prateek)*
- **`hashCode`** was added to `Label`, `Task`, and `CompletedEntry` to match
  their `equals`. `Task.equals` compares label chains itself (ignoring order)
  because `LinkedChain` has no `equals`. *(GenAI suggestion)*

**Where validation lives:** as the spec planned.
- **Menu (`LooseEnds`)** re-prompts every bad field (blank description;
  `thirty`, `abt 20`, 0, -15, 100000 for minutes; bad energy; blank or
  200-character labels; out-of-range, letter, very long, or Ctrl-D menu
  input). It keeps what was already typed.
- **Constructors** throw so an invalid object can't exist.
- **`Task`** treats a repeated label as a no-op and returns `false`.
- **`completeTask`/`deleteTask`** only act if `remove` returned `true`, so
  pressing it twice reports "already gone" and redisplays the list.
- **New:** when nothing matches, the app names the most restrictive filter and
  offers to loosen it, as our scope promised. Every action also has a way back:
  type `cancel`, or press Enter at "Task number" (scope's non-functional
  requirement).

**Test cases:** every case in our test plan is covered. The §4 "manual input"
cases are automated through `run(Scanner, PrintStream)` with scripted input,
so they run with the other tests. All **112** tests pass.
- **JUnit 4 instead of JUnit 5.** Ashna added the JUnit 4 jars to `lib/` and
  wrote `LabelTest`/`TaskTest` in JUnit 4, so every test file now uses JUnit 4
  and the project runs with just the jars in the repo. *(Group decision)*
- **`Task` copies the label chain it is given** instead of keeping the
  caller's chain, so later changes to that chain can't change the task. Ashna's
  `TaskTest.testGetters` now checks the labels' contents instead of the chain
  object. *(GenAI suggestion, reviewed by Prateek)*

### What changed in the LinkedChain?
Nothing. We used the provided `LinkedChain` unchanged. Two places differ from
what our spec assumed:
- The spec said `add(null)` returns `false`. The provided chain **throws
  `IllegalArgumentException`**. We didn't change the chain. Instead our code
  never passes `null`: the constructors and the `LooseEnds` methods check
  first, so the chain is still the last line of defense.
  `LinkedChainTest.testAddNull` documents the real behavior.
- The spec had `toArray()` with no arguments. The provided version is
  `toArray(T[] values)`, so we call it as
  `openTasks.toArray(new Task[openTasks.size()])`.

### What happened to our unknowns?
From the "What you don't know" section of our scope:

| Unknown from scope | Status |
| --- | --- |
| How to write the generic chain (node class, head reference, `remove` needing the previous node) | **No longer needed.** Staff provided `LinkedChain`. We read its `remove(T)`, which keeps a `previous` reference, and tested head/middle/tail removal in `LinkedChainTest`. |
| Do we need our own `equals` on Task, Label, CompletedEntry? What goes wrong without it? | **Figured out.** Yes: `contains`, `remove`, and `count` all call `equals`. Without it they compare references, so `new Label("laptop")` would never match the label already in the bag, `countLabel` would always be 0, and `removeLabel` would fail. `LabelTest.testEquals` and `LooseEndsTest.testCountLabel` check this. |
| How to walk the chain from outside the class (is `toArray` right?) | **Figured out.** Yes. We call `toArray` and loop over the array; the app never touches nodes. |
| How to make a generic array for `toArray` without the unchecked-cast warning | **Figured out.** The provided `toArray(T[] values)` has the caller pass in a correctly typed array (`new Task[size]`), so no generic array is created and there's no warning. |
| How to keep the bags consistent when one action touches several | **Figured out.** `completeTask` removes from the open bag first and only removes labels and adds the `CompletedEntry` if that worked, so a failed remove changes nothing. Tested in `testCompleteTwiceReportsGone`. |
| How to write chain tests that cover edge cases, not just the happy path | **Figured out.** `LinkedChainTest` covers empty, one element, remove head/middle/tail, remove absent, duplicates, null, and 1,000 elements. |

**Still open:** how to save to a text file and read it back safely (needed for
the "saving between runs" stretch goal).

### What doesn't work yet?
- Every MVP feature from our scope works, and every bad-input case from our
  scope is handled without crashing. All 112 tests pass.
- **Data is not saved** between runs; quitting loses everything.
- The weekly summary covers the last 7 days (rolling), not a calendar week.
- `mostRestrictiveFilter` checks each filter on its own. If only a
  *combination* rules everything out, it names the filter that rules out the
  most tasks, and loosening it might still find nothing (the app then says
  "Still nothing fits").
- `completeTask(Task)` uses `LocalDate.now()`, so a test run exactly at
  midnight could see a different date.

## Next Steps

### Big-O of the app methods vs. our spec
n = open tasks, m = labels in the label bag, d = done-pile entries,
k = distinct labels, L = labels on one task (small, usually 1–3).

| Method | Spec estimate | Actual | Why |
| --- | --- | --- | --- |
| `addTask` | O(n) | O(L²) | Copies labels with a duplicate check; `add` is O(1) at the front. Doesn't depend on n, so better than the spec. |
| `addLabel` | O(1) | O(n + L) | We now check the task is still open (`contains`, O(n)). Worse than the spec, but safer. |
| `removeLabel` | O(1) | O(n + L + m) | Same open check, plus `labelBag.remove` searches the bag. |
| `deleteTask` | O(n²) | O(n + L·m) | One `remove` from the open bag, then one label-bag remove per label. |
| `completeTask` | O(n²) | O(n + L·m) | Same as delete; adding the entry is O(1). |
| `whatNow` / `another` | O(n²) | O(n·L) | One pass with `toArray`, a label check per task, then `get(i)` on the matches. |
| `countOpen` | O(1) | O(1) | `size()`. Matches. |
| `countLabel` | O(n) | O(m) | One `count` on the label bag. Matches. |
| `weeklySummary` / `doneSummary` | O(n²) | O(d·L·k) | Collect the labels, then count each distinct one. Matches the spec's quadratic estimate. |
| `clearDone` | O(n) | O(1) | `clear()` just drops the head. Better than the spec. |
| `undo` (stretch) | — | O(d + L) | One done-pile remove plus re-adding labels. |
| `mostAvoidedLabel` (stretch) | — | O(m·k + k·d·L) | Distinct labels, then open and done counts for each. |

### Stretch goals from our scope

| Stretch goal | Decision |
| --- | --- |
| Avoidance report | **Done** in this prototype (menu 12, `mostAvoidedLabel`) |
| Undo the last action | **Done** in this prototype (menu 13, `undo`) |
| Recurring loose ends | **Committed** for the final |
| Saving between runs | **Committed** for the final |
| Staleness detector (14 days) | **Dropped**: needs a creation date on every task and a clock we can control in tests. Lower value than saving. |
| Label suggestions | **Dropped**: matching description words to labels is mostly string work and doesn't use the bags in an interesting way. |

### Remaining work, in order

| # | Item | Owner |
| --- | --- | --- |
| 1 | Review the prototype together; agree on energy matching and "blank = any label" | Ashna + Prateek |
| 2 | Recurring loose ends: a `recurring` flag on `Task`; completing it files a `CompletedEntry` and adds a fresh copy to the open bag | Ashna |
| 3 | Saving between runs: write all three bags to a text file on quit and rebuild them on start | Prateek |
| 4 | Improve `mostRestrictiveFilter` to try loosening each filter and pick the one that actually finds a task | Prateek |
| 5 | Update README Big-O table and reflection for the final | Ashna |
| 6 | Each person reviews the other's new tests before the final | Ashna + Prateek |

### New tests we will need
- **Recurring:** completing a recurring task leaves one copy open and adds one
  entry; completing a non-recurring task doesn't; undo after completing a
  recurring task doesn't leave two open copies.
- **Saving:** save then load gives equal bags; a missing file starts empty; an
  empty file starts empty; a corrupted line (bad minutes, blank description, no
  labels) is skipped and reported, not stored as `null` (the scope's
  "half-parsed save line" case); labels with mixed case load as one label.
- **Menu:** scripted-input tests for any new menu option, including `cancel`
  and Ctrl-D.

## Attribution

| File | Team member | Contribution |
| --- | --- | --- |
| `LinkedChain.java` | CS 2114 staff | Provided; used unchanged |
| `lib/*.jar` | Ashna | Added JUnit 4 and Hamcrest so the tests run from the repo |
| `Label.java` | Ashna, Prateek | Ashna wrote the constructor, methods, and class comment; Prateek revised normalization (`@`, length, spaces) and added `hashCode` (GenAI-assisted) |
| `Task.java` | Ashna, Prateek | Ashna wrote the first version, the last-label rule, and the "at least one label" check, and fixed the energy check and typos; Prateek made the same fixes in parallel and added label copying, `hasLabel`, `equals`/`hashCode`, and `toString` (GenAI-assisted); merged together |
| `CompletedEntry.java` | Prateek | Wrote (GenAI-assisted) |
| `LooseEnds.java` | Prateek | Wrote app logic, menu, and stretch goals (GenAI-assisted), following Ashna's class design and method signatures from Deliverable 2 |
| `LinkedChainTest.java` | Prateek | Wrote from our test plan §1 (GenAI-assisted) |
| `LabelTest.java` | Ashna, Prateek | Ashna wrote the constructor, null/empty, `toString`, and `equals` tests; Prateek added normalization, length, and space tests (GenAI-assisted) |
| `TaskTest.java` | Ashna, Prateek | Ashna wrote the getters, null-description, and no-labels tests; Prateek added the rest and adjusted `testGetters` for label copying (GenAI-assisted) |
| `CompletedEntryTest.java` | Prateek | Wrote (GenAI-assisted) |
| `LooseEndsTest.java` | Prateek | Wrote from our test plan §3–§4 (GenAI-assisted) |

**GenAI note:** We used Claude (Anthropic) to help build this prototype. We gave
it our scope (Deliverable 1), our specification (Deliverable 2), and our draft
`Label`, `Task`, and `LabelTest`. It found bugs in the drafts (the energy check
that rejected everything, typos, missing semicolons, the test field declared
inside `setUp`). It drafted `CompletedEntry`, `LooseEnds`, the stretch goals,
and the JUnit tests, and suggested the changes marked "GenAI" above. We read
every class and test, ran the program and the tests, and checked each one
against our spec and scope before keeping it.
