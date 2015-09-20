package dao;

import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.function.Consumer;

public class AbstractDao {

    protected void processResult(AsyncResult<String> lookup, Consumer<String> successHandler, Consumer<Throwable> errorHandler) {
        if (lookup.failed()) {
            errorHandler.accept(lookup.cause());
        } else {
            successHandler.accept(lookup.result());
        }
    }

    protected void processListResult(AsyncResult<List<JsonObject>> lookup, Consumer<List<JsonObject>> successHandler, Consumer<Throwable> errorHandler) {
        if (lookup.failed()) {
            errorHandler.accept(lookup.cause());
        } else {
            successHandler.accept(lookup.result());
        }
    }
}
