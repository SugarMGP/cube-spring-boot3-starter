package org.jh.cube;

import lombok.Getter;

/**
 * @author SugarMGP
 */
@Getter
public class CubeException extends RuntimeException {
    private final int code;
    private final String message;

    public CubeException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
