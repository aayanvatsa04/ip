# Billy User Guide

![Billy](Ui.png)

Billy is a laid-back chatbot that keeps track of your tasks. It is deliberately
short with you: it confirms what it did in a few words, never nags, and stays
out of the way otherwise. The one time Billy speaks plainly rather than casually
is when something has gone wrong, since an error is not the place for character.

Tell it what you need to do and it remembers, including between runs: your list
is saved to `data/billy.txt` after every change and read back the next time
Billy starts.

Billy handles three kinds of task — **todos**, **deadlines** and **events** —
and can list them, search them, mark them done and delete them.

## Quick reference

| What you want | Type |
| --- | --- |
| Add a task with no date | `todo <description>` |
| Add a task with a due date | `deadline <description> /by <date>` |
| Add a task spanning a period | `event <description> /from <date> /to <date>` |
| See everything | `list` |
| See one day | `on <date>` |
| Search descriptions | `find <word>` |
| Mark done / not done | `mark <number>` / `unmark <number>` |
| Remove a task | `delete <number>` |
| Leave | `bye` |

Commands are not case sensitive, so `LIST` works as well as `list`.

## Writing dates

A date is written as `yyyy-MM-dd` or `d/M/yyyy`, so the 2nd of December 2019 is
either `2019-12-02` or `2/12/2019`. The day comes first in the slash form.

A time of day may follow, as four digits on the 24-hour clock: `2019-12-02 1800`
is six in the evening. Leaving the time out is allowed and means Billy makes no
claim about the hour, rather than assuming midnight.

Billy shows dates back to you as `Dec 2 2019`, or `Dec 2 2019, 6:00pm` when a
time was given.

## Adding a todo

A task with nothing attached to it.

Example: `todo read book`

```
Alright, added:
  [T][ ] read book
You've got 1 task now.
```

## Adding a deadline

A task that must be done by a particular date or time.

Example: `deadline return book /by 2019-12-02 1800`

```
Alright, added:
  [D][ ] return book (by: Dec 2 2019, 6:00pm)
You've got 2 tasks now.
```

## Adding an event

A task that runs from one date or time until another. An event that would end
before it starts is refused.

Example: `event project meeting /from 2019-12-02 1400 /to 2019-12-02 1600`

```
Alright, added:
  [E][ ] project meeting (from: Dec 2 2019, 2:00pm to: Dec 2 2019, 4:00pm)
You've got 3 tasks now.
```

## Listing every task

Shows the whole list, numbered the way you refer to the tasks.

Example: `list`

```
Here's what you're on the hook for:
1.[T][ ] read book
2.[D][ ] return book (by: Dec 2 2019, 6:00pm)
3.[E][ ] project meeting (from: Dec 2 2019, 2:00pm to: Dec 2 2019, 4:00pm)
```

An empty list says so rather than showing a heading with nothing under it:

```
Your list is empty. Nothing to do... suspicious.
```

## Seeing one day

Shows the tasks falling on a particular day. A deadline falls on its due date,
and an event covers every day it runs across, both ends included. Todos carry no
date and so never appear here.

Example: `on 2019-12-02`

```
Here's what you've got on Dec 2 2019:
2.[D][ ] return book (by: Dec 2 2019, 6:00pm)
3.[E][ ] project meeting (from: Dec 2 2019, 2:00pm to: Dec 2 2019, 4:00pm)
```

If nothing falls on that day:

```
Nothing on Dec 5 2019. Enjoy the day off!
```

## Searching

Finds the tasks whose description mentions a word. Capitalization is ignored,
and part of a word counts, so `find book` also finds `textbook`.

Example: `find book`

```
Here's what matched:
1.[T][ ] read book
2.[D][ ] return book (by: Dec 2 2019, 6:00pm)
```

Note that the numbers are the ones from the full list, so a task found this way
can be marked or deleted straight away without running `list` first.

If nothing matches, the word is quoted back so a typo is easy to spot:

```
Nothing here mentions 'zzz'.
```

## Marking a task done, or not done again

Example: `mark 1`

```
Nice one. That's done:
  [T][X] read book
```

Example: `unmark 1`

```
No worries, back to not done:
  [T][ ] read book
```

## Deleting a task

Example: `delete 2`

```
Done. That one's gone:
  [D][ ] return book (by: Dec 2 2019, 6:00pm)
You've got 2 tasks now.
```

## Leaving

Example: `bye`

```
Catch you later! Your list will be right here when you get back.
```

## Shorter ways to type a command

Every command answers to a shorter word as well as its full name, so a long
session need not be spelled out in full. `t read book` adds a todo, and `rm 2`
deletes the second task.

| Command | Also answers to |
| --- | --- |
| `todo` | `t` |
| `deadline` | `d`, `dl` |
| `event` | `e`, `ev` |
| `list` | `l`, `ls` |
| `on` | — |
| `find` | `f` |
| `mark` | `m` |
| `unmark` | `um` |
| `delete` | `del`, `rm` |
| `bye` | `exit`, `quit`, `q` |

These are read the same way as the full keywords, so `LS` works as well as `ls`.
`on` has none, being short already.

## If something goes wrong

Billy explains rather than crashes. A command it does not know, a task number
that names nothing, a date it cannot read, or an event ending before it starts
are all answered with what to type instead, and the conversation carries on.

If the save file is damaged, Billy loads what it can, skips the lines it cannot
understand, and tells you how many it skipped, rather than losing the rest of
your list.
