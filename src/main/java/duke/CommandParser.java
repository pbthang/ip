package duke;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import duke.command.AddCommand;
import duke.command.Command;
import duke.command.DeleteCommand;
import duke.command.ExitCommand;
import duke.command.FindCommand;
import duke.command.HelpCommand;
import duke.command.ListCommand;
import duke.command.MarkDoneCommand;
import duke.storage.TaskList;
import duke.task.DeadlineTask;
import duke.task.EventTask;
import duke.task.TodoTask;

/**
 * Include utilities for parsing a string input into a command
 */
public class CommandParser {
    public static final String EXIT_PREFIX = "bye";
    public static final String LIST_PREFIX = "list";
    public static final String TODO_PREFIX = "todo";
    public static final String DEADLINE_PREFIX = "deadline";
    public static final String EVENT_PREFIX = "event";
    public static final String DONE_PREFIX = "done";
    public static final String DELETE_PREFIX = "delete";
    public static final String FIND_PREFIX = "find";
    public static final String HELP_PREFIX = "help";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /**
     * Checks if a string input represents an exit command
     *
     * @param commandString string input
     * @return true if <code>commandString</code> represents an exit command, else false
     */
    public static boolean isExit(String commandString) {
        return commandString.startsWith(EXIT_PREFIX);
    }

    /**
     * Parses a string input into a corresponding command
     *
     * @param commandString string input
     * @return the corresponding command
     * @throws DukeException if the command cannot be identified
     */
    public static Command parse(String commandString) throws DukeException {
        if (commandString.startsWith(LIST_PREFIX)) {
            return parseListCommand();
        } else if (commandString.startsWith(DONE_PREFIX)) {
            return parseMarkDoneCommand(commandString);
        } else if (commandString.startsWith(TODO_PREFIX)) {
            return parseTodoCommand(commandString);
        } else if (commandString.startsWith(DEADLINE_PREFIX)) {
            return parseDeadlineCommand(commandString);
        } else if (commandString.startsWith(EVENT_PREFIX)) {
            return parseEventCommand(commandString);
        } else if (commandString.startsWith(DELETE_PREFIX)) {
            return parseDeleteCommand(commandString);
        } else if (commandString.startsWith(FIND_PREFIX)) {
            return parseFindCommand(commandString);
        } else if (commandString.startsWith(EXIT_PREFIX)) {
            return new ExitCommand();
        } else if (commandString.startsWith(HELP_PREFIX)) {
            return parseHelpCommand(commandString);
        } else {
            throw new DukeException("Sorry, I don't understand that command...");
        }
    }

    private static Command parseListCommand() {
        return new ListCommand();
    }

    private static Command parseMarkDoneCommand(String commandString) throws DukeException {
        assert commandString.startsWith(DONE_PREFIX);
        String payload = commandString.substring(DONE_PREFIX.length()).trim();
        if (payload.length() <= 0) {
            throw new DukeException("Please indicate a task number to mark as done!");
        } else {
            try {
                int taskIndex = Integer.parseInt(payload) - 1;
                return new MarkDoneCommand(taskIndex);
            } catch (NumberFormatException e) {
                throw new DukeException("Please indicate a valid task number to mark as done!");
            } catch (IndexOutOfBoundsException e) {
                throw new DukeException("404 Task not found!");
            }
        }
    }

    private static Command parseTodoCommand(String commandString) throws DukeException {
        assert commandString.startsWith(TODO_PREFIX);
        String payload = commandString.substring(TODO_PREFIX.length()).trim();
        if (payload.length() <= 0) {
            throw new DukeException("Todo description cannot be empty!");
        }
        return new AddCommand(new TodoTask(payload, false));
    }

    private static Command parseDeadlineCommand(String commandString) throws DukeException {
        assert commandString.startsWith(DEADLINE_PREFIX);
        String payload = commandString.substring(DEADLINE_PREFIX.length()).trim();
        final String separatorString = "/by";
        int separatorIndex = payload.lastIndexOf(separatorString);
        if (separatorIndex < 0) {
            throw new DukeException(
                    "Please indicate in this format: deadline [description] "
                            + separatorString
                            + " [due date].");
        }
        String deadlineContent = payload.substring(0, separatorIndex).trim();
        String deadlineString = payload.substring(separatorIndex + separatorString.length()).trim();
        if (deadlineContent.length() <= 0) {
            throw new DukeException("Please indicate the deadline description!");
        } else if (deadlineString.length() <= 0) {
            throw new DukeException("Please indicate the due date!");
        }
        try {
            LocalDateTime deadline = LocalDateTime.parse(deadlineString, DATE_TIME_FORMATTER);
            return new AddCommand(new DeadlineTask(deadlineContent, false, deadline));
        } catch (DateTimeParseException e) {
            throw new DukeException("Please provide date time in the format yyyy-MM-dd HHmm, e.g. 2021-08-04 2359");
        }
    }

    private static Command parseEventCommand(String commandString) throws DukeException {
        assert commandString.startsWith(EVENT_PREFIX);
        String payload = commandString.substring(EVENT_PREFIX.length()).trim();
        final String separatorString = "/at";
        int separatorIndex = payload.lastIndexOf(separatorString);
        if (separatorIndex < 0) {
            throw new DukeException(
                    "Please indicate in this format: event [description] "
                            + separatorString
                            + " [due date].");
        }
        String eventContent = payload.substring(0, separatorIndex).trim();
        String eventDateString = payload.substring(separatorIndex + separatorString.length()).trim();
        if (eventContent.length() <= 0) {
            throw new DukeException("Please indicate the event description!");
        } else if (eventDateString.length() <= 0) {
            throw new DukeException("Please indicate the event date!");
        }
        try {
            LocalDateTime eventDate = LocalDateTime.parse(eventDateString, DATE_TIME_FORMATTER);
            return new AddCommand(new EventTask(eventContent, false, eventDate));
        } catch (DateTimeParseException e) {
            throw new DukeException("Please provide date time in the format yyyy-MM-dd HHmm, e.g. 2021-08-04 2359");
        }
    }

    private static Command parseDeleteCommand(String commandString) throws DukeException {
        assert commandString.startsWith(DELETE_PREFIX);
        String payload = commandString.substring(DELETE_PREFIX.length()).trim();
        if (payload.length() <= 0) {
            throw new DukeException("Please indicate a task number to delete!");
        } else {
            try {
                int taskIndex = Integer.parseInt(payload) - 1;
                if (taskIndex >= 0 && taskIndex < TaskList.getInstance().getSize()) {
                    return new DeleteCommand(taskIndex);
                } else {
                    throw new DukeException("404 Task not found");
                }
            } catch (NumberFormatException e) {
                throw new DukeException("Please indicate a valid task number to delete!");
            }
        }
    }

    private static Command parseFindCommand(String commandString) throws DukeException {
        assert commandString.startsWith(FIND_PREFIX);
        String payload = commandString.substring(FIND_PREFIX.length()).trim();
        if (payload.length() <= 0) {
            throw new DukeException("Please indicate a keyword to find tasks!");
        } else {
            return new FindCommand(payload);
        }
    }

    private static Command parseHelpCommand(String commandString) {
        assert commandString.startsWith(HELP_PREFIX);
        String payload = commandString.substring(HELP_PREFIX.length()).trim();
        return new HelpCommand(payload);
    }
}
