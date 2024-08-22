import java.util.*;

class User {
    String username;
    String password;
    List<Booking> bookings = new ArrayList<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

class ChargingStation {
    String id;
    String location;
    boolean isFastCharging;
    Map<String, Boolean> timeSlots = new LinkedHashMap<>();
    List<String> reviews = new ArrayList<>();
    double rating = 0.0;
    int ratingCount = 0;

    public ChargingStation(String id, String location, boolean isFastCharging) {
        this.id = id;
        this.location = location;
        this.isFastCharging = isFastCharging;
        initializeTimeSlots();
    }

    private void initializeTimeSlots() {
        String[] slots = {"09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00"};
        for (String slot : slots) {
            timeSlots.put(slot, true);
        }
    }

    public String toString() {
        return "Station ID: " + id + ", Location: " + location + ", Fast Charging: " + (isFastCharging ? "Yes" : "No") +
               ", Rating: " + (ratingCount == 0 ? "Not Rated" : rating + " (" + ratingCount + " reviews)");
    }

    public boolean isSlotAvailable(String slot) {
        return timeSlots.getOrDefault(slot, false);
    }

    public void bookSlot(String slot) {
        timeSlots.put(slot, false);
    }

    public void addReview(String review, double rating) {
        reviews.add(review);
        this.rating = (this.rating * this.ratingCount + rating) / ++this.ratingCount;
    }

    public List<String> getAvailableSlots() {
        List<String> availableSlots = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : timeSlots.entrySet()) {
            if (entry.getValue()) {
                availableSlots.add(entry.getKey());
            }
        }
        return availableSlots;
    }
}

class Booking {
    ChargingStation station;
    String timeSlot;

    public Booking(ChargingStation station, String timeSlot) {
        this.station = station;
        this.timeSlot = timeSlot;
    }

    public String toString() {
        return "Booking at " + station.location + " during " + timeSlot;
    }
}

public class EVChargingStationFinder {
    private static List<ChargingStation> stations = new ArrayList<>();
    private static Map<String, User> users = new HashMap<>();
    private static User currentUser;

