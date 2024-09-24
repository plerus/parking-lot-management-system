public class NonResidentAffiliationModel implements AffiliationModel {

   @Override
   public double payment(long minutes) {
      double tax = 0.5;
      return tax * minutes;
   }

   @Override
   public String toString() {
      return "Non_resident";
   }
}
