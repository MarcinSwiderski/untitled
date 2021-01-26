package model.hotelrequest;

import com.jsoniter.any.Any;
import model.RequestData;

public class HotelRequest {
    public enum RequestType {
        // from Room
        ROOM_REGISTER,
        ROOM_UNREGISTER,
        UPDATE_ROOM_STATUS,

        // from Terminal
        BOOK_ROOM,
        END_STAY,
    }

    private RequestType type;
    private Any arguments;

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }
    public Any getArguments() { return arguments; }
    public void setArguments(Any arguments) { this.arguments = arguments; }

    public static HotelRequest fromReqData(RequestType type, RequestData requestData) {
        HotelRequest hr = new HotelRequest();
        hr.setType(type);
        hr.setArguments(Any.wrap(requestData));
        return hr;
    }
}