    public static void main(String[] args) {
        initializeStations();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (currentUser == null) {
                displayLoginMenu(scanner);
            } else {
                displayMainMenu(scanner);
            }
        }
    }

    private static void displayLoginMenu(Scanner scanner) {
        System.out.println("\n1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                login(scanner);
                break;
            case 2:
                register(scanner);
                break;
            case 3:
                System.out.println("Exiting the system. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private static void displayMainMenu(Scanner scanner) {
        System.out.println("\nWelcome, " + currentUser.username);
        System.out.println("1. Find Charging Stations");
        System.out.println("2. Book Charging Slot");
        System.out.println("3. Cancel/Modify Booking");
        System.out.println("4. Add Review & Rating");
        System.out.println("5. Emergency Support");
        System.out.println("6. Logout");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                findChargingStations(scanner);
                break;
            case 2:
                bookChargingSlot(scanner);
                break;
            case 3:
                cancelOrModifyBooking(scanner);
                break;
            case 4:
                addReviewAndRating(scanner);
                break;
            case 5:
                emergencySupport();
                break;
            case 6:
                currentUser = null;
                System.out.println("Logged out successfully.");
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private static void initializeStations() {
        stations.add(new ChargingStation("1", "Downtown", true));
        stations.add(new ChargingStation("2", "Uptown", false));
        stations.add(new ChargingStation("3", "Suburbs", true));
        stations.add(new ChargingStation("4", "City Center", false));
    }

    private static void register(Scanner scanner) {
        System.out.print("Enter a username: ");
        String username = scanner.nextLine();
        if (users.containsKey(username)) {
            System.out.println("Username already exists. Please choose a different one.");
            return;
        }

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();
        users.put(username, new User(username, password));
        System.out.println("Registration successful. You can now log in.");
    }

    private static void login(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        User user = users.get(username);
        if (user != null && user.password.equals(password)) {
            currentUser = user;
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }

    private static void findChargingStations(Scanner scanner) {
        System.out.println("\nFind Charging Stations");
        System.out.print("Enter location filter (leave empty to skip): ");
        String locationFilter = scanner.nextLine();
        
        System.out.print("Require fast charging? (yes/no/skip): ");
        String fastChargingFilter = scanner.nextLine();
        boolean isFastCharging = fastChargingFilter.equalsIgnoreCase("yes");

        System.out.println("\nMatching Charging Stations:");
        for (ChargingStation station : stations) {
            if ((locationFilter.isEmpty() || station.location.equalsIgnoreCase(locationFilter)) &&
                (fastChargingFilter.equalsIgnoreCase("skip") || station.isFastCharging == isFastCharging)) {
                System.out.println(station);
            }
        }
    }

    private static void bookChargingSlot(Scanner scanner) {
        System.out.print("\nEnter the ID of the station to book: ");
        String id = scanner.nextLine();

        ChargingStation selectedStation = null;
        for (ChargingStation station : stations) {
            if (station.id.equals(id)) {
                selectedStation = station;
                break;
            }
        }

        if (selectedStation == null) {
            System.out.println("No station found with ID: " + id);
            return;
        }

        System.out.println("Available Time Slots:");
        List<String> availableSlots = selectedStation.getAvailableSlots();
        for (int i = 0; i < availableSlots.size(); i++) {
            System.out.println((i + 1) + ". " + availableSlots.get(i));
        }

        if (availableSlots.isEmpty()) {
            System.out.println("No available slots. Would you like to join the waitlist? (yes/no): ");
            String joinWaitlist = scanner.nextLine();
            if (joinWaitlist.equalsIgnoreCase("yes")) {
                System.out.println("You have been added to the waitlist.");
                // Implementation of waitlist logic
            }
            return;
        }

        System.out.print("Choose a time slot (number): ");
        int slotChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (slotChoice < 1 || slotChoice > availableSlots.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        String chosenSlot = availableSlots.get(slotChoice - 1);
        selectedStation.bookSlot(chosenSlot);
        Booking booking = new Booking(selectedStation, chosenSlot);
        currentUser.bookings.add(booking);

        System.out.println("Booking successful for " + booking);
        processPayment(scanner);
    }

    private static void cancelOrModifyBooking(Scanner scanner) {
        System.out.println("\nYour Bookings:");
        if (currentUser.bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (int i = 0; i < currentUser.bookings.size(); i++) {
            System.out.println((i + 1) + ". " + currentUser.bookings.get(i));
        }

        System.out.print("Choose a booking to cancel/modify (number): ");
        int bookingChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (bookingChoice < 1 || bookingChoice > currentUser.bookings.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        Booking chosenBooking = currentUser.bookings.get(bookingChoice - 1);
        System.out.println("1. Cancel Booking");
        System.out.println("2. Modify Booking");
        System.out.print("Choose an option: ");
        
        int option = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (option) {
            case 1:
                chosenBooking.station.timeSlots.put(chosenBooking.timeSlot, true);
                currentUser.bookings.remove(chosenBooking);
                System.out.println("Booking cancelled successfully.");
                break;
            case 2:
                chosenBooking.station.timeSlots.put(chosenBooking.timeSlot, true);
                currentUser.bookings.remove(chosenBooking);
                System.out.println("Booking cancelled. Please make a new booking.");
                bookChargingSlot(scanner);
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void addReviewAndRating(Scanner scanner) {
        System.out.print("Enter the ID of the station to review: ");
        String id = scanner.nextLine();

        ChargingStation selectedStation = null;
        for (ChargingStation station : stations) {
            if (station.id.equals(id)) {
                selectedStation = station;
                break;
            }
        }

        if (selectedStation == null) {
            System.out.println("No station found with ID: " + id);
            return;
        }

        System.out.print("Enter your review: ");
        String review = scanner.nextLine();
                System.out.print("Enter your rating (0.0 - 5.0): ");
        double rating = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        if (rating < 0.0 || rating > 5.0) {
            System.out.println("Invalid rating. Please enter a value between 0.0 and 5.0.");
            return;
        }

        selectedStation.addReview(review, rating);
        System.out.println("Thank you for your feedback!");
    }

    private static void processPayment(Scanner scanner) {
        System.out.println("Processing payment...");
        // Simulate payment process
        System.out.print("Enter payment amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        System.out.println("Payment of $" + amount + " was successful. Thank you!");
    }

    private static void emergencySupport() {
        System.out.println("\nEmergency Support Options:");
        System.out.println("1. Call Roadside Assistance");
        System.out.println("2. Report an Issue with a Charging Station");
        System.out.println("3. Contact Customer Support");
        System.out.print("Choose an option: ");

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                System.out.println("Dialing Roadside Assistance...");
                // Simulate calling assistance
                break;
            case 2:
                System.out.print("Enter the ID of the station to report: ");
                String stationId = scanner.nextLine();
                System.out.println("Reporting an issue with station ID: " + stationId);
                // Simulate reporting process
                break;
            case 3:
                System.out.println("Connecting to Customer Support...");
                // Simulate customer support connection
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
}

       
