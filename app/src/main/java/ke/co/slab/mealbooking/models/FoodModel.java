package ke.co.slab.mealbooking.models;

public class FoodModel {
    private String id;
    private String image;
    private String name;
    private String price;
    private String qty;

    public FoodModel(String id, String image, String name, String price, String qty) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.qty = qty;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }
}
