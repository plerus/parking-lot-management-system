public class ResidentAffiliationModel implements AffiliationModel{

   @Override
   public double payment(long minutes) {
      double tax = 0.05;
      return tax * minutes;
   }

   @Override
   public String toString() {
      return "Resident";
   }
}
