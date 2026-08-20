---
main_class: Billy
source_glob: src/main/java/*.java
---

# Billy text UI test plan

Test cases for Billy's text user interface. Each case lists its aim, the exact
lines typed into the console, and the exact console output expected in return.

Run them with the `test-ui` skill, or directly:

```bash
python3 .claude/skills/test-ui/scripts/run-ui-tests.py
```

## How the cases are run

* Every case starts a **fresh run** of the program, so no case can be affected by
  tasks left behind by an earlier one. Task numbering always restarts at 1.
* The input lines are fed to the program's standard input, one line at a time.
  Every case must end with `bye`, or the program would wait forever for input.
* The expected output is the **whole** console session, including the startup
  banner and the farewell, so any change to those is caught too.
* Trailing spaces at the end of a line are ignored when comparing, since they are
  invisible on screen. Everything else must match exactly.
* Cases run in the order written, and the session stops at the first failure.

## Conventions

* Headings must start with `TC-` followed by a number, or the runner will not
  treat the section as a test case.
* The settings at the top of this file name the entry point class and which
  source files to compile.

## TC-01 Greeting and immediate exit

**Aim:** Verify that Billy shows the banner and greeting on startup, and exits with a farewell when the user types `bye`.

**Input:**
```text
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-02 Adding one task of each type

**Aim:** Verify that `todo`, `deadline` and `event` each store a task, show the correct type and status icons, and report a running count that is singular for the first task.

**Input:**
```text
todo borrow book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] borrow book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] return book (by: Sunday)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 3 tasks in the list.
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-03 Listing a mix of task types and statuses

**Aim:** Verify that `list` numbers tasks from 1 and shows each one's type and done status, mixing all three task types. Mirrors the worked example in the Level-4 requirements.

**Input:**
```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
todo join sports club
mark 1
mark 4
todo borrow book
list
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] return book (by: June 6th)
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
Now you have 3 tasks in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] join sports club
Now you have 4 tasks in the list.
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] read book
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] join sports club
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] borrow book
Now you have 5 tasks in the list.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][X] read book
2.[D][ ] return book (by: June 6th)
3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
4.[T][X] join sports club
5.[T][ ] borrow book
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-04 Marking and unmarking keeps the task type

**Aim:** Verify that `mark` and `unmark` change only the status box, leaving the type label and the due date of a deadline intact. Guards the polymorphic formatting introduced by the Task subclasses.

**Input:**
```text
deadline pay fees /by Friday
mark 1
unmark 1
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] pay fees (by: Friday)
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [D][X] pay fees (by: Friday)
____________________________________________________________
____________________________________________________________
OK, I've marked this task as not done yet:
  [D][ ] pay fees (by: Friday)
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-05 Dates are accepted as free text

**Aim:** Verify that a due date is stored and echoed exactly as typed, including punctuation, since dates are not parsed at this stage.

**Input:**
```text
deadline do homework /by no idea :-p
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [D][ ] do homework (by: no idea :-p)
Now you have 1 task in the list.
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-06 Empty list and blank input

**Aim:** Verify that listing with nothing stored, and pressing Enter on an empty line, both give a helpful message rather than an error or an empty task.

**Input:**
```text
list

bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
Your list is empty. Nothing to do... suspicious.
____________________________________________________________
____________________________________________________________
You'll have to give me something to work with!
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-07 Invalid task numbers are rejected

**Aim:** Verify that a missing number, a non-numeric number, a number beyond the end of the list, and zero are all reported clearly instead of crashing the program.

**Input:**
```text
todo read book
mark
mark nine
mark 7
unmark 0
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
I need a task number, like 'mark 2'.
____________________________________________________________
____________________________________________________________
I need a task number, like 'mark 2'.
____________________________________________________________
____________________________________________________________
There's no task 7 on your list. You have 1.
____________________________________________________________
____________________________________________________________
There's no task 0 on your list. You have 1.
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-08 Malformed deadline and event commands

**Aim:** Verify that a deadline with no `/by`, a deadline with no description, and an event missing its `/to` each name the specific part that is missing, rather than storing a broken task.

**Input:**
```text
deadline homework
deadline /by Sunday
event meeting /from 2pm
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
I need '/by' in that command. Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
The description can't be empty. Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
I need '/to' in that command. Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-09 Unknown commands are rejected

**Aim:** Verify that a keyword Billy does not recognise is reported, and that nothing is stored as a result, so a typo cannot quietly become a task. The closing `list` confirms the list is still empty.

**Input:**
```text
blah
buy bread
list
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
I don't know what 'blah' means. I understand: todo, deadline, event, list, mark, unmark, bye.
____________________________________________________________
____________________________________________________________
I don't know what 'buy' means. I understand: todo, deadline, event, list, mark, unmark, bye.
____________________________________________________________
____________________________________________________________
Your list is empty. Nothing to do... suspicious.
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-10 Missing parts are named specifically

**Aim:** Verify that when a command has the right shape but a part of it is blank, the error names which part is missing, so the user knows what to correct.

**Input:**
```text
todo
deadline homework /by
event meeting /from /to 4pm
event meeting /from Mon /to
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
The description of a todo can't be empty. Try: todo borrow book
____________________________________________________________
____________________________________________________________
The due date can't be empty. Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
The start time can't be empty. Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
____________________________________________________________
The end time can't be empty. Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-11 Marking when the list is empty

**Aim:** Verify that marking or unmarking with nothing stored explains that the list is empty, rather than reporting a task number that could never have worked.

**Input:**
```text
mark 1
unmark 3
bye
```

**Expected output:**
```text
____________________________________________________________
 ____  _ _ _
| __ )(_) | |_   _
|  _ \| | | | | | |
| |_) | | | | |_| |
|____/|_|_|_|\__, |
             |___/
Hey there! Billy here, at your service.
I track todos, deadlines and events. Type 'list' to see them all.
____________________________________________________________
____________________________________________________________
Your list is empty, so there's nothing to change.
____________________________________________________________
____________________________________________________________
Your list is empty, so there's nothing to change.
____________________________________________________________
Catch you later! Don't be a stranger.
____________________________________________________________
```
