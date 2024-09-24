import java.util.Scanner;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.InputMismatchException;
import java.sql.SQLException;

public class Vehicle {

   private String owner, cellphone, vehicleType, brand, model, color, licensePlate;
   private AffiliationModel affiliationModel;
   private LocalDateTime entranceTime, exitTime;
   private long timeParked = 0;
   private boolean isInside;

   public Vehicle(String owner, String cellphone,String vehicleType, String brand, 
         String model, String color, String licensePlate, 
         AffiliationModel affiliationModel) {
      this.owner = owner;
      this.cellphone = cellphone;
      this.vehicleType = vehicleType;
      this.brand = brand;
      this.model = model;
      this.color = color;
      this.licensePlate = licensePlate;
      this.affiliationModel = affiliationModel;
   }

   public Vehicle(String owner, String cellphone,String vehicleType, String brand, 
         String model, String color, String licensePlate, 
         AffiliationModel affiliationModel, long timeParked, Boolean isInside) {
      this(owner, cellphone, vehicleType, brand, model, color, licensePlate, affiliationModel);
      this.timeParked = timeParked;
      this.isInside = isInside;
   }

   public String getOwner() {
      return owner;
   }

   public void setOwner(String owner) {
      if (owner.matches("^[a-zA-Z\\s]{1,100}+$")) {
         this.owner = owner;
      }
      else {
         Scanner scan = new Scanner(System.in);
         String ownerTemp;

         do {
            System.out.printf("%nInvalid name!%n%n");
            System.out.print("Name: ");
            ownerTemp = scan.next();

         } while (!ownerTemp.matches("^[a-zA-Z\\s]{1,100}+$"));

         this.owner = ownerTemp;
      }
   }

   public String getCellphone() {
      return cellphone;
   }

   public void setCellphone(String cellphone) {
      if (cellphone.matches("\\d{11}")) {
         this.cellphone = cellphone;
      }
      else {
         Scanner scan = new Scanner(System.in);
         String cellTemp;

         do {
            System.out.printf("%nInvalid phone number!%n%n");
            System.out.print("Number: ");
            cellTemp = scan.next();

         } while (!cellTemp.matches("\\d{11}"));

         this.cellphone = cellTemp;
      }
   }

   public String getVehicleType() {
      return vehicleType;
   }

   public void setVehicleType(String vehicleType) {
      if (vehicleType.equals("car") || vehicleType.equals("motorcycle")) {
         this.vehicleType = vehicleType;       
      }
      else {
         Scanner scan = new Scanner(System.in);
         String vehicleTypeTemp;

         do {
            System.out.printf("%nInvalid vehicle type!%n%n");
            System.out.print("Vehicle type: ");
            vehicleTypeTemp = scan.next();

         } while (!vehicleTypeTemp.equals("car") && !vehicleTypeTemp.equals("motorcycle"));

         this.vehicleType = vehicleTypeTemp;
      }
   }

   public String getBrand() {
      return brand;
   }

   public void setBrand(String brand) {
      if (brand.matches("[a-zA-Z0-9]{1,30}")) {
         this.brand = brand;
      }
      else {
         Scanner scan = new Scanner(System.in);
         String brandTemp;

         do {
            System.out.println("Brand must be 1 to 30 characters long and contain only letters and numbers.");
            System.out.print("Brand type: ");
            brandTemp = scan.next();

         } while (!brandTemp.matches("[a-zA-Z0-9]{1,30}"));

         this.brand = brandTemp;
      }
   }

   public String getModel() {
      return model;
   }

   public void setModel(String model) {
      if (model.matches("[a-zA-Z0-9]{1,30}")) {
         this.model = model;
      }
      else {
         Scanner scan = new Scanner(System.in);
         String modelTemp;

         do {
            System.out.println("Model must be 1 to 30 characters long and contain only letters and numbers.");
            System.out.print("Model type: ");
            modelTemp = scan.next();

         } while (!modelTemp.matches("[a-zA-Z0-9]{1,30}"));

         this.model = modelTemp;
      }
   }

   public String getColor() {
      return color;
   }

   public void setColor(String color) {
      this.color = color;
   }

   public String getLicensePlate() {
      return licensePlate;
   }

   public void setLicensePlate(String licensePlate) {
      //checks if license plate are in the correct formats
      if (licensePlate.matches("[a-zA-Z]{3}\\d[a-zA-Z]\\d{2}|[a-zA-Z]{3}\\d{4}")) {
         this.licensePlate = licensePlate;
      }
      else {
         Scanner scan = new Scanner(System.in);
         String plateTest;

         do {
            System.out.printf("%nInvalid license plate!%n%n");
            System.out.print("License plate: ");
            plateTest = scan.next();

         } while (!plateTest.matches("[a-zA-Z]{3}\\d[a-zA-Z]\\d{2}|[a-zA-Z]{3}\\d{4}"));

         this.licensePlate = plateTest;
      }
   }

