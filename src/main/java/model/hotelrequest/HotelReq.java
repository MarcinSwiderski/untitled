package model.hotelrequest;

import com.jsoniter.any.Any;
import model.Request;

public class HotelReq {
    public enum RequestType {
        ROOM_CREATED,
        ROOM_REMOVED,
        TERMINAL_RESERVE_ROOM,
        TERMINAL_END_RESERVATION,
    }

    private RequestType type;
    private Any arguments;

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }
    public Any getArguments() { return arguments; }
    public void setArguments(Any arguments) { this.arguments = arguments; }

    public static HotelReq fromReqData(RequestType type, Request request) {
        HotelReq hr = new HotelReq();
        hr.setType(type);
        hr.setArguments(Any.wrap(request));
        return hr;
    }
}
