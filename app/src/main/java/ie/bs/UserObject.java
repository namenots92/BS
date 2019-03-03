package ie.bs;

public class UserObject {
    String  id,
            name,
            image,
            email;

    boolean receive;

    public UserObject(){
        this.id = null;
        this.name = null;
        this.image = null;
        this.email = null;
        this.receive = false;
    }
    public UserObject(String id, String name, String image, String email, boolean receive){
        this.id = id;
        this.name = name;
        this.image = image;
        this.email = email;
        this.receive = receive;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getImage() {
        return image;
    }
    public String getEmail() {
        return email;
    }
    public Boolean getReceive(){
        return receive;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setReceive(Boolean receive){
        this.receive = receive;
    }

    @Override
    public boolean equals(Object obj) {
        boolean same = false;
        if(obj != null && obj instanceof UserObject){
            same = this.id.equals (((UserObject) obj).getId());
        }
        return same;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (this.id == null ? 0 : this.id.hashCode());
        return result;
    }
}