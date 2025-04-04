package vn.iotstar.UploadFile;

public class ImageUpload {
    private int id;
    private String avatar;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ImageUpload(int id, String avatar) {
        this.id = id;
        this.avatar = avatar;
    }
}
