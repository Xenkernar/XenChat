package Server;

import org.json.JSONObject;
@FunctionalInterface
public interface RequestSolution{
    public abstract JSONObject slove(JSONObject request);
}