   public AffiliationModel getAffiliationModel() {
      return this.affiliationModel;
   }

   public void setAffiliationModel(AffiliationModel affiliationModel) {
      if (affiliationModel instanceof EmployeeAffiliationModel || 
            affiliationModel instanceof ResidentAffiliationModel || 
            affiliationModel instanceof NonResidentAffiliationModel) {
         this.affiliationModel = affiliationModel;
      } 
      else {
         Scanner scan = new Scanner(System.in);
         int affiliationOption;

         do {
            System.out.printf("%nInvalid affiliation!%n%n");
            System.out.printf("Choose an affiliation:%n%s%n%s%n%s%n%s",
                  "[1]Employee", 
                  "[2]Resident", 
                  "[3]Non-resident", 
                  "Affiliation option: ");
            affiliationOption = scan.nextInt();
         } while (affiliationOption != 1 && affiliationOption != 2 && affiliationOption != 3);

         switch (affiliationOption) {
            case 1: affiliationModel = new EmployeeAffiliationModel();
                    break;
            case 2: affiliationModel = new ResidentAffiliationModel();
                    break;
            case 3: affiliationModel = new NonResidentAffiliationModel();
                    break;
            default: throw new IllegalArgumentException("Not an Affiliation");
         }

         this.affiliationModel = affiliationModel;
      }
   }

   public double getPayment() {
      return affiliationModel.payment(getTimeParked() / 60);
   }

   public void setInside(boolean isInside, String licensePlate) {
      var sql = "UPDATE vehicle SET is_inside = ? WHERE license_plate = ?";

      try (var conn = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {

         pstmt.setBoolean(1, isInside);
         pstmt.setString(2, licensePlate);

         pstmt.executeUpdate();
            }
      catch (SQLException e) {
         e.printStackTrace();
      }

      this.isInside = isInside;
   }

   public boolean getIsInside() {
      return isInside;
   }

   public void setEntranceTime(String licensePlate) {
      var sql = "UPDATE vehicle SET entrance = date_trunc('second', NOW()) WHERE license_plate = ?";

      try (var conn = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {

         pstmt.setString(1, licensePlate);
         pstmt.executeUpdate();
      }
      catch (SQLException e) {
         e.printStackTrace();
      }

      this.entranceTime = LocalDateTime.now();
   }

   public String getEntranceTime(String licensePlate) {
      return entranceTime.toString();
   }

   public void setExitTime(String licensePlate) {
      var sql = "UPDATE vehicle SET exit = date_trunc('second', NOW()) WHERE license_plate = ?";

      try (var conn = DB.connect();
            var pstmt = conn.prepareStatement(sql)) {

         pstmt.setString(1, licensePlate);
         pstmt.executeUpdate();
      }
      catch (SQLException e) {
         e.printStackTrace();
      }

      this.exitTime = LocalDateTime.now();
   }

   public String getExitTime(String licensePlate) {
      return exitTime.toString();
   }

   public long setTimeParked() {
      var selectSql = "SELECT entrance, exit FROM vehicle WHERE license_plate = ?";
      var updateSql = "UPDATE vehicle SET time_parked = ? WHERE license_plate = ?";

      try (var conn = DB.connect();
            var selectPstmt = conn.prepareStatement(selectSql);
            var updatePstmt = conn.prepareStatement(updateSql)) {

         selectPstmt.setString(1, licensePlate);
         var rs = selectPstmt.executeQuery();

         if (rs.next()) {
            LocalDateTime entranceTime = rs.getTimestamp("entrance").toLocalDateTime();
            LocalDateTime exitTime = rs.getTimestamp("exit").toLocalDateTime();
            this.timeParked = ChronoUnit.SECONDS.between(entranceTime, exitTime);

            updatePstmt.setLong(1, timeParked);
            updatePstmt.setString(2, licensePlate);
            updatePstmt.executeUpdate();

            return timeParked;
         }
         else {
            System.out.println("No entrance or exit time found for the given license plate.");
            return -1;
         }
      }
      catch (SQLException e) {
         e.printStackTrace();
         return -1;
      }
   }

   public long getTimeParked() {
   // return time parked in seconds
      return timeParked;
   }

   @Override
   public String toString() {
      return String.format("%s %s%n%s %s%n%s %s%n%s %s%n%s %s%n%s %s%n%s %s%n%s %s%n%s %s%n%s %02dh:%02dm%n",
            "Owner:", getOwner(),
            "Phone number:", getCellphone(),
            "Vehicle Type:", getVehicleType(),
            "Brand:", getBrand(),
            "Model:", getModel(),
            "Color:", getColor(),
            "Licence Plate:", getLicensePlate(),
            "Affiliation:", getAffiliationModel(),
            "Is inside:", getIsInside(), 
            //Convert time parked from seconds to hours and minutes
            "Time parked this month:", getTimeParked() / 3600, (getTimeParked() % 3600) / 60);
   }  
}
