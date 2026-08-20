# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: decent, not the best
* IDE and level of expertise: amateur

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Testing after code changes

After every change to the code, before reporting the work as done:

1. Update `test/ui-test-plan.md` if the change affects the text UI. Add cases for
   new commands or behaviour, and update the expected output of existing cases
   whose output the change deliberately alters. Skip this step only when the
   change cannot affect what the user sees on the console, and say so.
2. Invoke the `test-ui` skill to run the test cases.
3. Show the user the test session transcript, and report how many cases passed.
   Tool output is not reliably visible to the user, so save the transcript to a
   file and send it rather than assuming they can see the console output.

Never report a change as working without having run the tests. If a case fails,
fix the cause rather than editing the expected output to match — unless the new
output is genuinely intended, in which case update the plan and say which side
was changed and why.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.
