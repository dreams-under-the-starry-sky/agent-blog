package com.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorBody {
    private String code;
    private String message;

    public static ErrorBody of(ErrorCode errorCode) {
        return new ErrorBody(errorCode.getCode(), errorCode.getMessage());
    }
}
