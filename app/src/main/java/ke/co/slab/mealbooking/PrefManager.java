package ke.co.slab.mealbooking;

import android.content.Context;
import android.content.SharedPreferences;

import javax.crypto.spec.DESedeKeySpec;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

        // shared pref mode
    int PRIVATE_MODE = 0;
    // Shared preferences file name
    private static final String PREF_NAME = "meal-booking";
    private static final String SHARE_IMAGE = "image";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String SHARE_USERNAME = "username";
    private static final String SHARE_USER_ID = "user_id";
    private static final String SHARE_GRAND_TOTAL = "total";
    private static final String SHARE_PRODUCT_TITLE = "TITLE";
    private static final String SHARE_PRODUCT_DESC = "DESC";
    private static final String SHARE_PRODUCT_PRICE = "PRICE";
    private static final String SHARE_PRODUCT_ID= "ID";
    private static final String IS_FIRST_TIME_CART= "cart";
    private String image;

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public void setFirstTimeCart(boolean firstTimeCart) {
        editor.putBoolean(IS_FIRST_TIME_CART, firstTimeCart);
        editor.commit();
    }

    public boolean isFirstTimeCart() {
        return pref.getBoolean(IS_FIRST_TIME_CART, true);
    }

    public boolean isFirstTimeLaunch() { return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true); }

    // add username session to pref

    public void setUsername(String username)
    {
        editor.putString(SHARE_USERNAME,username);
        editor.commit();
    }

    public String getUsername(){
        return pref.getString(SHARE_USERNAME,null);
    }

    public void setRole(String role)
    {
        editor.putString("ROLE",role);
        editor.commit();
    }

    public String getRole(){
        return pref.getString("ROLE",null);
    }

    public void setDeviceName(String deviceName){
        editor.putString("DEVICE_NAME", deviceName);
        editor.commit();
    }

   // public void setSerialNo(String serialNo){
      //  editor.putString("SERIAL_NO", serialNo);
      //  editor.commit();
   // }

   // public String getDeviceName(){
      //  return pref.getString("DEVICE_NAME",null);
   // }

  //  public String getSerialNo(){
        //return pref.getString("SERIAL_NO",null);
   // }

  /*  public void setUserId(int id)
    {
        editor.putInt(SHARE_USER_ID,id);
        editor.commit();
    }*/

  //  public int getUserId()
   // {
      //  return pref.getInt(SHARE_USER_ID,0);
    //}

    public void setTotal(String total){
        editor.putString(SHARE_GRAND_TOTAL,total);
        editor.commit();
    }

    public String getToal()
    {
        return pref.getString(SHARE_GRAND_TOTAL,"0.00");
    }

    public void setTitle(String title){
        editor.putString(SHARE_PRODUCT_TITLE,title);
        editor.commit();
    }
    public String getTitle()
    {
        return pref.getString(SHARE_PRODUCT_TITLE,"Empty");
    }
   /* public void setDesc(String desc){
        editor.putString(SHARE_PRODUCT_DESC,desc);
        editor.commit();
    }
    public String getDesc()
    {
        return pref.getString(SHARE_PRODUCT_DESC,"Empty");
    }*/
    public void setPrice(String price){
        editor.putString(SHARE_PRODUCT_PRICE,price);
        editor.commit();
    }
    public String getPrice()
    {
        return pref.getString(SHARE_PRODUCT_PRICE,"0.00");
    }
    public void setId(String id){
        editor.putString(SHARE_PRODUCT_ID,id);
        editor.commit();
    }
    public String getId()
    {
        return pref.getString(SHARE_PRODUCT_ID,"0");
    }

    public void setImage(String image) {
        editor.putString(SHARE_IMAGE,image);
        editor.commit();

    }

    public String getImage()
    {
        return pref.getString(SHARE_IMAGE,"null");
    }
}
