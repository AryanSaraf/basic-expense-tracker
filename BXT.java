import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BXT {

	// ANSI escape sequences to add some colour
	public static final String GREEN_INTENSE = "\033[0;92m";
	public static final String ANSI_RED = "\033[0;31m";
	public static final String ANSI_RESET = "\033[0m";

	// the file where the data is saved
	public static final String OUTPUT_FILE = "bxt-dataset.csv";

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		File dataset = new File(OUTPUT_FILE);
		if (!dataset.exists()) {
			dataset.createNewFile();
		}
		// category is defined here since it must have a default value but still must change in every iteration
		String category = "none";
		System.out.println(GREEN_INTENSE +                       "\n" +
		                   "            ██████╗ ██╗  ██╗████████╗ \n" +
                           "            ██╔══██╗╚██╗██╔╝╚══██╔══╝ \n" +
                           "            ██████╔╝ ╚███╔╝    ██║    \n" +
                           "            ██╔══██╗ ██╔██╗    ██║    \n" +
                           "            ██████╔╝██╔╝ ██╗   ██║    \n" +
                           "            ╚═════╝ ╚═╝  ╚═╝   ╚═╝    \n" +
                           ANSI_RESET +
                           "           The Basic eXpense Tracker  \n" +
		                   "         [Type 'help' to get started.]\n");
		while (true) {
			// the prompt for the app interface
			System.out.print(GREEN_INTENSE + "[BXT]" + ANSI_RESET + "# ");
			// take user input
			String[] command = scanner.nextLine().split(" ");
			// check the first token of the input
			String mode = command[0];
			if (mode.equalsIgnoreCase("add")) {
				// the add command only has one syntax with 2 subsequent inputs
				if (command.length < 3) {
					System.out.println("Invalid syntax. Type 'help add' for more information.");
				}
				// the second input is a user-defined description so does not require validation
				String description = command[1];
				// the third input must be an integer so is first parsed to one
				// should an exception arise it is dealt with
				try {
					int amount = Integer.parseInt(command[2]);
					if (command.length > 3) {
						category = command[3];
						if (category.equals("-")) {
							category = "none";
						}
					} else {
						// the most recently used category is shown here
						// in the first iteration 'none' is the default category
						System.out.print("Add category [" + category  + "]: ");
						String previousCategory = category;
						category = scanner.nextLine();
						// if the user does not enter anything the previous category is taken
						if (category.equals("")) {
							category = previousCategory;
						}
					}
					addExpense(description, amount, category);
				} catch (NumberFormatException e) {
					System.out.println("Invalid syntax. Type 'help add' for more information.");
				}
			} else if (mode.equalsIgnoreCase("view")) {
				// since the view command can be used without any options, with one option,
				// or with two options, the length of the entire string is checked multiple times
				if (command.length > 1) {
					String option = command[1];
					if (option.equalsIgnoreCase("total")) {
						viewTotal();
					} else if (option.equalsIgnoreCase("by")) {
						if (command.length > 2) {
							String sortingCategory = command[2];
							viewSummary(sortingCategory, scanner);
						} else {
							// the command 'view by' must be followed by a category name to filter the results with
							System.out.println("Invalid syntax. Type 'help view' for more information.");
						}
					} else if (option.equalsIgnoreCase("date")) {
						if (command.length > 2) {
							String year = command[2];
							if (command.length > 3) {
								String month = command[3];
								if (command.length > 4) {
									String day = command[4];
									if (command.length > 5) {
										System.out.println("Invalid syntax. Type 'help view' for more information.");
									} else {
										viewDay(year, month, day);
									}
								} else {
									viewMonth(year, month);
								}
							} else {
								viewYear(year);
							}
						} else {
							viewDate();
						}
					} else {
						System.out.println("Unrecognised option: '" + option + "'. Type 'help view' for more information.");
					}
				} else {
					// the view command can also take zero options to display a table of all expenses
					viewExpenses();
				}
			} else if (mode.equalsIgnoreCase("help")) {
				if (command.length > 1) {
					String help = command[1];
					if (help.equalsIgnoreCase("add")) {
						System.out.println("\nUSAGE: add <description> <amount>\n\nThe ADD command is used to create new entries.\n" +
					    	               "- Enter the description of the expense followed by the amount spent.\n" +
					        	           "- You will be prompted to add a category. If nothing is entered, the most recent category is taken.\n");
					} else if (help.equalsIgnoreCase("view")) {
						System.out.println("\nUSAGE: view [total | by <category> | date [<year> [<month> [<day>]]]]\n\n" +
						                   "The VIEW command is used to view expenses.\n" +
					    	               "- Type 'view' to see all the entries listed. To see the total expense, type 'view total'.\n" +
					        	           "- To filter the expense by a specific category, type 'view by <category>'.\n" +
					        	           "- To view the entries along with their date of addition, type 'view date'.\n" +
					        	           "- You can specify the year, month, and day after 'view date' to view only the entries in that range of time.\n" +
					        	           "- The year field must be 4 digits long, while the month and date fields must be 2 digits long.\n" +
					        	           "- They are each to be separated by a space: e.g. view date 2023 06 01\n");
					} else if (help.equalsIgnoreCase("help")) {
						System.out.println("\nUSAGE: help [<command>]\n\nThe HELP command shows how to use BXT.\n" +
					        	           "- Type 'help' for the basic menu and 'help <command>' to learn about the command.\n\n" +
					        	           "How to read the USAGE field:\n- word -> command or option\n- <>   -> descriptor or user-defined value\n" +
					        	           "- []   -> optional input\n- |    -> logical OR i.e. input on the left or right (but not both)\n");
					} else if (help.equalsIgnoreCase("reset")) {
						System.out.println("\nUSAGE: reset\n\nThe RESET command deletes all the entries.\n" +
						                   "- You will be prompted to confirm your choice.\n" +
						                   "- Use with caution as all the saved data will be lost.\n");
					} else if (help.equalsIgnoreCase("exit")) {
						System.out.println("\nUSAGE: exit\n\nThe EXIT command quits BXT.\n");
					} else {
						System.out.println("Unrecognised command: '" + help + "'. Type 'help' for more information.");
					}
				} else {
					// the default help menu if no command is specified after it
					System.out.println("\nCOMMANDS:\nadd   - create entry\nview  - list entries\nhelp  - display this menu\n" +
					                   "reset - delete all entries (caution: irreversible)\nexit  - leave the application\n\nType" +
					                   " 'help <command>' for more information.\n");
				}
			} else if (mode.equalsIgnoreCase("exit")) {
				break;
			} else if (mode.equalsIgnoreCase("reset")) {
				// the N is capitalised to represent a default choice
				System.out.print(ANSI_RED + "CAUTION: ALL DATA WILL BE LOST." + ANSI_RESET + "\nConfirm reset? [y/N] ");
				String confirm = scanner.nextLine();
				if (confirm.equalsIgnoreCase("y")) {
					resetDataset();
					System.out.println("Data deleted.");
				} else {
					System.out.println("Operation aborted.");
				}
			} else if (mode.equals("")) {
				// if nothing is entered there should be no error message, only a repetition of the prompt
				continue;
			} else {
				System.out.println("Unrecognised command: '" + mode + "'. Type 'help' for more information.");
			}
		}
		// prevent a resource leak
		scanner.close();
	}

	public static void viewExpenses() throws FileNotFoundException {
		// the file object represents the file that is referred to in the constructor
		File dataset = new File(OUTPUT_FILE);
		// a scanner object is used to read the file from its object
		Scanner scanner = new Scanner(dataset);
		// formatting: '-' aligns the text to the left, '15' is the number of spaces to fit the word into, and 's' is string
		System.out.println("+---------------+---------------+---------------+");
		System.out.format("|%-15s|%-15s|%-15s|\n", "Description", "Amount", "Category");
		System.out.println("+---------------+---------------+---------------+");
		// if the file is empty
		if (!scanner.hasNextLine()) {
			System.out.format("|%-15s %-15s %-15s|\n", "", "Empty set", "");
		}
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			System.out.format("|%-15s|%-15s|%-15s|\n", data[0], data[1], data[2]);
		}
		System.out.println("+---------------+---------------+---------------+");
		scanner.close();
	}

	public static void viewExpenses(String category) throws FileNotFoundException {
		File dataset = new File(OUTPUT_FILE);
		Scanner scanner = new Scanner(dataset);
		System.out.println("+---------------+---------------+---------------+");
		System.out.format("|%-15s|%-15s|%-15s|\n", "Description", "Amount", "Category");
		System.out.println("+---------------+---------------+---------------+");
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			if (data[2].equals(category)) {
				System.out.format("|%-15s|%-15s|%-15s|\n", data[0], data[1], data[2]);
			}
		}
		System.out.println("+---------------+---------------+---------------+");
		scanner.close();
	}

	public static void viewSummary(String category, Scanner input) throws FileNotFoundException {
		File dataset = new File(OUTPUT_FILE);
		Scanner scanner = new Scanner(dataset);
		int total = 0;
		boolean found = false;
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			if (data[2].equals(category)) {
				found = true;
				total += Integer.parseInt(data[1]);
			}
		}
		if (found == true) {
			System.out.println("+---------------+---------------+");
			System.out.format("|%-15s|%-15s|\n", "Category", "Total");
			System.out.println("+---------------+---------------+");
			System.out.format("|%-15s|%-15d|\n", category, total);
			System.out.println("+---------------+---------------+");
			System.out.print("View individual items? [y/N] ");
			String viewItems = input.nextLine();
			if (viewItems.equalsIgnoreCase("y")) {
				viewExpenses(category);
			}
		} else {
			System.out.println("Category not found: '" + category + "'");
		}
		scanner.close();
	}

	public static void viewYear(String year) throws FileNotFoundException {
		File dataset = new File(OUTPUT_FILE);
		Scanner scanner = new Scanner(dataset);
		System.out.println("+---------------+---------------+---------------+---------------+");
		System.out.format("|%-15s|%-15s|%-15s|%-15s|\n", "Year", "Description", "Amount", "Category");
		System.out.println("+---------------+---------------+---------------+---------------+");
		boolean found = false;
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			String[] date = data[3].split("-");
			if (date[0].equals(year)) {
				found = true;
				System.out.format("|%-15s|%-15s|%-15s|%-15s|\n", year, data[0], data[1], data[2]);
			}
		}
		if (found == false) {
			System.out.format("|%-15s|                %-31s|\n", year, "Year not found");
		}
		System.out.println("+---------------+---------------+---------------+---------------+");
		scanner.close();
	}

	public static void viewMonth(String year, String month) throws FileNotFoundException {
		File dataset = new File(OUTPUT_FILE);
		Scanner scanner = new Scanner(dataset);
		System.out.println("+---------------+---------------+---------------+---------------+");
		System.out.format("|%-15s|%-15s|%-15s|%-15s|\n", "Month", "Description", "Amount", "Category");
		System.out.println("+---------------+---------------+---------------+---------------+");
		boolean found = false;
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			String[] date = data[3].split("-");
			if (date[0].equals(year) && date[1].equals(month)) {
				found = true;
				System.out.format("|%-15s|%-15s|%-15s|%-15s|\n", year + "-" + month, data[0], data[1], data[2]);
			}
		}
		if (found == false) {
			System.out.format("|%-15s|                %-31s|\n", year + "-" + month, "Month not found");
		}
		System.out.println("+---------------+---------------+---------------+---------------+");
		scanner.close();
	}

	public static void viewDay(String year, String month, String day) throws FileNotFoundException {
		File dataset = new File(OUTPUT_FILE);
		Scanner scanner = new Scanner(dataset);
		System.out.println("+---------------+---------------+---------------+---------------+");
		System.out.format("|%-15s|%-15s|%-15s|%-15s|\n", "Date", "Description", "Amount", "Category");
		System.out.println("+---------------+---------------+---------------+---------------+");
		boolean found = false;
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			String[] date = data[3].split("-");
			if (date[0].equals(year) && date[1].equals(month) && date[2].equals(day)) {
				found = true;
				System.out.format("|%-15s|%-15s|%-15s|%-15s|\n", year + "-" + month + "-" + day, data[0], data[1], data[2]);
			}
		}
		if (found == false) {
			System.out.format("|%-15s|                %-31s|\n", year + "-" + month + "-" + day, "Date not found");
		}
		System.out.println("+---------------+---------------+---------------+---------------+");
		scanner.close();
	}

	public static void viewDate() throws FileNotFoundException {
		File dataset = new File(OUTPUT_FILE);
		Scanner scanner = new Scanner(dataset);
		System.out.println("+---------------+---------------+---------------+---------------+");
		System.out.format("|%-15s|%-15s|%-15s|%-15s|\n", "Date", "Description", "Amount", "Category");
		System.out.println("+---------------+---------------+---------------+---------------+");
		boolean found = false;
		if (!scanner.hasNextLine()) {
			System.out.format("|%-26s %s %-26s|\n", "", "Empty set", "");
		}
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			String[] date = data[3].split("-");
			System.out.format("|%-15s|%-15s|%-15s|%-15s|\n", date[0] + "-" + date[1] + "-" + date[2], data[0], data[1], data[2]);
		}
		System.out.println("+---------------+---------------+---------------+---------------+");
		scanner.close();
	}

	public static void addExpense(String description, int amount, String category) throws IOException {
		// the writer object writes to a file
		// the value of 'true' in the constructor means that the writer appends to the file instead of overwriting it
		FileWriter writer = new FileWriter(OUTPUT_FILE, true);
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
		String formattedDate = date.format(formatter);
		writer.write(description + "," + amount + "," + category + ","  + formattedDate + "\n");
		writer.close();
	}

	public static void viewTotal() throws FileNotFoundException {
		File dataset = new File(OUTPUT_FILE);
		Scanner scanner = new Scanner(dataset);
		int total = 0;
		System.out.println("+---------------+---------------+");
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			total += Integer.parseInt(data[1]);
		}
		System.out.format("|%-15s|%-15d|\n", "Total expense:", total);
		System.out.println("+---------------+---------------+");
		scanner.close();
	}

	public static void resetDataset() throws IOException {
		// the second argument here is 'false' to overwrite the file instead of append to it
		FileWriter writer = new FileWriter(OUTPUT_FILE, false);
		writer.write("");
		writer.close();
	}
}
