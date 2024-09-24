public class Parking {
   String name;
   String cnpj;
   String address;
   String phoneNumber;
   Vehicle[] carParkingSpaces;
   Vehicle[] motorcycleParkingSpaces;

   //é bom usar aquela técnica de ter uma variável final que deve ser 1 ou não pode ser maior que 1 pra impedir que se crie mais de um objeto desta classe

   public Parking(String name, String cnpj, String address, String phoneNumber, int carParkingSpaces, int motorcycleParkingSpaces) {
      this.name = name;
      this.cnpj = cnpj;
      this.address = address;
      this.phoneNumber = phoneNumber;
      this.carParkingSpaces = new Vehicle[carParkingSpaces];
      this.motorcycleParkingSpaces = new Vehicle[motorcycleParkingSpaces];
   }
   public String getName() {
      return name;
   }

   public String getCnpj() {
      return cnpj;
   }

   public String getAddress() {
      return address;
   }

   public String getPhoneNumber() {
      return phoneNumber;
   }

   public int getCarParkingSpaces() {
      return carParkingSpaces.length;
   }

   public int getMotorcycleParkingSpaces() {
      return motorcycleParkingSpaces.length;
   }

   public int getAvailableCarParkingSpaces() {
      int amount = 0;

      for (int i = 0; i < getCarParkingSpaces(); i++) {
         if (carParkingSpaces[i] == null) {
            amount++;
         }
      }

      return amount;
   }

   public int getAvailableMotorcycleParkingSpaces() {
      int amount = 0;

      for (int i = 0; i < getMotorcycleParkingSpaces(); i++) {
         if (motorcycleParkingSpaces[i] == null) {
            amount++;
         }
      }

      return amount;
   }

   @Override
   public String toString() {
      return String.format("%s %s%n%s %s%n%s %s%n%s %s%n%s %d%n%s %d%n",
            "Parking lot name:", getName(),
            "Cnpj:", getCnpj(),
            "Address:", getAddress(),
            "Phone number:", getPhoneNumber(),
            "Car parking spaces:", getCarParkingSpaces(),
            "Motorcycle parking spaces:", getMotorcycleParkingSpaces());
   }
}
