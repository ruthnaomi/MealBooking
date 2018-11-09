package ke.co.slab.mealbooking.models;

public class CartModel {
    private String id;
    private String image;
    private String name;
    private String qty;
    private String net_price;
    private String gross_price;

    public CartModel(String id, String image, String name, String qty, String net_price, String gross_price) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.qty = qty;
        this.net_price = net_price;
        this.gross_price = gross_price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getNet_price() {
        return net_price;
    }

    public void setNet_price(String net_price) {
        this.net_price = net_price;
    }

    public String getGross_price() {
        return gross_price;
    }

    public void setGross_price(String gross_price) {
        this.gross_price = gross_price;
    }

    public String getImage() {
        return image;
    }

   // public void setImage(String image) {
        //this.image = image;
    //}
}
