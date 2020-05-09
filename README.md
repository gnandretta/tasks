# Tasks

Using text files to capture, review and act on tasks is effective because their
context is near. However, scheduling and prioritization become difficult for the
same reason—tasks are intertwined with information. This tool attempts to solve
that problem. Furthermore, text files are easy to read, change and sync in any
platform when written in a popular format such as markdown.

## Design

- [ ] Find tasks in a markdown file—output should contain only headlines and
      tasks.
- [ ] Find tasks in a directory—search in markdown files from the directory and
      recursively in its sub-directories.
- [ ] Allow search operations to filter out completed tasks.
- [ ] Attempt to integrate with an existent prompt library. Otherwise, page
      results and allow to select and open a tasks with `vim` or `$EDITOR`.
      - [inquirer](https://www.npmjs.com/package/inquirer).
      - [prompt](https://www.npmjs.com/package/prompt).
      - [enquirer](https://www.npmjs.com/package/enquirer).
- [ ] Support metadata for tracking.
      - [ ] *schedule* contains the date/time? to start working on the task.
      - [ ] *deadline* contains the latest date/time? the task should be completed.
      - [ ] *completed at* contains the date/time? the task was completed.
      - [ ] *next* (default?) indicates the task should be completed in the
            immediate future, *someday* is for the distant future, *maybe* for
            task that may or may not be started.
- [ ] Support nested tasks—as the one from above for the tracking metadata
      support. More than one level of nesting seems unlikely.
- [ ] Support metadata tasks.
- [ ] Google calendar sync. Maybe this is all it takes for mobile.
- [ ] Log actions. Most likely, it will involve updating the prompts and
      implementing commands that can be executed from the text editor. Logging
      should occur in a separate file.
- [ ] Archive tasks.
