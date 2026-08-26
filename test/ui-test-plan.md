---
main_class: Billy
source_glob: src/main/java/*.java
data_file: data/billy.txt
---

# Billy text UI test plan

Test cases for Billy's text user interface. Each case lists its aim, the exact
lines typed into the console, and the exact console output expected in return.

Run them with the `test-ui` skill, or directly:

```bash
python3 .claude/skills/test-ui/scripts/run-ui-tests.py
```

## How the cases are run

* Every case starts a **fresh run** of the program in a **working directory of its
  own**, so no case can be affected by tasks left behind by an earlier one —
  neither by what is still in memory, nor by the file the program saved to disk.
  Task numbering always restarts at 1.
* Because the working directory is empty, there is no saved data file unless the
  case asks for one, which is also how the program behaves the first time someone
  runs it on their computer.
* A case that needs a saved list to already exist writes it in an optional
  `**Data file:**` block. The runner plants those lines in `data/billy.txt` inside
  the case's working directory before the program starts. This is how a damaged
  file can be tested without damaging one by hand.
* An input line of exactly `--- restart ---` ends the run and starts another one
  in the same working directory. The output of both runs is compared as one
  session, which is how saving in one run and loading in the next is tested.
* The input lines are fed to the program's standard input, one line at a time.
  Ending with `bye` tests the normal exit. Leaving it out is also valid and tests
  the end-of-input exit, the same path as pressing Ctrl+D at the keyboard.
* The expected output is the **whole** console session, including the startup
  banner and the farewell, so any change to those is caught too. The banner and
  greeting are written once as the `GREETING` snippet, and the sign-off as
  `FAREWELL`; a line reading `{{GREETING}}` is replaced by those lines before
  comparing.
* Trailing spaces at the end of a line are ignored when comparing, since they are
  invisible on screen. Everything else must match exactly.
* Cases run in the order written, and the session stops at the first failure.

## Conventions

* Headings must start with `TC-` followed by a number, or the runner will not
  treat the section as a test case. A `## Snippet: NAME` heading defines reusable
  output instead of a case.
* Avoid trailing spaces on input lines: they are invisible, and many editors strip
  them on save. Leading and repeated inner spaces are safe to test with.
* The settings at the top of this file name the entry point class and which
  source files to compile.

## Snippet: GREETING

Everything Billy prints on startup, before any command is typed.

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
```

## Snippet: FAREWELL

Billy's sign-off, printed when the conversation ends.

```text
Catch you later! Don't be a stranger.
____________________________________________________________
```

## TC-01 Greeting and immediate exit

**Aim:** Verify that Billy shows the banner and greeting on startup, and exits with a farewell when the user types `bye`.

**Input:**
```text
bye
```

**Expected output:**
```text
{{GREETING}}
{{FAREWELL}}
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
{{GREETING}}
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
{{FAREWELL}}
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
{{GREETING}}
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
{{FAREWELL}}
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
{{GREETING}}
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
{{FAREWELL}}
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
{{GREETING}}
____________________________________________________________
Got it. I've added this task:
  [D][ ] do homework (by: no idea :-p)
Now you have 1 task in the list.
____________________________________________________________
{{FAREWELL}}
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
{{GREETING}}
____________________________________________________________
Your list is empty. Nothing to do... suspicious.
____________________________________________________________
____________________________________________________________
You'll have to give me something to work with!
____________________________________________________________
{{FAREWELL}}
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
{{GREETING}}
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
{{FAREWELL}}
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
{{GREETING}}
____________________________________________________________
I need '/by' in that command. Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
The description can't be empty. Try: deadline return book /by Sunday
____________________________________________________________
____________________________________________________________
I need '/to' in that command. Try: event project meeting /from Mon 2pm /to 4pm
____________________________________________________________
{{FAREWELL}}
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
{{GREETING}}
____________________________________________________________
I don't know what 'blah' means. I understand: todo, deadline, event, list, mark, unmark, delete, bye.
____________________________________________________________
____________________________________________________________
I don't know what 'buy' means. I understand: todo, deadline, event, list, mark, unmark, delete, bye.
____________________________________________________________
____________________________________________________________
Your list is empty. Nothing to do... suspicious.
____________________________________________________________
{{FAREWELL}}
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
{{GREETING}}
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
{{FAREWELL}}
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
{{GREETING}}
____________________________________________________________
Your list is empty, so there's nothing to change.
____________________________________________________________
____________________________________________________________
Your list is empty, so there's nothing to change.
____________________________________________________________
{{FAREWELL}}
```

## TC-12 Deleting a task renumbers the rest

**Aim:** Verify that `delete` removes the task at the given number, confirms which task went, reports the new count, and that the tasks after it move up so numbering stays consecutive. Mirrors the worked example in the Level-6 requirements.

**Input:**
```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
todo join sports club
todo borrow book
mark 1
mark 2
mark 4
list
delete 3
list
bye
```

**Expected output:**
```text
{{GREETING}}
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
Got it. I've added this task:
  [T][ ] borrow book
Now you have 5 tasks in the list.
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] read book
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [D][X] return book (by: June 6th)
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] join sports club
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][X] read book
2.[D][X] return book (by: June 6th)
3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
4.[T][X] join sports club
5.[T][ ] borrow book
____________________________________________________________
____________________________________________________________
Noted. I've removed this task:
  [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
Now you have 4 tasks in the list.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][X] read book
2.[D][X] return book (by: June 6th)
3.[T][X] join sports club
4.[T][ ] borrow book
____________________________________________________________
{{FAREWELL}}
```

## TC-13 Deleting with a bad or missing task number

**Aim:** Verify that deleting from an empty list, deleting without a number, and deleting a number beyond the end are each reported clearly, and that deleting the only task leaves an empty list rather than a broken one.

**Input:**
```text
delete 1
todo read book
delete
delete 9
delete 1
list
bye
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Your list is empty, so there's nothing to change.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
I need a task number, like 'delete 2'.
____________________________________________________________
____________________________________________________________
There's no task 9 on your list. You have 1.
____________________________________________________________
____________________________________________________________
Noted. I've removed this task:
  [T][ ] read book
Now you have 0 tasks in the list.
____________________________________________________________
____________________________________________________________
Your list is empty. Nothing to do... suspicious.
____________________________________________________________
{{FAREWELL}}
```

## TC-14 Ending the session by running out of input

**Aim:** Verify that Billy exits cleanly with its farewell when the input ends without `bye`, which is what happens when a user presses Ctrl+D. Guards the `hasNextLine` check that stops the loop from reading past the end of input.

**Input:**
```text
todo read book
list
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] read book
____________________________________________________________
{{FAREWELL}}
```

## TC-15 Keywords ignore case and surrounding spaces

**Aim:** Verify that commands are recognised whatever their capitalisation, and that leading or repeated spaces do not stop a command being understood. The task description must keep the capitalisation the user typed, since only the keyword is lowercased.

**Input:**
```text
TODO Read Book
   LiSt
  MaRk   1
List
BYE
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Got it. I've added this task:
  [T][ ] Read Book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] Read Book
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] Read Book
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][X] Read Book
____________________________________________________________
{{FAREWELL}}
```

## TC-16 Marking a task that is already done

**Aim:** Verify that marking an already-done task is harmless and simply confirms it again, and that unmarking afterwards still returns the task to not done. Pins down that the status is set outright rather than toggled.

**Input:**
```text
todo read book
mark 1
mark 1
unmark 1
list
bye
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] read book
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] read book
____________________________________________________________
____________________________________________________________
OK, I've marked this task as not done yet:
  [T][ ] read book
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] read book
____________________________________________________________
{{FAREWELL}}
```

## TC-17 Anything after `bye` is ignored

**Aim:** Verify that the exit command is recognised from its first word like every other command, so `bye now` ends the session rather than being reported as unknown. The line after it must not run, which proves the session really ended.

**Input:**
```text
todo read book
bye now
todo this must never run
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
{{FAREWELL}}
```

## TC-18 Tasks are saved and reloaded on the next run

**Aim:** Verify the point of Level 7: the list survives Billy being closed. The first run adds one task of each type and marks one of them; the second run must show the same three tasks, in the same order, with the same done statuses — which also proves that Billy created the `data` folder and the file itself, since the working directory started empty. The `Welcome back` note tells the user why their old tasks are there.

**Input:**
```text
todo read book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
mark 2
bye
--- restart ---
list
bye
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
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
____________________________________________________________
Nice! I've marked this task as done:
  [D][X] return book (by: Sunday)
____________________________________________________________
{{FAREWELL}}
{{GREETING}}
____________________________________________________________
Welcome back! I've loaded 3 tasks from your last session.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] read book
2.[D][X] return book (by: Sunday)
3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
____________________________________________________________
{{FAREWELL}}
```

## TC-19 Deleting and unmarking are saved too

**Aim:** Verify that saving is triggered by *every* change to the list, not just by adding. A task is deleted and another unmarked in the first run; if either change were left unsaved, the second run would show the old list. Loading exactly one task also checks the singular wording of the `Welcome back` note.

**Input:**
```text
todo read book
todo return book
mark 1
mark 2
delete 2
unmark 1
bye
--- restart ---
list
bye
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] return book
Now you have 2 tasks in the list.
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] read book
____________________________________________________________
____________________________________________________________
Nice! I've marked this task as done:
  [T][X] return book
____________________________________________________________
____________________________________________________________
Noted. I've removed this task:
  [T][X] return book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
OK, I've marked this task as not done yet:
  [T][ ] read book
____________________________________________________________
{{FAREWELL}}
{{GREETING}}
____________________________________________________________
Welcome back! I've loaded 1 task from your last session.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] read book
____________________________________________________________
{{FAREWELL}}
```

## TC-20 Emptying the list is saved as an empty list

**Aim:** Verify that deleting the last task really empties the saved file rather than leaving the old contents behind. Billy should say nothing about a previous session on the next run, since there is nothing to report — the same as a first run.

**Input:**
```text
todo read book
delete 1
bye
--- restart ---
list
bye
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
____________________________________________________________
____________________________________________________________
Noted. I've removed this task:
  [T][ ] read book
Now you have 0 tasks in the list.
____________________________________________________________
{{FAREWELL}}
{{GREETING}}
____________________________________________________________
Your list is empty. Nothing to do... suspicious.
____________________________________________________________
{{FAREWELL}}
```

## TC-21 A saved list written by hand is loaded

**Aim:** Verify that Billy reads the documented file format, not merely whatever it happens to write itself. The file here is typed by hand, with the type letter, the done flag and the details of each kind of task, and every one of the three types must come back correctly.

**Data file:**
```text
T | 1 | read book
D | 0 | return book | June 6th
E | 1 | project meeting | Aug 6th 2pm | 4pm
```

**Input:**
```text
list
bye
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Welcome back! I've loaded 3 tasks from your last session.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][X] read book
2.[D][ ] return book (by: June 6th)
3.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
____________________________________________________________
{{FAREWELL}}
```

## TC-22 A corrupted file loses only the damaged lines

**Aim:** Verify the stretch goal: a file that has been edited badly must not crash Billy or cost the user the tasks that are still readable. The damaged lines here cover the ways a line can go wrong — an unknown type letter, a done flag that is not 0 or 1, a missing field, and text that is not in the format at all. Billy should load the two good tasks, say how many lines it gave up on, and carry on working normally.

**Data file:**
```text
T | 1 | read book
X | 0 | who knows what this is
D | 2 | return book | Sunday
E | 0 | project meeting | Mon 2pm
this line is not in the format at all
T | 0 | join sports club
```

**Input:**
```text
list
todo buy milk
bye
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Welcome back! I've loaded 2 tasks from your last session.
Heads up: I skipped 4 lines in data/billy.txt that I couldn't understand.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][X] read book
2.[T][ ] join sports club
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] buy milk
Now you have 3 tasks in the list.
____________________________________________________________
{{FAREWELL}}
```

## TC-23 An empty saved file is not worth mentioning

**Aim:** Verify that a file left behind by a session that ended with no tasks is treated as an empty list rather than as damage. Blank lines in it should be passed over quietly, with no warning about skipped lines.

**Data file:**
```text

```

**Input:**
```text
list
bye
```

**Expected output:**
```text
{{GREETING}}
____________________________________________________________
Your list is empty. Nothing to do... suspicious.
____________________________________________________________
{{FAREWELL}}
```
