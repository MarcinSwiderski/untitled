package model.roomrequest;

import com.jsoniter.any.Any;
import model.RequestData;

public class RoomRequest {
    public enum RequestType {
        REKEY,
        ENTER,
        EXIT
    }

    private RequestType type;
    private Any arguments;

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }
    public Any getArguments() { return arguments; }
    public void setArguments(Any arguments) { this.arguments = arguments; }

    public static RoomRequest fromReqData(RequestType type, RequestData requestData) {
        RoomRequest hr = new RoomRequest();
        hr.setType(type);
        hr.setArguments(Any.wrap(requestData));
        return hr;
    }
}
