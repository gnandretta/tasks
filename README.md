# Tasks

Using text files to capture, review and act on tasks is effective because their
context is near. However, scheduling and prioritization become difficult for the
same reason—tasks are intertwined with information. This tool attempts to solve
that problem. Furthermore, text files are easy to read, change and sync in any
platform when written in a popular format such as markdown.

## Design

- [x] Find tasks in a markdown file—output should contain only headlines and
      tasks.
- [x] Find tasks in a directory—search in markdown files from the directory and
      recursively in its sub-directories.
- [ ] Filter completed tasks.
  - [x] Include completion data in the parsed tree.
  - [ ] Show only incomplete tasks.
  - [ ] Show only complete tasks.
- [ ] Attempt to integrate with an existent prompt library. Otherwise, page
      results and try to find a way to make it easy to edit tasks.
  - [x] Try [inquirer](https://www.npmjs.com/package/inquirer),
        [prompt](https://www.npmjs.com/package/prompt), and
        [enquirer](https://www.npmjs.com/package/enquirer).
    ~~Settled with enquirer, liked more the docs than inquirer's and prompt
    doesn't seem as powerful as those two.~~ Dropped enquirer because had to
    workaround a bug with its API (see cadfaf1), it shows a node warning when
    exiting with <C-c>, not sure what the story is when the items don't fit in
    the screen, if you set `choice.name` it will be used as the response instead
    of `choice.value`, the only way I found to show something different than the
    choice text when you select it is to override a method called
    [format](https://github.com/enquirer/enquirer/blob/65f0726b5317e6c0177f8157eb9efd3d134ed405/lib/prompts/select.js#L98-L104)
    that returns a styled string when the selection was completed—which makes me
    thing that customizing will be hard because methods have several
    responsibilities. Inquirer seems to suffer from the same customization
    issues, I couldn't find a way to change the symbols used for the prompt and
    selection, and it doesn't look like there's ever gonna be a way to
    [don't have infinite lists](https://github.com/SBoudrias/Inquirer.js/issues/206#issuecomment-199005812).
    For now let's have a command that just outputs text, put it to use and then
    implement something more interactive (using readline) only if it's worth it.
  - [ ] Format output.
  - [ ] Open with `vim` or `$EDITOR` positioning the cursor. Use text for
        heading and an optional number for task—maybe both should accept text or
        number.
- [ ] Build script.
- [ ] Improve prompts—do more than just show all tasks from all files.
- [ ] Support metadata for tracking.
  - [ ] *schedule* contains the date/time? to start working on the task.
  - [ ] *deadline* contains the latest date/time? the task should be completed.
  - [ ] *completed at* contains the date/time? the task was completed.
  - [ ] *next* (default?) indicates the task should be completed in the
        immediate future, *someday* is for the distant future, *maybe* for task
        that may or may not be started.
- [ ] Support nested tasks—as the one from above for the tracking metadata
      support. More than one level of nesting seems unlikely.
- [ ] Support partially completed tasks—`[-]`.
- [ ] Google calendar sync. Maybe this is all it takes for mobile.
- [ ] Log actions. Most likely, it will involve updating the prompts and
      implementing commands that can be executed from the text editor. Logging
      should occur in a separate file.
- [ ] Archive tasks.
- [ ] Interactive prompt?
