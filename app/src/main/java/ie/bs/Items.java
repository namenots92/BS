package ie.bs;

public class Items {

    private String name;
    private int price;
    private int thumbnail;

    public Items(){

    }

    public Items(String name, int price, int thumbnail){
        this.name = name;
        this.price = price;
        this.thumbnail = thumbnail;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriceOfItems() {
        return price;
    }

    public void setPriceOfItems(int price) {
        this.price = price;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}
