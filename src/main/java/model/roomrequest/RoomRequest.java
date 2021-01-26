package model.roomrequest;

import com.jsoniter.any.Any;
import model.RequestData;

public class RoomRequest {
    public enum RequestType {
        REKEY
    }

    private Any arguments;

    public Any getArguments() { return arguments; }
    public void setArguments(Any arguments) { this.arguments = arguments; }

    public static RoomRequest fromReqData(RequestType type, RequestData requestData) {
        RoomRequest hr = new RoomRequest();
        hr.setArguments(Any.wrap(requestData));
        return hr;
    }
}
