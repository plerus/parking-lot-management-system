import java.time.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {

   static final Parking parkingLot = new Parking("Parking Lot", "12345678910112","Los Angeles","9 8596-3230", 100, 120);
   static boolean payToday = false;

   public static void main (String[] args) {
      Scanner scan = new Scanner(System.in);
      boolean continueLoop = true;
      getPaymentAllVehicles();

      do {
         System.out.printf("%s%n%s%n%s%n%s%n%s%n%s%n%n%s%n%s",
               "[1] Vehicle entrance",
               "[2] Vehicle exit",
               "[3] Register vehicle",
               "[4] Update vehicle information",
               "[5] Delete vehicle", 
               "[6] Report",
               "[-1] To close program",
               "Option: ");
         int option = scan.nextInt();
         System.out.println();
         if (option == -1) {
            continueLoop = false;
         }

         menu(option);
      } while (continueLoop == true);
   }

   public static void menu(int option) {

      switch (option) {
         case 1: registerEntranceTime();
                 break;
         case 2: registerExitTime(); 
                 break;
         case 3: registerVehicle();
                 break;
         case 4: updateVehicleInfo();
                 break;
         case 5: deleteVehicle();
                 break;
         case 6: generateSummary();
                 break;
         case -1: System.out.println("Closing program...");
                  break;
         default: System.out.printf("Not a option!%n%n");
      }
   }

   public static void registerVehicle() {
      //Random vehicle data for modifying along user input
      Vehicle vehicle = new Vehicle( "owner", "12345678910", "car", "brand", "model", "color", "azc1294", new ResidentAffiliationModel());

      Scanner scan = new Scanner(System.in);

      System.out.print("Name: ");
      String owner = scan.next();
      vehicle.setOwner(owner);

      System.out.print("Cellphone: ");
      String cellphone = scan.next();
      vehicle.setCellphone(cellphone);

      System.out.print("Car[C] or motorcycle[M]: ");
      char option = scan.next().charAt(0);

      if (option != 'c' && option != 'm') {
         do {
            System.out.printf("%nNot a option!%n%n");
            System.out.println("Car[C] or motorcycle[M]: ");
            option = scan.next().charAt(0);

         } while (option != 'c' && option != 'm');
      }

      if (option == 'c') {
         vehicle.setVehicleType("car");
      }

      else if (option == 'm') {
         vehicle.setVehicleType("motorcycle");
      }

      System.out.print("Brand: ");
      String brand = scan.next();
      vehicle.setBrand(brand);

      System.out.print("Model: ");
      String model = scan.next();
      vehicle.setModel(model);

      System.out.print("Color: ");
      String color = scan.next();
      vehicle.setColor(color);

      System.out.print("License plate: ");
      String licensePlate = scan.next();
      vehicle.setLicensePlate(licensePlate.toUpperCase());

      System.out.printf("Choose an affiliation:%n%s%n%s%n%s%n%s", "[1]Employee", "[2]Resident", "[3]Non-resident", "Affiliation option: ");

      int affiliationOption = scan.nextInt();

      while (affiliationOption != 1 && affiliationOption != 2 && affiliationOption != 3) {
         System.out.printf("%nInvalid affiliation!%n%n");
         System.out.printf("Choose an affiliation:%n%s%n%s%n%s%n%s", 
               "[1]Employee", 
               "[2]Resident", 
               "[3]Non-resident", 
               "Affiliation option: ");
         affiliationOption = scan.nextInt();
      } 

      AffiliationModel affiliationModel;

      switch (affiliationOption) {
         case 1: affiliationModel = new EmployeeAffiliationModel();
                 break;
         case 2: affiliationModel = new ResidentAffiliationModel();     
                 break;
         case 3: affiliationModel = new NonResidentAffiliationModel();
                 break;
         default:throw new IllegalArgumentException("Invalid option");
      }
      vehicle.setAffiliationModel(affiliationModel);

      String sql = "INSERT INTO vehicle (full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model)"
         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

      try (Connection conn = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {

         pstmt.setString(1, vehicle.getOwner());
         pstmt.setString(2, vehicle.getCellphone());
         pstmt.setString(3, vehicle.getVehicleType());
         pstmt.setString(4, vehicle.getBrand());
         pstmt.setString(5, vehicle.getModel());
         pstmt.setString(6, vehicle.getColor());
         pstmt.setString(7, vehicle.getLicensePlate());
         pstmt.setString(8, vehicle.getAffiliationModel().toString());

         pstmt.executeUpdate();
         System.out.printf("%nVehicle registered!%n%n");

      } 
      catch (SQLException e) {
         System.err.println(e.getMessage());
      }
   } 

   public static void registerEntranceTime() {
      Scanner scan = new Scanner(System.in);
      System.out.print("Input license plate: ");
      String licensePlate = scan.next();
      licensePlate = licensePlate.toUpperCase();

      Vehicle vehicle = searchVehicleByLicensePlate(licensePlate);

      if (vehicle != null) {
         if (!vehicle.getIsInside()) {
            String vehicleType = vehicle.getVehicleType();
            String sql = "SELECT COUNT(*) FROM vehicle WHERE vehicle_type = ? AND is_inside = true";

            try (var conn = DB.connect();
                  var pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, vehicleType);
               var rs = pstmt.executeQuery();

               if (rs.next()) {
                  int count = rs.getInt(1);
                  if ("car".equals(vehicleType) && count >= 100) {
                     System.out.printf("Parking full for cars.%n%n");
                     return;
                  } else if ("motorcycle".equals(vehicleType) && count >= 120) {
                     System.out.printf("Parking full for motorcycles.%n%n");
                     return;
                  }
               }
            } 
            catch (SQLException e) {
               e.printStackTrace();
            }
            vehicle.setEntranceTime(vehicle.getLicensePlate());
            vehicle.setInside(true, vehicle.getLicensePlate());
            System.out.printf("%nEntrance registered!%n%n");
         }
         else {

            System.out.printf("%nVehicle is already parked!%n%n");
         }
      }
   }

   public static void registerExitTime() {

      Scanner scan = new Scanner(System.in);
      System.out.print("Input license plate: ");
      String licensePlate = scan.next();
      licensePlate = licensePlate.toUpperCase();
      Vehicle vehicle = searchVehicleByLicensePlate(licensePlate);

      if (vehicle != null) { 
         if (vehicle.getIsInside()) {
            vehicle.setExitTime(vehicle.getLicensePlate());
            vehicle.setInside(false, vehicle.getLicensePlate());
            //to register vehicle parking time 
            vehicle.setTimeParked();
            System.out.printf("%nExit registered!%n%n");
         }
         else {
            System.out.printf("%nVehicle isn't parked!%n%n");
         }
      }
   }

   public static void showVehicleInfo() {
      Scanner scan = new Scanner(System.in);
      System.out.print("Input license plate or enter number 2 to search by other info: ");
      String information = scan.next();
      System.out.println();
      ArrayList<Vehicle> vehicles = null;

      try {
         Integer.parseInt(information);
         System.out.println("Enter a option and the information to search separated by space: ");
         System.out.printf("%s%n%s %n%s%n%s%n%s %n%n%s%n%s", 
               "[1] Nome",
               "[2] Cellphone",
               "[3] Brand",
               "[4] Model",
               "[5] Color",
               "[-1] Previous",
               "Option: ");
         Scanner input = new Scanner(System.in);
         int option = input.nextInt();
         information = input.next();

         information = option == 1 ? information.substring(0, 1).toUpperCase() + 
            information.substring(1) : information;

         vehicles = searchVehicle(option, information);
      }
      catch(Exception NumberFormatException) {
         //exception occurred, it means user input licensePlate
         information = information.toUpperCase();
         vehicles = searchVehicle(6, information);
      }

      if (vehicles != null) {
         for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle.toString());
         }
      }
      else {
         System.out.printf("Vehicle isn't registered%n%n");
      }
   }

   public static void showAffiliationVehicles(int option) {
      String sql = "SELECT full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model, time_parked, is_inside FROM vehicle WHERE affiliation_model = ?";
      String affiliation = null;
      ArrayList<Vehicle> vehicles = new ArrayList<>();
      
      switch (option) {
         case 1:
            affiliation = "Resident";
            break;
         case 2:
            affiliation = "Non_resident";
            break;
         case 3:
            affiliation = "Employee";
            break;
         default:
            System.out.printf("%nInvalid option!%n");
      }

      try (var conn = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {
         pstmt.setString(1, affiliation);
         var rs = pstmt.executeQuery();
         while (rs.next()) {
            vehicles.add(new Vehicle(
                     rs.getString("full_name"),
                     rs.getString("cellphone"),
                     rs.getString("vehicle_type"),
                     rs.getString("brand"),
                     rs.getString("model"),
                     rs.getString("color"),
                     rs.getString("license_plate"),
                     fromString(rs.getString("affiliation_model")),
                     rs.getInt("time_parked"),
                     rs.getBoolean("is_inside")
                     ));
         }
      }
      catch (SQLException e) {
         e.printStackTrace();
      }

      if (vehicles.isEmpty()) {
         System.out.printf("%nThere are none %s!%n%n", affiliation);
      }
      else {
         for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle.toString());
         }
      }
   }    

   public static void updateVehicleInfo() {

      Scanner input = new Scanner(System.in);
      System.out.print("Input license plate: ");
      String licensePlate = input.next();
      licensePlate = licensePlate.toUpperCase();
      Vehicle vehicle = searchVehicleByLicensePlate(licensePlate);

      String licensePlateBeforeChange = vehicle.getLicensePlate();
      boolean continueLoop = true;
      Scanner scan = new Scanner(System.in);

      int option;
      String information;

      do {
         System.out.printf("%nUpdate:%n%s%n%s%n%s%n%s%n%s%n%s%n%n%s%n%s", 
               "[1] Name: ", 
               "[2] Cellphone", 
               "[3] Vehicle", 
               "[4] Color",
               "[5] License Plate", 
               "[6] Affiliation", 
               "[-1] To return to menu",
               "Option: ");
         option = input.nextInt();
         System.out.println();

         if (option != 0 && option >= -1 && option < 7) {
            continueLoop = false;
         }
      } while (continueLoop == true);

      switch (option) {
         case -1: 
            break;
         case 1: System.out.print("Enter name: ");
                 information = input.next();
                 vehicle.setOwner(information);
                 break;
         case 2: System.out.print("Enter cellphone: ");
                 information = input.next();
                 vehicle.setCellphone(information);
                 break;
         case 3: System.out.println("Car[C] or motorcycle[M]: ");
                 char cORm = input.next().charAt(0);
                 if (cORm != 'c' && cORm != 'm') {
                    do {
                       System.out.println("Not a cORm!");
                       System.out.println("Car[C] or motorcycle[M]: ");
                       cORm = scan.next().charAt(0);

                    } while (cORm != 'c' && cORm != 'm');
                 }

                 information = cORm == 'c' ? information = "car" : "motorcycle";
                 vehicle.setVehicleType(information);

                 System.out.println("Enter brand: ");
                 information = input.next();
                 vehicle.setBrand(information);

                 System.out.println("Enter model: ");
                 information = input.next();
                 vehicle.setModel(information);

                 System.out.println("Enter color: ");
                 information = input.next();
                 vehicle.setColor(information);

                 System.out.println("Enter license plate: ");
                 information = input.next();
                 vehicle.setLicensePlate(information);
                 licensePlate = licensePlate.toUpperCase();
                 break;
         case 4: System.out.print("Enter color: ");
                 information = input.next();
                 vehicle.setColor(information);
                 break;
         case 5: System.out.print("Enter license plate: ");
                 information = input.next();
                 vehicle.setLicensePlate(information);
                 licensePlate = licensePlate.toUpperCase();
                 break;
         case 6: System.out.printf("Choose an affiliation:%n%s%n%s%n%s%n%s", 
                       "[1]Employee", 
                       "[2]Resident", 
                       "[3]Non-resident", 
                       "Affiliation option: ");

                 int affiliationOption = scan.nextInt();

                 while (affiliationOption != 1 && affiliationOption != 2 && affiliationOption != 3) {
                    System.out.println("Invalid affiliation");
                    System.out.printf("Choose an affiliation:%n%s%n%s%n%s%n%s", 
                          "[1]Employee", 
                          "[2]Resident", 
                          "[3]Non-resident", 
                          "Affiliation option: "); 
                    affiliationOption = scan.nextInt();
                 } 

                 AffiliationModel affiliationModel;

                 switch (affiliationOption) {
                    case 1: affiliationModel = new EmployeeAffiliationModel();
                            break;
                    case 2: affiliationModel = new ResidentAffiliationModel();     
                            break;
                    case 3: affiliationModel = new NonResidentAffiliationModel();
                            break;
                    default:throw new IllegalArgumentException("Not an Affiliation");
                 }
                 vehicle.setAffiliationModel(affiliationModel);
                 break;
      }

      var sql  = "UPDATE vehicle "
         + "SET full_name = ?, cellphone= ?, vehicle_type = ?, brand = ?, model = ?, color = ?, license_plate = ?, affiliation_model = ? "
         + "WHERE license_plate = ?";

      try (var conn  = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {

         pstmt.setString(1, vehicle.getOwner());
         pstmt.setString(2, vehicle.getCellphone());
         pstmt.setString(3, vehicle.getVehicleType());
         pstmt.setString(4, vehicle.getBrand());
         pstmt.setString(5, vehicle.getModel());
         pstmt.setString(6, vehicle.getColor());
         pstmt.setString(7, vehicle.getLicensePlate());
         pstmt.setString(8, vehicle.getAffiliationModel().toString());
         pstmt.setString(9, licensePlateBeforeChange);

         pstmt.executeUpdate();
         System.out.printf("%nUpdated!%n%n");
      } 
      catch (SQLException e) {
         e.printStackTrace();
      }
   }

   public static void deleteVehicle() {
      Scanner scan = new Scanner(System.in);

      System.out.print("Input license plate: ");
      String licensePlate = scan.next();

      licensePlate = licensePlate.toUpperCase();
      Vehicle vehicle = searchVehicleByLicensePlate(licensePlate);

      System.out.printf("%n%s%n", vehicle.toString());
      System.out.print("Are you sure want to delete this vehicle?[y/n]: ");
      Scanner input = new Scanner(System.in);
      char option = input.next().charAt(0);

      System.out.println();

      if (option != 'y' && option != 'n') {
         do {
            System.out.println("Not a option!");
            System.out.println("Car[C] or motorcycle[M]: ");
            option = input.next().charAt(0);

         } while (option != 'y' && option != 'n');
      }

      if (option == 'y') {
         var sql  = "DELETE FROM vehicle WHERE license_plate=?";

         try (var conn  = DB.connect();
               var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vehicle.getLicensePlate());

            pstmt.executeUpdate();

            System.out.printf("Vehicle deleted!%n%n");

         } 
         catch (SQLException e) {
            e.printStackTrace();
         }
      }
      else {
         System.out.printf("Vehicle not deleted! Returning to menu.%n%n");
      }
   }

   public static void showEntranceAndExitTime(String licensePlate) {  
      var sql = "SELECT entrance, exit FROM vehicle WHERE license_plate = ?";

      try (var conn = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {

         pstmt.setString(1, licensePlate);
         var rs = pstmt.executeQuery();

         if (rs.next()) {
            String entranceTime = rs.getString("entrance");
            String exitTime = rs.getString("exit");

            entranceTime = (entranceTime != null) ? entranceTime : "---------";
            exitTime = (exitTime != null) ? exitTime : "---------";

            System.out.printf("%nEntrance time: %s%n", entranceTime);
            System.out.printf("Exit time: %s%n%n", exitTime);
         } else {
            System.out.println("No times found for the given license plate.");
         }
      } 
      catch (SQLException e) {
         e.printStackTrace();
      }
   }

   public static void chargeRentFromResidents() {
      //Tenho que puxar todos os veículos residentes no dia 1 de cada mês(virou meia-noite já emite a cobrança) e printar a placa e o valor no formato: 
      //núm. placa    tempo estacionado(min)    valor a pagar
      //ABC1234            700                      35.00

      //De alguma conferir o dia presente de tempos em tempos. Talvez aqui dentro do código ter algo que acompanha o tempo e ter um if que se bater tal dia já chama este método aqui e emite.
      //Posso fazer com que cada vez que o programa seja iniciado ele obtenha a data e se a data for dia 1 do mês ele emite a a cobrança na hora.
   }

   public static Vehicle searchVehicleByLicensePlate(String licensePlate) {
      var sql = "SELECT full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model, time_parked, is_inside FROM vehicle WHERE license_plate=?";

      Vehicle vehicle = null;
      try (var conn = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {

         pstmt.setString(1, licensePlate);
         var rs = pstmt.executeQuery();

         if (rs.next()) {
            return new Vehicle(
                  rs.getString("full_name"),
                  rs.getString("cellphone"),
                  rs.getString("vehicle_type"),
                  rs.getString("brand"),
                  rs.getString("model"),
                  rs.getString("color"),
                  rs.getString("license_plate"),
                  fromString(rs.getString("affiliation_model")),
                  rs.getInt("time_parked"),
                  rs.getBoolean("is_inside")
                  );
         }
      }
      catch (SQLException e) {
         e.printStackTrace();
      }
      if (vehicle != null) {
         return vehicle;
      }

      System.out.printf("%nThis vehicle isn't registered!%n%n");
      return null;
   }

   public static ArrayList<Vehicle> searchVehicle(int option, String column) {
      String sql = "";
      ArrayList<Vehicle> vehicles = new ArrayList<>();

      switch (option) {
         case 1:
            sql = "SELECT full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model, time_parked, is_inside FROM vehicle WHERE full_name = ?";
            break;
         case 2:
            sql = "SELECT full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model, time_parked, is_inside FROM vehicle WHERE cellphone = ?";
            break;
         case 3:
            sql = "SELECT full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model, time_parked, is_inside FROM vehicle WHERE brand = ?";
            break;
         case 4:
            sql = "SELECT full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model, time_parked, is_inside FROM vehicle WHERE model = ?";
            break;
         case 5:
            sql = "SELECT full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model, time_parked, is_inside FROM vehicle WHERE color = ?";
            break;
         case 6:
            sql = "SELECT full_name, cellphone, vehicle_type, brand, model, color, license_plate, affiliation_model, time_parked, is_inside FROM vehicle WHERE license_plate = ?";
            break;
         default:
            System.out.printf("%nInvalid option!%n");
            return null;
      }

      try (var conn = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {
         pstmt.setString(1, column);
         var rs = pstmt.executeQuery();
         while (rs.next()) {
            vehicles.add(new Vehicle(
                     rs.getString("full_name"),
                     rs.getString("cellphone"),
                     rs.getString("vehicle_type"),
                     rs.getString("brand"),
                     rs.getString("model"),
                     rs.getString("color"),
                     rs.getString("license_plate"),
                     fromString(rs.getString("affiliation_model")),
                     rs.getInt("time_parked"),
                     rs.getBoolean("is_inside")
                     ));
         }
      } 
      catch (SQLException e) {
         e.printStackTrace();
      }

      if (vehicles.isEmpty()) {
         System.out.printf("%nThis vehicle isn't registered!%n%n");
         return null;
      }
      return vehicles;
   }    

   public static void getPaymentAllVehicles() {
      LocalDate today = LocalDate.now();
      //Do later: save the payToday status in a file and call from there if is false or true. The implementation below doesn't help in case of close program and open again, the code below is executed again
      if (today.getDayOfMonth() == 21) {payToday = true;}
      else {payToday = false;}
      ArrayList<Vehicle> vehicles = null;

      if (today.getDayOfMonth() == 1 && payToday == true) {
         System.out.println("Today is the first day of month");
         var sql = "SELECT license_plate FROM vehicle";

         try (var conn = DB.connect();
               var stmt = conn.createStatement();
               var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
               String licensePlate = rs.getString("license_plate");

               vehicles = searchVehicle(6, licensePlate);

               if (vehicles != null) {
                  for (Vehicle vehicle : vehicles) {

                     double payment = vehicle.getPayment();
                     System.out.printf("License Plate: %s, Payment: $%.2f%n", licensePlate, payment);

                     sql = "UPDATE vehicle set time_parked = 0 where license_plate = ?";
                     var pstmt = conn.prepareStatement(sql); 

                     pstmt.setString(1, vehicle.getLicensePlate());

                     pstmt.executeUpdate();
                     pstmt.close();
                  }
               }
            }
         } 
         catch (SQLException e) {
            e.printStackTrace();
         }
      }
   }

   public static AffiliationModel fromString(String affiliation) {
      switch (affiliation) {
         case "Resident":
            return new ResidentAffiliationModel();
         case "Non_resident":
            return new NonResidentAffiliationModel();
         case "Employee":
            return new EmployeeAffiliationModel();
         default:
            throw new IllegalArgumentException("Unknown affiliation model: " + affiliation);
      }
   }

   public static void generateSummary() {
      Scanner scan = new Scanner(System.in);
      System.out.printf("%s%n%s%n%s%n%n%s%n%s",
            "[1] Show vehicle info",
            "[2] Show vehicle entrance and exit time",
            "[3] List all vehicles of Residents, Non-residents or Employees",
            "[-1] To previous menu",
            "option: ");

      Scanner input = new Scanner(System.in);
      int option = scan.nextInt();
      System.out.println();

      switch (option) {
         case 1: showVehicleInfo();
                 break;
         case 2: System.out.print("Enter License plate: ");
                 String licensePlate = input.next();
                 showEntranceAndExitTime(licensePlate.toUpperCase());
                 break;
         case 3: System.out.printf("[1] Residents, [2] Non-residents, [3] Employees%nOption: ");
                 int op = input.nextInt();
                 System.out.println();
                 while (op < 1 || op > 3) {
                    System.out.printf("Invalid input. [1] Residents, [2] Non-residents, [3] Employees%nOption: "); 
                    op = input.nextInt();
                    System.out.println();
                 }
                 showAffiliationVehicles(op);
                 break;
                 //Remove later this printf below and go to previous menu. Should call menu()?
         default: System.out.printf("Not a option!%n%n");
      }
   }
}
