package icu.yeguo.cloudnest.common;

import icu.yeguo.cloudnest.constant.HttpStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@Data
public class Response<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 2159630746807859669L;

    private int code;
    private T data;
    private String message;

    public Response(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(200, data, "success");
    }

    public static <T> Response<T> error(int code, String message) {
        return new Response<>(code,null, message);
    }

    public static <T> Response<T> error(String message) {
        return new Response<>(HttpStatus.InternalServerError,null, message);
    }
}
