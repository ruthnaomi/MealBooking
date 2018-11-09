package ke.co.slab.mealbooking;

public class Apis {
    public static final String URL = "http://meal.shulemall.com/api/";
    public static final String ASSET_URL = "http://meal.shulemall.com/image/meals/";
    public static final String MEALS = URL + "meals";
    public static final String LOGIN = URL + "login";
    public static final String CART = URL + "cart";
    public static final String BOOKING = URL + "booking";
    public static final String LOGOUT = URL + "logout";

   // public static final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    public static final String textPattern = "[a-zA-Z ]+";
  //  public static final String number = "[0-9]+";

    // MPESA KEYS

    public static final String app_key = "oHHh8Ua6iyi4sVduay0saOjWkHebAJmY";
    public static final String app_secret = "VbJsaZDVT9bA5EiW";
    public static final String till_number = "174379";
    public static final String call_back_url = URL + "callback";
}