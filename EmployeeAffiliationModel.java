public class EmployeeAffiliationModel implements AffiliationModel{

   @Override
   public double payment(long minutes) {
      double tax = 0.0;
      return tax * minutes;
   }

   @Override
   public String toString() {
      return "Employee";
   }
}
