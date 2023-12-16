package ddwu.mobile.finalproject.ma01_20181801;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;


public class PlaceXmlParser {

    private XmlPullParser parser;

    public PlaceXmlParser() {
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Place> parse(String xml) {
        ArrayList<Place> resultList = new ArrayList<>();
        Place dto = null;
        String tagType = "";

        try {
            parser.setInput(new StringReader(xml));
            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if (tag.equals("item")) {
                            dto = new Place();
                        } else if (tag.equals("addr1") && (dto != null)) {
                            tagType = "addr1";
                        } else if (tag.equals("addr2") && (dto != null)) {
                            tagType = "addr2";
                        } else if (tag.equals("firstimage") && (dto != null)) {
                            tagType = "firstimage";
                        } else if (tag.equals("firstimage2") && (dto != null)) {
                            tagType = "firstimage2";
                        } else if (tag.equals("mapx") && (dto != null)) {
                            tagType = "mapx";
                        } else if (tag.equals("mapy") && (dto != null)) {
                            tagType = "mapy";
                        } else if (tag.equals("sigungucode") && (dto != null)) {
                            tagType = "sigungucode";
                        } else if (tag.equals("title") && (dto != null)) {
                            tagType = "title";
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("item")) {
                            resultList.add(dto);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        switch (tagType) {
                            case "addr1":
                                dto.setAddr1(parser.getText());
                                break;
                            case "addr2":
                                dto.setAddr2(parser.getText());
                                break;
                            case "firstimage":
                                if (parser.getText().contains("http")) {
                                    dto.setFirstimage(parser.getText());
                                } else
                                    dto.setFirstimage("http://www.billking.co.kr/index/skin/board/basic_support/img/noimage.gif");
                                break;
                            case "firstimage2":
                                dto.setFirstimage2(parser.getText());
                                break;
                            case "mapx":
                                dto.setMapx(parser.getText());
                                break;
                            case "mapy":
                                dto.setMapy(parser.getText());
                                break;
                            case "sigungucode":
                                dto.setSigungucode(parser.getText());
                                break;
                            case "title":
                                dto.setTitle(parser.getText());
                                break;
                        }
                        tagType = "";
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
