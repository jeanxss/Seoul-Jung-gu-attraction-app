package ddwu.mobile.finalproject.ma01_20181801;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "place_table")
public class Place implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int _id;
    private String addr1;
    private String addr2;
    private String firstimage;
    private String firstimage2;
    private String mapx;
    private String mapy;
    private String sigungucode;
    private String title;
    private String imageFileName;       // 외부저장소에 저장했을 때의 파일명

    public Place() {
        this.imageFileName = null;      // 생성 시에는 외부저장소에 파일이 없으므로 null로 초기화
    }

    public Place(String addr1, String addr2, String firstimage, String firstimage2, String mapx, String mapy, String sigungucode, String title) {
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.firstimage = firstimage;
        this.firstimage2 = firstimage2;
        this.mapx = mapx;
        this.mapy = mapy;
        this.sigungucode = sigungucode;
        this.title = title;
        this.imageFileName = null;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }

    public String getFirstimage() {
        return firstimage;
    }

    public void setFirstimage(String firstimage) {
        this.firstimage = firstimage;
    }

    public String getFirstimage2() {
        return firstimage2;
    }

    public void setFirstimage2(String firstimage2) {
        this.firstimage2 = firstimage2;
    }

    public String getMapx() {
        return mapx;
    }

    public void setMapx(String mapx) {
        this.mapx = mapx;
    }

    public String getMapy() {
        return mapy;
    }

    public void setMapy(String mapy) {
        this.mapy = mapy;
    }

    public String getSigungucode() {
        return sigungucode;
    }

    public void setSigungucode(String sigungucode) {
        this.sigungucode = sigungucode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    @Override
    public String toString() {
        return "PlaceDTO{" +
                "_id='" + _id + '\'' +
                ", addr1='" + addr1 + '\'' +
                ", addr2='" + addr2 + '\'' +
                ", firstimage='" + firstimage + '\'' +
                ", firstimage2='" + firstimage2 + '\'' +
                ", mapx='" + mapx + '\'' +
                ", mapy='" + mapy + '\'' +
                ", sigungucode='" + sigungucode + '\'' +
                ", title='" + title + '\'' +
                ", imageFileName='" + imageFileName + '\'' +
                '}';
    }
}
