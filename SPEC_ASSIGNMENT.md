# Project 1: Specification - Assignment Instructions

Transcribed from the course page. This file is the reference for the specification
deliverable; `ASSIGNMENT.md` covers the earlier scope document, and `DESIGN.md` holds our
scope. Compare the spec against this file before submitting.

**Deliverable:** a 3-5 page document answering each section below, with the system
diagram embedded or attached alongside.

**Length per section:** no fixed requirement. Aim for completeness for our project and
internal consistency across sections.

---

## Purpose

> This is the heart of the project, so most of your design effort lives here. A
> specification turns your scope into a blueprint detailed enough that a teammate, or a
> coding assistant, could build from it without guessing about your intentions, goals,
> and requirements.

---

## The eight sections

### 1. Class design

> What classes will you write, and what single job does each own? No one class doing
> everything.

### 2. System diagram (required)

> A single picture showing the main pieces of your system and how they connect: which
> parts talk to which, and what moves between them. It does not have to be UML or any
> formal notation — boxes and arrows are fine, and a photo of a legible whiteboard or
> hand sketch counts. The test is whether a reader can see the shape of your system at a
> glance without reading the rest of the spec. Label each box with the class it stands
> for.

### 3. Data & state

> For each class, what data does it hold (fields and their types), and which collection
> or structure stores the core data — and why that one?

### 4. Method signatures

> For each class, the public methods: name, parameters and types, return type, and a
> one-line behavior. This is the contract a teammate or GenAI builds against.

### 5. Where validation lives

> Take each bad-input case from your scope: which class/method catches it, and what
> happens (re-prompt, exception, default)? Every named bad input should map to a defined
> response.

### 6. Test plan

> For each public method, one normal case and one bad-input case: input and expected
> result, in plain English.

### 7. Division of work

> Who in your project owns which classes or pieces, and how will you integrate them into
> one working program?

### 8. Revised scope

> What changed since your scope doc, and why — and where did each change originate (group
> discussion, GenAI, your own reflection)?

---

## The Meets bar

> Every class has one clear responsibility; a system diagram shows the parts and how they
> connect; each class's data and state are defined; public methods are specified by
> signature and behavior; every named bad-input case maps to a response; the test plan
> covers normal and bad input; and the changes from your scope are explained.

---

## GenAI probing tip from the course page

> Use GenAI to probe your specification, asking questions like:
>
> - Which details might you be missing?
> - Are there pieces of your specification that contradict each other?
> - Are there additional data sources?
>
> Use these and other probing questions to validate and polish your specification!

---

## Suggested formats per section

These are our choices for laying the sections out, not course requirements.

| Section | Format |
| --- | --- |
| Class design | Table: class name, one-sentence responsibility. |
| System diagram | Image file in the repo, embedded with `![](path)`; every box named after a class from section 1. |
| Data & state | One table per class: field, type, purpose; one sentence justifying the core collection. |
| Method signatures | One table per class: signature, return type, one-line behavior. |
| Where validation lives | Table: scope case number, input, catching class and method, response. |
| Test plan | Table per class: method, normal input and expected result, bad input and expected result. |
| Division of work | Table: person, classes owned; a paragraph on integration order and how merges happen. |
| Revised scope | Table: change, reason, origin (group discussion, GenAI, own reflection). |

---

## Checklist against our scope in DESIGN.md

### Class design and diagram
- [ ] Every class has a single responsibility stated in one sentence.
- [ ] No class owns input parsing, game rules and output together.
- [ ] Diagram boxes match the class list exactly, same names, no extras, none missing.
- [ ] Diagram arrows say what moves between classes (a command string, a `Port`, damage
      numbers, gold).

### Data, state and methods
- [ ] Every field has a type.
- [ ] The port map's storage is chosen and justified (scope section 5 left this open:
      port objects holding neighbour references was one idea).
- [ ] Game modes (at sea, in port, in encounter) have a defined representation, and the
      spec says which method rejects a command issued in the wrong mode.
- [ ] One seeded `Random` is created in one place and passed to every class that rolls
      dice (scope non-functional requirement 2).
- [ ] Every command from scope functional requirements 1-14 (`look`, `status`, `sail`,
      `repair`, `hire`, `buy`, `fight`, `flee`, `retire`, `help`, `quit`) maps to at
      least one public method.
- [ ] Every public method has name, parameter types, return type and one-line behavior.
- [ ] Placeholder numbers from scope (starting values, prices, gold target, map size) are
      picked or stored as named constants in a stated class.

### Validation
- [ ] Every bad-input case 1-27 from DESIGN.md section 4 has a row naming the class,
      the method and the response.
- [ ] Overflow handling (case 12, dividing gold by unit price before multiplying) names
      the `Port` method it lives in.
- [ ] `hasNextLine` handling (case 24) names the class that owns the `Scanner`.

### Tests
- [ ] Every public method from section 4 has one normal and one bad-input test.
- [ ] Tests that depend on randomness state the seed or the injected `Random` they use,
      so a forced fight is reachable in a JUnit test.

### Work and scope
- [ ] Each class has one owner, Marcos or Aidan.
- [ ] Integration plan says who merges, in what order classes get connected, and when
      (current meeting time: Tuesdays at 7pm).
- [ ] Every change from DESIGN.md lists its reason and its origin.
- [ ] Stretch goals are either kept out of the spec or marked as not built in the MVP.

### Consistency pass
- [ ] Every method named in the validation table appears in the method signatures.
- [ ] Every method named in the test plan appears in the method signatures.
- [ ] Every field a method reads or changes appears in that class's data & state table.
- [ ] Ran the GenAI probing questions above and fixed any contradictions found.
- [ ] Total length is 3-5 pages.
